> Historical report. Current status: see RELEASE_CANDIDATE_REPORT.md. This file is not final acceptance evidence.

# Quality assurance report — CCNA Academy 1.4

## v1.4 — what was executed in the authoring environment
A Kotlin 2.x JVM compiler with the kotlinx.serialization plugin was available, so every **non-Android** source file
(`data/*` except the three Android-bound repositories, and all of `lab/*`) was compiled and the unit tests were **run**:

| Suite | Tests | Result |
|---|---|---|
| `LabEngineTest` (new) | 19 | Pass |
| `LabContentTest` (new) | 9 | Pass |
| `LogicTest` (existing) | 11 of 12 run | Pass — `offlineTutorAnswersFromContent` not run locally because `Tutor.kt` imports Android classes; CI runs it |
| `ContentTest` (existing) | 5 | Pass |

The local runner used a minimal JUnit-compatible shim (assertions + `@Test`), not JUnit itself; CI runs the real JUnit 4.

What the new tests cover: modes/prompts/abbreviations, IOS-style errors (caret, incomplete, ambiguous) and that invalid input never
changes state, unsupported commands, `?`/Tab, full solutions **and alternative valid solutions** for every lab, both troubleshooting
faults (diagnosis text names the failing device/port), DTP outcome matrix, native-VLAN leakage, reload semantics (unsaved changes
lost, vlan.dat kept), router-on-a-stick (encapsulation-before-IP, duplicate VID), static routing return path, longest-prefix match
and AD, IP/VLAN-list/interface helpers, `show` output formats, LabState and ProgressState serialization, v1.3 → v1.4 migration,
animation integrity (edges, nodes, table operations, highlighted rows), canvas layout bounds/overlap, lesson extras validity,
computed subnet table, that every annotated output line actually appears in the trainer's output, and that every one of the
41 lessons has at least one v1.4 activity.

## v1.4 — what was NOT executed
* **The Compose UI was not compiled.** The Android SDK, Compose and AndroidX libraries are not available here. UI files
  (`ui/lab/*.kt` and the edited screens) were parsed by the Kotlin compiler with **no syntax errors**, but symbol resolution
  and type checking against Compose happen only in CI / Android Studio.
* No APK was built and nothing was run on a device or emulator. No installable build is delivered.
* The GitHub Actions workflow (`.github/workflows/android.yml`) builds debug/release APKs and runs all unit tests with real JUnit.

## v1.4 manual device checklist (phone + tablet, light + dark, large font, TalkBack)
1. Learn → lesson 2-01: learning-cycle strip, Compare tables (tap row, Hide answers, Quiz me), VLAN animation (play/pause/step/speed,
   static view, full screen zoom), command walkthrough, lab card, output activity, teach-back.
2. Settings → Reduce motion on → animation steps change without moving packets; system "Remove animations" has the same effect.
3. Labs hub → "VLANs and an 802.1Q trunk": solve on SW1 and SW2 using abbreviations, `?`, Tab chip, ↑ history; Topology tab shows
   trunk/VLAN labels and the reachability tests; Verify tab output; Review tab diff. Rotate the device mid-lab.
4. Background the app during a lab and during an animation → reopen: lab resumes with transcript; animation is paused.
5. Troubleshooting labs: confirm the trace explains the fault before and the success after the fix.
6. Challenge: no hints, no live checks; "Check my work" grades; Progress shows independent mastery separately.
7. Mistake notebook lists rejected commands; Retry and Understood work.
8. Settings → backup → reset → restore: saved labs and skills return.
9. Airplane mode: every v1.4 feature works offline.

---

# Previous release (1.3)

## What was verified automatically (in the authoring environment)
| Check | Result |
|---|---|
| Content pipeline QA gates (`build.py`, `build_memory.py`, `qbank/build_bank.py`, `build_study.py`) | Pass — 41 lessons, 29 diagrams, 241 flashcards, 236 questions, 137 glossary terms, 15 comparisons, 13 mistakes |
| Question bank: valid answer indices, "Choose N" counts, a rationale for every wrong option, no near-duplicate questions, no answer guessable by option length | Pass |
| Calculation answers recomputed with Python `ipaddress`/`json` | 22 of 22 match |
| Subnet generator logic cross-checked against Python `ipaddress` (20,000 random cases) | 0 mismatches |
| Diagram layouts rendered to images and inspected; no nodes outside canvas | Pass |
| Every navigation target string matched against the 41 registered routes | Pass |
| Every `ProgressState`/ViewModel/bank/library member referenced by screens exists | Pass |
| Bracket balance of every Kotlin file | Pass |
| WCAG contrast of the theme's text/background pairs (light and dark) | All ≥ 4.5:1 |
| Duplicate stems / ids / glossary terms; every lesson covered by a planner theme | None / Pass |

## What could NOT be done here
The authoring environment has no Android SDK, Kotlin compiler or internet access, so the app was **not compiled, run, or tested on a device
or emulator**, and no APK could be produced. To close that gap:

* `app/src/test/…` contains JVM unit tests that load the real assets through the app's own serializers (catches startup crashes from
  JSON/model mismatches) and test grading of all question types, exam distribution, scoring, generators, the SRS scheduler, the planner,
  global search, backward-compatible serialization, milestones (no fake stats), the offline tutor, and daily-challenge stability.
* `.github/workflows/android.yml` compiles debug + release APKs and runs those tests on every push.

## Manual device checklist (run once on a phone and a tablet, light and dark mode)
1. First launch → dashboard loads; turn on airplane mode → every tab still works.
2. Learn: open a lesson, long-press text to highlight, bookmark the lesson and a diagram, add a note, finish the knowledge check, mark complete.
3. Flashcards: due session → flip → Again/Hard/Good/Easy; favorites, custom deck, browser search.
4. Practice: Quick (10), submit each question type (single, multi, true/false, matching, ordering); bookmark; Review incorrect; Timed mode countdown.
5. Exam simulator: 20 questions / 30 min → flag, navigate, review screen, submit confirmation; let a short exam time out; check results, domain bars, trend chart.
6. Revise: daily plan tasks, daily challenge (all 5 parts, streak), memorization guide recall, cheatsheet search/mark/PDF share, speed drill, subnetting challenge.
7. Planner: create plan (date picker, study days, minutes, level, target) → weekly schedule → tasks open the right screens.
8. Tutor offline: explain, level, compare, subnet "10.45.130.77/19", summarize lesson, revision notes → save to notes. Optionally configure an AI endpoint.
9. Search "OSPF", "161", "native" → grouped results open correctly. Glossary A–Z index and search.
10. Settings: backup → reset → restore; theme switch; force-stop and reopen → progress persists.
11. Rotate the device on every screen; on a tablet confirm the navigation rail and two-pane Learn/Glossary layouts.

## Scope exclusions (1.3 — lifted in 1.4)
1.3 deliberately excluded any CLI or lab. 1.4 adds a limited educational CLI trainer and labs by explicit request; it is still not a
Packet Tracer replacement, IOS emulator or topology builder.
