package com.example.nimmaguru.repository

import com.example.nimmaguru.model.User
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    fun saveUser(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersCollection.document(user.uid).set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUser(
        uid: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersCollection.document(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.toObject(User::class.java)
                onSuccess(user)
            }
            .addOnFailureListener { onFailure(it) }
    }
}