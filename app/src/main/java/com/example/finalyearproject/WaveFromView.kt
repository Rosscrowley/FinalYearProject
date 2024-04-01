package com.example.finalyearproject

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.HorizontalScrollView
import kotlin.math.max

class WaveformView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var audioData: ShortArray? = null
    private val handler = Handler(Looper.getMainLooper())
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val playbackIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 4f
    }

    private var dynamicWidth: Int = 0
    private var playbackPositionMs: Long = 0
    var totalDurationMs: Long = 0

    var isDynamic: Boolean = false
        set(value) {
            field = value
            if (value) {
                audioData = null
            }
        }

    fun updateLiveData(liveData: ShortArray) {
        if (isDynamic) {
            // Initialize audioData if it's null
            if (audioData == null) {
                audioData = liveData
            } else {

                val newAudioData = ShortArray(audioData!!.size + liveData.size)
                System.arraycopy(audioData!!, 0, newAudioData, 0, audioData!!.size)
                System.arraycopy(liveData, 0, newAudioData, audioData!!.size, liveData.size)
                audioData = newAudioData
            }
            postInvalidate()

            val newWidth = calculateWidthForAudioData(audioData!!)
            setDynamicWidth(newWidth)

            requestScrollViewToScrollToEnd()
        }
    }

    private fun calculateWidthForAudioData(data: ShortArray): Int {
        val samplesPerSecond = 44100
        val pixelsPerSample = 0.01f
        return (data.size * pixelsPerSample).toInt()
    }
    private fun requestScrollViewToScrollToEnd() {
        handler.post {

            (parent as? HorizontalScrollView)?.apply {
                fullScroll(View.FOCUS_RIGHT)
            }
        }
    }
    fun setDynamicWidth(width: Int) {
        dynamicWidth = width
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (dynamicWidth > 0) {
            setMeasuredDimension(dynamicWidth, MeasureSpec.getSize(heightMeasureSpec))
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun setAudioData(data: ShortArray) {
        if (!isDynamic) {
            audioData = data
        }
    }

    fun setPlaybackPosition(playbackPositionMs: Long) {
        this.playbackPositionMs = playbackPositionMs
        if (!isDynamic) {
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (audioData == null) return

        val height = height.toFloat()
        val halfHeight = height / 2
        val maxShort = Short.MAX_VALUE.toFloat()

        // Draw waveform
        val widthToDraw = if (isDynamic) width.toFloat() else width * (playbackPositionMs.toFloat() / totalDurationMs)
        val step = max(1f, audioData!!.size.toFloat() / width).toInt()


        var x = 0f
        for (i in 0 until audioData!!.size step step) {
            val sample = audioData!![i]
            val y = halfHeight - (sample / maxShort) * halfHeight
            canvas.drawLine(x, halfHeight, x, y, paint)
            x += 1f
            if (!isDynamic && x > widthToDraw) break
        }

        if (!isDynamic) {
            canvas.drawLine(widthToDraw, 0f, widthToDraw, height.toFloat(), playbackIndicatorPaint)
        }
    }
}