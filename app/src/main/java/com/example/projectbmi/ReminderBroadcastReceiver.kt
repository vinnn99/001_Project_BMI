package com.example.projectbmi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.projectbmi.model.QuestTask
import com.google.gson.Gson
import java.time.LocalDate

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.example.projectbmi.WORKOUT_REMINDER") {
            Log.d("ReminderBroadcast", "Alarm triggered at ${java.util.Calendar.getInstance().time}")
            
            context?.let {
                showWorkoutReminder(it)
            }
        }
    }

    private fun showWorkoutReminder(context: Context) {
        try {
            val prefs = context.getSharedPreferences("daily_quest_prefs", Context.MODE_PRIVATE)
            val lastCompletedDate = prefs.getString("daily_completed_date", "")
            val todayDate = LocalDate.now().toString()
            
            // Only show reminder if today's workout is not completed
            if (lastCompletedDate != todayDate) {
                val userPrefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
                val scheduleJson = userPrefs.getString("schedule", null)
                
                if (scheduleJson != null) {
                    try {
                        val gson = Gson()
                        val scheduleArray = gson.fromJson(scheduleJson, Array<QuestTask>::class.java)
                        val schedule: List<QuestTask> = scheduleArray.toList()
                        
                        if (schedule.isNotEmpty()) {
                            val dayIndex = LocalDate.now().dayOfWeek.value - 1
                            val taskIndex = if (dayIndex >= 0 && dayIndex < schedule.size) dayIndex else 0
                            val todayTask: QuestTask = schedule[taskIndex]
                            
                            // Show reminder notification
                            NotificationHelper.createNotificationChannel(context)
                            NotificationHelper.sendTaskReminder(context, todayTask)
                            Log.d("ReminderBroadcast", "Workout reminder shown for: ${todayTask.task}")
                        }
                    } catch (e: Exception) {
                        Log.e("ReminderBroadcast", "Error parsing schedule: ${e.message}")
                    }
                }
            } else {
                Log.d("ReminderBroadcast", "Workout already completed today, skipping reminder")
            }
        } catch (e: Exception) {
            Log.e("ReminderBroadcast", "Error showing reminder: ${e.message}", e)
        }
    }
}
