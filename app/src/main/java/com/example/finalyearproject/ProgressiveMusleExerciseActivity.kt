package com.example.finalyearproject
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgressiveMuscleExerciseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progressive_muscle_exercise)

        val finishButton: Button = findViewById(R.id.button2)
        finishButton.setOnClickListener {
            markExerciseAsComplete()
        }
    }

    private fun markExerciseAsComplete() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("userProgress/$userId/dailyExercises/Progressive Muscle Exercise")
            val exerciseCompletionUpdate = mapOf("completed" to true, "date" to getCurrentDate())

            databaseReference.updateChildren(exerciseCompletionUpdate).addOnSuccessListener {
                Toast.makeText(this, "Exercise marked as complete.", Toast.LENGTH_SHORT).show()
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
}