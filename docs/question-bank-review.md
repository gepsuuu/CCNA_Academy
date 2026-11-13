# CCNA Academy question bank — answer key and QA report

236 original questions. Generated from `content-source/qbank`. Every item passed automated checks: valid indices, one rationale per wrong option, correct 'Choose N' counts, no near-duplicates (Jaccard > 0.6), no conspicuously longer correct option, and 22 calculation/routing answers recomputed with Python's ipaddress/json libraries.

## D1 Network Fundamentals — 50 questions (blueprint 20%)

### core-d1-001 · single · concept · difficulty 1 · obj 1.1 · Network components
Which device forwards packets between different IP networks by consulting its routing table?
- ✅ Router
- ❌ Layer 2 switch — A Layer 2 switch forwards frames inside a VLAN using MAC addresses; it cannot route between subnets.
- ❌ Access point — An access point bridges 802.11 clients onto the wired LAN; it does not route between IP networks.
- ❌ Hub — A hub repeats signals out every port and makes no addressing decisions.

**Explanation:** Moving traffic between different IP subnets is a Layer 3 function performed by routers (or Layer 3 switches). Hosts send off-subnet traffic to their default gateway, which looks up the destination in its routing table.

### core-d1-002 · single · scenario · difficulty 2 · obj 1.1.c · Network components
A company must block a specific file-sharing application that tunnels over TCP 443, while still allowing normal HTTPS browsing. Which capability is required?
- ✅ A next-generation firewall (NGFW)
- ❌ An extended ACL denying TCP 443 — This would block all HTTPS traffic, including legitimate browsing.
- ❌ A standard ACL on the internet interface — Standard ACLs match only the source IP address and cannot identify applications.
- ❌ Port security on access switches — Port security limits MAC addresses per switch port; it does not inspect applications.

**Explanation:** Traditional ACLs and stateful firewalls work with addresses and ports. When several applications share the same port, only application-aware inspection (AVC in an NGFW) can tell them apart.

### core-d1-003 · multi · concept · difficulty 2 · obj 1.1.h · Network components
Which statements about Power over Ethernet are correct? (Choose two.)
- ✅ A switch acting as PSE can power an IP phone over the same cable that carries its data
- ✅ IEEE 802.3at (PoE+) provides up to 30 W per port at the PSE
- ❌ IEEE 802.3af provides up to 60 W per port — 802.3af provides 15.4 W at the PSE; 60 W is an 802.3bt (Type 3) level.
- ❌ PoE always requires a separate power injector between the switch and the device — PoE-capable switches power devices directly; injectors are only used with non-PoE switches.

**Explanation:** PoE lets a PSE (usually the access switch) power PDs such as APs, phones and cameras. Standards: 802.3af 15.4 W, 802.3at 30 W, 802.3bt up to 60 W (Type 3) or 90 W (Type 4) at the PSE.

### core-d1-004 · single · scenario · difficulty 2 · obj 1.2 · Topology architectures
In a data center, every top-of-rack switch has one uplink to every backbone switch, top-of-rack switches never connect to each other, and any server-to-server path crosses the same number of hops. Which architecture is this?
- ✅ Spine-leaf
- ❌ Three-tier campus — Three-tier uses access, distribution and core layers; access switches do not connect to every core switch.
- ❌ Collapsed core — Collapsed core merges campus core and distribution; it does not describe leaf-to-every-spine connectivity.
- ❌ Hub-and-spoke WAN — Hub-and-spoke connects branch sites to a central site over a WAN, not racks in a data center.

**Explanation:** Spine-leaf is built for east-west traffic: every leaf links to every spine, so any two servers on different leaves are always leaf → spine → leaf apart.

### core-d1-005 · tf · concept · difficulty 1 · obj 1.2 · Topology architectures
True or false: In a two-tier (collapsed core) campus design, the core and distribution layers are combined into one layer.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** A collapsed core merges the distribution and core functions, which suits smaller campuses that do not need a separate core.

### core-d1-006 · match · concept · difficulty 1 · obj 1.2 · Topology architectures
Match each element to its primary role.
- Access layer → Connects endpoints and provides PoE and port security
- Distribution layer → Aggregates access switches and enforces routing/policy boundaries
- Core layer → Provides high-speed transport between distribution blocks
- SOHO router → Combines routing, switching, firewall and Wi-Fi in one device

**Explanation:** Hierarchical designs give each layer one job: access for endpoints, distribution for aggregation and policy, core for fast transport. Small offices typically use one all-in-one device.

### core-d1-007 · single · scenario · difficulty 2 · obj 1.2.f · Virtualization and cloud
A company rents virtual machines, storage and virtual networks from a provider but installs, patches and manages its own operating systems and applications. Which cloud service model is this?
- ✅ IaaS
- ❌ PaaS — With PaaS the provider also manages the OS and runtime; the customer only deploys code.
- ❌ SaaS — With SaaS the provider delivers a complete application; the customer manages no OS at all.
- ❌ Private cloud — Private cloud is a deployment model (who uses it), not a service model (what is provided).

**Explanation:** Service models describe how much the provider manages: IaaS (infrastructure), PaaS (plus OS/runtime), SaaS (the whole application). Deployment models are public, private, community and hybrid.

### core-d1-008 · single · troubleshooting · difficulty 3 · obj 1.4 · Cabling and interface issues
Users on Gi0/5 report very slow file transfers. Based on the output, what is the most likely cause?
```
SW1# show interfaces gigabitethernet0/5
GigabitEthernet0/5 is up, line protocol is up (connected)
  Half-duplex, 100Mb/s, media type is 10/100/1000BaseTX
  ...
     0 runts, 0 giants, 0 throttles
     12 input errors, 12 CRC, 0 frame, 0 overrun, 0 ignored
     0 output errors, 1523 collisions, 0 interface resets
     0 babbles, 418 late collision, 0 deferred
```
- ✅ A duplex mismatch with the connected device
- ❌ The port is assigned to the wrong VLAN — A wrong VLAN causes loss of connectivity, not collisions and CRC errors.
- ❌ Spanning tree is blocking the port — A blocking port does not forward user traffic at all and would not count collisions.
- ❌ Frames are larger than the MTU — The output shows 0 giants, so oversized frames are not the problem.

**Explanation:** When one side runs half duplex and the other full duplex, the full-duplex side transmits while the half-duplex side is sending. The half-duplex side records collisions—including late collisions after the first 64 bytes—and the other side sees CRC errors and runts. Fix by setting both ends to auto (or the same fixed values).

### core-d1-009 · single · scenario · difficulty 1 · obj 1.3.a · Cabling and interface issues
Two buildings are 1.5 km apart and need a 10 Gbps link. Which medium is most appropriate?
- ✅ Single-mode fiber
- ❌ Multimode fiber — MMF is intended for shorter runs, typically up to a few hundred meters at 10 Gbps.
- ❌ Cat 6A UTP — Twisted-pair Ethernet is limited to 100 m per segment.
- ❌ Cat 5e UTP with a crossover cable — Cable pinout does not change the 100 m copper limit.

**Explanation:** Copper Ethernet segments are limited to 100 m. Multimode fiber covers in-building and campus distances of hundreds of meters; single-mode fiber covers kilometers.

### core-d1-010 · single · scenario · difficulty 3 · obj 1.4 · Cabling and interface issues
A Cisco switch port left at auto speed/duplex connects to a PC whose NIC is hard-coded to 100 Mbps full duplex with auto-negotiation disabled. What will the switch port operate at?
- ✅ 100 Mbps, half duplex
- ❌ 100 Mbps, full duplex — Full duplex would require successful negotiation, which cannot happen when the PC has negotiation disabled.
- ❌ 1000 Mbps, full duplex — The PC is fixed at 100 Mbps; the switch detects that speed from the signal.
- ❌ The link stays down — The link comes up because the switch detects the speed; the problem is the duplex fallback.

**Explanation:** If auto-negotiation fails because the other side has it disabled, the auto side detects the speed but defaults to half duplex for 10/100 Mbps (full duplex for 1 Gbps). Hard-coding only one side therefore often causes a duplex mismatch.

### core-d1-011 · single · diagram · difficulty 1 · obj 1.13 · Ethernet frames and ARP
Refer to the frame diagram. Which field lets the receiving NIC detect that a frame was corrupted in transit?
*Diagram: frame_quiz*
- ✅ FCS
- ❌ Preamble — The preamble synchronizes the receiver's clock; it does not check integrity.
- ❌ Type — The Type (EtherType) field identifies the payload protocol, such as 0x0800 for IPv4.
- ❌ Source MAC — The source MAC identifies the sender and is used by switches for learning.

**Explanation:** Ethernet is the only common layer that adds a trailer: the FCS. The receiver recalculates the CRC over the frame and compares it with the FCS.

### core-d1-012 · single · scenario · difficulty 3 · obj 1.0 · Ethernet frames and ARP
Host A (192.168.1.10/24, gateway 192.168.1.1) pings 8.8.8.8 for the first time with an empty ARP cache. Which IPv4 address does Host A place in the target field of its ARP request?
- ✅ 192.168.1.1
- ❌ 8.8.8.8 — ARP only resolves addresses on the local link; a remote destination is reached through the gateway.
- ❌ 255.255.255.255 — The broadcast applies to the destination MAC (FFFF.FFFF.FFFF), not the ARP target IP.
- ❌ 192.168.1.10 — Asking for your own address is a gratuitous ARP, which is not used to reach a remote host.

**Explanation:** A host compares the destination with its own subnet. For remote destinations it ARPs for the default gateway and sends the frame to the gateway's MAC while the IP packet keeps 8.8.8.8 as the destination.

### core-d1-013 · multi · concept · difficulty 2 · obj 1.0 · Ethernet frames and ARP
Which statements about IPv4 ARP are true? (Choose two.)
- ✅ An ARP request is sent to the broadcast MAC address FFFF.FFFF.FFFF
- ✅ An ARP reply is normally sent as a unicast to the requester
- ❌ Routers forward ARP requests to other subnets — Routers do not forward Layer 2 broadcasts; ARP is confined to the local broadcast domain.
- ❌ IPv6 uses ARP with a larger address field — IPv6 replaces ARP with Neighbor Discovery (ICMPv6 NS/NA).

**Explanation:** ARP maps IPv4 addresses to MAC addresses on the local link: broadcast request, unicast reply, then both hosts cache the mapping.

### core-d1-014 · tf · concept · difficulty 2 · obj 1.0 · Ethernet frames and ARP
True or false: As a packet travels from a PC through two routers to a server (no NAT), the source and destination MAC addresses change at each hop while the source and destination IP addresses stay the same.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** Each router removes the incoming frame and builds a new one for the next link using its own MAC as the source and the next hop's MAC as the destination. The IP header is end to end.

### core-d1-015 · single · scenario · difficulty 2 · obj 1.5 · TCP vs UDP and ports
Why is the voice stream of a VoIP call carried over UDP rather than TCP?
- ✅ A retransmitted voice packet would arrive too late to be useful
- ❌ UDP encrypts the voice payload so calls cannot be intercepted — UDP provides no encryption; secure voice uses SRTP or similar.
- ❌ UDP guarantees in-order delivery of every voice sample — UDP provides no sequencing or delivery guarantees.
- ❌ TCP cannot carry packets that contain audio — TCP can carry any data; it is simply a poor fit for real-time media.

**Explanation:** UDP is connectionless with an 8-byte header and no retransmissions. For real-time audio and video, a late packet is as useless as a lost one, so applications use UDP and tolerate small losses.

### core-d1-016 · order · protocol-id · difficulty 1 · obj 1.5 · TCP vs UDP and ports
Arrange the messages that open a TCP connection and begin data transfer in the correct order.
1. Client sends SYN
2. Server replies with SYN-ACK
3. Client sends ACK
4. Application data is exchanged

**Explanation:** TCP's three-way handshake synchronizes sequence numbers in both directions (SYN, SYN-ACK, ACK) before any application data flows.

### core-d1-017 · match · protocol-id · difficulty 2 · obj 1.5 · TCP vs UDP and ports
Match each protocol to its transport and port.
- SSH → TCP 22
- DHCP server → UDP 67
- TACACS+ → TCP 49
- Syslog → UDP 514
- NTP → UDP 123

**Explanation:** SSH uses TCP 22; DHCP servers listen on UDP 67 (clients 68); TACACS+ uses TCP 49; Syslog uses UDP 514; NTP uses UDP 123.

### core-d1-018 · multi · protocol-id · difficulty 2 · obj 1.5 · TCP vs UDP and ports
Which protocols use TCP as their transport? (Choose two.)
- ✅ BGP
- ✅ SMTP
- ❌ TFTP — TFTP uses UDP port 69.
- ❌ SNMP — SNMP uses UDP 161 for polling and UDP 162 for traps.
- ❌ NTP — NTP uses UDP 123.

**Explanation:** Protocols that need reliable sessions (BGP, SMTP, SSH, HTTP/HTTPS, FTP) use TCP. Simple request/response or management protocols (TFTP, SNMP, NTP, Syslog, DHCP) use UDP.

### core-d1-019 · single · concept · difficulty 2 · obj 1.5 · TCP vs UDP and ports
Which TCP mechanism lets a receiver control how much data the sender may transmit before it must wait for an acknowledgment?
- ✅ Windowing
- ❌ Sequencing — Sequence numbers order the data and detect gaps but do not limit the sending rate.
- ❌ The three-way handshake — The handshake opens the connection; it does not control ongoing flow.
- ❌ The checksum — The checksum detects corruption only.

**Explanation:** TCP flow control uses a sliding window: the receiver advertises its window size, and the sender cannot have more unacknowledged data in flight than that window.

### core-d1-020 · single · calculation · difficulty 1 · obj 1.6 · Binary and hexadecimal
What is the decimal value of the binary octet 10101100?
- ✅ 172
- ❌ 168 — 168 is 10101000; the 4-bit is missing.
- ❌ 170 — 170 is 10101010.
- ❌ 180 — 180 is 10110100.

**Explanation:** Write the place values 128 64 32 16 8 4 2 1 above the bits and add those that are 1: 128 + 32 + 8 + 4 = 172.

### core-d1-021 · single · calculation · difficulty 2 · obj 1.8 · Binary and hexadecimal
What is the decimal value of the hexadecimal byte 0xC8?
- ✅ 200
- ❌ 192 — 192 is 0xC0.
- ❌ 208 — 208 is 0xD0.
- ❌ 184 — 184 is 0xB8.

**Explanation:** Each hex digit is four bits. The first digit is worth 16 times its value: C (12) × 16 = 192, plus 8 = 200 (binary 1100 1000).

### core-d1-022 · multi · concept · difficulty 1 · obj 1.7 · IPv4 and private addressing
Which addresses are RFC 1918 private addresses? (Choose two.)
- ✅ 172.20.5.1
- ✅ 10.200.1.1
- ❌ 172.32.1.1 — 172.32.x.x is outside 172.16.0.0/12 and is public.
- ❌ 192.169.1.1 — Only 192.168.0.0/16 is private.
- ❌ 169.254.1.1 — 169.254.0.0/16 is link-local (APIPA), not an RFC 1918 range.

**Explanation:** RFC 1918 defines 10.0.0.0/8, 172.16.0.0/12 and 192.168.0.0/16. APIPA (169.254/16) and loopback (127/8) are special ranges but not RFC 1918.

### core-d1-023 · single · troubleshooting · difficulty 1 · obj 1.10 · Client IP verification
A user's PC shows the address 169.254.88.12/16 and cannot reach any server. What is the most likely issue?
- ✅ The PC did not receive a reply from a DHCP server
- ❌ The DNS server address is wrong — A DNS problem would not change the PC's own IP address.
- ❌ The NAT pool on the edge router is exhausted — NAT happens on the router and does not alter the host's local address.
- ❌ The PC has a duplicate static address — A static address would be the configured one, not 169.254.x.x.

**Explanation:** 169.254.0.0/16 is used for automatic link-local addressing when no DHCP server answers. Check the cable/VLAN, the DHCP server, and any DHCP relay (ip helper-address).

### core-d1-024 · single · calculation · difficulty 1 · obj 1.6 · Subnetting and VLSM
How many usable host addresses are in 192.168.10.0/27?
- ✅ 30
- ❌ 32 — 32 is the total number of addresses, including network and broadcast.
- ❌ 14 — 14 is the number of usable hosts in a /28.
- ❌ 62 — 62 is the number of usable hosts in a /26.

**Explanation:** Usable hosts = 2^h − 2, where h is the number of host bits. The two reserved addresses are the network and broadcast addresses.

### core-d1-025 · single · calculation · difficulty 2 · obj 1.6 · Subnetting and VLSM
What is the broadcast address of the subnet containing 172.16.70.200/21?
- ✅ 172.16.71.255
- ❌ 172.16.79.255 — That would require a block of 16 starting at 64 (a /20).
- ❌ 172.16.70.255 — That is the broadcast of a /24, not a /21.
- ❌ 172.16.63.255 — That is the broadcast of the previous /21 (172.16.56.0/21).

**Explanation:** Mask /21 = 255.255.248.0. Block size = 256 − 248 = 8. Multiples of 8 near 70: 64 and 72. Network 172.16.64.0, broadcast 172.16.71.255.

### core-d1-026 · single · calculation · difficulty 2 · obj 1.6 · Subnetting and VLSM
What is the network address for host 10.45.130.77/19?
- ✅ 10.45.128.0
- ❌ 10.45.130.0 — That treats the prefix as /24.
- ❌ 10.45.96.0 — 96 is the previous /19 block (96–127).
- ❌ 10.45.0.0 — That treats the prefix as /16.

**Explanation:** Mask /19 = 255.255.224.0; block size 32. The largest multiple of 32 not exceeding 130 is 128, and the fourth octet becomes 0.

### core-d1-027 · single · calculation · difficulty 3 · obj 1.6 · Subnetting and VLSM
What is the LAST usable host address in the subnet that contains 192.168.33.150/26?
- ✅ 192.168.33.190
- ❌ 192.168.33.191 — .191 is the broadcast address and cannot be assigned.
- ❌ 192.168.33.189 — .189 is a valid host but not the last one.
- ❌ 192.168.33.254 — .254 belongs to the 192–255 subnet.

**Explanation:** Find the block (128–191), take the broadcast (.191) and subtract one for the last usable host.

### core-d1-028 · multi · calculation · difficulty 3 · obj 1.6 · Subnetting and VLSM
Which addresses are valid, assignable host addresses in 10.10.16.0/20? (Choose two.)
- ✅ 10.10.31.254
- ✅ 10.10.20.0
- ❌ 10.10.32.1 — 10.10.32.0 begins the next /20 block.
- ❌ 10.10.31.255 — This is the broadcast address of the subnet.
- ❌ 10.10.16.0 — This is the network address of the subnet.

**Explanation:** A /20 has a block size of 16 in the third octet. 10.10.16.0/20 runs from 10.10.16.0 (network) to 10.10.31.255 (broadcast). Addresses ending in .0 or .255 inside the range are valid hosts when they are not the boundaries.

### core-d1-029 · single · scenario · difficulty 3 · obj 1.6 · Subnetting and VLSM
Using VLSM on 172.16.4.0/22 (largest subnet first, starting at 172.16.4.0, no wasted gaps), you must allocate: LAN A 300 hosts, LAN B 100 hosts, LAN C 50 hosts and two point-to-point links. Which subnet is assigned to LAN B?
- ✅ 172.16.6.0/25
- ❌ 172.16.5.0/25 — This overlaps LAN A's /23, which already covers 172.16.5.0.
- ❌ 172.16.6.0/24 — A /24 provides 254 hosts — far more than needed, wasting space for LAN C and the links.
- ❌ 172.16.4.0/25 — This overlaps LAN A at the start of the block.

**Explanation:** 300 hosts → /23 (510 hosts): 172.16.4.0/23. 100 hosts → /25 (126): 172.16.6.0/25. 50 hosts → /26 (62): 172.16.6.128/26. Links → /30: 172.16.6.192/30 and 172.16.6.196/30.

### core-d1-030 · single · scenario · difficulty 2 · obj 1.6 · Subnetting and VLSM
You need at least 6 subnets from 192.168.100.0/24, each supporting at least 25 hosts. Which mask meets both requirements?
- ✅ 255.255.255.224
- ❌ 255.255.255.192 — /26 gives only 4 subnets.
- ❌ 255.255.255.240 — /28 gives only 14 hosts per subnet.
- ❌ 255.255.255.248 — /29 gives only 6 hosts per subnet.

**Explanation:** Borrow s bits so 2^s ≥ 6 (s = 3) and keep h host bits so 2^h − 2 ≥ 25 (h = 5). 24 + 3 = /27 and 32 − 27 = 5 — both satisfied.

### core-d1-031 · tf · concept · difficulty 2 · obj 1.6 · Subnetting and VLSM
True or false: A /31 prefix can be used on a point-to-point router link, giving two usable addresses with no network or broadcast address.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** RFC 3021 allows /31 on point-to-point links; both addresses are assigned to the two router interfaces, saving address space compared with /30.

### core-d1-032 · single · calculation · difficulty 1 · obj 1.8 · IPv6 addressing
What is the shortest valid form of 2001:0DB8:0000:0000:0000:0000:0A00:0001?
- ✅ 2001:DB8::A00:1
- ❌ 2001:DB8::A:1 — Trailing zeros in a hextet cannot be removed; 0A00 becomes A00, not A.
- ❌ 2001:DB8:::A00:1 — Three colons in a row are never valid.
- ❌ 2001:DB8:0::A00:1 — This is valid but not the shortest — the zero hextet should be absorbed into ::.

**Explanation:** Rule 1: remove leading zeros in each hextet. Rule 2: replace one run of all-zero hextets with ::. Only leading zeros may be dropped.

### core-d1-033 · tf · concept · difficulty 1 · obj 1.8 · IPv6 addressing
True or false: 2001:DB8::1::5 is a valid IPv6 address.
- ❌ True — The statement is false, so this choice is incorrect.
- ✅ False

**Explanation:** The :: shorthand can appear only once; with two, the number of missing zero hextets is ambiguous.

### core-d1-034 · match · concept · difficulty 2 · obj 1.9 · IPv6 addressing
Match each IPv6 prefix to its address type.
- FE80::/10 → Link-local
- FC00::/7 → Unique local
- FF00::/8 → Multicast
- 2000::/3 → Global unicast
- ::1/128 → Loopback

**Explanation:** Link-local FE80::/10 is created on every interface; unique local FC00::/7 is private-like; FF00::/8 is multicast; 2000::/3 is global unicast; ::1 is loopback.

### core-d1-035 · single · calculation · difficulty 3 · obj 1.9.d · IPv6 addressing
An interface with MAC address 00A0.C912.3456 is configured with 'ipv6 address 2001:DB8:5:1::/64 eui-64'. What global unicast address does it receive?
- ✅ 2001:DB8:5:1:2A0:C9FF:FE12:3456
- ❌ 2001:DB8:5:1:A0:C9FF:FE12:3456 — The universal/local bit was not flipped.
- ❌ 2001:DB8:5:1:2A0:C912:FFFE:3456 — FFFE must be inserted exactly in the middle of the 48-bit MAC.
- ❌ FE80::2A0:C9FF:FE12:3456 — This is the EUI-64 link-local address, not the global address requested.

**Explanation:** Modified EUI-64: 00A0C9 | 123456 → 00A0:C9FF:FE12:3456, then invert bit 7 of the first byte: 00 → 02. Appended to the /64 prefix gives 2001:DB8:5:1:2A0:C9FF:FE12:3456.

### core-d1-036 · single · config · difficulty 2 · obj 1.8 · IPv6 addressing
A router interface is configured with only 'ipv6 enable'. Which IPv6 address(es) will it have?
- ✅ Only a link-local address
- ❌ A global address learned through SLAAC — That requires 'ipv6 address autoconfig'.
- ❌ A unique local address — ULAs must be configured explicitly.
- ❌ No IPv6 address at all — A link-local address is created automatically.

**Explanation:** 'ipv6 enable' is useful on links that only need link-local addressing, such as router-to-router links where routing protocols use link-local next hops.

### core-d1-037 · multi · concept · difficulty 2 · obj 1.9 · IPv6 addressing
Which statements about IPv6 are true? (Choose two.)
- ✅ IPv6 does not use broadcast addresses
- ✅ Neighbor Solicitation messages replace the function of ARP requests
- ❌ The IPv6 base header includes a header checksum — The IPv6 base header removed the checksum; upper layers provide error checking.
- ❌ Link-local addresses are routed across the internet — Link-local addresses are valid only on their own link.

**Explanation:** IPv6 removes broadcast, replaces ARP with Neighbor Discovery, and simplifies the base header (no header checksum).

### core-d1-038 · single · troubleshooting · difficulty 2 · obj 1.10 · Client IP verification
A user can print to 10.1.20.10 but cannot reach any website. Based on the output, what is the problem?
```
C:\> ipconfig
Ethernet adapter Ethernet0:
   IPv4 Address. . . . . . . . . . . : 10.1.20.55
   Subnet Mask . . . . . . . . . . . : 255.255.255.0
   Default Gateway . . . . . . . . . : 10.1.2.1
```
- ✅ The default gateway is not in the host's subnet
- ❌ The subnet mask is too long for this network — A /24 correctly includes the printer and the host.
- ❌ The DNS server is unreachable from this PC — Even with DNS working, the host has no valid gateway to send packets to.
- ❌ DHCP is disabled on the network adapter — A static configuration is not a problem by itself.

**Explanation:** Local communication works because the printer is on the same subnet. Remote communication requires a gateway inside the host's own subnet (e.g., 10.1.20.1).

### core-d1-039 · single · command · difficulty 1 · obj 1.10 · Client IP verification
Which Linux command displays the host's default gateway?
- ✅ ip route
- ❌ ip addr — ip addr shows interface addresses, not routes.
- ❌ ipconfig /all — ipconfig is a Windows command.
- ❌ arp -a — arp -a shows cached IP-to-MAC mappings, not the configured gateway.

**Explanation:** On Linux, 'ip addr' shows addresses and 'ip route' shows the routing table, including the default route.

### core-d1-040 · single · scenario · difficulty 1 · obj 1.11.a · Wireless principles
Three APs cover adjacent areas in the 2.4 GHz band. Which channel assignment avoids overlapping channels?
- ✅ 1, 6 and 11
- ❌ 1, 2 and 3 — Adjacent channels overlap heavily and interfere with each other.
- ❌ 6, 6 and 6 — Three APs on one channel would create co-channel contention.
- ❌ 1, 4 and 8 — Channels only 3–4 apart still overlap in 2.4 GHz.

**Explanation:** 2.4 GHz channels are 5 MHz apart but each is about 20–22 MHz wide, so only 1, 6 and 11 avoid overlap in most regulatory domains.

### core-d1-041 · match · concept · difficulty 2 · obj 1.11 · Wireless principles
Match each wireless term to its definition.
- SSID → The human-readable wireless network name
- BSSID → The MAC address of an AP radio serving a BSS
- ESS → Multiple APs sharing one SSID so clients can roam
- IBSS → An ad hoc network of clients with no AP

**Explanation:** An SSID names the network; each AP radio has a BSSID; an ESS links several BSSs with the same SSID; an IBSS is ad hoc.

### core-d1-042 · single · scenario · difficulty 2 · obj 1.12 · Virtualization and cloud
Two customers connected to the same provider router both use 10.0.0.0/24 internally. Which technology keeps their routes isolated on that one router?
- ✅ VRF
- ❌ VLANs — VLANs separate Layer 2 broadcast domains but do not create separate routing tables.
- ❌ PAT — PAT translates addresses; it does not isolate routing tables.
- ❌ Containers — Containers isolate applications on a host, not routes on a router.

**Explanation:** Virtual Routing and Forwarding creates multiple independent routing tables on one router; interfaces are assigned to a VRF.

### core-d1-043 · tf · concept · difficulty 1 · obj 1.12 · Virtualization and cloud
True or false: Containers each include a full guest operating system kernel, making them heavier than virtual machines.
- ❌ True — The statement is false, so this choice is incorrect.
- ✅ False

**Explanation:** Containers share the host OS kernel and package only the application and its dependencies; VMs run a complete guest OS on a hypervisor.

### core-d1-044 · single · diagram · difficulty 1 · obj 1.13 · Switching concepts
Refer to the diagram. PC-A sends a unicast frame to PC-B, and the switch has no entry for BBBB.BBBB.BBBB. What does the switch do?
*Diagram: mac_quiz*
- ✅ Floods the frame out all ports in the VLAN except Gi0/1
- ❌ Drops the frame — Switches do not drop unknown unicasts; they flood them.
- ❌ Sends an ARP request for PC-B — Layer 2 switches do not ARP on behalf of hosts.
- ❌ Forwards the frame only to the default gateway — A switch does not use IP gateways to forward frames.

**Explanation:** A switch learns from the source MAC (PC-A on Gi0/1) and forwards by destination MAC. Unknown destinations are flooded within the VLAN; when PC-B replies, the switch learns its port.

### core-d1-045 · single · scenario · difficulty 2 · obj 1.13 · Switching concepts
A switch has 0050.56AA.0001 learned on Gi0/3. The host is moved to Gi0/7 and immediately sends a frame. What happens to the MAC address table entry?
- ✅ The entry is updated to Gi0/7 as soon as the frame arrives
- ❌ The old entry remains until the 300-second aging timer expires — Aging removes idle entries; a new source on another port updates the entry immediately.
- ❌ Two entries exist, one for each port — A MAC address maps to one port per VLAN.
- ❌ Gi0/7 becomes err-disabled — That only happens with port security or similar features configured.

**Explanation:** MAC learning is continuous: a frame from a known MAC on a different port updates the table at once.

### core-d1-046 · single · concept · difficulty 2 · obj 1.13 · Switching concepts
A 24-port switch has all ports in VLAN 1, each connected to a host in full duplex. How many broadcast domains and collision domains exist?
- ✅ 1 broadcast domain and 24 collision domains
- ❌ 24 broadcast domains and 1 collision domain — This reverses how switches segment the network.
- ❌ 1 broadcast domain and 1 collision domain — That describes a hub.
- ❌ 24 broadcast domains and 24 collision domains — Broadcast domains are separated by VLANs or routers, not ports.

**Explanation:** Switches separate collision domains per port (and full duplex removes collisions altogether); VLANs and routers separate broadcast domains.

### core-d1-047 · single · troubleshooting · difficulty 2 · obj 1.0 · OSI/TCP-IP models
A user can ping a web server by its IP address but cannot browse to it by name. At which layer is the problem most likely?
- ✅ Application layer
- ❌ Network layer — IP reachability is proven by the successful ping.
- ❌ Data link layer — Frames are clearly being delivered.
- ❌ Physical layer — A physical fault would also break the ping.

**Explanation:** Troubleshoot bottom-up: if IP connectivity works, check upper layers such as DNS settings, the DNS server, or the application itself.

### core-d1-048 · order · concept · difficulty 1 · obj 1.0 · OSI/TCP-IP models
Arrange the PDUs in the order they are created as data moves DOWN the stack on the sending host.
1. Data
2. Segment
3. Packet
4. Frame
5. Bits

**Explanation:** Application data is placed in a transport segment, which is placed in an IP packet, which is framed for the link and transmitted as bits.

### core-d1-049 · single · concept · difficulty 2 · obj 1.1.e · Network components
What is the main function of a Wireless LAN Controller in a controller-based WLAN?
- ✅ It centrally manages lightweight APs
- ❌ It routes traffic between VLANs on the campus core — That is the job of routers or Layer 3 switches.
- ❌ It provides PoE to access points — PoE comes from the switch (PSE) or an injector.
- ❌ It replaces the need for SSIDs — SSIDs are still defined — the WLC pushes them to APs.

**Explanation:** In split-MAC architecture, APs handle real-time 802.11 functions and the WLC handles management functions such as authentication, RF management and roaming.

### core-d1-050 · single · concept · difficulty 2 · obj 1.11.d · Wireless principles
Which statement correctly compares the 2.4 GHz and 5 GHz Wi-Fi bands?
- ✅ 5 GHz has more channels; 2.4 GHz reaches farther
- ❌ 2.4 GHz offers more non-overlapping channels than 5 GHz — 2.4 GHz has only three non-overlapping channels.
- ❌ 5 GHz signals penetrate walls better than 2.4 GHz — Higher frequencies are absorbed more by walls.
- ❌ Both bands have exactly three non-overlapping channels — 5 GHz has many more.

**Explanation:** Band choice trades range (2.4 GHz) against capacity and channel availability (5 GHz and 6 GHz).

## D2 Network Access — 45 questions (blueprint 20%)

### core-d2-001 · single · command · difficulty 1 · obj 2.1 · VLANs
After 'switchport mode access', which command places interface Gi0/4 in VLAN 30?
- ✅ switchport access vlan 30
- ❌ switchport vlan 30 — This is not a valid IOS interface command.
- ❌ vlan 30 access — 'vlan 30' is a global command that creates the VLAN; it does not assign ports.
- ❌ switchport trunk native vlan 30 — This sets the untagged VLAN on a trunk, not an access port's VLAN.

**Explanation:** Access ports carry one untagged data VLAN set with 'switchport access vlan <id>'. If the VLAN does not exist, IOS typically creates it.

### core-d2-002 · single · troubleshooting · difficulty 2 · obj 2.1 · VLANs
A PC on Gi0/11 is configured as 192.168.10.50/24 with gateway 192.168.10.1, the VLAN 10 gateway. It cannot reach anything. Based on the output, what is the cause?
```
SW1# show vlan brief
VLAN Name        Status    Ports
---- ----------- --------- -------------------------------
1    default     active    Gi0/12
10   STAFF       active    Gi0/1, Gi0/2, Gi0/3, Gi0/4, Gi0/5
                           Gi0/6, Gi0/7, Gi0/8, Gi0/9, Gi0/10
20   GUEST       active    Gi0/11
```
- ✅ Gi0/11 is in VLAN 20 instead of VLAN 10
- ❌ Gi0/11 is configured as a trunk port — Trunk ports do not appear in 'show vlan brief'; Gi0/11 is listed as an access port in VLAN 20.
- ❌ VLAN 10 does not exist — VLAN 10 is listed as active.
- ❌ The gateway must be in VLAN 1 — Each VLAN uses its own gateway in its own subnet.

**Explanation:** Always confirm the port's VLAN matches the host's subnet. Fix with 'switchport access vlan 10' on Gi0/11.

### core-d2-003 · multi · concept · difficulty 1 · obj 2.1 · VLANs
Which statements about VLAN 1 on a Cisco switch are true? (Choose two.)
- ✅ All switch ports belong to VLAN 1 by default
- ✅ VLAN 1 cannot be deleted
- ❌ VLAN 1 is in the extended VLAN range — The extended range is 1006–4094.
- ❌ Frames in VLAN 1 are always tagged on 802.1Q trunks — By default VLAN 1 is also the native VLAN, which is sent untagged.

**Explanation:** VLAN 1 is the default and default native VLAN. Best practice is to move unused ports and the native VLAN away from VLAN 1.

### core-d2-004 · single · scenario · difficulty 2 · obj 2.1.b · VLANs
An IP phone and a PC share Gi0/8, configured with 'switchport access vlan 10' and 'switchport voice vlan 20'. How is their traffic carried on the link to the switch?
- ✅ Voice tagged VLAN 20; PC untagged VLAN 10
- ❌ Both untagged in VLAN 10 — Voice would then share the data VLAN, defeating the voice VLAN's purpose.
- ❌ Both tagged with their own VLAN IDs — The PC sends normal untagged frames.
- ❌ Voice untagged; PC tagged VLAN 10 — This reverses the roles.

**Explanation:** The voice VLAN lets one access port carry two VLANs: tagged voice (learned by the phone via CDP or LLDP) and untagged data.

### core-d2-005 · tf · concept · difficulty 1 · obj 2.1 · VLANs
True or false: Hosts in VLAN 10 and VLAN 20 on the same switch can communicate directly without a router or Layer 3 switch.
- ❌ True — The statement is false, so this choice is incorrect.
- ✅ False

**Explanation:** Each VLAN is a separate broadcast domain and normally a separate subnet, so traffic between them must be routed.

### core-d2-006 · single · diagram · difficulty 2 · obj 2.2 · Trunking and 802.1Q
Refer to the diagram. PC1 sends a broadcast frame. Which devices receive it?
*Diagram: vlan_trunk*
- ✅ PC3 and the phone's PC
- ❌ PC3 only, across the trunk — The PC behind the phone is also in VLAN 10 on SW1.
- ❌ PC2, PC3 and PC4 — PC2 and PC4 are in VLAN 20, a different broadcast domain.
- ❌ PC2 and PC4 — These are in VLAN 20 and never receive VLAN 10 broadcasts.

**Explanation:** A broadcast is flooded to all ports in the same VLAN, including across trunks that allow that VLAN. Other VLANs never see it.

### core-d2-007 · single · config · difficulty 2 · obj 2.2 · Trunking and 802.1Q
SW1 and SW2 are connected by an 802.1Q trunk, and both ends are configured with 'switchport trunk native vlan 99'. How do frames belonging to VLAN 99 cross the trunk?
- ✅ Untagged (native VLAN)
- ❌ Tagged with VLAN ID 99 — Only non-native VLANs are tagged.
- ❌ They are dropped by the trunk — The native VLAN is carried untagged, not dropped.
- ❌ Tagged with VLAN ID 1 — The tag is never rewritten to VLAN 1.

**Explanation:** On an 802.1Q trunk, all VLANs are tagged except the native VLAN. Both ends must agree on the native VLAN.

### core-d2-008 · single · scenario · difficulty 2 · obj 2.2 · Trunking and 802.1Q
SW1 Gi0/1 and SW2 Gi0/1 are both configured with 'switchport mode dynamic auto'. What is the link's operational mode?
- ✅ Access
- ❌ Trunk — A trunk needs at least one side set to trunk or dynamic desirable.
- ❌ Err-disabled — DTP disagreement does not err-disable the port.
- ❌ Trunk carrying only the native VLAN — There is no such DTP outcome.

**Explanation:** DTP: desirable actively asks, auto only answers. auto + auto = access; desirable + auto/desirable/trunk = trunk.

### core-d2-009 · single · config · difficulty 3 · obj 2.2 · Trunking and 802.1Q
After the configuration shown, an engineer enters 'switchport trunk allowed vlan 30' on Gi0/1. Which VLANs are allowed on the trunk?
```
interface GigabitEthernet0/1
 switchport mode trunk
 switchport trunk allowed vlan 10,20
```
- ✅ Only VLAN 30
- ❌ VLANs 10, 20 and 30 — That result needs 'switchport trunk allowed vlan add 30'.
- ❌ VLANs 10 and 20 only — The new command overwrote the list.
- ❌ All VLANs (1–4094) — The allowed list is restricted, not reset to all.

**Explanation:** Use 'switchport trunk allowed vlan add <id>' to append, 'remove' to delete, and a bare list to replace.

### core-d2-010 · single · troubleshooting · difficulty 3 · obj 2.2 · Trunking and 802.1Q
CDP reports a native VLAN mismatch: SW1 uses native VLAN 99 and SW2 uses native VLAN 1. What is the main risk?
- ✅ Traffic can leak between VLANs
- ❌ The 802.1Q tag grows to 8 bytes — Tag size is always 4 bytes.
- ❌ All VLANs stop being tagged on the trunk — Non-native VLANs remain tagged.
- ❌ Both switches automatically change their native VLAN to 1 — Native VLANs are never adjusted automatically.

**Explanation:** Configure the same native VLAN on both ends (ideally an unused VLAN) to prevent VLAN leakage and spanning-tree inconsistencies.

### core-d2-011 · tf · concept · difficulty 1 · obj 2.2 · Trunking and 802.1Q
True or false: An 802.1Q tag adds 4 bytes to an Ethernet frame and contains a 12-bit VLAN ID.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** The tag carries TPID 0x8100 (16 bits), PCP (3 bits), DEI (1 bit) and the VLAN ID (12 bits), for 4 bytes total.

### core-d2-012 · single · troubleshooting · difficulty 2 · obj 2.1.c · Inter-VLAN routing
Hosts in VLAN 10 can reach their gateway, but hosts in VLAN 20 (192.168.20.0/24) cannot. Based on the configuration, what is wrong?
```
interface GigabitEthernet0/0.10
 encapsulation dot1Q 10
 ip address 192.168.10.1 255.255.255.0
interface GigabitEthernet0/0.20
 encapsulation dot1Q 30
 ip address 192.168.20.1 255.255.255.0
```
- ✅ Subinterface Gi0/0.20 uses 'encapsulation dot1Q 30' instead of 20
- ❌ Routers need 'ip routing' to route between subinterfaces — IPv4 routing is enabled by default on routers.
- ❌ The subinterface number must equal the VLAN ID — The number is only a label; the encapsulation VLAN is what matters.
- ❌ VLAN 20 must be the native VLAN — Only one VLAN can be native, and it is not required here.

**Explanation:** In router-on-a-stick, each subinterface's 'encapsulation dot1Q <vlan>' must match the VLAN its hosts use, and the switch port must be a trunk.

### core-d2-013 · multi · config · difficulty 3 · obj 2.1.c · Inter-VLAN routing
Which conditions are required for a Layer 3 switch to route between VLAN 10 and VLAN 20 using SVIs? (Choose two.)
- ✅ 'ip routing' is enabled globally
- ✅ Each SVI's VLAN exists and has at least one active port forwarding in it
- ❌ A router-on-a-stick trunk to an external router — SVIs route internally; no external router is needed.
- ❌ 'encapsulation dot1Q' on each SVI — That command is used on router subinterfaces, not SVIs.

**Explanation:** SVI routing: create VLANs, assign ports, configure 'interface vlan X' with an IP and 'no shutdown', and enable 'ip routing'.

### core-d2-014 · single · config · difficulty 1 · obj 2.1.c · Inter-VLAN routing
A router routes between VLANs using subinterfaces Gi0/0.10 (encapsulation dot1Q 10) and Gi0/0.20 (encapsulation dot1Q 20). How must the switch port connected to the router's Gi0/0 be configured?
- ✅ As an 802.1Q trunk
- ❌ As an access port in VLAN 10 — An access port would carry only VLAN 10.
- ❌ As a routed port — A routed port carries no VLANs to the router's subinterfaces.
- ❌ As a LACP EtherChannel member — Link aggregation is unrelated to carrying multiple VLANs.

**Explanation:** One physical router link carries multiple VLANs only when it is an 802.1Q trunk, with one subinterface per VLAN on the router.

### core-d2-015 · single · command · difficulty 1 · obj 2.3 · CDP and LLDP
Which command shows the IP address and software version of a directly connected Cisco neighbor?
- ✅ show cdp neighbors detail
- ❌ show cdp neighbors — The summary view lists device ID, local and remote ports, capability and platform, but not IP addresses.
- ❌ show cdp interface — This shows CDP timers and status per interface.
- ❌ show ip interface brief — This shows the local device's interfaces only.

**Explanation:** CDP neighbor details are essential for documenting topology when diagrams are missing.

### core-d2-016 · single · scenario · difficulty 2 · obj 2.3 · CDP and LLDP
A Cisco switch connects to a non-Cisco firewall. The engineer wants to see the firewall's system name and port from the switch. What should be done?
- ✅ Enable LLDP with 'lldp run'
- ❌ Enable CDP with 'cdp run' — CDP is Cisco proprietary and usually not supported by other vendors.
- ❌ Configure the port as a trunk — Trunking does not exchange device identity.
- ❌ Check 'show mac address-table' — This shows a MAC address only, not system name or port.

**Explanation:** Use LLDP in multi-vendor environments. Verify with 'show lldp neighbors [detail]'.

### core-d2-017 · match · protocol-id · difficulty 2 · obj 2.3 · CDP and LLDP
Match each item to the correct value.
- CDP default advertisement interval → 60 seconds
- CDP default holdtime → 180 seconds
- LLDP default advertisement interval → 30 seconds
- LLDP standard → IEEE 802.1AB

**Explanation:** CDP sends every 60 s with a 180 s holdtime. LLDP (802.1AB) sends every 30 s with a 120 s holdtime.

### core-d2-018 · single · scenario · difficulty 2 · obj 2.4 · EtherChannel
SW1 and SW2 connect over two links. Both switches use 'channel-group 1 mode passive' on these ports. What is the result?
- ✅ No EtherChannel forms
- ❌ An LACP EtherChannel forms — LACP needs at least one side in active mode.
- ❌ A PAgP EtherChannel forms — Passive is an LACP mode, not PAgP.
- ❌ A static EtherChannel forms — Static bundling uses mode on.

**Explanation:** Working combinations: active/active, active/passive (LACP), desirable/desirable, desirable/auto (PAgP), on/on (static).

### core-d2-019 · multi · concept · difficulty 2 · obj 2.4 · EtherChannel
Which mode combinations successfully form an EtherChannel? (Choose two.)
- ✅ active on one switch and passive on the other
- ✅ desirable on one switch and auto on the other
- ❌ passive on both switches — Neither side initiates LACP.
- ❌ auto on both switches — Neither side initiates PAgP.
- ❌ on on one switch and active on the other — Static 'on' does not negotiate, so it cannot pair with LACP.

**Explanation:** At least one side must actively negotiate, and both sides must use the same protocol (or both use 'on').

### core-d2-020 · single · troubleshooting · difficulty 3 · obj 2.4 · EtherChannel
Based on the output, what is the status of Gi0/2?
```
SW1# show etherchannel summary
Flags:  D - down        P - bundled in port-channel
        I - stand-alone s - suspended
        H - Hot-standby (LACP only)
        R - Layer3      S - Layer2
        U - in use

Group  Port-channel  Protocol    Ports
------+-------------+-----------+------------------
1      Po1(SU)         LACP      Gi0/1(P)    Gi0/2(s)
```
- ✅ It is suspended due to a mismatch
- ❌ It is bundled and forwarding traffic — Bundled ports show flag (P), as Gi0/1 does.
- ❌ It is a hot-standby LACP member — Hot-standby ports show flag (H).
- ❌ It is operating as a Layer 3 port — Layer 3 port-channels show flag R on the port-channel, and Po1 shows S (Layer 2).

**Explanation:** All EtherChannel members must match in speed, duplex, switchport mode and VLAN settings. Correct the mismatch and the port joins the bundle.

### core-d2-021 · tf · concept · difficulty 2 · obj 2.4 · EtherChannel
True or false: EtherChannel load balancing is per flow, so one large file transfer between two hosts normally uses only one member link.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** A hash of addresses (and optionally ports) selects a member link per flow to keep packets in order. Many flows spread across links; a single flow does not.

### core-d2-022 · single · diagram · difficulty 1 · obj 2.5 · Spanning Tree
Refer to the diagram. Which switch is the root bridge for VLAN 10, and why?
*Diagram: stp_quiz*
- ✅ SW1, because it has the lowest bridge priority
- ❌ SW3, because it has the highest MAC address — The lowest MAC is used only as a tiebreaker, and lowest wins.
- ❌ SW2, because it is connected to both other switches — Topology position does not determine the root.
- ❌ No root is elected because priorities differ — Different priorities simply decide the election.

**Explanation:** Bridge ID = priority (+ VLAN ID system-ID extension) + MAC. SW1's 24586 is lower than 32778, so SW1 is root regardless of MAC.

### core-d2-023 · single · diagram · difficulty 2 · obj 2.5 · Spanning Tree
Refer to the diagram. Which port is in the alternate (discarding) role?
*Diagram: stp_quiz*
- ✅ SW3's port toward SW2
- ❌ SW2's port toward SW3 — SW2's port is designated on that segment.
- ❌ SW1's port toward SW2 — All root bridge ports are designated.
- ❌ SW3's port toward SW1 — That is SW3's root port (lowest cost to the root).

**Explanation:** Root ports point toward the root; each segment gets one designated port; remaining ports discard. Ties are broken by the lowest bridge ID.

### core-d2-024 · single · calculation · difficulty 3 · obj 2.5 · Spanning Tree
A non-root switch can reach the root through path A (one 100 Mbps link) or path B (two 1 Gbps links). Using default short path costs, which path is used for its root port?
- ✅ Path B, with a total cost of 8
- ❌ Path A, with a total cost of 19 — 19 is higher than 8.
- ❌ Both paths, because STP load-balances equal-cost paths — STP picks a single root port.
- ❌ Path A, because it crosses fewer switches — STP compares cost, not hop count.

**Explanation:** Default short costs: 10 Mbps = 100, 100 Mbps = 19, 1 Gbps = 4, 10 Gbps = 2. Root path cost is the sum along the path.

### core-d2-025 · single · command · difficulty 2 · obj 2.5 · Spanning Tree
Which command makes SW2 the root bridge for VLAN 20 by automatically setting a low enough priority?
- ✅ spanning-tree vlan 20 root primary
- ❌ spanning-tree vlan 20 priority 30000 — Priority must be a multiple of 4096, so IOS rejects 30000.
- ❌ spanning-tree root vlan 20 — This is not valid IOS syntax.
- ❌ spanning-tree mode root — Mode selects the STP variant (e.g., rapid-pvst), not the root.

**Explanation:** Use 'root primary' / 'root secondary', or set 'spanning-tree vlan X priority N' with N as a multiple of 4096.

### core-d2-026 · match · concept · difficulty 3 · obj 2.5 · Spanning Tree
Match each STP protection feature to its behavior.
- PortFast → Moves an edge port immediately to forwarding
- BPDU Guard → Err-disables an edge port that receives a BPDU
- Root Guard → Blocks a port that receives superior BPDUs (root-inconsistent)
- Loop Guard → Stops a non-designated port from forwarding when BPDUs stop arriving

**Explanation:** PortFast speeds up host ports; BPDU Guard protects them from switches; Root Guard protects root placement; Loop Guard protects against unidirectional-link loops.

### core-d2-027 · tf · concept · difficulty 1 · obj 2.5 · Spanning Tree
True or false: In Rapid PVST+, the port states are discarding, learning and forwarding.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** RSTP merges the legacy disabled, blocking and listening states into one discarding state.

### core-d2-028 · single · troubleshooting · difficulty 2 · obj 2.5 · Spanning Tree
A user plugs a small managed switch into a wall port configured with PortFast and BPDU Guard. The port immediately becomes err-disabled. Why?
- ✅ The port received a BPDU from the new switch
- ❌ Port security detected a second MAC address — Port security is not mentioned and would report a security violation.
- ❌ A duplex mismatch occurred — Duplex problems do not err-disable ports.
- ❌ CDP detected an unknown device — CDP does not disable ports.

**Explanation:** BPDU Guard keeps unauthorized switches off edge ports. After removing the switch, recover with shutdown/no shutdown or errdisable recovery.

### core-d2-029 · order · concept · difficulty 2 · obj 2.5 · Spanning Tree
Arrange the spanning-tree decision process in order.
1. Elect the root bridge
2. Select one root port on each non-root switch
3. Select one designated port on each segment
4. Place all remaining ports in the alternate/blocking role

**Explanation:** STP elects the root first, then root ports (best path to root), then designated ports per segment; everything else discards.

### core-d2-030 · single · scenario · difficulty 2 · obj 2.7 · Wireless architectures and AP modes
Which AP mode lets a branch AP keep switching client traffic locally if the WAN link to the central WLC fails?
- ✅ FlexConnect
- ❌ Local — Local-mode APs tunnel all client traffic to the WLC and depend on it.
- ❌ Monitor — Monitor mode serves no clients.
- ❌ Sniffer — Sniffer mode captures traffic for an analyzer and serves no clients.

**Explanation:** FlexConnect is designed for branches across a WAN: control traffic goes to the WLC while client data can be switched locally.

### core-d2-031 · single · diagram · difficulty 2 · obj 2.6 · Wireless architectures and AP modes
Refer to the diagram. The APs operate in Local mode. Why are they connected to access ports rather than trunk ports?
*Diagram: wireless*
- ✅ Client traffic is tunneled to the WLC in CAPWAP
- ❌ Lightweight APs cannot negotiate trunks — APs in FlexConnect mode use trunks when switching multiple VLANs locally.
- ❌ CAPWAP works only in VLAN 1 — CAPWAP uses any routable management VLAN.
- ❌ Trunk ports cannot provide PoE — PoE works on trunk ports too.

**Explanation:** Local mode → access port (management VLAN). FlexConnect with local switching → trunk. WLC → trunk to reach many client VLANs.

### core-d2-032 · multi · protocol-id · difficulty 2 · obj 2.6 · Wireless architectures and AP modes
Which statements about CAPWAP are correct? (Choose two.)
- ✅ Control messages use UDP port 5246
- ✅ Client data uses UDP port 5247
- ❌ CAPWAP runs over TCP 443 — CAPWAP uses UDP.
- ❌ Encrypting the data channel is mandatory — Data-channel DTLS encryption is optional; control is always encrypted.

**Explanation:** CAPWAP builds tunnels between lightweight APs and the WLC: control (5246) and data (5247).

### core-d2-033 · single · config · difficulty 3 · obj 2.6 · Wireless architectures and AP modes
An AireOS WLC's distribution ports are bundled with LAG to a Catalyst switch. Which switch interface configuration is required?
- ✅ channel-group 1 mode on
- ❌ channel-group 1 mode active — LACP negotiation is not supported by AireOS LAG.
- ❌ channel-group 1 mode desirable — PAgP is not supported by AireOS LAG.
- ❌ channel-group 1 mode passive — Passive LACP waits for a peer that will never negotiate.

**Explanation:** AireOS WLC LAG is static, so the switch side must be 'mode on', with matching trunk settings.

### core-d2-034 · match · concept · difficulty 2 · obj 2.7 · Wireless architectures and AP modes
Match each AP mode to its purpose.
- Local → Serves clients and tunnels all client traffic to the WLC
- FlexConnect → Serves clients and can switch traffic locally at a branch
- Monitor → Serves no clients; scans RF for rogues and location
- Sniffer → Captures 802.11 frames and forwards them to a packet analyzer

**Explanation:** Choose AP mode by purpose: Local for campus, FlexConnect for branches, Monitor for RF security/location, Sniffer for troubleshooting captures.

### core-d2-035 · single · scenario · difficulty 1 · obj 2.9 · WLC GUI configuration
In the WLC GUI, an administrator creates a WLAN dedicated to IP phones. Which QoS profile should be selected?
- ✅ Platinum
- ❌ Gold — Gold is intended for video.
- ❌ Silver — Silver is best effort (the default).
- ❌ Bronze — Bronze is for background traffic.

**Explanation:** WLC QoS profiles: Platinum (voice), Gold (video), Silver (best effort), Bronze (background).

### core-d2-036 · single · troubleshooting · difficulty 2 · obj 2.9 · WLC GUI configuration
Clients connect to the 'Corp' SSID but receive addresses from the guest subnet. Which WLAN setting is most likely misconfigured?
- ✅ The WLAN's interface mapping
- ❌ The QoS profile — QoS affects prioritization, not addressing.
- ❌ SSID broadcast — Broadcasting the SSID does not affect which subnet clients use.
- ❌ Layer 2 security method — Security controls authentication and encryption, not the client VLAN.

**Explanation:** On a WLC, each WLAN is mapped to an interface (VLAN). A wrong mapping puts clients in the wrong subnet.

### core-d2-037 · single · troubleshooting · difficulty 3 · obj 2.8 · Device management access
SSH connections to SW1 fail, although its management SVI is reachable. Based on the configuration, which step is missing?
```
hostname SW1
ip domain-name lab.local
username admin secret Str0ngPass!
!
line vty 0 15
 login local
 transport input ssh
```
- ✅ Generating RSA keys with 'crypto key generate rsa'
- ❌ Configuring 'enable secret' — Privileged EXEC access is not required to open an SSH session.
- ❌ Configuring 'ip ssh version 1' — SSHv1 is obsolete and is not required.
- ❌ Configuring 'service password-encryption' — This only obscures clear-text passwords in the config.

**Explanation:** SSH prerequisites: hostname, ip domain-name, RSA keys (≥768 bits for SSHv2), local user (or AAA), 'login local' and 'transport input ssh' on the VTY lines.

### core-d2-038 · single · scenario · difficulty 2 · obj 2.8 · Device management access
An engineer must copy an IOS image to a router across a network where credentials must not be sent in clear text. Which protocol is most appropriate?
- ✅ SCP
- ❌ TFTP — TFTP has no authentication or encryption.
- ❌ FTP — FTP authenticates, but credentials and data are sent in clear text.
- ❌ Telnet — Telnet is a remote CLI protocol, not file transfer, and is unencrypted.

**Explanation:** For secure file transfer to network devices, use SCP or SFTP. TFTP and FTP are acceptable only on trusted management networks.

### core-d2-039 · tf · concept · difficulty 1 · obj 2.8 · Device management access
True or false: Console access to a router works even if the router has no IP address configured.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** The console is an out-of-band serial connection that does not depend on IP, which is why it is used for initial setup and recovery.

### core-d2-040 · single · concept · difficulty 1 · obj 2.1 · VLANs
Which VLAN ID belongs to the extended VLAN range?
- ✅ 2000
- ❌ 1000 — 1000 is in the normal range (1–1005).
- ❌ 1 — VLAN 1 is the default VLAN in the normal range.
- ❌ 1005 — 1002–1005 are reserved normal-range VLANs.

**Explanation:** Normal range: 1–1005 (1002–1005 reserved). Extended range: 1006–4094. VLAN IDs are 12 bits.

### core-d2-041 · single · command · difficulty 1 · obj 2.2 · Trunking and 802.1Q
Which command displays the native VLAN and the allowed VLANs of every trunk port?
- ✅ show interfaces trunk
- ❌ show vlan brief — It lists access ports per VLAN and omits trunks.
- ❌ show spanning-tree — It shows STP roles and states, not trunk allowed lists.
- ❌ show cdp neighbors — It shows neighbors, not trunk configuration.

**Explanation:** 'show interfaces trunk' is the first command to run when a VLAN does not cross a trunk.

### core-d2-042 · single · config · difficulty 2 · obj 2.8 · Device management access
Which VTY line configuration allows only SSH logins authenticated against local usernames?
- ✅ login local and transport input ssh
- ❌ password cisco, login, transport input all — This uses a shared line password and allows Telnet.
- ❌ login local and transport input telnet ssh — Telnet is still permitted.
- ❌ no login and transport input ssh — 'no login' disables authentication entirely.

**Explanation:** Combine local (or AAA) authentication with SSH-only transport, plus exec-timeout and an ACL with access-class for further hardening.

### core-d2-043 · single · scenario · difficulty 2 · obj 2.5 · Spanning Tree
In VLAN 1, SW1 has bridge priority 32769 and MAC 0011.1111.1111; SW2 has bridge priority 32769 and MAC 0011.1111.0001. Which switch is the root bridge?
- ✅ SW2, because the priorities tie and its MAC address is lower
- ❌ SW1, because its MAC address is higher — STP prefers the lowest bridge ID.
- ❌ Neither, because the priorities are identical — A tie in priority is always resolved by the MAC address.
- ❌ Both switches share the root bridge role — Each STP instance has exactly one root bridge.

**Explanation:** 32769 = 32768 default priority + VLAN 1 (system ID extension). Compare priority first, then MAC.

### core-d2-044 · multi · config · difficulty 3 · obj 2.4 · EtherChannel
Which items are required to build a Layer 3 EtherChannel between two multilayer switches? (Choose two.)
- ✅ 'no switchport' on the member interfaces and the port-channel interface
- ✅ The IP address configured on the port-channel interface
- ❌ An IP address on each member interface — Members carry no individual addresses.
- ❌ 'switchport mode trunk' on the port-channel — Trunking is a Layer 2 concept.

**Explanation:** Layer 3 EtherChannel members appear with flag R (Layer 3) in show etherchannel summary.

### core-d2-045 · single · troubleshooting · difficulty 3 · obj 2.2 · Trunking and 802.1Q
'show interfaces trunk' lists VLAN 30 under 'Vlans allowed on trunk' but not under 'Vlans allowed and active in management domain'. What is the most likely cause?
- ✅ VLAN 30 has not been created on this switch
- ❌ VLAN 30 is the native VLAN — The native VLAN would still be active.
- ❌ Spanning tree is blocking VLAN 30 — STP blocking is shown in the 'forwarding state and not pruned' list.
- ❌ The trunk uses ISL encapsulation — Encapsulation does not remove VLANs from the active list.

**Explanation:** Create the VLAN ('vlan 30') or make sure VTP is propagating it. The trunk output lists allowed, active and forwarding VLANs separately.

## D3 IP Connectivity — 58 questions (blueprint 25%)

### core-d3-001 · single · routing-table · difficulty 2 · obj 3.1 · Routing table and forwarding
Refer to the routing table. Which next hop does R1 use for a packet destined to 10.1.5.20?
```
R1# show ip route
Codes: C - connected, S - static, O - OSPF, L - local, * - candidate default

Gateway of last resort is 203.0.113.1 to network 0.0.0.0

S*    0.0.0.0/0 [1/0] via 203.0.113.1
      10.0.0.0/8 is variably subnetted, 9 subnets, 5 masks
C        10.0.12.0/30 is directly connected, GigabitEthernet0/1
L        10.0.12.1/32 is directly connected, GigabitEthernet0/1
C        10.0.13.0/30 is directly connected, GigabitEthernet0/2
L        10.0.13.1/32 is directly connected, GigabitEthernet0/2
C        10.0.14.0/30 is directly connected, GigabitEthernet0/3
L        10.0.14.1/32 is directly connected, GigabitEthernet0/3
O        10.1.0.0/16 [110/20] via 10.0.12.2, 00:10:11, GigabitEthernet0/1
O        10.1.4.0/22 [110/30] via 10.0.13.3, 00:10:11, GigabitEthernet0/2
S        10.1.5.0/24 [1/0] via 10.0.14.4
      203.0.113.0/24 is variably subnetted, 2 subnets, 2 masks
C        203.0.113.0/30 is directly connected, GigabitEthernet0/0
L        203.0.113.2/32 is directly connected, GigabitEthernet0/0
```
- ✅ 10.0.14.4
- ❌ 10.0.13.3 — 10.1.4.0/22 also matches, but /24 is more specific.
- ❌ 10.0.12.2 — 10.1.0.0/16 matches but is less specific.
- ❌ 203.0.113.1 — The default route is used only when nothing more specific matches.

**Explanation:** The router always picks the longest matching prefix first. Administrative distance only matters between routes to the same prefix.

### core-d3-002 · multi · routing-table · difficulty 3 · obj 3.2 · Routing table and forwarding
Refer to the routing table. Which destinations does R1 forward to next hop 10.0.13.3? (Choose two.)
```
R1# show ip route
Codes: C - connected, S - static, O - OSPF, L - local, * - candidate default

Gateway of last resort is 203.0.113.1 to network 0.0.0.0

S*    0.0.0.0/0 [1/0] via 203.0.113.1
      10.0.0.0/8 is variably subnetted, 9 subnets, 5 masks
C        10.0.12.0/30 is directly connected, GigabitEthernet0/1
L        10.0.12.1/32 is directly connected, GigabitEthernet0/1
C        10.0.13.0/30 is directly connected, GigabitEthernet0/2
L        10.0.13.1/32 is directly connected, GigabitEthernet0/2
C        10.0.14.0/30 is directly connected, GigabitEthernet0/3
L        10.0.14.1/32 is directly connected, GigabitEthernet0/3
O        10.1.0.0/16 [110/20] via 10.0.12.2, 00:10:11, GigabitEthernet0/1
O        10.1.4.0/22 [110/30] via 10.0.13.3, 00:10:11, GigabitEthernet0/2
S        10.1.5.0/24 [1/0] via 10.0.14.4
      203.0.113.0/24 is variably subnetted, 2 subnets, 2 masks
C        203.0.113.0/30 is directly connected, GigabitEthernet0/0
L        203.0.113.2/32 is directly connected, GigabitEthernet0/0
```
- ✅ 10.1.4.1
- ✅ 10.1.7.254
- ❌ 10.1.5.1 — The more specific static 10.1.5.0/24 wins, so it goes to 10.0.14.4.
- ❌ 10.1.8.1 — Outside the /22, so only 10.1.0.0/16 matches (via 10.0.12.2).
- ❌ 10.0.13.3 — This is a connected address on Gi0/2, delivered directly.

**Explanation:** A /22 has a block size of 4 in the third octet: 10.1.4.0/22 covers third-octet values 4–7. More specific routes inside it (10.1.5.0/24) take precedence.

### core-d3-003 · single · routing-table · difficulty 1 · obj 3.1 · Routing table and forwarding
Refer to the routing table. How does R1 forward a packet destined to 10.2.0.1?
```
R1# show ip route
Codes: C - connected, S - static, O - OSPF, L - local, * - candidate default

Gateway of last resort is 203.0.113.1 to network 0.0.0.0

S*    0.0.0.0/0 [1/0] via 203.0.113.1
      10.0.0.0/8 is variably subnetted, 9 subnets, 5 masks
C        10.0.12.0/30 is directly connected, GigabitEthernet0/1
L        10.0.12.1/32 is directly connected, GigabitEthernet0/1
C        10.0.13.0/30 is directly connected, GigabitEthernet0/2
L        10.0.13.1/32 is directly connected, GigabitEthernet0/2
C        10.0.14.0/30 is directly connected, GigabitEthernet0/3
L        10.0.14.1/32 is directly connected, GigabitEthernet0/3
O        10.1.0.0/16 [110/20] via 10.0.12.2, 00:10:11, GigabitEthernet0/1
O        10.1.4.0/22 [110/30] via 10.0.13.3, 00:10:11, GigabitEthernet0/2
S        10.1.5.0/24 [1/0] via 10.0.14.4
      203.0.113.0/24 is variably subnetted, 2 subnets, 2 masks
C        203.0.113.0/30 is directly connected, GigabitEthernet0/0
L        203.0.113.2/32 is directly connected, GigabitEthernet0/0
```
- ✅ Via the default route to 203.0.113.1
- ❌ Via 10.0.12.2, because 10.1.0.0/16 is the closest match — 10.2.0.1 is not inside 10.1.0.0/16.
- ❌ R1 drops the packet — A default route exists, so the packet is forwarded.
- ❌ Via Gi0/1 as a connected route — 10.2.0.1 is not on any connected subnet.

**Explanation:** A route matches only if the destination is inside its prefix. When nothing else matches, the default route (0.0.0.0/0) is used.

### core-d3-004 · single · routing-table · difficulty 2 · obj 3.1 · Routing table and forwarding
Refer to the routing table. What is the OSPF metric of the route used for 10.1.200.1?
```
R1# show ip route
Codes: C - connected, S - static, O - OSPF, L - local, * - candidate default

Gateway of last resort is 203.0.113.1 to network 0.0.0.0

S*    0.0.0.0/0 [1/0] via 203.0.113.1
      10.0.0.0/8 is variably subnetted, 9 subnets, 5 masks
C        10.0.12.0/30 is directly connected, GigabitEthernet0/1
L        10.0.12.1/32 is directly connected, GigabitEthernet0/1
C        10.0.13.0/30 is directly connected, GigabitEthernet0/2
L        10.0.13.1/32 is directly connected, GigabitEthernet0/2
C        10.0.14.0/30 is directly connected, GigabitEthernet0/3
L        10.0.14.1/32 is directly connected, GigabitEthernet0/3
O        10.1.0.0/16 [110/20] via 10.0.12.2, 00:10:11, GigabitEthernet0/1
O        10.1.4.0/22 [110/30] via 10.0.13.3, 00:10:11, GigabitEthernet0/2
S        10.1.5.0/24 [1/0] via 10.0.14.4
      203.0.113.0/24 is variably subnetted, 2 subnets, 2 masks
C        203.0.113.0/30 is directly connected, GigabitEthernet0/0
L        203.0.113.2/32 is directly connected, GigabitEthernet0/0
```
- ✅ 20
- ❌ 110 — 110 is the administrative distance, not the metric.
- ❌ 30 — 30 belongs to 10.1.4.0/22, which does not contain 10.1.200.1.
- ❌ 0 — [1/0] belongs to the static routes.

**Explanation:** Routing table brackets show [administrative distance/metric]. For OSPF the metric is the total cost to the destination.

### core-d3-005 · single · routing-table · difficulty 1 · obj 3.1.b · Routing table and forwarding
In the entry 'O 10.1.4.0/22 [110/30] via 10.0.13.3', what do 110 and 30 represent?
- ✅ Administrative distance 110 and OSPF cost 30
- ❌ Hop count 110 and AD 30 — OSPF does not use hop count, and the order is AD first.
- ❌ Route age 110 seconds and 30 hops — Route age appears separately as hh:mm:ss.
- ❌ Bandwidth 110 Mbps and delay 30 ms — These are not shown in the brackets.

**Explanation:** AD ranks route sources (OSPF = 110); the metric compares routes from the same source (OSPF cost).

### core-d3-006 · single · scenario · difficulty 2 · obj 3.2.c · Routing table and forwarding
A router learns 172.16.8.0/24 from OSPF and from EIGRP (internal), and also has a static route to 172.16.8.0/24 configured with administrative distance 200. Which route is installed?
- ✅ The EIGRP route
- ❌ The static route — Its AD was raised to 200, so it is now the least preferred.
- ❌ The OSPF route — OSPF's AD of 110 is higher than EIGRP's 90.
- ❌ All three, load-balanced — Load balancing happens only between equal routes from the same source.

**Explanation:** Administrative distance can be changed. A static route with a raised AD is a floating static route that is only installed if better sources disappear.

### core-d3-007 · single · scenario · difficulty 3 · obj 3.2.a · Routing table and forwarding
A router has an OSPF route to 192.168.0.0/16 and a RIP route to 192.168.10.0/24. A packet arrives for 192.168.10.5. Which route is used?
- ✅ The RIP route, because /24 is a longer prefix
- ❌ The OSPF route, because its AD (110) is lower than RIP's (120) — AD only compares routes to the identical prefix.
- ❌ Both routes are used for load sharing — The prefixes differ, so the more specific one wins.
- ❌ The default route — More specific routes exist.

**Explanation:** Routes to different prefixes are not compared by AD. The most specific matching prefix always wins.

### core-d3-008 · match · protocol-id · difficulty 2 · obj 3.2.c · Routing table and forwarding
Match each route source to its default administrative distance.
- Connected → 0
- Static → 1
- eBGP → 20
- EIGRP (internal) → 90
- OSPF → 110
- RIP → 120

**Explanation:** Lower administrative distance means more trusted: connected 0, static 1, eBGP 20, EIGRP 90, OSPF 110, IS-IS 115, RIP 120, iBGP 200.

### core-d3-009 · order · concept · difficulty 2 · obj 3.2 · Routing table and forwarding
Arrange the criteria a router uses to choose a route, from first evaluated to last.
1. Longest prefix match
2. Lowest administrative distance
3. Lowest metric

**Explanation:** Prefix length decides first. AD resolves ties between different sources for the same prefix; the metric resolves ties within one routing protocol.

### core-d3-010 · tf · concept · difficulty 2 · obj 3.1 · Routing table and forwarding
True or false: When a router forwards an IPv4 packet, it decrements the TTL and builds a new Layer 2 header for the outgoing link.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** Routing is a per-hop process: the frame is removed, the TTL is decremented (packets with TTL 0 are dropped), and a new frame is built for the next hop.

### core-d3-011 · single · diagram · difficulty 2 · obj 3.1 · Routing table and forwarding
Refer to the diagram. As the PC sends a packet to the server, which destination MAC address is in the frame leaving the PC?
*Diagram: routing*
- ✅ R1 Gi0/0's MAC
- ❌ The server's MAC — The PC cannot reach the server's MAC across routers.
- ❌ The MAC of R2's Gi0/1 — The PC only communicates directly with devices on its own subnet.
- ❌ FFFF.FFFF.FFFF — Unicast IP traffic uses the gateway's unicast MAC after ARP.

**Explanation:** Hosts send off-subnet traffic to the default gateway's MAC. Each router builds a new frame for the next link.

### core-d3-012 · single · diagram · difficulty 3 · obj 3.1 · Routing table and forwarding
Refer to the diagram. If R1 loses its OSPF route to 10.2.2.0/24, how does it forward traffic to 10.2.2.10?
*Diagram: routing*
- ✅ Using the static default route via 10.0.12.2
- ❌ It drops the traffic — A matching default route exists.
- ❌ It forwards out Gi0/0 toward the PC — No route points to Gi0/0 for 10.2.2.0/24.
- ❌ It floods the packet out all interfaces — Routers do not flood unknown destinations.

**Explanation:** The default route catches every destination without a more specific match. Here it also points to R2, so connectivity continues.

### core-d3-013 · single · routing-table · difficulty 1 · obj 3.1 · Routing table and forwarding
What does a routing-table entry with code 'L', such as 'L 10.0.12.1/32', represent?
- ✅ The router's own interface IP
- ❌ A loopback interface — Loopback subnets appear as C/L entries too, but L means local, not loopback.
- ❌ A route learned by LDP — LDP is a label protocol and does not install L routes.
- ❌ A manually configured host route — Manual routes appear with code S.

**Explanation:** IOS installs a connected (C) route for the subnet and a local (L) /32 route for the interface address itself.

### core-d3-014 · tf · concept · difficulty 2 · obj 3.1 · Routing table and forwarding
True or false: If OSPF learns two paths to the same prefix with equal cost, the router can install both and load-share traffic.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** Equal-cost multipath (ECMP) is supported by OSPF; IOS installs up to 4 equal-cost paths by default (configurable with maximum-paths).

### core-d3-015 · single · routing-table · difficulty 2 · obj 3.1 · Routing table and forwarding
Based on the output, how does the router forward traffic to 10.9.1.1?
```
O     10.9.0.0/16 [110/30] via 10.0.12.2, 00:02:41, GigabitEthernet0/1
                  [110/30] via 10.0.13.3, 00:02:41, GigabitEthernet0/2
```
- ✅ It load-shares across both next hops
- ❌ It uses only 10.0.12.2 because it is listed first — Both entries are active paths.
- ❌ It drops the traffic because the routes conflict — Equal-cost paths are not a conflict.
- ❌ It uses the default route — 10.9.0.0/16 matches more specifically.

**Explanation:** When multiple next hops appear under one prefix with the same [AD/metric], the router shares traffic between them.

### core-d3-016 · single · troubleshooting · difficulty 2 · obj 3.1 · Routing table and forwarding
A router has no default route and receives a packet for a destination that matches no route. What happens?
- ✅ The packet is dropped
- ❌ The packet is flooded out all interfaces — Flooding is a Layer 2 switch behavior.
- ❌ The router sends an ARP request for the destination — ARP is only for directly connected subnets.
- ❌ The packet is sent back out the interface it arrived on — That would create a loop.

**Explanation:** Without a matching route, the packet cannot be forwarded. A default route provides a catch-all.

### core-d3-017 · single · command · difficulty 1 · obj 3.3 · Static routing
Which command configures an IPv4 default route to next hop 198.51.100.1?
- ✅ ip route 0.0.0.0 0.0.0.0 198.51.100.1
- ❌ ip route 0.0.0.0 255.255.255.255 198.51.100.1 — A /32 mask would match only 0.0.0.0.
- ❌ ip default-route 198.51.100.1 — This is not valid IOS syntax.
- ❌ ip route default 198.51.100.1 — IOS requires the 0.0.0.0 0.0.0.0 form.

**Explanation:** The default route appears as S* and sets the gateway of last resort.

### core-d3-018 · single · config · difficulty 2 · obj 3.3.d · Static routing
R1 learns its default route from OSPF. An engineer wants a backup default route through ISP2 at 192.0.2.1 that is used only if the OSPF default disappears. Which command achieves this?
- ✅ ip route 0.0.0.0 0.0.0.0 192.0.2.1 115
- ❌ ip route 0.0.0.0 0.0.0.0 192.0.2.1 — The default static AD of 1 would replace the OSPF route immediately.
- ❌ ip route 0.0.0.0 0.0.0.0 192.0.2.1 90 — AD 90 is lower than 110, so this would become the primary route.
- ❌ ip route 0.0.0.0 0.0.0.0 192.0.2.1 100 — AD 100 is still lower than 110.

**Explanation:** A floating static route must use an AD higher than the route it backs up.

### core-d3-019 · single · command · difficulty 2 · obj 3.3 · Static routing
R1 reaches its IPv6 next hop through link-local address FE80::2 on Gi0/1. Which IPv6 default route is valid?
- ✅ ipv6 route ::/0 GigabitEthernet0/1 FE80::2
- ❌ ipv6 route ::/0 FE80::2 — Link-local addresses are only unique per link, so IOS needs the interface.
- ❌ ip route ::/0 GigabitEthernet0/1 FE80::2 — IPv6 routes use 'ipv6 route'.
- ❌ ipv6 route ::/128 GigabitEthernet0/1 FE80::2 — ::/128 is the unspecified address, not a default route.

**Explanation:** IPv6 default route = ::/0. Also ensure 'ipv6 unicast-routing' is enabled.

### core-d3-020 · single · troubleshooting · difficulty 3 · obj 3.3 · Static routing
R1 has 'ip route 10.20.0.0 255.255.0.0 10.0.0.2', and the route appears in its table. Pings from a PC on R1's LAN (192.168.1.0/24) to 10.20.5.5 fail. R2 has no routes other than its connected networks. What is the most likely cause?
- ✅ R2 has no route back to 192.168.1.0/24
- ❌ The static route's AD is too high — AD only matters when competing routes exist.
- ❌ The static route mask should be 255.255.255.0 — A /16 correctly covers 10.20.5.5.
- ❌ Static routes do not work for ping — ICMP is routed like any other IP packet.

**Explanation:** Routing must work in both directions. Add a return route on R2 (or a dynamic protocol) for 192.168.1.0/24.

### core-d3-021 · multi · concept · difficulty 2 · obj 3.3 · Static routing
Which statements about IPv4 static routes are true? (Choose two.)
- ✅ The default administrative distance is 1
- ✅ A route with a next-hop address is installed only if the next hop is reachable
- ❌ Static routes automatically adapt when the topology changes — They must be changed manually (unless a floating route takes over).
- ❌ Static routes are advertised to all neighbors by default — They are only advertised if redistributed into a routing protocol.

**Explanation:** Static routes are predictable and create no protocol overhead, but they do not react to failures except through floating backups.

### core-d3-022 · single · command · difficulty 1 · obj 3.3.c · Static routing
Which command creates a host route to 10.9.9.9 via next hop 10.0.0.2?
- ✅ ip route 10.9.9.9 255.255.255.255 10.0.0.2
- ❌ ip route 10.9.9.9 255.255.255.0 10.0.0.2 — This mask describes a /24 network.
- ❌ ip route 10.9.9.9 0.0.0.0 10.0.0.2 — A 0.0.0.0 mask turns the route into a default route.
- ❌ ip host 10.9.9.9 10.0.0.2 — 'ip host' creates a local name-to-address mapping.

**Explanation:** Host routes (/32) are useful for steering traffic for a single server or loopback.

### core-d3-023 · single · troubleshooting · difficulty 3 · obj 3.3 · Static routing
An engineer configures 'ip route 10.50.0.0 255.255.0.0 10.0.99.9', but it does not appear in 'show ip route'. What is the most likely reason?
- ✅ The next hop 10.0.99.9 is not reachable through any existing route
- ❌ Its administrative distance is too high — There is no competing route, and AD defaults to 1.
- ❌ Static routes are hidden from 'show ip route' — Static routes appear with code S.
- ❌ Static routing must be enabled with a global command — IPv4 static routing works by default.

**Explanation:** Check that the next hop is on a connected subnet (or reachable via another route).

### core-d3-024 · tf · concept · difficulty 3 · obj 3.3 · Static routing
True or false: A static route on an Ethernet interface configured with only an exit interface (no next-hop address) makes the router ARP for each destination, relying on the neighboring router's proxy ARP.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** With no next hop, the router treats destinations as directly connected and ARPs for them. Use a next-hop or fully specified route on multi-access links.

### core-d3-025 · single · troubleshooting · difficulty 2 · obj 3.4 · OSPFv2
R1 and R2 share a subnet in area 0 but never become OSPF neighbors. Based on the output, what is the cause?
```
R1# show ip ospf interface g0/0 | include Process|Network|Timer
  Process ID 1, Router ID 1.1.1.1, Network Type BROADCAST, Cost: 1
  Timer intervals configured, Hello 10, Dead 40, Wait 40, Retransmit 5

R2# show ip ospf interface g0/0 | include Process|Network|Timer
  Process ID 20, Router ID 2.2.2.2, Network Type BROADCAST, Cost: 1
  Timer intervals configured, Hello 5, Dead 20, Wait 20, Retransmit 5
```
- ✅ The Hello and Dead timers do not match
- ❌ The process IDs differ — Process IDs are locally significant and need not match.
- ❌ The router IDs differ — Router IDs must be unique, so differing is required.
- ❌ The network types are point-to-point — Both show BROADCAST.

**Explanation:** OSPF neighbor requirements: same area, subnet/mask, Hello/Dead timers, authentication and stub flag; unique router IDs; matching MTU to reach Full.

### core-d3-026 · multi · concept · difficulty 2 · obj 3.4 · OSPFv2
Which parameters must match for two OSPFv2 routers to become neighbors? (Choose two.)
- ✅ Area ID
- ✅ Hello and Dead intervals
- ❌ Process ID — Process IDs are local to each router.
- ❌ Router ID — Router IDs must be different.
- ❌ Interface priority — Priority only influences the DR/BDR election.

**Explanation:** Matching: area, subnet, timers, authentication, stub flag (and MTU for Full). Unique: router ID. Irrelevant: process ID.

### core-d3-027 · single · scenario · difficulty 2 · obj 3.4.e · OSPFv2
R1 has no 'router-id' command. Its interfaces are Loopback0 1.1.1.1, Loopback1 10.10.10.10, Gi0/0 192.168.100.1 (up) and Gi0/1 172.16.1.1 (up). What is R1's OSPF router ID?
- ✅ 10.10.10.10
- ❌ 192.168.100.1 — Physical interfaces are used only if no loopback exists.
- ❌ 1.1.1.1 — The highest, not the lowest, loopback address is chosen.
- ❌ 172.16.1.1 — It is neither a loopback nor the highest address.

**Explanation:** Router ID order: manual 'router-id' → highest loopback IPv4 → highest active physical IPv4.

### core-d3-028 · single · scenario · difficulty 3 · obj 3.4.d · OSPFv2
Three routers on one Ethernet segment start OSPF at the same time: R1 (priority 1, RID 3.3.3.3), R2 (priority 10, RID 1.1.1.1) and R3 (priority 0, RID 9.9.9.9). Which router becomes the DR?
- ✅ R2
- ❌ R3 — Priority 0 makes a router ineligible, even with the highest router ID.
- ❌ R1 — Router ID is only a tiebreaker when priorities are equal; R1 becomes the BDR.
- ❌ No DR is elected on Ethernet — Broadcast networks elect a DR and BDR.

**Explanation:** DR election: highest priority, then highest router ID. Priority 0 never becomes DR or BDR.

### core-d3-029 · single · scenario · difficulty 3 · obj 3.4.d · OSPFv2
R2 is the current DR on a LAN. An engineer raises R1's OSPF priority to 255. What happens?
- ✅ R2 remains the DR; elections are not preemptive
- ❌ R1 immediately becomes the DR on the LAN — The DR is not replaced just because a higher priority appears.
- ❌ R1 immediately becomes the BDR on the LAN — The BDR is not preempted either.
- ❌ All adjacencies reset automatically — Changing priority does not reset the neighbors.

**Explanation:** OSPF DR/BDR election is non-preemptive to avoid instability. Plan priorities before routers come up.

### core-d3-030 · single · routing-table · difficulty 3 · obj 3.4 · OSPFv2
Based on the output from the local router, which statement is true?
```
R5# show ip ospf neighbor
Neighbor ID   Pri   State           Dead Time   Address     Interface
2.2.2.2         1   FULL/DR         00:00:35    10.1.1.2    Gi0/0
3.3.3.3         1   2WAY/DROTHER    00:00:33    10.1.1.3    Gi0/0
4.4.4.4         1   FULL/BDR        00:00:38    10.1.1.4    Gi0/0
```
- ✅ The local router is a DROTHER; this is normal
- ❌ The adjacency with 3.3.3.3 has failed — 2WAY between two DROTHERs is the expected final state.
- ❌ The local router is the DR — The DR would be FULL with every neighbor.
- ❌ There is an MTU mismatch with 3.3.3.3 — MTU problems leave neighbors stuck in EXSTART/EXCHANGE.

**Explanation:** On broadcast segments, all routers become FULL with the DR and BDR. DROTHER-to-DROTHER neighbors stay in 2-Way.

### core-d3-031 · single · calculation · difficulty 2 · obj 3.4 · OSPFv2
With the default OSPF reference bandwidth, what costs do a 1 Gbps and a 10 Gbps interface receive?
- ✅ 1 and 1
- ❌ 10 and 1 — That requires a reference bandwidth of 10 Gbps.
- ❌ 4 and 2 — These are STP path costs.
- ❌ 1 and 0.1 — OSPF cost is an integer with a minimum of 1.

**Explanation:** Raise the reference bandwidth on every router (auto-cost reference-bandwidth) so fast links can be distinguished.

### core-d3-032 · single · calculation · difficulty 3 · obj 3.4 · OSPFv2
After 'auto-cost reference-bandwidth 10000' is configured, what is the OSPF cost of a 1 Gbps interface?
- ✅ 10
- ❌ 1 — 1 was the cost with the default 100 Mbps reference.
- ❌ 100 — That would require a 100 Gbps reference.
- ❌ 4 — 4 is the STP cost for 1 Gbps.

**Explanation:** The reference-bandwidth value is in Mbps. Configure the same value on all routers so costs stay consistent.

### core-d3-033 · single · calculation · difficulty 2 · obj 3.4 · OSPFv2
R1 can reach 10.5.5.0/24 through R2 (outgoing link costs 10 then 10) or through R3 (costs 5 then 20). Which path does OSPF install?
- ✅ Through R2, total cost 20
- ❌ Through R3, because its first link is cheaper — OSPF compares total path cost, not the first hop.
- ❌ Both, as equal-cost paths — 20 and 25 are not equal.
- ❌ Through R3, because it has fewer hops — Both paths have the same hop count; OSPF uses cost anyway.

**Explanation:** SPF sums the outgoing interface costs along each path and picks the lowest total.

### core-d3-034 · single · config · difficulty 3 · obj 3.4.a · OSPFv2
Which command enables OSPF in area 0 only on the interface addressed 172.16.10.5/30, and on no other interface in 172.16.10.0/24?
- ✅ network 172.16.10.4 0.0.0.3 area 0
- ❌ network 172.16.10.4 255.255.255.252 area 0 — The network command uses a wildcard mask, not a subnet mask.
- ❌ network 172.16.10.0 0.0.0.255 area 0 — This matches every interface in 172.16.10.0/24.
- ❌ network 172.16.10.5 0.0.0.3 area 1 — The area must be 0, as required.

**Explanation:** The OSPF 'network' statement uses a wildcard mask to select interfaces. Alternatively configure 'ip ospf 1 area 0' directly on the interface.

### core-d3-035 · single · scenario · difficulty 2 · obj 3.4 · OSPFv2
LAN interface Gi0/2 must be advertised into OSPF, but no OSPF adjacency should ever form on it. Which command achieves this?
- ✅ passive-interface GigabitEthernet0/2
- ❌ no network 10.0.2.0 0.0.0.255 area 0 — Removing the network statement covering Gi0/2 would stop advertising its subnet.
- ❌ ip ospf priority 0 (on Gi0/2) — This only prevents DR/BDR election; adjacencies still form.
- ❌ shutdown (on GigabitEthernet0/2) — The interface and its subnet would go down.

**Explanation:** Passive interfaces are best practice on user-facing LANs to prevent rogue OSPF neighbors.

### core-d3-036 · tf · concept · difficulty 2 · obj 3.4.b · OSPFv2
True or false: On an interface configured with 'ip ospf network point-to-point', no DR or BDR is elected.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** Point-to-point networks have only two routers, so a DR is unnecessary. Neighbors appear as FULL/ - in 'show ip ospf neighbor'.

### core-d3-037 · order · concept · difficulty 2 · obj 3.4 · OSPFv2
Arrange the OSPF neighbor states in the order a new adjacency moves through them.
1. Down
2. Init
3. 2-Way
4. ExStart
5. Exchange
6. Loading
7. Full

**Explanation:** Hellos move neighbors from Down to Init and 2-Way; ExStart chooses master/slave; Exchange swaps DBD summaries; Loading requests missing LSAs; Full means synchronized LSDBs.

### core-d3-038 · single · config · difficulty 2 · obj 3.4 · OSPFv2
R1 has a static default route to the ISP. Which OSPF command advertises this default route to the other OSPF routers?
- ✅ default-information originate
- ❌ redistribute static subnets — This redistributes specific static routes; a default route needs default-information originate.
- ❌ network 0.0.0.0 255.255.255.255 area 0 — This enables OSPF on all interfaces; it does not advertise a default route.
- ❌ ip default-network 0.0.0.0 — This is not how OSPF advertises a default route.

**Explanation:** Add 'always' to advertise the default even if the router has none in its table.

### core-d3-039 · single · concept · difficulty 1 · obj 3.4 · OSPFv2
Which statement describes how OSPF builds its routes?
- ✅ Each router runs SPF on a shared LSDB
- ❌ Routers send their full routing tables to neighbors every 30 seconds — That describes RIP.
- ❌ Routers choose paths with the fewest hops — OSPF uses cost, not hop count.
- ❌ The DR calculates the routes for every router on the segment — The DR reduces flooding; each router still runs SPF itself.

**Explanation:** Link-state: share topology (LSAs) → synchronize the LSDB → each router computes shortest paths independently.

### core-d3-040 · match · protocol-id · difficulty 2 · obj 3.4 · OSPFv2
Match each OSPF item to its value.
- Multicast address for all OSPF routers → 224.0.0.5
- Multicast address for DR/BDR → 224.0.0.6
- Administrative distance → 110
- IP protocol number → 89

**Explanation:** OSPF runs directly over IP (protocol 89), uses 224.0.0.5 and 224.0.0.6, and has AD 110.

### core-d3-041 · single · troubleshooting · difficulty 3 · obj 3.4 · OSPFv2
Two OSPF neighbors stay stuck in EXSTART/EXCHANGE and never reach FULL. What is the most likely cause?
- ✅ An interface MTU mismatch
- ❌ An area ID mismatch — Routers with mismatched areas never get past Init.
- ❌ A Hello timer mismatch — Mismatched timers prevent the neighbor from being seen at all.
- ❌ A passive interface on one side — A passive interface sends no Hellos, so no neighbor appears.

**Explanation:** Fix by matching the MTU on both interfaces (or, as a workaround, 'ip ospf mtu-ignore').

### core-d3-042 · single · command · difficulty 1 · obj 3.4 · OSPFv2
Which command shows, for each OSPF interface, its area, cost, state (DR/BDR/DROTHER/P2P) and neighbor count?
- ✅ show ip ospf interface brief
- ❌ show ip ospf neighbor — It lists neighbors, not per-interface cost.
- ❌ show ip route ospf — It shows only OSPF-learned routes.
- ❌ show ip protocols — It shows process-level settings such as router ID and passive interfaces.

**Explanation:** Use 'show ip ospf interface brief' for interface status, 'show ip ospf neighbor' for adjacencies and 'show ip protocols' for process settings.

### core-d3-043 · single · concept · difficulty 1 · obj 3.5 · First hop redundancy
What problem do first hop redundancy protocols solve?
- ✅ They keep the hosts' default gateway available
- ❌ They prevent Layer 2 loops — That is the job of STP.
- ❌ They balance traffic across WAN links to the internet — FHRPs protect the first hop, not WAN path selection.
- ❌ They provide redundant DHCP servers — DHCP redundancy uses separate mechanisms.

**Explanation:** HSRP, VRRP and GLBP let two or more routers present one virtual IP/MAC as the hosts' default gateway.

### core-d3-044 · single · diagram · difficulty 2 · obj 3.5 · First hop redundancy
Refer to the diagram. If R1 fails, what must the hosts change to keep reaching other networks?
*Diagram: fhrp*
- ✅ Nothing; R2 takes over the virtual IP
- ❌ Their default gateway to 10.1.1.3 — The virtual IP removes the need to reconfigure hosts.
- ❌ Renew their DHCP leases — DHCP is not involved in FHRP failover.
- ❌ Clear their ARP caches — The virtual MAC stays the same, so cached entries remain valid.

**Explanation:** The new active router sends gratuitous ARPs so switches update their MAC tables for the virtual MAC.

### core-d3-045 · single · diagram · difficulty 3 · obj 3.5 · First hop redundancy
Refer to the diagram. When R1 recovers after the failure, will it become the active router again?
*Diagram: fhrp*
- ✅ Yes, because of preempt and priority 110
- ❌ No, because HSRP never preempts — HSRP supports preemption, but it must be configured.
- ❌ No, because R2 has the higher priority — 100 is lower than 110.
- ❌ Only if R2 is reloaded — Preemption does not require reloading R2.

**Explanation:** HSRP preemption is off by default and must be enabled with 'standby <group> preempt'.

### core-d3-046 · match · protocol-id · difficulty 2 · obj 3.5 · First hop redundancy
Match each description to its first hop redundancy protocol.
- Cisco proprietary; active and standby routers → HSRP
- IETF standard; master and backup routers → VRRP
- Cisco proprietary; multiple routers forward at once using different virtual MACs → GLBP

**Explanation:** HSRP and GLBP are Cisco protocols; VRRP is the open standard. Only GLBP natively load-balances among gateways.

### core-d3-047 · tf · concept · difficulty 1 · obj 3.5 · First hop redundancy
True or false: HSRP preemption is enabled by default.
- ❌ True — The statement is false, so this choice is incorrect.
- ✅ False

**Explanation:** HSRP preemption must be configured; VRRP enables preemption by default.

### core-d3-048 · single · concept · difficulty 1 · obj 3.0 · Routing table and forwarding
Which routing protocol is a path-vector protocol used between autonomous systems on the internet?
- ✅ BGP
- ❌ OSPF — OSPF is a link-state interior gateway protocol.
- ❌ RIP — RIP is a distance-vector IGP using hop count.
- ❌ EIGRP — EIGRP is an advanced distance-vector IGP.

**Explanation:** IGPs (OSPF, EIGRP, RIP, IS-IS) route within an organization; BGP routes between autonomous systems.

### core-d3-049 · single · routing-table · difficulty 3 · obj 3.1 · Routing table and forwarding
Refer to the IPv6 routing table. Which next hop does R1 use for 2001:DB8:10:5::77?
```
R1# show ipv6 route
O   2001:DB8:10::/48 [110/20]
     via FE80::2, GigabitEthernet0/1
S   2001:DB8:10:5::/64 [1/0]
     via 2001:DB8:FF::3
C   2001:DB8:FF::/64 [0/0]
     via GigabitEthernet0/2, directly connected
L   2001:DB8:FF::1/128 [0/0]
     via GigabitEthernet0/2, receive
S   ::/0 [1/0]
     via 2001:DB8:FF::9
```
- ✅ 2001:DB8:FF::3
- ❌ FE80::2 — The OSPF /48 also matches, but /64 is more specific.
- ❌ 2001:DB8:FF::9 — The default route is used only when nothing else matches.
- ❌ The packet is delivered directly out Gi0/2 — 2001:DB8:10:5::/64 is not a connected prefix.

**Explanation:** Longest prefix match works the same way for IPv6. The fourth hextet (5) places the address inside 2001:DB8:10:5::/64.

### core-d3-050 · multi · routing-table · difficulty 3 · obj 3.1 · Routing table and forwarding
Refer to the IPv6 routing table. Which destinations are forwarded to FE80::2? (Choose two.)
```
R1# show ipv6 route
O   2001:DB8:10::/48 [110/20]
     via FE80::2, GigabitEthernet0/1
S   2001:DB8:10:5::/64 [1/0]
     via 2001:DB8:FF::3
C   2001:DB8:FF::/64 [0/0]
     via GigabitEthernet0/2, directly connected
L   2001:DB8:FF::1/128 [0/0]
     via GigabitEthernet0/2, receive
S   ::/0 [1/0]
     via 2001:DB8:FF::9
```
- ✅ 2001:DB8:10:6::1
- ✅ 2001:DB8:10:FFFF::1
- ❌ 2001:DB8:10:5::1 — The static 2001:DB8:10:5::/64 is more specific.
- ❌ 2001:DB8:11::1 — The third hextet 11 is outside the /48, so the default route is used.

**Explanation:** A /48 fixes the first three hextets (2001:DB8:10); any value in the fourth hextet matches unless a longer prefix exists.

### core-d3-051 · single · routing-table · difficulty 2 · obj 3.1.a · Routing table and forwarding
In 'show ip route', what does the code 'O IA' indicate?
- ✅ An OSPF inter-area route learned from another area
- ❌ An OSPF route inside the router's own area — Intra-area routes are shown with just 'O'.
- ❌ An external route redistributed into OSPF — External routes are shown as O E1 or O E2.
- ❌ An inactive OSPF route — Inactive routes are not shown in the routing table.

**Explanation:** CCNA configuration focuses on single-area OSPF, but you should still recognize O, O IA and O E2 in routing tables.

### core-d3-052 · single · config · difficulty 3 · obj 3.4 · OSPFv2
R1 uses 'network 10.0.12.0 0.0.0.3 area 0'. R2's connected interface 10.0.12.2/30 uses 'ip ospf 1 area 1'. Both processes are otherwise correct. Will they become neighbors?
- ✅ No, because the area IDs do not match
- ❌ Yes, because the network statement and interface command are interchangeable — They are interchangeable, but the areas still differ.
- ❌ Yes, because process IDs match — Process IDs never need to match; areas do.
- ❌ No, because one router uses a network statement and the other uses an interface command — Mixing methods works fine.

**Explanation:** The two configuration methods are equivalent; what matters is that both sides place the link in the same area.

### core-d3-053 · single · routing-table · difficulty 2 · obj 3.4 · OSPFv2
'show ip ospf neighbor' shows neighbor 2.2.2.2 in state 'FULL/ -' on Gi0/1. What does the dash mean?
- ✅ The link is point-to-point, so there is no DR/BDR
- ❌ The neighbor is failing — FULL means the adjacency is complete.
- ❌ The neighbor is a DROTHER — DROTHERs appear as DROTHER.
- ❌ The neighbor's priority is 0 on a broadcast network — It would show FULL/DROTHER or 2WAY/DROTHER.

**Explanation:** Serial links and Ethernet links set to 'ip ospf network point-to-point' show FULL/- neighbors.

### core-d3-054 · single · scenario · difficulty 3 · obj 3.3 · Static routing
R1 has 'ip route 10.70.0.0 255.255.0.0 10.0.12.2' and 'ip route 10.70.0.0 255.255.0.0 10.0.13.3'. Both next hops are reachable. What does the routing table contain?
- ✅ Both static routes, and traffic is load-shared across them
- ❌ Only the first route that was configured — IOS installs equal static routes together.
- ❌ Only the route with the lower next-hop address — Next-hop address is not a tiebreaker.
- ❌ Neither route, because they conflict — Parallel static routes are valid.

**Explanation:** Equal static routes provide simple load sharing. Give one a higher AD to make it a floating backup instead.

### core-d3-055 · single · config · difficulty 2 · obj 3.3 · Static routing
R1 has 'ipv6 route ::/0 2001:DB8:1::1' and 'ipv6 route ::/0 2001:DB8:2::1 200'. Both next hops are reachable. Which route is used?
- ✅ The route via 2001:DB8:1::1
- ❌ The route via 2001:DB8:2::1 — AD 200 is less preferred.
- ❌ Both, load-shared — Their ADs differ, so only one is installed.
- ❌ Neither, because IPv6 does not support floating routes — Floating static routes work for IPv6 too.

**Explanation:** The floating route is installed only if the primary route disappears, for example when its next hop becomes unreachable.

### core-d3-056 · single · scenario · difficulty 3 · obj 3.5 · First hop redundancy
HSRP group 10 has no preempt configured. R2 (priority 100) is active. R1 (priority 120) boots and joins the group. Which router is active?
- ✅ R2 remains active
- ❌ R1 becomes active immediately — That requires 'standby 10 preempt' on R1.
- ❌ Both routers become active — HSRP has exactly one active router per group.
- ❌ Neither; an election restarts and traffic stops — A router joining does not interrupt the active router.

**Explanation:** Configure preempt on the router that should always be active, usually with a delay so routing can converge first.

### core-d3-057 · single · routing-table · difficulty 3 · obj 3.2 · Routing table and forwarding
Refer to the routing table. An engineer adds 'ip route 10.1.4.0 255.255.252.0 10.0.14.4'. What happens to traffic for 10.1.6.1?
```
R1# show ip route
Codes: C - connected, S - static, O - OSPF, L - local, * - candidate default

Gateway of last resort is 203.0.113.1 to network 0.0.0.0

S*    0.0.0.0/0 [1/0] via 203.0.113.1
      10.0.0.0/8 is variably subnetted, 9 subnets, 5 masks
C        10.0.12.0/30 is directly connected, GigabitEthernet0/1
L        10.0.12.1/32 is directly connected, GigabitEthernet0/1
C        10.0.13.0/30 is directly connected, GigabitEthernet0/2
L        10.0.13.1/32 is directly connected, GigabitEthernet0/2
C        10.0.14.0/30 is directly connected, GigabitEthernet0/3
L        10.0.14.1/32 is directly connected, GigabitEthernet0/3
O        10.1.0.0/16 [110/20] via 10.0.12.2, 00:10:11, GigabitEthernet0/1
O        10.1.4.0/22 [110/30] via 10.0.13.3, 00:10:11, GigabitEthernet0/2
S        10.1.5.0/24 [1/0] via 10.0.14.4
      203.0.113.0/24 is variably subnetted, 2 subnets, 2 masks
C        203.0.113.0/30 is directly connected, GigabitEthernet0/0
L        203.0.113.2/32 is directly connected, GigabitEthernet0/0
```
- ✅ It goes to 10.0.14.4 (static AD 1 wins)
- ❌ It still goes to 10.0.13.3, because OSPF learned the route first — Learning order does not matter.
- ❌ It is load-shared between 10.0.13.3 and 10.0.14.4 — Routes with different ADs are not combined.
- ❌ It goes to the default route — A /22 route still matches.

**Explanation:** When two sources offer exactly the same prefix, only the lowest-AD route is installed. The OSPF route remains in the OSPF database as a backup.

### core-d3-058 · single · troubleshooting · difficulty 3 · obj 3.4.e · OSPFv2
An engineer enters 'router-id 5.5.5.5' under a running OSPF process, but 'show ip protocols' still shows the old router ID. Why?
- ✅ The OSPF process must be restarted
- ❌ Router IDs must match a loopback address — Any unique 32-bit value is allowed.
- ❌ Manual router IDs are ignored when a loopback exists — A manual router-id always has the highest preference.
- ❌ The router ID can only be changed by removing the network statements — Only a process restart is required.

**Explanation:** Changing the router ID resets adjacencies, so plan it during a maintenance window.

## D4 IP Services — 25 questions (blueprint 10%)

### core-d4-001 · order · protocol-id · difficulty 1 · obj 4.6 · DHCP and DNS
Arrange the DHCP messages exchanged when a client first obtains an address.
1. Discover
2. Offer
3. Request
4. Acknowledge

**Explanation:** The client broadcasts Discover, servers reply with Offer, the client broadcasts Request for one offer, and the chosen server confirms with Ack.

### core-d4-002 · single · troubleshooting · difficulty 2 · obj 4.6 · DHCP and DNS
Clients in VLAN 10 (gateway R1 Gi0/1, 10.1.10.1/24) receive no IP address. The DHCP server is 10.0.0.50, reachable from R1 through Gi0/2. What fixes the problem?
- ✅ Configure 'ip helper-address 10.0.0.50' on Gi0/1
- ❌ Configure 'ip helper-address 10.0.0.50' on Gi0/2 — Gi0/2 faces the server and never receives client broadcasts.
- ❌ Enable 'ip routing' on R1 — Routers already route IPv4 by default; the problem is broadcast forwarding.
- ❌ Configure PAT on R1 — NAT does not relay DHCP broadcasts.

**Explanation:** Routers do not forward broadcasts. 'ip helper-address' turns DHCP broadcasts into unicasts to the server and sets giaddr so the server picks the right pool.

### core-d4-003 · multi · config · difficulty 2 · obj 4.6 · DHCP and DNS
An IOS DHCP pool for 10.1.10.0/24 must never lease 10.1.10.1–10.1.10.20, and clients must receive gateway 10.1.10.1. Which commands achieve these two requirements? (Choose two.)
- ✅ ip dhcp excluded-address 10.1.10.1 10.1.10.20
- ✅ default-router 10.1.10.1 (inside the pool)
- ❌ ip helper-address 10.1.10.1 — This configures a relay, not the pool.
- ❌ dns-server 10.1.10.1 — This sets the DNS server option, not the gateway.
- ❌ network 10.1.10.0 0.0.0.255 — The pool's network command uses a subnet mask or prefix length, not a wildcard.

**Explanation:** Typical pool: excluded-address (global), then 'ip dhcp pool NAME' with network, default-router, dns-server and lease.

### core-d4-004 · tf · concept · difficulty 1 · obj 4.6 · DHCP and DNS
True or false: A DHCP Discover is broadcast because the client has no IP address yet and does not know the server's address.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** The Discover is sent from 0.0.0.0 to 255.255.255.255 (UDP 68 to 67). A relay is needed when the server is on another subnet.

### core-d4-005 · single · protocol-id · difficulty 1 · obj 4.3 · DHCP and DNS
Which DNS record type maps a hostname to an IPv6 address?
- ✅ AAAA
- ❌ A — A records hold IPv4 addresses.
- ❌ PTR — PTR records map addresses back to names.
- ❌ MX — MX records identify mail servers for a domain.

**Explanation:** Common records: A (IPv4), AAAA (IPv6), CNAME (alias), MX (mail), PTR (reverse), NS (name server).

### core-d4-006 · single · protocol-id · difficulty 2 · obj 4.3 · DHCP and DNS
Which statement about DNS transport is correct?
- ✅ Queries use UDP 53; zone transfers use TCP 53
- ❌ DNS uses only TCP 53 for every query — Most queries use UDP for speed.
- ❌ DNS uses UDP 67 for queries and replies — UDP 67 belongs to DHCP servers.
- ❌ DNS uses only TCP 443 for all lookups — DNS over HTTPS exists, but standard DNS uses port 53.

**Explanation:** DNS is a classic 'both' protocol: UDP for small query/response exchanges, TCP for zone transfers and large answers.

### core-d4-007 · match · concept · difficulty 2 · obj 4.1 · NAT and PAT
Match each NAT term to its definition.
- Inside local → The real IP address of an inside host, usually private
- Inside global → The public address that represents an inside host to the outside
- Outside global → The real IP address of an outside host
- Outside local → The address of an outside host as seen from the inside network

**Explanation:** Inside/outside describes where the host is; local/global describes which side of the router the address is seen from.

### core-d4-008 · single · diagram · difficulty 2 · obj 4.1 · NAT and PAT
Refer to the diagram. What is the inside global address and port for PC2's session?
*Diagram: nat_pat*
- ✅ 203.0.113.2:51001
- ❌ 192.168.1.11:51000 — This is PC2's inside local address.
- ❌ 198.51.100.80:443 — This is the outside global address of the web server.
- ❌ 203.0.113.2:51000 — This translation is already used by PC1.

**Explanation:** In the translation table, the inside global column shows how an inside host appears on the internet.

### core-d4-009 · single · diagram · difficulty 3 · obj 4.1 · NAT and PAT
Refer to the diagram. Why did the router change PC2's source port from 51000 to 51001?
*Diagram: nat_pat*
- ✅ To keep each PAT translation unique
- ❌ To randomize ports for security — PAT keeps the original port when it is free.
- ❌ Because port 51000 is reserved on the internet — 51000 is an ordinary dynamic port.
- ❌ Because the web server requires odd port numbers — Servers do not impose such requirements.

**Explanation:** PAT identifies sessions by public IP + port. When two inside hosts use the same source port, PAT assigns a different one.

### core-d4-010 · single · troubleshooting · difficulty 3 · obj 4.1 · NAT and PAT
No translations appear after this configuration. Gi0/0 is the LAN (192.168.1.1/24) and Gi0/1 faces the ISP. What is wrong?
```
interface GigabitEthernet0/0
 ip address 192.168.1.1 255.255.255.0
 ip nat outside
interface GigabitEthernet0/1
 ip address 203.0.113.2 255.255.255.252
 ip nat inside
!
access-list 1 permit 192.168.1.0 0.0.0.255
ip nat inside source list 1 interface GigabitEthernet0/1 overload
```
- ✅ Inside/outside roles are reversed
- ❌ The ACL wildcard mask is incorrect — 0.0.0.255 correctly matches 192.168.1.0/24.
- ❌ The 'overload' keyword is missing — 'overload' is present.
- ❌ PAT requires a NAT pool — PAT can overload the outside interface address.

**Explanation:** Mark interfaces correctly, then verify with 'show ip nat translations' and 'show ip nat statistics'.

### core-d4-011 · single · scenario · difficulty 1 · obj 4.1 · NAT and PAT
Web server 192.168.1.10 must always be reachable from the internet at 203.0.113.10. Which NAT type is appropriate?
- ✅ Static NAT
- ❌ Dynamic NAT with a pool — Pool mappings exist only while the inside host has an active session.
- ❌ PAT on the outside interface — PAT mainly supports outbound sessions; inbound access needs static mappings.
- ❌ No translation is needed — A private address is not routable on the internet.

**Explanation:** Configure: ip nat inside source static 192.168.1.10 203.0.113.10.

### core-d4-012 · tf · concept · difficulty 2 · obj 4.1 · NAT and PAT
True or false: Dynamic NAT without overload can run out of addresses when more inside hosts need translation than the pool contains.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** Each inside host needs its own pool address. When the pool is exhausted, new translations fail until entries time out — which is why PAT (overload) is common.

### core-d4-013 · single · scenario · difficulty 2 · obj 4.2 · NTP, SNMP and Syslog
A router synchronizes its clock to an NTP server that reports stratum 2. What stratum does the router report?
- ✅ 3
- ❌ 2 — The router cannot share its server's stratum.
- ❌ 1 — Stratum 1 servers are directly attached to a reference clock.
- ❌ 0 — Stratum 0 is the reference clock itself (GPS or atomic).

**Explanation:** Lower stratum means closer to the reference source. Each hop adds one.

### core-d4-014 · single · scenario · difficulty 1 · obj 4.2 · NTP, SNMP and Syslog
During an incident review, syslog timestamps from different switches do not line up. What should be implemented?
- ✅ NTP on all devices
- ❌ SNMPv3 — SNMP monitors devices; it does not synchronize clocks.
- ❌ Higher logging severity — Severity filters messages; it does not fix timestamps.
- ❌ CDP — CDP discovers neighbors and does not set time.

**Explanation:** Configure 'ntp server <ip>', set the time zone, and add 'service timestamps log datetime msec'.

### core-d4-015 · multi · protocol-id · difficulty 2 · obj 4.4 · NTP, SNMP and Syslog
Which statements about SNMP are correct? (Choose two.)
- ✅ Managers poll agents on UDP 161
- ✅ SNMPv3 can provide both authentication and encryption
- ❌ Traps must be acknowledged by the manager — Traps are unacknowledged; Informs are acknowledged.
- ❌ SNMPv2c encrypts the community string — v1 and v2c send community strings in clear text.

**Explanation:** SNMP: manager ↔ agent, data organized in MIBs and identified by OIDs. Use SNMPv3 for security.

### core-d4-016 · single · protocol-id · difficulty 2 · obj 4.5 · NTP, SNMP and Syslog
A router logs '%OSPF-5-ADJCHG: Process 1, Nbr 2.2.2.2 on Gi0/1 from LOADING to FULL'. What is the severity of this message?
- ✅ Level 5, Notification
- ❌ Level 5, Warning — Warning is level 4.
- ❌ Level 3, Error — The message shows 5, not 3.
- ❌ Level 6, Informational — Informational is level 6.

**Explanation:** Syslog format: %FACILITY-SEVERITY-MNEMONIC. Severities: 0 Emergency, 1 Alert, 2 Critical, 3 Error, 4 Warning, 5 Notification, 6 Informational, 7 Debugging.

### core-d4-017 · single · config · difficulty 3 · obj 4.5 · NTP, SNMP and Syslog
With 'logging trap warnings' configured, which messages are sent to the syslog server?
- ✅ Severity 0 through 4
- ❌ Only severity 4 — A logging level is a threshold, not a single level.
- ❌ Severity 4 through 7 — Higher numbers are less severe and are excluded.
- ❌ All messages — Notification, Informational and Debugging are filtered out.

**Explanation:** Lower number = more severe. 'logging trap 4' sends Emergency, Alert, Critical, Error and Warning.

### core-d4-018 · order · protocol-id · difficulty 2 · obj 4.5 · NTP, SNMP and Syslog
Arrange the syslog severity levels from most severe (0) to least severe (7).
1. Emergency
2. Alert
3. Critical
4. Error
5. Warning
6. Notification
7. Informational
8. Debugging

**Explanation:** Syslog severities run from 0 (Emergency) to 7 (Debugging).

### core-d4-019 · single · protocol-id · difficulty 1 · obj 4.7 · QoS
Which DSCP marking is recommended for voice bearer traffic?
- ✅ EF (DSCP 46)
- ❌ AF41 — AF41 is commonly used for interactive video.
- ❌ CS1 — CS1 is commonly used for low-priority scavenger traffic.
- ❌ DSCP 0 — 0 is default best effort.

**Explanation:** DSCP is 6 bits in the IP header. Voice is marked EF so every hop can give it priority queuing.

### core-d4-020 · single · scenario · difficulty 2 · obj 4.7 · QoS
An ISP drops traffic above the contracted 50 Mbps. What should the customer configure outbound to avoid those drops?
- ✅ Shaping to 50 Mbps
- ❌ Policing to 50 Mbps — Policing drops the excess itself — the same result as the ISP's drops.
- ❌ WRED — WRED manages queue depth; it does not hold traffic to a rate.
- ❌ Marking with EF — Marking alone does not limit the sending rate.

**Explanation:** Providers commonly police; customers shape to match. Shaping adds delay but avoids drops.

### core-d4-021 · multi · concept · difficulty 3 · obj 4.7 · QoS
Which statements describe Low Latency Queuing (LLQ)? (Choose two.)
- ✅ It provides a strict-priority queue for delay-sensitive traffic such as voice
- ✅ The priority queue is policed so it cannot starve the other queues
- ❌ It marks packets with DSCP values — Marking is a separate QoS tool.
- ❌ It randomly drops packets before a queue fills — That is WRED.

**Explanation:** LLQ = CBWFQ (bandwidth guarantees per class) plus a policed strict-priority queue.

### core-d4-022 · match · concept · difficulty 2 · obj 4.7 · QoS
Match each QoS tool to what it does.
- Classification → Identifies traffic by ACL, NBAR or existing markings
- Marking → Writes a CoS or DSCP value into the header
- Queuing → Schedules packets on a congested interface
- Policing → Drops or re-marks traffic that exceeds a rate
- Shaping → Buffers excess traffic and sends it later

**Explanation:** QoS tools work together: classify → mark → queue → avoid congestion → police/shape.

### core-d4-023 · tf · concept · difficulty 1 · obj 4.7 · QoS
True or false: QoS creates additional bandwidth on a congested link.
- ❌ True — The statement is false, so this choice is incorrect.
- ✅ False

**Explanation:** QoS manages existing bandwidth by prioritizing, protecting and limiting traffic classes; it never adds capacity.

### core-d4-024 · single · command · difficulty 2 · obj 4.8 · SSH, TFTP and FTP
Which global command forces a Cisco device to accept only SSH version 2 connections?
- ✅ ip ssh version 2
- ❌ transport input ssh2 — This is not valid syntax; 'transport input' controls which protocols are allowed on lines.
- ❌ crypto key generate rsa version 2 — Key generation has no version option.
- ❌ ssh server v2 — This is not valid IOS syntax.

**Explanation:** SSHv2 requires RSA keys of at least 768 bits; 2048 bits is recommended.

### core-d4-025 · single · protocol-id · difficulty 1 · obj 4.9 · SSH, TFTP and FTP
Which statement correctly compares FTP and TFTP?
- ✅ FTP: TCP with login; TFTP: UDP without login
- ❌ TFTP uses TCP 21 and FTP uses UDP 69 — The transports and ports are swapped.
- ❌ Both encrypt files in transit — Neither encrypts; use SCP or SFTP for encryption.
- ❌ TFTP supports directory listings and FTP does not — The opposite is true.

**Explanation:** Use TFTP only on trusted management networks for simple image and configuration copies.

## D5 Security Fundamentals — 34 questions (blueprint 15%)

### core-d5-001 · match · concept · difficulty 1 · obj 5.1 · Threats and security programs
Match each security term to its definition.
- Vulnerability → A weakness in a system that could be attacked
- Threat → A potential danger that could exploit a weakness
- Exploit → The method or code used to take advantage of a weakness
- Mitigation → A countermeasure that reduces the risk

**Explanation:** Example: unpatched software (vulnerability), an attacker (threat), a crafted packet (exploit), a patch or ACL (mitigation).

### core-d5-002 · single · scenario · difficulty 2 · obj 5.1 · Threats and security programs
An attacker sends small DNS queries to many open resolvers with the victim's IP as the spoofed source. The resolvers send large responses to the victim. Which attack is this?
- ✅ Reflection and amplification
- ❌ Man-in-the-middle — A MITM attacker intercepts traffic between two parties.
- ❌ Brute-force password attack — No credentials are being guessed.
- ❌ Phishing — Phishing tricks users with deceptive messages.

**Explanation:** Reflection uses a spoofed source address; amplification exploits protocols whose responses are larger than requests. Together they create a DDoS.

### core-d5-003 · match · concept · difficulty 1 · obj 5.1 · Threats and security programs
Match each social engineering technique to its description.
- Phishing → Fraudulent email that tricks users into revealing information
- Vishing → Deception over a voice phone call
- Smishing → Deception through SMS text messages
- Whaling → Targeted phishing aimed at senior executives
- Tailgating → Following an authorized person through a secured door

**Explanation:** Social engineering targets people rather than technology. User awareness, training and physical access control are the main defenses.

### core-d5-004 · single · concept · difficulty 1 · obj 5.1 · Threats and security programs
Which type of malware spreads across networks by itself without requiring user action?
- ✅ Worm
- ❌ Virus — A virus attaches to files or programs and needs user action to spread.
- ❌ Trojan horse — A Trojan disguises itself as legitimate software and relies on the user to run it.
- ❌ Ransomware — Ransomware encrypts data for ransom; spreading is not its defining trait.

**Explanation:** Malware types are defined by how they spread and what they do. Worms are especially dangerous because they need no interaction.

### core-d5-005 · multi · concept · difficulty 2 · obj 5.2 · Threats and security programs
Which items are security program elements listed in the CCNA objectives? (Choose two.)
- ✅ User awareness
- ✅ Physical access control
- ❌ OSPF authentication — A useful technical control, but not a security program element.
- ❌ Trunk pruning — A VLAN optimization, not a security program element.
- ❌ Dynamic NAT — An address translation feature, not a security program element.

**Explanation:** The three program elements are user awareness, user training and physical access control.

### core-d5-006 · single · config · difficulty 2 · obj 5.3 · Device access, passwords and AAA
Both 'enable password Cisco1' and 'enable secret Cisco2' are configured. Which password grants privileged EXEC access?
- ✅ Cisco2
- ❌ Cisco1 — The enable password is ignored while an enable secret is configured.
- ❌ Either password — Only the secret is checked.
- ❌ Neither; the configuration is rejected — IOS accepts both but warns that they should differ.

**Explanation:** Always use 'enable secret', which is stored as a hash.

### core-d5-007 · tf · concept · difficulty 2 · obj 5.3 · Device access, passwords and AAA
True or false: 'service password-encryption' protects passwords with strong, irreversible hashing.
- ❌ True — The statement is false, so this choice is incorrect.
- ✅ False

**Explanation:** It applies weak, reversible type 7 encryption to clear-text passwords in the configuration. Use 'secret' commands for hashed passwords.

### core-d5-008 · single · scenario · difficulty 2 · obj 5.4 · Device access, passwords and AAA
Which login method is an example of multifactor authentication?
- ✅ A password plus a code from a phone app
- ❌ A password plus a four-digit PIN — Both are something you know — one factor.
- ❌ Two different passwords in a row — Still a single factor type.
- ❌ A fingerprint scan on its own — One factor (something you are).

**Explanation:** MFA requires factors from different categories: knowledge, possession and inherence.

### core-d5-009 · match · concept · difficulty 1 · obj 5.8 · Device access, passwords and AAA
Match each AAA function to its purpose.
- Authentication → Verifies who the user is
- Authorization → Determines what the user is allowed to do
- Accounting → Records what the user did and when

**Explanation:** AAA: who are you, what can you do, what did you do.

### core-d5-010 · multi · protocol-id · difficulty 2 · obj 5.8 · Device access, passwords and AAA
Which statements about TACACS+ are true? (Choose two.)
- ✅ It uses TCP port 49
- ✅ It encrypts the entire packet payload
- ❌ It combines authentication and authorization in one exchange — That describes RADIUS; TACACS+ separates the three AAA functions.
- ❌ It uses UDP 1812 — UDP 1812 is RADIUS authentication.

**Explanation:** TACACS+ is preferred for device administration (per-command authorization and full encryption). RADIUS is common for network access such as 802.1X and VPN.

### core-d5-011 · single · config · difficulty 3 · obj 5.8 · Device access, passwords and AAA
Which command authenticates logins against TACACS+ but falls back to local accounts if the TACACS+ server is unreachable?
- ✅ aaa authentication login default group tacacs+ local
- ❌ aaa authentication login default local group tacacs+ — This checks local accounts first.
- ❌ login local — This line command uses only the local database.
- ❌ aaa fallback local — This is not valid IOS syntax.

**Explanation:** Method lists are evaluated left to right. A rejection from the server does not fall through; only an error does.

### core-d5-012 · single · command · difficulty 2 · obj 5.3 · Device access, passwords and AAA
Which command blocks login attempts for 120 seconds after 3 failures within 60 seconds?
- ✅ login block-for 120 attempts 3 within 60
- ❌ login delay 120 — This adds a delay between attempts but does not count failures.
- ❌ exec-timeout 120 3 — This sets an idle timeout for sessions.
- ❌ security passwords min-length 3 — This enforces minimum password length.

**Explanation:** Combine login block-for with strong passwords, SSH only and access-class ACLs on VTY lines.

### core-d5-013 · single · calculation · difficulty 2 · obj 5.6 · Access control lists
Which wildcard mask matches all addresses from 172.16.32.0 through 172.16.47.255?
- ✅ 0.0.15.255
- ❌ 0.0.16.255 — Wildcard octets for a contiguous block are always 2^n − 1.
- ❌ 255.255.240.0 — This is the subnet mask, not the wildcard.
- ❌ 0.0.31.255 — This matches 32 third-octet values (172.16.32.0–172.16.63.255).

**Explanation:** Wildcard = all-255s minus the subnet mask. Quick check: the interesting octet equals block size − 1.

### core-d5-014 · single · diagram · difficulty 2 · obj 5.6 · Access control lists
Refer to the ACL. Host 10.1.1.20 opens an SSH session (TCP 22) to 10.2.2.10. What happens?
*Diagram: acl_flow*
- ✅ It is denied by line 20
- ❌ It is permitted by line 10 — Line 10 requires destination port 443.
- ❌ It is permitted by line 30 — Processing stops at the first match, line 20.
- ❌ It is denied by the implicit deny — An explicit line matches first.

**Explanation:** ACLs are processed top-down and stop at the first match; each line must match protocol, addresses and ports.

### core-d5-015 · multi · diagram · difficulty 3 · obj 5.6 · Access control lists
Refer to the ACL. Which flows are permitted? (Choose two.)
*Diagram: acl_flow*
- ✅ 10.1.1.20 to 10.2.2.10 on TCP 443
- ✅ 10.9.9.9 to 8.8.8.8 on UDP 53
- ❌ 10.9.9.9 to 10.2.2.10 on TCP 443 — The source is outside 10.1.1.0/24, so line 20 denies it.
- ❌ 10.1.1.20 to 10.2.2.10 using ICMP — Line 10 matches only TCP, so line 20 denies it.

**Explanation:** Check every field of each line before moving to the next. A permit for one port does not permit other protocols to the same host.

### core-d5-016 · single · scenario · difficulty 2 · obj 5.6 · Access control lists
Host 10.1.1.50 must be blocked from reaching server 10.3.3.3, and all other traffic must be allowed. Which approach is best?
- ✅ An extended ACL inbound near 10.1.1.50
- ❌ A standard ACL inbound near 10.1.1.50 — A standard ACL matches only the source, so it would block the host from everything.
- ❌ An extended ACL outbound on every router interface — One well-placed ACL is enough.
- ❌ Port security on the server's switch port — Port security restricts MAC addresses, not IP flows.

**Explanation:** Place extended ACLs near the source and standard ACLs near the destination.

### core-d5-017 · single · troubleshooting · difficulty 2 · obj 5.6 · Access control lists
After this ACL is applied, users lose all connectivity through Gi0/1. Why?
```
ip access-list extended BLOCK-TELNET
 10 deny tcp any any eq 23
!
interface GigabitEthernet0/1
 ip access-group BLOCK-TELNET in
```
- ✅ There is no permit statement
- ❌ Telnet is required for routing updates — Routing protocols do not use Telnet.
- ❌ ACL names must be numbers — Named ACLs are fully supported.
- ❌ Extended ACLs cannot be applied inbound — Extended ACLs can be applied in either direction.

**Explanation:** Add 'permit ip any any' (or more specific permits) after the deny.

### core-d5-018 · single · troubleshooting · difficulty 3 · obj 5.6 · Access control lists
With this ACL applied, is traffic from 192.168.1.66 denied?
```
access-list 10 permit 192.168.1.0 0.0.0.255
access-list 10 deny host 192.168.1.66
```
- ✅ No; line 1 permits it first
- ❌ Yes; specific entries are always checked first — IOS does not reorder entries by specificity for you.
- ❌ Yes; it is denied by the implicit deny — The explicit permit matches first.
- ❌ It depends on the interface direction — Direction does not change the order of evaluation.

**Explanation:** Place specific entries before general ones: deny host 192.168.1.66 first, then permit the subnet.

### core-d5-019 · single · command · difficulty 1 · obj 5.6 · Access control lists
Which command applies standard ACL 5 to restrict who can open SSH sessions to the device's VTY lines?
- ✅ access-class 5 in (under line vty)
- ❌ ip access-group 5 in (under line vty) — ip access-group is an interface command.
- ❌ access-list 5 vty — This is not valid syntax.
- ❌ ip ssh access-list 5 — This is not standard IOS syntax for VTY filtering.

**Explanation:** Interfaces use 'ip access-group'; VTY lines use 'access-class'.

### core-d5-020 · tf · concept · difficulty 1 · obj 5.6 · Access control lists
True or false: Only one IPv4 ACL can be applied to an interface per direction.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** The rule is one ACL per interface, per direction, per protocol. Applying a new one replaces the old one.

### core-d5-021 · single · troubleshooting · difficulty 3 · obj 5.7 · Layer 2 security
Based on the output, what is happening on Gi0/5?
```
SW1# show port-security interface gi0/5
Port Security              : Enabled
Port Status                : Secure-up
Violation Mode             : Restrict
Maximum MAC Addresses      : 1
Total MAC Addresses        : 1
Sticky MAC Addresses       : 0
Security Violation Count   : 7
```
- ✅ Violations are dropped and logged; the port stays up
- ❌ The port has been err-disabled — Port status 'Secure-up' shows it is operational.
- ❌ Violating frames are dropped silently without counting — That describes protect mode.
- ❌ Seven sticky MAC addresses were learned — Only 1 MAC address is allowed and present.

**Explanation:** Violation modes: protect (drop silently), restrict (drop + log + count), shutdown (err-disable, the default).

### core-d5-022 · match · concept · difficulty 2 · obj 5.7 · Layer 2 security
Match each port-security violation mode to its behavior.
- Protect → Drops offending frames without logging or counting
- Restrict → Drops offending frames, logs and increments the counter
- Shutdown → Puts the port into the err-disabled state

**Explanation:** Shutdown is the default and the most disruptive; restrict is common where visibility matters but uptime is important.

### core-d5-023 · single · config · difficulty 2 · obj 5.7 · Layer 2 security
What are the default settings after entering only 'switchport port-security' on an access port?
- ✅ Maximum of 1 MAC address and violation mode shutdown
- ❌ Maximum of 2 MAC addresses and violation mode restrict — These must be configured explicitly.
- ❌ Maximum of 1 MAC address and violation mode protect — The default mode is shutdown.
- ❌ Unlimited MAC addresses with logging only — Port security limits addresses by default.

**Explanation:** Port security works on access ports (or static trunks), not on dynamic DTP ports.

### core-d5-024 · single · scenario · difficulty 2 · obj 5.7 · Layer 2 security
A user plugs in a home router that starts handing out 192.168.0.x addresses to colleagues. Which feature prevents this?
- ✅ DHCP snooping
- ❌ Dynamic ARP Inspection alone — DAI validates ARP; it does not block rogue DHCP servers.
- ❌ PortFast — PortFast only speeds up port transitions.
- ❌ Root Guard — Root Guard protects the STP root placement.

**Explanation:** DHCP snooping trusts only uplinks toward legitimate servers and builds the binding table that DAI uses.

### core-d5-025 · multi · config · difficulty 3 · obj 5.7 · Layer 2 security
DHCP snooping is enabled globally. The legitimate DHCP server is reached through uplink Gi0/24, and clients are in VLAN 10. Which additional commands are required? (Choose two.)
- ✅ ip dhcp snooping vlan 10
- ✅ ip dhcp snooping trust (on Gi0/24)
- ❌ ip dhcp snooping trust (on every access port) — Trusting user ports defeats the protection.
- ❌ ip arp inspection vlan 10 — DAI is a separate, optional feature built on top of snooping.

**Explanation:** Without trusting the uplink, the switch drops the real server's OFFERs and clients get no addresses.

### core-d5-026 · single · concept · difficulty 2 · obj 5.7 · Layer 2 security
Dynamic ARP Inspection validates ARP packets on untrusted ports against which information?
- ✅ The DHCP snooping binding table
- ❌ The MAC address table on the switch — The MAC table does not contain IP addresses.
- ❌ The router's IP routing table — Routes are unrelated to ARP validation.
- ❌ The CDP neighbor table entries — CDP is unrelated to ARP validation.

**Explanation:** DAI prevents ARP spoofing/poisoning, a common man-in-the-middle technique.

### core-d5-027 · single · scenario · difficulty 2 · obj 5.5 · VPNs
A branch and headquarters must communicate securely over the internet, and branch hosts must not need any VPN software. Which solution fits?
- ✅ A site-to-site IPsec VPN between the two gateways
- ❌ A remote-access VPN client on every branch PC — This requires software on each host.
- ❌ Clientless SSL VPN — This gives individual users browser-based access to specific applications.
- ❌ GRE without IPsec — GRE alone provides no encryption.

**Explanation:** Site-to-site VPNs connect networks; remote-access VPNs connect individual users.

### core-d5-028 · multi · concept · difficulty 2 · obj 5.5 · VPNs
Which security services does IPsec provide? (Choose two.)
- ✅ Data confidentiality
- ✅ Anti-replay protection
- ❌ Dynamic routing — IPsec protects traffic but does not exchange routes.
- ❌ Bandwidth reservation — QoS, not IPsec, manages bandwidth.

**Explanation:** IPsec services: confidentiality, integrity, origin authentication and anti-replay.

### core-d5-029 · match · protocol-id · difficulty 2 · obj 5.9 · Wireless security
Match each Wi-Fi security generation to its key mechanism.
- WEP → RC4 with static keys — considered broken
- WPA → TKIP
- WPA2 → AES-CCMP
- WPA3-Personal → SAE

**Explanation:** Each generation strengthened protection; WPA3 also requires Protected Management Frames.

### core-d5-030 · single · scenario · difficulty 2 · obj 5.9 · Wireless security
Each employee must authenticate to Wi-Fi with their own directory credentials, validated by a RADIUS server. Which option fits?
- ✅ WPA2- or WPA3-Enterprise using 802.1X
- ❌ WPA2-Personal with a shared passphrase — Everyone shares one key with no per-user identity.
- ❌ An open network with MAC filtering — MAC addresses are easily spoofed and provide no encryption.
- ❌ WEP with a long key — WEP is insecure regardless of key length.

**Explanation:** Personal mode = shared key (PSK/SAE). Enterprise mode = individual credentials via 802.1X/EAP and RADIUS.

### core-d5-031 · single · config · difficulty 2 · obj 5.10 · Wireless security
In the WLC GUI, which Layer 2 security settings configure a WLAN for WPA2-Personal?
- ✅ WPA2 policy, AES encryption, PSK key management
- ❌ WPA policy, TKIP encryption, 802.1X key management — This is legacy WPA-Enterprise.
- ❌ Open security with web authentication — This is a guest portal, not WPA2.
- ❌ WEP with a 104-bit key — WEP is obsolete and insecure.

**Explanation:** On the WLAN's Security tab: Layer 2 = WPA+WPA2 with WPA2 policy, AES (CCMP) cipher and PSK authentication key management.

### core-d5-032 · single · diagram · difficulty 2 · obj 5.9 · Wireless security
Refer to the diagram. Which 802.1X role does the AP/WLC perform?
*Diagram: dot1x_quiz*
- ✅ Authenticator
- ❌ Supplicant — The supplicant is the client device.
- ❌ Authentication server — That is the RADIUS server, such as ISE.
- ❌ Certificate authority — A CA issues certificates and is not an 802.1X role.

**Explanation:** 802.1X roles: supplicant (client), authenticator (switch/AP/WLC), authentication server (RADIUS).

### core-d5-033 · tf · concept · difficulty 2 · obj 5.9 · Wireless security
True or false: WPA3-Personal uses SAE, which resists offline dictionary attacks against the passphrase.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** SAE replaces the WPA2 pre-shared key handshake, so capturing the handshake does not allow offline guessing. It also provides forward secrecy.

### core-d5-034 · single · concept · difficulty 2 · obj 5.1 · Threats and security programs
How does an IPS differ from an IDS?
- ✅ An IPS is inline and can block; an IDS only alerts
- ❌ An IDS is inline and blocks traffic; an IPS only alerts — The roles are reversed.
- ❌ Both only log traffic and cannot take action — An IPS can drop traffic.
- ❌ An IPS replaces the need for firewalls — IPS complements firewalls; NGFWs often integrate both.

**Explanation:** Prevention requires being in the traffic path; detection can work from a SPAN copy.

## D6 Automation and Programmability — 24 questions (blueprint 10%)

### core-d6-001 · match · concept · difficulty 1 · obj 6.2 · Automation and controller-based networking
Match each activity to the network plane it belongs to.
- Forwarding a frame using the MAC address table → Data plane
- Exchanging OSPF LSAs with a neighbor → Control plane
- An administrator logging in with SSH → Management plane

**Explanation:** The data plane moves user traffic, the control plane builds forwarding information, and the management plane is used to operate the device.

### core-d6-002 · single · scenario · difficulty 2 · obj 6.3 · Automation and controller-based networking
A Python script retrieves the device inventory from Catalyst Center using its REST API. Which interface is the script using?
- ✅ Northbound interface
- ❌ Southbound interface — The controller uses southbound interfaces to talk to network devices.
- ❌ Data plane — The data plane forwards user traffic.
- ❌ Underlay — The underlay is the physical IP transport network.

**Explanation:** Picture the controller in the middle: applications above (northbound, usually REST/JSON) and devices below (southbound).

### core-d6-003 · multi · protocol-id · difficulty 2 · obj 6.3 · Automation and controller-based networking
Which of these are southbound protocols a controller can use to configure network devices? (Choose two.)
- ✅ NETCONF
- ✅ OpenFlow
- ❌ JSON — JSON is a data format, not a protocol.
- ❌ YAML — YAML is a data format, typically used by Ansible playbooks.
- ❌ OAuth — OAuth is an authorization framework, typically for APIs.

**Explanation:** Southbound options include NETCONF, RESTCONF, OpenFlow, OpFlex, SSH/CLI and SNMP.

### core-d6-004 · tf · concept · difficulty 2 · obj 6.3 · Automation and controller-based networking
True or false: In controller-based networking, user traffic no longer passes through switches; the controller forwards all packets.
- ❌ True — The statement is false, so this choice is incorrect.
- ✅ False

**Explanation:** The controller centralizes control and management, but devices still forward user traffic in their data planes.

### core-d6-005 · match · protocol-id · difficulty 3 · obj 6.2 · Automation and controller-based networking
Match each Cisco SD-Access function to the technology that provides it.
- Underlay routing → IS-IS
- Overlay data plane encapsulation → VXLAN
- Overlay control plane → LISP
- Management and automation → Catalyst Center

**Explanation:** SD-Access builds a fabric: an IS-IS underlay, VXLAN encapsulation, LISP mapping, and Catalyst Center for automation and assurance.

### core-d6-006 · single · concept · difficulty 1 · obj 6.2 · Automation and controller-based networking
In a software-defined fabric, what is the underlay?
- ✅ The physical IP transport network
- ❌ The virtual tunnels that carry user traffic — That is the overlay.
- ❌ The controller's northbound API — APIs are not part of the underlay.
- ❌ The wireless network only — The underlay covers the whole transport network.

**Explanation:** Underlay = physical transport; overlay = virtual networks on top; fabric = both managed as one.

### core-d6-007 · single · scenario · difficulty 2 · obj 6.1 · Automation and controller-based networking
Why does controller-based management reduce configuration errors across 300 switches?
- ✅ Policy is defined centrally and pushed consistently
- ❌ It removes the need for spanning tree — Controllers do not change Layer 2 loop prevention requirements.
- ❌ It encrypts the CLI — Encryption is unrelated to consistency.
- ❌ It disables the management plane on switches — Devices still need management access.

**Explanation:** Automation improves consistency, speed and auditability, and frees engineers for design work.

### core-d6-008 · single · scenario · difficulty 2 · obj 6.4 · AI and ML in operations
A monitoring platform learns typical DHCP onboarding times for each site and alerts when a site deviates. Which kind of AI is this?
- ✅ Predictive AI
- ❌ Generative AI — Generative AI creates content such as text or configurations.
- ❌ A static threshold alarm — A static threshold does not learn per-site baselines.
- ❌ Rule-based ACL logging — ACL logging records matches; it does not learn behavior.

**Explanation:** Predictive AI uses telemetry to build baselines, detect anomalies and forecast problems.

### core-d6-009 · single · scenario · difficulty 2 · obj 6.4 · AI and ML in operations
An engineer uses a generative AI assistant to draft an ACL for a production router. What is the correct practice?
- ✅ Validate it in a lab before deploying
- ❌ Apply it immediately because AI output is always accurate — Generated configurations can contain errors.
- ❌ Trust it because the model was trained on vendor documentation — Training data does not guarantee correctness.
- ❌ Paste the router's credentials into the prompt for better results — Sensitive data should not be shared with AI services.

**Explanation:** Use AI to speed up drafting and analysis, but keep humans responsible for validating changes.

### core-d6-010 · tf · concept · difficulty 1 · obj 6.4 · AI and ML in operations
True or false: Supervised machine learning trains models using labeled data.
- ✅ True
- ❌ False — The statement is true, so this choice is incorrect.

**Explanation:** Supervised learning uses labeled examples; unsupervised learning finds patterns in unlabeled data; reinforcement learning learns from rewards.

### core-d6-011 · match · protocol-id · difficulty 1 · obj 6.5 · REST APIs and JSON
Match each CRUD operation to the HTTP method normally used in REST APIs.
- Create → POST
- Read → GET
- Update (replace) → PUT
- Delete → DELETE

**Explanation:** REST maps CRUD to HTTP verbs. PATCH is used for partial updates.

### core-d6-012 · single · troubleshooting · difficulty 2 · obj 6.5 · REST APIs and JSON
A script calling a controller API receives HTTP 401. What is the most likely cause?
- ✅ Missing or invalid authentication
- ❌ The requested resource does not exist — That is 404.
- ❌ The server hit an internal error — That is 500.
- ❌ The request succeeded — Success codes are 2xx.

**Explanation:** 401 Unauthorized = authentication needed or failed; 403 Forbidden = authenticated but not allowed.

### core-d6-013 · single · protocol-id · difficulty 1 · obj 6.5 · REST APIs and JSON
Which HTTP status code indicates that a POST request successfully created a new resource?
- ✅ 201 Created
- ❌ 200 OK — 200 is generic success, typically for GET.
- ❌ 204 No Content — 204 is success with no response body.
- ❌ 400 Bad Request — 400 is a client error.

**Explanation:** 2xx = success, 4xx = client error, 5xx = server error.

### core-d6-014 · multi · concept · difficulty 2 · obj 6.5 · REST APIs and JSON
Which statements describe REST APIs? (Choose two.)
- ✅ They are stateless; each request carries all the information the server needs
- ✅ They use HTTP methods on resources identified by URIs
- ❌ They require SNMP as the transport — REST normally uses HTTP/HTTPS.
- ❌ They keep a session state on the server between calls — That would violate the stateless constraint.
- ❌ They can only return XML — JSON is the most common format; XML and YAML are also possible.

**Explanation:** REST constraints: client-server, stateless, cacheable, uniform interface, layered system (and optional code-on-demand).

### core-d6-015 · single · calculation · difficulty 3 · obj 6.7 · REST APIs and JSON
How many JSON objects does this document contain, including the outermost one?
```
{
  "device": {
    "hostname": "SW1",
    "interfaces": [
      {"name": "Gi0/1", "enabled": true},
      {"name": "Gi0/2", "enabled": false}
    ]
  }
}
```
- ✅ 4
- ❌ 3 — This misses one of the objects.
- ❌ 2 — This counts only the outer and 'device' objects.
- ❌ 5 — The array itself is not an object.

**Explanation:** Every { } is an object; [ ] is an array. Count braces, not keys.

### core-d6-016 · single · calculation · difficulty 2 · obj 6.7 · REST APIs and JSON
Which statement about this JSON document is correct?
```
{
  "device": {
    "hostname": "SW1",
    "interfaces": [
      {"name": "Gi0/1", "enabled": true},
      {"name": "Gi0/2", "enabled": false}
    ]
  }
}
```
- ✅ 'interfaces' is an array containing two objects
- ❌ The values of 'enabled' are strings — true and false are unquoted JSON booleans.
- ❌ 'hostname' has a number value — "SW1" is a string.
- ❌ The document contains two arrays — There is only one array ('interfaces').

**Explanation:** JSON value types: string, number, boolean, null, object and array.

### core-d6-017 · single · concept · difficulty 1 · obj 6.7 · REST APIs and JSON
Which of these is valid JSON?
- ✅ {"vlan": 10, "name": "SALES"}
- ❌ {'vlan': 10, 'name': 'SALES'} — JSON does not allow single quotes.
- ❌ {"vlan": 10, "name": "SALES",} — Trailing commas are not allowed.
- ❌ {vlan: 10, name: "SALES"} — Keys must be quoted strings.

**Explanation:** JSON rules: double-quoted keys and strings, commas between members, no trailing commas, no comments.

### core-d6-018 · multi · concept · difficulty 2 · obj 6.6 · Ansible and Terraform
Which statements about Ansible are true? (Choose two.)
- ✅ It is agentless and connects to devices over SSH or APIs
- ✅ Playbooks are written in YAML
- ❌ It requires an agent on every managed device — That describes Puppet or Chef.
- ❌ It describes infrastructure using HCL — HCL is Terraform's language.

**Explanation:** Ansible key files: inventory (hosts and groups), playbooks (YAML), modules (units of work). It is written in Python and push-based.

### core-d6-019 · single · concept · difficulty 1 · obj 6.6 · Ansible and Terraform
Which tool uses declarative HCL configuration files and keeps a state file of the resources it manages?
- ✅ Terraform
- ❌ Ansible — Ansible uses YAML playbooks and has no state file.
- ❌ NETCONF — NETCONF is a device configuration protocol.
- ❌ Syslog — Syslog collects log messages.

**Explanation:** Terraform is infrastructure as code: providers, plan/apply workflow, state file.

### core-d6-020 · order · command · difficulty 2 · obj 6.6 · Ansible and Terraform
Arrange the typical Terraform workflow steps in order.
1. Write the .tf configuration files
2. Run terraform init
3. Run terraform plan
4. Run terraform apply

**Explanation:** 'init' downloads providers, 'plan' previews changes, and 'apply' makes them.

### core-d6-021 · single · concept · difficulty 1 · obj 6.6 · Ansible and Terraform
Which Ansible file lists the managed devices and organizes them into groups?
- ✅ Inventory
- ❌ Playbook — A playbook defines the tasks to run.
- ❌ Module — A module is a unit of work called by a task.
- ❌ State file — State files belong to Terraform.

**Explanation:** Example: an inventory group [access_switches] targeted by a playbook that sets NTP and banners.

### core-d6-022 · tf · command · difficulty 2 · obj 6.6 · Ansible and Terraform
True or false: 'terraform plan' changes the managed infrastructure.
- ❌ True — The statement is false, so this choice is incorrect.
- ✅ False

**Explanation:** 'terraform plan' only previews the changes Terraform would make; 'terraform apply' performs them.

### core-d6-023 · single · concept · difficulty 2 · obj 6.5 · REST APIs and JSON
Which HTTP request header commonly carries a token for REST API authentication?
- ✅ Authorization
- ❌ Content-Type — Content-Type describes the body format, such as application/json.
- ❌ Accept — Accept states which response formats the client can handle.
- ❌ User-Agent — User-Agent identifies the client software.

**Explanation:** Some APIs use custom headers (Catalyst Center uses X-Auth-Token), but the idea is the same: credentials accompany each request.

### core-d6-024 · single · concept · difficulty 1 · obj 6.1 · Automation and controller-based networking
Which statement best compares traditional and controller-based network management?
- ✅ Traditional is per-device; controller-based is centralized policy
- ❌ Traditional networks have no control plane — Every traditional device runs its own control plane.
- ❌ Controller-based networks do not use IP addressing — IP is still used everywhere.
- ❌ Traditional networks cannot be monitored — SNMP, syslog and similar tools work on traditional networks.

**Explanation:** Controllers add a central point for policy, automation, assurance and APIs.
