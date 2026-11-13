# Question pack format (schema 1)

Packs are UTF-8 JSON files of up to 5 MB. Built-in packs live in `app/src/main/assets/packs/` (listed in `index.json`).
Imported packs (Practice → Question packs → *Import file* or *Download*) are validated and stored on the device, so they work offline.
Download links must use HTTPS.

## Pack fields
| Field | Required | Notes |
|---|---|---|
| `schema` | yes | Must be `1` |
| `id` | yes | 2–60 chars `[A-Za-z0-9._-]`; unique; cannot reuse a built-in pack id. Re-importing the same id replaces the pack (updates). |
| `name`, `version`, `category`, `description`, `author` | name required | `category` groups packs (e.g. "Topic pack", "Difficulty pack") |
| `topics` | no | `{id, title, domainId, lessonId}`. Topics referenced by questions but not listed are created automatically → new categories need no app update. |
| `questions` | yes | See below |

## Question fields
| Field | Notes |
|---|---|
| `id` | Unique across all installed packs (prefix with the pack id) |
| `domainId` | `d1` Network Fundamentals … `d6` Automation and Programmability |
| `objective`, `topic` | Blueprint objective (e.g. `3.4`) and topic id |
| `difficulty` | 1 Beginner, 2 Intermediate, 3 CCNA level |
| `format` | concept, scenario, troubleshooting, diagram, calculation, routing-table, protocol-id, config, command |
| `type` | `single`, `multi`, `tf`, `match`, `order` |
| `stem` | Question text. For `multi`, say how many to choose |
| `exhibit` | Optional monospace text (CLI output, configs, JSON) |
| `diagram` | Optional id of a built-in diagram |
| `options`, `correct` | `single`/`multi`/`tf`: option texts and 0-based correct indices (`tf`: `["True","False"]`) |
| `left`, `right` | `match`: `right[i]` is the correct partner of `left[i]` (the app shuffles the display) |
| `items` | `order`: items in the correct order (the app shuffles the display) |
| `explanation`, `whyCorrect`, `whyWrong` | `whyWrong[i]` explains why option *i* is wrong (empty string for correct options) |
| `concept`, `tip`, `lessonId` | Related concept, optional memory tip, lesson to recommend (e.g. `3-03`) |
| `shuffle` | Optional, default `true` — set `false` for options that must stay in order |

Invalid questions (bad indices, unknown type/domain, duplicate ids, etc.) are skipped and reported in the pack list.
See `sample-pack.json` for a complete example of each structure.
