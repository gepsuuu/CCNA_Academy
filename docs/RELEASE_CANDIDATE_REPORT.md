# CCNA Academy 1.5.0-rc1: delivery and acceptance report

## Decision
**Source release candidate delivered. Full seven-phase acceptance is NOT complete. No APK is included.**

The request to continue without questions was followed. Missing source capabilities were implemented and available checks were executed. Android compilation, runtime/device testing, and a full independent lesson-by-lesson technical/educational acceptance review cannot be represented as completed.

## What changed in this delivery
- 41 lessons retain the eight-stage learning flow and original lesson identifiers.
- Every lesson now has a visual reference and an interactive practice check with option rationales.
- 47 vector diagram definitions in the existing Android renderer: 18 added in this delivery, including exact VLAN, OSPF, and packet fixtures plus 15 mechanism comparisons.
- Two unique authored packet/field walkthroughs, placed in six relevant lessons: DNS/ARP/routing/TCP/HTTP and OSPF adjacency progression.
- Packet controls inspect devices/interfaces, selected headers, changed fields, previous/next steps, and DNS/gateway cache branches where applicable. They are authored educational fixtures, not captured traffic or a full IOS emulator.
- Ten output inspectors compare healthy/abnormal excerpts, expose field explanations, and reveal evidence-based interpretations. Each declares its fixture and simulated provenance.
- Ten additional configuration code blocks cover data/voice VLANs, both ends of routed LACP, IPv6 static-route variants, DHCP client, dynamic NAT, PAT, NTP lab server/client, port security, and DAI with a static printer. Context, purpose, verification, and safe restoration accompany them.
- Migrated prose uses document flow instead of a card around every block. Narrow tables stack fields; code uses horizontal scrolling, copy controls, and basic command-category colors.
- Stage navigation respects the app reduced-motion setting. Packet steps use manual navigation, not automatic motion.
- New block highlights resolve in the library/export view; old deep/example keys remain supported. New activity text is indexed for search/tutor use.
- A nonexistent JSON helper in a prior Kotlin test was replaced with the existing AppJson helper.
- CI now runs unit tests/lint before uploading candidate APKs. This workflow was edited, not remotely executed.
- Candidate versionCode is 6, versionName 1.5.0-rc1. The application ID remains unchanged.

## Seven-phase status
| Phase | Delivered evidence | Acceptance status |
| --- | --- | --- |
| 1. Repository audit | File/hash inventory, change manifest, protected-source fingerprints, content/reference checks | Automated/source audit delivered; executable Android baseline still blocked |
| 2. System design | Data/UI/migration/validation specification in SYSTEM_DESIGN.md | Specification delivered; runtime assumptions unverified |
| 3. Gold-standard pilots | VLAN and OSPF source, exact fixture diagrams, checks, inspectors, OSPF progression | Device review gate not passed |
| 4. Reusable components | Packet/field inspector, output inspector, adaptive tables, plain teaching flow, navigation | Source implemented; compilation, accessibility and interaction review pending |
| 5. All-lesson migration | 41 lessons, 328 stages, visual references and practice checks | Source migration delivered; full technical/educational acceptance pending |
| 6. Validation/regression | 62 Python tests passed; deterministic asset comparison; protected-source checks | Kotlin/CLI runtime tests, lint, APK build, upgrade and device checks not run |
| 7. Delivery/report | This report, source package, manifests, logs, build workflow, acceptance register | Candidate delivery complete; final accepted-release report cannot be issued |

## Executed checks
The final run is recorded in release-python-tests.txt. All 62 Python tests passed. Checks include original lesson-field preservation, stable IDs, stage applicability, diagram references, diagram node bounds/overlaps for new definitions, per-option rationale shape, packet MAC/TTL/IP/port/handshake consistency, cache branches, output fixture shape, configuration context/cleanup presence, protected source hashes, and fail-closed release acceptance.

These checks do not compile Kotlin, render Compose, prove every technical statement, measure accessibility/contrast, or exercise real network devices. Geometry checks do not prove label readability. Added Kotlin regression tests are supplied but **not executed**.

## Build limitation and actual evidence
android-build-attempt.txt records that Java is present, but gradle, kotlinc, adb and sdkmanager are unavailable on PATH. The requested Gradle validation/build command failed with `gradle: command not found`. Attempts to obtain the Gradle distribution and Android command-line tools were rejected by the environment with HTTP 403 and `url_policy_denied`. No remote build connector was available. No successful compilation, APK, installation, screenshot, emulator result, or device approval is claimed.

## Remaining work, not just environmental blockers
1. Compile/test/lint the Android application with the specified toolchain and correct any resulting issues.
2. Run the original CLI trainer regression tests and the new serialization/highlight/activity tests.
3. Review both pilots on Android before final all-lesson acceptance. The historical pilot-before-migration gate was not retroactively satisfied.
4. Perform full technical/educational review across all 41 lessons, including original retained material, all questions, platform-specific commands, and scenario consistency.
5. Review diagrams on narrow/large-text/dark/light layouts. Some new visuals are conceptual comparisons with reading-order arrows, not detailed physical illustrations or fully interactive topology simulations. Specialized physical connector/fiber illustrations and richer state-driven topology behavior remain beyond this candidate.
6. Test TalkBack, touch targets, code/table scrolling, large fonts, orientation changes, reduced motion, and performance. No screenshot-based visual QA was possible here.
7. Test upgrade and backup/restore with real existing progress, notes, highlights, bookmarks, reviews, and trainer sessions. Source preservation is not proof of runtime preservation.
8. Complete the evidence-backed acceptance register and final sign-off. No lesson is marked accepted solely because its source exists.

## Safe installation boundary
Use the same application ID and original signing key for an in-place update. Export a backup before testing. A debug APK signed with a different key cannot safely replace an existing differently signed install. Do not uninstall an existing installation to bypass a signature error without first preserving its data. Release signing and physical installation were not performed.

## Start points
- SYSTEM_DESIGN.md: source architecture and migration contract.
- REPOSITORY_AUDIT.md and repository-inventory.json: audit scope and inventory.
- release-acceptance.json: current evidence register, all lesson acceptances pending.
- ../scripts/validate_content.py: deterministic asset and Python validation.
- ../scripts/build_candidate.sh and ../.github/workflows/android.yml: candidate build paths for a capable environment.
- ../scripts/acceptance_gate.py: intentionally exits 2 while acceptance is incomplete.

Earlier pilot/migration reports are historical and superseded by this report for current status.

## Selected authoritative references
The selected new/clarified topics were checked against the sources recorded in release-source-register.json. That register is not a claim that every original lesson, command, diagram or question has passed a full independent review.
