package com.example.nimmaguru.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nimmaguru.R
import com.example.nimmaguru.auth.AuthRepository
import com.example.nimmaguru.model.Notification
import com.example.nimmaguru.repository.NotificationRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val authRepository = AuthRepository()
    val notificationRepository = remember { NotificationRepository() }
    val userId = authRepository.getCurrentUserId() ?: ""

    var notifications by remember { mutableStateOf(listOf<Notification>()) }
    var isLoading by remember { mutableStateOf(true) }

    // 1. Fetch notifications
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            notificationRepository.getNotifications(userId) {
                notifications = it
                isLoading = false
            }
        }
    }

    // 2. Mark as read immediately when unread notifications are detected
    LaunchedEffect(notifications) {
        if (userId.isNotEmpty() && notifications.any { !it.isRead }) {
            notificationRepository.markAllAsRead(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = { notificationRepository.markAllAsRead(userId) }) {
                            Text(stringResource(R.string.mark_all_read), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Text(stringResource(R.string.no_notifications), color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = { 
                            if (!notification.isRead) {
                                notificationRepository.markAsRead(notification.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val date = sdf.format(Date(notification.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        elevation = if (notification.isRead) CardDefaults.cardElevation(0.dp) else CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (notification.isRead) Color.Gray.copy(alpha = 0.2f) 
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (notification.isRead) Color.Gray else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title, 
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold, 
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = date, 
                        fontSize = 11.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = notification.message, 
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}