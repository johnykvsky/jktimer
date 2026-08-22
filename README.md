# JK Timer

A fast, clean, and fully customizable interval timer for Android — designed for **HIIT**, **Tabata**, **circuit training**, **calisthenics**, **boxing**, and all structured workout routines.

Built with **100% Kotlin**, **Jetpack Compose**, and **Material 3**. Fully open-source and privacy-focused with **zero internet permissions**.

---

## ✨ Features

### 🏋️ 1. Quick, Simple & Advanced Training Modes
- **Quick Workout:** Instant ad-hoc interval timer without needing to save a preset.
- **Simple Training:** Easy-to-use routine configuration for workout duration, rest duration, and number of sets using stepper controls.
- **Advanced Training:** Multi-phase routine builder supporting up to **99 steps** in any custom sequence (e.g. `15s workout` → `20s workout` → `15s rest` → `60s workout`). Add custom step titles (e.g. *"Pushups"*, *"Plank"*, *"Catch Breath"*), reorder, duplicate, and delete steps on the fly.
- **Shareable Routine Summary:** Tap the **Simple** or **Advanced** badge on any saved routine card to open a formatted summary modal containing the routine title, total workout duration, and complete parameters (intervals & sets for Simple, numbered multi-line sequence with step names for Advanced), complete with a one-tap button to copy to clipboard for sharing with friends.
- **Saved Routines:** Store and manage custom workout routines with one-tap start, edit, duplicate, and delete.

### ⏱️ 2. Configurable Time Adjustment Interval
- **Custom Time Step:** Set your preferred time interval in Settings from **1s to 99s** (default: `5s`).
- **Unified Steppers:** The configured time interval is automatically used whenever you tap `-` or `+` on time fields across Quick, Simple, and Advanced trainings.

### 🎨 3. Smart Visual Feedback & Dynamic Colors
- **High-Visibility Countdown Ring:** Large, glanceable animated progress arc.
- **Alternating Workout Colors:** Automatically switches between energizing **Warm Orange** and **Electric Blue** when consecutive workout intervals run back-to-back without a rest interval.
- **"Next Up" Badge:** Displays the upcoming exercise name and duration in advance.
- **Total Remaining Time:** Optional countdown badge showing overall remaining workout duration.
- **Round Stepper:** Glanceable progress pills displaying completed and upcoming rounds.
- **Landscape Mode:** Adaptive 2-column layout optimized for tablets and widescreen phones.

### 🔊 4. Audio & Haptic Alerts
- Distinct sound cues for countdown ticks (`3, 2, 1`), 10-second warning, interval transitions, and workout completion.
- In-app **beep volume slider** (10% to 100%) and sound test button.
- Optional **haptic vibration feedback** with vibration test button.
- Quick mute / unmute button in the top app bar.

### 📱 5. Background Timer & Lock-Screen Support
- Android Foreground Service with live notification updates keeps your workout active even when your phone is locked or you switch to music apps.
- Notification controls to pause, resume, and stop routines.

### 🌐 6. Personalization & Settings
- Full **English** and **Polish** (*Polski*) localization with automatic system language detection and an in-app language switcher.
- **Dark, Light, and System** theme modes.
- Configurable **Preparation Delay** countdown (`0s`, `3s`, `5s`, `10s`, `15s`).
- Configurable **Time Interval** step (`1s` to `99s`).
- Toggle for displaying total remaining workout duration.

### 🔒 7. 100% Private & Offline
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

