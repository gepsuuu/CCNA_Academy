"""Builds study.json: comparisons, common mistakes and glossary (with validation)."""
import json, sys
from compare import COMPARISONS
from mistakes import MISTAKES
from glossary import GLOSSARY, MISSING_RELATED
from diagrams import DIAGRAMS
from lib import LESSONS
import d1, d2, d3, d4, d5, d6  # noqa: F401
dids = {d["id"] for d in DIAGRAMS}; lids = {l["id"] for l in LESSONS}
err = []
if MISSING_RELATED: err.append(f"glossary related terms missing: {MISSING_RELATED}")
for c in COMPARISONS:
    for l in c["lessons"]:
        if l not in lids: err.append(f"{c['id']} lesson {l}")
    for b in c["blocks"]:
        if b.get("diagram") and b["diagram"] not in dids: err.append(f"{c['id']} diagram {b['diagram']}")
        if b["type"] == "table" and any(len(r) != len(b["headers"]) for r in b["rows"]): err.append(f"{c['id']} table {b['title']}")
for m in MISTAKES:
    if m["lessonId"] not in lids: err.append(f"{m['id']} lesson")
    if not all(m[k] for k in ("wrong", "right", "recognize", "avoid", "example")): err.append(f"{m['id']} empty field")
for g in GLOSSARY:
    if not (g["simple"] and g["technical"] and g["domainId"] in {"d1","d2","d3","d4","d5","d6"}): err.append(g["id"])
if err: print("ERRORS", err); sys.exit(1)
json.dump(dict(comparisons=COMPARISONS, mistakes=MISTAKES, glossary=GLOSSARY), open(sys.argv[1] + "/study.json", "w"), ensure_ascii=False, indent=1)
print(len(COMPARISONS), "comparisons", len(MISTAKES), "mistakes", len(GLOSSARY), "glossary terms")
