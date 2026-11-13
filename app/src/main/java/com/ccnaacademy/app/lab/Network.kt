package com.ccnaacademy.app.lab

/**
 * Derives operational behaviour (link status, trunking, VLAN reachability, routing, ping) from the configured LabState.
 * Everything here is computed, never scripted, so a configuration fault always changes the outcome.
 */
class Network(val st: LabState) {

    // ---------------- Physical / Layer 2 status ----------------

    private fun isPhysical(name: String) = !IfName.isSub(name) && !name.startsWith("Vlan") && !name.startsWith("Loopback")

    /** Physical port is up/up: cabled, not shut on either end. */
    fun linkUp(devId: String, ifName: String): Boolean {
        val d = st.devOrNull(devId) ?: return false
        if (d.kind != "host") { val i = d.running.intf(ifName) ?: return false; if (i.shutdown) return false }
        val (pd, pi) = st.peer(devId, ifName) ?: return false
        val p = st.devOrNull(pd) ?: return false
        if (p.kind == "host") return true
        return p.running.intf(pi)?.shutdown == false
    }

    /** "trunk", "access" or "down" for a switchport, using a simplified DTP outcome table. */
    fun operMode(devId: String, ifName: String): String {
        val d = st.dev(devId); val i = d.running.intf(ifName) ?: return "down"
        if (!i.switchport) return "routed"
        if (!linkUp(devId, ifName)) return "down"
        val (pd, pi) = st.peer(devId, ifName)!!
        val p = st.dev(pd)
        val peerIf = p.running.intf(pi)
        val peerSwitchport = p.kind == "switch" && peerIf?.switchport == true
        val sendsDtp: (Intf) -> Boolean = { it.mode != "access" && !it.nonegotiate }
        return when (i.mode) {
            "access" -> "access"
            "trunk" -> "trunk"
            "dynamic desirable" -> if (peerSwitchport && peerIf!!.mode != "access" && sendsDtp(peerIf)) "trunk" else "access"
            "dynamic auto" -> if (peerSwitchport && (peerIf!!.mode == "trunk" || peerIf.mode == "dynamic desirable") && sendsDtp(peerIf)) "trunk" else "access"
            else -> "access"
        }
    }

    /** Status column used by 'show interfaces status' / 'show ip interface brief'. */
    fun status(devId: String, ifName: String): Pair<String, String> {
        val d = st.dev(devId); val i = d.running.intf(ifName) ?: return "deleted" to "down"
        if (i.shutdown) return "administratively down" to "down"
        return when {
            IfName.isSub(ifName) -> { val parent = IfName.parent(ifName); val pi = d.running.intf(parent)
                if (pi?.shutdown != false) "administratively down" to "down" else if (linkUp(devId, parent)) "up" to "up" else "down" to "down" }
            ifName.startsWith("Vlan") -> "up" to (if (sviUp(d, ifName.removePrefix("Vlan").toInt())) "up" else "down")
            ifName.startsWith("Loopback") -> "up" to "up"
            linkUp(devId, ifName) -> "up" to "up"
            else -> "down" to "down"
        }
    }

    /** VLANs carried by a port right now. Access ports carry one VLAN; trunks carry allowed ∩ existing VLANs. */
    fun vlansOnPort(devId: String, ifName: String): Set<Int> {
        val d = st.dev(devId); val i = d.running.intf(ifName) ?: return emptySet()
        return when (operMode(devId, ifName)) {
            "access" -> if (d.running.vlanExists(i.accessVlan)) setOf(i.accessVlan) else emptySet()
            "trunk" -> ((d.running.vlans.keys + 1).filter { i.allowedSet(it) }).toSet()
            else -> emptySet()
        }
    }

    private fun sviUp(d: Device, v: Int): Boolean {
        if (!d.running.vlanExists(v)) return false
        return d.running.interfaces.any { it.switchport && v in vlansOnPort(d.id, it.name) }
    }

    fun intfUp(devId: String, ifName: String) = status(devId, ifName).second == "up"

    // ---------------- Layer 2 forwarding ----------------

    /** An addressable Layer 3 endpoint: a host NIC, a router (sub)interface, or a switch SVI. */
    data class Endpoint(val dev: String, val ifName: String) { val key get() = "$dev|$ifName" }

    private data class Frame(val dev: String, val port: String, val tag: Int?)   // frame arriving at dev/port with tag (null = untagged)
    private data class L2Node(val dev: String, val vlan: Int)

    data class Segment(val endpoints: Set<Endpoint>, val drops: List<String>, val linksUsed: Map<String, List<Link>>)

    /** Every endpoint that receives a broadcast sent by [from]. Drop reasons explain where frames stopped. */
    fun segment(from: Endpoint): Segment {
        val drops = linkedSetOf<String>()
        val reached = linkedSetOf<Endpoint>()
        val pathTo = mutableMapOf<String, List<Link>>()            // endpoint key -> physical links crossed
        val visited = mutableSetOf<L2Node>()
        val queue = ArrayDeque<Pair<Frame, List<Link>>>()
        val src = st.dev(from.dev)
        reached += from; pathTo[from.key] = emptyList()

        fun linkOf(dev: String, port: String) = st.links.first { (it.a == dev && it.aIf.equals(port, true)) || (it.b == dev && it.bIf.equals(port, true)) }
        fun sendOut(dev: String, port: String, tag: Int?, path: List<Link>) {
            if (!linkUp(dev, port)) { if (st.peer(dev, port) != null) drops += "${st.dev(dev).name} ${IfName.short(port)} is down, so frames cannot leave it."; return }
            val (pd, pp) = st.peer(dev, port)!!
            queue.add(Frame(pd, pp, tag) to (path + linkOf(dev, port)))
        }
        fun floodVlan(dev: String, vlan: Int, path: List<Link>) {
            if (!visited.add(L2Node(dev, vlan))) return
            val d = st.dev(dev)
            d.running.intf("Vlan$vlan")?.let { svi -> if (!svi.shutdown && svi.ip != null) { val e = Endpoint(dev, svi.name); if (reached.add(e)) pathTo[e.key] = path } }
            d.running.interfaces.filter { it.switchport }.forEach { p ->
                when (operMode(dev, p.name)) {
                    "access" -> if (p.accessVlan == vlan) sendOut(dev, p.name, null, path)
                    "trunk" -> if (p.allowedSet(vlan)) sendOut(dev, p.name, if (vlan == p.nativeVlan) null else vlan, path)
                }
            }
        }

        // Initial transmission from the source endpoint
        when {
            src.kind == "host" -> sendOut(src.id, "NIC", null, emptyList())
            src.kind == "switch" -> floodVlan(src.id, from.ifName.removePrefix("Vlan").toInt(), emptyList())
            IfName.isSub(from.ifName) -> { val sub = src.running.intf(from.ifName)!!
                sendOut(src.id, IfName.parent(from.ifName), if (sub.dot1qNative) null else sub.dot1q, emptyList()) }
            else -> sendOut(src.id, from.ifName, null, emptyList())
        }

        while (queue.isNotEmpty()) {
            val (f, path) = queue.removeFirst()
            val d = st.dev(f.dev)
            when (d.kind) {
                "host" -> if (f.tag == null) { val e = Endpoint(d.id, "NIC"); if (reached.add(e)) pathTo[e.key] = path }
                          else drops += "${d.id} received a frame tagged for VLAN ${f.tag}; hosts discard 802.1Q-tagged frames."
                "router" -> {
                    val target = if (f.tag == null)
                        d.running.interfaces.firstOrNull { it.name.equals(f.port, true) && it.ip != null }
                            ?: d.running.interfaces.firstOrNull { IfName.parent(it.name).equals(f.port, true) && it.dot1qNative }
                    else d.running.interfaces.firstOrNull { IfName.parent(it.name).equals(f.port, true) && IfName.isSub(it.name) && it.dot1q == f.tag && !it.dot1qNative }
                    if (target == null) drops += if (f.tag == null) "${d.name} ${IfName.short(f.port)} has no IP address for untagged frames."
                        else "${d.name} has no subinterface on ${IfName.short(f.port)} with 'encapsulation dot1q ${f.tag}', so VLAN ${f.tag} frames are dropped."
                    else if (target.shutdown) drops += "${d.name} ${IfName.short(target.name)} is administratively down."
                    else { val e = Endpoint(d.id, target.name); if (reached.add(e)) pathTo[e.key] = path }
                }
                "switch" -> {
                    val p = d.running.intf(f.port) ?: continue
                    when (operMode(d.id, p.name)) {
                        "access" -> if (f.tag != null) drops += "${d.name} ${IfName.short(p.name)} is an access port; it dropped a frame tagged for VLAN ${f.tag} (possible trunk/access mismatch)."
                            else if (!d.running.vlanExists(p.accessVlan)) drops += "${d.name} ${IfName.short(p.name)} is assigned to VLAN ${p.accessVlan}, which does not exist on ${d.name}."
                            else floodVlan(d.id, p.accessVlan, path)
                        "trunk" -> {
                            val v = f.tag ?: p.nativeVlan
                            when {
                                !p.allowedSet(v) -> drops += "${d.name} ${IfName.short(p.name)} does not allow VLAN $v on the trunk (allowed: ${VlanList.format(p.allowed)})."
                                !d.running.vlanExists(v) -> drops += "VLAN $v does not exist on ${d.name}, so the trunk cannot forward it."
                                else -> floodVlan(d.id, v, path)
                            }
                        }
                    }
                }
            }
        }
        return Segment(reached, drops.toList(), pathTo.mapValues { it.value })
    }

    // ---------------- Layer 3 ----------------

    data class Addr(val ep: Endpoint, val ip: String, val mask: String)

    fun addresses(devId: String): List<Addr> {
        val d = st.dev(devId)
        if (d.kind == "host") return listOfNotNull(if (d.hostIp != null && d.hostMask != null) Addr(Endpoint(d.id, "NIC"), d.hostIp, d.hostMask) else null)
        return d.running.interfaces.filter { it.ip != null && it.mask != null && intfUp(devId, it.name) }.map { Addr(Endpoint(d.id, it.name), it.ip!!, it.mask!!) }
    }

    data class Route(val code: String, val prefix: Long, val len: Int, val nextHop: String?, val exitIf: String?, val ad: Int, val metric: Int = 0)

    /** The routing table a router would install: connected + local routes, then usable static routes. */
    fun routes(devId: String): List<Route> {
        val d = st.dev(devId); if (d.kind != "router") return emptyList()
        val out = mutableListOf<Route>()
        addresses(devId).forEach { a ->
            val len = Ip.prefixLen(a.mask) ?: return@forEach
            out += Route("C", Ip.network(a.ip, a.mask)!!, len, null, a.ep.ifName, 0)
            out += Route("L", Ip.parse(a.ip)!!, 32, null, a.ep.ifName, 0)
        }
        val connected = out.toList()
        d.running.routes.forEach { r ->
            val len = Ip.prefixLen(r.mask) ?: return@forEach
            val usable = when {
                r.exitIf != null -> intfUp(devId, r.exitIf)
                r.nextHop != null -> resolve(connected, Ip.parse(r.nextHop)!!) != null
                else -> false
            }
            if (usable) out += Route("S", Ip.network(r.prefix, r.mask)!!, len, r.nextHop, r.exitIf, r.ad)
        }
        // Same prefix learned more than once: lowest AD wins (connected 0 beats static 1, floating statics stay out).
        return out.groupBy { it.prefix to it.len }.flatMap { (_, rs) -> val best = rs.minOf { it.ad }; rs.filter { it.ad == best } }
    }

    private fun resolve(table: List<Route>, ip: Long): Route? =
        table.filter { (ip and Ip.maskOf(it.len)) == it.prefix }.maxByOrNull { it.len }

    /** Longest-prefix match. */
    fun lookup(devId: String, dst: String): Route? = resolve(routes(devId), Ip.parse(dst)!!)

    data class Trace(val ok: Boolean, val steps: List<String>, val failure: String?, val links: List<Link>)

    private fun ownerOf(ip: String): Addr? = st.devices.flatMap { addresses(it.id) }.firstOrNull { it.ip == ip }

    /** One-way delivery of a packet from a device to an IP address, hop by hop. */
    fun deliver(fromDev: String, dstIp: String, srcHint: String? = null): Trace {
        val steps = mutableListOf<String>(); val used = mutableListOf<Link>()
        var cur = fromDev; var ttl = 16
        while (ttl-- > 0) {
            val d = st.dev(cur); val mine = addresses(cur)
            if (mine.any { it.ip == dstIp }) { steps += "${d.name} owns $dstIp — delivered."; return Trace(true, steps, null, used) }
            // Choose next hop and outgoing endpoint
            val (nextHop, out) = when (d.kind) {
                "router" -> {
                    val r = lookup(cur, dstIp) ?: return Trace(false, steps, "${d.name} has no route to $dstIp (no matching prefix and no default route), so it drops the packet.", used)
                    if (r.code == "L") return Trace(true, steps + "${d.name} owns $dstIp — delivered.", null, used)
                    val prefixTxt = "${Ip.str(r.prefix)}/${r.len}"
                    if (r.code == "C") { steps += "${d.name}: longest match $prefixTxt is directly connected on ${IfName.short(r.exitIf!!)}."; dstIp to mine.first { it.ep.ifName == r.exitIf } }
                    else {
                        val nh = r.nextHop
                        val outRoute = if (nh != null) resolve(routes(cur).filter { it.code == "C" }, Ip.parse(nh)!!) else null
                        val outIf = r.exitIf ?: outRoute?.exitIf ?: return Trace(false, steps, "${d.name} cannot resolve next hop $nh.", used)
                        steps += "${d.name}: longest match $prefixTxt (static, AD ${r.ad}) → next hop ${nh ?: "via " + IfName.short(outIf)} out ${IfName.short(outIf)}."
                        (nh ?: dstIp) to mine.first { it.ep.ifName == outIf }
                    }
                }
                else -> {
                    val a = if (d.kind == "host") mine.firstOrNull() else mine.firstOrNull { Ip.sameSubnet(it.ip, dstIp, it.mask) } ?: mine.firstOrNull()
                    a ?: return Trace(false, steps, "${d.name} has no usable IP interface (check that the interface/SVI is up and addressed).", used)
                    val gw = if (d.kind == "host") d.hostGw else d.running.defaultGateway
                    if (Ip.sameSubnet(a.ip, dstIp, a.mask)) { steps += "${d.name}: $dstIp is in my subnet — send directly."; dstIp to a }
                    else if (gw == null) return Trace(false, steps, "${d.name}: $dstIp is in another subnet and no default gateway is configured.", used)
                    else { steps += "${d.name}: $dstIp is in another subnet — send to default gateway $gw."; gw to a }
                }
            }
            // Layer 2 delivery to the next hop
            val seg = segment(out.ep)
            val target = seg.endpoints.mapNotNull { e -> addresses(e.dev).firstOrNull { it.ep == e && it.ip == nextHop } }.firstOrNull()
            if (target == null) {
                val owner = ownerOf(nextHop)
                val why = when {
                    seg.drops.isNotEmpty() -> seg.drops.first()
                    owner == null -> "No up interface in the network owns $nextHop."
                    else -> "$nextHop (${st.dev(owner.ep.dev).name} ${IfName.short(owner.ep.ifName)}) is not in the same Layer 2 segment as ${d.name} ${IfName.short(out.ep.ifName)}."
                }
                return Trace(false, steps, "ARP for $nextHop from ${d.name} gets no reply. $why", used)
            }
            used += seg.linksUsed[target.ep.key].orEmpty()
            if (target.ep.dev != cur) steps += "Frame reaches ${st.dev(target.ep.dev).name} ${if (target.ep.ifName == "NIC") "" else IfName.short(target.ep.ifName)}".trimEnd() + "."
            cur = target.ep.dev
        }
        return Trace(false, steps, "TTL expired — the packet is looping between routers.", used)
    }

    /** Round trip: echo request to [dstIp] and echo reply back to the sender's source address. */
    fun ping(fromDev: String, dstIp: String): Trace {
        val d = st.dev(fromDev)
        val src = when (d.kind) {
            "router" -> lookup(fromDev, dstIp)?.let { r ->
                val outIf = r.exitIf ?: r.nextHop?.let { nh -> resolve(routes(fromDev).filter { it.code == "C" }, Ip.parse(nh)!!)?.exitIf }
                addresses(fromDev).firstOrNull { it.ep.ifName == outIf }?.ip }
            else -> addresses(fromDev).firstOrNull()?.ip
        }
        val fwd = deliver(fromDev, dstIp)
        if (!fwd.ok) return Trace(false, listOf("Echo request:") + fwd.steps, fwd.failure, fwd.links)
        if (src == null) return Trace(false, fwd.steps, "No source address available on ${d.name}.", fwd.links)
        val owner = ownerOf(dstIp) ?: return fwd
        val back = deliver(owner.ep.dev, src)
        val steps = listOf("Echo request:") + fwd.steps + "Echo reply (back to $src):" + back.steps
        return Trace(back.ok, steps, if (back.ok) null else "The request arrived but the reply failed: " + back.failure, fwd.links + back.links)
    }

    /** Host-to-host test used by the topology view and task grading. */
    fun hostPing(a: String, b: String): Trace {
        val ip = st.dev(b).hostIp ?: return Trace(false, emptyList(), "$b has no IP address.", emptyList())
        return ping(a, ip)
    }
}
