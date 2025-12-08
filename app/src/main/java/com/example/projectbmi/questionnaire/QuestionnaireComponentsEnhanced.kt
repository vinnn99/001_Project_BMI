package com.example.projectbmi.questionnaire

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Enhanced Questionnaire Components with Modern UI/UX
 * Features:
 * - Gradient backgrounds
 * - Step-based color coding
 * - Smooth animations
 * - Better visual hierarchy
 * - Responsive design
 */

/**
 * Step colors for visual distinction
 */
object StepColors {
    fun getStepAccent(stepIndex: Int): Color = when (stepIndex) {
        0 -> Color(0xFF3B82F6)      // Blue - Fitness Goal
        1 -> Color(0xFFF97316)      // Orange - Intensity
        2 -> Color(0xFF10B981)      // Green - Days Available
        3 -> Color(0xFF8B5CF6)      // Purple - Experience
        4 -> Color(0xFFEF4444)      // Red - Focus Area
        else -> Color(0xFF6366F1)   // Default Indigo
    }

    fun getStepGradient(stepIndex: Int): Brush = Brush.linearGradient(
        colors = listOf(
            getStepAccent(stepIndex).copy(alpha = 0.08f),
            getStepAccent(stepIndex).copy(alpha = 0.03f)
        )
    )
}

/**
 * Enhanced circular icon container with gradient background
 */
@Composable
fun EnhancedIconCircle(
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.2f),
                        accentColor.copy(alpha = 0.08f)
                    )
                ),
                shape = CircleShape
            )
            .border(2.dp, accentColor.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(36.dp)
        )
    }
}

/**
 * Enhanced selection indicator with smooth animation
 */
@Composable
fun EnhancedSelectionIndicator(
    isSelected: Boolean,
    isMultiSelect: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = if (isMultiSelect) RoundedCornerShape(6.dp) else CircleShape
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color(0xFFD1D5DB),
        animationSpec = tween(durationMillis = 300),
        label = "borderColor"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected && isMultiSelect) accentColor else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )
    
    Box(
        modifier = modifier
            .size(24.dp)
            .border(2.dp, borderColor, shape)
            .background(backgroundColor, shape),
        contentAlignment = Alignment.Center
    ) {
        when {
            isSelected && isMultiSelect -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            isSelected && !isMultiSelect -> {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accentColor, CircleShape)
                )
            }
        }
    }
}

/**
 * Enhanced option card with multiple states and animations
 */
@Composable
fun EnhancedOptionCard(
    option: QuestionOption,
    isSelected: Boolean,
    isMultiSelect: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "optionScale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 2.dp,
        animationSpec = tween(durationMillis = 300),
        label = "elevation"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.1f) else Color.White,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "borderColor"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(elevation = elevation, shape = RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                onClick = onClick,
                indication = ripple(color = accentColor),
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EnhancedSelectionIndicator(
                isSelected = isSelected,
                isMultiSelect = isMultiSelect,
                accentColor = accentColor
            )
            
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else Color(0xFF9CA3AF),
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = option.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF1F2937) else Color(0xFF4B5563)
            )
            
            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Enhanced question card with step number and accent color
 */
@Composable
fun EnhancedQuestionCard(
    question: String,
    subtitle: String,
    icon: ImageVector,
    stepNumber: Int,
    totalSteps: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Step indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EnhancedIconCircle(
                        icon = icon,
                        accentColor = accentColor
                    )
                    
                    Column {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1F2937)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            // Step counter badge
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = accentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .align(Alignment.End)
            ) {
                Text(
                    text = "Step $stepNumber/$totalSteps",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Step indicator dots with animation
 */
@Composable
fun StepIndicatorDots(
    currentStep: Int,
    totalSteps: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val isPassed = index < currentStep
            
            val dotSize by animateDpAsState(
                targetValue = if (isActive) 8.dp else 6.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "dotSize"
            )
            
            val dotColor by animateColorAsState(
                targetValue = when {
                    isActive -> accentColor
                    isPassed -> accentColor.copy(alpha = 0.6f)
                    else -> Color(0xFFD1D5DB)
                },
                animationSpec = tween(durationMillis = 300),
                label = "dotColor"
            )
            
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(dotColor, CircleShape)
            )
        }
    }
}

/**
 * Enhanced options list with better spacing
 */
@Composable
fun EnhancedOptionsList(
    options: List<QuestionOption>,
    selectedOptions: Set<String>,
    isMultiSelect: Boolean,
    accentColor: Color,
    onSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(options) { option ->
            EnhancedOptionCard(
                option = option,
                isSelected = option.value in selectedOptions,
                isMultiSelect = isMultiSelect,
                accentColor = accentColor,
                onClick = { onSelectionChange(option.value) }
            )
        }
    }
}

/**
 * Enhanced bottom section with Next button
 */
@Composable
fun EnhancedBottomSection(
    selectedOptions: Set<String>,
    isMultiSelect: Boolean,
    isSingleSelectAnswered: Boolean,
    accentColor: Color,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = if (isMultiSelect) selectedOptions.isNotEmpty() else isSingleSelectAnswered

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Multi-select counter
        if (isMultiSelect && selectedOptions.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .background(
                        color = accentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${selectedOptions.size} selected",
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp
                )
            }
        }

        // Next button - Only show for multi-select (last step)
        if (isMultiSelect) {
            Button(
                onClick = onNextClick,
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEnabled) Color.White else Color(0xFF9CA3AF)
                )
            }
        }
    }
}

/**
 * Main Enhanced Questionnaire Screen
 * 
 * Features:
 * - Gradient background
 * - Step counter and indicator dots
 * - Color-coded by step
 * - Smooth animations
 * - Next button with validation
 */
@Composable
fun EnhancedQuestionnaireScreen(
    screen: QuestionScreen,
    stepNumber: Int,
    totalSteps: Int,
    selectedOptions: Set<String>,
    onSelectionChange: (String) -> Unit,
    onNextClick: () -> Unit,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val accentColor = StepColors.getStepAccent(stepNumber - 1)
    val isSingleSelectAnswered = selectedOptions.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0F4FF),
                        Color(0xFFF5F1FF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
    ) {
        // Progress bar with accent color
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFFE0E7FF), RoundedCornerShape(3.dp))
                .clip(RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(accentColor, RoundedCornerShape(3.dp))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step indicator dots
        StepIndicatorDots(
            currentStep = stepNumber - 1,
            totalSteps = totalSteps,
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Question card with step info
        EnhancedQuestionCard(
            question = screen.question,
            subtitle = screen.subtitle,
            icon = screen.icon,
            stepNumber = stepNumber,
            totalSteps = totalSteps,
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Options list (scrollable)
        EnhancedOptionsList(
            options = screen.options,
            selectedOptions = selectedOptions,
            isMultiSelect = screen.isMultiSelect,
            accentColor = accentColor,
            onSelectionChange = onSelectionChange,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Bottom section with Next button
        EnhancedBottomSection(
            selectedOptions = selectedOptions,
            isMultiSelect = screen.isMultiSelect,
            isSingleSelectAnswered = isSingleSelectAnswered,
            accentColor = accentColor,
            onNextClick = onNextClick
        )
        }
    }
}
