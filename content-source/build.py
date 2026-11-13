import json, sys
from lib import LESSONS
import d1,d2,d3,d4,d5,d6
from diagrams import DIAGRAMS
from scenario_diagrams import register
register()
from mechanism_diagrams import register as register_mechanisms
register_mechanisms()
DOM=[
 ("d1","1.0","Network Fundamentals",20,"Devices, topologies, cabling, TCP/UDP, IPv4/IPv6 addressing, wireless principles, virtualization, switching concepts.",
  [("d1c1","Networking Foundations"),("d1c2","Physical Layer and Ethernet"),("d1c3","Transport Layer"),("d1c4","IPv4 Addressing"),("d1c5","IPv6 Addressing"),("d1c6","Clients, Wireless and Virtualization"),("d1c7","Switching Concepts")]),
 ("d2","2.0","Network Access",20,"VLANs, trunking, inter-VLAN routing, CDP/LLDP, EtherChannel, Rapid PVST+, wireless architectures, device management access.",
  [("d2c1","VLANs and Trunking"),("d2c2","Discovery and Link Aggregation"),("d2c3","Spanning Tree"),("d2c4","Wireless LAN Architecture"),("d2c5","Management Access")]),
 ("d3","3.0","IP Connectivity",25,"Routing table, forwarding decisions, static routing, single-area OSPFv2, first hop redundancy.",
  [("d3c1","Routing Essentials"),("d3c2","OSPF"),("d3c3","Gateway Redundancy")]),
 ("d4","4.0","IP Services",10,"NAT, NTP, DHCP, DNS, SNMP, syslog, QoS, SSH, TFTP/FTP.",
  [("d4c1","Addressing Services"),("d4c2","Management and QoS Services")]),
 ("d5","5.0","Security Fundamentals",15,"Threats, security programs, device access, AAA, ACLs, Layer 2 security, VPNs, wireless security.",
  [("d5c1","Security Concepts and Access"),("d5c2","Traffic Filtering and Layer 2 Security"),("d5c3","VPN and Wireless Security")]),
 ("d6","6.0","Automation and Programmability",10,"Automation, controller-based networking and SDN, AI/ML, REST APIs, JSON, Ansible and Terraform.",
  [("d6c1","Automation and SDN"),("d6c2","APIs and Configuration Management")]),
]
TIPS=["Longest prefix match always wins before administrative distance is considered.",
"A switch learns from SOURCE MAC addresses and forwards based on DESTINATION MAC addresses.",
"Standard ACLs near the destination, extended ACLs near the source.",
"The 2.4 GHz non-overlapping channels are 1, 6 and 11.",
"OSPF process IDs are locally significant; area IDs must match.",
"'switchport trunk allowed vlan add' appends — without 'add' you replace the list.",
"Syslog level 4 (Warning) includes levels 0-4. Lower number = more severe.",
"IPv6 has no broadcast; multicast FF02::1 reaches all nodes on the link.",
"DROTHERs stay in 2-Way state with each other on a broadcast segment.",
"BPDU Guard err-disables a PortFast port that receives a BPDU.",
"LACP passive + passive never forms an EtherChannel.",
"Inside local = private inside address; inside global = its public translation.",
"TACACS+ uses TCP 49 and encrypts the whole payload; RADIUS uses UDP 1812/1813.",
"A floating static route needs an AD higher than the route it backs up.",
"In EUI-64, insert FFFE in the middle of the MAC and flip the 7th bit.",
"DHCP snooping must trust the uplink toward the legitimate DHCP server.",
"Shaping buffers excess traffic; policing drops or re-marks it.",
"SVI autostate depends on an active VLAN, an enabled SVI, and an eligible up/forwarding member under the platform rules.",
"401 requires valid authentication credentials and a challenge; 403 means the server refuses the request, not necessarily that authentication succeeded.",
"JSON keys and strings always use double quotes.",
"Ansible is agentless and push-based; Terraform is declarative IaC using HCL.",
"Late collisions on an interface often point to a duplex mismatch.",
"An IPv6 static route with a link-local next hop must include the exit interface.",
"Block size = 256 − mask octet. /27 → 256 − 224 = 32.",
"CAPWAP control uses UDP 5246 and data uses UDP 5247.",
"HSRP default priority is 100 and preemption is off by default.",
"A root bridge has no root port; eligible forwarding ports are normally designated, but disabled or shared-segment backup cases require the actual topology.",
"Private ranges: 10/8, 172.16/12, 192.168/16. 172.32.x.x is public.",
"SNMP agents listen on UDP 161; traps and informs go to UDP 162.",
"Generative AI output must be validated by an engineer before deployment."]
ids={d["id"] for d in DIAGRAMS}
err=[]
lessons_by=[l for l in LESSONS]
for l in LESSONS:
    for d in l["diagrams"]:
        if d not in ids: err.append(f"{l['id']} missing diagram {d}")
    for q in l["quiz"]:
        if not (0<=q["answer"]<len(q["options"])): err.append(q["id"])
    for k in ["simple","why","summary"]:
        if not l[k]: err.append(f"{l['id']} empty {k}")
    if len(l["deep"])<3: err.append(l["id"]+" deep")
used={d for l in LESSONS for d in l["diagrams"]}

for d in DIAGRAMS:
    nid={n["id"] for n in d["nodes"]}
    for e in d["edges"]:
        if e["src"] not in nid or e["dst"] not in nid: err.append(f"{d['id']} edge {e}")
if err: print("ERR",err); sys.exit(1)
domains=[]
for did,num,title,w,desc,chs in DOM:
    chapters=[]
    for cid,ct in chs:
        ls=[l["id"] for l in LESSONS if l["chapterId"]==cid]
        if not ls: print("empty chapter",cid)
        chapters.append(dict(id=cid,title=ct,lessonIds=ls))
    domains.append(dict(id=did,number=num,title=title,weight=w,description=desc,chapters=chapters))
assigned={x for d in domains for c in d["chapters"] for x in c["lessonIds"]}
assert assigned=={l["id"] for l in LESSONS}
cards=[]
for l in LESSONS:
    for i,t in enumerate(l["terms"]):
        cards.append(dict(id=f"{l['id']}-t{i}",front=t["term"],back=t["definition"],lessonId=l["id"],domainId=l["domainId"]))
    for i,m in enumerate(l["memorize"]):
        cards.append(dict(id=f"{l['id']}-m{i}",front=f"Must memorize — {l['title']}",back=m,lessonId=l["id"],domainId=l["domainId"]))
# All-lesson progressive migration; legacy fields remain unchanged.
from migrate_lessons import apply_migration, validate_migration
apply_migration(LESSONS)
validate_migration(LESSONS, {d["id"] for d in DIAGRAMS})
used.update(b['diagram'] for l in LESSONS for stage in l['learningStages'] for b in stage['blocks'] if b.get('diagram'))
used.update(b['packet']['diagramId'] for l in LESSONS for stage in l['learningStages'] for b in stage['blocks'] if b.get('packet'))
print("Unreferenced by lesson body (may be quiz-only):", sorted(ids-used))

out=dict(blueprint=dict(exam="CCNA 200-301",version="v1.1",note="Aligned to Cisco's 200-301 v1.1 exam topics (effective Aug 20, 2024). Cisco lists Feb 2, 2027 as the last v1.1 test date and Feb 3, 2027 as the first v2.0 test date. This content is not a v2.0 course; lessons are tagged by v1.1 objective for a deliberate future remap."),
 domains=domains,lessons=LESSONS,flashcards=[],tips=TIPS)
import os
dst=sys.argv[1]
os.makedirs(dst,exist_ok=True)
json.dump(out,open(f"{dst}/curriculum.json","w"),ensure_ascii=False,indent=1)
json.dump(dict(diagrams=DIAGRAMS),open(f"{dst}/diagrams.json","w"),ensure_ascii=False,indent=1)
print(len(LESSONS),"lessons",sum(len(l["quiz"]) for l in LESSONS),"questions",len(cards),"cards",len(DIAGRAMS),"diagrams")
