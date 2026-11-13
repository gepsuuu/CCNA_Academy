from qlib import *
D = "d1"

S(D, "1.1", "components", 1, "concept", "Which device forwards packets between different IP networks by consulting its routing table?",
  ["*Router || A router makes Layer 3 forwarding decisions by matching the destination IP address against its routing table.",
   "Layer 2 switch || A Layer 2 switch forwards frames inside a VLAN using MAC addresses; it cannot route between subnets.",
   "Access point || An access point bridges 802.11 clients onto the wired LAN; it does not route between IP networks.",
   "Hub || A hub repeats signals out every port and makes no addressing decisions."],
  "Moving traffic between different IP subnets is a Layer 3 function performed by routers (or Layer 3 switches). Hosts send off-subnet traffic to their default gateway, which looks up the destination in its routing table.",
  "Router vs switch roles", "Switches connect devices in a network; routers connect networks.")

S(D, "1.1.c", "components", 2, "scenario",
  "A company must block a specific file-sharing application that tunnels over TCP 443, while still allowing normal HTTPS browsing. Which capability is required?",
  ["*A next-generation firewall (NGFW) || An NGFW identifies applications by behavior and signatures, not just by port, so it can block one app on 443 and allow others.",
   "An extended ACL denying TCP 443 || This would block all HTTPS traffic, including legitimate browsing.",
   "A standard ACL on the internet interface || Standard ACLs match only the source IP address and cannot identify applications.",
   "Port security on access switches || Port security limits MAC addresses per switch port; it does not inspect applications."],
  "Traditional ACLs and stateful firewalls work with addresses and ports. When several applications share the same port, only application-aware inspection (AVC in an NGFW) can tell them apart.",
  "NGFW application awareness", "Same port, different app → you need application awareness (NGFW).")

S(D, "1.1.h", "components", 2, "concept", "Which statements about Power over Ethernet are correct?",
  ["*A switch acting as PSE can power an IP phone over the same cable that carries its data || PoE delivers power and data on one Ethernet cable; the switch is the Power Sourcing Equipment.",
   "*IEEE 802.3at (PoE+) provides up to 30 W per port at the PSE || 802.3at raised the per-port budget from 15.4 W (802.3af) to 30 W.",
   "IEEE 802.3af provides up to 60 W per port || 802.3af provides 15.4 W at the PSE; 60 W is an 802.3bt (Type 3) level.",
   "PoE always requires a separate power injector between the switch and the device || PoE-capable switches power devices directly; injectors are only used with non-PoE switches."],
  "PoE lets a PSE (usually the access switch) power PDs such as APs, phones and cameras. Standards: 802.3af 15.4 W, 802.3at 30 W, 802.3bt up to 60 W (Type 3) or 90 W (Type 4) at the PSE.",
  "PoE standards", "af < at < bt — alphabetical order = more power.")

S(D, "1.2", "topology", 2, "scenario",
  "In a data center, every top-of-rack switch has one uplink to every backbone switch, top-of-rack switches never connect to each other, and any server-to-server path crosses the same number of hops. Which architecture is this?",
  ["*Spine-leaf || Leaves (top-of-rack) connect to every spine and never to each other, giving predictable east-west latency.",
   "Three-tier campus || Three-tier uses access, distribution and core layers; access switches do not connect to every core switch.",
   "Collapsed core || Collapsed core merges campus core and distribution; it does not describe leaf-to-every-spine connectivity.",
   "Hub-and-spoke WAN || Hub-and-spoke connects branch sites to a central site over a WAN, not racks in a data center."],
  "Spine-leaf is built for east-west traffic: every leaf links to every spine, so any two servers on different leaves are always leaf → spine → leaf apart.",
  "Spine-leaf topology", "Leaves never touch leaves; spines never touch spines.")

TF(D, "1.2", "topology", 1, "concept", "In a two-tier (collapsed core) campus design, the core and distribution layers are combined into one layer.", True,
   "A collapsed core merges the distribution and core functions, which suits smaller campuses that do not need a separate core.", "Two-tier campus design")

M(D, "1.2", "topology", 1, "concept", "Match each element to its primary role.",
  [("Access layer", "Connects endpoints and provides PoE and port security"),
   ("Distribution layer", "Aggregates access switches and enforces routing/policy boundaries"),
   ("Core layer", "Provides high-speed transport between distribution blocks"),
   ("SOHO router", "Combines routing, switching, firewall and Wi-Fi in one device")],
  "Hierarchical designs give each layer one job: access for endpoints, distribution for aggregation and policy, core for fast transport. Small offices typically use one all-in-one device.",
  "Hierarchical design layers")

S(D, "1.2.f", "virtualization", 2, "scenario",
  "A company rents virtual machines, storage and virtual networks from a provider but installs, patches and manages its own operating systems and applications. Which cloud service model is this?",
  ["*IaaS || Infrastructure as a Service provides compute, storage and networking; the customer manages OS and applications.",
   "PaaS || With PaaS the provider also manages the OS and runtime; the customer only deploys code.",
   "SaaS || With SaaS the provider delivers a complete application; the customer manages no OS at all.",
   "Private cloud || Private cloud is a deployment model (who uses it), not a service model (what is provided)."],
  "Service models describe how much the provider manages: IaaS (infrastructure), PaaS (plus OS/runtime), SaaS (the whole application). Deployment models are public, private, community and hybrid.",
  "Cloud service models", "IaaS = you manage from the OS up.")

S(D, "1.4", "cabling", 3, "troubleshooting",
  "Users on Gi0/5 report very slow file transfers. Based on the output, what is the most likely cause?",
  ["*A duplex mismatch with the connected device || Late collisions on a half-duplex port are the classic symptom of the other end running full duplex.",
   "The port is assigned to the wrong VLAN || A wrong VLAN causes loss of connectivity, not collisions and CRC errors.",
   "Spanning tree is blocking the port || A blocking port does not forward user traffic at all and would not count collisions.",
   "Frames are larger than the MTU || The output shows 0 giants, so oversized frames are not the problem."],
  "When one side runs half duplex and the other full duplex, the full-duplex side transmits while the half-duplex side is sending. The half-duplex side records collisions—including late collisions after the first 64 bytes—and the other side sees CRC errors and runts. Fix by setting both ends to auto (or the same fixed values).",
  "Duplex mismatch symptoms", "Late collisions = check duplex.",
  ex="SW1# show interfaces gigabitethernet0/5\nGigabitEthernet0/5 is up, line protocol is up (connected)\n  Half-duplex, 100Mb/s, media type is 10/100/1000BaseTX\n  ...\n     0 runts, 0 giants, 0 throttles\n     12 input errors, 12 CRC, 0 frame, 0 overrun, 0 ignored\n     0 output errors, 1523 collisions, 0 interface resets\n     0 babbles, 418 late collision, 0 deferred")

S(D, "1.3.a", "cabling", 1, "scenario", "Two buildings are 1.5 km apart and need a 10 Gbps link. Which medium is most appropriate?",
  ["*Single-mode fiber || SMF uses a narrow core and laser optics to carry high speeds over kilometers.",
   "Multimode fiber || MMF is intended for shorter runs, typically up to a few hundred meters at 10 Gbps.",
   "Cat 6A UTP || Twisted-pair Ethernet is limited to 100 m per segment.",
   "Cat 5e UTP with a crossover cable || Cable pinout does not change the 100 m copper limit."],
  "Copper Ethernet segments are limited to 100 m. Multimode fiber covers in-building and campus distances of hundreds of meters; single-mode fiber covers kilometers.",
  "Fiber vs copper distances", "Long distance = single-mode laser.")

S(D, "1.4", "cabling", 3, "scenario",
  "A Cisco switch port left at auto speed/duplex connects to a PC whose NIC is hard-coded to 100 Mbps full duplex with auto-negotiation disabled. What will the switch port operate at?",
  ["*100 Mbps, half duplex || The switch can sense the speed but cannot learn the duplex, so it falls back to half duplex at 10/100 Mbps — creating a duplex mismatch.",
   "100 Mbps, full duplex || Full duplex would require successful negotiation, which cannot happen when the PC has negotiation disabled.",
   "1000 Mbps, full duplex || The PC is fixed at 100 Mbps; the switch detects that speed from the signal.",
   "The link stays down || The link comes up because the switch detects the speed; the problem is the duplex fallback."],
  "If auto-negotiation fails because the other side has it disabled, the auto side detects the speed but defaults to half duplex for 10/100 Mbps (full duplex for 1 Gbps). Hard-coding only one side therefore often causes a duplex mismatch.",
  "Auto-negotiation fallback", "Configure both ends the same: both auto or both fixed.")

S(D, "1.13", "ethernet-arp", 1, "diagram", "Refer to the frame diagram. Which field lets the receiving NIC detect that a frame was corrupted in transit?",
  ["*FCS || The 4-byte Frame Check Sequence carries a CRC; a mismatch increments the CRC error counter and the frame is discarded.",
   "Preamble || The preamble synchronizes the receiver's clock; it does not check integrity.",
   "Type || The Type (EtherType) field identifies the payload protocol, such as 0x0800 for IPv4.",
   "Source MAC || The source MAC identifies the sender and is used by switches for learning."],
  "Ethernet is the only common layer that adds a trailer: the FCS. The receiver recalculates the CRC over the frame and compares it with the FCS.",
  "Ethernet frame fields", "Only Layer 2 adds a trailer — the FCS.", dg="frame_quiz")

S(D, "1.0", "ethernet-arp", 3, "scenario",
  "Host A (192.168.1.10/24, gateway 192.168.1.1) pings 8.8.8.8 for the first time with an empty ARP cache. Which IPv4 address does Host A place in the target field of its ARP request?",
  ["*192.168.1.1 || 8.8.8.8 is off-subnet, so Host A needs the MAC address of its default gateway.",
   "8.8.8.8 || ARP only resolves addresses on the local link; a remote destination is reached through the gateway.",
   "255.255.255.255 || The broadcast applies to the destination MAC (FFFF.FFFF.FFFF), not the ARP target IP.",
   "192.168.1.10 || Asking for your own address is a gratuitous ARP, which is not used to reach a remote host."],
  "A host compares the destination with its own subnet. For remote destinations it ARPs for the default gateway and sends the frame to the gateway's MAC while the IP packet keeps 8.8.8.8 as the destination.",
  "ARP for remote destinations", "Off-subnet? ARP for the gateway, not the destination.")

S(D, "1.0", "ethernet-arp", 2, "concept", "Which statements about IPv4 ARP are true?",
  ["*An ARP request is sent to the broadcast MAC address FFFF.FFFF.FFFF || Every host in the broadcast domain must receive the question.",
   "*An ARP reply is normally sent as a unicast to the requester || Only the requester needs the answer, so the owner replies directly.",
   "Routers forward ARP requests to other subnets || Routers do not forward Layer 2 broadcasts; ARP is confined to the local broadcast domain.",
   "IPv6 uses ARP with a larger address field || IPv6 replaces ARP with Neighbor Discovery (ICMPv6 NS/NA)."],
  "ARP maps IPv4 addresses to MAC addresses on the local link: broadcast request, unicast reply, then both hosts cache the mapping.",
  "ARP operation")

TF(D, "1.0", "ethernet-arp", 2, "concept",
   "As a packet travels from a PC through two routers to a server (no NAT), the source and destination MAC addresses change at each hop while the source and destination IP addresses stay the same.", True,
   "Each router removes the incoming frame and builds a new one for the next link using its own MAC as the source and the next hop's MAC as the destination. The IP header is end to end.",
   "Layer 2 vs Layer 3 addressing", "MACs are hop-by-hop; IPs are end-to-end.")

S(D, "1.5", "tcp-udp", 2, "scenario", "Why is the voice stream of a VoIP call carried over UDP rather than TCP?",
  ["*A retransmitted voice packet would arrive too late to be useful || Real-time media prefers timely delivery; TCP retransmissions add delay and jitter.",
   "UDP encrypts the voice payload so calls cannot be intercepted || UDP provides no encryption; secure voice uses SRTP or similar.",
   "UDP guarantees in-order delivery of every voice sample || UDP provides no sequencing or delivery guarantees.",
   "TCP cannot carry packets that contain audio || TCP can carry any data; it is simply a poor fit for real-time media."],
  "UDP is connectionless with an 8-byte header and no retransmissions. For real-time audio and video, a late packet is as useless as a lost one, so applications use UDP and tolerate small losses.",
  "UDP for real-time traffic")

O(D, "1.5", "tcp-udp", 1, "protocol-id", "Arrange the messages that open a TCP connection and begin data transfer in the correct order.",
  ["Client sends SYN", "Server replies with SYN-ACK", "Client sends ACK", "Application data is exchanged"],
  "TCP's three-way handshake synchronizes sequence numbers in both directions (SYN, SYN-ACK, ACK) before any application data flows.",
  "TCP three-way handshake", "Ask → answer-and-ask → answer.")

M(D, "1.5", "tcp-udp", 2, "protocol-id", "Match each protocol to its transport and port.",
  [("SSH", "TCP 22"), ("DHCP server", "UDP 67"), ("TACACS+", "TCP 49"), ("Syslog", "UDP 514"), ("NTP", "UDP 123")],
  "SSH uses TCP 22; DHCP servers listen on UDP 67 (clients 68); TACACS+ uses TCP 49; Syslog uses UDP 514; NTP uses UDP 123.",
  "Well-known ports", "TACACS+ = TCP, both start with T.")

S(D, "1.5", "tcp-udp", 2, "protocol-id", "Which protocols use TCP as their transport?",
  ["*BGP || BGP peers establish a TCP session on port 179.",
   "*SMTP || SMTP transfers mail over TCP port 25.",
   "TFTP || TFTP uses UDP port 69.",
   "SNMP || SNMP uses UDP 161 for polling and UDP 162 for traps.",
   "NTP || NTP uses UDP 123."],
  "Protocols that need reliable sessions (BGP, SMTP, SSH, HTTP/HTTPS, FTP) use TCP. Simple request/response or management protocols (TFTP, SNMP, NTP, Syslog, DHCP) use UDP.",
  "TCP vs UDP protocols")

S(D, "1.5", "tcp-udp", 2, "concept", "Which TCP mechanism lets a receiver control how much data the sender may transmit before it must wait for an acknowledgment?",
  ["*Windowing || The receive window advertises how many bytes may be outstanding, providing flow control.",
   "Sequencing || Sequence numbers order the data and detect gaps but do not limit the sending rate.",
   "The three-way handshake || The handshake opens the connection; it does not control ongoing flow.",
   "The checksum || The checksum detects corruption only."],
  "TCP flow control uses a sliding window: the receiver advertises its window size, and the sender cannot have more unacknowledged data in flight than that window.",
  "TCP windowing")

S(D, "1.6", "binary", 1, "calculation", "What is the decimal value of the binary octet 10101100?",
  ["*172 || 128 + 32 + 8 + 4 = 172.", "168 || 168 is 10101000; the 4-bit is missing.", "170 || 170 is 10101010.", "180 || 180 is 10110100."],
  "Write the place values 128 64 32 16 8 4 2 1 above the bits and add those that are 1: 128 + 32 + 8 + 4 = 172.",
  "Binary to decimal", "Write 128-64-32-16-8-4-2-1 on your scratch board first.", verify=lambda: str(int("10101100", 2)))

S(D, "1.8", "binary", 2, "calculation", "What is the decimal value of the hexadecimal byte 0xC8?",
  ["*200 || C = 12, so 12 × 16 + 8 = 200.", "192 || 192 is 0xC0.", "208 || 208 is 0xD0.", "184 || 184 is 0xB8."],
  "Each hex digit is four bits. The first digit is worth 16 times its value: C (12) × 16 = 192, plus 8 = 200 (binary 1100 1000).",
  "Hexadecimal conversion", "High digit × 16 + low digit.", verify=lambda: str(0xC8))

S(D, "1.7", "ipv4", 1, "concept", "Which addresses are RFC 1918 private addresses?",
  ["*172.20.5.1 || 172.16.0.0/12 covers 172.16.0.0 – 172.31.255.255.",
   "*10.200.1.1 || 10.0.0.0/8 is entirely private.",
   "172.32.1.1 || 172.32.x.x is outside 172.16.0.0/12 and is public.",
   "192.169.1.1 || Only 192.168.0.0/16 is private.",
   "169.254.1.1 || 169.254.0.0/16 is link-local (APIPA), not an RFC 1918 range."],
  "RFC 1918 defines 10.0.0.0/8, 172.16.0.0/12 and 192.168.0.0/16. APIPA (169.254/16) and loopback (127/8) are special ranges but not RFC 1918.",
  "Private IPv4 ranges", "172 is private only from .16 to .31.",
  verify=lambda: [a for a in ["172.20.5.1", "10.200.1.1", "172.32.1.1", "192.169.1.1", "169.254.1.1"]
                  if any(in_net(a, n) for n in ("10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16"))])

S(D, "1.10", "client-ip", 1, "troubleshooting", "A user's PC shows the address 169.254.88.12/16 and cannot reach any server. What is the most likely issue?",
  ["*The PC did not receive a reply from a DHCP server || Windows and macOS self-assign an APIPA 169.254.x.x address when DHCP fails.",
   "The DNS server address is wrong || A DNS problem would not change the PC's own IP address.",
   "The NAT pool on the edge router is exhausted || NAT happens on the router and does not alter the host's local address.",
   "The PC has a duplicate static address || A static address would be the configured one, not 169.254.x.x."],
  "169.254.0.0/16 is used for automatic link-local addressing when no DHCP server answers. Check the cable/VLAN, the DHCP server, and any DHCP relay (ip helper-address).",
  "APIPA", "169.254 = DHCP failed.")

S(D, "1.6", "subnetting", 1, "calculation", "How many usable host addresses are in 192.168.10.0/27?",
  ["*30 || 32 − 27 = 5 host bits; 2^5 − 2 = 30.", "32 || 32 is the total number of addresses, including network and broadcast.",
   "14 || 14 is the number of usable hosts in a /28.", "62 || 62 is the number of usable hosts in a /26."],
  "Usable hosts = 2^h − 2, where h is the number of host bits. The two reserved addresses are the network and broadcast addresses.",
  "Usable hosts formula", "/27 → block 32 → 30 hosts.", verify=lambda: hosts(27))

S(D, "1.6", "subnetting", 2, "calculation", "What is the broadcast address of the subnet containing 172.16.70.200/21?",
  ["*172.16.71.255 || /21 has a block size of 8 in the third octet; 70 falls in 64–71, so the broadcast is 172.16.71.255.",
   "172.16.79.255 || That would require a block of 16 starting at 64 (a /20).",
   "172.16.70.255 || That is the broadcast of a /24, not a /21.",
   "172.16.63.255 || That is the broadcast of the previous /21 (172.16.56.0/21)."],
  "Mask /21 = 255.255.248.0. Block size = 256 − 248 = 8. Multiples of 8 near 70: 64 and 72. Network 172.16.64.0, broadcast 172.16.71.255.",
  "Block-size subnetting", verify=lambda: bcast("172.16.70.200/21"))

S(D, "1.6", "subnetting", 2, "calculation", "What is the network address for host 10.45.130.77/19?",
  ["*10.45.128.0 || /19 has a block size of 32 in the third octet; 130 lies in 128–159.",
   "10.45.130.0 || That treats the prefix as /24.", "10.45.96.0 || 96 is the previous /19 block (96–127).", "10.45.0.0 || That treats the prefix as /16."],
  "Mask /19 = 255.255.224.0; block size 32. The largest multiple of 32 not exceeding 130 is 128, and the fourth octet becomes 0.",
  "Finding the network address", verify=lambda: net("10.45.130.77/19"))

S(D, "1.6", "subnetting", 3, "calculation", "What is the LAST usable host address in the subnet that contains 192.168.33.150/26?",
  ["*192.168.33.190 || /26 blocks are 64; 150 is in 128–191, so the broadcast is .191 and the last host is .190.",
   "192.168.33.191 || .191 is the broadcast address and cannot be assigned.",
   "192.168.33.189 || .189 is a valid host but not the last one.",
   "192.168.33.254 || .254 belongs to the 192–255 subnet."],
  "Find the block (128–191), take the broadcast (.191) and subtract one for the last usable host.",
  "Usable host range", "Last usable = broadcast − 1.", verify=lambda: last_host("192.168.33.150/26"))

S(D, "1.6", "subnetting", 3, "calculation", "Which addresses are valid, assignable host addresses in 10.10.16.0/20?",
  ["*10.10.31.254 || The /20 spans 10.10.16.0 – 10.10.31.255; .31.254 is the last usable host.",
   "*10.10.20.0 || Inside a /20, 10.10.20.0 is neither the network nor the broadcast address, so it is a valid host.",
   "10.10.32.1 || 10.10.32.0 begins the next /20 block.",
   "10.10.31.255 || This is the broadcast address of the subnet.",
   "10.10.16.0 || This is the network address of the subnet."],
  "A /20 has a block size of 16 in the third octet. 10.10.16.0/20 runs from 10.10.16.0 (network) to 10.10.31.255 (broadcast). Addresses ending in .0 or .255 inside the range are valid hosts when they are not the boundaries.",
  "Host validity in larger subnets", "x.x.x.0 can be a valid host when the prefix is shorter than /24.",
  verify=lambda: [a for a in ["10.10.31.254", "10.10.20.0", "10.10.32.1", "10.10.31.255", "10.10.16.0"]
                  if in_net(a, "10.10.16.0/20") and a not in (net("10.10.16.0/20"), bcast("10.10.16.0/20"))])

S(D, "1.6", "subnetting", 3, "scenario",
  "Using VLSM on 172.16.4.0/22 (largest subnet first, starting at 172.16.4.0, no wasted gaps), you must allocate: LAN A 300 hosts, LAN B 100 hosts, LAN C 50 hosts and two point-to-point links. Which subnet is assigned to LAN B?",
  ["*172.16.6.0/25 || LAN A uses 172.16.4.0/23 (4.0–5.255); the next free block is 172.16.6.0, and 100 hosts need a /25.",
   "172.16.5.0/25 || This overlaps LAN A's /23, which already covers 172.16.5.0.",
   "172.16.6.0/24 || A /24 provides 254 hosts — far more than needed, wasting space for LAN C and the links.",
   "172.16.4.0/25 || This overlaps LAN A at the start of the block."],
  "300 hosts → /23 (510 hosts): 172.16.4.0/23. 100 hosts → /25 (126): 172.16.6.0/25. 50 hosts → /26 (62): 172.16.6.128/26. Links → /30: 172.16.6.192/30 and 172.16.6.196/30.",
  "VLSM allocation", "Allocate largest first, on the next free boundary.")

S(D, "1.6", "subnetting", 2, "scenario",
  "You need at least 6 subnets from 192.168.100.0/24, each supporting at least 25 hosts. Which mask meets both requirements?",
  ["*255.255.255.224 || /27 gives 8 subnets with 30 usable hosts each.",
   "255.255.255.192 || /26 gives only 4 subnets.",
   "255.255.255.240 || /28 gives only 14 hosts per subnet.",
   "255.255.255.248 || /29 gives only 6 hosts per subnet."],
  "Borrow s bits so 2^s ≥ 6 (s = 3) and keep h host bits so 2^h − 2 ≥ 25 (h = 5). 24 + 3 = /27 and 32 − 27 = 5 — both satisfied.",
  "Subnet and host requirements", verify=lambda: mask(27))

TF(D, "1.6", "subnetting", 2, "concept", "A /31 prefix can be used on a point-to-point router link, giving two usable addresses with no network or broadcast address.", True,
   "RFC 3021 allows /31 on point-to-point links; both addresses are assigned to the two router interfaces, saving address space compared with /30.",
   "/31 point-to-point links")

S(D, "1.8", "ipv6", 1, "calculation", "What is the shortest valid form of 2001:0DB8:0000:0000:0000:0000:0A00:0001?",
  ["*2001:DB8::A00:1 || Leading zeros are removed and the single run of zero hextets is replaced by ::.",
   "2001:DB8::A:1 || Trailing zeros in a hextet cannot be removed; 0A00 becomes A00, not A.",
   "2001:DB8:::A00:1 || Three colons in a row are never valid.",
   "2001:DB8:0::A00:1 || This is valid but not the shortest — the zero hextet should be absorbed into ::."],
  "Rule 1: remove leading zeros in each hextet. Rule 2: replace one run of all-zero hextets with ::. Only leading zeros may be dropped.",
  "IPv6 abbreviation", "Leading zeros only; :: only once.", verify=lambda: v6("2001:0DB8:0000:0000:0000:0000:0A00:0001"))

TF(D, "1.8", "ipv6", 1, "concept", "2001:DB8::1::5 is a valid IPv6 address.", False,
   "The :: shorthand can appear only once; with two, the number of missing zero hextets is ambiguous.", "IPv6 notation rules")

M(D, "1.9", "ipv6", 2, "concept", "Match each IPv6 prefix to its address type.",
  [("FE80::/10", "Link-local"), ("FC00::/7", "Unique local"), ("FF00::/8", "Multicast"), ("2000::/3", "Global unicast"), ("::1/128", "Loopback")],
  "Link-local FE80::/10 is created on every interface; unique local FC00::/7 is private-like; FF00::/8 is multicast; 2000::/3 is global unicast; ::1 is loopback.",
  "IPv6 address types", "FE80 = link, FD = local, FF = flock, 2/3 = global.")

S(D, "1.9.d", "ipv6", 3, "calculation",
  "An interface with MAC address 00A0.C912.3456 is configured with 'ipv6 address 2001:DB8:5:1::/64 eui-64'. What global unicast address does it receive?",
  ["*2001:DB8:5:1:2A0:C9FF:FE12:3456 || Split the MAC, insert FFFE, and flip the 7th bit (00 → 02).",
   "2001:DB8:5:1:A0:C9FF:FE12:3456 || The universal/local bit was not flipped.",
   "2001:DB8:5:1:2A0:C912:FFFE:3456 || FFFE must be inserted exactly in the middle of the 48-bit MAC.",
   "FE80::2A0:C9FF:FE12:3456 || This is the EUI-64 link-local address, not the global address requested."],
  "Modified EUI-64: 00A0C9 | 123456 → 00A0:C9FF:FE12:3456, then invert bit 7 of the first byte: 00 → 02. Appended to the /64 prefix gives 2001:DB8:5:1:2A0:C9FF:FE12:3456.",
  "Modified EUI-64", "Insert FFFE, flip bit 7.", verify=lambda: eui64("2001:DB8:5:1::/64", "00A0.C912.3456"))

S(D, "1.8", "ipv6", 2, "config", "A router interface is configured with only 'ipv6 enable'. Which IPv6 address(es) will it have?",
  ["*Only a link-local address || ipv6 enable activates IPv6 and auto-creates the FE80::/10 link-local address.",
   "A global address learned through SLAAC || That requires 'ipv6 address autoconfig'.",
   "A unique local address || ULAs must be configured explicitly.",
   "No IPv6 address at all || A link-local address is created automatically."],
  "'ipv6 enable' is useful on links that only need link-local addressing, such as router-to-router links where routing protocols use link-local next hops.",
  "IPv6 interface configuration")

S(D, "1.9", "ipv6", 2, "concept", "Which statements about IPv6 are true?",
  ["*IPv6 does not use broadcast addresses || Functions that used broadcast in IPv4 use multicast, such as FF02::1.",
   "*Neighbor Solicitation messages replace the function of ARP requests || NDP NS/NA (ICMPv6 135/136) resolve link-layer addresses.",
   "The IPv6 base header includes a header checksum || The IPv6 base header removed the checksum; upper layers provide error checking.",
   "Link-local addresses are routed across the internet || Link-local addresses are valid only on their own link."],
  "IPv6 removes broadcast, replaces ARP with Neighbor Discovery, and simplifies the base header (no header checksum).",
  "IPv6 behavior")

S(D, "1.10", "client-ip", 2, "troubleshooting", "A user can print to 10.1.20.10 but cannot reach any website. Based on the output, what is the problem?",
  ["*The default gateway is not in the host's subnet || The host is in 10.1.20.0/24 but its gateway is 10.1.2.1, so off-subnet traffic cannot be delivered.",
   "The subnet mask is too long for this network || A /24 correctly includes the printer and the host.",
   "The DNS server is unreachable from this PC || Even with DNS working, the host has no valid gateway to send packets to.",
   "DHCP is disabled on the network adapter || A static configuration is not a problem by itself."],
  "Local communication works because the printer is on the same subnet. Remote communication requires a gateway inside the host's own subnet (e.g., 10.1.20.1).",
  "Default gateway verification", "Gateway must be in your own subnet.",
  ex="C:\\> ipconfig\nEthernet adapter Ethernet0:\n   IPv4 Address. . . . . . . . . . . : 10.1.20.55\n   Subnet Mask . . . . . . . . . . . : 255.255.255.0\n   Default Gateway . . . . . . . . . : 10.1.2.1")

S(D, "1.10", "client-ip", 1, "command", "Which Linux command displays the host's default gateway?",
  ["*ip route || The routing table output includes a line such as 'default via 10.0.0.1 dev eth0'.",
   "ip addr || ip addr shows interface addresses, not routes.",
   "ipconfig /all || ipconfig is a Windows command.",
   "arp -a || arp -a shows cached IP-to-MAC mappings, not the configured gateway."],
  "On Linux, 'ip addr' shows addresses and 'ip route' shows the routing table, including the default route.", "Client OS verification")

S(D, "1.11.a", "wireless-principles", 1, "scenario", "Three APs cover adjacent areas in the 2.4 GHz band. Which channel assignment avoids overlapping channels?",
  ["*1, 6 and 11 || These are the three non-overlapping 20 MHz channels in the 2.4 GHz band.",
   "1, 2 and 3 || Adjacent channels overlap heavily and interfere with each other.",
   "6, 6 and 6 || Three APs on one channel would create co-channel contention.",
   "1, 4 and 8 || Channels only 3–4 apart still overlap in 2.4 GHz."],
  "2.4 GHz channels are 5 MHz apart but each is about 20–22 MHz wide, so only 1, 6 and 11 avoid overlap in most regulatory domains.",
  "Non-overlapping channels", "1-6-11.")

M(D, "1.11", "wireless-principles", 2, "concept", "Match each wireless term to its definition.",
  [("SSID", "The human-readable wireless network name"), ("BSSID", "The MAC address of an AP radio serving a BSS"),
   ("ESS", "Multiple APs sharing one SSID so clients can roam"), ("IBSS", "An ad hoc network of clients with no AP")],
  "An SSID names the network; each AP radio has a BSSID; an ESS links several BSSs with the same SSID; an IBSS is ad hoc.",
  "802.11 service sets")

S(D, "1.12", "virtualization", 2, "scenario", "Two customers connected to the same provider router both use 10.0.0.0/24 internally. Which technology keeps their routes isolated on that one router?",
  ["*VRF || Each VRF has its own routing table, so overlapping prefixes can coexist.",
   "VLANs || VLANs separate Layer 2 broadcast domains but do not create separate routing tables.",
   "PAT || PAT translates addresses; it does not isolate routing tables.",
   "Containers || Containers isolate applications on a host, not routes on a router."],
  "Virtual Routing and Forwarding creates multiple independent routing tables on one router; interfaces are assigned to a VRF.",
  "VRF", "VRF = a virtual router inside a router.")

TF(D, "1.12", "virtualization", 1, "concept", "Containers each include a full guest operating system kernel, making them heavier than virtual machines.", False,
   "Containers share the host OS kernel and package only the application and its dependencies; VMs run a complete guest OS on a hypervisor.",
   "Containers vs VMs")

S(D, "1.13", "switching", 1, "diagram", "Refer to the diagram. PC-A sends a unicast frame to PC-B, and the switch has no entry for BBBB.BBBB.BBBB. What does the switch do?",
  ["*Floods the frame out all ports in the VLAN except Gi0/1 || Unknown unicast frames are flooded so the destination can still receive them.",
   "Drops the frame || Switches do not drop unknown unicasts; they flood them.",
   "Sends an ARP request for PC-B || Layer 2 switches do not ARP on behalf of hosts.",
   "Forwards the frame only to the default gateway || A switch does not use IP gateways to forward frames."],
  "A switch learns from the source MAC (PC-A on Gi0/1) and forwards by destination MAC. Unknown destinations are flooded within the VLAN; when PC-B replies, the switch learns its port.",
  "Unknown unicast flooding", dg="mac_quiz")

S(D, "1.13", "switching", 2, "scenario",
  "A switch has 0050.56AA.0001 learned on Gi0/3. The host is moved to Gi0/7 and immediately sends a frame. What happens to the MAC address table entry?",
  ["*The entry is updated to Gi0/7 as soon as the frame arrives || Switches learn from every source MAC and move the entry to the new port.",
   "The old entry remains until the 300-second aging timer expires || Aging removes idle entries; a new source on another port updates the entry immediately.",
   "Two entries exist, one for each port || A MAC address maps to one port per VLAN.",
   "Gi0/7 becomes err-disabled || That only happens with port security or similar features configured."],
  "MAC learning is continuous: a frame from a known MAC on a different port updates the table at once.", "MAC address learning")

S(D, "1.13", "switching", 2, "concept",
  "A 24-port switch has all ports in VLAN 1, each connected to a host in full duplex. How many broadcast domains and collision domains exist?",
  ["*1 broadcast domain and 24 collision domains || One VLAN is one broadcast domain; each switch port is its own collision domain.",
   "24 broadcast domains and 1 collision domain || This reverses how switches segment the network.",
   "1 broadcast domain and 1 collision domain || That describes a hub.",
   "24 broadcast domains and 24 collision domains || Broadcast domains are separated by VLANs or routers, not ports."],
  "Switches separate collision domains per port (and full duplex removes collisions altogether); VLANs and routers separate broadcast domains.",
  "Broadcast vs collision domains")

S(D, "1.0", "osi", 2, "troubleshooting", "A user can ping a web server by its IP address but cannot browse to it by name. At which layer is the problem most likely?",
  ["*Application layer || Successful pings prove Layers 1–3 work; failing name resolution is a DNS (application) issue.",
   "Network layer || IP reachability is proven by the successful ping.",
   "Data link layer || Frames are clearly being delivered.",
   "Physical layer || A physical fault would also break the ping."],
  "Troubleshoot bottom-up: if IP connectivity works, check upper layers such as DNS settings, the DNS server, or the application itself.",
  "Layered troubleshooting")

O(D, "1.0", "osi", 1, "concept", "Arrange the PDUs in the order they are created as data moves DOWN the stack on the sending host.",
  ["Data", "Segment", "Packet", "Frame", "Bits"],
  "Application data is placed in a transport segment, which is placed in an IP packet, which is framed for the link and transmitted as bits.",
  "Encapsulation", "Some People Fear Birds (Segment, Packet, Frame, Bits).")

S(D, "1.1.e", "components", 2, "concept", "What is the main function of a Wireless LAN Controller in a controller-based WLAN?",
  ["*It centrally manages lightweight APs || Lightweight APs depend on the WLC for configuration and management via CAPWAP.",
   "It routes traffic between VLANs on the campus core || That is the job of routers or Layer 3 switches.",
   "It provides PoE to access points || PoE comes from the switch (PSE) or an injector.",
   "It replaces the need for SSIDs || SSIDs are still defined — the WLC pushes them to APs."],
  "In split-MAC architecture, APs handle real-time 802.11 functions and the WLC handles management functions such as authentication, RF management and roaming.",
  "WLC role")

S(D, "1.11.d", "wireless-principles", 2, "concept", "Which statement correctly compares the 2.4 GHz and 5 GHz Wi-Fi bands?",
  ["*5 GHz has more channels; 2.4 GHz reaches farther || Higher frequencies attenuate faster but the 5 GHz band contains many more 20 MHz channels.",
   "2.4 GHz offers more non-overlapping channels than 5 GHz || 2.4 GHz has only three non-overlapping channels.",
   "5 GHz signals penetrate walls better than 2.4 GHz || Higher frequencies are absorbed more by walls.",
   "Both bands have exactly three non-overlapping channels || 5 GHz has many more."],
  "Band choice trades range (2.4 GHz) against capacity and channel availability (5 GHz and 6 GHz).",
  "Wi-Fi bands")
