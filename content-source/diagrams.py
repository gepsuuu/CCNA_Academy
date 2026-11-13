DIAGRAMS = []

def N(id, x, y, w, h, label, kind="neutral", sub="", shape="round"):
    return dict(id=id, x=x, y=y, w=w, h=h, label=label, sub=sub, kind=kind, shape=shape)

def E(a, b, label="", style="solid", arrow="end", kind="neutral"):
    return dict(src=a, dst=b, label=label, style=style, arrow=arrow, kind=kind)

def T(x, y, text, size=22, bold=False, kind="text"):
    return dict(x=x, y=y, text=text, size=size, bold=bold, kind=kind)

def G(id, title, w, h, nodes, edges=(), texts=(), legend=(), explanation=""):
    DIAGRAMS.append(dict(id=id, title=title, width=w, height=h, nodes=list(nodes), edges=list(edges),
                         texts=list(texts), legend=[dict(kind=k, label=l) for k, l in legend], explanation=explanation))

# 1 OSI vs TCP/IP
osi = [("7 Application", "app"), ("6 Presentation", "app"), ("5 Session", "app"), ("4 Transport", "transport"),
       ("3 Network", "network"), ("2 Data Link", "datalink"), ("1 Physical", "physical")]
pdu = ["Data", "Data", "Data", "Segment / Datagram", "Packet", "Frame", "Bits"]
nodes = []
for i, (l, k) in enumerate(osi):
    nodes.append(N(f"o{i}", 30, 70 + i * 74, 300, 64, l, k))
    nodes.append(N(f"p{i}", 360, 70 + i * 74, 260, 64, pdu[i], "neutral", shape="pill"))
nodes += [N("t1", 650, 70, 320, 212, "Application", "app", "HTTP, DNS, DHCP, SSH, SNMP"),
          N("t2", 650, 292, 320, 64, "Transport", "transport", "TCP, UDP"),
          N("t3", 650, 366, 320, 64, "Internet", "network", "IPv4, IPv6, ICMP"),
          N("t4", 650, 440, 320, 138, "Network Access (Link)", "datalink", "Ethernet, 802.11, ARP*")]
G("osi_tcpip", "OSI vs TCP/IP model", 1000, 600, nodes,
  texts=[T(180, 35, "OSI model", 26, True), T(490, 35, "PDU name", 26, True), T(810, 35, "TCP/IP model", 26, True)],
  legend=[("app", "Upper layers (application data)"), ("transport", "Transport (ports)"), ("network", "Network (IP addressing)"),
          ("datalink", "Data link (MAC/frames)"), ("physical", "Physical (signals)")],
  explanation="Left: the 7 OSI layers. Middle: the PDU name at each layer. Right: the 4-layer TCP/IP model aligned to the OSI layers it covers. *ARP is often described as working between Layers 2 and 3.")

# 2 Encapsulation
rows = [[("Application data", "app", 560)],
        [("TCP hdr", "transport", 140), ("Application data", "app", 560)],
        [("IP hdr", "network", 120), ("TCP hdr", "transport", 140), ("Application data", "app", 560)],
        [("Eth hdr", "datalink", 120), ("IP hdr", "network", 120), ("TCP hdr", "transport", 140), ("Application data", "app", 560), ("FCS", "datalink", 70)],
        [("1 0 1 1 0 0 1 0 1 1 0 1 0 0 1 1 ...", "physical", 1010)]]
names = ["Data (L7)", "Segment (L4)", "Packet (L3)", "Frame (L2)", "Bits (L1)"]
nodes = []
for r, row in enumerate(rows):
    total = sum(w for _, _, w in row)
    x = 1250 - total
    for c, (l, k, w) in enumerate(row):
        nodes.append(N(f"e{r}{c}", x, 40 + r * 100, w - 6, 70, l, k, shape="rect"))
        x += w
texts = [T(110, 75 + r * 100, n, 22, True) for r, n in enumerate(names)]
G("encapsulation", "Encapsulation down the stack", 1270, 540, nodes, texts=texts,
  legend=[("app", "Application data"), ("transport", "TCP/UDP header"), ("network", "IP header"), ("datalink", "Ethernet header / FCS trailer"), ("physical", "Signals on the medium")],
  explanation="Each layer wraps the data from the layer above with its own header. Ethernet also adds a trailer (FCS). The receiver removes them in reverse order (de-encapsulation).")

# 3 Ethernet frame
f = [("Preamble", "7 B", "physical", 120), ("SFD", "1 B", "physical", 70), ("Destination MAC", "6 B", "datalink", 170), ("Source MAC", "6 B", "datalink", 170),
     ("Type", "2 B", "network", 90), ("Data (payload)", "46-1500 B", "app", 250), ("FCS", "4 B", "security", 90)]
nodes = []; x = 20
for i, (l, s, k, w) in enumerate(f):
    nodes.append(N(f"f{i}", x, 110, w - 4, 110, l, k, s, shape="rect")); x += w
G("ethernet_frame", "Ethernet II frame", 1000, 360, nodes,
  texts=[T(115, 80, "L1 sync (not in frame size)", 16), T(560, 80, "Frame: 64 - 1518 bytes (untagged)", 20, True),
         T(500, 280, "Type examples: 0x0800 IPv4, 0x86DD IPv6, 0x0806 ARP. 802.1Q tag (4 B) goes between Source MAC and Type.", 18)],
  legend=[("physical", "Preamble/SFD"), ("datalink", "Addressing"), ("network", "Type (EtherType)"), ("app", "Payload"), ("security", "Error detection")],
  explanation="Switches read the destination MAC to forward and the source MAC to learn. The FCS lets the receiver discard corrupted frames (counted as CRC errors).")

# 4 TCP handshake
G("tcp_handshake", "TCP three-way handshake", 1000, 520,
  [N("c", 40, 30, 220, 70, "Client", "host", "10.1.1.10:51515"), N("s", 740, 30, 220, 70, "Server", "server", "203.0.113.5:443"),
   N("m1", 300, 140, 400, 60, "SYN  (seq = x)  →", "transport", shape="pill"),
   N("m2", 300, 230, 400, 60, "←  SYN-ACK  (seq = y, ack = x+1)", "transport", shape="pill"),
   N("m3", 300, 320, 400, 60, "ACK  (ack = y+1)  →", "transport", shape="pill"),
   N("m4", 300, 420, 400, 60, "Connection established: data flows", "ok", shape="pill")],
  [E("c", "m1", "", "dashed", "none"), E("m1", "s"), E("s", "m2", "", "dashed", "none"), E("m2", "c"), E("c", "m3", "", "dashed", "none"), E("m3", "s")],
  legend=[("transport", "TCP control segments"), ("ok", "Established session")],
  explanation="The client initiates with SYN, the server answers with SYN-ACK, the client completes with ACK. Sequence and acknowledgment numbers then track every byte for reliable delivery.")

# 5 IPv4 structure
G("ipv4_structure", "IPv4 address structure (/24 example)", 1000, 470,
  [N("a1", 40, 70, 220, 70, "192", "network", "11000000"), N("a2", 270, 70, 220, 70, "168", "network", "10101000"),
   N("a3", 500, 70, 220, 70, "10", "network", "00001010"), N("a4", 730, 70, 220, 70, "25", "host", "00011001"),
   N("m1", 40, 200, 220, 70, "255", "mask", "11111111"), N("m2", 270, 200, 220, 70, "255", "mask", "11111111"),
   N("m3", 500, 200, 220, 70, "255", "mask", "11111111"), N("m4", 730, 200, 220, 70, "0", "neutral", "00000000"),
   N("net", 40, 330, 680, 60, "Network portion: 24 bits → 192.168.10.0", "network", shape="pill"),
   N("hst", 730, 330, 220, 60, "Host: 8 bits", "host", shape="pill")],
  texts=[T(500, 40, "IP address 192.168.10.25", 22, True), T(500, 175, "Subnet mask 255.255.255.0 (/24)", 22, True),
         T(500, 430, "Network 192.168.10.0  •  Broadcast 192.168.10.255  •  Usable .1 - .254 (2^8 - 2 = 254 hosts)", 19)],
  legend=[("network", "Network bits"), ("host", "Host bits"), ("mask", "Mask 1-bits (network)")],
  explanation="Each octet is 8 bits. Mask bits set to 1 mark the network portion; 0 bits mark the host portion. All-zero host bits = network address, all-one host bits = broadcast.")

# 6 Subnetting
sub = [("192.168.1.0/26", ".1 - .62", "bc .63"), ("192.168.1.64/26", ".65 - .126", "bc .127"),
       ("192.168.1.128/26", ".129 - .190", "bc .191"), ("192.168.1.192/26", ".193 - .254", "bc .255")]
kinds = ["vlan10", "vlan20", "vlan30", "vlan99"]
nodes = [N("big", 40, 40, 920, 70, "192.168.1.0/24 — 254 usable hosts", "neutral", "Borrow 2 host bits → 4 subnets of 64 addresses (block size 256 - 192 = 64)")]
for i, (a, b, c) in enumerate(sub):
    nodes.append(N(f"s{i}", 40 + i * 232, 200, 220, 140, a, kinds[i], f"hosts {b}\n{c}"))
G("subnetting", "Subnetting 192.168.1.0/24 into /26", 1000, 430, nodes,
  [E("big", f"s{i}", "", "solid", "end") for i in range(4)],
  texts=[T(500, 390, "Mask 255.255.255.192 • 62 usable hosts per subnet (2^6 - 2)", 20, True)],
  legend=[("vlan10", "Subnet 1"), ("vlan20", "Subnet 2"), ("vlan30", "Subnet 3"), ("vlan99", "Subnet 4")],
  explanation="Subnets start at multiples of the block size (0, 64, 128, 192). Network = first address, broadcast = next subnet - 1, usable hosts lie in between.")

# 7 IPv6 structure
G("ipv6_structure", "IPv6 global unicast address (/64 LAN)", 1000, 470,
  [N("g", 40, 90, 420, 90, "Global routing prefix", "network", "48 bits (assigned by ISP/RIR)"),
   N("sid", 470, 90, 140, 90, "Subnet ID", "vlan20", "16 bits"),
   N("iid", 620, 90, 340, 90, "Interface ID", "host", "64 bits (manual, EUI-64, random)"),
   N("ex", 40, 220, 920, 60, "2001:0DB8:ACAD : 0001 : 0000:0000:0000:0001  →  2001:DB8:ACAD:1::1/64", "neutral", shape="pill"),
   N("t1", 40, 320, 220, 70, "GUA 2000::/3", "network"), N("t2", 270, 320, 220, 70, "ULA FC00::/7", "vlan30"),
   N("t3", 500, 320, 220, 70, "Link-local FE80::/10", "datalink"), N("t4", 730, 320, 230, 70, "Multicast FF00::/8", "app")],
  texts=[T(500, 50, "128 bits = 8 hextets of 16 bits", 22, True), T(500, 430, "Rules: drop leading zeros; replace one run of all-zero hextets with ::", 19)],
  legend=[("network", "Prefix (network)"), ("vlan20", "Subnet ID"), ("host", "Interface ID")],
  explanation="A typical LAN uses /64: 48-bit global routing prefix + 16-bit subnet ID + 64-bit interface ID. The bottom row shows the main address types by prefix.")

# 8 Wireless architecture
G("wireless", "Controller-based wireless (split-MAC)", 1000, 560,
  [N("wlc", 380, 20, 240, 80, "WLC", "server", "Trunk / static LAG"),
   N("sw", 380, 170, 240, 70, "Access switch", "switch"),
   N("ap1", 80, 320, 200, 70, "AP1 (Local)", "router", "Ch 1"), N("ap2", 400, 320, 200, 70, "AP2 (Local)", "router", "Ch 6"),
   N("ap3", 720, 320, 200, 70, "AP3 (Local)", "router", "Ch 11"),
   N("c1", 40, 460, 140, 60, "Laptop", "host"), N("c2", 200, 460, 140, 60, "Phone", "host"), N("c3", 430, 460, 140, 60, "Tablet", "host"), N("c4", 760, 460, 140, 60, "Laptop", "host")],
  [E("wlc", "sw", "trunk", arrow="none"), E("sw", "ap1", "access port", arrow="none"), E("sw", "ap2", "access", arrow="none"), E("sw", "ap3", "access", arrow="none"),
   E("ap1", "wlc", "CAPWAP", "dashed", "both", "accent"), E("ap3", "wlc", "CAPWAP", "dashed", "both", "accent"),
   E("c1", "ap1", "SSID Corp", "dashed", "none", "app"), E("c2", "ap1", "", "dashed", "none", "app"), E("c3", "ap2", "", "dashed", "none", "app"), E("c4", "ap3", "", "dashed", "none", "app")],
  legend=[("accent", "CAPWAP tunnel (UDP 5246 control / 5247 data)"), ("app", "802.11 RF association"), ("neutral", "Wired Ethernet")],
  explanation="Lightweight APs in Local mode tunnel client traffic to the WLC over CAPWAP, so they sit on access ports. Adjacent 2.4 GHz APs use non-overlapping channels 1, 6, 11. All APs share the SSID, forming an ESS for roaming.")

# 9 MAC learning
G("mac_learning", "MAC learning and forwarding", 1000, 520,
  [N("sw", 360, 180, 280, 90, "Switch", "switch", "VLAN 1"),
   N("a", 40, 40, 200, 70, "PC-A", "host", "AAAA.AAAA.AAAA"), N("b", 760, 40, 200, 70, "PC-B", "host", "BBBB.BBBB.BBBB"),
   N("c", 40, 330, 200, 70, "PC-C", "host", "CCCC.CCCC.CCCC"),
   N("tbl", 560, 330, 400, 150, "MAC table", "note", "AAAA.AAAA.AAAA → Gi0/1 (learned)\nBBBB.BBBB.BBBB → ? (unknown)\nAging: 300 s")],
  [E("a", "sw", "Gi0/1: frame src A, dst B", kind="accent"), E("sw", "b", "flood (unknown B)", "dashed", kind="warn"), E("sw", "c", "flood", "dashed", kind="warn")],
  legend=[("accent", "Incoming frame (learn source)"), ("warn", "Flooded copies"), ("note", "MAC address table")],
  explanation="The switch learns A's MAC on Gi0/1 from the source address. B is unknown, so the frame is flooded out every port in the VLAN except Gi0/1. When B replies, the switch learns B's port and future frames are forwarded only there.")

# 10 VLAN trunk
G("vlan_trunk", "VLANs across an 802.1Q trunk", 1000, 520,
  [N("s1", 120, 200, 240, 80, "SW1", "switch"), N("s2", 640, 200, 240, 80, "SW2", "switch"),
   N("h1", 20, 40, 170, 70, "PC1 VLAN 10", "vlan10"), N("h2", 210, 40, 170, 70, "PC2 VLAN 20", "vlan20"),
   N("h3", 620, 40, 170, 70, "PC3 VLAN 10", "vlan10"), N("h4", 810, 40, 170, 70, "PC4 VLAN 20", "vlan20"),
   N("ph", 120, 380, 240, 70, "IP phone (voice VLAN 30)", "vlan30", "PC behind phone: VLAN 10")],
  [E("h1", "s1", "access", arrow="none", kind="vlan10"), E("h2", "s1", "access", arrow="none", kind="vlan20"),
   E("h3", "s2", "access", arrow="none", kind="vlan10"), E("h4", "s2", "access", arrow="none", kind="vlan20"),
   E("s1", "s2", "Trunk: tagged 10, 20, 30 • native 99 untagged", arrow="both", kind="accent"), E("ph", "s1", "access + voice VLAN", arrow="none", kind="vlan30")],
  legend=[("vlan10", "VLAN 10 (Data)"), ("vlan20", "VLAN 20 (HR)"), ("vlan30", "VLAN 30 (Voice)"), ("accent", "802.1Q trunk")],
  explanation="Access ports carry one untagged VLAN. The trunk carries all VLANs, tagging each frame with its VLAN ID; only the native VLAN crosses untagged. PC1 and PC3 share a broadcast domain; PC1 and PC2 do not.")

# 11 dot1q tag
f = [("Dst MAC", "6 B", "datalink", 140), ("Src MAC", "6 B", "datalink", 140), ("802.1Q tag", "4 B", "accent", 160), ("Type", "2 B", "network", 90), ("Payload", "46-1500 B", "app", 240), ("FCS", "4 B", "security", 90)]
nodes = []; x = 60
for i, (l, s, k, w) in enumerate(f):
    nodes.append(N(f"q{i}", x, 60, w - 4, 90, l, k, s, shape="rect")); x += w
nodes += [N("tp", 150, 240, 200, 80, "TPID", "accent", "16 bits = 0x8100"), N("pcp", 360, 240, 150, 80, "PCP", "vlan30", "3 bits (CoS)"),
          N("dei", 520, 240, 120, 80, "DEI", "neutral", "1 bit"), N("vid", 650, 240, 200, 80, "VLAN ID", "vlan10", "12 bits (0-4095)")]
G("dot1q_tag", "802.1Q tag inserted into the frame", 1000, 400, nodes,
  [E("q2", "tp", "", "dashed", "none"), E("q2", "vid", "", "dashed", "none")],
  texts=[T(500, 370, "Usable VLAN IDs 1-4094 (0 and 4095 reserved). Native VLAN frames are sent without this tag.", 19)],
  legend=[("accent", "Tag / TPID"), ("vlan30", "Priority (CoS)"), ("vlan10", "VLAN identifier")],
  explanation="The 4-byte tag sits between the source MAC and Type fields. The receiving switch reads the 12-bit VLAN ID to keep VLANs separate across the trunk.")

# 12 Inter-VLAN
G("intervlan", "Inter-VLAN routing options", 1000, 500,
  [N("r", 60, 30, 380, 90, "Router-on-a-stick", "router", "Gi0/0.10 dot1Q 10 • Gi0/0.20 dot1Q 20"),
   N("sw1", 140, 210, 220, 70, "L2 switch", "switch"),
   N("p1", 40, 370, 180, 70, "PC VLAN 10", "vlan10", "gw 10.10.10.1"), N("p2", 280, 370, 180, 70, "PC VLAN 20", "vlan20", "gw 10.20.20.1"),
   N("l3", 560, 30, 400, 90, "Layer 3 switch (ip routing)", "switch", "SVI Vlan10 10.10.10.1 • SVI Vlan20 10.20.20.1"),
   N("p3", 560, 370, 180, 70, "PC VLAN 10", "vlan10"), N("p4", 780, 370, 180, 70, "PC VLAN 20", "vlan20")],
  [E("r", "sw1", "802.1Q trunk", arrow="both", kind="accent"), E("p1", "sw1", "", arrow="none", kind="vlan10"), E("p2", "sw1", "", arrow="none", kind="vlan20"),
   E("p3", "l3", "access VLAN 10", arrow="none", kind="vlan10"), E("p4", "l3", "access VLAN 20", arrow="none", kind="vlan20")],
  legend=[("vlan10", "VLAN 10"), ("vlan20", "VLAN 20"), ("accent", "Trunk to router")],
  explanation="Left: a router with one subinterface per VLAN over a trunk. Right: a Layer 3 switch routes internally between SVIs. Each VLAN's hosts use their gateway IP on the routing device.")

# 13 STP
G("stp", "Spanning Tree roles (VLAN 10)", 1000, 560,
  [N("s1", 380, 30, 240, 90, "SW1 — ROOT", "ok", "Priority 24586 (24576 + 10)"),
   N("s2", 60, 380, 240, 90, "SW2", "switch", "Priority 32778"), N("s3", 700, 380, 240, 90, "SW3", "switch", "Priority 32778, higher MAC"),
   N("l1", 140, 175, 170, 50, "DP  ⟷  RP", "note", shape="pill"), N("l2", 690, 175, 170, 50, "DP  ⟷  RP", "note", shape="pill"),
   N("l3", 380, 470, 240, 60, "SW2: DP  •  SW3: ALT", "danger", "blocked (discarding)", shape="pill")],
  [E("s1", "s2", "cost 4 (1G)", arrow="none", kind="ok"), E("s1", "s3", "cost 4 (1G)", arrow="none", kind="ok"), E("s2", "s3", "", "dashed", "none", kind="danger")],
  legend=[("ok", "Root bridge / forwarding links"), ("danger", "Alternate (blocked) port"), ("note", "Port roles: DP designated, RP root, ALT alternate")],
  explanation="SW1 has the lowest bridge ID and becomes root; all its ports are designated. SW2 and SW3 choose their direct links as root ports (cost 4). On the SW2-SW3 link, SW2 (lower bridge ID) owns the designated port, so SW3's port becomes alternate and discards, breaking the loop.")

# 14 Routing
G("routing", "Packet forwarding across routers", 1000, 540,
  [N("pc", 20, 60, 170, 80, "PC", "host", "10.1.1.10/24\ngw 10.1.1.1"),
   N("r1", 280, 60, 180, 80, "R1", "router", "Gi0/0 10.1.1.1\nGi0/1 10.0.12.1"),
   N("r2", 560, 60, 180, 80, "R2", "router", "Gi0/1 10.0.12.2\nGi0/0 10.2.2.1"),
   N("sv", 820, 60, 160, 80, "Server", "server", "10.2.2.10"),
   N("tbl", 120, 260, 760, 220, "R1 routing table", "note",
     "C  10.1.1.0/24 is directly connected, Gi0/0\nL  10.1.1.1/32 is directly connected, Gi0/0\nC  10.0.12.0/30 is directly connected, Gi0/1\nO  10.2.2.0/24 [110/2] via 10.0.12.2, Gi0/1\nS* 0.0.0.0/0 [1/0] via 10.0.12.2", shape="rect")],
  [E("pc", "r1", "", arrow="end", kind="accent"), E("r1", "r2", "", arrow="end", kind="accent"), E("r2", "sv", "", arrow="end", kind="accent"),
   E("r1", "tbl", "", "dashed", "none")],
  texts=[T(235, 30, "10.1.1.0/24", 17, True), T(510, 30, "10.0.12.0/30", 17, True), T(780, 30, "10.2.2.0/24", 17, True)],
  legend=[("accent", "Packet path (dst 10.2.2.10)"), ("note", "Routing table lookup")],
  explanation="The PC sends to its gateway (R1). R1's longest match for 10.2.2.10 is the OSPF route 10.2.2.0/24 [AD 110 / cost 2] via 10.0.12.2. R2 delivers it on its connected network. MAC addresses change each hop; IPs stay the same.")

# 15 OSPF neighbor states
st = [("Down", "neutral"), ("Init", "neutral"), ("2-Way", "vlan20"), ("ExStart", "warn"), ("Exchange", "warn"), ("Loading", "warn"), ("Full", "ok")]
desc = ["No Hellos", "Hello received", "Own RID seen\n(DR/BDR elected)", "Master/slave", "DBD exchange", "LSR / LSU", "LSDBs synced"]
nodes = []
for i, (s, k) in enumerate(st):
    nodes.append(N(f"st{i}", 15 + i * 140, 70, 125, 80, s, k, desc[i]))
nodes += [N("dr", 40, 300, 280, 80, "DR", "ok", "highest priority, then RID"), N("bdr", 360, 300, 280, 80, "BDR", "vlan20", "backup"),
          N("do", 680, 300, 280, 80, "DROTHERs", "neutral", "2-Way with each other")]
G("ospf_states", "OSPF neighbor states and DR/BDR", 1000, 460, nodes,
  [E(f"st{i}", f"st{i+1}") for i in range(6)] + [E("do", "dr", "FULL", "dashed", "both", "ok"), E("do", "bdr", "", "dashed", "both", "ok")],
  texts=[T(500, 35, "Hellos to 224.0.0.5 every 10 s • Dead 40 s (broadcast / point-to-point)", 20, True),
         T(500, 250, "Broadcast (Ethernet) segment", 20, True), T(500, 425, "Must match: area, subnet, Hello/Dead, auth, stub flag • unique RID • MTU for Full", 18)],
  legend=[("vlan20", "Neighbor (2-Way)"), ("warn", "Database synchronization"), ("ok", "Fully adjacent")],
  explanation="Routers progress from Down to Full. On Ethernet, a DR and BDR are elected; DROTHERs become Full only with the DR and BDR and remain 2-Way with each other. Point-to-point links have no DR/BDR.")

# 16 FHRP
G("fhrp", "First Hop Redundancy (HSRP example)", 1000, 500,
  [N("r1", 120, 40, 260, 90, "R1 — Active", "ok", "10.1.1.2 • priority 110 • preempt"),
   N("r2", 620, 40, 260, 90, "R2 — Standby", "neutral", "10.1.1.3 • priority 100"),
   N("v", 330, 200, 340, 70, "Virtual IP 10.1.1.1", "accent", "vMAC 0000.0C07.AC01", shape="pill"),
   N("sw", 380, 320, 240, 60, "Switch", "switch"),
   N("h", 380, 420, 240, 60, "Hosts: gateway 10.1.1.1", "host")],
  [E("r1", "v", "forwards", kind="ok"), E("r2", "v", "takes over on failure", "dashed", kind="warn"), E("v", "sw", "", arrow="none"), E("sw", "h", "", arrow="none")],
  legend=[("ok", "Active forwarder"), ("warn", "Failover path"), ("accent", "Shared virtual gateway")],
  explanation="Hosts point to the virtual IP. The active router answers for the virtual MAC. If it fails, the standby becomes active and continues forwarding without any host reconfiguration.")

# 17 DHCP DORA
G("dhcp_dora", "DHCP DORA with relay", 1000, 520,
  [N("c", 20, 40, 200, 80, "Client", "host", "UDP 68"), N("r", 400, 40, 200, 80, "Router (relay)", "router", "ip helper-address 10.0.0.50"),
   N("s", 780, 40, 200, 80, "DHCP server", "server", "10.0.0.50 • UDP 67"),
   N("d", 20, 170, 560, 55, "1 DISCOVER (broadcast) →  relayed as unicast →", "vlan10", shape="pill"),
   N("o", 420, 245, 560, 55, "← 2 OFFER (proposed IP, mask, gw, DNS)", "vlan20", shape="pill"),
   N("q", 20, 320, 560, 55, "3 REQUEST (broadcast) →  relayed →", "vlan10", shape="pill"),
   N("a", 420, 395, 560, 55, "← 4 ACK (lease confirmed)", "vlan20", shape="pill")],
  texts=[T(500, 490, "The relay sets giaddr so the server chooses the correct pool for the client's subnet.", 19)],
  legend=[("vlan10", "Client → server"), ("vlan20", "Server → client")],
  explanation="Discover, Offer, Request, Acknowledge. Routers do not forward broadcasts, so the client-facing interface uses ip helper-address to relay DHCP messages to a server on another subnet.")

# 18 NAT/PAT
G("nat_pat", "PAT (NAT overload) at the internet edge", 1000, 560,
  [N("h1", 20, 40, 220, 70, "PC1", "host", "192.168.1.10"), N("h2", 20, 160, 220, 70, "PC2", "host", "192.168.1.11"),
   N("r", 380, 90, 240, 100, "Edge router", "router", "Gi0/0 ip nat inside\nGi0/1 ip nat outside"),
   N("w", 760, 90, 220, 100, "Web server", "cloud", "198.51.100.80:443"),
   N("t", 20, 300, 960, 210, "show ip nat translations", "note",
     "Pro  Inside global          Inside local           Outside global\ntcp  203.0.113.2:51000    192.168.1.10:51000    198.51.100.80:443\ntcp  203.0.113.2:51001    192.168.1.11:51000    198.51.100.80:443", shape="rect")],
  [E("h1", "r", "", kind="accent"), E("h2", "r", "", kind="accent"), E("r", "w", "src 203.0.113.2", kind="ok"), E("r", "t", "", "dashed", "none")],
  texts=[T(310, 260, "INSIDE (private)", 20, True), T(800, 260, "OUTSIDE (internet)", 20, True)],
  legend=[("accent", "Inside local addresses"), ("ok", "Translated to inside global"), ("note", "Translation table")],
  explanation="Both PCs share public address 203.0.113.2. PAT keeps sessions unique by translating source ports when needed (51000 → 51001 for PC2). Returning traffic is matched to the table and translated back.")

# 19 QoS
G("qos", "QoS tools along the path", 1000, 420,
  [N("c", 20, 100, 170, 100, "Classify", "vlan10", "ACL / NBAR"), N("m", 210, 100, 170, 100, "Mark", "vlan20", "CoS / DSCP\nEF = 46"),
   N("q", 400, 100, 190, 100, "Queue", "accent", "LLQ priority\nCBWFQ"), N("ca", 610, 100, 180, 100, "Avoid", "warn", "WRED"),
   N("ps", 810, 100, 170, 100, "Police / Shape", "danger", "drop or buffer"),
   N("tb", 20, 280, 360, 70, "Trust boundary at phone / access switch", "note", shape="pill"),
   N("v", 420, 280, 560, 70, "Voice targets: delay ≤150 ms • jitter ≤30 ms • loss ≤1%", "note", shape="pill")],
  [E("c", "m"), E("m", "q"), E("q", "ca"), E("ca", "ps")],
  legend=[("vlan10", "Identify traffic"), ("vlan20", "Mark headers"), ("accent", "Schedule under congestion"), ("danger", "Rate control")],
  explanation="Traffic is classified, marked, queued (voice in the priority queue), protected from synchronized drops by WRED, and rate-controlled by policing (drop/re-mark) or shaping (buffer).")

# 20 ACL flow
G("acl_flow", "ACL processing (inbound)", 1000, 560,
  [N("p", 20, 40, 200, 70, "Packet arrives", "host", "Gi0/1 in"),
   N("a1", 300, 40, 400, 70, "10 permit tcp 10.1.1.0 0.0.0.255 host 10.2.2.10 eq 443", "vlan10", shape="rect"),
   N("a2", 300, 160, 400, 70, "20 deny ip any host 10.2.2.10", "vlan10", shape="rect"),
   N("a3", 300, 280, 400, 70, "30 permit ip any any", "vlan10", shape="rect"),
   N("a4", 300, 400, 400, 70, "(implicit) deny any", "danger", shape="rect"),
   N("fw", 800, 100, 180, 70, "Forward", "ok"), N("dr", 800, 400, 180, 70, "Drop", "danger")],
  [E("p", "a1"), E("a1", "a2", "no match"), E("a2", "a3", "no match"), E("a3", "a4", "no match"),
   E("a1", "fw", "match", kind="ok"), E("a3", "fw", "match", kind="ok"), E("a2", "dr", "match", kind="danger"), E("a4", "dr", "", kind="danger")],
  legend=[("vlan10", "Configured entries (top-down)"), ("ok", "Permit"), ("danger", "Deny / implicit deny")],
  explanation="Entries are checked in sequence order. The first match decides and processing stops. If nothing matches, the hidden implicit deny drops the packet.")

# 21 L2 security
G("l2_security", "DHCP snooping, DAI and port security", 1000, 520,
  [N("srv", 380, 20, 240, 70, "Legit DHCP server", "server"),
   N("sw", 360, 180, 280, 90, "Access switch", "switch", "DHCP snooping + DAI on VLAN 10"),
   N("u1", 20, 380, 220, 80, "User PC", "host", "port security max 1"),
   N("rg", 390, 380, 220, 80, "Rogue DHCP", "danger", "OFFER dropped"),
   N("at", 760, 380, 220, 80, "Attacker", "danger", "fake ARP dropped"),
   N("bt", 700, 120, 280, 90, "Binding table", "note", "MAC • IP • VLAN • port • lease")],
  [E("srv", "sw", "TRUSTED uplink", arrow="both", kind="ok"), E("u1", "sw", "untrusted", arrow="both"),
   E("rg", "sw", "untrusted", "dashed", kind="danger"), E("at", "sw", "untrusted", "dashed", kind="danger"), E("sw", "bt", "", "dashed", "none")],
  legend=[("ok", "Trusted port"), ("danger", "Blocked attack"), ("note", "Snooping database used by DAI")],
  explanation="Server messages are allowed only on trusted ports, so the rogue server's OFFERs are dropped. DAI checks ARP on untrusted ports against the binding table. Port security limits MAC addresses per port.")

# 22 VPN
G("vpn", "Site-to-site and remote-access VPNs", 1000, 480,
  [N("hq", 20, 60, 220, 90, "HQ firewall", "security", "VPN gateway"), N("br", 760, 60, 220, 90, "Branch router", "router", "VPN gateway"),
   N("net", 380, 60, 240, 90, "Internet", "cloud"),
   N("ru", 760, 300, 220, 90, "Remote user", "host", "VPN client (TLS / IPsec)")],
  [E("hq", "net", "", arrow="none"), E("net", "br", "", arrow="none"), E("hq", "br", "IPsec site-to-site tunnel (ESP)", "dashed", "both", "accent"),
   E("ru", "hq", "Remote-access tunnel", "dashed", "both", "ok")],
  texts=[T(500, 440, "IPsec: confidentiality • integrity • authentication • anti-replay  |  IKE UDP 500, NAT-T UDP 4500", 18)],
  legend=[("accent", "Site-to-site (gateway to gateway)"), ("ok", "Remote access (client to gateway)")],
  explanation="Site-to-site tunnels are permanent and transparent to hosts. Remote-access tunnels are built on demand by client software on the user's device.")

# 23 802.1X
G("dot1x", "802.1X / WPA-Enterprise roles", 1000, 380,
  [N("s", 20, 140, 240, 100, "Supplicant", "host", "Laptop / phone"), N("a", 380, 140, 240, 100, "Authenticator", "router", "AP / WLC or switch"),
   N("as", 740, 140, 240, 100, "Authentication server", "server", "RADIUS (e.g., ISE)")],
  [E("s", "a", "", arrow="both", kind="accent"), E("a", "as", "", arrow="both", kind="ok")],
  texts=[T(320, 100, "EAP (EAPOL)", 17, True), T(680, 100, "RADIUS UDP 1812/1813", 17, True), T(500, 300, "Personal mode skips RADIUS: WPA2 uses PSK; WPA3 uses SAE.", 19)],
  legend=[("accent", "EAP exchange"), ("ok", "RADIUS")],
  explanation="The client (supplicant) proves its identity through the authenticator to a RADIUS server. Each user gets unique credentials and encryption keys.")

# 24 SDN
G("sdn", "Controller-based networking (SDN)", 1000, 560,
  [N("apps", 200, 20, 600, 80, "Applications / scripts / Ansible / Terraform", "app"),
   N("ctl", 200, 190, 600, 100, "Controller", "accent", "Catalyst Center • SD-WAN Manager • APIC"),
   N("d1", 40, 400, 200, 80, "Switch", "switch", "data plane"), N("d2", 290, 400, 200, 80, "Router", "router", "data plane"),
   N("d3", 540, 400, 200, 80, "WLC / AP", "server", "data plane"), N("d4", 780, 400, 200, 80, "Firewall", "security", "data plane")],
  [E("apps", "ctl", "Northbound API (REST/JSON)", arrow="both", kind="app"),
   E("ctl", "d1", "", arrow="both", kind="ok"), E("ctl", "d2", "", arrow="both", kind="ok"), E("ctl", "d3", "", arrow="both", kind="ok"), E("ctl", "d4", "", arrow="both", kind="ok")],
  texts=[T(500, 340, "Southbound: NETCONF • RESTCONF • OpenFlow • OpFlex • SSH/CLI • SNMP", 19, True), T(500, 520, "Underlay = physical IP network • Overlay = tunnels (e.g., VXLAN) • Fabric = both managed as one", 18)],
  legend=[("app", "Northbound interface"), ("ok", "Southbound interfaces"), ("accent", "Centralized control/management")],
  explanation="Applications request outcomes from the controller via northbound REST APIs. The controller translates intent into device configuration using southbound protocols. Devices still forward traffic in their data planes.")

# 25 REST
G("rest_api", "REST API call and JSON response", 1000, 520,
  [N("cl", 20, 40, 260, 90, "Client (Python script)", "host"),
   N("sv", 720, 40, 260, 90, "Controller REST API", "accent", "HTTPS"),
   N("rq", 20, 180, 960, 70, "GET /api/v1/network-device   •   Authorization: Bearer <token>   •   Accept: application/json", "vlan10", shape="rect"),
   N("rs", 20, 290, 960, 200, "200 OK  •  Content-Type: application/json", "note",
     "{ \"response\": [ { \"hostname\": \"SW1\", \"managementIp\": \"10.0.0.11\", \"reachable\": true, \"uptimeDays\": 42 } ] }", shape="rect")],
  [E("cl", "sv", "request", kind="vlan10"), E("sv", "cl", "", kind="ok")],
  legend=[("vlan10", "HTTP request (verb + URI + headers)"), ("note", "Response: status code + JSON body")],
  explanation="CRUD maps to HTTP verbs (POST, GET, PUT/PATCH, DELETE). The JSON body here has one object containing an array with one object of four key/value pairs: two strings, a boolean, and a number.")

# ---------------- Neutral variants used by the question bank (no answer-revealing labels) ----------------
G("stp_quiz", "Switched topology (VLAN 10)", 1000, 520,
  [N("s1", 380, 30, 240, 90, "SW1", "switch", "Priority 24586 (24576 + 10)"),
   N("s2", 60, 380, 240, 90, "SW2", "switch", "Priority 32778\nMAC 0011.2222.2222"),
   N("s3", 700, 380, 240, 90, "SW3", "switch", "Priority 32778\nMAC 0011.3333.3333")],
  [E("s1", "s2", "1 Gbps", arrow="none"), E("s1", "s3", "1 Gbps", arrow="none"), E("s2", "s3", "1 Gbps", arrow="none")],
  legend=[("switch", "Switch running Rapid PVST+")],
  explanation="All links are 1 Gbps (default cost 4). Determine the root bridge, root ports and designated ports from bridge IDs and costs.")

G("mac_quiz", "Switch forwarding", 1000, 480,
  [N("sw", 360, 180, 280, 90, "Switch", "switch", "VLAN 1"),
   N("a", 40, 40, 200, 70, "PC-A", "host", "AAAA.AAAA.AAAA"), N("b", 760, 40, 200, 70, "PC-B", "host", "BBBB.BBBB.BBBB"),
   N("c", 40, 330, 200, 70, "PC-C", "host", "CCCC.CCCC.CCCC"),
   N("tbl", 560, 330, 400, 120, "MAC address table", "note", "AAAA.AAAA.AAAA → Gi0/1\n(no other entries)")],
  [E("a", "sw", "Gi0/1: src AAAA…, dst BBBB…", kind="accent"), E("sw", "b", "Gi0/2", "dashed", "none"), E("sw", "c", "Gi0/3", "dashed", "none")],
  legend=[("accent", "Incoming frame"), ("note", "Current MAC table")],
  explanation="PC-A sends a unicast frame to PC-B. The table contains only PC-A's entry.")

G("dot1x_quiz", "Enterprise WLAN authentication", 1000, 340,
  [N("s", 20, 120, 260, 100, "Employee laptop", "host"), N("a", 370, 120, 260, 100, "AP / WLC", "router"),
   N("as", 720, 120, 260, 100, "Cisco ISE", "server", "RADIUS server")],
  [E("s", "a", "EAP", arrow="both", kind="accent"), E("a", "as", "RADIUS", arrow="both", kind="ok")],
  legend=[("accent", "EAP exchange"), ("ok", "RADIUS")],
  explanation="The laptop authenticates with its own credentials before it is allowed onto the network.")

f = [("Preamble", "7 B", 120), ("SFD", "1 B", 70), ("Destination MAC", "6 B", 170), ("Source MAC", "6 B", 170), ("Type", "2 B", 90), ("Data", "46-1500 B", 250), ("FCS", "4 B", 90)]
nodes = []; x = 20
for i, (l, s_, w) in enumerate(f):
    nodes.append(N(f"f{i}", x, 110, w - 4, 110, l, "neutral", s_, shape="rect")); x += w
G("frame_quiz", "Ethernet II frame layout", 1000, 300, nodes, legend=[("neutral", "Frame field (size in bytes)")],
  explanation="Fields of an Ethernet II frame in transmission order.")
