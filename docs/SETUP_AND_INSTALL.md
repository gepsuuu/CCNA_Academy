# Build, install and release — CCNA Academy 1.4

## Requirements
| Item | Version |
|---|---|
| Android Studio | Ladybug (2024.2) or newer |
| JDK | 17 (bundled with Android Studio) |
| Gradle / Android Gradle Plugin / Kotlin | 8.11.1 / 8.7.3 / 2.1.0 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 26 (Android 8.0+) |

Dependencies (all from Google Maven / Maven Central): Compose BOM 2024.12.01 (Material 3, icons-extended), Activity Compose 1.9.3,
Navigation Compose 2.8.5, Lifecycle 2.8.7, DataStore Preferences 1.1.1, kotlinx-serialization-json 1.7.3, Core KTX 1.15.0, JUnit 4.13.2 (tests).
No third-party SDKs, analytics or ads.

## Option A — Build an APK without installing anything (GitHub Actions)
1. Create a GitHub repository and push this folder (`git init && git add . && git commit -m "CCNA Academy" && git push`).
2. Open the repository's **Actions** tab → **Build Android APKs** runs automatically on every push (or press *Run workflow*).
3. When it finishes, download **ccna-academy-apks** from the run's *Artifacts*: it contains `app-debug.apk` and `app-release.apk`.
4. The **unit-test-report** artifact shows the content-integrity and logic tests.

## Option B — Android Studio
1. *File → Open* → select the `CCNAAcademy` folder. Let Gradle sync (it downloads Gradle and dependencies the first time).
2. Run ▶ on a device or emulator, or *Build → Build App Bundle(s)/APK(s) → Build APK(s)*.
   Output: `app/build/outputs/apk/debug/app-debug.apk`.
3. Command line: this project does not include the Gradle wrapper JAR. Run `gradle wrapper --gradle-version 8.11.1` once (or let Android
   Studio create it), then `./gradlew assembleDebug`, `./gradlew assembleRelease` and `./gradlew testDebugUnitTest`.

## Release signing
Without configuration, `assembleRelease` signs with the debug key — fine for sideloading and testing, **not** for Google Play.
For a real release, create a keystore (`keytool -genkeypair -v -keystore ccna.jks -keyalg RSA -keysize 2048 -validity 10000 -alias ccna`) and set:
`CCNA_KEYSTORE=/path/ccna.jks`, `CCNA_KEYSTORE_PASSWORD`, `CCNA_KEY_ALIAS`, `CCNA_KEY_PASSWORD` before building
(in GitHub Actions, store them as repository secrets and expose them as env variables in the build step). Never commit the keystore.

## Installing the APK on an Android phone or tablet
1. Copy `app-debug.apk` (or `app-release.apk`) to the device (USB, Google Drive, email, etc.).
2. Open it from the Files app. Android asks to allow installs from that source: *Settings → Apps → Special app access → Install unknown apps* → allow for your Files/Drive app.
3. Tap **Install**. If an older CCNA Academy build signed with a *different* key is installed, uninstall it first (back up your progress in *Settings → Backup & restore* before doing so).
4. With a computer: enable *Developer options → USB debugging* and run `adb install -r app-debug.apk`.

## Regenerating content
```
cd content-source
python build.py ../app/src/main/assets            # lessons, diagrams
python build_memory.py ../app/src/main/assets     # flashcards, memorization guides, cheatsheets, techniques
python qbank/build_bank.py ../app/src/main/assets # question bank (fails on any QA error)
python build_study.py ../app/src/main/assets      # comparisons, common mistakes, glossary
python qbank/export_review.py                     # docs/question-bank-review.md
```

## 1.4 notes
* No new dependencies were added: the CLI trainer, animations and tables use the existing Compose, Canvas and kotlinx.serialization stack.
* The 1.4 Compose UI has not been compiled in the authoring environment. Build once in Android Studio (or let the GitHub Actions
  workflow run) and fix any reported compile errors before distributing. The non-UI trainer logic and content were compiled and tested.
* Upgrading over 1.3 keeps all progress: new fields in `ProgressState` have defaults.
