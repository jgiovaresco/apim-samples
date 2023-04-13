package io.apim.samples.avro

import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.io.DatumReader
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.EncoderFactory
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

class JsonSerDe(private val schema: Schema) {
  fun serialize(data: Any?): String {
    val writer = GenericDatumWriter<Any>(schema)
    val output = ByteArrayOutputStream()
    val encoder = EncoderFactory.get().jsonEncoder(schema, output)

    writer.write(data, encoder)
    encoder.flush()
    output.flush()


    return output.toString(StandardCharsets.UTF_8)
  }

  fun deserialize(json: String): Any {
    val decoder = DecoderFactory.get().jsonDecoder(schema, json)
    val reader: DatumReader<Any> = GenericDatumReader(schema)
    return reader.read(null, decoder)
  }
}
