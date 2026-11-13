MISTAKES = []
def M(id, title, domain, wrong, right, recognize, avoid, example, lesson):
    MISTAKES.append(dict(id=id, title=title, domainId=domain, wrong=wrong, right=right, recognize=recognize, avoid=avoid, example=example, lessonId=lesson))

M("subnet-hosts", "Subnetting: forgetting the −2 or the wrong octet", "d1",
  "Usable hosts equal the total number of addresses, and the block size applies to the 4th octet for every prefix.",
  "Usable hosts = 2^h − 2 (except /31 and /32). The block size applies to the 'interesting' octet — the 3rd octet for /17–/24, the 2nd for /9–/16.",
  "Answer options include both 2^h and 2^h − 2 (e.g., 32 and 30), or the prefix is shorter than /24.",
  "Write h and the interesting octet before calculating. Check: does your answer subtract the network and broadcast?",
  "10.1.0.0/21 → interesting octet 3, block 8 → 10.1.0.0–10.1.7.255, 2,046 usable hosts (not 2,048, and not a /24-style answer).", "1-09")
M("net-bcast", "Confusing network and broadcast addresses", "d1",
  "x.x.x.0 is always the network address and x.x.x.255 is always the broadcast.",
  "Network = all host bits 0; broadcast = all host bits 1, for that prefix. In a /22, 10.1.5.255 can be a valid host and 10.1.7.255 is the broadcast.",
  "The question asks for 'first usable', 'last usable', or 'valid host' with a prefix shorter than /24.",
  "Always derive network and broadcast from the block, never from the last octet alone.",
  "10.1.4.0/22 spans 10.1.4.0–10.1.7.255. 10.1.5.0 and 10.1.5.255 are both valid hosts.", "1-09")
M("vlan-subnet", "Confusing VLANs and subnets", "d2",
  "Putting two hosts in the same VLAN lets them talk even if their IP subnets differ, or a VLAN routes traffic by itself.",
  "A VLAN is a Layer 2 broadcast domain; a subnet is a Layer 3 address range. Normally one VLAN = one subnet, and inter-VLAN traffic needs a router or Layer 3 switch.",
  "A host can reach nothing outside its VLAN, or a host is in the right VLAN but has an address from another VLAN's subnet.",
  "Check both: the switch port VLAN (show vlan brief) and the host IP/mask/gateway.",
  "PC in VLAN 20 configured with 192.168.10.50/24 (VLAN 10's subnet) cannot reach its gateway.", "2-01")
M("trunk", "Misunderstanding trunk ports", "d2",
  "A trunk automatically carries every VLAN, and the native VLAN is tagged like the others.",
  "Trunks carry only VLANs that are allowed AND exist on the switch; the native VLAN is untagged and must match on both ends. 'allowed vlan X' without 'add' replaces the list.",
  "show interfaces trunk output, native VLAN mismatch messages, or an 'allowed vlan' command without 'add'.",
  "Read all three trunk lists (allowed, active, forwarding) and check the native VLAN on both ends.",
  "switchport trunk allowed vlan 40 on a trunk carrying 10,20,30 leaves only VLAN 40.", "2-02")
M("stp", "Misunderstanding STP", "d2",
  "The switch with the highest priority or most connections becomes root; the root bridge has a root port.",
  "Lowest bridge ID (priority, then MAC) wins. The root bridge has no root port — all its ports are designated. Path selection uses total cost, not hop count.",
  "Questions giving priorities and MACs, or asking which port blocks.",
  "Follow the order: root bridge → root ports → designated ports → everything else discards.",
  "Priorities 4096 vs 32768: the 4096 switch is root even if its MAC is higher.", "2-06")
M("ad-metric", "Confusing administrative distance and metric", "d3",
  "The route with the lowest metric always wins, or AD is compared between routes to different prefixes.",
  "Order: longest prefix → lowest AD (between different sources for the same prefix) → lowest metric (within the same protocol).",
  "Routes from different protocols in the table, or [AD/metric] values in brackets.",
  "First filter by prefix length; only then look at AD; only then at metric.",
  "O 10.1.1.0/24 [110/2] vs S 10.1.0.0/16 [1/0]: a packet to 10.1.1.5 uses the OSPF /24.", "3-01")
M("rt-reading", "Misreading routing tables", "d3",
  "The first matching line in the output is used, or 'L' means loopback.",
  "IOS uses the longest matching prefix wherever it appears. L = local /32 for the router's own interface IP; C = connected subnet; S* = static default.",
  "An exhibit with overlapping routes like /16, /22 and /24.",
  "Write the destination, list every matching prefix, pick the longest, then read next hop and exit interface on that line.",
  "10.1.5.20 matches 10.1.0.0/16, 10.1.4.0/22 and 10.1.5.0/24 → the /24 is used.", "3-01")
M("wildcard", "Confusing wildcard masks and subnet masks", "d5",
  "ACLs and OSPF network statements use subnet masks, or the wildcard is just the mask reversed in order.",
  "Wildcard = 255.255.255.255 − subnet mask; 0 bits must match, 1 bits are ignored. /26 → 0.0.0.63.",
  "Options containing both 255.255.255.192 and 0.0.0.63 for an ACL or OSPF command.",
  "In ACLs and 'network … area' commands, expect small numbers at the end (0.0.0.x).",
  "network 10.0.12.4 0.0.0.3 area 0 — not 255.255.255.252.", "5-03")
M("acl", "Misunderstanding ACL processing", "d5",
  "The most specific entry wins, or traffic not denied is permitted.",
  "Entries are processed top-down; the first match decides; anything unmatched is dropped by the implicit deny.",
  "An ACL with a broad permit above a specific deny, or an ACL with only deny lines.",
  "Walk the packet line by line and stop at the first match; remember the invisible deny at the end.",
  "permit 192.168.1.0 0.0.0.255 then deny host 192.168.1.66 → .66 is permitted.", "5-03")
M("tcp-udp", "Confusing TCP and UDP", "d1",
  "Anything important uses TCP; DNS is UDP-only; TFTP is TCP like FTP.",
  "Choice depends on needs: reliability → TCP; low overhead/real time → UDP. DNS uses both; TFTP is UDP 69; TACACS+ is TCP 49; RADIUS is UDP.",
  "Port/transport matching questions and 'which uses a handshake' questions.",
  "Memorize the exceptions list: DNS (both), TFTP (UDP), TACACS+ (TCP), Syslog (UDP).",
  "SNMP is important but uses UDP 161/162.", "1-06")
M("ipv6-types", "Misunderstanding IPv6 address types", "d1",
  "FE80 addresses are routable, IPv6 uses broadcast, or FD00 addresses are global.",
  "Link-local FE80::/10 never leaves the link; IPv6 has no broadcast (multicast FF00::/8 instead); FC00::/7 (FD..) is unique local; 2000::/3 is global unicast.",
  "Questions asking which address a router uses as a next hop, or which address type a prefix is.",
  "Classify by the first hex digits: 2/3 global, FD unique local, FE80 link-local, FF multicast.",
  "OSPFv3 neighbors use link-local addresses as next hops.", "1-10")
M("dhcp-dns", "Confusing DHCP and DNS", "d4",
  "DNS assigns IP addresses, or DHCP resolves names.",
  "DHCP gives a host its IP settings (address, mask, gateway, DNS server). DNS translates names to addresses.",
  "Symptoms: 169.254.x.x address → DHCP problem; can ping IPs but not names → DNS problem.",
  "Ask: is the host missing its own address (DHCP) or failing to resolve names (DNS)?",
  "ping 8.8.8.8 works, ping www.cisco.com fails → check DNS, not DHCP.", "4-01")
M("nat-pat", "Confusing NAT and PAT", "d4",
  "Dynamic NAT lets unlimited hosts share a pool, or inside global means the private address.",
  "Dynamic NAT is one public address per active host; PAT ('overload') shares an address using ports. Inside local = private inside address; inside global = its public translation.",
  "Translation tables with ports, pool exhaustion scenarios, and NAT terminology questions.",
  "Look for the 'overload' keyword and port numbers in the translation table.",
  "ip nat inside source list 1 pool PUB (no overload) fails once all pool addresses are in use.", "4-02")
