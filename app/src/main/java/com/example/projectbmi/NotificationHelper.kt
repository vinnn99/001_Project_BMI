package com.example.projectbmi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "workout_reminders"
    private const val CHANNEL_NAME = "Workout Reminders"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for daily workouts"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun generatePersonalizedNotification(
        context: Context,
        userProfile: UserDiscoveryState
    ) {
        val goal = userProfile.primaryGoal?.lowercase() ?: "maintain"
        val intensity = userProfile.exerciseIntensity?.lowercase() ?: "medium"
        val focusAreas = userProfile.focusArea?.split(",")?.map { it.trim().lowercase() } ?: listOf("mixed")
        val experience = userProfile.experienceLevel?.lowercase() ?: "intermediate"

        // Generate personalized recommendation
        val (title, message) = generateRecommendation(goal, intensity, focusAreas, experience)

        // Create notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun generateRecommendation(
        goal: String,
        intensity: String,
        focusAreas: List<String>,
        experience: String
    ): Pair<String, String> {
        val title = when (goal) {
            "weight loss" -> "💪 Time to Burn Some Calories!"
            "muscle gain" -> "🏋️ Build That Strength!"
            "endurance" -> "🏃 Keep Your Cardio Strong!"
            "flexibility" -> "🧘 Time to Stretch It Out!"
            else -> "🎯 Let's Get Moving!"
        }

        val message = when {
            intensity.contains("high") && focusAreas.any { it.contains("cardio") } ->
                "Try a 30-min HIIT cardio session today! High intensity for maximum results."
            
            intensity.contains("high") && focusAreas.any { it.contains("strength") } ->
                "Time for some heavy lifting! Do 4-5 compound movements with progressive overload."
            
            intensity.contains("medium") && focusAreas.any { it.contains("strength") } ->
                "Let's do a balanced strength workout. 3-4 exercises per muscle group."
            
            intensity.contains("medium") && focusAreas.any { it.contains("cardio") } ->
                "Perfect time for a 20-30 min steady-state cardio session."
            
            intensity.contains("low") ->
                "Today's a good day for a light walk or easy yoga. Recovery matters too!"
            
            focusAreas.any { it.contains("flexibility") } ->
                "Dedicate 20 minutes to stretching and mobility work today."
            
            else -> 
                "Your personalized workout is ready. Open the app to see your recommendations!"
        }

        return Pair(title, message)
    }

    fun sendTestNotification(context: Context, task: com.example.projectbmi.model.QuestTask) {
        try {
            android.util.Log.d("NotificationHelper", "Attempting to send notification for: ${task.task}")
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("💪 Don't Forget Your Workout!")
                .setContentText(task.task)
                .setStyle(NotificationCompat.BigTextStyle().bigText("Today's task: ${task.task}\n\nDuration: ~${task.duration} minutes"))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.notify(1001, notificationBuilder.build())
            
            android.util.Log.d("NotificationHelper", "Notification sent successfully")
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Error sending notification: ${e.message}", e)
            e.printStackTrace()
        }
    }

    fun sendTaskReminder(context: Context, task: com.example.projectbmi.model.QuestTask) {
        try {
            android.util.Log.d("NotificationHelper", "Sending workout reminder for: ${task.task}")
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("💪 Time to Exercise!")
                .setContentText(task.task)
                .setStyle(NotificationCompat.BigTextStyle().bigText("Today's workout: ${task.task}\n\nDuration: ${task.duration} minutes\n\nOpen the app to get started!"))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.notify(1001, notificationBuilder.build())
            
            android.util.Log.d("NotificationHelper", "Workout reminder sent successfully")
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Error sending workout reminder: ${e.message}", e)
        }
    }
}
