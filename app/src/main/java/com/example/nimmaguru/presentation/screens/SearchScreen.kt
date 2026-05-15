package com.example.nimmaguru.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nimmaguru.R
import com.example.nimmaguru.model.Guru
import com.example.nimmaguru.repository.GuruRepository
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var guruList by remember { mutableStateOf(listOf<Guru>()) }
    var filteredList by remember { mutableStateOf(listOf<Guru>()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val primaryColor = MaterialTheme.colorScheme.primary

    val categories = listOf("All", "Mathematics", "Science", "English", "Kannada", "Social Science")

    LaunchedEffect(Unit) {
        GuruRepository().getGurus(
            onSuccess = {
                guruList = it
                filteredList = it
                isLoading = false
            },
            onFailure = {
                isLoading = false
            }
        )
    }

    LaunchedEffect(searchQuery, selectedCategory) {
        if (searchQuery.isBlank() && selectedCategory == "All") {
            filteredList = guruList
            return@LaunchedEffect
        }

        // Basic filtering without AI
        filteredList = guruList.filter { guru ->
            val matchesSearch = searchQuery.isEmpty() || 
                guru.name.contains(searchQuery, ignoreCase = true) ||
                guru.subject.contains(searchQuery, ignoreCase = true) ||
                guru.village.contains(searchQuery, ignoreCase = true)
            
            val matchesCategory = selectedCategory == "All" || 
                guru.subject.equals(selectedCategory, ignoreCase = true)
            
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.find_your_guru),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLeadingIconColor = primaryColor,
                cursorColor = primaryColor,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )

        LazyRow(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val displayLabel = if (category == "All") stringResource(R.string.category_all) else category
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(displayLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        } else if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.no_gurus_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (selectedCategory != "All" || searchQuery.isNotEmpty()) {
                        TextButton(onClick = { 
                            searchQuery = ""
                            selectedCategory = "All"
                        }) {
                            Text(stringResource(R.string.clear_filters), color = primaryColor)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredList) { guru ->
                    SearchGuruCard(guru) {
                        navController.navigate("profile/${guru.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchGuruCard(guru: Guru, onClick: () -> Unit) {
    val placeholder = if (guru.gender == "Female") 
        "https://avatar.iran.liara.run/public/girl" 
    else 
        "https://avatar.iran.liara.run/public/boy"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (guru.imageUrl.isEmpty()) placeholder else guru.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = guru.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = guru.subject,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = guru.village,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "5.0",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
}