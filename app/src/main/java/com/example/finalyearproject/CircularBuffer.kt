package com.example.finalyearproject

class CircularBuffer(size: Int) {
    private val buffer = ShortArray(size)
    private var writeIndex = 0
    private var readIndex = 0

    val isFull: Boolean
        get() = (writeIndex + 1) % buffer.size == readIndex

    val isEmpty: Boolean
        get() = writeIndex == readIndex

    fun write(value: Short) {
        if (!isFull) {
            buffer[writeIndex] = value
            writeIndex = (writeIndex + 1) % buffer.size
        }
    }

    fun read(): Short {
        return if (!isEmpty) {
            val value = buffer[readIndex]
            readIndex = (readIndex + 1) % buffer.size
            value
        } else {
            0
        }
    }
}