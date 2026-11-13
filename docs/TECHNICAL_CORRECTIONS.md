# Technical corrections (1.5.0-rc1 -> rc2)

All IDs preserved. No highlights invalidated: changed text stays under the same block IDs; highlight resolver unchanged in behavior.

| Area | Defect | Correction | Reference |
|---|---|---|---|
| Diagrams 1-07/1-08 | 1-07 (Binary/Hex) showed RFC1918 private addressing | RFC1918 diagram moved to 1-08; new 1-07 worked 172 -> 10101100 -> 0xAC | RFC 1918 |
| Diagrams 2-07/2-08/2-09 | Shifted by one lesson; 2-09 showed SSID/WLAN | Re-keyed; new 2-09 SSH/VTY/console diagram | Cisco IOS SSH config guide |
| Diagrams 6-01/6-02/6-04 | 6-02 showed controller APIs, 6-04 showed AI | Re-keyed; new 6-04 Ansible vs Terraform diagram | Ansible / Terraform docs |
| 2-09 config | privilege 1 SSH user cannot `enable` over VTY with no enable secret | Added `enable secret` + mode explanation | Cisco IOS behavior ("% No password set") |
| 3-03 OSPF | Hello compatibility not explicit | Added area, hello/dead, auth, stub flag, subnet/mask; MTU via DBD (ExStart/Exchange); process ID local | RFC 2328 §10.5, §10.6 |
| Packet walkthrough | No HTTP response; DNS step mislabeled | Added response leg with seq 5001 / ack 1121 (=1001+120); retitled DNS step; HTTPS/TLS and NAT stated as out of scope | RFC 9293, RFC 9110 |
| 5-04 | Stale "acceptance pending" text in lesson | Replaced with pointer to port-security/DAI fixtures | - |
| Blueprint note | v2.0 date wording | Pinned to v1.1; v2.0 transition noted, not mixed | Cisco exam topics page (date not re-verified in rc2) |
| SYSTEM_DESIGN.md | Pipeline omitted Compare, added Review | Exact 8-stage pipeline; guarded by test_docs.py | Handoff §4 |
