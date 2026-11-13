package com.ccnaacademy.app.lab

/**
 * Lab catalog. Tasks are graded against the live LabState and computed network behaviour, never against the exact
 * commands typed, so every equivalent valid solution is accepted (abbreviations, different order, interface range, etc.).
 */
class LabTask(
    val id: String,
    val title: String,
    val detail: String,
    /** Progressive hints: 1 = concept, 2 = command family, 3 = partial syntax. */
    val hints: List<String>,
    val verify: String,
    val skill: String = "cli",               // cli | output | troubleshoot | concept
    val check: (LabState, Network) -> Boolean
)

class Lab(
    val id: String,
    val title: String,
    val kind: String,                        // guided | troubleshoot | challenge
    val lessonIds: List<String>,
    val minutes: Int,
    val scenario: String,
    val objectives: List<String>,
    val symptom: String? = null,             // troubleshooting labs: what the user reports
    val tasks: List<LabTask>,
    val whyItWorks: String,
    val verifyCommands: List<Pair<String, String>>,   // device id -> command, offered as one-tap buttons
    val pingTests: List<Pair<String, String>> = emptyList(),
    val initial: () -> LabState
) {
    val assessed get() = kind == "challenge"
    val label get() = when (kind) { "guided" -> "Guided practice"; "troubleshoot" -> "Troubleshooting"; else -> "Independent challenge" }
}

// ---------------------------------------------------------------- builders
private fun DeviceConfig.vlans(vararg v: Pair<Int, String>) = copy(vlans = vlans + v.toMap())
private fun DeviceConfig.port(name: String, f: (Intf) -> Intf) = withIntf(f(intf(name)!!))
private fun DeviceConfig.access(name: String, vlan: Int, desc: String = "") = port(name) { it.copy(mode = "access", accessVlan = vlan, description = desc) }
private fun DeviceConfig.trunk(name: String, native: Int = 1, allowed: List<Int>? = null, desc: String = "") =
    port(name) { it.copy(mode = "trunk", nativeVlan = native, allowed = allowed, description = desc) }
private fun DeviceConfig.ip(name: String, ip: String, mask: String, up: Boolean = true) = withIntf((intf(name) ?: Intf(name)).copy(ip = ip, mask = mask, shutdown = !up))
private fun DeviceConfig.sub(name: String, vlan: Int, ip: String, mask: String) = withIntf(Intf(name, dot1q = vlan, ip = ip, mask = mask))

private fun sw(id: String, x: Float, y: Float, cfg: DeviceConfig = Platforms.switch2960(id), saved: Boolean = false) =
    Device(id, "switch", cfg, if (saved) cfg else null, x = x, y = y)
private fun rt(id: String, x: Float, y: Float, cfg: DeviceConfig = Platforms.router(id), saved: Boolean = false) =
    Device(id, "router", cfg, if (saved) cfg else null, x = x, y = y)
private fun pc(id: String, ip: String, gw: String?, x: Float, y: Float, mask: String = "255.255.255.0") =
    Device(id, "host", DeviceConfig(id, emptyList()), hostIp = ip, hostMask = mask, hostGw = gw, x = x, y = y)
private fun link(a: String, aIf: String, b: String, bIf: String = "NIC") = Link(a, aIf, b, bIf)

// ---------------------------------------------------------------- check helpers
private fun LabState.cfg(id: String) = dev(id).running
private fun LabState.vlan(id: String, v: Int, name: String) = cfg(id).vlans[v]?.equals(name, ignoreCase = false) == true
private fun LabState.isAccess(id: String, port: String, v: Int) = cfg(id).intf(port)!!.let { it.mode == "access" && it.accessVlan == v }
private fun LabState.isTrunk(id: String, port: String) = cfg(id).intf(port)!!.mode == "trunk"
private fun LabState.allowedExactly(id: String, port: String, vs: Set<Int>) = cfg(id).intf(port)!!.allowed?.toSet() == vs
private fun LabState.saved(id: String) = dev(id).startup == dev(id).running
private fun LabState.did(id: String, cmd: String) = "$id:$cmd" in events
private fun Network.reach(a: String, b: String) = hostPing(a, b).ok

// ---------------------------------------------------------------- shared VLAN topology
// SW1 Fa0/1 PC-A (VLAN 10)  Fa0/2 PC-B (VLAN 20)  Gi0/1 <-> SW2 Gi0/1   SW2 Fa0/1 PC-C (VLAN 10)  Fa0/2 PC-D (VLAN 20)
private fun vlanTopology(sw1: DeviceConfig, sw2: DeviceConfig, saved: Boolean = false) = LabState(
    devices = listOf(
        sw("SW1", 330f, 140f, sw1, saved), sw("SW2", 670f, 140f, sw2, saved),
        pc("PC-A", "192.168.10.11", "192.168.10.1", 200f, 420f), pc("PC-B", "192.168.20.11", "192.168.20.1", 420f, 420f),
        pc("PC-C", "192.168.10.12", "192.168.10.1", 580f, 420f), pc("PC-D", "192.168.20.12", "192.168.20.1", 800f, 420f)
    ),
    links = listOf(
        link("SW1", "GigabitEthernet0/1", "SW2", "GigabitEthernet0/1"),
        link("SW1", "FastEthernet0/1", "PC-A"), link("SW1", "FastEthernet0/2", "PC-B"),
        link("SW2", "FastEthernet0/1", "PC-C"), link("SW2", "FastEthernet0/2", "PC-D")
    )
)

private fun vlanSolved(h: String): DeviceConfig = Platforms.switch2960(h)
    .vlans(10 to "SALES", 20 to "ENG", 99 to "NATIVE")
    .access("FastEthernet0/1", 10, "PC").access("FastEthernet0/2", 20, "PC")
    .trunk("GigabitEthernet0/1", 99, listOf(10, 20, 99), "Trunk to ${if (h == "SW1") "SW2" else "SW1"}")

object LabCatalog {

    val cliBasics = Lab(
        id = "cli-basics", title = "CLI fundamentals: modes, help, and saving", kind = "guided", lessonIds = listOf("2-09", "2-01"), minutes = 15,
        scenario = "A new access switch has just been unboxed. Learn the IOS modes, use context help, give the switch a name, label a port, and prove that saving the configuration matters.",
        objectives = listOf("Move between user EXEC, privileged EXEC and configuration modes", "Use ? and abbreviations",
            "Explain running-config vs startup-config", "Save and survive a reload"),
        tasks = listOf(
            LabTask("enable", "Enter privileged EXEC mode", "The prompt changes from > to #.",
                listOf("Most show and all configuration commands need privileged EXEC.", "The command is a single word that 'enables' privileged commands.", "en____"),
                "Look at the prompt: Switch#") { s, _ -> s.did("SW1", "enable") },
            LabTask("help", "Use context-sensitive help", "Type ? on its own, or after a partial command such as 'show ?'.",
                listOf("IOS lists the valid next words when you type a question mark.", "Try '?' or 'show ?'.", "show ?"), "Help output lists keywords and descriptions.") { s, _ -> s.did("SW1", "help") },
            LabTask("showrun", "Display the running configuration", "Running-config is the live configuration held in RAM.",
                listOf("The active configuration is called the running configuration.", "Use a show command; 'sh run' is the common abbreviation.", "show running-____"),
                "Output begins with 'Building configuration...'", skill = "output") { s, _ -> s.did("SW1", "shrun") },
            LabTask("hostname", "Rename the switch to SW-LAB", "Hostnames are set in global configuration mode.",
                listOf("Configuration changes start in global configuration mode.", "'configure terminal', then the 'hostname' command.", "hostname SW-____"),
                "The prompt changes to SW-LAB(config)#") { s, _ -> s.cfg("SW1").hostname == "SW-LAB" },
            LabTask("desc", "Describe Fa0/1 as PC-A", "Descriptions document what is connected to a port.",
                listOf("Interface settings are entered in interface configuration mode.", "'interface fa0/1', then 'description ...'.", "description PC-_"),
                "show interfaces status → Name column") { s, _ -> s.cfg("SW1").intf("FastEthernet0/1")!!.description.trim().equals("PC-A", true) },
            LabTask("startup", "Compare with the startup configuration", "Startup-config is stored in NVRAM and loaded at boot.",
                listOf("Return to privileged EXEC (end or Ctrl+Z), or use 'do' from config mode.", "Use 'show startup-config'.", "do show start____"),
                "On a new switch: 'startup-config is not present'", skill = "output") { s, _ -> s.did("SW1", "shstart") },
            LabTask("save", "Save the configuration", "Copy running-config to startup-config.",
                listOf("Unsaved changes are lost on reload.", "Use the copy command from privileged EXEC ('write memory' also works).", "copy running-config startup-____"),
                "show startup-config now shows hostname SW-LAB") { s, _ -> s.saved("SW1") && s.cfg("SW1").hostname == "SW-LAB" },
            LabTask("reload", "Reload and confirm the name survived", "Restart the switch; the saved hostname must still be there.",
                listOf("A reload rebuilds running-config from startup-config.", "The command is 'reload' in privileged EXEC.", "reload"),
                "After reload the prompt shows SW-LAB>") { s, _ -> s.did("SW1", "reload") && s.cfg("SW1").hostname == "SW-LAB" },
        ),
        whyItWorks = "Configuration commands change running-config immediately. Only 'copy running-config startup-config' (or 'write memory') writes it to NVRAM, so only saved changes survive a reload. VLANs 2-1001 are different: on a VTP server/client switch they are stored in vlan.dat as soon as you create them.",
        verifyCommands = listOf("SW1" to "show running-config", "SW1" to "show startup-config", "SW1" to "show interfaces status"),
        initial = { LabState(listOf(sw("SW1", 500f, 160f, Platforms.switch2960("Switch")), pc("PC-A", "192.168.1.10", null, 500f, 420f)),
            listOf(link("SW1", "FastEthernet0/1", "PC-A"))) }
    )

    val vlanGuided = Lab(
        id = "vlan-trunk", title = "VLANs and an 802.1Q trunk (two switches)", kind = "guided", lessonIds = listOf("2-01", "2-02"), minutes = 25,
        scenario = "Sales (VLAN 10) and Engineering (VLAN 20) have users on both floors. SW1 and SW2 are connected by Gi0/1. Build the VLANs, assign the access ports, and trunk the uplink with native VLAN 99 and only the VLANs that are needed.",
        objectives = listOf("Create and name VLANs", "Assign static access ports", "Configure a static trunk, native VLAN and allowed list", "Verify with show vlan brief and show interfaces trunk"),
        tasks = listOf(
            LabTask("v-sw1", "Create VLAN 10 SALES and VLAN 20 ENG on SW1", "Use exactly these names.",
                listOf("Each VLAN is created in the VLAN database and can be named.", "In global config: 'vlan <id>' then 'name <text>'.", "vlan 10 → name SALES"),
                "SW1# show vlan brief") { s, _ -> s.vlan("SW1", 10, "SALES") && s.vlan("SW1", 20, "ENG") },
            LabTask("v-sw2", "Create the same VLANs on SW2", "VLANs are local to each switch (VTP is not used here).",
                listOf("SW2 has its own VLAN database.", "Select SW2 in the device picker and repeat the vlan/name commands.", "vlan 20 → name ENG"),
                "SW2# show vlan brief") { s, _ -> s.vlan("SW2", 10, "SALES") && s.vlan("SW2", 20, "ENG") },
            LabTask("access", "Fa0/1 → VLAN 10 and Fa0/2 → VLAN 20 on both switches, as static access ports", "Hard-code access mode so DTP cannot form a trunk.",
                listOf("A port connected to a PC should be a static access port in one VLAN.", "'switchport mode access' and 'switchport access vlan <id>'.", "interface fa0/1 → switchport mode access → switchport access vlan 10"),
                "show vlan brief: Fa0/1 under VLAN 10, Fa0/2 under VLAN 20") { s, _ ->
                listOf("SW1", "SW2").all { s.isAccess(it, "FastEthernet0/1", 10) && s.isAccess(it, "FastEthernet0/2", 20) } },
            LabTask("trunk", "Make Gi0/1 a static trunk on both switches", "Do not rely on DTP negotiation.",
                listOf("A trunk carries several VLANs on one link using 802.1Q tags.", "'switchport mode trunk' on the interface (this platform only supports 802.1Q).", "interface g0/1 → switchport mode trunk"),
                "show interfaces trunk → Gi0/1 Mode 'on', Status 'trunking'") { s, n ->
                listOf("SW1", "SW2").all { s.isTrunk(it, "GigabitEthernet0/1") && n.operMode(it, "GigabitEthernet0/1") == "trunk" } },
            LabTask("native", "Create VLAN 99 NATIVE on both switches and use it as the native VLAN on both trunk ends", "Native VLANs must match on both ends.",
                listOf("Untagged frames on a trunk belong to the native VLAN; best practice is an unused VLAN.", "'switchport trunk native vlan <id>' on Gi0/1.", "switchport trunk native vlan 99"),
                "show interfaces trunk → Native vlan 99 on both switches") { s, _ ->
                listOf("SW1", "SW2").all { s.vlan(it, 99, "NATIVE") && s.cfg(it).intf("GigabitEthernet0/1")!!.nativeVlan == 99 } },
            LabTask("allowed", "Allow only VLANs 10, 20 and 99 on both trunk ends", "Limit the trunk to the VLANs that are needed.",
                listOf("By default a trunk allows VLANs 1-4094.", "'switchport trunk allowed vlan <list>' replaces the list; '... add <list>' appends.", "switchport trunk allowed vlan 10,20,99"),
                "show interfaces trunk → Vlans allowed on trunk: 10,20,99") { s, _ ->
                listOf("SW1", "SW2").all { s.allowedExactly(it, "GigabitEthernet0/1", setOf(10, 20, 99)) } },
            LabTask("verify", "Prove it: PC-A ↔ PC-C and PC-B ↔ PC-D work; PC-A ↔ PC-B does not", "Use the Topology tab's reachability tests.",
                listOf("Same VLAN across the trunk = same broadcast domain. Different VLANs need a router.", "Run the PC-A → PC-C and PC-B → PC-D tests in the Topology tab.", "If a test fails, read the trace: it names the device and port that dropped the frame."),
                "Topology tab → Test", skill = "output") { s, n ->
                    listOf("SW1", "SW2").all { s.isAccess(it, "FastEthernet0/1", 10) && s.isAccess(it, "FastEthernet0/2", 20) && n.operMode(it, "GigabitEthernet0/1") == "trunk" } &&
                    n.reach("PC-A", "PC-C") && n.reach("PC-B", "PC-D") && !n.reach("PC-A", "PC-B") },
            LabTask("save", "Save the configuration on both switches", "Interface settings are in running-config until saved.",
                listOf("Running-config is lost on reload unless copied to NVRAM.", "Use 'copy running-config startup-config' on each switch.", "copy run start"),
                "show startup-config") { s, _ -> s.saved("SW1") && s.saved("SW2") },
        ),
        whyItWorks = "Each VLAN is a separate broadcast domain. Access ports carry one VLAN untagged; the trunk tags VLAN 10 and 20 frames with 802.1Q so the other switch can place them in the right VLAN, while native VLAN 99 frames cross untagged. PC-A and PC-B are in different VLANs and subnets, so they cannot communicate without a router.",
        verifyCommands = listOf("SW1" to "show vlan brief", "SW1" to "show interfaces trunk", "SW2" to "show interfaces trunk", "SW1" to "show interfaces fa0/1 switchport"),
        pingTests = listOf("PC-A" to "PC-C", "PC-B" to "PC-D", "PC-A" to "PC-B"),
        initial = { vlanTopology(Platforms.switch2960("SW1"), Platforms.switch2960("SW2")) }
    )

    val trunkFault = Lab(
        id = "trunk-fault", title = "Troubleshoot: Engineering lost connectivity after a change", kind = "troubleshoot", lessonIds = listOf("2-02"), minutes = 15,
        scenario = "Same network as the VLAN lab. Last night an engineer 'added' VLAN 99 to the SW2 uplink. This morning Engineering users on different floors cannot reach each other. Sales is fine.",
        objectives = listOf("Collect evidence before changing anything", "Interpret 'show interfaces trunk'", "Apply the smallest correct fix", "Verify the fix and confirm nothing else broke"),
        symptom = "PC-B (SW1) cannot reach PC-D (SW2). PC-A ↔ PC-C still works.",
        tasks = listOf(
            LabTask("evidence", "Collect evidence on both switches", "Compare the trunk on SW1 and SW2 before changing anything.",
                listOf("Start with the link that both VLANs share.", "Use 'show interfaces trunk' on SW1 and on SW2.", "show interfaces trunk"),
                "Compare 'Vlans allowed on trunk' on both ends", skill = "troubleshoot") { s, _ -> s.did("SW1", "shinttrunk") && s.did("SW2", "shinttrunk") },
            LabTask("fix", "Restore VLAN 20 on the trunk without allowing extra VLANs", "The allowed list must end up as 10,20,99 on SW2.",
                listOf("One end allows fewer VLANs than the other.", "Edit the allowed list on SW2 Gi0/1. Remember the difference between replacing and adding.", "switchport trunk allowed vlan add 20"),
                "SW2# show interfaces trunk", skill = "troubleshoot") { s, _ -> s.allowedExactly("SW2", "GigabitEthernet0/1", setOf(10, 20, 99)) && s.allowedExactly("SW1", "GigabitEthernet0/1", setOf(10, 20, 99)) },
            LabTask("verify", "Verify PC-B ↔ PC-D and confirm PC-A ↔ PC-C still works", "A fix is not finished until it is verified.",
                listOf("Re-run the same tests that showed the failure.", "Topology tab → run both reachability tests.", "Both tests should show the trunk carrying the VLAN."),
                "Topology tab → Test", skill = "output") { _, n -> n.reach("PC-B", "PC-D") && n.reach("PC-A", "PC-C") },
            LabTask("save", "Save SW2", "Make the fix permanent.", listOf("Unsaved fixes disappear after a reload.", "copy running-config startup-config on SW2.", "copy run start"),
                "SW2# show startup-config") { s, _ -> s.saved("SW2") },
        ),
        whyItWorks = "The engineer typed 'switchport trunk allowed vlan 10,99' instead of 'switchport trunk allowed vlan add 99'. Without 'add', the command replaces the whole list, so VLAN 20 was removed from SW2's end of the trunk and VLAN 20 frames were dropped there. Adding VLAN 20 back restores the list to 10,20,99 on both ends.",
        verifyCommands = listOf("SW1" to "show interfaces trunk", "SW2" to "show interfaces trunk", "SW2" to "show running-config"),
        pingTests = listOf("PC-B" to "PC-D", "PC-A" to "PC-C"),
        initial = { vlanTopology(vlanSolved("SW1"), vlanSolved("SW2").trunk("GigabitEthernet0/1", 99, listOf(10, 99), "Trunk to SW1"), saved = true) }
    )

    val vlanMissingFault = Lab(
        id = "vlan-missing-fault", title = "Troubleshoot: a port that is in no VLAN", kind = "troubleshoot", lessonIds = listOf("2-01"), minutes = 12,
        scenario = "During a clean-up someone deleted 'unused' VLANs on SW2. PC-D now has no connectivity, although its port is up and the cable is fine.",
        objectives = listOf("Recognise an inactive access VLAN", "Use show vlan brief and show interfaces switchport", "Fix without touching the port configuration"),
        symptom = "PC-D cannot reach PC-B. The SW2 Fa0/2 link light is on.",
        tasks = listOf(
            LabTask("evidence", "Find where Fa0/2 is on SW2", "Look for the port in the VLAN table, then check its switchport details.",
                listOf("A port must belong to an existing VLAN to forward frames.", "'show vlan brief' and 'show interfaces fa0/2 switchport' on SW2.", "show interfaces fa0/2 switchport"),
                "Access Mode VLAN: 20 (Inactive)", skill = "troubleshoot") { s, _ -> s.did("SW2", "shvlanbr") && s.did("SW2", "shintsw") },
            LabTask("fix", "Recreate VLAN 20 named ENG on SW2", "The port keeps its VLAN assignment; the VLAN itself is missing.",
                listOf("Deleting a VLAN does not move its ports; they become inactive.", "Create the VLAN again in global configuration.", "vlan 20 → name ENG"),
                "SW2# show vlan brief → Fa0/2 under VLAN 20", skill = "troubleshoot") { s, _ -> s.vlan("SW2", 20, "ENG") && s.isAccess("SW2", "FastEthernet0/2", 20) },
            LabTask("verify", "Verify PC-B ↔ PC-D", "Run the reachability test.", listOf("Re-test the failing path.", "Topology tab → PC-B → PC-D.", "The trace should show VLAN 20 crossing the trunk."),
                "Topology tab → Test", skill = "output") { _, n -> n.reach("PC-B", "PC-D") },
        ),
        whyItWorks = "On Catalyst switches, deleting a VLAN leaves its access ports assigned to that VLAN ID, but the ports stop forwarding because the VLAN no longer exists (shown as 'Inactive'). They also disappear from 'show vlan brief'. Recreating VLAN 20 reactivates Fa0/2 without any interface changes.",
        verifyCommands = listOf("SW2" to "show vlan brief", "SW2" to "show interfaces fa0/2 switchport", "SW2" to "show interfaces trunk"),
        pingTests = listOf("PC-B" to "PC-D"),
        initial = { vlanTopology(vlanSolved("SW1"), vlanSolved("SW2").let { it.copy(vlans = it.vlans - 20) }, saved = true) }
    )

    val vlanChallenge = Lab(
        id = "vlan-challenge", title = "Challenge: HR and Operations VLANs", kind = "challenge", lessonIds = listOf("2-01", "2-02"), minutes = 20,
        scenario = "New requirements, no hints. HR uses Fa0/5 and Operations uses Fa0/6 on both switches. The switches are linked on Gi0/2 this time.",
        objectives = listOf(
            "VLAN 30 named HR and VLAN 40 named OPS on both switches",
            "Fa0/5 static access VLAN 30, Fa0/6 static access VLAN 40 on both switches",
            "Gi0/2 static trunk on both switches, native VLAN 98 (named NATIVE), allowed VLANs 30,40,98 only",
            "PC-E ↔ PC-G (HR) and PC-F ↔ PC-H (OPS) reachable; HR cannot reach OPS",
            "Both switches saved"),
        tasks = listOf(
            LabTask("vlans", "VLANs 30 HR, 40 OPS and 98 NATIVE exist on both switches", "", emptyList(), "show vlan brief") { s, _ ->
                listOf("SW1", "SW2").all { s.vlan(it, 30, "HR") && s.vlan(it, 40, "OPS") && s.vlan(it, 98, "NATIVE") } },
            LabTask("access", "Access ports assigned", "", emptyList(), "show vlan brief") { s, _ ->
                listOf("SW1", "SW2").all { s.isAccess(it, "FastEthernet0/5", 30) && s.isAccess(it, "FastEthernet0/6", 40) } },
            LabTask("trunk", "Trunk configured to specification", "", emptyList(), "show interfaces trunk") { s, n ->
                listOf("SW1", "SW2").all { s.isTrunk(it, "GigabitEthernet0/2") && n.operMode(it, "GigabitEthernet0/2") == "trunk" &&
                    s.cfg(it).intf("GigabitEthernet0/2")!!.nativeVlan == 98 && s.allowedExactly(it, "GigabitEthernet0/2", setOf(30, 40, 98)) } },
            LabTask("reach", "Reachability matches the design", "", emptyList(), "Topology tab", skill = "output") { _, n ->
                n.reach("PC-E", "PC-G") && n.reach("PC-F", "PC-H") && !n.reach("PC-E", "PC-F") },
            LabTask("save", "Both switches saved", "", emptyList(), "show startup-config") { s, _ -> s.saved("SW1") && s.saved("SW2") },
        ),
        whyItWorks = "Same principles, different values: VLANs must exist on every switch that carries them, access ports carry one untagged VLAN, and both trunk ends must agree on the native VLAN and allow every VLAN that has users on both sides.",
        verifyCommands = listOf("SW1" to "show vlan brief", "SW1" to "show interfaces trunk", "SW2" to "show interfaces trunk"),
        pingTests = listOf("PC-E" to "PC-G", "PC-F" to "PC-H", "PC-E" to "PC-F"),
        initial = { LabState(
            devices = listOf(sw("SW1", 330f, 140f), sw("SW2", 670f, 140f),
                pc("PC-E", "172.16.30.21", "172.16.30.1", 200f, 420f), pc("PC-F", "172.16.40.21", "172.16.40.1", 420f, 420f),
                pc("PC-G", "172.16.30.22", "172.16.30.1", 580f, 420f), pc("PC-H", "172.16.40.22", "172.16.40.1", 800f, 420f)),
            links = listOf(link("SW1", "GigabitEthernet0/2", "SW2", "GigabitEthernet0/2"),
                link("SW1", "FastEthernet0/5", "PC-E"), link("SW1", "FastEthernet0/6", "PC-F"),
                link("SW2", "FastEthernet0/5", "PC-G"), link("SW2", "FastEthernet0/6", "PC-H"))) }
    )

    val roas = Lab(
        id = "roas", title = "Inter-VLAN routing with router-on-a-stick", kind = "guided", lessonIds = listOf("2-03"), minutes = 20,
        scenario = "SW1 already has PC-A in VLAN 10 (192.168.10.0/24) and PC-B in VLAN 20 (192.168.20.0/24). R1 is connected to SW1 Gi0/2. Make R1 the default gateway (.1) for both VLANs using 802.1Q subinterfaces.",
        objectives = listOf("Trunk the switch port facing the router", "Create one subinterface per VLAN with encapsulation dot1Q", "Enable the physical interface", "Verify routing between VLANs"),
        tasks = listOf(
            LabTask("swtrunk", "Make SW1 Gi0/2 a static trunk", "Routers do not negotiate DTP, so the switch side must be 'trunk'.",
                listOf("The router link must carry both VLANs.", "'switchport mode trunk' on SW1 Gi0/2.", "interface g0/2 → switchport mode trunk"),
                "SW1# show interfaces trunk") { s, n -> n.operMode("SW1", "GigabitEthernet0/2") == "trunk" },
            LabTask("phys", "Enable R1 Gi0/0", "Router interfaces are shut down by default. Subinterfaces depend on the physical interface.",
                listOf("A subinterface can only be up if its parent interface is up.", "'no shutdown' on interface Gi0/0.", "interface g0/0 → no shutdown"),
                "R1# show ip interface brief") { s, _ -> !s.cfg("R1").intf("GigabitEthernet0/0")!!.shutdown },
            LabTask("sub10", "Gi0/0.10: VLAN 10, 192.168.10.1/24", "Encapsulation must be configured before the IP address.",
                listOf("Each VLAN needs its own logical router interface.", "'interface g0/0.10', 'encapsulation dot1Q 10', 'ip address ...'.", "encapsulation dot1Q 10 → ip address 192.168.10.1 255.255.255.0"),
                "R1# show ip interface brief") { s, _ -> s.cfg("R1").interfaces.any { it.dot1q == 10 && !it.dot1qNative && it.ip == "192.168.10.1" && it.mask == "255.255.255.0" && IfName.parent(it.name) == "GigabitEthernet0/0" } },
            LabTask("sub20", "Gi0/0.20: VLAN 20, 192.168.20.1/24", "Same pattern for VLAN 20.",
                listOf("The subinterface number does not have to match the VLAN, but matching is best practice.", "'interface g0/0.20', 'encapsulation dot1Q 20', 'ip address ...'.", "ip address 192.168.20.1 255.255.255.0"),
                "R1# show ip route") { s, _ -> s.cfg("R1").interfaces.any { it.dot1q == 20 && !it.dot1qNative && it.ip == "192.168.20.1" && it.mask == "255.255.255.0" && IfName.parent(it.name) == "GigabitEthernet0/0" } },
            LabTask("verify", "Verify PC-A ↔ PC-B through R1", "Inspect the trace: traffic goes up the trunk to R1 and back.",
                listOf("Hosts send off-subnet traffic to their default gateway.", "Topology tab → PC-A → PC-B; also check 'show ip route' on R1.", "Two connected routes (C) should appear on R1."),
                "Topology tab → Test", skill = "output") { _, n -> n.reach("PC-A", "PC-B") },
            LabTask("save", "Save R1 and SW1", "", listOf("Save both devices you changed.", "copy running-config startup-config", "copy run start"), "show startup-config") { s, _ -> s.saved("R1") && s.saved("SW1") },
        ),
        whyItWorks = "SW1 tags frames for VLAN 10 and 20 on the trunk to R1. R1 places each tagged frame on the matching subinterface (encapsulation dot1Q), routes between the two connected subnets, and sends the packet back down the same trunk tagged for the destination VLAN. The Layer 2 header is rebuilt with new source/destination MACs and a new VLAN tag; the IP addresses do not change.",
        verifyCommands = listOf("R1" to "show ip interface brief", "R1" to "show ip route", "SW1" to "show interfaces trunk", "SW1" to "show vlan brief"),
        pingTests = listOf("PC-A" to "PC-B"),
        initial = { LabState(
            devices = listOf(
                sw("SW1", 500f, 260f, Platforms.switch2960("SW1").vlans(10 to "SALES", 20 to "ENG").access("FastEthernet0/1", 10, "PC-A").access("FastEthernet0/2", 20, "PC-B"), saved = true),
                rt("R1", 500f, 70f),
                pc("PC-A", "192.168.10.11", "192.168.10.1", 300f, 460f), pc("PC-B", "192.168.20.11", "192.168.20.1", 700f, 460f)),
            links = listOf(link("SW1", "GigabitEthernet0/2", "R1", "GigabitEthernet0/0"), link("SW1", "FastEthernet0/1", "PC-A"), link("SW1", "FastEthernet0/2", "PC-B"))) }
    )

    val staticRouting = Lab(
        id = "static-routing", title = "Static and default routes between two sites", kind = "guided", lessonIds = listOf("3-02", "3-01"), minutes = 20,
        scenario = "R1 (LAN 192.168.1.0/24) and R2 (LAN 192.168.2.0/24) share the point-to-point link 10.0.12.0/30. R1 is finished except for routing. Bring R2 up, then give both routers a way to reach the remote LAN.",
        objectives = listOf("Enable and address router interfaces", "Add a static route with a next hop", "Add a default route", "Understand why a return route is required"),
        tasks = listOf(
            LabTask("r2wan", "Enable R2 Gi0/1 (10.0.12.2/30 is already configured)", "",
                listOf("Router interfaces are shut down by default.", "'no shutdown' on R2 Gi0/1.", "interface g0/1 → no shutdown"),
                "R2# show ip interface brief → up/up") { s, n -> n.intfUp("R2", "GigabitEthernet0/1") },
            LabTask("r2lan", "Address R2 Gi0/0 as 192.168.2.1/24 and enable it", "",
                listOf("The LAN gateway is the router's LAN interface.", "'ip address' and 'no shutdown' on Gi0/0.", "ip address 192.168.2.1 255.255.255.0"),
                "R2# show ip route → C 192.168.2.0/24") { s, n -> s.cfg("R2").intf("GigabitEthernet0/0")!!.ip == "192.168.2.1" && n.intfUp("R2", "GigabitEthernet0/0") },
            LabTask("r1route", "On R1, add a static route to 192.168.2.0/24 via 10.0.12.2", "Then try PC-1 → PC-2 and read why it still fails.",
                listOf("A router only knows connected networks until you add routes.", "'ip route <network> <mask> <next-hop>' in global config.", "ip route 192.168.2.0 255.255.255.0 10.0.12.2"),
                "R1# show ip route → S 192.168.2.0/24 [1/0] via 10.0.12.2") { _, n -> n.routes("R1").any { it.code == "S" && Ip.str(it.prefix) == "192.168.2.0" && it.len == 24 } },
            LabTask("r2default", "On R2, add a default route to R1 (10.0.12.1)", "R2 needs a path back to 192.168.1.0/24. A default route covers it.",
                listOf("Packets need a route in both directions.", "A default route matches 0.0.0.0/0, the shortest possible prefix.", "ip route 0.0.0.0 0.0.0.0 10.0.12.1"),
                "R2# show ip route → Gateway of last resort is 10.0.12.1") { _, n -> n.routes("R2").any { it.code == "S" && it.len == 0 } },
            LabTask("verify", "Verify PC-1 ↔ PC-2", "", listOf("Run the test and read the hop-by-hop trace.", "Topology tab → PC-1 → PC-2 (or 'ping 192.168.2.10' on R1).", "Every hop should show a longest-prefix match."),
                "Topology tab → Test", skill = "output") { _, n -> n.reach("PC-1", "PC-2") },
            LabTask("save", "Save R1 and R2", "", listOf("Save both routers.", "copy running-config startup-config", "copy run start"), "show startup-config") { s, _ -> s.saved("R1") && s.saved("R2") },
        ),
        whyItWorks = "R1 forwards 192.168.2.0/24 traffic to 10.0.12.2 because of the static route. The echo reply from PC-2 is addressed to 192.168.1.10; R2 has no specific route for it, so it uses its default route (0.0.0.0/0) to R1. Without the return route the request arrives but the reply is dropped — a classic exam scenario. Notice that 'ping 192.168.2.10' from R1 itself already worked before R2 had a default route: R1 sources that ping from 10.0.12.1, a network R2 knows as directly connected. Always test from the end hosts' subnets.",
        verifyCommands = listOf("R1" to "show ip route", "R2" to "show ip route", "R2" to "show ip interface brief", "R1" to "ping 192.168.2.10"),
        pingTests = listOf("PC-1" to "PC-2"),
        initial = { LabState(
            devices = listOf(
                rt("R1", 300f, 120f, Platforms.router("R1").ip("GigabitEthernet0/0", "192.168.1.1", "255.255.255.0").ip("GigabitEthernet0/1", "10.0.12.1", "255.255.255.252"), saved = true),
                rt("R2", 700f, 120f, Platforms.router("R2").ip("GigabitEthernet0/1", "10.0.12.2", "255.255.255.252", up = false)),
                pc("PC-1", "192.168.1.10", "192.168.1.1", 300f, 420f), pc("PC-2", "192.168.2.10", "192.168.2.1", 700f, 420f)),
            links = listOf(link("R1", "GigabitEthernet0/1", "R2", "GigabitEthernet0/1"), link("R1", "GigabitEthernet0/0", "PC-1"), link("R2", "GigabitEthernet0/0", "PC-2"))) }
    )

    val all: List<Lab> = listOf(cliBasics, vlanGuided, trunkFault, vlanMissingFault, vlanChallenge, roas, staticRouting)
    val byId = all.associateBy { it.id }
    fun forLesson(lessonId: String) = all.filter { lessonId in it.lessonIds }
}
