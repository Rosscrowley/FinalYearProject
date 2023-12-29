package com.example.finalyearproject

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BreathingExerciseView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var circleRadius: Float = 100f // Start with the min radius
    private val minRadius = 100f // Min radius for breathing out
    private val maxRadius = 300f // Max radius for breathing in
    private var circleColor: Int = Color.GREEN // Start with green for breathing in
    private val inhaleColor = Color.GREEN // Color for inhaling
    private val exhaleColor = Color.RED // Color for exhaling
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun animateBreathingIn() {
        circleColor = inhaleColor // Set to inhale color
        val animator = ValueAnimator.ofFloat(minRadius, maxRadius).apply {
            duration = 3000 // Duration of breathe in is 3 seconds
            addUpdateListener {
                circleRadius = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun animateHolding() {
        // No change in radius, just hold the color for 3 seconds
        postDelayed({
            invalidate() // Trigger a redraw to ensure any queued drawing operations are completed
        }, 3000) // Hold duration is 3 seconds
    }

    fun animateBreathingOut() {
        circleColor = exhaleColor // Set to exhale color
        val animator = ValueAnimator.ofFloat(maxRadius, minRadius).apply {
            duration = 3000 // Duration of breathing out is 3 seconds
            addUpdateListener { animation ->
                circleRadius = animation.animatedValue as Float
                invalidate() // Redraw the view with the new radius
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = circleColor
        canvas.drawCircle(width / 2f, height / 2f, circleRadius, paint)
    }
}