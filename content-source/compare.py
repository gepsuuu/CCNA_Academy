from memorize import txt, bul, tab, trick, dia, code
COMPARISONS = []
def C(id, title, domain, summary, blocks, related_lessons):
    COMPARISONS.append(dict(id=id, title=title, domainId=domain, summary=summary, blocks=blocks, lessons=related_lessons))

C("tcp-udp", "TCP vs UDP", "d1", "Reliable connection-oriented delivery versus lightweight best-effort delivery.", [
  tab("Side by side", ["Feature", "TCP", "UDP"], [
    ["Connection", "Connection-oriented (3-way handshake)", "Connectionless"], ["Reliability", "Acknowledgments and retransmission", "None (best effort)"],
    ["Ordering", "Sequence numbers, in-order delivery", "No ordering"], ["Flow control", "Windowing", "None"],
    ["Header size", "20 bytes minimum", "8 bytes"], ["Typical uses", "HTTP/HTTPS, SSH, SMTP, FTP, BGP", "DNS queries, DHCP, TFTP, SNMP, NTP, Syslog, voice/video"]]),
  txt("Example", "Downloading a file over HTTPS must arrive complete and in order → TCP. A VoIP call needs low delay; a late packet is useless → UDP."),
  trick("Exam focus", "DNS uses both (UDP 53 for queries, TCP 53 for zone transfers/large replies). TACACS+ is TCP 49; RADIUS is UDP 1812/1813.", "exam"),
  dia("TCP handshake", "tcp_handshake")], ["1-06"])

C("osi-tcpip", "OSI vs TCP/IP", "d1", "A 7-layer reference model versus the 4-layer model the internet actually uses.", [
  tab("Layer mapping", ["OSI", "TCP/IP", "Examples"], [
    ["7 Application, 6 Presentation, 5 Session", "Application", "HTTP, DNS, SSH, DHCP"], ["4 Transport", "Transport", "TCP, UDP"],
    ["3 Network", "Internet", "IPv4, IPv6, ICMP"], ["2 Data Link, 1 Physical", "Network Access (Link)", "Ethernet, 802.11"]]),
  bul("Key differences", ["OSI is a teaching and troubleshooting reference; TCP/IP describes the implemented protocol suite.",
                          "Cisco still names devices by OSI layer number (Layer 2 switch, Layer 3 switch).", "PDUs: segment (L4), packet (L3), frame (L2), bits (L1)."]),
  dia("Models side by side", "osi_tcpip")], ["1-03"])

C("switch-router", "Switch vs router", "d1", "Forwarding inside a network versus forwarding between networks.", [
  tab("Side by side", ["Feature", "Layer 2 switch", "Router"], [
    ["OSI layer", "2 (Data Link)", "3 (Network)"], ["Forwards using", "Destination MAC + MAC address table", "Destination IP + routing table"],
    ["Broadcasts", "Floods within the VLAN", "Does not forward broadcasts (separates broadcast domains)"],
    ["Connects", "Devices in the same LAN/VLAN", "Different networks/subnets, WAN, internet"], ["Learns", "Source MACs automatically", "Routes: connected, static, dynamic"],
    ["Extra functions", "VLANs, STP, port security", "NAT, ACLs, VPN, DHCP relay"]]),
  trick("Layer 3 switch", "A multilayer switch does both: Layer 2 switching plus routing between VLANs with SVIs ('ip routing'). Routers usually remain at the WAN/internet edge.")], ["1-01", "1-14"])

C("hub-switch", "Hub vs switch", "d1", "Repeating every signal everywhere versus intelligent frame forwarding.", [
  tab("Side by side", ["Feature", "Hub", "Switch"], [
    ["OSI layer", "1", "2"], ["Forwarding", "Repeats bits out every port", "Forwards a frame only to the destination port (floods unknown/broadcast)"],
    ["Collision domains", "One shared domain", "One per port"], ["Broadcast domains", "One", "One per VLAN"], ["Duplex", "Half duplex (CSMA/CD)", "Full duplex"],
    ["Security/performance", "Every host sees all traffic; bandwidth shared", "Dedicated bandwidth per port"]]),
  dia("How a switch learns", "mac_learning")], ["1-14"])

C("static-dynamic", "Static vs dynamic routing", "d3", "Manually configured routes versus routes learned from a routing protocol.", [
  tab("Side by side", ["Feature", "Static routing", "Dynamic routing (e.g., OSPF)"], [
    ["Configuration", "Manual on every router", "Protocol exchanges routes automatically"], ["Reaction to failure", "None (unless a floating static backs it up)", "Converges automatically"],
    ["Overhead", "No CPU/bandwidth for updates", "Hellos, LSAs, SPF calculations"], ["Default AD", "1", "OSPF 110, EIGRP 90, RIP 120"],
    ["Scales to", "Small / stub networks", "Medium to large networks"], ["Typical use", "Default route to ISP, stub branches, backups", "Enterprise cores and campuses"]]),
  code("Examples", ["ip route 0.0.0.0 0.0.0.0 203.0.113.1", "ip route 0.0.0.0 0.0.0.0 198.51.100.1 130   ! floating backup", "router ospf 1", " network 10.0.0.0 0.255.255.255 area 0"])], ["3-02", "3-03"])

C("ospf-others", "OSPF vs other routing protocols", "d3", "How OSPF compares with RIP, EIGRP and BGP.", [
  tab("Comparison", ["", "OSPF", "EIGRP", "RIPv2", "BGP"], [
    ["Type", "Link state", "Advanced distance vector", "Distance vector", "Path vector"], ["Metric", "Cost (bandwidth)", "Bandwidth + delay (default)", "Hop count (max 15)", "Path attributes"],
    ["AD", "110", "90 (170 external)", "120", "20 eBGP / 200 iBGP"], ["Standard", "Open", "Cisco (informational RFC)", "Open", "Open"],
    ["Transport", "IP protocol 89", "IP protocol 88", "UDP 520", "TCP 179"], ["Scope", "IGP", "IGP", "IGP", "EGP (between autonomous systems)"]]),
  trick("Exam scope", "CCNA v1.1 configures single-area OSPFv2 only; the others appear in comparison and AD questions.", "exam")], ["3-03", "3-01"])

C("access-native", "Access VLAN vs native VLAN", "d2", "The single VLAN of an access port versus the untagged VLAN on a trunk.", [
  tab("Side by side", ["Feature", "Access VLAN", "Native VLAN"], [
    ["Applies to", "Access ports", "802.1Q trunk ports"], ["Tagging", "Frames untagged (host has no idea about VLANs)", "The one VLAN sent untagged on the trunk"],
    ["Default", "VLAN 1", "VLAN 1"], ["Command", "switchport access vlan 10", "switchport trunk native vlan 99"], ["Must match", "The host's subnet", "Both ends of the trunk"]]),
  trick("Mismatch", "A native VLAN mismatch places untagged frames in different VLANs on each side — CDP reports it. Use the same, unused VLAN on both ends.", "warn"),
  dia("VLANs over a trunk", "vlan_trunk")], ["2-01", "2-02"])

C("access-trunk", "Access port vs trunk port", "d2", "One VLAN to an endpoint versus many VLANs between switches.", [
  tab("Side by side", ["Feature", "Access port", "Trunk port"], [
    ["VLANs carried", "One data VLAN (+ optional voice VLAN)", "Many VLANs (allowed list)"], ["Tagging", "Untagged (voice VLAN tagged by the phone)", "802.1Q tagged except native VLAN"],
    ["Connects to", "PCs, printers, phones, Local-mode APs", "Switches, routers (ROAS), WLCs, FlexConnect APs, hypervisors"],
    ["Commands", "switchport mode access / switchport access vlan X", "switchport mode trunk / switchport trunk allowed vlan …"],
    ["Verify", "show vlan brief", "show interfaces trunk"]]),
  bul("DTP", ["auto + auto = access", "desirable + auto/desirable/trunk = trunk", "Best practice: hard-code mode and use 'switchport nonegotiate' on trunks"])], ["2-01", "2-02"])

C("stp-rstp", "STP (802.1D) vs RSTP (802.1w)", "d2", "Legacy timer-based spanning tree versus rapid convergence.", [
  tab("Side by side", ["Feature", "802.1D STP", "802.1w RSTP / Rapid PVST+"], [
    ["Port states", "Blocking, Listening, Learning, Forwarding, Disabled", "Discarding, Learning, Forwarding"],
    ["Port roles", "Root, Designated, Blocking", "Root, Designated, Alternate, Backup"], ["Convergence", "Up to ~30–50 s (timers)", "Typically a few seconds (handshake/proposal-agreement)"],
    ["Edge ports", "PortFast (Cisco extension)", "Edge ports built in (PortFast configures them)"], ["Cisco per-VLAN version", "PVST+", "Rapid PVST+"]]),
  trick("Same election", "Both elect the root bridge with the lowest bridge ID and use the same cost values; RSTP just converges faster."),
  dia("Port roles example", "stp")], ["2-06"])

C("ipv4-ipv6", "IPv4 vs IPv6", "d1", "32-bit dotted decimal versus 128-bit hexadecimal addressing.", [
  tab("Side by side", ["Feature", "IPv4", "IPv6"], [
    ["Address size", "32 bits", "128 bits"], ["Notation", "Dotted decimal 192.168.1.1", "Hex hextets 2001:DB8::1"], ["Broadcast", "Yes", "No — multicast instead"],
    ["Address resolution", "ARP", "NDP (ICMPv6 NS/NA)"], ["Auto-configuration", "DHCP / APIPA", "SLAAC, DHCPv6, link-local always present"],
    ["Header checksum", "Yes", "No"], ["Private-style range", "RFC 1918 (10/8, 172.16/12, 192.168/16)", "Unique local FC00::/7"], ["Typical LAN prefix", "/24 (varies)", "/64"]]),
  dia("IPv6 address structure", "ipv6_structure")], ["1-08", "1-10"])

C("std-ext-acl", "Standard vs extended ACL", "d5", "Source-only filtering versus full 5-tuple filtering.", [
  tab("Side by side", ["Feature", "Standard", "Extended"], [
    ["Numbers", "1–99, 1300–1999", "100–199, 2000–2699"], ["Matches", "Source IP only", "Protocol, source, destination, ports"],
    ["Placement", "Near the destination", "Near the source"], ["Example", "access-list 10 permit 192.168.1.0 0.0.0.255", "access-list 110 deny tcp any host 10.1.1.10 eq 23"],
    ["Other uses", "NAT match lists, VTY access-class", "Firewall-style filtering"]]),
  bul("Same rules for both", ["Top-down, first match", "Implicit deny any at the end", "One ACL per interface, per direction, per protocol"]),
  dia("ACL processing", "acl_flow")], ["5-03"])

C("nat-pat", "NAT vs PAT", "d4", "One public address per inside host versus many hosts sharing one address via ports.", [
  tab("Side by side", ["Feature", "Static NAT", "Dynamic NAT", "PAT (overload)"], [
    ["Mapping", "1:1 permanent", "1:1 from a pool, while in use", "Many:1 using port numbers"], ["Inbound access", "Yes", "Only while a mapping exists", "Not for unsolicited sessions (without static entries)"],
    ["Address use", "One public per host", "Pool size limits hosts", "Thousands of hosts per public IP"], ["Keyword", "static", "pool", "overload"]]),
  dia("PAT example", "nat_pat")], ["4-02"])

C("public-private", "Public vs private IP addresses", "d1", "Internet-routable addresses versus reusable internal addresses.", [
  tab("Side by side", ["Feature", "Public", "Private (RFC 1918)"], [
    ["Ranges", "Most addresses outside reserved blocks", "10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16"], ["Routable on internet", "Yes", "No"],
    ["Uniqueness", "Globally unique, assigned by RIR/ISP", "Reused by many organizations"], ["Needs NAT for internet", "No", "Yes"]]),
  trick("Not private, not public", "127.0.0.0/8 loopback, 169.254.0.0/16 APIPA and 100.64.0.0/10 shared CGN space are special-purpose, not RFC 1918.", "warn")], ["1-08"])

C("aaa", "Authentication vs authorization vs accounting", "d5", "Who you are, what you may do, and what you did.", [
  tab("AAA", ["Function", "Question answered", "Example"], [
    ["Authentication", "Who are you?", "Username/password, certificate, MFA"], ["Authorization", "What are you allowed to do?", "Helpdesk may run show commands only"],
    ["Accounting", "What did you do, and when?", "Log of every command and session time"]]),
  tab("RADIUS vs TACACS+", ["", "RADIUS", "TACACS+"], [["Transport", "UDP 1812/1813", "TCP 49"], ["Encryption", "Password only", "Entire payload"],
                                                   ["AAA separation", "AuthN + AuthZ combined", "All three separate"], ["Typical use", "Network access (802.1X, VPN)", "Device administration"]])], ["5-02"])

C("trad-sdn", "Traditional networking vs SDN", "d6", "Box-by-box distributed control versus centralized, API-driven control.", [
  tab("Side by side", ["Feature", "Traditional", "Controller-based / SDN"], [
    ["Control plane", "Distributed on every device", "Centralized logic on a controller (devices still forward)"], ["Configuration", "Per-device CLI", "Central policy pushed via southbound APIs"],
    ["Interfaces", "CLI, SNMP", "Northbound REST APIs; southbound NETCONF, RESTCONF, OpenFlow…"], ["Consistency", "Prone to drift and typos", "Templates and intent enforce consistency"],
    ["Visibility", "Device by device", "Network-wide assurance and analytics"]]),
  dia("Controller architecture", "sdn")], ["6-01"])
