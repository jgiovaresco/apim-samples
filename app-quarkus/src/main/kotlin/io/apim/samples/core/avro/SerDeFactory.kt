package io.apim.samples.core.avro

import io.apim.samples.core.avro.impl.AvroSerDeConfluent
import io.apim.samples.core.avro.impl.AvroSerDeSimple
import io.apim.samples.core.avro.impl.JsonSerDe
import jakarta.enterprise.context.ApplicationScoped
import org.apache.avro.Schema

enum class SerializationFormat {
  CONFLUENT, SIMPLE
}

@ApplicationScoped
class SerDeFactory {
  fun newAvroSerDe(schema: Schema, format: SerializationFormat): AvroSerDe = when (format) {
    SerializationFormat.SIMPLE -> AvroSerDeSimple(schema)
    SerializationFormat.CONFLUENT -> AvroSerDeConfluent(schema)
  }

  fun newJsonSerDe(schema: Schema): JsonSerDe = JsonSerDe(schema)
}
