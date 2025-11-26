package com.example.projectbmi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectbmi.model.BMIRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class CoachViewModel(private val repo: AIRepository = AIRepository()) : ViewModel() {
    private val _tips = MutableStateFlow<List<String>>(emptyList())
    val tips: StateFlow<List<String>> = _tips

    fun loadQuickTips(bmi: Float, category: String, gender: String, age: Int = 0, history: List<BMIRecord> = emptyList()) {
        viewModelScope.launch {
            try {
                Log.d("CoachViewModel", "Loading tips for BMI=$bmi, category=$category")
                val result = repo.generateQuickTips(bmi, category, gender, age, history)
                _tips.value = result
                Log.d("CoachViewModel", "Tips loaded: ${result.size} items")
            } catch (e: Exception) {
                Log.e("CoachViewModel", "Error loading tips", e)
                _tips.value = listOf("Stay consistent with your fitness goals and maintain a healthy lifestyle!")
            }
        }
    }
    
    fun generateChatResponse(question: String, context: String, onResponse: (String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("CoachViewModel", "Generating response for question: $question")
                // For now, generate rule-based responses. Later can be replaced with OpenAI/API call
                val response = when {
                    question.contains("diet", ignoreCase = true) -> 
                        "Based on your BMI, I recommend focusing on a balanced diet with plenty of whole foods. " +
                        "Try to include lean proteins, vegetables, and whole grains in every meal. " +
                        "Would you like specific meal suggestions?"
                    
                    question.contains("exercise", ignoreCase = true) ->
                        "For effective exercise, start with 30 minutes of moderate activity 5 times a week. " +
                        "This could be brisk walking, swimming, or cycling. " +
                        "Would you like a beginner-friendly workout plan?"
                    
                    question.contains("weight", ignoreCase = true) ->
                        "Healthy weight management is about sustainable lifestyle changes. " +
                        "Small, consistent changes to diet and activity levels are more effective than rapid changes. " +
                        "What specific concerns do you have about weight management?"
                    
                    else ->
                        "I can help you with diet plans, exercise routines, and general health advice. " +
                        "What specific aspect would you like to focus on?"
                }
                Log.d("CoachViewModel", "Response generated successfully")
                onResponse(response)
            } catch (e: Exception) {
                Log.e("CoachViewModel", "Error generating response", e)
                onResponse("Sorry, I'm having trouble understanding your question. Could you rephrase it?")
            }
        }
    }
}
