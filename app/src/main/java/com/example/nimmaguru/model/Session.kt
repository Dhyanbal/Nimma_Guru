package com.example.nimmaguru.model

import java.util.UUID

data class Session(
    val id: String = UUID.randomUUID().toString(),
    val guruId: String = "", // Added guruId for interactivity
    val guruName: String = "",
    val subject: String = "",
    val location: String = "",
    val date: String = "", // e.g., "2025-05-10" or "Sunday, 15 Oct"
    val time: String = "", // e.g., "10:00 AM"
    val description: String = ""
)
