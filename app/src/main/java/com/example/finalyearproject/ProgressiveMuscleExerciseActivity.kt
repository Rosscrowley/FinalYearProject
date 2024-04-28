package com.example.finalyearproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgressiveMuscleExerciseActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var muscleGroupProgressBar: ProgressBar
    private lateinit var emojiFace: ImageView
    private lateinit var relaxLabel: TextView
    private lateinit var startButton: Button
    private lateinit var counterTextView: TextView
    private var cycleCount = 1
    private var isRelaxState = true
    private var countDownTimer: CountDownTimer? = null
    private val muscleGroups = listOf("Cheek and Jaw", "Forehead and Eyebrows", "Fists and Arms", "Feet and Legs", "Shoulders")
    private var currentGroupIndex = 0
    private var shouldCompleteExercise = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progressive_muscle_exercise)

        setupCloseButton()
        progressBar = findViewById(R.id.progressBar)
        emojiFace = findViewById(R.id.emojiFace)
        relaxLabel = findViewById(R.id.relaxLabel)
        startButton = findViewById(R.id.button3)
        startButton.isEnabled = true

        counterTextView = findViewById(R.id.textView8)

        progressBar.max = 1000

         muscleGroupProgressBar = findViewById(R.id.muscleGroupProgressBar)
        muscleGroupProgressBar.max = muscleGroups.size * 1000

        startButton.setOnClickListener {
            startProgress()
        }
    }
    private fun startProgress() {
        cycleCount = 1
        isRelaxState = false
        updateImageAndText(isRelaxState)
        updateCycleText(cycleCount)
        updateMuscleGroupText(muscleGroups[currentGroupIndex])
        updateMuscleGroupProgress()
        startButton.isEnabled = false

        progressBar.max = 1000
        progressBar.progress = 0


        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(50500, 100) {

            var elapsedTimeInCycle = 0L

            override fun onTick(millisUntilFinished: Long) {
                elapsedTimeInCycle += 100

                progressBar.progress = (elapsedTimeInCycle % 10000 / 10).toInt()

                // Toggle image and text every 5 seconds
                if (elapsedTimeInCycle % 5000 == 0L) {
                    isRelaxState = !isRelaxState
                    updateImageAndText(isRelaxState)
                }

                // Update cycle count text every 10 seconds
                if (elapsedTimeInCycle % 10000 == 0L && cycleCount <= 5) {
                    // Increment the cycle count at the end of each cycle.
                    if (cycleCount < 5) {
                        cycleCount++
                    }
                    updateCycleText(cycleCount)
                }
            }

            override fun onFinish() {

                progressBar.progress = 1000

                // Check if there are more muscle groups to go through
                if (currentGroupIndex < muscleGroups.size - 1) {
                    // Move to the next muscle group
                    currentGroupIndex++

                    // Reset for the next muscle group's first cycle
                    isRelaxState = false // Start with "stress" state
                    updateImageAndText(isRelaxState)
                    cycleCount = 1 // Reset cycle count
                    updateCycleText(cycleCount)

                    updateMuscleGroupText(muscleGroups[currentGroupIndex])
                    updateMuscleGroupProgress()
                    // Start the progress for the new muscle group
                    startProgress()
                } else {
                    // All muscle groups have been completed, handle the end of the session
                    if (shouldCompleteExercise) {
                        markExerciseAsComplete()
                        startButton.isEnabled = true
                    }
                }
            }
        }.start()
    }

    private fun updateMuscleGroupText(muscleGroup: String) {
        runOnUiThread {
            // Update TextView that shows the name of the current muscle group
            findViewById<TextView>(R.id.textView7).text = muscleGroup
        }
    }

    private fun updateImageAndText(isRelax: Boolean) {
        runOnUiThread {
            if (isRelax) {
                relaxLabel.text = "Relax"
                emojiFace.setImageResource(R.drawable.relax_image)
            } else {
                relaxLabel.text = "Stress"
                emojiFace.setImageResource(R.drawable.stress_image)
            }
        }
    }

    private fun updateCycleText(cycle: Int) {
        runOnUiThread {
            counterTextView.text = getString(R.string.cycle_text, cycle, 5)
        }
    }
    private fun markExerciseAsComplete() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("userProgress/$userId/dailyExercises/Progressive Muscle Exercise")
            val exerciseCompletionUpdate = mapOf("completed" to true, "date" to getCurrentDate())

            databaseReference.updateChildren(exerciseCompletionUpdate).addOnSuccessListener {
               // Toast.makeText(this, "Exercise marked as complete.", Toast.LENGTH_SHORT).show()
                saveUserExerciseScore(userId, "ProgressiveMuscle1", 5.0f)
                XpManager.awardXpForActivity(userId, "ProgressiveMuscle1", 5)
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

    private fun updateMuscleGroupProgress() {
        val totalProgress = (currentGroupIndex + 1) * 1000
        muscleGroupProgressBar.progress = totalProgress
    }

    private fun cleanupBeforeExit() {
        countDownTimer?.cancel()
        shouldCompleteExercise = false
    }
    private fun setupCloseButton() {
        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            cleanupBeforeExit()
            val intent = Intent(this@ProgressiveMuscleExerciseActivity, ExercisesActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}