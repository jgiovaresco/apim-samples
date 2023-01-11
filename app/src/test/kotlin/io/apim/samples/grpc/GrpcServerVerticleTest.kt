package io.apim.samples.grpc

import io.grpc.examples.routeguide.Feature
import io.grpc.examples.routeguide.RouteGuideGrpc
import io.grpc.examples.routeguide.RouteNote
import io.grpc.examples.routeguide.point
import io.grpc.examples.routeguide.rectangle
import io.grpc.examples.routeguide.routeNote
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.vertx.core.Promise
import io.vertx.core.net.SocketAddress
import io.vertx.grpc.client.GrpcClient
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.rxjava3.config.ConfigRetriever
import io.vertx.rxjava3.core.Vertx
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.map

@ExtendWith(VertxExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GrpcServerVerticleTest {
  private val vertx: Vertx = Vertx.vertx()
  private val configRetriever: ConfigRetriever = ConfigRetriever.create(vertx)

  lateinit var client: GrpcClient
  lateinit var server: SocketAddress

  @BeforeAll
  fun setUp(testContext: VertxTestContext) {
    vertx.deployVerticle(GrpcServerVerticle(configRetriever))
      .subscribeBy { testContext.completeNow() }

    client = GrpcClient.client(vertx.delegate)
    server = SocketAddress.inetSocketAddress(GrpcServerVerticle.DEFAULT_PORT, "localhost")
  }

  @AfterAll
  fun tearDown(testContext: VertxTestContext) {
    vertx.close()
      .subscribeBy { testContext.completeNow() }
  }

  @Nested
  @ExtendWith(VertxExtension::class)
  inner class GetFeature {
    private val method = RouteGuideGrpc.getGetFeatureMethod()

    @Test
    fun `should return the feature if the provided point match`(context: VertxTestContext) {
      val message = point {
        latitude = 407838351
        longitude = -746143763
      }

      client.request(server, method).onSuccess { request ->
        request.end(message)

        request.response().onSuccess { response ->
          response.last().onSuccess { feature ->
            expectThat(feature) {
              get { feature.name }.isEqualTo("Patriots Path, Mendham, NJ 07945, USA")
              get { feature.location }.isEqualTo(message)
            }

            context.completeNow()
          }

        }
      }
    }

    @Test
    fun `should return a nameless feature if the provided point doesn't match`(context: VertxTestContext) {
      val message = point {
        latitude = 1
        longitude = 2
      }

      client.request(server, method).onSuccess { request ->
        request.end(message)

        request.response().onSuccess { response ->
          response.last().onSuccess { feature ->
            expectThat(feature) {
              get { feature.name }.isEmpty()
              get { feature.location }.isEqualTo(message)
            }

            context.completeNow()
          }

        }
      }
    }
  }

  @Nested
  @ExtendWith(VertxExtension::class)
  inner class ListFeatures {
    private val method = RouteGuideGrpc.getListFeaturesMethod()

    @Test
    fun `should return all the features in the provided rectangle`(context: VertxTestContext) {
      val message = rectangle {
        hi = point {
          latitude = 406500000
          longitude = -745000000
        }
        lo = point {
          latitude = 402300000
          longitude = -747900000
        }
      }

      client.request(server, method)
        .compose { request ->
          request.end(message)
          request.response()
        }
        .compose { response ->
          val promise = Promise.promise<List<Feature>>()

          val features = mutableListOf<Feature>()
          response.handler { features.add(it) }
          response.endHandler { promise.complete(features) }
          response.exceptionHandler { promise.fail(it) }

          promise.future()
        }
        .onComplete(context.succeeding { features ->
          expectThat(features) {
            hasSize(2)

            and {
              map { it.name }.containsExactly(
                "1 Merck Access Road, Whitehouse Station, NJ 08889, USA",
                "330 Evelyn Avenue, Hamilton Township, NJ 08619, USA"
              )
            }
          }

          context.completeNow()
        })
    }
  }

  @Nested
  @ExtendWith(VertxExtension::class)
  inner class RecordRoute {
    private val method = RouteGuideGrpc.getRecordRouteMethod()

    @Test
    fun `should send all routes and return a summary`(context: VertxTestContext) {
      client.request(server, method)
        .compose { request ->
          listOf(
            point {
              latitude = 406337092
              longitude = -740122226
            },
            point {
              latitude = 406421967
              longitude = -747727624
            },
          )
            .forEach {
              request.write(it)
            }
          request.end()
          request.response()
        }
        .compose { it.last() }
        .onComplete(context.succeeding { summary ->
          expectThat(summary) {
            get { pointCount }.isEqualTo(2)
            get { featureCount }.isEqualTo(2)
            get { distance }.isEqualTo(64180)
            get { elapsedTime }.isGreaterThanOrEqualTo(0)
          }

          context.completeNow()
        })
    }
  }

  @Nested
  @ExtendWith(VertxExtension::class)
  inner class RouteChat {
    private val method = RouteGuideGrpc.getRouteChatMethod()

    @Test
    fun `should send all routes and return a summary`(context: VertxTestContext) {

      client.request(server, method)
        .compose { request ->
          request.write(routeNote {
            location = point {
              latitude = 0
              longitude = 0
            }
            message = "Note 1"
          })
          request.write(routeNote {
            location = point {
              latitude = 0
              longitude = 0
            }
            message = "Note 2"
          })
          request.write(routeNote {
            location = point {
              latitude = 0
              longitude = 0
            }
            message = "Note 3"
          })
          request.end()
          request.response()
        }
        .compose { response ->
          val promise = Promise.promise<List<RouteNote>>()

          val features = mutableListOf<RouteNote>()
          response.handler { features.add(it) }
          response.endHandler { promise.complete(features) }
          response.exceptionHandler { promise.fail(it) }

          promise.future()
        }

        .onComplete(context.succeeding { notes ->
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

          context.completeNow()
        })
    }
  }
}
