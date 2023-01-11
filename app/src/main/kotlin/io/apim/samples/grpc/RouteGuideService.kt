package io.apim.samples.grpc

import io.grpc.examples.routeguide.Feature
import io.grpc.examples.routeguide.Point
import io.grpc.examples.routeguide.Rectangle
import io.grpc.examples.routeguide.RouteNote
import io.grpc.examples.routeguide.RouteSummary
import io.grpc.examples.routeguide.VertxRouteGuideGrpc
import io.grpc.examples.routeguide.feature
import io.grpc.examples.routeguide.point
import io.grpc.examples.routeguide.routeSummary
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import io.vertx.core.streams.ReadStream
import io.vertx.core.streams.WriteStream
import org.slf4j.LoggerFactory
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit.NANOSECONDS
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class RouteGuideService(private val features: List<Feature>) : VertxRouteGuideGrpc.RouteGuideVertxImplBase() {
  private val logger = LoggerFactory.getLogger(javaClass)
  private val routeNotes: ConcurrentMap<Point, MutableList<RouteNote>> = ConcurrentHashMap()

  /**
   * Obtains the feature at a given position.
   *
   * @param location the location to check.
   * @return The feature object at the point. Note that an empty name indicates no feature.
   */
  override fun getFeature(location: Point): Future<Feature> {
    val found =
      features.find { it.filterByLocation(location) }
        ?: feature {
          name = ""
          this.location = point {
            latitude = location.latitude
            longitude = location.longitude
          }
        }

    return Future.succeededFuture(found)
  }

  /**
   * Obtains the Features available within the given Rectangle.
   *
   * Results are streamed rather than returned at once (e.g. in a response message with a repeated field), as the
   * rectangle may cover a large area and contain a huge number of features.
   */
  override fun listFeatures(request: Rectangle, response: WriteStream<Feature>) {
    val left = min(request.lo.longitude, request.hi.longitude)
    val right = max(request.lo.longitude, request.hi.longitude)
    val top = max(request.lo.latitude, request.hi.latitude)
    val bottom = min(request.lo.latitude, request.hi.latitude)

    features.filter { it.name.isNotBlank() }
      .filter { feature ->
        val lat = feature.location.latitude
        val lon = feature.location.longitude

        lon in left..right && lat >= bottom && lat <= top
      }
      .forEach { response.write(it) }

    response.end()
  }

  /**
   * Accepts a stream of Points on a route being traversed, returning a RouteSummary when traversal is completed.
   */
  override fun recordRoute(request: ReadStream<Point>): Future<RouteSummary> {
    val response = Promise.promise<RouteSummary>()

    request.exceptionHandler {
      logger.error("Fail to process recordRoute request", it)
      response.fail(it)
    }

    val routerRecorder = RouteRecorder(features)
    request.handler(routerRecorder::append)
    request.endHandler { response.complete(routerRecorder.buildSummary()) }

    return response.future()
  }

  /**
   * Accepts a stream of RouteNotes sent while a route is being traversed, while receiving other RouteNotes
   * (e.g. from other users).
   */
  override fun routeChat(request: ReadStream<RouteNote>, response: WriteStream<RouteNote>) {
   request.handler { note ->
     val locationNotes = getOrCreateNotes(note.location)

     locationNotes.forEach { response.write(it) }

     locationNotes.add(note)
   }

    request.exceptionHandler {
      logger.error("routeChat cancelled", it)
      response.end()
    }

    request.endHandler { response.end() }
  }

  /**
   * Get the notes list for the given location. If missing, create it.
   */
  private fun getOrCreateNotes(location: Point): MutableList<RouteNote> {
    val notes: MutableList<RouteNote> = Collections.synchronizedList(ArrayList())
    return routeNotes.putIfAbsent(location, notes) ?: notes
  }
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
    val time = NANOSECONDS.toSeconds(System.nanoTime() - startTime)
    return routeSummary {
      pointCount = pointsCount
      featureCount = featuresCount
      distance = this@RouteRecorder.distance
      elapsedTime = time.toInt()
    }
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

fun JsonObject.toPoint(): Point = point {
  latitude = getInteger("latitude")
  longitude = getInteger("longitude")
}
fun JsonObject.toFeature(): Feature = feature {
  name = getString("name")
  location = getJsonObject("location").toPoint()
}
