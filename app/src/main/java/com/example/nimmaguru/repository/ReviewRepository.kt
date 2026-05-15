package com.example.nimmaguru.repository

import com.example.nimmaguru.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReviewRepository {

    private val db = FirebaseFirestore.getInstance()

    fun addReview(
        review: Review,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("reviews").add(review)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getReviewsForGuru(
        guruId: String,
        onSuccess: (List<Review>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("reviews")
            .whereEqualTo("guruId", guruId)
            .get()
            .addOnSuccessListener { snapshot ->
                val reviewsList = mutableListOf<Review>()
                for (doc in snapshot) {
                    try {
                        val review = doc.toObject(Review::class.java)
                        reviewsList.add(review)
                    } catch (e: Exception) {
                        // Skip malformed reviews
                    }
                }
                // Sort manually by timestamp if needed
                onSuccess(reviewsList.sortedByDescending { it.timestamp })
            }
            .addOnFailureListener { onFailure(it) }
    }
}