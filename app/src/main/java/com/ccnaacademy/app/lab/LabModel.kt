package com.ccnaacademy.app.lab

import kotlinx.serialization.Serializable

/*
 * Shared state model for the offline CLI trainer.
 *
 * One LabState drives the terminal, the topology view, verification output and grading, so they can never
 * contradict each other. This is an educational model of a small, documented subset of Cisco IOS behaviour
 * (Catalyst 2960-style access switches and ISR-style routers). It is NOT an IOS emulator: see SUPPORTED_SCOPE.
 */

@Serializable
enum class Mode(val suffix: String) {
    USER(">"), PRIV("#"), CONFIG("(config)#"), IF("(config-if)#"), IF_RANGE("(config-if-range)#"),
    SUBIF("(config-subif)#"), VLAN("(config-vlan)#");
    val isConfig get() = this != USER && this != PRIV
}

@Serializable data class StaticRoute(val prefix: String, val mask: String, val nextHop: String? = null, val exitIf: String? = null, val ad: Int = 1)

@Serializable data class Intf(
    val name: String,                      // canonical, e.g. "GigabitEthernet0/1", "Vlan10", "GigabitEthernet0/0.10"
    val shutdown: Boolean = false,
    val description: String = "",
    val switchport: Boolean = false,       // true on switch physical ports (Layer 2)
    val mode: String = "dynamic auto",     // access | trunk | dynamic auto | dynamic desirable (switchports only)
    val nonegotiate: Boolean = false,
    val accessVlan: Int = 1,
    val nativeVlan: Int = 1,
    val allowed: List<Int>? = null,        // null = all VLANs (1-4094)
    val ip: String? = null,
    val mask: String? = null,
    val dot1q: Int? = null,                // router subinterface encapsulation dot1q <vlan>
    val dot1qNative: Boolean = false
) {
    val allowedSet: (Int) -> Boolean get() = { v -> allowed?.contains(v) ?: true }
}

/** Everything that appears in running-config. Copied to startup-config by 'copy running-config startup-config'. */
@Serializable data class DeviceConfig(
    val hostname: String,
    val interfaces: List<Intf>,
    val vlans: Map<Int, String> = emptyMap(),   // switch VLAN database (VLAN 1 is implicit and permanent)
    val routes: List<StaticRoute> = emptyList(),
    val defaultGateway: String? = null,
    val enableSecret: Boolean = false
) {
    fun intf(name: String) = interfaces.firstOrNull { it.name.equals(name, true) }
    fun withIntf(i: Intf): DeviceConfig {
        val exists = interfaces.any { it.name.equals(i.name, true) }
        val list = if (exists) interfaces.map { if (it.name.equals(i.name, true)) i else it } else interfaces + i
        return copy(interfaces = list.sortedWith(compareBy({ IfName.typeOrder(it.name) }, { IfName.sortKey(it.name) })))
    }
    fun vlanName(v: Int) = if (v == 1) "default" else vlans[v]
    fun vlanExists(v: Int) = v == 1 || v in vlans
}

/** Per-device CLI session: current mode and submode context. */
@Serializable data class Session(val mode: Mode = Mode.USER, val ctxIfs: List<String> = emptyList(), val ctxVlans: List<Int> = emptyList())

@Serializable data class Device(
    val id: String,
    val kind: String,                           // switch | router | host
    val running: DeviceConfig,
    val startup: DeviceConfig? = null,
    val session: Session = Session(),
    /** Fixed host settings (hosts are not configured through the CLI). */
    val hostIp: String? = null, val hostMask: String? = null, val hostGw: String? = null,
    val x: Float = 0f, val y: Float = 0f
) {
    val name get() = if (kind == "host") id else running.hostname
    val prompt get() = running.hostname + session.mode.suffix
    val configurable get() = kind != "host"
}

@Serializable data class Link(val a: String, val aIf: String, val b: String, val bIf: String)

@Serializable data class LabState(
    val devices: List<Device>,
    val links: List<Link>,
    val history: Map<String, List<String>> = emptyMap(),  // deviceId -> previously entered commands
    val events: Set<String> = emptySet()                  // "deviceId:commandId" for every successfully executed command
) {
    fun dev(id: String) = devices.first { it.id == id }
    fun devOrNull(id: String) = devices.firstOrNull { it.id == id }
    fun update(d: Device) = copy(devices = devices.map { if (it.id == d.id) d else it })
    /** The far end of a physical port, if cabled. */
    fun peer(devId: String, ifName: String): Pair<String, String>? {
        links.forEach { l ->
            if (l.a == devId && l.aIf.equals(ifName, true)) return l.b to l.bIf
            if (l.b == devId && l.bIf.equals(ifName, true)) return l.a to l.aIf
        }
        return null
    }
}

/** Factory-default configurations for the modelled platforms. */
object Platforms {
    fun switch2960(hostname: String, fast: Int = 24, gig: Int = 2) = DeviceConfig(
        hostname = hostname,
        interfaces = (1..fast).map { Intf("FastEthernet0/$it", switchport = true) } +
            (1..gig).map { Intf("GigabitEthernet0/$it", switchport = true) } +
            Intf("Vlan1", shutdown = true)
    )
    fun router(hostname: String, gig: Int = 3) = DeviceConfig(
        hostname = hostname,
        interfaces = (0 until gig).map { Intf("GigabitEthernet0/$it", shutdown = true) }
    )
    fun factory(kind: String, hostname: String) = if (kind == "router") router(hostname) else switch2960(hostname)
}

/** Interface-name parsing, abbreviation and sorting. */
object IfName {
    val TYPES = listOf("FastEthernet", "GigabitEthernet", "Vlan", "Loopback")
    private val SHORT = mapOf("FastEthernet" to "Fa", "GigabitEthernet" to "Gi", "Vlan" to "Vl", "Loopback" to "Lo")

    /** Resolves "g0/1", "gi 0/1", "GigabitEthernet0/1.10", "vlan 10" -> canonical name, or null if not an interface name. */
    fun parse(raw: String): String? {
        val s = raw.replace(" ", "")
        val m = Regex("^([A-Za-z]+)([0-9][0-9/.]*)$").find(s) ?: return null
        val t = m.groupValues[1]; val num = m.groupValues[2]
        val matches = TYPES.filter { it.startsWith(t, ignoreCase = true) }
        if (matches.size != 1) return null
        val type = matches[0]
        val ok = when (type) {
            "Vlan" -> Regex("^\\d{1,4}$").matches(num) && num.toInt() in 1..4094
            "Loopback" -> Regex("^\\d{1,4}$").matches(num)
            else -> Regex("^\\d+/\\d+(\\.\\d{1,4})?$").matches(num)
        }
        return if (ok) type + num else null
    }
    fun short(name: String): String {
        TYPES.forEach { if (name.startsWith(it)) return SHORT.getValue(it) + name.removePrefix(it) }
        return name
    }
    fun typeOrder(name: String) = when {
        name.startsWith("FastEthernet") -> 0; name.startsWith("GigabitEthernet") -> 1; name.startsWith("Vlan") -> 2; else -> 3
    }
    /** Numeric sort key so Fa0/2 sorts before Fa0/10 and Gi0/0 before Gi0/0.10. */
    fun sortKey(name: String): String = Regex("\\d+").replace(name) { it.value.padStart(5, '0') }
    fun isSub(name: String) = name.contains('.')
    fun parent(name: String) = name.substringBefore('.')
}

/** IPv4 helpers (validated with unit tests). */
object Ip {
    fun parse(s: String): Long? {
        val p = s.trim().split(".")
        if (p.size != 4) return null
        var v = 0L
        for (o in p) { val n = o.toIntOrNull() ?: return null; if (n !in 0..255 || (o.length > 1 && o.startsWith("0"))) return null; v = (v shl 8) or n.toLong() }
        return v
    }
    fun str(v: Long) = "${(v shr 24) and 255}.${(v shr 16) and 255}.${(v shr 8) and 255}.${v and 255}"
    /** Prefix length of a contiguous mask, or null if the mask is not contiguous. */
    fun prefixLen(mask: String): Int? {
        val m = parse(mask) ?: return null
        val len = java.lang.Long.bitCount(m)
        return if (maskOf(len) == m) len else null
    }
    fun maskOf(len: Int): Long = if (len == 0) 0L else (0xFFFFFFFFL shl (32 - len)) and 0xFFFFFFFFL
    fun maskStr(len: Int) = str(maskOf(len))
    fun network(ip: String, mask: String): Long? { val a = parse(ip) ?: return null; val m = parse(mask) ?: return null; return a and m }
    fun sameSubnet(a: String, b: String, mask: String) = network(a, mask) != null && network(a, mask) == network(b, mask)
    fun contains(prefix: String, mask: String, ip: String): Boolean {
        val n = parse(prefix) ?: return false; val m = parse(mask) ?: return false; val a = parse(ip) ?: return false
        return (a and m) == (n and m)
    }
    /** Classful length used by 'show ip route' grouping. */
    fun classfulLen(v: Long): Int { val first = (v shr 24).toInt(); return when { first < 128 -> 8; first < 192 -> 16; else -> 24 } }
    fun isHostAddress(ip: String, mask: String): Boolean {
        val a = parse(ip) ?: return false; val len = prefixLen(mask) ?: return false
        if (len >= 31) return true
        val m = maskOf(len); val host = a and m.inv() and 0xFFFFFFFFL
        return host != 0L && host != (m.inv() and 0xFFFFFFFFL)
    }
}

/** VLAN list parsing/formatting for 'switchport trunk allowed vlan' and show output. */
object VlanList {
    fun parse(s: String): List<Int>? {
        val out = sortedSetOf<Int>()
        for (part in s.split(",")) {
            if (part.isBlank()) return null
            val r = part.split("-")
            when (r.size) {
                1 -> out += r[0].toIntOrNull()?.takeIf { it in 1..4094 } ?: return null
                2 -> { val a = r[0].toIntOrNull() ?: return null; val b = r[1].toIntOrNull() ?: return null
                       if (a !in 1..4094 || b !in 1..4094 || a > b) return null; out += (a..b) }
                else -> return null
            }
        }
        return out.toList()
    }
    fun format(list: List<Int>?): String {
        if (list == null) return "1-4094"
        if (list.isEmpty()) return "none"
        val s = list.sorted(); val parts = mutableListOf<String>(); var i = 0
        while (i < s.size) {
            var j = i
            while (j + 1 < s.size && s[j + 1] == s[j] + 1) j++
            parts += if (j - i >= 1) "${s[i]}-${s[j]}" else "${s[i]}"
            i = j + 1
        }
        return parts.joinToString(",")
    }
}

/** What the trainer models. Shown to learners in the Supported Commands screen. */
val SUPPORTED_SCOPE = listOf(
    "Modelled: CLI modes, hostname, VLAN database, access/trunk/dynamic switchport modes with simplified DTP outcomes, native VLAN, allowed VLAN lists, SVIs, router interfaces and 802.1Q subinterfaces, IPv4 addressing, static/default routes, running vs startup configuration, reload.",
    "Reachability (ping and the topology 'Test' button) is computed from the modelled tables: VLAN membership, trunks, native-VLAN tagging, connected and static routes, and host default gateways. It checks both the forward and return path.",
    "Not modelled: STP, DTP timers, CDP, ARP timing, MAC aging, dynamic routing protocols, ACLs, NAT, DHCP, security features, IPv6, and real traffic. Real IOS output contains more fields; trainer output keeps the fields the lessons teach.",
    "An accepted command is not proof of end-to-end connectivity. Use the verification commands and the reachability test."
)
