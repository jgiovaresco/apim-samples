package io.apim.samples.avro

import org.apache.avro.Schema

class AvroSerDeFactoryImpl: AvroSerDeFactory {
    override fun new(schema: Schema, format: SerializationFormat): AvroSerDe = when(format){
        SerializationFormat.SIMPLE -> AvroSerDeSimple(schema)
        SerializationFormat.CONFLUENT -> AvroSerDeConfluent(schema)
    }
}
