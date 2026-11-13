from qlib import *
D = "d2"

S(D, "2.1", "vlan", 1, "command", "After 'switchport mode access', which command places interface Gi0/4 in VLAN 30?",
  ["*switchport access vlan 30 || This assigns the access port's data VLAN.",
   "switchport vlan 30 || This is not a valid IOS interface command.",
   "vlan 30 access || 'vlan 30' is a global command that creates the VLAN; it does not assign ports.",
   "switchport trunk native vlan 30 || This sets the untagged VLAN on a trunk, not an access port's VLAN."],
  "Access ports carry one untagged data VLAN set with 'switchport access vlan <id>'. If the VLAN does not exist, IOS typically creates it.",
  "Access port configuration")

S(D, "2.1", "vlan", 2, "troubleshooting",
  "A PC on Gi0/11 is configured as 192.168.10.50/24 with gateway 192.168.10.1, the VLAN 10 gateway. It cannot reach anything. Based on the output, what is the cause?",
  ["*Gi0/11 is in VLAN 20 instead of VLAN 10 || The PC is addressed for VLAN 10 but connected to a VLAN 20 port, so it cannot reach its gateway.",
   "Gi0/11 is configured as a trunk port || Trunk ports do not appear in 'show vlan brief'; Gi0/11 is listed as an access port in VLAN 20.",
   "VLAN 10 does not exist || VLAN 10 is listed as active.",
   "The gateway must be in VLAN 1 || Each VLAN uses its own gateway in its own subnet."],
  "Always confirm the port's VLAN matches the host's subnet. Fix with 'switchport access vlan 10' on Gi0/11.",
  "VLAN assignment",
  ex="SW1# show vlan brief\nVLAN Name        Status    Ports\n---- ----------- --------- -------------------------------\n1    default     active    Gi0/12\n10   STAFF       active    Gi0/1, Gi0/2, Gi0/3, Gi0/4, Gi0/5\n                           Gi0/6, Gi0/7, Gi0/8, Gi0/9, Gi0/10\n20   GUEST       active    Gi0/11")

S(D, "2.1", "vlan", 1, "concept", "Which statements about VLAN 1 on a Cisco switch are true?",
  ["*All switch ports belong to VLAN 1 by default || VLAN 1 is the default VLAN for every port.",
   "*VLAN 1 cannot be deleted || IOS does not allow removing the default VLAN.",
   "VLAN 1 is in the extended VLAN range || The extended range is 1006–4094.",
   "Frames in VLAN 1 are always tagged on 802.1Q trunks || By default VLAN 1 is also the native VLAN, which is sent untagged."],
  "VLAN 1 is the default and default native VLAN. Best practice is to move unused ports and the native VLAN away from VLAN 1.",
  "Default VLAN")

S(D, "2.1.b", "vlan", 2, "scenario",
  "An IP phone and a PC share Gi0/8, configured with 'switchport access vlan 10' and 'switchport voice vlan 20'. How is their traffic carried on the link to the switch?",
  ["*Voice tagged VLAN 20; PC untagged VLAN 10 || The phone tags voice traffic with the voice VLAN and passes the PC's untagged frames through.",
   "Both untagged in VLAN 10 || Voice would then share the data VLAN, defeating the voice VLAN's purpose.",
   "Both tagged with their own VLAN IDs || The PC sends normal untagged frames.",
   "Voice untagged; PC tagged VLAN 10 || This reverses the roles."],
  "The voice VLAN lets one access port carry two VLANs: tagged voice (learned by the phone via CDP or LLDP) and untagged data.",
  "Voice VLAN", "Data untagged, voice tagged.")

TF(D, "2.1", "vlan", 1, "concept", "Hosts in VLAN 10 and VLAN 20 on the same switch can communicate directly without a router or Layer 3 switch.", False,
   "Each VLAN is a separate broadcast domain and normally a separate subnet, so traffic between them must be routed.", "VLAN separation")

S(D, "2.2", "trunking", 2, "diagram", "Refer to the diagram. PC1 sends a broadcast frame. Which devices receive it?",
  ["*PC3 and the phone's PC || Both are in VLAN 10; the broadcast crosses the trunk tagged with VLAN 10.",
   "PC3 only, across the trunk || The PC behind the phone is also in VLAN 10 on SW1.",
   "PC2, PC3 and PC4 || PC2 and PC4 are in VLAN 20, a different broadcast domain.",
   "PC2 and PC4 || These are in VLAN 20 and never receive VLAN 10 broadcasts."],
  "A broadcast is flooded to all ports in the same VLAN, including across trunks that allow that VLAN. Other VLANs never see it.",
  "VLAN broadcast domains", dg="vlan_trunk")

S(D, "2.2", "trunking", 2, "config", "SW1 and SW2 are connected by an 802.1Q trunk, and both ends are configured with 'switchport trunk native vlan 99'. How do frames belonging to VLAN 99 cross the trunk?",
  ["*Untagged (native VLAN) || Native VLAN frames are sent without an 802.1Q tag.",
   "Tagged with VLAN ID 99 || Only non-native VLANs are tagged.",
   "They are dropped by the trunk || The native VLAN is carried untagged, not dropped.",
   "Tagged with VLAN ID 1 || The tag is never rewritten to VLAN 1."],
  "On an 802.1Q trunk, all VLANs are tagged except the native VLAN. Both ends must agree on the native VLAN.",
  "Native VLAN")

S(D, "2.2", "trunking", 2, "scenario", "SW1 Gi0/1 and SW2 Gi0/1 are both configured with 'switchport mode dynamic auto'. What is the link's operational mode?",
  ["*Access || Dynamic auto only responds to trunk requests; neither side initiates, so no trunk forms.",
   "Trunk || A trunk needs at least one side set to trunk or dynamic desirable.",
   "Err-disabled || DTP disagreement does not err-disable the port.",
   "Trunk carrying only the native VLAN || There is no such DTP outcome."],
  "DTP: desirable actively asks, auto only answers. auto + auto = access; desirable + auto/desirable/trunk = trunk.",
  "DTP negotiation", "Somebody has to be eager.")

S(D, "2.2", "trunking", 3, "config", "After the configuration shown, an engineer enters 'switchport trunk allowed vlan 30' on Gi0/1. Which VLANs are allowed on the trunk?",
  ["*Only VLAN 30 || Without the 'add' keyword, the command replaces the existing allowed list.",
   "VLANs 10, 20 and 30 || That result needs 'switchport trunk allowed vlan add 30'.",
   "VLANs 10 and 20 only || The new command overwrote the list.",
   "All VLANs (1–4094) || The allowed list is restricted, not reset to all."],
  "Use 'switchport trunk allowed vlan add <id>' to append, 'remove' to delete, and a bare list to replace.",
  "Trunk allowed VLAN list", "ADD or you REPLACE.",
  ex="interface GigabitEthernet0/1\n switchport mode trunk\n switchport trunk allowed vlan 10,20")

S(D, "2.2", "trunking", 3, "troubleshooting", "CDP reports a native VLAN mismatch: SW1 uses native VLAN 99 and SW2 uses native VLAN 1. What is the main risk?",
  ["*Traffic can leak between VLANs || Each side assigns untagged frames to its own native VLAN.",
   "The 802.1Q tag grows to 8 bytes || Tag size is always 4 bytes.",
   "All VLANs stop being tagged on the trunk || Non-native VLANs remain tagged.",
   "Both switches automatically change their native VLAN to 1 || Native VLANs are never adjusted automatically."],
  "Configure the same native VLAN on both ends (ideally an unused VLAN) to prevent VLAN leakage and spanning-tree inconsistencies.",
  "Native VLAN mismatch")

TF(D, "2.2", "trunking", 1, "concept", "An 802.1Q tag adds 4 bytes to an Ethernet frame and contains a 12-bit VLAN ID.", True,
   "The tag carries TPID 0x8100 (16 bits), PCP (3 bits), DEI (1 bit) and the VLAN ID (12 bits), for 4 bytes total.", "802.1Q tag")

S(D, "2.1.c", "intervlan", 2, "troubleshooting", "Hosts in VLAN 10 can reach their gateway, but hosts in VLAN 20 (192.168.20.0/24) cannot. Based on the configuration, what is wrong?",
  ["*Subinterface Gi0/0.20 uses 'encapsulation dot1Q 30' instead of 20 || The router expects VLAN 30 tags on that subinterface, so VLAN 20 frames never reach it.",
   "Routers need 'ip routing' to route between subinterfaces || IPv4 routing is enabled by default on routers.",
   "The subinterface number must equal the VLAN ID || The number is only a label; the encapsulation VLAN is what matters.",
   "VLAN 20 must be the native VLAN || Only one VLAN can be native, and it is not required here."],
  "In router-on-a-stick, each subinterface's 'encapsulation dot1Q <vlan>' must match the VLAN its hosts use, and the switch port must be a trunk.",
  "Router-on-a-stick",
  ex="interface GigabitEthernet0/0.10\n encapsulation dot1Q 10\n ip address 192.168.10.1 255.255.255.0\ninterface GigabitEthernet0/0.20\n encapsulation dot1Q 30\n ip address 192.168.20.1 255.255.255.0")

S(D, "2.1.c", "intervlan", 3, "config", "Which conditions are required for a Layer 3 switch to route between VLAN 10 and VLAN 20 using SVIs?",
  ["*'ip routing' is enabled globally || Without it, the switch does not route between its Layer 3 interfaces.",
   "*Each SVI's VLAN exists and has at least one active port forwarding in it || An SVI is up/up only when its VLAN exists and has an active member port.",
   "A router-on-a-stick trunk to an external router || SVIs route internally; no external router is needed.",
   "'encapsulation dot1Q' on each SVI || That command is used on router subinterfaces, not SVIs."],
  "SVI routing: create VLANs, assign ports, configure 'interface vlan X' with an IP and 'no shutdown', and enable 'ip routing'.",
  "SVI inter-VLAN routing")

S(D, "2.1.c", "intervlan", 1, "config", "A router routes between VLANs using subinterfaces Gi0/0.10 (encapsulation dot1Q 10) and Gi0/0.20 (encapsulation dot1Q 20). How must the switch port connected to the router's Gi0/0 be configured?",
  ["*As an 802.1Q trunk || The router's subinterfaces expect tagged frames for VLANs 10 and 20.",
   "As an access port in VLAN 10 || An access port would carry only VLAN 10.",
   "As a routed port || A routed port carries no VLANs to the router's subinterfaces.",
   "As a LACP EtherChannel member || Link aggregation is unrelated to carrying multiple VLANs."],
  "One physical router link carries multiple VLANs only when it is an 802.1Q trunk, with one subinterface per VLAN on the router.",
  "Router-on-a-stick trunk")

S(D, "2.3", "cdp-lldp", 1, "command", "Which command shows the IP address and software version of a directly connected Cisco neighbor?",
  ["*show cdp neighbors detail || The detail view includes management addresses and the IOS version.",
   "show cdp neighbors || The summary view lists device ID, local and remote ports, capability and platform, but not IP addresses.",
   "show cdp interface || This shows CDP timers and status per interface.",
   "show ip interface brief || This shows the local device's interfaces only."],
  "CDP neighbor details are essential for documenting topology when diagrams are missing.", "CDP verification")

S(D, "2.3", "cdp-lldp", 2, "scenario", "A Cisco switch connects to a non-Cisco firewall. The engineer wants to see the firewall's system name and port from the switch. What should be done?",
  ["*Enable LLDP with 'lldp run' || LLDP is the vendor-neutral IEEE 802.1AB discovery protocol, often disabled by default on IOS.",
   "Enable CDP with 'cdp run' || CDP is Cisco proprietary and usually not supported by other vendors.",
   "Configure the port as a trunk || Trunking does not exchange device identity.",
   "Check 'show mac address-table' || This shows a MAC address only, not system name or port."],
  "Use LLDP in multi-vendor environments. Verify with 'show lldp neighbors [detail]'.", "LLDP")

M(D, "2.3", "cdp-lldp", 2, "protocol-id", "Match each item to the correct value.",
  [("CDP default advertisement interval", "60 seconds"), ("CDP default holdtime", "180 seconds"),
   ("LLDP default advertisement interval", "30 seconds"), ("LLDP standard", "IEEE 802.1AB")],
  "CDP sends every 60 s with a 180 s holdtime. LLDP (802.1AB) sends every 30 s with a 120 s holdtime.", "Discovery protocol timers")

S(D, "2.4", "etherchannel", 2, "scenario", "SW1 and SW2 connect over two links. Both switches use 'channel-group 1 mode passive' on these ports. What is the result?",
  ["*No EtherChannel forms || Passive only responds to LACP; neither side starts negotiation.",
   "An LACP EtherChannel forms || LACP needs at least one side in active mode.",
   "A PAgP EtherChannel forms || Passive is an LACP mode, not PAgP.",
   "A static EtherChannel forms || Static bundling uses mode on."],
  "Working combinations: active/active, active/passive (LACP), desirable/desirable, desirable/auto (PAgP), on/on (static).",
  "LACP modes")

S(D, "2.4", "etherchannel", 2, "concept", "Which mode combinations successfully form an EtherChannel?",
  ["*active on one switch and passive on the other || LACP active initiates and passive responds.",
   "*desirable on one switch and auto on the other || PAgP desirable initiates and auto responds.",
   "passive on both switches || Neither side initiates LACP.",
   "auto on both switches || Neither side initiates PAgP.",
   "on on one switch and active on the other || Static 'on' does not negotiate, so it cannot pair with LACP."],
  "At least one side must actively negotiate, and both sides must use the same protocol (or both use 'on').",
  "EtherChannel negotiation")

S(D, "2.4", "etherchannel", 3, "troubleshooting", "Based on the output, what is the status of Gi0/2?",
  ["*It is suspended due to a mismatch || Flag (s) means suspended; common causes are VLAN, trunk or speed/duplex mismatches.",
   "It is bundled and forwarding traffic || Bundled ports show flag (P), as Gi0/1 does.",
   "It is a hot-standby LACP member || Hot-standby ports show flag (H).",
   "It is operating as a Layer 3 port || Layer 3 port-channels show flag R on the port-channel, and Po1 shows S (Layer 2)."],
  "All EtherChannel members must match in speed, duplex, switchport mode and VLAN settings. Correct the mismatch and the port joins the bundle.",
  "EtherChannel verification",
  ex="SW1# show etherchannel summary\nFlags:  D - down        P - bundled in port-channel\n        I - stand-alone s - suspended\n        H - Hot-standby (LACP only)\n        R - Layer3      S - Layer2\n        U - in use\n\nGroup  Port-channel  Protocol    Ports\n------+-------------+-----------+------------------\n1      Po1(SU)         LACP      Gi0/1(P)    Gi0/2(s)")

TF(D, "2.4", "etherchannel", 2, "concept", "EtherChannel load balancing is per flow, so one large file transfer between two hosts normally uses only one member link.", True,
   "A hash of addresses (and optionally ports) selects a member link per flow to keep packets in order. Many flows spread across links; a single flow does not.",
   "EtherChannel load balancing")

S(D, "2.5", "stp", 1, "diagram", "Refer to the diagram. Which switch is the root bridge for VLAN 10, and why?",
  ["*SW1, because it has the lowest bridge priority || The lowest bridge ID wins, and priority is compared before MAC address.",
   "SW3, because it has the highest MAC address || The lowest MAC is used only as a tiebreaker, and lowest wins.",
   "SW2, because it is connected to both other switches || Topology position does not determine the root.",
   "No root is elected because priorities differ || Different priorities simply decide the election."],
  "Bridge ID = priority (+ VLAN ID system-ID extension) + MAC. SW1's 24586 is lower than 32778, so SW1 is root regardless of MAC.",
  "Root bridge election", "Lowest wins in STP.", dg="stp_quiz")

S(D, "2.5", "stp", 2, "diagram", "Refer to the diagram. Which port is in the alternate (discarding) role?",
  ["*SW3's port toward SW2 || On the SW2–SW3 segment, SW2 has the lower bridge ID and owns the designated port, leaving SW3's port as alternate.",
   "SW2's port toward SW3 || SW2's port is designated on that segment.",
   "SW1's port toward SW2 || All root bridge ports are designated.",
   "SW3's port toward SW1 || That is SW3's root port (lowest cost to the root)."],
  "Root ports point toward the root; each segment gets one designated port; remaining ports discard. Ties are broken by the lowest bridge ID.",
  "STP port roles", dg="stp_quiz")

S(D, "2.5", "stp", 3, "calculation",
  "A non-root switch can reach the root through path A (one 100 Mbps link) or path B (two 1 Gbps links). Using default short path costs, which path is used for its root port?",
  ["*Path B, with a total cost of 8 || Each 1 Gbps link costs 4, so 4 + 4 = 8, which is lower than 19.",
   "Path A, with a total cost of 19 || 19 is higher than 8.",
   "Both paths, because STP load-balances equal-cost paths || STP picks a single root port.",
   "Path A, because it crosses fewer switches || STP compares cost, not hop count."],
  "Default short costs: 10 Mbps = 100, 100 Mbps = 19, 1 Gbps = 4, 10 Gbps = 2. Root path cost is the sum along the path.",
  "STP path cost", "100-19-4-2.")

S(D, "2.5", "stp", 2, "command", "Which command makes SW2 the root bridge for VLAN 20 by automatically setting a low enough priority?",
  ["*spanning-tree vlan 20 root primary || This sets priority to 24576, or lower if needed, to win the election.",
   "spanning-tree vlan 20 priority 30000 || Priority must be a multiple of 4096, so IOS rejects 30000.",
   "spanning-tree root vlan 20 || This is not valid IOS syntax.",
   "spanning-tree mode root || Mode selects the STP variant (e.g., rapid-pvst), not the root."],
  "Use 'root primary' / 'root secondary', or set 'spanning-tree vlan X priority N' with N as a multiple of 4096.",
  "Root bridge configuration")

M(D, "2.5", "stp", 3, "concept", "Match each STP protection feature to its behavior.",
  [("PortFast", "Moves an edge port immediately to forwarding"), ("BPDU Guard", "Err-disables an edge port that receives a BPDU"),
   ("Root Guard", "Blocks a port that receives superior BPDUs (root-inconsistent)"),
   ("Loop Guard", "Stops a non-designated port from forwarding when BPDUs stop arriving")],
  "PortFast speeds up host ports; BPDU Guard protects them from switches; Root Guard protects root placement; Loop Guard protects against unidirectional-link loops.",
  "STP protection features")

TF(D, "2.5", "stp", 1, "concept", "In Rapid PVST+, the port states are discarding, learning and forwarding.", True,
   "RSTP merges the legacy disabled, blocking and listening states into one discarding state.", "RSTP port states")

S(D, "2.5", "stp", 2, "troubleshooting", "A user plugs a small managed switch into a wall port configured with PortFast and BPDU Guard. The port immediately becomes err-disabled. Why?",
  ["*The port received a BPDU from the new switch || BPDU Guard err-disables a PortFast port that receives any BPDU.",
   "Port security detected a second MAC address || Port security is not mentioned and would report a security violation.",
   "A duplex mismatch occurred || Duplex problems do not err-disable ports.",
   "CDP detected an unknown device || CDP does not disable ports."],
  "BPDU Guard keeps unauthorized switches off edge ports. After removing the switch, recover with shutdown/no shutdown or errdisable recovery.",
  "BPDU Guard")

O(D, "2.5", "stp", 2, "concept", "Arrange the spanning-tree decision process in order.",
  ["Elect the root bridge", "Select one root port on each non-root switch", "Select one designated port on each segment", "Place all remaining ports in the alternate/blocking role"],
  "STP elects the root first, then root ports (best path to root), then designated ports per segment; everything else discards.",
  "STP election sequence")

S(D, "2.7", "wlan-arch", 2, "scenario", "Which AP mode lets a branch AP keep switching client traffic locally if the WAN link to the central WLC fails?",
  ["*FlexConnect || FlexConnect APs can switch client data locally and keep serving clients without the WLC.",
   "Local || Local-mode APs tunnel all client traffic to the WLC and depend on it.",
   "Monitor || Monitor mode serves no clients.",
   "Sniffer || Sniffer mode captures traffic for an analyzer and serves no clients."],
  "FlexConnect is designed for branches across a WAN: control traffic goes to the WLC while client data can be switched locally.",
  "AP modes")

S(D, "2.6", "wlan-arch", 2, "diagram", "Refer to the diagram. The APs operate in Local mode. Why are they connected to access ports rather than trunk ports?",
  ["*Client traffic is tunneled to the WLC in CAPWAP || The WLC places client traffic into VLANs, not the access switch.",
   "Lightweight APs cannot negotiate trunks || APs in FlexConnect mode use trunks when switching multiple VLANs locally.",
   "CAPWAP works only in VLAN 1 || CAPWAP uses any routable management VLAN.",
   "Trunk ports cannot provide PoE || PoE works on trunk ports too."],
  "Local mode → access port (management VLAN). FlexConnect with local switching → trunk. WLC → trunk to reach many client VLANs.",
  "AP switch port types", dg="wireless")

S(D, "2.6", "wlan-arch", 2, "protocol-id", "Which statements about CAPWAP are correct?",
  ["*Control messages use UDP port 5246 || CAPWAP control is carried on UDP 5246 and protected by DTLS.",
   "*Client data uses UDP port 5247 || The data channel uses UDP 5247.",
   "CAPWAP runs over TCP 443 || CAPWAP uses UDP.",
   "Encrypting the data channel is mandatory || Data-channel DTLS encryption is optional; control is always encrypted."],
  "CAPWAP builds tunnels between lightweight APs and the WLC: control (5246) and data (5247).", "CAPWAP")

S(D, "2.6", "wlan-arch", 3, "config", "An AireOS WLC's distribution ports are bundled with LAG to a Catalyst switch. Which switch interface configuration is required?",
  ["*channel-group 1 mode on || AireOS LAG does not negotiate, so the switch must use a static EtherChannel.",
   "channel-group 1 mode active || LACP negotiation is not supported by AireOS LAG.",
   "channel-group 1 mode desirable || PAgP is not supported by AireOS LAG.",
   "channel-group 1 mode passive || Passive LACP waits for a peer that will never negotiate."],
  "AireOS WLC LAG is static, so the switch side must be 'mode on', with matching trunk settings.",
  "WLC LAG")

M(D, "2.7", "wlan-arch", 2, "concept", "Match each AP mode to its purpose.",
  [("Local", "Serves clients and tunnels all client traffic to the WLC"), ("FlexConnect", "Serves clients and can switch traffic locally at a branch"),
   ("Monitor", "Serves no clients; scans RF for rogues and location"), ("Sniffer", "Captures 802.11 frames and forwards them to a packet analyzer")],
  "Choose AP mode by purpose: Local for campus, FlexConnect for branches, Monitor for RF security/location, Sniffer for troubleshooting captures.",
  "AP modes")

S(D, "2.9", "wlc-gui", 1, "scenario", "In the WLC GUI, an administrator creates a WLAN dedicated to IP phones. Which QoS profile should be selected?",
  ["*Platinum || Platinum is intended for voice.", "Gold || Gold is intended for video.", "Silver || Silver is best effort (the default).", "Bronze || Bronze is for background traffic."],
  "WLC QoS profiles: Platinum (voice), Gold (video), Silver (best effort), Bronze (background).", "WLC QoS profiles", "Precious metals rank priority.")

S(D, "2.9", "wlc-gui", 2, "troubleshooting", "Clients connect to the 'Corp' SSID but receive addresses from the guest subnet. Which WLAN setting is most likely misconfigured?",
  ["*The WLAN's interface mapping || This mapping decides which VLAN (and DHCP scope) clients join.",
   "The QoS profile || QoS affects prioritization, not addressing.",
   "SSID broadcast || Broadcasting the SSID does not affect which subnet clients use.",
   "Layer 2 security method || Security controls authentication and encryption, not the client VLAN."],
  "On a WLC, each WLAN is mapped to an interface (VLAN). A wrong mapping puts clients in the wrong subnet.",
  "WLAN to VLAN mapping")

S(D, "2.8", "mgmt-access", 3, "troubleshooting",
  "SSH connections to SW1 fail, although its management SVI is reachable. Based on the configuration, which step is missing?",
  ["*Generating RSA keys with 'crypto key generate rsa' || SSH stays disabled until RSA keys exist; hostname and domain name are already set.",
   "Configuring 'enable secret' || Privileged EXEC access is not required to open an SSH session.",
   "Configuring 'ip ssh version 1' || SSHv1 is obsolete and is not required.",
   "Configuring 'service password-encryption' || This only obscures clear-text passwords in the config."],
  "SSH prerequisites: hostname, ip domain-name, RSA keys (≥768 bits for SSHv2), local user (or AAA), 'login local' and 'transport input ssh' on the VTY lines.",
  "SSH configuration",
  ex="hostname SW1\nip domain-name lab.local\nusername admin secret Str0ngPass!\n!\nline vty 0 15\n login local\n transport input ssh")

S(D, "2.8", "mgmt-access", 2, "scenario", "An engineer must copy an IOS image to a router across a network where credentials must not be sent in clear text. Which protocol is most appropriate?",
  ["*SCP || SCP runs over SSH, so both authentication and data transfer are encrypted.",
   "TFTP || TFTP has no authentication or encryption.",
   "FTP || FTP authenticates, but credentials and data are sent in clear text.",
   "Telnet || Telnet is a remote CLI protocol, not file transfer, and is unencrypted."],
  "For secure file transfer to network devices, use SCP or SFTP. TFTP and FTP are acceptable only on trusted management networks.",
  "Secure file transfer")

TF(D, "2.8", "mgmt-access", 1, "concept", "Console access to a router works even if the router has no IP address configured.", True,
   "The console is an out-of-band serial connection that does not depend on IP, which is why it is used for initial setup and recovery.", "Console access")

S(D, "2.1", "vlan", 1, "concept", "Which VLAN ID belongs to the extended VLAN range?",
  ["*2000 || The extended range is 1006–4094.", "1000 || 1000 is in the normal range (1–1005).",
   "1 || VLAN 1 is the default VLAN in the normal range.", "1005 || 1002–1005 are reserved normal-range VLANs."],
  "Normal range: 1–1005 (1002–1005 reserved). Extended range: 1006–4094. VLAN IDs are 12 bits.", "VLAN ranges")

S(D, "2.2", "trunking", 1, "command", "Which command displays the native VLAN and the allowed VLANs of every trunk port?",
  ["*show interfaces trunk || It lists mode, encapsulation, native VLAN, allowed, active and forwarding VLANs per trunk.",
   "show vlan brief || It lists access ports per VLAN and omits trunks.",
   "show spanning-tree || It shows STP roles and states, not trunk allowed lists.",
   "show cdp neighbors || It shows neighbors, not trunk configuration."],
  "'show interfaces trunk' is the first command to run when a VLAN does not cross a trunk.", "Trunk verification")

S(D, "2.8", "mgmt-access", 2, "config", "Which VTY line configuration allows only SSH logins authenticated against local usernames?",
  ["*login local and transport input ssh || 'login local' uses the local user database and 'transport input ssh' rejects Telnet.",
   "password cisco, login, transport input all || This uses a shared line password and allows Telnet.",
   "login local and transport input telnet ssh || Telnet is still permitted.",
   "no login and transport input ssh || 'no login' disables authentication entirely."],
  "Combine local (or AAA) authentication with SSH-only transport, plus exec-timeout and an ACL with access-class for further hardening.",
  "Secure VTY lines")

S(D, "2.5", "stp", 2, "scenario",
  "In VLAN 1, SW1 has bridge priority 32769 and MAC 0011.1111.1111; SW2 has bridge priority 32769 and MAC 0011.1111.0001. Which switch is the root bridge?",
  ["*SW2, because the priorities tie and its MAC address is lower || The MAC address breaks the tie, and lower wins.",
   "SW1, because its MAC address is higher || STP prefers the lowest bridge ID.",
   "Neither, because the priorities are identical || A tie in priority is always resolved by the MAC address.",
   "Both switches share the root bridge role || Each STP instance has exactly one root bridge."],
  "32769 = 32768 default priority + VLAN 1 (system ID extension). Compare priority first, then MAC.", "Bridge ID tiebreaker")

S(D, "2.4", "etherchannel", 3, "config", "Which items are required to build a Layer 3 EtherChannel between two multilayer switches?",
  ["*'no switchport' on the member interfaces and the port-channel interface || Layer 3 port-channels use routed ports.",
   "*The IP address configured on the port-channel interface || The logical interface carries the address, not the members.",
   "An IP address on each member interface || Members carry no individual addresses.",
   "'switchport mode trunk' on the port-channel || Trunking is a Layer 2 concept."],
  "Layer 3 EtherChannel members appear with flag R (Layer 3) in show etherchannel summary.", "Layer 3 EtherChannel")

S(D, "2.2", "trunking", 3, "troubleshooting",
  "'show interfaces trunk' lists VLAN 30 under 'Vlans allowed on trunk' but not under 'Vlans allowed and active in management domain'. What is the most likely cause?",
  ["*VLAN 30 has not been created on this switch || A VLAN must exist in the local VLAN database to be active on the trunk.",
   "VLAN 30 is the native VLAN || The native VLAN would still be active.",
   "Spanning tree is blocking VLAN 30 || STP blocking is shown in the 'forwarding state and not pruned' list.",
   "The trunk uses ISL encapsulation || Encapsulation does not remove VLANs from the active list."],
  "Create the VLAN ('vlan 30') or make sure VTP is propagating it. The trunk output lists allowed, active and forwarding VLANs separately.",
  "Trunk VLAN lists")
