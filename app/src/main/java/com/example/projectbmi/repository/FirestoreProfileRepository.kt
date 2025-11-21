package com.example.projectbmi.repository

import com.example.projectbmi.model.UserProfile
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.lang.Exception

// DTO reused from UserProfileStore - keep shape compatible
data class UserProfileDto(
    val fitnessGoals: List<String> = emptyList(),
    val exerciseFrequency: String? = null,
    val dietPattern: String? = null,
    val sleepDuration: String? = null,
    val weightManagementChallenges: List<String> = emptyList(),
    val updatedAt: Long? = null
)

object FirestoreProfileRepository {
    private val db = Firebase.firestore

    private fun profileDocRef(uid: String) = db.collection("users").document(uid).collection("meta").document("profile")

    suspend fun saveProfile(uid: String, dto: UserProfileDto) {
        val docRef = profileDocRef(uid)
        val dtoWithTs = dto.copy(updatedAt = dto.updatedAt ?: System.currentTimeMillis())
        docRef.set(dtoWithTs).await()
    }

    suspend fun getProfileOnce(uid: String): UserProfileDto? {
        val snap = profileDocRef(uid).get().await()
        return if (snap.exists()) snap.toObject(UserProfileDto::class.java) else null
    }

    fun profileFlow(uid: String): Flow<UserProfileDto?> = callbackFlow {
        val listener = profileDocRef(uid).addSnapshotListener { snap, ex ->
            if (ex != null) {
                close(ex)
                return@addSnapshotListener
            }
            if (snap != null && snap.exists()) {
                val dto = try { snap.toObject(UserProfileDto::class.java) } catch (e: Exception) { null }
                trySend(dto)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    // Overload to accept DataStore DTO directly and convert to Firestore DTO
    // NOTE: DataStore was removed; callers should construct UserProfileDto defined in this file.
}
