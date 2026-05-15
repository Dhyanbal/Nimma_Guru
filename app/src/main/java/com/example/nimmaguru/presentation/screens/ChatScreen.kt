package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nimmaguru.R
import com.example.nimmaguru.auth.AuthRepository
import com.example.nimmaguru.model.Message
import com.example.nimmaguru.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    receiverId: String,
    receiverName: String,
    navController: NavController
) {
    val context = LocalContext.current
    val authRepository = AuthRepository()
    val chatRepository = ChatRepository()
    val db = FirebaseFirestore.getInstance()
    val senderId = authRepository.getCurrentUserId() ?: ""

    var messages by remember { mutableStateOf(listOf<Message>()) }
    var text by remember { mutableStateOf("") }
    var senderName by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(senderId) {
        if (senderId.isNotEmpty()) {
            db.collection("users").document(senderId).get()
                .addOnSuccessListener { senderName = it.getString("name") ?: "Someone" }
        }
    }

    LaunchedEffect(senderId, receiverId) {
        if (senderId.isNotEmpty() && receiverId.isNotEmpty()) {
            chatRepository.getMessages(senderId, receiverId, 
                onMessagesReceived = { messages = it },
                onError = { 
                    Toast.makeText(context, context.getString(R.string.connection_lost), Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(receiverName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.online), fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.type_message)) },
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (text.trim().isNotEmpty()) {
                                val messageText = text.trim()
                                val message = Message(
                                    senderId = senderId,
                                    receiverId = receiverId,
                                    text = messageText,
                                    timestamp = System.currentTimeMillis()
                                )
                                text = "" // Clear early for better UX
                                chatRepository.sendMessage(
                                    message = message,
                                    senderName = senderName,
                                    onSuccess = { },
                                    onFailure = { 
                                        text = messageText // Restore on failure
                                        Toast.makeText(context, context.getString(R.string.failed_to_send), Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { paddingValues ->
        val groupedMessages = remember(messages) {
            messages.groupBy { 
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it.timestamp)) 
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface),
            state = listState,
            contentPadding = PaddingValues(16.dp)
        ) {
            groupedMessages.forEach { (date, messagesInDate) ->
                item(key = date) {
                    DateHeader(date)
                }
                items(messagesInDate) { message ->
                    MessageBubble(message, message.senderId == senderId)
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = date,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMe: Boolean) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val time = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.text,
                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = time,
                    fontSize = 10.sp,
                    color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
