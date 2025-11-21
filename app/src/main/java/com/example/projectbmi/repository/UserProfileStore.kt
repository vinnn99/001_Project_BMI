package com.example.projectbmi.repository

import android.content.Context
import com.example.projectbmi.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Placeholder local store kept for compatibility but does not persist locally.
 * App is running in Firestore-only mode — reads/writes should go to Firestore.
 */
object UserProfileStore {
    fun getProfileFlow(context: Context): Flow<UserProfile> = flow {
        emit(UserProfile())
    }

    suspend fun saveProfile(context: Context, profile: UserProfile) {
        // no-op: persistence moved to Firestore
    }
}
