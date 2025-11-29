package com.example.projectbmi.model

data class QuestTask(
    val day: String,
    val task: String,
    val category: String = "",
    val duration: Int = 30,
    val intensity: String = "medium",
    val notes: String = ""
)
