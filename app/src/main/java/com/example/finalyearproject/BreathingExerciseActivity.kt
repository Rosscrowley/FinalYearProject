package com.example.finalyearproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BreathingExerciseActivity : AppCompatActivity() {

    private lateinit var viewModel: BreathingViewModel
    private var countdownTimer: CountDownTimer? = null
    private var timeLeftInMilliseconds: Long = 0
    private var isTimerRunning: Boolean = false
    private var selectedDurationMs: Long = 0
    private var selectedMinutes: Int = 0

    private lateinit var instructionText: TextView
    private lateinit var countdownText: TextView
    private lateinit var playButton: ImageButton
    private lateinit var pauseButton: ImageButton
    private lateinit var restartButton: ImageButton
    private lateinit var closeButton: ImageButton
    private lateinit var breathingView: BreathingExerciseView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breathing_exercise)

        instructionText = findViewById(R.id.instructionText)
        countdownText = findViewById(R.id.countdownText)
        playButton = findViewById(R.id.playButton)
        pauseButton = findViewById(R.id.pauseButton)
        restartButton = findViewById(R.id.restartButton)
        closeButton = findViewById(R.id.closeButton)
        breathingView = findViewById(R.id.breathingView)

        closeButton.setOnClickListener {
            startActivity(Intent(this@BreathingExerciseActivity, ExercisesActivity::class.java))
        }

        viewModel = ViewModelProvider(this)[BreathingViewModel::class.java]

        viewModel.state.observe(this) { state ->
            when (state) {
                BreathingState.Inhale -> {
                    instructionText.text = "Breathe In"
                    breathingView.animateBreathingIn()

                }
                BreathingState.Hold -> {
                    instructionText.text = "Hold"
                    breathingView.animateHolding()
                }
                BreathingState.Exhale -> {
                    instructionText.text = "Breathe Out"
                    breathingView.animateBreathingOut()
                }
            }
        }

        playButton.setOnClickListener {
            onPlayButtonClick(it)
        }

        pauseButton.setOnClickListener {
            onPauseButtonClick(it)
        }

        restartButton.setOnClickListener {
            onRestartButtonClick(it)
        }

        promptDurationSelection()
    }

    private fun onPlayButtonClick(view: View) {
        if (!isTimerRunning && selectedDurationMs > 0) {
            startCountdown()
            viewModel.startExercise()
        }
    }

    private fun onPauseButtonClick(view: View) {
        countdownTimer?.cancel()
        viewModel.pauseExercise()
        isTimerRunning = false
    }

    private fun onRestartButtonClick(view: View) {
        restartTimer()
        viewModel.startExercise()
    }

    private fun promptDurationSelection() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 1
            maxValue = 10
        }

        AlertDialog.Builder(this)
            .setTitle("Choose Duration (minutes)")
            .setView(numberPicker)
            .setPositiveButton("OK") { dialog, _ ->
                selectedMinutes = numberPicker.value
                selectedDurationMs = numberPicker.value * 60 * 1000L
                updateCountdownText(selectedDurationMs)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun startCountdown() {
        timeLeftInMilliseconds = selectedDurationMs
        countdownTimer?.cancel()

        countdownTimer = object : CountDownTimer(timeLeftInMilliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMilliseconds = millisUntilFinished
                updateCountdownText()
            }

            override fun onFinish() {
                countdownText.text = "Done!"
                isTimerRunning = false
                timeLeftInMilliseconds = selectedDurationMs
                markExerciseAsComplete()
            }
        }.start()

        isTimerRunning = true
    }

    private fun updateCountdownText(timeInMillis: Long = this.timeLeftInMilliseconds) {
        val minutes = (timeInMillis / 1000) / 60
        val seconds = (timeInMillis / 1000) % 60
        countdownText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun restartTimer() {
        startCountdown()
    }

    private fun markExerciseAsComplete() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("userProgress/$userId/dailyExercises/Breathing Exercise")
            val exerciseCompletionUpdate = mapOf("completed" to true, "date" to getCurrentDate())

            databaseReference.updateChildren(exerciseCompletionUpdate).addOnSuccessListener {
                Toast.makeText(this, "Exercise marked as complete.", Toast.LENGTH_SHORT).show()

                val score = selectedMinutes.toFloat()
                saveUserExerciseScore(userId, "Breathing1", score)

                setResult(Activity.RESULT_OK)
                finish()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to mark exercise as complete: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun saveUserExerciseScore(userId: String, exerciseId: String, score: Float) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val exerciseScore = ExerciseScore(score, currentDate)

        val database = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app")
        val userScoreRef = database.getReference("userScores/$userId/$exerciseId")

        userScoreRef.push().setValue(exerciseScore)
            .addOnSuccessListener {
                // Handle success
                Toast.makeText(this, "score saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Handle failure
            }
    }
}
