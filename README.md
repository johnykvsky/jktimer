# JK Timer

A fast, clean, and fully customizable interval timer for Android — designed for **HIIT**, **Tabata**, **circuit training**, **calisthenics**, **boxing**, and all structured workout routines.

Built with **100% Kotlin**, **Jetpack Compose**, and **Material 3**. Fully open-source and privacy-focused with **zero internet permissions**.

---

## ✨ Features

### 🏋️ 1. Simple & Advanced Training Modes
- **Simple Training:** Quickly configure workout duration, rest duration, and number of sets with rapid-adjustment increment chips (`+5s`, `+10s`, `+30s`).
- **Advanced Training:** Construct custom multi-phase routines with up to **99 steps** in any sequence (e.g. `15s workout` → `20s workout` → `15s rest` → `60s workout`). Add custom step titles (e.g. *"Pushups"*, *"Plank"*, *"Catch Breath"*), reorder, duplicate, and delete steps on the fly.

### 🎨 2. Smart Visual Feedback & Dynamic Colors
- **High-Visibility Countdown Ring:** Large, glanceable animated progress arc.
- **Alternating Workout Colors:** Automatically switches between energizing **Orange** and **Electric Blue** when consecutive workout intervals run back-to-back without a rest interval.
- **"Next Up" Badge:** Displays the upcoming exercise name and duration in advance.
- **Total Remaining Time:** Optional banner showing overall remaining workout duration.
- **Round Stepper:** Glanceable progress pills displaying completed and upcoming rounds.
- **Landscape Mode:** Adaptive 2-column layout optimized for tablets and widescreen phones.

### 🔊 3. Audio & Haptic Alerts
- Distinct sound cues for countdown ticks (`3, 2, 1`), 10-second warning, interval transitions, and workout completion.
- In-app **beep volume slider** (10% to 100%).
- Optional **haptic vibration feedback**.
- Quick mute / unmute button in the top app bar.

### 📱 4. Background Timer & Lock-Screen Support
- Android Foreground Service with live notification updates keeps your workout active even when your phone is locked or you switch to music apps.
- Notification controls to pause, resume, and stop routines.

### 🌐 5. Bilingual & Theme Support
- Full **English** and **Polish** (*Polski*) localization with automatic system language detection and an in-app language switcher.
- **Dark, Light, and System** theme modes.
- Configurable preparation delay countdown (`0s`, `3s`, `5s`, `10s`).

### 🔒 6. 100% Private & Offline
- **No Internet Permission:** Does not request `android.permission.INTERNET`.
- **Zero Trackers:** No analytics, telemetry, or crash reporters.
- **No Ads:** Completely free and open-source.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM with `StateFlow` and Coroutines
- **Storage:** Local SharedPreferences with JSON serialization
- **Audio & Haptics:** Android `SoundPool` and `Vibrator` / `VibratorManager`
- **Min SDK:** 26 (Android 8.0 Oreo)
- **Target SDK:** 37 (Android 15+)

---

## 🚀 Building from Source

### Prerequisites
- Android Studio Ladybug (or newer) / IntelliJ IDEA
- JDK 17+ (or JDK 21+)
- Android SDK (API 35+)

### Commands

```bash
# Clone the repository
git clone https://github.com/johnykvsky/jktimer.git
cd jktimer

# Run Unit Tests
./gradlew testDebugUnitTest

# Build Debug APK
./gradlew assembleDebug

# Build Release Android App Bundle (.aab)
./gradlew bundleRelease
```

The debug APK will be located at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🎵 Sound Assets

All bundled sound effects in `app/src/main/res/raw/` are licensed under **Creative Commons 0 (CC0 Public Domain)**:
- `countdown_beep.ogg` — OpenGameArt *"8bit Menu Select"*
- `ten_second_warning.wav` — OpenGameArt *"Chiptune SFX Pack"*
- `workout_interval_end.ogg` — OpenGameArt *"16bit Success sound"*
- `training_end.ogg` — OpenGameArt *"New thing get!"*

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

