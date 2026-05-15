package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nimmaguru.R
import com.example.nimmaguru.model.Session
import com.example.nimmaguru.repository.SessionRepository
import com.example.nimmaguru.utils.ReminderManager
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController? = null) {
    val sessionRepository = SessionRepository()
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary

    var sessions by remember { mutableStateOf(listOf<Session>()) }
    var isLoading by remember { mutableStateOf(true) }
    
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    var showDetailSheet by remember { mutableStateOf<Session?>(null) }

    LaunchedEffect(Unit) {
        sessionRepository.getUpcomingSessions(
            onSuccess = {
                sessions = it
                isLoading = false
            },
            onFailure = {
                isLoading = false
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showDetailSheet != null) {
        val session = showDetailSheet!!
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = null },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            SessionDetailContent(
                session = session,
                onViewProfile = { guruId ->
                    if (!guruId.isNullOrBlank()) {
                        showDetailSheet = null
                        navController?.navigate("profile/$guruId")
                    } else {
                        Toast.makeText(context, "Guru profile not available for this session", Toast.LENGTH_SHORT).show()
                    }
                },
                onBookNow = {
                    // Schedule a reminder 1 day before the community session
                    ReminderManager.scheduleReminder(
                        context = context,
                        bookingId = session.id,
                        dateStr = session.date,
                        timeStr = session.time,
                        subject = session.subject
                    )
                    Toast.makeText(context, "Joined! Reminder set for tomorrow.", Toast.LENGTH_SHORT).show()
                    showDetailSheet = null
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.community_session_calendar), 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 24.sp, 
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { navController?.navigate("notifications") }) {
                    Icon(
                        Icons.Outlined.Notifications, 
                        null, 
                        modifier = Modifier.size(28.dp), 
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = primaryColor, modifier = Modifier.size(32.dp))
                }
                
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = primaryColor, modifier = Modifier.size(32.dp))
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    sessions = sessions,
                    onDateSelected = { selectedDate = it }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.nav_sessions) + ": " + selectedDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 12.dp)
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp), 
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            val dailySessions = sessions.filter { isSameDate(it.date, selectedDate) }

            if (dailySessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp), 
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_sessions_found), 
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(dailySessions) { session ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        SessionCard(session) {
                            showDetailSheet = session
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CalendarGrid(currentMonth: YearMonth, selectedDate: LocalDate, sessions: List<Session>, onDateSelected: (LocalDate) -> Unit) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
    val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    Column(modifier = Modifier.padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day, 
                    modifier = Modifier.weight(1f), 
                    textAlign = TextAlign.Center, 
                    fontSize = 11.sp, 
                    color = MaterialTheme.colorScheme.primary, 
                    fontWeight = FontWeight.Black
                )
            }
        }
        
        var dayCounter = 1
        for (row in 0..5) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val currentDayIndex = row * 7 + col
                    if (currentDayIndex < firstDayOfWeek || dayCounter > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(60.dp))
                        if (currentDayIndex >= firstDayOfWeek) dayCounter++
                    } else {
                        val date = currentMonth.atDay(dayCounter)
                        CalendarDay(
                            day = dayCounter.toString(),
                            isSelected = date == selectedDate,
                            hasSessions = sessions.any { isSameDate(it.date, date) },
                            modifier = Modifier.weight(1f),
                            onClick = { onDateSelected(date) }
                        )
                        dayCounter++
                    }
                }
            }
            if (dayCounter > daysInMonth) break
        }
    }
}

@Composable
fun CalendarDay(day: String, isSelected: Boolean, hasSessions: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .height(65.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day, 
                fontSize = 17.sp, 
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, 
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
        if (hasSessions) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            )
        }
    }
}

@Composable
fun SessionCard(session: Session, onClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp, 60.dp), 
                shape = RoundedCornerShape(12.dp), 
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, 
                    verticalArrangement = Arrangement.Center
                ) {
                    val parts = session.time.uppercase().split(" ")
                    Text(
                        text = parts.getOrNull(0) ?: "", 
                        fontWeight = FontWeight.ExtraBold, 
                        fontSize = 16.sp, 
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = parts.getOrNull(1) ?: "", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 11.sp, 
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.subject, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 17.sp, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.guru) + ": ${session.guruName}", 
                    fontSize = 14.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (session.location.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn, 
                            null, 
                            modifier = Modifier.size(16.dp), 
                            tint = primaryColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = session.location, 
                            fontSize = 13.sp, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                null, 
                tint = primaryColor, 
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun SessionDetailContent(session: Session, onViewProfile: (String) -> Unit, onBookNow: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text(text = session.subject, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = session.location, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(R.string.guru) + ": ${session.guruName}", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onBookNow, 
            modifier = Modifier.fillMaxWidth().height(56.dp), 
            shape = RoundedCornerShape(16.dp)
        ) { 
            Text(stringResource(R.string.welcome), fontWeight = FontWeight.Bold, fontSize = 16.sp) 
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = { onViewProfile(session.guruId) }, 
            modifier = Modifier.fillMaxWidth().height(56.dp), 
            shape = RoundedCornerShape(16.dp)
        ) { 
            Text(stringResource(R.string.view_profile), fontWeight = FontWeight.Bold, fontSize = 16.sp) 
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun isSameDate(sessionDate: String, localDate: LocalDate): Boolean {
    val d = localDate.toString()
    if (sessionDate == d) return true
    val dayNum = localDate.dayOfMonth.toString()
    val monthName = localDate.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault())).lowercase()
    return sessionDate.lowercase().contains(dayNum) && sessionDate.lowercase().contains(monthName)
}
