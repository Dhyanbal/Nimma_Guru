package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuruRegistrationScreen(navController: NavController) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var subject by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val karnatakaDistricts = listOf(
        "Bagalkot", "Ballari", "Belagavi", "Bengaluru Rural", "Bengaluru Urban",
        "Bidar", "Chamarajanagar", "Chikkaballapur", "Chikkamagaluru", "Chitradurga",
        "Dakshina Kannada", "Davanagere", "Dharwad", "Gadag", "Hassan", "Haveri",
        "Kalaburagi", "Kodagu", "Kolar", "Koppal", "Mandya", "Mysuru", "Raichur",
        "Ramanagara", "Shivamogga", "Tumakuru", "Udupi", "Uttara Kannada",
        "Vijayapura", "Yadgir", "Vijayanagara"
    ).sorted()

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F5FF))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Register as Guru",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Complete your profile for approval.", color = Color.Gray)

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Guru Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ GENDER SELECTION
        Text(text = "Gender", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                onClick = { gender = "Male" },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (gender == "Male") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                    Text("Male", fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                onClick = { gender = "Female" },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (gender == "Female") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                    Text("Female", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // District Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = district,
                onValueChange = {},
                readOnly = true,
                label = { Text("District (Karnataka)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
            label = { Text("Village / Taluk") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = about,
            onValueChange = { about = it },
            label = { Text("About You (Bio)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            minLines = 4
        )

        Spacer(modifier = Modifier.height(30.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Button(
                onClick = {
                    if (name.isEmpty() || subject.isEmpty() || village.isEmpty() || district.isEmpty()) {
                        Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true

                    val guruData = mapOf(
                        "name" to name,
                        "gender" to gender,
                        "subject" to subject,
                        "district" to district,
                        "village" to village,
                        "bio" to about,
                        "status" to "Pending"
                    )

                    db.collection("users").document(userId)
                        .update(guruData)
                        .addOnSuccessListener {
                            isLoading = false
                            Toast.makeText(context, "Registration submitted for approval", Toast.LENGTH_LONG).show()
                            navController.navigate("home") {
                                popUpTo("registerGuru") { inclusive = true }
                            }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Submit for Approval", fontSize = 18.sp)
            }
        }
    }
}