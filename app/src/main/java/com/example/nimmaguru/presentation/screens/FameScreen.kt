package com.example.nimmaguru.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
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
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FameScreen(navController: NavController, isTab: Boolean = false) {
    var gurusWithRatings by remember { mutableStateOf(listOf<Pair<Guru, Double>>()) }
    var isLoading by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        GuruRepository().getGurus(
            onSuccess = { gurus ->
                val tempData = mutableListOf<Pair<Guru, Double>>()
                var processedCount = 0

                if (gurus.isEmpty()) {
                    isLoading = false
                }

                gurus.forEach { guru ->
                    db.collection("reviews").whereEqualTo("guruId", guru.id).get()
                        .addOnSuccessListener { snapshot ->
                            val rating = if (snapshot.isEmpty) 0.0 else {
                                snapshot.documents.map { it.getLong("rating") ?: 0L }.average()
                            }
                            tempData.add(guru to rating)
                            processedCount++

                            if (processedCount == gurus.size) {
                                gurusWithRatings = tempData.sortedByDescending { it.second }
                                isLoading = false
                            }
                        }
                        .addOnFailureListener {
                            processedCount++
                            if (processedCount == gurus.size) {
                                gurusWithRatings = tempData.sortedByDescending { it.second }
                                isLoading = false
                            }
                        }
                }
            },
            onFailure = { isLoading = false }
        )
    }

    Scaffold(
        topBar = {
            if (!isTab) {
                TopAppBar(
                    title = { Text(stringResource(R.string.wall_of_fame), fontWeight = FontWeight.Bold) },
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
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFFFD700)
                        )
                        Text(
                            stringResource(R.string.top_rated_mentors),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(R.string.guiding_generation),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                itemsIndexed(gurusWithRatings) { index, pair ->
                    FameCard(pair.first, pair.second, index + 1)
                }
            }
        }
    }
}

@Composable
fun FameCard(guru: Guru, rating: Double, rank: Int) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val placeholder = "https://avatar.iran.liara.run/public/boy"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Circle
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = rankColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = rank.toString(),
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Profile Image
            AsyncImage(
                model = if (guru.imageUrl.isEmpty()) placeholder else guru.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = guru.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = guru.subject,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }

            // Rating
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", rating),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = "Rating",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
