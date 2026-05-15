package com.example.nimmaguru.repository

import com.example.nimmaguru.model.Session
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SessionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val sessionsCollection = db.collection("sessions")

    fun getUpcomingSessions(
        onSuccess: (List<Session>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        sessionsCollection
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val sessions = snapshot.toObjects(Session::class.java)
                onSuccess(sessions)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getSessionsByGuru(
        guruId: String,
        onSuccess: (List<Session>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        sessionsCollection
            .whereEqualTo("guruId", guruId)
            .get()
            .addOnSuccessListener { snapshot ->
                val sessions = snapshot.toObjects(Session::class.java)
                onSuccess(sessions)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun addSession(
        session: Session,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        sessionsCollection.document(session.id).set(session)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteSession(
        sessionId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        sessionsCollection.document(sessionId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
