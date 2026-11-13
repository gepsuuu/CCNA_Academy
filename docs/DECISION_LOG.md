# Decision log (rc2)
- D-01 Input `.txt` had ZIP signature (PK); extracted as ZIP.
- D-02 Android/Gradle/Maven/GitHub return 403; no APK possible. Did not fabricate builds or signing.
- D-03 Mechanism diagrams re-keyed by title semantics; semantic test (test_alignment.py) added, verified failing on rc1 assets (negative control).
- D-04 Used Kotlin 2.3.10 compiler + serialization plugin from PyPI `kotlin-jupyter-kernel` to compile/run Android-free code on JVM. Recorded as partial, not as Android build.
- D-05 Moved `lessonSectionText` to data/HighlightText.kt and `OfflineTutor` to data/OfflineTutor.kt (no behavior change) for JVM testability.
- D-06 Diagram text alternative + zoom buttons added (uncompiled Compose).
- D-07 Corrected InteractiveTeachingTest fixture: completedLessons is Map<String,Long>; rc1 fixture used array.
- D-08 versionCode 7 / 1.5.0-rc2; applicationId unchanged; no keystore available -> release APK must be signed by owner with original key.
- D-09 Per user instruction, remaining runtime gates skipped and left blocked, not passed.
