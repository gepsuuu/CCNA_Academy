# Flashcard library: 16 categories across all six CCNA domains.
CARDS = []
CATEGORIES = [
    ("definitions", "Networking definitions"), ("protocols", "Protocols and functions"), ("ports", "TCP/UDP port numbers"),
    ("osi", "OSI and TCP/IP layers"), ("acronyms", "Networking acronyms"), ("subnetting", "IPv4 subnetting"),
    ("ipv6", "IPv6 concepts"), ("vlan", "VLANs and trunking"), ("stp", "Spanning Tree"), ("routing", "Routing and routing protocols"),
    ("ospf", "OSPF terminology"), ("acl", "ACL rules and behavior"), ("security", "Network security"),
    ("wireless", "Wireless standards"), ("cisco", "Cisco terminology and IOS"), ("automation", "Automation and programmability"),
]

def C(cat, dom, front, answer, explanation, example, tip, kind="term", diagram=None):
    CARDS.append(dict(id=f"{cat}-{sum(1 for c in CARDS if c['category']==cat)+1:03d}", category=cat, domainId=dom,
                      kind=kind, front=front, diagram=diagram, answer=answer, explanation=explanation, example=example, tip=tip))

# ---------------- Ports (generated from an accurate table) ----------------
PORTS = [
    ("FTP data", "TCP", "20", "File Transfer Protocol data channel (active mode).", "An FTP client downloads a file; the server opens the data connection from port 20 in active mode.", "FTP uses TWO ports: 21 = control (commands), 20 = data."),
    ("FTP control", "TCP", "21", "FTP control connection for commands and authentication (username/password in clear text).", "ftp 192.0.2.10 connects to TCP 21 and logs in.", "21 controls, 20 carries."),
    ("SSH / SCP / SFTP", "TCP", "22", "Encrypted remote CLI access; SCP and SFTP also run over SSH.", "ssh -l admin 10.0.0.1 from a PC to a router configured with 'transport input ssh'.", "SSH (22) replaced Telnet (23): secure comes first, one number lower."),
    ("Telnet", "TCP", "23", "Unencrypted remote CLI access.", "Packet capture of a Telnet session shows the password in clear text.", "Telnet = 23 = 'clear as day' — never use it on production networks."),
    ("SMTP", "TCP", "25", "Sending/relaying email between mail servers.", "A mail server forwards a message to another domain's MX server on TCP 25.", "SMTP Sends Mail To People — port 25."),
    ("TACACS+", "TCP", "49", "Device-administration AAA; encrypts the entire payload.", "Switches authenticate administrators against Cisco ISE using TACACS+.", "TACACS+ is TCP (49) — both start with T."),
    ("DNS", "UDP/TCP", "53", "Name resolution; UDP for normal queries, TCP for zone transfers and large responses.", "nslookup www.cisco.com sends a UDP 53 query to the configured resolver.", "DNS 53 uses BOTH transports — a classic trick question."),
    ("DHCP server", "UDP", "67", "DHCP server listens on UDP 67.", "A DHCP Discover is sent from 0.0.0.0:68 to 255.255.255.255:67.", "Server 67 comes before client 68 — the server is 'first'."),
    ("DHCP client", "UDP", "68", "DHCP client receives offers/acks on UDP 68.", "The client's lease negotiation uses source port 68.", "Client 68 = 'one more' than server 67."),
    ("TFTP", "UDP", "69", "Trivial FTP: no authentication; used to copy IOS images and configs.", "copy running-config tftp: on a router.", "TFTP is 'trivial' — UDP 69, no login."),
    ("HTTP", "TCP", "80", "Unencrypted web traffic.", "Browsing http://192.168.1.1 to reach a device web GUI.", "HTTP 80 — HTTPS is 443."),
    ("POP3", "TCP", "110", "Retrieving email from a server (downloads mail).", "A mail client downloads messages from a POP3 server.", "POP3 = 110."),
    ("NTP", "UDP", "123", "Clock synchronization.", "ntp server 192.0.2.1 makes a router sync time over UDP 123.", "NTP keeps time: 1-2-3, like counting seconds."),
    ("IMAP", "TCP", "143", "Accessing email stored on a server (mail stays on server).", "A phone and laptop both see the same mailbox via IMAP.", "IMAP 143 keeps mail on the server; POP3 110 downloads it."),
    ("SNMP (agent)", "UDP", "161", "Manager polls agents with Get/Set requests.", "An NMS sends SNMP GetRequest to a switch on UDP 161.", "161 = polls, 162 = traps (the 'next' number goes to the manager)."),
    ("SNMP trap/inform", "UDP", "162", "Agents send unsolicited Traps/Informs to the manager.", "A switch sends a linkDown trap to the NMS on UDP 162.", "Traps travel to 162."),
    ("BGP", "TCP", "179", "Border Gateway Protocol peering.", "Two ISPs establish an eBGP session over TCP 179.", "BGP is the only common routing protocol that runs over TCP."),
    ("LDAP", "TCP/UDP", "389", "Directory access (e.g., Active Directory lookups).", "ISE queries AD via LDAP for group membership.", "LDAP 389; LDAPS (secure) 636."),
    ("HTTPS", "TCP", "443", "HTTP over TLS (encrypted web, REST APIs).", "Catalyst Center's REST API is accessed via HTTPS 443.", "HTTPS 443 — the 'S' is for secure."),
    ("Syslog", "UDP", "514", "Sending log messages to a syslog server.", "logging host 192.0.2.50 sends messages over UDP 514.", "Syslog 514."),
    ("IKE (IPsec)", "UDP", "500", "Internet Key Exchange negotiates IPsec security associations.", "Two VPN gateways start IKE negotiation on UDP 500.", "IKE 500; NAT-T moves IPsec to UDP 4500."),
    ("IPsec NAT-T", "UDP", "4500", "Encapsulates IPsec ESP in UDP when NAT is present.", "A remote user behind a home router uses NAT-T on UDP 4500.", "4500 = 500 with NAT in front."),
    ("RADIUS authentication", "UDP", "1812", "RADIUS authentication/authorization (legacy 1645).", "A WLC sends 802.1X credentials to ISE on UDP 1812.", "RADIUS: 1812 auth, 1813 accounting."),
    ("RADIUS accounting", "UDP", "1813", "RADIUS accounting (legacy 1646).", "ISE records session start/stop on UDP 1813.", "Accounting is the 'next' port: 1813."),
    ("CAPWAP control", "UDP", "5246", "Lightweight AP to WLC control channel (DTLS-encrypted).", "An AP joins a WLC using CAPWAP control on UDP 5246.", "CAPWAP: 5246 control, 5247 data."),
    ("CAPWAP data", "UDP", "5247", "Lightweight AP to WLC client data tunnel.", "Client traffic in Local mode is tunneled to the WLC on UDP 5247.", "Data comes after control: 5247."),
    ("RDP", "TCP", "3389", "Remote Desktop Protocol to Windows hosts.", "An administrator RDPs to a jump server on TCP 3389.", "RDP 3389."),
    ("NETCONF", "TCP", "830", "NETCONF over SSH for model-driven configuration.", "An automation script edits a router config via NETCONF on TCP 830.", "NETCONF runs over SSH on 830."),
]
for name, tr, port, fn, ex, tip in PORTS:
    C("ports", "d4" if name not in ("TACACS+", "RADIUS authentication", "RADIUS accounting", "IKE (IPsec)", "IPsec NAT-T") else "d5",
      f"Which transport protocol and port number does {name} use?", f"{tr} {port}", fn, ex, tip, kind="question")
C("ports", "d1", "Port ranges: well-known, registered, and dynamic/ephemeral?", "Well-known 0-1023 • Registered 1024-49151 • Dynamic/ephemeral 49152-65535",
  "Servers listen on well-known or registered ports; clients pick a temporary ephemeral source port for each connection.",
  "A browser connects from 10.1.1.10:52344 to 203.0.113.5:443.", "Think 1K, 49K, 65K boundaries.", kind="question")

# ---------------- Administrative distance (generated) ----------------
AD = [("Connected interface", "0"), ("Static route", "1"), ("eBGP", "20"), ("EIGRP (internal)", "90"), ("OSPF", "110"),
      ("IS-IS", "115"), ("RIP", "120"), ("EIGRP (external)", "170"), ("iBGP", "200"), ("Unusable / unknown", "255")]
for src, v in AD:
    C("routing", "d3", f"What is the default administrative distance of: {src}?", v,
      "Administrative distance ranks route sources by trustworthiness. It is compared only when two sources offer the SAME prefix; lower wins.",
      f"If OSPF and a static route both offer 10.1.1.0/24, the static (AD 1) is installed over OSPF (AD 110).",
      "Order to recite: 0 Connected, 1 Static, 20 eBGP, 90 EIGRP, 110 OSPF, 115 IS-IS, 120 RIP, 170 ext-EIGRP, 200 iBGP, 255 unusable.", kind="question")

# ---------------- Subnet masks (generated, exact arithmetic) ----------------
def mask(p):
    m = (0xFFFFFFFF << (32 - p)) & 0xFFFFFFFF
    return ".".join(str((m >> s) & 255) for s in (24, 16, 8, 0))
for p in [8, 12, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]:
    hb = 32 - p
    octet = (p - 1) // 8 + 1
    m = mask(p).split(".")[octet - 1]
    block = 256 - int(m)
    C("subnetting", "d1", f"/{p}: subnet mask, usable hosts, and block size?",
      f"{mask(p)} • {2**hb - 2:,} usable hosts • block size {block} in octet {octet}",
      f"/{p} leaves {hb} host bits: 2^{hb} - 2 = {2**hb - 2:,} usable hosts. Block size = 256 - {m} = {block}.",
      f"Subnets of 10.0.0.0/{p} step by {block} in octet {octet}." if p >= 9 else "10.0.0.0/8 is the whole Class A private block.",
      "Mask octet values: 128 192 224 240 248 252 254 255 (add the next bit value each time).", kind="problem")
C("subnetting", "d1", "Which subnet does 192.168.10.77/27 belong to? Give the broadcast address.", "Network 192.168.10.64 • broadcast 192.168.10.95",
  "/27 → mask .224 → block 32 in the 4th octet. Multiples of 32: 0, 32, 64, 96. 77 falls in 64-95.", "Usable range .65 - .94 (30 hosts).",
  "Find the multiple of the block size at or below the host octet.", kind="problem")
C("subnetting", "d1", "172.16.45.10/20 — network address and broadcast?", "Network 172.16.32.0 • broadcast 172.16.47.255",
  "/20 → mask 255.255.240.0 → block 16 in the 3rd octet. 45 lies in 32-47. Octets after the interesting one become 0 (network) or 255 (broadcast).",
  "Usable range 172.16.32.1 - 172.16.47.254.", "Interesting octet = the one where the mask is not 0 or 255.", kind="problem")
C("subnetting", "d1", "You need 500 hosts per subnet. Smallest prefix?", "/23 (510 usable hosts)",
  "2^9 - 2 = 510 ≥ 500, so 9 host bits → 32 - 9 = /23. 2^8 - 2 = 254 is too small.", "A /23 such as 10.1.2.0/23 spans 10.1.2.0 - 10.1.3.255.",
  "Find the first power of two greater than hosts + 2.", kind="problem")
C("subnetting", "d1", "You need 12 subnets from a /24. What new prefix?", "/28 (2^4 = 16 subnets, 14 hosts each)",
  "Borrow s bits where 2^s ≥ 12 → s = 4. 24 + 4 = /28.", "192.168.5.0/24 split into sixteen /28s: .0, .16, .32 ...", "Subnets = 2^borrowed bits; hosts = 2^host bits - 2.", kind="problem")
C("subnetting", "d1", "What is the wildcard mask for a /26?", "0.0.0.63",
  "Wildcard = 255.255.255.255 - subnet mask = 255.255.255.255 - 255.255.255.192.", "access-list 10 permit 192.168.1.64 0.0.0.63 matches .64-.127.",
  "Wildcard last octet = block size - 1 (64 - 1 = 63).", kind="problem")
C("subnetting", "d1", "Point-to-point link prefixes: /30 vs /31?", "/30 = 2 usable hosts (network + broadcast reserved) • /31 = 2 addresses, both usable (RFC 3021)",
  "A /31 has no network or broadcast address on point-to-point links, saving addresses. Cisco routers support /31 on P2P interfaces.",
  "10.0.0.0/31: R1 10.0.0.0, R2 10.0.0.1.", "Both give exactly two router addresses; /31 wastes none.", kind="question")
C("subnetting", "d1", "Convert 11010110 to decimal.", "214", "128 + 64 + 16 + 4 + 2 = 214.", "Mask octet? No — not contiguous ones, so 214 is not a valid mask octet.",
  "Write 128 64 32 16 8 4 2 1 above the bits and add the 1s.", kind="problem")
C("subnetting", "d1", "Convert 172 to binary.", "10101100", "172 - 128 = 44; 44 - 32 = 12; 12 - 8 = 4; 4 - 4 = 0 → bits 128, 32, 8, 4.",
  "172.16.0.0 begins with 10101100.", "Subtract the largest fitting place value each step.", kind="problem")
C("subnetting", "d1", "How many usable hosts in a /25 and a /29?", "/25 = 126 • /29 = 6", "2^7 - 2 = 126; 2^3 - 2 = 6.", "A /29 is typical for a small DMZ.",
  "Memorize: /24 254, /25 126, /26 62, /27 30, /28 14, /29 6, /30 2.", kind="problem")

# ---------------- OSI / TCP/IP ----------------
OSI = [
    ("7", "Application", "Interface to network services for applications.", "HTTP, DNS, DHCP, SSH, SNMP, FTP", "Data"),
    ("6", "Presentation", "Data formatting, encryption, compression.", "JPEG, ASCII, TLS encryption concepts", "Data"),
    ("5", "Session", "Establishes, manages, and terminates sessions.", "Session setup/teardown between applications", "Data"),
    ("4", "Transport", "End-to-end delivery, ports, segmentation, reliability (TCP) or best effort (UDP).", "TCP, UDP", "Segment (TCP) / Datagram (UDP)"),
    ("3", "Network", "Logical addressing and routing between networks.", "IPv4, IPv6, ICMP, OSPF (routing protocols operate here)", "Packet"),
    ("2", "Data Link", "Framing, MAC addressing, error detection on the local link.", "Ethernet, 802.11, 802.1Q, PPP", "Frame"),
    ("1", "Physical", "Transmission of bits as electrical, light, or radio signals.", "Cables, connectors, hubs, RF", "Bits"),
]
for n, name, fn, ex, pdu in OSI:
    C("osi", "d1", f"OSI Layer {n}: name and responsibility?", f"{name} — {fn}", f"PDU at this layer: {pdu}. Typical protocols/examples: {ex}.",
      f"Troubleshooting at Layer {n}: " + {"7": "app/DNS errors", "6": "encoding/encryption mismatch", "5": "session drops", "4": "blocked ports, TCP resets",
                                         "3": "no route, wrong gateway", "2": "VLAN/MAC/duplex issues", "1": "cable down, no link light"}[n] + ".",
      "Top-down: All People Seem To Need Data Processing. Bottom-up: Please Do Not Throw Sausage Pizza Away.", kind="question")
C("osi", "d1", "Name the four TCP/IP model layers and the OSI layers each covers.", "Application (OSI 5-7) • Transport (4) • Internet (3) • Network Access/Link (1-2)",
  "The TCP/IP model is the practical internet stack; OSI is the 7-layer reference model.", "HTTP → Application; TCP → Transport; IP → Internet; Ethernet → Link.",
  "4 layers: A-T-I-N (\"At In\").", kind="question")
C("osi", "d1", "PDU names from Layer 4 down to Layer 1?", "Segment → Packet → Frame → Bits", "Data is encapsulated with a header at each layer; Ethernet adds a trailer (FCS).",
  "A web request: data → TCP segment → IP packet → Ethernet frame → bits.", "Some People Fear Birds: Segment, Packet, Frame, Bits.", kind="question", diagram="encapsulation")
C("osi", "d1", "Which layer does each device primarily operate at: hub, switch, router?", "Hub L1 • Switch L2 • Router L3",
  "Hubs repeat bits, switches forward frames by MAC, routers forward packets by IP. Multilayer switches also route (L3).", "A Layer 3 switch with SVIs routes between VLANs.",
  "Hub repeats bits (L1), switch reads MACs in frames (L2), router reads IPs in packets (L3).", kind="question")
C("osi", "d1", "Diagram: identify the layers in the encapsulation figure.", "Data (L7) → TCP header (L4) → IP header (L3) → Ethernet header + FCS (L2) → bits (L1)",
  "Each header belongs to the layer that adds it. Only Layer 2 adds a trailer.", "Remove headers in reverse order at the receiver.", "Headers stack outward: lower layers wrap higher layers.",
  kind="diagram", diagram="encapsulation")

# ---------------- Acronyms ----------------
ACR = [
    ("ARP", "Address Resolution Protocol", "Maps IPv4 addresses to MAC addresses on the local link.", "PC broadcasts 'Who has 10.1.1.1?' to learn the gateway's MAC.", "IPv6 uses NDP instead of ARP.", "d1"),
    ("NDP", "Neighbor Discovery Protocol", "IPv6 protocol (ICMPv6) for address resolution, router discovery and SLAAC.", "NS/NA replace ARP request/reply.", "Message types: RS 133, RA 134, NS 135, NA 136.", "d1"),
    ("CSMA/CD", "Carrier Sense Multiple Access with Collision Detection", "Half-duplex wired Ethernet access method.", "Hubs required CSMA/CD.", "CD = Detection = wired; CA = Avoidance = wireless.", "d1"),
    ("CSMA/CA", "Carrier Sense Multiple Access with Collision Avoidance", "802.11 access method; stations cannot detect collisions while transmitting.", "Wi-Fi clients wait random backoff before sending.", "A for Air.", "d1"),
    ("FCS", "Frame Check Sequence", "4-byte CRC trailer of an Ethernet frame.", "Corrupted frames increment the CRC error counter.", "Only Layer 2 adds a trailer.", "d1"),
    ("OUI", "Organizationally Unique Identifier", "First 24 bits of a MAC address identifying the vendor.", "00:00:0C is a Cisco OUI.", "OUI = first half of the MAC.", "d1"),
    ("SVI", "Switched Virtual Interface", "Layer 3 interface for a VLAN on a switch.", "interface vlan 10 / ip address 10.10.10.1 255.255.255.0", "SVI up/up needs the VLAN plus an active port in it.", "d2"),
    ("DTP", "Dynamic Trunking Protocol", "Cisco protocol negotiating trunk status.", "dynamic auto + dynamic auto = access.", "Disable with switchport nonegotiate.", "d2"),
    ("VTP", "VLAN Trunking Protocol", "Cisco protocol that propagates VLAN definitions between switches.", "VTP modes: server, client, transparent (and off in v3).", "Many networks use transparent mode to avoid accidental VLAN deletion.", "d2"),
    ("CDP", "Cisco Discovery Protocol", "Cisco Layer 2 neighbor discovery (60 s / 180 s holdtime).", "show cdp neighbors detail shows neighbor IPs.", "LLDP is the open standard (802.1AB).", "d2"),
    ("LLDP", "Link Layer Discovery Protocol", "IEEE 802.1AB vendor-neutral discovery (30 s / 120 s).", "lldp run enables it globally.", "Usually disabled by default on Cisco IOS.", "d2"),
    ("LACP", "Link Aggregation Control Protocol", "IEEE EtherChannel negotiation (active/passive).", "channel-group 1 mode active", "passive + passive never forms.", "d2"),
    ("PAgP", "Port Aggregation Protocol", "Cisco EtherChannel negotiation (desirable/auto).", "channel-group 1 mode desirable", "auto + auto never forms.", "d2"),
    ("BPDU", "Bridge Protocol Data Unit", "STP control message exchanged between switches.", "BPDU Guard err-disables a PortFast port that receives one.", "Root bridge sends BPDUs every 2 s (hello).", "d2"),
    ("WLC", "Wireless LAN Controller", "Central controller for lightweight APs.", "Catalyst 9800 WLC manages APs via CAPWAP.", "Split-MAC: real-time functions on AP, management on WLC.", "d2"),
    ("CAPWAP", "Control And Provisioning of Wireless Access Points", "AP-to-WLC tunneling protocol.", "UDP 5246 control, 5247 data.", "Control is always DTLS-encrypted.", "d2"),
    ("FHRP", "First Hop Redundancy Protocol", "Provides a redundant virtual default gateway (HSRP, VRRP, GLBP).", "Hosts use virtual IP 10.1.1.1 shared by two routers.", "VRRP is the open standard.", "d3"),
    ("HSRP", "Hot Standby Router Protocol", "Cisco FHRP with active/standby routers.", "standby 1 ip 10.1.1.1", "Default priority 100; no preempt by default.", "d3"),
    ("VRRP", "Virtual Router Redundancy Protocol", "Standards-based FHRP with master/backup routers.", "Virtual MAC 0000.5E00.01xx.", "Preempt enabled by default.", "d3"),
    ("GLBP", "Gateway Load Balancing Protocol", "Cisco FHRP that load-balances using multiple virtual MACs (AVG/AVF).", "AVG answers ARP with different AVF MACs.", "G = Gateway load balancing = more than one forwarder.", "d3"),
    ("SPF", "Shortest Path First", "Dijkstra algorithm used by link-state protocols such as OSPF.", "Each router runs SPF on its LSDB.", "OSPF = Open SHORTEST PATH FIRST.", "d3"),
    ("LSA", "Link-State Advertisement", "OSPF unit of topology information.", "Type 1 Router LSA, Type 2 Network LSA.", "LSAs live in the LSDB.", "d3"),
    ("DHCP", "Dynamic Host Configuration Protocol", "Automatically assigns IP settings.", "DORA: Discover, Offer, Request, Ack.", "Server UDP 67, client UDP 68.", "d4"),
    ("NAT", "Network Address Translation", "Translates IP addresses (typically private to public).", "ip nat inside source static 192.168.1.10 203.0.113.10", "Inside local → inside global.", "d4"),
    ("PAT", "Port Address Translation", "Many-to-one NAT using port numbers (NAT overload).", "ip nat inside source list 1 interface g0/1 overload", "Keyword: overload.", "d4"),
    ("QoS", "Quality of Service", "Tools to prioritize and manage traffic during congestion.", "Voice in an LLQ priority queue.", "QoS manages bandwidth; it doesn't add it.", "d4"),
    ("DSCP", "Differentiated Services Code Point", "6-bit Layer 3 QoS marking (0-63).", "Voice marked EF (46).", "DSCP 6 bits; CoS 3 bits.", "d4"),
    ("CoS", "Class of Service", "3-bit Layer 2 priority in the 802.1Q tag (PCP field).", "IP phones mark voice CoS 5.", "CoS only exists on tagged frames.", "d4"),
    ("SNMP", "Simple Network Management Protocol", "Monitoring/management using MIBs and OIDs.", "NMS polls UDP 161; traps to UDP 162.", "Only SNMPv3 offers authentication + encryption.", "d4"),
    ("NTP", "Network Time Protocol", "Synchronizes clocks (UDP 123).", "ntp server 192.0.2.1", "Lower stratum = closer to the reference clock.", "d4"),
    ("AAA", "Authentication, Authorization, Accounting", "Framework for controlling and auditing access.", "aaa new-model with TACACS+.", "Who are you, what can you do, what did you do.", "d5"),
    ("ACL", "Access Control List", "Ordered permit/deny rules for filtering or identifying traffic.", "ip access-group WEB in", "Top-down, first match, implicit deny.", "d5"),
    ("DAI", "Dynamic ARP Inspection", "Validates ARP packets against the DHCP snooping binding table.", "ip arp inspection vlan 10", "DAI depends on DHCP snooping (or ARP ACLs).", "d5"),
    ("IPS", "Intrusion Prevention System", "Inline inspection that blocks malicious traffic.", "NGFW with IPS blocks an exploit attempt.", "IPS prevents (inline); IDS detects (monitors).", "d5"),
    ("MFA", "Multifactor Authentication", "Two or more factor types: know, have, are.", "Password + phone push notification.", "Two passwords are still ONE factor.", "d5"),
    ("VPN", "Virtual Private Network", "Encrypted tunnel across an untrusted network.", "Site-to-site IPsec between branch and HQ.", "Site-to-site = gateways; remote access = client.", "d5"),
    ("WPA", "Wi-Fi Protected Access", "Wi-Fi security family; WPA2 uses AES-CCMP, WPA3 uses SAE.", "WPA3-Personal replaces PSK with SAE.", "WEP < WPA < WPA2 < WPA3.", "d5"),
    ("SAE", "Simultaneous Authentication of Equals", "WPA3-Personal key exchange resistant to offline dictionary attacks.", "Replaces the WPA2 PSK 4-way handshake weakness.", "SAE = WPA3 Personal.", "d5"),
    ("SDN", "Software-Defined Networking", "Centralized control via a controller and APIs.", "Catalyst Center programs switches via southbound APIs.", "Separates/centralizes the control plane.", "d6"),
    ("API", "Application Programming Interface", "Defined way for software to interact with other software.", "GET /dna/intent/api/v1/network-device", "NBI to apps, SBI to devices.", "d6"),
    ("REST", "Representational State Transfer", "Stateless API style over HTTP using verbs and URIs.", "POST creates, GET reads, PUT/PATCH update, DELETE deletes.", "Stateless: every request carries its own auth/context.", "d6"),
    ("JSON", "JavaScript Object Notation", "Lightweight data format of objects {} and arrays [].", "{\"hostname\": \"R1\", \"up\": true}", "Keys always in double quotes.", "d6"),
    ("YAML", "YAML Ain't Markup Language", "Human-readable data format using indentation; used by Ansible playbooks.", "- name: Configure NTP", "Indentation matters in YAML.", "d6"),
    ("VXLAN", "Virtual Extensible LAN", "Overlay encapsulation (MAC-in-UDP) used in SD-Access and data centers.", "SD-Access data plane uses VXLAN.", "24-bit VNI allows ~16 million segments.", "d6"),
    ("LISP", "Locator/ID Separation Protocol", "SD-Access control plane mapping endpoints to locations.", "Fabric control-plane node runs LISP map server.", "SD-Access: LISP control, VXLAN data, IS-IS underlay.", "d6"),
]
for a, full, fn, ex, tip, dom in ACR:
    C("acronyms", dom, f"Expand and explain: {a}", full, fn, ex, tip)

def H(cat, dom, items):
    for it in items:
        C(cat, dom, *it)

# ---------------- Definitions ----------------
H("definitions", "d1", [
    ("Broadcast domain", "A set of devices that receive each other's Layer 2 broadcasts.", "Each VLAN is one broadcast domain; routers separate broadcast domains.", "Three VLANs on one switch = three broadcast domains.", "Routers and VLANs break up broadcast domains; switches don't (within a VLAN)."),
    ("Collision domain", "A network segment where simultaneous transmissions can collide.", "Every switch port is its own collision domain; a hub is one shared collision domain.", "A 24-port switch has 24 collision domains.", "Full duplex eliminates collisions entirely."),
    ("Default gateway", "The router IP a host uses to reach destinations outside its subnet.", "Must be in the same subnet as the host.", "Host 10.1.1.10/24 uses gateway 10.1.1.1.", "Wrong gateway = local works, remote fails."),
    ("Latency vs jitter", "Latency is delay; jitter is the variation in delay.", "Voice tolerates ≤150 ms one-way delay and ≤30 ms jitter.", "Packets arriving 20, 80, 35 ms apart have high jitter.", "Jitter = 'jittery' (inconsistent) delay."),
    ("Unicast, broadcast, multicast, anycast", "One-to-one; one-to-all; one-to-group; one-to-nearest.", "IPv6 has no broadcast; anycast is a unicast address on multiple devices.", "FF02::1 (multicast to all nodes) replaces the IPv4 broadcast role.", "IPv6 = U-M-A (no B)."),
    ("Duplex", "Half duplex sends or receives at one time; full duplex does both simultaneously.", "Mismatch causes late collisions and CRC errors.", "Switch port full, PC half → slow, errors.", "Match both ends or use auto on both."),
    ("Spine-leaf", "Data center topology where every leaf connects to every spine.", "No leaf-to-leaf or spine-to-spine links; consistent hop count.", "Server → leaf → spine → leaf → server.", "Leaves never touch leaves."),
    ("Three-tier vs two-tier", "Access, distribution, core vs access + collapsed core.", "Collapsed core merges core and distribution for smaller campuses.", "A single building with two distribution/core switches.", "Two-tier = collapsed core."),
    ("Virtual machine vs container", "A VM runs a full guest OS on a hypervisor; a container shares the host OS kernel.", "Containers are lighter and start faster.", "Docker container vs ESXi VM.", "Container = shared kernel."),
    ("VRF", "Virtual Routing and Forwarding: multiple isolated routing tables on one router.", "Allows overlapping IP space between customers.", "Customer A and B both use 10.0.0.0/24 on one PE router.", "VRF = virtual router inside a router."),
    ("PoE", "Power over Ethernet: switch (PSE) powers devices (PD) over the data cable.", "802.3af 15.4 W, 802.3at 30 W, 802.3bt up to 60/90 W at the PSE.", "APs and IP phones powered by the access switch.", "af < at < bt (alphabetical = more power)."),
])
H("definitions", "d5", [
    ("Vulnerability vs threat vs exploit", "Weakness • potential danger • method that takes advantage of the weakness.", "Mitigation is the countermeasure that reduces risk.", "Unpatched IOS (vulnerability) + attacker (threat) + crafted packet (exploit).", "V-T-E: the weakness, the danger, the weapon."),
])

# ---------------- Protocols ----------------
H("protocols", "d1", [
    ("TCP vs UDP — key differences?", "TCP: connection-oriented, reliable, ordered, windowing, 20-byte header. UDP: connectionless, best effort, 8-byte header.", "TCP uses the 3-way handshake and acknowledgments; UDP has none.", "HTTPS uses TCP; voice (RTP) uses UDP.", "TCP = Telephone call (setup first); UDP = postcard."),
    ("TCP three-way handshake order?", "SYN → SYN-ACK → ACK", "Establishes sequence numbers in both directions before data flows.", "Client SYN to 443, server SYN-ACK, client ACK.", "Ask, answer-and-ask, answer.", "question", "tcp_handshake"),
    ("ICMP purpose", "Error reporting and diagnostics for IP (echo request/reply, unreachable, time exceeded).", "ping uses echo request/reply; traceroute relies on TTL time-exceeded messages.", "ping 8.8.8.8 → Echo Reply.", "ICMP is Layer 3 helper; it has no ports."),
])
H("protocols", "d4", [
    ("DHCP DORA sequence?", "Discover → Offer → Request → Acknowledge", "Discover and Request are broadcast by the client; relay converts them to unicast with ip helper-address.", "A PC boots and receives 10.1.10.50/24 from the server.", "D-O-R-A like 'Dora the Explorer' exploring for an address.", "question", "dhcp_dora"),
    ("Purpose of ip helper-address", "Relays DHCP broadcasts as unicast to a DHCP server on another subnet.", "Configured on the interface facing the clients; sets giaddr so the server picks the right pool.", "interface g0/1 / ip helper-address 10.0.0.50", "Helper goes where the HELP is needed: the client side."),
    ("DNS record types A, AAAA, CNAME, MX, PTR?", "A = IPv4 • AAAA = IPv6 • CNAME = alias • MX = mail server • PTR = reverse lookup", "PTR maps IP to name; others map names to data.", "www.example.com A 203.0.113.80.", "AAAA = 4×A because IPv6 is 4× longer (128 vs 32 bits)."),
    ("NTP stratum meaning", "Distance from the reference clock; lower = more accurate.", "Stratum 0 = reference clock (GPS/atomic); stratum 1 server attaches to it.", "A router synced to a stratum 2 server becomes stratum 3.", "Stratum is like floors from ground level."),
    ("SNMP Trap vs Inform", "Both are unsolicited agent → manager notifications; Inform is acknowledged, Trap is not.", "Informs are retransmitted if not acknowledged (SNMPv2c+).", "Link down event sent as Inform to the NMS.", "Inform = 'I'll make sure you're informed' (ack)."),
    ("Syslog message format", "%FACILITY-SEVERITY-MNEMONIC: description", "Severity number 0-7; e.g., %LINK-3-UPDOWN is severity 3 (Error).", "%OSPF-5-ADJCHG: Process 1, Nbr 2.2.2.2 ... FULL", "The digit between dashes is the severity."),
    ("QoS: policing vs shaping", "Policing drops/re-marks excess traffic; shaping buffers it (adds delay).", "Shaping is egress only; policing can be ingress or egress.", "ISP polices to 100 Mbps; customer shapes to 100 Mbps.", "Police officers stop you; shapers make you wait."),
])

# ---------------- IPv6 ----------------
H("ipv6", "d1", [
    ("IPv6 address length and notation", "128 bits; 8 hextets of 4 hex digits separated by colons.", "Each hextet = 16 bits; each hex digit = 4 bits.", "2001:0DB8:0000:0000:0000:0000:0000:0001", "8 × 16 = 128."),
    ("IPv6 abbreviation rules", "1) Remove leading zeros in each hextet. 2) Replace ONE run of consecutive all-zero hextets with ::.", "Using :: twice is invalid because the length becomes ambiguous.", "2001:0DB8:0000:0000:0010:0000:0000:0001 → 2001:DB8::10:0:0:1", "Leading zeros only; :: only once."),
    ("Abbreviate 2001:0DB8:0000:00A0:0000:0000:0000:0100", "2001:DB8:0:A0::100", "Remove leading zeros; the longest zero run (three hextets) becomes ::.", "A0 keeps its trailing 0 — only leading zeros go.", "Pick the longest zero run for ::.", "problem"),
    ("Global unicast prefix", "2000::/3 (addresses beginning 2 or 3)", "Publicly routable; typically /48 site, /64 per subnet.", "2001:DB8::/32 is reserved for documentation.", "Globals start with 2 or 3."),
    ("Unique local prefix", "FC00::/7 (FD00::/8 in practice)", "Private-like; not routed on the internet.", "FD12:3456:789A:1::1", "FD = 'Fake/private Domain'."),
    ("Link-local prefix", "FE80::/10", "Auto-created on every IPv6 interface; used for NDP and as routing next hops; never routed.", "FE80::1 on R1 Gi0/0", "Every IPv6 interface has an FE80 address — it never leaves the link."),
    ("Multicast prefix and key groups", "FF00::/8 • FF02::1 all nodes • FF02::2 all routers • FF02::5 OSPFv3 routers • FF02::1:FFxx:xxxx solicited-node", "IPv6 uses multicast instead of broadcast.", "Router Advertisements go to FF02::1.", "FF = 'Forwarded to the Flock'."),
    ("Modified EUI-64 for MAC 0050.56AB.CDEF", "0250:56FF:FEAB:CDEF", "Split MAC, insert FFFE, flip the 7th bit of the first byte (00 → 02).", "Prefix 2001:DB8:1:1::/64 → 2001:DB8:1:1:250:56FF:FEAB:CDEF", "Insert FFFE, flip bit 7 (U/L).", "problem"),
    ("SLAAC", "Stateless Address Autoconfiguration: host builds its address from the RA prefix + its own interface ID.", "Requires a router sending RAs ('ipv6 unicast-routing'). DNS info may come from RA (RDNSS) or stateless DHCPv6.", "ipv6 address autoconfig on a router interface.", "Stateless = no server tracks the address."),
    ("ipv6 enable vs ipv6 address", "'ipv6 enable' creates only a link-local address; 'ipv6 address' assigns a global/ULA (and link-local automatically).", "Routing protocols can use link-local next hops.", "ipv6 address 2001:DB8:1::1/64", "enable = link-local only."),
    ("NDP message types", "RS 133, RA 134, NS 135, NA 136, Redirect 137", "NS/NA replace ARP; RS/RA discover routers and prefixes.", "Host sends RS to FF02::2; router replies RA.", "Router S/A before Neighbor S/A: 133-136 in order."),
])

# ---------------- VLAN ----------------
H("vlan", "d2", [
    ("VLAN ID ranges", "Normal 1-1005 (1002-1005 reserved) • Extended 1006-4094 • 0 and 4095 reserved", "VLAN ID is 12 bits (4096 values).", "vlan 2000 is an extended-range VLAN.", "12 bits → 4096 values."),
    ("Default VLAN and native VLAN", "Default VLAN = VLAN 1 (all ports, undeletable). Native VLAN = untagged VLAN on an 802.1Q trunk (default 1).", "Best practice: change native VLAN to an unused VLAN on both ends.", "switchport trunk native vlan 99", "Native = Not tagged."),
    ("802.1Q tag size and fields", "4 bytes: TPID 0x8100 (16 bits), PCP (3), DEI (1), VLAN ID (12)", "Inserted between source MAC and EtherType.", "Tagged frame max size 1522 bytes.", "4 bytes, 12-bit VID.", "question", "dot1q_tag"),
    ("DTP combinations forming a trunk", "trunk + trunk/desirable/auto • desirable + desirable/auto. NOT auto + auto.", "Auto waits; desirable actively asks.", "Two new switches default dynamic auto → access link.", "Somebody must be eager (desirable/trunk)."),
    ("Command to add VLAN 40 to a trunk's allowed list", "switchport trunk allowed vlan add 40", "Without 'add' the list is replaced.", "Forgetting 'add' drops all other VLANs.", "ADD or you REPLACE."),
    ("Why doesn't 'show vlan brief' list trunk ports?", "It lists access ports per VLAN; trunk VLANs appear in 'show interfaces trunk'.", "Use show interfaces trunk for allowed/active/forwarding VLANs.", "Gi0/1 trunk missing from show vlan brief.", "Trunks hide from vlan brief."),
    ("Router-on-a-stick key command", "encapsulation dot1Q <vlan-id> on each subinterface", "Subinterfaces Gi0/0.10, .20 each get an IP in their VLAN's subnet.", "interface g0/0.20 / encapsulation dot1Q 20 / ip address 10.20.0.1 255.255.255.0", "Subinterface number need not match, but VLAN ID must."),
    ("Voice VLAN behavior", "Phone tags voice traffic with the voice VLAN (802.1Q); PC traffic behind it stays untagged in the access VLAN.", "Learned by the phone via CDP/LLDP.", "switchport access vlan 10 / switchport voice vlan 20", "One port, two VLANs: data untagged, voice tagged."),
    ("EtherChannel mode pairs that work", "LACP: active+active, active+passive. PAgP: desirable+desirable, desirable+auto. Static: on+on.", "passive+passive and auto+auto fail; on never mixes with negotiation.", "channel-group 1 mode active on both switches.", "At least one side must be 'eager'."),
    ("EtherChannel member requirements", "Same speed, duplex, switchport mode, access VLAN / trunk native and allowed VLANs.", "Mismatched members are suspended (s).", "show etherchannel summary → Po1(SU) Gi0/1(P) Gi0/2(s)", "Clone the config on every member."),
])

# ---------------- STP ----------------
H("stp", "d2", [
    ("How is the root bridge elected?", "Lowest bridge ID: priority (default 32768 + VLAN ID), then lowest MAC.", "Priority is configured in multiples of 4096.", "spanning-tree vlan 10 root primary", "LOWEST wins in STP."),
    ("Root port selection order", "Lowest root path cost → lowest neighbor bridge ID → lowest neighbor port priority → lowest neighbor port number.", "Every non-root switch has exactly one root port.", "Two equal-cost uplinks: the one toward the lower bridge ID wins.", "Cost, then Bridge ID, then Port."),
    ("STP costs (short)", "10 Mbps 100 • 100 Mbps 19 • 1 Gbps 4 • 10 Gbps 2", "Root path cost = sum of costs of ingress ports along the path.", "Two 1G hops to root = cost 8.", "100-19-4-2."),
    ("RSTP port states", "Discarding, Learning, Forwarding", "Legacy 802.1D: Blocking, Listening, Learning, Forwarding, Disabled.", "Alternate ports are discarding.", "RSTP merges disabled/blocking/listening into Discarding."),
    ("RSTP port roles", "Root, Designated, Alternate, Backup (and Disabled)", "Alternate = backup path to root; Backup = redundant port to the same segment.", "Blocked uplink on access switch = Alternate.", "Alternate → another switch; Backup → same switch/segment."),
    ("PortFast + BPDU Guard", "PortFast skips listening/learning on edge ports; BPDU Guard err-disables the port if a BPDU arrives.", "Use on access ports to end hosts only.", "spanning-tree portfast / spanning-tree bpduguard enable", "Fast for hosts; Guard against switches."),
    ("Root Guard vs Loop Guard", "Root Guard: port can't become root port (root-inconsistent). Loop Guard: port stops if BPDUs stop arriving (loop-inconsistent).", "Root Guard on ports toward access switches; Loop Guard on non-designated ports.", "A rogue switch with priority 0 is blocked by Root Guard.", "Root Guard protects WHO is root; Loop Guard protects against missing BPDUs."),
    ("Diagram: which port blocks?", "SW3's port toward SW2 (alternate/discarding)", "SW1 is root; SW2 has the lower bridge ID so it owns the designated port on the SW2-SW3 segment.", "Lower bridge ID wins designated-port ties.", "Non-root, non-designated = blocked.", "diagram", "stp"),
    ("Timers in 802.1D", "Hello 2 s • Forward delay 15 s • Max age 20 s", "Convergence up to 50 s in legacy STP; RSTP converges in seconds.", "Root sends hellos every 2 s.", "2-15-20."),
])

# ---------------- Routing ----------------
H("routing", "d3", [
    ("Route selection order", "1) Longest prefix match 2) Lowest administrative distance 3) Lowest metric", "AD and metric only matter between routes to the SAME prefix.", "10.1.1.0/24 beats 10.1.0.0/16 regardless of AD.", "Long, Low, Low."),
    ("Routing table codes C, L, S, S*, O", "Connected • Local (/32 interface IP) • Static • Static default (candidate default) • OSPF", "L routes are host routes for the router's own interface addresses.", "L 10.1.1.1/32 is directly connected, Gi0/0", "Star = default."),
    ("Read: O 10.2.2.0/24 [110/3] via 10.0.0.2, Gi0/1", "OSPF route, AD 110, metric (cost) 3, next hop 10.0.0.2, exit Gi0/1", "Format [AD/metric].", "A static would show [1/0].", "Brackets: [trust/cost]."),
    ("Floating static route", "A backup static with AD set higher than the primary route's AD.", "Installed only when the primary route disappears.", "ip route 0.0.0.0 0.0.0.0 172.16.1.1 130 backing up OSPF (110).", "Float ABOVE the primary's AD."),
    ("IPv6 static route with link-local next hop", "Must include the exit interface: ipv6 route ::/0 Gi0/1 FE80::2", "Link-local addresses are only unique per link.", "ipv6 route 2001:DB8:2::/64 Gi0/0 FE80::1", "Link-local needs a link (interface)."),
    ("Default route syntax IPv4/IPv6", "ip route 0.0.0.0 0.0.0.0 <next-hop> • ipv6 route ::/0 <next-hop>", "Shown as S* and 'Gateway of last resort'.", "ip route 0.0.0.0 0.0.0.0 203.0.113.1", "All zeros = match everything."),
    ("Distance vector vs link state", "Distance vector shares routes with neighbors (RIP, EIGRP is advanced DV); link state shares topology (LSAs) and runs SPF (OSPF, IS-IS).", "Link-state routers each build a full map of the area.", "OSPF floods LSAs; RIP sends its table every 30 s.", "DV = rumors from neighbors; LS = everyone has the map."),
    ("Routing protocol characteristics", "RIP: DV, hop count (max 15), AD 120 • EIGRP: advanced DV, Cisco, composite metric, AD 90 • OSPF: link state, cost, AD 110 • BGP: path vector, AD 20/200", "CCNA v1.1 configures only OSPFv2; others are recognized for comparison.", "A route learned by EIGRP and OSPF → EIGRP (90) wins.", "Only OSPF is configured on the exam."),
    ("HSRP vs VRRP vs GLBP", "HSRP: Cisco, active/standby • VRRP: standard, master/backup • GLBP: Cisco, load-balancing AVG/AVFs", "All provide a virtual gateway IP.", "Multi-vendor site → VRRP.", "VRRP = the only open standard."),
])

# ---------------- OSPF ----------------
H("ospf", "d3", [
    ("OSPF neighbor states in order", "Down → Init → 2-Way → ExStart → Exchange → Loading → Full", "DROTHERs stay 2-Way with each other.", "show ip ospf neighbor: FULL/DR", "Don't Invite 2 Extra Exhausted Lazy Friends.", "question", "ospf_states"),
    ("What must match for OSPF neighbors?", "Area ID, subnet/mask, Hello & Dead timers, authentication, stub flag; unique router IDs; MTU for Full", "Process ID does NOT need to match.", "Timer mismatch → never neighbors.", "Process ID is local."),
    ("OSPF router ID selection", "1) router-id command 2) highest loopback IP 3) highest active physical interface IP", "Changed RID requires 'clear ip ospf process'.", "router-id 1.1.1.1", "Manual → Loopback → Physical."),
    ("DR/BDR election", "Highest interface priority (default 1, 0 = never), then highest RID. Non-preemptive.", "Only on broadcast/NBMA networks; none on point-to-point.", "ip ospf priority 255 on the intended DR.", "Highest wins in OSPF (opposite of STP)."),
    ("OSPF cost formula", "Cost = reference bandwidth (100 Mbps default) / interface bandwidth", "FastEthernet and faster all get cost 1 by default; change with auto-cost reference-bandwidth.", "auto-cost reference-bandwidth 10000 → 1G = 10, 10G = 1.", "Reference ÷ interface."),
    ("OSPF Hello/Dead timers and multicast groups", "Hello 10 s, Dead 40 s (broadcast & P2P). 224.0.0.5 all OSPF routers, 224.0.0.6 DR/BDR.", "Dead = 4× Hello by default.", "DROTHERs send updates to 224.0.0.6.", "5 = everyone, 6 = the DRs."),
    ("passive-interface", "Stops OSPF Hellos on an interface while still advertising its network.", "Use on user LANs to prevent rogue adjacencies.", "passive-interface g0/2", "Passive = advertise but don't talk."),
    ("network command wildcard example", "network 10.0.12.0 0.0.0.3 area 0 enables OSPF on interfaces in 10.0.12.0/30.", "Uses a wildcard mask, not a subnet mask. Alternative: ip ospf 1 area 0 under the interface.", "network 0.0.0.0 255.255.255.255 area 0 matches all interfaces.", "Wildcard, not mask."),
    ("LSDB, LSA, SPF relationship", "LSAs describe links → stored in the LSDB → SPF computes shortest paths → best routes enter the routing table.", "All routers in an area have identical LSDBs.", "Type 1 Router LSA, Type 2 Network LSA (from DR).", "Advertise, store, compute."),
    ("default-information originate", "Advertises a default route into OSPF (requires a default route in the table unless 'always').", "Common on internet edge routers.", "R1 has ip route 0.0.0.0 0.0.0.0 ISP; OSPF spreads it.", "Originate the default to others."),
])

# ---------------- ACL ----------------
H("acl", "d5", [
    ("Standard vs extended ACL numbers and matching", "Standard 1-99, 1300-1999: source IP only. Extended 100-199, 2000-2699: protocol, source, destination, ports.", "Named ACLs can be either type.", "access-list 110 permit tcp any host 10.1.1.10 eq 443", "Standard = Source only."),
    ("ACL placement rule", "Extended near the source; standard near the destination.", "Standard ACLs can't tell destinations apart, so placing them near the source blocks too much.", "Block PC1 from Server only → extended inbound on PC1's gateway interface.", "E-S (Extended-Source), S-D (Standard-Destination)."),
    ("ACL processing behavior", "Top-down, first match wins, implicit deny any at the end.", "An ACL with only deny statements blocks everything.", "Line 10 permit any above line 20 deny host X → X is permitted.", "Order matters; specific before general."),
    ("Wildcard mask for 172.16.0.0/12", "0.15.255.255", "255.255.255.255 - 255.240.0.0 = 0.15.255.255.", "access-list 5 permit 172.16.0.0 0.15.255.255", "Subtract the mask from all-255s.", "problem"),
    ("'host' and 'any' keywords", "host 10.1.1.1 = 10.1.1.1 0.0.0.0 • any = 0.0.0.0 255.255.255.255", "Shortcuts for exact match and match-all.", "deny ip host 10.1.1.50 any", "Host = all 0s wildcard; Any = all 1s."),
    ("Apply ACL to interface vs VTY", "Interface: ip access-group NAME in|out • VTY lines: access-class NAME in", "One ACL per interface, per direction, per protocol.", "line vty 0 4 / access-class 5 in", "Group on interfaces, class on lines."),
    ("Diagram: where does a packet to 10.2.2.10 on TCP 22 from 10.1.1.5 match?", "Line 20 (deny ip any host 10.2.2.10) — line 10 only permits port 443.", "First matching line decides; processing stops.", "Port 443 would match line 10 and be forwarded.", "Check protocol AND port before moving on.", "diagram", "acl_flow"),
    ("Inbound vs outbound", "Inbound filters as packets enter the interface (before routing); outbound as they leave.", "Direction is from the router interface's perspective.", "ip access-group 101 in on the LAN interface.", "Stand inside the router and look at the interface."),
])

# ---------------- Security ----------------
H("security", "d5", [
    ("RADIUS vs TACACS+", "RADIUS: UDP 1812/1813, standard, encrypts password only, combines authN/authZ. TACACS+: TCP 49, Cisco, encrypts whole payload, separates AAA, per-command authorization.", "TACACS+ preferred for device admin; RADIUS for network access (802.1X/VPN).", "Switch admin logins via TACACS+; Wi-Fi users via RADIUS.", "TACACS+ = Total encryption, TCP."),
    ("enable secret vs enable password", "enable secret is hashed and takes precedence; enable password is clear text (or type 7).", "service password-encryption only applies weak reversible type 7.", "enable secret Str0ng!", "Always use secret."),
    ("SSH configuration prerequisites", "hostname, ip domain-name, crypto key generate rsa (≥768 bits for v2), local user, login local, transport input ssh", "ip ssh version 2 forces SSHv2.", "crypto key generate rsa modulus 2048", "Name the box, name the domain, make keys."),
    ("Port security violation modes", "Protect: drop silently • Restrict: drop + log + counter • Shutdown: err-disable (default)", "Default max MAC = 1.", "switchport port-security violation restrict", "P-R-S: Polite, Reporting, Severe."),
    ("DHCP snooping trusted vs untrusted", "Untrusted ports (default) drop DHCP server messages (OFFER/ACK/NAK); trusted ports (uplinks to real servers) allow them.", "Builds the binding table used by DAI.", "ip dhcp snooping trust on the uplink.", "Trust the path to the real server."),
    ("Dynamic ARP Inspection", "Drops ARP packets on untrusted ports whose IP/MAC don't match the DHCP snooping binding table (or ARP ACL).", "Prevents ARP spoofing / MITM.", "ip arp inspection vlan 10", "DAI needs DHCP snooping."),
    ("IPsec services", "Confidentiality, integrity, authentication, anti-replay", "ESP (IP 50) encrypts; AH (IP 51) does not.", "Site-to-site tunnel between firewalls.", "C-I-A + anti-replay."),
    ("Site-to-site vs remote-access VPN", "Site-to-site: gateway-to-gateway, always on, transparent to hosts. Remote access: client software (TLS/IPsec), on demand.", "Clientless SSL VPN uses only a browser.", "Branch IPsec vs home worker with Secure Client.", "Sites are permanent; people come and go."),
    ("Social engineering variants", "Phishing (email), spear phishing (targeted), whaling (executives), vishing (voice), smishing (SMS), pretexting, tailgating", "Mitigated by user awareness, training, and physical controls.", "Fake IT call asking for MFA code = vishing.", "V = Voice, S = SMS."),
    ("MFA factors", "Something you know, have, are (plus somewhere you are, in some models).", "Password + PIN is still single factor (both 'know').", "Password + certificate on smart card.", "Know-Have-Are."),
    ("Security program elements", "User awareness, user training, physical access control", "Objective 5.2.", "Phishing simulations, badge readers.", "People, practice, physical."),
])

# ---------------- Wireless ----------------
H("wireless", "d1", [
    ("802.11 standards and bands", "a: 5 GHz • b: 2.4 • g: 2.4 • n (Wi-Fi 4): 2.4/5 • ac (Wi-Fi 5): 5 • ax (Wi-Fi 6): 2.4/5, 6E adds 6 GHz • be (Wi-Fi 7): 2.4/5/6", "Max theoretical rates: b 11 Mbps, a/g 54 Mbps.", "Wi-Fi 6E client uses a 6 GHz channel.", "'a' was first to 5 GHz; 'ac' only 5."),
    ("2.4 GHz non-overlapping channels", "1, 6, 11", "Adjacent APs should alternate these to avoid co-channel interference.", "AP1 ch1, AP2 ch6, AP3 ch11.", "1-6-11, five apart."),
    ("SSID vs BSSID vs ESS", "SSID = network name • BSSID = AP radio MAC • ESS = multiple BSSs sharing an SSID for roaming", "IBSS = ad hoc, no AP.", "Corp SSID on 30 APs = one ESS.", "B = the Box's MAC."),
    ("Wi-Fi security evolution", "WEP (RC4, broken) → WPA (TKIP) → WPA2 (AES-CCMP) → WPA3 (SAE, PMF required, 192-bit Enterprise)", "Personal = PSK/SAE; Enterprise = 802.1X/EAP + RADIUS.", "WPA2-PSK with AES on a small office.", "Each step = stronger crypto.", "question", None),
    ("802.1X roles", "Supplicant (client) • Authenticator (AP/WLC/switch) • Authentication server (RADIUS)", "EAP between supplicant and authenticator; RADIUS to server.", "Laptop → WLC → ISE.", "S-A-AS.", "diagram", "dot1x"),
])
H("wireless", "d2", [
    ("AP modes", "Local (default, tunnels to WLC) • FlexConnect (local switching) • Monitor • Sniffer • Rogue detector • SE-Connect • Bridge/Mesh", "Local AP → access port; FlexConnect local switching → trunk.", "Branch APs in FlexConnect survive WAN outage.", "Flex = flexible local switching."),
    ("Autonomous vs lightweight vs cloud APs", "Autonomous: standalone config • Lightweight: WLC via CAPWAP (split-MAC) • Cloud: managed from a cloud dashboard (e.g., Meraki)", "Split-MAC: AP handles real-time (beacons, ACKs, encryption); WLC handles auth, RF, roaming.", "Campus lightweight APs + 9800 WLC.", "Light = needs a controller."),
    ("WLC LAG requirement (AireOS)", "Static EtherChannel: channel-group X mode on on the switch", "AireOS WLC doesn't negotiate LACP/PAgP.", "Two WLC ports bundled to a core switch.", "WLC LAG = mode ON."),
    ("WLC QoS profiles", "Platinum (voice) • Gold (video) • Silver (best effort, default) • Bronze (background)", "Selected per WLAN in the GUI.", "Voice SSID set to Platinum.", "Precious metals rank priority."),
])

# ---------------- Cisco terminology / IOS ----------------
H("cisco", "d2", [
    ("IOS modes and prompts", "User EXEC (R1>) → Privileged EXEC (R1#) → Global config (R1(config)#) → interface/line/router submodes", "enable, configure terminal, exit/end.", "R1(config-if)# no shutdown", "> look, # do, (config) change."),
    ("running-config vs startup-config", "Running = active in RAM; startup = saved in NVRAM, loaded at boot.", "Save with copy running-config startup-config (or write memory).", "Unsaved changes are lost after reload.", "RAM runs, NVRAM starts."),
    ("show ip interface brief statuses", "up/up = working • up/down = L1 ok, L2 problem • down/down = physical issue • administratively down/down = shutdown", "Router interfaces are shutdown by default.", "Gi0/1 administratively down → no shutdown.", "Status = Layer 1; Protocol = Layer 2."),
    ("show mac address-table purpose", "Lists MAC → port → VLAN mappings (dynamic/static).", "Locate a device's switch port.", "show mac address-table address 00a1.b2c3.d4e5", "Find the port for a MAC."),
    ("show interfaces trunk shows", "Trunk ports, mode, encapsulation, native VLAN, allowed VLANs, active VLANs, forwarding (not pruned) VLANs", "First stop for VLAN-over-trunk issues.", "Native VLAN mismatch spotted here.", "Trunk truth table."),
    ("err-disabled recovery", "shutdown then no shutdown, or errdisable recovery cause <cause>", "Causes include psecure-violation and bpduguard.", "errdisable recovery cause bpduguard", "Bounce the port after fixing the cause."),
    ("Console line access settings", "line console 0 / password X / login (or login local) / exec-timeout 10 0", "Default console: 9600 baud, 8 data bits, no parity, 1 stop bit.", "9600 8N1 in PuTTY.", "9600-8-N-1."),
    ("show ip route vs show ip protocols", "show ip route displays the routing table; show ip protocols shows routing protocol settings (RID, networks, passive interfaces).", "Troubleshoot OSPF config with show ip protocols.", "Router ID 1.1.1.1 visible in show ip protocols.", "Table vs settings."),
])
H("cisco", "d1", [
    ("Catalyst Center", "Current name of Cisco DNA Center: enterprise controller for automation, assurance, and policy.", "Exposes northbound REST APIs.", "Intent-based policy pushed to switches.", "DNA Center → Catalyst Center."),
])

# ---------------- Automation ----------------
H("automation", "d6", [
    ("Data, control, and management planes", "Data: forwards traffic • Control: builds forwarding info (routing protocols, STP, ARP) • Management: manages devices (SSH, SNMP, syslog)", "SDN centralizes control/management logic.", "OSPF = control plane; ACL filtering = data plane.", "Data moves, Control decides, Management manages."),
    ("Northbound vs southbound APIs", "Northbound: controller ↔ applications (REST) • Southbound: controller ↔ devices (NETCONF, RESTCONF, OpenFlow, OpFlex, CLI/SSH, SNMP)", "Picture the controller in the middle: apps above, devices below.", "Python script calling Catalyst Center API = NBI.", "North = up to apps; South = down to devices.", "diagram", "sdn"),
    ("CRUD to HTTP verbs", "Create POST • Read GET • Update PUT/PATCH • Delete DELETE", "PUT replaces a resource; PATCH partially updates.", "GET /devices returns a list.", "POST creates — think 'post a new letter'."),
    ("HTTP status code classes", "2xx success (200 OK, 201 Created, 204 No Content) • 4xx client error (400, 401, 403, 404) • 5xx server error (500)", "401 = not authenticated; 403 = authenticated but not allowed.", "Bad token → 401.", "4xx = your fault, 5xx = server's fault."),
    ("JSON syntax rules", "Objects {}, arrays [], key/value pairs with double-quoted keys; values: string, number, boolean, null, object, array", "No comments, no trailing commas.", "{\"vlan\": 10, \"name\": \"SALES\", \"active\": true}", "Curly = object, square = list."),
    ("Count the JSON: {\"vlans\":[{\"id\":10},{\"id\":20}]}", "1 outer object with 1 key; its value is an array of 2 objects", "Each {} is an object; [] is an array.", "id values are numbers.", "Count braces for objects, brackets for arrays.", "problem"),
    ("REST constraints", "Client-server, stateless, cacheable, uniform interface, layered system, (optional) code on demand", "Stateless: each request is self-contained (including auth token).", "Bearer token sent on every call.", "Stateless is the most tested constraint."),
    ("Ansible key facts", "Agentless, push model, SSH/APIs, YAML playbooks, inventory file, modules, Python-based", "Ideal for network config consistency.", "Playbook sets NTP on all access switches.", "Ansible: no Agent, YAML."),
    ("Terraform key facts", "Declarative infrastructure as code, HCL language, providers, plan/apply workflow, state file", "Typically provisions infrastructure (cloud, controllers).", "terraform plan → terraform apply", "Terraform: Terra-FORM infrastructure from HCL."),
    ("Underlay vs overlay vs fabric", "Underlay = physical network/IP reachability • Overlay = virtual tunnels on top (e.g., VXLAN) • Fabric = both managed as one", "SD-Access: IS-IS underlay, VXLAN data plane, LISP control plane.", "Users in a virtual network ride VXLAN over the campus.", "Under = physical, Over = virtual."),
    ("Predictive vs generative AI in NetOps", "Predictive: baselines, anomaly detection, forecasting • Generative: creates summaries, answers, config drafts (must be validated)", "Objective 6.4.", "Assurance flags abnormal onboarding time (predictive).", "Predict the future vs generate content."),
    ("Traditional vs controller-based networking", "Traditional: per-device CLI, distributed control • Controller-based: centralized policy, automation, assurance, APIs", "Catalyst Center vs box-by-box configuration.", "Push a VLAN to 200 switches from one place.", "One brain vs many brains."),
])
