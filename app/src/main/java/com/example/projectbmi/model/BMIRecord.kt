package com.example.projectbmi.model

data class BMIRecord(
    val timestamp: Long,
    val bmi: Float,
    val category: String,
    val gender: String,
    val heightCm: Int,
    val weightKg: Float
)
