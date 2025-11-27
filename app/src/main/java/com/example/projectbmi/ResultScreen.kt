package com.example.projectbmi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.navigation.NavController
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    navController: NavController,
    bmi: String,
    category: String,
    gender: String
) {
    val bmiValue = bmi.toDoubleOrNull() ?: 0.0
    val categoryText = category.replace("%20", " ")
    val genderText = gender.replace("%20", " ")
    val drawableResId = getDrawableForCategoryAndGender(categoryText, genderText)
    val context = LocalContext.current
    val historyVm: HistoryViewModel = viewModel()

    val categoryColor = when (categoryText) {
        "Underweight" -> Color(0xFF4A90E2)
        "Normal" -> Color(0xFF2F9E44)
        "Overweight" -> Color(0xFFFFA500)
        "Obese" -> Color(0xFFEE5A6F)
        else -> Color(0xFF5A4AE3)
    }

    val description = when (categoryText) {
        "Underweight" -> "Your weight is below normal. Consider increasing nutritious meals and maintaining a healthy routine."
        "Normal" -> "You're maintaining a healthy weight range. Keep up regular exercise and balanced meals."
        "Overweight" -> "You are slightly above the normal range. Try adopting a healthier diet and routine exercises."
        "Obese" -> "Your BMI is in the obese range. Consider consulting a health professional for a better lifestyle plan."
        else -> "Maintain a balanced lifestyle for optimal health."
    }

    // Dynamic recommendations based on category and gender
    val recommendations = getRecommendations(categoryText, genderText)

    // AI-generated weekly schedule
    val aiRepo = AIRepository()
    var weeklySchedule by remember { mutableStateOf<List<String>>(emptyList()) }
    var generating by remember { mutableStateOf(false) }
    var showTasksDialog by remember { mutableStateOf(false) }

    // Simple entry animation flags
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnim = true }
    val scaleAnim by animateFloatAsState(targetValue = if (startAnim) 1f else 0.85f, animationSpec = tween(durationMillis = 600))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Your BMI Result",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF5A4AE3)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    navigationIconContentColor = Color(0xFF5A4AE3),
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFFBFBFB))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ===== HEADER CARD =====
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF7B68EE),
                                        Color(0xFF5A4AE3),
                                        Color(0xFF6B5BE3)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Your BMI",
                                    color = Color.White.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.labelMedium,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = String.format("%.1f", bmiValue),
                                    color = Color.White,
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 56.sp,
                                    modifier = Modifier.scale(scaleAnim)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    Text(
                                        text = categoryText,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        color = categoryColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val imageId = drawableResId ?: R.drawable.person_placeholder
                                Image(
                                    painter = painterResource(id = imageId),
                                    contentDescription = "$categoryText illustration",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .scale(scaleAnim),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ===== THREE ACTION BUTTONS =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // shared gradient for primary button and shared sizes
                    val btnGradient = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF7B68EE), Color(0xFF5A4AE3), Color(0xFF6B5BE3))
                    )
                    // Ask AI Button
                    GradientActionButton(
                        text = "Ask AI",
                        modifier = Modifier.weight(1f).height(50.dp),
                        gradient = btnGradient,
                        onClick = { navController.navigate("chat/$categoryText") }
                    )

                    // Recalculate Button
                    SecondaryActionButton(
                        text = "Recalculate",
                        modifier = Modifier.weight(1f).height(50.dp),
                        onClick = {
                            navController.navigate("calculator") {
                                popUpTo("result/{bmi}/{category}/{gender}") { inclusive = true }
                            }
                        }
                    )

                    // History Button
                    SecondaryActionButton(
                        text = "History",
                        modifier = Modifier.weight(1f).height(50.dp),
                        onClick = { navController.navigate("history") }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ===== INTERPRETATION CARD =====
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            "Interpretation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4A4A4A),
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // BMI Range Indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEEEEEE))
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF4A90E2)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF2F9E44)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFFA500)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFEE5A6F)))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("< 18.5", style = MaterialTheme.typography.labelSmall, color = Color(0xFF999999))
                            Text("18.5 - 24.9", style = MaterialTheme.typography.labelSmall, color = Color(0xFF999999))
                            Text("25 - 29.9", style = MaterialTheme.typography.labelSmall, color = Color(0xFF999999))
                            Text("> 30", style = MaterialTheme.typography.labelSmall, color = Color(0xFF999999))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ===== RECOMMENDATIONS CARD =====
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Recommendations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                            Spacer(modifier = Modifier.weight(1f))
                            Text("Personalized tips", style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAA9B3))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Column {
                            recommendations.forEach { tip ->
                                Row(modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(tip, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4A4A4A))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ===== DAILY QUEST CARD =====
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            if (weeklySchedule.isEmpty()) {
                                generating = true
                                weeklySchedule = AIRepository().generateWeeklySchedule(bmiValue.toFloat(), categoryText, genderText)
                                try {
                                    val prefs = context.getSharedPreferences("bmi_prefs", Context.MODE_PRIVATE)
                                    prefs.edit().putString("weekly_schedule", weeklySchedule.joinToString("||")).apply()
                                } catch (_: Exception) {}
                                generating = false
                            }
                            showTasksDialog = true
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Daily Quest",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            if (generating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF6366F1))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "View schedule",
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        val todayIndex = LocalDate.now().dayOfWeek.value % 7
                        val todaysTask = weeklySchedule.getOrNull(todayIndex)

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFEDD5),
                            tonalElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("📌", fontSize = 18.sp, modifier = Modifier.padding(top = 2.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Today's Goal",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF5D4037)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        todaysTask ?: "Tap to generate daily task",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF5D4037),
                                        maxLines = 3,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Text(
                            "📅 Tap card to view full weekly schedule",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFAA9B83),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                // Weekly Tasks Dialog
                if (showTasksDialog) {
                    AlertDialog(
                        onDismissRequest = { showTasksDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showTasksDialog = false }) {
                                Text("Close")
                            }
                        },
                        title = { Text("Weekly Tasks") },
                        text = {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                val schedule = if (weeklySchedule.isEmpty()) {
                                    AIRepository().generateWeeklySchedule(bmiValue.toFloat(), categoryText, genderText)
                                } else weeklySchedule

                                val days = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
                                schedule.forEachIndexed { idx, t ->
                                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                                        Text(days.getOrNull(idx) ?: "-", modifier = Modifier.width(50.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(t, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===== SHARE & SAVE ACTIONS =====
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = {
                        val shareText = "My BMI: ${String.format("%.1f", bmiValue)} ($categoryText)"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val chooser = Intent.createChooser(sendIntent, "Share via")
                        context.startActivity(chooser)
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share Result", tint = Color(0xFF5A4AE3))
                    }

                    IconButton(onClick = {
                        val prefs = context.getSharedPreferences("bmi_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("last_bmi", String.format("%.1f", bmiValue)).putString("last_category", categoryText).apply()
                        try {
                            val rec = com.example.projectbmi.model.BMIRecord(
                                timestamp = System.currentTimeMillis(),
                                bmi = bmiValue.toFloat(),
                                category = categoryText,
                                gender = genderText,
                                heightCm = 0,
                                weightKg = 0f
                            )
                            historyVm.saveRecord(rec)
                            Toast.makeText(context, "Hasil BMI disimpan ke riwayat", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Gagal menyimpan riwayat: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save Result", tint = Color(0xFF5A4AE3))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DayCircle(day: String, isActive: Boolean = false) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                color = if (isActive) Color(0xFFFFD45A) else Color(0xFFF0F0F0),
                shape = RoundedCornerShape(50)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color.Black else Color(0xFF666666),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GradientActionButton(
    text: String,
    modifier: Modifier = Modifier,
    gradient: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(6.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(brush = gradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    borderColor: Color = Color(0xFF5A4AE3),
    bgColor: Color = Color(0xFFF2F2F5),
    textColor: Color = Color(0xFF5A4AE3)
) {
    Box(
        modifier = modifier
            .shadow(4.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(color = bgColor)
            .border(BorderStroke(1.dp, borderColor.copy(alpha = 0.14f)), shape = RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * Helper function to get drawable resource based on category and gender
 */
private fun getDrawableForCategoryAndGender(category: String, gender: String): Int? {
    return when {
        category.equals("Underweight", ignoreCase = true) && gender.equals("Male", ignoreCase = true) -> R.drawable.person_male_underweight
        category.equals("Underweight", ignoreCase = true) && gender.equals("Female", ignoreCase = true) -> R.drawable.person_female_underweight

        category.equals("Normal", ignoreCase = true) && gender.equals("Male", ignoreCase = true) -> R.drawable.person_male_normal
        category.equals("Normal", ignoreCase = true) && gender.equals("Female", ignoreCase = true) -> R.drawable.person_female_normal

        category.equals("Overweight", ignoreCase = true) && gender.equals("Male", ignoreCase = true) -> R.drawable.person_male_overweight
        category.equals("Overweight", ignoreCase = true) && gender.equals("Female", ignoreCase = true) -> R.drawable.person_female_overweight

        category.equals("Obese", ignoreCase = true) && gender.equals("Male", ignoreCase = true) -> R.drawable.person_male_obese
        category.equals("Obese", ignoreCase = true) && gender.equals("Female", ignoreCase = true) -> R.drawable.person_female_obese

        else -> null
    }
}

// Return simple recommendation strings tailored by category and gender
private fun getRecommendations(category: String, gender: String): List<String> {
    return when (category) {
        "Underweight" -> listOf(
            "Increase calorie intake with nutrient-dense foods.",
            "Include strength-training to build muscle mass.",
            "Have regular meals and healthy snacks."
        )
        "Normal" -> listOf(
            "Maintain a balanced diet and regular activity.",
            "Monitor weight monthly to detect changes early.",
            "Keep hydration and sleep hygiene."
        )
        "Overweight" -> listOf(
            "Reduce processed foods and added sugars.",
            "Aim for 150 minutes of moderate activity weekly.",
            "Consider small, sustainable calorie reductions."
        )
        "Obese" -> listOf(
            "Consult a healthcare professional for personalized plan.",
            "Focus on gradual weight loss (0.5-1 kg/week).",
            "Combine diet changes with supervised physical activity."
        )
        else -> listOf(
            "Maintain a balanced lifestyle.",
            "Stay active and monitor progress."
        )
    }
}
