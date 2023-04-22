package io.apim.samples.core

import io.vertx.core.MultiMap
import io.vertx.ext.web.impl.ParsableMIMEValue

/** Transform a MultiMap into a simple map. Multiple values are joined in a string separated with ; */
fun MultiMap.toSimpleMap() = this.entries()
  .groupBy { it.key.lowercase() }
  .mapValues { it.value.joinToString(";") { h -> h.value } }

fun ParsableMIMEValue.isText(): Boolean {
  return this.component() == "text"
}

fun ParsableMIMEValue.isJson(): Boolean {
  return this.component() == "application" && this.subComponent().contains("json")
}
