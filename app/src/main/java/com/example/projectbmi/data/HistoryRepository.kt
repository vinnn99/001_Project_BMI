package com.example.projectbmi.data

import android.content.Context
import android.util.Log
import com.example.projectbmi.model.BMIRecord
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

// HistoryRepository now supports a local SharedPreferences fallback for unsigned users
// and performs best-effort sync when a user signs in.

/**
 * Firestore-backed history repository. Stores BMI records under
 * collection users/{uid}/history as individual documents keyed by timestamp.
 */
class HistoryRepository(private val context: Context) {
    private val db = Firebase.firestore
    private val prefs = context.getSharedPreferences("bmi_prefs", Context.MODE_PRIVATE)

    private fun historyCollection(uid: String) = db.collection("users").document(uid).collection("history")

    private fun readLocalHistory(): List<BMIRecord> {
        val raw = prefs.getString("local_history", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<BMIRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val rec = BMIRecord(
                    timestamp = obj.optLong("timestamp", 0L),
                    bmi = obj.optDouble("bmi", 0.0).toFloat(),
                    category = obj.optString("category", ""),
                    gender = obj.optString("gender", ""),
                    heightCm = obj.optInt("heightCm", 0),
                    weightKg = obj.optDouble("weightKg", 0.0).toFloat()
                )
                list.add(rec)
            }
            list
        } catch (e: Exception) {
            Log.e("HistoryRepository", "Error parsing local history", e)
            emptyList()
        }
    }

    private fun writeLocalHistory(list: List<BMIRecord>) {
        try {
            val arr = JSONArray()
            list.forEach { r ->
                val obj = JSONObject()
                obj.put("timestamp", r.timestamp)
                obj.put("bmi", r.bmi.toDouble())
                obj.put("category", r.category)
                obj.put("gender", r.gender)
                obj.put("heightCm", r.heightCm)
                obj.put("weightKg", r.weightKg.toDouble())
                arr.put(obj)
            }
            prefs.edit().putString("local_history", arr.toString()).apply()
        } catch (e: Exception) {
            Log.e("HistoryRepository", "Error writing local history", e)
        }
    }

    fun historyFlow(): Flow<List<BMIRecord>> = callbackFlow {
        val uid = Firebase.auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Log.w("HistoryRepository", "No user UID available, returning local history")
            trySend(readLocalHistory())
            close()
            return@callbackFlow
        }

        try {
            // When user signs in, attempt to flush local pending records to Firestore
            val pending = readLocalHistory()
            if (pending.isNotEmpty()) {
                Log.d("HistoryRepository", "Found ${pending.size} pending local records, attempting sync")
                pending.forEach { rec ->
                    try {
                        val docId = rec.timestamp.toString()
                        historyCollection(uid).document(docId).set(rec).await()
                    } catch (e: Exception) {
                        Log.w("HistoryRepository", "Failed syncing local record ${rec.timestamp}", e)
                    }
                }
                // clear local after attempting sync
                writeLocalHistory(emptyList())
            }

            Log.d("HistoryRepository", "Setting up history listener for uid: $uid")
            val listener = historyCollection(uid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snap, ex ->
                    if (ex != null) {
                        Log.e("HistoryRepository", "Snapshot listener error: ${ex.message}", ex)
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
            // Always persist locally as a fallback (so unsigned users see history)
            val existing = readLocalHistory().toMutableList()
            existing.add(0, record) // keep newest first
            writeLocalHistory(existing)

            val uid = Firebase.auth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                Log.w("HistoryRepository", "No authenticated user; record saved locally only: ${record.timestamp}")
                return
            }

            val docId = record.timestamp.toString()
            Log.d("HistoryRepository", "Adding record to Firestore: $docId")
            historyCollection(uid).document(docId).set(record).await()

            // On success, remove this record from local cache if present
            try {
                val remaining = readLocalHistory().filter { it.timestamp != record.timestamp }
                writeLocalHistory(remaining)
            } catch (e: Exception) {
                Log.w("HistoryRepository", "Error removing synced local record", e)
            }

            Log.d("HistoryRepository", "Record added successfully")
        } catch (e: Exception) {
            Log.e("HistoryRepository", "Error adding record", e)
            // Don't throw - allow app to continue even if Firestore fails
        }
    }
}
