# CCNA Academy 1.5.0-rc1

**Source release candidate, not a verified APK or an accepted seven-phase release.**

Start with [the current delivery report](docs/RELEASE_CANDIDATE_REPORT.md), [system design](docs/SYSTEM_DESIGN.md), and [acceptance register](docs/release-acceptance.json). All 41 lessons have staged source, a visual reference and a practice check. The package adds packet/OSPF walkthroughs, output inspectors, configuration extensions, mobile layout changes, and preservation fixes. 62 Python tests passed; Android/Kotlin/device tests were not run.

No APK is included. Gradle/Android tools are unavailable in the delivery environment and their downloads were policy-blocked. The included CI workflow was edited but not executed remotely. Do not treat source existence or passing Python tests as Android acceptance.

## Reproducible checks
- Python 3.10+ with the standard library: `python scripts/validate_content.py`.
- Android build environment: JDK 17, Gradle 8.11.1, Android SDK platform/build tools required by app/build.gradle.kts, and dependency access.
- Candidate build: `sh scripts/build_candidate.sh` or the supplied GitHub Actions workflow.
- Final acceptance check: `python scripts/acceptance_gate.py`. It intentionally exits 2 until evidence and all lesson acceptances are complete.

Keep the same application ID/signing key for an in-place upgrade and export a backup before device testing. Do not uninstall a working app to bypass a signing mismatch without preserving its data.

---

## Historical project documentation

# Current delivery: all-lesson content migration draft

All 41 lessons have progressive stages, including the 39 migrated in this delivery. Version 1.5.0-rc2. Android build and device acceptance are pending (toolchain unavailable). No APK is included. See docs/RELEASE_CANDIDATE_REPORT.md.

Read docs/ALL_LESSONS_MIGRATION_REPORT.md first for scope, validation, remaining work, and build instructions. The earlier pilot report is historical.

---

# CCNA Academy — Android (Kotlin + Jetpack Compose)

A complete offline CCNA 200-301 learning app: curriculum, interactive lessons, labeled teaching diagrams,
knowledge checks, practice questions, spaced-repetition flashcards, timed exam simulation, and progress analytics.
Since v1.4 it also includes a limited, offline, stateful **CLI trainer** with guided, troubleshooting and challenge labs, step-through
**animations**, and **interactive explanatory tables** inside the lessons. The trainer models a documented subset of IOS
(`docs/CLI_TRAINER.md`); it is not a full IOS emulator or a Packet Tracer replacement.

## Build & run
1. Open the `CCNAAcademy` folder in **Android Studio (Ladybug 2024.2 or newer)**.
2. Let Gradle sync (uses Gradle 8.11.1 / AGP 8.7.3 / Kotlin 2.1.0 — Android Studio downloads them).
   If you want to use `./gradlew` from a terminal, run `gradle wrapper` once (or let Android Studio generate it).
3. Run on a device/emulator (minSdk 26 / Android 8.0, targetSdk 35).
4. Release APK/AAB: *Build > Generate Signed App Bundle / APK* (replace the debug signing config in `app/build.gradle.kts`).

## Features
| Area | What it does |
|---|---|
| Dashboard | Continue-learning, overall progress ring, blueprint-weighted exam readiness, day streak, today/week/total study time + 7-day chart, completed chapters, weak topics, upcoming revisions, quick access to flashcards/practice/exam, daily tip |
| Learn | 6 domains → 22 chapters → 41 lessons. Tablet/landscape uses a list-detail layout |
| Lesson | 10-part structure: simple → in depth (Beginner / Intermediate / CCNA exam level) → diagrams → why it matters → examples → common mistakes → terminology → **must memorize** → exam notes → summary → knowledge check. "Beyond the exam" boxes mark supplementary content |
| Diagrams | 25 vector diagrams with labels, legends and explanations; tap for full-screen pinch-zoom; adapt to light/dark |
| Practice | Filter by domain and focus (mixed, unanswered, previously missed, weak topics), instant feedback and review |
| Exam simulation | 40 questions weighted by official domain weights, 48-minute timer, flag/overview grid, per-domain scoring, answer review |
| Flashcards | 316 cards (terms + must-memorize facts), Leitner spaced repetition (1/2/4/8/16 days), flip animation |
| Progress | Readiness per domain, strengths/needs work, topics requiring revision, answer stats, exam history chart, 14-day study history |
| Settings | System/Light/Dark, Material You colors (Android 12+), offline status, reset |

## Interactive lessons, CLI labs and animations (v1.4)
| Area | What it does |
|---|---|
| Learning cycle | Each lesson shows which stages it offers: Understand → Compare → Visualize → Observe → Configure → Verify → Troubleshoot → Recall |
| Explanatory tables | Tables inside lessons (access vs trunk, DTP outcomes, ports and IP protocol numbers, prefix sizes, IPv6 types, AD, route fields, OSPF neighbor requirements, NAT terms, wildcards, L2 security, Wi-Fi security, syslog levels, REST…). Tap a row for its explanation, hide answers for recall, "Quiz me", filter, stacked cards on phones |
| Animations | 12 step-through concept demonstrations with play/pause/step/replay/speed, captions, tables that update per step, static step list, full-screen zoom, reduced motion |
| Command walkthroughs | Command → mode → purpose → verification → common mistake → undo |
| Interpret real output | Output generated live by the trainer, with tappable annotations and a question |
| CLI labs | 7 labs: CLI fundamentals, VLANs + trunk, two troubleshooting labs, an independent challenge, router-on-a-stick, static/default routing. Tasks · Topology · CLI · Verify · Review; progressive hints; save/resume; config diff |
| Topology | Derived from the lab configuration: trunks, access VLANs, down links, device inspector, two-way reachability tests with hop-by-hop reasons |
| Mistake notebook | Every rejected command with the IOS error and the trainer's explanation; retry and mark understood |
| Skills | Concepts, CLI syntax, output interpretation and troubleshooting tracked separately; guided completion vs independent mastery |

See `CHANGELOG.md`, `docs/CLI_TRAINER.md` (supported commands and limits) and `docs/LESSON_COVERAGE.md` (per-lesson matrix).

## Revision system (v1.1 — Prompt 2)
| Area | What it does |
|---|---|
| Revise tab | Hub for today's plan, flashcards, Memorization Center, cheatsheets, speed techniques, subnetting center, daily challenge |
| Flashcards | 241 rich cards in 16 categories (front: term / question / problem / diagram; back: answer, explanation, example, exam memory tip). Flip, Again/Hard/Good/Easy spaced repetition (SM-2 style), your own Easy/Medium/Hard labels, favorites, custom decks, shuffle, multi-topic practice, difficult-only, previously-incorrect, and automatic "repeatedly forgotten" (3+ lapses) detection. Daily target with due/overdue counts and a new-cards-per-day setting |
| Memorization Center | 14 guides (OSI, TCP/IP, ports, private ranges, masks, IPv6 types, routing protocols, OSPF, STP, VLAN ranges, wireless, AD, common protocols, Cisco terminology) using mnemonics, chunking, visual associations, comparison tables, memory tricks, active recall with self-grading, mini-quizzes, filter by technique, and an 8-prompt daily memory challenge |
| Cheatsheets | 23 sheets with tables, command blocks and diagrams. Keyword search with snippets, bookmarks, long-press to mark important rows, "marked only" view, save PDF to device, share PDF (FileProvider), export all listed sheets to one PDF |
| Speed techniques | 14 techniques: method, worked example, fast vs traditional table, when it is safe, pitfalls, and timed drills with per-question pace vs target |
| Subnetting center | 10 lessons Beginner → CCNA level, randomized generator (13 question types, 3 levels, typed or multiple-choice answers, full worked solutions), timed 10-question challenge per level with best scores, accuracy by question type |
| Daily plan | Six tasks generated once per day and adapted to performance: flashcard target (backlog-aware), least-recalled memorization guide, cheatsheet from the weakest domain, lowest-scoring speed technique, 5-question quiz (missed + weak + weakest domain), and weak-topic lesson revision |

## Question bank & exam simulator (v1.2 — Prompt 3)
- **236 original questions** in 6 built-in domain packs, weighted to the v1.1 blueprint (d1 50 · d2 45 · d3 58 · d4 25 · d5 34 · d6 24),
  difficulty 62 beginner / 126 intermediate / 48 CCNA-level.
- **Formats:** single answer (158), multiple select (26), true/false (24), matching (20), ordering (8); categories include scenario,
  troubleshooting, diagram, calculation, routing-table, protocol-ID, configuration and command questions with CLI/JSON exhibits.
- **Every question** has: correct answer, explanation, why-correct, why-each-wrong-option, concept, domain/objective, difficulty, memory tip
  (where useful) and a linked lesson. Answers are never shown before submission. Options are shuffled per session.
- **Practice modes:** Quick (10), Standard (25), Intensive (50), Full bank, Weak topics (auto), Timed (countdown), Review incorrect, Bookmarked,
  plus topic packs, difficulty packs and filters by domain / difficulty / category / pack.
- **Exam simulator:** configurable length and time limit, blueprint-weighted random selection, difficulty floor, navigation, flags,
  review-before-submit screen, countdown with auto-submit, confirmation dialog, results with full explanations. Clearly labelled as an
  independent estimate — not Cisco's interface, scoring or question pool.
- **Analytics after every test:** total, correct, incorrect, unanswered, accuracy, time spent, average time per question, per-domain bars,
  weakest/strongest topics, recommended lessons, score-trend chart; plus a Practice analytics screen with trends, domain and topic accuracy and history.
- **Expansion:** pack manager (enable/disable, import JSON file, HTTPS download, remove), validation of imported packs, automatic new topics.
  Format spec: `docs/question-pack-format.md`; template: `docs/sample-pack.json`.
- **QA:** `content-source/qbank/build_bank.py` refuses to build on structural errors, wrong "Choose N" counts, missing wrong-option rationales,
  near-duplicates, or a correct option that stands out by length; 22 calculation/routing answers are recomputed with Python's `ipaddress`/`json`.
  Full answer key for human review: `docs/question-bank-review.md`.

## Advanced study features (v1.3 — Prompt 4)
| Feature | Where |
|---|---|
| **Personalized study planner** — exam date, study days, daily minutes, session length, knowledge level, target score → phases (Learn / Practice / Final review), a weekly schedule of themed days ending in revision + mock exam, time-feasibility check, and theme priorities recomputed from completed lessons, exam results and weak topics | Home → Study planner |
| **Networking tutor** — 10 modes (explain simply, explain at a level, ask, why an answer is correct, practice questions, subnetting step by step, compare, examples, summarize lesson, personal revision notes). **Offline by default**, answering from the app's verified content and labelled as such; optional OpenAI-compatible AI service with your own key, clearly labelled "AI-generated". Subnetting is always computed on-device | Home / Revise → Tutor; key in Settings |
| **Comparison center** — all 15 requested comparisons with tables, diagrams and exam tips | Revise / Learn |
| **Common mistakes** — 13 traps: wrong assumption, correct concept, how to recognize, how to avoid, example, linked lesson | Revise / Learn |
| **Glossary** — 137 terms with full form, simple + technical explanation, domain, related links; search, domain filter, A–Z index, tablet two-pane | Revise / Learn |
| **Daily challenge** — question, subnetting problem, flashcard, concept to memorize, 60-second speed drill; completion + streak | Home / Revise |
| **Dashboard & motivation** — completion, readiness, weekly stats, streak, domain performance, flashcards mastered, questions completed, weak-topic recommendations, 14 milestones computed only from real data | Home → Milestones |
| **Notes & bookmarks** — notes organized by topic (also from lessons and tutor answers), long-press lesson highlights, lesson / diagram / question / cheatsheet bookmarks, search, Markdown export and share | Bookmark icon in top bar |
| **Global search** — lessons, glossary/protocols, comparisons, mistakes, flashcards, cheatsheets, questions, notes, grouped by category | Search icon in top bar |
| **Reliability** — content-load error screen, corrupt-progress preservation, JSON backup/restore, Android Auto Backup (API key excluded), offline status | Settings |
| **Design** — navy/deep-blue/cyan identity, AA contrast verified, roomier reading typography, subtle transitions | — |

Build, install and signing: **docs/SETUP_AND_INSTALL.md** • Storage: **docs/DATA_MODEL.md** • QA: **docs/QA_REPORT.md** • CI: `.github/workflows/android.yml`

## Architecture
- `data/` — kotlinx-serialization models, `ContentRepository` (bundled JSON assets → always offline),
  `ProgressRepository` (DataStore, local), `Analytics.kt` (readiness, streaks, weak topics, revision schedule).
- `AppViewModel` — single source of truth; all progress writes go through it.
- `ui/nav/AppRoot.kt` — adaptive shell: bottom bar on phones, navigation rail at ≥600 dp.
- `ui/components/DiagramView.kt` — one renderer for every diagram (nodes, edges, arrows, legends) drawn on Canvas.
- `ui/components/BlockView.kt` — one renderer for guide / cheatsheet / lesson blocks, with highlight marking.
- `data/BankModels.kt`, `data/BankEngine.kt` (grading, selection, exam distribution), `data/PackRepository.kt` (packs, import, download), `ui/components/QuestionCard.kt` (all question types).
- `data/Planner.kt`, `data/Tutor.kt` (offline knowledge base + optional AI client), `data/Search.kt`, `data/Achievements.kt`, `data/DailyChallenge.kt`, `data/StudyModels.kt`.
- `data/Srs.kt` (scheduler), `data/Generators.kt` (problem generators), `data/DailyPlan.kt` (adaptive planner), `util/PdfExport.kt` (framework PdfDocument).

## Editing content
All lessons, questions, flashcards and diagrams are generated from Python in `content-source/`:
```
cd content-source
python build.py ../app/src/main/assets          # curriculum + diagrams
python build_memory.py ../app/src/main/assets   # flashcards, guides, cheatsheets, techniques, subnet lessons
python qbank/build_bank.py ../app/src/main/assets  # question bank packs (with QA gate)
python qbank/export_review.py                     # regenerate docs/question-bank-review.md
python build_study.py ../app/src/main/assets      # comparisons, common mistakes, glossary
```
The build validates diagram references, quiz answer indexes and chapter assignments.
Every lesson carries its blueprint objective numbers (e.g. `3.4`) and an `examRequired` flag.

## Blueprint note
Content follows CCNA 200-301 **v1.1** (six domains: 20/20/25/10/15/10%). Cisco has announced v2.0 from **3 Feb 2027**;
because lessons are tagged by objective, the curriculum can be remapped by editing `build.py`.

Not affiliated with or endorsed by Cisco Systems, Inc.
