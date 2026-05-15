package com.example.nimmaguru.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsConditionsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold) },
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
                text = "Terms and Conditions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = """
                    By using the Nimma Guru application, you agree to the following terms:
                    
                    1. Acceptance of Terms: By accessing this app, you accept these terms in full.
                    
                    2. User Conduct: Users must act respectfully. Students and Gurus are expected to maintain professional standards during interactions.
                    
                    3. Account Responsibility: You are responsible for maintaining the confidentiality of your account credentials.
                    
                    4. Service Availability: We strive for 24/7 availability but do not guarantee uninterrupted service.
                    
                    5. Content Ownership: All app content is the property of Nimma Guru.
                    
                    6. Limitation of Liability: Nimma Guru is a platform connecting users. We are not liable for interactions or agreements made outside the app.
                    
                    We reserve the right to update these terms at any time.
                """.trimIndent(),
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
    }
}
