package com.example.finalyearproject

data class SoundItem(
    val sound: String,
    val examples: String,
    var isSelected: Boolean = false // This tracks whether the sound is selected
)
