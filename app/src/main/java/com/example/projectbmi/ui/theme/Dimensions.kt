package com.example.projectbmi.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Responsive dimension system for BMI Calculator app.
 * Adapts UI elements based on screen width breakpoints.
 *
 * Breakpoints:
 * - Compact: <360dp (small/old phones)
 * - Medium: 360-600dp (normal phones)
 * - Expanded: >600dp (tablets)
 *
 * Usage example:
 * ```
 * Card(
 *     modifier = Modifier
 *         .fillMaxWidth(Dimensions.cardWidth())
 *         .padding(Dimensions.cardPadding())
 * ) {
 *     Text(
 *         text = "BMI Calculator",
 *         fontSize = Dimensions.titleTextSize()
 *     )
 * }
 * ```
 */
object Dimensions {

    /**
     * Screen size categories based on width breakpoints
     */
    private enum class ScreenSize {
        Compact,    // <360dp
        Medium,     // 360-600dp
        Expanded    // >600dp
    }

    /**
     * Determines current screen size category
     */
    @Composable
    private fun getScreenSize(): ScreenSize {
        val screenWidthDp = LocalConfiguration.current.screenWidthDp
        return when {
            screenWidthDp < 360 -> ScreenSize.Compact
            screenWidthDp <= 600 -> ScreenSize.Medium
            else -> ScreenSize.Expanded
        }
    }

    /**
     * Returns the appropriate card width fraction based on screen size.
     *
     * - Compact: 0.95f (use most of screen width)
     * - Medium: 0.85f (standard padding)
     * - Expanded: 0.70f (prevent card from being too wide)
     *
     * Usage: `Modifier.fillMaxWidth(Dimensions.cardWidth())`
     */
    @Composable
    fun cardWidth(): Float = when (getScreenSize()) {
        ScreenSize.Compact -> 0.95f
        ScreenSize.Medium -> 0.85f
        ScreenSize.Expanded -> 0.70f
    }

    /**
     * Returns card corner radius based on screen size.
     *
     * - Compact: 16dp
     * - Medium: 20dp
     * - Expanded: 28dp
     */
    @Composable
    fun cardCornerRadius(): Dp = when (getScreenSize()) {
        ScreenSize.Compact -> 16.dp
        ScreenSize.Medium -> 20.dp
        ScreenSize.Expanded -> 28.dp
    }

    /**
     * Returns outer padding for cards and containers.
     *
     * - Compact: 12dp (minimal padding for small screens)
     * - Medium: 16dp (standard padding)
     * - Expanded: 24dp (comfortable padding for tablets)
     *
     * Usage: `Modifier.padding(Dimensions.cardPadding())`
     */
    @Composable
    fun cardPadding(): Dp = when (getScreenSize()) {
        ScreenSize.Compact -> 12.dp
        ScreenSize.Medium -> 16.dp
        ScreenSize.Expanded -> 24.dp
    }

    /**
     * Returns inner padding for card content.
     *
     * - Compact: 16dp
     * - Medium: 20dp
     * - Expanded: 28dp
     */
    @Composable
    fun contentPadding(): Dp = when (getScreenSize()) {
        ScreenSize.Compact -> 16.dp
        ScreenSize.Medium -> 20.dp
        ScreenSize.Expanded -> 28.dp
    }

    /**
     * Returns vertical spacing between UI elements.
     *
     * - Compact: 12dp (compact spacing)
     * - Medium: 16dp (standard spacing)
     * - Expanded: 20dp (comfortable spacing)
     *
     * Usage: `Modifier.height(Dimensions.verticalSpacing())`
     */
    @Composable
    fun verticalSpacing(): Dp = when (getScreenSize()) {
        ScreenSize.Compact -> 12.dp
        ScreenSize.Medium -> 16.dp
        ScreenSize.Expanded -> 20.dp
    }

    /**
     * Returns horizontal spacing/gaps between elements.
     *
     * - Compact: 8dp
     * - Medium: 12dp
     * - Expanded: 16dp
     */
    @Composable
    fun horizontalSpacing(): Dp = when (getScreenSize()) {
        ScreenSize.Compact -> 8.dp
        ScreenSize.Medium -> 12.dp
        ScreenSize.Expanded -> 16.dp
    }

    /**
     * Returns title text size.
     *
     * - Compact: 24sp (readable on small screens)
     * - Medium: 28sp (standard title size)
     * - Expanded: 32sp (large title for tablets)
     *
     * Usage: `fontSize = Dimensions.titleTextSize()`
     */
    @Composable
    fun titleTextSize(): TextUnit = when (getScreenSize()) {
        ScreenSize.Compact -> 24.sp
        ScreenSize.Medium -> 28.sp
        ScreenSize.Expanded -> 32.sp
    }

    /**
     * Returns body text size for regular content.
     *
     * - Compact: 14sp
     * - Medium: 16sp
     * - Expanded: 18sp
     */
    @Composable
    fun bodyTextSize(): TextUnit = when (getScreenSize()) {
        ScreenSize.Compact -> 14.sp
        ScreenSize.Medium -> 16.sp
        ScreenSize.Expanded -> 18.sp
    }

    /**
     * Returns button text size.
     *
     * - Compact: 14sp
     * - Medium: 16sp
     * - Expanded: 18sp
     */
    @Composable
    fun buttonTextSize(): TextUnit = when (getScreenSize()) {
        ScreenSize.Compact -> 14.sp
        ScreenSize.Medium -> 16.sp
        ScreenSize.Expanded -> 18.sp
    }

    /**
     * Returns button height for consistent touch targets.
     *
     * - Compact: 48dp (minimum touch target)
     * - Medium: 54dp (comfortable touch target)
     * - Expanded: 60dp (large touch target for tablets)
     *
     * Usage: `Modifier.height(Dimensions.buttonHeight())`
     */
    @Composable
    fun buttonHeight(): Dp = when (getScreenSize()) {
        ScreenSize.Compact -> 48.dp
        ScreenSize.Medium -> 54.dp
        ScreenSize.Expanded -> 60.dp
    }

    /**
     * Returns icon size for consistent visual hierarchy.
     *
     * - Compact: 48dp
     * - Medium: 60dp
     * - Expanded: 72dp
     */
    @Composable
    fun iconSize(): Dp = when (getScreenSize()) {
        ScreenSize.Compact -> 48.dp
        ScreenSize.Medium -> 60.dp
        ScreenSize.Expanded -> 72.dp
    }

    /**
     * Returns large text size for numeric displays (BMI result, age counter, etc.)
     *
     * - Compact: 48sp
     * - Medium: 56sp
     * - Expanded: 64sp
     */
    @Composable
    fun largeTextSize(): TextUnit = when (getScreenSize()) {
        ScreenSize.Compact -> 48.sp
        ScreenSize.Medium -> 56.sp
        ScreenSize.Expanded -> 64.sp
    }

    /**
     * Returns spacing for gender/option selection buttons
     *
     * - Compact: 12dp
     * - Medium: 16dp
     * - Expanded: 20dp
     */
    @Composable
    fun selectionSpacing(): Dp = when (getScreenSize()) {
        ScreenSize.Compact -> 12.dp
        ScreenSize.Medium -> 16.dp
        ScreenSize.Expanded -> 20.dp
    }
}
