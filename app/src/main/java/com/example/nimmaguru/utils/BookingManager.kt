package com.example.nimmaguru.utils

import androidx.compose.runtime.mutableStateListOf
import com.example.nimmaguru.model.Booking

object BookingManager {

    val bookings =
        mutableStateListOf<Booking>()

    fun addBooking(
        booking: Booking
    ) {
        bookings.add(booking)
    }

    fun cancelBooking(bookingId: String) {
        bookings.removeIf { it.id == bookingId }
    }
}