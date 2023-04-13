package io.apim.samples.rest.avro

import io.apim.samples.avro.SerializationFormat
import io.apim.samples.rest.sendError
import io.vertx.rxjava3.ext.web.RoutingContext
import org.apache.avro.Schema
import org.apache.avro.SchemaParseException
import java.util.*

fun RoutingContext.getOutputFormatFromQueryParam(param: String = "output"): OutputFormat {
  val output = queryParam(param).elementAtOrNull(0) ?: "avro"
  try {
    return OutputFormat.valueOf(output.uppercase(Locale.getDefault()))
  } catch (e: IllegalArgumentException) {
    sendError(400, "Invalid $param format", "Valid values are: ${OutputFormat.values().joinToString(", ") { it.name.lowercase() }}")
    throw e
  }
}

fun RoutingContext.getSerializationFormatFromQueryParam(param: String = "format"): SerializationFormat {
  val format = queryParam(param).elementAtOrNull(0) ?: SerializationFormat.CONFLUENT.name
  try {
    return SerializationFormat.valueOf(format.uppercase(Locale.getDefault()))
  } catch (e: IllegalArgumentException) {
    sendError(400, "Invalid $param", "Valid values are: ${SerializationFormat.values().joinToString(", ") { it.name.lowercase() }}")
    throw e
  }
}

fun RoutingContext.getSchemaFromHeader(header: String = "X-Avro-Schema"): Schema {
  val schemaString = request().getHeader(header)
  if (schemaString == null) {
    sendError(400, "Avro schema required in $header header")
    throw IllegalArgumentException("Avro schema required in $header header")
  }

  return try {
    Schema.Parser().parse(schemaString)
  } catch (e: SchemaParseException) {
    sendError(400, "Invalid avro schema", e.message)
    throw e
  }
}
