package com.example.nimmaguru.model

data class Guru(
    val id: String = "",
    val name: String = "",
    val subject: String = "",
    val gender: String = "", // Added gender field
    val village: String = "",
    val district: String = "",
    val about: String = "",
    val imageUrl: String = "",
    val availableHours: String = "Weekends: 9:00 AM - 1:00 PM",
    val venue: String = "Local Community Center (Samudaya Bhavana)",
    val reviews: List<Review> = emptyList(),
    val status: String = "Pending" // "Pending", "Approved", "Rejected"
)