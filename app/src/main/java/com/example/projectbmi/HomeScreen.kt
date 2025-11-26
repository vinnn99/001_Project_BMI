package com.example.projectbmi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate

@Composable
fun HomeScreen(
    navController: NavController,
    historyViewModel: HistoryViewModel
) {
    val history = historyViewModel.history.collectAsState().value
    val latestRecord = history.firstOrNull()
    val bmiValue = latestRecord?.bmi ?: 0.0
    val bmiCategory = latestRecord?.category ?: "Normal Weight"
    val bmiDescription = "You're maintaining a healthy weight. Keep exercising and eat nutritious meals."
    val progress = (latestRecord?.let { 60 } ?: 50).coerceIn(0, 100)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F5FA))
            .padding(20.dp)
    ) {

        //--------------------------------------------------
        // HEADER
        //--------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BodyWise",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B3F72)
                )
                Text(
                    text = "Track your health daily",
                    fontSize = 14.sp,
                    color = Color(0xFF7A738C)
                )
            }

            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFF4B3F72),
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        //--------------------------------------------------
        // BMI CARD
        //--------------------------------------------------
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = String.format("%.1f", bmiValue),
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B3F72)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(0xFFAEE9C1),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            bmiCategory,
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B3F72)
                        )
                    }
                }

                Image(
                    painter = painterResource(id = getImageForCategory(bmiCategory)),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(28.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        //--------------------------------------------------
        // COACHING CARD
        //--------------------------------------------------
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            shadowElevation = 5.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = bmiDescription,
                modifier = Modifier.padding(22.dp),
                fontSize = 17.sp,
                color = Color(0xFF4B3F72),
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        //--------------------------------------------------
        // DAILY QUEST CARD
        //--------------------------------------------------
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            shadowElevation = 5.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Daily Quest",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B3F72)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.Notifications,
                        null,
                        tint = Color(0xFF4B3F72)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE3E3E3), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                CalendarRow()

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${progress}% Progress", color = Color(0xFF4B3F72))

                    Spacer(modifier = Modifier.width(12.dp))

                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier
                            .height(8.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFAEE9C1),
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        //--------------------------------------------------
        // ACTION BUTTONS
        //--------------------------------------------------
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { 
                    try {
                        navController.navigate("chat/home")
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Navigation to chat failed", e)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(54.dp)
            ) {
                Text("Tanya AI", color = Color(0xFF4B3F72), fontSize = 18.sp)
            }

            Button(
                onClick = { 
                    try {
                        navController.navigate("calculator")
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Navigation to calculator failed", e)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B3F72)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(54.dp)
            ) {
                Text("Calculate Again", color = Color.White, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        //--------------------------------------------------
        // HISTORY BUTTON
        //--------------------------------------------------
        Button(
            onClick = { 
                try {
                    navController.navigate("history")
                } catch (e: Exception) {
                    android.util.Log.e("HomeScreen", "Navigation to history failed", e)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE066)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Icon(
                Icons.Default.Notifications,
                null,
                tint = Color(0xFF4B3F72)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "View History",
                color = Color(0xFF4B3F72),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CalendarRow() {
    val today = LocalDate.now()
    val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    // Get current day of week (1-7 where 1=Monday), convert to 0-6 where 0=Sunday
    val currentDayOfWeek = (today.dayOfWeek.value % 7) // Java's dayOfWeek: 1=Mon, 7=Sun -> we want 0=Sun

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        daysOfWeek.forEachIndexed { index, day ->
            if (index == currentDayOfWeek) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFE066)
                    ) {
                        Text(
                            text = today.dayOfMonth.toString(),
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4B3F72)
                        )
                    }
                    Text(
                        text = "Today",
                        fontSize = 12.sp,
                        color = Color(0xFF4B3F72)
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE0E0E0)
                    ) {
                        Spacer(modifier = Modifier.size(32.dp))
                    }
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        color = Color(0xFFB0B0B0)
                    )
                }
            }
        }
    }
}

fun getImageForCategory(category: String): Int {
    return when (category) {
        "Underweight" -> R.drawable.person_placeholder
        "Normal Weight" -> R.drawable.person_placeholder
        "Overweight" -> R.drawable.person_placeholder
        "Obese" -> R.drawable.person_placeholder
        else -> R.drawable.person_placeholder
    }
}
