"""Writes a human-readable answer key for auditing every question."""
import sys, os, io, contextlib
sys.argv = ["x", "/tmp/qb"]
with contextlib.redirect_stdout(io.StringIO()):
    import build_bank
from qlib import QS, TOPICS
D = build_bank.DOMAINS
L = ["# CCNA Academy question bank — answer key and QA report", "",
     f"{len(QS)} original questions. Generated from `content-source/qbank`. Every item passed automated checks: valid indices, "
     "one rationale per wrong option, correct 'Choose N' counts, no near-duplicates (Jaccard > 0.6), no conspicuously longer correct option, "
     f"and {sum(1 for q in QS if q.get('verified'))} calculation/routing answers recomputed with Python's ipaddress/json libraries.", ""]
for d, (title, w) in D.items():
    qs = [q for q in QS if q["domainId"] == d]
    L += [f"## {d.upper()} {title} — {len(qs)} questions (blueprint {w}%)", ""]
    for q in qs:
        L.append(f"### {q['id']} · {q['type']} · {q['format']} · difficulty {q['difficulty']} · obj {q['objective']} · {TOPICS[q['topic']][0]}")
        L.append(q["stem"])
        if q["exhibit"]: L += ["```", q["exhibit"], "```"]
        if q["diagram"]: L.append(f"*Diagram: {q['diagram']}*")
        if q["type"] in ("single", "multi", "tf"):
            for i, o in enumerate(q["options"]):
                mark = "✅" if i in q["correct"] else "❌"
                why = q["whyWrong"][i] if i not in q["correct"] else ""
                L.append(f"- {mark} {o}" + (f" — {why}" if why else ""))
        elif q["type"] == "match":
            L += [f"- {a} → {b}" for a, b in zip(q["left"], q["right"])]
        else:
            L += [f"{i+1}. {t}" for i, t in enumerate(q["items"])]
        L += ["", f"**Explanation:** {q['explanation']}", ""]
open(sys.argv[2] if len(sys.argv) > 2 else os.path.join(os.path.dirname(__file__), "..", "..", "CCNAAcademy", "docs", "question-bank-review.md"), "w").write("\n".join(L))
print("review written")
