package com.example.nimmaguru.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nimmaguru.R
import com.example.nimmaguru.model.Guru
import com.example.nimmaguru.model.Review
import com.example.nimmaguru.repository.GuruRepository
import com.example.nimmaguru.repository.ReviewRepository
import com.example.nimmaguru.utils.FavoritesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuruProfileScreen(
    guruId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val reviewRepository = ReviewRepository()
    val guruRepository = GuruRepository()
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var guru by remember { mutableStateOf(Guru()) }
    var reviews by remember { mutableStateOf(listOf<Review>()) }
    var reviewText by remember { mutableStateOf("") }
    var selectedRating by remember { mutableIntStateOf(5) }
    var isLoadingReviews by remember { mutableStateOf(true) }
    var currentUserName by remember { mutableStateOf("Student") }

    LaunchedEffect(guruId) {
        // Fetch Guru Details
        guruRepository.getGuruById(
            guruId = guruId,
            onSuccess = { guru = it },
            onFailure = {}
        )

        // Fetch Current User Name for the Review
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    currentUserName = doc.getString("name") ?: "Student"
                }
        }

        // Fetch Reviews
        reviewRepository.getReviewsForGuru(
            guruId = guruId,
            onSuccess = {
                reviews = it
                isLoadingReviews = false
            },
            onFailure = { 
                isLoadingReviews = false 
                Toast.makeText(context, "Error fetching reviews", Toast.LENGTH_SHORT).show()
            }
        )
    }

    val averageRating = if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 12.dp,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate("chat/${guru.id}/${guru.name}") },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.chat), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { navController.navigate("booking/${guru.id}/${guru.name}") },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text(text = stringResource(R.string.book_now), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            item { ProfileHeaderModern(guru, averageRating, navController) }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    InfoSectionModern(guru)
                    Spacer(modifier = Modifier.height(32.dp))
                    TimingsSectionModern()
                    Spacer(modifier = Modifier.height(40.dp))

                    WriteReviewSectionModern(
                        reviewText = reviewText,
                        onReviewChange = { reviewText = it },
                        selectedRating = selectedRating,
                        onRatingChange = { selectedRating = it },
                        onSubmit = {
                            if (reviewText.isNotEmpty()) {
                                val newReview = Review(
                                    studentName = currentUserName,
                                    comment = reviewText,
                                    rating = selectedRating,
                                    guruId = guruId
                                )
                                reviewRepository.addReview(newReview, {
                                    reviewText = ""
                                    selectedRating = 5
                                    Toast.makeText(context, context.getString(R.string.review_posted), Toast.LENGTH_SHORT).show()
                                    // Refresh reviews
                                    reviewRepository.getReviewsForGuru(guruId, { reviews = it }, {})
                                }, {
                                    Toast.makeText(context, "Failed to post review", Toast.LENGTH_SHORT).show()
                                })
                            } else {
                                Toast.makeText(context, "Please write a comment", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = stringResource(R.string.student_reviews),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (isLoadingReviews) {
                item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            } else if (reviews.isEmpty()) {
                item { 
                    Text(
                        stringResource(R.string.no_reviews),
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    ) 
                }
            } else {
                items(reviews) { review -> ReviewCardModern(review) }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun ProfileHeaderModern(guru: Guru, averageRating: Double, navController: NavController) {
    val placeholder = if (guru.gender == "Female") 
        "https://avatar.iran.liara.run/public/girl" 
    else 
        "https://avatar.iran.liara.run/public/boy"

    val isFavorite = FavoritesManager.isFavorite(guru)

    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }

                IconButton(
                    onClick = { FavoritesManager.toggleFavorite(guru) },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = if (guru.imageUrl.isEmpty()) placeholder else guru.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .border(6.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = guru.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = guru.subject,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                if (averageRating > 0) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                    Text(text = String.format("%.1f", averageRating), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun InfoSectionModern(guru: Guru) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoChipModern(icon = Icons.Default.MenuBook, label = stringResource(R.string.expertise), value = guru.subject, modifier = Modifier.weight(1f))
            val location = if (guru.district.isNotEmpty()) "${guru.village}, ${guru.district}" else guru.village
            InfoChipModern(icon = Icons.Default.LocationOn, label = stringResource(R.string.location), value = location, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.about_guru),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp
        ) {
            Text(
                text = guru.about,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoChipModern(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.surface, CircleShape).padding(6.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun TimingsSectionModern() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = stringResource(R.string.availability),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 18.sp
                )
                Text(
                    text = stringResource(R.string.weekend_timing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun WriteReviewSectionModern(reviewText: String, onReviewChange: (String) -> Unit, selectedRating: Int, onRatingChange: (Int) -> Unit, onSubmit: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = stringResource(R.string.experience_question), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                (1..5).forEach { rating ->
                    Icon(
                        imageVector = if (rating <= selectedRating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (rating <= selectedRating) Color(0xFFFFC107) else Color.Gray,
                        modifier = Modifier.size(44.dp).clickable { onRatingChange(rating) }.padding(4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = reviewText,
                onValueChange = onReviewChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.feedback_placeholder)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.post_feedback), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ReviewCardModern(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Text(review.studentName.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = review.studentName, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Row(
                    modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = review.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}
