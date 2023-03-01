package io.apim.samples.avro

interface AvroSerDe {
    fun serialize(data: Any?): ByteArray

    fun deserialize(binary: ByteArray): Any?
}
