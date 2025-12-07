package com.example.projectbmi

import com.example.projectbmi.model.BMIRecord
import com.example.projectbmi.model.QuestTask
import com.example.projectbmi.model.DailyQuestParams
import java.util.UUID

/**
 * Simple on-device rule-based tips generator used as a stub for AI integration.
 * Keeps everything local and private; later can be replaced by a cloud LLM call.
 */
object AIRepository {
    // Track last used pool index to ensure variety in regenerations
    var lastGeneratedPoolIndex: Int = -1
        private set
    fun generateQuickTips(bmi: Float, category: String, gender: String, age: Int = 0, history: List<BMIRecord> = emptyList()): List<String> {
        val tips = mutableListOf<String>()
        if (category.lowercase().contains("normal")) {
            tips.add("Great job — maintain a balanced diet and regular exercise 3x/week.")
            tips.add("Include strength training 2x/week to preserve muscle mass.")
        } else if (category.lowercase().contains("underweight") || category.lowercase().contains("kurus")) {
            tips.add("Consider small frequent meals and add healthy calorie-dense foods (nuts, avocado).")
            tips.add("Consult a nutritionist if unintended weight loss continues.")
        } else if (category.lowercase().contains("overweight") || category.lowercase().contains("gemuk")) {
            tips.add("Start with daily 20–30 minutes of brisk walking and reduce sugary drinks.")
            tips.add("Replace one high-calorie snack with fruit or yogurt.")
        } else {
            tips.add("Aim for a mix of cardio and strength training and watch portion sizes.")
            tips.add("Track food intake for a week to find easy places to reduce calories.")
        }

        // small personalization
        if (age in 60..150) {
            tips.add("For older adults, prioritize balance exercises and low-impact cardio.")
        }
        if (gender.lowercase().contains("f")) {
            tips.add("Ensure adequate calcium and vitamin D intake for bone health.")
        }

        return tips
    }

    /**
     * Generate a simple 7-day schedule (one short task per day) tailored to the BMI category and gender.
     * This is intentionally lightweight and deterministic so it can run locally without network.
     */
    fun generateWeeklySchedule(bmi: Float, category: String, gender: String, age: Int = 0): List<String> {
        val lower = category.lowercase()
        val baseTasks = when {
            lower.contains("underweight") || lower.contains("kurus") -> listOf(
                "Eat 3 balanced meals + 2 nutritious snacks",
                "Add a protein- and calorie-dense smoothie",
                "Strength training: 20–30 min (bodyweight)",
                "Include healthy fats (nuts, avocado) in meals",
                "Try a calorie-dense breakfast (eggs, oats)",
                "Light walk + focus on portion size increases",
                "Prepare meal plan with extra calories"
            )
            lower.contains("overweight") || lower.contains("gemuk") -> listOf(
                "30 min brisk walking or cycling",
                "Swap soda for water or unsweetened tea",
                "Include a vegetable-rich dinner",
                "Strength training: 20–30 min (light weights)",
                "Limit sugary snacks today",
                "Try a guided home cardio session (20 min)",
                "Plan healthy meals for the week"
            )
            lower.contains("obese") -> listOf(
                "20 min low-impact walking or pool exercise",
                "Choose a high-protein breakfast",
                "Replace one meal with a large salad",
                "Short resistance band session: 15–20 min",
                "Avoid sugary drinks today",
                "Practice mindful eating at each meal",
                "Schedule a short consult/check-in with a provider"
            )
            else -> listOf(
                "30 min activity (brisk walk)",
                "Maintain balanced meals",
                "Strength training: 2 sets of basic moves",
                "Hydration focus: 8 glasses",
                "Try a new healthy recipe",
                "Track food intake for today",
                "Rest and light mobility work"
            )
        }

        // Small personalization tweaks
        val genderHint = if (gender.lowercase().startsWith("f")) " (prioritize bone health)" else ""

        return baseTasks.mapIndexed { idx, task ->
            // Add gentle personalization without exposing PII
            if (idx == 0) "$task$genderHint" else task
        }
    }

    /**
     * Adjust schedule based on user preferences (time available, exercise history, focus area).
     * This provides personalization beyond just goal + intensity.
     * Applies smart scheduling that works from any day of the week.
     */
    private fun adjustSchedule(baseSchedule: List<QuestTask>, params: DailyQuestParams): List<QuestTask> {
        var adjusted = baseSchedule.toList()
        
        // 1. Adjust based on days per week (how many days user wants to workout)
        // Use smart filtering that works regardless of which day user fills the questionnaire
        adjusted = when {
            params.daysPerWeek <= 4 -> {
                // 3-4 days: Distribute workouts evenly across week
                // Select every other day starting from Monday: Mon, Wed, Fri, Sat
                val workoutDays = listOf("Monday", "Wednesday", "Friday", "Saturday")
                adjusted.filter { it.day in workoutDays }
            }
            params.daysPerWeek <= 6 -> {
                // 5-6 days: Keep all days except 1 (usually Sunday is lighter/rest)
                // Filter to get 6 consecutive days starting from Monday through Saturday
                adjusted.filterIndexed { idx, _ -> idx < 6 }
            }
            else -> adjusted // 7 days: keep all days
        }
        
        // 2. Adjust duration based on exercise history (experience level)
        adjusted = adjusted.map { task ->
            val durationMultiplier = when (params.exerciseHistory) {
                "beginner" -> 0.8  // Reduce duration by 20%
                "intermediate" -> 1.0  // Keep same
                "advanced" -> 1.2  // Increase by 20%
                else -> 1.0
            }
            val newDuration = (task.duration * durationMultiplier).toInt().coerceAtLeast(10)
            task.copy(duration = newDuration)
        }
        
        // 3. Prioritize focus area (boost matching category tasks)
        adjusted = adjusted.map { task ->
            val shouldBoost = when (params.focusArea.lowercase()) {
                "cardio" -> task.category == "cardio"
                "strength" -> task.category == "strength"
                "flexibility" -> task.category == "flexibility"
                "mixed" -> task.category == "mixed"
                else -> false
            }
            
            if (shouldBoost && task.category != "nutrition" && task.category != "recovery") {
                // Boost duration by 15% for focused category
                val boostedDuration = (task.duration * 1.15).toInt()
                task.copy(duration = boostedDuration)
            } else {
                task
            }
        }
        
        return adjusted
    }

    /**
     * Generate a personalized 7-day weekly schedule based on detailed user discovery profile.
     * Returns a list of QuestTask objects with day, task, duration, intensity, and notes.
     * @param excludePoolIndices List of pool indices to avoid for ensuring variety across regenerations
     */
    fun generateWeeklySchedule(params: DailyQuestParams, excludePoolIndices: List<Int> = emptyList()): List<QuestTask> {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val schedule = mutableListOf<QuestTask>()
        
        // Use multiple entropy sources for unique random each call
        // UUID.randomUUID() guarantees uniqueness even on very fast consecutive calls
        val uuidLong = UUID.randomUUID().mostSignificantBits.toLong()
        val seed = (System.nanoTime() + System.currentTimeMillis() + Math.random() * 1000000 + uuidLong).toLong()
        val random = kotlin.random.Random(seed)
        android.util.Log.d("AIRepository", "generateWeeklySchedule called with seed: $seed (UUID: $uuidLong)")

        // Define multiple task pool options for each category to enable randomization
        val taskPools = when {
            // Weight Loss Focus
            params.goal == "weight-loss" && params.intensity == "low" -> listOf(
                // Pool 1
                listOf(
                    QuestTask("Monday", "20 min brisk walk", "cardio", 20, "low", "Burn ~100 cal"),
                    QuestTask("Tuesday", "Track meals + water intake", "nutrition", 15, "low", "Use app to log food"),
                    QuestTask("Wednesday", "15 min home stretching", "flexibility", 15, "low", "Improve mobility"),
                    QuestTask("Thursday", "20 min light cycling", "cardio", 20, "low", "Steady pace"),
                    QuestTask("Friday", "Meal prep for weekend", "nutrition", 30, "low", "Prepare healthy portions"),
                    QuestTask("Saturday", "30 min leisurely walk", "cardio", 30, "low", "Outdoor activity"),
                    QuestTask("Sunday", "Rest + hydration focus", "recovery", 10, "low", "Drink 2L water")
                ),
                // Pool 2 (alternative)
                listOf(
                    QuestTask("Monday", "25 min nature walk", "cardio", 25, "low", "Enjoy outdoors"),
                    QuestTask("Tuesday", "Meal planning session", "nutrition", 20, "low", "Plan healthy meals"),
                    QuestTask("Wednesday", "15 min yoga stretching", "flexibility", 15, "low", "Relax & stretch"),
                    QuestTask("Thursday", "20 min swimming", "cardio", 20, "low", "Low-impact cardio"),
                    QuestTask("Friday", "Prep healthy snacks", "nutrition", 25, "low", "Make protein snacks"),
                    QuestTask("Saturday", "35 min park walk", "cardio", 35, "low", "Increase duration"),
                    QuestTask("Sunday", "Recovery + reflection", "recovery", 10, "low", "Rest day")
                ),
                // Pool 4 (cycling focused)
                listOf(
                    QuestTask("Monday", "22 min easy cycling", "cardio", 22, "low", "Low resistance"),
                    QuestTask("Tuesday", "Calorie tracking", "nutrition", 15, "low", "Log all meals"),
                    QuestTask("Wednesday", "18 min water aerobics", "cardio", 18, "low", "Joint-friendly"),
                    QuestTask("Thursday", "12 min core exercises", "strength", 12, "low", "Gentle abs"),
                    QuestTask("Friday", "Healthy grocery shopping", "nutrition", 25, "low", "Buy fresh produce"),
                    QuestTask("Saturday", "30 min casual bike ride", "cardio", 30, "low", "Explore neighborhood"),
                    QuestTask("Sunday", "Meditation + rest", "recovery", 10, "low", "Mental health day")
                ),
                // Pool 5 (home workout focused)
                listOf(
                    QuestTask("Monday", "20 min walking workout video", "cardio", 20, "low", "Follow along"),
                    QuestTask("Tuesday", "Portion control practice", "nutrition", 15, "low", "Measure food"),
                    QuestTask("Wednesday", "15 min chair exercises", "mixed", 15, "low", "Low impact strength"),
                    QuestTask("Thursday", "25 min slow dancing", "cardio", 25, "low", "Fun movement"),
                    QuestTask("Friday", "Meal prep containers", "nutrition", 20, "low", "Organize portions"),
                    QuestTask("Saturday", "35 min mall walking", "cardio", 35, "low", "Social activity"),
                    QuestTask("Sunday", "Stretching + planning", "recovery", 15, "low", "Week prep")
                )
            )
            params.goal == "weight-loss" && params.intensity == "medium" -> listOf(
                // Pool 1
                listOf(
                    QuestTask("Monday", "30 min cardio (walk/jog)", "cardio", 30, "medium", "Burn ~300 cal"),
                    QuestTask("Tuesday", "Track meals + strength 20 min", "mixed", 40, "medium", "Light weights"),
                    QuestTask("Wednesday", "30 min cardio session", "cardio", 30, "medium", "Steady-state"),
                    QuestTask("Thursday", "Strength training 25 min", "strength", 25, "medium", "Upper body focus"),
                    QuestTask("Friday", "Meal prep + light cardio", "mixed", 45, "medium", "30 min prep + 15 min walk"),
                    QuestTask("Saturday", "45 min mixed activity", "mixed", 45, "medium", "Cardio + flexibility"),
                    QuestTask("Sunday", "20 min stretching + nutrition planning", "recovery", 20, "low", "Plan next week")
                ),
                // Pool 2 (alternative)
                listOf(
                    QuestTask("Monday", "35 min running", "cardio", 35, "medium", "Steady pace"),
                    QuestTask("Tuesday", "25 min strength training", "strength", 25, "medium", "Bodyweight"),
                    QuestTask("Wednesday", "30 min cycling", "cardio", 30, "medium", "Moderate intensity"),
                    QuestTask("Thursday", "20 min HIIT lite", "cardio", 20, "medium", "Short bursts"),
                    QuestTask("Friday", "Meal prep + tracking", "nutrition", 35, "medium", "Organize meals"),
                    QuestTask("Saturday", "40 min dance or sports", "mixed", 40, "medium", "Fun activity"),
                    QuestTask("Sunday", "Stretching + meal planning", "recovery", 20, "low", "Relax")
                ),
                // Pool 4 (swimming focused)
                listOf(
                    QuestTask("Monday", "35 min swimming laps", "cardio", 35, "medium", "Full body burn"),
                    QuestTask("Tuesday", "30 min kettlebell workout", "strength", 30, "medium", "Functional strength"),
                    QuestTask("Wednesday", "30 min rowing machine", "cardio", 30, "medium", "Upper body cardio"),
                    QuestTask("Thursday", "25 min resistance bands", "strength", 25, "medium", "Full body toning"),
                    QuestTask("Friday", "Nutrition journaling", "nutrition", 20, "low", "Track macros"),
                    QuestTask("Saturday", "45 min hiking", "cardio", 45, "medium", "Nature workout"),
                    QuestTask("Sunday", "Yoga flow + rest", "recovery", 25, "low", "Active recovery")
                ),
                // Pool 5 (sports focused)
                listOf(
                    QuestTask("Monday", "40 min basketball/soccer", "cardio", 40, "medium", "Team sports"),
                    QuestTask("Tuesday", "25 min push/pull workout", "strength", 25, "medium", "Upper body"),
                    QuestTask("Wednesday", "35 min jump rope intervals", "cardio", 35, "medium", "High calorie burn"),
                    QuestTask("Thursday", "30 min leg workout", "strength", 30, "medium", "Lower body focus"),
                    QuestTask("Friday", "Meal prep batch cooking", "nutrition", 40, "low", "Week preparation"),
                    QuestTask("Saturday", "50 min tennis/badminton", "cardio", 50, "medium", "Fun cardio"),
                    QuestTask("Sunday", "Foam rolling + rest", "recovery", 20, "low", "Muscle recovery")
                )
            )
            params.goal == "weight-loss" && params.intensity == "high" -> listOf(
                // Pool 1
                listOf(
                    QuestTask("Monday", "45 min HIIT cardio", "cardio", 45, "high", "Intervals + recovery"),
                    QuestTask("Tuesday", "40 min strength training", "strength", 40, "high", "Full body circuit"),
                    QuestTask("Wednesday", "45 min cardio (run/bike)", "cardio", 45, "high", "High intensity"),
                    QuestTask("Thursday", "40 min strength + core", "strength", 40, "high", "Weights + abs"),
                    QuestTask("Friday", "Meal prep + 20 min cardio", "mixed", 50, "high", "Prep + light activity"),
                    QuestTask("Saturday", "60 min mixed workout", "mixed", 60, "high", "Cardio + strength"),
                    QuestTask("Sunday", "Active recovery + meal plan", "recovery", 30, "low", "Yoga + planning")
                ),
                // Pool 2 (alternative)
                listOf(
                    QuestTask("Monday", "50 min run", "cardio", 50, "high", "Hard effort"),
                    QuestTask("Tuesday", "45 min weight training", "strength", 45, "high", "Heavy compounds"),
                    QuestTask("Wednesday", "40 min circuit training", "mixed", 40, "high", "Full body burn"),
                    QuestTask("Thursday", "45 min strength + cardio", "mixed", 45, "high", "Combo session"),
                    QuestTask("Friday", "30 min intense cardio", "cardio", 30, "high", "Max effort"),
                    QuestTask("Saturday", "60 min full workout", "mixed", 60, "high", "Complete routine"),
                    QuestTask("Sunday", "Active recovery", "recovery", 30, "low", "Easy walk")
                ),
                // Pool 4 (CrossFit style)
                listOf(
                    QuestTask("Monday", "50 min CrossFit WOD", "mixed", 50, "high", "AMRAP workout"),
                    QuestTask("Tuesday", "45 min Olympic lifts", "strength", 45, "high", "Power training"),
                    QuestTask("Wednesday", "45 min sprints + burpees", "cardio", 45, "high", "Max intensity"),
                    QuestTask("Thursday", "50 min pull/push workout", "strength", 50, "high", "Compound movements"),
                    QuestTask("Friday", "Carb cycling nutrition", "nutrition", 25, "low", "High/low carb days"),
                    QuestTask("Saturday", "65 min boot camp", "mixed", 65, "high", "Military style"),
                    QuestTask("Sunday", "Mobility work", "recovery", 30, "low", "Stretch + foam roll")
                ),
                // Pool 5 (HIIT focused)
                listOf(
                    QuestTask("Monday", "40 min Tabata training", "cardio", 40, "high", "20s on/10s off"),
                    QuestTask("Tuesday", "50 min strength circuit", "strength", 50, "high", "No rest between sets"),
                    QuestTask("Wednesday", "45 min spin class", "cardio", 45, "high", "High resistance"),
                    QuestTask("Thursday", "45 min bodyweight HIIT", "mixed", 45, "high", "Intense calisthenics"),
                    QuestTask("Friday", "Pre/post workout nutrition", "nutrition", 20, "low", "Timing optimization"),
                    QuestTask("Saturday", "60 min battle ropes + box jumps", "mixed", 60, "high", "Power endurance"),
                    QuestTask("Sunday", "Cold therapy + rest", "recovery", 25, "low", "Ice bath recovery")
                )
            )

            // Maintain Focus
            params.goal == "maintain" && params.intensity == "low" -> listOf(
                // Pool 1
                listOf(
                    QuestTask("Monday", "20 min walk", "cardio", 20, "low", "Light activity"),
                    QuestTask("Tuesday", "Balanced meal check", "nutrition", 10, "low", "Review diet"),
                    QuestTask("Wednesday", "15 min stretching", "flexibility", 15, "low", "Mobility work"),
                    QuestTask("Thursday", "20 min casual activity", "cardio", 20, "low", "Flexible timing"),
                    QuestTask("Friday", "Light meal planning", "nutrition", 15, "low", "Prep basics"),
                    QuestTask("Saturday", "30 min leisure walk", "cardio", 30, "low", "Enjoy movement"),
                    QuestTask("Sunday", "Rest + hydration", "recovery", 10, "low", "Recovery focus")
                ),
                // Pool 2 (alternative)
                listOf(
                    QuestTask("Monday", "25 min easy walk", "cardio", 25, "low", "Relaxing pace"),
                    QuestTask("Tuesday", "Check nutrition goals", "nutrition", 15, "low", "Track meals"),
                    QuestTask("Wednesday", "20 min yoga", "flexibility", 20, "low", "Gentle flow"),
                    QuestTask("Thursday", "15 min swimming", "cardio", 15, "low", "Easy swim"),
                    QuestTask("Friday", "Meal prep basics", "nutrition", 20, "low", "Simple prep"),
                    QuestTask("Saturday", "35 min park walk", "cardio", 35, "low", "Nature time"),
                    QuestTask("Sunday", "Recovery day", "recovery", 10, "low", "Full rest")
                ),
                // Pool 3 (more variety)
                listOf(
                    QuestTask("Monday", "18 min garden walk", "cardio", 18, "low", "Light movement"),
                    QuestTask("Tuesday", "Nutrition journaling", "nutrition", 12, "low", "Track daily intake"),
                    QuestTask("Wednesday", "22 min tai chi", "flexibility", 22, "low", "Mindful movement"),
                    QuestTask("Thursday", "25 min bike ride", "cardio", 25, "low", "Easy cycling"),
                    QuestTask("Friday", "Prep weekly snacks", "nutrition", 18, "low", "Healthy options"),
                    QuestTask("Saturday", "28 min nature stroll", "cardio", 28, "low", "Scenic route"),
                    QuestTask("Sunday", "Meditation + rest", "recovery", 15, "low", "Mental recovery")
                ),
                // Pool 4 (pilates focused)
                listOf(
                    QuestTask("Monday", "20 min pilates mat", "flexibility", 20, "low", "Core stability"),
                    QuestTask("Tuesday", "Mindful eating", "nutrition", 15, "low", "Slow eating practice"),
                    QuestTask("Wednesday", "25 min water walking", "cardio", 25, "low", "Pool exercise"),
                    QuestTask("Thursday", "15 min resistance bands", "strength", 15, "low", "Light resistance"),
                    QuestTask("Friday", "Balance meal portions", "nutrition", 10, "low", "Check plate balance"),
                    QuestTask("Saturday", "30 min nature photography walk", "cardio", 30, "low", "Mindful walking"),
                    QuestTask("Sunday", "Restorative yoga", "recovery", 20, "low", "Deep relaxation")
                ),
                // Pool 5 (gentle mixed)
                listOf(
                    QuestTask("Monday", "20 min morning stretch walk", "mixed", 20, "low", "Wake up routine"),
                    QuestTask("Tuesday", "Hydration tracking", "nutrition", 10, "low", "8 glasses goal"),
                    QuestTask("Wednesday", "20 min dancing at home", "cardio", 20, "low", "Fun movement"),
                    QuestTask("Thursday", "15 min light calisthenics", "strength", 15, "low", "Bodyweight basics"),
                    QuestTask("Friday", "Colorful meal prep", "nutrition", 25, "low", "Rainbow vegetables"),
                    QuestTask("Saturday", "35 min dog walking", "cardio", 35, "low", "Pet bonding time"),
                    QuestTask("Sunday", "Bath + rest", "recovery", 15, "low", "Self-care day")
                )
            )
            params.goal == "maintain" && params.intensity == "medium" -> listOf(
                // Pool 1
                listOf(
                    QuestTask("Monday", "30 min cardio", "cardio", 30, "medium", "Walk or cycle"),
                    QuestTask("Tuesday", "25 min strength", "strength", 25, "medium", "2-3 exercises"),
                    QuestTask("Wednesday", "30 min cardio", "cardio", 30, "medium", "Running or swim"),
                    QuestTask("Thursday", "25 min strength", "strength", 25, "medium", "Different muscles"),
                    QuestTask("Friday", "Meal prep", "nutrition", 30, "low", "Prepare meals"),
                    QuestTask("Saturday", "40 min mixed", "mixed", 40, "medium", "Cardio + strength"),
                    QuestTask("Sunday", "Stretching + planning", "recovery", 20, "low", "Relax & plan")
                ),
                // Pool 2 (alternative)
                listOf(
                    QuestTask("Monday", "35 min jogging", "cardio", 35, "medium", "Steady jog"),
                    QuestTask("Tuesday", "30 min strength", "strength", 30, "medium", "Dumbbells"),
                    QuestTask("Wednesday", "35 min cycling", "cardio", 35, "medium", "Moderate pace"),
                    QuestTask("Thursday", "20 min HIIT", "cardio", 20, "medium", "Intervals"),
                    QuestTask("Friday", "Meal prep + cardio", "mixed", 40, "medium", "Prep + walk"),
                    QuestTask("Saturday", "45 min sports/dance", "mixed", 45, "medium", "Fun activity"),
                    QuestTask("Sunday", "Yoga + planning", "recovery", 25, "low", "Stretch & relax")
                ),
                // Pool 3 (more variety)
                listOf(
                    QuestTask("Monday", "40 min swimming", "cardio", 40, "medium", "Full body swim"),
                    QuestTask("Tuesday", "28 min weights", "strength", 28, "medium", "Compound lifts"),
                    QuestTask("Wednesday", "32 min elliptical", "cardio", 32, "medium", "Low impact"),
                    QuestTask("Thursday", "22 min plyometrics", "strength", 22, "medium", "Jump exercises"),
                    QuestTask("Friday", "Grocery + prep", "nutrition", 35, "low", "Shopping + cooking"),
                    QuestTask("Saturday", "50 min hiking", "mixed", 50, "medium", "Outdoor adventure"),
                    QuestTask("Sunday", "Foam rolling + rest", "recovery", 25, "low", "Mobility work")
                ),
                // Pool 4 (balance focused)
                listOf(
                    QuestTask("Monday", "35 min trail running", "cardio", 35, "medium", "Variable terrain"),
                    QuestTask("Tuesday", "30 min TRX workout", "strength", 30, "medium", "Suspension training"),
                    QuestTask("Wednesday", "30 min boxing cardio", "cardio", 30, "medium", "Bag work"),
                    QuestTask("Thursday", "25 min yoga strength", "mixed", 25, "medium", "Power yoga"),
                    QuestTask("Friday", "Balanced macros tracking", "nutrition", 20, "low", "Carbs/protein/fats"),
                    QuestTask("Saturday", "45 min rock climbing", "mixed", 45, "medium", "Bouldering"),
                    QuestTask("Sunday", "Active rest walk", "recovery", 25, "low", "Light stroll")
                ),
                // Pool 5 (functional fitness)
                listOf(
                    QuestTask("Monday", "35 min kettlebell complex", "mixed", 35, "medium", "Full body flow"),
                    QuestTask("Tuesday", "30 min sandbag training", "strength", 30, "medium", "Odd object lifting"),
                    QuestTask("Wednesday", "30 min rowing intervals", "cardio", 30, "medium", "2min hard/1min easy"),
                    QuestTask("Thursday", "25 min farmer carries + squats", "strength", 25, "medium", "Grip + legs"),
                    QuestTask("Friday", "Meal timing optimization", "nutrition", 15, "low", "Eating schedule"),
                    QuestTask("Saturday", "40 min obstacle course", "mixed", 40, "medium", "Fun challenge"),
                    QuestTask("Sunday", "Mobility flow", "recovery", 20, "low", "Joint health")
                )
            )
            params.goal == "maintain" && params.intensity == "high" -> listOf(
                // Pool 1
                listOf(
                    QuestTask("Monday", "45 min cardio", "cardio", 45, "high", "Fast pace"),
                    QuestTask("Tuesday", "40 min strength", "strength", 40, "high", "Full body"),
                    QuestTask("Wednesday", "45 min cardio", "cardio", 45, "high", "Interval training"),
                    QuestTask("Thursday", "40 min strength", "strength", 40, "high", "Heavy weights"),
                    QuestTask("Friday", "30 min cardio", "cardio", 30, "medium", "Recovery pace"),
                    QuestTask("Saturday", "60 min mixed", "mixed", 60, "high", "Complete workout"),
                    QuestTask("Sunday", "Active recovery", "recovery", 30, "low", "Yoga or walk")
                ),
                // Pool 2 (alternative)
                listOf(
                    QuestTask("Monday", "50 min running", "cardio", 50, "high", "Hard effort"),
                    QuestTask("Tuesday", "45 min weight training", "strength", 45, "high", "Heavy compounds"),
                    QuestTask("Wednesday", "45 min circuit training", "mixed", 45, "high", "Full body"),
                    QuestTask("Thursday", "45 min strength", "strength", 45, "high", "Power moves"),
                    QuestTask("Friday", "25 min cardio", "cardio", 25, "medium", "Light session"),
                    QuestTask("Saturday", "65 min full workout", "mixed", 65, "high", "Complete routine"),
                    QuestTask("Sunday", "Active recovery", "recovery", 30, "low", "Easy walk")
                ),
                // Pool 4 (endurance athlete)
                listOf(
                    QuestTask("Monday", "50 min tempo run", "cardio", 50, "high", "Sustained fast pace"),
                    QuestTask("Tuesday", "40 min strength for runners", "strength", 40, "high", "Leg + core power"),
                    QuestTask("Wednesday", "45 min interval training", "cardio", 45, "high", "Speed work"),
                    QuestTask("Thursday", "45 min upper body strength", "strength", 45, "high", "Arm drive power"),
                    QuestTask("Friday", "30 min recovery jog", "cardio", 30, "low", "Easy pace"),
                    QuestTask("Saturday", "70 min long run", "cardio", 70, "high", "Endurance building"),
                    QuestTask("Sunday", "Compression + ice bath", "recovery", 30, "low", "Recovery protocol")
                ),
                // Pool 5 (athlete maintenance)
                listOf(
                    QuestTask("Monday", "50 min sport-specific drills", "mixed", 50, "high", "Skill practice"),
                    QuestTask("Tuesday", "45 min Olympic lifting", "strength", 45, "high", "Power development"),
                    QuestTask("Wednesday", "40 min agility training", "cardio", 40, "high", "Speed + coordination"),
                    QuestTask("Thursday", "45 min compound lifts", "strength", 45, "high", "Maintain strength"),
                    QuestTask("Friday", "Performance nutrition", "nutrition", 20, "low", "Optimize timing"),
                    QuestTask("Saturday", "60 min competition simulation", "mixed", 60, "high", "Game practice"),
                    QuestTask("Sunday", "Contrast therapy + rest", "recovery", 35, "low", "Hot/cold therapy")
                )
            )

            // Build Muscle Focus
            params.goal == "build" && params.intensity == "low" -> listOf(
                // Pool 1
                listOf(
                    QuestTask("Monday", "20 min light strength", "strength", 20, "low", "Bodyweight basics"),
                    QuestTask("Tuesday", "Track protein intake", "nutrition", 15, "low", "Aim for 1.6g/kg"),
                    QuestTask("Wednesday", "20 min light strength", "strength", 20, "low", "Different exercises"),
                    QuestTask("Thursday", "15 min core work", "strength", 15, "low", "Abs & stability"),
                    QuestTask("Friday", "Meal prep (high protein)", "nutrition", 30, "low", "Protein-rich meals"),
                    QuestTask("Saturday", "20 min light strength", "strength", 20, "low", "Gentle progression"),
                    QuestTask("Sunday", "Rest + protein tracking", "recovery", 10, "low", "Recovery focus")
                ),
                // Pool 2 (alternative)
                listOf(
                    QuestTask("Monday", "25 min strength", "strength", 25, "low", "Light weights"),
                    QuestTask("Tuesday", "Protein meal plan", "nutrition", 20, "low", "Plan high-protein"),
                    QuestTask("Wednesday", "25 min strength", "strength", 25, "low", "Bodyweight"),
                    QuestTask("Thursday", "20 min arms/core", "strength", 20, "low", "Targeted work"),
                    QuestTask("Friday", "Protein prep", "nutrition", 35, "low", "Cook meals"),
                    QuestTask("Saturday", "25 min strength", "strength", 25, "low", "Full body light"),
                    QuestTask("Sunday", "Rest day", "recovery", 10, "low", "Recovery")
                ),
                // Pool 4 (beginner muscle gain)
                listOf(
                    QuestTask("Monday", "20 min push-ups progression", "strength", 20, "low", "Chest building"),
                    QuestTask("Tuesday", "Protein smoothie prep", "nutrition", 15, "low", "Easy protein intake"),
                    QuestTask("Wednesday", "20 min pull-ups assists", "strength", 20, "low", "Back development"),
                    QuestTask("Thursday", "15 min squat variations", "strength", 15, "low", "Leg foundation"),
                    QuestTask("Friday", "High-calorie meal prep", "nutrition", 30, "low", "Surplus eating"),
                    QuestTask("Saturday", "25 min dumbbell workout", "strength", 25, "low", "Basic movements"),
                    QuestTask("Sunday", "Stretching + recovery", "recovery", 15, "low", "Prevent soreness")
                ),
                // Pool 5 (home gym beginner)
                listOf(
                    QuestTask("Monday", "20 min resistance band chest", "strength", 20, "low", "Portable equipment"),
                    QuestTask("Tuesday", "Meal frequency planning", "nutrition", 15, "low", "5-6 small meals"),
                    QuestTask("Wednesday", "20 min resistance band back", "strength", 20, "low", "Rowing motions"),
                    QuestTask("Thursday", "18 min bodyweight legs", "strength", 18, "low", "Lunges + squats"),
                    QuestTask("Friday", "Bulk cooking session", "nutrition", 40, "low", "Weekly prep"),
                    QuestTask("Saturday", "25 min full body bands", "strength", 25, "low", "Circuit training"),
                    QuestTask("Sunday", "Rest + hydration", "recovery", 10, "low", "Muscle recovery")
                )
            )
            params.goal == "build" && params.intensity == "medium" -> listOf(
                // Pool 1
                listOf(
                    QuestTask("Monday", "35 min strength (push)", "strength", 35, "medium", "Chest, shoulders, tris"),
                    QuestTask("Tuesday", "Track high-protein meals", "nutrition", 15, "low", "1.6-2g protein/kg"),
                    QuestTask("Wednesday", "35 min strength (pull)", "strength", 35, "medium", "Back, lats, bis"),
                    QuestTask("Thursday", "30 min leg day", "strength", 30, "medium", "Quads, hamstrings"),
                    QuestTask("Friday", "Meal prep + light cardio", "mixed", 40, "low", "20 min activity"),
                    QuestTask("Saturday", "40 min strength (full)", "strength", 40, "medium", "Compound movements"),
                    QuestTask("Sunday", "Rest + meal planning", "recovery", 15, "low", "Prep high-protein meals")
                ),
                // Pool 2 (alternative)
                listOf(
                    QuestTask("Monday", "40 min upper body", "strength", 40, "medium", "Push focus"),
                    QuestTask("Tuesday", "High-protein nutrition", "nutrition", 20, "low", "Track macros"),
                    QuestTask("Wednesday", "40 min back/pull", "strength", 40, "medium", "Pull focus"),
                    QuestTask("Thursday", "35 min legs", "strength", 35, "medium", "Lower body"),
                    QuestTask("Friday", "Meal prep + walk", "mixed", 45, "low", "Nutrition + activity"),
                    QuestTask("Saturday", "45 min full body", "strength", 45, "medium", "All muscle groups"),
                    QuestTask("Sunday", "Rest + meal prep", "recovery", 20, "low", "Prepare meals")
                ),
                // Pool 4 (hypertrophy focused)
                listOf(
                    QuestTask("Monday", "40 min chest + triceps", "strength", 40, "medium", "8-12 rep range"),
                    QuestTask("Tuesday", "Anabolic window nutrition", "nutrition", 20, "low", "Post-workout meals"),
                    QuestTask("Wednesday", "40 min back + biceps", "strength", 40, "medium", "Volume training"),
                    QuestTask("Thursday", "38 min leg hypertrophy", "strength", 38, "medium", "High volume"),
                    QuestTask("Friday", "Carb loading", "nutrition", 15, "low", "Pre-workout carbs"),
                    QuestTask("Saturday", "45 min shoulders + abs", "strength", 45, "medium", "Complete physique"),
                    QuestTask("Sunday", "Massage + rest", "recovery", 20, "low", "Muscle recovery")
                ),
                // Pool 5 (powerbuilding)
                listOf(
                    QuestTask("Monday", "42 min bench press focus", "strength", 42, "medium", "Strength + volume"),
                    QuestTask("Tuesday", "Protein distribution", "nutrition", 18, "low", "Every 3-4 hours"),
                    QuestTask("Wednesday", "42 min deadlift focus", "strength", 42, "medium", "Pull power"),
                    QuestTask("Thursday", "40 min squat focus", "strength", 40, "medium", "Leg power"),
                    QuestTask("Friday", "Pre-workout meal timing", "nutrition", 15, "low", "1-2 hours before"),
                    QuestTask("Saturday", "48 min accessory work", "strength", 48, "medium", "Isolation exercises"),
                    QuestTask("Sunday", "Active rest + stretching", "recovery", 25, "low", "Light movement")
                )
            )
            params.goal == "build" && params.intensity == "high" -> listOf(
                // Pool 1
                listOf(
                    QuestTask("Monday", "45 min strength (push)", "strength", 45, "high", "Heavy compounds"),
                    QuestTask("Tuesday", "Protein tracking + prep", "nutrition", 20, "low", "2g protein/kg goal"),
                    QuestTask("Wednesday", "45 min strength (pull)", "strength", 45, "high", "Heavy lifts"),
                    QuestTask("Thursday", "45 min leg day", "strength", 45, "high", "Squats, deadlifts"),
                    QuestTask("Friday", "30 min light cardio", "cardio", 30, "low", "Active recovery"),
                    QuestTask("Saturday", "60 min full strength", "strength", 60, "high", "All muscle groups"),
                    QuestTask("Sunday", "Rest + nutrition review", "recovery", 20, "low", "Plan nutrition")
                ),
                // Pool 2 (alternative)
                listOf(
                    QuestTask("Monday", "50 min heavy push", "strength", 50, "high", "Chest focus"),
                    QuestTask("Tuesday", "Protein + supplements", "nutrition", 25, "low", "Max intake"),
                    QuestTask("Wednesday", "50 min heavy pull", "strength", 50, "high", "Back focus"),
                    QuestTask("Thursday", "50 min power legs", "strength", 50, "high", "Max strength"),
                    QuestTask("Friday", "20 min light cardio", "cardio", 20, "low", "Easy recovery"),
                    QuestTask("Saturday", "65 min full workout", "strength", 65, "high", "Complete session"),
                    QuestTask("Sunday", "Rest + meal planning", "recovery", 25, "low", "Prep meals")
                ),
                // Pool 4 (bodybuilding prep)
                listOf(
                    QuestTask("Monday", "50 min chest day", "strength", 50, "high", "Mass building"),
                    QuestTask("Tuesday", "Macro tracking strict", "nutrition", 30, "low", "Perfect nutrition"),
                    QuestTask("Wednesday", "50 min back day", "strength", 50, "high", "Width + thickness"),
                    QuestTask("Thursday", "55 min leg day", "strength", 55, "high", "Complete leg work"),
                    QuestTask("Friday", "25 min cardio fasted", "cardio", 25, "low", "Fat oxidation"),
                    QuestTask("Saturday", "55 min arms + shoulders", "strength", 55, "high", "Detail work"),
                    QuestTask("Sunday", "Meal prep + posing", "recovery", 30, "low", "Competition prep")
                ),
                // Pool 5 (strongman training)
                listOf(
                    QuestTask("Monday", "55 min heavy squat day", "strength", 55, "high", "Max strength"),
                    QuestTask("Tuesday", "High calorie surplus", "nutrition", 20, "low", "4000+ calories"),
                    QuestTask("Wednesday", "50 min heavy bench", "strength", 50, "high", "Upper power"),
                    QuestTask("Thursday", "60 min deadlift day", "strength", 60, "high", "Posterior chain"),
                    QuestTask("Friday", "Recovery cardio", "cardio", 20, "low", "Blood flow"),
                    QuestTask("Saturday", "55 min overhead press + carries", "strength", 55, "high", "Event training"),
                    QuestTask("Sunday", "Ice bath + massage", "recovery", 30, "low", "Deep recovery")
                )
            )

            else -> listOf(
                // Default Pool
                listOf(
                    QuestTask("Monday", "30 min activity", "cardio", 30, "medium", "Any movement"),
                    QuestTask("Tuesday", "20 min strength", "strength", 20, "medium", "Light weights"),
                    QuestTask("Wednesday", "25 min cardio", "cardio", 25, "medium", "Walk or cycle"),
                    QuestTask("Thursday", "15 min stretching", "flexibility", 15, "low", "Mobility"),
                    QuestTask("Friday", "Meal prep", "nutrition", 30, "low", "Prepare meals"),
                    QuestTask("Saturday", "40 min mixed", "mixed", 40, "medium", "Varied activity"),
                    QuestTask("Sunday", "Rest day", "recovery", 10, "low", "Relax")
                )
            )
        }

        // Select one of the task pools randomly with MULTIPLE entropy sources + exclusion logic
        val selectedIndex: Int
        val selectedTasks = if (taskPools.isNotEmpty()) {
            // Get list of available (non-excluded) pools FIRST
            val availablePools = if (excludePoolIndices.isNotEmpty() && taskPools.size > 1) {
                (0 until taskPools.size).filter { it !in excludePoolIndices }
            } else {
                (0 until taskPools.size).toList()
            }
            
            // If we have available pools, pick from them; otherwise use all pools
            val poolsToChooseFrom = if (availablePools.isNotEmpty()) availablePools else (0 until taskPools.size).toList()
            
            // Use combination of UUID + nanoTime + random for better distribution
            val uuidBasedIndex = (uuidLong.hashCode() and 0x7FFFFFFF) % poolsToChooseFrom.size
            val timeBasedIndex = ((System.nanoTime() / 1000000) % poolsToChooseFrom.size).toInt()
            val randomBasedIndex = random.nextInt(poolsToChooseFrom.size)
            
            // Combine all three entropy sources with XOR for selection
            val selectionIndex = (uuidBasedIndex xor timeBasedIndex xor randomBasedIndex) % poolsToChooseFrom.size
            val candidateIndex = poolsToChooseFrom[selectionIndex]
            
            android.util.Log.d("AIRepository", "Pool selection - Available pools: $poolsToChooseFrom, Excluded: $excludePoolIndices, Selected: $candidateIndex/${taskPools.size}")
            
            selectedIndex = candidateIndex
            android.util.Log.d("AIRepository", "Final selected pool index: $selectedIndex out of ${taskPools.size} pools")
            android.util.Log.d("AIRepository", "First task of selected pool: ${taskPools[selectedIndex].firstOrNull()?.task ?: "none"}")
            taskPools[selectedIndex]
        } else {
            selectedIndex = -1
            android.util.Log.e("AIRepository", "No task pools found for goal=${params.goal}, intensity=${params.intensity}!")
            emptyList()
        }

        // Store the selected index for next generation
        lastGeneratedPoolIndex = selectedIndex
        
        // Apply dynamic adjustments based on user preferences
        val adjustedSchedule = adjustSchedule(selectedTasks, params)
        
        android.util.Log.d("AIRepository", "Base tasks: ${selectedTasks.size}, After adjustment: ${adjustedSchedule.size}, Pool: $lastGeneratedPoolIndex")
        android.util.Log.d("AIRepository", "Sample task: ${adjustedSchedule.firstOrNull()?.let { "${it.day}: ${it.task} (${it.duration}min)" } ?: "none"}")
        
        return adjustedSchedule
    }

    /**
     * Generate a short list of follow-up questions (5) to clarify user needs based on discovery params.
     * This is a lightweight, deterministic generator suitable for on-device behavior.
     */
    fun generateFollowUpQuestions(params: DailyQuestParams): List<String> {
        val questions = mutableListOf<String>()
        // Base question about motivation
        questions.add("What's your main motivation for this plan (health, appearance, energy)?")

        // Time availability question derived from intensity
        questions.add(
            when (params.intensity) {
                "low" -> "Do you prefer short sessions (10-30 min) most days or longer sessions less often?"
                "high" -> "Can you commit to higher-intensity sessions (30-60 min) on several days per week?"
                else -> "Would you rather have mixed shorter+longer sessions across the week?"
            }
        )

        // Eating / dietary clarification
        questions.add("Do you have any specific dietary preferences or restrictions we should consider?")

        // Sleep/Recovery probe
        questions.add("Are sleep and recovery a priority this week, or do you want to focus more on active workouts?")

        // Support / accountability
        questions.add("Would you like reminders and progress check-ins during the week?")

        return questions
    }

    /**
     * Create a short plan summary string based on discovery params.
     */
    fun generatePlanSummary(params: DailyQuestParams, state: com.example.projectbmi.UserDiscoveryState): String {
        val sb = StringBuilder()
        sb.append("Plan summary:\n")
        sb.append("Goal: ${params.goal.replace('-', ' ')}\n")
        sb.append("Exercise frequency/intensity: ${params.intensity} (${state.exerciseFrequency ?: "—"})\n")
        sb.append("Eating pattern: ${params.focusArea} (${state.eatingPattern ?: "—"})\n")
        sb.append("Sleep: ${state.sleepHours ?: "—"}\n")
        if (state.challenges.isNotEmpty()) sb.append("Challenges: ${state.challenges.joinToString(", ")}\n")
        sb.append("Recommendation: Follow the generated 7-day schedule; prioritize consistency and recovery.")
        return sb.toString()
    }

}
