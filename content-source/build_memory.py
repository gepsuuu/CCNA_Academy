import json, sys, collections
from cards import CARDS, CATEGORIES
from memorize import GUIDES
from cheatsheets import SHEETS
from speed import TECHS
from subnet_lessons import SUBNET_LESSONS
from diagrams import DIAGRAMS
dids = {d["id"] for d in DIAGRAMS}
err = []
ids = [c["id"] for c in CARDS]
if len(ids) != len(set(ids)): err.append("dup card ids")
for c in CARDS:
    if c["diagram"] and c["diagram"] not in dids: err.append(c["id"])
    for k in ("front", "answer", "explanation", "example", "tip"):
        if not c[k]: err.append(f"{c['id']} empty {k}")
def blocks(owner, bl):
    for b in bl:
        if b.get("diagram") and b["diagram"] not in dids: err.append(f"{owner} diagram {b['diagram']}")
        if b["type"] == "table":
            n = len(b["headers"])
            for r in b["rows"]:
                if len(r) != n: err.append(f"{owner} table '{b['title']}' row {r}")
def qs(owner, q):
    for x in q:
        if not 0 <= x["answer"] < len(x["options"]): err.append(f"{owner} {x['id']}")
for g in GUIDES: blocks(g["id"], g["blocks"]); qs(g["id"], g["quiz"])
for s in SHEETS: blocks(s["id"], s["blocks"])
for t in TECHS: qs(t["id"], t["questions"])
for l in SUBNET_LESSONS: blocks(l["id"], l["blocks"])
GENS = {"bin2dec","dec2bin","cidr2mask","mask2cidr","network","broadcast","hostrange","hosts","subnets","wildcard","private","vlsm","ports","hex"}
for t in TECHS:
    for g in t["generators"]:
        if g not in GENS: err.append(f"gen {g}")
    if not t["generators"] and not t["questions"]: err.append(f"{t['id']} no exercises")
if err: print("ERRORS", err); sys.exit(1)
cnt = collections.Counter(c["category"] for c in CARDS); dom = collections.Counter(c["domainId"] for c in CARDS)
print("cards per category", dict(cnt)); print("cards per domain", dict(dom))
out = dict(categories=[dict(id=a, title=b) for a, b in CATEGORIES], cards=CARDS, guides=GUIDES, cheatsheets=SHEETS, techniques=TECHS, subnetLessons=SUBNET_LESSONS)
json.dump(out, open(sys.argv[1] + "/memory.json", "w"), ensure_ascii=False, indent=1)
print(len(CARDS), "cards", len(GUIDES), "guides", len(SHEETS), "sheets", len(TECHS), "techniques")
