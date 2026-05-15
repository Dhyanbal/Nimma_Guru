package com.example.nimmaguru.repository

import com.example.nimmaguru.model.Message
import com.example.nimmaguru.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val notificationRepository = NotificationRepository()

    fun sendMessage(
        message: Message,
        senderName: String, // Added senderName for notification
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val chatId = getChatId(message.senderId, message.receiverId)
        
        if (message.senderId.isEmpty() || message.receiverId.isEmpty()) {
            onFailure(Exception("Invalid User IDs"))
            return
        }

        val chatMetadata = mapOf(
            "participants" to listOf(message.senderId, message.receiverId),
            "lastMessage" to message.text,
            "lastTimestamp" to message.timestamp
        )

        db.collection("chats").document(chatId)
            .set(chatMetadata, SetOptions.merge())
            .addOnSuccessListener {
                db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener {
                        // ✅ TRIGGER NOTIFICATION
                        notificationRepository.sendNotification(
                            Notification(
                                receiverId = message.receiverId,
                                title = "New Message",
                                message = "You have a new message from $senderName"
                            )
                        )
                        onSuccess()
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getMessages(
        senderId: String,
        receiverId: String,
        onMessagesReceived: (List<Message>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val chatId = getChatId(senderId, receiverId)
        
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                
                val messagesList = mutableListOf<Message>()
                snapshot?.documents?.forEach { doc ->
                    val msg = Message(
                        senderId = doc.getString("senderId") ?: "",
                        receiverId = doc.getString("receiverId") ?: "",
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                    messagesList.add(msg)
                }
                onMessagesReceived(messagesList)
            }
    }

    private fun getChatId(id1: String, id2: String): String {
        return if (id1 < id2) "${id1}_${id2}" else "${id2}_${id1}"
    }
}