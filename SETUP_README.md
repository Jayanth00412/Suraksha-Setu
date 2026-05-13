# Suraksha-Setu v2.0 - Android Studio Setup Guide

## What's New in v2.0
- ✅ Always shows Register/Login on first launch
- ✅ Once registered, only shows Login on subsequent opens
- ✅ Trust Circle: save contacts with real phone numbers
- ✅ Call button on every contact opens phone dialer directly
- ✅ Live GPS location updates every 5 seconds
- ✅ SOS sends live Google Maps link via SMS to all saved contacts
- ✅ Logout button on main screen

---

## Step 1: Prerequisites

Install these before opening the project:

| Tool | Version | Download |
|------|---------|----------|
| Android Studio | Hedgehog (2023.1.1) or newer | https://developer.android.com/studio |
| JDK | 17 (comes with Android Studio) | Built-in |
| Android SDK | API 34 | Via SDK Manager in Android Studio |

---

## Step 2: Firebase Setup (REQUIRED)

This app uses Firebase Auth + Firestore. You need a free Firebase project.

1. Go to https://console.firebase.google.com
2. Click **"Add Project"** → name it "suraksha-setu"
3. In Project > **Authentication** → Enable **Email/Password** sign-in
4. In Project > **Firestore Database** → Create database in **test mode**
5. Click the Android icon (⚙) to add an Android app:
   - Package name: `com.suraksha.setu`
   - Download `google-services.json`
6. **Copy `google-services.json`** into:
   ```
   suraksha-setu-v2/android/app/google-services.json
   ```

---

## Step 3: Open in Android Studio

1. Open Android Studio
2. Click **"Open"** → navigate to `suraksha-setu-v2/android/`
3. Click **OK** — Android Studio will detect it as a Gradle project
4. Wait for **Gradle sync** to complete (first time downloads ~500MB of dependencies)
5. If you see "Gradle sync failed" → see Troubleshooting below

---

## Step 4: Run the App

### On Emulator:
1. Tools → **Device Manager** → Create Virtual Device
2. Choose **Pixel 6** → System Image: **API 34 (Android 14)**
3. Click ▶️ **Run** button (or Shift+F10)

### On Real Phone:
1. Enable **Developer Options** on your phone:
   - Settings → About Phone → Tap "Build Number" 7 times
2. Enable **USB Debugging** in Developer Options
3. Connect phone via USB → Select your device in Android Studio
4. Click ▶️ **Run**

---

## Step 5: First Use Flow

```
App Launch
    ↓
LoginActivity (always shown on fresh install)
    ↓ (new user taps "Register")
RegisterActivity → fills Name, Phone, Email, Password
    ↓ (account created in Firebase)
MainActivity (home screen with SOS, Live Map)
    ↓ (next time app opens)
LoginActivity → user enters email + password → Main
```

---

## Firestore Security Rules (After Testing)

Once you're done testing, go to Firestore → Rules and paste:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
      match /contacts/{contactId} {
        allow read, write: if request.auth.uid == userId;
      }
    }
    match /alerts/{alertId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    match /volunteers/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
  }
}
```

---

## Troubleshooting

### "google-services.json not found"
→ Download from Firebase Console and paste into `app/` folder

### "Gradle sync failed: Could not resolve..."
→ Check internet connection. File → Invalidate Caches → Restart

### "JAVA_HOME not set"
→ In Android Studio: File → Project Structure → SDK Location → Set JDK path

### Emulator has no location
→ In emulator: (•••) → Location → Set a custom location (e.g. 12.9716, 77.5946)

### SMS not sending on emulator
→ SMS only works on real devices. Use a physical phone to test SOS SMS feature.

---

## App Permissions Required at Runtime

| Permission | Purpose |
|-----------|---------|
| ACCESS_FINE_LOCATION | Real-time GPS for live tracking & SOS |
| SEND_SMS | Send emergency location to trusted contacts |
| CALL_PHONE | Dial contacts from Trust Circle |
| INTERNET | Firebase sync & online alerts |

---

## Project Structure

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/suraksha/setu/
│   │   │   ├── LoginActivity.kt        ← Auth gate (always first)
│   │   │   ├── RegisterActivity.kt     ← New user registration
│   │   │   ├── MainActivity.kt         ← Home + SOS + Live location
│   │   │   ├── TrustCircleActivity.kt  ← Contacts with call button ← NEW
│   │   │   ├── VolunteerActivity.kt    ← Volunteer/responder hub
│   │   │   ├── SettingsActivity.kt     ← Shake/tap trigger config
│   │   │   └── SOSForegroundService.kt ← Background SOS service
│   │   ├── res/layout/
│   │   │   ├── activity_login.xml
│   │   │   ├── activity_register.xml
│   │   │   ├── activity_main.xml
│   │   │   ├── activity_trust_circle.xml  ← NEW
│   │   │   ├── item_contact.xml           ← NEW (call button row)
│   │   │   └── ...
│   │   └── AndroidManifest.xml
│   ├── google-services.json  ← YOU MUST ADD THIS
│   └── build.gradle
├── build.gradle
└── SETUP_README.md  ← This file
```
