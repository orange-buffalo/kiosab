package io.orangebuffalo.kiosab.integrations

import com.fasterxml.jackson.core.Base64Variants
import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.orangebuffalo.kiosab.AsyncOutputStreamConfig
import io.orangebuffalo.kiosab.asyncOutputStreamWriter
import kotlinx.coroutines.flow.fold

class JacksonTest : FunSpec({

    test("should generate proper JSON with async methods") {
        val actualJson = asyncOutputStreamWriter(config = AsyncOutputStreamConfig(emitOnBytes = 1)) {
            val factory = JsonFactory()

            @Suppress("BlockingMethodInNonBlockingContext")
            val generator = factory.createGenerator(outputStream, JsonEncoding.UTF8)
            generator.codec = ObjectMapper()
            generator.useDefaultPrettyPrinter()

            generator.writeStartObjectAsync()

            generator.writeFieldNameAsync("stringArray")
            generator.writeArrayAsync(arrayOf("one", "two"), 0, 2)

            generator.writeFieldIdAsync(42L)
            generator.writeArrayAsync(longArrayOf(1L, 2L), 0, 2)

            generator.writeFieldNameAsync("nested")
            generator.writeStartObjectAsync()
            generator.writeEndObjectAsync()

            generator.writeFieldNameAsync("nested2")
            generator.writeStartObjectAsync("forThis")
            generator.writeEndObjectAsync()

            generator.writeFieldNameAsync("intArray")
            generator.writeArrayAsync(intArrayOf(3, 4), 0, 2)

            generator.writeFieldNameAsync("doubleArray")
            generator.writeArrayAsync(doubleArrayOf(5.0, 6.0), 0, 2)

            generator.writeFieldNameAsync("binary")
            generator.writeBinaryAsync(byteArrayOf(7, 8))

            generator.writeFieldNameAsync("binaryVar")
            generator.writeBinaryAsync(Base64Variants.MIME, byteArrayOf(9, 10), 0, 2)

            generator.writeFieldNameAsync("binaryArray")
            generator.writeBinaryAsync(byteArrayOf(11, 12), 0, 2)

            generator.writeBinaryFieldAsync("byteArray", byteArrayOf(13, 14))

            generator.writeFieldNameAsync("boolean0")
            generator.writeBooleanAsync(true)

            generator.writeBooleanFieldAsync("boolean", false)

            generator.writeFieldNameAsync("embedded")
            generator.writeEmbeddedObjectAsync(null)

            generator.writeFieldNameAsync("null0")
            generator.writeNullAsync()

            generator.writeNullFieldAsync("nullField")

            generator.writeFieldNameAsync("int0")
            generator.writeNumberAsync(15)

            generator.writeFieldNameAsync("long0")
            generator.writeNumberAsync(16L)

            generator.writeFieldNameAsync("double0")
            generator.writeNumberAsync(17.0)

            generator.writeFieldNameAsync("bigDecimal0")
            generator.writeNumberAsync(18.toBigDecimal())

            generator.writeFieldNameAsync("doubleInteger0")
            generator.writeNumberAsync(19.toBigInteger())

            generator.writeFieldNameAsync("stringNumber0")
            generator.writeNumberAsync("20")

            generator.writeFieldNameAsync("float0")
            generator.writeNumberAsync(21.0f)

            generator.writeFieldNameAsync("charArrayNumber")
            generator.writeNumberAsync(charArrayOf('2', '2'), 0, 2)

            generator.writeFieldNameAsync("short0")
            generator.writeNumberAsync(23.toShort())

            generator.writeNumberFieldAsync("int", 24)
            generator.writeNumberFieldAsync("long", 25L)
            generator.writeNumberFieldAsync("double", 26.0)
            generator.writeNumberFieldAsync("float", 27.0f)
            generator.writeNumberFieldAsync("bigInt", 28.toBigInteger())
            generator.writeNumberFieldAsync("bigDecimal", 29.toBigDecimal())
            generator.writeNumberFieldAsync("short", 30.toShort())

            generator.writeObjectFieldStartAsync("objectField")
            generator.writeEndObjectAsync()

            generator.writeFieldNameAsync("pojo0")
            generator.writePOJOAsync(Pojo("value"))

            generator.writeFieldNameAsync("longArray")
            generator.writeStartArrayAsync()
            generator.writeNumberAsync(3)
            generator.writeNumberAsync(4)
            generator.writeEndArrayAsync()

            generator.writeFieldNameAsync("anotherArray")
            generator.writeStartArrayAsync("context")
            generator.writeEndArrayAsync()

            generator.writeArrayFieldStartAsync("arrayField")
            generator.writeEndArrayAsync()

            generator.writePOJOFieldAsync("pojo", Pojo("value2"))

            generator.writeEndObjectAsync()

            generator.closeAsync()

        }.fold(StringBuilder()) { acc, value ->
            val bytes = ByteArray(value.limit())
            value.get(bytes)
            acc.append(String(bytes))
            acc
        }.toString()

        val expectedJson = javaClass.getResource("/Jackson.json")?.readText()?.trim()
        actualJson.shouldBe(expectedJson)
    }
})

private data class Pojo(val value: String)
