# Lesson coverage matrix (v1.4)

Generated from the app's catalogs (`lab/LessonExtras.kt`, `lab/Labs.kt`, `lab/Animations.kt`) and `curriculum.json`. Columns: **Tables** = explanatory tables in the lesson (C = linked comparison), **Diagrams** = existing static diagrams, **Anim** = step-through animations, **CLI** = command-by-command walkthrough, **Labs** = trainer labs (G guided, T troubleshooting, X independent challenge), **Output** = output-interpretation activities, **Interactive** = interactive exercises available in the lesson, **Accessible alternative** = non-motion / non-drag alternative provided.

| Lesson | Objectives | Tables | Diagrams | Anim | CLI | Labs | Output | Interactive | Accessible alternative | Note |
|---|---|---|---|---|---|---|---|---|---|---|
| 1-01 Network Components and Their Roles | 1.1 | 1 | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 1-02 Network Topology Architectures | 1.2 | 1 | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 1-03 OSI and TCP/IP Models and Encapsulation | 1.0 (foundation) | 1 + 1C | 2 | 1 | — | — | — | knowledge check, table recall/quiz, teach-back, step-through | static step list + reduced motion; stacked cards on narrow screens |  |
| 1-04 Cabling, Interfaces, and Interface Issues | 1.3, 1.4 | 1 | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 1-05 Ethernet Frames, MAC Addresses, and ARP | 1.13, 1.0 (foundation) | 1 | 1 | 1 | — | — | — | knowledge check, table recall/quiz, step-through | static step list + reduced motion; stacked cards on narrow screens |  |
| 1-06 TCP vs UDP | 1.5 | 1 + 1C | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 1-07 Binary and Hexadecimal Conversions | 1.6, 1.8 (supporting) | 1 | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 1-08 IPv4 Addressing and Private Addresses | 1.6, 1.7 | 1 + 1C | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 1-09 IPv4 Subnetting and VLSM | 1.6 | 1 | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 1-10 IPv6 Addressing and Address Types | 1.8, 1.9 | 1 + 1C | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 1-11 Verifying IP Parameters on Client Operating Systems | 1.10 | 1 | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 1-12 Wireless Principles | 1.11 | 1 | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 1-13 Virtualization and Cloud Fundamentals | 1.12, 1.2.f | 1 | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 1-14 Switching Concepts: MAC Learning, Forwarding, and Flooding | 1.13 | 1 + 2C | 1 | 1 | — | — | — | knowledge check, table recall/quiz, teach-back, step-through | static step list + reduced motion; stacked cards on narrow screens |  |
| 2-01 VLANs: Access, Voice, and Default VLAN | 2.1 | 2 + 2C | 1 | 1 | 5 cmds | G:cli-basics G:vlan-trunk T:vlan-missing-fault X:vlan-challenge | 1 | knowledge check, table recall/quiz, predict/interpret output, teach-back, CLI lab, step-through | static step list + reduced motion; stacked cards on narrow screens; tap chips for ?, Tab, history |  |
| 2-02 Trunking: 802.1Q and the Native VLAN | 2.2 | 2 | 1 | 1 | 5 cmds | G:vlan-trunk T:trunk-fault X:vlan-challenge | 1 | knowledge check, table recall/quiz, predict/interpret output, teach-back, CLI lab, step-through | static step list + reduced motion; stacked cards on narrow screens; tap chips for ?, Tab, history |  |
| 2-03 Inter-VLAN Routing | 2.1.c, 1.1.b, 3.0 (supporting) | 1 | 1 | 1 | 5 cmds | G:roas | 1 | knowledge check, table recall/quiz, predict/interpret output, CLI lab, step-through | static step list + reduced motion; stacked cards on narrow screens; tap chips for ?, Tab, history |  |
| 2-04 Layer 2 Discovery: CDP and LLDP | 2.3 | 1 | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens | CDP/LLDP neighbour discovery is not modelled in the trainer; use the comparison table and question bank. |
| 2-05 EtherChannel and LACP | 2.4 | 1 | 0 | 1 | — | — | — | knowledge check, table recall/quiz, step-through | static step list + reduced motion; stacked cards on narrow screens | EtherChannel behaviour (negotiation and hashing) is taught with the animation and negotiation table; the CLI trainer does not model port-channels. |
| 2-06 Spanning Tree Protocol and Rapid PVST+ | 2.5 | 1 + 1C | 1 | 1 | — | — | — | knowledge check, table recall/quiz, step-through | static step list + reduced motion; stacked cards on narrow screens | STP is taught with the step-through animation and role table; the trainer does not model BPDUs. |
| 2-07 Cisco Wireless Architectures and AP Modes | 2.6, 2.7 | 1 | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 2-08 Interpreting WLAN Configuration in the WLC GUI | 2.9, 5.10 | 1 | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens | WLAN configuration is done in the WLC GUI, so this lesson uses GUI interpretation rather than CLI practice. |
| 2-09 Device Management Access | 2.8, 4.8, 4.9 | 3 | 0 | 0 | 5 cmds | G:cli-basics | 1 | knowledge check, table recall/quiz, predict/interpret output, CLI lab | stacked cards on narrow screens; tap chips for ?, Tab, history |  |
| 3-01 Routing Fundamentals and the Routing Table | 3.1, 3.2 | 2 + 1C | 1 | 1 | — | G:static-routing | — | knowledge check, table recall/quiz, teach-back, CLI lab, step-through | static step list + reduced motion; stacked cards on narrow screens; tap chips for ?, Tab, history |  |
| 3-02 Static Routing for IPv4 and IPv6 | 3.3 | 2 | 1 | 1 | 4 cmds | G:static-routing | 1 | knowledge check, table recall/quiz, predict/interpret output, CLI lab, step-through | static step list + reduced motion; stacked cards on narrow screens; tap chips for ?, Tab, history |  |
| 3-03 OSPFv2 Single Area | 3.4 | 1 + 1C | 1 | 1 | — | — | — | knowledge check, table recall/quiz, step-through | static step list + reduced motion; stacked cards on narrow screens | OSPF is taught with the adjacency animation and neighbor-requirements table; the trainer does not model dynamic routing. |
| 3-04 First Hop Redundancy Protocols | 3.5 | 1 | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 4-01 DHCP and DNS | 4.3, 4.6 | 1 | 1 | 2 | — | — | — | knowledge check, table recall/quiz, step-through | static step list + reduced motion; stacked cards on narrow screens | DHCP is taught with the DORA and relay animations; the trainer does not model DHCP. |
| 4-02 NAT and PAT | 4.1, 1.7 | 1 + 1C | 1 | 1 | — | — | — | knowledge check, table recall/quiz, step-through | static step list + reduced motion; stacked cards on narrow screens | NAT/PAT is taught with the translation-table animation; the trainer does not model NAT. |
| 4-03 NTP, SNMP, and Syslog | 4.2, 4.4, 4.5 | 2 | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 4-04 Quality of Service (QoS) | 4.7 | 2 | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 5-01 Security Concepts and Security Program Elements | 5.1, 5.2 | 1 | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 5-02 Device Access Control, Passwords, and AAA | 5.3, 5.4, 5.8 | 0 + 1C | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 5-03 Access Control Lists (ACLs) | 5.6 | 1 + 1C | 1 | 1 | — | — | — | knowledge check, table recall/quiz, teach-back, step-through | static step list + reduced motion; stacked cards on narrow screens | ACL logic is taught with the step-through ACL evaluator; the trainer does not model ACLs. |
| 5-04 Layer 2 Security: Port Security, DHCP Snooping, and DAI | 5.7 | 1 | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens | Port security, DHCP snooping and DAI are covered with comparison tables; the trainer does not model them. |
| 5-05 VPN Fundamentals | 5.5 | 1 | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 5-06 Wireless Security Protocols | 5.9, 5.10 | 1 | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 6-01 Automation and Controller-Based Networking | 6.1, 6.2, 6.3 | 0 + 1C | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 6-02 AI and Machine Learning in Network Operations | 6.4 | 1 | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 6-03 REST APIs and JSON | 6.5, 6.7 | 1 | 1 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |
| 6-04 Configuration Management: Ansible and Terraform | 6.6 | 1 | 0 | 0 | — | — | — | knowledge check, table recall/quiz | stacked cards on narrow screens |  |

**Totals:** 41 lessons · 41 with explanatory tables · 14 with animations · 6 with CLI labs · 7 labs · 12 animations · 5 output activities.

## Not yet covered by v1.4 additions

Lessons below rely on the existing v1.3 content (text, diagrams, knowledge checks, question bank). They are candidates for the next iteration:

