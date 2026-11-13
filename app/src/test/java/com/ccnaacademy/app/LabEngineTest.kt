package com.ccnaacademy.app

import com.ccnaacademy.app.data.AppJson
import com.ccnaacademy.app.lab.*
import org.junit.Assert.*
import org.junit.Test

class LabEngineTest {

    private fun engine(lab: Lab) = CliEngine(lab.initial())
    private fun CliEngine.script(dev: String, vararg lines: String): List<OutLine> = lines.flatMap { run(dev, it) }
    private fun List<OutLine>.errors() = filter { it.kind == "err" }
    private fun List<OutLine>.text() = joinToString("\n") { it.text }
    private fun CliEngine.done(lab: Lab) = lab.tasks.map { it.id to it.check(state, Network(state)) }.filter { !it.second }.map { it.first }

    private val vlanSwitchScript = arrayOf(
        "en", "conf t", "vlan 10", "name SALES", "vlan 20", "name ENG", "vlan 99", "name NATIVE",
        "int fa0/1", "switchport mode access", "switchport access vlan 10",
        "int fa0/2", "sw mo acc", "sw acc vl 20",
        "interface gi0/1", "switchport mode trunk", "switchport trunk native vlan 99", "switchport trunk allowed vlan 10,20,99",
        "end", "copy running-config startup-config")

    // ---------------------------------------------------------------- modes, abbreviations, help, errors
    @Test fun modesPromptsAndAbbreviations() {
        val e = engine(LabCatalog.cliBasics)
        assertEquals("Switch>", e.prompt("SW1"))
        e.run("SW1", "en"); assertEquals("Switch#", e.prompt("SW1"))
        e.run("SW1", "conf t"); assertEquals("Switch(config)#", e.prompt("SW1"))
        e.run("SW1", "int fa0/1"); assertEquals("Switch(config-if)#", e.prompt("SW1"))
        e.run("SW1", "hostname SW-LAB")   // global command from interface mode falls back to global config, as on IOS
        assertEquals("SW-LAB(config)#", e.prompt("SW1"))
        e.run("SW1", "vlan 10"); assertEquals("SW-LAB(config-vlan)#", e.prompt("SW1"))
        e.run("SW1", "exit"); assertEquals("SW-LAB(config)#", e.prompt("SW1"))
        e.run("SW1", "end"); assertEquals("SW-LAB#", e.prompt("SW1"))
        e.run("SW1", "disable"); assertEquals("SW-LAB>", e.prompt("SW1"))
    }

    @Test fun invalidCommandsNeverSilentlySucceed() {
        val e = engine(LabCatalog.cliBasics)
        val o1 = e.run("SW1", "configure terminal")
        assertTrue(o1.errors().any { it.text.contains("Invalid input") })
        assertTrue("explains the mode problem", o1.any { it.kind == "note" && it.text.contains("enable") })
        assertEquals("Switch>", e.prompt("SW1"))
        e.run("SW1", "enable")
        val o2 = e.run("SW1", "show runing-config")
        assertTrue(o2.errors().any { it.text.trim() == "^" || it.text.endsWith("^") })
        assertTrue(e.run("SW1", "show").errors().any { it.text.contains("Incomplete") })
        assertTrue(e.run("SW1", "c").errors().any { it.text.contains("Ambiguous") })
        e.run("SW1", "conf t"); e.run("SW1", "int fa0/1")
        val o3 = e.run("SW1", "switchport access vlan 5000")
        assertTrue(o3.errors().isNotEmpty())
        assertEquals(1, e.state.dev("SW1").running.intf("FastEthernet0/1")!!.accessVlan)
        val o4 = e.run("SW1", "ip address 10.0.0.1 255.255.255.0")
        assertTrue("L2 ports cannot take IP addresses", o4.errors().isNotEmpty())
        assertNull(e.state.dev("SW1").running.intf("FastEthernet0/1")!!.ip)
        val o5 = e.run("SW1", "show vlan brief")
        assertTrue(o5.any { it.kind == "note" && it.text.contains("do ") })
    }

    @Test fun unsupportedCommandsAreHonestAndChangeNothing() {
        val e = engine(LabCatalog.cliBasics)
        e.script("SW1", "en", "conf t")
        val before = e.state.dev("SW1").running
        val o = e.run("SW1", "spanning-tree mode rapid-pvst")
        assertTrue(o.any { it.kind == "note" && it.text.contains("outside this trainer") })
        assertEquals(before, e.state.dev("SW1").running)
        e.run("SW1", "int g0/1")
        val enc = e.run("SW1", "switchport trunk encapsulation dot1q")
        assertTrue(enc.any { it.kind == "note" && it.text.contains("802.1Q") })
    }

    @Test fun contextHelpAndTabCompletion() {
        val e = engine(LabCatalog.cliBasics)
        assertTrue(e.run("SW1", "?").text().contains("enable"))
        e.run("SW1", "en")
        val sh = e.run("SW1", "show ?").text()
        listOf("running-config", "vlan", "interfaces", "ip").forEach { assertTrue("show ? lists $it", sh.contains(it)) }
        assertTrue(e.run("SW1", "sh?").text().contains("show"))
        assertEquals("show ", e.complete("SW1", "sh"))
        assertEquals("show running-config ", e.complete("SW1", "show ru"))
        assertEquals("c", e.complete("SW1", "c"))   // ambiguous: configure / copy
        assertTrue(LabCatalog.cliBasics.tasks.first { it.id == "help" }.check(e.state, Network(e.state)))
    }

    // ---------------------------------------------------------------- vertical slice: VLANs and trunks
    @Test fun vlanLabCanBeSolvedAndEveryTaskPasses() {
        val lab = LabCatalog.vlanGuided; val e = engine(lab)
        assertEquals(lab.tasks.size, e.done(lab).size)   // nothing passes at the start
        e.script("SW1", *vlanSwitchScript); e.script("SW2", *vlanSwitchScript)
        assertEquals(emptyList<String>(), e.done(lab))
        val vb = e.run("SW1", "show vlan brief").text()
        assertTrue(vb.contains(Regex("10\\s+SALES\\s+active\\s+Fa0/1")))
        assertTrue(vb.contains(Regex("20\\s+ENG\\s+active\\s+Fa0/2")))
        assertFalse("trunks are not listed in show vlan brief", vb.contains("Gi0/1"))
        val tr = e.run("SW1", "show interfaces trunk").text()
        assertTrue(tr.contains(Regex("Gi0/1\\s+on\\s+802.1q\\s+trunking\\s+99")))
        assertTrue(tr.contains("10,20,99"))
        val trace = Network(e.state).hostPing("PC-A", "PC-B")
        assertFalse(trace.ok); assertNotNull(trace.failure)
    }

    @Test fun equivalentSolutionsAreAccepted() {
        val lab = LabCatalog.vlanGuided; val e = engine(lab)
        val alt = arrayOf("enable", "configure terminal", "interface range fa0/1 - 2", "switchport mode access", "exit",
            "interface fa0/1", "switchport access vlan 10",       // auto-creates VLAN 10 (named later)
            "interface fa0/2", "switchport access vlan 20",
            "vlan 10,20,99", "exit", "vlan 10", "name SALES", "vlan 20", "name ENG", "vlan 99", "name NATIVE", "exit",
            "interface g0/1", "switchport mode trunk", "switchport trunk allowed vlan 10", "switchport trunk allowed vlan add 20,99",
            "switchport trunk native vlan 99", "do write memory")
        e.script("SW1", *alt); e.script("SW2", *alt)
        assertEquals(emptyList<String>(), e.done(lab))
    }

    @Test fun allowedListReplacementWarnsAndTrunkFaultIsDiagnosable() {
        val lab = LabCatalog.trunkFault; val e = engine(lab)
        val n0 = Network(e.state)
        val t = n0.hostPing("PC-B", "PC-D")
        assertFalse(t.ok)
        assertTrue("trace names the dropping port: ${t.failure}", t.failure!!.contains("does not allow VLAN 20"))
        assertTrue(n0.hostPing("PC-A", "PC-C").ok)
        e.script("SW1", "en", "show interfaces trunk"); e.script("SW2", "en", "show interfaces trunk")
        e.script("SW2", "conf t", "int g0/1")
        val warn = e.run("SW2", "switchport trunk allowed vlan 20")
        assertTrue("replacing the list explains what was lost", warn.any { it.kind == "note" && it.text.contains("replaced") })
        assertFalse(Network(e.state).hostPing("PC-A", "PC-C").ok)  // the classic mistake breaks Sales too
        e.script("SW2", "switchport trunk allowed vlan add 10,99", "end", "copy run start")
        assertEquals(emptyList<String>(), e.done(lab))
    }

    @Test fun deletedVlanMakesPortInactive() {
        val lab = LabCatalog.vlanMissingFault; val e = engine(lab)
        e.run("SW2", "enable")
        assertFalse(e.run("SW2", "show vlan brief").text().contains(Regex("Fa0/2(?!\\d)")))
        assertTrue(e.run("SW2", "show interfaces fa0/2 switchport").text().contains("Access Mode VLAN: 20 (Inactive)"))
        val f = Network(e.state).hostPing("PC-B", "PC-D")
        assertFalse(f.ok); assertTrue(f.failure!!.contains("VLAN 20"))
        e.script("SW2", "conf t", "vlan 20", "name ENG", "end")
        assertEquals(emptyList<String>(), e.done(lab))
    }

    @Test fun challengeSolutionPassesAndHasNoHints() {
        val lab = LabCatalog.vlanChallenge; val e = engine(lab)
        assertTrue(lab.tasks.all { it.hints.isEmpty() })
        val s = arrayOf("en", "conf t", "vlan 30", "name HR", "vlan 40", "name OPS", "vlan 98", "name NATIVE",
            "int fa0/5", "sw mode access", "sw access vlan 30", "int fa0/6", "sw mode access", "sw access vlan 40",
            "int g0/2", "sw mode trunk", "sw trunk native vlan 98", "sw trunk allowed vlan 30,40,98", "end", "wr")
        e.script("SW1", *s); e.script("SW2", *s)
        assertEquals(emptyList<String>(), e.done(lab))
    }

    @Test fun dtpOutcomesAndNativeVlanLeak() {
        val base = LabCatalog.vlanGuided
        fun outcome(m1: String, m2: String, noneg1: Boolean = false): String {
            val e = engine(base)
            e.script("SW1", "en", "conf t", "int g0/1", "switchport mode $m1"); if (noneg1) e.run("SW1", "switchport nonegotiate")
            e.script("SW2", "en", "conf t", "int g0/1", "switchport mode $m2")
            return Network(e.state).operMode("SW1", "GigabitEthernet0/1")
        }
        assertEquals("access", outcome("dynamic auto", "dynamic auto"))
        assertEquals("trunk", outcome("dynamic desirable", "dynamic auto"))
        assertEquals("trunk", outcome("trunk", "dynamic auto"))
        assertEquals("trunk", outcome("dynamic desirable", "dynamic desirable"))
        assertEquals("access", outcome("access", "trunk"))
        val e = engine(base); e.script("SW1", "en", "conf t", "int g0/1", "switchport mode trunk", "switchport nonegotiate")
        assertEquals("trunk on SW1 but SW2 (auto) receives no DTP frames", "access", Network(e.state).operMode("SW2", "GigabitEthernet0/1"))

        // Native VLAN mismatch: SW1 sends VLAN 10 untagged (native 10); SW2 puts untagged frames into its native VLAN 20.
        val m = engine(base)
        val cfg = arrayOf("en", "conf t", "vlan 10", "vlan 20", "int fa0/1", "sw mode acc", "sw acc vl 10", "int fa0/2", "sw mode acc", "sw acc vl 20", "int g0/1", "sw mode trunk")
        m.script("SW1", *cfg, "sw trunk native vlan 10"); m.script("SW2", *cfg, "sw trunk native vlan 20")
        val n = Network(m.state)
        val seg = n.segment(Network.Endpoint("PC-A", "NIC")).endpoints.map { it.dev }
        assertTrue("VLAN 10 on SW1 leaks into VLAN 20 on SW2: $seg", "PC-D" in seg)
        assertFalse("PC-C (VLAN 10 on SW2) is not reached", "PC-C" in seg)
    }

    @Test fun reloadDiscardsUnsavedChangesButKeepsVlanDat() {
        val e = engine(LabCatalog.cliBasics)
        e.script("SW1", "en", "conf t", "hostname TEMP", "vlan 50", "name LAB", "end")
        val o = e.run("SW1", "reload")
        assertTrue(o.any { it.kind == "note" && it.text.contains("lost") })
        assertEquals("Switch", e.state.dev("SW1").running.hostname)
        assertEquals("LAB", e.state.dev("SW1").running.vlans[50])
        assertEquals("Switch>", e.prompt("SW1"))
        assertFalse("VLANs are not part of running-config on a VTP server", e.run("SW1", "en").let { e.run("SW1", "show run").text().contains("vlan 50") })
    }

    @Test fun cliBasicsLabCompletes() {
        val lab = LabCatalog.cliBasics; val e = engine(lab)
        e.script("SW1", "enable", "?", "show running-config", "configure terminal", "hostname SW-LAB", "interface fa0/1", "description PC-A",
            "do show startup-config", "end", "copy running-config startup-config", "reload")
        assertEquals(emptyList<String>(), e.done(lab))
    }

    // ---------------------------------------------------------------- routing labs
    @Test fun routerOnAStick() {
        val lab = LabCatalog.roas; val e = engine(lab)
        e.script("SW1", "en", "conf t", "int g0/2", "switchport mode trunk", "end", "wr")
        e.script("R1", "en", "conf t", "int g0/0", "no shut", "int g0/0.10")
        val bad = e.run("R1", "ip address 192.168.10.1 255.255.255.0")
        assertTrue("IP before encapsulation is rejected", bad.errors().isNotEmpty())
        e.script("R1", "encapsulation dot1Q 10", "ip address 192.168.10.1 255.255.255.0",
            "int g0/0.20", "encapsulation dot1q 20", "ip address 192.168.20.1 255.255.255.0", "end", "copy run start")
        assertEquals(emptyList<String>(), e.done(lab))
        val rt = e.run("R1", "show ip route").text()
        assertTrue(rt.contains("C        192.168.10.0/24 is directly connected, GigabitEthernet0/0.10"))
        assertTrue(rt.contains("L        192.168.20.1/32 is directly connected, GigabitEthernet0/0.20"))
        assertTrue(e.run("R1", "show ip interface brief").text().contains(Regex("GigabitEthernet0/0.10\\s+192.168.10.1\\s+YES manual up\\s+up")))
        val dup = e.script("R1", "conf t", "int g0/0.30", "encapsulation dot1q 10")
        assertTrue(dup.errors().isNotEmpty())
    }

    @Test fun staticRoutingNeedsAReturnPath() {
        val lab = LabCatalog.staticRouting; val e = engine(lab)
        e.script("R2", "en", "conf t", "int g0/1", "no shut", "int g0/0", "ip address 192.168.2.1 255.255.255.0", "no shutdown", "end")
        e.script("R1", "en", "conf t", "ip route 192.168.2.0 255.255.255.0 10.0.12.2", "end")
        val one = Network(e.state).hostPing("PC-1", "PC-2")
        assertFalse(one.ok)
        assertTrue("explains the missing return route: ${one.failure}", one.failure!!.contains("reply") && one.failure!!.contains("R2 has no route"))
        // R1's own ping is sourced from 10.0.12.1, which R2 knows as connected, so it succeeds even without the return route.
        assertTrue(e.run("R1", "ping 192.168.2.10").text().contains("!!!!!"))
        e.script("R2", "conf t", "ip route 0.0.0.0 0.0.0.0 10.0.12.1", "end")
        assertTrue(Network(e.state).hostPing("PC-1", "PC-2").ok)
        assertTrue(e.run("R1", "ping 192.168.2.10").text().contains("!!!!!"))
        val r2 = e.run("R2", "show ip route").text()
        assertTrue(r2.contains("Gateway of last resort is 10.0.12.1 to network 0.0.0.0"))
        assertTrue(r2.contains("S*    0.0.0.0/0 [1/0] via 10.0.12.1"))
        val r1 = e.run("R1", "show ip route").text()
        assertTrue(r1.contains("S     192.168.2.0/24 [1/0] via 10.0.12.2"))
        assertTrue(r1.contains("10.0.0.0/8 is variably subnetted, 2 subnets, 2 masks"))
        e.script("R1", "copy run start"); e.script("R2", "copy run start")
        assertEquals(emptyList<String>(), e.done(lab))
        // invalid route inputs
        assertTrue(e.script("R1", "conf t", "ip route 192.168.2.5 255.255.255.0 10.0.12.2").errors().any { it.text.contains("Inconsistent") })
        assertTrue(e.run("R1", "ip route 192.168.9.0 255.0.255.0 10.0.12.2").errors().isNotEmpty())
    }

    @Test fun longestPrefixAndAdministrativeDistance() {
        val e = engine(LabCatalog.staticRouting)
        e.script("R2", "en", "conf t", "int g0/1", "no shut", "end")
        e.script("R1", "en", "conf t", "ip route 0.0.0.0 0.0.0.0 10.0.12.2", "ip route 172.16.0.0 255.255.0.0 10.0.12.2",
            "ip route 172.16.5.0 255.255.255.0 10.0.12.2", "ip route 172.16.5.0 255.255.255.0 10.0.12.2 200", "end")
        val n = Network(e.state)
        assertEquals(24, n.lookup("R1", "172.16.5.9")!!.len)
        assertEquals(16, n.lookup("R1", "172.16.6.9")!!.len)
        assertEquals(0, n.lookup("R1", "8.8.8.8")!!.len)
        assertEquals("only the AD 1 copy is installed", 1, n.routes("R1").count { Ip.str(it.prefix) == "172.16.5.0" && it.len == 24 })
    }

    // ---------------------------------------------------------------- catalog integrity
    @Test fun everyLabIsWellFormed() {
        LabCatalog.all.forEach { lab ->
            val s = lab.initial()
            s.links.forEach { l ->
                listOf(l.a to l.aIf, l.b to l.bIf).forEach { (d, i) ->
                    val dev = s.devOrNull(d); assertNotNull("${lab.id}: device $d", dev)
                    if (dev!!.kind != "host") assertNotNull("${lab.id}: $d $i exists", dev.running.intf(i))
                }
            }
            assertTrue(lab.tasks.isNotEmpty())
            assertEquals("${lab.id}: unique task ids", lab.tasks.size, lab.tasks.map { it.id }.toSet().size)
            if (!lab.assessed) lab.tasks.forEach { assertEquals("${lab.id}/${it.id} has 3 progressive hints", 3, it.hints.size) }
            assertTrue("${lab.id}: tasks must not be complete at start", lab.tasks.any { !it.check(s, Network(s)) })
            val e = CliEngine(s)
            lab.verifyCommands.forEach { (d, cmd) ->
                e.state = e.state.update(e.state.dev(d).copy(session = Session(Mode.PRIV)))
                val o = e.run(d, cmd)
                assertTrue("${lab.id}: '$cmd' on $d must be supported: ${o.text()}", o.errors().isEmpty())
            }
            lab.pingTests.forEach { (a, b) -> assertNotNull(s.dev(a).hostIp); assertNotNull(s.dev(b).hostIp) }
            if (lab.kind == "troubleshoot") assertNotNull(lab.symptom)
        }
        assertEquals(LabCatalog.all.size, LabCatalog.byId.size)
    }

    @Test fun labStateSurvivesSerialization() {
        val e = engine(LabCatalog.vlanGuided)
        e.script("SW1", *vlanSwitchScript)
        e.script("SW2", "en", "conf t", "int g0/1")
        val json = AppJson.encodeToString(LabState.serializer(), e.state)
        val back = AppJson.decodeFromString(LabState.serializer(), json)
        assertEquals(e.state, back)
        assertEquals("SW2(config-if)#", back.dev("SW2").prompt)
    }

    @Test fun ipAndVlanListHelpers() {
        assertEquals(24, Ip.prefixLen("255.255.255.0")); assertNull(Ip.prefixLen("255.0.255.0"))
        assertEquals("255.255.255.252", Ip.maskStr(30))
        assertFalse(Ip.isHostAddress("192.168.1.0", "255.255.255.0")); assertFalse(Ip.isHostAddress("192.168.1.255", "255.255.255.0"))
        assertTrue(Ip.isHostAddress("10.0.12.1", "255.255.255.252")); assertTrue(Ip.isHostAddress("10.0.0.0", "255.255.255.254"))
        assertNull(Ip.parse("192.168.01.1")); assertNull(Ip.parse("256.1.1.1"))
        assertEquals(listOf(10, 20, 21, 22, 99), VlanList.parse("10,20-22,99"))
        assertEquals("10,20-22,99", VlanList.format(listOf(99, 22, 21, 20, 10)))
        assertNull(VlanList.parse("0")); assertNull(VlanList.parse("10-5")); assertNull(VlanList.parse("4095"))
        assertEquals("GigabitEthernet0/1", IfName.parse("g0/1")); assertEquals("GigabitEthernet0/0.10", IfName.parse("gi0/0.10"))
        assertEquals("Vlan10", IfName.parse("vlan10")); assertEquals("FastEthernet0/24", IfName.parse("fa0/24")); assertNull(IfName.parse("x0/1"))
    }

    @Test fun referenceIsGeneratedFromTheGrammar() {
        val ref = CliEngine(LabCatalog.cliBasics.initial()).reference()
        assertTrue(ref.size >= 40)
        assertTrue(ref.any { it.first == "switchport trunk allowed vlan add <VLAN_IDS>" })
        assertTrue(ref.all { it.third.isNotBlank() })
    }
}
