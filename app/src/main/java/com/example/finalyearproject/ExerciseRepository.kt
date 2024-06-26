package com.example.finalyearproject

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class ExerciseRepository {

    suspend fun fetchAllExercises(): Map<String, String> {
        val exercisesRef = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("Exercises")
        val snapshot = exercisesRef.get().await()
        val result = snapshot.children.associate { it.key!! to it.child("name").getValue(String::class.java)!! }
        Log.d("ExerciseRepo", "Fetched exercises: $result")
        return result
    }

    suspend fun fetchUserExerciseScores(userId: String): Map<String, List<ExerciseScore>> {
        val scoresRef = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("userScores/$userId")
        val snapshot = scoresRef.get().await()
        val exerciseData = mutableMapOf<String, MutableList<ExerciseScore>>()
        snapshot.children.forEach { exerciseTypeSnapshot ->
            val exerciseScores = mutableListOf<ExerciseScore>()
            exerciseTypeSnapshot.children.mapNotNullTo(exerciseScores) { scoreSnapshot ->
                scoreSnapshot.getValue(ExerciseScore::class.java)
            }
            exerciseData[exerciseTypeSnapshot.key ?: "unknown"] = exerciseScores
        }
        Log.d("ExerciseRepo", "Fetched scores for user $userId: $exerciseData")
        return exerciseData
    }
}