package com.example.projectbmi

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Female
import androidx.compose.material.icons.outlined.Male
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectbmi.model.BMIRecord
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMICalculatorScreen(
    navController: NavController,
    quickRecalc: Boolean = false,
    vm: BMIViewModel = viewModel()
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(0) }
    // navigationTriggered removed; navigation now only happens from NEXT/FINISH button

    val gender by vm.gender.collectAsState()
    val age by vm.age.collectAsState()
    val height by vm.height.collectAsState()
    val weight by vm.weight.collectAsState()
    // History VM to persist full records (height/weight)
    val historyVm: HistoryViewModel = viewModel()

    android.util.Log.d("BMICalc", "Rendering: step=$step")

    DisposableEffect(Unit) {
        onDispose {
            android.util.Log.d("BMICalc", "Screen disposed")
        }
    }

    // Animation for step transitions
    val transition = updateTransition(targetState = step, label = "step")
    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 300) },
        label = "alpha"
    ) { 1f }

    LaunchedEffect(quickRecalc) {
        if (quickRecalc) {
            try {
                val prefs = context.getSharedPreferences("bmi_prefs", android.content.Context.MODE_PRIVATE)
                val savedGender = prefs.getString("last_gender", null)
                val savedAge = prefs.getInt("last_age", -1)
                val savedHeight = prefs.getInt("last_height_cm", -1)
                val savedWeight = prefs.getFloat("last_weight_kg", -1f)

                if (!savedGender.isNullOrEmpty()) vm.setGender(savedGender)
                if (savedAge in 1..120) vm.setAge(savedAge)
                if (savedHeight in 50..250) vm.setHeight(savedHeight)
                if (savedWeight in 20f..300f) vm.setWeight(savedWeight)

                step = 3 // jump straight to weight step
            } catch (e: Exception) {
                android.util.Log.e("BMICalc", "Failed to load quick recalc data", e)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),  // Top
                        Color(0xFF764ba2)   // Bottom
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "BMI Calculator", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ) 
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .alpha(alpha),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // Enhanced Animated Progress Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isActive = index == step
                        val isFilled = index <= step
                        
                        val scale by animateFloatAsState(
                            targetValue = if (isActive) 1.3f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "scale$index"
                        )
                        
                        val size by animateDpAsState(
                            targetValue = if (isActive) 16.dp else if (isFilled) 12.dp else 10.dp,
                            animationSpec = tween(300),
                            label = "size$index"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(size)
                                .scale(scale)
                                .shadow(
                                    elevation = if (isActive) 12.dp else if (isFilled) 4.dp else 0.dp,
                                    shape = CircleShape,
                                    spotColor = Color(0xFFFF6584).copy(alpha = 0.5f)
                                )
                                .background(
                                    brush = if (isActive) {
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFFFF6584), Color(0xFFFF8FA3))
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                if (isFilled) Color.White else Color.White.copy(alpha = 0.25f),
                                                if (isFilled) Color.White else Color.White.copy(alpha = 0.25f)
                                            )
                                        )
                                    },
                                    shape = CircleShape
                                )
                        )
                        
                        if (index < 3) {
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                when (step) {
                    0 -> {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(400)) + slideInVertically(tween(400)),
                            label = "gender_card"
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(36.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Select Gender",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1A1A1A),
                                        letterSpacing = (-0.5).sp
                                    )
                                    Spacer(modifier = Modifier.height(36.dp))
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                        // Male Card
                                        GenderCard(
                                            icon = Icons.Outlined.Male,
                                            label = "Male",
                                            selected = gender == "Male",
                                            onClick = { vm.setGender("Male") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        // Female Card
                                        GenderCard(
                                            icon = Icons.Outlined.Female,
                                            label = "Female",
                                            selected = gender == "Female",
                                            onClick = { vm.setGender("Female") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(400)) + slideInVertically(tween(400)),
                            label = "age_card"
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(36.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "What is your age?",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1A1A1A),
                                        letterSpacing = (-0.5).sp
                                    )
                                    Spacer(modifier = Modifier.height(48.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        // Minus Button
                                        FloatingActionButton(
                                            onClick = { if (age > 1) vm.setAge(age - 1) },
                                            containerColor = Color(0xFF5B67F1),
                                            elevation = FloatingActionButtonDefaults.elevation(
                                                defaultElevation = 6.dp,
                                                pressedElevation = 12.dp
                                            ),
                                            modifier = Modifier.size(64.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.KeyboardArrowLeft,
                                                contentDescription = "Decrease age",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        
                                        // Age Display
                                        Card(
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFFF8F9FA)
                                            ),
                                            elevation = CardDefaults.cardElevation(4.dp),
                                            modifier = Modifier
                                                .width(120.dp)
                                                .height(100.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    age.toString(),
                                                    fontSize = 64.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF5B67F1),
                                                    letterSpacing = (-2).sp
                                                )
                                            }
                                        }
                                        
                                        // Plus Button - CRITICAL: Now with proper spacing
                                        FloatingActionButton(
                                            onClick = { if (age < 120) vm.setAge(age + 1) },
                                            containerColor = Color(0xFF5B67F1),
                                            elevation = FloatingActionButtonDefaults.elevation(
                                                defaultElevation = 6.dp,
                                                pressedElevation = 12.dp
                                            ),
                                            modifier = Modifier.size(64.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.KeyboardArrowRight,
                                                contentDescription = "Increase age",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(400)) + slideInVertically(tween(400)),
                            label = "height_card"
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(36.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "What's your height?",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1A1A1A),
                                        letterSpacing = (-0.5).sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Text(
                                        "Enter your height in centimeters",
                                        fontSize = 14.sp,
                                        color = Color.Gray.copy(alpha = 0.7f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    Icon(
                                        imageVector = Icons.Outlined.Straighten,
                                        contentDescription = "Height",
                                        modifier = Modifier.size(64.dp),
                                        tint = Color(0xFF5B67F1)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(24.dp))

                                    var heightInput by remember { mutableStateOf(height.toString()) }
                                    var heightError by remember { mutableStateOf("") }
                                    OutlinedTextField(
                                        value = heightInput,
                                        onValueChange = {
                                            var filtered = it.filter { ch -> ch.isDigit() }.take(3)
                                            heightInput = filtered
                                            val parsed = filtered.toIntOrNull()
                                            if (parsed == null) {
                                                heightError = "Please enter a number"
                                            } else if (parsed < 50 || parsed > 250) {
                                                heightError = "Height must be between 50-250 cm"
                                            } else {
                                                heightError = ""
                                                vm.setHeight(parsed)
                                            }
                                        },
                                        label = { 
                                            Text(
                                                "Height (cm)",
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            ) 
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .padding(top = 8.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF5B67F1),
                                            unfocusedBorderColor = Color(0xFFD0D0D0),
                                            focusedLabelColor = Color(0xFF5B67F1),
                                            unfocusedLabelColor = Color(0xFF9E9E9E),
                                            cursorColor = Color(0xFF5B67F1)
                                        ),
                                        textStyle = TextStyle(
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = Color(0xFF1A1A1A)
                                        )
                                    )
                                    if (heightError.isNotEmpty()) {
                                        Text(
                                            heightError,
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(400)) + slideInVertically(tween(400)),
                            label = "weight_card"
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(36.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "What's your weight?",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1A1A1A),
                                        letterSpacing = (-0.5).sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Text(
                                        "Enter your weight in kilograms",
                                        fontSize = 14.sp,
                                        color = Color.Gray.copy(alpha = 0.7f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    Icon(
                                        imageVector = Icons.Outlined.MonitorWeight,
                                        contentDescription = "Weight",
                                        modifier = Modifier.size(64.dp),
                                        tint = Color(0xFFFF6584)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(24.dp))

                                    var weightInput by remember { mutableStateOf(String.format("%.1f", weight)) }
                                    var weightError by remember { mutableStateOf("") }
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = {
                                            var filtered = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }
                                            val normalized = filtered.replace(',', '.')
                                            val firstDot = normalized.indexOf('.')
                                            if (firstDot >= 0) {
                                                filtered = normalized.substring(0, firstDot + 1) + normalized.substring(firstDot + 1).replace(".", "")
                                                val fractional = filtered.substring(firstDot + 1)
                                                if (fractional.length > 1) filtered = filtered.substring(0, firstDot + 2)
                                            } else {
                                                filtered = normalized
                                            }
                                            weightInput = filtered
                                            val parsed = filtered.toFloatOrNull()
                                            if (parsed == null) {
                                                weightError = "Please enter a number"
                                            } else if (parsed < 20f || parsed > 300f) {
                                                weightError = "Weight must be between 20-300 kg"
                                            } else {
                                                weightError = ""
                                                vm.setWeight(parsed)
                                            }
                                        },
                                        label = { 
                                            Text(
                                                "Weight (kg)",
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            ) 
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .padding(top = 8.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF5B67F1),
                                            unfocusedBorderColor = Color(0xFFD0D0D0),
                                            focusedLabelColor = Color(0xFF5B67F1),
                                            unfocusedLabelColor = Color(0xFF9E9E9E),
                                            cursorColor = Color(0xFF5B67F1)
                                        ),
                                        textStyle = TextStyle(
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = Color(0xFF1A1A1A)
                                        )
                                    )
                                    if (weightError.isNotEmpty()) {
                                        Text(
                                            weightError,
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { if (step > 0) step -= 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(2.dp, Color.White),
                        enabled = step > 0
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (step > 0) {
                                Icon(
                                    Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                "BACK",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 12.dp
                        ),
                        onClick = {
                        var canAdvance = true
                        // Simple validation: if on height or weight step, ensure no error text
                        if (step == 2) {
                            // read local validation state by checking vm.height range
                            val h = vm.height.value
                            if (h < 50 || h > 250) canAdvance = false
                        }
                        if (step == 3) {
                            val w = vm.weight.value
                            if (w < 20f || w > 300f) canAdvance = false
                        }

                        if (canAdvance) {
                            if (step < 3) step += 1 else {
                                try {
                                    val (bmi, category) = vm.calculateBmi()
                                    android.util.Log.d("BMICalc", "FINISH button: BMI=$bmi, Category=$category")
                                    
                                    // Save to SharedPreferences for Ask AI to use later
                                    val prefs = context.getSharedPreferences("bmi_prefs", android.content.Context.MODE_PRIVATE)
                                    val bmiFormatted = String.format("%.1f", bmi)
                                    prefs.edit()
                                        .putString("last_bmi", bmiFormatted)
                                        .putString("last_category", category)
                                        .putString("last_gender", vm.gender.value)
                                        .putInt("last_age", vm.age.value)
                                        .putInt("last_height_cm", vm.height.value)
                                        .putFloat("last_weight_kg", vm.weight.value)
                                        .apply()
                                    android.util.Log.d("BMICalc", "Saved to prefs: BMI=$bmiFormatted, Category=$category, Gender=${vm.gender.value}, Age=${vm.age.value}, Height=${vm.height.value}, Weight=${vm.weight.value}")
                                    
                                    // Save full record including height and weight before navigating
                                    val rec = BMIRecord(
                                        timestamp = System.currentTimeMillis(),
                                        bmi = bmi,
                                        category = category,
                                        gender = vm.gender.value,
                                        heightCm = vm.height.value,
                                        weightKg = vm.weight.value
                                    )
                                    historyVm.saveRecord(rec)
                                    val encodedCategory = java.net.URLEncoder.encode(category, "UTF-8")
                                    val route = "result/$bmiFormatted/$encodedCategory/${vm.gender.value}"
                                    android.util.Log.d("BMICalc", "FINISH: Navigating to route: $route")
                                    navController.navigate(route)
                                } catch (e: Exception) {
                                    android.util.Log.e("BMICalc", "Error on FINISH button click", e)
                                }
                            }
                        }
                    }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                if (step < 3) "NEXT" else "FINISH",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp,
                                color = Color(0xFF5B67F1)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                if (step == 3) Icons.Default.Check else Icons.Default.KeyboardArrowRight,
                                contentDescription = if (step == 3) "Finish" else "Next",
                                tint = Color(0xFF5B67F1),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    )
    }
}

// Helper Composable for Gender Selection Card
@Composable
fun GenderCard(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF6366F1) else Color(0xFFF3F4F6)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 12.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(48.dp),
                tint = if (selected) Color.White else Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Color(0xFF000000),
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }
    }
}