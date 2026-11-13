# Data model and storage

## Read-only content (bundled in `app/src/main/assets`, always offline)
| File | Kotlin type | Contents |
|---|---|---|
| `curriculum.json` | `Curriculum` | Blueprint, 6 domains → 22 chapters → 41 lessons (10-part structure, knowledge checks), 30 daily tips |
| `diagrams.json` | `DiagramFile` | 29 vector diagrams (nodes, edges, labels, legend, explanation) rendered on Canvas |
| `memory.json` | `MemoryLibrary` | 241 flashcards in 16 categories, 14 memorization guides, 23 cheatsheets, 14 speed techniques, 10 subnetting lessons |
| `study.json` | `StudyContent` | 15 comparisons, 13 common mistakes, 137 glossary terms |
| `packs/index.json`, `packs/core-d1…d6.json` | `QuestionPack` | 236 questions in 6 built-in packs (schema: `docs/question-pack-format.md`) |

## Code-defined learning content (v1.4, validated by unit tests)
| File | Contents |
|---|---|
| `lab/Labs.kt` | 7 lab definitions: topology, starting configs, tasks (graded on state), progressive hints, verification commands |
| `lab/Animations.kt` | 12 step-through animations: nodes, links, per-step packets/captions/table operations |
| `lab/LessonExtras.kt` | Per-lesson tables, comparison links, command walkthroughs, output activities, teach-back prompts |

## Student data (on device)
Stored with **Jetpack DataStore** (`files/datastore/ccna_progress.preferences_pb`) as one JSON document, `ProgressState`, written atomically on every change.
Unknown keys are ignored and every field has a default, so older backups load in newer versions. If the stored JSON ever fails to decode,
the raw text is preserved under a separate key instead of being overwritten.

| Area | `ProgressState` fields |
|---|---|
| Lessons | `completedLessons`, `lastLessonId`, `reviews` (spaced lesson revision), `answers` (knowledge checks) |
| Study time | `studySecondsByDay` (foreground time on study screens only) |
| Flashcards | `srs` (SM-2 state per card), `favorites`, `userDifficulty`, `customDecks`, `newIntroduced`, `reviewsByDay`, `newPerDay` |
| Memorization & drills | `guideVisits`, `recallStats`, `challengeScores`, `drills`, `genStats`, `sheetBookmarks`, `highlights` (cheatsheets) |
| Daily routine | `plan` (today's adaptive revision plan), `dailyDone`, `challengeDone` (daily challenge parts), `conceptsMemorized` |
| Question bank | `bankStats` (per question), `bankBookmarks`, `sessions` (practice & exam results, last 200), `disabledPacks`, `exams` |
| Planner | `planner` (`PlannerSettings`: exam date, study days, minutes/day, session length, level, target) |
| Notes | `notes`, `lessonBookmarks`, `diagramBookmarks`, `lessonHighlights` |
| Motivation | `badges` (first time each milestone was reached — milestones are always recomputed from real data) |
| Appearance | `themeMode`, `dynamicColor`, `reducedMotion`, `animSpeed` (v1.4) |
| CLI labs (v1.4) | `labs` (`LabSave`: full `LabState` of every device incl. running/startup config and CLI mode, per-device transcripts, hint levels, completion, challenge checks), `labMistakes` (mistake notebook, last 200) |
| Skills & activities (v1.4) | `skills` (concept / cli / output / troubleshoot), `activities` (table quizzes, output interpretation, teach-back self-checks) |

Other storage: imported question packs in `files/packs/*.json`; AI tutor settings (endpoint, model, API key) in a **separate** DataStore
(`tutor_settings`) that is excluded from Android backup and from the in-app backup file; PDF exports in `cache/exports`.

## Backup
* In-app: *Settings → Backup & restore* writes/reads a validated JSON copy of `ProgressState`.
* Android Auto Backup / device transfer include the progress DataStore and imported packs only (`res/xml/data_extraction_rules.xml`, `backup_rules.xml`).
