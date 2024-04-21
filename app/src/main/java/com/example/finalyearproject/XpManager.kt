package com.example.finalyearproject

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object XpManager {
    private const val STANDARD_XP = 100

    fun awardXpForActivity(userId: String, activityType: String, durationOrIntensity: Int) {
        val xpAwarded = when (activityType) {
            "breathingExercise", "readingExercise", "syllableCounting", "tongueTwisterExercise", "ProgressiveMuscle1", "FlexibleRateTechnique" -> durationOrIntensity * STANDARD_XP
            else -> 0
        }

        if (xpAwarded > 0) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            updateUserXp(userId, xpAwarded, currentDate)
        }
    }

    private fun updateUserXp(userId: String, xpToAdd: Int, currentDate: String) {
        val userRef = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("users/$userId")

        // Update the total XP
        userRef.child("xp").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentXp = mutableData.getValue(Int::class.java) ?: 0
                mutableData.value = currentXp + xpToAdd
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {
                if (databaseError != null) {
                    Log.e("updateUserXp", "Failed to update total XP: ${databaseError.message}")
                } else {
                    Log.d("updateUserXp", "Total XP successfully updated.")
                }
            }
        })

        // Update the daily XP
        userRef.child("dailyXp").child(currentDate).runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentDailyXp = mutableData.getValue(Int::class.java) ?: 0
                mutableData.value = currentDailyXp + xpToAdd
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {
                if (databaseError != null) {
                    Log.e("updateUserXp", "Failed to update daily XP: ${databaseError.message}")
                } else {
                    Log.d("updateUserXp", "Daily XP successfully updated.")
                }
            }
        })
    }
}
