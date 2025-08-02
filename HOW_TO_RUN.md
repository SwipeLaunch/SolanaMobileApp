# How to Run - Solana Mobile App

## 🚀 Quick Start Guide

This is a **native Kotlin Android app** with Twitter-like swipe functionality for discovering Solana tokens.

## 📋 Prerequisites

- **Android Studio** or **Android SDK** installed
- **Android emulator** or **physical Android device**
- **Git** for version control

## 🏃‍♂️ Running the App

### Option 1: Using Commands (Recommended)

```bash
# 1. Navigate to project directory
cd /path/to/SolanaMobileApp

# 2. Check if emulator is running
adb devices

# 3. If no devices, start emulator
emulator -avd <your_avd_name> &
# Example: emulator -avd Medium_Phone_API_36.0 &

# 4. Build the app
cd android && ./gradlew assembleDebug

# 5. Install and launch
adb install -r ./app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.anonymous.SolanaMobileApp/.MainActivity
```

### Option 2: Using Android Studio

1. **Open Android Studio**
2. **Open Project** → Select `android` folder
3. **Wait for sync** to complete
4. **Click Run** ▶️ button
5. **Select emulator** or connected device

## 📱 App Features

### ✨ What You'll See:
- **🚀 Solana Mobile Discover** header
- **Swipeable token cards** with:
  - Token name (MOON, DOGE 2.0, SOL CAT, etc.)
  - Creator (@username)
  - Description with emojis
  - Like count ❤️
- **Bottom navigation tabs**:
  - Discover | Superstar | Rankings | Activities | Profile

### 🎮 How to Use:
- **Swipe RIGHT** → ❤️ Like token (green overlay)
- **Swipe LEFT** → 👎 Pass token (red overlay)  
- **Tap bottom tabs** → Navigate between sections
- **Cards auto-replenish** from token pool

## 🔧 Troubleshooting

### Emulator Not Starting?
```bash
# List available AVDs
emulator -list-avds

# Start specific AVD
emulator -avd <avd_name> -dns-server 8.8.8.8 &

# Check connection
adb devices
```

### Build Errors?
```bash
# Clean build
cd android && ./gradlew clean

# Rebuild
./gradlew assembleDebug
```

### App Not Installing?
```bash
# Uninstall old version
adb uninstall com.anonymous.SolanaMobileApp

# Reinstall
adb install -r ./app/build/outputs/apk/debug/app-debug.apk
```

### Check App Logs:
```bash
# View app logs
adb logcat | grep SolanaMobileApp

# Clear logs first
adb logcat -c
```

## 🌐 Git Branch

Currently on: **`swipe`** branch
- Contains all swipe functionality
- Native Kotlin implementation
- No React Native dependencies

## 📂 Project Structure

```
SolanaMobileApp/
├── android/                 # Main Android project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/anonymous/SolanaMobileApp/
│   │   │   │   ├── MainActivity.kt          # Main activity with tabs
│   │   │   │   ├── SwipeableTokenCard.kt    # Swipe card component
│   │   │   │   ├── TokenData.kt             # Data model
│   │   │   │   └── TokenAdapter.kt          # List adapter
│   │   │   └── res/
│   │   │       ├── layout/                  # UI layouts
│   │   │       ├── drawable/                # Icons & graphics
│   │   │       └── menu/                    # Navigation menu
│   │   └── build.gradle                     # App dependencies
│   ├── build.gradle                         # Project config
│   └── settings.gradle                      # Project settings
└── HOW_TO_RUN.md                           # This file
```

## 🎯 Key Components

- **MainActivity.kt**: Main app logic with bottom navigation
- **SwipeableTokenCard.kt**: Custom swipeable card with gestures
- **TokenData.kt**: Data model for tokens
- **Bottom Navigation**: 5 tabs (Discover active)
- **Swipe Gestures**: Left/right with smooth animations

## 💡 Tips

- **Cards stack 3 deep** for smooth UX
- **Swipe threshold**: ~300px for action
- **Auto-rotation** based on swipe distance
- **Toast notifications** confirm actions
- **Cards replenish** automatically

## 🐛 Known Issues

- Currently shows placeholder token data
- Other tabs show toast messages (not implemented)
- Image placeholders (no actual images loaded)

## 🚀 Next Steps

- Connect to Solana blockchain
- Add real token data from API
- Implement other tab functionalities
- Add user authentication
- Connect wallet integration

---

**Happy Swiping! 🎉**