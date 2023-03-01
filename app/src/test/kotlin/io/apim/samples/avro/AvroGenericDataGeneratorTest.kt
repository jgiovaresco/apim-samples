package io.apim.samples.avro

import org.apache.avro.Schema
import org.apache.avro.generic.GenericFixed
import org.apache.avro.generic.GenericRecord
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*

class AvroGenericDataGeneratorTest {

  @Nested
  inner class Primitives {

    @Test
    fun `generate from boolean schema`() {
      val schema = Schema.Parser().parse("""{"type": "boolean"}""")
      expectThat(generate(schema)).isNotNull().isA<Boolean>()
    }

    @Test
    fun `generate from null schema`() {
      val schema = Schema.Parser().parse("""{"type": "null"}""")
      expectThat(generate(schema)).isNull()
      expectThat(generate(null)).isNull()
    }

    @Test
    fun `generate from int schema`() {
      val schema = Schema.Parser().parse("""{"type": "int"}""")
      expectThat(generate(schema)).isNotNull().isA<Int>()
    }

    @Test
    fun `generate from long schema`() {
      val schema = Schema.Parser().parse("""{"type": "long"}""")
      expectThat(generate(schema)).isNotNull().isA<Long>()
    }

    @Test
    fun `generate from float schema`() {
      val schema = Schema.Parser().parse("""{"type": "float"}""")
      expectThat(generate(schema)).isNotNull().isA<Float>()
    }

    @Test
    fun `generate from double schema`() {
      val schema = Schema.Parser().parse("""{"type": "double"}""")
      expectThat(generate(schema)).isNotNull().isA<Double>()
    }

    @Test
    fun `generate from bytes schema`() {
      val schema = Schema.Parser().parse("""{"type": "bytes"}""")
      expectThat(generate(schema)).isNotNull().isA<ByteArray>()
    }

    @Test
    fun `generate from string schema`() {
      val schema = Schema.Parser().parse("""{"type": "string"}""")
      expectThat(generate(schema)).isNotNull().isA<String>()
    }
  }

  @Nested
  inner class Record {

    @Test
    fun `generate from record schema`() {
      val schema = Schema.Parser().parse("""
     {
        "type": "record",
        "name": "Payment",
        "fields": [
            {
                "name": "id",
                "type": "string"
            },
            {
                "name": "amount",
                "type": "double"
            }
        ]
    }
    """.trimIndent())
      expectThat(generate(schema)).isNotNull().isA<GenericRecord>().and {
        get { get("id") }.isNotNull().isA<String>()
        get { get("amount") }.isNotNull().isA<Double>()
      }
    }
  }

  @Nested
  inner class Enum {
    @Test
    fun `generate from enum schema`() {
      val expectedValues = listOf("SPADES", "HEARTS", "DIAMONDS", "CLUBS")
      val schema = Schema.Parser().parse("""
      {
        "type": "enum",
        "name": "Suit",
        "symbols" : [${expectedValues.joinToString(",") { "\"$it\"" }}]
      }
    """.trimIndent())
      expectThat(generate(schema)).isNotNull().isA<String>().isContainedIn(expectedValues)
    }
  }

  @Nested
  inner class Arrays {
    @Test
    fun `generate from string array schema`() {
      val schema = Schema.Parser().parse("""
      {
        "type": "array",
        "items" : "string",
        "default": []
      }
    """.trimIndent())
      expectThat(generate(schema)).isNotNull().isA<List<String>>().hasSize(3)
    }
  }

  @Nested
  inner class Maps {
    @Test
    fun `generate from long value map schema`() {
      val schema = Schema.Parser().parse("""
      {
        "type": "map",
        "values" : "long",
        "default": {}
      }
    """.trimIndent())
      expectThat(generate(schema)).isNotNull().isA<Map<String, Long>>().hasSize(3)
    }
  }

  @Nested
  inner class Unions {
    @Test
    fun `generate from union schema`() {
      val schema = Schema.Parser().parse("""
      ["null", "string"]
    """.trimIndent())

      repeat(10) {
        val result = generate(schema)

        val isNull = result == null
        val isString = result != null && result is String

        expectThat(isNull || isString).describedAs("Expecting a null or string but was '$result'").isTrue()
      }
    }
  }

  @Nested
  inner class Fixed {
    @Test
    fun `generate from fixed schema`() {
      val schema = Schema.Parser().parse("""
      {"type": "fixed", "size": 16, "name": "md5"}
      """.trimIndent())

      val result = generate(schema)
      expectThat(result).isNotNull().isA<GenericFixed>().and {
        get { bytes().asList() }.hasSize(16)
      }
    }
  }
}
