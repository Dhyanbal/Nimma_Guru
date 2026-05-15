package com.example.nimmaguru.utils

import com.example.nimmaguru.model.Guru

// This file is deprecated and can be safely deleted.
// AI Assistant feature has been removed.
class SmartSearchManager {
    suspend fun searchGurus(query: String, gurus: List<Guru>): List<Guru> {
        return gurus.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.subject.contains(query, ignoreCase = true) ||
                    it.village.contains(query, ignoreCase = true)
        }
    }
}
