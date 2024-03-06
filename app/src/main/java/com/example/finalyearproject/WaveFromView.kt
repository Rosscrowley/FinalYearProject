package com.example.finalyearproject

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class WaveformView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var audioData: ShortArray? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    fun setAudioData(data: ShortArray) {
        audioData = data
        invalidate() // Redraw the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        audioData?.let { data ->
            val height = height.toFloat()
            val width = width.toFloat()
            val halfHeight = height / 2
            val maxShort = Short.MAX_VALUE.toFloat()
            val step = data.size.toFloat() / width // Number of samples to skip for each pixel

            var x = 0f
            for (i in data.indices step step.toInt()) {
                val sample = data[i]
                val y = (sample / maxShort) * halfHeight + halfHeight
                canvas.drawLine(x, halfHeight, x, y, paint)
                x += 1f
            }
        }
    }
}