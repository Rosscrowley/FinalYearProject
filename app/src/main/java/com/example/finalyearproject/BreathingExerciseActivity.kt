package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageButton
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.finalyearproject.databinding.ActivityBreathingExerciseBinding

class BreathingExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBreathingExerciseBinding
    private lateinit var viewModel: BreathingViewModel
    private var countdownTimer: CountDownTimer? = null
    private var timeLeftInMilliseconds: Long = 0
    private var isTimerRunning: Boolean = false
    private var selectedDurationMs: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreathingExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        promptDurationSelection()

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            val intent = Intent(this@BreathingExerciseActivity, ExercisesActivity::class.java)
            startActivity(intent)
        }

        viewModel = ViewModelProvider(this).get(BreathingViewModel::class.java)

        binding.playButton.setOnClickListener {
            viewModel.startExercise()
        }

        viewModel.state.observe(this, { state ->
            when (state) {
                BreathingState.Inhale -> {
                    binding.instructionText.text = "Breathe In"
                    binding.breathingView.animateBreathingIn()

                }
                BreathingState.Hold -> {
                    binding.instructionText.text = "Hold"
                    binding.breathingView.animateHolding()
                }
                BreathingState.Exhale -> {
                    binding.instructionText.text = "Breathe Out"
                    binding.breathingView.animateBreathingOut()
                }
            }
        })

        binding.pauseButton.setOnClickListener {
            viewModel.pauseExercise()
        }

        binding.restartButton.setOnClickListener {
            viewModel.restartExercise()
        }
    }

    fun onPlayButtonClick(view: View) {
        if (!isTimerRunning && selectedDurationMs > 0) {
            startCountdown()
            viewModel.startExercise()
        }
    }

    private fun promptDurationSelection() {

        val numberPicker = NumberPicker(this).apply {
            minValue = 1 // minimum time
            maxValue = 10
        }

        AlertDialog.Builder(this)
            .setTitle("Choose Duration (minutes)")
            .setView(numberPicker)
            .setPositiveButton("OK") { dialog, _ ->
                selectedDurationMs = numberPicker.value * 60 * 1000L
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun startCountdown() {

        timeLeftInMilliseconds = selectedDurationMs

        countdownTimer?.cancel() // Cancel any existing timer

        countdownTimer = object : CountDownTimer(timeLeftInMilliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMilliseconds = millisUntilFinished
                updateCountdownText()
            }

            override fun onFinish() {
                binding.countdownText.text = "Done!"
                isTimerRunning = false
                timeLeftInMilliseconds = selectedDurationMs // Reset the timer duration for potential restart
            }
        }.start()

        isTimerRunning = true
    }

    private fun updateCountdownText() {
        val minutes = (timeLeftInMilliseconds / 1000) / 60
        val seconds = (timeLeftInMilliseconds / 1000) % 60
        binding.countdownText.text = String.format("%02d:%02d", minutes, seconds)
    }

    fun onPauseButtonClick(view: View) {
        viewModel.pauseExercise()
        pauseTimer()
    }

    fun onRestartButtonClick(view: View) {
        viewModel.restartExercise()
        restartTimer()
    }
    private fun pauseTimer() {
        countdownTimer?.cancel()
        isTimerRunning = false
    }

    private fun restartTimer() {
        countdownTimer?.cancel()
        timeLeftInMilliseconds = selectedDurationMs
        startCountdown()
    }
}