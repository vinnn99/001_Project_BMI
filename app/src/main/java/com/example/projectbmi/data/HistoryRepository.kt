package com.example.projectbmi.data

import com.example.projectbmi.model.BMIRecord
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

/**
 * Firestore-backed history repository. Stores BMI records under
 * collection users/{uid}/history as individual documents keyed by timestamp.
 */
class HistoryRepository {
    private val db = Firebase.firestore

    private fun historyCollection(uid: String) = db.collection("users").document(uid).collection("history")

    fun historyFlow(): Flow<List<BMIRecord>> = callbackFlow {
        val uid = Firebase.auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Log.w("HistoryRepository", "No user UID available, returning empty history")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        try {
            Log.d("HistoryRepository", "Setting up history listener for uid: $uid")
            val listener = historyCollection(uid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snap, ex ->
                    if (ex != null) {
                        Log.e("HistoryRepository", "Snapshot listener error: ${ex.message}", ex)
                        // Don't close the flow on permission errors — try to send empty list instead
                        if (ex.message?.contains("PERMISSION_DENIED") == true) {
                            Log.w("HistoryRepository", "Permission denied reading history, returning empty list")
                            try {
                                trySend(emptyList())
                            } catch (e: Exception) {
                                Log.e("HistoryRepository", "Error sending empty list on permission denial", e)
                            }
                        } else {
                            close(ex)
                        }
                        return@addSnapshotListener
                    }
                    if (snap != null) {
                        val list = snap.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(BMIRecord::class.java)
                            } catch (e: Exception) {
                                Log.w("HistoryRepository", "Error converting document to BMIRecord", e)
                                null
                            }
                        }
                        Log.d("HistoryRepository", "Loaded ${list.size} history records")
                        try {
                            trySend(list)
                        } catch (e: Exception) {
                            Log.e("HistoryRepository", "Error sending history list", e)
                        }
                    } else {
                        Log.w("HistoryRepository", "Snapshot is null")
                        try {
                            trySend(emptyList())
                        } catch (e: Exception) {
                            Log.e("HistoryRepository", "Error sending empty list on null snapshot", e)
                        }
                    }
                }

            awaitClose {
                Log.d("HistoryRepository", "Removing history listener")
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e("HistoryRepository", "Critical error in historyFlow setup", e)
            try {
                trySend(emptyList())
                close(e)
            } catch (ex: Exception) {
                Log.e("HistoryRepository", "Error closing flow after exception", ex)
            }
        }
    }

    suspend fun addRecord(record: BMIRecord) {
        try {
            val uid = Firebase.auth.currentUser?.uid ?: return
            val docId = record.timestamp.toString()
            Log.d("HistoryRepository", "Adding record: $docId")
            historyCollection(uid).document(docId).set(record).await()
            Log.d("HistoryRepository", "Record added successfully")
        } catch (e: Exception) {
            Log.e("HistoryRepository", "Error adding record", e)
            // Don't rethrow — let caller handle via HistoryViewModel
            throw e
        }
    }
}
