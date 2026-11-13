"""Builds and quality-checks the CCNA question bank, then writes domain packs to <assets>/packs/."""
import json, os, re, sys, collections, itertools
sys.path.insert(0, os.path.dirname(__file__))
from qlib import QS, TOPICS
import q_d1, q_d2, q_d3, q_d4, q_d5, q_d6  # noqa: F401  (registers questions)

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from diagrams import DIAGRAMS

DOMAINS = {"d1": ("Network Fundamentals", 20), "d2": ("Network Access", 20), "d3": ("IP Connectivity", 25),
           "d4": ("IP Services", 10), "d5": ("Security Fundamentals", 15), "d6": ("Automation and Programmability", 10)}
errors, warnings = [], []
dids = {d["id"] for d in DIAGRAMS}

def words(s): return set(re.findall(r"[a-z0-9.]+", s.lower())) - {"the", "a", "an", "of", "to", "is", "which", "what", "and", "in", "on", "for", "true", "or", "false"}

# ---------- structural QA ----------
ids = [q["id"] for q in QS]
if len(ids) != len(set(ids)): errors.append("duplicate ids")
for q in QS:
    t = q["type"]
    if q["diagram"] and q["diagram"] not in dids: errors.append(f"{q['id']} unknown diagram")
    for k in ("stem", "explanation", "concept", "whyCorrect"):
        if not q[k].strip(): errors.append(f"{q['id']} empty {k}")
    if t in ("single", "multi", "tf"):
        n = len(q["options"])
        if not all(0 <= c < n for c in q["correct"]): errors.append(f"{q['id']} bad index")
        if len(q["whyWrong"]) != n: errors.append(f"{q['id']} whyWrong length")
        if t != "tf" and any(not q["whyWrong"][i] for i in range(n) if i not in q["correct"]): errors.append(f"{q['id']} missing wrong rationale")
        if t == "single" and len(q["correct"]) != 1: errors.append(f"{q['id']} single needs 1")
        if t == "multi":
            m = re.search(r"\(Choose (\w+)\.\)$", q["stem"])
            if not m or {"two": 2, "three": 3}[m.group(1)] != len(q["correct"]): errors.append(f"{q['id']} choose-N mismatch")
        if t == "single" and n < 3: errors.append(f"{q['id']} too few options")
        # answer must not be leaked by option length/format: warn if correct option is uniquely the longest by >2x
    if t == "match" and (len(q["left"]) < 3 or len(q["left"]) != len(q["right"])): errors.append(f"{q['id']} match size")
    if t == "order" and len(q["items"]) < 3: errors.append(f"{q['id']} order size")
    if "(Choose" in q["stem"] and t != "multi": errors.append(f"{q['id']} choose in non-multi")

# ---------- test-wiseness guard: correct option must not stand out by length ----------
for q in QS:
    if q["type"] != "single": continue
    L = [len(o) for o in q["options"]]; c = q["correct"][0]
    other = max(L[i] for i in range(len(L)) if i != c)
    if L[c] > 15 and L[c] > other * 1.5: errors.append(f"{q['id']} correct option is conspicuously longer ({L[c]} vs {other})")

# ---------- duplicate / near-duplicate detection ----------
for a, b in itertools.combinations(QS, 2):
    wa, wb = words(a["stem"] + " " + " ".join(a["options"] + a["left"] + a["items"])), words(b["stem"] + " " + " ".join(b["options"] + b["left"] + b["items"]))
    j = len(wa & wb) / max(1, len(wa | wb))
    if j > 0.6: errors.append(f"near-duplicate {a['id']} / {b['id']} ({j:.2f})")
    elif j > 0.45: warnings.append(f"similar {a['id']} / {b['id']} ({j:.2f})")

if errors:
    print("QUALITY ERRORS:"); [print("  ", e) for e in errors]; sys.exit(1)

# ---------- report ----------
n = len(QS)
print(f"{n} questions — all checks passed (calculation answers machine-verified where marked).")
by_dom = collections.Counter(q["domainId"] for q in QS)
for d, (title, w) in DOMAINS.items():
    print(f"  {d} {title:32s} {by_dom[d]:3d} ({by_dom[d] * 100 / n:4.1f}% vs blueprint {w}%)")
print("  difficulty:", dict(sorted(collections.Counter(q["difficulty"] for q in QS).items())))
print("  types:", dict(collections.Counter(q["type"] for q in QS)))
print("  formats:", dict(collections.Counter(q["format"] for q in QS)))
print("  machine-verified answers:", sum(1 for q in QS if q.get("verified")))
sc=[q for q in QS if q["type"]=="single"]
longest=sum(1 for q in sc if max(range(len(q["options"])),key=lambda i:len(q["options"][i]))==q["correct"][0])
print(f"  single-answer items where the correct option is the longest: {longest}/{len(sc)}")
for w in warnings: print("  note:", w)

# ---------- write packs ----------
out = os.path.join(sys.argv[1], "packs"); os.makedirs(out, exist_ok=True)
index = []
for d, (title, w) in DOMAINS.items():
    qs = [q for q in QS if q["domainId"] == d]
    topics = [dict(id=k, title=v[0], domainId=v[1], lessonId=v[2]) for k, v in TOPICS.items() if v[1] == d]
    pack = dict(schema=1, id=f"core-{d}", name=f"Core: {title}", version=1, category="Core CCNA 200-301",
                description=f"{len(qs)} original questions for {title} ({w}% of the v1.1 blueprint).",
                author="CCNA Academy", topics=topics, questions=qs)
    fn = f"core-{d}.json"
    json.dump(pack, open(os.path.join(out, fn), "w"), ensure_ascii=False, indent=1)
    index.append(fn)
json.dump(dict(packs=index), open(os.path.join(out, "index.json"), "w"), indent=1)
print("wrote", len(index), "packs to", out)
