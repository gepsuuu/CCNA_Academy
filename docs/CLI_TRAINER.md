# CLI trainer: supported commands and limitations

Generated from the trainer grammar (`lab/CliEngine.kt`). Abbreviations are accepted as on IOS when unambiguous.

## Scope

- Modelled: CLI modes, hostname, VLAN database, access/trunk/dynamic switchport modes with simplified DTP outcomes, native VLAN, allowed VLAN lists, SVIs, router interfaces and 802.1Q subinterfaces, IPv4 addressing, static/default routes, running vs startup configuration, reload.
- Reachability (ping and the topology 'Test' button) is computed from the modelled tables: VLAN membership, trunks, native-VLAN tagging, connected and static routes, and host default gateways. It checks both the forward and return path.
- Not modelled: STP, DTP timers, CDP, ARP timing, MAC aging, dynamic routing protocols, ACLs, NAT, DHCP, security features, IPv6, and real traffic. Real IOS output contains more fields; trainer output keeps the fields the lessons teach.
- An accepted command is not proof of end-to-end connectivity. Use the verification commands and the reachability test.

## Commands

| Command | Mode(s) | Purpose |
|---|---|---|
| `enable` | User EXEC | Enter privileged EXEC mode |
| `disable` | Privileged EXEC | Return to user EXEC mode |
| `configure terminal` | Privileged EXEC | Enter global configuration mode |
| `copy running-config startup-config` | Privileged EXEC | Save running-config to startup-config (NVRAM) |
| `write` | Privileged EXEC | Save running-config (same as write memory) |
| `write memory` | Privileged EXEC | Save running-config (legacy shortcut) |
| `erase startup-config` | Privileged EXEC | Delete startup-config |
| `reload` | Privileged EXEC | Restart the device; unsaved changes are lost |
| `ping <A.B.C.D>` | User EXEC / Privileged EXEC | Test reachability (modelled) |
| `exit` | User EXEC / Privileged EXEC | Log out (returns to user EXEC in the trainer) |
| `show running-config` | Privileged EXEC | Display the running configuration |
| `show startup-config` | Privileged EXEC | Display the saved configuration |
| `show vlan brief` | User EXEC / Privileged EXEC (switch only) | VLANs and their access ports |
| `show vlan` | User EXEC / Privileged EXEC (switch only) | VLAN table (trainer shows the brief form) |
| `show interfaces trunk` | User EXEC / Privileged EXEC (switch only) | Operational trunks |
| `show interfaces status` | User EXEC / Privileged EXEC (switch only) | Port status, VLAN and description |
| `show interfaces <INTERFACE> switchport` | User EXEC / Privileged EXEC (switch only) | Administrative and operational switchport settings |
| `show ip interface brief` | User EXEC / Privileged EXEC | Interface IP addresses and status |
| `show ip route` | User EXEC / Privileged EXEC (router only) | Routing table |
| `hostname <WORD>` | Global config | Set the device name |
| `vlan <VLAN_IDS>` | Global config (switch only) | Create VLAN(s) and enter VLAN configuration mode |
| `no vlan <VLAN_IDS>` | Global config (switch only) | Delete VLAN(s) |
| `interface <INTERFACE>` | Global config | Enter interface configuration mode |
| `interface range <RANGE>` | Global config (switch only) | Configure several ports at once |
| `ip route <A.B.C.D> <A.B.C.D> <A.B.C.D>` | Global config (router only) | Static route via a next-hop address |
| `ip route <A.B.C.D> <A.B.C.D> <A.B.C.D> <<1-255>>` | Global config (router only) | Floating static route (custom AD) |
| `ip route <A.B.C.D> <A.B.C.D> <INTERFACE>` | Global config (router only) | Static route out an interface |
| `no ip route <A.B.C.D> <A.B.C.D>` | Global config (router only) | Remove static route(s) to a prefix |
| `ip default-gateway <A.B.C.D>` | Global config (switch only) | Gateway for the switch's own management traffic |
| `no ip default-gateway` | Global config (switch only) | Remove the default gateway |
| `name <WORD>` | VLAN config (switch only) | Name the VLAN |
| `no name` | VLAN config (switch only) | Restore the default VLAN name |
| `switchport mode access` | Interface / Interface range (switch only) | Make the port a static access port |
| `switchport mode trunk` | Interface / Interface range (switch only) | Make the port a static 802.1Q trunk |
| `switchport mode dynamic auto` | Interface / Interface range (switch only) | DTP: wait for the other side to request a trunk |
| `switchport mode dynamic desirable` | Interface / Interface range (switch only) | DTP: actively request a trunk |
| `switchport access vlan <<1-4094>>` | Interface / Interface range (switch only) | Assign the access VLAN |
| `no switchport access vlan` | Interface / Interface range (switch only) | Return the port to VLAN 1 |
| `switchport trunk native vlan <<1-4094>>` | Interface / Interface range (switch only) | Set the untagged (native) VLAN on a trunk |
| `no switchport trunk native vlan` | Interface / Interface range (switch only) | Native VLAN back to 1 |
| `switchport trunk allowed vlan <VLAN_IDS>` | Interface / Interface range (switch only) | REPLACE the allowed VLAN list |
| `switchport trunk allowed vlan add <VLAN_IDS>` | Interface / Interface range (switch only) | Append VLANs to the allowed list |
| `switchport trunk allowed vlan remove <VLAN_IDS>` | Interface / Interface range (switch only) | Remove VLANs from the allowed list |
| `switchport trunk allowed vlan all` | Interface / Interface range (switch only) | Allow all VLANs (default) |
| `switchport trunk allowed vlan none` | Interface / Interface range (switch only) | Allow no VLANs |
| `no switchport trunk allowed vlan` | Interface / Interface range (switch only) | Allowed list back to all VLANs |
| `switchport nonegotiate` | Interface / Interface range (switch only) | Disable DTP frames |
| `no switchport nonegotiate` | Interface / Interface range (switch only) | Re-enable DTP frames |
| `shutdown` | Interface / Interface range / Subinterface | Administratively disable the interface |
| `no shutdown` | Interface / Interface range / Subinterface | Enable the interface |
| `description <LINE>` | Interface / Interface range / Subinterface | Label the interface |
| `no description` | Interface / Interface range / Subinterface | Remove the description |
| `ip address <A.B.C.D> <A.B.C.D>` | Interface / Subinterface | Assign an IPv4 address |
| `no ip address` | Interface / Subinterface | Remove the IPv4 address |
| `encapsulation dot1Q <<1-4094>>` | Subinterface (router only) | Tag this subinterface with a VLAN |
| `encapsulation dot1Q <<1-4094>> native` | Subinterface (router only) | Native (untagged) VLAN subinterface |
| `exit` | Global config / Interface / Interface range / Subinterface / VLAN config | Go up one level |
| `end` | Global config / Interface / Interface range / Subinterface / VLAN config | Return to privileged EXEC (same as Ctrl+Z) |

## Editing aids

- `?` alone or after a word lists the next options; `sh?` lists completions.
- Tab (hardware key or on-screen chip) completes an unambiguous word.
- ↑/↓ recall per-device history (last 50 commands).
- `do <exec command>` from any configuration mode; configuration submodes fall back to global configuration like IOS.
- `end` / Ctrl+Z chip returns to privileged EXEC.

## Platform model

- Switches: Catalyst 2960-style, Fa0/1-24 and Gi0/1-2, default `switchport mode dynamic auto`, VLAN 1 SVI shut down, 802.1Q only.
- Routers: Gi0/0-0/2, shut down by default, 802.1Q subinterfaces.
- VLANs 2-1001 are stored in vlan.dat (not shown in running-config, kept across reload), as on a VTP server.
- DTP outcomes follow the standard matrix; timers are not modelled.
- `show` output keeps the fields the lessons teach; real IOS prints more fields (for example duplex/speed lines in running-config).

## Recognised but not modelled

These real IOS commands produce `% Invalid input` plus a `[Trainer]` explanation and change nothing: router ospf, access-list, ip nat, ip dhcp, ip helper-address, ip routing, spanning-tree, channel-group, switchport trunk encapsulation (not on this platform), port-security, voice VLAN, line/username/crypto/ssh, NTP/SNMP/syslog, CDP/LLDP/VTP, IPv6, show mac address-table, debug, traceroute, telnet/ssh sessions.
