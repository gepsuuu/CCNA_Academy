package com.ccnaacademy.app.lab

/**
 * Lesson-embedded learning content added in v1.4: explanatory tables, command walkthroughs, output-interpretation
 * activities (generated live by the CLI engine so the output can never disagree with the trainer), teach-back prompts,
 * and links to existing comparisons, animations and labs.
 */

/** kind: reference (static facts), demo (scripted example), troubleshoot (symptom → evidence → fix). */
data class LessonTable(
    val id: String, val title: String, val headers: List<String>, val rows: List<List<String>>,
    val kind: String = "reference", val notes: List<String> = emptyList(), val recall: Boolean = true,
    val caption: String = ""
)

data class CommandStep(
    val command: String, val mode: String, val purpose: String, val verify: String,
    val mistake: String, val undo: String = "", val note: String = ""
)

data class Walkthrough(val id: String, val title: String, val device: String, val steps: List<CommandStep>)

/** Run [prep] on a lab, then show [command]'s real trainer output with annotations. */
data class OutputActivity(
    val id: String, val title: String, val labId: String, val prep: List<Pair<String, String>>,
    val device: String, val command: String, val annotations: List<Pair<String, String>>,
    val question: String, val options: List<String>, val answer: Int, val explanation: String
) {
    fun render(): List<String> {
        val lab = LabCatalog.byId.getValue(labId)
        val e = CliEngine(lab.initial())
        lab.initial().devices.filter { it.configurable }.forEach { d -> e.state = e.state.update(e.state.dev(d.id).copy(session = Session(Mode.PRIV))) }
        prep.forEach { (d, c) -> e.run(d, c) }
        e.state = e.state.update(e.state.dev(device).copy(session = Session(Mode.PRIV)))
        return e.run(device, command).filter { it.kind == "out" }.map { it.text }
    }
}

data class TeachBack(val prompt: String, val checklist: List<String>, val model: String)

data class LessonExtras(
    val lessonId: String,
    val tables: List<LessonTable> = emptyList(),
    val comparisons: List<String> = emptyList(),        // study.json comparison ids
    val walkthroughs: List<Walkthrough> = emptyList(),
    val outputs: List<OutputActivity> = emptyList(),
    val teachBack: TeachBack? = null,
    val noLabReason: String? = null                     // configuration-focused lessons without a CLI lab say why
) {
    val animations get() = AnimationCatalog.forLesson(lessonId)
    val labs get() = LabCatalog.forLesson(lessonId)
}

object LessonExtrasCatalog {

    // ---------------------------------------------------------------- shared tables
    private val subnetTable: LessonTable by lazy {
        val rows = (22..30).map { len -> listOf("/$len", Ip.maskStr(len), blockSize(len), (Math.pow(2.0, (32 - len).toDouble()).toLong() - 2).toString()) } +
            listOf(listOf("/31", Ip.maskStr(31), "2 (4th octet)", "2 (point-to-point, RFC 3021)"), listOf("/32", Ip.maskStr(32), "1", "1 (host route / loopback)"))
        LessonTable("t-prefix", "Prefix lengths, masks and sizes", listOf("Prefix", "Mask", "Block size", "Usable hosts"), rows,
            notes = (22..30).map { len -> "Usable hosts = 2^(32−$len) − 2 = ${(1L shl (32 - len)) - 2}. The −2 removes the network and broadcast addresses." } +
                listOf("/31 has no network/broadcast pair; both addresses are usable on point-to-point links.", "/32 identifies exactly one address."),
            caption = "Block size = 256 − the interesting mask octet. Values are computed by the app, not typed by hand.")
    }
    fun blockSize(len: Int): String = when {
        len <= 8 -> "${1 shl (8 - len)} (1st octet)"
        len <= 16 -> "${256 - ((Ip.maskOf(len) shr 16) and 255).toInt()} (2nd octet)"
        len <= 24 -> "${256 - ((Ip.maskOf(len) shr 8) and 255).toInt()} (3rd octet)"
        else -> "${256 - (Ip.maskOf(len) and 255).toInt()} (4th octet)"
    }

    private val accessTrunk = LessonTable("t-access-trunk", "Access port vs trunk port", listOf("Property", "Access port", "Trunk port"), listOf(
        listOf("VLANs carried", "One data VLAN (plus optional voice VLAN)", "Many VLANs (allowed list, default 1-4094)"),
        listOf("Tagging", "Untagged", "802.1Q tagged, except the native VLAN"),
        listOf("Typical neighbour", "PC, printer, AP in local mode, IP phone", "Switch, router (ROAS), AP in FlexConnect/autonomous with several SSIDs"),
        listOf("Key commands", "switchport mode access / switchport access vlan N", "switchport mode trunk / trunk native vlan N / trunk allowed vlan …"),
        listOf("Verify with", "show vlan brief, show interfaces X switchport", "show interfaces trunk"),
        listOf("Common mistake", "Leaving DTP on (mode dynamic) for end-host ports", "Native VLAN mismatch; replacing the allowed list without 'add'")),
        notes = listOf("Trunks carry many broadcast domains on one link.", "The 4-byte tag sits between the source MAC and the EtherType.", "APs in local mode tunnel client traffic to the WLC over CAPWAP, so an access port is enough.",
            "Use the command walkthrough below for each command's mode and verification.", "Trunk ports never appear in 'show vlan brief'.", "Both are classic exam distractors."))

    private val dtp = LessonTable("t-dtp", "DTP outcomes (static or dynamic switchport modes)", listOf("Side A", "Side B", "Result"), listOf(
        listOf("trunk", "trunk", "Trunk"), listOf("trunk", "dynamic auto", "Trunk"), listOf("trunk", "dynamic desirable", "Trunk"),
        listOf("dynamic desirable", "dynamic desirable", "Trunk"), listOf("dynamic desirable", "dynamic auto", "Trunk"),
        listOf("dynamic auto", "dynamic auto", "Access (nobody asks)"), listOf("access", "any", "Access (mismatch if the other side is trunk)"),
        listOf("trunk + nonegotiate", "dynamic auto/desirable", "Access on the dynamic side (no DTP frames received)")),
        notes = listOf("", "Auto answers DTP requests from a trunk port.", "", "", "Desirable actively asks; auto answers.", "Neither side initiates, so the link stays access.",
            "A trunk facing an access port creates a mismatch; frames in the native VLAN may still pass.", "Use 'switchport nonegotiate' only with static trunk/access ports on both ends."),
        caption = "Best practice: hard-code 'switchport mode access' or 'switchport mode trunk' and do not rely on DTP.")

    private val trunkTrouble = LessonTable("t-trunk-ts", "Troubleshooting VLANs and trunks", listOf("Symptom", "Possible cause", "Check", "Evidence", "Fix", "Verify"), listOf(
        listOf("One VLAN fails across switches; others work", "VLAN not allowed on one trunk end", "show interfaces trunk (both ends)", "VLAN missing from 'allowed'", "switchport trunk allowed vlan add N", "Run the same test again; both lists match"),
        listOf("One host has no connectivity; port is up", "Access VLAN deleted or never created", "show interfaces X switchport", "Access Mode VLAN: N (Inactive)", "vlan N (recreate)", "show vlan brief lists the port"),
        listOf("Traffic leaks between VLANs; CDP warnings", "Native VLAN mismatch", "show interfaces trunk / CDP log", "Different 'Native vlan' on each end", "switchport trunk native vlan N on both", "Native column matches"),
        listOf("Link up but not trunking", "DTP: auto + auto, or access on one end", "show interfaces X switchport", "Operational Mode: static access", "switchport mode trunk on both", "show interfaces trunk lists the port"),
        listOf("Host in the wrong subnet / no DHCP lease", "Port in the wrong access VLAN", "show vlan brief", "Port listed under another VLAN", "switchport access vlan N", "Host gets the expected address")),
        kind = "troubleshoot", recall = false, caption = "Causes are possibilities — confirm with evidence before changing anything.")

    // ---------------------------------------------------------------- walkthroughs
    private val vlanWalk = Walkthrough("w-vlan", "Build VLANs and access ports", "SW1", listOf(
        CommandStep("vlan 10", "Global config", "Creates VLAN 10 and enters VLAN config mode.", "show vlan brief", "Typing it in privileged EXEC (needs configure terminal).", "no vlan 10 (ports in VLAN 10 become inactive)"),
        CommandStep("name SALES", "VLAN config", "Gives the VLAN a readable name (max 32 characters).", "show vlan brief → Name column", "Trying to rename VLAN 1 (not allowed)."),
        CommandStep("interface fa0/1", "Global config", "Selects the port. 'interface range fa0/1 - 4' configures several.", "Prompt shows (config-if)#", "Wrong port number — check 'show interfaces status'."),
        CommandStep("switchport mode access", "Interface", "Hard-codes access mode and disables DTP negotiation.", "show interfaces fa0/1 switchport → Administrative Mode: static access", "Leaving the default 'dynamic auto'.", "no switchport mode"),
        CommandStep("switchport access vlan 10", "Interface", "Puts the port in VLAN 10. If VLAN 10 does not exist, IOS creates it (with a default name).", "show vlan brief", "Assigning the wrong VLAN — host lands in another subnet.", "no switchport access vlan (back to VLAN 1)",
            "VLANs 2-1001 are stored in vlan.dat on a VTP server/client switch — not in running-config.")))

    private val trunkWalk = Walkthrough("w-trunk", "Configure an 802.1Q trunk", "SW1", listOf(
        CommandStep("interface g0/1", "Global config", "Selects the uplink.", "Prompt shows (config-if)#", "Configuring only one end of the link."),
        CommandStep("switchport mode trunk", "Interface", "Makes the port a static trunk. On switches that also support ISL, 'switchport trunk encapsulation dot1q' must come first.", "show interfaces trunk → Mode on, Status trunking", "Relying on DTP (auto + auto never trunks).", "switchport mode access"),
        CommandStep("switchport trunk native vlan 99", "Interface", "Untagged frames belong to VLAN 99 instead of VLAN 1.", "show interfaces trunk → Native vlan", "Setting it on one end only (native VLAN mismatch).", "no switchport trunk native vlan"),
        CommandStep("switchport trunk allowed vlan 10,20,99", "Interface", "REPLACES the allowed list.", "show interfaces trunk → Vlans allowed on trunk", "Using this form when you meant to add one VLAN.", "no switchport trunk allowed vlan (back to all)"),
        CommandStep("switchport trunk allowed vlan add 30", "Interface", "Appends VLAN 30 to the existing list.", "show interfaces trunk", "Forgetting 'add' — every other VLAN is removed.", "switchport trunk allowed vlan remove 30")))

    private val roasWalk = Walkthrough("w-roas", "Router-on-a-stick", "R1", listOf(
        CommandStep("interface g0/0", "Global config", "Physical interface facing the switch trunk.", "show ip interface brief", "Leaving it shut down — subinterfaces stay down too."),
        CommandStep("no shutdown", "Interface", "Router interfaces are shut down by default.", "Status up / Protocol up", "Enabling only the subinterfaces.", "shutdown"),
        CommandStep("interface g0/0.10", "Global config", "Creates a logical subinterface for VLAN 10.", "show ip interface brief", "Subinterface number ≠ VLAN is legal but confusing."),
        CommandStep("encapsulation dot1Q 10", "Subinterface", "Accepts frames tagged with VLAN 10. Add 'native' for the native VLAN.", "show running-config", "Configuring the IP address first (rejected).", "no encapsulation dot1Q"),
        CommandStep("ip address 192.168.10.1 255.255.255.0", "Subinterface", "Default gateway address for VLAN 10 hosts.", "show ip route → C 192.168.10.0/24", "Using an address outside the hosts' subnet.", "no ip address")))

    private val staticWalk = Walkthrough("w-static", "Static and default routes", "R1", listOf(
        CommandStep("ip route 192.168.2.0 255.255.255.0 10.0.12.2", "Global config", "Network route via a next-hop address.", "show ip route → S 192.168.2.0/24 [1/0] via 10.0.12.2", "Next hop that is not reachable — the route is not installed.", "no ip route 192.168.2.0 255.255.255.0"),
        CommandStep("ip route 0.0.0.0 0.0.0.0 10.0.12.1", "Global config", "Default route (matches everything, shortest prefix).", "Gateway of last resort is set", "Forgetting the return path on the far router."),
        CommandStep("ip route 192.168.2.0 255.255.255.0 10.0.13.2 5", "Global config", "Floating static: AD 5 keeps it out of the table while a better route (AD 1) exists.", "Appears only when the primary disappears", "Giving the backup an AD lower than the primary."),
        CommandStep("ip route 10.9.9.9 255.255.255.255 10.0.12.2", "Global config", "Host route (/32).", "show ip route", "Host bits set in a network route → '%Inconsistent address and mask'.")))

    private val cliWalk = Walkthrough("w-cli", "CLI modes and configuration files", "Switch", listOf(
        CommandStep("enable", "User EXEC (>)", "Enters privileged EXEC (#).", "Prompt ends with #", "Trying configuration commands at the > prompt.", "disable"),
        CommandStep("configure terminal", "Privileged EXEC", "Enters global configuration mode.", "Prompt shows (config)#", "Abbreviating to 'c' (ambiguous) — 'conf t' works.", "end or Ctrl+Z"),
        CommandStep("do show running-config", "Any config mode", "Runs an EXEC command without leaving configuration mode.", "Output appears", "Forgetting 'do' — '% Invalid input'."),
        CommandStep("copy running-config startup-config", "Privileged EXEC", "Saves RAM configuration to NVRAM.", "show startup-config", "Assuming changes are saved automatically.", "erase startup-config (deletes the saved copy)"),
        CommandStep("reload", "Privileged EXEC", "Restarts the device; running-config is rebuilt from startup-config.", "Prompt returns to >", "Reloading with unsaved changes.")))

    private val sshTable = LessonTable("t-ssh", "Enabling SSH (reference — not modelled in the trainer)", listOf("Step", "Command", "Mode", "Why"), listOf(
        listOf("1", "hostname SW1", "Global", "The RSA key name uses hostname + domain"),
        listOf("2", "ip domain-name example.com", "Global", "Required before generating keys"),
        listOf("3", "crypto key generate rsa modulus 2048", "Global", "SSH needs an RSA key pair (≥768 bits for SSHv2)"),
        listOf("4", "ip ssh version 2", "Global", "Disable the weaker SSHv1"),
        listOf("5", "username admin secret <password>", "Global", "Local credentials for 'login local'"),
        listOf("6", "line vty 0 15 / login local / transport input ssh", "Line", "Use local users and allow only SSH (not Telnet)")),
        notes = listOf("", "", "", "", "'secret' stores a hashed password; 'password' is weaker.", "Verify with 'show ip ssh' and 'show ssh'."), caption = "These commands are listed for study. The trainer reports them as unsupported rather than pretending they worked.")

    private val modesTable = LessonTable("t-modes", "IOS modes", listOf("Mode", "Prompt", "Enter with", "Leave with", "Used for"), listOf(
        listOf("User EXEC", "Switch>", "Log in", "exit", "Limited show commands, ping"),
        listOf("Privileged EXEC", "Switch#", "enable", "disable", "All show commands, copy, reload"),
        listOf("Global config", "Switch(config)#", "configure terminal", "end / exit", "Device-wide settings: hostname, VLANs, routes"),
        listOf("Interface config", "Switch(config-if)#", "interface fa0/1", "exit / end", "Port settings"),
        listOf("VLAN config", "Switch(config-vlan)#", "vlan 10", "exit / end", "VLAN name"),
        listOf("Subinterface", "Router(config-subif)#", "interface g0/0.10", "exit / end", "Router-on-a-stick")))

    private val runStart = LessonTable("t-runstart", "running-config vs startup-config", listOf("", "running-config", "startup-config"), listOf(
        listOf("Stored in", "RAM", "NVRAM"), listOf("Changes when", "Immediately as you type", "Only when you save"), listOf("Survives reload", "No", "Yes"),
        listOf("View", "show running-config", "show startup-config"), listOf("Save", "copy running-config startup-config", "—"),
        listOf("Erase", "reload without saving", "erase startup-config")))

    private val ports = LessonTable("t-ports", "Protocols, transports and ports", listOf("Protocol", "Transport", "Number", "Typical use", "Note"), listOf(
        listOf("FTP", "TCP", "20 data / 21 control", "File transfer", "Active mode data from port 20"),
        listOf("SSH", "TCP", "22", "Secure remote CLI", ""), listOf("Telnet", "TCP", "23", "Clear-text remote CLI", "Avoid"),
        listOf("SMTP", "TCP", "25", "Email between servers", ""), listOf("DNS", "UDP / TCP", "53", "Name resolution", "TCP for zone transfers and large responses"),
        listOf("DHCP", "UDP", "67 server / 68 client", "Address assignment", ""), listOf("TFTP", "UDP", "69", "Simple file transfer (IOS images)", "No authentication"),
        listOf("HTTP", "TCP", "80", "Web", ""), listOf("NTP", "UDP", "123", "Time sync", ""), listOf("SNMP", "UDP", "161 agent / 162 traps", "Monitoring", "Informs are acknowledged"),
        listOf("HTTPS", "TCP", "443", "Secure web / REST APIs", ""), listOf("Syslog", "UDP", "514", "Log messages", ""),
        listOf("ICMP", "— (IP protocol 1)", "no port", "ping, unreachable messages", "Not TCP or UDP"),
        listOf("OSPF", "— (IP protocol 89)", "no port", "Routing", "IP protocol number, not a port")),
        caption = "Port numbers identify applications inside TCP/UDP. ICMP and OSPF ride directly on IP and use IP protocol numbers instead.")

    private val ipv6Types = LessonTable("t-ipv6", "IPv6 address types", listOf("Type", "Prefix", "Scope / purpose"), listOf(
        listOf("Global unicast", "2000::/3", "Internet-routable"), listOf("Unique local", "FC00::/7 (FD00::/8 used)", "Private, like RFC 1918"),
        listOf("Link-local", "FE80::/10", "Same link only; every IPv6 interface has one; next hop in routing"),
        listOf("Multicast", "FF00::/8", "One-to-many; FF02::1 all nodes, FF02::2 all routers"), listOf("Loopback", "::1/128", "Local host"),
        listOf("Unspecified", "::/128", "No address yet (e.g. source during DAD)"), listOf("Anycast", "(from unicast space)", "Nearest of several devices")),
        caption = "IPv6 has no broadcast; multicast replaces it.")

    private val adTable = LessonTable("t-ad", "Administrative distance (route source trust)", listOf("Source", "AD"), listOf(
        listOf("Connected", "0"), listOf("Static", "1"), listOf("eBGP", "20"), listOf("EIGRP (internal)", "90"), listOf("OSPF", "110"),
        listOf("RIP", "120"), listOf("EIGRP (external)", "170"), listOf("iBGP", "200"), listOf("Unusable", "255")),
        caption = "AD breaks ties between DIFFERENT sources for the SAME prefix. It never overrides longest-prefix match.")

    private val routeFields = LessonTable("t-route-fields", "Reading a route", listOf("Field", "Example", "Meaning"), listOf(
        listOf("Code", "S / O / C / L", "How the route was learned"), listOf("Prefix", "192.168.2.0/24", "Destination network and length (longer = more specific)"),
        listOf("[AD/metric]", "[110/2]", "Trust of the source / cost within that protocol"), listOf("Next hop", "via 10.0.12.2", "Router to send the packet to"),
        listOf("Exit interface", "GigabitEthernet0/1", "Interface used to reach the next hop"), listOf("Gateway of last resort", "10.0.12.1 to network 0.0.0.0", "Default route in use")),
        kind = "demo")

    private val staticTypes = LessonTable("t-static-types", "Static route types", listOf("Type", "Example", "Use"), listOf(
        listOf("Network route", "ip route 192.168.2.0 255.255.255.0 10.0.12.2", "Reach one remote subnet"),
        listOf("Default route", "ip route 0.0.0.0 0.0.0.0 10.0.12.1", "Everything not matched elsewhere (stub sites)"),
        listOf("Host route", "ip route 10.9.9.9 255.255.255.255 10.0.12.2", "One address (/32)"),
        listOf("Floating static", "ip route 192.168.2.0 255.255.255.0 10.0.13.2 5", "Backup path with higher AD"),
        listOf("IPv6 default", "ipv6 route ::/0 2001:db8:12::2", "IPv6 equivalent of 0.0.0.0/0")))

    private val staticTrouble = LessonTable("t-static-ts", "Troubleshooting static routing", listOf("Symptom", "Possible cause", "Check", "Fix"), listOf(
        listOf("Request leaves but no reply", "No return route on the far router", "show ip route on the far router", "Add a route or default route back"),
        listOf("Static route missing from the table", "Next hop unreachable / exit interface down", "show ip interface brief", "Fix the interface or next hop"),
        listOf("'%Inconsistent address and mask'", "Host bits set in the prefix", "Re-read the command", "Use the network address"),
        listOf("Traffic takes the backup path", "Primary route removed or AD set wrongly", "show ip route", "Give the backup a higher AD")), kind = "troubleshoot", recall = false)

    private val ospfReq = LessonTable("t-ospf-req", "OSPF neighbor requirements", listOf("Must match", "Must be unique", "Does not need to match"), listOf(
        listOf("Area ID", "Router ID", "Process ID"), listOf("Subnet and mask", "Interface IP", "Interface cost"),
        listOf("Hello / dead timers", "", "Router priority"), listOf("Authentication", "", ""), listOf("Stub area flags", "", ""), listOf("MTU (else stuck in ExStart/Exchange)", "", "")),
        caption = "Process IDs are locally significant: R1 can run 'router ospf 1' and R2 'router ospf 99'.")

    private val dhcpMsgs = LessonTable("t-dhcp", "DHCP messages", listOf("Message", "From → To", "Typical delivery", "Purpose"), listOf(
        listOf("Discover", "Client → any server", "Broadcast", "Find a server"), listOf("Offer", "Server → client", "Broadcast or unicast", "Propose an address"),
        listOf("Request", "Client → server", "Broadcast", "Accept one offer"), listOf("Ack", "Server → client", "Broadcast or unicast", "Confirm the lease"),
        listOf("Release", "Client → server", "Unicast", "Give the address back")))

    private val natTerms = LessonTable("t-nat", "NAT address terms", listOf("Term", "Example", "Meaning"), listOf(
        listOf("Inside local", "192.168.1.10", "Inside host's real (private) address"), listOf("Inside global", "203.0.113.5", "How the inside host appears outside"),
        listOf("Outside global", "198.51.100.20", "Outside host's real address"), listOf("Outside local", "198.51.100.20", "How the outside host appears inside (usually the same)")))

    private val wildcard = LessonTable("t-wildcard", "Wildcard masks", listOf("Wildcard", "Matches", "Equivalent"), listOf(
        listOf("0.0.0.0", "Exactly one address", "host 10.1.1.10"), listOf("0.0.0.255", "First three octets fixed", "a /24"),
        listOf("0.0.0.63", "A /26 block", "e.g. 192.168.1.64 – .127"), listOf("0.0.255.255", "First two octets fixed", "a /16"),
        listOf("255.255.255.255", "Any address", "any")), caption = "Wildcard = 255.255.255.255 − subnet mask. 0 bits must match; 1 bits are ignored.")

    private val l2sec = LessonTable("t-l2sec", "Layer 2 security features", listOf("Feature", "Stops", "How", "Key command"), listOf(
        listOf("Port security", "Unauthorised devices, MAC flooding", "Limits MACs per port; violation shutdown/restrict/protect", "switchport port-security"),
        listOf("DHCP snooping", "Rogue DHCP servers", "Only trusted ports may send server messages; builds a binding table", "ip dhcp snooping / ip dhcp snooping trust"),
        listOf("Dynamic ARP Inspection", "ARP spoofing / poisoning", "Checks ARP against the DHCP snooping binding table", "ip arp inspection vlan N"),
        listOf("BPDU Guard", "Rogue switches on edge ports", "Err-disables a PortFast port that receives a BPDU", "spanning-tree bpduguard enable")))

    private val wlanModes = LessonTable("t-ap-modes", "AP modes", listOf("Mode", "Behaviour"), listOf(
        listOf("Local", "Default lightweight mode; client data tunnelled to the WLC over CAPWAP"), listOf("FlexConnect", "Can switch traffic locally at a branch if the WLC link fails"),
        listOf("Monitor", "No clients; scans for rogues/IDS"), listOf("Sniffer", "Captures 802.11 frames for analysis"),
        listOf("Bridge / mesh", "Point-to-point or mesh links between APs"), listOf("SE-Connect", "Spectrum analysis")))

    private val wlanSec = LessonTable("t-wpa", "Wi-Fi security", listOf("Standard", "Encryption", "Personal auth", "Enterprise auth"), listOf(
        listOf("WPA", "TKIP (RC4)", "PSK", "802.1X/EAP"), listOf("WPA2", "AES-CCMP", "PSK", "802.1X/EAP"), listOf("WPA3", "AES-CCMP (GCMP-256 in 192-bit Enterprise mode)", "SAE", "802.1X/EAP")))

    private val mgmt = LessonTable("t-mgmt", "Network management protocols", listOf("Protocol", "Transport/port", "Direction", "Exam point"), listOf(
        listOf("NTP", "UDP 123", "Client ← server", "Lower stratum = closer to the reference clock"), listOf("SNMP get/set", "UDP 161", "Manager → agent", "v3 adds authentication and encryption"),
        listOf("SNMP trap/inform", "UDP 162", "Agent → manager", "Informs are acknowledged"), listOf("Syslog", "UDP 514", "Device → server", "Severity 0 (emergency) to 7 (debugging)")))

    private val syslog = LessonTable("t-syslog", "Syslog severity levels", listOf("Level", "Keyword"), listOf(
        listOf("0", "Emergency"), listOf("1", "Alert"), listOf("2", "Critical"), listOf("3", "Error"), listOf("4", "Warning"), listOf("5", "Notification"), listOf("6", "Informational"), listOf("7", "Debugging")),
        caption = "'logging trap 4' sends levels 0-4. Mnemonic: Every Awesome Cisco Engineer Will Need Ice-cream Daily.")

    private val rest = LessonTable("t-rest", "REST: HTTP methods and status codes", listOf("Item", "Meaning"), listOf(
        listOf("GET", "Read (CRUD: Read)"), listOf("POST", "Create"), listOf("PUT / PATCH", "Replace / partially update"), listOf("DELETE", "Delete"),
        listOf("200 OK / 201 Created", "Success"), listOf("400 Bad Request", "Malformed request"), listOf("401 Unauthorized", "Not authenticated"),
        listOf("403 Forbidden", "Authenticated but not allowed"), listOf("404 Not Found", "Resource does not exist")))

    private val etherModes = LessonTable("t-ec-modes", "EtherChannel negotiation", listOf("Side A", "Side B", "Channel forms?"), listOf(
        listOf("LACP active", "LACP active", "Yes"), listOf("LACP active", "LACP passive", "Yes"), listOf("LACP passive", "LACP passive", "No"),
        listOf("PAgP desirable", "PAgP desirable / auto", "Yes"), listOf("PAgP auto", "PAgP auto", "No"), listOf("on", "on", "Yes (no negotiation)"),
        listOf("on", "active / desirable", "No")), caption = "Members must match speed, duplex, access/trunk mode, allowed and native VLANs.")

    private val stpStates = LessonTable("t-stp-ports", "STP port roles and states", listOf("Role", "Where", "State"), listOf(
        listOf("Root port", "Non-root switch, best path to root", "Forwarding"), listOf("Designated port", "One per segment, best path from the segment to root", "Forwarding"),
        listOf("Alternate (RSTP)", "Backup path to root", "Discarding"), listOf("Backup (RSTP)", "Backup on a shared segment", "Discarding"),
        listOf("Non-designated (802.1D)", "Loser on a segment", "Blocking")))

    private val intfTrouble = LessonTable("t-intf-ts", "Interface status and common causes", listOf("Status / Protocol", "Likely cause", "Next check"), listOf(
        listOf("administratively down / down", "'shutdown' configured", "no shutdown"), listOf("down / down", "Cable, far end down, speed mismatch", "Check cable and far end"),
        listOf("up / down", "Layer 2 problem (e.g. encapsulation/keepalive on serial)", "Compare both ends"),
        listOf("err-disabled", "Port security, BPDU Guard, etc.", "show interfaces status err-disabled; fix cause; shut/no shut"),
        listOf("up / up but late collisions", "Duplex mismatch", "show interfaces → duplex on both ends")), kind = "troubleshoot", recall = false)

    private val macIp = LessonTable("t-mac-ip", "MAC address vs IP address", listOf("", "MAC address", "IPv4 address"), listOf(
        listOf("Layer", "2 (data link)", "3 (network)"), listOf("Length", "48 bits (12 hex digits)", "32 bits (dotted decimal)"),
        listOf("Scope", "Local link only — rewritten every hop", "End to end (unless NAT)"), listOf("Assigned by", "Manufacturer (burned in)", "Admin or DHCP"),
        listOf("Resolved by", "ARP (IP → MAC)", "DNS (name → IP)"), listOf("Used by", "Switches", "Routers")))

    private val domains = LessonTable("t-domains", "Collision domains vs broadcast domains", listOf("Device", "Separates collision domains?", "Separates broadcast domains?"), listOf(
        listOf("Hub", "No", "No"), listOf("Switch", "Yes — each port", "No (unless VLANs)"), listOf("Switch with VLANs", "Yes", "Yes — one per VLAN"), listOf("Router", "Yes", "Yes — each interface")))


    // ---------------------------------------------------------------- tables for the remaining lessons
    private val components = LessonTable("t-components", "Network components", listOf("Component", "Main layer", "Role"), listOf(
        listOf("Router", "3", "Forwards packets between networks using a routing table"), listOf("Layer 2 switch", "2", "Forwards frames within a VLAN using a MAC table"),
        listOf("Layer 3 switch", "2 + 3", "Switching plus routing between VLANs (SVIs)"), listOf("Next-generation firewall", "3-7", "Stateful filtering plus application awareness and IPS"),
        listOf("IPS", "3-7", "Inline; can block malicious traffic (an IDS only alerts)"), listOf("Access point", "1-2", "Bridges wireless clients onto the wired LAN"),
        listOf("Wireless LAN controller", "—", "Centrally manages lightweight APs (CAPWAP)"), listOf("Endpoint", "—", "PC, phone, printer, IoT device"),
        listOf("Server", "—", "Provides services (web, DNS, DHCP, files)"), listOf("PoE", "1", "Power over the Ethernet cable for APs, phones, cameras")))

    private val topologies = LessonTable("t-topologies", "Topology architectures", listOf("Architecture", "Structure", "Typical use"), listOf(
        listOf("Two-tier (collapsed core)", "Access + combined distribution/core", "Small/medium campus"),
        listOf("Three-tier", "Access → distribution → core", "Large campus"),
        listOf("Spine-leaf", "Every leaf connects to every spine; leaves never connect to each other", "Data centre (predictable east-west latency)"),
        listOf("WAN", "Connects sites over provider networks", "Branch to headquarters"), listOf("SOHO", "One device combines router, switch, AP, firewall", "Home / small office"),
        listOf("On-premises vs cloud", "Your hardware vs provider infrastructure (IaaS/PaaS/SaaS)", "Cost, control and scaling trade-offs")))

    private val binary = LessonTable("t-binary", "Binary place values (one octet)", listOf("Bit", "Value", "Example 172 = 10101100"), listOf(
        listOf("1st", "128", "1 (128)"), listOf("2nd", "64", "0"), listOf("3rd", "32", "1 (32 → 160)"), listOf("4th", "16", "0"),
        listOf("5th", "8", "1 (8 → 168)"), listOf("6th", "4", "1 (4 → 172)"), listOf("7th", "2", "0"), listOf("8th", "1", "0")),
        caption = "Subtract the largest place value that fits, write 1, continue. Each hex digit is exactly 4 bits: 0xAC = 1010 1100 = 172.")

    private val clientCmds = LessonTable("t-client", "Verifying IP settings on clients", listOf("Task", "Windows", "macOS", "Linux"), listOf(
        listOf("IP, mask, gateway, DNS", "ipconfig /all", "ifconfig / networksetup -getinfo Wi-Fi", "ip addr, ip route"),
        listOf("Routing table", "route print", "netstat -rn", "ip route"), listOf("ARP cache", "arp -a", "arp -a", "ip neigh"),
        listOf("Reachability", "ping", "ping", "ping"), listOf("Path", "tracert", "traceroute", "traceroute"), listOf("DNS lookup", "nslookup", "nslookup / dig", "dig / nslookup")))

    private val virtualization = LessonTable("t-virt", "Virtualization building blocks", listOf("Concept", "What it is", "Exam point"), listOf(
        listOf("Type 1 hypervisor", "Runs directly on hardware (bare metal)", "ESXi, Hyper-V, KVM — data centres"),
        listOf("Type 2 hypervisor", "Runs as an application on a host OS", "VirtualBox, VMware Workstation — labs/desktops"),
        listOf("Virtual machine", "Full guest OS on virtual hardware", "Strong isolation, larger, slower to start"),
        listOf("Container", "Isolated process sharing the host kernel", "Lightweight, starts in seconds"),
        listOf("VRF", "Separate routing table instance on one router", "Overlapping IP ranges can coexist")))

    private val cdpLldp = LessonTable("t-cdp-lldp", "CDP vs LLDP", listOf("", "CDP", "LLDP"), listOf(
        listOf("Standard", "Cisco proprietary", "IEEE 802.1AB"), listOf("Default on Cisco IOS", "Enabled", "Usually disabled ('lldp run' to enable)"),
        listOf("Timers (send / hold)", "60 s / 180 s", "30 s / 120 s"), listOf("Neighbours", "show cdp neighbors [detail]", "show lldp neighbors [detail]"),
        listOf("Disable per interface", "no cdp enable", "no lldp transmit / no lldp receive"), listOf("Security note", "Reveals platform, IOS and IP — disable on untrusted ports", "Same")))

    private val wlcGui = LessonTable("t-wlc-gui", "WLAN settings in the WLC GUI", listOf("Tab", "Key settings"), listOf(
        listOf("General", "Profile name, SSID, status, interface/VLAN the WLAN maps to"), listOf("Security → Layer 2", "WPA2/WPA3, PSK or 802.1X"),
        listOf("Security → AAA servers", "RADIUS servers for 802.1X"), listOf("QoS", "Platinum (voice), Gold (video), Silver (best effort), Bronze (background)"),
        listOf("Advanced", "Session timeout, client exclusion, FlexConnect local switching")))

    private val fhrp = LessonTable("t-fhrp", "First hop redundancy protocols", listOf("", "HSRP", "VRRP", "GLBP"), listOf(
        listOf("Standard", "Cisco", "Open (RFC 5798)", "Cisco"), listOf("Roles", "Active / standby", "Master / backup", "AVG + AVFs"),
        listOf("Load balancing", "Per group only", "Per group only", "Yes, across routers"), listOf("Default priority", "100", "100", "100"),
        listOf("Preemption default", "Off", "On", "Off"), listOf("Virtual MAC", "0000.0C07.ACxx (v1)", "0000.5E00.01xx", "0007.B400.xxyy"),
        listOf("Multicast", "224.0.0.2 (v1) / 224.0.0.102 (v2)", "224.0.0.18", "224.0.0.102")))

    private val qos = LessonTable("t-qos", "QoS toolkit", listOf("Tool", "What it does", "Example"), listOf(
        listOf("Classification", "Identifies traffic", "ACL, NBAR, incoming marking"), listOf("Marking", "Writes a priority value", "DSCP (6 bits, IP header), CoS (3 bits, 802.1Q)"),
        listOf("Queuing", "Decides send order during congestion", "LLQ for voice + CBWFQ"), listOf("Policing", "Drops or re-marks excess", "Provider edge"),
        listOf("Shaping", "Buffers excess to smooth to a rate", "Customer edge toward a slower WAN"), listOf("Congestion avoidance", "Drops early to slow TCP", "WRED")))
    private val dscp = LessonTable("t-dscp", "Common DSCP values", listOf("Name", "Decimal", "Typical traffic"), listOf(
        listOf("EF", "46", "Voice"), listOf("AF41", "34", "Interactive video"), listOf("AF31", "26", "Mission-critical data"), listOf("CS3", "24", "Call signalling"),
        listOf("AF21", "18", "Transactional data"), listOf("CS1", "8", "Scavenger / bulk"), listOf("Default (BE)", "0", "Best effort")),
        caption = "AFxy = 8x + 2y. Voice targets: one-way delay ≤150 ms, jitter ≤30 ms, loss ≤1%.")

    private val secTerms = LessonTable("t-sec-terms", "Security vocabulary", listOf("Term", "Meaning", "Example"), listOf(
        listOf("Vulnerability", "Weakness", "Unpatched IOS image"), listOf("Exploit", "Method that uses a vulnerability", "Malicious crafted packet"),
        listOf("Threat", "Potential for harm", "Attacker scanning the network"), listOf("Mitigation", "Reduces risk", "Patch, ACL, MFA"),
        listOf("DoS / DDoS", "Exhaust resources", "SYN flood from a botnet"), listOf("Spoofing", "Fake source identity", "Spoofed IP or MAC"),
        listOf("Man-in-the-middle", "Intercept traffic", "ARP poisoning"), listOf("Social engineering", "Trick people", "Phishing email")))

    private val vpn = LessonTable("t-vpn", "VPN types", listOf("", "Site-to-site", "Remote access"), listOf(
        listOf("Connects", "Network to network", "One user to a network"), listOf("Endpoints", "Routers / firewalls", "Client software (or browser for clientless TLS)"),
        listOf("Typical protocol", "IPsec", "TLS/SSL or IPsec"), listOf("Visible to users", "No — transparent", "Yes — the user connects"),
        listOf("IPsec services", "Confidentiality, integrity, authentication, anti-replay", "Same when IPsec is used")))

    private val aiTable = LessonTable("t-ai", "AI in network operations", listOf("Approach", "What it does", "Network example", "Caution"), listOf(
        listOf("Predictive AI / ML", "Learns patterns from data to forecast or detect anomalies", "Predict link saturation, flag unusual traffic", "Needs good telemetry"),
        listOf("Generative AI", "Creates new content from prompts", "Draft a configuration or summarise logs", "Output must be validated before deployment"),
        listOf("Supervised learning", "Trained on labelled examples", "Classify traffic types", ""), listOf("Unsupervised learning", "Finds structure in unlabelled data", "Cluster devices by behaviour", "")))

    private val ansibleTf = LessonTable("t-ansible-tf", "Ansible vs Terraform", listOf("", "Ansible", "Terraform"), listOf(
        listOf("Primary job", "Configuration management / automation", "Infrastructure as code (provisioning)"), listOf("Language", "YAML playbooks", "HCL"),
        listOf("Model", "Agentless, push over SSH/APIs", "Declarative; plan then apply"), listOf("State", "No state file", "State file tracks what exists"),
        listOf("Idempotent", "Yes (modules)", "Yes")))

    // ---------------------------------------------------------------- output activities
    private val outTrunkFault = OutputActivity("o-trunk", "Read the trunk table", "trunk-fault", emptyList(), "SW2", "show interfaces trunk",
        annotations = listOf("on" to "Mode 'on' = static trunk (switchport mode trunk).", "trunking" to "Status: the port is operating as a trunk.",
            "Vlans allowed on trunk" to "The configured allowed list on THIS end.", "10,99" to "VLAN 20 is missing on SW2's end — VLAN 20 frames are dropped here.",
            "allowed and active" to "Allowed VLANs that also exist on this switch."),
        question = "Based on this output, which VLAN cannot cross the SW1–SW2 trunk?", options = listOf("VLAN 10", "VLAN 20", "VLAN 99", "All VLANs cross"), answer = 1,
        explanation = "SW2 allows only 10 and 99. A VLAN must be allowed on both ends (and exist on both switches) to cross a trunk.")

    private val outInactive = OutputActivity("o-inactive", "Spot the inactive port", "vlan-missing-fault", emptyList(), "SW2", "show interfaces fa0/2 switchport",
        annotations = listOf("Administrative Mode: static access" to "Configured as an access port.", "(Inactive)" to "The port's VLAN no longer exists, so it forwards nothing.",
            "Negotiation of Trunking: Off" to "Static access disables DTP."),
        question = "Why does PC-D have no connectivity?", options = listOf("Fa0/2 is shut down", "Fa0/2 is a trunk", "VLAN 20 does not exist on SW2", "Duplex mismatch"), answer = 2,
        explanation = "'Access Mode VLAN: 20 (Inactive)' means the port is still assigned to VLAN 20 but VLAN 20 is not in SW2's VLAN database.")

    private val outRoute = OutputActivity("o-route", "Interpret a routing table", "static-routing",
        listOf("R2" to "conf t", "R2" to "int g0/1", "R2" to "no shut", "R2" to "int g0/0", "R2" to "ip address 192.168.2.1 255.255.255.0", "R2" to "no shut", "R2" to "end"),
        "R2", "show ip route",
        annotations = listOf("Gateway of last resort is not set" to "No default route on R2.", "C        10.0.12.0/30" to "Connected: the link to R1.",
            "L        10.0.12.2/32" to "Local: R2's own address on that link.", "192.168.2.0/24 is directly connected" to "R2's LAN."),
        question = "PC-1 (192.168.1.10) pings PC-2. What happens to the echo reply at R2?", options = listOf("It is routed to R1 via the default route", "It is dropped: R2 has no route to 192.168.1.0/24",
            "It is flooded out all interfaces", "It is sent to 10.0.12.2"), answer = 1,
        explanation = "R2 knows only connected networks. 192.168.1.10 matches nothing and there is no gateway of last resort, so the reply is dropped.")

    private val outRoas = OutputActivity("o-roas", "Subinterfaces in the routing table", "roas",
        listOf("SW1" to "conf t", "SW1" to "int g0/2", "SW1" to "sw mode trunk", "SW1" to "end",
            "R1" to "conf t", "R1" to "int g0/0", "R1" to "no shut", "R1" to "int g0/0.10", "R1" to "encapsulation dot1q 10", "R1" to "ip address 192.168.10.1 255.255.255.0",
            "R1" to "int g0/0.20", "R1" to "encapsulation dot1q 20", "R1" to "ip address 192.168.20.1 255.255.255.0", "R1" to "end"),
        "R1", "show ip route",
        annotations = listOf("GigabitEthernet0/0.10" to "Logical subinterface for VLAN 10.", "GigabitEthernet0/0.20" to "Logical subinterface for VLAN 20.",
            "variably subnetted" to "IOS groups subnets of the same classful network; /24 connected + /32 local = 2 masks."),
        question = "How many physical router interfaces carry the traffic for both VLANs?", options = listOf("One", "Two", "Four", "None — the switch routes"), answer = 0,
        explanation = "Both subinterfaces live on Gi0/0. Traffic between VLANs goes up and back down the same trunk.")

    private val outStartup = OutputActivity("o-startup", "running vs startup", "cli-basics", listOf("SW1" to "conf t", "SW1" to "hostname SW-LAB", "SW1" to "end"),
        "SW1", "show startup-config", annotations = listOf("startup-config is not present" to "Nothing has been saved to NVRAM yet."),
        question = "The prompt already shows SW-LAB#. What happens to the hostname after a reload right now?", options = listOf("It stays SW-LAB", "It reverts to Switch", "The switch will not boot", "It becomes SW-LAB1"), answer = 1,
        explanation = "The change is only in running-config. With no startup-config, the switch boots with defaults.")

    // ---------------------------------------------------------------- per-lesson mapping
    val all: Map<String, LessonExtras> = listOf(
        LessonExtras("1-03", tables = listOf(LessonTable("t-pdu", "Layers, PDUs and devices", listOf("TCP/IP layer", "OSI layers", "PDU", "Address", "Device"), listOf(
            listOf("Application", "7, 6, 5", "Data", "—", "—"), listOf("Transport", "4", "Segment (TCP) / datagram (UDP)", "Port", "Firewall (stateful)"),
            listOf("Internet", "3", "Packet", "IP address", "Router"), listOf("Link", "2", "Frame", "MAC address", "Switch"), listOf("(Physical)", "1", "Bits", "—", "Cable, hub")))),
            comparisons = listOf("osi-tcpip"),
            teachBack = TeachBack("Explain what changes in a packet as it crosses one router.", listOf("Layer 2 header rebuilt", "MAC addresses change", "IP addresses stay", "TTL decremented"),
                "The router removes the Ethernet header, keeps the IP packet (decrementing TTL), and builds a new Ethernet header with its own MAC as source and the next hop's MAC as destination. IP addresses and ports stay the same unless NAT is used.")),
        LessonExtras("1-04", tables = listOf(intfTrouble)),
        LessonExtras("1-05", tables = listOf(macIp)),
        LessonExtras("1-06", tables = listOf(ports), comparisons = listOf("tcp-udp")),
        LessonExtras("1-08", tables = listOf(subnetTable), comparisons = listOf("public-private")),
        LessonExtras("1-09", tables = listOf(subnetTable)),
        LessonExtras("1-10", tables = listOf(ipv6Types), comparisons = listOf("ipv4-ipv6")),
        LessonExtras("1-12", tables = listOf(wlanSec)),
        LessonExtras("1-14", tables = listOf(domains), comparisons = listOf("hub-switch", "switch-router"),
            teachBack = TeachBack("Explain how a switch decides whether to forward, filter or flood a frame.", listOf("learns source MAC", "looks up destination MAC", "floods unknown unicast and broadcast", "filters if destination is on the ingress port"),
                "It records the source MAC and ingress port, then looks up the destination MAC. Known: forward out that port only (or filter if it is the ingress port). Unknown unicast, broadcast or multicast: flood within the VLAN.")),
        LessonExtras("2-01", tables = listOf(accessTrunk, trunkTrouble), comparisons = listOf("access-trunk", "access-native"), walkthroughs = listOf(vlanWalk), outputs = listOf(outInactive),
            teachBack = TeachBack("Explain why PC-A in VLAN 10 cannot reach PC-B in VLAN 20 on the same switch.", listOf("separate broadcast domains", "different subnets", "needs a router or Layer 3 switch"),
                "Each VLAN is a separate broadcast domain and normally a separate IP subnet. Frames are never switched between VLANs, so traffic must be routed by a router or Layer 3 switch.")),
        LessonExtras("2-02", tables = listOf(dtp, trunkTrouble), walkthroughs = listOf(trunkWalk), outputs = listOf(outTrunkFault),
            teachBack = TeachBack("Explain the difference between 'switchport trunk allowed vlan 30' and 'switchport trunk allowed vlan add 30'.", listOf("first replaces the list", "add appends", "other VLANs stop crossing"),
                "Without 'add' the command replaces the entire allowed list with VLAN 30, so every other VLAN stops crossing the trunk. With 'add', VLAN 30 is appended to the existing list.")),
        LessonExtras("2-03", tables = listOf(LessonTable("t-intervlan", "Inter-VLAN routing options", listOf("Option", "How", "Pros", "Cons"), listOf(
            listOf("Router-on-a-stick", "One trunk + 802.1Q subinterfaces", "Cheap, one router port", "All inter-VLAN traffic shares one link"),
            listOf("Layer 3 switch (SVIs)", "interface vlan N + ip routing", "Wire-speed, no extra hop", "Needs a multilayer switch"),
            listOf("Separate router ports", "One physical port per VLAN", "Simple", "Does not scale")))), walkthroughs = listOf(roasWalk), outputs = listOf(outRoas)),
        LessonExtras("2-05", tables = listOf(etherModes), noLabReason = "EtherChannel behaviour (negotiation and hashing) is taught with the animation and negotiation table; the CLI trainer does not model port-channels."),
        LessonExtras("2-06", tables = listOf(stpStates), comparisons = listOf("stp-rstp"), noLabReason = "STP is taught with the step-through animation and role table; the trainer does not model BPDUs."),
        LessonExtras("2-07", tables = listOf(wlanModes)),
        LessonExtras("2-08", tables = listOf(wlcGui), noLabReason = "WLAN configuration is done in the WLC GUI, so this lesson uses GUI interpretation rather than CLI practice."),
        LessonExtras("2-09", tables = listOf(modesTable, runStart, sshTable), walkthroughs = listOf(cliWalk), outputs = listOf(outStartup)),
        LessonExtras("3-01", tables = listOf(routeFields, adTable), comparisons = listOf("static-dynamic"),
            teachBack = TeachBack("Explain how a router chooses between 10.0.0.0/8 learned by OSPF and 10.1.0.0/16 learned statically for 10.1.2.3.", listOf("longest prefix first", "/16 wins", "AD not compared for different prefixes"),
                "Longest-prefix match comes first: the /16 is more specific, so it is used regardless of AD. AD only matters when the same prefix is learned from different sources.")),
        LessonExtras("3-02", tables = listOf(staticTypes, staticTrouble), walkthroughs = listOf(staticWalk), outputs = listOf(outRoute)),
        LessonExtras("3-03", tables = listOf(ospfReq), comparisons = listOf("ospf-others"), noLabReason = "OSPF is taught with the adjacency animation and neighbor-requirements table; the trainer does not model dynamic routing."),
        LessonExtras("4-01", tables = listOf(dhcpMsgs), noLabReason = "DHCP is taught with the DORA and relay animations; the trainer does not model DHCP."),
        LessonExtras("4-02", tables = listOf(natTerms), comparisons = listOf("nat-pat"), noLabReason = "NAT/PAT is taught with the translation-table animation; the trainer does not model NAT."),
        LessonExtras("4-03", tables = listOf(mgmt, syslog)),
        LessonExtras("5-02", comparisons = listOf("aaa")),
        LessonExtras("5-03", tables = listOf(wildcard), comparisons = listOf("std-ext-acl"), noLabReason = "ACL logic is taught with the step-through ACL evaluator; the trainer does not model ACLs.",
            teachBack = TeachBack("Explain why 'deny ip 192.168.20.0 0.0.0.255 any' placed before 'permit icmp any any' blocks pings from 192.168.20.7.", listOf("top-down", "first match", "evaluation stops"),
                "ACLs are processed top-down and stop at the first match. The deny line matches all IP traffic from 192.168.20.0/24, including ICMP, so the later permit is never reached.")),
        LessonExtras("5-04", tables = listOf(l2sec), noLabReason = "Port security, DHCP snooping and DAI are covered with comparison tables; the trainer does not model them."),
        LessonExtras("6-01", comparisons = listOf("trad-sdn")),
        LessonExtras("6-03", tables = listOf(rest)),
        LessonExtras("1-01", tables = listOf(components)),
        LessonExtras("1-02", tables = listOf(topologies)),
        LessonExtras("1-07", tables = listOf(binary)),
        LessonExtras("1-11", tables = listOf(clientCmds)),
        LessonExtras("1-13", tables = listOf(virtualization)),
        LessonExtras("2-04", tables = listOf(cdpLldp), noLabReason = "CDP/LLDP neighbour discovery is not modelled in the trainer; use the comparison table and question bank."),
        LessonExtras("3-04", tables = listOf(fhrp)),
        LessonExtras("4-04", tables = listOf(qos, dscp)),
        LessonExtras("5-01", tables = listOf(secTerms)),
        LessonExtras("5-05", tables = listOf(vpn)),
        LessonExtras("5-06", tables = listOf(wlanSec)),
        LessonExtras("6-02", tables = listOf(aiTable)),
        LessonExtras("6-04", tables = listOf(ansibleTf)),
    ).associateBy { it.lessonId }

    fun forLesson(id: String) = all[id] ?: LessonExtras(id)

    val allOutputs get() = all.values.flatMap { it.outputs }
}
