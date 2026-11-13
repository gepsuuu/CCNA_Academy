"""Topology data for the existing Android vector renderer, not background images."""
from diagrams import N,E,G

def register():
 G('packet_web_fixture','Packet walkthrough: exact routed fixture',600,1120,
 [N('pc',30,40,240,110,'PC-A','datalink','192.168.10.10/24'),
  N('dns',330,40,240,110,'DNS','app','192.168.10.53/24'),
  N('sw',180,240,240,110,'SW1 | VLAN 10','datalink','Fa0/1 PC; Fa0/2 DNS'),
  N('r1',180,470,240,110,'R1','network','LAN .10.1; transit .12.1'),
  N('r2',180,700,240,110,'R2','network','transit .12.2; LAN .100.1'),
  N('web',180,930,240,110,'WEB | HTTP 80','app','198.51.100.20/24')],
 [E('pc','sw','Fa0/1'),E('dns','sw','Fa0/2'),E('sw','r1','Gi0/1 -> R1 Gi0/0'),E('r1','r2','10.0.12.0/30'),E('r2','web','198.51.100.0/24')],
 legend=[('datalink','Local Ethernet/VLAN'),('network','Routed hop'),('app','Service endpoint')],
 explanation='Exact authored packet fixture. PC and DNS share VLAN 10 through SW1. SW1 Gi0/1 reaches R1 Gi0/0 192.168.10.1/24. R1 Gi0/1 10.0.12.1/30 reaches R2 Gi0/0 10.0.12.2/30. R2 Gi0/1 198.51.100.1/24 reaches WEB. Arrows identify the depicted delivery relationships, not a ban on return traffic. No NAT is configured. Inspect the device controls for full MAC/interface details. Header changes are shown by the walkthrough, not computed by this diagram.')
 G('vlan_pilot_fixture','VLAN pilot: ports and gateway paths',640,1160,
 [N('a',20,40,260,100,'PC-A | VLAN 10','vlan10','192.168.10.10/24'),
  N('r',360,40,260,100,'R1 | Gi0/0','network','.10.1 and .20.1 gateways'),
  N('s1',190,290,260,110,'SW1','datalink','Fa0/1 A; Gi0/2 R1'),
  N('s2',190,600,260,110,'SW2','datalink','Gi0/1 to SW1 Gi0/1'),
  N('b',20,940,260,110,'PC-B | VLAN 10','vlan10','192.168.10.20/24'),
  N('c',360,940,260,110,'PC-C | VLAN 20','vlan20','192.168.20.20/24')],
 [E('a','s1','Fa0/1 access 10'),E('r','s1','Gi0/2 trunk'),E('s1','s2','Gi0/1 trunk'),E('s2','b','Fa0/1 access 10'),E('s2','c','Fa0/2 access 20')],
 legend=[('vlan10','VLAN 10 broadcast domain'),('vlan20','VLAN 20 broadcast domain'),('network','Routing boundary')],
 explanation='Exact main VLAN pilot fixture, separate from the phone extension. Both trunks permit 10,20,999 and use untagged native VLAN 999. VLAN 10/20 frames are tagged on trunks and ordinary PC frames are untagged on access links. PC-A/PC-B share a broadcast domain; PC-C does not. R1 subinterfaces Gi0/0.10 and .20 provide 192.168.10.1/24 and 192.168.20.1/24. Lines show physical links; arrows are reading guides, not one-way Ethernet. A routed conversation replaces the Layer 2 header at R1; ordinary routing does not translate IP endpoints.')
 G('ospf_pilot_fixture','OSPF pilot: point-to-point area 0',600,900,
 [N('l1',130,30,340,100,'R1 Loopback0','network','192.168.1.1/32; cost 1'),
  N('r1',130,240,340,110,'R1 | ID 1.1.1.1','network','Gi0/0 10.0.12.1/30'),
  N('r2',130,520,340,110,'R2 | ID 2.2.2.2','network','Gi0/0 10.0.12.2/30'),
  N('l2',130,760,340,100,'R2 Loopback0','network','192.168.2.1/32; cost 1')],
 [E('l1','r1','passive advertisement'),E('r1','r2','area 0; p2p; cost 10'),E('r2','l2','passive advertisement')],
 legend=[('network','OSPF-enabled interface or advertised prefix')],
 explanation='Exact OSPF command fixture. Both transit interfaces explicitly use OSPF point-to-point network type and outgoing cost 10, so there is no DR/BDR election. Loopbacks are advertised passively with cost 1. R1 cost to R2 loopback is 10+1=11. Router IDs identify OSPF speakers and are not the transit next-hop addresses. The broadcast DR/BDR discussion is a different scenario, not a role shown on this link. Arrows organize the drawing; Hello/database exchanges and data can travel both directions.')
