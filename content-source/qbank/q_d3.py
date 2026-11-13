from qlib import *
D = "d3"

RT1 = """R1# show ip route
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
L        203.0.113.2/32 is directly connected, GigabitEthernet0/0"""

ROUTES = [("0.0.0.0/0", "203.0.113.1"), ("10.1.0.0/16", "10.0.12.2"), ("10.1.4.0/22", "10.0.13.3"), ("10.1.5.0/24", "10.0.14.4")]
def lpm(dst):
    import ipaddress
    best = max((r for r in ROUTES if ipaddress.ip_address(dst) in ipaddress.ip_network(r[0])),
               key=lambda r: ipaddress.ip_network(r[0]).prefixlen)
    return best[1]

S(D, "3.1", "routing-table", 2, "routing-table", "Refer to the routing table. Which next hop does R1 use for a packet destined to 10.1.5.20?",
  ["*10.0.14.4 || The static route 10.1.5.0/24 is the longest prefix that matches 10.1.5.20.",
   "10.0.13.3 || 10.1.4.0/22 also matches, but /24 is more specific.",
   "10.0.12.2 || 10.1.0.0/16 matches but is less specific.",
   "203.0.113.1 || The default route is used only when nothing more specific matches."],
  "The router always picks the longest matching prefix first. Administrative distance only matters between routes to the same prefix.",
  "Longest prefix match", ex=RT1, verify=lambda: lpm("10.1.5.20"))

S(D, "3.2", "routing-table", 3, "routing-table", "Refer to the routing table. Which destinations does R1 forward to next hop 10.0.13.3?",
  ["*10.1.4.1 || 10.1.4.0/22 (10.1.4.0–10.1.7.255) is the longest match.",
   "*10.1.7.254 || Still inside 10.1.4.0/22, and no longer prefix covers it.",
   "10.1.5.1 || The more specific static 10.1.5.0/24 wins, so it goes to 10.0.14.4.",
   "10.1.8.1 || Outside the /22, so only 10.1.0.0/16 matches (via 10.0.12.2).",
   "10.0.13.3 || This is a connected address on Gi0/2, delivered directly."],
  "A /22 has a block size of 4 in the third octet: 10.1.4.0/22 covers third-octet values 4–7. More specific routes inside it (10.1.5.0/24) take precedence.",
  "Overlapping routes", ex=RT1, verify=lambda: [d for d in ["10.1.4.1", "10.1.7.254", "10.1.5.1", "10.1.8.1"] if lpm(d) == "10.0.13.3"])

S(D, "3.1", "routing-table", 1, "routing-table", "Refer to the routing table. How does R1 forward a packet destined to 10.2.0.1?",
  ["*Via the default route to 203.0.113.1 || No 10.2.x.x route exists, so the gateway of last resort is used.",
   "Via 10.0.12.2, because 10.1.0.0/16 is the closest match || 10.2.0.1 is not inside 10.1.0.0/16.",
   "R1 drops the packet || A default route exists, so the packet is forwarded.",
   "Via Gi0/1 as a connected route || 10.2.0.1 is not on any connected subnet."],
  "A route matches only if the destination is inside its prefix. When nothing else matches, the default route (0.0.0.0/0) is used.",
  "Default route", ex=RT1, verify=lambda: "Via the default route to 203.0.113.1" if lpm("10.2.0.1") == "203.0.113.1" else "")

S(D, "3.1", "routing-table", 2, "routing-table", "Refer to the routing table. What is the OSPF metric of the route used for 10.1.200.1?",
  ["*20 || 10.1.200.1 matches only 10.1.0.0/16, shown as [110/20]: AD 110, metric 20.",
   "110 || 110 is the administrative distance, not the metric.",
   "30 || 30 belongs to 10.1.4.0/22, which does not contain 10.1.200.1.",
   "0 || [1/0] belongs to the static routes."],
  "Routing table brackets show [administrative distance/metric]. For OSPF the metric is the total cost to the destination.",
  "Reading [AD/metric]", ex=RT1)

S(D, "3.1.b", "routing-table", 1, "routing-table", "In the entry 'O 10.1.4.0/22 [110/30] via 10.0.13.3', what do 110 and 30 represent?",
  ["*Administrative distance 110 and OSPF cost 30 || Brackets are [AD/metric].",
   "Hop count 110 and AD 30 || OSPF does not use hop count, and the order is AD first.",
   "Route age 110 seconds and 30 hops || Route age appears separately as hh:mm:ss.",
   "Bandwidth 110 Mbps and delay 30 ms || These are not shown in the brackets."],
  "AD ranks route sources (OSPF = 110); the metric compares routes from the same source (OSPF cost).", "Routing table fields")

S(D, "3.2.c", "routing-table", 2, "scenario",
  "A router learns 172.16.8.0/24 from OSPF and from EIGRP (internal), and also has a static route to 172.16.8.0/24 configured with administrative distance 200. Which route is installed?",
  ["*The EIGRP route || For the same prefix, the lowest AD wins: EIGRP 90 < OSPF 110 < static 200.",
   "The static route || Its AD was raised to 200, so it is now the least preferred.",
   "The OSPF route || OSPF's AD of 110 is higher than EIGRP's 90.",
   "All three, load-balanced || Load balancing happens only between equal routes from the same source."],
  "Administrative distance can be changed. A static route with a raised AD is a floating static route that is only installed if better sources disappear.",
  "Administrative distance")

S(D, "3.2.a", "routing-table", 3, "scenario",
  "A router has an OSPF route to 192.168.0.0/16 and a RIP route to 192.168.10.0/24. A packet arrives for 192.168.10.5. Which route is used?",
  ["*The RIP route, because /24 is a longer prefix || Longest prefix match is applied before administrative distance.",
   "The OSPF route, because its AD (110) is lower than RIP's (120) || AD only compares routes to the identical prefix.",
   "Both routes are used for load sharing || The prefixes differ, so the more specific one wins.",
   "The default route || More specific routes exist."],
  "Routes to different prefixes are not compared by AD. The most specific matching prefix always wins.",
  "Longest match vs AD", "Long beats low.")

M(D, "3.2.c", "routing-table", 2, "protocol-id", "Match each route source to its default administrative distance.",
  [("Connected", "0"), ("Static", "1"), ("eBGP", "20"), ("EIGRP (internal)", "90"), ("OSPF", "110"), ("RIP", "120")],
  "Lower administrative distance means more trusted: connected 0, static 1, eBGP 20, EIGRP 90, OSPF 110, IS-IS 115, RIP 120, iBGP 200.",
  "Administrative distance values")

O(D, "3.2", "routing-table", 2, "concept", "Arrange the criteria a router uses to choose a route, from first evaluated to last.",
  ["Longest prefix match", "Lowest administrative distance", "Lowest metric"],
  "Prefix length decides first. AD resolves ties between different sources for the same prefix; the metric resolves ties within one routing protocol.",
  "Route selection order")

TF(D, "3.1", "routing-table", 2, "concept", "When a router forwards an IPv4 packet, it decrements the TTL and builds a new Layer 2 header for the outgoing link.", True,
   "Routing is a per-hop process: the frame is removed, the TTL is decremented (packets with TTL 0 are dropped), and a new frame is built for the next hop.",
   "Packet forwarding")

S(D, "3.1", "routing-table", 2, "diagram", "Refer to the diagram. As the PC sends a packet to the server, which destination MAC address is in the frame leaving the PC?",
  ["*R1 Gi0/0's MAC || The server is on another subnet, so the PC frames the packet to its default gateway.",
   "The server's MAC || The PC cannot reach the server's MAC across routers.",
   "The MAC of R2's Gi0/1 || The PC only communicates directly with devices on its own subnet.",
   "FFFF.FFFF.FFFF || Unicast IP traffic uses the gateway's unicast MAC after ARP."],
  "Hosts send off-subnet traffic to the default gateway's MAC. Each router builds a new frame for the next link.",
  "Default gateway forwarding", dg="routing")

S(D, "3.1", "routing-table", 3, "diagram", "Refer to the diagram. If R1 loses its OSPF route to 10.2.2.0/24, how does it forward traffic to 10.2.2.10?",
  ["*Using the static default route via 10.0.12.2 || 0.0.0.0/0 still matches every destination.",
   "It drops the traffic || A matching default route exists.",
   "It forwards out Gi0/0 toward the PC || No route points to Gi0/0 for 10.2.2.0/24.",
   "It floods the packet out all interfaces || Routers do not flood unknown destinations."],
  "The default route catches every destination without a more specific match. Here it also points to R2, so connectivity continues.",
  "Default route as backup", dg="routing")

S(D, "3.1", "routing-table", 1, "routing-table", "What does a routing-table entry with code 'L', such as 'L 10.0.12.1/32', represent?",
  ["*The router's own interface IP || Local routes are /32 host routes for each configured interface address.",
   "A loopback interface || Loopback subnets appear as C/L entries too, but L means local, not loopback.",
   "A route learned by LDP || LDP is a label protocol and does not install L routes.",
   "A manually configured host route || Manual routes appear with code S."],
  "IOS installs a connected (C) route for the subnet and a local (L) /32 route for the interface address itself.",
  "Local routes")

TF(D, "3.1", "routing-table", 2, "concept", "If OSPF learns two paths to the same prefix with equal cost, the router can install both and load-share traffic.", True,
   "Equal-cost multipath (ECMP) is supported by OSPF; IOS installs up to 4 equal-cost paths by default (configurable with maximum-paths).",
   "ECMP")

S(D, "3.1", "routing-table", 2, "routing-table", "Based on the output, how does the router forward traffic to 10.9.1.1?",
  ["*It load-shares across both next hops || Two equal-cost OSPF paths are installed for 10.9.0.0/16.",
   "It uses only 10.0.12.2 because it is listed first || Both entries are active paths.",
   "It drops the traffic because the routes conflict || Equal-cost paths are not a conflict.",
   "It uses the default route || 10.9.0.0/16 matches more specifically."],
  "When multiple next hops appear under one prefix with the same [AD/metric], the router shares traffic between them.",
  "Equal-cost load sharing",
  ex="O     10.9.0.0/16 [110/30] via 10.0.12.2, 00:02:41, GigabitEthernet0/1\n                  [110/30] via 10.0.13.3, 00:02:41, GigabitEthernet0/2")

S(D, "3.1", "routing-table", 2, "troubleshooting", "A router has no default route and receives a packet for a destination that matches no route. What happens?",
  ["*The packet is dropped || Routers never guess where to forward.",
   "The packet is flooded out all interfaces || Flooding is a Layer 2 switch behavior.",
   "The router sends an ARP request for the destination || ARP is only for directly connected subnets.",
   "The packet is sent back out the interface it arrived on || That would create a loop."],
  "Without a matching route, the packet cannot be forwarded. A default route provides a catch-all.", "No matching route")

S(D, "3.3", "static", 1, "command", "Which command configures an IPv4 default route to next hop 198.51.100.1?",
  ["*ip route 0.0.0.0 0.0.0.0 198.51.100.1 || Destination 0.0.0.0 with mask 0.0.0.0 matches every address.",
   "ip route 0.0.0.0 255.255.255.255 198.51.100.1 || A /32 mask would match only 0.0.0.0.",
   "ip default-route 198.51.100.1 || This is not valid IOS syntax.",
   "ip route default 198.51.100.1 || IOS requires the 0.0.0.0 0.0.0.0 form."],
  "The default route appears as S* and sets the gateway of last resort.", "Default static route")

S(D, "3.3.d", "static", 2, "config",
  "R1 learns its default route from OSPF. An engineer wants a backup default route through ISP2 at 192.0.2.1 that is used only if the OSPF default disappears. Which command achieves this?",
  ["*ip route 0.0.0.0 0.0.0.0 192.0.2.1 115 || AD 115 is higher than OSPF's 110, so it stays inactive while the OSPF route exists.",
   "ip route 0.0.0.0 0.0.0.0 192.0.2.1 || The default static AD of 1 would replace the OSPF route immediately.",
   "ip route 0.0.0.0 0.0.0.0 192.0.2.1 90 || AD 90 is lower than 110, so this would become the primary route.",
   "ip route 0.0.0.0 0.0.0.0 192.0.2.1 100 || AD 100 is still lower than 110."],
  "A floating static route must use an AD higher than the route it backs up.", "Floating static route", "Float ABOVE the primary's AD.")

S(D, "3.3", "static", 2, "command", "R1 reaches its IPv6 next hop through link-local address FE80::2 on Gi0/1. Which IPv6 default route is valid?",
  ["*ipv6 route ::/0 GigabitEthernet0/1 FE80::2 || A link-local next hop must be paired with the exit interface.",
   "ipv6 route ::/0 FE80::2 || Link-local addresses are only unique per link, so IOS needs the interface.",
   "ip route ::/0 GigabitEthernet0/1 FE80::2 || IPv6 routes use 'ipv6 route'.",
   "ipv6 route ::/128 GigabitEthernet0/1 FE80::2 || ::/128 is the unspecified address, not a default route."],
  "IPv6 default route = ::/0. Also ensure 'ipv6 unicast-routing' is enabled.", "IPv6 static routing")

S(D, "3.3", "static", 3, "troubleshooting",
  "R1 has 'ip route 10.20.0.0 255.255.0.0 10.0.0.2', and the route appears in its table. Pings from a PC on R1's LAN (192.168.1.0/24) to 10.20.5.5 fail. R2 has no routes other than its connected networks. What is the most likely cause?",
  ["*R2 has no route back to 192.168.1.0/24 || Echo requests reach the destination, but replies cannot be routed back.",
   "The static route's AD is too high || AD only matters when competing routes exist.",
   "The static route mask should be 255.255.255.0 || A /16 correctly covers 10.20.5.5.",
   "Static routes do not work for ping || ICMP is routed like any other IP packet."],
  "Routing must work in both directions. Add a return route on R2 (or a dynamic protocol) for 192.168.1.0/24.",
  "Return routes", "Ping needs a path there AND back.")

S(D, "3.3", "static", 2, "concept", "Which statements about IPv4 static routes are true?",
  ["*The default administrative distance is 1 || Only connected routes are more trusted.",
   "*A route with a next-hop address is installed only if the next hop is reachable || IOS needs a recursive lookup to succeed.",
   "Static routes automatically adapt when the topology changes || They must be changed manually (unless a floating route takes over).",
   "Static routes are advertised to all neighbors by default || They are only advertised if redistributed into a routing protocol."],
  "Static routes are predictable and create no protocol overhead, but they do not react to failures except through floating backups.",
  "Static route behavior")

S(D, "3.3.c", "static", 1, "command", "Which command creates a host route to 10.9.9.9 via next hop 10.0.0.2?",
  ["*ip route 10.9.9.9 255.255.255.255 10.0.0.2 || A /32 mask matches exactly one address.",
   "ip route 10.9.9.9 255.255.255.0 10.0.0.2 || This mask describes a /24 network.",
   "ip route 10.9.9.9 0.0.0.0 10.0.0.2 || A 0.0.0.0 mask turns the route into a default route.",
   "ip host 10.9.9.9 10.0.0.2 || 'ip host' creates a local name-to-address mapping."],
  "Host routes (/32) are useful for steering traffic for a single server or loopback.", "Host route")

S(D, "3.3", "static", 3, "troubleshooting", "An engineer configures 'ip route 10.50.0.0 255.255.0.0 10.0.99.9', but it does not appear in 'show ip route'. What is the most likely reason?",
  ["*The next hop 10.0.99.9 is not reachable through any existing route || IOS installs the route only when the recursive lookup succeeds.",
   "Its administrative distance is too high || There is no competing route, and AD defaults to 1.",
   "Static routes are hidden from 'show ip route' || Static routes appear with code S.",
   "Static routing must be enabled with a global command || IPv4 static routing works by default."],
  "Check that the next hop is on a connected subnet (or reachable via another route).", "Static route installation")

TF(D, "3.3", "static", 3, "concept",
   "A static route on an Ethernet interface configured with only an exit interface (no next-hop address) makes the router ARP for each destination, relying on the neighboring router's proxy ARP.", True,
   "With no next hop, the router treats destinations as directly connected and ARPs for them. Use a next-hop or fully specified route on multi-access links.",
   "Exit-interface static routes")

S(D, "3.4", "ospf", 2, "troubleshooting", "R1 and R2 share a subnet in area 0 but never become OSPF neighbors. Based on the output, what is the cause?",
  ["*The Hello and Dead timers do not match || Neighbors must agree on Hello (10 vs 5) and Dead (40 vs 20) intervals.",
   "The process IDs differ || Process IDs are locally significant and need not match.",
   "The router IDs differ || Router IDs must be unique, so differing is required.",
   "The network types are point-to-point || Both show BROADCAST."],
  "OSPF neighbor requirements: same area, subnet/mask, Hello/Dead timers, authentication and stub flag; unique router IDs; matching MTU to reach Full.",
  "OSPF adjacency requirements",
  ex="R1# show ip ospf interface g0/0 | include Process|Network|Timer\n  Process ID 1, Router ID 1.1.1.1, Network Type BROADCAST, Cost: 1\n  Timer intervals configured, Hello 10, Dead 40, Wait 40, Retransmit 5\n\nR2# show ip ospf interface g0/0 | include Process|Network|Timer\n  Process ID 20, Router ID 2.2.2.2, Network Type BROADCAST, Cost: 1\n  Timer intervals configured, Hello 5, Dead 20, Wait 20, Retransmit 5")

S(D, "3.4", "ospf", 2, "concept", "Which parameters must match for two OSPFv2 routers to become neighbors?",
  ["*Area ID || Both interfaces must be in the same area.",
   "*Hello and Dead intervals || Mismatched timers prevent the adjacency.",
   "Process ID || Process IDs are local to each router.",
   "Router ID || Router IDs must be different.",
   "Interface priority || Priority only influences the DR/BDR election."],
  "Matching: area, subnet, timers, authentication, stub flag (and MTU for Full). Unique: router ID. Irrelevant: process ID.",
  "OSPF neighbor requirements", "Process ID is local; area must match.")

S(D, "3.4.e", "ospf", 2, "scenario",
  "R1 has no 'router-id' command. Its interfaces are Loopback0 1.1.1.1, Loopback1 10.10.10.10, Gi0/0 192.168.100.1 (up) and Gi0/1 172.16.1.1 (up). What is R1's OSPF router ID?",
  ["*10.10.10.10 || With no manual router ID, the highest loopback IP is preferred over physical interfaces.",
   "192.168.100.1 || Physical interfaces are used only if no loopback exists.",
   "1.1.1.1 || The highest, not the lowest, loopback address is chosen.",
   "172.16.1.1 || It is neither a loopback nor the highest address."],
  "Router ID order: manual 'router-id' → highest loopback IPv4 → highest active physical IPv4.",
  "OSPF router ID", "Manual → Loopback → Physical.")

S(D, "3.4.d", "ospf", 3, "scenario",
  "Three routers on one Ethernet segment start OSPF at the same time: R1 (priority 1, RID 3.3.3.3), R2 (priority 10, RID 1.1.1.1) and R3 (priority 0, RID 9.9.9.9). Which router becomes the DR?",
  ["*R2 || The highest interface priority wins; R2's 10 beats R1's 1.",
   "R3 || Priority 0 makes a router ineligible, even with the highest router ID.",
   "R1 || Router ID is only a tiebreaker when priorities are equal; R1 becomes the BDR.",
   "No DR is elected on Ethernet || Broadcast networks elect a DR and BDR."],
  "DR election: highest priority, then highest router ID. Priority 0 never becomes DR or BDR.",
  "DR/BDR election", "OSPF: highest wins (opposite of STP).")

S(D, "3.4.d", "ospf", 3, "scenario", "R2 is the current DR on a LAN. An engineer raises R1's OSPF priority to 255. What happens?",
  ["*R2 remains the DR; elections are not preemptive || R1 becomes DR only after a new election, such as when the DR fails or OSPF is restarted.",
   "R1 immediately becomes the DR on the LAN || The DR is not replaced just because a higher priority appears.",
   "R1 immediately becomes the BDR on the LAN || The BDR is not preempted either.",
   "All adjacencies reset automatically || Changing priority does not reset the neighbors."],
  "OSPF DR/BDR election is non-preemptive to avoid instability. Plan priorities before routers come up.",
  "Non-preemptive DR election")

S(D, "3.4", "ospf", 3, "routing-table", "Based on the output from the local router, which statement is true?",
  ["*The local router is a DROTHER; this is normal || DROTHERs form full adjacencies only with the DR and BDR.",
   "The adjacency with 3.3.3.3 has failed || 2WAY between two DROTHERs is the expected final state.",
   "The local router is the DR || The DR would be FULL with every neighbor.",
   "There is an MTU mismatch with 3.3.3.3 || MTU problems leave neighbors stuck in EXSTART/EXCHANGE."],
  "On broadcast segments, all routers become FULL with the DR and BDR. DROTHER-to-DROTHER neighbors stay in 2-Way.",
  "OSPF neighbor states",
  ex="R5# show ip ospf neighbor\nNeighbor ID   Pri   State           Dead Time   Address     Interface\n2.2.2.2         1   FULL/DR         00:00:35    10.1.1.2    Gi0/0\n3.3.3.3         1   2WAY/DROTHER    00:00:33    10.1.1.3    Gi0/0\n4.4.4.4         1   FULL/BDR        00:00:38    10.1.1.4    Gi0/0")

S(D, "3.4", "ospf", 2, "calculation", "With the default OSPF reference bandwidth, what costs do a 1 Gbps and a 10 Gbps interface receive?",
  ["*1 and 1 || Cost = 100 Mbps / bandwidth, with a minimum of 1, so all links of 100 Mbps or faster cost 1.",
   "10 and 1 || That requires a reference bandwidth of 10 Gbps.",
   "4 and 2 || These are STP path costs.",
   "1 and 0.1 || OSPF cost is an integer with a minimum of 1."],
  "Raise the reference bandwidth on every router (auto-cost reference-bandwidth) so fast links can be distinguished.",
  "OSPF cost", verify=lambda: "1 and 1" if max(1, 100 // 1000) == 1 and max(1, 100 // 10000) == 1 else "")

S(D, "3.4", "ospf", 3, "calculation", "After 'auto-cost reference-bandwidth 10000' is configured, what is the OSPF cost of a 1 Gbps interface?",
  ["*10 || 10000 Mbps / 1000 Mbps = 10.", "1 || 1 was the cost with the default 100 Mbps reference.",
   "100 || That would require a 100 Gbps reference.", "4 || 4 is the STP cost for 1 Gbps."],
  "The reference-bandwidth value is in Mbps. Configure the same value on all routers so costs stay consistent.",
  "OSPF reference bandwidth", verify=lambda: str(10000 // 1000))

S(D, "3.4", "ospf", 2, "calculation",
  "R1 can reach 10.5.5.0/24 through R2 (outgoing link costs 10 then 10) or through R3 (costs 5 then 20). Which path does OSPF install?",
  ["*Through R2, total cost 20 || 10 + 10 = 20 is lower than 5 + 20 = 25.",
   "Through R3, because its first link is cheaper || OSPF compares total path cost, not the first hop.",
   "Both, as equal-cost paths || 20 and 25 are not equal.",
   "Through R3, because it has fewer hops || Both paths have the same hop count; OSPF uses cost anyway."],
  "SPF sums the outgoing interface costs along each path and picks the lowest total.", "OSPF path selection",
  verify=lambda: "Through R2, total cost 20" if 10 + 10 < 5 + 20 else "")

S(D, "3.4.a", "ospf", 3, "config",
  "Which command enables OSPF in area 0 only on the interface addressed 172.16.10.5/30, and on no other interface in 172.16.10.0/24?",
  ["*network 172.16.10.4 0.0.0.3 area 0 || The wildcard 0.0.0.3 matches only 172.16.10.4–172.16.10.7.",
   "network 172.16.10.4 255.255.255.252 area 0 || The network command uses a wildcard mask, not a subnet mask.",
   "network 172.16.10.0 0.0.0.255 area 0 || This matches every interface in 172.16.10.0/24.",
   "network 172.16.10.5 0.0.0.3 area 1 || The area must be 0, as required."],
  "The OSPF 'network' statement uses a wildcard mask to select interfaces. Alternatively configure 'ip ospf 1 area 0' directly on the interface.",
  "OSPF network command")

S(D, "3.4", "ospf", 2, "scenario", "LAN interface Gi0/2 must be advertised into OSPF, but no OSPF adjacency should ever form on it. Which command achieves this?",
  ["*passive-interface GigabitEthernet0/2 || The subnet is still advertised, but no Hellos are sent or processed on that interface.",
   "no network 10.0.2.0 0.0.0.255 area 0 || Removing the network statement covering Gi0/2 would stop advertising its subnet.",
   "ip ospf priority 0 (on Gi0/2) || This only prevents DR/BDR election; adjacencies still form.",
   "shutdown (on GigabitEthernet0/2) || The interface and its subnet would go down."],
  "Passive interfaces are best practice on user-facing LANs to prevent rogue OSPF neighbors.", "OSPF passive interface")

TF(D, "3.4.b", "ospf", 2, "concept", "On an interface configured with 'ip ospf network point-to-point', no DR or BDR is elected.", True,
   "Point-to-point networks have only two routers, so a DR is unnecessary. Neighbors appear as FULL/ - in 'show ip ospf neighbor'.",
   "OSPF network types")

O(D, "3.4", "ospf", 2, "concept", "Arrange the OSPF neighbor states in the order a new adjacency moves through them.",
  ["Down", "Init", "2-Way", "ExStart", "Exchange", "Loading", "Full"],
  "Hellos move neighbors from Down to Init and 2-Way; ExStart chooses master/slave; Exchange swaps DBD summaries; Loading requests missing LSAs; Full means synchronized LSDBs.",
  "OSPF neighbor states", "Don't Invite 2 Extra Exhausted Lazy Friends.")

S(D, "3.4", "ospf", 2, "config", "R1 has a static default route to the ISP. Which OSPF command advertises this default route to the other OSPF routers?",
  ["*default-information originate || It injects a default route into OSPF when one exists in R1's routing table.",
   "redistribute static subnets || This redistributes specific static routes; a default route needs default-information originate.",
   "network 0.0.0.0 255.255.255.255 area 0 || This enables OSPF on all interfaces; it does not advertise a default route.",
   "ip default-network 0.0.0.0 || This is not how OSPF advertises a default route."],
  "Add 'always' to advertise the default even if the router has none in its table.", "OSPF default route")

S(D, "3.4", "ospf", 1, "concept", "Which statement describes how OSPF builds its routes?",
  ["*Each router runs SPF on a shared LSDB || OSPF is a link-state protocol.",
   "Routers send their full routing tables to neighbors every 30 seconds || That describes RIP.",
   "Routers choose paths with the fewest hops || OSPF uses cost, not hop count.",
   "The DR calculates the routes for every router on the segment || The DR reduces flooding; each router still runs SPF itself."],
  "Link-state: share topology (LSAs) → synchronize the LSDB → each router computes shortest paths independently.",
  "Link-state operation")

M(D, "3.4", "ospf", 2, "protocol-id", "Match each OSPF item to its value.",
  [("Multicast address for all OSPF routers", "224.0.0.5"), ("Multicast address for DR/BDR", "224.0.0.6"),
   ("Administrative distance", "110"), ("IP protocol number", "89")],
  "OSPF runs directly over IP (protocol 89), uses 224.0.0.5 and 224.0.0.6, and has AD 110.", "OSPF facts")

S(D, "3.4", "ospf", 3, "troubleshooting", "Two OSPF neighbors stay stuck in EXSTART/EXCHANGE and never reach FULL. What is the most likely cause?",
  ["*An interface MTU mismatch || Database description packets are rejected when the MTUs differ.",
   "An area ID mismatch || Routers with mismatched areas never get past Init.",
   "A Hello timer mismatch || Mismatched timers prevent the neighbor from being seen at all.",
   "A passive interface on one side || A passive interface sends no Hellos, so no neighbor appears."],
  "Fix by matching the MTU on both interfaces (or, as a workaround, 'ip ospf mtu-ignore').", "OSPF MTU mismatch")

S(D, "3.4", "ospf", 1, "command", "Which command shows, for each OSPF interface, its area, cost, state (DR/BDR/DROTHER/P2P) and neighbor count?",
  ["*show ip ospf interface brief || It summarizes OSPF-enabled interfaces with area, cost, state and neighbors.",
   "show ip ospf neighbor || It lists neighbors, not per-interface cost.",
   "show ip route ospf || It shows only OSPF-learned routes.",
   "show ip protocols || It shows process-level settings such as router ID and passive interfaces."],
  "Use 'show ip ospf interface brief' for interface status, 'show ip ospf neighbor' for adjacencies and 'show ip protocols' for process settings.",
  "OSPF verification")

S(D, "3.5", "fhrp", 1, "concept", "What problem do first hop redundancy protocols solve?",
  ["*They keep the hosts' default gateway available || Routers share a virtual IP that stays reachable if one router fails.",
   "They prevent Layer 2 loops || That is the job of STP.",
   "They balance traffic across WAN links to the internet || FHRPs protect the first hop, not WAN path selection.",
   "They provide redundant DHCP servers || DHCP redundancy uses separate mechanisms."],
  "HSRP, VRRP and GLBP let two or more routers present one virtual IP/MAC as the hosts' default gateway.", "FHRP purpose")

S(D, "3.5", "fhrp", 2, "diagram", "Refer to the diagram. If R1 fails, what must the hosts change to keep reaching other networks?",
  ["*Nothing; R2 takes over the virtual IP || Hosts keep the same gateway IP and ARP entry.",
   "Their default gateway to 10.1.1.3 || The virtual IP removes the need to reconfigure hosts.",
   "Renew their DHCP leases || DHCP is not involved in FHRP failover.",
   "Clear their ARP caches || The virtual MAC stays the same, so cached entries remain valid."],
  "The new active router sends gratuitous ARPs so switches update their MAC tables for the virtual MAC.", "FHRP failover", dg="fhrp")

S(D, "3.5", "fhrp", 3, "diagram", "Refer to the diagram. When R1 recovers after the failure, will it become the active router again?",
  ["*Yes, because of preempt and priority 110 || Preemption lets the higher-priority router reclaim the active role.",
   "No, because HSRP never preempts || HSRP supports preemption, but it must be configured.",
   "No, because R2 has the higher priority || 100 is lower than 110.",
   "Only if R2 is reloaded || Preemption does not require reloading R2."],
  "HSRP preemption is off by default and must be enabled with 'standby <group> preempt'.", "HSRP preemption", dg="fhrp")

M(D, "3.5", "fhrp", 2, "protocol-id", "Match each description to its first hop redundancy protocol.",
  [("Cisco proprietary; active and standby routers", "HSRP"), ("IETF standard; master and backup routers", "VRRP"),
   ("Cisco proprietary; multiple routers forward at once using different virtual MACs", "GLBP")],
  "HSRP and GLBP are Cisco protocols; VRRP is the open standard. Only GLBP natively load-balances among gateways.", "FHRP comparison")

TF(D, "3.5", "fhrp", 1, "concept", "HSRP preemption is enabled by default.", False,
   "HSRP preemption must be configured; VRRP enables preemption by default.", "HSRP defaults")

S(D, "3.0", "routing-table", 1, "concept", "Which routing protocol is a path-vector protocol used between autonomous systems on the internet?",
  ["*BGP || BGP exchanges reachability with AS-path information between autonomous systems.",
   "OSPF || OSPF is a link-state interior gateway protocol.",
   "RIP || RIP is a distance-vector IGP using hop count.",
   "EIGRP || EIGRP is an advanced distance-vector IGP."],
  "IGPs (OSPF, EIGRP, RIP, IS-IS) route within an organization; BGP routes between autonomous systems.", "Routing protocol types")

# ---------------- additional CCNA-level IP connectivity questions ----------------
RT6 = """R1# show ipv6 route
O   2001:DB8:10::/48 [110/20]
     via FE80::2, GigabitEthernet0/1
S   2001:DB8:10:5::/64 [1/0]
     via 2001:DB8:FF::3
C   2001:DB8:FF::/64 [0/0]
     via GigabitEthernet0/2, directly connected
L   2001:DB8:FF::1/128 [0/0]
     via GigabitEthernet0/2, receive
S   ::/0 [1/0]
     via 2001:DB8:FF::9"""
V6R = [("::/0", "2001:DB8:FF::9"), ("2001:DB8:10::/48", "FE80::2"), ("2001:DB8:10:5::/64", "2001:DB8:FF::3")]
def lpm6(dst):
    import ipaddress
    return max((r for r in V6R if ipaddress.ip_address(dst) in ipaddress.ip_network(r[0])), key=lambda r: ipaddress.ip_network(r[0]).prefixlen)[1]

S(D, "3.1", "routing-table", 3, "routing-table", "Refer to the IPv6 routing table. Which next hop does R1 use for 2001:DB8:10:5::77?",
  ["*2001:DB8:FF::3 || The static /64 is the longest prefix containing the destination.",
   "FE80::2 || The OSPF /48 also matches, but /64 is more specific.",
   "2001:DB8:FF::9 || The default route is used only when nothing else matches.",
   "The packet is delivered directly out Gi0/2 || 2001:DB8:10:5::/64 is not a connected prefix."],
  "Longest prefix match works the same way for IPv6. The fourth hextet (5) places the address inside 2001:DB8:10:5::/64.",
  "IPv6 longest match", ex=RT6, verify=lambda: lpm6("2001:DB8:10:5::77"))

S(D, "3.1", "routing-table", 3, "routing-table", "Refer to the IPv6 routing table. Which destinations are forwarded to FE80::2?",
  ["*2001:DB8:10:6::1 || Inside the /48 and not covered by the more specific static /64.",
   "*2001:DB8:10:FFFF::1 || Still inside 2001:DB8:10::/48 (the fourth hextet can be 0–FFFF).",
   "2001:DB8:10:5::1 || The static 2001:DB8:10:5::/64 is more specific.",
   "2001:DB8:11::1 || The third hextet 11 is outside the /48, so the default route is used."],
  "A /48 fixes the first three hextets (2001:DB8:10); any value in the fourth hextet matches unless a longer prefix exists.",
  "IPv6 prefix matching", ex=RT6,
  verify=lambda: [d for d in ["2001:DB8:10:6::1", "2001:DB8:10:FFFF::1", "2001:DB8:10:5::1", "2001:DB8:11::1"] if lpm6(d) == "FE80::2"])

S(D, "3.1.a", "routing-table", 2, "routing-table", "In 'show ip route', what does the code 'O IA' indicate?",
  ["*An OSPF inter-area route learned from another area || IA routes come from summary LSAs created by an ABR.",
   "An OSPF route inside the router's own area || Intra-area routes are shown with just 'O'.",
   "An external route redistributed into OSPF || External routes are shown as O E1 or O E2.",
   "An inactive OSPF route || Inactive routes are not shown in the routing table."],
  "CCNA configuration focuses on single-area OSPF, but you should still recognize O, O IA and O E2 in routing tables.",
  "OSPF route codes")

S(D, "3.4", "ospf", 3, "config", "R1 uses 'network 10.0.12.0 0.0.0.3 area 0'. R2's connected interface 10.0.12.2/30 uses 'ip ospf 1 area 1'. Both processes are otherwise correct. Will they become neighbors?",
  ["*No, because the area IDs do not match || Hellos carry the area ID, and mismatched areas are rejected.",
   "Yes, because the network statement and interface command are interchangeable || They are interchangeable, but the areas still differ.",
   "Yes, because process IDs match || Process IDs never need to match; areas do.",
   "No, because one router uses a network statement and the other uses an interface command || Mixing methods works fine."],
  "The two configuration methods are equivalent; what matters is that both sides place the link in the same area.",
  "OSPF area matching")

S(D, "3.4", "ospf", 2, "routing-table", "'show ip ospf neighbor' shows neighbor 2.2.2.2 in state 'FULL/ -' on Gi0/1. What does the dash mean?",
  ["*The link is point-to-point, so there is no DR/BDR || Point-to-point links do not elect a DR.",
   "The neighbor is failing || FULL means the adjacency is complete.",
   "The neighbor is a DROTHER || DROTHERs appear as DROTHER.",
   "The neighbor's priority is 0 on a broadcast network || It would show FULL/DROTHER or 2WAY/DROTHER."],
  "Serial links and Ethernet links set to 'ip ospf network point-to-point' show FULL/- neighbors.", "OSPF point-to-point")

S(D, "3.3", "static", 3, "scenario",
  "R1 has 'ip route 10.70.0.0 255.255.0.0 10.0.12.2' and 'ip route 10.70.0.0 255.255.0.0 10.0.13.3'. Both next hops are reachable. What does the routing table contain?",
  ["*Both static routes, and traffic is load-shared across them || Equal prefix, AD and metric → both are installed.",
   "Only the first route that was configured || IOS installs equal static routes together.",
   "Only the route with the lower next-hop address || Next-hop address is not a tiebreaker.",
   "Neither route, because they conflict || Parallel static routes are valid."],
  "Equal static routes provide simple load sharing. Give one a higher AD to make it a floating backup instead.",
  "Parallel static routes")

S(D, "3.3", "static", 2, "config",
  "R1 has 'ipv6 route ::/0 2001:DB8:1::1' and 'ipv6 route ::/0 2001:DB8:2::1 200'. Both next hops are reachable. Which route is used?",
  ["*The route via 2001:DB8:1::1 || It has the default AD of 1; the other is a floating route with AD 200.",
   "The route via 2001:DB8:2::1 || AD 200 is less preferred.",
   "Both, load-shared || Their ADs differ, so only one is installed.",
   "Neither, because IPv6 does not support floating routes || Floating static routes work for IPv6 too."],
  "The floating route is installed only if the primary route disappears, for example when its next hop becomes unreachable.",
  "IPv6 floating static route")

S(D, "3.5", "fhrp", 3, "scenario",
  "HSRP group 10 has no preempt configured. R2 (priority 100) is active. R1 (priority 120) boots and joins the group. Which router is active?",
  ["*R2 remains active || Without preemption, a higher-priority router does not take over an established active router.",
   "R1 becomes active immediately || That requires 'standby 10 preempt' on R1.",
   "Both routers become active || HSRP has exactly one active router per group.",
   "Neither; an election restarts and traffic stops || A router joining does not interrupt the active router."],
  "Configure preempt on the router that should always be active, usually with a delay so routing can converge first.",
  "HSRP preemption")

S(D, "3.2", "routing-table", 3, "routing-table",
  "Refer to the routing table. An engineer adds 'ip route 10.1.4.0 255.255.252.0 10.0.14.4'. What happens to traffic for 10.1.6.1?",
  ["*It goes to 10.0.14.4 (static AD 1 wins) || Same prefix, so the lower AD wins.",
   "It still goes to 10.0.13.3, because OSPF learned the route first || Learning order does not matter.",
   "It is load-shared between 10.0.13.3 and 10.0.14.4 || Routes with different ADs are not combined.",
   "It goes to the default route || A /22 route still matches."],
  "When two sources offer exactly the same prefix, only the lowest-AD route is installed. The OSPF route remains in the OSPF database as a backup.",
  "AD with identical prefixes", ex=RT1)

S(D, "3.4.e", "ospf", 3, "troubleshooting",
  "An engineer enters 'router-id 5.5.5.5' under a running OSPF process, but 'show ip protocols' still shows the old router ID. Why?",
  ["*The OSPF process must be restarted || IOS warns that a reload or process clear is required.",
   "Router IDs must match a loopback address || Any unique 32-bit value is allowed.",
   "Manual router IDs are ignored when a loopback exists || A manual router-id always has the highest preference.",
   "The router ID can only be changed by removing the network statements || Only a process restart is required."],
  "Changing the router ID resets adjacencies, so plan it during a maintenance window.", "OSPF router ID changes")
