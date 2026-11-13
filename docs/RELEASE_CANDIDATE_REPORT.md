# CCNA Academy 1.5.0-rc2 — release candidate report

**Status: source release candidate. NOT an accepted Android release. No APK.** `scripts/acceptance_gate.py` exits 2 (50 unmet conditions) by design.

## Verified (actual commands)
| Check | Command | Result |
|---|---|---|
| Asset regeneration + Python tests | `python scripts/validate_content.py` | identical assets; 74 tests OK |
| Kotlin tests (plain JVM, offline compiler) | `tools/offline-kotlin/kc.sh` then `RunnerKt` | 64/64 passed (8 classes) |
| Acceptance gate | `python scripts/acceptance_gate.py` | blocked (expected) |

Counts verified from source: 41 lessons × 8 stages, 124 scored questions, 50 diagrams, versionCode 7 / 1.5.0-rc2, applicationId com.ccnaacademy.app. Pipeline: Understand → Compare → Visualize → Observe → Configure → Verify → Troubleshoot → Recall.

## Not executed (blocked: no Android toolchain/network)
Gradle build, Compose compilation, Android lint, debug/release APK, emulator/device pilot review, accessibility/TalkBack, rotation, upgrade and backup/restore on device. Evidence: docs/evidence/android-toolchain-probe.txt.

## Changes
See docs/TECHNICAL_CORRECTIONS.md, docs/DECISION_LOG.md, docs/SYSTEM_DESIGN.md.

## Known limitations
- Compose edits (diagram semantics, zoom buttons, toggle rows, stage-jump helper wiring) are uncompiled.
- Legacy scored KnowledgeCheck loses unsubmitted selections on rotation.
- CLI trainer does not model OSPF, NAT, ACLs, DHCP, STP, EtherChannel, SSH, port security, DAI, IPv6 (rejected with teaching messages).
- Walkthroughs/outputs are authored simulations, not captures or live output.

## To finish
`./gradlew testDebugUnitTest lintDebug assembleDebug`, emulator review of 2-01 and 3-03, sign release with original keystore, record evidence in release-acceptance.json.
