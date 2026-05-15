package com.example.nimmaguru.model

data class Review(
    val studentName: String = "",
    val comment: String = "",
    val rating: Int = 5,
    val guruId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)