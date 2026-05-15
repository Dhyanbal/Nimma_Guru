package com.example.nimmaguru.utils

import androidx.compose.runtime.mutableStateOf

object ThemeManager {

    val isDarkMode =
        mutableStateOf(false)

    fun toggleTheme() {

        isDarkMode.value =
            !isDarkMode.value
    }
}