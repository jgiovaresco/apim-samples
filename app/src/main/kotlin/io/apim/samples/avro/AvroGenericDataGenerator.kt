package io.apim.samples.avro

import io.github.serpro69.kfaker.Faker
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericFixed
import org.apache.avro.generic.GenericRecord
import kotlin.random.Random

val faker = Faker()

fun generate(schema: Schema?): Any? = when(schema?.type) {
  Schema.Type.BOOLEAN -> faker.random.nextBoolean()
  Schema.Type.INT -> faker.random.nextInt()
  Schema.Type.LONG -> faker.random.nextLong()
  Schema.Type.FLOAT -> faker.random.nextFloat()
  Schema.Type.DOUBLE -> faker.random.nextDouble()
  Schema.Type.BYTES -> faker.random.randomString().toByteArray()
  Schema.Type.STRING -> faker.random.randomString()
  Schema.Type.RECORD -> newRecord(schema)
  Schema.Type.ENUM -> faker.random.randomValue(schema.enumSymbols)
  Schema.Type.ARRAY -> newArray(schema)
  Schema.Type.MAP -> newMap(schema)
  Schema.Type.UNION -> newUnion(schema)
  Schema.Type.FIXED -> newFixed(schema)
  Schema.Type.NULL -> null
  null -> null
}

private fun newRecord(schema: Schema): GenericRecord {
  val record = GenericData.Record(schema)

  schema.fields.forEach {
    record.put(it.name(), generate(it.schema()))
  }

  return record
}

private fun newArray(schema: Schema): List<Any?> {
  val list = mutableListOf<Any?>()
  repeat(3) { list.add(generate(schema.elementType)) }
  return list
}

private fun newMap(schema: Schema): Map<String, Any?> {
  val map = mutableMapOf<String, Any?>()
  repeat(3) { map[faker.random.randomString()] = generate(schema.valueType) }
  return map
}

private fun newUnion(schema: Schema): Any? {
  val selectedSchema = faker.random.randomValue(schema.types)
  return generate(selectedSchema)
}

private fun newFixed(schema: Schema): GenericFixed {
  val bytes = ByteArray(schema.fixedSize)
  Random.nextBytes(bytes)
  return GenericData.Fixed(schema, bytes)
}
