package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nimmaguru.R
import com.example.nimmaguru.auth.AuthRepository
import com.example.nimmaguru.model.User
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {

    val context = LocalContext.current
    val authRepository = AuthRepository()
    val db = FirebaseFirestore.getInstance()
    val uid = authRepository.getCurrentUserId() ?: ""

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profileImage by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var village by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val karnatakaDistricts = listOf(
        "Bagalkot", "Ballari", "Belagavi", "Bengaluru Rural", "Bengaluru Urban",
        "Bidar", "Chamarajanagar", "Chikkaballapur", "Chikkamagaluru", "Chitradurga",
        "Dakshina Kannada", "Davanagere", "Dharwad", "Gadag", "Hassan", "Haveri",
        "Kalaburagi", "Kodagu", "Kolar", "Koppal", "Mandya", "Mysuru", "Raichur",
        "Ramanagara", "Shivamogga", "Tumakuru", "Udupi", "Uttara Kannada",
        "Vijayapura", "Yadgir", "Vijayanagara"
    ).sorted()

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val user = doc.toObject(User::class.java)
                    user?.let {
                        name = it.name
                        bio = it.bio
                        profileImage = it.profileImage
                        gender = it.gender
                        village = it.village
                        district = it.district
                        role = it.role
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_profile)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.full_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(stringResource(R.string.bio_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = profileImage,
                    onValueChange = { profileImage = it },
                    label = { Text(stringResource(R.string.profile_image_url)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.gender), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AssistChip(
                        onClick = { gender = "Male" },
                        label = { Text(stringResource(R.string.male)) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            if (gender == "Male") Icon(Icons.Default.Person, null)
                        },
                        colors = if (gender == "Male") 
                            AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else AssistChipDefaults.assistChipColors()
                    )
                    AssistChip(
                        onClick = { gender = "Female" },
                        label = { Text(stringResource(R.string.female)) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            if (gender == "Female") Icon(Icons.Default.Person, null)
                        },
                        colors = if (gender == "Female") 
                            AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else AssistChipDefaults.assistChipColors()
                    )
                }

                if (role == "GURU") {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(stringResource(R.string.location_karnataka), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = district,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.district)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            karnatakaDistricts.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        district = selectionOption
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = village,
                        onValueChange = { village = it },
                        label = { Text(stringResource(R.string.village_taluk)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isSaving) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Button(
                        onClick = {
                            isSaving = true
                            
                            val updates = mutableMapOf<String, Any>(
                                "name" to name,
                                "bio" to bio,
                                "profileImage" to profileImage,
                                "gender" to gender
                            )
                            
                            if (role == "GURU") {
                                updates["village"] = village
                                updates["district"] = district
                            }

                            db.collection("users").document(uid)
                                .update(updates)
                                .addOnSuccessListener {
                                    isSaving = false
                                    Toast.makeText(context, context.getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    isSaving = false
                                    Toast.makeText(context, context.getString(R.string.update_failed, it.message ?: ""), Toast.LENGTH_SHORT).show()
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.save_changes), fontSize = 18.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
