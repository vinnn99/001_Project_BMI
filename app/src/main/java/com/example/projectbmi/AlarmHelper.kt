package com.example.projectbmi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object AlarmHelper {
    private const val TAG = "AlarmHelper"

    fun scheduleWorkoutReminder(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Create intent for BroadcastReceiver
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                action = "com.example.projectbmi.WORKOUT_REMINDER"
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1001, // Unique ID for this alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Schedule for 6:00 AM
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 6)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            
            // If time already passed today, schedule for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            // Schedule repeating alarm - daily at 6 AM
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ requires SCHEDULE_EXACT_ALARM permission
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Exact alarm scheduled for ${calendar.time}")
                } catch (e: SecurityException) {
                    // Fallback if permission denied
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Fallback alarm scheduled (no exact permission) for ${calendar.time}")
                }
            } else {
                // Pre-Android 12
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Exact alarm scheduled for ${calendar.time}")
            }
            
            // Also schedule repeating alarm for daily trigger
            try {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
                Log.d(TAG, "Daily repeating alarm scheduled")
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling repeating alarm", e)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm: ${e.message}", e)
        }
    }

    fun cancelWorkoutReminder(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                action = "com.example.projectbmi.WORKOUT_REMINDER"
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Alarm cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling alarm: ${e.message}", e)
        }
    }
}
