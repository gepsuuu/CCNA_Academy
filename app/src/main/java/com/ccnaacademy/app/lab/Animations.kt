package com.ccnaacademy.app.lab

/*
 * Step-based concept demonstrations. Each animation is a scripted explanation (clearly labelled "Concept demonstration"
 * in the UI) and never claims to reflect the learner's lab. Every step has a caption that explains what happens and why,
 * so the static step-by-step view carries the same information as the motion.
 *
 * Canvas coordinates: 1000 x 560.
 */

data class ANode(val id: String, val label: String, val sub: String = "", val kind: String = "neutral", val x: Float, val y: Float, val w: Float = 150f, val h: Float = 70f)
data class AEdge(val a: String, val b: String, val label: String = "", val kind: String = "neutral", val style: String = "solid")
data class APacket(val from: String, val to: String, val label: String, val kind: String = "accent")
data class ATable(val id: String, val title: String, val headers: List<String>, val rows: List<List<String>> = emptyList(), val ordered: Boolean = false)
/** Table operation: add a row, or update/remove the row whose first column equals row[0]. */
data class TOp(val table: String, val row: List<String>, val op: String = "add")

data class AStep(
    val title: String,
    val text: String,
    val packets: List<APacket> = emptyList(),
    val nodes: List<String> = emptyList(),            // highlighted devices
    val ops: List<TOp> = emptyList(),
    val rows: Map<String, List<Int>> = emptyMap(),    // highlighted rows (indices in the table state after this step)
    val drop: String? = null,                         // node where traffic is dropped / blocked
    val badges: Map<String, String> = emptyMap(),     // node id -> short status label shown on the node
    val links: Map<String, String> = emptyMap()       // "a|b" -> status kind for link colouring: ok | danger | warn | accent
)

data class Anim(
    val id: String, val title: String, val lessonIds: List<String>, val question: String,
    val nodes: List<ANode>, val edges: List<AEdge>, val tables: List<ATable>, val steps: List<AStep>,
    val note: String = ""
) {
    /** Table contents after applying the operations of steps 0..index. */
    fun tablesAt(index: Int): List<ATable> = tables.map { t ->
        var rows = t.rows
        steps.take(index + 1).flatMap { it.ops }.filter { it.table == t.id }.forEach { op ->
            rows = when (op.op) {
                "add" -> rows + listOf(op.row)
                "update" -> rows.map { if (it.firstOrNull() == op.row.first()) op.row else it }
                "remove" -> rows.filterNot { it.firstOrNull() == op.row.first() }
                else -> rows
            }
        }
        t.copy(rows = rows)
    }
    fun edge(a: String, b: String) = edges.firstOrNull { (it.a == a && it.b == b) || (it.a == b && it.b == a) }
}

private fun n(id: String, label: String, sub: String, kind: String, x: Float, y: Float, w: Float = 150f, h: Float = 70f) = ANode(id, label, sub, kind, x, y, w, h)
private fun p(from: String, to: String, label: String, kind: String = "accent") = APacket(from, to, label, kind)
private fun lk(a: String, b: String) = "$a|$b"

object AnimationCatalog {

    // ------------------------------------------------------------------ Encapsulation
    private val encapsulation = Anim(
        "anim_encap", "Encapsulation and decapsulation across a router", listOf("1-03"),
        "Which headers stay the same end to end, and which are rebuilt at every hop?",
        nodes = listOf(n("PC", "PC", "192.168.1.10\nMAC 0A:0A", "host", 60f, 230f), n("R1", "R1", "Gi0/0 192.168.1.1\nGi0/1 10.0.0.1", "router", 425f, 230f),
            n("SRV", "Web server", "203.0.113.80\nvia ISP", "server", 790f, 230f)),
        edges = listOf(AEdge("PC", "R1", "Ethernet LAN"), AEdge("R1", "SRV", "Ethernet WAN")),
        tables = listOf(ATable("pdu", "Headers on the wire", listOf("Hop", "L2 src → dst", "L3 src → dst", "L4 ports", "TTL"))),
        steps = listOf(
            AStep("Application data", "The browser creates an HTTPS request. Upper layers hand data to the transport layer.", nodes = listOf("PC"), badges = mapOf("PC" to "Data")),
            AStep("Transport: segment", "TCP adds a header with source port 49152 (ephemeral) and destination port 443. Data + TCP header = segment.", nodes = listOf("PC"), badges = mapOf("PC" to "Segment")),
            AStep("Network: packet", "IP adds source 192.168.1.10 and destination 203.0.113.80. These addresses normally stay the same end to end (unless NAT is used). TTL starts at 64 on many hosts.",
                nodes = listOf("PC"), badges = mapOf("PC" to "Packet")),
            AStep("Data link: frame", "The destination is off-subnet, so the PC uses the default gateway's MAC (learned by ARP) as the destination MAC. Ethernet adds a header and an FCS trailer.",
                nodes = listOf("PC"), badges = mapOf("PC" to "Frame"), ops = listOf(TOp("pdu", listOf("PC → R1", "0A:0A → R1-Gi0/0", "192.168.1.10 → 203.0.113.80", "49152 → 443", "64"))), rows = mapOf("pdu" to listOf(0))),
            AStep("Physical: bits", "The NIC sends the frame as signals on the cable.", packets = listOf(p("PC", "R1", "Frame (L2+L3+L4+data)"))),
            AStep("R1 decapsulates Layer 2", "R1 checks the FCS, removes the Ethernet header and reads the IP header. The destination MAC was R1's own, so the frame was for R1.",
                nodes = listOf("R1"), badges = mapOf("R1" to "Read L3")),
            AStep("R1 routes and re-encapsulates", "R1 looks up 203.0.113.80, decrements TTL to 63 and builds a NEW Ethernet header with its outgoing interface MAC as source and the next hop's MAC as destination. IP addresses and TCP ports are unchanged.",
                nodes = listOf("R1"), ops = listOf(TOp("pdu", listOf("R1 → next hop", "R1-Gi0/1 → next-hop MAC", "192.168.1.10 → 203.0.113.80", "49152 → 443", "63"))), rows = mapOf("pdu" to listOf(1)),
                packets = listOf(p("R1", "SRV", "New frame, same packet"))),
            AStep("Server decapsulates", "The server removes each header in turn — Ethernet, IP, TCP — and passes the data to the web service listening on port 443.",
                nodes = listOf("SRV"), badges = mapOf("SRV" to "Data"))
        ),
        note = "Rule of thumb: Layer 2 addresses change at every router hop; Layer 3 addresses and Layer 4 ports stay the same end to end unless NAT/PAT rewrites them."
    )

    // ------------------------------------------------------------------ ARP and switching
    private val arp = Anim(
        "anim_arp_switch", "ARP and Ethernet switching", listOf("1-05", "1-14"),
        "How does PC-A find PC-B's MAC address, and when does the switch flood instead of forwarding to one port?",
        nodes = listOf(n("SW", "SW1", "VLAN 1", "switch", 425f, 60f), n("A", "PC-A", "10.0.0.1\nAAAA.AAAA.AAAA", "host", 80f, 360f),
            n("B", "PC-B", "10.0.0.2\nBBBB.BBBB.BBBB", "host", 425f, 360f), n("C", "PC-C", "10.0.0.3\nCCCC.CCCC.CCCC", "host", 770f, 360f)),
        edges = listOf(AEdge("SW", "A", "Fa0/1"), AEdge("SW", "B", "Fa0/2"), AEdge("SW", "C", "Fa0/3")),
        tables = listOf(ATable("mac", "SW1 MAC address table", listOf("MAC address", "Port", "Type")), ATable("arp", "PC-A ARP cache", listOf("IP address", "MAC address"))),
        steps = listOf(
            AStep("PC-A needs a MAC address", "PC-A wants to send to 10.0.0.2, which is in its own subnet. Its ARP cache has no entry, so it must ask.", nodes = listOf("A")),
            AStep("ARP request (broadcast)", "PC-A sends 'Who has 10.0.0.2? Tell 10.0.0.1' to FFFF.FFFF.FFFF. The switch learns the SOURCE MAC AAAA.AAAA.AAAA on Fa0/1.",
                packets = listOf(p("A", "SW", "ARP request → FFFF.FFFF.FFFF", "warn")), ops = listOf(TOp("mac", listOf("aaaa.aaaa.aaaa", "Fa0/1", "DYNAMIC"))), rows = mapOf("mac" to listOf(0))),
            AStep("Flooding", "A broadcast is flooded out every port in the VLAN except the one it arrived on.",
                packets = listOf(p("SW", "B", "ARP request", "warn"), p("SW", "C", "ARP request", "warn"))),
            AStep("Only the owner answers", "PC-C sees its IP is not the target and discards the request. PC-B recognises 10.0.0.2 and caches PC-A's mapping from the request.",
                nodes = listOf("B"), badges = mapOf("C" to "Ignores", "B" to "That's me")),
            AStep("ARP reply (unicast)", "PC-B replies directly to AAAA.AAAA.AAAA. The switch learns BBBB.BBBB.BBBB on Fa0/2 and, because AAAA is known, forwards only out Fa0/1.",
                packets = listOf(p("B", "SW", "ARP reply → AAAA", "ok"), p("SW", "A", "ARP reply", "ok")),
                ops = listOf(TOp("mac", listOf("bbbb.bbbb.bbbb", "Fa0/2", "DYNAMIC"))), rows = mapOf("mac" to listOf(1))),
            AStep("PC-A caches the answer", "PC-A stores 10.0.0.2 → BBBB.BBBB.BBBB and can now build the Ethernet frame.",
                ops = listOf(TOp("arp", listOf("10.0.0.2", "bbbb.bbbb.bbbb"))), rows = mapOf("arp" to listOf(0)), nodes = listOf("A")),
            AStep("Known unicast", "The data frame to BBBB is forwarded only out Fa0/2. PC-C never sees it. Forward/filter decisions use the DESTINATION MAC.",
                packets = listOf(p("A", "SW", "Data → BBBB", "ok"), p("SW", "B", "Data", "ok")), rows = mapOf("mac" to listOf(1))),
            AStep("Unknown unicast and aging", "If a destination MAC is not in the table (for example after it aged out — 300 seconds by default), the switch floods the frame like a broadcast until it learns the MAC again.",
                badges = mapOf("SW" to "Aging 300 s"))
        ),
        note = "Switches learn from source MACs and forward on destination MACs. Broadcasts, and unicasts to unknown MACs, are flooded within the VLAN."
    )

    // ------------------------------------------------------------------ VLANs and trunks
    private val vlanTrunk = Anim(
        "anim_vlan_trunk", "VLANs, 802.1Q tagging and the native VLAN", listOf("2-01", "2-02"),
        "Why does a broadcast from PC-A reach PC-C but not PC-B, and what happens to the tag on the trunk?",
        nodes = listOf(n("SW1", "SW1", "native 99", "switch", 250f, 80f), n("SW2", "SW2", "native 99", "switch", 600f, 80f),
            n("A", "PC-A", "VLAN 10", "vlan10", 80f, 380f), n("B", "PC-B", "VLAN 20", "vlan20", 330f, 380f),
            n("C", "PC-C", "VLAN 10", "vlan10", 530f, 380f), n("D", "PC-D", "VLAN 20", "vlan20", 780f, 380f)),
        edges = listOf(AEdge("SW1", "SW2", "Gi0/1 trunk: 10,20,99", "accent"), AEdge("SW1", "A", "Fa0/1 acc 10"), AEdge("SW1", "B", "Fa0/2 acc 20"),
            AEdge("SW2", "C", "Fa0/1 acc 10"), AEdge("SW2", "D", "Fa0/2 acc 20")),
        tables = listOf(ATable("trunk", "Frames on the trunk", listOf("Frame", "VLAN", "On the trunk", "Receiving switch places it in"))),
        steps = listOf(
            AStep("Two broadcast domains", "VLAN 10 and VLAN 20 are separate broadcast domains, even though they share the same switches.", nodes = listOf("A", "C"), badges = mapOf("A" to "VLAN 10", "B" to "VLAN 20")),
            AStep("Untagged on the access port", "PC-A sends a broadcast. Hosts send normal untagged frames; SW1 assigns it to VLAN 10 because Fa0/1 is an access port in VLAN 10.",
                packets = listOf(p("A", "SW1", "Broadcast (untagged)", "vlan10"))),
            AStep("Flooded only within VLAN 10", "SW1 floods the frame out ports in VLAN 10 and the trunk. PC-B on Fa0/2 is in VLAN 20, so it never receives it.",
                packets = listOf(p("SW1", "SW2", "802.1Q tag: VLAN 10", "vlan10")), badges = mapOf("B" to "Not flooded"),
                ops = listOf(TOp("trunk", listOf("PC-A broadcast", "10", "Tagged (VLAN ID 10)", "VLAN 10"))), rows = mapOf("trunk" to listOf(0))),
            AStep("Tag removed at the far end", "SW2 reads the 4-byte 802.1Q tag (12-bit VLAN ID = 10), removes it and floods the frame out its VLAN 10 access ports.",
                packets = listOf(p("SW2", "C", "Untagged", "vlan10")), badges = mapOf("D" to "Not flooded")),
            AStep("Native VLAN frames are untagged", "Frames in the native VLAN (99 on both ends) cross the trunk without a tag. The receiver puts untagged frames into ITS native VLAN.",
                packets = listOf(p("SW1", "SW2", "Untagged (native 99)", "vlan99")),
                ops = listOf(TOp("trunk", listOf("Native VLAN frame", "99", "Untagged", "VLAN 99 (native matches)"))), rows = mapOf("trunk" to listOf(1))),
            AStep("Native VLAN mismatch", "If SW2's native VLAN were 1, the untagged VLAN 99 frame would land in VLAN 1: traffic leaks between VLANs. CDP reports a native VLAN mismatch, and the trunk keeps running.",
                packets = listOf(p("SW1", "SW2", "Untagged — lands in VLAN 1!", "danger")), drop = "SW2", badges = mapOf("SW2" to "native 1?"),
                ops = listOf(TOp("trunk", listOf("Mismatch example", "99", "Untagged", "VLAN 1 — wrong VLAN"))), rows = mapOf("trunk" to listOf(2)), links = mapOf(lk("SW1", "SW2") to "danger")),
            AStep("Different VLANs need a router", "PC-A (VLAN 10) and PC-B (VLAN 20) are in different subnets and broadcast domains. Without a router or Layer 3 switch they cannot communicate.",
                nodes = listOf("A", "B"), drop = "SW1", badges = mapOf("SW1" to "No routing"))
        ),
        note = "Trunks tag every VLAN except the native VLAN. Both ends must agree on the native VLAN and must allow every VLAN that has to cross."
    )

    // ------------------------------------------------------------------ Inter-VLAN routing
    private val roas = Anim(
        "anim_intervlan", "Inter-VLAN routing: router-on-a-stick", listOf("2-03"),
        "What changes in the frame when PC-A in VLAN 10 sends to PC-B in VLAN 20?",
        nodes = listOf(n("R1", "R1", "Gi0/0.10 .10.1\nGi0/0.20 .20.1", "router", 425f, 40f, 170f), n("SW1", "SW1", "Gi0/2 trunk", "switch", 425f, 230f),
            n("A", "PC-A", "192.168.10.11\nVLAN 10", "vlan10", 150f, 410f), n("B", "PC-B", "192.168.20.11\nVLAN 20", "vlan20", 700f, 410f)),
        edges = listOf(AEdge("R1", "SW1", "802.1Q trunk", "accent"), AEdge("SW1", "A", "Fa0/1 acc 10"), AEdge("SW1", "B", "Fa0/2 acc 20")),
        tables = listOf(ATable("hdr", "Header changes per hop", listOf("Hop", "Src MAC", "Dst MAC", "802.1Q", "Src IP → Dst IP", "TTL"), ordered = true)),
        steps = listOf(
            AStep("Off-subnet decision", "PC-A compares 192.168.20.11 with its own subnet 192.168.10.0/24. It is remote, so PC-A sends the frame to its default gateway 192.168.10.1 (R1's Gi0/0.10).", nodes = listOf("A")),
            AStep("Ingress: access port", "The frame is untagged from PC-A. Destination MAC = R1's Gi0/0 MAC; destination IP = PC-B.",
                packets = listOf(p("A", "SW1", "Dst MAC R1, Dst IP .20.11", "vlan10")),
                ops = listOf(TOp("hdr", listOf("PC-A → SW1", "PC-A", "R1 Gi0/0", "none", "192.168.10.11 → 192.168.20.11", "64"))), rows = mapOf("hdr" to listOf(0))),
            AStep("Up the trunk with tag 10", "SW1 forwards the frame to R1 on the trunk and adds an 802.1Q tag for VLAN 10.",
                packets = listOf(p("SW1", "R1", "Tag 10", "vlan10")), ops = listOf(TOp("hdr", listOf("SW1 → R1", "PC-A", "R1 Gi0/0", "VLAN 10", "192.168.10.11 → 192.168.20.11", "64"))), rows = mapOf("hdr" to listOf(1))),
            AStep("Routing decision", "R1 accepts the frame on Gi0/0.10 (encapsulation dot1Q 10), strips Layer 2, and finds 192.168.20.0/24 directly connected on Gi0/0.20. TTL is decremented.",
                nodes = listOf("R1"), badges = mapOf("R1" to "C 192.168.20.0/24")),
            AStep("Egress: new Layer 2 header", "R1 rewrites the frame: source MAC = R1, destination MAC = PC-B (from ARP), tag = VLAN 20. It goes back down the SAME physical link.",
                packets = listOf(p("R1", "SW1", "Tag 20", "vlan20")), ops = listOf(TOp("hdr", listOf("R1 → SW1", "R1 Gi0/0", "PC-B", "VLAN 20", "192.168.10.11 → 192.168.20.11", "63"))), rows = mapOf("hdr" to listOf(2))),
            AStep("Delivered untagged", "SW1 removes the tag and sends the frame out Fa0/2 in VLAN 20. The IP addresses never changed; the MACs and the VLAN tag did.",
                packets = listOf(p("SW1", "B", "Untagged", "vlan20")), ops = listOf(TOp("hdr", listOf("SW1 → PC-B", "R1 Gi0/0", "PC-B", "none", "192.168.10.11 → 192.168.20.11", "63"))), rows = mapOf("hdr" to listOf(3)))
        ),
        note = "Every inter-VLAN packet crosses the router link twice (in and out), which is why a single router-on-a-stick link can become a bottleneck. Layer 3 switches with SVIs route internally."
    )

    // ------------------------------------------------------------------ STP
    private val stp = Anim(
        "anim_stp", "Spanning Tree: root election, port roles and a link failure", listOf("2-06"),
        "Which port blocks in a triangle of switches, and what happens when a link fails?",
        nodes = listOf(n("S1", "SW1", "BID 32769.0011.1111.1111", "switch", 400f, 40f, 200f), n("S2", "SW2", "BID 32769.0022.2222.2222", "switch", 100f, 380f, 200f),
            n("S3", "SW3", "BID 32769.0033.3333.3333", "switch", 700f, 380f, 200f)),
        edges = listOf(AEdge("S1", "S2", "1 Gbps (cost 4)"), AEdge("S1", "S3", "1 Gbps (cost 4)"), AEdge("S2", "S3", "1 Gbps (cost 4)")),
        tables = listOf(ATable("roles", "Port roles (VLAN 1)", listOf("Port", "Role", "State"),
            rows = listOf(listOf("SW1 Gi0/1", "?", "?"), listOf("SW1 Gi0/2", "?", "?"), listOf("SW2 Gi0/1", "?", "?"), listOf("SW2 Gi0/2", "?", "?"), listOf("SW3 Gi0/1", "?", "?"), listOf("SW3 Gi0/2", "?", "?")))),
        steps = listOf(
            AStep("A physical loop", "Three switches in a triangle give redundancy, but a broadcast could circulate forever. STP logically blocks one port so there is exactly one active path between any two switches.",
                links = mapOf(lk("S1", "S2") to "warn", lk("S1", "S3") to "warn", lk("S2", "S3") to "warn")),
            AStep("Root bridge election", "Switches exchange BPDUs. The lowest Bridge ID (priority, then MAC) wins. All priorities are 32768 + VLAN 1 = 32769, so the lowest MAC (SW1) becomes root.",
                packets = listOf(p("S1", "S2", "BPDU", "accent"), p("S1", "S3", "BPDU", "accent")), nodes = listOf("S1"), badges = mapOf("S1" to "ROOT"),
                ops = listOf(TOp("roles", listOf("SW1 Gi0/1", "Designated", "Forwarding"), "update"), TOp("roles", listOf("SW1 Gi0/2", "Designated", "Forwarding"), "update")), rows = mapOf("roles" to listOf(0, 1))),
            AStep("Root ports", "Each non-root switch picks the port with the lowest cost to the root. SW2 and SW3 both reach SW1 directly at cost 4.",
                ops = listOf(TOp("roles", listOf("SW2 Gi0/1", "Root", "Forwarding"), "update"), TOp("roles", listOf("SW3 Gi0/1", "Root", "Forwarding"), "update")), rows = mapOf("roles" to listOf(2, 4)),
                badges = mapOf("S1" to "ROOT", "S2" to "RP Gi0/1", "S3" to "RP Gi0/1")),
            AStep("Designated port on SW2–SW3", "On the SW2–SW3 segment both switches advertise cost 4 to the root. The tie is broken by the lower sender Bridge ID: SW2. SW2 Gi0/2 is designated; SW3 Gi0/2 is not.",
                ops = listOf(TOp("roles", listOf("SW2 Gi0/2", "Designated", "Forwarding"), "update"), TOp("roles", listOf("SW3 Gi0/2", "Alternate", "Blocking (802.1D) / Discarding (RSTP)"), "update")),
                rows = mapOf("roles" to listOf(3, 5)), drop = "S3", links = mapOf(lk("S2", "S3") to "danger"), badges = mapOf("S1" to "ROOT", "S3" to "Gi0/2 blocks")),
            AStep("Loop-free tree", "Traffic from SW3 to SW2 now goes via the root. The blocked port still receives BPDUs so it can react to failures.",
                links = mapOf(lk("S1", "S2") to "ok", lk("S1", "S3") to "ok", lk("S2", "S3") to "danger"), badges = mapOf("S1" to "ROOT")),
            AStep("Link failure: SW1–SW2", "The SW1–SW2 link fails. SW2 loses its root port. Its best remaining path to the root is via SW3 (4 + 4 = 8).",
                links = mapOf(lk("S1", "S2") to "danger"), drop = "S2", badges = mapOf("S1" to "ROOT", "S2" to "Lost RP"),
                ops = listOf(TOp("roles", listOf("SW1 Gi0/1", "—", "Link down"), "update"), TOp("roles", listOf("SW2 Gi0/1", "—", "Link down"), "update")), rows = mapOf("roles" to listOf(0, 2))),
            AStep("Reconvergence", "SW2 Gi0/2 becomes its root port and SW3 Gi0/2 moves to designated/forwarding. Timing is simplified here: classic 802.1D can take up to about 50 s (max age 20 s + 2 × forward delay 15 s); RSTP usually converges in a few seconds.",
                links = mapOf(lk("S1", "S3") to "ok", lk("S2", "S3") to "ok", lk("S1", "S2") to "danger"), badges = mapOf("S1" to "ROOT", "S2" to "RP Gi0/2"),
                ops = listOf(TOp("roles", listOf("SW2 Gi0/2", "Root", "Forwarding"), "update"), TOp("roles", listOf("SW3 Gi0/2", "Designated", "Forwarding"), "update")), rows = mapOf("roles" to listOf(3, 5)))
        ),
        note = "802.1D states: blocking, listening, learning, forwarding (disabled). RSTP collapses them to discarding, learning, forwarding and adds alternate/backup roles for fast failover. Rapid PVST+ runs one RSTP instance per VLAN."
    )

    // ------------------------------------------------------------------ EtherChannel
    private val etherchannel = Anim(
        "anim_etherchannel", "EtherChannel: one logical link from many", listOf("2-05"),
        "Why doesn't STP block the second link, and does one flow use both links?",
        nodes = listOf(n("S1", "SW1", "Gi0/1-2 → Po1\nLACP active", "switch", 120f, 200f, 180f), n("S2", "SW2", "Gi0/1-2 → Po1\nLACP passive", "switch", 700f, 200f, 180f)),
        edges = listOf(AEdge("S1", "S2", "Gi0/1 + Gi0/2 = Port-channel 1", "accent")),
        tables = listOf(ATable("flows", "Load distribution (src-mac hash, illustrative)", listOf("Flow", "Source MAC", "Member link"))),
        steps = listOf(
            AStep("Without EtherChannel", "Two parallel links form a Layer 2 loop, so STP blocks one of them. You paid for 2 Gbps but can use 1 Gbps.", drop = "S2", badges = mapOf("S2" to "1 port blocked")),
            AStep("LACP negotiation", "With 'channel-group 1 mode active' on SW1 and 'mode passive' on SW2, LACPDUs are exchanged. Active + passive (or active + active) forms a channel; passive + passive never does. Speed, duplex, VLAN and trunk settings must match.",
                packets = listOf(p("S1", "S2", "LACPDU", "accent"), p("S2", "S1", "LACPDU", "accent"))),
            AStep("One logical interface", "Port-channel 1 appears to STP as a single link, so nothing is blocked and both members carry traffic.", links = mapOf(lk("S1", "S2") to "ok"), badges = mapOf("S1" to "Po1 (SU)", "S2" to "Po1 (SU)")),
            AStep("Flows are hashed", "The switch hashes fields (for example the source MAC) to choose a member link. Flow 1 hashes to Gi0/1.",
                packets = listOf(p("S1", "S2", "Flow 1 → Gi0/1", "vlan10")), ops = listOf(TOp("flows", listOf("Flow 1", "AAAA.AAAA.AAAA", "Gi0/1"))), rows = mapOf("flows" to listOf(0))),
            AStep("A different flow, a different link", "Flow 2 hashes to Gi0/2. Each individual flow stays on one member link (so frames stay in order); load sharing happens across many flows.",
                packets = listOf(p("S1", "S2", "Flow 2 → Gi0/2", "vlan20")), ops = listOf(TOp("flows", listOf("Flow 2", "BBBB.BBBB.BBBB", "Gi0/2"))), rows = mapOf("flows" to listOf(1))),
            AStep("Member failure", "If Gi0/2 fails, Flow 2 is re-hashed onto Gi0/1. Po1 stays up and STP does not reconverge.",
                ops = listOf(TOp("flows", listOf("Flow 2", "BBBB.BBBB.BBBB", "Gi0/1 (re-hashed)"), "update")), rows = mapOf("flows" to listOf(1)), badges = mapOf("S1" to "Po1 still up"))
        ),
        note = "The hash method is platform-dependent (src-mac is the default on many Catalyst access switches). PAgP is Cisco's protocol (desirable/auto); LACP (802.3ad) uses active/passive; 'mode on' forces a channel without negotiation."
    )

    // ------------------------------------------------------------------ Routing / longest prefix
    private val routing = Anim(
        "anim_routing_lpm", "Routing decision: longest-prefix match", listOf("3-01", "3-02"),
        "When several routes match, which one does the router use?",
        nodes = listOf(n("R1", "R1", "making the decision", "router", 400f, 220f, 170f), n("ISP", "ISP", "203.0.113.1", "cloud", 780f, 30f),
            n("R2", "R2", "10.0.12.2", "router", 780f, 160f), n("R3", "R3", "10.0.13.2", "router", 780f, 290f), n("R4", "R4", "10.0.14.2", "router", 780f, 420f)),
        edges = listOf(AEdge("R1", "ISP", "Gi0/0"), AEdge("R1", "R2", "Gi0/1"), AEdge("R1", "R3", "Gi0/2"), AEdge("R1", "R4", "Gi0/3")),
        tables = listOf(ATable("rt", "R1 routing table", listOf("Code", "Prefix", "AD/metric", "Next hop"), ordered = true, rows = listOf(
            listOf("S*", "0.0.0.0/0", "1/0", "203.0.113.1"), listOf("S", "10.0.0.0/8", "1/0", "10.0.12.2"),
            listOf("O", "10.1.0.0/16", "110/2", "10.0.13.2"), listOf("S", "10.1.1.0/24", "1/0", "10.0.14.2")))),
        steps = listOf(
            AStep("Packet to 10.1.1.50", "R1 must forward a packet to 10.1.1.50. It compares the destination with every prefix in the table.", nodes = listOf("R1"), badges = mapOf("R1" to "dst 10.1.1.50")),
            AStep("Four routes match", "0.0.0.0/0, 10.0.0.0/8, 10.1.0.0/16 and 10.1.1.0/24 all contain 10.1.1.50.", rows = mapOf("rt" to listOf(0, 1, 2, 3)), badges = mapOf("R1" to "4 matches")),
            AStep("Longest prefix wins", "The /24 is the most specific match, so R1 uses it — even though the OSPF /16 has a different AD. AD only compares routes to the SAME prefix from different sources; metric compares routes from the same protocol.",
                rows = mapOf("rt" to listOf(3)), packets = listOf(p("R1", "R4", "to 10.1.1.50 via /24", "ok"))),
            AStep("Packet to 10.2.3.4", "Only 0.0.0.0/0 and 10.0.0.0/8 match. The /8 is longer, so the packet goes to R2.",
                rows = mapOf("rt" to listOf(1)), packets = listOf(p("R1", "R2", "to 10.2.3.4 via /8", "ok")), badges = mapOf("R1" to "dst 10.2.3.4")),
            AStep("Packet to 8.8.8.8", "Only the default route matches (/0 matches everything), so the packet goes to the ISP.",
                rows = mapOf("rt" to listOf(0)), packets = listOf(p("R1", "ISP", "to 8.8.8.8 via default", "ok")), badges = mapOf("R1" to "dst 8.8.8.8")),
            AStep("No match, no default", "Without the default route, a packet to 8.8.8.8 would match nothing and be dropped (the router can send ICMP 'destination unreachable').",
                drop = "R1", badges = mapOf("R1" to "Drop"))
        ),
        note = "Order of decisions: 1) longest prefix match; 2) for the same prefix from different sources, lowest administrative distance; 3) for the same prefix from the same protocol, lowest metric."
    )

    // ------------------------------------------------------------------ OSPF
    private val ospf = Anim(
        "anim_ospf", "OSPF: from neighbor to Full adjacency", listOf("3-03"),
        "What has to match for two routers to become OSPF neighbors, and what do the states mean?",
        nodes = listOf(n("R1", "R1", "RID 1.1.1.1\nArea 0", "router", 120f, 200f, 170f), n("R2", "R2", "RID 2.2.2.2\nArea 0", "router", 700f, 200f, 170f)),
        edges = listOf(AEdge("R1", "R2", "10.0.12.0/30 point-to-point, cost 1")),
        tables = listOf(ATable("nbr", "R1 neighbor table", listOf("Neighbor ID", "State", "What happened"))),
        steps = listOf(
            AStep("Hello", "R1 multicasts a Hello to 224.0.0.5 every 10 s (dead interval 40 s on broadcast/point-to-point). Area, subnet/mask, hello/dead timers, authentication and stub flags must match; router IDs must be unique.",
                packets = listOf(p("R1", "R2", "Hello (RID 1.1.1.1)", "accent"))),
            AStep("Init", "R2 hears R1 but R1 is not yet listed in a Hello from R2.", packets = listOf(p("R2", "R1", "Hello (seen: none)", "accent")),
                ops = listOf(TOp("nbr", listOf("2.2.2.2", "INIT", "Hello received")))),
            AStep("2-Way", "R2's Hello lists 1.1.1.1, so each router sees itself in the other's Hello: bidirectional communication. On multi-access networks DR/BDR election happens here; DROTHERs stay 2-Way with each other.",
                packets = listOf(p("R2", "R1", "Hello (seen: 1.1.1.1)", "ok")), ops = listOf(TOp("nbr", listOf("2.2.2.2", "2-WAY", "Saw own RID"), "update")), rows = mapOf("nbr" to listOf(0))),
            AStep("ExStart and Exchange", "The router with the higher RID (R2) becomes master for the database exchange. They swap Database Description (DBD) packets summarising their LSDBs.",
                packets = listOf(p("R1", "R2", "DBD", "warn"), p("R2", "R1", "DBD", "warn")), ops = listOf(TOp("nbr", listOf("2.2.2.2", "EXCHANGE", "DBDs swapped"), "update")), rows = mapOf("nbr" to listOf(0))),
            AStep("Loading", "Each router requests missing LSAs with LSR and receives them in LSU packets, acknowledged with LSAck.",
                packets = listOf(p("R1", "R2", "LSR", "warn"), p("R2", "R1", "LSU", "warn")), ops = listOf(TOp("nbr", listOf("2.2.2.2", "LOADING", "LSR/LSU"), "update")), rows = mapOf("nbr" to listOf(0))),
            AStep("Full", "The LSDBs are identical. Each router runs SPF (Dijkstra) independently and installs the lowest-cost routes with AD 110.",
                ops = listOf(TOp("nbr", listOf("2.2.2.2", "FULL", "LSDBs synchronised"), "update")), rows = mapOf("nbr" to listOf(0)), links = mapOf(lk("R1", "R2") to "ok"),
                badges = mapOf("R1" to "FULL", "R2" to "FULL")),
            AStep("Cost", "Cost = reference bandwidth ÷ interface bandwidth. With the default 100 Mbps reference, FastEthernet and GigabitEthernet both cost 1 — so many networks set 'auto-cost reference-bandwidth' to a higher value on every router.",
                badges = mapOf("R1" to "cost 1", "R2" to "cost 1"))
        ),
        note = "Concept demonstration only: the CLI trainer does not model OSPF. Use 'show ip ospf neighbor' output questions in the question bank for practice."
    )

    // ------------------------------------------------------------------ DHCP
    private val dhcp = Anim(
        "anim_dhcp", "DHCP: Discover, Offer, Request, Acknowledge", listOf("4-01"),
        "How does a client with no IP address get one, and which messages are broadcasts?",
        nodes = listOf(n("C", "Client", "no IP yet\nMAC 0C:0C", "host", 80f, 220f), n("SW", "SW1", "", "switch", 425f, 220f), n("S", "R1 (DHCP server)", "192.168.1.1\npool 192.168.1.0/24", "server", 770f, 220f, 170f)),
        edges = listOf(AEdge("C", "SW", "Fa0/1"), AEdge("SW", "S", "Gi0/1")),
        tables = listOf(ATable("bind", "R1 DHCP bindings", listOf("IP address", "Client MAC", "Lease")), ATable("cfg", "Client settings", listOf("Setting", "Value"))),
        steps = listOf(
            AStep("Discover", "The client has no address, so it sends from 0.0.0.0 (UDP 68) to the broadcast 255.255.255.255 (UDP 67): 'Is there a DHCP server?'",
                packets = listOf(p("C", "SW", "DISCOVER 0.0.0.0 → 255.255.255.255", "warn"), p("SW", "S", "DISCOVER (flooded)", "warn"))),
            AStep("Offer", "The server reserves 192.168.1.10 and offers it with mask, gateway, DNS and lease time. Depending on the client's broadcast flag the Offer is broadcast or unicast.",
                packets = listOf(p("S", "SW", "OFFER 192.168.1.10", "ok"), p("SW", "C", "OFFER", "ok"))),
            AStep("Request", "The client broadcasts a Request for the offered address, which also tells any other servers that their offers were declined.",
                packets = listOf(p("C", "SW", "REQUEST 192.168.1.10 (broadcast)", "warn"), p("SW", "S", "REQUEST", "warn"))),
            AStep("Acknowledge", "The server confirms the lease and records the binding.", packets = listOf(p("S", "SW", "ACK", "ok"), p("SW", "C", "ACK", "ok")),
                ops = listOf(TOp("bind", listOf("192.168.1.10", "000c.000c.000c", "1 day"))), rows = mapOf("bind" to listOf(0))),
            AStep("Client configured", "The client applies the settings. Lease renewal (T1) starts at 50% of the lease time with a unicast Request to the server.",
                ops = listOf(TOp("cfg", listOf("IP / mask", "192.168.1.10 /24")), TOp("cfg", listOf("Default gateway", "192.168.1.1")), TOp("cfg", listOf("DNS", "192.168.1.1"))),
                rows = mapOf("cfg" to listOf(0, 1, 2)), nodes = listOf("C"), badges = mapOf("C" to "192.168.1.10"))
        ),
        note = "DHCP server: UDP 67. Client: UDP 68. Routers do not forward broadcasts — see the relay animation for servers on another subnet."
    )

    private val dhcpRelay = Anim(
        "anim_dhcp_relay", "DHCP relay with ip helper-address", listOf("4-01"),
        "How can a client get an address from a server on a different subnet when routers block broadcasts?",
        nodes = listOf(n("C", "Client", "VLAN 10\nno IP yet", "host", 60f, 220f), n("R1", "R1 (relay agent)", "Gi0/0 192.168.10.1\nip helper-address 10.1.1.5", "router", 380f, 220f, 210f),
            n("S", "DHCP server", "10.1.1.5\npools for many subnets", "server", 780f, 220f, 170f)),
        edges = listOf(AEdge("C", "R1", "192.168.10.0/24"), AEdge("R1", "S", "routed network")),
        tables = listOf(ATable("pk", "What crosses each segment", listOf("Message", "Source", "Destination", "giaddr"), ordered = true)),
        steps = listOf(
            AStep("Broadcast stops at the router", "The client's Discover is a broadcast. Without a relay, R1 would drop it at the subnet boundary.", packets = listOf(p("C", "R1", "DISCOVER (broadcast)", "warn"))),
            AStep("Relay as unicast", "R1 Gi0/0 has 'ip helper-address 10.1.1.5'. It converts the broadcast into a unicast to the server and fills in giaddr = 192.168.10.1, the interface where the request arrived.",
                packets = listOf(p("R1", "S", "DISCOVER (unicast)", "accent")), ops = listOf(TOp("pk", listOf("Discover", "192.168.10.1", "10.1.1.5", "192.168.10.1"))), rows = mapOf("pk" to listOf(0))),
            AStep("The server chooses the pool", "The server uses giaddr to pick the 192.168.10.0/24 pool and unicasts the Offer back to the relay agent.",
                packets = listOf(p("S", "R1", "OFFER 192.168.10.20", "ok")), ops = listOf(TOp("pk", listOf("Offer", "10.1.1.5", "192.168.10.1", "192.168.10.1"))), rows = mapOf("pk" to listOf(1))),
            AStep("Relay delivers to the client", "R1 forwards the Offer onto the client's subnet. Request and Ack follow the same relay path.",
                packets = listOf(p("R1", "C", "OFFER", "ok"))),
            AStep("Request / Ack", "The client requests the address; R1 relays it; the server acknowledges; the client is configured with gateway 192.168.10.1.",
                packets = listOf(p("C", "R1", "REQUEST", "warn"), p("R1", "S", "REQUEST (relayed)", "accent"), p("S", "R1", "ACK", "ok")), badges = mapOf("C" to "192.168.10.20"))
        ),
        note = "ip helper-address is configured on the interface that RECEIVES the client broadcasts (the client's gateway interface)."
    )

    // ------------------------------------------------------------------ NAT / PAT
    private val pat = Anim(
        "anim_pat", "PAT (NAT overload): addresses and ports", listOf("4-02"),
        "How can two inside hosts share one public address, even when they use the same source port?",
        nodes = listOf(n("A", "PC1", "192.168.1.10", "host", 40f, 100f), n("B", "PC2", "192.168.1.11", "host", 40f, 360f),
            n("R1", "R1 (PAT)", "inside Gi0/0\noutside Gi0/1 203.0.113.5", "router", 380f, 220f, 200f), n("WEB", "Web server", "198.51.100.20:443", "server", 790f, 220f)),
        edges = listOf(AEdge("A", "R1", "inside"), AEdge("B", "R1", "inside"), AEdge("R1", "WEB", "outside / internet")),
        tables = listOf(ATable("nat", "R1 — show ip nat translations", listOf("Pro", "Inside global", "Inside local", "Outside local", "Outside global"))),
        steps = listOf(
            AStep("Inside host sends", "PC1 sends from 192.168.1.10:51000 to 198.51.100.20:443. Private addresses are not routable on the internet.", packets = listOf(p("A", "R1", "192.168.1.10:51000 → .20:443"))),
            AStep("Translate source", "R1 rewrites the source to its outside address 203.0.113.5. The port 51000 is free, so it keeps it, and records the mapping.",
                packets = listOf(p("R1", "WEB", "203.0.113.5:51000 → .20:443", "ok")),
                ops = listOf(TOp("nat", listOf("tcp", "203.0.113.5:51000", "192.168.1.10:51000", "198.51.100.20:443", "198.51.100.20:443"))), rows = mapOf("nat" to listOf(0))),
            AStep("Second host, same port", "PC2 also uses source port 51000. 203.0.113.5:51000 is taken, so R1 picks another port (51001). The port number is what keeps the sessions apart.",
                packets = listOf(p("B", "R1", "192.168.1.11:51000 → .20:443"), p("R1", "WEB", "203.0.113.5:51001 → .20:443", "ok")),
                ops = listOf(TOp("nat", listOf("tcp", "203.0.113.5:51001", "192.168.1.11:51000", "198.51.100.20:443", "198.51.100.20:443"))), rows = mapOf("nat" to listOf(1))),
            AStep("Reply comes back", "The server answers 203.0.113.5:51001. R1 finds that inside global address and port in the table…", packets = listOf(p("WEB", "R1", "→ 203.0.113.5:51001", "accent")), rows = mapOf("nat" to listOf(1))),
            AStep("…and translates the destination", "R1 rewrites the destination to 192.168.1.11:51000 and forwards to PC2.", packets = listOf(p("R1", "B", "→ 192.168.1.11:51000", "ok")), rows = mapOf("nat" to listOf(1)))
        ),
        note = "Inside local = real private address of the inside host. Inside global = how it appears outside. With PAT many inside locals share one inside global, distinguished by port."
    )

    // ------------------------------------------------------------------ ACL
    private val acl = Anim(
        "anim_acl", "ACL evaluation: top-down, first match", listOf("5-03"),
        "Which rule decides a packet's fate, and why does rule order matter?",
        nodes = listOf(n("SRC", "Sources", "192.168.10.0/24\n192.168.20.0/24\n192.168.30.0/24", "host", 40f, 210f, 190f, 110f),
            n("R1", "R1 Gi0/0 IN", "ip access-group 110 in", "router", 400f, 220f, 190f), n("SRV", "Server", "10.1.1.10", "server", 790f, 220f)),
        edges = listOf(AEdge("SRC", "R1", "inbound"), AEdge("R1", "SRV")),
        tables = listOf(ATable("acl", "access-list 110 (evaluated top-down)", listOf("Seq", "Action", "Match"), ordered = true, rows = listOf(
            listOf("10", "permit", "tcp 192.168.10.0 0.0.0.255 host 10.1.1.10 eq 443"), listOf("20", "deny", "tcp 192.168.10.0 0.0.0.255 host 10.1.1.10 eq 22"),
            listOf("30", "deny", "ip 192.168.20.0 0.0.0.255 any"), listOf("40", "permit", "icmp any any"), listOf("—", "deny", "ip any any (implicit, not shown)"))),
            ATable("res", "Results", listOf("Packet", "First match", "Result"))),
        steps = listOf(
            AStep("Packet 1: 192.168.10.5 → 10.1.1.10 TCP 443", "Seq 10 matches the source, destination, protocol and port. Permitted. Evaluation stops at the first match.",
                rows = mapOf("acl" to listOf(0)), packets = listOf(p("SRC", "R1", "TCP 443"), p("R1", "SRV", "permitted", "ok")),
                ops = listOf(TOp("res", listOf(".10.5 → TCP 443", "10", "permit")))),
            AStep("Packet 2: 192.168.10.5 → 10.1.1.10 TCP 22", "Seq 10 does not match (port 22 ≠ 443). Seq 20 matches. Denied.",
                rows = mapOf("acl" to listOf(1)), packets = listOf(p("SRC", "R1", "TCP 22", "danger")), drop = "R1",
                ops = listOf(TOp("res", listOf(".10.5 → TCP 22", "20", "deny")))),
            AStep("Packet 3: 192.168.20.7 → 10.1.1.10 ICMP", "Seq 30 matches every IP packet from 192.168.20.0/24 — including ICMP — so the packet is denied before seq 40 is ever checked. Order matters.",
                rows = mapOf("acl" to listOf(2)), packets = listOf(p("SRC", "R1", "ICMP echo", "danger")), drop = "R1",
                ops = listOf(TOp("res", listOf(".20.7 → ICMP", "30", "deny (40 never reached)")))),
            AStep("Packet 4: 192.168.30.9 → 10.1.1.10 TCP 80", "No configured line matches. Every ACL ends with an implicit 'deny ip any any', so the packet is dropped.",
                rows = mapOf("acl" to listOf(4)), packets = listOf(p("SRC", "R1", "TCP 80", "danger")), drop = "R1",
                ops = listOf(TOp("res", listOf(".30.9 → TCP 80", "implicit", "deny")))),
            AStep("Placement and direction", "This extended ACL is applied INBOUND on the interface closest to the sources, so unwanted traffic is dropped before it crosses the network. Standard ACLs (source only) are placed close to the destination.",
                nodes = listOf("R1"), badges = mapOf("R1" to "IN on Gi0/0"))
        ),
        note = "Wildcard 0.0.0.255 means 'match the first three octets'. One ACL per interface, per direction, per protocol."
    )

    val all: List<Anim> = listOf(encapsulation, arp, vlanTrunk, roas, stp, etherchannel, routing, ospf, dhcp, dhcpRelay, pat, acl)
    val byId = all.associateBy { it.id }
    fun forLesson(lessonId: String) = all.filter { lessonId in it.lessonIds }
}
