package com.example.finalyearproject

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.finalyearproject.databinding.ActivityBreathingExerciseBinding

class BreathingExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBreathingExerciseBinding
    private lateinit var viewModel: BreathingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreathingExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        promptDurationSelection()

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
        viewModel.startExercise()
    }

    private fun promptDurationSelection() {
        // Example using an AlertDialog with a NumberPicker
        val numberPicker = NumberPicker(this).apply {
            minValue = 1 // minimum time
            maxValue = 30 // maximum time, change as needed
        }

        AlertDialog.Builder(this)
            .setTitle("Choose Duration (minutes)")
            .setView(numberPicker)
            .setPositiveButton("OK") { dialog, _ ->
                val selectedTime = numberPicker.value // Get the selected value
                startCountdown(selectedTime * 60 * 1000L) // Convert minutes to milliseconds
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun startCountdown(durationMs: Long) {

        // Assuming you have a TextView with the id countdownText for the timer display
        val countdownText = binding.countdownText

        object : CountDownTimer(durationMs, 1000) { // update every second
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                countdownText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                countdownText.text = "" // or some completion text
                // Optionally stop the breathing exercise or notify the user
            }
        }.start()
    }

    fun onPauseButtonClick(view: View) {
        viewModel.pauseExercise()
    }

    fun onRestartButtonClick(view: View) {
        viewModel.restartExercise()
    }
}