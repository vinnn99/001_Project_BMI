package com.example.projectbmi.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectbmi.model.IntensityRecommendation
import com.example.projectbmi.model.IntensityConfirmation

/**
 * Beautiful dialog for confirming intensity when recommendation differs from user choice
 * Shows warning with age-based recommendation and gives user 2 options
 */
@Composable
fun IntensityRecommendationDialog(
    recommendation: IntensityRecommendation,
    onConfirm: (IntensityConfirmation) -> Unit,
    onDismiss: () -> Unit = {}
) {
    if (!recommendation.shouldShowDialog) {
        return  // Don't show dialog if no recommendation needed
    }

    var selectedOption by remember { mutableStateOf<String?>(null) }
    
    // Scrim (dark background)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.32f))
            .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        // Dialog content
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(20.dp))
                .clickable(false) { },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ============ Header with Icon ============
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFF57C00),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ============ Title ============
                Text(
                    text = "Intensity Check",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ============ Age & Status Info ============
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Age: ${recommendation.userAge} years old",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Your Choice
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Your Choice",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = recommendation.userSelected.uppercase(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text("→", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                            // Recommendation
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Recommended",
                                    fontSize = 11.sp,
                                    color = Color(0xFFF57C00)
                                )
                                Text(
                                    text = recommendation.recommended.uppercase(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF57C00)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ============ Warning Message ============
                Text(
                    text = recommendation.warningMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ============ Explanation ============
                if (recommendation.explanation.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        color = Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text = recommendation.explanation,
                            fontSize = 12.sp,
                            color = Color(0xFF5D4037),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ============ Option 1: Accept Recommendation ============
                IntensityOptionButton(
                    label = "Accept Recommended (${recommendation.recommended.uppercase()})",
                    isSelected = selectedOption == "accept",
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = Color(0xFFF57C00).copy(alpha = 0.1f),
                    borderColor = Color(0xFFF57C00),
                    textColor = Color(0xFFF57C00),
                    onClick = { selectedOption = "accept" },
                    sublabel = "Better for your age group"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ============ Option 2: Keep Selection ============
                IntensityOptionButton(
                    label = "Keep My Choice (${recommendation.userSelected.uppercase()})",
                    isSelected = selectedOption == "keep",
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    borderColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.primary,
                    onClick = { selectedOption = "keep" },
                    sublabel = "I know my limits"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ============ Action Buttons ============
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    // Confirm Button
                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                val finalIntensity = when (selectedOption) {
                                    "accept" -> recommendation.recommended
                                    "keep" -> recommendation.userSelected
                                    else -> recommendation.userSelected
                                }
                                val wasRecommendationAccepted = selectedOption == "accept"
                                
                                onConfirm(IntensityConfirmation(
                                    finalIntensity = finalIntensity,
                                    wasRecommendationAccepted = wasRecommendationAccepted
                                ))
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = selectedOption != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedOption != null) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            "Continue",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable button component for selecting intensity option
 */
@Composable
private fun IntensityOptionButton(
    label: String,
    sublabel: String,
    isSelected: Boolean,
    icon: ImageVector,
    backgroundColor: Color,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val scale by animateDpAsState(
        targetValue = if (isSelected) 1.02.dp else 1.dp,
        animationSpec = tween(200)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .scale(1f + (scale.value - 1f) / 100),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) borderColor else borderColor.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = sublabel,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }

            if (isSelected) {
                Surface(
                    modifier = Modifier
                        .size(20.dp)
                        .background(borderColor, shape = RoundedCornerShape(50.dp)),
                    color = borderColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✓", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
