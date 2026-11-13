"""Authoring helpers for the CCNA question bank.

Option syntax for S():  "option text || why this option is right/wrong"; prefix '*' marks a correct option.
One '*' -> single-answer; two or more -> multiple-select (stem gets "(Choose N.)" automatically).
"""
import ipaddress

TOPICS = {
    # d1 Network Fundamentals
    "components": ("Network components", "d1", "1-01"), "topology": ("Topology architectures", "d1", "1-02"),
    "osi": ("OSI/TCP-IP models", "d1", "1-03"), "cabling": ("Cabling and interface issues", "d1", "1-04"),
    "ethernet-arp": ("Ethernet frames and ARP", "d1", "1-05"), "tcp-udp": ("TCP vs UDP and ports", "d1", "1-06"),
    "binary": ("Binary and hexadecimal", "d1", "1-07"), "ipv4": ("IPv4 and private addressing", "d1", "1-08"),
    "subnetting": ("Subnetting and VLSM", "d1", "1-09"), "ipv6": ("IPv6 addressing", "d1", "1-10"),
    "client-ip": ("Client IP verification", "d1", "1-11"), "wireless-principles": ("Wireless principles", "d1", "1-12"),
    "virtualization": ("Virtualization and cloud", "d1", "1-13"), "switching": ("Switching concepts", "d1", "1-14"),
    # d2 Network Access
    "vlan": ("VLANs", "d2", "2-01"), "trunking": ("Trunking and 802.1Q", "d2", "2-02"),
    "intervlan": ("Inter-VLAN routing", "d2", "2-03"), "cdp-lldp": ("CDP and LLDP", "d2", "2-04"),
    "etherchannel": ("EtherChannel", "d2", "2-05"), "stp": ("Spanning Tree", "d2", "2-06"),
    "wlan-arch": ("Wireless architectures and AP modes", "d2", "2-07"), "wlc-gui": ("WLC GUI configuration", "d2", "2-08"),
    "mgmt-access": ("Device management access", "d2", "2-09"),
    # d3 IP Connectivity
    "routing-table": ("Routing table and forwarding", "d3", "3-01"), "static": ("Static routing", "d3", "3-02"),
    "ospf": ("OSPFv2", "d3", "3-03"), "fhrp": ("First hop redundancy", "d3", "3-04"),
    # d4 IP Services
    "dhcp-dns": ("DHCP and DNS", "d4", "4-01"), "nat": ("NAT and PAT", "d4", "4-02"),
    "mgmt-services": ("NTP, SNMP and Syslog", "d4", "4-03"), "qos": ("QoS", "d4", "4-04"),
    "ssh-files": ("SSH, TFTP and FTP", "d4", "2-09"),
    # d5 Security Fundamentals
    "sec-concepts": ("Threats and security programs", "d5", "5-01"), "aaa": ("Device access, passwords and AAA", "d5", "5-02"),
    "acl": ("Access control lists", "d5", "5-03"), "l2-security": ("Layer 2 security", "d5", "5-04"),
    "vpn": ("VPNs", "d5", "5-05"), "wireless-security": ("Wireless security", "d5", "5-06"),
    # d6 Automation and Programmability
    "automation": ("Automation and controller-based networking", "d6", "6-01"), "ai": ("AI and ML in operations", "d6", "6-02"),
    "rest-json": ("REST APIs and JSON", "d6", "6-03"), "config-mgmt": ("Ansible and Terraform", "d6", "6-04"),
}
FORMATS = {"concept", "scenario", "calculation", "routing-table", "diagram", "protocol-id", "config", "troubleshooting", "command"}
QS = []
_counter = {}


def _new(dom, obj, topic, diff, fmt, stem, qtype, expl, concept, tip, ex, dg):
    assert topic in TOPICS, topic
    assert TOPICS[topic][1] == dom, (topic, dom)
    assert diff in (1, 2, 3), diff
    assert fmt in FORMATS, fmt
    _counter[dom] = _counter.get(dom, 0) + 1
    q = dict(id=f"core-{dom}-{_counter[dom]:03d}", domainId=dom, objective=obj, topic=topic, difficulty=diff, format=fmt,
             type=qtype, stem=stem, exhibit=ex, diagram=dg, explanation=expl, concept=concept, tip=tip,
             lessonId=TOPICS[topic][2], verified=False, shuffle=qtype in ("single", "multi"), options=[], correct=[], whyCorrect="", whyWrong=[], left=[], right=[], items=[])
    QS.append(q)
    return q


def S(dom, obj, topic, diff, fmt, stem, opts, expl, concept, tip="", ex=None, dg=None, verify=None):
    texts, reasons, correct = [], [], []
    for i, o in enumerate(opts):
        assert "||" in o, o
        t, r = (x.strip() for x in o.split("||", 1))
        if t.startswith("*"):
            correct.append(i); t = t[1:].strip()
        texts.append(t); reasons.append(r)
    assert correct, stem
    assert len(set(texts)) == len(texts), ("duplicate options", stem)
    qtype = "single" if len(correct) == 1 else "multi"
    if qtype == "multi":
        assert len(correct) < len(texts)
        stem = f"{stem} (Choose {['', 'one', 'two', 'three', 'four'][len(correct)]}.)"
    q = _new(dom, obj, topic, diff, fmt, stem, qtype, expl, concept, tip, ex, dg)
    q.update(options=texts, correct=correct, whyCorrect=" ".join(reasons[i] for i in correct),
             whyWrong=["" if i in correct else reasons[i] for i in range(len(texts))])
    if verify is not None:
        q["verified"] = True
        exp = verify() if callable(verify) else verify
        exp = exp if isinstance(exp, (list, tuple, set)) else [exp]
        got = sorted(texts[i] for i in correct)
        assert sorted(str(e) for e in exp) == got, f"VERIFY FAILED {q['id']}: expected {sorted(exp)} got {got}"
    return q


def TF(dom, obj, topic, diff, fmt, statement, truth, expl, concept, tip="", ex=None, dg=None):
    q = _new(dom, obj, topic, diff, fmt, f"True or false: {statement}", "tf", expl, concept, tip, ex, dg)
    word = "true" if truth else "false"
    q.update(options=["True", "False"], correct=[0 if truth else 1], whyCorrect=f"The statement is {word}. {expl}",
             whyWrong=["", f"The statement is {word}, so this choice is incorrect."] if truth else
                      [f"The statement is {word}, so this choice is incorrect.", ""])
    return q


def M(dom, obj, topic, diff, fmt, stem, pairs, expl, concept, tip="", ex=None, dg=None):
    left = [a for a, _ in pairs]; right = [b for _, b in pairs]
    assert len(set(left)) == len(left) and len(set(right)) == len(right), stem
    q = _new(dom, obj, topic, diff, fmt, stem, "match", expl, concept, tip, ex, dg)
    q.update(left=left, right=right, whyCorrect="; ".join(f"{a} → {b}" for a, b in pairs))
    return q


def O(dom, obj, topic, diff, fmt, stem, items, expl, concept, tip="", ex=None, dg=None):
    assert len(set(items)) == len(items) and len(items) >= 3
    q = _new(dom, obj, topic, diff, fmt, stem, "order", expl, concept, tip, ex, dg)
    q.update(items=items, whyCorrect=" → ".join(items))
    return q


# ---------- verification helpers (used by calculation questions) ----------
def net(cidr): return str(ipaddress.ip_network(cidr, strict=False).network_address)
def bcast(cidr): return str(ipaddress.ip_network(cidr, strict=False).broadcast_address)
def last_host(cidr): return str(ipaddress.ip_network(cidr, strict=False).broadcast_address - 1)
def hosts(prefix): return str(2 ** (32 - prefix) - 2)
def wildcard(cidr): return str(ipaddress.ip_network(cidr, strict=False).hostmask)
def mask(prefix): return str(ipaddress.ip_network(f"0.0.0.0/{prefix}").netmask)
def v6(addr): return ipaddress.IPv6Address(addr).compressed.upper()
def eui64(prefix, mac):
    m = bytearray(bytes.fromhex(mac.replace(".", "").replace(":", "")))
    m[0] ^= 0x02
    iid = bytes(m[:3]) + b"\xff\xfe" + bytes(m[3:])
    n = ipaddress.IPv6Network(prefix)
    return v6(int(n.network_address) | int.from_bytes(iid, "big"))
def in_net(addr, cidr): return ipaddress.ip_address(addr) in ipaddress.ip_network(cidr, strict=False)
