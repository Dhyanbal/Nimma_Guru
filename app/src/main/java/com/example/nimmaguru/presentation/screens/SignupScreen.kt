package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nimmaguru.R
import com.example.nimmaguru.auth.AuthRepository
import com.example.nimmaguru.model.User
import com.example.nimmaguru.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SignupScreen(navController: NavController) {

    val context = LocalContext.current
    val authRepository = AuthRepository()
    val userRepository = UserRepository()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("STUDENT") }
    
    // Guru Specific Fields
    var subject by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
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
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.create_account),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = stringResource(R.string.join_nimma_guru),
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // NAME
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.full_name)) },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // EMAIL
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.email)) },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Email
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // PASSWORD
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password)) },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(R.string.i_am_a),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RoleCard(
                            title = stringResource(R.string.student),
                            isSelected = selectedRole == "STUDENT",
                            onClick = { selectedRole = "STUDENT" },
                            modifier = Modifier.weight(1f)
                        )

                        RoleCard(
                            title = stringResource(R.string.guru),
                            isSelected = selectedRole == "GURU",
                            onClick = { selectedRole = "GURU" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // SHOW EXTRA FIELDS IF GURU IS SELECTED
                    if (selectedRole == "GURU") {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.guru_profile_details),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text(stringResource(R.string.subject_expertise)) },
                            leadingIcon = { Icon(Icons.Default.MenuBook, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = village,
                            onValueChange = { village = it },
                            label = { Text(stringResource(R.string.your_location)) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text(stringResource(R.string.about_you)) },
                            leadingIcon = { Icon(Icons.Default.Info, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            minLines = 3
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                                    Toast.makeText(context, context.getString(R.string.fill_fields), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                
                                if (selectedRole == "GURU" && (subject.isEmpty() || village.isEmpty() || bio.isEmpty())) {
                                    Toast.makeText(context, context.getString(R.string.complete_guru_profile), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isLoading = true

                                authRepository.registerUser(
                                    email = email.lowercase().trim(),
                                    password = password,
                                    onSuccess = {
                                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                                        val user = User(
                                            uid = uid,
                                            name = name,
                                            email = email.lowercase().trim(),
                                            role = selectedRole,
                                            status = "Pending",
                                            bio = bio,
                                            profileImage = "",
                                            subject = subject,
                                            village = village
                                        )

                                        userRepository.saveUser(
                                            user = user,
                                            onSuccess = {
                                                isLoading = false
                                                Toast.makeText(context, context.getString(R.string.account_created_wait), Toast.LENGTH_LONG).show()
                                                navController.navigate("login") {
                                                    popUpTo("signup") { inclusive = true }
                                                }
                                            },
                                            onFailure = {
                                                isLoading = false
                                                Toast.makeText(context, context.getString(R.string.save_profile_failed), Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                    onFailure = {
                                        isLoading = false
                                        Toast.makeText(context, it.message ?: context.getString(R.string.signup_failed), Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(stringResource(R.string.signup), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TextButton(onClick = { navController.navigate("login") }) {
                        Text(stringResource(R.string.already_have_account))
                    }
                }
            }
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Gray
            )
        }
    }
}