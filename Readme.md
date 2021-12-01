# kiosab
Kotlin IO Stream Async Bridge

## The motivation

Many libraries out there continue to only provide blocking IO APIs for streaming
(with some examples being Jackson streaming API, ZIP compression, etc). This leads to
otherwise non-blocking applications, that rely on Kotlin coroutines and ecosystem, introducing
dispatchers (thread pulls) for blocking IO operations and thus limiting the scalability.

Although it is not possible to truly convert blocking API to non-blocking, it is possible to provide
some solutions with compromises for output IO.

In case of a small output volumes, we can simply use `ByteArrayOutputStream` that will effectively 
make the IO operations non-blocking. Then, the resulting byte array can be fed into non-blocking 
API like Java NIO or Kotlin Ktor.  
               
However, for large data volumes this approach is not applicable due to high memory requirements. 
This is the targeted use case for `kiosab` library (although the small data outputs 
will work without issues as well). The library provides API to convert a series of `write` operations
on `OutputStream` into a `Flow` of `ByteBuffer`. The buffers are emitted once filled, allowing to pass
them to the underlying non-blocking output consumers (e.g. Ktor, Spring Web Flux, etc). The producer will
suspend once a buffer is filled until consumer processes the output (or, if buffering enabled on `Flow` by 
the user, until buffer space is available).

## API

The entry point is the `Flow` builder of `io.orangebuffalo.kiosab.AsyncOutputStreamKt#asyncOutputStreamWriter`. 
It accepts a lambda executed with `io.orangebuffalo.kiosab.AsyncOutputStreamContext` receiver, that provides
two main primitives: `outputStream: java.io.OutputStream` to be used to write data to and 
`suspend fun writeAsync(spec: suspend () -> Unit)` which should wrap any code that writes to the stream.

The code inside `writeAsync` will not suspend and will be executed fully, filling the buffers in memory. Once buffers 
are filled (or `outputStream.flush()` is called), all filled buffers will be emitted on existing `writeAsync`.
This is where the client code gets suspended.

Clients should write as little as possible inside each `writeAsync` call for effective memory usage. The more
is written, the more buffers get accumulated. 
       
### Controlling the buffer size

By default, the buffer size is 4kb. This is the size of `ByteBuffer`s that are emitted. 

To change it, pass `AsyncOutputStreamConfig` with desired value into `asyncOutputStreamWriter`.

### Integration with other frameworks

`kiosab` provides extension functions for some popular libraries. They allow to skip wrapping
the code into `writeAsync` and streamline the code.
* `Jackson`: `JsonGenerator` has `write*` extensions with `*Async` suffix.
