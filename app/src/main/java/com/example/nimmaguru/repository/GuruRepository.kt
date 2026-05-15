package com.example.nimmaguru.repository

import com.example.nimmaguru.model.Guru
import com.example.nimmaguru.model.User
import com.google.firebase.firestore.FirebaseFirestore

class GuruRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getGurus(
        onSuccess: (List<Guru>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users")
            .whereEqualTo("role", "GURU")
            .whereEqualTo("status", "Approved")
            .get()
            .addOnSuccessListener { result ->
                val guruList = mutableListOf<Guru>()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    val guru = mapUserToGuru(document.id, user)
                    guruList.add(guru)
                }
                onSuccess(guruList)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun getGuruById(
        guruId: String,
        onSuccess: (Guru) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(guruId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        onSuccess(mapUserToGuru(document.id, user))
                    } else {
                        onFailure(Exception("User data is null"))
                    }
                } else {
                    onFailure(Exception("Guru document does not exist"))
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun getAllGurusForAdmin(
        onSuccess: (List<Guru>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users")
            .whereEqualTo("role", "GURU")
            .get()
            .addOnSuccessListener { result ->
                val guruList = mutableListOf<Guru>()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    val guru = mapUserToGuru(document.id, user)
                    guruList.add(guru)
                }
                onSuccess(guruList)
            }
            .addOnFailureListener { onFailure(it) }
    }

    private fun mapUserToGuru(id: String, user: User): Guru {
        return Guru(
            id = id,
            name = user.name,
            subject = user.subject,
            gender = user.gender,
            village = user.village,
            district = user.district,
            about = user.bio,
            imageUrl = user.profileImage,
            status = user.status,
            availableHours = user.availableHours,
            venue = user.venue
        )
    }

    fun updateGuruStatus(
        guruId: String,
        status: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(guruId).update("status", status)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
