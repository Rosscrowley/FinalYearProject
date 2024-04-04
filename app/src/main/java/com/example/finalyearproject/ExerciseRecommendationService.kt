package com.example.finalyearproject

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExerciseRecommendationService(private val exerciseRepository: ExerciseRepository) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchAndRecommendExercises(userId: String): List<ExerciseActivity> {
        // Fetch all exercises and user's exercise scores from the database
        val allExercises = exerciseRepository.fetchAllExercises()
        val userExerciseScores = exerciseRepository.fetchUserExerciseScores(userId)

        // Identify untried exercises
        val untriedExercises = allExercises.keys - userExerciseScores.keys

        // Early recommendation for users with minimal activity: prioritize untried exercises
        val maxRecommendations = 3
        var recommendations = mutableListOf<String>()

        // Add untried exercises to recommendations, up to the max count
        recommendations.addAll(untriedExercises.shuffled().take(maxRecommendations))

        // If we haven't filled our recommendation quota with untried exercises, fill the rest with tried exercises
        if (recommendations.size < maxRecommendations) {

            val today = LocalDate.now()
            val oneWeekAgo = today.minusDays(7)
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            val triedExercisePriorities = userExerciseScores.mapValues { (exerciseId, scores) ->
                val frequency = scores.count {
                    val attemptDate = LocalDate.parse(it.date, dateFormatter)
                    !(attemptDate.isBefore(oneWeekAgo) || attemptDate.isAfter(today))
                }
                val averageScore = scores.map { it.score }.average()
                val priority = (1.0 / (frequency + 1)) + (1.0 - averageScore / 5.0)

                Log.d("ExerciseRec", "Exercise: $exerciseId, Frequency past week: $frequency, Average Score: $averageScore, Priority: $priority")

                priority
            }

            recommendations.addAll(triedExercisePriorities.toList().sortedByDescending { it.second }
                .map { it.first }
                .filterNot { recommendations.contains(it) }
                .take(maxRecommendations - recommendations.size))
        }

        // Convert exercise IDs to names for the final recommendation list
        val recommendedExerciseNames = recommendations.mapNotNull { allExercises[it] }

        Log.d("ExerciseRec", "Recommended exercises: $recommendedExerciseNames")

        val recommendedExercises = recommendedExerciseNames.mapNotNull { exerciseName ->
            val className = exerciseToActivityMap[exerciseName]?.name // Directly get the fully qualified class name as a string
            if (className != null) {
                ExerciseActivity("id_placeholder", exerciseName, className)
            } else null
        }

        Log.d("ExerciseRecService", "All Exercises: $allExercises")
        Log.d("ExerciseRecService", "User's Exercise Scores: $userExerciseScores")
        Log.d("ExerciseRecService", "Untried Exercises: $untriedExercises")
        Log.d("ExerciseRecService", "Recommendations before prioritization: $recommendations")


        Log.d("ExerciseRecService", "Final Recommended Exercises: $recommendedExercises")

        return recommendedExercises
    }

    private val exerciseToActivityMap = mapOf(

        "Breathing Exercise" to BreathingExerciseActivity::class.java,
        "Reading Comprehension" to ChooseReadingTopicActivity::class.java,
        "Tongue Twisters" to ChooseTongueTwisterActivity::class.java,
        "Syllable Counting" to SyllCountInfo::class.java,
        "Flexible Rate Technique" to FlexRateTechExerciseActivity::class.java,
        "Progressive Muscle Exercise" to ProgressiveMuscleExerciseActivity::class.java
    )

}
