package io.apim.samples.avro

import org.apache.avro.Schema

enum class SerializationFormat { CONFLUENT, SIMPLE, }

interface AvroSerDeFactory {
    fun new(schema: Schema, format: SerializationFormat = SerializationFormat.SIMPLE): AvroSerDe
}
