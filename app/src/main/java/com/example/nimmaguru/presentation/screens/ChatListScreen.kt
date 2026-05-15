package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nimmaguru.auth.AuthRepository
import com.example.nimmaguru.model.User
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

// Helper data class for UI
data class ChatItem(
    val partner: User,
    val lastMessage: String,
    val lastTimestamp: Long
)

@Composable
fun ChatListScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val authRepository = AuthRepository()
    val currentUserId = authRepository.getCurrentUserId() ?: ""

    var chatItems by remember { mutableStateOf(listOf<ChatItem>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }

        // ✅ IMPROVED LOGIC: Pair User details with Last Message metadata
        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { result, error ->
                if (error != null) {
                    isLoading = false
                    Toast.makeText(context, "Error syncing chats", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (result == null || result.isEmpty) {
                    isLoading = false
                    chatItems = emptyList()
                    return@addSnapshotListener
                }

                val tempChatMap = mutableMapOf<String, Pair<String, Long>>()
                val participantIds = result.documents.mapNotNull { doc ->
                    val participants = doc.get("participants") as? List<*>
                    val otherId = participants?.firstOrNull { it != currentUserId }?.toString()
                    if (otherId != null) {
                        tempChatMap[otherId] = Pair(
                            doc.getString("lastMessage") ?: "",
                            doc.getLong("lastTimestamp") ?: 0L
                        )
                    }
                    otherId
                }.distinct()

                if (participantIds.isEmpty()) {
                    isLoading = false
                    chatItems = emptyList()
                    return@addSnapshotListener
                }

                // FETCH USER DETAILS
                db.collection("users")
                    .whereIn("uid", participantIds)
                    .get()
                    .addOnSuccessListener { userDocs ->
                        val users = userDocs.toObjects(User::class.java)
                        chatItems = users.map { user ->
                            val meta = tempChatMap[user.uid]
                            ChatItem(
                                partner = user,
                                lastMessage = meta?.first ?: "No messages yet",
                                lastTimestamp = meta?.second ?: 0L
                            )
                        }.sortedByDescending { it.lastTimestamp }
                        isLoading = false
                    }
                    .addOnFailureListener {
                        isLoading = false
                        Toast.makeText(context, "Failed to load user details", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Messages",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Recent conversations", color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (chatItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No messages found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(chatItems) { item ->
                    ChatPartnerItem(item) {
                        navController.navigate("chat/${item.partner.uid}/${item.partner.name}")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatPartnerItem(item: ChatItem, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val time = if (item.lastTimestamp > 0) sdf.format(Date(item.lastTimestamp)) else ""
    
    val placeholder = if (item.partner.gender == "Female") 
        "https://avatar.iran.liara.run/public/girl" 
    else 
        "https://avatar.iran.liara.run/public/boy"

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (item.partner.profileImage.isEmpty()) placeholder else item.partner.profileImage,
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.partner.name.ifEmpty { "User" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = time,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = item.lastMessage,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
