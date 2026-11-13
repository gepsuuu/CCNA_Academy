package com.ccnaacademy.app.lab

/** One line of terminal output. kind: echo (prompt + typed command), out (device output), err (IOS error), note (trainer explanation, not IOS output). */
data class OutLine(val text: String, val kind: String = "out")

enum class ArgKind(val label: String) {
    WORD("WORD"), LINE("LINE"), VLAN("<1-4094>"), VLANLIST("VLAN_IDS"), IP("A.B.C.D"), MASK("A.B.C.D"),
    INTF("INTERFACE"), INTFRANGE("RANGE"), AD("<1-255>")
}

sealed class Tok {
    data class Kw(val w: String, val help: String) : Tok()
    data class Arg(val kind: ArgKind, val help: String) : Tok()
}

/** A supported command pattern. [kinds] restricts it to switches, routers or both. */
class Cmd(
    val id: String, val modes: Set<Mode>, val toks: List<Tok>, val summary: String,
    val kinds: Set<String> = setOf("switch", "router"), val exec: CliEngine.Exec.(List<String>) -> Unit
)

/**
 * Educational IOS command interpreter. Supports abbreviations ("sh vl br"), '?' help, Tab completion, command history,
 * 'do', automatic fall-back from configuration submodes to global configuration, and honest messages for
 * real-but-unmodelled commands. All effects are applied to the shared [LabState].
 */
class CliEngine(var state: LabState) {

    inner class Exec(val devId: String, val out: MutableList<OutLine>) {
        val dev get() = state.dev(devId)
        val cfg get() = dev.running
        val ses get() = dev.session
        fun say(t: String) { t.split("\n").forEach { out += OutLine(it, "out") } }
        fun err(t: String) { out += OutLine(t, "err") }
        fun note(t: String) { out += OutLine("[Trainer] $t", "note") }
        fun setCfg(c: DeviceConfig) { state = state.update(dev.copy(running = c)) }
        fun setSes(s: Session) { state = state.update(dev.copy(session = s)) }
        fun editIfs(f: (Intf) -> Intf) { var c = cfg; ses.ctxIfs.forEach { n -> c.intf(n)?.let { c = c.withIntf(f(it)) } }; setCfg(c) }
        val ifs get() = ses.ctxIfs.mapNotNull { cfg.intf(it) }
    }

    // ------------------------------------------------------------------ grammar
    private fun kw(w: String, h: String) = Tok.Kw(w, h)
    private fun arg(k: ArgKind, h: String) = Tok.Arg(k, h)
    private val EXEC = setOf(Mode.USER, Mode.PRIV)
    private val P = setOf(Mode.PRIV)
    private val G = setOf(Mode.CONFIG)
    private val IFM = setOf(Mode.IF, Mode.IF_RANGE)
    private val SUBM = setOf(Mode.SUBIF)
    private val L3IF = setOf(Mode.IF, Mode.SUBIF)
    private val ALLCFG = setOf(Mode.CONFIG, Mode.IF, Mode.IF_RANGE, Mode.SUBIF, Mode.VLAN)
    private val SW = setOf("switch"); private val RT = setOf("router")

    private val SHOW = kw("show", "Show running system information")
    private fun showCmd(id: String, modes: Set<Mode>, toks: List<Tok>, s: String, kinds: Set<String> = setOf("switch", "router"), f: Exec.(List<String>) -> Unit) =
        Cmd(id, modes, listOf(SHOW) + toks, s, kinds, f)

    val commands: List<Cmd> = listOf(
        // ----- EXEC
        Cmd("enable", setOf(Mode.USER), listOf(kw("enable", "Turn on privileged commands")), "Enter privileged EXEC mode") { setSes(Session(Mode.PRIV)) },
        Cmd("disable", P, listOf(kw("disable", "Turn off privileged commands")), "Return to user EXEC mode") { setSes(Session(Mode.USER)) },
        Cmd("conft", P, listOf(kw("configure", "Enter configuration mode"), kw("terminal", "Configure from the terminal")), "Enter global configuration mode") {
            say("Enter configuration commands, one per line.  End with CNTL/Z."); setSes(Session(Mode.CONFIG)) },
        Cmd("copyrs", P, listOf(kw("copy", "Copy from one file to another"), kw("running-config", "Copy from current system configuration"), kw("startup-config", "Copy to startup configuration")),
            "Save running-config to startup-config (NVRAM)") { say("Destination filename [startup-config]? "); save() },
        Cmd("write", P, listOf(kw("write", "Write running configuration to memory")), "Save running-config (same as write memory)") { save() },
        Cmd("wr", P, listOf(kw("write", "Write running configuration to memory"), kw("memory", "Write to NV memory")), "Save running-config (legacy shortcut)") { save() },
        Cmd("erase", P, listOf(kw("erase", "Erase a filesystem"), kw("startup-config", "Erase contents of configuration memory")), "Delete startup-config") {
            say("Erasing the nvram filesystem will remove all configuration files! Continue? [confirm]\n[OK]\nErase of nvram: complete")
            state = state.update(dev.copy(startup = null)); note("Startup-config deleted. The running-config is unchanged until you reload.") },
        Cmd("reload", P, listOf(kw("reload", "Halt and perform a cold restart")), "Restart the device; unsaved changes are lost") { reload() },
        Cmd("ping", setOf(Mode.USER, Mode.PRIV), listOf(kw("ping", "Send echo messages"), arg(ArgKind.IP, "Ping destination address")), "Test reachability (modelled)") { ping(it[0]) },
        Cmd("exitx", EXEC, listOf(kw("exit", "Exit from the EXEC")), "Log out (returns to user EXEC in the trainer)") { setSes(Session(Mode.USER)); note("Session closed. You are back at the user EXEC prompt.") },

        showCmd("shrun", P, listOf(kw("running-config", "Current operating configuration")), "Display the running configuration") { say(runningConfig(dev.id, cfg)) },
        showCmd("shstart", P, listOf(kw("startup-config", "Contents of startup configuration")), "Display the saved configuration") {
            val s = dev.startup; if (s == null) say("startup-config is not present") else say(runningConfig(dev.id, s, startup = true)) },
        showCmd("shvlanbr", EXEC, listOf(kw("vlan", "VTP VLAN status"), kw("brief", "VTP all VLAN status in brief")), "VLANs and their access ports", SW) { say(showVlanBrief(dev.id)) },
        showCmd("shvlan", EXEC, listOf(kw("vlan", "VTP VLAN status")), "VLAN table (trainer shows the brief form)", SW) {
            say(showVlanBrief(dev.id)); note("Real 'show vlan' also lists VLAN type, SAID and MTU sections; the trainer shows the brief table.") },
        showCmd("shinttrunk", EXEC, listOf(kw("interfaces", "Interface status and configuration"), kw("trunk", "Show interface trunk information")), "Operational trunks", SW) { say(showTrunk(dev.id)) },
        showCmd("shintstatus", EXEC, listOf(kw("interfaces", "Interface status and configuration"), kw("status", "Show interface line status")), "Port status, VLAN and description", SW) { say(showStatus(dev.id)) },
        showCmd("shintsw", EXEC, listOf(kw("interfaces", "Interface status and configuration"), arg(ArgKind.INTF, "Interface name"), kw("switchport", "Show interface switchport information")),
            "Administrative and operational switchport settings", SW) { a -> val n = a[0]
            if (cfg.intf(n)?.switchport != true) err("% Invalid input: ${IfName.short(n)} is not a switchport on this device.") else say(showSwitchport(dev.id, n)) },
        showCmd("shipintbr", EXEC, listOf(kw("ip", "IP information"), kw("interface", "IP interface status and configuration"), kw("brief", "Brief summary of IP status and configuration")),
            "Interface IP addresses and status") { say(showIpIntBrief(dev.id)) },
        showCmd("shiproute", EXEC, listOf(kw("ip", "IP information"), kw("route", "IP routing table")), "Routing table", RT) { say(showIpRoute(dev.id)) },
        Cmd("shiproutesw", EXEC, listOf(SHOW, kw("ip", "IP information"), kw("route", "IP routing table")), "Routing table", SW) {
            say("Default gateway is ${cfg.defaultGateway ?: "not set"}\n\nHost               Gateway           Last Use    Total Uses  Interface\nICMP redirect cache is empty")
            note("This access switch is not routing (no 'ip routing'); it only uses 'ip default-gateway' for its own management traffic.") },

        // ----- Global configuration
        Cmd("hostname", G, listOf(kw("hostname", "Set system's network name"), arg(ArgKind.WORD, "This system's network name")), "Set the device name") { a ->
            val h = a[0]
            if (!Regex("^[A-Za-z][A-Za-z0-9-]{0,62}$").matches(h)) { err("% Hostname contains one or more illegal characters."); note("Hostnames must start with a letter and use letters, digits and hyphens.") }
            else setCfg(cfg.copy(hostname = h)) },
        Cmd("vlan", G, listOf(kw("vlan", "Vlan commands"), arg(ArgKind.VLANLIST, "ISL VLAN IDs 1-1005 / 1006-4094")), "Create VLAN(s) and enter VLAN configuration mode", SW) { a ->
            val ids = VlanList.parse(a[0])!!
            if (ids.any { it in 1002..1005 }) { err("%VLANs 1002-1005 are default VLANs and cannot be configured."); return@Cmd }
            var c = cfg; ids.filter { it != 1 && it !in c.vlans }.forEach { c = c.copy(vlans = c.vlans + (it to "VLAN" + it.toString().padStart(4, '0'))) }
            setCfg(c); setSes(Session(Mode.VLAN, ctxVlans = ids)) },
        Cmd("novlan", G, listOf(kw("no", "Negate a command or set its defaults"), kw("vlan", "Vlan commands"), arg(ArgKind.VLANLIST, "VLAN IDs")), "Delete VLAN(s)", SW) { a ->
            val ids = VlanList.parse(a[0])!!
            if (1 in ids) { err("%Default VLAN 1 may not be deleted."); return@Cmd }
            setCfg(cfg.copy(vlans = cfg.vlans - ids.toSet()))
            val orphan = cfg.interfaces.filter { it.switchport && it.mode == "access" && it.accessVlan in ids }
            if (orphan.isNotEmpty()) note("Ports ${orphan.joinToString { IfName.short(it.name) }} are still assigned to the deleted VLAN and are now inactive.") },
        Cmd("intf", G, listOf(kw("interface", "Select an interface to configure"), arg(ArgKind.INTF, "Interface name")), "Enter interface configuration mode") { a -> enterInterface(a[0]) },
        Cmd("intrange", G, listOf(kw("interface", "Select an interface to configure"), kw("range", "interface range command"), arg(ArgKind.INTFRANGE, "Interface range, e.g. fa0/1 - 4")),
            "Configure several ports at once", SW) { a ->
            val list = parseRange(a[0])
            if (list == null || list.any { cfg.intf(it)?.switchport != true }) { err("% Invalid interface range."); note("Use a form such as 'interface range fa0/1 - 4' or 'interface range fa0/1 - 2 , fa0/5'.") }
            else setSes(Session(Mode.IF_RANGE, ctxIfs = list)) },
        Cmd("iproute", G, listOf(kw("ip", "Global IP configuration subcommands"), kw("route", "Establish static routes"), arg(ArgKind.IP, "Destination prefix"), arg(ArgKind.MASK, "Destination prefix mask"),
            arg(ArgKind.IP, "Forwarding router's address")), "Static route via a next-hop address", RT) { a -> addRoute(a[0], a[1], a[2], null, 1) },
        Cmd("iproutead", G, listOf(kw("ip", "Global IP configuration subcommands"), kw("route", "Establish static routes"), arg(ArgKind.IP, "Destination prefix"), arg(ArgKind.MASK, "Destination prefix mask"),
            arg(ArgKind.IP, "Forwarding router's address"), arg(ArgKind.AD, "Distance metric for this route")), "Floating static route (custom AD)", RT) { a -> addRoute(a[0], a[1], a[2], null, a[3].toInt()) },
        Cmd("iprouteif", G, listOf(kw("ip", "Global IP configuration subcommands"), kw("route", "Establish static routes"), arg(ArgKind.IP, "Destination prefix"), arg(ArgKind.MASK, "Destination prefix mask"),
            arg(ArgKind.INTF, "Exit interface")), "Static route out an interface", RT) { a -> addRoute(a[0], a[1], null, a[2], 1) },
        Cmd("noiproute", G, listOf(kw("no", "Negate a command or set its defaults"), kw("ip", "Global IP configuration subcommands"), kw("route", "Establish static routes"),
            arg(ArgKind.IP, "Destination prefix"), arg(ArgKind.MASK, "Destination prefix mask")), "Remove static route(s) to a prefix", RT) { a ->
            val before = cfg.routes.size
            setCfg(cfg.copy(routes = cfg.routes.filterNot { it.prefix == a[0] && it.mask == a[1] }))
            if (before == cfg.routes.size) err("%No matching route to delete") },
        Cmd("defgw", G, listOf(kw("ip", "Global IP configuration subcommands"), kw("default-gateway", "Specify default gateway (if not routing IP)"), arg(ArgKind.IP, "IP address of default gateway")),
            "Gateway for the switch's own management traffic", SW) { a -> setCfg(cfg.copy(defaultGateway = a[0])) },
        Cmd("nodefgw", G, listOf(kw("no", "Negate a command or set its defaults"), kw("ip", "Global IP configuration subcommands"), kw("default-gateway", "Specify default gateway")),
            "Remove the default gateway", SW) { setCfg(cfg.copy(defaultGateway = null)) },

        // ----- VLAN configuration
        Cmd("vname", setOf(Mode.VLAN), listOf(kw("name", "Ascii name of the VLAN"), arg(ArgKind.WORD, "The ascii name for the VLAN")), "Name the VLAN", SW) { a ->
            if (1 in ses.ctxVlans) { err("%Default VLAN 1 may not have its name changed."); return@Cmd }
            if (a[0].length > 32) { err("% Name too long (max 32 characters)."); return@Cmd }
            setCfg(cfg.copy(vlans = cfg.vlans + ses.ctxVlans.associateWith { a[0] })) },
        Cmd("novname", setOf(Mode.VLAN), listOf(kw("no", "Negate a command"), kw("name", "Ascii name of the VLAN")), "Restore the default VLAN name", SW) {
            setCfg(cfg.copy(vlans = cfg.vlans + ses.ctxVlans.filter { it != 1 }.associateWith { "VLAN" + it.toString().padStart(4, '0') })) },

        // ----- Interface configuration (switchports)
        Cmd("swmodeacc", IFM, listOf(kw("switchport", "Set switching mode characteristics"), kw("mode", "Set trunking mode of the interface"), kw("access", "Set trunking mode to ACCESS unconditionally")),
            "Make the port a static access port", SW) { editIfs { it.copy(mode = "access", nonegotiate = false) } },
        Cmd("swmodetrunk", IFM, listOf(kw("switchport", "Set switching mode characteristics"), kw("mode", "Set trunking mode of the interface"), kw("trunk", "Set trunking mode to TRUNK unconditionally")),
            "Make the port a static 802.1Q trunk", SW) { editIfs { it.copy(mode = "trunk") } },
        Cmd("swmodeauto", IFM, listOf(kw("switchport", "Set switching mode characteristics"), kw("mode", "Set trunking mode of the interface"), kw("dynamic", "Set trunking mode to dynamically negotiate access or trunk mode"),
            kw("auto", "Set trunking mode dynamic negotiation parameter to AUTO")), "DTP: wait for the other side to request a trunk", SW) { dyn("dynamic auto") },
        Cmd("swmodedes", IFM, listOf(kw("switchport", "Set switching mode characteristics"), kw("mode", "Set trunking mode of the interface"), kw("dynamic", "Set trunking mode to dynamically negotiate access or trunk mode"),
            kw("desirable", "Set trunking mode dynamic negotiation parameter to DESIRABLE")), "DTP: actively request a trunk", SW) { dyn("dynamic desirable") },
        Cmd("swaccvlan", IFM, listOf(kw("switchport", "Set switching mode characteristics"), kw("access", "Set access mode characteristics of the interface"), kw("vlan", "Set VLAN when interface is in access mode"),
            arg(ArgKind.VLAN, "VLAN ID of the VLAN when this port is in access mode")), "Assign the access VLAN", SW) { a ->
            val v = a[0].toInt()
            if (v in 1002..1005) { err("% VLANs 1002-1005 cannot be used as access VLANs."); return@Cmd }
            if (!cfg.vlanExists(v)) { err("% Access VLAN does not exist. Creating vlan $v"); setCfg(cfg.copy(vlans = cfg.vlans + (v to "VLAN" + v.toString().padStart(4, '0')))) }
            editIfs { it.copy(accessVlan = v) }
            if (ifs.any { it.mode != "access" }) note("The access VLAN is stored, but it is only used while the port operates as an access port. Add 'switchport mode access' to make that explicit and disable DTP.") },
        Cmd("noswaccvlan", IFM, listOf(kw("no", "Negate a command"), kw("switchport", "Set switching mode characteristics"), kw("access", "Set access mode characteristics"), kw("vlan", "Set VLAN when interface is in access mode")),
            "Return the port to VLAN 1", SW) { editIfs { it.copy(accessVlan = 1) } },
        Cmd("swnative", IFM, listOf(kw("switchport", "Set switching mode characteristics"), kw("trunk", "Set trunking characteristics of the interface"), kw("native", "Set trunking native characteristics"),
            kw("vlan", "Set native VLAN when interface is in trunking mode"), arg(ArgKind.VLAN, "VLAN ID of the native VLAN")), "Set the untagged (native) VLAN on a trunk", SW) { a ->
            editIfs { it.copy(nativeVlan = a[0].toInt()) } },
        Cmd("noswnative", IFM, listOf(kw("no", "Negate a command"), kw("switchport", "Set switching mode characteristics"), kw("trunk", "Set trunking characteristics"), kw("native", "Set trunking native characteristics"),
            kw("vlan", "Set native VLAN")), "Native VLAN back to 1", SW) { editIfs { it.copy(nativeVlan = 1) } },
        Cmd("swallowset", IFM, listOf(kw("switchport", "Set switching mode characteristics"), kw("trunk", "Set trunking characteristics of the interface"), kw("allowed", "Set allowed VLAN characteristics when interface is in trunking mode"),
            kw("vlan", "Set allowed VLANs when interface is in trunking mode"), arg(ArgKind.VLANLIST, "VLAN IDs of the allowed VLANs when this port is in trunking mode")), "REPLACE the allowed VLAN list", SW) { a ->
            val list = VlanList.parse(a[0])!!
            val lost = ifs.flatMap { i -> (i.allowed ?: emptyList()).filter { it !in list } }.distinct()
            editIfs { it.copy(allowed = list) }
            if (ifs.isNotEmpty() && lost.isNotEmpty()) note("This command replaced the list. VLAN(s) ${VlanList.format(lost)} are no longer allowed. Use 'switchport trunk allowed vlan add ...' to append.") },
        Cmd("swallowadd", IFM, listOf(kw("switchport", "Set switching mode characteristics"), kw("trunk", "Set trunking characteristics"), kw("allowed", "Set allowed VLAN characteristics"), kw("vlan", "Set allowed VLANs"),
            kw("add", "add VLANs to the current list"), arg(ArgKind.VLANLIST, "VLAN IDs to add")), "Append VLANs to the allowed list", SW) { a ->
            val add = VlanList.parse(a[0])!!; editIfs { i -> i.copy(allowed = i.allowed?.let { (it + add).distinct().sorted() }) } },
        Cmd("swallowrem", IFM, listOf(kw("switchport", "Set switching mode characteristics"), kw("trunk", "Set trunking characteristics"), kw("allowed", "Set allowed VLAN characteristics"), kw("vlan", "Set allowed VLANs"),
            kw("remove", "remove VLANs from the current list"), arg(ArgKind.VLANLIST, "VLAN IDs to remove")), "Remove VLANs from the allowed list", SW) { a ->
            val rem = VlanList.parse(a[0])!!.toSet(); editIfs { i -> i.copy(allowed = (i.allowed ?: (1..4094).toList()).filter { it !in rem }) } },
        Cmd("swallowall", IFM, listOf(kw("switchport", "Set switching mode characteristics"), kw("trunk", "Set trunking characteristics"), kw("allowed", "Set allowed VLAN characteristics"), kw("vlan", "Set allowed VLANs"),
            kw("all", "all VLANs")), "Allow all VLANs (default)", SW) { editIfs { it.copy(allowed = null) } },
        Cmd("swallownone", IFM, listOf(kw("switchport", "Set switching mode characteristics"), kw("trunk", "Set trunking characteristics"), kw("allowed", "Set allowed VLAN characteristics"), kw("vlan", "Set allowed VLANs"),
            kw("none", "no VLANs")), "Allow no VLANs", SW) { editIfs { it.copy(allowed = emptyList()) } },
        Cmd("noswallow", IFM, listOf(kw("no", "Negate a command"), kw("switchport", "Set switching mode characteristics"), kw("trunk", "Set trunking characteristics"), kw("allowed", "Set allowed VLAN characteristics"),
            kw("vlan", "Set allowed VLANs")), "Allowed list back to all VLANs", SW) { editIfs { it.copy(allowed = null) } },
        Cmd("swnoneg", IFM, listOf(kw("switchport", "Set switching mode characteristics"), kw("nonegotiate", "Device will not engage in negotiation protocol on this interface")), "Disable DTP frames", SW) {
            if (ifs.any { it.mode.startsWith("dynamic") }) err("Command rejected: Conflict between 'nonegotiate' and 'dynamic' status.") else editIfs { it.copy(nonegotiate = true) } },
        Cmd("noswnoneg", IFM, listOf(kw("no", "Negate a command"), kw("switchport", "Set switching mode characteristics"), kw("nonegotiate", "Device will not engage in negotiation protocol")),
            "Re-enable DTP frames", SW) { editIfs { it.copy(nonegotiate = false) } },

        // ----- Interface configuration (common)
        Cmd("shut", setOf(Mode.IF, Mode.IF_RANGE, Mode.SUBIF), listOf(kw("shutdown", "Shutdown the selected interface")), "Administratively disable the interface") { editIfs { it.copy(shutdown = true) } },
        Cmd("noshut", setOf(Mode.IF, Mode.IF_RANGE, Mode.SUBIF), listOf(kw("no", "Negate a command or set its defaults"), kw("shutdown", "Shutdown the selected interface")), "Enable the interface") {
            val wasDown = ifs.filter { it.shutdown }; editIfs { it.copy(shutdown = false) }
            val net = Network(state)
            wasDown.filter { !IfName.isSub(it.name) }.forEach { i ->
                val up = net.status(devId, i.name).second == "up"
                say(if (up) "%LINK-3-UPDOWN: Interface ${i.name}, changed state to up\n%LINEPROTO-5-UPDOWN: Line protocol on Interface ${i.name}, changed state to up"
                    else "%LINK-3-UPDOWN: Interface ${i.name}, changed state to down")
            }
        },
        Cmd("desc", setOf(Mode.IF, Mode.IF_RANGE, Mode.SUBIF), listOf(kw("description", "Interface specific description"), arg(ArgKind.LINE, "Up to 240 characters describing this interface")),
            "Label the interface") { a -> editIfs { it.copy(description = a[0].take(240)) } },
        Cmd("nodesc", setOf(Mode.IF, Mode.IF_RANGE, Mode.SUBIF), listOf(kw("no", "Negate a command"), kw("description", "Interface specific description")), "Remove the description") { editIfs { it.copy(description = "") } },
        Cmd("ipaddr", L3IF, listOf(kw("ip", "Interface Internet Protocol config commands"), kw("address", "Set the IP address of an interface"), arg(ArgKind.IP, "IP address"), arg(ArgKind.MASK, "IP subnet mask")),
            "Assign an IPv4 address") { a -> setIp(a[0], a[1]) },
        Cmd("noipaddr", L3IF, listOf(kw("no", "Negate a command"), kw("ip", "Interface Internet Protocol config commands"), kw("address", "Set the IP address of an interface")), "Remove the IPv4 address") {
            editIfs { it.copy(ip = null, mask = null) } },
        Cmd("encap", SUBM, listOf(kw("encapsulation", "Set encapsulation type for an interface"), kw("dot1Q", "IEEE 802.1Q Virtual LAN"), arg(ArgKind.VLAN, "IEEE 802.1Q VLAN ID")),
            "Tag this subinterface with a VLAN", RT) { a -> encap(a[0].toInt(), false) },
        Cmd("encapnat", SUBM, listOf(kw("encapsulation", "Set encapsulation type for an interface"), kw("dot1Q", "IEEE 802.1Q Virtual LAN"), arg(ArgKind.VLAN, "IEEE 802.1Q VLAN ID"), kw("native", "Make this as native vlan")),
            "Native (untagged) VLAN subinterface", RT) { a -> encap(a[0].toInt(), true) },

        // ----- Mode navigation
        Cmd("exitcfg", ALLCFG, listOf(kw("exit", "Exit from configure mode")), "Go up one level") {
            setSes(if (ses.mode == Mode.CONFIG) Session(Mode.PRIV) else Session(Mode.CONFIG)) },
        Cmd("end", ALLCFG, listOf(kw("end", "Exit from configure mode")), "Return to privileged EXEC (same as Ctrl+Z)") { setSes(Session(Mode.PRIV)) },
    )

    // ------------------------------------------------------------------ command helpers
    private fun Exec.save() {
        say("Building configuration...\n[OK]")
        state = state.update(dev.copy(startup = cfg))
    }

    private fun Exec.reload() {
        val unsaved = dev.startup != cfg
        say("Proceed with reload? [confirm]")
        val base = dev.startup ?: Platforms.factory(dev.kind, if (dev.kind == "router") "Router" else "Switch")
        // VLANs 2-1001 live in vlan.dat (VTP server/client mode), not in running-config, so they survive a reload.
        state = state.update(dev.copy(running = base.copy(vlans = cfg.vlans), session = Session(Mode.USER)))
        say("\n%SYS-5-RELOAD: Reload requested by console.\n\nPress RETURN to get started!")
        note(if (unsaved) "Unsaved running-config changes were lost. The device booted from ${if (dev.startup == null) "factory defaults (no startup-config)" else "startup-config"}; VLANs in vlan.dat were kept."
             else "Running-config matched startup-config, so nothing was lost.")
    }

    private fun Exec.dyn(mode: String) {
        if (ifs.any { it.nonegotiate }) { err("Command rejected: Conflict between 'nonegotiate' and 'dynamic' status."); note("Remove 'switchport nonegotiate' first."); return }
        editIfs { it.copy(mode = mode) }
    }

    private fun Exec.enterInterface(n: String) {
        val existing = cfg.intf(n)
        when {
            existing != null -> setSes(Session(if (IfName.isSub(n)) Mode.SUBIF else Mode.IF, ctxIfs = listOf(existing.name)))
            n.startsWith("Vlan") && dev.kind == "switch" -> { setCfg(cfg.withIntf(Intf(n))); setSes(Session(Mode.IF, ctxIfs = listOf(n))) }
            IfName.isSub(n) && dev.kind == "router" && cfg.intf(IfName.parent(n)) != null -> { setCfg(cfg.withIntf(Intf(n))); setSes(Session(Mode.SUBIF, ctxIfs = listOf(n))) }
            IfName.isSub(n) -> { err("% Invalid input detected."); note("Subinterfaces are used on router ports in this trainer (router-on-a-stick).") }
            else -> { err("% Invalid input detected."); note("${IfName.short(n)} does not exist on ${cfg.hostname}. Use 'show ip interface brief' to list its interfaces.") }
        }
    }

    private fun parseRange(s: String): List<String>? {
        val out = mutableListOf<String>()
        for (part in s.split(",").map { it.trim() }) {
            val m = Regex("^([A-Za-z]+)\\s*(\\d+)/(\\d+)\\s*(?:-\\s*(\\d+))?$").find(part) ?: return null
            val type = m.groupValues[1]; val slot = m.groupValues[2]; val a = m.groupValues[3].toInt()
            val b = m.groupValues[4].ifBlank { null }?.toInt() ?: a
            if (b < a) return null
            for (p in a..b) out += IfName.parse("$type$slot/$p") ?: return null
        }
        return out.distinct()
    }

    private fun Exec.setIp(ip: String, mask: String) {
        val i = ifs.firstOrNull() ?: return
        if (dev.kind == "switch" && !i.name.startsWith("Vlan")) { err("% Invalid input detected."); note("Layer 2 switchports cannot have IP addresses. Configure the management address on an SVI, e.g. 'interface vlan 99'."); return }
        val len = Ip.prefixLen(mask)
        if (len == null) { err("% Bad mask $mask for address $ip"); note("Subnet masks must be contiguous 1-bits followed by 0-bits (for example 255.255.255.0)."); return }
        if (!Ip.isHostAddress(ip, mask)) { err("Bad mask /$len for address $ip"); note("$ip is the network or broadcast address of its /$len subnet; choose a usable host address."); return }
        if (IfName.isSub(i.name) && i.dot1q == null) {
            err("% Configuring IP routing on a LAN subinterface is only allowed if that\nsubinterface is already configured as part of an IEEE 802.10, IEEE 802.1Q,\nor ISL vLAN.")
            note("Enter 'encapsulation dot1Q <vlan>' on this subinterface first."); return
        }
        val clash = cfg.interfaces.firstOrNull { o -> o.name != i.name && o.ip != null && o.mask != null &&
            (Ip.sameSubnet(o.ip, ip, o.mask) || Ip.sameSubnet(o.ip, ip, mask)) }
        if (clash != null) { err("% ${Ip.str(Ip.network(ip, mask)!!)} overlaps with ${clash.name}"); return }
        editIfs { it.copy(ip = ip, mask = mask) }
    }

    private fun Exec.encap(v: Int, native: Boolean) {
        val i = ifs.firstOrNull() ?: return
        val dup = cfg.interfaces.firstOrNull { it.name != i.name && IfName.parent(it.name) == IfName.parent(i.name) && it.dot1q == v }
        if (dup != null) { err("% Configuration of multiple subinterfaces of the same main\ninterface with the same VID ($v) is not permitted.\nThis VID is already configured on ${dup.name}."); return }
        editIfs { it.copy(dot1q = v, dot1qNative = native) }
    }

    private fun Exec.addRoute(prefix: String, mask: String, nh: String?, exitIf: String?, ad: Int) {
        if (Ip.prefixLen(mask) == null) { err("%Inconsistent address and mask"); return }
        if (Ip.network(prefix, mask) != Ip.parse(prefix)) { err("%Inconsistent address and mask"); note("$prefix has host bits set for mask $mask. Use the network address ${Ip.str(Ip.network(prefix, mask)!!)}."); return }
        if (exitIf != null && cfg.intf(exitIf) == null) { err("% Invalid input detected."); return }
        if (nh != null && cfg.interfaces.any { it.ip == nh }) { err("%Invalid next hop address (it's this router)"); return }
        val r = StaticRoute(prefix, mask, nh, exitIf, ad)
        setCfg(cfg.copy(routes = cfg.routes.filterNot { it.prefix == prefix && it.mask == mask && it.nextHop == nh && it.exitIf == exitIf } + r))
        if (exitIf != null && exitIf.startsWith("GigabitEthernet")) note("A static route out a multi-access Ethernet interface (no next hop) relies on Proxy ARP. Prefer a next-hop address.")
    }

    private fun Exec.ping(ip: String) {
        val net = Network(state)
        val t = net.ping(devId, ip)
        say("Type escape sequence to abort.\nSending 5, 100-byte ICMP Echos to $ip, timeout is 2 seconds:")
        if (t.ok) say("!!!!!\nSuccess rate is 100 percent (5/5), round-trip min/avg/max = 1/1/2 ms")
        else say(".....\nSuccess rate is 0 percent (0/5)")
        note("Modelled result from the lab tables (not real ICMP). " + if (t.ok) "Forward and return paths were both found." else (t.failure ?: ""))
    }

    // ------------------------------------------------------------------ show output
    fun runningConfig(devId: String, c: DeviceConfig, startup: Boolean = false): String {
        val d = state.dev(devId)
        val sb = StringBuilder()
        sb.append("!\nversion 15.2\nhostname ${c.hostname}\n!\n")
        c.interfaces.forEach { i ->
            sb.append("interface ${i.name}\n")
            if (i.description.isNotBlank()) sb.append(" description ${i.description}\n")
            if (i.switchport) {
                if (i.accessVlan != 1) sb.append(" switchport access vlan ${i.accessVlan}\n")
                if (i.nativeVlan != 1) sb.append(" switchport trunk native vlan ${i.nativeVlan}\n")
                if (i.allowed != null) sb.append(" switchport trunk allowed vlan ${VlanList.format(i.allowed)}\n")
                when (i.mode) { "access" -> sb.append(" switchport mode access\n"); "trunk" -> sb.append(" switchport mode trunk\n")
                    "dynamic desirable" -> sb.append(" switchport mode dynamic desirable\n") }
                if (i.nonegotiate) sb.append(" switchport nonegotiate\n")
            } else {
                if (i.dot1q != null) sb.append(" encapsulation dot1Q ${i.dot1q}${if (i.dot1qNative) " native" else ""}\n")
                if (i.ip != null) sb.append(" ip address ${i.ip} ${i.mask}\n") else sb.append(" no ip address\n")
            }
            if (i.shutdown) sb.append(" shutdown\n")
            sb.append("!\n")
        }
        c.defaultGateway?.let { sb.append("ip default-gateway $it\n!\n") }
        c.routes.forEach { r -> sb.append("ip route ${r.prefix} ${r.mask} ${r.nextHop ?: r.exitIf}${if (r.ad != 1) " ${r.ad}" else ""}\n") }
        if (c.routes.isNotEmpty()) sb.append("!\n")
        sb.append("end")
        val body = sb.toString()
        val head = if (startup) "Using ${body.length} out of 65536 bytes\n" else "Building configuration...\n\nCurrent configuration : ${body.length} bytes\n"
        val tail = if (d.kind == "switch") "\n" else ""
        return head + body + tail
    }

    fun showVlanBrief(devId: String): String {
        val d = state.dev(devId); val net = Network(state)
        val sb = StringBuilder("\nVLAN Name                             Status    Ports\n---- -------------------------------- --------- -------------------------------\n")
        val ids = (d.running.vlans.keys + 1).sorted()
        ids.forEach { v ->
            val ports = d.running.interfaces.filter { it.switchport && it.accessVlan == v && net.operMode(devId, it.name) != "trunk" && it.mode != "trunk" }.map { IfName.short(it.name) }
            val chunks = ports.chunked(4).map { it.joinToString(", ") }
            sb.append(v.toString().padEnd(5) + d.running.vlanName(v)!!.take(32).padEnd(33) + "active    " + (chunks.firstOrNull() ?: "") + "\n")
            chunks.drop(1).forEach { sb.append(" ".repeat(48) + it + "\n") }
        }
        listOf(1002 to "fddi-default", 1003 to "token-ring-default", 1004 to "fddinet-default", 1005 to "trnet-default").forEach { (v, n) ->
            sb.append(v.toString().padEnd(5) + n.padEnd(33) + "act/unsup \n") }
        return sb.toString().trimEnd()
    }

    fun showTrunk(devId: String): String {
        val d = state.dev(devId); val net = Network(state)
        val trunks = d.running.interfaces.filter { it.switchport && net.operMode(devId, it.name) == "trunk" }
        if (trunks.isEmpty()) return ""
        val existing = (d.running.vlans.keys + 1)
        fun modeTxt(i: Intf) = when (i.mode) { "trunk" -> "on"; "dynamic desirable" -> "desirable"; "dynamic auto" -> "auto"; else -> i.mode }
        val sb = StringBuilder("\nPort        Mode             Encapsulation  Status        Native vlan\n")
        trunks.forEach { sb.append(IfName.short(it.name).padEnd(12) + modeTxt(it).padEnd(17) + "802.1q".padEnd(15) + "trunking".padEnd(14) + it.nativeVlan + "\n") }
        sb.append("\nPort        Vlans allowed on trunk\n")
        trunks.forEach { sb.append(IfName.short(it.name).padEnd(12) + VlanList.format(it.allowed) + "\n") }
        sb.append("\nPort        Vlans allowed and active in management domain\n")
        trunks.forEach { i -> sb.append(IfName.short(i.name).padEnd(12) + VlanList.format(existing.filter { i.allowedSet(it) }.sorted()) + "\n") }
        sb.append("\nPort        Vlans in spanning tree forwarding state and not pruned\n")
        trunks.forEach { i -> sb.append(IfName.short(i.name).padEnd(12) + VlanList.format(existing.filter { i.allowedSet(it) }.sorted()) + "\n") }
        return sb.toString().trimEnd()
    }

    fun showStatus(devId: String): String {
        val d = state.dev(devId); val net = Network(state)
        val sb = StringBuilder("\nPort      Name               Status       Vlan       Duplex  Speed Type\n")
        d.running.interfaces.filter { it.switchport }.forEach { i ->
            val st = when { i.shutdown -> "disabled"; net.linkUp(devId, i.name) -> "connected"; else -> "notconnect" }
            val vlan = when { net.operMode(devId, i.name) == "trunk" -> "trunk"; i.mode == "trunk" -> "trunk"; else -> i.accessVlan.toString() }
            val gig = i.name.startsWith("Gigabit")
            val up = st == "connected"
            sb.append(IfName.short(i.name).padEnd(10) + i.description.take(18).padEnd(19) + st.padEnd(13) + vlan.padEnd(11) +
                (if (up) "a-full" else "  auto").padEnd(8) + (if (up) (if (gig) "a-1000" else " a-100") else "  auto").padEnd(6) + (if (gig) "10/100/1000BaseTX" else "10/100BaseTX") + "\n")
        }
        return sb.toString().trimEnd()
    }

    fun showSwitchport(devId: String, n: String): String {
        val d = state.dev(devId); val i = d.running.intf(n)!!; val net = Network(state)
        val admin = when (i.mode) { "access" -> "static access"; "trunk" -> "trunk"; else -> i.mode }
        val oper = when (net.operMode(devId, n)) { "trunk" -> "trunk"; "access" -> "static access"; else -> "down" }
        val accName = d.running.vlanName(i.accessVlan)
        return """
            |Name: ${IfName.short(n)}
            |Switchport: Enabled
            |Administrative Mode: $admin
            |Operational Mode: $oper
            |Administrative Trunking Encapsulation: dot1q
            |Operational Trunking Encapsulation: ${if (oper == "trunk") "dot1q" else "native"}
            |Negotiation of Trunking: ${if (i.mode == "access" || i.nonegotiate) "Off" else "On"}
            |Access Mode VLAN: ${i.accessVlan} (${accName ?: "Inactive"})
            |Trunking Native Mode VLAN: ${i.nativeVlan} (${d.running.vlanName(i.nativeVlan) ?: "Inactive"})
            |Trunking VLANs Enabled: ${if (i.allowed == null) "ALL" else VlanList.format(i.allowed)}
        """.trimMargin()
    }

    fun showIpIntBrief(devId: String): String {
        val d = state.dev(devId); val net = Network(state)
        val sb = StringBuilder("Interface              IP-Address      OK? Method Status                Protocol\n")
        d.running.interfaces.forEach { i ->
            val (s, p) = net.status(devId, i.name)
            sb.append(i.name.padEnd(23) + (i.ip ?: "unassigned").padEnd(16) + "YES " + (if (i.ip != null) "manual" else "unset ").padEnd(7) + s.padEnd(22) + p + "\n")
        }
        return sb.toString().trimEnd()
    }

    fun showIpRoute(devId: String): String {
        val net = Network(state); val rs = net.routes(devId)
        val sb = StringBuilder("Codes: L - local, C - connected, S - static, R - RIP, M - mobile, B - BGP\n       O - OSPF, IA - OSPF inter area, * - candidate default\n\n")
        val def = rs.firstOrNull { it.len == 0 }
        sb.append(if (def != null) "Gateway of last resort is ${def.nextHop ?: Ip.str(0)} to network 0.0.0.0\n\n" else "Gateway of last resort is not set\n\n")
        fun line(r: Route0, showLen: Boolean, indent: Int): String {
            val code = (if (r.r.len == 0) r.r.code + "*" else r.r.code).padEnd(indent)
            val pfx = Ip.str(r.r.prefix) + if (showLen) "/${r.r.len}" else ""
            return code + pfx + when (r.r.code) {
                "S" -> " [${r.r.ad}/0] via ${r.r.nextHop ?: ""}".trimEnd() + (if (r.r.nextHop == null) " is directly connected, ${r.r.exitIf}" else "")
                else -> " is directly connected, ${r.r.exitIf}"
            }
        }
        rs.filter { it.len == 0 }.forEach { sb.append(line(Route0(it), true, 6) + "\n") }
        rs.filter { it.len > 0 }.groupBy { it.prefix and Ip.maskOf(Ip.classfulLen(it.prefix)) }.toSortedMap().forEach { (major, group) ->
            val cl = Ip.classfulLen(major)
            val sorted = group.sortedWith(compareBy({ it.prefix }, { it.len }))
            if (group.size == 1 && group[0].len == cl) { sb.append(line(Route0(group[0]), true, 6) + "\n"); return@forEach }
            val masks = group.map { it.len }.distinct()
            if (masks.size == 1) { sb.append("      ${Ip.str(major)}/${masks[0]} is subnetted, ${group.size} subnets\n"); sorted.forEach { sb.append(line(Route0(it), false, 9) + "\n") } }
            else { sb.append("      ${Ip.str(major)}/$cl is variably subnetted, ${group.size} subnets, ${masks.size} masks\n"); sorted.forEach { sb.append(line(Route0(it), true, 9) + "\n") } }
        }
        return sb.toString().trimEnd()
    }
    private class Route0(val r: Network.Route)

    // ------------------------------------------------------------------ matching
    private sealed class M {
        data class Full(val cmd: Cmd, val args: List<String>, val exact: Int, val resolved: List<String?>) : M()
        data class Incomplete(val cmd: Cmd, val resolved: List<String?>, val pi: Int) : M()
        data class Fail(val cmd: Cmd, val at: Int, val resolved: List<String?>) : M()
    }

    private fun validArg(k: ArgKind, s: String): Boolean = when (k) {
        ArgKind.WORD -> s.isNotBlank()
        ArgKind.LINE, ArgKind.INTFRANGE -> s.isNotBlank()
        ArgKind.VLAN -> s.toIntOrNull()?.let { it in 1..4094 } == true
        ArgKind.VLANLIST -> VlanList.parse(s) != null
        ArgKind.IP -> Ip.parse(s) != null
        ArgKind.MASK -> Ip.parse(s) != null
        ArgKind.AD -> s.toIntOrNull()?.let { it in 1..255 } == true
        ArgKind.INTF -> IfName.parse(s) != null
    }

    /** resolved[i] = keyword matched by token i (null for arguments). */
    private fun match(c: Cmd, t: List<String>): M {
        var ti = 0; val args = mutableListOf<String>(); var exact = 0; val res = mutableListOf<String?>()
        for ((pi, tok) in c.toks.withIndex()) {
            if (ti >= t.size) return M.Incomplete(c, res, pi)
            when (tok) {
                is Tok.Kw -> {
                    if (!tok.w.startsWith(t[ti], ignoreCase = true)) return M.Fail(c, ti, res)
                    if (tok.w.equals(t[ti], true)) exact++
                    res += tok.w; ti++
                }
                is Tok.Arg -> when (tok.kind) {
                    ArgKind.LINE, ArgKind.INTFRANGE -> { args += t.subList(ti, t.size).joinToString(" "); repeat(t.size - ti) { res += null }; ti = t.size }
                    ArgKind.INTF -> {
                        val one = IfName.parse(t[ti])
                        val two = if (one == null && ti + 1 < t.size) IfName.parse(t[ti] + t[ti + 1]) else null
                        when { one != null -> { args += one; res += null; ti++ }
                               two != null -> { args += two; res += null; res += null; ti += 2 }
                               else -> return M.Fail(c, ti, res) }
                    }
                    else -> { if (!validArg(tok.kind, t[ti])) return M.Fail(c, ti, res); args += t[ti]; res += null; ti++ }
                }
            }
        }
        return if (ti < t.size) M.Fail(c, ti, res) else M.Full(c, args, exact, res)
    }

    private fun tokenize(line: String) = line.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

    private fun available(d: Device, mode: Mode) = commands.filter { mode in it.modes && d.kind in it.kinds }

    /** Ambiguity check: does token i prefix-match more than one distinct keyword among commands sharing the earlier keywords? */
    private fun ambiguousAt(d: Device, mode: Mode, t: List<String>): Int? {
        val cands = available(d, mode)
        for (i in t.indices) {
            val words = cands.mapNotNull { c ->
                // keyword expected at token i if earlier tokens line up 1:1 with keyword tokens
                if (i >= c.toks.size) null
                else if ((0 until i).all { j -> val k = c.toks[j]; k is Tok.Kw && k.w.startsWith(t[j], true) }) (c.toks[i] as? Tok.Kw)?.w
                else null
            }.distinct().filter { it.startsWith(t[i], true) }
            if (words.size > 1 && words.none { it.equals(t[i], true) }) return i
            if (words.isEmpty()) return null
        }
        return null
    }

    private val UNSUPPORTED = listOf(
        "router ospf" to "OSPF is taught with animations and output interpretation in the OSPF lesson; this trainer does not model dynamic routing.",
        "router" to "Dynamic routing protocols are not modelled in this trainer.",
        "access-list" to "ACLs are taught with the step-through ACL evaluator animation; they are not modelled in the CLI trainer.",
        "ip access-list" to "ACLs are taught with the step-through ACL evaluator animation; they are not modelled in the CLI trainer.",
        "ip nat" to "NAT/PAT is taught with the translation-table animation; it is not modelled in the CLI trainer.",
        "ip dhcp" to "DHCP is taught with the DORA and relay animations; it is not modelled in the CLI trainer.",
        "ip helper-address" to "DHCP relay is shown in the DHCP relay animation; it is not modelled in the CLI trainer.",
        "ip routing" to "Layer 3 switching is not modelled; use router-on-a-stick on R1 in this trainer.",
        "spanning-tree" to "STP is taught with the STP animation; it is not modelled in the CLI trainer.",
        "channel-group" to "EtherChannel is taught with the EtherChannel animation; it is not modelled in the CLI trainer.",
        "switchport trunk encapsulation" to "This platform (2960-style) supports only 802.1Q, so the command does not exist here. On switches that also support ISL (for example 3560) you must enter 'switchport trunk encapsulation dot1q' before 'switchport mode trunk'.",
        "switchport port-security" to "Port security is covered in the Layer 2 security lesson tables; it is not modelled here.",
        "switchport voice" to "Voice VLANs are explained in the VLAN lesson; they are not modelled here.",
        "line" to "Console/VTY line configuration (passwords, SSH access) is listed in the device-management command table but not modelled here.",
        "username" to "Local users and SSH are listed in the device-management command table but not modelled here.",
        "crypto" to "SSH key generation is not modelled in this trainer.",
        "ip ssh" to "SSH is not modelled in this trainer.", "ip domain-name" to "SSH prerequisites are not modelled in this trainer.",
        "enable secret" to "Passwords are not modelled in this trainer.", "enable password" to "Passwords are not modelled in this trainer.",
        "service" to "Service options are not modelled.", "banner" to "Banners are not modelled.", "ntp" to "NTP is not modelled.",
        "logging" to "Syslog configuration is not modelled.", "snmp-server" to "SNMP is not modelled.", "cdp" to "CDP is not modelled.",
        "lldp" to "LLDP is not modelled.", "vtp" to "VTP is not modelled; the trainer behaves like a VTP server in a single-switch domain.",
        "ipv6" to "IPv6 is taught in the IPv6 lesson tables; it is not modelled in this trainer.",
        "show mac address-table" to "MAC learning is shown in the ARP and switching animation; the trainer does not model MAC tables.",
        "show spanning-tree" to "STP is not modelled.", "show cdp" to "CDP is not modelled.", "show ip ospf" to "OSPF is not modelled.",
        "show version" to "Platform details are not modelled.",
        "show interfaces" to "Supported forms: 'show interfaces trunk', 'show interfaces status', 'show interfaces <name> switchport'.",
        "show ip protocols" to "Dynamic routing is not modelled.", "show history" to "Use the ↑/↓ history buttons.",
        "debug" to "Debugging is not modelled.", "traceroute" to "Use ping and the reachability trace in the Topology tab.",
        "telnet" to "Remote sessions are not modelled; select the device in the device picker instead.", "ssh" to "Remote sessions are not modelled; select the device in the device picker instead.",
        "clock" to "The clock is not modelled.", "terminal" to "Terminal settings are not modelled."
    )
    private fun unsupported(t: List<String>): String? {
        val low = t.map { it.lowercase() }.let { if (it.firstOrNull() == "no") it.drop(1) else it }
        return UNSUPPORTED.firstOrNull { (phrase, _) ->
            val p = phrase.split(" ")
            low.size >= p.size && p.indices.all { i -> p[i] == low[i] || (low[i].length >= 2 && p[i].startsWith(low[i])) }
        }?.let { "'${it.first}' is a real IOS command, but it is outside this trainer's model. Nothing was changed. ${it.second}" }
    }

    private fun event(devId: String, id: String) { state = state.copy(events = state.events + "$devId:$id") }

    // ------------------------------------------------------------------ public API
    fun prompt(devId: String) = state.dev(devId).prompt

    /** Executes one line on a device and returns the transcript lines (including the echoed prompt). */
    fun run(devId: String, rawLine: String): List<OutLine> {
        val out = mutableListOf<OutLine>()
        val d0 = state.dev(devId)
        out += OutLine(d0.prompt + rawLine, "echo")
        val line = rawLine.trimEnd()
        if (line.isBlank()) return out
        if (line.trim().endsWith("?")) { out += help(devId, line.trim()); event(devId, "help"); return out }
        state = state.copy(history = state.history + (devId to ((state.history[devId] ?: emptyList()) + line).takeLast(50)))
        if (line.trim().equals("^Z", true)) { if (d0.session.mode.isConfig) state = state.update(d0.copy(session = Session(Mode.PRIV))); return out }
        val t = tokenize(line)

        // 'do' runs an EXEC command from configuration mode
        if (d0.session.mode.isConfig && "do".startsWith(t[0].lowercase()) && t[0].length >= 2 && t.size > 1) {
            val saved = d0.session
            state = state.update(d0.copy(session = Session(Mode.PRIV)))
            val sub = run(devId, t.drop(1).joinToString(" ")).drop(1)
            state = state.update(state.dev(devId).copy(session = saved))
            return out + sub
        }

        val exec = Exec(devId, out)
        val res = resolve(d0, d0.session.mode, t)
        if (res is M.Full) { res.cmd.exec(exec, res.args); event(devId, res.cmd.id); return out }

        // Submodes fall back to global configuration, as on IOS
        if (d0.session.mode in setOf(Mode.IF, Mode.IF_RANGE, Mode.SUBIF, Mode.VLAN)) {
            val g = resolve(d0, Mode.CONFIG, t)
            if (g is M.Full) { state = state.update(d0.copy(session = Session(Mode.CONFIG))); g.cmd.exec(exec, g.args); event(devId, g.cmd.id); return out }
        }
        explainFailure(d0, t, line, res, out)
        return out
    }

    private fun resolve(d: Device, mode: Mode, t: List<String>): M? {
        val all = available(d, mode).map { match(it, t) }
        val full = all.filterIsInstance<M.Full>()
        if (full.isNotEmpty()) {
            if (ambiguousAt(d, mode, t) != null) return null
            val best = full.maxOf { it.exact }
            val top = full.filter { it.exact == best }
            // Prefer a keyword over an argument at the same position (e.g. 'add' vs a VLAN list).
            return top.maxByOrNull { f -> f.resolved.count { it != null } }
        }
        return all.firstOrNull { it is M.Incomplete } ?: all.filterIsInstance<M.Fail>().maxByOrNull { it.at }
    }

    private fun explainFailure(d: Device, t: List<String>, line: String, res: M?, out: MutableList<OutLine>) {
        val mode = d.session.mode
        val amb = ambiguousAt(d, mode, t)
        if (amb != null) {
            out += OutLine("% Ambiguous command:  \"${line.trim()}\"", "err")
            val words = available(d, mode).mapNotNull { c -> (c.toks.getOrNull(amb) as? Tok.Kw)?.w?.takeIf { it.startsWith(t[amb], true) } }.distinct()
            out += OutLine("[Trainer] '${t[amb]}' could mean: ${words.joinToString()}. Type more letters.", "note"); return
        }
        if (res is M.Incomplete) {
            out += OutLine("% Incomplete command.", "err")
            out += OutLine("[Trainer] " + (unsupported(t) ?: "Add the missing keyword or value. Type the command followed by ' ?' to see what comes next."), "note"); return
        }
        val at = (res as? M.Fail)?.at ?: 0
        // Caret under the first character of the offending token
        var pos = 0; var idx = 0
        Regex("\\S+").findAll(line).forEach { m -> if (idx == at) pos = m.range.first; idx++ }
        if (at >= t.size) pos = line.length
        out += OutLine(" ".repeat(d.prompt.length + pos) + "^", "err")
        out += OutLine("% Invalid input detected at '^' marker.", "err")
        unsupported(t)?.let { out += OutLine("[Trainer] $it", "note"); return }
        // Same command valid in another mode?
        val otherModes = Mode.values().filter { it != mode && resolve(d, it, t) is M.Full }.toSet()
        val otherKind = Mode.values().any { m -> commands.any { m in it.modes && d.kind !in it.kinds && match(it, t) is M.Full } }
        val tip = when {
            (Mode.PRIV in otherModes) && mode == Mode.USER -> "That command needs privileged EXEC mode. Type 'enable' first."
            (Mode.PRIV in otherModes || Mode.USER in otherModes) && mode.isConfig -> "That is an EXEC command. From configuration mode, prefix it with 'do' (for example 'do ${line.trim()}')."
            Mode.CONFIG in otherModes && !mode.isConfig -> "That is a configuration command. Enter 'configure terminal' first."
            otherModes.any { it in setOf(Mode.IF, Mode.IF_RANGE, Mode.SUBIF) } -> "That is an interface command. Select an interface first, e.g. 'interface ${if (d.kind == "router") "g0/0" else "fa0/1"}'."
            Mode.VLAN in otherModes -> "That is a VLAN-mode command. Enter 'vlan <id>' first."
            otherKind -> "That command is valid on a ${if (d.kind == "router") "switch" else "router"} in this trainer, not on this ${d.kind}."
            else -> "Check the spelling and parameters. Type '?' to list the commands available in this mode."
        }
        out += OutLine("[Trainer] $tip", "note")
    }

    /** Context help: '?' lists the next possible words; 'sh?' lists completions of a partial word. */
    fun help(devId: String, line: String): List<OutLine> {
        val d = state.dev(devId); val mode = d.session.mode
        val base = line.dropLast(1)
        val partial = base.isNotEmpty() && !base.endsWith(" ")
        val t = tokenize(base)
        val done = if (partial) t.dropLast(1) else t
        val nexts = available(d, mode).mapNotNull { c ->
            when (val m = match(c, done)) {
                is M.Incomplete -> c.toks[m.pi]
                is M.Full -> if (!partial) Tok.Kw("<cr>", "") else null
                else -> null
            }
        }.distinct()
        val shown = if (partial) nexts.filterIsInstance<Tok.Kw>().filter { it.w.startsWith(t.last(), true) } else nexts
        if (shown.isEmpty()) return listOf(OutLine("% Unrecognized command", "err"))
        if (partial) return listOf(OutLine(shown.joinToString("  ") { (it as Tok.Kw).w }, "out"))
        return shown.sortedBy { if (it is Tok.Kw) it.w else "~" }.map { tk ->
            when (tk) { is Tok.Kw -> OutLine("  " + tk.w.padEnd(18) + tk.help, "out"); is Tok.Arg -> OutLine("  " + tk.kind.label.padEnd(18) + tk.help, "out") }
        }.distinctBy { it.text }
    }

    /** Tab completion of the last word when it is an unambiguous keyword prefix. */
    fun complete(devId: String, line: String): String {
        if (line.isBlank() || line.endsWith(" ")) return line
        val opts = help(devId, "$line?").firstOrNull()?.takeIf { it.kind == "out" }?.text?.split(Regex("\\s+"))?.filter { it.isNotBlank() } ?: return line
        if (opts.size != 1) return line
        val t = line.trimEnd(); val cut = t.lastIndexOf(' ') + 1
        return t.substring(0, cut) + opts[0] + " "
    }

    /** Every supported command, for the reference screen (generated from the grammar so it cannot drift). */
    fun reference(): List<Triple<String, String, String>> = commands.filter { it.id != "shiproutesw" }.map { c ->
        val pattern = c.toks.joinToString(" ") { when (it) { is Tok.Kw -> it.w; is Tok.Arg -> "<${it.kind.label}>" } }
        val modes = c.modes.joinToString(" / ") { modeLabel(it) } + if (c.kinds.size == 1) " (${c.kinds.first()} only)" else ""
        Triple(pattern, modes, c.summary)
    }

    companion object {
        fun modeLabel(m: Mode) = when (m) {
            Mode.USER -> "User EXEC"; Mode.PRIV -> "Privileged EXEC"; Mode.CONFIG -> "Global config"; Mode.IF -> "Interface"
            Mode.IF_RANGE -> "Interface range"; Mode.SUBIF -> "Subinterface"; Mode.VLAN -> "VLAN config"
        }
    }
}
