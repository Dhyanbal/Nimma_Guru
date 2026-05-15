package com.example.nimmaguru.model

import java.util.UUID

data class Booking(
    val id: String = UUID.randomUUID().toString(),
    val studentId: String = "",
    val studentName: String = "",
    val guruId: String = "",
    val guruName: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "Pending"
)