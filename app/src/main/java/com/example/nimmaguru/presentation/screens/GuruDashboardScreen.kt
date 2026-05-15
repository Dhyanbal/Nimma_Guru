package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nimmaguru.R
import com.example.nimmaguru.auth.AuthRepository
import com.example.nimmaguru.model.Booking
import com.example.nimmaguru.model.Notification
import com.example.nimmaguru.model.Session
import com.example.nimmaguru.repository.BookingRepository
import com.example.nimmaguru.repository.NotificationRepository
import com.example.nimmaguru.repository.SessionRepository
import com.example.nimmaguru.repository.UserRepository

@Composable
fun GuruDashboardScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_dashboard)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                    label = { Text(stringResource(R.string.chats)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_profile)) }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("createSession") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.new_session)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> GuruDashboardContent(navController)
                1 -> ChatListScreen(navController)
                2 -> SettingsScreen(navController)
            }
        }
    }
}

@Composable
fun GuruDashboardContent(navController: NavController) {
    val authRepository = AuthRepository()
    val userRepository = UserRepository()
    val bookingRepository = BookingRepository()
    val sessionRepository = SessionRepository()
    val context = LocalContext.current
    val userId = authRepository.getCurrentUserId()

    var guruName by remember { mutableStateOf("Guru") }
    var bookings by remember { mutableStateOf(listOf<Booking>()) }
    var mySessions by remember { mutableStateOf(listOf<Session>()) }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshData() {
        if (userId != null) {
            isLoading = true
            
            userRepository.getUser(userId,
                onSuccess = { user ->
                    if (user != null) {
                        guruName = user.name
                    }
                },
                onFailure = {}
            )

            bookingRepository.getBookingsForGuru(
                guruId = userId,
                onSuccess = { bks ->
                    bookings = bks
                    sessionRepository.getSessionsByGuru(
                        guruId = userId,
                        onSuccess = { ses ->
                            mySessions = ses
                            isLoading = false
                        },
                        onFailure = {
                            isLoading = false
                        }
                    )
                },
                onFailure = {
                    isLoading = false
                    Toast.makeText(context, context.getString(R.string.failed_load_bookings), Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    LaunchedEffect(userId) {
        refreshData()
    }

    val approvedCount = bookings.count { it.status == "Approved" }
    val pendingCount = bookings.count { it.status == "Pending" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.guru_dashboard_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.welcome_back_name, guruName), 
                    color = Color.Gray
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = stringResource(R.string.total_students),
                    count = approvedCount.toString(),
                    icon = Icons.Default.Groups,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.pending_requests),
                    count = pendingCount.toString(),
                    icon = Icons.Default.PendingActions,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.my_upcoming_sessions_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (mySessions.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.see_all),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { /* Future: Nav to sessions list */ }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                if (mySessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_sessions_scheduled), fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(mySessions) { session ->
                            MySessionCard(session) {
                                sessionRepository.deleteSession(session.id, {
                                    Toast.makeText(context, context.getString(R.string.session_deleted), Toast.LENGTH_SHORT).show()
                                    refreshData()
                                }, {
                                    Toast.makeText(context, context.getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
                                })
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.recent_booking_requests),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (bookings.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_student_bookings), color = Color.Gray)
                }
            }
        } else {
            items(bookings.sortedByDescending { it.status == "Pending" }) { booking ->
                GuruBookingItem(booking) {
                    refreshData()
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun StatCard(title: String, count: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = count, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MySessionCard(session: Session, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_session_title)) },
            text = { Text(stringResource(R.string.delete_session_msg)) },
            confirmButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    onDelete() 
                }) {
                    Text(stringResource(R.string.delete), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Card(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = session.subject,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = session.date, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = session.time, fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = session.location, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun GuruBookingItem(booking: Booking, onStatusUpdate: () -> Unit) {
    val bookingRepository = BookingRepository()
    val notificationRepository = NotificationRepository()
    val context = LocalContext.current
    var isUpdating by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = booking.studentName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = booking.studentName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${booking.date} ${stringResource(R.string.at_time)} ${booking.time}", 
                    fontSize = 13.sp, 
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(status = booking.status)
            }
            
            if (booking.status == "Pending") {
                Row {
                    IconButton(
                        onClick = {
                            isUpdating = true
                            bookingRepository.updateBookingStatus(booking.id, "Rejected", {
                                isUpdating = false
                                notificationRepository.sendNotification(
                                    Notification(
                                        receiverId = booking.studentId,
                                        title = "Booking Rejected",
                                        message = "Sorry, Guru ${booking.guruName} rejected your appointment."
                                    )
                                )
                                onStatusUpdate()
                            }, {
                                isUpdating = false
                                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                            })
                        },
                        modifier = Modifier.size(32.dp).background(Color(0xFFFFEBEE), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            isUpdating = true
                            bookingRepository.updateBookingStatus(booking.id, "Approved", {
                                isUpdating = false
                                notificationRepository.sendNotification(
                                    Notification(
                                        receiverId = booking.studentId,
                                        title = "Booking Approved!",
                                        message = "Guru ${booking.guruName} has accepted your appointment."
                                    )
                                )
                                onStatusUpdate()
                            }, {
                                isUpdating = false
                                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                            })
                        },
                        modifier = Modifier.size(32.dp).background(Color(0xFFE8F5E9), CircleShape)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    }
                }
            } else if (isUpdating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val backgroundColor = when (status) {
        "Approved" -> Color(0xFFE8F5E9)
        "Rejected" -> Color(0xFFFFEBEE)
        else -> Color(0xFFFFF3E0)
    }
    val contentColor = when (status) {
        "Approved" -> Color(0xFF2E7D32)
        "Rejected" -> Color(0xFFC62828)
        else -> Color(0xFFEF6C00)
    }
    
    val label = when (status) {
        "Approved" -> stringResource(R.string.status_approved)
        "Rejected" -> stringResource(R.string.status_rejected)
        else -> stringResource(R.string.status_pending)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
