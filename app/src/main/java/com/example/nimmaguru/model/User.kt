package com.example.nimmaguru.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val gender: String = "",
    val role: String = "STUDENT", // "STUDENT" | "GURU" | "ADMIN"
    val status: String = "Pending",
    val bio: String = "",
    val profileImage: String = "",
    val subject: String = "",
    val village: String = "",
    val district: String = "",
    val availableHours: String = "Weekends: 9:00 AM - 1:00 PM",
    val venue: String = "Local Community Center (Samudaya Bhavana)",
    val favorites: List<String> = emptyList() // Added to persist favorites
)
