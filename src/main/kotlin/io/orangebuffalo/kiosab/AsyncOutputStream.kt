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

/**
 * [Flow] builder for bridging [OutputStream] to async API.
 *
 * [spec] function will be invoked with [AsyncOutputStreamContext] receiver,
 * providing access to the [OutputStream]. Any operation that writes to this
 * stream should be invoked within [AsyncOutputStreamContext.writeAsync] clause.
 * This is the function that can suspend in case the underlying buffer is filled
 * with data after [AsyncOutputStreamContext.writeAsync] invocation.
 *
 * The buffer size of the underlying stream is controlled by [config] parameter.
 * It is recommended that invocation of [AsyncOutputStreamContext.writeAsync] never
 * write volumes higher than this buffer, otherwise additional memory will be used
 * for buffering - we can only suspend on [AsyncOutputStreamContext.writeAsync],
 * but not on [OutputStream] operations. Ideally, we should write as little as possible
 * in each [AsyncOutputStreamContext.writeAsync] for efficient buffer usage.
 *
 * The [Flow] will emit one [ByteBuffer] for each [AsyncOutputStreamConfig.emitOnBytes] buffer size,
 * once it is filled. Invoking [OutputStream.flush] inside [AsyncOutputStreamContext.writeAsync]
 * will force emission on [AsyncOutputStreamContext.writeAsync] completion.
 *
 * Please note, we provide libraries integration for cleaner usage of [AsyncOutputStreamContext.writeAsync].
 * See kiosab docs.
 */
fun asyncOutputStreamWriter(
    config: AsyncOutputStreamConfig = AsyncOutputStreamConfig(),
    spec: suspend AsyncOutputStreamContext.() -> Unit
): Flow<ByteBuffer> {
    return flow {
        val context = AsyncOutputStreamContext(config, this::emit)
        currentCoroutineContext()[StreamContext]?.context = context
        context.spec()
        context.close()
    }.flowOn(StreamContext())
}

/**
 * The same as [AsyncOutputStreamContext.writeAsync], but can be invoked outside of
 * [AsyncOutputStreamContext] receiver. Intended for extensions usage.
 *
 * Please note, it must only be invoked within [asyncOutputStreamWriter] builder,
 * otherwise it will fail.
 */
suspend fun writeAsync(spec: suspend () -> Unit) {
    val context = currentCoroutineContext()[StreamContext]?.context
        ?: throw IllegalStateException("This method can only be invoked from within asyncOutputStreamWriter")
    context.writeAsync(spec)
}

/**
 * Configuration for [asyncOutputStreamWriter]
 */
data class AsyncOutputStreamConfig(

    /**
     * The size of the buffer. Emission will happen once the buffer is filled
     * (unless explicitly flushed).
     */
    var emitOnBytes: Int = 4 * 1024
)

/**
 * See [asyncOutputStreamWriter].
 */
class AsyncOutputStreamContext(
    config: AsyncOutputStreamConfig,
    private val emit: suspend (ByteBuffer) -> Unit
) {
    private val outputStreamInternal = AsyncOutputStream(config.emitOnBytes)

    /**
     * To be used for APIs that require [OutputStream].
     * See [asyncOutputStreamWriter] for the details.
     */
    val outputStream: OutputStream
        get() = outputStreamInternal

    /**
     * See [asyncOutputStreamWriter] for the details.
     */
    suspend fun writeAsync(spec: suspend () -> Unit) {
        spec()
        outputStreamInternal.emitAllFlushedBuffers(emit)
    }

    /**
     * Closes the underlying [OutputStream] and emits all buffers.
     */
    suspend fun close() {
        @Suppress("BlockingMethodInNonBlockingContext")
        outputStreamInternal.close()
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
            if (!closed) {
                flush()
                closed = true
            }
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

