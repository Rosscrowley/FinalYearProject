package com.example.finalyearproject

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BreathingExerciseView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var circleRadius: Float = 100f // Start with the min radius
    private val minRadius = 100f // Min radius for breathing out
    private val maxRadius = 300f // Max radius for breathing in
    private var circleColor: Int = getColorFromRes(R.color.appColour) // Start with color for breathing in
    private val inhaleColor = getColorFromRes(R.color.appColour) // Color for inhaling
    private val exhaleColor = getColorFromRes(R.color.appColour2) // Color for exhaling
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun animateBreathingIn() {
        circleColor = inhaleColor
        val animator = ValueAnimator.ofFloat(minRadius, maxRadius).apply {
            duration = 3000
            addUpdateListener {
                circleRadius = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun animateHolding() {
        postDelayed({
            invalidate()
        }, 3000)
    }

    fun animateBreathingOut() {
        circleColor = exhaleColor
        val animator = ValueAnimator.ofFloat(maxRadius, minRadius).apply {
            duration = 3000
            addUpdateListener { animation ->
                circleRadius = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = circleColor
        canvas.drawCircle(width / 2f, height / 2f, circleRadius, paint)
    }

    private fun getColorFromRes(colorResId: Int): Int {
        return context.resources.getColor(colorResId, null)
    }
}