package com.example.nimmaguru.repository

import com.example.nimmaguru.model.Booking
import com.google.firebase.firestore.FirebaseFirestore

class BookingRepository {

    private val db = FirebaseFirestore.getInstance()
    private val bookingsCollection = db.collection("bookings")

    fun addBooking(
        booking: Booking,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        bookingsCollection.document(booking.id).set(booking)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getBookingsForStudent(
        studentId: String,
        onSuccess: (List<Booking>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        bookingsCollection.whereEqualTo("studentId", studentId).get()
            .addOnSuccessListener { snapshot ->
                val bookings = snapshot.toObjects(Booking::class.java)
                onSuccess(bookings)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun cancelBooking(
        bookingId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        bookingsCollection.document(bookingId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getBookingsForGuru(
        guruId: String,
        onSuccess: (List<Booking>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        bookingsCollection.whereEqualTo("guruId", guruId).get()
            .addOnSuccessListener { snapshot ->
                val bookings = snapshot.toObjects(Booking::class.java)
                onSuccess(bookings)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateBookingStatus(
        bookingId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        bookingsCollection.document(bookingId).update("status", newStatus)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}