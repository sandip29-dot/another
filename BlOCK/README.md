# BlOCK for Android

> **"Brick by brick."**
> 
> *The nightclub bouncer for your phone. Stops short-video scrolling cold across every app. Detect short feed → send you away → count the attempt.*

BlOCK is the native Android companion to the BlOCK Chrome extension. It is engineered with a single ruthless objective: **permanently eradicate short-form video doom-scrolling** (Instagram Reels, YouTube Shorts, TikTok, Facebook Reels, Snapchat Spotlight, and browser URLs) from your life.

100% offline. Zero network permissions. No analytics. No accounts. 

---

## 🏛️ Architecture & Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.1.0 |
| **UI Framework** | Jetpack Compose + Material 3 (Dark-First Neo-Brutalist design system) |
| **Architecture** | MVVM (Model-View-ViewModel) with unidirectional data flow (UDF) |
| **Local Database** | Room 2.6.1 (`BlockDatabase`, `BlockAttemptDao`) |
| **Settings / State** | Jetpack DataStore Preferences (`SettingsDataStore`, `CommitmentLockManager`) |
| **Dependency Injection** | Hilt 2.53.1 (`@HiltAndroidApp`, `@HiltViewModel`, `EntryPointAccessors`) |
| **Core Interception** | `AccessibilityService` (`GLOBAL_ACTION_HOME`, window & node inspection) |
| **Overlay Engine** | WindowManager `TYPE_ACCESSIBILITY_OVERLAY` with custom physics shatter engine |
| **Watchdog** | WorkManager 2.10.0 + Boot Receiver (`WatchdogWorker`, `ServiceMonitor`) |
| **Home-Screen Widget** | Jetpack Glance 1.1.1 (`BlockWidget`, `BlockWidgetReceiver`) |
| **Target SDK** | minSdk 26 (Android 8.0), compileSdk & targetSdk 35 (Android 15) |

---

## 🎨 Design Language: "Brick by Brick" (Neo-Brutalism)

* **Palette**: Void Black (`#000000`), Bone Off-White (`#F5F0E8`), High-Voltage Electric Lime (`#CCFF00`), and Danger Red (`#FF3333`).
* **Geometry**: Hard right angles, thick 2–4dp outlines, solid unblurred offset drop shadows (`4dp, 4dp`).
* **The Voice**: A deadpan nightclub bouncer guarding the door to your attention:
  * *"Not tonight."*
  * *"Nope."*
  * *"Future you says no."*
  * *"Door's closed."*
  * *"Turn around."*
* **The Slam (Signature Moment)**: The instant a short feed appears, you are ejected to the home screen. A full-screen overlay slams a giant tilted `BLOCKED` stamp over the display (~400ms) with a heavy haptic thud, then the screen shatters into a grid of falling blocks that tumble off-screen.
* **The Tower**: Every intercepted scroll drops a physical block onto your persistent tower. Consecutive streak days add glowing golden foundation levels; a day where the shield was disabled shows fracture cracks.
* **Time Reclaimed**: Interceptions × ~35 seconds, translated into real human achievements (*"14 chapters read"*, *"3 lectures watched"*, *"45 push-ups completed"*).

---

## ⚡ Detection Engine & Rules

The detection engine uses a **2-signal threshold** and **debouncing** to guarantee that normal app navigation (main feed, DMs, profiles, long YouTube videos) is never interrupted:

1. **Signals**:
   * View ID (e.g. `clips_viewer_view_pager`, `reel_recycler`)
   * Class Name (e.g. `androidx.viewpager2.widget.ViewPager2`, `TextureView`)
   * Content Description regex (e.g. `(?i).*reel.*`, `(?i).*shorts.*`)
2. **Debounce Gate**:
   * 2,000ms cooldown per app package.
   * 1,000ms global cooldown to prevent re-trigger loops during home-launcher transitions.
3. **Editable `rules.json`**:
   * Shipped in `assets/rules.json` and mirrored to `filesDir/rules.json` on first launch.
   * Modifiable without app updates.
4. **Learn Mode**:
   * Inspects the live accessibility node tree of **any** newly released app.
   * Extracts candidate IDs, class names, and content descriptions.
   * Allows one-tap custom rule generation.

---

## 🔒 Permanent Friction: The Commitment Lock

Android technically allows users to turn off an Accessibility Service at will. BlOCK fights impulsive disabling with intentional friction:

* **Commitment Lock**: Set a guard for **1 Day**, **7 Days**, or **30 Days**.
* **The Disable Challenge**:
  1. **Past-Self Note**: Read the note you wrote to yourself when you were clear-headed.
  2. **10-Minute Cooldown**: A mandatory countdown timer. No bypassing.
  3. **Verbatim Typing Challenge**: You must type:
     > *"I am choosing to remove the guard I set up for myself, and I accept full responsibility for the time I am about to lose."*
* **Watchdog Service**: Periodic WorkManager worker monitors if the service was switched off and fires a high-priority alert.
* **Optional Device Admin**: Blocks direct app uninstallation until explicitly deactivated in Android Security settings.

---

## 🚀 How to Build & Install

### Requirements
* Android Studio Ladybug (2024.2+) or newer
* Android SDK 35 (Android 15) installed
* JDK 17
* Physical Android phone (Android 8.0 to Android 15)

### Build via Android Studio
1. Launch Android Studio.
2. Select **Open** and navigate to `c:\Python\Anti-Gravity\BlOCK`.
3. Allow Gradle to sync dependencies.
4. Connect your Android phone via USB with **USB Debugging** enabled in Developer Options.
5. Click **Run 'app'** (`Shift + F10`) or select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

### Build via Command Line
```bash
cd c:\Python\Anti-Gravity\BlOCK

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Install directly to connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 Stage-by-Stage Real-Device Testing Plan

Follow these 6 stages to test and verify BlOCK on your physical device:

### Stage 1: Engine + Redirect
1. Install and launch BlOCK.
2. Complete the onboarding screens and tap **ENABLE ACCESSIBILITY**.
3. Under Android **Accessibility Settings**, locate **BlOCK** and switch it **ON**.
   * *Note for Android 13+ sideloaded APKs*: If the toggle is greyed out with "Restricted setting", go to **Phone Settings → Apps → BlOCK → tap 3-dots top right → Allow restricted settings**.
4. Open **Instagram** and tap on a Reel, or open **YouTube** and tap on Shorts.
5. **Expected result**: You are immediately ejected to the Home screen in under 150ms. You cannot view a second reel.
6. Verify normal feeds, DMs, and standard horizontal YouTube videos continue working untouched.

### Stage 2: Counter + Room Persistence
1. Return to BlOCK.
2. Open the **Dashboard** and **Stats** tabs.
3. **Expected result**:
   * The "TODAY" counter increments by 1 for each blocked attempt.
   * Total count increases.
   * Under **Stats**, the app shows up in the "BY APP" breakdown with its exact count.
4. Kill the BlOCK app process completely from recent apps and reopen it.
5. **Expected result**: All counts, history, and streak data remain intact (persisted in SQLite Room database `block_db`).

### Stage 3: Design + The Slam
1. Trigger a block by opening a short video.
2. **Expected result**:
   * Immediate haptic thud (`VibrationEffect.EFFECT_HEAVY_CLICK`).
   * Giant Electric Lime `BLOCKED` stamp slams onto the screen tilted at -12°.
   * Deadpan bouncer quote displays below the stamp (e.g. *"Not tonight."*).
   * After 400ms, the screen breaks into an 8×16 grid of falling blocks that tumble off screen.
3. Test **Reduce Motion**:
   * Enable "Remove animations" in Android Accessibility settings.
   * Trigger a block: BlOCK instantly shows the static stamp without physics particles, respecting user accessibility preferences.

### Stage 4: Tower + Stats Visualization
1. Block multiple reels across different hours of the day.
2. Open **Dashboard**:
   * Inspect **The Tower**: Canvas displays individual blocks stacked brick-by-brick.
   * Every 5 blocks / streak day displays a glowing golden reinforcement floor.
   * Time Reclaimed displays tangible units (e.g. *"14 chapters read"* or *"45 push-ups completed"*).
3. Open **Stats**:
   * Heatmap Calendar displays 90-day activity squares with color intensity based on attempts.
   * Danger Hours chart displays 24 bars, with the highest scrolling danger hour highlighted in Red.

### Stage 5: Commitment Lock
1. In BlOCK, navigate to the **Lock** tab.
2. Select **1 DAY** and type a personal note: *"I need to study for finals."*
3. Tap **LOCK IT IN**.
4. Status changes to **LOCKED IN** with countdown.
5. Tap **I WANT TO DISABLE...**:
   * Modal opens showing your past-self note.
   * 10-minute countdown timer starts. The disable button is disabled until 00:00.
   * Try typing the deterrent sentence. Notice live character-by-character validation.

### Stage 6: Widget & Background Watchdog
1. Long-press your phone's home screen and select **Widgets**.
2. Find **BlOCK** and drag the neo-brutalist widget to your screen.
3. **Expected result**: Displays your tower height, today's blocks, and streak days. Tapping it opens the app.
4. Test **Learn Mode**:
   * In BlOCK → Shield → **LEARN MODE**.
   * Tap **START CAPTURE**.
   * Open any app with video feeds (e.g. Reddit or Threads).
   * Return to BlOCK: see the captured node tree and save a new custom rule.
5. Test **Watchdog**:
   * Manually disable BlOCK's Accessibility Service in Settings.
   * WorkManager periodic worker detects the shield is down and fires a warning notification to re-enable protection.

---

## 🛠️ Adding Custom Rules

### Method 1: Using In-App Learn Mode (Recommended)
1. Navigate to **Shield → LEARN MODE**.
2. Tap **START CAPTURE**.
3. Open the target app and navigate to the short video feed.
4. Return to BlOCK. Select at least 2 detected signals (View ID, Class Name, or Content Description).
5. Name the app and tap **SAVE RULE**.

### Method 2: Editing `rules.json` Directly
Detection rules follow this JSON schema:
```json
{
  "id": "my_custom_app",
  "appPackage": "com.example.app",
  "appName": "My App",
  "enabled": true,
  "minSignals": 2,
  "signals": [
    {
      "type": "viewId",
      "pattern": "video_recycler_pager"
    },
    {
      "type": "className",
      "pattern": "androidx.viewpager2.widget.ViewPager2"
    },
    {
      "type": "contentDescription",
      "pattern": "(?i).*reels.*"
    }
  ]
}
```

---

## 🛡️ Privacy Guarantee

BlOCK contains **NO** `android.permission.INTERNET` in its manifest. It cannot send or receive data over Wi-Fi or cellular connections. It cannot make network calls. Your usage data, block attempts, and habits are physically incapable of leaving your device.
