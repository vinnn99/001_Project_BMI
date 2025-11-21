package com.example.projectbmi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BMIViewModel : ViewModel() {
    private val _gender = MutableStateFlow("Male")
    val gender: StateFlow<String> = _gender

    private val _age = MutableStateFlow(25)
    val age: StateFlow<Int> = _age

    private val _height = MutableStateFlow(170)
    val height: StateFlow<Int> = _height

    private val _weight = MutableStateFlow(70f)
    val weight: StateFlow<Float> = _weight

    fun setGender(g: String) { _gender.value = g }
    fun setAge(a: Int) { _age.value = a }
    fun setHeight(h: Int) { _height.value = h }
    fun setWeight(w: Float) { _weight.value = w }

    fun calculateBmi(): Pair<Float, String> {
        val h = _height.value / 100f
        val bmi = if (h > 0) _weight.value / (h * h) else 0f
        val category = when {
            bmi < 18.5f -> "Underweight"
            bmi < 25f -> "Normal"
            bmi < 30f -> "Overweight"
            else -> "Obese"
        }
        return bmi to category
    }
}
