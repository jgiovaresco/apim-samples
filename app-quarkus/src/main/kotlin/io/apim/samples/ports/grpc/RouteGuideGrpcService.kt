package io.apim.samples.ports.grpc

import io.grpc.examples.routeguide.*
import io.quarkus.grpc.GrpcService
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import io.vertx.core.json.JsonObject
import io.vertx.mutiny.core.Vertx
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import kotlin.math.*

@GrpcService
class RouteGuideGrpcService(private val vertx: Vertx) : RouteGuide {
  private var features: List<Feature> = emptyList()
  private val routeNotes: ConcurrentMap<Point, MutableList<RouteNote>> = ConcurrentHashMap()

  /**
   * Obtains the feature at a given position.
   *
   * @param location the location to check.
   * @return The feature object at the point. Note that an empty name indicates no feature.
   */
  override fun getFeature(location: Point): Uni<Feature> {
    return features().filter { it.filterByLocation(location) }
      .collect().first()
      .onItem().ifNull().continueWith {
        Feature.newBuilder()
          .setName("")
          .setLocation(location)
          .build()
      }
  }

  /**
   * Obtains the Features available within the given Rectangle.
   *
   * Results are streamed rather than returned at once (e.g. in a response message with a repeated field), as the
   * rectangle may cover a large area and contain a huge number of features.
   */
  override fun listFeatures(request: Rectangle): Multi<Feature> {
    val left = min(request.lo.longitude, request.hi.longitude)
    val right = max(request.lo.longitude, request.hi.longitude)
    val top = max(request.lo.latitude, request.hi.latitude)
    val bottom = min(request.lo.latitude, request.hi.latitude)

    return features()
      .filter { it.name.isNotBlank() }
      .filter { feature ->
        val lat = feature.location.latitude
        val lon = feature.location.longitude

        lon in left..right && lat >= bottom && lat <= top
      }
  }

  /**
   * Accepts a stream of Points on a route being traversed, returning a RouteSummary when traversal is completed.
   */
  override fun recordRoute(request: Multi<Point>): Uni<RouteSummary> {
    val recorder = RouteRecorder(features)
    return request
      .onItem().invoke { point -> recorder.append(point) }
      .collect().last()
      .map { recorder.buildSummary() }
  }


  /**
   * Accepts a stream of RouteNotes sent while a route is being traversed, while receiving other RouteNotes
   * (e.g. from other users).
   */
  override fun routeChat(request: Multi<RouteNote>): Multi<RouteNote> {
    return request
      .onItem().transformToMultiAndConcatenate {
        val notes = getOrCreateNotes(it.location)

        Multi.createFrom().items(notes.stream())
          .onCompletion().invoke { notes.add(it) }
      }
  }


  /**
   * Get the notes list for the given location. If missing, create it.
   */
  private fun getOrCreateNotes(location: Point): MutableList<RouteNote> {
    val notes: MutableList<RouteNote> = Collections.synchronizedList(ArrayList())
    return routeNotes.putIfAbsent(location, notes) ?: notes
  }

  private fun features() = Multi.createFrom().items(features.stream())
    .onCompletion().ifEmpty().switchTo(
      vertx.fileSystem().readFile("grpc/route_guide.json")
        .onItem().transform { json ->
          JsonObject(json.toString())
            .getJsonArray("features")
            .map { f -> (f as JsonObject).toFeature() }
        }
        .onItem().invoke { list -> this.features = list }
        .onItem().transformToMulti { Multi.createFrom().items(it.stream()) }
    )
}

class RouteRecorder(private val features: List<Feature>) {
  private var pointsCount = 0
  private var featuresCount = 0
  private var distance = 0
  private var previousPoint: Point? = null
  private val startTime = System.nanoTime()

  fun append(nextPoint: Point) {
    pointsCount++

    if (features.find { it.filterByLocation(nextPoint) } != null) {
      featuresCount++
    }

    if (previousPoint != null) {
      distance += calcDistance(previousPoint!!, nextPoint)
    }
    previousPoint = nextPoint
  }

  fun buildSummary(): RouteSummary {
    val time = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime)

    return RouteSummary.newBuilder()
      .setPointCount(pointsCount)
      .setFeatureCount(featuresCount)
      .setDistance(distance)
      .setElapsedTime(time.toInt())
      .build()
  }

  /**
   * Calculate the distance between two points using the "haversine" formula.
   * This code was taken from http://www.movable-type.co.uk/scripts/latlong.html.
   *
   * @param start The starting point
   * @param end The end point
   * @return The distance between the points in meters
   */
  private fun calcDistance(start: Point, end: Point): Int {
    val lat1: Double = start.decimalLatitude()
    val lat2: Double = end.decimalLatitude()
    val lon1: Double = start.decimalLongitude()
    val lon2: Double = end.decimalLongitude()
    val r = 6371000 // Earth radius in meters
    val phi1 = Math.toRadians(lat1)
    val phi2 = Math.toRadians(lat2)
    val deltaPhi = Math.toRadians(lat2 - lat1)
    val deltaLambda = Math.toRadians(lon2 - lon1)
    val a = (sin(deltaPhi / 2) * sin(deltaPhi / 2)
      + cos(phi1) * cos(phi2) * sin(deltaLambda / 2) * sin(deltaLambda / 2))
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return (r * c).toInt()
  }
}

fun Feature.filterByLocation(point: Point): Boolean =
  this.location.latitude == point.latitude && this.location.longitude == point.longitude

private const val COORDINATE_FACTOR = 1e7
fun Point.decimalLatitude() = latitude / COORDINATE_FACTOR
fun Point.decimalLongitude() = longitude / COORDINATE_FACTOR

fun JsonObject.toPoint(): Point = Point.newBuilder()
  .setLatitude(getInteger("latitude"))
  .setLongitude(getInteger("longitude"))
  .build()

fun JsonObject.toFeature(): Feature = Feature.newBuilder()
  .setName(getString("name"))
  .setLocation(getJsonObject("location").toPoint())
  .build()
