package com.example.projectbmi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SummaryState(
    val aiSummary: String = "",
    val goal: String = "",
    val intensity: String = "",
    val daysAvailable: String = "",
    val experienceLevel: String = "",
    val focusArea: String = "",
    val isLoading: Boolean = false
)

// Color constants for better performance and reusability
private val GRADIENT_INDIGO = Color(0xFF667EEA)
private val GRADIENT_PURPLE = Color(0xFF764BA2)
private val WELLNESS_CARD_YELLOW = Color(0xFFFEF3C7)
private val ACTION_ITEMS_GREEN = Color(0xFFDCFCE7)
private val MOTIVATION_BLUE = Color(0xFFE0E7FF)
private val NEXT_STEPS_PURPLE = Color(0xFFE0D5FF)
private val PRIMARY_GOAL_GREEN = Color(0xFFECFDF5)
private val INTENSITY_YELLOW = Color(0xFFFEF08A)
private val DAYS_BLUE = Color(0xFFDBEAFE)
private val EXPERIENCE_PURPLE = Color(0xFFF3E8FF)
private val FOCUS_ORANGE = Color(0xFFFFEDD5)

@Composable
fun ModernSummaryScreen(
    summaryState: SummaryState,
    onGenerateClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Gradient header banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(GRADIENT_INDIGO, GRADIENT_PURPLE)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "AI Summary",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Your Wellness Plan Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(GRADIENT_INDIGO, GRADIENT_PURPLE)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            "Your Wellness Plan",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF667EEA),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            "AI-Personalized",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Wellness Summary
            if (summaryState.aiSummary.isNotEmpty()) {
                SummarySectionCard(
                    title = "Your Wellness Summary",
                    content = cleanText(extractSummary(summaryState.aiSummary)),
                    icon = Icons.Default.FavoriteBorder,
                    backgroundColor = WELLNESS_CARD_YELLOW,
                    iconColor = Color(0xFFD97706),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Key Action Items
                SummaryActionItemsCardRefined(
                    summary = summaryState.aiSummary,
                    backgroundColor = ACTION_ITEMS_GREEN,
                    iconColor = Color(0xFF059669),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Motivational Message
                SummaryMotivationalCardRefined(
                    summary = summaryState.aiSummary,
                    backgroundColor = MOTIVATION_BLUE,
                    iconColor = Color(0xFF4F46E5),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Next Steps
                SummaryNextStepsCardRefined(
                    summary = summaryState.aiSummary,
                    backgroundColor = NEXT_STEPS_PURPLE,
                    iconColor = Color(0xFF9333EA),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary Stats Section
            if (summaryState.goal.isNotEmpty() || summaryState.intensity.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stats Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(0xFFF3F4F6),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                "Your Profile",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF6366F1),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                "Personalized stats",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)

                    // Stats Grid
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Goal
                        if (summaryState.goal.isNotEmpty()) {
                            SummaryStatItem(
                                label = "Primary Goal",
                                value = summaryState.goal,
                                icon = Icons.Default.EmojiEvents,
                                backgroundColor = PRIMARY_GOAL_GREEN,
                                iconColor = Color(0xFF10B981)
                            )
                        }

                        // Intensity
                        if (summaryState.intensity.isNotEmpty()) {
                            SummaryStatItem(
                                label = "Workout Intensity",
                                value = summaryState.intensity,
                                icon = Icons.Default.Bolt,
                                backgroundColor = INTENSITY_YELLOW,
                                iconColor = Color(0xFFF59E0B)
                            )
                        }

                        // Days Available
                        if (summaryState.daysAvailable.isNotEmpty()) {
                            SummaryStatItem(
                                label = "Days Available",
                                value = summaryState.daysAvailable,
                                icon = Icons.Default.CalendarMonth,
                                backgroundColor = DAYS_BLUE,
                                iconColor = Color(0xFF3B82F6)
                            )
                        }

                        // Experience Level
                        if (summaryState.experienceLevel.isNotEmpty()) {
                            SummaryStatItem(
                                label = "Experience Level",
                                value = summaryState.experienceLevel,
                                icon = Icons.Default.School,
                                backgroundColor = EXPERIENCE_PURPLE,
                                iconColor = Color(0xFFA855F7)
                            )
                        }

                        // Focus Area
                        if (summaryState.focusArea.isNotEmpty()) {
                            SummaryStatItem(
                                label = "Focus Area",
                                value = summaryState.focusArea,
                                icon = Icons.Default.FitnessCenter,
                                backgroundColor = FOCUS_ORANGE,
                                iconColor = Color(0xFFEA580C)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Generate Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { if (!summaryState.isLoading) onGenerateClick() },
                    modifier = Modifier
                        .width(220.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667EEA)
                    ),
                    enabled = !summaryState.isLoading
                ) {
                    if (summaryState.isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text(
                                "Generating...",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Text(
                                "Generate Schedule",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// HELPER COMPOSABLES
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun SummarySectionCard(
    title: String,
    content: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937),
                fontSize = 14.sp
            )
        }
        Text(
            content,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF374151),
            lineHeight = 20.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
// REFINED HELPER COMPOSABLES - Enhanced with modern design
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun SummaryActionItemsCardRefined(
    summary: String,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val actionItems = remember(summary) { extractActionItems(summary) }
    
    if (actionItems.isEmpty()) return
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                "Key Action Items",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                fontSize = 14.sp
            )
        }
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            actionItems.take(4).forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Gradient badge for numbering
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(iconColor, iconColor.copy(alpha = 0.7f))
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (index + 1).toString(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        cleanText(item),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151),
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 2.dp),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMotivationalCardRefined(
    summary: String,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val motivational = remember(summary) { extractMotivational(summary) }
    
    if (motivational.isEmpty()) return
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                "Your Motivation",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                fontSize = 14.sp
            )
        }
        Text(
            cleanText(motivational),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF374151),
            lineHeight = 20.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
private fun SummaryNextStepsCardRefined(
    summary: String,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val nextSteps = remember(summary) { extractNextSteps(summary) }
    
    if (nextSteps.isEmpty()) return
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NavigateNext,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                "Next Steps",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                fontSize = 14.sp
            )
        }
        Text(
            cleanText(nextSteps),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF374151),
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun SummaryStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
                Text(
                    cleanText(value).replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937),
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// HELPER FUNCTIONS
// ═══════════════════════════════════════════════════════════════════════

private val DOUBLE_ASTERISK_REGEX = Regex("\\*\\*")
private val ASTERISK_REGEX = Regex("\\*")
private val NUMBER_PREFIX_REGEX = Regex("^[0-9]+\\.\\s*")
private val NUMBERED_ITEM_REGEX = Regex("^[0-9]+\\..*")

private fun cleanText(text: String): String {
    return text
        .replace(DOUBLE_ASTERISK_REGEX, "")
        .replace(ASTERISK_REGEX, "")
        .replace(NUMBER_PREFIX_REGEX, "")
        .trim()
}

private fun extractSummary(summary: String): String {
    val lines = summary.split("\n")
    val summaryStart = lines.indexOfFirst { it.contains("Personalized Wellness Summary", ignoreCase = true) || it.contains("Wellness Summary", ignoreCase = true) }
    val keyActionsStart = lines.indexOfFirst { it.contains("Key Action Items", ignoreCase = true) }
    
    return if (summaryStart != -1 && keyActionsStart != -1) {
        lines.subList(summaryStart + 1, keyActionsStart)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()
    } else {
        summary.split("\n").take(3).joinToString(" ").trim()
    }
}

private fun extractActionItems(summary: String): List<String> {
    val lines = summary.split("\n")
    val keyActionsStart = lines.indexOfFirst { it.contains("Key Action Items", ignoreCase = true) }
    val motivationStart = lines.indexOfFirst { it.contains("Motivational Message", ignoreCase = true) || it.contains("Motivation", ignoreCase = true) }
    
    return if (keyActionsStart != -1 && motivationStart != -1) {
        lines.subList(keyActionsStart + 1, motivationStart)
            .filter { it.trim().matches(NUMBERED_ITEM_REGEX) }
            .map { it.replaceFirst(NUMBER_PREFIX_REGEX, "").trim() }
    } else {
        emptyList()
    }
}

private fun extractMotivational(summary: String): String {
    val lines = summary.split("\n")
    val motivationStart = lines.indexOfFirst { it.contains("Motivational Message", ignoreCase = true) || it.contains("Your Motivation", ignoreCase = true) }
    val nextStepsStart = lines.indexOfFirst { it.contains("Next Steps", ignoreCase = true) }
    
    return if (motivationStart != -1) {
        val endIndex = if (nextStepsStart != -1) nextStepsStart else lines.size
        lines.subList(motivationStart + 1, endIndex)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()
    } else {
        ""
    }
}

private fun extractNextSteps(summary: String): String {
    val lines = summary.split("\n")
    val nextStepsStart = lines.indexOfFirst { it.contains("Next Steps", ignoreCase = true) }
    
    return if (nextStepsStart != -1) {
        lines.subList(nextStepsStart + 1, lines.size)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()
    } else {
        ""
    }
}
