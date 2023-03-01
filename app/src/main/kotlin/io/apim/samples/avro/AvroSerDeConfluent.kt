package io.apim.samples.avro

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.Schema

class AvroSerDeConfluent(private val schema: Schema) : AvroSerDe {
  override fun serialize(data: Any?): ByteArray {
    val serializer = KafkaAvroSerializer()
    serializer.configure(mapOf("schema.registry.url" to "mock://my-scope"), false)
    return serializer.serialize("topic", data)
  }

  override fun deserialize(binary: ByteArray): Any? {
    val deserializer = KafkaAvroDeserializer()
    deserializer.configure(mapOf("schema.registry.url" to "mock://my-scope"), false)
    return deserializer.deserialize("topic", binary, schema)
  }
}
