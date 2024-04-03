package com.example.finalyearproject

import androidx.appcompat.app.AppCompatActivity

data class ExerciseActivity(
    val name: String,
    val activityClass: Class<out AppCompatActivity>
)
