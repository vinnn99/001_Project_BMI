package com.example.projectbmi.questionnaire

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * ATOMIC COMPONENTS - Single Responsibility
 */

/**
 * Circular icon container with colored background.
 * Used in question cards to display the question type icon.
 */
@Composable
fun IconCircle(
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(color.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Selection indicator for options (radio button or checkbox).
 * Adapts appearance based on selection type.
 */
@Composable
fun SelectionIndicator(
    isSelected: Boolean,
    isMultiSelect: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = if (isMultiSelect) RoundedCornerShape(6.dp) else CircleShape
    val primaryColor = Color(0xFF6366F1)
    
    Box(
        modifier = modifier
            .size(24.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) primaryColor else Color(0xFFD1D5DB),
                shape = shape
            )
            .background(
                color = if (isSelected && isMultiSelect) primaryColor else Color.Transparent,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            isSelected && isMultiSelect -> Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            isSelected && !isMultiSelect -> Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(primaryColor, CircleShape)
            )
        }
    }
}

/**
 * Animated option card with icon, text, and selection indicator.
 * Supports both single-select (radio) and multi-select (checkbox) modes.
 */
@Composable
fun OptionCard(
    option: QuestionOption,
    isSelected: Boolean,
    isMultiSelect: Boolean,
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
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = tween(durationMillis = 300),
        label = "elevation"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFEDE7FF) else Color.White,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = onClick,
                indication = androidx.compose.material3.ripple(color = Color(0xFF6366F1)),
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionIndicator(
                isSelected = isSelected,
                isMultiSelect = isMultiSelect
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF6366F1) else Color(0xFF6B7280),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = option.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF1F2937) else Color(0xFF4B5563)
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * COMPOSITE COMPONENTS - Reusable Sections
 */

/**
 * Question card header with icon, title, and subtitle.
 * Provides context for the current question.
 */
@Composable
fun QuestionCard(
    question: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCircle(
                icon = icon,
                color = Color(0xFF6366F1)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

/**
 * Scrollable list of option cards.
 * Handles both single-select and multi-select modes.
 */
@Composable
fun OptionsList(
    options: List<QuestionOption>,
    selectedOptions: Set<String>,
    isMultiSelect: Boolean,
    onSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            OptionCard(
                option = option,
                isSelected = option.value in selectedOptions,
                isMultiSelect = isMultiSelect,
                onClick = { onSelectionChange(option.value) }
            )
        }
    }
}

/**
 * Bottom section - Only shows selection counter for multi-select.
 * No Next button needed - uses auto-advance pattern.
 */
@Composable
fun BottomSection(
    selectedCount: Int,
    isMultiSelect: Boolean,
    modifier: Modifier = Modifier
) {
    if (isMultiSelect && selectedCount > 0) {
        Text(
            text = "$selectedCount selected",
            color = Color(0xFF6366F1),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.padding(16.dp)
        )
    }
}

/**
 * MAIN REUSABLE COMPONENT
 * 
 * Complete questionnaire screen with auto-advance pattern.
 * - Single-select: Auto-advances after selection (800ms delay)
 * - Multi-select: Shows counter, user manually proceeds
 * 
 * @param screen Question data (question, options, icons, type)
 * @param selectedOptions Currently selected option values
 * @param onSelectionChange Handler for option selection/deselection
 * @param progress Progress value (0.0 to 1.0)
 */
@Composable
fun QuestionnaireScreen(
    screen: QuestionScreen,
    selectedOptions: Set<String>,
    onSelectionChange: (String) -> Unit,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF6366F1),
            trackColor = Color(0xFFE0E7FF)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Question header
        QuestionCard(
            question = screen.question,
            subtitle = screen.subtitle,
            icon = screen.icon
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Options list (scrollable)
        OptionsList(
            options = screen.options,
            selectedOptions = selectedOptions,
            isMultiSelect = screen.isMultiSelect,
            onSelectionChange = onSelectionChange,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bottom section (only counter for multi-select)
        BottomSection(
            selectedCount = selectedOptions.size,
            isMultiSelect = screen.isMultiSelect
        )
    }
}
