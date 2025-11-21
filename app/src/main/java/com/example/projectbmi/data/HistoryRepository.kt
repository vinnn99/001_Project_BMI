package com.example.projectbmi.data

import com.example.projectbmi.model.BMIRecord
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = historyCollection(uid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snap, ex ->
                if (ex != null) {
                    close(ex)
                    return@addSnapshotListener
                }
                if (snap != null) {
                    val list = snap.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(BMIRecord::class.java)
                        } catch (e: Exception) { null }
                    }
                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun addRecord(record: BMIRecord) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val docId = record.timestamp.toString()
        historyCollection(uid).document(docId).set(record).await()
    }
}
