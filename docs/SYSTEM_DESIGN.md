# CCNA Academy system design (1.5.0-rc2)

This document is the Phase 2 design. It describes how the source works now. It is not evidence that the Android app compiles or behaves correctly on a device; see `RELEASE_CANDIDATE_REPORT.md` and `release-acceptance.json` for evidence.

## 1. Teaching model

Every lesson follows one progressive narrative through exactly eight stages, in this order:

**Understand → Compare → Visualize → Observe → Configure → Verify → Troubleshoot → Recall**

- All eight stages are always present and listed in the stage navigation strip, so every stage stays discoverable.
- A stage that genuinely does not apply is kept with `applicable = false` and a learner-facing `reason`. The strip labels it "(n/a)" and the lesson shows the reason. No filler or irrelevant CLI is invented.
- Deeper mechanics, calculations, defaults, and platform nuances use progressive disclosure (`detail` blocks), not separate Beginner, Intermediate, and Exam tiers.
- `content-source/tests/test_migration.py` and `test_alignment.py` enforce the stage list and order. `test_docs.py` rejects documentation that drops Compare, substitutes Review, or reorders the stages.

## 2. Canonical authoring sources and generation order

Generated JSON under `app/src/main/assets/` must never be edited by hand. `content-source/build.py` regenerates it deterministically in this order:

1. `d1.py`–`d6.py` (via `lib.py`): the original 41 lessons and legacy fields (`simple`, `deep`, `why`, `examples`, `mistakes`, `terms`, `memorize`, `examNotes`, `supplementary`, `summary`, `diagrams`, `quiz`).
2. `diagrams.py`: the original vector diagrams.
3. `scenario_diagrams.py`: exact fixture diagrams (packet, VLAN pilot, OSPF pilot).
4. `mechanism_diagrams.py`: one lesson-specific mechanism diagram per mapped lesson. Chain diagrams are in `MECHANISMS`; diagrams with branching or labelled relationships are in `CUSTOM`.
5. `migrate_lessons.apply_migration()` builds `learningStages` from:
   - `pilot_lessons.json` for 2-01 and 3-03
   - `migration_authoring.py` for the other 39 lessons
   - retained legacy fields, placed inside the appropriate stages as `reference-*` blocks
   - `interactive_authoring.py` for packet walkthroughs, output inspectors and pilot checks
   - `configuration_expansions.py` for additional fixtures with context, commands, verification and undo
   - mechanism and fixture diagram placement
6. `validate_migration()` checks stage order, block uniqueness, payload shape, and diagram references.
7. `build_memory.py` / `build_study.py` generate `memory.json` / `study.json` (unchanged in this release).

`scripts/validate_content.py` regenerates into a temporary directory, byte-compares the result with the packaged assets, and runs every Python test.

## 3. Stable identifiers

| Object | ID form | Stability rule |
| --- | --- | --- |
| Lesson | `D-NN` (e.g. `2-01`) | Never renamed. Progress, reviews, notes, bookmarks and highlights key on it. |
| Stage | `understand` … `recall` | Fixed vocabulary. |
| Migrated block | `<lesson>-<stage>-<slug>` | Slug is authored, not positional. |
| Pilot block | `<lesson>-<stage>-pilot-NN` | Positional. **New pilot blocks are appended at the end of a stage, never inserted**, so existing IDs keep pointing at the same block. |
| Legacy question | `<lesson>-qN` | Never renamed; answer stats and SRS key on it. |
| Diagram | original names; `mechanism_<lesson>`; `*_fixture` | A `mechanism_<lesson>` diagram must teach that lesson (`test_alignment.py`). |
| Packet step | authored step id | Steps may be appended; saved state stores only an index and falls back safely. |

When historical text is technically wrong, it is corrected in place under the same ID. The correction is recorded in `TECHNICAL_CORRECTIONS.md`, and any affected baseline (for example a legacy fingerprint) is updated in the same change, with the reason stated. An error is never kept just to keep a fingerprint test passing.

## 4. Data model and backward-compatible decoding

- `AppJson` sets `ignoreUnknownKeys = true`, `encodeDefaults = true` and `explicitNulls = false`.
- Every field added since v1.0 has a default. `Lesson.learningStages` defaults to an empty list. `Block` keeps its original defaults and adds optional `id`, `correctIndex`, `rationales`, `packet?` and `inspector?`.
- Nullable payloads (`packet`, `inspector`, `diagram`) are rendered only through `?.let`. A block whose payload is missing renders nothing for that payload.
- `ProgressState` was **not changed** in 1.5. Older backups (v1.0–v1.4 shapes) decode with defaults, as covered by `LessonPresentationTest.preV14BackupDecodesWithDefaultsAndKeepsUserData`.
- `ProgressRepository` keeps the raw undecodable progress JSON under a separate key instead of overwriting it, and `restoreJson` rejects invalid backups before replacing data. Neither behavior changed.

## 5. Highlights (old and new)

The resolver is `data/HighlightText.kt: lessonSectionText`. In rc2 it moved out of the Compose screen so it can be tested on the JVM.

| Stored key | Origin | Resolution |
| --- | --- | --- |
| `lesson:simple`, `:why`, `:summary` | v1.3 legacy view | Legacy field text |
| `lesson:deepN`, `lesson:exN` | v1.3 legacy view | Indexed legacy section (empty if index missing) |
| `lesson:block:<blockId>` | 1.5 progressive view | Block title, text, items and rows |
| unknown block ID | future removal | Visible "Preserved highlight" placeholder; the key is never deleted |

The legacy fields remain in the assets and in the "Open preserved reference" view, so old highlights still resolve. The library list and Markdown export both use this one resolver.

## 6. Search and tutor consumers

- `Search.kt` indexes the legacy lesson fields plus `Lesson.progressiveText()`. That text includes stage reasons, block text/items/rows, rationales, packet steps and headers, and inspector excerpts and fields.
- `OfflineTutor.kt` (split from `Tutor.kt` in rc2 so it compiles without Android) scores lessons using the same `progressiveText()`.
- Diagram text alternatives (`diagramTextAlternative`) are generated at render time and are not indexed separately; diagram titles and explanations are already in the assets.

## 7. Assessment and analytics boundaries

- **Scored:** the legacy lesson quiz (`KnowledgeCheck`), question bank, exams, drills and daily challenge. These call `recordAnswer` and feed analytics, SRS and the planner.
- **Practice only:** stage `check` blocks and pilot interactive checks. They show per-option rationales, say on screen that they are not scored, and never call `recordAnswer`.
- **Activities:** output-interpretation, table recall and teach-back record into `ProgressState.activities` (v1.4 behavior, unchanged).

## 8. Packet and output simulation scope

- `PacketWalkthrough` steps are authored teaching fixtures, not packet captures. Header maps contain only fields that exist at that step. Local decision steps have an empty map, and the UI says no header is invented.
- The web walkthrough covers:
  - DNS cache hit/miss, including ARP to the on-link resolver before the query
  - gateway ARP hit/miss
  - MAC rewrite and TTL decrement at each routed hop, with IP endpoints and ports unchanged (no NAT)
  - the TCP three-way handshake
  - HTTP request and response with exact sequence/acknowledgment arithmetic
  - plain HTTP only; TLS and NAT are named as out of scope
- The OSPF walkthrough covers the Down → Init → 2-Way → ExStart → Exchange → Loading → Full progression on the pilot point-to-point fixture. Attempt is described as NBMA-only.
- Output inspectors are authored excerpts labelled "simulated" and tied to a declared fixture.
- **CLI trainer boundary:**
  - `lab/CliEngine.kt`, `lab/Network.kt` and the other protected lab files are byte-identical to rc1 (`protected-source-hashes.json`).
  - The trainer models: IOS modes, hostname, interfaces and IPv4 addressing, VLAN creation, access/trunk/DTP behavior, allowed and native VLAN lists, router-on-a-stick subinterfaces, and static routes with administrative distance.
  - It explicitly rejects, with a teaching message, commands it does not model: OSPF and other dynamic routing, ACLs, NAT, DHCP, `ip routing`, STP, EtherChannel, port security, voice VLAN, line/username/SSH/crypto, NTP, logging, SNMP, CDP/LLDP, VTP, IPv6 and MAC tables (`CliEngine.UNSUPPORTED`).
  - No new trainer protocol support is claimed in 1.5.

## 9. Responsive layout and accessibility

- Lessons render in a `LazyColumn`. Item keys are `stage-<stageId>` and `content-<blockId>`.
- Stage jump targets come from `stageItemIndex()`, which has a JVM test against the emission order. With reduced motion on, jumps use `scrollToItem` instead of animation.
- Tables stack as label/value pairs below 600 dp and scroll horizontally above that. Code and terminal output scroll horizontally, are selectable, and have a copy action.
- Diagrams:
  - The `Canvas` exposes `diagramTextAlternative(d)` as its content description: title, every element and detail, every labelled relationship, and the legend.
  - The authored explanation and the legend are also shown as visible text.
  - Full-screen view supports pinch zoom plus Zoom in, Zoom out and Reset buttons for single-pointer users.
- Packet cache toggles are whole-row toggleable controls (`Role.Checkbox`) at least 48 dp tall.
- Interactive state (packet position, cache branches, inspector selection, check answers, detail expansion, legacy-view toggle) uses `rememberSaveable` so it survives rotation and process recreation. Exception: the legacy `KnowledgeCheck` keeps its v1.4 `remember` state, so in-progress answers to the scored quiz reset on rotation. Already recorded answers are persisted. This is listed as a known limitation.

## 10. Release evidence

`docs/release-acceptance.json` is the only acceptance register, and `scripts/acceptance_gate.py` fails closed:

- A gate passes only with `status: passed` and hash-verified evidence files.
- A lesson passes only with `acceptance: passed`.
- Gates that need a real Android toolchain or device (Gradle build, Compose compilation, lint, APK, emulator or device review, TalkBack, upgrade and restore on a device) stay `blocked` or `pending` until real evidence exists.
- JVM evidence from the offline Kotlin compiler is recorded as exactly that. It is not an Android build.
