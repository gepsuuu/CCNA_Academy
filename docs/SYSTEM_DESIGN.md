# Source architecture and migration contract

## Goal and scope
Teach each topic through Understand, Visualize, Observe, Configure, Verify, Troubleshoot, Recall, and Review. A source migration is not educational or device acceptance. Keep the existing offline app, identifiers, storage, trainer, search, tutor, review scheduling, and backup behavior intact.

## Authoring and build
Existing content-source modules remain the canonical historical material. migrate_lessons.py composes authored stages, pilots, configuration expansions, mechanism diagrams, and interactive fixtures. build.py regenerates all packaged JSON deterministically. validate_content.py compares temporary regenerated assets with the packaged files and runs Python tests. Avoid editing generated assets without updating their source.

## Data model
Lesson.learningStages has a backward-compatible default. LearningStage carries stable id/title, applicability, reason, and blocks. Block retains its old defaults and adds optional packet/inspector payloads, stable id, correctIndex and rationales. PacketWalkthrough contains a diagram reference, declared assumptions, devices, and steps. Each step has an id, device/interface, action, why, next, selected header map, and a known cache condition. OutputInspection contains a command, fixture, healthy/abnormal excerpts, field explanations, a question/answer, and provenance. New fields default to absent where historical JSON lacks them.

## UI contract
Use a lazy list with stable stage/block keys. Stage buttons navigate to the corresponding item; reduced motion uses immediate scrolling. TeachingBlockFrame uses document flow for migrated blocks while legacy tools keep their cards. Tables stack fields below 600 dp and retain the wide table above it. Code stays selectable/copyable and horizontally scrollable. Packet/output views use stacked controls and explicit labels rather than color alone. All actual Android behavior remains subject to compilation/device QA.

## Interactive scope
Packet and OSPF activities are deterministic authored walkthroughs. Cache switches select declared branches and reset the step position. They do not mutate the existing CLI engine. Header differences may cross different messages, which the UI explicitly warns about. Output inspectors are authored excerpts, not dynamically generated from trainer state. Do not claim these are a complete protocol simulator, capture analyzer, or production network connection.

## Preservation contract
Do not rename original lesson/question/diagram identifiers. Retain original lesson fields so old highlights and references can resolve. New highlights use lessonId:block:blockId. Library/export resolves both formats and provides a preserved-highlight fallback for a missing new block. Do not reset ProgressState or replace storage as part of content loading. Protected-source hashes document unchanged trainer/persistence-related files. Runtime upgrade and backup/restore must still be tested.

## Technical authoring rules
Declare each fixture, prerequisites, platform family, interface roles, addresses, return path, and what is intentionally omitted. Separate mutually exclusive NAT or topology alternatives. Explain command mode and purpose; provide verification, abnormal evidence, correction, and restoration. Label simulated outputs. Distinguish OSPF point-to-point from broadcast DR/BDR behavior, local ARP from remote routing, MAC rewrites from unchanged no-NAT IP endpoints, and transport success from application success.

## Acceptance and release
Run content tests, Kotlin tests, lint, candidate builds, pilot device review, all-lesson technical review, mobile/accessibility review, and real upgrade/restore tests. Record actual evidence with hashes in release-acceptance.json. acceptance_gate.py fails closed while gates or lesson acceptances are pending. CI publishes only candidate artifacts after tests/build, not a device-approved release. Do not turn a missing result into a pass.
