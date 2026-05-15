package com.example.nimmaguru.repository

import android.util.Log
import com.example.nimmaguru.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationRepository {
    private val db = FirebaseFirestore.getInstance()

    fun sendNotification(notification: Notification) {
        val doc = db.collection("notifications").document()
        val data = mapOf(
            "id" to doc.id,
            "receiverId" to notification.receiverId,
            "title" to notification.title,
            "message" to notification.message,
            "timestamp" to notification.timestamp,
            "isRead" to notification.isRead
        )
        db.collection("notifications").document(doc.id).set(data)
    }

    fun markAsRead(notificationId: String) {
        if (notificationId.isEmpty()) return
        db.collection("notifications").document(notificationId)
            .update("isRead", true)
            .addOnSuccessListener {
                Log.d("NotificationRepo", "Successfully marked $notificationId as read")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationRepo", "Error marking notification $notificationId as read", e)
            }
    }

    fun markAllAsRead(userId: String) {
        if (userId.isEmpty()) return
        
        db.collection("notifications")
            .whereEqualTo("receiverId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot == null || snapshot.isEmpty) return@addOnSuccessListener
                
                val batch = db.batch()
                var count = 0
                snapshot.documents.forEach { doc ->
                    // Mark as read if it's explicitly false OR if the field is missing
                    if (doc.getBoolean("isRead") != true) {
                        batch.update(doc.reference, "isRead", true)
                        count++
                    }
                }
                
                if (count > 0) {
                    batch.commit().addOnSuccessListener {
                        Log.d("NotificationRepo", "Marked $count notifications as read for user $userId")
                    }.addOnFailureListener { e ->
                        Log.e("NotificationRepo", "Failed to commit batch mark all as read", e)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("NotificationRepo", "Failed to fetch notifications for markAllAsRead", e)
            }
    }

    fun getUnreadCount(userId: String, onResult: (Int) -> Unit) {
        if (userId.isEmpty()) {
            onResult(0)
            return
        }
        db.collection("notifications")
            .whereEqualTo("receiverId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(0)
                    return@addSnapshotListener
                }
                val count = snapshot?.documents?.count { it.getBoolean("isRead") != true } ?: 0
                onResult(count)
            }
    }

    fun getNotifications(userId: String, onResult: (List<Notification>) -> Unit) {
        if (userId.isEmpty()) {
            onResult(emptyList())
            return
        }
        db.collection("notifications")
            .whereEqualTo("receiverId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.map { doc ->
                    Notification(
                        id = doc.id,
                        receiverId = doc.getString("receiverId") ?: "",
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        isRead = doc.getBoolean("isRead") ?: false
                    )
                } ?: emptyList()
                
                onResult(list.sortedByDescending { it.timestamp })
            }
    }
}