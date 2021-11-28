package io.orangebuffalo.kiosab

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

fun outputStreamAsyncProducer(
    config: AsyncOutputStreamConfig = AsyncOutputStreamConfig(),
    spec: suspend AsyncOutputStreamContext.() -> Unit
): Flow<ByteBuffer> {
    return flow {
        val context = AsyncOutputStreamContext(config, this::emit)
        currentCoroutineContext()[StreamContext]?.context = context
        context.spec()
        context.flush()
    }.flowOn(StreamContext())
}

suspend fun writeAsync(spec: suspend () -> Unit) {
    val context = currentCoroutineContext()[StreamContext]?.context
        ?: throw IllegalStateException("This method can only be invoked from within outputStreamAsyncProducer")
    context.writeAsync(spec)
}

data class AsyncOutputStreamConfig(
    var emitOnBytes: Int = 4 * 1024
)

class AsyncOutputStreamContext(
    config : AsyncOutputStreamConfig,
    private val emit: suspend (ByteBuffer) -> Unit
) {
    private val outputStreamInternal = AsyncOutputStream(config.emitOnBytes)

    val outputStream: OutputStream
        get() = outputStreamInternal

    suspend fun writeAsync(spec: suspend () -> Unit) {
        spec()
        outputStreamInternal.emitAllFlushedBuffers(emit)
    }

    suspend fun flush() {
        @Suppress("BlockingMethodInNonBlockingContext")
        outputStreamInternal.flush()
        outputStreamInternal.emitAllFlushedBuffers(emit)
    }

    private class AsyncOutputStream(private val capacity: Int) : OutputStream() {
        private var currentBuffer: ByteBuffer = ByteBuffer.allocate(capacity)
        private val flushedBuffers = ArrayList<ByteBuffer>(1)
        private var closed = false

        override fun write(b: Int) {
            ensureOpen()
            currentBuffer.put(b.toByte())
            flushIfRequired()
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            ensureOpen()
            Objects.checkFromIndexSize(off, len, b.size)
            var written = 0
            while (written < len) {
                val newLen = minOf(len - written, capacity - currentBuffer.position())
                currentBuffer.put(b, off + written, newLen)
                flushIfRequired()
                written += newLen
            }
        }

        suspend fun emitAllFlushedBuffers(emit: suspend (ByteBuffer) -> Unit) {
            flushedBuffers.forEach { emit(it) }
            flushedBuffers.clear()
        }

        private fun flushIfRequired() {
            if (currentBuffer.position() == capacity) {
                flush()
            }
        }

        override fun close() {
            flush()
            closed = true
        }

        override fun flush() {
            ensureOpen()
            if (currentBuffer.position() > 0) {
                currentBuffer.limit(currentBuffer.position())
                currentBuffer.flip()
                flushedBuffers.add(currentBuffer)
                currentBuffer = ByteBuffer.allocate(capacity)
            }
        }

        private fun ensureOpen() {
            if (closed) {
                throw IllegalStateException("Stream has been closed")
            }
        }
    }
}

private data class StreamContext(var context: AsyncOutputStreamContext? = null) :
    AbstractCoroutineContextElement(StreamContext) {
    companion object Key : CoroutineContext.Key<StreamContext>
}

