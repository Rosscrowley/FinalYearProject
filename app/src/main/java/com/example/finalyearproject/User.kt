package com.example.finalyearproject

data class User(
    val name: String,
    val dob: String,
    val feared_sounds: List<String> = emptyList(),
    val firstLogin: Boolean = false,
    var lastLoginDateString: String,
    var consecutiveLogins: Int = 0, // Added field for consecutive login count
    var xp: Int = 0
) {
    // No-argument constructor for Firebase or other serialization/deserialization frameworks
    constructor() : this("", "", emptyList(), false, "", 0, 0)

}
