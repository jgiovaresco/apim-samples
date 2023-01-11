package io.apim.samples.grpc

import io.apim.samples.grpcPort
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.vertx.core.json.JsonObject
import io.vertx.grpc.server.GrpcServer
import io.vertx.grpc.server.GrpcServiceBridge
import io.vertx.rxjava3.config.ConfigRetriever
import io.vertx.rxjava3.core.AbstractVerticle
import org.slf4j.LoggerFactory

class GrpcServerVerticle(private val configRetriever: ConfigRetriever) : AbstractVerticle() {
  companion object {
    const val DEFAULT_PORT = 8892
  }

  private val logger = LoggerFactory.getLogger(javaClass)

  override fun rxStart(): Completable {
    return Single.zip(configRetriever.config, grpcServer()) { config, grpcServer -> Pair(config, grpcServer) }
      .flatMap {

        val (config, grpcServer) = it
        Single.fromCompletionStage(
          vertx.delegate.createHttpServer()
            .requestHandler(grpcServer)
            .listen(config.getInteger(grpcPort, DEFAULT_PORT))
            .toCompletionStage()
        )
      }
      .doOnError { logger.error("Fail to start $javaClass", it) }
      .doOnSuccess { logger.info("GRPC server started on port ${it.actualPort()}") }
      .ignoreElement()
  }

  private fun grpcServer(): Single<GrpcServer> = vertx.fileSystem().readFile("grpc/route_guide.json")
    .map {
      val features = JsonObject(it.toString())
        .getJsonArray("feature")
        .map { f -> (f as JsonObject).toFeature() }

      val server = GrpcServer.server(vertx.delegate)
      GrpcServiceBridge.bridge(RouteGuideService(features)).bind(server)
      server
    }
}
