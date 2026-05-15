package com.example.nimmaguru.presentation.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import com.example.nimmaguru.R

@Composable
fun OnboardingScreen(navController: NavController) {
    val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
    var selectedLanguage by remember { mutableStateOf(if (currentLocale == "kn") "Kannada" else "English") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🎓", fontSize = 64.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.tagline),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.choose_language),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LanguageChip(
                    text = "ಕನ್ನಡ",
                    isSelected = selectedLanguage == "Kannada",
                    onClick = { 
                        selectedLanguage = "Kannada"
                        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("kn")
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                )
                LanguageChip(
                    text = "English",
                    isSelected = selectedLanguage == "English",
                    onClick = { 
                        selectedLanguage = "English"
                        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("en")
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(stringResource(R.string.get_started), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { navController.navigate("login") }) {
                Text(stringResource(R.string.already_have_account), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun LanguageChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f)),
        shadowElevation = if (isSelected) 8.dp else 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}