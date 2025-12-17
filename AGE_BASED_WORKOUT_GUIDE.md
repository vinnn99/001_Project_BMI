# Age-Based Workout Scheduling System

## 📋 Overview

Sistem BMI Calculator sekarang memiliki **age-aware workout scheduling** yang menyesuaikan intensitas, durasi, dan jenis latihan berdasarkan umur user. Fitur ini memastikan program latihan aman dan efektif untuk semua rentang usia.

---

## 🎯 Age Groups & Recommendations

### Group 1: Ages 16-25 (Young Adults)
**Capacity**: HIGH  
**Intensity Adjustment**: Dapat upgrade dari medium → high intensity

#### Workout Characteristics:
- ✅ **Duration**: Dapat ditingkatkan hingga 10% dari baseline
- ✅ **Intensity**: High intensity training (HIIT, heavy weights)
- ✅ **Volume**: Banyak sets/reps, volume tinggi
- ✅ **Recovery**: Cepat recovery (1-2 menit istirahat)
- ✅ **Focus**: Building strength, muscle, endurance

#### Contoh Schedule (Weight Loss, High Intensity):
```
Monday    → 45 min HIIT cardio (Intervals + recovery)
Tuesday   → 40 min strength training (Full body circuit)
Wednesday → 45 min cardio (Run/Bike high intensity)
Thursday  → 40 min strength + core (Weights + abs)
Friday    → Meal prep + 20 min cardio
Saturday  → 60 min mixed workout (Cardio + strength)
Sunday    → Active recovery + meal plan (30 min yoga)
```

#### Tips:
- Manfaatkan kapasitas recovery maksimal
- Coba progressive overload (tambah weight setiap minggu)
- Jangan lupa stretching & mobility work
- Monitor sleep (7-9 jam)

---

### Group 2: Ages 26-35 (Early Adulthood)
**Capacity**: BALANCED  
**Intensity Adjustment**: Tetap sesuai request user

#### Workout Characteristics:
- ✅ **Duration**: Standard, sesuai request
- ✅ **Intensity**: Medium-High, tergantung goal
- ✅ **Volume**: Balanced, tidak terlalu extreme
- ✅ **Recovery**: Normal (2-3 menit istirahat)
- ✅ **Focus**: Balance antara strength, cardio, health

#### Contoh Schedule (Weight Loss, Medium Intensity):
```
Monday    → 30 min cardio (Walk/Jog)
Tuesday   → 25 min strength training
Wednesday → 30 min cardio (Running/Swimming)
Thursday  → 25 min strength training
Friday    → Meal prep
Saturday  → 45 min mixed activity
Sunday    → 20 min stretching + planning
```

#### Tips:
- Fokus pada konsistensi (3-4x/minggu)
- Mulai consider work-life balance
- Integrasikan flexibility & mobility training
- Track progress (weight, reps, times)

---

### Group 3: Ages 36-50 (Mid-Life)
**Capacity**: MODERATE  
**Intensity Adjustment**: Downgrade medium intensity → medium, high → medium

#### Workout Characteristics:
- ⚠️ **Duration**: Dikurangi ~10% dari baseline
- ⚠️ **Intensity**: Medium intensity, hindari extreme
- ⚠️ **Recovery**: Lebih lama diperlukan (3-4 menit istirahat)
- ⚠️ **Focus**: Injury prevention, mobility, strength maintenance
- ⚠️ **Notes**: "Focus on proper form & recovery"

#### Contoh Schedule (Weight Loss, Medium Intensity, Age-Adjusted):
```
Monday    → 27 min cardio (10% less)
Tuesday   → 22 min strength (lighter weight)
Wednesday → 27 min cardio
Thursday  → 22 min strength (different muscles)
Friday    → Meal prep
Saturday  → 40 min mixed activity (adjusted)
Sunday    → 18 min stretching + planning
```

#### Tips:
- Prioritas: Form > Weight
- Jangan overdo high intensity
- Tambahan: 1-2 yoga/flexibility session per minggu
- Sleep quality matters lebih dari quantity
- Check-up kesehatan rutin

---

### Group 4: Ages 51-65 (Late Adulthood)
**Capacity**: LOW-MEDIUM  
**Intensity Adjustment**: ALL downgraded to LOW

#### Workout Characteristics:
- 🛑 **Duration**: Dikurangi ~25% dari baseline
- 🛑 **Intensity**: LOW ONLY (bahkan jika request high)
- 🛑 **Recovery**: Sangat important (4-5 menit istirahat)
- 🛑 **Safety**: "Joint-friendly, 2-3 min rest between sets. Stop if pain occurs."
- 🛑 **Focus**: Injury prevention, joint health, consistency

#### Contoh Schedule (Weight Loss, Age-Adjusted):
```
Monday    → 15 min low-impact walking
Tuesday   → Track meals + water intake
Wednesday → 11 min home stretching (joint-friendly)
Thursday  → 15 min light cycling (low resistance)
Friday    → Meal prep
Saturday  → 22 min leisurely walk
Sunday    → Rest + hydration focus
```

#### Tips:
- **PENTING**: Warm up 5-10 menit SETIAP sesi
- Choose low-impact: walking, swimming, cycling (bukan running)
- Minimal weight training: bodyweight saja atau very light dumbbells
- 2-3 flexibility/yoga session per minggu MANDATORY
- Monitor heart rate jika ada kondisi
- Konsultasi doctor sebelum mulai program baru

---

### Group 5: Ages 65+ (Senior)
**Capacity**: VERY LOW  
**Intensity Adjustment**: ALWAYS LOW, Force flexibility focus

#### Workout Characteristics:
- 🛑 **Duration**: Dikurangi ~40% dari baseline
- 🛑 **Intensity**: LOW ONLY (automatic)
- 🛑 **Type**: Automatically convert strength → flexibility
- 🛑 **Focus**: "Very gentle. Warm up 10 min. Do with supervision if needed. Focus on balance & flexibility."
- 🛑 **Safety**: CRITICAL

#### Contoh Schedule (Age-Adjusted):
```
Monday    → 12 min gentle walk (after 5 min warm-up)
Tuesday   → Nutrition & hydration focus
Wednesday → 10 min chair yoga/stretching
Thursday  → 9 min water walking (pool)
Friday    → Simple meal prep
Saturday  → 14 min nature stroll (slow pace)
Sunday    → Meditation + full rest day
```

#### Tips:
- **MANDATORY**: Always warm up 5-10 minutes first
- **MANDATORY**: Do with supervision (trainer/family/friend)
- Pain = STOP immediately
- Flexibility & balance adalah priority utama
- Swimming adalah ideal untuk senior (low impact, full body)
- Tai Chi atau Qigong excellent untuk balance
- 3-4 session per minggu, short duration lebih baik dari 1 long session
- Doctor consultation WAJIB sebelum start

---

## 🔧 How It Works (Technical)

### Code Implementation

#### 1. Age Detection
```kotlin
val age = state.age ?: 30  // Default 30 if not provided
```

#### 2. Age Group Classification
```kotlin
when {
    age in 16..25 -> ScreenSize.Compact
    age in 26..35 -> ScreenSize.Medium
    age in 36..50 -> ScreenSize.Expanded
    age in 51..65 -> ScreenSize.ExtraLarge
    else -> ScreenSize.Senior  // 65+
}
```

#### 3. Dynamic Adjustment
```kotlin
val adjustedTask = task.copy(
    duration = (task.duration * durationMultiplier).toInt(),
    notes = safetyNotes
)
```

### Files Modified
- ✅ `AIRepository.kt` - Core age adjustment logic
- ✅ `DailyQuestParams.kt` - Added age parameter
- ✅ `UserDiscoveryState.kt` - Added age field
- ✅ `AskAIScreenClean.kt` - Integration with age data

---

## 📊 Duration Multipliers by Age Group

| Age Range | Multiplier | Duration Change | Example: 30 min → |
|-----------|-----------|-----------------|------------------|
| 16-25    | 1.1x      | +10%            | 33 min           |
| 26-35    | 1.0x      | None            | 30 min           |
| 36-50    | 0.9x      | -10%            | 27 min           |
| 51-65    | 0.75x     | -25%            | 22 min           |
| 65+      | 0.6x      | -40%            | 18 min           |

---

## 🎬 How to Use

### 1. User Provides Age
Ketika user menjawab questionnaire di Ask AI:
```
"Berapa umur Anda?"
→ Input: 55
→ System saves age = 55
```

### 2. System Generates Age-Appropriate Schedule
```kotlin
// System automatically:
val params = DailyQuestParams(..., age = 55)
val schedule = AIRepository.generateWeeklySchedule(params)
// Schedule already age-adjusted!
```

### 3. User Sees Customized Workout Plan
```
Age 55 + Request: High Intensity
System Response: "Medium Intensity" (downgraded for safety)
Duration: 22 min (reduced from 30 min)
Notes: "Joint-friendly, proper form & recovery"
```

---

## ✅ Intensity Downgrade Rules

```
Request: LOW
├─ Age 16-25  → LOW (keep as is)
├─ Age 26-35  → LOW (keep as is)
├─ Age 36-50  → LOW (keep as is)
├─ Age 51-65  → LOW (keep as is)
└─ Age 65+    → LOW (keep as is)

Request: MEDIUM
├─ Age 16-25  → HIGH (upgrade capacity)
├─ Age 26-35  → MEDIUM (keep as is)
├─ Age 36-50  → MEDIUM (keep as is)
├─ Age 51-65  → LOW (downgrade for safety)
└─ Age 65+    → LOW (downgrade for safety)

Request: HIGH
├─ Age 16-25  → HIGH (keep as is)
├─ Age 26-35  → HIGH (keep as is)
├─ Age 36-50  → MEDIUM (downgrade for safety)
├─ Age 51-65  → LOW (downgrade for safety)
└─ Age 65+    → LOW (downgrade for safety)
```

---

## 🔔 Safety Notes Added

Each age group gets automatic safety notes:

### Age 36-50
```
"Focus on proper form & recovery"
```

### Age 51-65
```
"Joint-friendly, 2-3 min rest between sets. Stop if pain occurs."
```

### Age 65+
```
"Very gentle. Warm up 10 min. Do with supervision if needed. Focus on balance & flexibility."
```

---

## 🧪 Testing Guide

### Test Case 1: Young Adult (22 years)
```
Input: age=22, goal="weight-loss", intensity="medium"
Expected: intensity upgraded to "high"
Duration: +10% boost
Result: 30 min → 33 min HIIT
```

### Test Case 2: Mid-Life (45 years)
```
Input: age=45, goal="weight-loss", intensity="high"
Expected: intensity downgraded to "medium"
Duration: -10% reduction
Result: 45 min → 40 min moderate cardio
```

### Test Case 3: Senior (68 years)
```
Input: age=68, goal="maintain", intensity="medium"
Expected: intensity forced to "low"
Category: Strength → Flexibility (auto-convert)
Duration: -40% reduction
Result: 30 min → 18 min gentle stretching
Safe notes: "Very gentle. Warm up 10 min..."
```

---

## 🎯 Benefits

✅ **Safety First**: Mencegah over-training di usia tertentu  
✅ **Realistic Goals**: Sesuai dengan kapasitas recovery  
✅ **Injury Prevention**: Mengurangi risiko injury, terutama untuk 50+  
✅ **Long-term Sustainability**: Program yang bisa dilakukan jangka panjang  
✅ **Evidence-Based**: Mengikuti sports science guidelines  
✅ **Automatic**: User tidak perlu adjust manual

---

## 📚 References

Program ini mengikuti guidelines dari:
- **American College of Sports Medicine (ACSM)** - Exercise Guidelines by Age
- **American Heart Association** - Age-Specific Exercise Recommendations
- **National Institute on Aging** - Fitness Guidelines for Seniors

---

## ❓ FAQ

### Q: Bagaimana jika user tidak ingin provide age?
**A**: Default age = 30 (young adult, neutral). User bisa update later.

### Q: Apakah program bisa di-customize setelah generate?
**A**: Ya! User bisa regenerate atau manually adjust di Daily Quest.

### Q: Apakah age check berjalan real-time?
**A**: Ya! Setiap kali schedule di-generate, age check otomatis dilakukan.

### Q: Apakah ada upper limit umur?
**A**: 65+ semua masuk kategori "Senior" dengan treatment sama.

### Q: Bagaimana dengan athlete 50+ yang fit?
**A**: System conservative untuk safety. Athlete fit bisa regenerate/upgrade manual.

---

## 🚀 Future Enhancements

- [ ] Allow user override age adjustment ("I'm 60 but very fit")
- [ ] Additional health condition checks (back pain, arthritis, etc)
- [ ] Heart rate zone calculation by age
- [ ] Menopause/Andropause specific recommendations
- [ ] Recovery time recommendation by age
- [ ] Injury history check

---

**Last Updated**: December 16, 2025  
**Version**: 1.0  
**Status**: ✅ Production Ready
