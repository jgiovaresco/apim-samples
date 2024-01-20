package io.apim.samples.core.avro

interface AvroSerDe {
    fun serialize(data: Any?): ByteArray

    fun deserialize(binary: ByteArray): Any?
}
