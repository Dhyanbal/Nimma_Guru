package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nimmaguru.R
import com.example.nimmaguru.model.Notification
import com.example.nimmaguru.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val notificationRepository = NotificationRepository()

    var gurus by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshTrigger) {
        db.collection("users")
            .whereEqualTo("role", "GURU")
            .get()
            .addOnSuccessListener { result ->
                gurus = result.documents.map { it.data!! }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(context, "Failed to load gurus", Toast.LENGTH_SHORT).show()
            }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.delete_guru)) },
            text = { Text(stringResource(R.string.delete_confirm_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uid = showDeleteDialog!!
                        db.collection("users").document(uid).delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, context.getString(R.string.guru_deleted_success), Toast.LENGTH_SHORT).show()
                                showDeleteDialog = null
                                refreshTrigger++
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, context.getString(R.string.guru_delete_failed), Toast.LENGTH_SHORT).show()
                                showDeleteDialog = null
                            }
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admin_panel), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.manage_verify_gurus),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp),
                fontSize = 14.sp
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(gurus) { guru ->
                        AdminGuruCard(
                            guru = guru,
                            onAction = { status ->
                                val uid = guru["uid"].toString()
                                db.collection("users")
                                    .document(uid)
                                    .update("status", status)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, context.getString(R.string.guru_status_toast, status), Toast.LENGTH_SHORT).show()
                                        
                                        notificationRepository.sendNotification(
                                            Notification(
                                                receiverId = uid,
                                                title = if (status == "Approved") context.getString(R.string.account_approved_title) else context.getString(R.string.account_rejected_title),
                                                message = if (status == "Approved") context.getString(R.string.account_approved_msg) 
                                                          else context.getString(R.string.account_rejected_msg)
                                            )
                                        )

                                        refreshTrigger++
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Action failed", Toast.LENGTH_SHORT).show()
                                    }
                            },
                            onDelete = {
                                showDeleteDialog = guru["uid"].toString()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminGuruCard(
    guru: Map<String, Any>,
    onAction: (String) -> Unit,
    onDelete: () -> Unit
) {
    val name = guru["name"] ?: "No Name"
    val email = guru["email"] ?: ""
    val status = guru["status"] ?: "Pending"
    val profileImage = guru["profileImage"]?.toString() ?: ""
    val gender = guru["gender"]?.toString() ?: "Male"

    val placeholder = if (gender == "Female") 
        "https://avatar.iran.liara.run/public/girl" 
    else 
        "https://avatar.iran.liara.run/public/boy"

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = if (profileImage.isEmpty()) placeholder else profileImage,
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(text = email.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = when (status) {
                    "Approved" -> Color(0xFFC8E6C9)
                    "Rejected" -> Color(0xFFFFCDD2)
                    else -> MaterialTheme.colorScheme.secondaryContainer
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = status.toString(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (status) {
                        "Approved" -> Color(0xFF2E7D32)
                        "Rejected" -> Color(0xFFC62828)
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (status != "Rejected") {
                    OutlinedButton(
                        onClick = { onAction("Rejected") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.reject))
                    }
                }

                if (status != "Approved") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAction("Approved") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.approve))
                    }
                }
            }
        }
    }
}
