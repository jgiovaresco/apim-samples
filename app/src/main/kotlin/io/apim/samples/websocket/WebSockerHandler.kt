package io.apim.samples.websocket

import io.reactivex.rxjava3.core.Completable

interface WebSockerHandler {
  fun handle(): Completable
}
