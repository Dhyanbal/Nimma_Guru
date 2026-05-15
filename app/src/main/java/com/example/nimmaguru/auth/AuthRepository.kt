package com.example.nimmaguru.auth

import com.google.firebase.auth.FirebaseAuth

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    fun registerUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(email, password)

            .addOnSuccessListener {

                onSuccess()
            }

            .addOnFailureListener {

                onFailure(it)
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        auth.signInWithEmailAndPassword(email, password)

            .addOnSuccessListener {

                onSuccess()
            }

            .addOnFailureListener {

                onFailure(it)
            }
    }

    fun isUserLoggedIn(): Boolean {

        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun fetchUserRole(
        uid: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(uid).get()
            .addOnSuccessListener {
                val role = it.getString("role") ?: "Student"
                onSuccess(role)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun logout() {

        auth.signOut()
    }
}