from memorize import txt, bul, tab, mn, chunks, trick, code, dia
SHEETS = []
def S(id, title, domain, tags, blocks): SHEETS.append(dict(id=id, title=title, domainId=domain, tags=tags, blocks=blocks))

def m(p):
    v = (0xFFFFFFFF << (32 - p)) & 0xFFFFFFFF if p else 0
    return ".".join(str((v >> s) & 255) for s in (24, 16, 8, 0))
def wc(p):
    v = (~((0xFFFFFFFF << (32 - p)) & 0xFFFFFFFF)) & 0xFFFFFFFF if p else 0xFFFFFFFF
    return ".".join(str((v >> s) & 255) for s in (24, 16, 8, 0))

S("osi", "OSI model", "d1", ["layers", "pdu", "encapsulation"], [
    tab("Seven layers", ["#", "Layer", "Function", "PDU", "Examples"],
        [["7", "Application", "Network services to apps", "Data", "HTTP, DNS, SSH, DHCP"], ["6", "Presentation", "Format, encrypt, compress", "Data", "TLS, JPEG, ASCII"],
         ["5", "Session", "Open/manage/close sessions", "Data", "Session control"], ["4", "Transport", "Ports, reliability, segmentation", "Segment/Datagram", "TCP, UDP"],
         ["3", "Network", "Logical addressing, routing", "Packet", "IPv4, IPv6, ICMP"], ["2", "Data Link", "Framing, MAC, error detection", "Frame", "Ethernet, 802.11, 802.1Q"],
         ["1", "Physical", "Signals, media, connectors", "Bits", "UTP, fiber, RF"]]),
    mn("Mnemonic (7→1)", "All People Seem To Need Data Processing", ["Application", "Presentation", "Session", "Transport", "Network", "Data Link", "Physical"]),
    dia("Encapsulation", "encapsulation"),
    bul("Troubleshooting bottom-up", ["L1: link light, cable, speed/duplex", "L2: VLAN, MAC table, trunk, STP", "L3: IP/mask/gateway, routes", "L4: ports/ACLs", "L7: DNS, application"])])

S("tcpip", "TCP/IP model", "d1", ["layers", "tcp", "udp"], [
    tab("Layers", ["TCP/IP", "OSI", "Protocols"], [["Application", "5-7", "HTTP(S), DNS, DHCP, SSH, SNMP, NTP, FTP, TFTP"], ["Transport", "4", "TCP, UDP"],
                                                   ["Internet", "3", "IPv4, IPv6, ICMP"], ["Network Access", "1-2", "Ethernet, 802.11, ARP*"]], "*ARP sits between L2 and L3."),
    tab("TCP vs UDP", ["", "TCP", "UDP"], [["Connection", "Connection-oriented (3-way handshake)", "Connectionless"], ["Reliability", "ACKs, retransmission, sequencing", "Best effort"],
                                          ["Flow control", "Windowing", "None"], ["Header", "20 bytes min", "8 bytes"], ["Use", "Web, SSH, email, file transfer", "Voice/video, DNS query, DHCP, TFTP, SNMP"]]),
    dia("Three-way handshake", "tcp_handshake")])

PORT_ROWS = [["20/21", "TCP", "FTP data/control"], ["22", "TCP", "SSH, SCP, SFTP"], ["23", "TCP", "Telnet"], ["25", "TCP", "SMTP"], ["49", "TCP", "TACACS+"],
             ["53", "UDP/TCP", "DNS"], ["67/68", "UDP", "DHCP server/client"], ["69", "UDP", "TFTP"], ["80", "TCP", "HTTP"], ["110", "TCP", "POP3"], ["123", "UDP", "NTP"],
             ["143", "TCP", "IMAP"], ["161/162", "UDP", "SNMP / SNMP traps"], ["179", "TCP", "BGP"], ["389", "TCP/UDP", "LDAP"], ["443", "TCP", "HTTPS"], ["500", "UDP", "IKE (IPsec)"],
             ["514", "UDP", "Syslog"], ["830", "TCP", "NETCONF (SSH)"], ["1812/1813", "UDP", "RADIUS auth/acct"], ["3389", "TCP", "RDP"], ["4500", "UDP", "IPsec NAT-T"],
             ["5246/5247", "UDP", "CAPWAP control/data"]]
S("ports", "Common TCP/UDP ports", "d4", ["ports", "tcp", "udp"], [
    tab("Port reference", ["Port", "Transport", "Protocol"], PORT_ROWS),
    tab("Port ranges", ["Range", "Name"], [["0-1023", "Well-known"], ["1024-49151", "Registered"], ["49152-65535", "Dynamic / ephemeral"]]),
    tab("IP protocol numbers (not ports)", ["Protocol", "Number"], [["ICMP", "1"], ["TCP", "6"], ["UDP", "17"], ["ESP", "50"], ["AH", "51"], ["EIGRP", "88"], ["OSPF", "89"]])])

S("masks", "IPv4 subnet mask reference", "d1", ["mask", "cidr", "hosts"], [
    tab("All prefixes /8 – /32", ["Prefix", "Mask", "Wildcard", "Usable hosts"],
        [[f"/{p}", m(p), wc(p), ("2 (P2P)" if p == 31 else "1 (host)" if p == 32 else f"{2**(32-p)-2:,}")] for p in range(8, 33)]),
    trick("Valid mask octets", "0 128 192 224 240 248 252 254 255")])

S("cidr", "CIDR notation", "d1", ["cidr", "prefix", "summarization"], [
    txt("What CIDR means", "Classless Inter-Domain Routing writes the mask as a prefix length (/n = number of network bits) and ignores class boundaries."),
    tab("Prefix → addresses", ["Prefix", "Total addresses", "Equivalent /24s"], [["/16", "65,536", "256"], ["/20", "4,096", "16"], ["/21", "2,048", "8"], ["/22", "1,024", "4"],
                                                                                ["/23", "512", "2"], ["/24", "256", "1"], ["/25", "128", "½"], ["/26", "64", "¼"]]),
    txt("Summarization example", "10.1.0.0/24, 10.1.1.0/24, 10.1.2.0/24, 10.1.3.0/24 share the first 22 bits → summary 10.1.0.0/22 (third-octet values 0-3 differ only in the last 2 bits)."),
    trick("Rule", "Each prefix bit shorter doubles the addresses; each bit longer halves them.")])

S("special", "IPv4 private and special addresses", "d1", ["private", "rfc1918", "apipa", "loopback"], [
    tab("Ranges", ["Range", "Purpose"], [["10.0.0.0/8", "Private (RFC 1918)"], ["172.16.0.0/12", "Private (172.16-172.31)"], ["192.168.0.0/16", "Private"],
                                        ["127.0.0.0/8", "Loopback"], ["169.254.0.0/16", "APIPA / link-local"], ["100.64.0.0/10", "Shared address space (carrier-grade NAT)"],
                                        ["224.0.0.0/4", "Multicast"], ["240.0.0.0/4", "Reserved"], ["192.0.2.0/24, 198.51.100.0/24, 203.0.113.0/24", "Documentation (TEST-NET)"],
                                        ["255.255.255.255", "Limited broadcast"]]),
    tab("Classes", ["Class", "First octet", "Default mask"], [["A", "1-126", "/8"], ["B", "128-191", "/16"], ["C", "192-223", "/24"], ["D", "224-239", "Multicast"], ["E", "240-255", "Reserved"]])])

S("ipv6", "IPv6 address types", "d1", ["ipv6", "eui-64", "slaac", "ndp"], [
    tab("Types", ["Type", "Prefix", "Notes"], [["Global unicast", "2000::/3", "Routable"], ["Unique local", "FC00::/7", "Private-like (FD..)"], ["Link-local", "FE80::/10", "Auto; NDP; next hop"],
                                              ["Multicast", "FF00::/8", "FF02::1 nodes, ::2 routers, ::5/::6 OSPFv3, ::1:FFxx:xxxx solicited-node"],
                                              ["Loopback", "::1", ""], ["Unspecified", "::", ""], ["Anycast", "unicast space", "Nearest device"]]),
    bul("Shortening rules", ["Remove leading zeros per hextet", "Replace ONE longest run of all-zero hextets with ::"]),
    bul("EUI-64", ["Split MAC 48 bits in half", "Insert FFFE", "Flip 7th bit (U/L) of first byte", "0011.2233.4455 → 0211:22FF:FE33:4455"]),
    code("Key commands", ["ipv6 unicast-routing", "interface g0/0", " ipv6 address 2001:DB8:1:1::1/64", " ipv6 address 2001:DB8:1:1::/64 eui-64", " ipv6 address autoconfig", " ipv6 enable",
                          "show ipv6 interface brief", "show ipv6 route"]),
    tab("NDP (ICMPv6)", ["Message", "Type", "Use"], [["RS", "133", "Host asks for routers"], ["RA", "134", "Router advertises prefix/gateway"], ["NS", "135", "Address resolution / DAD"],
                                                  ["NA", "136", "Reply with MAC"], ["Redirect", "137", "Better next hop"]])])

S("subnetting", "Subnetting reference", "d1", ["subnetting", "block size", "vlsm"], [
    bul("Block-size method", ["1. Find the interesting octet (mask not 0/255)", "2. Block = 256 − mask octet", "3. Network = largest multiple of block ≤ host octet",
                              "4. Broadcast = next network − 1", "5. Usable = network+1 to broadcast−1"]),
    tab("Formulas", ["Want", "Formula"], [["Subnets", "2^s (s = borrowed bits)"], ["Usable hosts", "2^h − 2 (h = host bits)"], ["Host bits for N hosts", "smallest h with 2^h − 2 ≥ N"],
                                         ["Wildcard", "255.255.255.255 − mask"]]),
    txt("Worked example", "10.20.37.200/21 → 3rd octet, mask 248, block 8 → 37 is in 32-39 → network 10.20.32.0, broadcast 10.20.39.255, hosts 10.20.32.1 – 10.20.39.254 (2,046)."),
    bul("VLSM", ["Sort requirements largest → smallest", "Allocate each at the next free boundary of its block size", "Use /30 or /31 for P2P links"]),
    dia("Splitting a /24 into /26s", "subnetting")])

S("binary", "Binary and hex conversion", "d1", ["binary", "hex", "conversion"], [
    tab("Place values", ["128", "64", "32", "16", "8", "4", "2", "1"], [["2^7", "2^6", "2^5", "2^4", "2^3", "2^2", "2^1", "2^0"]]),
    tab("Hex ↔ binary ↔ decimal", ["Hex", "Binary", "Dec", "Hex", "Binary", "Dec"],
        [[format(i, "X"), format(i, "04b"), str(i), format(i + 8, "X"), format(i + 8, "04b"), str(i + 8)] for i in range(8)]),
    bul("Shortcuts", ["Ones from the left = mask values: 1→128, 2→192, 3→224, 4→240…", "255 − x flips every bit (wildcards)", "Hex byte → split into two nibbles: 0xC8 = 1100 1000 = 200",
                      "Hex to decimal: first digit × 16 + second digit (0xC8 = 12×16 + 8 = 200)"])])

S("vlan", "VLANs and trunking", "d2", ["vlan", "trunk", "802.1q", "dtp"], [
    tab("Ranges", ["Range", "Use"], [["1", "Default"], ["2-1001", "Normal"], ["1002-1005", "Reserved legacy"], ["1006-4094", "Extended"]]),
    code("Access / voice port", ["vlan 10", " name SALES", "interface g0/5", " switchport mode access", " switchport access vlan 10", " switchport voice vlan 20"]),
    code("Trunk", ["interface g0/1", " switchport trunk encapsulation dot1q   (only if ISL also supported)", " switchport mode trunk", " switchport trunk native vlan 99",
                   " switchport trunk allowed vlan 10,20,30", " switchport trunk allowed vlan add 40", " switchport nonegotiate"]),
    tab("DTP", ["Local \\ Remote", "auto", "desirable", "trunk"], [["auto", "Access", "Trunk", "Trunk"], ["desirable", "Trunk", "Trunk", "Trunk"], ["trunk", "Trunk", "Trunk", "Trunk"]]),
    code("Verify", ["show vlan brief", "show interfaces trunk", "show interfaces g0/1 switchport"]),
    code("Inter-VLAN (ROAS and SVI)", ["interface g0/0.10", " encapsulation dot1Q 10", " ip address 10.10.10.1 255.255.255.0", "!", "ip routing", "interface vlan 10",
                                       " ip address 10.10.10.1 255.255.255.0", " no shutdown"]),
    dia("Trunk diagram", "vlan_trunk")])

S("stp", "Spanning Tree Protocol", "d2", ["stp", "rstp", "portfast", "bpdu"], [
    bul("Election", ["Root bridge: lowest BID (priority + VLAN + MAC)", "Root port: lowest cost to root → lowest neighbor BID → lowest neighbor port ID", "Designated port: one per segment", "Rest: alternate/backup (discarding)"]),
    tab("Costs", ["Speed", "Cost"], [["10 Mbps", "100"], ["100 Mbps", "19"], ["1 Gbps", "4"], ["10 Gbps", "2"]]),
    tab("States", ["802.1D", "RSTP"], [["Blocking / Listening / Disabled", "Discarding"], ["Learning", "Learning"], ["Forwarding", "Forwarding"]]),
    tab("Timers (802.1D)", ["Timer", "Default"], [["Hello", "2 s"], ["Forward delay", "15 s"], ["Max age", "20 s"]]),
    code("Commands", ["spanning-tree mode rapid-pvst", "spanning-tree vlan 10 root primary", "spanning-tree vlan 10 priority 4096", "interface g0/5", " spanning-tree portfast",
                      " spanning-tree bpduguard enable", " spanning-tree guard root", "show spanning-tree vlan 10"]),
    dia("Topology example", "stp")])

S("etherchannel", "EtherChannel", "d2", ["etherchannel", "lacp", "pagp", "port-channel"], [
    tab("Modes", ["Protocol", "Modes", "Forms with"], [["LACP (802.3ad)", "active / passive", "active–active, active–passive"], ["PAgP (Cisco)", "desirable / auto", "desirable–desirable, desirable–auto"],
                                                   ["Static", "on", "on–on only"]]),
    code("Layer 2", ["interface range g0/1 - 2", " channel-group 1 mode active", "interface port-channel 1", " switchport mode trunk"]),
    code("Layer 3", ["interface range g0/1 - 2", " no switchport", " channel-group 2 mode active", "interface port-channel 2", " no switchport", " ip address 10.0.0.1 255.255.255.252"]),
    tab("show etherchannel summary flags", ["Flag", "Meaning"], [["P", "Bundled"], ["s", "Suspended"], ["D", "Down"], ["S", "Layer 2"], ["R", "Layer 3"], ["U", "In use"], ["I", "Stand-alone"]]),
    bul("Members must match", ["Speed and duplex", "Switchport mode", "Access VLAN / trunk native + allowed VLANs"])])

S("ospf", "OSPF", "d3", ["ospf", "dr", "bdr", "neighbor", "cost"], [
    tab("Facts", ["Item", "Value"], [["Type", "Link state (SPF/Dijkstra)"], ["AD", "110"], ["Metric", "Cost = 100 Mbps / BW (default)"], ["Hello/Dead", "10/40 s (broadcast, P2P)"],
                                    ["Multicast", "224.0.0.5 all, 224.0.0.6 DR/BDR"], ["IP protocol", "89"]]),
    bul("Neighbor must-match", ["Area", "Subnet/mask", "Hello/Dead timers", "Authentication", "Stub flag", "MTU (for Full)", "RIDs must be unique"]),
    bul("States", ["Down → Init → 2-Way → ExStart → Exchange → Loading → Full"]),
    bul("DR/BDR", ["Highest priority (default 1; 0 = never)", "Then highest RID", "Non-preemptive", "None on point-to-point"]),
    code("Configuration", ["router ospf 1", " router-id 1.1.1.1", " network 10.0.12.0 0.0.0.3 area 0", " passive-interface g0/2", " auto-cost reference-bandwidth 10000",
                           " default-information originate", "interface g0/1", " ip ospf 1 area 0", " ip ospf cost 10", " ip ospf priority 100", " ip ospf network point-to-point"]),
    code("Verify", ["show ip ospf neighbor", "show ip ospf interface brief", "show ip protocols", "show ip route ospf"]),
    dia("States", "ospf_states")])

S("static", "Static routing", "d3", ["static", "default route", "floating"], [
    code("IPv4", ["ip route 192.168.20.0 255.255.255.0 10.0.0.2          ! next hop", "ip route 192.168.20.0 255.255.255.0 g0/1               ! exit interface",
                  "ip route 192.168.20.0 255.255.255.0 g0/1 10.0.0.2      ! fully specified", "ip route 0.0.0.0 0.0.0.0 203.0.113.1                 ! default",
                  "ip route 10.9.9.9 255.255.255.255 10.0.0.2             ! host route", "ip route 0.0.0.0 0.0.0.0 198.51.100.1 130            ! floating (AD 130)"]),
    code("IPv6", ["ipv6 unicast-routing", "ipv6 route 2001:DB8:2::/64 2001:DB8:12::2", "ipv6 route ::/0 g0/1 FE80::2      ! link-local needs interface", "ipv6 route ::/0 2001:DB8:12::2 200   ! floating"]),
    bul("Rules", ["Static AD = 1", "Floating AD must exceed primary's AD", "Installed only if next hop reachable / interface up", "Remember the return route"])])

S("ad", "Administrative distance", "d3", ["ad", "route selection"], [
    tab("Defaults", ["Source", "AD"], [["Connected", "0"], ["Static", "1"], ["eBGP", "20"], ["EIGRP", "90"], ["OSPF", "110"], ["IS-IS", "115"], ["RIP", "120"], ["EIGRP external", "170"],
                                      ["iBGP", "200"], ["Unusable", "255"]]),
    trick("Selection order", "Longest prefix → lowest AD → lowest metric", "warn")])

S("routingtable", "Routing table interpretation", "d3", ["routing table", "show ip route", "longest match"], [
    code("Sample", ["Gateway of last resort is 10.0.12.2 to network 0.0.0.0", "S*  0.0.0.0/0 [1/0] via 10.0.12.2", "C   10.1.1.0/24 is directly connected, GigabitEthernet0/0",
                    "L   10.1.1.1/32 is directly connected, GigabitEthernet0/0", "O   10.2.2.0/24 [110/2] via 10.0.12.2, 00:04:11, GigabitEthernet0/1"]),
    tab("Fields of the O route", ["Field", "Value"], [["Source", "O = OSPF"], ["Prefix", "10.2.2.0/24"], ["AD / metric", "110 / 2"], ["Next hop", "10.0.12.2"], ["Age", "00:04:11"], ["Exit", "Gi0/1"]]),
    tab("Codes", ["Code", "Meaning"], [["C", "Connected"], ["L", "Local /32"], ["S", "Static"], ["S*", "Static default"], ["O", "OSPF intra-area"], ["O IA", "OSPF inter-area"],
                                      ["O E2", "OSPF external type 2"], ["D", "EIGRP"], ["R", "RIP"], ["B", "BGP"]]),
    dia("Forwarding example", "routing")])

S("acl", "ACL concepts", "d5", ["acl", "wildcard", "access-list"], [
    tab("Types", ["Type", "Numbers", "Matches", "Place"], [["Standard", "1-99, 1300-1999", "Source IP", "Near destination"], ["Extended", "100-199, 2000-2699", "Proto, src, dst, ports", "Near source"]]),
    bul("Rules", ["Top-down, first match", "Implicit deny any at end", "One ACL per interface, direction, protocol", "in = entering interface; out = leaving"]),
    tab("Wildcards", ["Keyword / prefix", "Wildcard"], [["host x", "0.0.0.0"], ["any", "255.255.255.255"], ["/24", "0.0.0.255"], ["/26", "0.0.0.63"], ["/30", "0.0.0.3"], ["/16", "0.0.255.255"]]),
    code("Examples", ["access-list 10 permit 192.168.1.0 0.0.0.255", "ip access-list extended WEB", " 10 permit tcp 10.1.1.0 0.0.0.255 host 10.2.2.10 eq 443", " 20 deny ip any host 10.2.2.10",
                      " 30 permit ip any any", "interface g0/1", " ip access-group WEB in", "line vty 0 4", " access-class 10 in", "show access-lists"]),
    dia("Processing flow", "acl_flow")])

S("nat", "NAT and PAT", "d4", ["nat", "pat", "overload"], [
    tab("Terms", ["Term", "Meaning"], [["Inside local", "Inside host's real (private) IP"], ["Inside global", "Public IP representing inside host"], ["Outside global", "Outside host's real IP"],
                                      ["Outside local", "Outside host as seen inside (usually = outside global)"]]),
    code("Static", ["interface g0/0", " ip nat inside", "interface g0/1", " ip nat outside", "ip nat inside source static 192.168.1.10 203.0.113.10"]),
    code("Dynamic pool", ["access-list 1 permit 192.168.1.0 0.0.0.255", "ip nat pool PUB 203.0.113.20 203.0.113.30 netmask 255.255.255.0", "ip nat inside source list 1 pool PUB"]),
    code("PAT", ["ip nat inside source list 1 interface g0/1 overload"]),
    code("Verify", ["show ip nat translations", "show ip nat statistics", "clear ip nat translation *"]),
    dia("PAT example", "nat_pat")])

S("dhcpdns", "DHCP and DNS", "d4", ["dhcp", "dns", "helper"], [
    bul("DHCP DORA", ["Discover (client broadcast)", "Offer (server)", "Request (client broadcast)", "Ack (server)", "UDP 67 server / 68 client"]),
    code("IOS DHCP server", ["ip dhcp excluded-address 192.168.10.1 192.168.10.10", "ip dhcp pool LAN10", " network 192.168.10.0 255.255.255.0", " default-router 192.168.10.1",
                             " dns-server 8.8.8.8", " lease 7"]),
    code("Relay and client", ["interface g0/1        ! client-facing", " ip helper-address 10.0.0.50", "interface g0/2", " ip address dhcp"]),
    code("Verify", ["show ip dhcp binding", "show ip dhcp pool", "show ip dhcp conflict"]),
    tab("DNS records", ["Record", "Maps"], [["A", "Name → IPv4"], ["AAAA", "Name → IPv6"], ["CNAME", "Alias → name"], ["MX", "Domain → mail server"], ["PTR", "IP → name"], ["NS", "Zone → name server"]]),
    code("IOS DNS", ["ip name-server 8.8.8.8", "ip domain-name example.com", "no ip domain-lookup   ! stop typo lookups"]),
    dia("DORA with relay", "dhcp_dora")])

S("wireless", "Wireless standards", "d1", ["wireless", "802.11", "wpa", "ap modes"], [
    tab("802.11", ["Std", "Name", "Band", "Max rate"], [["b", "—", "2.4", "11 Mbps"], ["a", "—", "5", "54 Mbps"], ["g", "—", "2.4", "54 Mbps"], ["n", "Wi-Fi 4", "2.4/5", "600 Mbps"],
                                                     ["ac", "Wi-Fi 5", "5", "~6.9 Gbps"], ["ax", "Wi-Fi 6/6E", "2.4/5/6", "~9.6 Gbps"]]),
    tab("Security", ["", "Encryption", "Personal", "Enterprise"], [["WPA", "TKIP", "PSK", "802.1X"], ["WPA2", "AES-CCMP", "PSK", "802.1X"], ["WPA3", "AES/GCMP", "SAE", "802.1X (192-bit option)"]]),
    tab("AP modes", ["Mode", "Purpose", "Switch port"], [["Local", "Tunnel all to WLC", "Access"], ["FlexConnect", "Local switching / WAN survivability", "Trunk"], ["Monitor", "Sensor only", "Access"],
                                                       ["Sniffer", "Capture to analyzer", "Access"], ["Bridge", "Mesh / point-to-point", "—"]]),
    bul("Key facts", ["2.4 GHz non-overlapping: 1, 6, 11", "CAPWAP UDP 5246 control / 5247 data", "AireOS WLC LAG = static (mode on)", "QoS: Platinum voice, Gold video, Silver BE, Bronze background"]),
    dia("Controller-based WLAN", "wireless")])

S("security", "Security fundamentals", "d5", ["security", "aaa", "port security", "vpn"], [
    tab("RADIUS vs TACACS+", ["", "RADIUS", "TACACS+"], [["Transport", "UDP 1812/1813", "TCP 49"], ["Encryption", "Password only", "Entire payload"], ["AAA", "AuthN+AuthZ combined", "Separated"],
                                                        ["Typical use", "Network access (802.1X, VPN)", "Device administration"], ["Standard", "Open", "Cisco-developed"]]),
    tab("Port security", ["Mode", "Drops", "Logs/counter", "Err-disable"], [["protect", "Yes", "No", "No"], ["restrict", "Yes", "Yes", "No"], ["shutdown (default)", "Yes", "Yes", "Yes"]]),
    code("Layer 2 security", ["switchport port-security", "switchport port-security maximum 2", "switchport port-security mac-address sticky", "ip dhcp snooping",
                              "ip dhcp snooping vlan 10", "interface g0/24", " ip dhcp snooping trust", " ip arp inspection trust", "ip arp inspection vlan 10"]),
    code("Device hardening", ["enable secret <pw>", "username admin secret <pw>", "service password-encryption", "login block-for 120 attempts 3 within 60", "line vty 0 4",
                              " login local", " transport input ssh", " exec-timeout 10 0"]),
    tab("VPN", ["", "Site-to-site", "Remote access"], [["Endpoints", "Gateway ↔ gateway", "Client ↔ gateway"], ["Host software", "None", "VPN client or browser"], ["Duration", "Always on", "On demand"]]),
    bul("Terms", ["Vulnerability = weakness", "Threat = potential danger", "Exploit = attack method", "Mitigation = countermeasure", "MFA = know + have + are"])])

S("automation", "Network automation terminology", "d6", ["automation", "sdn", "rest", "json", "ansible", "terraform"], [
    tab("Planes", ["Plane", "Does", "Examples"], [["Data", "Forwards traffic", "Switching, routing lookup, ACL, NAT"], ["Control", "Builds forwarding info", "OSPF, STP, ARP"],
                                                  ["Management", "Manages devices", "SSH, SNMP, Syslog, NTP"]]),
    tab("APIs", ["Interface", "Between", "Examples"], [["Northbound", "Controller ↔ apps", "REST/JSON"], ["Southbound", "Controller ↔ devices", "NETCONF, RESTCONF, OpenFlow, OpFlex, CLI, SNMP"]]),
    tab("CRUD ↔ HTTP", ["CRUD", "Verb"], [["Create", "POST"], ["Read", "GET"], ["Update", "PUT / PATCH"], ["Delete", "DELETE"]]),
    tab("Status codes", ["Code", "Meaning"], [["200", "OK"], ["201", "Created"], ["204", "No content"], ["400", "Bad request"], ["401", "Unauthorized (not authenticated)"],
                                             ["403", "Forbidden"], ["404", "Not found"], ["500", "Server error"]]),
    tab("Ansible vs Terraform", ["", "Ansible", "Terraform"], [["Approach", "Procedural tasks (idempotent modules)", "Declarative desired state"], ["Language", "YAML", "HCL"],
                                                              ["Agent", "Agentless (SSH/API)", "Agentless (provider APIs)"], ["Key files", "Inventory, playbook", "*.tf, state file"]]),
    code("JSON example", ['{', '  "hostname": "SW1",', '  "vlans": [10, 20],', '  "mgmt": {"ip": "10.0.0.11", "up": true}', '}']),
    bul("SD-Access", ["Underlay: IS-IS", "Overlay data plane: VXLAN", "Control plane: LISP", "Policy: TrustSec SGTs", "Controller: Catalyst Center"]),
    dia("Controller architecture", "sdn")])

S("ios", "Cisco IOS command reference", "d2", ["ios", "commands", "show"], [
    code("Navigation & files", ["enable", "configure terminal", "end / exit", "copy running-config startup-config", "write memory", "show running-config", "show startup-config",
                                "show version", "reload", "do show ip interface brief   ! from config mode"]),
    code("Basic device setup", ["hostname R1", "no ip domain-lookup", "banner motd # Authorized only #", "enable secret <pw>", "line console 0", " password <pw>", " login",
                                " logging synchronous", "service password-encryption", "clock timezone PHT 8"]),
    code("Interfaces", ["interface g0/0", " description Uplink to SW1", " ip address 10.1.1.1 255.255.255.0", " no shutdown", " speed 1000", " duplex full",
                        "show ip interface brief", "show interfaces g0/0", "show interfaces status"]),
    code("Switching", ["show mac address-table", "show vlan brief", "show interfaces trunk", "show spanning-tree", "show etherchannel summary", "show cdp neighbors detail",
                       "show lldp neighbors", "show port-security interface g0/5"]),
    code("Routing & services", ["show ip route", "show ip protocols", "show ip ospf neighbor", "show ipv6 route", "show ip nat translations", "show ip dhcp binding",
                                "show ntp associations", "show access-lists", "show logging", "show standby brief"]),
    code("Troubleshooting", ["ping 10.1.1.1", "traceroute 10.2.2.10", "debug ip ospf adj   /   undebug all", "terminal monitor", "show processes cpu"])])
