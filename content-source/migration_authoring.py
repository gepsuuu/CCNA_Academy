"""Hand-authored lesson-specific teaching scenarios. Not a technical approval certificate."""
LESSON_DESIGNS = {}
def lesson(id, opening, compare, mechanism, visual, observe, verify, troubleshoot, question, options, correct, rationales, config=None, config_note='', no_config=''):
    LESSON_DESIGNS[id] = dict(opening=opening, compare=compare, mechanism=mechanism, visual=visual, observe=observe, verify=verify, troubleshoot=troubleshoot, question=question, options=options, correct=correct, rationales=rationales, config=config, config_note=config_note, no_config=no_config)

lesson('1-01',
'A new branch needs laptops, phones, a printer, Wi-Fi, and access to a head-office application. Before choosing equipment, trace what each conversation needs. A laptop talking to its local printer needs local forwarding; reaching head office needs routing; deciding whether that conversation is permitted needs security policy.',
'A Layer 2 switch forwards within a VLAN using destination MAC addresses. A router or routing-enabled Layer 3 switch forwards between IP networks. An access point bridges wireless clients into a network. A controller coordinates devices; it is not automatically in every user packet path. A firewall applies policy, and an inline IPS can inspect and block matching attacks.',
'An endpoint creates data and a server provides a service. These are roles, not necessarily different hardware types. A switch learns where source MAC addresses are reachable, while a router consults destination IP prefixes. PoE adds a separate power relationship: a power sourcing device supplies a compatible powered device after detection/negotiation. Link connectivity, power budget, and application availability are separate checks.',
'Picture laptop, access switch, router, and head-office server in that order. Place the AP beside the switch, not between the router and every wired PC. Label the AP uplink with both data and power relationships. This is a guided topology description; a dedicated device-role illustration remains a visual acceptance item.',
'1. A laptop sends a local print job toward the printer MAC after address resolution.\n2. The switch forwards within the local VLAN.\n3. For the head-office server, the laptop sends a frame to its gateway instead.\n4. The gateway routes toward head office; a firewall may permit or deny the flow.\n5. The server must still accept the application request. No single device role guarantees the whole chain.',
'Check the switch port state, MAC learning, gateway routes, and application response separately. On supported Catalyst platforms, show power inline helps distinguish an unpowered AP from a data-path fault. Available power and consumed power are not the same as interface speed.',
'An AP is dark but the switch is online. Inspect cabling, PoE capability, per-port power state, and total power budget before changing routing. If it powers up but clients still fail, check radio, authentication, VLAN, DHCP, and gateway evidence in that order. Replacing a router does not correct an exhausted PoE budget.',
'A phone has no power on a non-PoE switch port. Which change directly addresses that symptom?',
['Use a compatible PoE source or power adapter','Add a static route','Change the DNS server','Increase the OSPF cost'],0,
['Power must come from a compatible source; this addresses the missing power relationship.','A route affects forwarding after the phone operates, not electrical power.','DNS resolves names and cannot supply power.','OSPF cost affects path selection, not power delivery.'],
no_config='This lesson identifies device roles. It does not define one universal configuration for routers, APs, firewalls, and PoE platforms; their configurations are taught in the relevant lessons.')

lesson('1-02',
'A growing office can no longer connect every new switch wherever a free port appears. The design needs predictable paths and failure boundaries. A topology architecture describes how device roles and links are arranged so growth and outages can be reasoned about.',
'Two-tier campus designs combine distribution and core roles; three-tier designs separate access, distribution, and core. Spine-leaf connects each leaf to each spine and is often used for data-center east-west traffic. SOHO combines roles in few devices. A WAN joins sites; cloud describes where services run and who operates them, not a particular cable pattern.',
'Physical topology is the actual wiring. Logical topology is the forwarding relationship, which VLANs, routing, tunnels, and blocked links can change. Redundant links improve resilience only if forwarding and failure handling are designed. A second cable can create a Layer 2 loop instead of useful redundancy.',
'For a three-tier campus, read from endpoints through access, distribution, and core. For spine-leaf, put leaves in one row and spines above; connect every leaf to every spine. Do not add direct leaf-to-leaf links to the basic example. Compare the number of devices crossed, not merely how attractive the drawing looks.',
'1. Trace a same-access-switch conversation.\n2. Trace a conversation to a different access block.\n3. Remove one uplink in the design.\n4. Identify the remaining physical path.\n5. Ask whether routing or spanning tree actually permits that path. Physical redundancy is a prerequisite, not proof of convergence.',
'Use inventory and neighbor information to reconcile the intended topology with cabling. Confirm uplink capacities, routing adjacencies or STP roles, and the failure domain. A link diagram alone cannot establish that redundant power or WAN providers are independent.',
'One distribution switch failure disconnects an entire floor despite two access uplinks. Check whether both uplinks terminate on that same distribution switch or share a common upstream dependency. Correct the design dependency, then test an approved failure scenario rather than assuming two lines on a diagram mean resilience.',
'In the basic spine-leaf model, how does a leaf reach another leaf?',
['Through a spine','Through a required direct leaf-to-leaf link','Through every spine in sequence','Through a wireless controller'],0,
['Each leaf connects to spines, which provide paths between leaves.','Direct leaf-to-leaf links are not part of this basic model.','A normal path uses a selected spine, not all spines serially.','A wireless controller is not the spine-leaf transit role.'],
no_config='An architecture is a design choice rather than one IOS feature. Device configurations depend on the chosen Layer 2/Layer 3 design and are covered in switching and routing lessons.')

lesson('1-03',
'A browser reports that a page cannot load. The cable can be connected while DNS, TCP, or the application is failing. Layered models help separate these responsibilities instead of treating every failure as the internet being down.',
'OSI uses seven conceptual layers; the TCP/IP model groups responsibilities differently. The models are reasoning tools, not seven separate physical boxes. A TCP port identifies a transport endpoint; an IP address supports network-layer delivery; a MAC address supports local-link delivery.',
'Encapsulation adds the information needed at each layer: application bytes become transport data, an IP packet, and a link frame. At a routed Ethernet hop, the router removes the old frame and creates a new one for the next link. Ordinary routing preserves IP endpoints but changes TTL and the IPv4 checksum; NAT is a separate exception.',
'In the encapsulation diagram, read the nested headers from outside inward on reception and inside outward on transmission. Arrows indicate processing order, not that every intermediate switch opens the application. In the model comparison, align functions rather than assuming a one-to-one layer-number mapping.',
'1. Assume DNS has already resolved the web server and the client has a route.\n2. TCP creates a segment for the connection.\n3. IPv4 supplies the remote destination address.\n4. Ethernet supplies the local next-hop MAC.\n5. A switch forwards the frame; a router replaces its link header.\n6. The receiving server delivers data up its stack. TLS, when used, adds another security exchange; it is not performed by every transit switch.',
'Inspect a packet capture in an authorized lab: compare Ethernet destination, IP destination, and TCP destination port. On a remote-server flow, the Ethernet destination on the first link is the gateway, not the remote server. Successful ICMP does not verify TCP service or TLS identity.',
'An IP ping works but the browser fails. Test name resolution, the intended transport port, and application/TLS behavior. Do not replace a working cable solely because an application reports an error. Correct the layer supported by evidence, then retest the original application.',
'For a remote web server, which destination belongs in the first Ethernet frame?',
['The local gateway MAC','The remote server MAC learned across the internet','The DNS hostname','The TCP destination port'],0,
['The first frame must reach the local next hop.','ARP does not learn a remote server MAC across routers.','A hostname is resolved by application services, not used as an Ethernet address.','A port is a transport field, not a MAC address.'],
no_config='Layer models and encapsulation are explanatory foundations, not a configurable IOS protocol. Addressing, VLANs, and routing supply the concrete configurations.')

lesson('1-04',
'A new uplink shows link light but loses traffic under load. Before changing routing, ask whether the medium, optics, speed, and duplex agree. Physical compatibility determines whether the higher-layer conversation can be carried reliably.',
'Copper twisted pairs carry electrical signals; fiber carries light. Single-mode fiber supports a different propagation mode and optic design from multimode fiber; do not choose by connector shape alone. LC, SC, and ST describe connector forms, while transceiver standards determine wavelength, fiber type, reach, and speed compatibility.',
'Twisting reduces susceptibility to interference by helping a balanced pair reject common-mode noise. An 8P8C connector, often called RJ-45 in Ethernet practice, has eight contact positions. T568A and T568B assign pair colors differently; the same scheme at both ends makes a straight-through cable. Traditional crossover wiring exchanges the relevant transmit/receive pairs; auto-MDI-X can remove the need for manual crossover selection on supported devices.\n\nFull duplex permits simultaneous transmission and reception and does not use CSMA/CD. Collision detection belongs to traditional shared or half-duplex Ethernet. A collision counter is not normal proof of busy full-duplex switching.',
'Imagine the cable as four balanced pairs, not eight unrelated wires. A future pinout illustration must state viewing orientation and pin numbers before comparing T568A/B. For fiber, identify core, cladding, transmitter, and receiver without drawing literal zigzag paths as the exact electromagnetic behavior. Physical illustrations remain pending; this text is not a substitute for them.',
'1. Confirm the exact port and transceiver types at both ends.\n2. Check the correct fiber type and transmit-to-receive pairing.\n3. Bring up the link and inspect negotiated speed/duplex.\n4. Generate approved test traffic.\n5. Compare error-counter deltas, not just lifetime totals. A dirty connector can pass some light and still cause errors.',
'On IOS, show interfaces GigabitEthernet0/1 reveals operational state, speed/duplex where supported, and error counters. On switches, show interfaces status summarizes port status. CRC/input errors suggest corruption but do not uniquely identify the cause; inspect both ends, optics, cable, and configuration.',
'For an up link with rising CRC errors, replace a suspect patch lead with a known-good compatible lead and inspect optics safely. Never look into fiber or a transceiver. For duplex mismatch, restore compatible negotiation/settings on both ends instead of forcing one side only. Retest traffic and compare fresh error deltas.',
'Which statement fits ordinary full-duplex switched Ethernet?',
['It does not use CSMA/CD','Every busy port must show collisions','Only one endpoint may transmit at a time','Fiber always guarantees error-free delivery'],0,
['Full duplex uses independent transmit/receive capability rather than collision detection.','Collisions are not a normal congestion signal on full-duplex links.','That restriction describes half-duplex operation.','Incorrect optics, dirty connectors, or other faults can corrupt fiber links.'],
no_config='This stage is physical selection and installation, not a universal speed/duplex command recipe. Forced settings depend on the exact medium and platform; verify both ends before changing them.')

lesson('1-05',
'A PC knows the IP address of a local printer, but Ethernet needs a local destination MAC address. ARP bridges that gap for IPv4: it asks which local interface owns the needed next-hop IP.',
'ARP maps an IPv4 next-hop address to a link-layer address. DNS maps a name to records such as IP addresses. A switch MAC table maps a MAC and VLAN to a port. These three tables answer different questions; clearing one is not equivalent to clearing the others.',
'For a local destination, the host resolves that destination IP. For a remote destination, routing selects a gateway and the host resolves the gateway IP. An ARP request is normally an Ethernet broadcast on the local VLAN; the target normally replies by unicast. ARP is carried directly in Ethernet, not inside TCP or UDP. Routers do not ordinarily forward the ARP broadcast into other subnets.',
'In the Ethernet frame diagram, distinguish source/destination MAC, type, payload, and FCS. The ARP message occupies the payload and contains protocol and hardware addresses. The FCS detects errors; it does not encrypt the frame or guarantee recovery.',
'1. PC-A 192.168.10.10/24 wants 192.168.10.20.\n2. With no cached mapping, it broadcasts an ARP request on VLAN 10.\n3. PC-B 192.168.10.20 replies with its MAC.\n4. PC-A caches the mapping and sends the IPv4 packet in a unicast frame.\n5. For 192.168.20.20 instead, PC-A resolves gateway 192.168.10.1. The remote IP remains the packet destination.',
'On a client, arp -a displays cached IPv4 mappings; on IOS use show ip arp. Check the next-hop IP, MAC, and interface/VLAN. A missing entry can mean no recent traffic, not necessarily a fault. In a lab capture, inspect the request target IP and reply sender IP before judging the mapping.',
'If a local peer is unreachable and ARP remains unresolved, verify local mask, VLAN, link, and the peer address before adding routes. If an incorrect mapping persists, investigate duplicate IPs or spoofing; deleting the cache alone treats a symptom. Correct the address or security issue and observe a fresh valid exchange.',
'PC-A sends to a remote subnet through 192.168.10.1. What does it normally ARP for?',
['192.168.10.1','The remote server IP','The DNS name','Every router on the path'],0,
['The gateway is the local next hop selected by routing.','The remote server is not directly on this link.','ARP resolves IPv4 addresses, not names.','Each router resolves its own local next hop as needed.'],
no_config='ARP normally operates automatically from addressing and routing. Static ARP is not needed for this demonstration; configuring it would hide the mechanism being taught.')

lesson('1-06',
'A file transfer must deliver every byte in order. A live voice application may prefer a late packet to be discarded rather than waiting for retransmission. Transport protocols give applications different building blocks, not a simple good-versus-bad choice.',
'TCP supplies a reliable ordered byte stream with connection state, sequence numbers, acknowledgments, retransmission, and flow control. UDP supplies datagrams without those transport guarantees. Applications can implement reliability above UDP; QUIC is an important example. UDP is not automatically faster in every workload, and TCP does not provide encryption by itself.',
'A TCP connection is identified by endpoints including IP addresses and ports. The three-way handshake synchronizes sequence spaces: SYN, SYN-ACK, ACK. Sequence numbers count bytes; SYN and FIN each consume sequence space. Acknowledgment numbers indicate the next expected byte. Flow control protects the receiver; congestion control responds to network conditions.',
'Use the handshake diagram as a time sequence: each arrow is a segment between client and server, not a routed hop. Mark the flags and distinguish sequence from acknowledgment. Retransmission can add arrows; the clean three-arrow illustration is one successful initial exchange.',
'1. Assume routing and address resolution are ready. Client sends SYN with illustrative sequence 100.\n2. Server sends SYN-ACK with sequence 500 and acknowledgment 101.\n3. Client acknowledges 501.\n4. Client sends 20 bytes starting at 101.\n5. With no gaps, server acknowledges 121. These numbers are educational examples, not real random initial values.',
'In a lab capture, filter the intended flow and inspect flags, sequence/ACK numbers, retransmissions, and advertised window. A completed handshake proves transport establishment, not successful login or application processing. A UDP response timeout alone cannot distinguish a blocked packet from a silent application.',
'If SYNs repeat without a reply, check service listening state, path, firewall policy, and return route. If the handshake succeeds but the application stalls, inspect TLS/application errors and loss/window evidence. Do not change from TCP to UDP as a generic repair.',
'A segment starts at sequence 101 and carries 20 bytes with no gap. What cumulative ACK is expected?',
['121','120','101','20'],0,
['The receiver expects the byte after 101 through 120, namely 121.','120 is the last byte received, not the next expected byte.','101 would indicate the data has not advanced the cumulative acknowledgment.','20 is the payload length, not a sequence-space acknowledgment.'],
no_config='TCP and UDP behavior is selected by applications and stacks, not enabled by a generic router command. The exercise interprets traffic rather than configuring a transport protocol on IOS.')

lesson('1-07',
'A subnet mask such as 255.255.255.224 looks arbitrary until you see its bits. Binary makes prefix boundaries visible; hexadecimal makes long bit patterns such as IPv6 addresses easier to read.',
'Binary uses two symbols per digit and hexadecimal uses sixteen. One hexadecimal digit represents four bits; one IPv4 octet contains eight bits. A decimal value and its binary or hexadecimal representation are the same quantity, not different addresses.',
'For an eight-bit octet, positions from left to right are 128,64,32,16,8,4,2,1. Add the weights whose bits are 1. For decimal-to-binary, select weights from largest to smallest and keep eight positions. For hexadecimal, group bits into nibbles and convert each group independently.',
'Write the eight weights above eight bit positions. Under 11000000, mark only 128 and 64: the sum is 192. Split 10101100 into 1010 and 1100 to read AC. Leading zeros preserve width; they do not change the value.',
'1. Convert 224: choose 128,64,32 and leave the remaining bits zero.\n2. Read 11100000 as 224.\n3. Count three leading 1s in this octet.\n4. In 255.255.255.224, the first three octets add 24 leading 1s, giving /27.\n5. Reverse the conversion to check the result.',
'Check by recomputing the weighted sum and by reversing the conversion. For a contiguous subnet mask, all 1s precede all 0s. 255.255.255.160 is not a valid contiguous IPv4 subnet mask because 160 is 10100000.',
'If an answer exceeds 255, check that you did not assign a ninth bit to an octet. If a hex conversion is wrong, group from the right and pad the left with zeros. Do not treat decimal 10 and hexadecimal 10 as equal; hexadecimal 10 is decimal 16.',
'What is binary 11100000 in decimal?',
['224','192','240','7'],0,
['128 + 64 + 32 = 224.','192 represents 11000000, missing the 32 bit.','240 represents 11110000, adding a 16 bit.','7 is 00000111, the weights at the other end.'],
no_config='Number-base conversion is a calculation skill, not a device configuration. Use the subnetting and IPv6 lessons to apply it to interfaces.')

lesson('1-08',
'Two PCs are connected to the same switch but disagree about whether the other is local. Their addresses alone do not answer that question: each host combines its address with its subnet mask to decide which destinations are on-link.',
'An IPv4 address is 32 bits. A prefix length tells how many leading bits identify the network. Private addresses can be reused in separate organizations, but private does not mean encrypted or trusted. Public address space is not synonymous with permission to access a service.',
'Bitwise AND of an address and its mask produces the network prefix. In a conventional /24, 192.168.10.10 and 192.168.10.20 share 192.168.10.0; 192.168.20.20 does not. RFC1918 private blocks are 10.0.0.0/8, 172.16.0.0/12, and 192.168.0.0/16. Not every 172.x address is private.',
'In the IPv4 structure diagram, read the prefix boundary before interpreting host bits. For /24, shade 24 network bits and eight host bits. Then move the boundary for /27; do not keep assuming the last octet is entirely host space.',
'1. PC-A is 192.168.10.10/24, gateway 192.168.10.1.\n2. Destination 192.168.10.20 matches the local prefix, so PC-A resolves that host.\n3. Destination 192.168.20.20 is remote, so PC-A selects the gateway.\n4. Without a usable route/gateway, the host cannot send that remote conversation merely because the switch link is up.',
'On IOS use show ip interface brief for addresses and state, then show ip route connected for installed connected networks. On the client check address, mask, and gateway together. A displayed address is not proof that the peer uses the same mask.',
'For local reachability failure, compare masks and duplicate-address evidence. For remote-only failure, check gateway and return routes. Correct the host or interface address plan first; adding NAT cannot repair two hosts using incompatible local subnet assumptions.',
'Which address is inside the RFC1918 172.16.0.0/12 block?',
['172.20.1.1','172.32.1.1','172.15.1.1','192.169.1.1'],0,
['The second octet range is 16 through 31.','32 is outside that /12 block.','15 is below the block.','192.169.0.0 is not the private 192.168.0.0/16 block.'],
config='''enable
configure terminal
interface GigabitEthernet0/0
 ip address 192.168.10.1 255.255.255.0
 no shutdown
end
show ip interface brief
show ip route connected''',
config_note='Isolated IOS router R1 Gi0/0 connects to PC-A 192.168.10.10/24, gateway 192.168.10.1. interface selects interface mode; ip address sets address/mask; no shutdown enables it administratively. Expect an up/up connected interface and 192.168.10.0/24 route when the link is operational. Undo by restoring the captured original address; no ip address removes addressing and can disconnect management.')

lesson('1-09',
'A site receives 192.168.10.0/24 but needs separate LANs for 50, 25, and 10 hosts. Giving every LAN a /24 wastes space and overlaps the allocation. Subnetting divides a prefix into nonoverlapping smaller prefixes; VLSM lets their sizes differ.',
'A fixed-length plan uses equal subnet sizes. VLSM uses sizes matched to requirements. More prefix bits mean fewer host bits and smaller subnets. A wildcard mask used by some IOS commands is not the same object as a subnet mask, even when it is its inverse.',
'For ordinary multiaccess IPv4 subnets, usable host count is 2^(32-prefix)-2 because the all-zero and all-one host patterns are network and broadcast. /31 point-to-point and /32 host routes are exceptions. Allocate largest requirements first, round up to a suitable block, align to its boundary, and check for overlap.',
'In the subnetting diagram, treat each block as a range, not a single address. For /26, fourth-octet boundaries are 0,64,128,192. A /27 boundary falls every 32 addresses. The start must be aligned to the size of its block.',
'1. Fifty hosts need 64 total addresses: 192.168.10.0/26, hosts .1-.62, broadcast .63.\n2. Twenty-five need 32: 192.168.10.64/27, hosts .65-.94, broadcast .95.\n3. Ten need 16: 192.168.10.96/28, hosts .97-.110, broadcast .111.\n4. All ranges fit the /24 and do not overlap.\n5. Remaining space starts at .112 but is not automatically one aligned subnet of any desired size.',
'For 192.168.10.77/27, mask is 255.255.255.224, network .64, broadcast .95, usable .65-.94. Verify by ANDing with the mask and by checking the 32-address interval. A gateway consumes a usable address in the same subnet.',
'If a plan overlaps, compare full network-to-broadcast intervals, not only interface addresses. If the host requirement fails, include gateway and infrastructure addresses before choosing a prefix. Recalculate the block rather than choosing the next visually convenient decimal number.',
'Which is the smallest ordinary IPv4 subnet that supports 25 hosts?',
['/27','/28','/26','/30'],0,
['A /27 has 32 total and 30 usable addresses.','A /28 has only 14 usable addresses.','A /26 works but is larger than necessary.','A /30 has only two usable addresses.'],
no_config='This lesson produces and checks an address plan. Interface configuration is demonstrated in IPv4 addressing; no device command performs the reasoning for you.')

lesson('1-10',
'A network needs IPv6 without treating its addresses as impossibly long strings. Break each address into hexadecimal groups, identify its prefix and scope, then ask how the host finds a local neighbor or router.',
'IPv6 has 128-bit addresses and no broadcast. Unicast identifies one interface, multicast reaches a group, and anycast uses a shared unicast address to reach one instance according to routing. Link-local addresses operate on one link; global unicast and unique-local addresses serve different scopes. IPv6 does not automatically encrypt traffic.',
'Leading zeros within a hextet may be omitted. One consecutive zero run may be replaced by ::; using :: twice would make expansion ambiguous. Neighbor Discovery uses ICMPv6 rather than ARP. Router Advertisements support default-router discovery; DHCPv6 is not the ordinary source of a default gateway. Modified EUI-64 is one interface-ID method, not a requirement for every IPv6 host.',
'In the IPv6 diagram, count eight 16-bit hextets before compression. Expand 2001:db8:10::1 to 2001:0db8:0010:0000:0000:0000:0000:0001. Separate the /64 prefix from the interface identifier. 2001:db8::/32 is documentation space, not a production allocation.',
'1. R1 and PC-A share 2001:db8:10::/64.\n2. PC-A learns or is configured with a router and determines the destination path.\n3. It uses Neighbor Discovery to resolve the local next hop.\n4. A router decrements Hop Limit when forwarding.\n5. IPv6 routers do not fragment transit packets; Packet Too Big feedback helps the source adapt.',
'Use show ipv6 interface brief, show ipv6 neighbors, and show ipv6 route on IOS. Verify link-local and global addresses, interface state, neighbor resolution, and the selected prefix. A link-local next hop requires an interface context because the same address can exist on different links.',
'If local IPv6 works but remote IPv6 fails, inspect forwarding enablement, default-router information, and return routes. Do not block all ICMPv6 as a blanket security measure: essential discovery and path-MTU behavior depend on it. Correct the specific policy or addressing issue and retest.',
'Which mechanism replaces IPv4 ARP for IPv6 neighbor resolution?',
['ICMPv6 Neighbor Discovery','DNS AAAA lookup','DHCPv4 DORA','TCP SYN'],0,
['Neighbor Solicitation/Advertisement resolve local IPv6 neighbors.','AAAA records map names to IPv6 addresses, not next-hop MACs.','DHCPv4 is an IPv4 configuration protocol.','A SYN establishes TCP state after network delivery is possible.'],
config='''enable
configure terminal
ipv6 unicast-routing
interface GigabitEthernet0/0
 ipv6 address 2001:DB8:10::1/64
 no shutdown
end
show ipv6 interface brief
show ipv6 neighbors''',
config_note='Isolated IPv6-capable IOS router R1 Gi0/0 faces PC-A 2001:db8:10::10/64. Global ipv6 unicast-routing enables routing; the interface command assigns the documentation-prefix address. Configure the host gateway appropriately. Expect neighbor entries only after traffic/discovery. Restore the baseline for undo; removing ipv6 unicast-routing affects all IPv6 forwarding on the router.')

lesson('1-11',
'A user says the network is down. Before changing a switch, inspect the actual interface the operating system is using. A laptop may have Wi-Fi, Ethernet, a VPN, and virtual adapters with different addresses and routes.',
'An interface address tells where an adapter belongs; a default route tells where unmatched destinations go; DNS settings tell which resolver to ask. A VPN may change routes without changing the physical cable. An address on a disconnected virtual adapter is not proof of active connectivity.',
'Inspect address/prefix, gateway, DNS, link state, and selected route together. DHCP-derived settings can be valid but inappropriate for the intended VLAN. A 169.254.x.x IPv4 link-local address on a DHCP-configured client often indicates failure to obtain a usable DHCP lease; it is not a normal internet-routable assignment.',
'Picture the laptop with separate Ethernet, Wi-Fi, and VPN interfaces. Draw a route-selection decision before choosing the egress adapter. A single default-gateway label on the laptop is too simplistic when several routes or policy mechanisms are active.',
'1. Identify the connected adapter.\n2. Read its address and prefix.\n3. Determine whether the target is local.\n4. Inspect the selected route and gateway.\n5. Test gateway reachability, then remote IP, then name resolution and the intended service. Each test narrows a different hypothesis.',
'Windows: ipconfig /all and route print. Linux: ip address and ip route; DNS inspection depends on the resolver manager, such as resolvectl status where available. macOS: ifconfig and route -n get default, with scutil --dns for resolver configuration. Command availability and adapter names vary; these are inspection commands, not universal output layouts.',
'If IP access works but names fail, test the configured resolver and its response instead of renewing every interface blindly. If only a VPN destination fails, inspect the VPN route and policy. Correct the relevant adapter settings and repeat the same target/service test.',
'A client reaches a server by IP but its hostname does not resolve. What should be investigated first?',
['DNS configuration and resolver response','A mandatory replacement of the Ethernet cable','OSPF cost on every router','The display resolution'],0,
['The difference between the tests is name resolution.','The working IP path argues against immediate cable replacement without further evidence.','Routing cost does not explain the isolated hostname-resolution symptom.','Display settings do not participate in name resolution.'],
no_config='The objective is verifying client IP parameters across operating systems. Configuration commands vary by OS and network manager; this lesson intentionally uses read-only inspection.')

lesson('1-12',
'Wi-Fi shows a strong signal but video calls still break up. Radio strength is only one part of the conversation: clients also share airtime, contend with interference, authenticate, and need a working wired path.',
'SSID is a network name; BSSID identifies a particular basic service set. A channel is a slice of radio spectrum, not a VLAN. Co-channel users share airtime; overlapping-channel interference can disrupt reception. Higher advertised PHY rate is not the same as application throughput.',
'Wireless uses a shared medium with contention and acknowledgments rather than Ethernet CSMA/CD. Walls, distance, noise, channel width, and client capability affect performance. Wider channels can raise peak rates while reducing channel-reuse options. Permitted channels and power depend on regulatory domain; 1/6/11 is a common 20-MHz 2.4-GHz planning example where permitted, not a universal global rule.',
'In the wireless illustration, distinguish coverage overlap from channel overlap. A coverage circle is a planning simplification, not a hard radio boundary. Label clients sharing one channel as sharing airtime; do not draw each as receiving the full advertised rate simultaneously.',
'1. A client discovers an SSID/BSSID.\n2. It associates and completes the configured security process.\n3. It contends for airtime to send data.\n4. The AP bridges or tunnels traffic according to architecture.\n5. DHCP, gateway, DNS, and application services must still work after radio association.',
'Check association, security status, RSSI/SNR where available, retries, channel utilization, and wired uplink state. Compare several clients and locations. A high RSSI with high utilization can still produce poor performance; one signal bar display is not an RF survey.',
'For poor calls only in a crowded room, inspect airtime utilization and retries before increasing transmit power. For clients that associate but get no IP, inspect VLAN/DHCP rather than channel selection. Change one justified RF or wired setting, then compare the same workload and location.',
'Why can strong signal coexist with poor Wi-Fi throughput?',
['The channel may have heavy contention or interference','Strong signal guarantees dedicated airtime','The SSID always defines the channel width','Wireless clients use Ethernet collision detection'],0,
['Signal strength does not remove shared-medium contention or noise.','A strong signal does not reserve the medium for one client.','SSID is a name; channel width is a radio setting.','802.11 does not use Ethernet CSMA/CD.'],
no_config='This lesson explains RF principles. Channel, power, and security configuration depend on controller/AP platform and regulatory domain; the WLAN GUI lessons supply configuration context.')

lesson('1-13',
'A server team wants several isolated applications without buying a physical machine for each. Virtualization separates logical workloads from physical hardware, but those workloads still need CPU, memory, storage, and a real network path.',
'Virtual machines run guest operating systems on a hypervisor. Containers share the host kernel while isolating processes. A VRF separates routing tables on a network device. These solve different isolation problems; a container is not a small VRF, and a VLAN is not a virtual machine.',
'A virtual switch connects virtual NICs and uplinks them to physical networking. Traffic between workloads on one host may remain inside that host. Traffic leaving it depends on uplink VLANs and routing. Cloud moves or shares operational responsibilities; it does not remove the need for address planning, identity, security, or availability design.',
'Place two VMs above a hypervisor and a virtual switch, then connect one physical NIC to an access switch. Draw same-host traffic inside the server and off-host traffic through the uplink. Beside it, draw two VRFs as separate route tables, not separate physical routers.',
'1. VM-A sends to VM-B on the same virtual network.\n2. The virtual switch may deliver locally.\n3. VM-A sends to an off-host service.\n4. The uplink carries the appropriate VLAN traffic.\n5. A router or virtual gateway forwards between subnets. Moving the workload changes placement, not the need for these forwarding decisions.',
'Inspect the guest IP/route, virtual NIC attachment, port group or virtual-network assignment, host uplink, and physical trunk. For VRFs, verify the correct routing-table context. A healthy physical link cannot prove that the VM is attached to the intended virtual network.',
'A VM loses access after migration to another host. Compare virtual-network names, VLAN mapping, uplink allowed lists, and destination-host resources. Correct the inconsistent mapping and test from the guest. Rebooting every physical switch is not a targeted virtualization fix.',
'Which mechanism directly separates IP routing tables on one router?',
['VRF','A container image','A VM snapshot','An Ethernet FCS'],0,
['A VRF supplies a distinct routing context.','A container image packages a workload, not router forwarding tables.','A snapshot captures VM state, not a routing context.','FCS detects frame corruption.'],
no_config='The blueprint asks for virtualization fundamentals. Hypervisor, container, cloud, and VRF setup differ by platform; a generic command sequence would imply unsupported equivalence.')

lesson('1-14',
'A switch receives a frame for a MAC address it has never seen. It cannot ask a routing table which local port owns that MAC. Instead, it learns from source addresses and uses controlled flooding until it has destination information.',
'A MAC table is not an ARP table. The MAC table maps VLAN/MAC to port; ARP maps IPv4 next-hop to MAC. Known unicast forwarding uses a learned port. Unknown unicast flooding stays within the VLAN and eligible forwarding ports. A broadcast is flooded even if the sender is already known.',
'Learning uses the source MAC of an arriving frame, not its destination. Entries age so unused information does not remain forever; exact timers are platform/configuration dependent. If a source moves to a different port, the switch can update its location. If the destination is learned on the same incoming port, the switch normally filters the frame rather than sending it back out that port.',
'In the MAC-learning diagram, read each frame arrow before reading the table update. Annotate VLAN, source MAC, incoming port, and the resulting learned entry. Then look up the destination separately. A table entry is evidence of observed source traffic, not proof the device is still connected.',
'1. Empty VLAN 10 table: A sends to B on Fa0/1.\n2. Learn A on Fa0/1.\n3. B is unknown, so flood through eligible VLAN 10 ports except Fa0/1.\n4. B replies on Fa0/2; learn B there.\n5. Forward the reply only to Fa0/1.\n6. Later A-to-B known unicast goes only toward Fa0/2, not every port.',
'Use show mac address-table dynamic on a supported switch after generating traffic. Inspect VLAN, MAC, type, and port. Compare with show interfaces switchport and show vlan brief. No dynamic entry before traffic is not automatically a fault.',
'If the destination MAC appears on an unexpected uplink, trace the topology before moving cables. Rapid MAC movement can indicate a loop or a legitimate mobility event; correlate timestamps and STP. Correct the actual topology/VLAN problem, then observe stable learning and expected unicast forwarding.',
'An unknown unicast enters Fa0/1 in VLAN 10. Where is it flooded?',
['Eligible forwarding ports in VLAN 10 except Fa0/1','Every port in every VLAN','Only back through Fa0/1','Only the default gateway regardless of topology'],0,
['Flooding respects VLAN and forwarding eligibility and excludes ingress.','VLANs bound the flood domain.','The switch does not reflect the frame back to its ingress port.','Unknown-unicast forwarding is not an automatic gateway-only operation.'],
no_config='MAC learning is automatic on the demonstrated switching ports. VLAN and trunk configuration is taught separately; static MAC entries would obscure the learning exercise.')

lesson('2-02',
'Finance users on two switches need one shared VLAN without a separate interswitch cable for every department. A trunk multiplexes several VLANs on one link while keeping their forwarding identities separate.',
'An access port classifies ordinary untagged endpoint traffic into its access VLAN. A trunk carries multiple VLANs. Native VLAN controls the conventional untagged association; the allowed list controls which VLANs may cross. These settings are independent, and neither creates the VLAN on the peer.',
'For non-native VLANs in this fixture, the switch inserts an 802.1Q tag after the source MAC field. The receiver reads the VLAN ID and forwards within that VLAN. With untagged native behavior, a native-VLAN mismatch can classify untagged traffic differently at each end. Native tagging and some control traffic are platform-dependent exceptions; inspect the actual configuration.',
'In the tag diagram, locate destination MAC, source MAC, tag, original type, and payload. The tag adds four bytes; it does not replace the IP destination. Label a VLAN 10 frame tagged across the trunk and untagged toward an ordinary access host.',
'1. SW1 receives an untagged frame on a VLAN 10 access port.\n2. It associates the frame with VLAN 10.\n3. On the trunk, VLAN 10 is tagged because native is 999.\n4. SW2 reads tag 10 and checks VLAN/forwarding eligibility.\n5. It removes the tag when sending to an ordinary VLAN 10 access host. A trunk is not inter-VLAN routing.',
'Run show interfaces trunk on both switches. Read operational status, native VLAN, allowed VLANs, active VLANs, and forwarding/not-pruned lists separately. show vlan brief verifies VLAN existence but does not list trunk membership like access-port membership.',
'VLAN 10 works but VLAN 20 fails. If VLAN 20 is absent from the allowed list, add it on the relevant trunk rather than recreating all VLANs. If untagged traffic lands in the wrong domain, compare native VLANs. Verify both ends after correction and test the affected VLAN specifically.',
'Both ends trunk successfully, but SW2 permits only VLAN 10. What happens to VLAN 20 traffic across that link?',
['It is not permitted by SW2\'s allowed list','It automatically becomes VLAN 10','It is routed without a router','The native VLAN overrides all filtering'],0,
['Trunk operation does not override per-VLAN restrictions.','Filtering does not translate the VLAN ID into another allowed VLAN.','A trunk preserves Layer 2 separation and does not create routing.','Native classification does not grant every VLAN permission.'],
config='''enable
configure terminal
vlan 10
vlan 20
vlan 999
interface GigabitEthernet0/1
 switchport mode trunk
 switchport trunk native vlan 999
 switchport trunk allowed vlan 10,20,999
 no shutdown
end
show interfaces trunk''',
config_note='Apply on both isolated Catalyst-style switches, Gi0/1 to Gi0/1, with fixed 802.1Q support. vlan creates local VLANs; interface selects the link; mode trunk fixes the role; native vlan sets untagged association; allowed vlan replaces the list. Expect trunking with native 999 and eligible VLANs 10/20/999 after convergence. Restore captured native/list/mode settings for undo; no allowed-list restriction can broaden access.')

lesson('2-03',
'Finance can reach Finance across switches, but not Engineering in another VLAN. That is expected Layer 2 separation. Inter-VLAN routing adds a deliberate Layer 3 path rather than weakening the VLAN boundary.',
'Router-on-a-stick uses tagged router subinterfaces on one physical trunk. A Layer 3 switch can use switched virtual interfaces (SVIs) as gateways and route internally. A Layer 2 management SVI does not by itself enable inter-VLAN routing. The correct choice depends on platform, capacity, and design.',
'A host uses its mask to select the local gateway for a remote subnet. The gateway receives a frame addressed to itself, removes Layer 2 encapsulation, looks up the destination IP, and builds a new frame for the outgoing VLAN. Original IP endpoints normally remain unchanged; TTL decreases. ACLs can control the newly possible routed communication.',
'Use the inter-VLAN illustration to distinguish VLAN-colored access links from the trunk and the routing boundary. The command fixture is R1 Gi0/0 to SW1 Gi0/1, carrying VLANs 10 and 20. PC-A is 192.168.10.10/24 with gateway .10.1; PC-B is 192.168.20.20/24 with gateway .20.1. The shorthand gateways mean 192.168.10.1 and 192.168.20.1.',
'1. PC-A decides PC-B is remote.\n2. It resolves 192.168.10.1 with ARP if needed.\n3. Its frame reaches R1 in VLAN 10.\n4. R1 finds connected 192.168.20.0/24.\n5. R1 resolves PC-B on VLAN 20 and constructs a new frame.\n6. PC-B uses its gateway for the reply. The return route matters as much as the outbound route.',
'On R1 use show ip interface brief and show ip route connected. On SW1 use show interfaces trunk and show vlan brief. Confirm .10 and .20 subinterfaces have the right addresses and encapsulations, then test each local gateway before cross-VLAN reachability.',
'If PC-A reaches its gateway but PC-B cannot, check VLAN 20 access membership, trunk eligibility, encapsulation 20, and PC-B\'s mask/gateway. Correct the evidenced mismatch, then verify both local gateways and the cross-VLAN application. A single successful router-originated ping may not use the same source/path as the client.',
'Which fields normally change when the router forwards between these VLANs without NAT?',
['Ethernet headers and IPv4 TTL/checksum','Original IP source and destination only','The host\'s configured subnet mask','Every TCP port on every hop'],0,
['A new link-layer frame is built, and IPv4 forwarding updates TTL/checksum.','Ordinary routing does not translate the original IP endpoints.','A router does not rewrite a host\'s configured mask in transit.','Transport ports are not rewritten by ordinary routing.'],
config='''enable
configure terminal
interface GigabitEthernet0/0
 no ip address
 no shutdown
interface GigabitEthernet0/0.10
 encapsulation dot1Q 10
 ip address 192.168.10.1 255.255.255.0
interface GigabitEthernet0/0.20
 encapsulation dot1Q 20
 ip address 192.168.20.1 255.255.255.0
end
show ip route connected''',
config_note='IOS router R1 supports 802.1Q subinterfaces. On SW1 create VLANs 10/20 and configure Gi0/1 as a trunk allowing both, with ordinary access ports assigned to the respective VLANs. Parent no ip address avoids overlapping parent addressing; encapsulation selects each VLAN; ip address supplies its gateway. Expect both connected /24 routes when operational. Undo in the isolated lab by shutting the parent and removing only the two exercise subinterfaces, then restoring the baseline.')

lesson('2-04',
'You are remotely supporting a switch and need to know what is plugged into its uplink. Discovery protocols let directly connected devices advertise identity and port information, reducing dependence on an outdated cable spreadsheet.',
'CDP is Cisco-specific; LLDP is standards-based. Both discover local-link neighbors, not all routers across a routed path. Their advertised management address is useful inventory information, not proof that management traffic can reach it. Neither is a substitute for authenticated identity.',
'Advertisements carry information such as device identity, local/remote port, capabilities, and management details. A receiver keeps a neighbor entry until refreshed or aged out. Global and per-interface enablement matter. LLDP transmit and receive can be controlled separately; a device can receive without advertising.',
'Place SW1 Gi0/1 directly opposite SW2 Gi0/2. Draw advertisements in both directions on that one link. Label local interface from the viewing switch\'s perspective and remote port from the neighbor\'s advertisement; these are easy to reverse.',
'1. SW2 advertises its identity and Gi0/2.\n2. SW1 receives it on Gi0/1.\n3. SW1 records local Gi0/1 and remote Gi0/2.\n4. A detail view may reveal SW2\'s management IP.\n5. A separate IP path and security policy are needed to log in to that address.',
'Use show cdp neighbors detail and show lldp neighbors detail on supported IOS switches. Verify local interface, remote port ID, capability, and hold information. An empty table may mean discovery is disabled, unsupported, filtered, or not yet received; it does not alone prove the cable is disconnected.',
'If only one side sees a neighbor, inspect transmit/receive settings and protocol support on both ends. Enable the intended protocol only on trusted infrastructure links according to policy. After correction, wait for an advertisement and confirm the port pair rather than assuming interface numbers match.',
'SW1 receives an advertisement on Gi0/1 that names remote Gi0/2. Which is SW1\'s local port?',
['Gi0/1','Gi0/2','The management IP address','The router ID'],0,
['The receiving interface is the local port.','Gi0/2 is the advertised neighbor port.','An IP address is not the local physical interface identifier.','Router ID is not the receiving port.'],
config='''enable
configure terminal
lldp run
interface GigabitEthernet0/1
 lldp transmit
 lldp receive
end
show lldp neighbors detail''',
config_note='On two isolated LLDP-capable IOS switches, connect SW1 Gi0/1 to SW2 Gi0/2 and enable LLDP on the corresponding peer interface. lldp run is global; transmit/receive are interface settings. CDP uses cdp run globally and cdp enable per interface where supported. Undo by restoring the previous per-interface/global policy; disabling globally affects all links.')

lesson('2-05',
'Two switches need more aggregate capacity and a backup path. Simply adding parallel Layer 2 cables can create a loop or cause STP to block one. EtherChannel groups compatible links into one logical port-channel.',
'LACP active initiates negotiation; passive responds. Active/active and active/passive can form a bundle; passive/passive does not initiate one. Static on mode is not LACP. A Layer 2 port-channel carries switched traffic; a Layer 3 port-channel is a routed interface.',
'Member links must have compatible operational and configuration attributes. The switch distributes flows using a hash over selected header fields; one ordinary flow generally stays on one member rather than gaining the sum of all link speeds. STP treats a Layer 2 bundle as one logical link. Losing one member can reduce capacity without taking the whole channel down.',
'Picture two physical links between SW1 and SW2 inside a single bracket labeled Port-channel1. Draw two different flows assigned to different members, then one flow staying on one member. The bracket represents a logical link, not a packet split into arbitrary alternating bytes.',
'1. Compatible ports exchange LACP information.\n2. Selected members join the logical channel.\n3. A flow hashes to one member.\n4. Another flow may use the other.\n5. When a member fails, surviving capacity and hash assignments change. Minimum-link policies can affect whether the channel stays up.',
'Use show etherchannel summary and show lacp neighbor. Read the output legend: common IOS flags include S for Layer 2, U for in use, and P for bundled member. A physical up interface that is suspended or standalone is not a healthy bundled member. Verify the logical trunk with show interfaces trunk.',
'If both ends are passive, change one intended end to active. If a member is suspended, compare speed, duplex, trunk mode, native/allowed VLANs, and group membership. Correct consistency before forcing static on. Verify bundled members and traffic after the change.',
'Why might one TCP transfer not double its rate across a two-link EtherChannel?',
['A flow is normally assigned to one member by hashing','LACP encrypts half the traffic','Every second packet is always discarded','STP must block every member individually'],0,
['Aggregate capacity serves multiple flows; a single flow generally uses one member.','LACP negotiates aggregation, not encryption.','Discarding alternate packets is not normal aggregation behavior.','STP treats a correctly formed Layer 2 channel as a logical link.'],
config='''enable
configure terminal
interface range GigabitEthernet0/1 - 2
 switchport mode trunk
 switchport trunk allowed vlan 10,20
 channel-group 1 mode active
 no shutdown
interface Port-channel1
 switchport mode trunk
 switchport trunk allowed vlan 10,20
end
show etherchannel summary
show lacp neighbor''',
config_note='Two isolated Catalyst-style switches with compatible Gi0/1-2 connected as pairs; VLANs 10/20 already exist on both. Apply compatible configuration on both ends. channel-group creates/selects the logical bundle using LACP; Port-channel1 holds logical interface policy. Restore baseline safely by shutting members before removing channel-group 1 and the unused port-channel; unbundling live parallel links can create a loop. Layer 3 EtherChannel uses no switchport on supported platforms and addresses the port-channel, not each member; it requires its own routed fixture.')

lesson('2-06',
'A triangle of switches has redundant cables. Ethernet broadcasts have no IP-style hop limit at Layer 2, so uncontrolled forwarding around the triangle can circulate frames and destabilize MAC learning. Spanning Tree preserves redundancy while removing active forwarding loops.',
'The root bridge is elected by lowest bridge ID, not highest performance. Root ports lead non-root switches toward the root; designated ports serve segments; alternate ports provide alternate paths and discard ordinary data. Rapid PVST+ maintains a rapid spanning-tree instance per VLAN on supported Cisco switches.',
'BPDUs carry topology/election information. Priority and the extended system ID participate in bridge identity; MAC address breaks an otherwise equal comparison. Path cost and subsequent tie-breakers determine port roles. Rapid states are discarding, learning, and forwarding. PortFast accelerates edge-port transition; it does not disable STP. BPDU guard protects intended edge ports; root guard, loop guard, and BPDU filtering have distinct purposes and must not be treated as interchangeable.',
'In the STP diagram, identify the root first, then each non-root root port, then designated/alternate roles. A blocked or discarding data path can still process BPDUs. Draw BPDU information separately from ordinary user-frame forwarding.',
'1. Switches initially advertise their view of the root.\n2. Superior BPDU information propagates.\n3. Switches select root paths and segment roles.\n4. A redundant path discards ordinary data.\n5. On an approved link failure, the topology can reconverge. Timing depends on protocol, link type, and configuration; do not promise a universal failover time.',
'Use show spanning-tree vlan 10 and inspect Root ID, Bridge ID, root cost, port role, and state. On the root, Root ID and Bridge ID identify the same bridge. On an edge port, verify PortFast and guard policy using supported detail commands. A discarding alternate port is often healthy redundancy.',
'If an unexpected access switch becomes root, inspect bridge priorities and topology before changing them. If BPDU guard err-disables a port, investigate whether a switch was attached to an intended edge port. Remove the cause before recovery; disabling protection merely to make the port green can restore a loop.',
'An alternate port discards ordinary data in a redundant triangle. What is the best initial interpretation?',
['It may be normal loop prevention','It must have a broken cable','STP has encrypted the port','It cannot receive BPDUs'],0,
['An alternate discarding path can be the intended loop-free topology.','A role/state does not alone prove physical failure.','STP controls forwarding topology, not encryption.','Discarding ordinary data does not mean ignoring BPDUs.'],
config='''enable
configure terminal
interface FastEthernet0/10
 switchport mode access
 switchport access vlan 10
 spanning-tree portfast
 spanning-tree bpduguard enable
end
show spanning-tree vlan 10''',
config_note='Isolated Rapid PVST+-capable switch with VLAN 10 and Fa0/10 connected only to an ordinary endpoint. PortFast is an edge-port setting; BPDU guard protects that assumption. Do not apply this recipe to interswitch uplinks. Undo the exercise with no spanning-tree portfast and no spanning-tree bpduguard enable on that interface, restoring the prior access policy. Root/guard design requires topology-wide review.')

lesson('2-07',
'A branch AP can be online while its controller connection is unavailable. Whether users can still pass traffic depends on architecture and switching mode, not simply whether the AP has a radio.',
'Autonomous APs operate with local configuration; controller-based APs coordinate with a WLC. Central switching tunnels client traffic toward the controller; FlexConnect local switching can bridge selected WLAN traffic at the branch. AP modes such as monitor or sniffer have different roles from ordinary client service.',
'CAPWAP separates control and data relationships between AP and controller. The AP management network must reach the controller, but client VLANs depend on where traffic is switched. A centrally switched AP uplink may not need every client VLAN locally; a locally switched multi-VLAN AP may require a trunk. Controller and switch LAG support depends on platform and design.',
'In the wireless illustration, draw AP-to-controller control separately from client-data flow. For central switching, trace client data through the tunnel; for local switching, stop the tunnel assumption at the AP and trace the local VLAN. Do not draw all WLANs as having one universal data path.',
'1. AP obtains management connectivity and discovers/joins its controller.\n2. The controller supplies supported WLAN policy.\n3. A client associates and authenticates.\n4. Data is centrally or locally switched according to the WLAN/AP design.\n5. During WAN failure, behavior depends on configured local switching/authentication and platform capabilities.',
'Check AP join state, management reachability, time/certificate evidence where relevant, WLAN policy, and actual switching mode. Inspect the switch uplink VLANs appropriate to that mode. A joined AP does not prove the client VLAN has DHCP or routing.',
'Clients authenticate but cannot get an address on a locally switched WLAN. Inspect AP VLAN mapping and the local switch trunk before troubleshooting the central controller data path. If the AP cannot join, inspect discovery, routing, filtering, and platform logs instead. Verify the specific failed phase after correction.',
'Which setting determines whether a client VLAN must be carried locally on an AP uplink?',
['The WLAN/AP switching design','The SSID text alone','The AP\'s paint color','A universal requirement that every AP port be a trunk'],0,
['Local versus central switching changes the wired VLAN requirement.','An SSID name does not specify the data path.','Appearance has no forwarding role.','AP port mode depends on architecture and VLAN needs.'],
no_config='This lesson compares architectures and AP modes. AireOS and IOS XE controller configuration models differ; the next WLAN GUI lesson provides a platform-aware interpretation workflow rather than invented universal commands.')

lesson('2-08',
'A WLAN is visible but clients cannot use the network. A successful SSID broadcast only proves advertisement; the GUI must also bind the WLAN to the right security, policy, VLAN, and AP scope.',
'An SSID is what clients see; an internal WLAN/profile name is an administrative object. A client VLAN is a forwarding association, not the SSID itself. AireOS uses WLAN/interface concepts, while Catalyst 9800 IOS XE uses WLAN profiles, policy profiles, and tags. Menu names and bindings are not interchangeable.',
'For WPA2-Personal, clients and infrastructure must agree on PSK and compatible cipher settings. Authentication, association, IP assignment, and application access are separate stages. QoS profiles classify treatment; they do not manufacture bandwidth. An enabled WLAN profile may still not be applied to the intended APs.',
'Use a dependency chain: WLAN/SSID, security, policy/VLAN mapping, AP assignment, client state. This is a conceptual GUI map, not a screenshot of a specific controller release. Follow the binding arrows before looking at the final enabled toggle.',
'1. Create or inspect the intended WLAN object.\n2. Verify security and cipher compatibility.\n3. Bind the policy to the client VLAN.\n4. Apply it to the intended AP scope.\n5. Test one client through authentication, DHCP, gateway, DNS, and application access. A saved GUI form is not a successful client test.',
'Inspect client detail and event history. Distinguish association/authentication failures from DHCP failures. Check WLAN administrative state, AP assignment, security, policy profile/interface, VLAN availability, and DHCP path. Record controller family/release before comparing screenshots or instructions.',
'If authentication fails, compare PSK/security settings and client compatibility. If authentication succeeds but no IP is assigned, inspect policy/VLAN and DHCP. Correct one layer, reconnect a test client, and verify the full intended service. Do not lower WLAN security as a generic connectivity fix.',
'A client authenticates successfully but receives no DHCP lease. What is the next focused check?',
['Client VLAN/policy mapping and DHCP path','Assume the PSK must be wrong','Disable encryption permanently','Change the SSID spelling without evidence'],0,
['The failure is after authentication and before usable IP configuration.','Successful authentication argues against the stated PSK hypothesis.','Removing encryption is not a justified DHCP repair.','SSID spelling does not directly repair a wrong VLAN or DHCP path.'],
config='GUI WORKFLOW: WLAN object -> security -> policy/VLAN -> AP assignment -> enable -> client test',
config_note='This is a platform-neutral GUI workflow, not IOS CLI and not an executable terminal command. In an isolated controller lab, select a WPA2-Personal/AES WLAN, supply a unique lab PSK through the GUI, map to an existing client VLAN, and bind to the intended APs. Exact screens depend on controller family/release. Undo by disabling/removing only the lab WLAN and restoring its prior bindings; do not delete a shared policy profile.')

lesson('2-09',
'You need to manage a switch without standing beside it. Remote access needs both an IP path and a secure management service. A working ping does not prove SSH is enabled, and SSH being enabled does not make every user authorized.',
'Console is local/out-of-band access. SSH encrypts remote terminal traffic; Telnet does not. HTTPS protects web management transport; HTTP does not. TACACS+ and RADIUS support AAA, not the terminal transport itself. TFTP/FTP move files and should not be confused with an interactive management session.',
'On IOS, VTY lines control remote terminal sessions. Local user authentication, host keys, SSH version, and transport restrictions work together. A Layer 2 switch also needs a reachable management SVI and a default gateway for off-subnet management; a routing-enabled device uses its routing table instead. FTP/TFTP have different transfer behavior and security properties; prefer protected alternatives when supported.',
'Place admin PC 192.168.10.10/24 beside R1 Gi0/0 192.168.10.1/24. Trace IP reachability first, then SSH host identity and user authentication. The console path is separate so a bad remote-access change does not remove recovery access.',
'1. Admin reaches the management IP.\n2. Client and device negotiate SSH and verify host identity.\n3. User authenticates.\n4. Authorization determines permitted work.\n5. Accounting can record activity. Accepting an unexpected host-key change without investigation defeats an important identity check.',
'Use show ip ssh and show users on IOS; inspect VTY transport and authentication configuration through an authorized console. Test a new SSH session before closing the recovery session. For transfers, verify destination, permissions, and integrity rather than assuming a successful TCP connection proves a correct backup.',
'If SSH times out, check IP path, management ACL, and service. If it reaches authentication but fails, inspect user/method configuration. If commands are denied after login, inspect authorization. Correct the relevant layer and verify with the intended account; do not enable Telnet to hide an SSH configuration error.',
'An SSH login succeeds but a command is denied. Which area is most directly implicated?',
['Authorization','Copper pair ordering alone','DNS recursion alone','A mandatory TCP handshake failure'],0,
['The identity was accepted, but the requested action is not permitted.','A working session does not support pair ordering as the direct cause of command denial.','DNS is not the permission decision.','A completed SSH login implies transport establishment occurred.'],
config='''enable
configure terminal
hostname R1
ip domain name academy.example
username learner privilege 1 secret LabOnly-ChangeMe-2026
crypto key generate rsa modulus 2048
ip ssh version 2
line vty 0 4
 login local
 transport input ssh
end
show ip ssh''',
config_note='Isolated IOS router with crypto support, reachable Gi0/0 192.168.10.1/24, and console recovery. The password is an explicitly disposable lab example, never a production secret. hostname/domain support key identity; RSA creates host keys; login local selects the user database; transport restricts VTY 0-4. Review any additional VTY ranges on the actual device. Key-generation syntax and supported algorithms vary. Restore the captured VTY/user baseline for undo; do not zeroize shared keys blindly.')

lesson('3-01',
'A router has several routes that seem to match one packet. The key is to separate building the routing table from forwarding a packet through that table. The most trusted route source is not always the most specific forwarding match.',
'Longest prefix match chooses the most specific installed matching prefix. Administrative distance helps choose between route sources for the same prefix. A routing protocol metric compares paths within that protocol. These are different decisions, not three interchangeable numbers.',
'A routing entry identifies prefix, source code, next hop or directly connected interface, and often administrative distance/metric. A default route /0 matches anything but loses to a more specific matching installed route. Connected networks require operational interfaces. The forwarding implementation may use a FIB derived from the routing table rather than perform a text-table search per packet.',
'In the routing diagram, separate destination IP lookup from next-hop MAC resolution. Label each routed link with a different Layer 2 header. The destination IP remains the remote endpoint without NAT; the Ethernet destination becomes the next hop on each link.',
'1. Installed routes include 10.0.0.0/8 via A, 10.1.0.0/16 via B, and a default via C.\n2. Packet destination 10.1.2.3 matches all three.\n3. /16 is the longest match, so choose B.\n4. Resolve B on the outgoing link if needed.\n5. Construct the new frame and decrement TTL. Do not compare the /8 administrative distance against the /16 to override specificity.',
'Use show ip route and show ip route 10.1.2.3. Read the route source, selected prefix, next hop, outgoing interface, and bracketed distance/metric where shown. Verify next-hop reachability and return routing. A default gateway configured for a Layer 2 switch is not a substitute for routes on a routing-enabled router.',
'If traffic takes the default despite an intended specific route, check whether the specific route is actually installed and its next hop resolvable. If the route exists but traffic fails, inspect ARP, interface, ACL, and return path. Correct the evidenced issue and repeat the same destination lookup and end-to-end test.',
'Installed /8 and /16 routes both match 10.1.2.3. Which is used by default forwarding?',
['The /16 route','Always the route with the lower administrative distance regardless of prefix','Always the /8 route','Always the default route'],0,
['The /16 is the longest matching installed prefix.','Distance selects route sources for a prefix, not a less-specific forwarding match over a more-specific one.','The /8 is less specific.','A default loses to either more-specific matching route.'],
no_config='This lesson interprets routing and forwarding. Static and OSPF lessons provide the route-creation commands; adding an unrelated route would obscure the lookup exercise.')

lesson('3-02',
'A small branch has one path to head office. Rather than run a dynamic protocol, you can explicitly tell its router which next hop reaches the remote prefix. A static route is that instruction; it still needs a reachable next hop and a return path.',
'A network route covers a prefix, a host route covers one address (/32 IPv4 or /128 IPv6), and a default covers unmatched destinations. A floating static has a higher administrative distance than the preferred route to the same prefix. It is not necessarily used merely because an application fails.',
'Next-hop routes depend on recursive resolution to an outgoing path. A link-local IPv6 next hop requires the outgoing interface to disambiguate scope. A floating route can remain unused while a failed upstream path still leaves the preferred route installed; tracking is a separate mechanism. Never describe administrative distance as a reachability probe.',
'In the routing diagram, identify R1, R2, the transit subnet, and the remote LAN. The exact fixture here is R1 Gi0/0 10.0.12.1/30 to R2 10.0.12.2/30, with remote LAN 192.168.20.0/24 behind R2. Client LAN 192.168.10.0/24 is behind R1.',
'1. R1 receives a packet for 192.168.20.20.\n2. It matches the installed /24 static via 10.0.12.2.\n3. R1 resolves R2\'s MAC and forwards.\n4. R2 delivers to its connected LAN.\n5. R2 needs a route back to 192.168.10.0/24. One-way routing is not a complete conversation.',
'Use show ip route static, show ip route 192.168.20.20, and show ip interface brief. For IPv6 use show ipv6 route static. Confirm the route is installed, not just present in running configuration. Test from a source that exercises the intended return path.',
'If the static is configured but absent from the table, check next-hop resolution and interface state. If outbound packets arrive but replies fail, inspect the remote return route and policy. Correct the specific route, verify installation, then retest from the original client subnet.',
'A floating static has distance 200 while OSPF installs the same prefix at distance 110. Which is preferred?',
['OSPF while its route remains installed','The floating static because 200 is larger','Both because distance is a load-sharing weight','Neither because static routes cannot have a distance'],0,
['Lower administrative distance is preferred for competing sources to the same prefix.','A larger distance makes this static less preferred.','Distance is not a proportional load-sharing weight.','IOS supports a configured administrative distance on static routes.'],
config='''enable
configure terminal
ip route 192.168.20.0 255.255.255.0 10.0.12.2
end
show ip route static
show ip route 192.168.20.20''',
config_note='IOS R1 uses the transit and LAN fixture above; R2 needs ip route 192.168.10.0 255.255.255.0 10.0.12.1 in global configuration. The mask identifies the destination prefix and the final address is the next hop. Undo with no followed by the exact exercise route. A default uses 0.0.0.0 0.0.0.0; an IPv4 host route uses mask 255.255.255.255. IPv6 syntax is ipv6 route PREFIX/LENGTH NEXT-HOP; use an explicit interface with a link-local next hop. These variants need their own address/return-path checks.')

lesson('3-04',
'Hosts can have a perfectly valid default gateway address and still lose remote access when that one router fails. First-hop redundancy gives hosts a stable gateway identity while routers coordinate who forwards for it.',
'HSRP uses active/standby roles; VRRP elects responsibility for a virtual router; GLBP can distribute gateway forwarding among members on supported platforms. These are first-hop mechanisms, not replacements for OSPF or a complete WAN redundancy design. Version and platform affect details and defaults.',
'The host uses a virtual gateway IP and associated virtual MAC. Routers exchange protocol information and select the forwarding role. A surviving router can take over the virtual identity after failure detection. Upstream reachability still matters: an active router with a failed WAN can remain active unless tracking/design causes a change.',
'In the FHRP diagram, distinguish each router\'s real interface address from the shared virtual gateway. Draw host traffic toward the virtual MAC and then through the current forwarder. A role change moves forwarding responsibility; it does not rewrite the host\'s configured gateway to a new physical address.',
'1. Host 192.168.10.10/24 uses virtual gateway 192.168.10.1.\n2. R1 and R2 have distinct real addresses in the subnet.\n3. The current forwarder answers for the virtual identity.\n4. After a detected failure, the surviving eligible router assumes responsibility.\n5. Routing beyond it must still deliver the packet and return traffic.',
'For an HSRP lab, show standby brief identifies group, state, virtual IP, and peer roles. For VRRP use platform-supported show vrrp. Compare both peers, host ARP, and upstream routes. Do not infer a universal timer or preemption default across protocols/versions.',
'Hosts lose remote access while the active router\'s LAN interface remains up. Inspect upstream reachability and tracking rather than assuming the peer must detect every remote outage. Correct the tracked dependency or routing design, then test a controlled failure and recovery with application traffic.',
'What should a host normally use as its default gateway in the demonstrated FHRP design?',
['The virtual gateway IP','Both routers\' MAC addresses typed as IPs','The OSPF router ID','The DNS server regardless of role'],0,
['The stable virtual IP is backed by the participating routers.','MAC addresses are not gateway IP configuration values.','An OSPF ID is a routing-protocol identity, not necessarily a gateway address.','A DNS server is not a router merely because it resolves names.'],
no_config='Objective 3.5 is conceptual. HSRP/VRRP/GLBP configurations and defaults are version-specific; this lesson teaches purpose, roles, and verification rather than pretending one recipe configures all three.')

lesson('4-01',
'A new laptop joins a VLAN with no manually entered address. It needs an address, mask, gateway, and resolver before a hostname-based application can work. DHCP supplies configuration; DNS answers naming questions after usable network delivery exists.',
'DHCP leases configuration; DNS resolves records. A DHCP relay forwards between a client broadcast domain and a remote server. A recursive DNS resolver may answer from cache or query other servers. Neither protocol replaces routing, and DHCP does not guarantee that the supplied DNS server is reachable.',
'In an initial DHCPv4 allocation, Discover, Offer, Request, and ACK establish a lease. The initial selecting Request is broadcast so servers can see the selection; not every DHCP message in every state is broadcast. Renewals may be unicast. A relay supplies information such as giaddr so the server can select the client subnet. DNS can use UDP or TCP; a hostname lookup is not always one UDP packet.',
'In the DORA diagram, distinguish client broadcast from relay-to-server delivery. Put DNS resolution after usable addressing in this scenario, then show cache hit and cache miss as alternative branches. ARP for the DNS next hop may be required before sending a DNS query; it is not reserved for after DNS completes.',
'1. Client broadcasts Discover on VLAN 10.\n2. R1 relays toward DHCP server 192.0.2.10.\n3. Server offers a VLAN 10 lease.\n4. Client requests the selected offer and receives ACK.\n5. Client checks DNS cache for portal.academy.example.\n6. On a miss, it routes/resolves the next hop to its configured resolver and sends a query.\n7. The resolver response supplies records; the application then selects an address and transport.',
'Inspect client lease details and resolver settings. On IOS relay R1 inspect show running-config interface GigabitEthernet0/0 and routes to the server. On a client use nslookup portal.academy.example against the intended resolver. A valid lease proves configuration delivery, not DNS or application success.',
'If Discover repeats with no Offer, inspect VLAN, relay, server scope, routing, and snooping policy. If an address is assigned but names fail, inspect DNS separately. Correct the evidenced relay/scope/resolver issue, renew only the test client if appropriate, and retest the original hostname/application.',
'A client has a valid lease and can reach a server by IP but not by name. Which service is the focused next check?',
['DNS','Re-run every DHCP allocation indefinitely','Change all VLAN IDs','Disable ARP'],0,
['Name resolution is the distinguishing failed function.','A working lease does not justify repeatedly restarting allocation.','Changing VLANs can disrupt a working IP path without addressing the resolver.','ARP supports local delivery and disabling it is not a DNS repair.'],
config='''enable
configure terminal
interface GigabitEthernet0/0
 ip address 192.168.10.1 255.255.255.0
 ip helper-address 192.0.2.10
 no shutdown
end
show running-config interface GigabitEthernet0/0
show ip route 192.0.2.10''',
config_note='Isolated IOS R1 Gi0/0 faces VLAN 10 clients. A preconfigured routed path reaches DHCP server 192.0.2.10, which has a 192.168.10.0/24 scope and a return route. helper-address is placed on the client-facing interface. It does not create a server pool. Undo with no ip helper-address 192.0.2.10 on that interface and restore the prior address. DHCP-client configuration is interface ip address dhcp on a separate intended uplink; do not combine it with this statically addressed client gateway.')

lesson('4-02',
'A private-addressed branch needs to communicate with an external network that does not route its internal addresses. NAT changes selected address fields at a boundary; routing still chooses paths and security policy still decides what is permitted.',
'Static NAT is a fixed one-to-one mapping. Dynamic NAT allocates one-to-one mappings from a pool. PAT/overload lets multiple flows share an address by distinguishing transport ports or other identifiers. NAT is not encryption and should not be taught as a complete firewall.',
'Inside local is the inside host address as seen internally; inside global is its translated external representation. Outside local/global describe the outside host from the respective views. For basic inside-source translation, an outbound source is rewritten and return traffic is translated back. Checksums must reflect changed fields. This is the explicit exception to ordinary routing preserving IP endpoints.',
'In the NAT diagram, label the boundary and show the packet immediately before and after translation. Keep original host identity separate from the translated representation. For PAT, include ports in the mapping; a single IP-only arrow cannot explain flow separation.',
'1. Inside host 192.168.10.10 sends toward an external lab server.\n2. R1 matches its static mapping to 198.51.100.10.\n3. The outbound source becomes 198.51.100.10.\n4. The external network routes replies to that address.\n5. R1 translates the reply destination back to 192.168.10.10.\n6. Layer 2 headers are independently rebuilt on each routed link.',
'Use show ip nat translations and show ip nat statistics on a NAT-capable IOS router. Inspect inside local/global, protocol/ports where relevant, interface roles, and counters. A static mapping can exist before traffic. For dynamic NAT, generate matching traffic and verify pool availability and routes.',
'If no dynamic translation appears, check inside/outside roles, matching ACL, route, and pool availability. If a translation exists but replies fail, inspect external routing to the global address and return policy. Correct the specific issue and retest the same inside-originated flow. Clearing all translations can disrupt unrelated sessions.',
'Which is the inside global address in the stated static mapping?',
['198.51.100.10','192.168.10.10','The outside server MAC','The VLAN ID'],0,
['It represents the inside host to the outside network.','That is the inside local address.','A MAC address is not a NAT inside-global IP.','VLAN identity is a Layer 2 classification, not the translated IP.'],
config='''enable
configure terminal
interface GigabitEthernet0/0
 ip nat inside
interface GigabitEthernet0/1
 ip nat outside
exit
ip nat inside source static 192.168.10.10 198.51.100.10
end
show ip nat translations
show ip nat statistics''',
config_note='NAT-capable IOS router with already addressed/routed Gi0/0 inside 192.168.10.0/24 and Gi0/1 outside. The external lab must route 198.51.100.10 to R1; documentation addresses are not public production allocations. Interface commands mark roles; the global static command maps local to global. Undo the exact static mapping with no ip nat inside source static 192.168.10.10 198.51.100.10, then restore interface roles only if unused by other rules. Dynamic pools and overload are separate variants, retained in technical expansion, not combined blindly with this static fixture.')

lesson('4-03',
'Three routers report the same outage at different times, and monitoring shows a spike without an explanation. Operations needs a shared clock, measurable counters, and event messages. NTP, SNMP, and syslog supply different pieces of that evidence.',
'NTP synchronizes time; SNMP reads/manages structured objects and can notify; syslog sends event messages with facilities and severity. A syslog severity number is not an SNMP OID. Lower syslog severity numbers mean more severe events, from emergency 0 through debugging 7.',
'NTP clients evaluate time sources; stratum describes distance from a reference, not a guarantee of accuracy or trust. SNMP managers query agents using MIB object identifiers; counters often require time deltas to become rates. SNMPv3 security capabilities differ from community-based versions. Syslog delivery and retention depend on transport, filtering, and collector configuration.',
'Place the network device in the center, with separate arrows to a time server, monitoring manager, and log collector. Label what travels on each arrow: time exchanges, object queries/notifications, and event records. Shared IP connectivity does not make these one protocol.',
'1. R1 exchanges time information with its configured source.\n2. A manager polls an interface counter twice and calculates a delta over elapsed time.\n3. A link-state change produces a syslog message if enabled and within the threshold.\n4. The collector timestamps/correlates evidence.\n5. A missing message can reflect filtering or delivery failure, not absence of an event.',
'Use show clock detail, show ntp associations, show ntp status, and show logging on supported IOS. Inspect synchronization state and selected source, not only the displayed clock. On the collector verify source identity, severity threshold, receipt time, and retention. Avoid exposing credentials or SNMP communities in shared screenshots.',
'If NTP remains unsynchronized, check reachability, UDP policy, source validity, and authentication configuration where used. If warning logs are missing with a threshold of errors, review the intended threshold rather than increasing every device to debug. Correct the filter/path and generate a safe test event.',
'A syslog threshold includes levels 0 through 4. Which event is excluded?',
['Informational level 6','Emergency level 0','Critical level 2','Warning level 4'],0,
['6 is numerically above the configured threshold.','0 is included and is the most severe level.','2 is included.','4 is included at the threshold.'],
config='''enable
configure terminal
ntp server 192.0.2.123
logging host 192.0.2.50
logging trap warnings
end
show ntp associations
show ntp status
show logging''',
config_note='Isolated IOS R1 already has routed access to an authorized NTP server 192.0.2.123 and syslog collector 192.0.2.50. ntp server configures R1 as a client; logging host selects the collector; warnings includes severity 0-4. This does not configure SNMP or make R1 an authoritative clock. NTP serving/master behavior and authentication need platform-specific design. Undo with no ntp server 192.0.2.123 and no logging host 192.0.2.50, restoring the original threshold.')

lesson('4-04',
'A backup transfer fills a WAN uplink and voice becomes choppy. QoS cannot create more bandwidth, but it can classify traffic and decide how limited forwarding resources are shared when congestion occurs.',
'Classification identifies traffic; marking labels it; queuing schedules it. Policing drops or remarks excess traffic according to policy; shaping buffers and delays traffic to fit a rate. A DSCP mark is a request for treatment, not a guarantee that every network honors it.',
'Congestion occurs when offered traffic exceeds a resource\'s service capacity. Delay, jitter, and loss affect applications differently. Priority treatment must be bounded so other traffic is not starved. Trust boundaries determine whose markings are accepted; accepting every endpoint\'s highest-priority mark defeats policy.',
'In the QoS illustration, draw several input classes entering queues before one constrained egress. Label classification before marking and scheduling. Draw shaping as buffering over time, not as a faster link. Queue length represents waiting data, not extra capacity.',
'1. Voice and backup packets reach a constrained egress.\n2. Policy classifies them and applies or trusts markings.\n3. Packets enter appropriate queues.\n4. The scheduler serves them according to policy.\n5. Excess traffic may be delayed, dropped, or remarked depending on shaping/policing. Without congestion, queue priority may have little visible effect.',
'On supported IOS platforms use show policy-map interface to inspect applied direction, class matches, offered rate, drops, and queue behavior. Compare deltas during a controlled workload. A policy existing in configuration is not proof it is attached to the correct interface/direction.',
'If voice remains poor, verify classification counters, trust boundary, policy attachment, and actual bottleneck. If shaping is set above the provider\'s real service rate, congestion can occur beyond your controlled queue. Correct the rate/classification based on the service contract and retest voice plus background traffic.',
'Which action best describes shaping?',
['Buffering/delaying traffic to fit a rate','Creating additional physical bandwidth','Encrypting all priority packets','Always dropping every packet above an instantaneous rate'],0,
['Shaping smooths traffic using buffering and scheduling.','QoS does not create physical capacity.','Encryption is a separate function.','Dropping/remarking excess is characteristic of policing, not the defining shaping behavior.'],
no_config='Objective 4.7 explains per-hop behavior. Scheduler, queue, and policy syntax vary by platform; this lesson interprets mechanisms and counters instead of applying an unvalidated universal WAN policy.')

lesson('5-01',
'A staff member receives a convincing password-reset message. The network can be fully operational while an attacker exploits a human or software weakness. Security starts by identifying what can go wrong and which control reduces that risk.',
'A threat is a potential cause of harm; a vulnerability is a weakness; an exploit uses a weakness; mitigation reduces risk. Confidentiality protects against unauthorized disclosure, integrity against unauthorized alteration, and availability against disruption. These goals can require different controls.',
'Defense in depth combines technical, administrative, and physical controls. Least privilege limits permitted actions. Awareness training helps users recognize suspicious requests, while patching and access controls reduce technical exposure. A control can reduce likelihood or impact without eliminating every threat.',
'Trace a defensive chain: suspicious message, user decision, identity verification, access control, detection, response. Mark where each control interrupts or detects the chain. This is a risk model, not an attack execution guide.',
'1. A user receives an unexpected credential request.\n2. The user verifies through an established channel rather than the message link.\n3. Identity controls can prevent unauthorized login.\n4. Monitoring can detect unusual access.\n5. The response process contains and investigates the event. No single control guarantees prevention.',
'Check whether controls are actually deployed: patch inventory, access reviews, approved training records, physical access logs, and tested response procedures. A policy document alone is not evidence that systems enforce it. Use authorized audits and avoid collecting unnecessary personal data.',
'If suspicious access is reported, preserve relevant evidence and follow the organization\'s incident process. Revoke exposed sessions/credentials through approved channels and verify recovery. Do not delete logs or improvise broad production changes that destroy evidence.',
'An unpatched software flaw is best classified as what?',
['A vulnerability','A guarantee of an active exploit','An availability metric','A completed mitigation'],0,
['It is a weakness that could be exploited.','A weakness does not prove exploitation occurred.','It is not a service-availability measurement.','Leaving the flaw unpatched is not a mitigation.'],
no_config='Security concepts and program elements are not one IOS feature. Configuration belongs to the specific access-control, ACL, Layer 2 security, and wireless lessons.')

lesson('5-02',
'An engineer must log in to a router, but should not automatically gain every command. Device access separates proving identity from deciding permissions and recording actions.',
'Authentication asks who you are; authorization asks what you may do; accounting records activity. A local user database is different from centralized TACACS+/RADIUS. A password policy, MFA, certificates, and biometrics address identity assurance in different ways; a long password alone does not replace every other control.',
'IOS EXEC prompts reflect access mode: > is User EXEC and # is Privileged EXEC. configure terminal enters global configuration; interface and router commands enter more specific contexts. Local secrets protect stored credentials better than reversible obfuscation, but strength and algorithms depend on release. service password-encryption is not strong protection for secrets.',
'Place three checkpoints along a management session: identity, permission, audit record. Keep the encrypted SSH transport around the conversation as a separate layer. A secure transport does not automatically grant the correct privilege.',
'1. A user connects over an allowed management path.\n2. The configured authentication method validates identity.\n3. Authorization limits commands/roles.\n4. Accounting records supported actions.\n5. A centralized server timeout may invoke a configured fallback; an explicit rejection is not the same as an unreachable server.',
'Use authorized console access to inspect local users, enable secret, line authentication, and configured AAA method lists. Test with a low-privilege account and a separate recovery path. show users identifies sessions but does not prove that every action is being accounted for.',
'If login fails after an AAA change, distinguish server reachability from credential rejection. Do not remove all authentication on a remotely managed device. Restore the approved method list via recovery access, verify both authorized and unauthorized test cases, and preserve accounting evidence.',
'A system accepts identity but blocks configuration commands. Which AAA function is making that decision?',
['Authorization','Authentication only','Accounting only','Ethernet learning'],0,
['Authorization determines permitted actions.','Authentication validates identity, not all subsequent permissions.','Accounting records activity rather than granting the command.','MAC learning is unrelated to management permissions.'],
config='''enable
configure terminal
username learner privilege 1 secret LabOnly-ChangeMe-2026
line console 0
 login local
end
show users''',
config_note='Isolated IOS lab with existing privileged recovery access and no conflicting AAA method-list policy. The disposable password is not a production credential. username creates a local low-privilege account; line console selects console configuration; login local uses that database. This does not configure SSH or centralized AAA. Before closing recovery access, test the intended login and privilege behavior. Undo by restoring the prior console authentication and removing only the lab user; never leave a production console unauthenticated.')

lesson('5-03',
'A server should accept HTTPS from Finance but not arbitrary traffic from that subnet. An ACL evaluates packet fields against ordered rules so the network can permit a specific conversation and deny others.',
'A standard IPv4 ACL primarily matches source addresses. An extended ACL can match protocol, source, destination, and transport ports. Inbound/outbound is relative to the router interface, not the user\'s perspective. A conventional ACL is not a stateful firewall; a TCP established keyword is not full connection tracking.',
'Rules are processed in order and the first match decides. An implicit deny applies when no rule matches. Wildcard 0 bits must match and 1 bits are ignored. Placement and direction determine which packets encounter the ACL. An ACL used to select NAT traffic has a different purpose from the same-looking ACL applied as an interface filter.',
'In the ACL flow diagram, follow one packet from top rule to first match, then stop. Do not continue to a later permit after an earlier deny. Fixture: Finance 192.168.10.0/24 enters R1 Gi0/0; server 192.168.20.20 is reached through Gi0/1. Routing is already working.',
'1. Finance client sends TCP to 192.168.20.20 port 443.\n2. R1 receives it on Gi0/0.\n3. The inbound ACL matches the HTTPS permit.\n4. A TCP port 22 attempt from the same client misses that rule.\n5. It matches the explicit deny. Return traffic uses the opposite direction and must satisfy any policies there.',
'Use show access-lists FINANCE_IN and show ip interface GigabitEthernet0/0. Verify sequence, fields, direction, and attachment. Generate one permitted and one denied test and compare counter changes. A rule with zero matches may be unused, misplaced, or simply not exercised.',
'If all traffic fails after applying an ACL, inspect implicit deny and direction before deleting the entire policy. If an intended deny is bypassed, inspect earlier permits and actual source/destination fields, including NAT placement effects. Correct the narrow rule/attachment and retest both positive and negative cases.',
'A broad permit precedes a narrow deny that matches the same packet. What happens?',
['The earlier permit decides','The later deny always overrides','Both rules average their actions','The packet waits for every rule to match'],0,
['First match terminates ordinary ACL evaluation.','A later rule does not override an earlier matching permit.','ACL actions are not averaged.','A packet need not match every rule.'],
config='''enable
configure terminal
ip access-list extended FINANCE_IN
 10 permit tcp 192.168.10.0 0.0.0.255 host 192.168.20.20 eq 443
 20 deny ip 192.168.10.0 0.0.0.255 any
 30 permit ip any any
exit
interface GigabitEthernet0/0
 ip access-group FINANCE_IN in
end
show access-lists FINANCE_IN
show ip interface GigabitEthernet0/0''',
config_note='Isolated IOS routed fixture above. The ACL permits Finance HTTPS to one server, denies other Finance IP traffic, and leaves other sources permitted; this is a teaching policy, not a complete anti-spoofing or production security policy. Named ACL mode holds ordered entries; interface ip access-group attaches it inbound. Undo by removing the interface attachment first with no ip access-group FINANCE_IN in, then removing only the unused lab ACL. Preserve console recovery.')

lesson('5-04',
'A rogue device can offer false DHCP settings or claim the gateway\'s IP-to-MAC mapping on a local VLAN. Layer 2 security adds checks close to the endpoint instead of assuming every local frame is trustworthy.',
'Port security limits learned/allowed source MACs. DHCP snooping filters server-originated DHCP messages at trust boundaries and builds bindings. Dynamic ARP inspection validates ARP using bindings or configured ARP ACLs. These are complementary; port security does not prove user identity and DAI does not encrypt ARP.',
'Trust is a per-feature ingress decision, not a label saying a cable is safe for every purpose. DHCP server-facing ports must be deliberately trusted for snooping. DAI trust is separate. Static-addressed hosts may lack DHCP bindings and need an explicit validated design. Violation modes and rate limits affect drop, logging, or shutdown behavior; verify platform defaults.',
'In the Layer 2 security diagram, label client ports untrusted and the authorized server/uplink path deliberately trusted. Draw DHCP binding creation before using that binding to validate ARP. Do not imply DAI can invent a binding for every static printer.',
'1. A client obtains a legitimate DHCP lease through the trusted server path.\n2. Snooping records MAC, IP, VLAN, interface, and lease information.\n3. An ARP arrives on an untrusted port.\n4. DAI compares it with valid policy/binding evidence.\n5. A mismatch is dropped/logged according to configuration.\n6. Port security independently evaluates source-MAC policy.',
'Use show ip dhcp snooping, show ip dhcp snooping binding, show ip arp inspection vlan 10, and show port-security interface FastEthernet0/1 on supported Catalyst IOS. Check enabled VLANs, trusted interfaces, actual bindings, drop counters, and violation mode. Do not enable DAI before planning static-host handling and binding persistence.',
'After DAI is enabled, a static printer fails while DHCP clients work. Inspect missing binding/ARP ACL coverage before trusting every access port. Add a validated static-host policy through the platform-supported method, then test legitimate ARP and rejection of an unauthorized mapping. If a port is err-disabled, remove the cause before recovery.',
'A static printer has no DHCP snooping binding. What is the safest design response before enforcing DAI?',
['Plan validated static-host ARP policy','Trust every client port','Assume DAI creates a lease automatically','Disable all switch security permanently'],0,
['Static devices need explicit validated treatment such as an appropriate ARP ACL.','Blanket trust bypasses the intended checks.','DAI does not act as a DHCP server.','Removing all protection is not a targeted solution.'],
config='''enable
configure terminal
ip dhcp snooping
ip dhcp snooping vlan 10
interface GigabitEthernet0/1
 ip dhcp snooping trust
end
show ip dhcp snooping
show ip dhcp snooping binding''',
config_note='Isolated supported Catalyst switch, VLAN 10 already operational; Gi0/1 leads only to the authorized DHCP infrastructure, and clients use untrusted access ports. Review option-82/server compatibility and rate limits before testing. These commands enable snooping, not DAI or port security. After valid bindings and static-host policy exist, DAI is enabled with ip arp inspection vlan 10; trust must be planned separately. Undo by restoring prior feature/VLAN/trust policy, not blindly trusting clients. Full DAI and port-security configuration acceptance remains pending.')

lesson('5-05',
'Two offices need private communication across a network they do not control. A VPN creates a protected logical relationship over that transport, but the outer path must still work and the endpoints must agree on identity and policy.',
'Site-to-site VPNs connect networks through gateways. Remote-access VPNs connect individual users/devices to an organization. IPsec protects IP traffic; a plain GRE tunnel is encapsulation without inherent encryption. A VPN does not make compromised endpoints trustworthy.',
'IPsec uses negotiated security associations and policy to protect selected traffic. IKE negotiates identity/security parameters; ESP commonly supplies protected payload transport. Tunnel mode adds an outer IP header around the protected inner packet. Routing, traffic selection, NAT traversal, and MTU can all affect operation.',
'In the VPN diagram, label inner source/destination as the communicating private endpoints and outer source/destination as tunnel gateways. Draw encryption over the protected inner content, not as making every outer header invisible. The provider still needs to deliver the outer packets.',
'1. Gateway receives traffic matching VPN policy.\n2. Peers establish required security associations.\n3. The gateway protects/encapsulates the selected packet.\n4. The underlay routes the outer packet.\n5. The peer verifies and decapsulates it.\n6. The destination network and return path must still permit delivery.',
'Inspect tunnel/IKE/IPsec state and packet counters using commands appropriate to the configured IKE version and platform. Compare encapsulated and decapsulated counter deltas during a controlled flow. A tunnel marked up does not prove that the desired subnet is selected or routed into it.',
'If the tunnel is up but one subnet fails, compare selectors, routes, ACLs, NAT exemptions where applicable, and return path. If small packets work but larger ones fail, investigate MTU/path-MTU behavior. Correct the evidenced mismatch and test bidirectional application traffic.',
'In an IPsec tunnel-mode site-to-site packet, what does the outer IP destination normally identify?',
['The remote VPN gateway','Always the final private server','The user\'s password','The local switch port'],0,
['The underlay must deliver the encapsulated packet to the peer gateway.','The final server is identified by the protected inner packet in this model.','A password is not an IP destination field.','A switch port is not the outer network-layer endpoint.'],
no_config='Objective 5.5 describes VPN purpose and concepts. IKE/IPsec configuration depends on platform, version, identity, crypto policy, and topology; an unvalidated generic crypto recipe would be misleading.')

lesson('5-06',
'A WLAN name is visible to everyone nearby. Security must establish who may join and protect the radio conversation; hiding the SSID is not a substitute for authentication and encryption.',
'WPA/WPA2/WPA3 are security generations with different mechanisms. WPA2-Personal uses a shared key; WPA2-Enterprise uses 802.1X/EAP with an authentication service. WPA3-Personal uses SAE rather than simply reusing the WPA2 PSK exchange. Enterprise identity and personal shared secrets have different management tradeoffs.',
'In an 802.1X design, the client is the supplicant, the AP/controller acts as authenticator, and an authentication server evaluates identity through the configured EAP method. Server-certificate validation matters for methods that use it. A successful association is not necessarily successful authentication or IP connectivity. Protected Management Frames requirements and transition modes depend on the security mode and platform.',
'In the 802.1X diagram, distinguish client-to-authenticator EAPOL from authenticator-to-server exchanges. The AP is not necessarily the credential database. For Personal mode, do not draw a RADIUS server as mandatory.',
'1. Client discovers the WLAN and its advertised security.\n2. It associates and performs the selected authentication/key establishment.\n3. Authorized data access becomes possible.\n4. The client obtains IP configuration.\n5. Gateway/DNS/application tests follow. A wrong PSK and a missing DHCP lease fail at different phases.',
'In the controller GUI inspect the actual WLAN security mode, cipher, PSK or AAA binding, PMF settings, client state, and failure reason. For Enterprise, verify the intended authentication server and certificate chain through approved procedures. Never expose real PSKs in shared lesson screenshots.',
'If one older client fails after a security upgrade, inspect its supported mode/cipher/PMF rather than disabling security globally. If all Enterprise clients fail, inspect server reachability, EAP policy, time, and certificate validation. Correct the compatibility or trust issue and verify both authentication and data access.',
'Which component is the supplicant in the described Enterprise WLAN?',
['The wireless client','The RADIUS server','The DHCP relay','The router\'s OSPF process'],0,
['The client requests authenticated access.','The server evaluates authentication rather than acting as the supplicant.','DHCP relay handles address-configuration forwarding.','OSPF is unrelated to the client authentication role.'],
config='GUI WORKFLOW: select lab WLAN -> WPA2-Personal/AES -> unique lab PSK -> intended VLAN/policy -> AP binding -> test client',
config_note='This is a GUI procedure, not executable IOS CLI. Objective 5.10 uses WPA2 PSK; retain that curriculum target while discussing WPA3 separately. Use an isolated supported controller and a unique disposable PSK entered privately. Verify exact GUI family/release and client compatibility. Undo by restoring the prior lab WLAN security/policy, not by leaving it open. Production WLAN changes need a planned client-impact review.')

lesson('6-01',
'A team must apply one consistent policy to many switches. Repeating manual commands makes omissions easy. Automation expresses repeatable work; controller-based networking adds a coordinated view and interfaces for intent and device programming.',
'Traditional distributed control lets devices make protocol decisions locally. Controller-based designs centralize selected control/management functions while devices still forward traffic. The data plane moves packets, the control plane determines forwarding behavior, and the management plane configures/observes the system. Centralized logically does not mean one unprotected physical server.',
'Northbound APIs expose controller capabilities to applications; southbound interfaces connect controller functions to infrastructure. Underlay supplies transport reachability. Overlay supplies logical connectivity over that transport. A fabric combines coordinated components and policy. A controller does not eliminate the need for healthy underlay links, addressing, and routing.',
'In the SDN diagram, put applications above the controller and infrastructure below. Label northbound and southbound from the controller\'s perspective. Draw user traffic through the forwarding devices rather than automatically through the controller process.',
'1. An operator submits intended policy.\n2. The controller validates/translates it.\n3. Infrastructure receives supported configuration or forwarding instructions.\n4. Devices forward user packets.\n5. Telemetry reports actual state.\n6. The system compares intent with evidence. An API success response may mean acceptance, not completed convergence.',
'Inspect controller inventory, device reachability, task status, actual device configuration, and end-to-end service. Compare desired versus observed state. Verify high availability and failure behavior for the particular architecture; do not assume controller loss either stops all traffic or has no effect.',
'If an overlay endpoint is unreachable, test underlay reachability before changing overlay policy. If a task is accepted but not realized, inspect per-device errors and permissions. Correct the failing dependency and verify actual forwarding, not just a green request status.',
'Which plane primarily forwards ordinary user packets?',
['Data plane','Management plane only','The northbound API by definition','The change-approval spreadsheet'],0,
['The data plane performs packet forwarding.','Management configures and observes rather than defining the packet-forwarding role.','An API exposes functions; it is not inherently the user forwarding path.','Approval records do not forward traffic.'],
no_config='Objectives 6.1-6.3 explain architecture and impact. Controller APIs and device programming differ by product; this lesson does not invent a universal controller command.')

lesson('6-02',
'An operations dashboard predicts a link problem while a generative assistant proposes a configuration change. Both use AI-related methods, but a prediction and a generated explanation are different outputs with different validation needs.',
'Predictive models estimate outcomes or detect patterns from data. Generative models produce new text or other content. Supervised learning uses labeled examples; unsupervised methods can discover structure or anomalies. An anomaly is a deviation from a learned baseline, not proof of an attack or root cause.',
'Network data quality, sampling, missing values, and changing workloads affect model usefulness. False positives create unnecessary investigation; false negatives miss events. Correlation is not causation. A generated command can be plausible and still wrong for the platform or current state, so evidence, scope, and human approval remain necessary.',
'Picture a pipeline from telemetry to preprocessing, model output, analyst review, approved action, and measured result. Put a validation gate before any production change. Keep the raw evidence accessible beside the generated summary.',
'1. Collect authorized interface/error telemetry.\n2. Compare it with a relevant baseline.\n3. A model flags an unusual trend.\n4. An engineer checks raw counters, topology, and maintenance context.\n5. A generative tool may summarize evidence or propose a test.\n6. Validate the proposal in a lab and follow change control before acting.',
'Check the data interval, missingness, model scope, confidence/calibration where available, and false-positive/negative behavior against known cases. For generated content, verify commands against the exact platform and supplied state. Do not upload secrets or sensitive logs to an unapproved service.',
'If alert volume rises after a workload change, investigate baseline drift and data changes before declaring many simultaneous failures. If generated guidance contradicts device evidence, prefer verified evidence and correct the proposal. Record the validation outcome rather than treating fluent wording as authority.',
'A model flags unusual traffic. What conclusion is justified immediately?',
['The pattern needs investigation against context and evidence','An attack is proven','The generated fix is safe to apply automatically','The physical cable must be replaced'],0,
['An anomaly is a signal to investigate, not a complete diagnosis.','Unusual traffic can have benign explanations.','Generated changes still require technical and operational validation.','The anomaly alone does not identify a cable fault.'],
no_config='The objective is explaining AI/ML in operations, not training a model or connecting an AI service. No production mutation or synthetic telemetry is performed by this lesson.')

lesson('6-03',
'A controller exposes device inventory through an API. Instead of scraping a screen, a client requests a resource and receives structured data. Understanding method, identity, status, and encoding prevents automation from treating every response as success.',
'GET retrieves a representation; POST performs resource-specific processing and commonly creates subordinate resources; PUT creates/replaces the target representation; PATCH applies a partial modification; DELETE requests removal. Exact API contracts matter. JSON is a data format, not a transport or authentication method.',
'HTTP requests carry method, target, headers, and sometimes a body. HTTPS protects transport when certificate validation is correct. Authentication may use Basic over TLS, tokens, or other documented mechanisms; Base64 alone is not encryption. Idempotent methods describe intended effect of repetition, not identical responses or permission to retry every request blindly.',
'In the REST diagram, label client request and server response separately. Mark method/path, authentication header, content type, status, and JSON body. For {"vlans":[10,20,30]}, identify one object member whose value is an array of three numbers. Object member order is not semantically significant; array order is.',
'1. A client validates the HTTPS server identity.\n2. It sends an authorized GET for inventory.\n3. The server returns a status and representation.\n4. The client checks status and content type before parsing JSON.\n5. It validates expected fields and handles pagination if the API uses it.\n6. A write requires the documented method, schema, permission, and verification of resulting state.',
'Interpret 2xx as the documented success class, 4xx as a request/client-side class, and 5xx as server-side failure. 401 concerns authentication; 403 indicates refusal, commonly authorization; 404 can mean absent or deliberately concealed resources. A 204 response has no content to parse. JSON requires double-quoted strings and lowercase true/false/null; comments and trailing commas are not standard JSON.',
'If parsing fails, inspect status/content type and whether an HTML error page or empty 204 body was returned. If 401/403 occurs, correct credentials/scope through approved processes rather than disabling certificate checks. If a write times out, determine whether it took effect before retrying a non-idempotent operation.',
'What is the value type of vlans in {"vlans":[10,20,30]}?',
['Array','String','Boolean','Three separate top-level objects'],0,
['Square brackets contain an ordered array of three numbers.','The value is not enclosed as one quoted string.','A Boolean would be true or false.','The top level is one object with one member.'],
config='JSON DATA EXAMPLE: {"vlans":[10,20,30],"enabled":true}',
config_note='This is a valid JSON data example after the label, not an IOS command or a live API request. Use a documented sandbox API for hands-on work; no endpoint or token is invented here. Validate syntax and expected schema before writes. Undo depends on the API resource and prior state, not a universal HTTP rollback command.')

lesson('6-04',
'A team needs consistent NTP and interface policy across many devices. Configuration management records desired work in reviewable files, but a repeatable mistake can spread faster than a manual one. Scope and validation matter as much as automation.',
'Ansible commonly runs agentless tasks from a control node over SSH or APIs using inventory, playbooks, modules, and variables. Terraform uses declarative configuration and providers to manage resources while tracking managed objects in state. They overlap in capability; Ansible is not limited to CLI and Terraform is not limited to public cloud.',
'Idempotence means repeated execution converges on the intended state without unnecessary change; it depends on the module/task/provider and design, not just the tool name. Terraform state can contain sensitive data and needs protected storage/locking. A plan previews proposed changes but does not prove they are operationally safe. Automation rollback requires captured prior state and a tested restoration path.',
'Follow two workflows: Ansible inventory -> playbook/tasks -> modules -> devices; Terraform configuration -> plan -> reviewed apply -> managed resources/state. Draw verification after both. Do not depict a plan as already changing the infrastructure.',
'1. Select a small lab inventory.\n2. Review variables and intended diff.\n3. Run supported dry-run/check or plan behavior, understanding its limits.\n4. Approve and apply only the intended scope.\n5. Verify actual device/service state.\n6. Repeat and inspect whether unnecessary changes recur. Store secrets outside ordinary lesson files.',
'Inspect targeted hosts/resources, task/plan output, actual configuration, and service tests. For Ansible, check mode support varies by module. For Terraform, review create/update/destroy actions and state handling. A successful tool exit is not a substitute for end-to-end verification.',
'If a run targets too many devices, stop before applying and correct inventory/workspace/variables. If every repeat reports changes, inspect non-idempotent tasks, generated values, and drift. Do not delete Terraform state as a generic repair; it can orphan management relationships. Restore through a reviewed plan.',
'Which statement about idempotence is accurate?',
['It depends on the actual tasks/modules/providers and desired state','Every script is automatically idempotent','It means no configuration may ever change','It guarantees a safe production change'],0,
['Tooling and task design determine repeat behavior.','A script can repeat destructive or unnecessary actions.','Convergence can require an initial change.','Repeatability does not prove correctness, scope, or operational safety.'],
no_config='Objective 6.6 recognizes capabilities. No runnable playbook/provider example is supplied without a defined lab inventory and supported module versions; the lesson teaches safe workflow and interpretation.')
