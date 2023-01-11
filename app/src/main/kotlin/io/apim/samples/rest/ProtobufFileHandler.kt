package io.apim.samples.rest

import io.reactivex.rxjava3.core.Completable
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.ext.web.RoutingContext
import java.io.FileNotFoundException

fun protobufFileHandler(ctx: RoutingContext) {
  listFilesOrSendProtoFileContent(ctx).subscribe()
}

fun listFilesOrSendProtoFileContent(route: RoutingContext): Completable {
  val req = route.request()
  val res = route.response()

  if (req.path().equals("/grpc")) {
    val directory = ClassLoader.getSystemResource("grpc") ?: return res.setStatusCode(404).end()

    return route.vertx().fileSystem().readDir(directory.file, ".*\\.proto\$")
      .map { files ->
        json {
          obj("protoFiles" to array(files.map { it.replace(directory.file, req.absoluteURI()) }))
        }
      }
      .flatMapCompletable {
        res.putHeader("Content-Type", "application/json")
          .end(it.toString())
      }
  }

  if(req.path().endsWith("proto")) {
    return res.sendFile(req.path().drop(1))
      .onErrorResumeNext { th ->
        when(th) {
          is FileNotFoundException -> res.setStatusCode(404).end()
          else -> res.setStatusCode(500).end()
        }
      }
  }

  return res.setStatusCode(404).end()

}
