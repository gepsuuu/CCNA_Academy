# Changelog

## 1.4.0 — Interactive lessons, CLI labs, animated diagrams and explanatory tables

### Scope change
Earlier versions intentionally excluded any CLI or simulator. v1.4 adds a **limited, stateful, offline educational CLI trainer**,
guided labs and step-through networking animations, as requested in the upgrade specification. It is not an IOS emulator or a
Packet Tracer replacement: supported commands and limits are documented in `docs/CLI_TRAINER.md` and inside the app.

### Added
- **CLI trainer engine** (`lab/CliEngine.kt`, `lab/LabModel.kt`, `lab/Network.kt`), pure Kotlin and unit tested:
  - IOS modes and prompts (user/privileged EXEC, global, interface, interface range, subinterface, VLAN), abbreviations,
    `?` help, Tab completion, per-device history, `do`, submode → global fall-back, `end`/Ctrl+Z.
  - Switch model (2960-style): VLAN database (vlan.dat semantics), access/trunk/dynamic modes with the DTP outcome matrix,
    `nonegotiate`, native VLAN, allowed-list replace/add/remove/all/none, SVIs, `ip default-gateway`.
  - Router model: interfaces shut by default, IPv4 addressing with mask/host/overlap validation, 802.1Q subinterfaces
    (encapsulation before IP), static/default/floating routes with longest-prefix match and AD.
  - running-config vs startup-config, `copy run start`, `write`, `erase startup-config`, `reload` (unsaved changes lost, VLANs kept).
  - `show vlan brief`, `show interfaces trunk|status|<if> switchport`, `show ip interface brief`, `show ip route`, `show run/start`, `ping`.
  - Honest errors: caret position, `% Invalid input`, `% Incomplete command`, `% Ambiguous command`, plus a blue `[Trainer]`
    explanation (wrong mode, use `do`, interface vs global command, platform difference). Real-but-unmodelled commands are
    recognised and reported as unsupported — never fake success.
  - Computed reachability (VLAN segments, trunk tagging and native-VLAN behaviour, routing, default gateways) with hop-by-hop
    traces and a reason for every failure; both forward and return paths are checked.
- **7 labs** (`lab/Labs.kt`): CLI fundamentals; VLANs + trunk (vertical slice); 2 troubleshooting labs (allowed-list replacement,
  deleted VLAN); independent VLAN challenge (changed values, no hints, check-on-demand); router-on-a-stick; static/default routing.
  Tasks are graded on the shared state, so every equivalent valid solution is accepted. Progressive hints: concept → command family → partial syntax.
- **Lab screen**: Tasks · Topology · CLI · Verify · Review tabs (two-pane on tablets); live topology derived from the configuration
  with trunk/VLAN/down styling, device inspector, reachability tests; verification commands that don't disturb the console mode;
  review with config diff vs the starting state, explanation, teach-back and the lab's rejected commands. Save/resume, reset.
- **12 step-through animations** (`lab/Animations.kt`): encapsulation, ARP + switching, VLANs/802.1Q/native VLAN, router-on-a-stick,
  STP election + failure, EtherChannel, longest-prefix match, OSPF adjacency, DHCP DORA, DHCP relay, PAT, ACL evaluation.
  Play/pause, previous/next, replay, 0.5×/1×/1.5× speed, step indicator, captions, linked tables that update per step with
  highlighted rows, static step-by-step view, full-screen pinch-zoom, reduced motion (app setting + system animator scale),
  no autoplay, pause on background. Clearly labelled as concept demonstrations.
- **Explanatory tables in lessons** (`lab/LessonExtras.kt`): explanatory tables in all 41 lessons plus 15 linked comparisons — tap rows for
  explanations, hide/reveal for active recall, "Quiz me" fill-in, filtering, stacked cards on narrow screens, kind labels
  (reference / scripted example / troubleshooting / lab state). Subnet table values are computed, not typed.
- **Command walkthroughs** (mode, purpose, verification, common mistake, undo, notes), **output-interpretation activities** whose
  output is generated live by the trainer and whose annotations are tested against that output, and **teach-back** prompts
  (self-assessed; the app does not claim to grade free text).
- **Learning-cycle strip** on every lesson showing which of Understand → Compare → Visualize → Observe → Configure → Verify →
  Troubleshoot → Recall the lesson offers.
- **Labs hub**, **supported-commands screen** (generated from the grammar) and **mistake notebook** (rejected commands with explanations, retry, mark understood).
- **Skill tracking** separated into concepts, CLI syntax, output interpretation and troubleshooting; guided completion vs independent
  mastery shown separately on the Progress screen.
- Settings: reduce motion, default animation speed.

### Changed
- Lesson screen inserts Compare / Visualize / Configure / Practice / Interpret output / Teach it back sections where content exists.
- Practice tab and Learn chips link to the labs hub. Version 1.4.0 (versionCode 5).
- App waits for stored progress before showing screens, so a lab can never start from an empty state and overwrite a saved one.

### Data and migration
- `ProgressState` gains `labs`, `skills`, `labMistakes`, `activities`, `reducedMotion`, `animSpeed`. All have defaults; v1.0–1.3 data
  and backups load unchanged (unit-tested). Reset progress keeps motion preferences.

### Tests
- New: `LabEngineTest` (19 tests), `LabContentTest` (9 tests); 44 tests pass locally (UI not compiled — see docs/QA_REPORT.md). Existing suites unchanged.
