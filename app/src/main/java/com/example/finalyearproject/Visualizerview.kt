package com.example.finalyearproject

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class VisualizerView : View {
    private val paint: Paint = Paint()
    private var amplitudes: ByteArray = byteArrayOf()

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        paint.strokeWidth = 5f
        paint.color = -0x1000000
    }

    fun updateVisualizer(data: ByteArray) {
        amplitudes = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in 0 until amplitudes.size) {
            val x = (i * width / amplitudes.size).toFloat()
            val y = height / 2 + amplitudes[i].toFloat() / 128 * (height / 2)
            canvas.drawLine(x, height / 2.toFloat(), x, y, paint)
        }
    }
}