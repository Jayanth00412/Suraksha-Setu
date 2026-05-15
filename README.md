# SURAKSHA-SETU

## Hyper Local Safety Network Android Application

SURAKSHA-SETU is an Android-based emergency safety application designed to provide instant SOS support, live location tracking, nearby responder connectivity, and safer route assistance during emergency situations. The application aims to improve community safety using real-time mobile technology.

# Features

## 🚨 Emergency SOS Trigger

* One-tap SOS activation
* Sends emergency alerts instantly
* Designed for quick emergency response

## 📍 Live Location Tracking

* Real-time GPS location updates
* Opens location directly in Google Maps
* Helps responders identify user location quickly

## 👥 Nearby Responders System

* Displays nearby available responders
* Hyper-local safety network concept
* Faster emergency assistance

## ⚡ Safe Path Assistance

* Displays safer travel routes
* Helps users avoid unsafe areas

## 🔔 Push Notification Support

* Emergency alert notifications
* Firebase Cloud Messaging integration

## 🗺 Google Maps Integration

* Embedded map services
* Real-time navigation support

---

# Technologies Used

* Java
* Android Studio
* Firebase
* Google Maps API
* XML
* Gradle

---

# Project Structure

```bash
app/
 ├── java/
 ├── res/
 ├── manifests/
 ├── Gradle Scripts/
```

---

# Installation Steps

## 1. Clone Repository

```bash
git clone https://github.com/yourusername/Suraksha-Setu.git
```

## 2. Open in Android Studio

* Open Android Studio
* Click "Open Project"
* Select project folder

## 3. Configure Firebase

* Add `google-services.json`
* Enable Firebase services

## 4. Add Google Maps API Key

Add your API key inside:

```xml
AndroidManifest.xml
```

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY"/>
```

## 5. Build and Run

* Connect Android device
* Enable USB debugging
* Click ▶ Run
  
# Permissions Used

* Internet
* Location Access
* SMS
* Microphone
* Notifications


# Future Enhancements

* AI-based danger prediction
* Voice-activated SOS
* Offline emergency support
* Real-time volunteer tracking
* Emergency contact integration


# Use Case

This application can be useful for:

* Women safety
* Elderly emergency support
* Night travel safety
* Community emergency response systems
  
