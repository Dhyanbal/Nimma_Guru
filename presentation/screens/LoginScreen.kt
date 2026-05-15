package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nimmaguru.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val authRepository = AuthRepository()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            
            // App Logo/Icon Placeholder
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🎓", fontSize = 50.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Welcome Back",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Sign in to continue",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Forgot Password?",
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable {
                                if (email.isEmpty()) {
                                    Toast.makeText(context, "Please enter your email first", Toast.LENGTH_SHORT).show()
                                } else {
                                    authRepository.sendPasswordResetEmail(
                                        email = email,
                                        onSuccess = {
                                            Toast.makeText(context, "Reset link sent to $email", Toast.LENGTH_LONG).show()
                                        },
                                        onFailure = {
                                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                if (email.isEmpty() || password.isEmpty()) {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isLoading = true
                                authRepository.loginUser(
                                    email = email,
                                    password = password,
                                    onSuccess = {
                                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                        authRepository.fetchUserRole(
                                            uid = uid,
                                            onSuccess = { role ->
                                                isLoading = false
                                                when (role) {
                                                    "Guru" -> navController.navigate("guruDashboard") { popUpTo("login") { inclusive = true } }
                                                    "Admin" -> navController.navigate("adminPanel") { popUpTo("login") { inclusive = true } }
                                                    else -> navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                                }
                                            },
                                            onFailure = {
                                                isLoading = false
                                                navController.navigate("home")
                                            }
                                        )
                                    },
                                    onFailure = {
                                        isLoading = false
                                        Toast.makeText(context, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Don't have an account? ", color = Color.Gray)
                        TextButton(onClick = { navController.navigate("signup") }) {
                            Text("Sign Up", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}