# Firebase Firestore Setup - Fix PERMISSION_DENIED Error

## Problem
```
PERMISSION_DENIED: Missing or insufficient permissions
```

## ✅ Status
- ✅ Anonymous Authentication: **ENABLED**
- ✅ App auto sign-in: **CONFIGURED** (in MainActivity.kt)
- ⚠️ Firestore Rules: **NEEDS UPDATE**

---

## 🔥 CRITICAL: Update Firestore Rules

### Step 1: Open Firebase Console
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **ProjectBMI**
3. Click **Firestore Database** (left sidebar)
4. Click **Rules** tab (top menu)

### Step 2: Copy & Paste This Exact Rule
**Replace ALL existing rules with:**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow authenticated users to read/write their own data
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Step 3: Click PUBLISH
- Click the **Publish** button (top right)
- Wait 30-60 seconds for rules to propagate

---

## ✅ Verify Setup

### 1. Check Logcat (Android Studio)
Run the app and filter by: `HistoryRepository`

**Expected logs:**
```
D/HistoryRepository: Adding record to Firestore: 1701763200000
D/HistoryRepository: Record added successfully
```

**If you see:**
```
E/HistoryRepository: Error adding record
PERMISSION_DENIED: Missing or insufficient permissions
```
→ **Go back and publish Firestore rules again!**

### 2. Check Firebase Console
1. Go to **Firestore Database** → **Data** tab
2. You should see this structure:
```
users/
  └── {anonymous_uid}/
      └── history/
          └── {timestamp}/
              ├── bmi: 23.5
              ├── category: "Normal Weight"
              ├── gender: "Male"
              ├── heightCm: 170
              ├── weightKg: 68
              └── timestamp: 1701763200000
```

---

## 🔍 Troubleshooting

### Still Getting PERMISSION_DENIED?

**1. Verify Anonymous Auth is Enabled:**
- Go to Firebase Console → **Authentication** → **Sign-in method**
- Find **Anonymous** provider
- Status should be: **Enabled** ✅

**2. Check if User is Signed In:**
Look for this toast when app starts:
```
"Firebase connected successfully! UID: abc123..."
```

**3. Verify Rules Were Published:**
- Go to Firestore Rules tab
- Check the timestamp shows recent update
- Rules should contain: `request.auth.uid == userId`

**4. Clear App Data & Restart:**
```bash
# In Android Studio
Run → Stop App
Run → Clear App Data
Run → Run 'app'
```

**5. Check Logcat for Auth Status:**
```
D/MainActivity: Firebase connected successfully! UID: ...
```

---

## 📱 Current App Behavior

| Scenario | What Happens |
|----------|--------------|
| **Rules OK + Auth OK** | ✅ Data saved to Firestore + Local |
| **Rules FAIL** | ⚠️ Data saved to Local only (fallback) |
| **No Internet** | ⚠️ Data saved to Local only (will sync when online) |

---

## 🎯 Quick Fix Checklist

- [ ] Open Firebase Console
- [ ] Go to Firestore Database → Rules
- [ ] Copy rules from above
- [ ] Click Publish
- [ ] Wait 60 seconds
- [ ] Restart app
- [ ] Calculate BMI
- [ ] Check Logcat for "Record added successfully"
- [ ] Check Firebase Console Data tab for new document
