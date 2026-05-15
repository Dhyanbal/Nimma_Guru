package com.example.nimmaguru.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nimmaguru.R
import com.example.nimmaguru.model.Guru
import com.example.nimmaguru.model.User
import com.example.nimmaguru.repository.GuruRepository
import com.example.nimmaguru.repository.NotificationRepository
import com.example.nimmaguru.repository.UserRepository
import com.example.nimmaguru.utils.FavoritesManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    BottomNavItem(stringResource(R.string.home), Icons.Filled.Home, Icons.Outlined.Home),
                    BottomNavItem(stringResource(R.string.nav_sessions), Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
                    BottomNavItem(stringResource(R.string.nav_thanks), Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
                    BottomNavItem(stringResource(R.string.nav_fame), Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
                    BottomNavItem(stringResource(R.string.nav_profile), Icons.Filled.Person, Icons.Outlined.PersonOutline)
                )

                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(text = item.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("chatList") },
                containerColor = primaryColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "Chat")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (selectedTab) {
                0 -> HomeContent(navController)
                1 -> CalendarScreen(navController)
                2 -> FavoritesScreen(navController)
                3 -> FameScreen(navController, isTab = true)
                4 -> SettingsScreen(navController)
            }
        }
    }
}

data class BottomNavItem(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

@Composable
fun HomeContent(navController: NavController) {
    var guruList by remember { mutableStateOf(listOf<Guru>()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var unreadNotifications by remember { mutableIntStateOf(0) }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val auth = FirebaseAuth.getInstance()
    val userRepository = UserRepository()
    val guruRepository = GuruRepository()
    val notificationRepository = remember { NotificationRepository() }

    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance().time
            delay(1000)
        }
    }

    val dateFormatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            userRepository.getUser(uid, 
                onSuccess = { user -> currentUser = user },
                onFailure = { /* Handle error */ }
            )
            
            notificationRepository.getUnreadCount(uid) { count ->
                unreadNotifications = count
            }
        }

        guruRepository.getGurus(
            onSuccess = {
                guruList = it
                isLoading = false
            },
            onFailure = { isLoading = false }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(primaryColor)
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateFormatter.format(currentTime),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = timeFormatter.format(currentTime),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = stringResource(R.string.welcome) + ",", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 18.sp)
                            Text(
                                text = currentUser?.name ?: stringResource(R.string.student),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                .clickable { navController.navigate("notifications") },
                            contentAlignment = Alignment.Center
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifications > 0) {
                                        Badge(
                                            containerColor = Color.Red,
                                            contentColor = Color.White
                                        ) {
                                            Text(unreadNotifications.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.NotificationsNone, 
                                    contentDescription = "Notifications", 
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { navController.navigate("search") },
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = primaryColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.search_placeholder),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.available_gurus),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 16.dp)
            )
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }
        } else {
            items(guruList) { guru ->
                GuruListItem(guru, 
                    onCardClick = {
                        navController.navigate("profile/${guru.id}")
                    },
                    onFavoriteClick = {
                        FavoritesManager.toggleFavorite(guru)
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun GuruListItem(guru: Guru, onCardClick: () -> Unit, onFavoriteClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isFavorite = FavoritesManager.isFavorite(guru)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (guru.imageUrl.isEmpty()) {
                    if (guru.gender == "Female") "https://avatar.iran.liara.run/public/girl" 
                    else "https://avatar.iran.liara.run/public/boy"
                } else guru.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = guru.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = guru.subject,
                    color = primaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${guru.village}, ${guru.district}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}