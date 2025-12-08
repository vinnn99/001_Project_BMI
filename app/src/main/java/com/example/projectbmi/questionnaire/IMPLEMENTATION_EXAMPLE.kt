package com.example.projectbmi.questionnaire

/**
 * COMPLETE EXAMPLE: Using EnhancedQuestionnaireScreen in your existing code
 * 
 * This example shows the EXACT way to integrate the enhanced components
 * into your AskAIScreenClean composable.
 */

/*
═══════════════════════════════════════════════════════════════════════════════
STEP-BY-STEP IMPLEMENTATION
═══════════════════════════════════════════════════════════════════════════════

1. ADD IMPORT to AskAIScreenClean.kt:

    import com.example.projectbmi.questionnaire.EnhancedQuestionnaireScreen
    import com.example.projectbmi.questionnaire.StepColors

2. REPLACE this section in AskAIScreenClean:

    OLD CODE (existing questionnaire rendering):
    ─────────────────────────────────────────────
    when {
        generated != null -> {
            // Preview and save...
        }
        state.currentStep in 1..5 -> {
            val screenIndex = state.currentStep - 1
            var currentScreen = QuestionnaireData.screens[screenIndex]
            
            if (state.currentStep == 1) {
                val filteredGoalOptions = QuestionnaireData.getFilteredGoalOptions(state.bmiCategory)
                currentScreen = currentScreen.copy(options = filteredGoalOptions)
            }
            
            val selectedOptions = when (state.currentStep) {
                1 -> state.primaryGoal?.let { setOf(it) } ?: emptySet()
                2 -> state.exerciseIntensity?.let { setOf(it) } ?: emptySet()
                3 -> state.daysAvailable?.let { setOf(it) } ?: emptySet()
                4 -> state.experienceLevel?.let { setOf(it) } ?: emptySet()
                5 -> state.focusArea?.let { setOf(it) } ?: emptySet()
                else -> emptySet()
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                QuestionnaireScreen(  // OLD COMPONENT
                    screen = currentScreen,
                    selectedOptions = selectedOptions,
                    onSelectionChange = { value ->
                        // Old implementation...
                    },
                    progress = state.currentStep / 5f
                )
                
                if (currentScreen.isMultiSelect) {
                    FloatingActionButton(/* ... */) { /* ... */ }
                }
            }
        }
    }

    NEW CODE (with enhanced questionnaire):
    ─────────────────────────────────────────────
    when {
        generated != null -> {
            // Preview and save...
        }
        state.currentStep in 1..5 -> {
            val screenIndex = state.currentStep - 1
            var currentScreen = QuestionnaireData.screens[screenIndex]
            
            // Filter options for step 1 based on BMI category
            if (state.currentStep == 1) {
                val filteredGoalOptions = QuestionnaireData.getFilteredGoalOptions(state.bmiCategory)
                currentScreen = currentScreen.copy(options = filteredGoalOptions)
            }
            
            // Build selected options set based on current step
            val selectedOptions = when (state.currentStep) {
                1 -> state.primaryGoal?.let { setOf(it) } ?: emptySet()
                2 -> state.exerciseIntensity?.let { setOf(it) } ?: emptySet()
                3 -> state.daysAvailable?.let { setOf(it) } ?: emptySet()
                4 -> state.experienceLevel?.let { setOf(it) } ?: emptySet()
                5 -> state.focusArea?.let { setOf(it) } ?: emptySet()
                else -> emptySet()
            }
            
            // ===== USE ENHANCED COMPONENT =====
            EnhancedQuestionnaireScreen(
                screen = currentScreen,
                stepNumber = state.currentStep,      // Current step (1-5)
                totalSteps = 5,                       // Always 5 for this questionnaire
                selectedOptions = selectedOptions,   // Set of selected values
                onSelectionChange = { value ->
                    // Update state based on current step
                    state = when (state.currentStep) {
                        1 -> state.copy(primaryGoal = value)
                        2 -> state.copy(exerciseIntensity = value)
                        3 -> state.copy(daysAvailable = value)
                        4 -> state.copy(experienceLevel = value)
                        5 -> state.copy(focusArea = value)
                        else -> state
                    }
                },
                onNextClick = {
                    // Advance to next step
                    if (state.currentStep < 5) {
                        state = state.copy(currentStep = state.currentStep + 1)
                    } else {
                        // Last step - show AI tips/chat
                        state = state.copy(currentStep = 6)
                    }
                },
                progress = state.currentStep / 5f
            )
        }
    }

═══════════════════════════════════════════════════════════════════════════════
VISUAL IMPROVEMENTS YOU GET:
═══════════════════════════════════════════════════════════════════════════════

✓ Beautiful gradient background (light blue → light purple)
✓ Step-specific color coding:
  - Step 1: Blue (#3B82F6)
  - Step 2: Orange (#F97316)
  - Step 3: Green (#10B981)
  - Step 4: Purple (#8B5CF6)
  - Step 5: Red (#EF4444)

✓ Step indicator dots that animate
✓ Step counter badge in question card
✓ Smooth animations:
  - Card scale (1.0 → 1.02)
  - Elevation (2dp → 12dp)
  - Color transitions
  - Checkmark animation
  
✓ Larger icons and better spacing
✓ Clear "Next" button at bottom
✓ Proper validation (button disabled until selection made)
✓ Multi-select counter

═══════════════════════════════════════════════════════════════════════════════
PROPERTIES EXPLANATION
═══════════════════════════════════════════════════════════════════════════════

stepNumber: Int
  Current step in the questionnaire (1, 2, 3, 4, or 5)
  Used to: Pick color, show step counter, calculate progress

totalSteps: Int
  Total number of steps (always 5 for this questionnaire)
  Used to: Calculate progress bar fill, show step dots

selectedOptions: Set<String>
  Set of selected option values for current step
  Example:
    - Step 1: setOf("weight-loss") or emptySet()
    - Step 5: setOf("cardio") or emptySet()
  For single-select: Usually 0 or 1 item
  For multi-select: Can be multiple items

onSelectionChange: (String) -> Unit
  Callback when user taps an option
  Parameter: value = option.value (e.g., "weight-loss", "cardio")
  Called when: User taps any option card

onNextClick: () -> Unit
  Callback when user clicks the Next button
  Called when: Next button is tapped (only if option selected)
  Used to: Advance to next step or finish questionnaire

progress: Float
  Progress bar fill amount (0.0 to 1.0)
  Calculate: stepNumber / totalSteps
  Example:
    - Step 1: 1/5 = 0.2f
    - Step 3: 3/5 = 0.6f
    - Step 5: 5/5 = 1.0f

═══════════════════════════════════════════════════════════════════════════════
OPTIONAL: REMOVE OLD QUESTIONNAIRE COMPONENTS
═══════════════════════════════════════════════════════════════════════════════

If you want to clean up, you can delete or comment out the old components
in QuestionnaireComponents.kt since they're replaced by:

OLD COMPONENTS TO REMOVE:
  - IconCircle() → replaced by EnhancedIconCircle()
  - SelectionIndicator() → replaced by EnhancedSelectionIndicator()
  - OptionCard() → replaced by EnhancedOptionCard()
  - QuestionCard() → replaced by EnhancedQuestionCard()
  - OptionsList() → replaced by EnhancedOptionsList()
  - BottomSection() → replaced by EnhancedBottomSection()
  - QuestionnaireScreen() → replaced by EnhancedQuestionnaireScreen()

The models in QuestionnaireModels.kt should stay as-is since they're shared.

═══════════════════════════════════════════════════════════════════════════════
TESTING
═══════════════════════════════════════════════════════════════════════════════

After implementing:

1. Build the project: .\gradlew.bat assembleDebug
2. Run on emulator/device
3. Navigate to Ask AI section
4. Verify:
   ✓ Background has gradient
   ✓ Step dots appear and animate
   ✓ Cards have rounded corners and shadows
   ✓ Options scale up when selected
   ✓ Checkmark appears with animation
   ✓ Step counter shows "Step 1/5", "Step 2/5", etc.
   ✓ Color changes per step
   ✓ Next button is disabled until option selected
   ✓ Next button enables when option selected
   ✓ Progress bar fills as you advance

═══════════════════════════════════════════════════════════════════════════════
COMMON ISSUES & FIXES
═══════════════════════════════════════════════════════════════════════════════

Issue: Build fails with "Unresolved reference"
Fix: Add imports at top of file:
  import com.example.projectbmi.questionnaire.EnhancedQuestionnaireScreen
  import com.example.projectbmi.questionnaire.StepColors

Issue: Buttons not appearing
Fix: Make sure onNextClick callback is properly defined and updates state

Issue: Colors not changing per step
Fix: Verify stepNumber is being passed correctly (1-5, not 0-4)

Issue: Animations not smooth
Fix: Check device performance, try on different device/emulator

═══════════════════════════════════════════════════════════════════════════════
CUSTOMIZATION EXAMPLES
═══════════════════════════════════════════════════════════════════════════════

Change step colors:
  In QuestionnaireComponentsEnhanced.kt, modify StepColors.getStepAccent():
  
  fun getStepAccent(stepIndex: Int): Color = when (stepIndex) {
      0 -> Color(0xFF00FF00)  // Change to green
      1 -> Color(0xFFFF0000)  // Change to red
      // etc...
  }

Change animation speed:
  Find any animateColorAsState() call, change:
    animationSpec = tween(durationMillis = 300)  // Change 300 to your value
  Smaller = faster, larger = slower

Change card border radius:
  Look for RoundedCornerShape(16.dp), change 16.dp to your value
  Smaller = sharper corners, larger = more rounded

═══════════════════════════════════════════════════════════════════════════════
*/
