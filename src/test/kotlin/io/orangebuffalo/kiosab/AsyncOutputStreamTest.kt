@file:Suppress("BlockingMethodInNonBlockingContext")

package io.orangebuffalo.kiosab

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer

class AsyncOutputStreamTest : FunSpec({

    test("should not emit any buffers if nothing is written") {
        val collectedBuffers = asyncOutputStreamWriter {
            // no op
        }.toList()

        collectedBuffers.shouldBeEmpty()
    }

    test("should emit if written less than buffer size") {
        val collectedBuffers = asyncOutputStreamWriter(AsyncOutputStreamConfig(emitOnBytes = 5)) {
            writeAsync {
                outputStream.writeBytes(1, 2)
            }
        }.toList()

        collectedBuffers.shouldHaveSize(1)
        collectedBuffers[0].shouldHaveData(1, 2)
    }

    test("should emit if written exactly the buffer size") {
        val collectedBuffers = asyncOutputStreamWriter(AsyncOutputStreamConfig(emitOnBytes = 5)) {
            writeAsync {
                outputStream.writeBytes(1, 2, 3, 4, 5)
            }
        }.toList()

        collectedBuffers.shouldHaveSize(1)
        collectedBuffers[0].shouldHaveData(1, 2, 3, 4, 5)
    }

    test("should emit if written more than the buffer size") {
        val collectedBuffers = asyncOutputStreamWriter(AsyncOutputStreamConfig(emitOnBytes = 5)) {
            writeAsync {
                outputStream.writeBytes(1, 2, 3, 4, 5, 6)
            }
        }.toList()

        collectedBuffers.shouldHaveSize(2)
        collectedBuffers[0].shouldHaveData(1, 2, 3, 4, 5)
        collectedBuffers[1].shouldHaveData(6)
    }

    test("should support int overload") {
        val collectedBuffers = asyncOutputStreamWriter(AsyncOutputStreamConfig(emitOnBytes = 5)) {
            writeAsync {
                outputStream.write(42)
            }
        }.toList()

        collectedBuffers.shouldHaveSize(1)
        collectedBuffers[0].shouldHaveData(42)
    }

    test("should prohibit write after close") {
        shouldThrow<IllegalStateException> {
            asyncOutputStreamWriter(AsyncOutputStreamConfig(emitOnBytes = 5)) {
                outputStream.close()

                writeAsync {
                    outputStream.write(42)
                }

            }.toList()
        }
    }

    test("should emit if buffer is rolled over multiple times") {
        val collectedBuffers = asyncOutputStreamWriter(AsyncOutputStreamConfig(emitOnBytes = 2)) {
            writeAsync {
                outputStream.writeBytes(1, 2, 3, 4, 5)
            }
        }.toList()

        collectedBuffers.shouldHaveSize(3)
        collectedBuffers[0].shouldHaveData(1, 2)
        collectedBuffers[1].shouldHaveData(3, 4)
        collectedBuffers[2].shouldHaveData(5)
    }

    test("should emit if multiple write operations invoked") {
        val collectedBuffers = asyncOutputStreamWriter(AsyncOutputStreamConfig(emitOnBytes = 2)) {
            writeAsync {
                outputStream.writeBytes(1, 2, 3)
            }
            writeAsync {
                outputStream.writeBytes(4, 5)
            }
            writeAsync {
                outputStream.writeBytes(6)
            }
        }.toList()

        collectedBuffers.shouldHaveSize(3)
        collectedBuffers[0].shouldHaveData(1, 2)
        collectedBuffers[1].shouldHaveData(3, 4)
        collectedBuffers[2].shouldHaveData(5, 6)
    }

    test("should emit with extension function") {
        val collectedBuffers = asyncOutputStreamWriter(AsyncOutputStreamConfig(emitOnBytes = 5)) {
            outputStream.testExtension()
        }.toList()

        collectedBuffers.shouldHaveSize(1)
        collectedBuffers[0].shouldHaveData(42)
    }

    test("should fail if extension is called outside of the context") {
        shouldThrow<IllegalStateException> {
            ByteArrayOutputStream().testExtension()
        }
    }

    test("should work if stream was manually closed") {
        val collectedBuffers = asyncOutputStreamWriter {
            outputStream.writeBytes(1)
            outputStream.close()
        }.toList()

        collectedBuffers.shouldHaveSize(1)
        collectedBuffers[0].shouldHaveData(1)
    }

    test("should work if stream was manually closed without writing") {
        val collectedBuffers = asyncOutputStreamWriter {
            outputStream.close()
        }.toList()

        collectedBuffers.shouldHaveSize(0)
    }
})

private suspend fun OutputStream.testExtension() {
    writeAsync {
        write(42)
    }
}

private fun ByteBuffer.shouldHaveData(vararg elements: Byte) {
    val expectedData = byteArrayOf(*elements)

    limit().shouldBe(expectedData.size)
    position().shouldBe(0)

    val actualData = ByteArray(expectedData.size)
    get(actualData)
    actualData.shouldBe(expectedData)
}

private fun OutputStream.writeBytes(vararg elements: Byte) {
    write(byteArrayOf(*elements), 0, elements.size)
}
