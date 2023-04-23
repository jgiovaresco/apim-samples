package io.apim.samples.ports.grpc

import io.grpc.examples.routeguide.Point
import io.grpc.examples.routeguide.Rectangle
import io.grpc.examples.routeguide.RouteGuide
import io.grpc.examples.routeguide.RouteNote
import io.quarkus.grpc.GrpcClient
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*

@QuarkusTest
class RouteGuideGrpcServiceTest {

  @GrpcClient("routeGuide")
  lateinit var routeGuide: RouteGuide

  @Nested
  inner class GetFeature {

    @Test
    fun `should return the feature if the provided point match`() {
      val message = Point.newBuilder().setLatitude(407838351).setLongitude(-746143763).build()

      val feature = routeGuide.getFeature(message)
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(feature) {
        get { feature.name }.isEqualTo("Patriots Path, Mendham, NJ 07945, USA")
        get { feature.location }.isEqualTo(message)
      }
    }

    @Test
    fun `should return a nameless feature if the provided point doesn't match`() {
      val message = Point.newBuilder().setLatitude(1).setLongitude(2).build()

      val feature = routeGuide.getFeature(message)
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(feature) {
        get { feature.name }.isEmpty()
        get { feature.location }.isEqualTo(message)
      }
    }
  }

  @Nested
  inner class ListFeatures {
    @Test
    fun `should return all the features in the provided rectangle`() {
      val message = Rectangle.newBuilder()
        .setHi(Point.newBuilder().setLatitude(406500000).setLongitude(-745000000).build())
        .setLo(Point.newBuilder().setLatitude(402300000).setLongitude(-747900000).build())
        .build()

      val features = routeGuide.listFeatures(message).collect().asList()
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(features) {
        hasSize(2)

        and {
          map { it.name }.containsExactly(
            "1 Merck Access Road, Whitehouse Station, NJ 08889, USA",
            "330 Evelyn Avenue, Hamilton Township, NJ 08619, USA"
          )
        }
      }
    }
  }

  @Nested
  inner class RecordRoute {
    @Test
    fun `should send all routes and return a summary`() {
      val request = Multi.createFrom().items(
        Point.newBuilder().setLatitude(406337092).setLongitude(-740122226).build(),
        Point.newBuilder().setLatitude(406421967).setLongitude(-747727624).build(),
      )

      val summary = routeGuide.recordRoute(request)
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(summary) {
        get { pointCount }.isEqualTo(2)
        get { featureCount }.isEqualTo(2)
        get { distance }.isEqualTo(64180)
        get { elapsedTime }.isGreaterThanOrEqualTo(0)
      }
    }
  }

  @Nested
  inner class RouteChat {
    @Test
    fun `should send all routes and return a summary`() {

      val request = Multi.createFrom().items(
        RouteNote.newBuilder().setLocation(Point.newBuilder().setLatitude(0).setLongitude(0).build()).setMessage("Note 1").build(),
        RouteNote.newBuilder().setLocation(Point.newBuilder().setLatitude(0).setLongitude(0).build()).setMessage("Note 2").build(),
        RouteNote.newBuilder().setLocation(Point.newBuilder().setLatitude(0).setLongitude(0).build()).setMessage("Note 3").build(),
      )

      val notes = routeGuide.routeChat(request)
        .collect().asList()
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(notes) {
        hasSize(3)
        and {
          map { it.message }.containsExactly(
            "Note 1",
            "Note 1",
            "Note 2"
          )
        }
      }

    }
  }
}
