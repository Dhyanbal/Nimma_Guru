package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nimmaguru.R
import com.example.nimmaguru.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    val authRepository = AuthRepository()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = stringResource(R.string.welcome_back),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = stringResource(R.string.login_to_continue),
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = MaterialTheme.colorScheme.surface
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // EMAIL
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.email)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Email
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PASSWORD
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    null
                                )
                            }
                        },
                        visualTransformation =
                            if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = {
                            if (email.isEmpty()) {
                                Toast.makeText(context, context.getString(R.string.enter_email_first), Toast.LENGTH_SHORT).show()
                            } else {
                                authRepository.sendPasswordResetEmail(
                                    email = email,
                                    onSuccess = {
                                        Toast.makeText(context, context.getString(R.string.password_reset_sent, email), Toast.LENGTH_LONG).show()
                                    },
                                    onFailure = {
                                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }) {
                            Text(stringResource(R.string.forgot_password), color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {

                        Button(
                            onClick = {

                                if (email.isEmpty() || password.isEmpty()) {
                                    Toast.makeText(context, context.getString(R.string.fill_fields), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isLoading = true

                                auth.signInWithEmailAndPassword(email.lowercase().trim(), password)
                                    .addOnSuccessListener {

                                        val uid = auth.currentUser?.uid ?: ""

                                        db.collection("users")
                                            .document(uid)
                                            .get()
                                            .addOnSuccessListener { doc ->

                                                isLoading = false

                                                val role = doc.getString("role") ?: "STUDENT"
                                                val status = doc.getString("status") ?: "Pending"

                                                when (role) {

                                                    "ADMIN" -> {
                                                        navController.navigate("adminPanel") {
                                                            popUpTo("login") { inclusive = true }
                                                        }
                                                    }

                                                    "GURU" -> {
                                                        if (status == "Approved") {
                                                            navController.navigate("guruDashboard") {
                                                                popUpTo("login") { inclusive = true }
                                                            }
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                context.getString(R.string.guru_not_approved),
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }

                                                    else -> {
                                                        navController.navigate("home") {
                                                            popUpTo("login") { inclusive = true }
                                                        }
                                                    }
                                                }
                                            }
                                            .addOnFailureListener {
                                                isLoading = false
                                                Toast.makeText(context, context.getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        Toast.makeText(context, context.getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                                    }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(stringResource(R.string.login), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TextButton(onClick = {
                        navController.navigate("signup")
                    }) {
                        Text(stringResource(R.string.dont_have_account))
                    }
                }
            }
        }
    }
}
