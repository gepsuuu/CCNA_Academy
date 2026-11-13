from qlib import *
D = "d4"

O(D, "4.6", "dhcp-dns", 1, "protocol-id", "Arrange the DHCP messages exchanged when a client first obtains an address.",
  ["Discover", "Offer", "Request", "Acknowledge"],
  "The client broadcasts Discover, servers reply with Offer, the client broadcasts Request for one offer, and the chosen server confirms with Ack.",
  "DHCP DORA", "D-O-R-A.")

S(D, "4.6", "dhcp-dns", 2, "troubleshooting",
  "Clients in VLAN 10 (gateway R1 Gi0/1, 10.1.10.1/24) receive no IP address. The DHCP server is 10.0.0.50, reachable from R1 through Gi0/2. What fixes the problem?",
  ["*Configure 'ip helper-address 10.0.0.50' on Gi0/1 || The relay must sit on the interface that receives the clients' broadcasts.",
   "Configure 'ip helper-address 10.0.0.50' on Gi0/2 || Gi0/2 faces the server and never receives client broadcasts.",
   "Enable 'ip routing' on R1 || Routers already route IPv4 by default; the problem is broadcast forwarding.",
   "Configure PAT on R1 || NAT does not relay DHCP broadcasts."],
  "Routers do not forward broadcasts. 'ip helper-address' turns DHCP broadcasts into unicasts to the server and sets giaddr so the server picks the right pool.",
  "DHCP relay", "Helper goes where the help is needed — the client side.")

S(D, "4.6", "dhcp-dns", 2, "config",
  "An IOS DHCP pool for 10.1.10.0/24 must never lease 10.1.10.1–10.1.10.20, and clients must receive gateway 10.1.10.1. Which commands achieve these two requirements?",
  ["*ip dhcp excluded-address 10.1.10.1 10.1.10.20 || This global command keeps the range out of every pool.",
   "*default-router 10.1.10.1 (inside the pool) || This gives clients their default gateway.",
   "ip helper-address 10.1.10.1 || This configures a relay, not the pool.",
   "dns-server 10.1.10.1 || This sets the DNS server option, not the gateway.",
   "network 10.1.10.0 0.0.0.255 || The pool's network command uses a subnet mask or prefix length, not a wildcard."],
  "Typical pool: excluded-address (global), then 'ip dhcp pool NAME' with network, default-router, dns-server and lease.",
  "IOS DHCP server")

TF(D, "4.6", "dhcp-dns", 1, "concept", "A DHCP Discover is broadcast because the client has no IP address yet and does not know the server's address.", True,
   "The Discover is sent from 0.0.0.0 to 255.255.255.255 (UDP 68 to 67). A relay is needed when the server is on another subnet.",
   "DHCP Discover")

S(D, "4.3", "dhcp-dns", 1, "protocol-id", "Which DNS record type maps a hostname to an IPv6 address?",
  ["*AAAA || AAAA records hold 128-bit IPv6 addresses.", "A || A records hold IPv4 addresses.",
   "PTR || PTR records map addresses back to names.", "MX || MX records identify mail servers for a domain."],
  "Common records: A (IPv4), AAAA (IPv6), CNAME (alias), MX (mail), PTR (reverse), NS (name server).",
  "DNS record types", "AAAA = 4×A, because IPv6 is 4× longer.")

S(D, "4.3", "dhcp-dns", 2, "protocol-id", "Which statement about DNS transport is correct?",
  ["*Queries use UDP 53; zone transfers use TCP 53 || TCP is also used when responses are too large for UDP.",
   "DNS uses only TCP 53 for every query || Most queries use UDP for speed.",
   "DNS uses UDP 67 for queries and replies || UDP 67 belongs to DHCP servers.",
   "DNS uses only TCP 443 for all lookups || DNS over HTTPS exists, but standard DNS uses port 53."],
  "DNS is a classic 'both' protocol: UDP for small query/response exchanges, TCP for zone transfers and large answers.",
  "DNS transport")

M(D, "4.1", "nat", 2, "concept", "Match each NAT term to its definition.",
  [("Inside local", "The real IP address of an inside host, usually private"),
   ("Inside global", "The public address that represents an inside host to the outside"),
   ("Outside global", "The real IP address of an outside host"),
   ("Outside local", "The address of an outside host as seen from the inside network")],
  "Inside/outside describes where the host is; local/global describes which side of the router the address is seen from.",
  "NAT terminology")

S(D, "4.1", "nat", 2, "diagram", "Refer to the diagram. What is the inside global address and port for PC2's session?",
  ["*203.0.113.2:51001 || PC2's private socket 192.168.1.11:51000 is translated to the shared public address on port 51001.",
   "192.168.1.11:51000 || This is PC2's inside local address.",
   "198.51.100.80:443 || This is the outside global address of the web server.",
   "203.0.113.2:51000 || This translation is already used by PC1."],
  "In the translation table, the inside global column shows how an inside host appears on the internet.",
  "Reading NAT translations", dg="nat_pat")

S(D, "4.1", "nat", 3, "diagram", "Refer to the diagram. Why did the router change PC2's source port from 51000 to 51001?",
  ["*To keep each PAT translation unique || PAT changes the source port when needed to avoid collisions.",
   "To randomize ports for security || PAT keeps the original port when it is free.",
   "Because port 51000 is reserved on the internet || 51000 is an ordinary dynamic port.",
   "Because the web server requires odd port numbers || Servers do not impose such requirements."],
  "PAT identifies sessions by public IP + port. When two inside hosts use the same source port, PAT assigns a different one.",
  "PAT port translation", dg="nat_pat")

S(D, "4.1", "nat", 3, "troubleshooting", "No translations appear after this configuration. Gi0/0 is the LAN (192.168.1.1/24) and Gi0/1 faces the ISP. What is wrong?",
  ["*Inside/outside roles are reversed || The LAN must be 'ip nat inside' and the ISP-facing interface 'ip nat outside'.",
   "The ACL wildcard mask is incorrect || 0.0.0.255 correctly matches 192.168.1.0/24.",
   "The 'overload' keyword is missing || 'overload' is present.",
   "PAT requires a NAT pool || PAT can overload the outside interface address."],
  "Mark interfaces correctly, then verify with 'show ip nat translations' and 'show ip nat statistics'.",
  "NAT interface roles",
  ex="interface GigabitEthernet0/0\n ip address 192.168.1.1 255.255.255.0\n ip nat outside\ninterface GigabitEthernet0/1\n ip address 203.0.113.2 255.255.255.252\n ip nat inside\n!\naccess-list 1 permit 192.168.1.0 0.0.0.255\nip nat inside source list 1 interface GigabitEthernet0/1 overload")

S(D, "4.1", "nat", 1, "scenario", "Web server 192.168.1.10 must always be reachable from the internet at 203.0.113.10. Which NAT type is appropriate?",
  ["*Static NAT || A permanent one-to-one mapping allows inbound connections at any time.",
   "Dynamic NAT with a pool || Pool mappings exist only while the inside host has an active session.",
   "PAT on the outside interface || PAT mainly supports outbound sessions; inbound access needs static mappings.",
   "No translation is needed || A private address is not routable on the internet."],
  "Configure: ip nat inside source static 192.168.1.10 203.0.113.10.", "Static NAT")

TF(D, "4.1", "nat", 2, "concept", "Dynamic NAT without overload can run out of addresses when more inside hosts need translation than the pool contains.", True,
   "Each inside host needs its own pool address. When the pool is exhausted, new translations fail until entries time out — which is why PAT (overload) is common.",
   "Dynamic NAT limits")

S(D, "4.2", "mgmt-services", 2, "scenario", "A router synchronizes its clock to an NTP server that reports stratum 2. What stratum does the router report?",
  ["*3 || A client is one stratum further from the reference clock than its server.",
   "2 || The router cannot share its server's stratum.",
   "1 || Stratum 1 servers are directly attached to a reference clock.",
   "0 || Stratum 0 is the reference clock itself (GPS or atomic)."],
  "Lower stratum means closer to the reference source. Each hop adds one.", "NTP stratum")

S(D, "4.2", "mgmt-services", 1, "scenario", "During an incident review, syslog timestamps from different switches do not line up. What should be implemented?",
  ["*NTP on all devices || A common time source makes logs from different devices comparable.",
   "SNMPv3 || SNMP monitors devices; it does not synchronize clocks.",
   "Higher logging severity || Severity filters messages; it does not fix timestamps.",
   "CDP || CDP discovers neighbors and does not set time."],
  "Configure 'ntp server <ip>', set the time zone, and add 'service timestamps log datetime msec'.", "NTP purpose")

S(D, "4.4", "mgmt-services", 2, "protocol-id", "Which statements about SNMP are correct?",
  ["*Managers poll agents on UDP 161 || Get/Set requests go to the agent on port 161.",
   "*SNMPv3 can provide both authentication and encryption || The authPriv security level adds privacy (encryption).",
   "Traps must be acknowledged by the manager || Traps are unacknowledged; Informs are acknowledged.",
   "SNMPv2c encrypts the community string || v1 and v2c send community strings in clear text."],
  "SNMP: manager ↔ agent, data organized in MIBs and identified by OIDs. Use SNMPv3 for security.", "SNMP")

S(D, "4.5", "mgmt-services", 2, "protocol-id", "A router logs '%OSPF-5-ADJCHG: Process 1, Nbr 2.2.2.2 on Gi0/1 from LOADING to FULL'. What is the severity of this message?",
  ["*Level 5, Notification || The number between the dashes is the severity; 5 = Notification.",
   "Level 5, Warning || Warning is level 4.",
   "Level 3, Error || The message shows 5, not 3.",
   "Level 6, Informational || Informational is level 6."],
  "Syslog format: %FACILITY-SEVERITY-MNEMONIC. Severities: 0 Emergency, 1 Alert, 2 Critical, 3 Error, 4 Warning, 5 Notification, 6 Informational, 7 Debugging.",
  "Syslog message format")

S(D, "4.5", "mgmt-services", 3, "config", "With 'logging trap warnings' configured, which messages are sent to the syslog server?",
  ["*Severity 0 through 4 || Choosing a level includes it and every more severe (lower-numbered) level.",
   "Only severity 4 || A logging level is a threshold, not a single level.",
   "Severity 4 through 7 || Higher numbers are less severe and are excluded.",
   "All messages || Notification, Informational and Debugging are filtered out."],
  "Lower number = more severe. 'logging trap 4' sends Emergency, Alert, Critical, Error and Warning.",
  "Syslog levels", "Every Awesome Cisco Engineer Will Need Ice cream Daily (0–7).")

O(D, "4.5", "mgmt-services", 2, "protocol-id", "Arrange the syslog severity levels from most severe (0) to least severe (7).",
  ["Emergency", "Alert", "Critical", "Error", "Warning", "Notification", "Informational", "Debugging"],
  "Syslog severities run from 0 (Emergency) to 7 (Debugging).", "Syslog severity order")

S(D, "4.7", "qos", 1, "protocol-id", "Which DSCP marking is recommended for voice bearer traffic?",
  ["*EF (DSCP 46) || Expedited Forwarding is designed for low-delay, low-jitter traffic such as voice.",
   "AF41 || AF41 is commonly used for interactive video.",
   "CS1 || CS1 is commonly used for low-priority scavenger traffic.",
   "DSCP 0 || 0 is default best effort."],
  "DSCP is 6 bits in the IP header. Voice is marked EF so every hop can give it priority queuing.", "DSCP markings")

S(D, "4.7", "qos", 2, "scenario", "An ISP drops traffic above the contracted 50 Mbps. What should the customer configure outbound to avoid those drops?",
  ["*Shaping to 50 Mbps || Shaping buffers bursts and sends them at the contracted rate.",
   "Policing to 50 Mbps || Policing drops the excess itself — the same result as the ISP's drops.",
   "WRED || WRED manages queue depth; it does not hold traffic to a rate.",
   "Marking with EF || Marking alone does not limit the sending rate."],
  "Providers commonly police; customers shape to match. Shaping adds delay but avoids drops.",
  "Policing vs shaping", "Police stop you; shapers make you wait.")

S(D, "4.7", "qos", 3, "concept", "Which statements describe Low Latency Queuing (LLQ)?",
  ["*It provides a strict-priority queue for delay-sensitive traffic such as voice || Priority traffic is always sent first.",
   "*The priority queue is policed so it cannot starve the other queues || Traffic above the configured priority rate is dropped during congestion.",
   "It marks packets with DSCP values || Marking is a separate QoS tool.",
   "It randomly drops packets before a queue fills || That is WRED."],
  "LLQ = CBWFQ (bandwidth guarantees per class) plus a policed strict-priority queue.", "LLQ")

M(D, "4.7", "qos", 2, "concept", "Match each QoS tool to what it does.",
  [("Classification", "Identifies traffic by ACL, NBAR or existing markings"), ("Marking", "Writes a CoS or DSCP value into the header"),
   ("Queuing", "Schedules packets on a congested interface"), ("Policing", "Drops or re-marks traffic that exceeds a rate"),
   ("Shaping", "Buffers excess traffic and sends it later")],
  "QoS tools work together: classify → mark → queue → avoid congestion → police/shape.", "QoS tools")

TF(D, "4.7", "qos", 1, "concept", "QoS creates additional bandwidth on a congested link.", False,
   "QoS manages existing bandwidth by prioritizing, protecting and limiting traffic classes; it never adds capacity.", "QoS purpose")

S(D, "4.8", "ssh-files", 2, "command", "Which global command forces a Cisco device to accept only SSH version 2 connections?",
  ["*ip ssh version 2 || This disables SSHv1.",
   "transport input ssh2 || This is not valid syntax; 'transport input' controls which protocols are allowed on lines.",
   "crypto key generate rsa version 2 || Key generation has no version option.",
   "ssh server v2 || This is not valid IOS syntax."],
  "SSHv2 requires RSA keys of at least 768 bits; 2048 bits is recommended.", "SSH version")

S(D, "4.9", "ssh-files", 1, "protocol-id", "Which statement correctly compares FTP and TFTP?",
  ["*FTP: TCP with login; TFTP: UDP without login || TFTP (UDP 69) is minimal; FTP (TCP 20/21) has logins and directory operations.",
   "TFTP uses TCP 21 and FTP uses UDP 69 || The transports and ports are swapped.",
   "Both encrypt files in transit || Neither encrypts; use SCP or SFTP for encryption.",
   "TFTP supports directory listings and FTP does not || The opposite is true."],
  "Use TFTP only on trusted management networks for simple image and configuration copies.", "FTP vs TFTP")
