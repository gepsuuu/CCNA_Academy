"""Separate isolated fixtures; never paste alternative configurations together."""
EXPANSIONS={}
def add(lid,key,title,fixture,commands,explanation,verify,undo):
 def entry(stage,suffix,heading,text,type='text',**kw):
  EXPANSIONS.setdefault(lid,[]).append(dict(stage=stage,block=dict(id=f'{lid}-extension-{key}-{suffix}',type=type,title=heading,text=text,**kw)))
 entry('Configure','context',title+' | objective and fixture',fixture+'\n\nInstructional configuration for an isolated lab, not a live connection. Unsupported commands must not be entered into the offline trainer as if it were full IOS.')
 entry('Configure','commands',title+' | configuration','Copy only into the stated device/context after capturing its baseline.','code',items=commands.strip().splitlines())
 entry('Configure','explain',title+' | command purpose and modes',explanation)
 entry('Verify','verify',title+' | verification and expected result',verify)
 entry('Troubleshoot','undo',title+' | correction and safe restoration',undo)

add('2-01','voice','Data and voice on one access port',
'SW1 Fa0/10 connects to a compatible Cisco phone; PC-A is behind the phone. Data VLAN 10 uses 192.168.10.0/24; voice VLAN 30 uses 192.168.30.0/24. VLAN gateways/DHCP/voice services and uplinks carrying both VLANs are preconfigured. Use a Catalyst IOS platform supporting this voice-VLAN syntax and CDP; this is separate from the two-department pilot fixture.',
'''enable
configure terminal
vlan 10
 name DATA
vlan 30
 name VOICE
interface FastEthernet0/10
 switchport mode access
 switchport access vlan 10
 switchport voice vlan 30
 cdp enable
 no shutdown
end
show interfaces FastEthernet0/10 switchport''',
'enable selects Privileged EXEC; configure terminal selects global configuration. vlan/name create and label each local VLAN. interface selects Fa0/10. mode access establishes the data-port role; access vlan 10 classifies ordinary untagged PC traffic. voice vlan 30 advertises the voice association to a compatible phone, which tags its voice traffic. cdp enable requires global CDP to be enabled as well. no shutdown enables the port; end returns to EXEC. This is a multi-VLAN access-port feature, not a general interswitch trunk. QoS trust requirements vary by platform and are not silently assumed.',
'Inspect administrative/operational mode, Access Mode VLAN 10, and Voice VLAN 30 in show interfaces Fa0/10 switchport. Confirm the phone learned VLAN 30 and the PC remains in VLAN 10. Check DHCP leases and both gateways. A phone that powers up has not necessarily obtained voice connectivity.',
'If the PC works but the phone does not, check voice-VLAN discovery, VLAN 30 uplink eligibility, DHCP/voice services, and phone compatibility. Restore the captured voice/access/CDP settings; no switchport voice vlan removes the exercise voice association. Do not delete VLAN 30 while other phones use it.')

for name,ip in [('SW1','10.0.12.1'),('SW2','10.0.12.2')]:
 add('2-05','l3-'+name.lower(),'Layer 3 LACP on '+name,
'Two multilayer Catalyst IOS/IOS XE switches supporting routed ports and LACP. Gi0/1 and Gi0/2 connect to the same numbered peer ports; Port-channel2 uses 10.0.12.0/30. This is a fresh fixture, not the Layer 2 Po1 trunk example.',
 f'''enable
configure terminal
ip routing
interface range GigabitEthernet0/1 - 2
 no switchport
 no ip address
 channel-group 2 mode active
 no shutdown
interface Port-channel2
 no switchport
 ip address {ip} 255.255.255.252
 no shutdown
end
show etherchannel summary
show ip interface brief''',
'ip routing enables Layer 3 forwarding on a supported switch. interface range selects physical members; no switchport makes them routed and no ip address avoids member addressing. channel-group 2 mode active negotiates LACP. The logical Port-channel2 is routed and receives the single /30 address. Member ports do not each get an IP. Both sides use compatible physical settings; group numbers are local but this fixture uses 2 on both.',
'Expect Po2(RU) with both members P in the actual output legend, an up/up logical interface, and connected 10.0.12.0/30. Ping the peer port-channel address. One flow may still use one member. Check show lacp neighbor if members do not bundle.',
'For a suspended member, compare routed/switchport state, speed/duplex, LACP mode, and peer cabling. In the isolated lab, shut members before removing channel-group 2 and the unused Port-channel2, then restore baseline. Do not disable global ip routing if unrelated services depend on it.')

add('3-02','ipv6','IPv6 network, host, default, and floating routes',
'IPv6-capable IOS R1 Gi0/0 2001:db8:12::1/64 to R2 ::2. R1 LAN is 2001:db8:10::/64; R2 LAN is 2001:db8:20::/64. Both have ipv6 unicast-routing and operational interfaces. R2 has return route 2001:db8:10::/64 via 2001:db8:12::1. Backup next hop 2001:db8:13::2 is reachable through a separate operational R1 interface in 2001:db8:13::/64.',
'''enable
configure terminal
ipv6 route 2001:DB8:20::/64 2001:DB8:12::2
ipv6 route 2001:DB8:20::20/128 2001:DB8:12::2
ipv6 route ::/0 2001:DB8:12::2
ipv6 route 2001:DB8:20::/64 2001:DB8:13::2 200
end
show ipv6 route static''',
'All ipv6 route lines are global configuration. /64 selects a network, /128 selects one host, and ::/0 is the default. The final 200 is administrative distance for the backup /64. The /128 wins forwarding for that host while installed, even if the /64 changes. To test /64 failover, use another host such as ::30 or remove the exercise host route. A link-local next hop instead requires an outgoing interface, for example ipv6 route 2001:DB8:20::/64 GigabitEthernet0/0 FE80::2 when that peer address is verified.',
'Expect the preferred /64, host /128, and default to be installed if next hops resolve. The distance-200 /64 is normally not the selected route while the preferred /64 exists. Inspect show ipv6 route for a chosen destination. A remote service outage does not necessarily remove the preferred static; tracking is a separate feature.',
'If the route is absent, verify next-hop reachability and interface state. If return traffic fails, inspect R2 routes and policy. Undo each exact route with its no form, including the distance on the floating route. Do not remove the shared transit addressing as part of route cleanup.')

add('4-01','client','IOS DHCP client on a separate uplink',
'Fresh IOS router CLIENT-R1 Gi0/1 connects to a trusted lab DHCP network 192.0.2.0/24. The authorized server has a valid pool, mask, gateway, and DNS options. This is not the statically addressed client-facing relay interface in the earlier example.',
'''enable
configure terminal
interface GigabitEthernet0/1
 ip address dhcp
 no shutdown
end
show ip interface brief
show dhcp lease''',
'interface enters the uplink context. ip address dhcp requests an address rather than assigning a fixed one. no shutdown enables the link. The server decides the lease/options; the client command does not create a pool. show dhcp lease is a supported IOS DHCP-client inspection command on applicable releases; use the platform equivalent if absent.',
'Expect an assigned address within the authorized pool, not a fixed invented value. Verify lease details, interface state, installed routes, and resolver settings. A lease alone does not prove remote application access.',
'If no lease arrives, inspect link/VLAN, server scope, relay if needed, and DHCP filtering. Restore the original static or DHCP addressing through console access. Removing ip address dhcp can immediately remove management reachability; do not do it on an active remote management path.')

add('4-02','dynamic','Dynamic NAT pool without overload',
'Fresh NAT-capable IOS R1: Gi0/0 192.168.10.1/24 inside, Gi0/1 203.0.113.1/30 outside to upstream 203.0.113.2. Inside clients use R1. R1 has an external route/default; upstream routes 198.51.100.0/24 toward R1. Pool 198.51.100.10-.20 is reserved for this lab. No static mapping or competing NAT rule is configured.',
'''enable
configure terminal
interface GigabitEthernet0/0
 ip nat inside
interface GigabitEthernet0/1
 ip nat outside
exit
ip access-list standard NAT_INSIDE
 permit 192.168.10.0 0.0.0.255
exit
ip nat pool ACADEMY_POOL 198.51.100.10 198.51.100.20 netmask 255.255.255.0
ip nat inside source list NAT_INSIDE pool ACADEMY_POOL
end
show ip nat translations
show ip nat statistics''',
'Interface commands identify inside/outside roles. The standard ACL selects inside source addresses for translation; it is not applied as an interface firewall. ip nat pool defines eleven usable global addresses. The final source-list command binds selection to the pool. Without overload, simultaneous inside hosts consume one-to-one mappings; pool exhaustion can prevent new allocations.',
'Generate approved inside-originated traffic and inspect inside local/global mappings and pool use. Expect globals within .10-.20; do not assume one predetermined allocation order. Verify return routing to the pool. These are expectations, not live outputs.',
'For exhaustion, inspect allocated mappings and policy rather than clearing unrelated sessions. Remove the exact source-list binding before removing an unused pool/ACL; active translations may require planned cleanup on the actual platform. Restore interface roles only if no other rule depends on them.')

add('4-02','pat','PAT as a separate alternative',
'Use a fresh/reset copy of the dynamic-NAT fixture, with the same inside/outside interfaces and routing but no pool binding. Gi0/1 has outside address 203.0.113.1. This is supplementary PAT coverage, not a command to stack blindly onto the dynamic-pool exercise.',
'''enable
configure terminal
ip access-list standard NAT_INSIDE
 permit 192.168.10.0 0.0.0.255
exit
ip nat inside source list NAT_INSIDE interface GigabitEthernet0/1 overload
end
show ip nat translations''',
'The ACL selects inside sources. interface GigabitEthernet0/1 chooses its address as the global representation. overload permits multiple flows to share it using transport identifiers. The fixture still requires the earlier inside/outside role configuration; this alternative block changes the translation rule, not the entire router setup.',
'Generate two inside TCP/UDP flows and inspect distinct translation tuples sharing the interface global address. A PAT mapping does not encrypt data or automatically permit unsolicited inbound services.',
'Remove the exact overload rule with its no form during an approved lab cleanup, then restore the baseline. Do not remove the shared ACL if another rule still references it. Correct route/interface-role/selection errors before assuming port exhaustion.')

add('4-03','server','NTP server and client in an isolated time lab',
'IOS R1 10.0.12.1/30 and R2 10.0.12.2/30 share a working link. R1 has no external source in this deliberately isolated lab. The local-master example is not an accurate UTC reference and must not be advertised to production.',
'''enable
configure terminal
ntp master 8
end
show ntp status''',
'On R1, global ntp master 8 permits its local clock to act as a lab time source at the stated stratum. It does not attach an atomic/GPS clock or prove correct UTC. On R2, configure global ntp server 10.0.12.1, then use show ntp associations and show ntp status. In production, prefer authorized accurate upstream sources with appropriate access/authentication policy.',
'After exchanges and selection, R2 should identify 10.0.12.1 as its selected source if eligible. Synchronization may take time; a displayed wall clock is not proof of selection. Check both source and client, and compare logs only after confirming synchronization.',
'If R2 remains unsynchronized, check UDP 123 reachability, source eligibility, and policy. Undo R2 with no ntp server 10.0.12.1 and R1 with no ntp master. Never leave an inaccurate lab master reachable by production clients.')

add('5-04','port-security','Port security on one endpoint port',
'Isolated Catalyst IOS switch SW1 Fa0/1 connects to exactly one authorized PC in VLAN 10. No phone, AP, hypervisor, or downstream switch shares the port. Capture the original policy and MAC evidence before enabling sticky learning.',
'''enable
configure terminal
interface FastEthernet0/1
 switchport mode access
 switchport access vlan 10
 switchport port-security
 switchport port-security maximum 1
 switchport port-security violation restrict
 switchport port-security mac-address sticky
end
show port-security interface FastEthernet0/1
show port-security address''',
'Port security requires a supported static access-port context here. maximum 1 permits one secure MAC; restrict drops violating traffic and reports supported violation evidence rather than using the shutdown mode. Sticky learning records learned secure addresses in running configuration; saving configuration affects persistence. Learn only from the intended endpoint, not an unknown device.',
'Generate legitimate traffic, inspect the secure MAC and port, then test an approved alternate endpoint in the isolated lab. Expect violation/drop evidence under restrict, not an automatic claim that the port is err-disabled. Review the actual platform output and counters.',
'If a legitimate replacement PC fails, compare the secure MAC with the approved inventory. Remove/replace only the stale secure entry through the supported command, then retest. Undo the lab port-security settings and any generated sticky lines from the captured baseline; disabling the feature does not justify leaving stale saved configuration unreviewed.')

add('5-04','dai','DAI with DHCP clients and one static printer',
'SW1 VLAN 10: Fa0/1 DHCP PC, Fa0/2 static printer 192.168.10.50 MAC 0200.0000.1050. Gi0/1 leads only to authorized gateway/DHCP infrastructure. DHCP snooping is already enabled on VLAN 10, Gi0/1 is snooping-trusted, and the PC binding has been verified. Plan persistence/rate limits before enforcement.',
'''enable
configure terminal
arp access-list ACADEMY_STATIC
 permit ip host 192.168.10.50 mac host 0200.0000.1050
exit
ip arp inspection filter ACADEMY_STATIC vlan 10
interface GigabitEthernet0/1
 ip arp inspection trust
exit
ip arp inspection vlan 10
end
show ip arp inspection vlan 10
show ip arp inspection interfaces
show arp access-list ACADEMY_STATIC''',
'arp access-list enters ARP ACL mode; the permit validates the printer sender binding. filter applies that ACL to VLAN 10. The static keyword is deliberately omitted so unmatched packets can fall back to DHCP snooping bindings; using static would change that behavior. DAI trust is separate from DHCP-snooping trust. Enabling the VLAN starts ingress inspection on untrusted ports.',
'Check PC binding, static printer policy, trusted uplink, and DAI counters. Legitimate DHCP-client and printer ARP should pass; an unauthorized mapping should be rejected in an approved isolated test. Do not invent a universal default rate: inspect the platform and configure a justified limit if required.',
'If the printer fails, verify exact sender IP/MAC and ACL application. If all clients fail after reload, inspect binding persistence before bypassing inspection. In the lab, remove the VLAN enforcement and filter only through a planned restoration, then remove the unused ACL and restore trust policy. Never trust all access ports as a generic repair.')

# A precise pinout comparison is more reliable than an ambiguous connector picture.
A=['white/green','green','white/orange','blue','white/blue','orange','white/brown','brown']
B=['white/orange','orange','white/green','blue','white/blue','green','white/brown','brown']
EXPANSIONS.setdefault('1-04',[]).append(dict(stage='Visualize',block=dict(id='1-04-pinout-reference',type='table',title='T568A and T568B by numbered contact',text='Pin numbers are authoritative. A physical connector illustration must specify its viewing orientation before left/right is inferred. Same scheme at both ends is straight-through. Traditional 10/100 crossover swaps pairs on pins 1/2 and 3/6; gigabit crossover/auto-MDI-X details require the applicable interface standard. Do not untwist more pair length than installation guidance permits.',headers=['Pin','T568A','T568B'],rows=[[str(i+1),a,b] for i,(a,b) in enumerate(zip(A,B))])))
