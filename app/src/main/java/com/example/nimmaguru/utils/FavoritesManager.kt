package com.example.nimmaguru.utils

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.nimmaguru.model.Guru
import com.example.nimmaguru.repository.GuruRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.atomic.AtomicInteger

object FavoritesManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val guruRepository = GuruRepository()

    val favoriteGurus = mutableStateListOf<Guru>()
    var isLoaded = false
        private set
    
    private var isCurrentlyLoading = false
    private val pendingCallbacks = mutableListOf<() -> Unit>()

    /**
     * Loads favorites from Firestore. 
     * Handles concurrent calls by queuing callbacks.
     */
    fun loadFavorites(onComplete: () -> Unit = {}) {
        val userId = auth.currentUser?.uid
        
        if (userId == null) {
            favoriteGurus.clear()
            isLoaded = true
            onComplete()
            return
        }

        // If already loaded and not currently refreshing, return immediately
        if (isLoaded && !isCurrentlyLoading) {
            onComplete()
            return
        }

        // Queue the callback
        synchronized(pendingCallbacks) {
            pendingCallbacks.add(onComplete)
        }

        // If already loading, the queued callback will be called when it finishes
        if (isCurrentlyLoading) return

        isCurrentlyLoading = true
        
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    completeLoading(emptyList())
                    return@addOnSuccessListener
                }

                val rawFavorites = document.get("favorites") as? List<*>
                val favoriteIds = rawFavorites?.filterIsInstance<String>()?.filter { it.isNotEmpty() } ?: emptyList()
                
                if (favoriteIds.isEmpty()) {
                    completeLoading(emptyList())
                    return@addOnSuccessListener
                }

                val loadedGurus = mutableListOf<Guru>()
                val processedCount = AtomicInteger(0)
                val totalToProcess = favoriteIds.size
                
                favoriteIds.forEach { id ->
                    try {
                        guruRepository.getGuruById(
                            guruId = id,
                            onSuccess = { guru ->
                                synchronized(loadedGurus) { loadedGurus.add(guru) }
                                if (processedCount.incrementAndGet() == totalToProcess) {
                                    completeLoading(loadedGurus)
                                }
                            },
                            onFailure = {
                                Log.e("FavoritesManager", "Failed to load guru $id")
                                if (processedCount.incrementAndGet() == totalToProcess) {
                                    completeLoading(loadedGurus)
                                }
                            }
                        )
                    } catch (e: Exception) {
                        Log.e("FavoritesManager", "Exception loading guru $id", e)
                        if (processedCount.incrementAndGet() == totalToProcess) {
                            completeLoading(loadedGurus)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FavoritesManager", "Error fetching user document", e)
                completeLoading(favoriteGurus.toList()) // Keep current list on failure
            }
    }

    private fun completeLoading(newList: List<Guru>) {
        favoriteGurus.clear()
        favoriteGurus.addAll(newList.distinctBy { it.id })
        isLoaded = true
        isCurrentlyLoading = false
        
        val callbacks = synchronized(pendingCallbacks) {
            val list = pendingCallbacks.toList()
            pendingCallbacks.clear()
            list
        }
        callbacks.forEach { it() }
    }

    fun addFavorite(guru: Guru) {
        val userId = auth.currentUser?.uid ?: return
        
        if (!favoriteGurus.any { it.id == guru.id }) {
            favoriteGurus.add(guru)
            db.collection("users").document(userId)
                .update("favorites", FieldValue.arrayUnion(guru.id))
        }
    }

    fun removeFavorite(guru: Guru) {
        val userId = auth.currentUser?.uid ?: return
        
        favoriteGurus.removeAll { it.id == guru.id }
        db.collection("users").document(userId)
            .update("favorites", FieldValue.arrayRemove(guru.id))
    }

    fun isFavorite(guru: Guru): Boolean {
        return favoriteGurus.any { it.id == guru.id }
    }

    fun toggleFavorite(guru: Guru) {
        if (isFavorite(guru)) {
            removeFavorite(guru)
        } else {
            addFavorite(guru)
        }
    }
}
