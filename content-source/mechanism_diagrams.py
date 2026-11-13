"""Lesson-specific mechanism data for the app's existing vector diagram renderer."""
from diagrams import N,E,G
MECHANISMS={
'1-01':('Component roles on a packet path',[
 ('Endpoint','app','Creates application data'),('Access switch / AP','datalink','Local Ethernet or wireless delivery'),('Router','network','Selects a next hop between networks'),('Firewall / IPS','network','Applies policy and inspection'),('Server','app','Provides the requested service')],
'Each component has a distinct job. A switch forwards within a VLAN; a router connects networks; an access point (AP) bridges wireless clients; a firewall enforces policy. An IPS can inspect/block supported traffic. A controller manages devices but is not automatically in every data path. PoE supplies supported powered devices subject to negotiation and power budget, not unlimited watts per port.'),
'1-02':('Campus and data-center topology choices',[
 ('Campus access','datalink','Endpoints attach here'),('Distribution / collapsed core','network','Policy and aggregation'),('Campus core, if present','network','High-speed resilient transit'),('Alternative: leaf','datalink','Servers attach to leaf switches'),('Alternative: spine','network','Every leaf connects to every spine')],
'The first three nodes describe a three-tier campus; a two-tier design collapses distribution/core. The final two nodes describe a separate spine-leaf alternative, not extra campus tiers. Spine-leaf aims for consistent fabric paths, commonly using routed ECMP. WAN/SOHO/cloud choices depend on scale, failure domains, cost, and operational ownership. Vertical arrows organize the comparison, not a literal five-device cable chain.'),
'1-04':('Cabling and interfaces: physical media and link delivery',[
 ('Electrical transmitter','physical','Copper balanced pairs'),('Copper channel','physical','Category, length, termination matter'),('Receiving interface','physical','Check CRC/input errors, speed, duplex'),('Optical transmitter','physical','Matched wavelength and optics'),('Fiber / optical receiver','physical','Match SMF/MMF and Tx-to-Rx path')],
'The upper three nodes describe copper; the lower two describe a separate optical path. Copper category and channel construction constrain supported rates/distances. Fiber type, wavelength, transceiver support, connector cleanliness, polarity, and optical budget must match. Duplex fiber commonly crosses transmitter to receiver; supported bidirectional optics use a different arrangement. This is a mechanism comparison, not a connector-orientation pinout. Use the numbered T568A/B table for copper contacts.'),
'1-08':('Private addressing is not a security policy',[
 ('Inside host','network','Example 192.168.10.10/24'),('Inside gateway','network','Routes traffic off the LAN'),('Optional NAT boundary','network','Rewrites selected addresses/ports'),('External destination','app','Needs a reachable return path')],
'RFC1918 private ranges are 10.0.0.0/8, 172.16.0.0/12, and 192.168.0.0/16. They are not globally unique public assignments. NAT is common at IPv4 edges but is not encryption or a substitute for firewall policy. A private address can route inside an organization without NAT. Other non-public/special-use ranges exist, so outside RFC1918 does not universally mean public/reachable.'),
'1-11':('Client diagnostics: isolate the failing layer',[
 ('Address and prefix','network','Is the client configured correctly?'),('Local link / gateway','datalink','Can the intended next hop be reached?'),('Remote IP path','network','Do routes and return policy work?'),('Name resolution','app','Does the resolver return the right answer?'),('Application service','app','Port, TLS, and service health')],
'Use ipconfig /all on Windows, ip addr/ip route on Linux, and ifconfig/route -n get default on macOS as applicable. Check resolver configuration with the OS-appropriate tool. A successful ping does not prove DNS, TCP, TLS, or application health; blocked ICMP alone does not prove all traffic fails. The sequence is an evidence ladder, not a claim that every OS uses identical commands.'),
'1-13':('Virtualization: separate compute and forwarding',[
 ('Physical host','physical','CPU, memory, NIC resources'),('Hypervisor and VMs','app','VMs have guest operating systems'),('Containers: separate model','app','Share a host kernel; isolate processes'),('VRF BLUE','network','Independent routing/forwarding table'),('VRF GREEN','network','No automatic route sharing with BLUE')],
'VMs, containers, and VRFs solve different isolation problems. Containers are not simply small VMs. A VRF separates Layer 3 forwarding tables, permitting overlapping addresses in different contexts. A VLAN separates Layer 2 broadcast domains. VRF communication requires explicit routing/policy such as controlled route leaking; it is not created by placing boxes next to each other. Arrows organize the comparison rather than connecting BLUE to GREEN.'),
'2-04':('Discovery is link-local evidence',[
 ('Device A interface','datalink','CDP / LLDP advertisement'),('Direct Layer 2 link','datalink','Neighbor exchange on this link'),('Device B interface','datalink','Neighbor identity and port details'),('Operator verification','app','Compare advertised and physical ports')],
'CDP is Cisco-specific; LLDP is an IEEE discovery protocol. Advertisements describe adjacent devices and interfaces, not end-to-end reachability. They are not normally routed to discover arbitrary remote networks. Check both local and remote port identifiers, platform/capability information, and security policy before enabling discovery on an untrusted edge.'),
'2-05':('EtherChannel: one logical path, multiple members',[
 ('Traffic flows','transport','Flow A and flow B'),('Hash selection','datalink','Supported fields select a member'),('Logical Port-channel','datalink','One interface to routing or STP'),('Physical member 1','physical','One selected flow path'),('Physical member 2','physical','Other flows may use this member')],
'An EtherChannel bundles compatible links to the same logical peer. LACP active/passive negotiates; passive/passive does not initiate. A single flow commonly stays on one member and does not gain the sum of all link rates. A Layer 2 bundle participates in STP as a logical interface; a routed bundle has an IP on the port-channel. The member nodes are alternatives selected by a hash, not serial hops.'),
'2-07':('Wireless control and data paths differ',[
 ('Wireless client','datalink','802.11 association and traffic'),('Access point','datalink','Radio termination and forwarding'),('CAPWAP control to WLC','network','Management/control relationship'),('Central switching option','network','Client data traverses controller'),('Local switching option','network','Client data exits at AP site')],
'The last two nodes are alternative data paths, not a serial chain. Central/local switching depends on architecture and AP mode. CAPWAP control uses UDP 5246 and data commonly uses UDP 5247; security and tunnel details depend on platform. Autonomous, lightweight, FlexConnect, and cloud-managed designs do not have identical control/data paths. Confirm AP/controller compatibility and reachability before assuming a failed client is an RF issue.'),
'2-08':('WLAN policy: SSID to the wired network',[
 ('SSID / WLAN profile','app','The advertised service name'),('Security policy','app','WPA mode, PSK or enterprise AAA'),('Policy / VLAN mapping','datalink','Selects the wired client segment'),('DHCP and gateway','network','Provides usable IP connectivity'),('Permitted application','app','DNS, routing, ACL, service')],
'A visible SSID is not proof that clients can authenticate or obtain an address. Verify security compatibility, the actual WLAN-to-policy/VLAN mapping, VLAN transport, DHCP, and gateway reachability. Controller GUI names differ between AireOS and IOS XE Catalyst 9800. A voice/data or management VLAN must not be guessed from the SSID text. This is a policy dependency chain, not a claim of identical configuration screens.'),
'4-03':('NTP: traceable source and synchronization',[
 ('Reference clock','physical','Stratum 0 reference, not an NTP host'),('Primary time server','app','Stratum 1 when directly referenced'),('Downstream server','app','Example stratum 2 after selection'),('Network device client','network','Example stratum 3 after selection'),('Evidence check','app','Selected association and sync status')],
'Stratum counts distance from a reference, not absolute accuracy. NTP source selection also considers eligibility and quality; it is not simply choose the lowest number. Stratum 16 is unsynchronized. A lab ntp master command does not create an accurate UTC source. Verify selected associations and synchronization, not only the wall-clock display. The diagram shows a representative hierarchy, not guaranteed measured offsets.'),
'5-01':('Risk analysis separates cause from control',[
 ('Asset and exposure','app','What needs protection?'),('Vulnerability','app','A weakness in the environment'),('Threat and exploit path','network','A possible actor/event and method'),('Impact and likelihood','app','Evaluate the resulting risk'),('Controls and residual risk','network','Prevent, detect, respond, recover')],
'A threat is not the same as a vulnerability, and a control does not eliminate every risk. Identify the asset, weakness, plausible event, business impact, and likelihood before selecting layered controls. Prevention, detection, response, and recovery have different jobs. Security awareness, least privilege, patching, backups, and monitoring address different failure modes. Arrows describe an analysis sequence, not an attack procedure.'),
'5-02':('AAA: identity, permissions, and records',[
 ('Access request','app','User/device asks to enter'),('Authentication','app','Verify the claimed identity'),('Authorization','network','Determine allowed actions'),('Accounting','app','Record activity and sessions'),('Recovery access','network','Test approved fallback safely')],
'Authentication asks who; authorization asks what is permitted; accounting records what happened. Local accounts and centralized AAA are different designs. RADIUS commonly uses UDP 1812/1813; TACACS+ commonly uses TCP 49. Their protection and authorization models differ. Preserve a tested console/recovery path before changing AAA. Do not enable a method list that can lock out administrators without validating the intended server and fallback behavior.'),
'6-01':('Controller APIs do not replace the data plane',[
 ('Application / operator','app','Intent, policy, automation request'),('Northbound interface','app','Application-facing API'),('Controller','network','Coordinates supported control/policy'),('Southbound interface','network','Device-facing communication'),('Device forwarding plane','datalink','Forwards traffic using installed state')],
'Northbound/southbound describe the relationship to a controller, not a fixed physical compass direction. The data plane forwards packets; control/management functions determine or configure forwarding behavior. Architectures vary: not every controller is in the user-data path, and not every network has a fully centralized control plane. An API request must be authorized and its actual result verified.'),
'6-02':('AI and machine learning: outputs require engineering validation',[
 ('Approved observations','app','Logs, metrics, inventory with access controls'),('Model inference','app','Prediction, classification, or generation'),('Candidate explanation','app','A hypothesis, not verified truth'),('Engineer and policy review','network','Validate evidence, scope, rollback'),('Controlled change and checks','network','Approve, apply, verify, monitor')],
'Predictive and generative AI have different outputs. Correlation does not establish causation; generated commands can be incorrect or platform-incompatible. Keep sensitive data within approved systems. Validate recommendations against authoritative documentation and actual device state. A human-approved change still needs rollback and postchecks. This app provides educational material and does not autonomously change production devices.')}


# Lessons whose relationships are not a single reading chain get explicit geometry and labelled edges.
# Each entry: (title, nodes[(id,x,y,w,h,label,kind,sub)], edges[(src,dst,label)], legend, explanation)
CUSTOM={
'1-07':('Binary and hexadecimal: one octet, three notations',
 [('dec',90,35,460,110,'Decimal 172','app','Value to convert'),
  ('w',90,235,460,110,'Place weights 128 64 32 16 8 4 2 1','network','172 = 128 + 32 + 8 + 4'),
  ('bin',90,435,460,110,'Binary 1010 1100','datalink','1 under each weight used; keep 8 bits'),
  ('hi',20,635,280,110,'High nibble 1010','datalink','8 + 2 = 10 = hex A'),
  ('lo',340,635,280,110,'Low nibble 1100','datalink','8 + 4 = 12 = hex C'),
  ('hex',90,835,460,110,'Hexadecimal 0xAC','app','Same value: 172 decimal')],
 [('dec','w','select largest weights first'),('w','bin','mark used weights as 1'),('bin','hi','split into 4-bit nibbles'),('bin','lo','split into 4-bit nibbles'),('hi','hex','A'),('lo','hex','C')],
 [('app','Value in a human-readable base'),('network','Arithmetic step'),('datalink','Bit pattern')],
 'Worked example: 172 decimal. Subtract the largest place weight that fits: 172-128=44, 44-32=12, 12-8=4, 4-4=0, so the 128, 32, 8, and 4 bits are 1 and the rest are 0: 10101100. Each hexadecimal digit is exactly four bits (a nibble), so split 1010|1100 and convert each nibble separately: 1010=10=A and 1100=12=C, giving 0xAC. Reverse the steps to convert hex or binary back to decimal. Leading zeros keep the eight-bit width but do not change the value: 00001010 is 10. The same nibble rule makes each IPv6 hex digit represent four of the 128 address bits.'),
'2-09':('Remote management: reachability, SSH, VTY, and recovery',
 [('pc',90,35,460,110,'Admin PC 192.168.10.10/24','app','SSH client'),
  ('gi',90,235,460,110,'R1 Gi0/0 192.168.10.1/24','network','IP reachability must exist first'),
  ('ssh',90,435,460,110,'SSH server on R1','app','hostname + domain -> RSA host key; SSH v2'),
  ('vty',90,635,460,110,'line vty 0 4','app','transport input ssh; login local'),
  ('usr',90,835,460,110,'Local user learner','app','privilege 1 lands in user EXEC (R1>)'),
  ('con',90,1035,460,110,'Console port (separate path)','physical','Out-of-band recovery if VTY/SSH breaks')],
 [('pc','gi','TCP 22 over IP'),('gi','ssh','host key exchange'),('ssh','vty','session uses a VTY line'),('vty','usr','login local checks user')],
 [('app','Management service or credential'),('network','Layer 3 reachability'),('physical','Out-of-band access')],
 'Read top to bottom for an SSH session in the lesson fixture. The PC must first reach 192.168.10.1 over IP; a Layer 2 switch would instead need a management SVI and ip default-gateway for off-subnet admins. SSH needs a hostname, a domain name, and an RSA key pair before ip ssh version 2 can be used. The VTY lines accept only SSH (transport input ssh) and authenticate against the local username database (login local). A privilege 1 user starts in user EXEC; entering privileged EXEC additionally requires an enable secret. The console port is intentionally not connected by an arrow: it is an independent recovery path that keeps working if a VTY change locks out remote access. Telnet sends credentials in clear text and is excluded by the transport restriction.'),
'6-04':('Ansible and Terraform: two configuration-management workflows',
 [('ainv',20,95,280,120,'Ansible inventory','app','Which hosts and groups'),
  ('apb',20,335,280,120,'Playbook tasks (YAML)','app','Desired steps per host'),
  ('amod',20,575,280,120,'Modules push changes','network','Agentless over SSH or API'),
  ('tcfg',340,95,280,120,'Terraform config (HCL)','app','Declared resources'),
  ('tplan',340,335,280,120,'terraform plan','app','Preview: no change yet'),
  ('tapply',340,575,280,120,'terraform apply','network','Provider API calls; state updated'),
  ('ver',90,815,460,120,'Verify actual device state','datalink','show commands, API reads, tests')],
 [('ainv','apb','select hosts'),('apb','amod','run tasks'),('tcfg','tplan','compare with state'),('tplan','tapply','reviewed approval'),('amod','ver','postcheck'),('tapply','ver','postcheck')],
 [('app','Authored definition or preview'),('network','Action that changes devices'),('datalink','Independent verification')],
 'Left column: Ansible. An inventory selects hosts, a YAML playbook lists tasks, and modules push changes over SSH or device APIs without an installed agent on network devices. Right column: Terraform. HCL declares desired resources; terraform plan compares them with recorded state and the provider to preview changes without applying them; terraform apply makes provider API calls and updates the state file, which can contain sensitive data. The two columns are alternatives, not one pipeline. Idempotence depends on the module or provider, not just the tool name. Both workflows end in independent verification because a successful run does not prove the intended operational result.')}

def register():
 for lid,(title,nodes,explanation) in MECHANISMS.items():
  height=70+len(nodes)*200
  G('mechanism_'+lid,title,640,height,
    [N('n'+str(i),90,35+i*200,460,110,label,kind,sub) for i,(label,kind,sub) in enumerate(nodes)],
    [E('n'+str(i),'n'+str(i+1),'read next') for i in range(len(nodes)-1)],
    legend=[('app','Application / operational role'),('network','Layer 3 / control / policy'),('datalink','Layer 2'),('physical','Physical resource')],explanation=explanation)
 for lid,(title,nodes,edges,legend,explanation) in CUSTOM.items():
  assert lid not in MECHANISMS,lid
  height=max(y+h for _,_,y,_,h,*_ in nodes)+35
  G('mechanism_'+lid,title,640,height,[N(*n) for n in nodes],[E(a,b,label) for a,b,label in edges],legend=legend,explanation=explanation)

def mechanism_lessons():
 return sorted(set(MECHANISMS)|set(CUSTOM))
