package com.example.finalyearproject

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

object XpManager {
    private const val STANDARD_XP = 100

    fun awardXpForActivity(userId: String, activityType: String, durationOrIntensity: Int) {
        val xpAwarded = when (activityType) {
            "breathingExercise" -> durationOrIntensity * STANDARD_XP
            "readingExercise" -> durationOrIntensity * STANDARD_XP
            "syllableCounting" -> durationOrIntensity * STANDARD_XP
            "tongueTwisterExercise" -> durationOrIntensity * STANDARD_XP
            "ProgressiveMuscle1" -> durationOrIntensity * STANDARD_XP
           "FlexibleRateTechnique" -> durationOrIntensity * STANDARD_XP
            else -> 0
        }

        if (xpAwarded > 0) {
            updateUserXp(userId, xpAwarded)
        }
    }

    private fun updateUserXp(userId: String, xpToAdd: Int) {
        val userRef = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("users/$userId")
        userRef.child("xp").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentXp = mutableData.getValue(Int::class.java) ?: 0
                mutableData.value = currentXp + xpToAdd
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {
                if (databaseError != null) {
                    Log.e("updateUserXp", "Failed to update XP: ${databaseError.message}")
                } else {
                    Log.d("updateUserXp", "XP successfully updated.")
                }
            }
        })
    }
}