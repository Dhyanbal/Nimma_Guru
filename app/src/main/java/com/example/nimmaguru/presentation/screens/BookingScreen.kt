package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nimmaguru.auth.AuthRepository
import com.example.nimmaguru.model.Booking
import com.example.nimmaguru.model.Notification
import com.example.nimmaguru.repository.BookingRepository
import com.example.nimmaguru.repository.NotificationRepository
import com.example.nimmaguru.utils.ReminderManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    guruId: String,
    guruName: String,
    navController: NavController
) {
    val context = LocalContext.current
    val authRepository = AuthRepository()
    val bookingRepository = BookingRepository()
    val notificationRepository = NotificationRepository()

    var studentName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Appointment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Secure your learning slot",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Guru: $guruName",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = studentName,
                onValueChange = { studentName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Student Name") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = selectedDate,
                onValueChange = { selectedDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (Example: 12 May)") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = selectedTime,
                onValueChange = { selectedTime = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Time (Example: 10:00 AM)") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Button(
                    onClick = {
                        val userId = authRepository.getCurrentUserId()
                        if (userId == null) {
                            Toast.makeText(context, "Please login to book", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (studentName.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true
                        val booking = Booking(
                            studentId = userId,
                            studentName = studentName,
                            guruId = guruId,
                            guruName = guruName,
                            date = selectedDate,
                            time = selectedTime
                        )

                        bookingRepository.addBooking(
                            booking = booking,
                            onSuccess = {
                                isLoading = false
                                
                                // Schedule a reminder for the student (1 day before)
                                ReminderManager.scheduleReminder(
                                    context = context,
                                    bookingId = booking.id,
                                    dateStr = selectedDate,
                                    timeStr = selectedTime,
                                    subject = "Appointment with $guruName"
                                )

                                notificationRepository.sendNotification(
                                    Notification(
                                        receiverId = guruId,
                                        title = "New Booking Request",
                                        message = "Student $studentName requested an appointment on $selectedDate at $selectedTime."
                                    )
                                )
                                Toast.makeText(context, "Booking Confirmed!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onFailure = {
                                isLoading = false
                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Confirm Booking", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
