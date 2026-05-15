package com.example.nimmaguru.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nimmaguru.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Privacy Policy for Nimma Guru",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = """
                    Welcome to Nimma Guru. We value your privacy and are committed to protecting your personal data. 
                    
                    1. Information We Collect: We collect information you provide directly to us, such as your name, email address, and profile details when you register as a Student or Guru.
                    
                    2. How We Use Your Information: We use this information to provide our services, facilitate connections between Students and Gurus, and improve our app.
                    
                    3. Data Security: We implement appropriate security measures to protect your information from unauthorized access.
                    
                    4. Third-Party Services: We use Firebase for authentication and database management. Your data is stored securely on their servers.
                    
                    5. Your Rights: You can update your profile information at any time through the Settings.
                    
                    If you have any questions about this Privacy Policy, please contact us.
                """.trimIndent(),
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
    }
}
