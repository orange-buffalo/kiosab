/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("BlockingMethodInNonBlockingContext")

package io.orangebuffalo.kiosab.integrations

import com.fasterxml.jackson.core.Base64Variant
import com.fasterxml.jackson.core.JsonGenerator
import io.orangebuffalo.kiosab.writeAsync
import java.math.BigDecimal
import java.math.BigInteger

suspend fun JsonGenerator.writeArrayAsync(array: Array<String>, offset: Int, length: Int) = writeAsync {
    writeArray(array, offset, length)
}

suspend fun JsonGenerator.writeArrayAsync(array: LongArray, offset: Int, length: Int) = writeAsync {
    writeArray(array, offset, length)
}

suspend fun JsonGenerator.writeArrayAsync(array: IntArray, offset: Int, length: Int) = writeAsync {
    writeArray(array, offset, length)
}

suspend fun JsonGenerator.writeArrayAsync(array: DoubleArray, offset: Int, length: Int) = writeAsync {
    writeArray(array, offset, length)
}

suspend fun JsonGenerator.writeBinaryAsync(data: ByteArray) = writeAsync {
    writeBinary(data)
}

suspend fun JsonGenerator.writeBinaryAsync(bv: Base64Variant, data: ByteArray, offset: Int, len: Int) = writeAsync {
    writeBinary(bv, data, offset, len)
}

suspend fun JsonGenerator.writeBinaryAsync(data: ByteArray, offset: Int, len: Int) = writeAsync {
    writeBinary(data, offset, len)
}

suspend fun JsonGenerator.writeArrayFieldStartAsync(fieldName: String) = writeAsync {
    writeArrayFieldStart(fieldName)
}

suspend fun JsonGenerator.writeBinaryFieldAsync(fieldName: String, data: ByteArray) = writeAsync {
    writeBinaryField(fieldName, data)
}

suspend fun JsonGenerator.writeBooleanAsync(state: Boolean) = writeAsync {
    writeBoolean(state)
}

suspend fun JsonGenerator.writeBooleanFieldAsync(fieldName: String, value: Boolean) = writeAsync {
    writeBooleanField(fieldName, value)
}

suspend fun JsonGenerator.writeEmbeddedObjectAsync(`object`: Any?) = writeAsync {
    writeEmbeddedObject(`object`)
}

suspend fun JsonGenerator.writeEndArrayAsync() = writeAsync {
    writeEndArray()
}

suspend fun JsonGenerator.writeEndObjectAsync() = writeAsync {
    writeEndObject()
}

suspend fun JsonGenerator.writeFieldIdAsync(id: Long) = writeAsync {
    writeFieldId(id)
}

suspend fun JsonGenerator.writeFieldNameAsync(name: String) = writeAsync {
    writeFieldName(name)
}

suspend fun JsonGenerator.writeNullAsync() = writeAsync {
    writeNull()
}

suspend fun JsonGenerator.writeNullFieldAsync(fieldName: String) = writeAsync {
    writeNullField(fieldName)
}

suspend fun JsonGenerator.writeNumberAsync(v: Int) = writeAsync {
    writeNumber(v)
}

suspend fun JsonGenerator.writeNumberAsync(v: Long) = writeAsync {
    writeNumber(v)
}

suspend fun JsonGenerator.writeNumberAsync(v: Double) = writeAsync {
    writeNumber(v)
}

suspend fun JsonGenerator.writeNumberAsync(v: BigDecimal) = writeAsync {
    writeNumber(v)
}

suspend fun JsonGenerator.writeNumberAsync(v: BigInteger) = writeAsync {
    writeNumber(v)
}

suspend fun JsonGenerator.writeNumberAsync(v: String) = writeAsync {
    writeNumber(v)
}

suspend fun JsonGenerator.writeNumberAsync(v: Float) = writeAsync {
    writeNumber(v)
}

suspend fun JsonGenerator.writeNumberAsync(v: Short) = writeAsync {
    writeNumber(v)
}

suspend fun JsonGenerator.writeNumberAsync(encodedValueBuffer: CharArray, offset: Int, len: Int) = writeAsync {
    writeNumber(encodedValueBuffer, offset, len)
}

suspend fun JsonGenerator.writeNumberFieldAsync(fieldName: String, value: Float) = writeAsync {
    writeNumberField(fieldName, value)
}

suspend fun JsonGenerator.writeNumberFieldAsync(fieldName: String, value: Int) = writeAsync {
    writeNumberField(fieldName, value)
}

suspend fun JsonGenerator.writeNumberFieldAsync(fieldName: String, value: Long) = writeAsync {
    writeNumberField(fieldName, value)
}

suspend fun JsonGenerator.writeNumberFieldAsync(fieldName: String, value: Short) = writeAsync {
    writeNumberField(fieldName, value)
}

suspend fun JsonGenerator.writeNumberFieldAsync(fieldName: String, value: Double) = writeAsync {
    writeNumberField(fieldName, value)
}

suspend fun JsonGenerator.writeNumberFieldAsync(fieldName: String, value: BigInteger) = writeAsync {
    writeNumberField(fieldName, value)
}

suspend fun JsonGenerator.writeNumberFieldAsync(fieldName: String, value: BigDecimal) = writeAsync {
    writeNumberField(fieldName, value)
}

suspend fun JsonGenerator.writeObjectFieldStartAsync(fieldName: String) = writeAsync {
    writeObjectFieldStart(fieldName)
}

suspend fun JsonGenerator.writePOJOAsync(pojo: Any) = writeAsync {
    writePOJO(pojo)
}

suspend fun JsonGenerator.writePOJOFieldAsync(fieldName: String, pojo: Any) = writeAsync {
    writePOJOField(fieldName, pojo)
}

suspend fun JsonGenerator.writeStartArrayAsync() = writeAsync {
    writeStartArray()
}

suspend fun JsonGenerator.writeStartObjectAsync() = writeAsync {
    writeStartObject()
}

suspend fun JsonGenerator.writeStartObjectAsync(forValue: Any) = writeAsync {
    writeStartObject(forValue)
}

suspend fun JsonGenerator.writeStartArrayAsync(forValue: Any) = writeAsync {
    writeStartArray(forValue)
}

suspend fun JsonGenerator.closeAsync() = writeAsync {
    close()
}
