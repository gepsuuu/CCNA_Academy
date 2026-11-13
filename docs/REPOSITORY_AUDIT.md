# Repository audit scope

Automated inventory covers 167 non-cache files present before this audit report was written. repository-inventory.json records paths, sizes and SHA-256 hashes; release-change-manifest.json compares them with the original extracted project. Generated inventory/report files are not recursively self-hashed.

## Inspected integration areas
Lesson model and screen, block/table rendering, highlight library/export resolution, search/tutor text composition, content generation and validation, pilot/migration tests, existing trainer/persistence source preservation, candidate version metadata, and CI artifact ordering.

## Findings addressed
- Prior migration test referenced nonexistent JsonCodec; now uses existing AppJson.
- New block highlights had no library/export resolver; both old and new key forms now resolve.
- Migrated content repeated card framing; new prose uses document flow.
- Narrow tables required horizontal reading; new table layout stacks labeled fields.
- New packet/output activity payloads and their text are wired into rendering and search.
- Prior CI uploaded APKs before unit tests; tests/lint now precede candidate publication.
- Exact pilot command fixtures were not represented by dedicated diagrams; three exact fixture definitions were added.
- Fifteen lessons lacked a diagram reference; topic-specific mechanism comparisons now provide references for all 41 lessons.
- Some daily tips used absolute HTTP/STP/SVI shortcuts; those tips were clarified in the generator.

## Limits
This is an automated/source integration audit, not a successfully executed Android baseline or a full manual technical review of every line. The unchanged protected source files are checked by hashes. Kotlin tests, lint, CLI runtime regression, actual storage upgrades, backup/restore and visual/accessibility QA remain unexecuted. See RELEASE_CANDIDATE_REPORT.md for the binding current status.
