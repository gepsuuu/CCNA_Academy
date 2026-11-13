"""Authored, inspectable educational fixtures. Never live device telemetry."""
PC='02:00:00:00:10:10'; DNS='02:00:00:00:10:53'; R1L='02:00:00:01:10:01'; R1T='02:00:00:01:12:01'; R2T='02:00:00:02:12:02'; R2L='02:00:00:02:20:01'; WEB='02:00:00:00:20:20'
CLIENT='192.168.10.10'; RESOLVER='192.168.10.53'; SERVER='198.51.100.20'
DEVICES=[
 dict(name='PC-A',interface='eth0',details='192.168.10.10/24; gateway 192.168.10.1; DNS 192.168.10.53; MAC '+PC),
 dict(name='SW1',interface='Fa0/1, Fa0/2, Gi0/1',details='One VLAN 10: PC-A on Fa0/1, DNS on Fa0/2, R1 on Gi0/1. Forwarding state assumed converged. No routing or NAT.'),
 dict(name='DNS',interface='eth0',details='192.168.10.53/24; MAC '+DNS+'; authored A record portal.academy.example = 198.51.100.20.'),
 dict(name='R1',interface='Gi0/0, Gi0/1',details='Gi0/0 192.168.10.1/24 MAC '+R1L+'; Gi0/1 10.0.12.1/30 MAC '+R1T+'; route 198.51.100.0/24 via 10.0.12.2.'),
 dict(name='R2',interface='Gi0/0, Gi0/1',details='Gi0/0 10.0.12.2/30 MAC '+R2T+'; Gi0/1 198.51.100.1/24 MAC '+R2L+'; return route 192.168.10.0/24 via 10.0.12.1.'),
 dict(name='WEB',interface='eth0',details='198.51.100.20/24; gateway 198.51.100.1; MAC '+WEB+'; plain HTTP port 80 for this isolated demonstration, not HTTPS.')]

def step(id,title,device,interface,action,why,next,headers=None,condition='always'):
 return dict(id=id,title=title,device=device,interface=interface,action=action,why=why,next=next,headers=headers or {},condition=condition)
def arp(sender_ip,target_ip,src,dst,operation):
 return {'EtherType':'ARP','Source MAC':src,'Destination MAC':dst,'ARP operation':operation,'ARP sender IPv4':sender_ip,'ARP target IPv4':target_ip,'IP header':'None: ARP is not an IPv4 packet'}
def ip(src,dst,sm,dm,ttl,proto,**kw):
 return {'EtherType':'IPv4','Source MAC':sm,'Destination MAC':dm,'Source IPv4':src,'Destination IPv4':dst,'TTL':str(ttl),'Protocol':proto,**kw}
STEPS=[
 step('request','Application requests a hostname','PC-A','eth0','The application wants portal.academy.example.','It needs an address before selecting a remote transport endpoint.','Check the DNS cache.'),
 step('cache','Check DNS cache','PC-A','eth0','Inspect the selected cache condition. A warm cache supplies the authored address; a cold cache needs a resolver query.','A valid cache entry avoids this resolver exchange, not the later routing and TCP work.','For a miss, route to the configured local resolver; for a hit, continue to the web destination.'),
 step('dns-route','Route to the DNS resolver first','PC-A','eth0','192.168.10.53 is on-link under /24. The resolver MAC is initially unknown.','DNS itself needs IP routing and local-link delivery; ARP cannot always wait until after DNS.','Broadcast ARP for the resolver.',condition='dnsMiss'),
 step('dns-arp-request','Resolve the local DNS MAC','PC-A','eth0','Send an ARP request on VLAN 10. SW1 learns PC-A and floods eligible VLAN 10 ports.','The client needs the resolver link address, not the web server MAC.','DNS answers the request.',arp(CLIENT,RESOLVER,PC,'ff:ff:ff:ff:ff:ff','request'),'dnsMiss'),
 step('dns-arp-reply','DNS answers ARP','DNS','eth0','Send a unicast ARP reply; SW1 learns the DNS source MAC.','The resolver owns 192.168.10.53 on this link.','PC-A can transmit the DNS query.',arp(RESOLVER,CLIENT,DNS,PC,'reply'),'dnsMiss'),
 step('dns-query','Send the DNS query','PC-A','eth0','Send a UDP query for an A record.','The resolver is local, so the Ethernet destination is DNS, not R1.','The authored resolver answers.',ip(CLIENT,RESOLVER,PC,DNS,64,'UDP',**{'Source port':'53000','Destination port':'53','DNS question':'A portal.academy.example'}),'dnsMiss'),
 step('dns-answer','DNS sends the answer','DNS','eth0','Return the authored A record 198.51.100.20 to PC-A.','This example assumes a successful resolver response and omits its upstream work.','Route to the web destination.',ip(RESOLVER,CLIENT,DNS,PC,64,'UDP',**{'Source port':'53','Destination port':'53000','DNS answer':SERVER}),'dnsMiss'),
 step('web-route','Select the web next hop','PC-A','eth0','198.51.100.20 is outside 192.168.10.0/24; select gateway 192.168.10.1.','The IP destination stays the server; the first Ethernet destination must be the local gateway.','Use the gateway cache or resolve its MAC.'),
 step('gateway-arp-request','Resolve the gateway','PC-A','eth0','Broadcast ARP for 192.168.10.1.','The cold gateway-cache branch needs a next-hop MAC.','R1 replies.',arp(CLIENT,'192.168.10.1',PC,'ff:ff:ff:ff:ff:ff','request'),'gatewayMiss'),
 step('gateway-arp-reply','R1 answers ARP','R1','Gi0/0','Reply with the LAN interface MAC.','R1 owns the selected next-hop address.','The client can send its TCP SYN.',arp('192.168.10.1',CLIENT,R1L,PC,'reply'),'gatewayMiss'),
 step('router-cache-assumption','Inspect remaining assumptions','R1','Gi0/1','R1/R2 next-hop ARP caches and WEB gateway cache are prepopulated in this authored fixture.','This keeps the trace focused without inventing instantaneous unresolved forwarding. Real cold routers would perform their own ARP exchanges.','Follow the same SYN across all three Ethernet links.')]

def traverse(label,src,dst,flags,seq,ack,sp,dp,reverse=False,payload=''):
 if not reverse:
  hops=[('PC-A','eth0',PC,R1L,64),('R1','Gi0/1',R1T,R2T,63),('R2','Gi0/1',R2L,WEB,62)]
 else:
  hops=[('WEB','eth0',WEB,R2L,64),('R2','Gi0/0',R2T,R1T,63),('R1','Gi0/0',R1L,PC,62)]
 for i,(dev,iface,sm,dm,ttl) in enumerate(hops):
  headers=ip(src,dst,sm,dm,ttl,'TCP',**{'Source port':str(sp),'Destination port':str(dp),'TCP flags':flags,'Sequence':str(seq),'Acknowledgment':str(ack),'Payload':payload or 'none'})
  STEPS.append(step(label+'-'+str(i),label+' on link '+str(i+1),dev,iface,
   'Transmit the '+label+' frame. '+('A router has removed the previous Ethernet header, selected the next hop, decremented TTL, and updated the IPv4 header checksum.' if i else 'The endpoint creates the segment and IPv4 packet.'),
   'MAC addresses describe this link. Original IP endpoints and TCP ports are unchanged because no NAT is configured.',
   'Inspect the next link or receiving endpoint.',headers))
traverse('SYN',CLIENT,SERVER,'SYN',1000,0,49152,80)
STEPS.insert(12,step('switch-forward','SW1 forwards the SYN','SW1','Fa0/1 -> Gi0/1','Forward the known unicast toward R1.','SW1 uses VLAN/MAC forwarding and does not decrement IPv4 TTL or replace endpoint MAC addresses.','R1 receives and routes the frame.',ip(CLIENT,SERVER,PC,R1L,64,'TCP',**{'Source port':'49152','Destination port':'80','TCP flags':'SYN','Sequence':'1000','Acknowledgment':'0','Payload':'none'})))
traverse('SYN-ACK',SERVER,CLIENT,'SYN, ACK',5000,1001,80,49152,True)
traverse('ACK',CLIENT,SERVER,'ACK',1001,5001,49152,80)
# Illustrative request length of 120 bytes so the response acknowledgment is exact: 1001 + 120 = 1121.
traverse('HTTP request',CLIENT,SERVER,'PSH, ACK',1001,5001,49152,80,payload='GET / HTTP/1.1, Host: portal.academy.example (illustrative 120 bytes)')
traverse('HTTP response',SERVER,CLIENT,'PSH, ACK',5001,1121,80,49152,True,payload='HTTP/1.1 200 OK with page content (illustrative; length not modeled)')
STEPS.append(step('application','Application data is exchanged','PC-A','eth0','PC-A receives the HTTP response; its TCP acknowledges the received bytes and the browser processes the page.','The response acknowledges 1121 = 1001 + the 120 request bytes. A TCP handshake alone is not proof of application success. Plain HTTP is readable on every link; real HTTPS would add a TLS handshake after TCP and encrypt the HTTP messages, which this lab does not model. With NAT, a translating router would also rewrite IP addresses/ports; this fixture has none.','Review header changes and the cache branches.'))
PACKET=dict(id='web-no-nat',diagramId='packet_web_fixture',title='Follow the Packet: DNS, ARP, routing, TCP, HTTP',
 assumptions='Authored offline walkthrough, not captured traffic or an IOS emulator. IPv4 Ethernet links; no NAT, loss, fragmentation, ACL, or TLS. Client addresses are configured. DNS/gateway cache branches are selectable; router/server ARP caches are prepopulated. Illustrative initial TCP sequences are 1000/5000. MAC addresses are locally administered examples.',devices=DEVICES,steps=STEPS)

OUTPUTS=[]
def output(id,lesson,command,fixture,healthy,abnormal,fields,question,answer):
 OUTPUTS.append(dict(id=id,lessonId=lesson,command=command,fixture=fixture,healthy=healthy,abnormal=abnormal,fields=[dict(label=a,healthy=b,abnormal=c) for a,b,c in fields],question=question,answer=answer,provenance='Authored simulated IOS-style excerpts. No physical device connection. Omitted lines and formatting vary by platform/release.'))
output('vlan','2-01','show vlan brief','SW1 Fa0/1 is Finance VLAN 10; Fa0/2 is Engineering VLAN 20; Gi0/1 is a trunk. VLANs 10/20 exist.',
'VLAN Name                 Status    Ports\n10   FINANCE              active    Fa0/1\n20   ENGINEERING          active    Fa0/2',
'VLAN Name                 Status    Ports\n10   FINANCE              active    Fa0/1, Fa0/2\n20   ENGINEERING          active',
[('Ports','Fa0/2 belongs to VLAN 20.','Fa0/2 is in VLAN 10; the host IP is not automatically changed.'),('Status','active means the VLAN exists and is active.','An active VLAN can still have wrong access membership.')],
'Engineering cannot reach its gateway. What mismatch is visible?','Fa0/2 is assigned to VLAN 10 instead of 20. Verify the physical host port, correct switchport access vlan 20, then check gateway reachability.')
output('trunk','2-02','show interfaces trunk','SW1 Gi0/1 to SW2; native VLAN 999; VLANs 10/20/999 exist and are forwarding in the healthy case.',
'Port   Mode  Encapsulation Status    Native vlan\nGi0/1  on    802.1q        trunking  999\nPort   Vlans allowed on trunk\nGi0/1  10,20,999\nPort   Vlans allowed and active in management domain\nGi0/1  10,20,999\nPort   Vlans in spanning tree forwarding state and not pruned\nGi0/1  10,20,999',
'Port   Mode  Encapsulation Status    Native vlan\nGi0/1  on    802.1q        trunking  999\nPort   Vlans allowed on trunk\nGi0/1  10,999\nPort   Vlans allowed and active in management domain\nGi0/1  10,999\nPort   Vlans in spanning tree forwarding state and not pruned\nGi0/1  10,999',
[('Status','trunking confirms the operational role.','The trunk remains up; VLAN 20 is nevertheless filtered.'),('Allowed list','VLAN 20 is permitted and eligible.','VLAN 20 is missing from the allowed list.')],
'Is this a physical link failure?','Not on this evidence: the link is trunking. Add the required VLAN after checking both ends, then verify allowed/active/forwarding lists.')
output('mac','1-14','show mac address-table','SW1 VLAN 10: PC-A MAC 0200.0000.1010 on Fa0/1. Fa0/2 is another access port; no host mobility is expected.',
'Vlan  Mac Address       Type      Ports\n10    0200.0000.1010    DYNAMIC   Fa0/1',
'Vlan  Mac Address       Type      Ports\n10    0200.0000.1010    DYNAMIC   Fa0/2',
[('VLAN/MAC/port','Source traffic was learned on the intended port.','The MAC was observed on an unexpected port; investigate wiring/mobility/loops.'),('DYNAMIC','Learned from source frames; it can age.','A changed port is not proof of an attack without context.')],
'Does the abnormal entry alone prove a loop?','No. It proves an unexpected learned location in this fixture. Correlate topology, recent moves, and MAC-flap/STP evidence before diagnosing a loop.')
output('interface','1-08','show ip interface brief','R1 Gi0/0 should be 192.168.10.1/24 and operationally up with a connected peer.',
'Interface              IP-Address      OK? Method Status                Protocol\nGigabitEthernet0/0     192.168.10.1    YES manual up                    up',
'Interface              IP-Address      OK? Method Status                Protocol\nGigabitEthernet0/0     192.168.10.1    YES manual administratively down down',
[('Status','up means not administratively disabled and link operational.','administratively down indicates shutdown.'),('Protocol','up supports connected-route installation.','down prevents normal forwarding; no shutdown still requires a working link.')],
'Which configuration action directly addresses the visible cause?','Enter the intended interface and use no shutdown after confirming the change is authorized. Then verify up/up and the connected route; do not assume the cable is healthy before retesting.')
output('route','3-02','show ip route 192.168.20.0','R1 Gi0/0 10.0.12.1/30 to R2 10.0.12.2/30; remote 192.168.20.0/24. No default or alternate matching route in this fixture.',
'S    192.168.20.0/24 [1/0] via 10.0.12.2',
'% Network not in table',
[('Prefix','The remote /24 is installed.','The requested remote network is absent in this authored lookup excerpt.'),('[1/0]','Conventional static distance 1 and metric 0.','A configured but unresolved next hop can prevent installation.')],
'Is adding a DNS server sufficient to repair the missing route?','No. Verify the intended static route and next-hop resolution, install/correct it, and confirm the return route. DNS does not create a forwarding path.')
output('ospf-neighbor','3-03','show ip ospf neighbor','Pilot R1/R2 point-to-point link 10.0.12.0/30; R2 router ID 2.2.2.2. Abnormal case has persistent exchange trouble, not a transient capture.',
'Neighbor ID     Pri State       Dead Time Address     Interface\n2.2.2.2          0 FULL/  -     00:00:33  10.0.12.2  GigabitEthernet0/0',
'Neighbor ID     Pri State       Dead Time Address     Interface\n2.2.2.2          0 EXSTART/ -   00:00:33  10.0.12.2  GigabitEthernet0/0',
[('State','Full means database synchronization for this adjacency.','Persistent ExStart needs MTU/identity/exchange investigation; not one guaranteed cause.'),('Neighbor ID vs Address','ID is 2.2.2.2; reachable neighbor interface is 10.0.12.2.','Do not use the ID as a next hop without a reachable address.')],
'Is 2-Way always a failure?','No. It can be normal between DROTHERs on broadcast networks. This fixture is point-to-point and should form Full; inspect actual network type before applying the broadcast explanation.')
output('ospf-interface','3-03','show ip ospf interface GigabitEthernet0/0','Pilot R1 Gi0/0 10.0.12.1/30, process 1, router ID 1.1.1.1, area 0, explicit cost 10; R2 uses area 0.',
'Internet Address 10.0.12.1/30, Area 0\nProcess ID 1, Router ID 1.1.1.1, Network Type POINT_TO_POINT, Cost: 10',
'Internet Address 10.0.12.1/30, Area 1\nProcess ID 1, Router ID 1.1.1.1, Network Type POINT_TO_POINT, Cost: 10',
[('Area','Both peers use area 0.','R1 area 1 mismatches the stated R2 area 0.'),('Network Type/Cost','Point-to-point has no DR/BDR; explicit cost is 10.','Matching cost is not an adjacency requirement; matching area is.')],
'Which visible mismatch prevents the intended adjacency?','Area 1 on R1 versus area 0 on R2. Restore the intended interface area and verify neighbor formation; changing cost alone does not repair an area mismatch.')
output('stp','2-06','show spanning-tree vlan 10','SW1 is intended root for VLAN 10; SW2 Gi0/1 is root port and Gi0/2 is alternate. Both examples are SW2 port-role excerpts; BKN is inconsistent/blocked.',
'Interface Role Sts Cost\nGi0/1     Root FWD 4\nGi0/2     Altn BLK 4',
'Interface Role Sts Cost\nGi0/1     Desg BKN 4\nGi0/2     Root FWD 4',
[('Altn BLK','Alternate blocking/discarding is normal redundancy.','Do not confuse a healthy alternate with a broken cable.'),('BKN','No inconsistent port in the healthy excerpt.','An inconsistent state requires detail/log investigation; exact reason is not inferred from this excerpt.')],
'Should you disable STP to make both links forward?','No. That can create a Layer 2 loop. Inspect the inconsistent reason, topology, and guard configuration; restore a valid loop-free design.')
output('etherchannel','2-05','show etherchannel summary','SW1 Port-channel1 is a Layer 2 LACP bundle using Gi0/1 and Gi0/2 to the same peer; excerpts omit other groups.',
'Group Port-channel Protocol Ports\n1     Po1(SU)      LACP     Gi0/1(P) Gi0/2(P)',
'Group Port-channel Protocol Ports\n1     Po1(SU)      LACP     Gi0/1(P) Gi0/2(s)',
[('SU','Layer 2 channel in use.','The channel can remain in use with one member.'),('P vs s','P marks a bundled member.','s marks suspended in common IOS legends; inspect the actual legend and member consistency.')],
'Does Po1(SU) prove both links contribute?','No. Gi0/2 is suspended in the abnormal excerpt. Compare member configuration and LACP state; verify both members are bundled after correction.')
output('acl','5-03','show access-lists FINANCE_IN','Pilot ACL inbound on R1 Gi0/0. Test source 192.168.10.10 to server 192.168.20.20 TCP 443; counters are illustrative observations, not generated live.',
'Extended IP access list FINANCE_IN\n10 permit tcp 192.168.10.0 0.0.0.255 host 192.168.20.20 eq 443 (5 matches)\n20 deny ip 192.168.10.0 0.0.0.255 any (2 matches)\n30 permit ip any any',
'Extended IP access list FINANCE_IN\n5 deny ip 192.168.10.0 0.0.0.255 any (5 matches)\n10 permit tcp 192.168.10.0 0.0.0.255 host 192.168.20.20 eq 443\n30 permit ip any any',
[('Sequence','Specific HTTPS permit precedes Finance deny.','Broad deny at sequence 5 shadows the intended permit.'),('Matches','Counters increase when the corresponding rule is hit.','A later permit cannot override an earlier first match.')],
'Why does HTTPS fail in the abnormal case?','Sequence 5 denies the Finance packet before sequence 10 is considered. Correct the ordered policy with recovery access, then test permitted and denied traffic.')

OUTPUT_BY_LESSON={o['lessonId']:o for o in OUTPUTS}
# Some lessons share the same relevant fixture, but no output is represented as a computed live state.
OUTPUT_BY_LESSON['3-01']=next(o for o in OUTPUTS if o['id']=='route')

OSPF_PACKET=dict(id='ospf-p2p-adjacency',diagramId='ospf_pilot_fixture',title='Inspect OSPF neighbor progression',
 assumptions='Authored OSPFv2 point-to-point area 0 progression, not a live routing process. Both interfaces use matching Hello/Dead intervals 10/40, matching authentication policy and compatible MTU. Loopback advertisements and cost 10+1=11 match the pilot. Fields are selected explanatory values, not a complete byte-level packet capture. Packet types may repeat; this is a successful representative sequence, not a promise of exact packet counts.',
 devices=[dict(name='R1',interface='Gi0/0',details='10.0.12.1/30; router ID 1.1.1.1; area 0; outgoing cost 10; point-to-point.'),dict(name='R2',interface='Gi0/0',details='10.0.12.2/30; router ID 2.2.2.2; area 0; outgoing cost 10; point-to-point.')],
 steps=[
 step('down','Down: no valid neighbor information','R1','Gi0/0','No valid Hello has established the neighbor relationship.','Down is a neighbor state, not necessarily a physically down interface.','Receive a valid Hello.'),
 step('hello','Init: receive a Hello','R1','Gi0/0','R1 receives a Hello from R2 that does not yet list R1.','R1 knows R2, but bidirectional recognition is not established.','A subsequent valid Hello lists R1.',{'IP protocol':'89 (not TCP/UDP)','IPv4 destination':'224.0.0.5','IPv4 TTL':'1','OSPF type':'1 Hello','Router ID':'2.2.2.2','Area ID':'0.0.0.0','Hello/Dead':'10/40','Neighbor list':'R1 not yet listed'}),
 step('two-way','2-Way: bidirectional recognition','R1','Gi0/0','R2 lists R1 router ID 1.1.1.1 in a valid Hello.','On this point-to-point link the routers proceed toward adjacency. Broadcast DROTHER pairs can remain 2-Way normally.','Begin database synchronization.',{'OSPF type':'1 Hello','Router ID':'2.2.2.2','Area ID':'0.0.0.0','Neighbor list':'1.1.1.1','DR/BDR election':'Not used on this point-to-point link'}),
 step('exstart','ExStart: negotiate database exchange','R1','Gi0/0','Neighbors establish database-description sequencing and roles.','Persistent ExStart requires evidence such as MTU and router-ID checks, not an automatic single-cause diagnosis.','Exchange database summaries.',{'OSPF type':'2 Database Description','Area ID':'0.0.0.0','Purpose':'Negotiate exchange sequencing; not application data'}),
 step('exchange','Exchange: describe the database','R2','Gi0/0','Neighbors exchange Database Description packets containing LSA header summaries.','A summary identifies available LSAs; it is not necessarily the complete LSA content.','Request missing/newer information if needed.',{'OSPF type':'2 Database Description','Content':'LSA header summaries'}),
 step('loading','Loading: request and receive missing LSAs','R1','Gi0/0','A neighbor requests missing/newer LSAs and receives Link State Updates.','Loading may be brief or unnecessary when nothing is missing.','Acknowledge reliable flooding and complete synchronization.',{'OSPF request type':'3 Link State Request','OSPF response type':'4 Link State Update','Content':'Requested LSAs; exact database omitted'}),
 step('ack','Acknowledge LSA delivery','R2','Gi0/0','Use the protocol acknowledgment behavior for reliable LSA flooding.','Type 5 is a Link State Acknowledgment packet; it is not a TCP ACK.','Reach Full when required database synchronization is complete.',{'OSPF type':'5 Link State Acknowledgment','Transport':'OSPF directly over IPv4 protocol 89'}),
 step('full','Full: adjacency synchronized','R1','Gi0/0','The intended adjacency is Full; SPF derives routes from the synchronized topology.','Full is not proof that every destination/application works. Routing, ACLs, and return paths still matter.','Inspect the remote loopback route.',{'Neighbor state':'FULL/-','Remote router ID':'2.2.2.2','Next-hop interface address':'10.0.12.2','Remote loopback prefix':'192.168.2.1/32','Expected pilot route cost':'11 = transit 10 + remote loopback 1'})])

PILOT_CHECKS={
 '2-01':dict(question='PC-A in VLAN 10 and PC-C in VLAN 20 have working access links but no Layer 3 gateway. What enables their IP conversation?',items=['A routing function with correct gateways and policy','Making both VLAN names identical','Clearing the MAC table repeatedly','Assuming a trunk routes automatically'],correctIndex=0,rationales=['Inter-VLAN communication requires a Layer 3 path and appropriate host gateways.','VLAN names are labels and do not merge forwarding domains.','Clearing learned MACs does not create routing.','A trunk transports VLANs while preserving their separation.']),
 '3-03':dict(question='In the explicit point-to-point pilot, which OSPF neighbor state is the intended synchronized result?',items=['2-Way forever because every OSPF link elects a DR','Full, with no DR/BDR election on this network type','ExStart as the final healthy state','Down after the first valid Hello'],correctIndex=1,rationales=['Point-to-point links do not use the broadcast DR/BDR adjacency rule.','This point-to-point pair should synchronize to Full; DR/BDR is not used.','ExStart negotiates exchange and is not the final synchronized state.','A valid Hello begins neighbor discovery rather than requiring the neighbor to stay Down.'])}
