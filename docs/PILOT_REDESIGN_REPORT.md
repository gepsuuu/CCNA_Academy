> Historical report. Current status: see RELEASE_CANDIDATE_REPORT.md. This file is not final acceptance evidence.

# CCNA Academy: pilot content overhaul delivery

Status: working source draft, not an accepted or device-tested release.

## Implemented
- VLAN lesson 2-01 and OSPF lesson 3-03 now have an optional ordered eight-stage narrative.
- 54 authored blocks cover motivation, comparisons, protocol behavior, configuration, simulated verification, troubleshooting, and reveal-based reasoning prompts.
- Existing block rendering is extended with expandable explanations and terminal copy controls.
- Existing diagram cards provide the existing full-screen viewing interaction.
- Original lesson fields, questions, and identifiers remain unchanged. Original explanations and saved highlight locations are accessible through the preserved-reference disclosure.
- The Lesson model gains an optional learningStages field defaulting to an empty list. ProgressState is unchanged.
- Python generation incorporates and validates the pilot source. CI includes a pilot-content gate.
- A coverage record includes all 41 lessons and explicitly marks every lesson incomplete under the redesign acceptance standard.

## Validation actually executed
- Curriculum and diagram generation: passed.
- Memory generation: passed.
- Study generation: passed.
- Python compileall: passed.
- Ten new Python content tests: passed.
- Semantic comparison of the full original curriculum against the new curriculum after removing learningStages: identical.
- No original files deleted.

## Not executed / blocked
- Android build command attempted; failed to start because gradle is not installed. Android SDK is not configured.
- Kotlin tests, existing CLI engine tests, Compose compilation, emulator/device tests, installation, and runtime persistence tests were not executed.
- Two Kotlin compatibility/reference tests are supplied but unexecuted.
- No APK is included. The existing GitHub Actions workflow can build and test the source after upload; that workflow has not been triggered here.

## Important remaining work
- These are pilot drafts, not gold-standard accepted lessons. No mass migration has started.
- Exact scenario-specific diagrams are still needed. Existing illustrations are explicitly labeled conceptual rather than claimed to match the new command fixtures.
- Follow the Packet interactive header inspector and stateful topology changes are not implemented in this draft.
- New recall prompts reveal explanations but do not record scored answers. Existing scored quizzes remain unchanged.
- New blocks do not yet support stable-ID highlighting. Original highlights remain accessible in unchanged legacy sections.
- CLI syntax highlighting, inspectable output fields, full line-by-line command review, accessibility, narrow-screen layout, and complete technical review remain pending.
- Voice VLAN coverage remains in the preserved reference material and needs integration/review.
- Search and tutor continue to consume the original lesson fields; indexing of new stages remains pending.
- Old tiers remain in preserved reference content and all nonpilot lessons. They are not the default narrative for the two pilots.
- No claim is made that unchanged code proves unchanged runtime behavior.

## Review and build
Open the project in an Android-capable environment with JDK 17, Gradle 8.11.1 and the SDK required by app/build.gradle.kts. Run the content checks, testDebugUnitTest and assembleDebug. Review lessons 2-01 and 3-03 on the target Android device before approving full migration.

## Source locations
- content-source/pilot_lessons.json: editable pilot narratives and technical source links.
- content-source/validate_pilots.py: structural validator.
- content-source/tests/test_pilots.py: executed Python tests.
- app/src/test/java/com/ccnaacademy/app/PilotLessonTest.kt: unexecuted Kotlin tests.
- docs/redesign-coverage.json: all-lesson coverage and remaining work.
- docs/pilot-validation-summary.json: validation status.
- docs/pilot-python-test-results.txt: executed test output.
- docs/pilot-android-build-attempt.txt: build prerequisite failure.
- docs/redesign-change-manifest.json: changed/new file inventory.
