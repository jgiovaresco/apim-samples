package io.apim.samples.core.avro.impl

import io.apim.samples.core.avro.AvroSerDe
import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.EncoderFactory
import java.io.ByteArrayOutputStream

class AvroSerDeSimple(private val schema: Schema) : AvroSerDe {
  override fun serialize(data: Any?): ByteArray {
    val writer = GenericDatumWriter<Any>(schema)
    val output = ByteArrayOutputStream()
    val encoder = EncoderFactory.get().binaryEncoder(output, null)

    writer.write(data, encoder)
    encoder.flush()

    return output.toByteArray()
  }

  override fun deserialize(binary: ByteArray): Any? {
    val reader = GenericDatumReader<Any>(schema)
    val decoder = DecoderFactory.get().binaryDecoder(binary, null)

    return reader.read(null, decoder)
  }
}
