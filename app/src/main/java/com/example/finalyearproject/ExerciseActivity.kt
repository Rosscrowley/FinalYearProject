package com.example.finalyearproject

data class ExerciseActivity(
    val id: String,
    val name: String,
    val activityClassName: String? = null,
    var completed: Boolean = false
)
