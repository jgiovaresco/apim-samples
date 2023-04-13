package io.apim.samples.rest

import io.vertx.ext.web.impl.ParsableMIMEValue
import io.vertx.rxjava3.core.MultiMap

/** Transform a MultiMap into a simple map. Multiple values are joined in a string separated with ; */
fun MultiMap.toSimpleMap() = this.entries()
  .groupBy { it.key }
  .mapValues { it.value.joinToString(";") { h -> h.value } }

fun ParsableMIMEValue.isText(): Boolean {
  return this.component() == "text"
}

fun ParsableMIMEValue.isJson(): Boolean {
  return this.component() == "application" && this.subComponent().contains("json")
}

fun ParsableMIMEValue.isAvro(): Boolean {
  return this.component() == "avro" || this.subComponent().contains("avro")
}
