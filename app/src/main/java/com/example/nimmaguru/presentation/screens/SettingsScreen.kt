package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nimmaguru.R
import com.example.nimmaguru.auth.AuthRepository
import com.example.nimmaguru.model.User
import com.example.nimmaguru.repository.NotificationRepository
import com.example.nimmaguru.utils.ThemeManager
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val authRepository = AuthRepository()
    val notificationRepository = NotificationRepository()
    val db = FirebaseFirestore.getInstance()
    val userId = authRepository.getCurrentUserId() ?: ""
    val context = LocalContext.current

    var userData by remember { mutableStateOf(User()) }
    val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
    var showLanguageDialog by remember { mutableStateOf(false) }
    var hasUnreadNotifications by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    doc.toObject(User::class.java)?.let { userData = it }
                }

            notificationRepository.getNotifications(userId) { notifications ->
                hasUnreadNotifications = notifications.any { !it.isRead }
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("en")
                                AppCompatDelegate.setApplicationLocales(appLocale)
                                showLanguageDialog = false
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentLocale == "en", onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.english))
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("kn")
                                AppCompatDelegate.setApplicationLocales(appLocale)
                                showLanguageDialog = false
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentLocale == "kn", onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.kannada))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Gradient Header with Profile Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        )
                    )
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(80.dp)) {
                        val placeholder = if (userData.gender == "Female") 
                            "https://avatar.iran.liara.run/public/girl" 
                        else 
                            "https://avatar.iran.liara.run/public/boy"

                        AsyncImage(
                            model = if (userData.profileImage.isEmpty()) placeholder else userData.profileImage,
                            contentDescription = null,
                            modifier = Modifier
                                .size(75.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentScale = ContentScale.Crop
                        )
                        
                        IconButton(
                            onClick = { navController.navigate("editProfile") },
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.BottomEnd)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userData.name.ifEmpty { stringResource(R.string.student) },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if(userData.role == "GURU") stringResource(R.string.guru) else stringResource(R.string.student),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(text = userData.email, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.account_settings),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsItemModern(stringResource(R.string.edit_profile), Icons.Default.Person) { navController.navigate("editProfile") }
                SettingsItemModern(stringResource(R.string.my_bookings), Icons.Default.EventNote) { navController.navigate("myBookings") }
                SettingsItemModern(stringResource(R.string.change_password), Icons.Default.Lock) { 
                    if (userData.email.isNotEmpty()) {
                        authRepository.sendPasswordResetEmail(
                            email = userData.email,
                            onSuccess = {
                                Toast.makeText(context, context.getString(R.string.password_reset_sent, userData.email), Toast.LENGTH_LONG).show()
                            },
                            onFailure = {
                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
                SettingsItemModern(stringResource(R.string.notifications), Icons.Default.Notifications, showBadge = hasUnreadNotifications) { 
                    navController.navigate("notifications") 
                }
                SettingsItemModern(stringResource(R.string.language), Icons.Default.Language, trailing = if(currentLocale == "kn") stringResource(R.string.kannada) else stringResource(R.string.english)) { 
                    showLanguageDialog = true
                }
                SettingsItemModern(stringResource(R.string.theme), Icons.Default.DarkMode, trailing = if(ThemeManager.isDarkMode.value) stringResource(R.string.dark) else stringResource(R.string.light)) { ThemeManager.toggleTheme() }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.support_others),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsItemModern(stringResource(R.string.help_support), Icons.AutoMirrored.Filled.Help) { navController.navigate("helpSupport") }
                SettingsItemModern(stringResource(R.string.privacy_policy), Icons.Default.Shield) { navController.navigate("privacyPolicy") }
                SettingsItemModern(stringResource(R.string.terms_conditions), Icons.Default.Description) { navController.navigate("termsConditions") }
                SettingsItemModern(stringResource(R.string.logout), Icons.AutoMirrored.Filled.Logout, isDestructive = true) {
                    authRepository.logout()
                    navController.navigate("login") { popUpTo(0) }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SettingsItemModern(
    title: String,
    icon: ImageVector,
    trailing: String? = null,
    showBadge: Boolean = false,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDestructive) MaterialTheme.colorScheme.errorContainer 
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (isDestructive) MaterialTheme.colorScheme.error 
                               else MaterialTheme.colorScheme.primary
                    )
                }
                if (showBadge) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestructive) MaterialTheme.colorScheme.error 
                       else MaterialTheme.colorScheme.onSurface
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            Icon(
                Icons.Default.ChevronRight, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
