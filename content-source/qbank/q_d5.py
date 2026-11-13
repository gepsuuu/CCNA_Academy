from qlib import *
D = "d5"

M(D, "5.1", "sec-concepts", 1, "concept", "Match each security term to its definition.",
  [("Vulnerability", "A weakness in a system that could be attacked"), ("Threat", "A potential danger that could exploit a weakness"),
   ("Exploit", "The method or code used to take advantage of a weakness"), ("Mitigation", "A countermeasure that reduces the risk")],
  "Example: unpatched software (vulnerability), an attacker (threat), a crafted packet (exploit), a patch or ACL (mitigation).",
  "Security vocabulary")

S(D, "5.1", "sec-concepts", 2, "scenario",
  "An attacker sends small DNS queries to many open resolvers with the victim's IP as the spoofed source. The resolvers send large responses to the victim. Which attack is this?",
  ["*Reflection and amplification || Spoofing reflects traffic toward the victim, and small queries produce much larger responses.",
   "Man-in-the-middle || A MITM attacker intercepts traffic between two parties.",
   "Brute-force password attack || No credentials are being guessed.",
   "Phishing || Phishing tricks users with deceptive messages."],
  "Reflection uses a spoofed source address; amplification exploits protocols whose responses are larger than requests. Together they create a DDoS.",
  "Reflection/amplification")

M(D, "5.1", "sec-concepts", 1, "concept", "Match each social engineering technique to its description.",
  [("Phishing", "Fraudulent email that tricks users into revealing information"), ("Vishing", "Deception over a voice phone call"),
   ("Smishing", "Deception through SMS text messages"), ("Whaling", "Targeted phishing aimed at senior executives"),
   ("Tailgating", "Following an authorized person through a secured door")],
  "Social engineering targets people rather than technology. User awareness, training and physical access control are the main defenses.",
  "Social engineering")

S(D, "5.1", "sec-concepts", 1, "concept", "Which type of malware spreads across networks by itself without requiring user action?",
  ["*Worm || Worms self-replicate by exploiting vulnerabilities on other systems.",
   "Virus || A virus attaches to files or programs and needs user action to spread.",
   "Trojan horse || A Trojan disguises itself as legitimate software and relies on the user to run it.",
   "Ransomware || Ransomware encrypts data for ransom; spreading is not its defining trait."],
  "Malware types are defined by how they spread and what they do. Worms are especially dangerous because they need no interaction.",
  "Malware types")

S(D, "5.2", "sec-concepts", 2, "concept", "Which items are security program elements listed in the CCNA objectives?",
  ["*User awareness || Making all users aware of threats such as phishing.",
   "*Physical access control || Protecting equipment rooms with badges, locks and cameras.",
   "OSPF authentication || A useful technical control, but not a security program element.",
   "Trunk pruning || A VLAN optimization, not a security program element.",
   "Dynamic NAT || An address translation feature, not a security program element."],
  "The three program elements are user awareness, user training and physical access control.", "Security program elements")

S(D, "5.3", "aaa", 2, "config", "Both 'enable password Cisco1' and 'enable secret Cisco2' are configured. Which password grants privileged EXEC access?",
  ["*Cisco2 || When both exist, IOS uses the enable secret and ignores the enable password.",
   "Cisco1 || The enable password is ignored while an enable secret is configured.",
   "Either password || Only the secret is checked.",
   "Neither; the configuration is rejected || IOS accepts both but warns that they should differ."],
  "Always use 'enable secret', which is stored as a hash.", "enable secret precedence")

TF(D, "5.3", "aaa", 2, "concept", "'service password-encryption' protects passwords with strong, irreversible hashing.", False,
   "It applies weak, reversible type 7 encryption to clear-text passwords in the configuration. Use 'secret' commands for hashed passwords.",
   "Type 7 encryption")

S(D, "5.4", "aaa", 2, "scenario", "Which login method is an example of multifactor authentication?",
  ["*A password plus a code from a phone app || This combines something you know with something you have.",
   "A password plus a four-digit PIN || Both are something you know — one factor.",
   "Two different passwords in a row || Still a single factor type.",
   "A fingerprint scan on its own || One factor (something you are)."],
  "MFA requires factors from different categories: knowledge, possession and inherence.", "MFA")

M(D, "5.8", "aaa", 1, "concept", "Match each AAA function to its purpose.",
  [("Authentication", "Verifies who the user is"), ("Authorization", "Determines what the user is allowed to do"),
   ("Accounting", "Records what the user did and when")],
  "AAA: who are you, what can you do, what did you do.", "AAA")

S(D, "5.8", "aaa", 2, "protocol-id", "Which statements about TACACS+ are true?",
  ["*It uses TCP port 49 || TACACS+ runs over TCP.",
   "*It encrypts the entire packet payload || RADIUS encrypts only the password.",
   "It combines authentication and authorization in one exchange || That describes RADIUS; TACACS+ separates the three AAA functions.",
   "It uses UDP 1812 || UDP 1812 is RADIUS authentication."],
  "TACACS+ is preferred for device administration (per-command authorization and full encryption). RADIUS is common for network access such as 802.1X and VPN.",
  "TACACS+ vs RADIUS")

S(D, "5.8", "aaa", 3, "config", "Which command authenticates logins against TACACS+ but falls back to local accounts if the TACACS+ server is unreachable?",
  ["*aaa authentication login default group tacacs+ local || Methods are tried in order; 'local' is used only if TACACS+ returns an error.",
   "aaa authentication login default local group tacacs+ || This checks local accounts first.",
   "login local || This line command uses only the local database.",
   "aaa fallback local || This is not valid IOS syntax."],
  "Method lists are evaluated left to right. A rejection from the server does not fall through; only an error does.",
  "AAA method lists")

S(D, "5.3", "aaa", 2, "command", "Which command blocks login attempts for 120 seconds after 3 failures within 60 seconds?",
  ["*login block-for 120 attempts 3 within 60 || This enables login quiet-mode against brute-force attempts.",
   "login delay 120 || This adds a delay between attempts but does not count failures.",
   "exec-timeout 120 3 || This sets an idle timeout for sessions.",
   "security passwords min-length 3 || This enforces minimum password length."],
  "Combine login block-for with strong passwords, SSH only and access-class ACLs on VTY lines.", "Login protection")

S(D, "5.6", "acl", 2, "calculation", "Which wildcard mask matches all addresses from 172.16.32.0 through 172.16.47.255?",
  ["*0.0.15.255 || The range is a /20 (block of 16 in the third octet); wildcard = 255.255.255.255 − 255.255.240.0.",
   "0.0.16.255 || Wildcard octets for a contiguous block are always 2^n − 1.",
   "255.255.240.0 || This is the subnet mask, not the wildcard.",
   "0.0.31.255 || This matches 32 third-octet values (172.16.32.0–172.16.63.255)."],
  "Wildcard = all-255s minus the subnet mask. Quick check: the interesting octet equals block size − 1.",
  "Wildcard masks", verify=lambda: wildcard("172.16.32.0/20"))

S(D, "5.6", "acl", 2, "diagram", "Refer to the ACL. Host 10.1.1.20 opens an SSH session (TCP 22) to 10.2.2.10. What happens?",
  ["*It is denied by line 20 || Line 10 permits only port 443; line 20 denies any IP traffic to 10.2.2.10.",
   "It is permitted by line 10 || Line 10 requires destination port 443.",
   "It is permitted by line 30 || Processing stops at the first match, line 20.",
   "It is denied by the implicit deny || An explicit line matches first."],
  "ACLs are processed top-down and stop at the first match; each line must match protocol, addresses and ports.",
  "ACL processing", dg="acl_flow")

S(D, "5.6", "acl", 3, "diagram", "Refer to the ACL. Which flows are permitted?",
  ["*10.1.1.20 to 10.2.2.10 on TCP 443 || Line 10 matches source, destination and port.",
   "*10.9.9.9 to 8.8.8.8 on UDP 53 || Lines 10 and 20 do not match; line 30 permits it.",
   "10.9.9.9 to 10.2.2.10 on TCP 443 || The source is outside 10.1.1.0/24, so line 20 denies it.",
   "10.1.1.20 to 10.2.2.10 using ICMP || Line 10 matches only TCP, so line 20 denies it."],
  "Check every field of each line before moving to the next. A permit for one port does not permit other protocols to the same host.",
  "ACL matching", dg="acl_flow")

S(D, "5.6", "acl", 2, "scenario",
  "Host 10.1.1.50 must be blocked from reaching server 10.3.3.3, and all other traffic must be allowed. Which approach is best?",
  ["*An extended ACL inbound near 10.1.1.50 || It matches source and destination and drops the traffic before it crosses the network.",
   "A standard ACL inbound near 10.1.1.50 || A standard ACL matches only the source, so it would block the host from everything.",
   "An extended ACL outbound on every router interface || One well-placed ACL is enough.",
   "Port security on the server's switch port || Port security restricts MAC addresses, not IP flows."],
  "Place extended ACLs near the source and standard ACLs near the destination.", "ACL placement",
  "Extended → near Source; Standard → near Destination.")

S(D, "5.6", "acl", 2, "troubleshooting", "After this ACL is applied, users lose all connectivity through Gi0/1. Why?",
  ["*There is no permit statement || Every ACL ends with an invisible 'deny any'.",
   "Telnet is required for routing updates || Routing protocols do not use Telnet.",
   "ACL names must be numbers || Named ACLs are fully supported.",
   "Extended ACLs cannot be applied inbound || Extended ACLs can be applied in either direction."],
  "Add 'permit ip any any' (or more specific permits) after the deny.", "Implicit deny",
  ex="ip access-list extended BLOCK-TELNET\n 10 deny tcp any any eq 23\n!\ninterface GigabitEthernet0/1\n ip access-group BLOCK-TELNET in")

S(D, "5.6", "acl", 3, "troubleshooting", "With this ACL applied, is traffic from 192.168.1.66 denied?",
  ["*No; line 1 permits it first || ACLs stop at the first match.",
   "Yes; specific entries are always checked first || IOS does not reorder entries by specificity for you.",
   "Yes; it is denied by the implicit deny || The explicit permit matches first.",
   "It depends on the interface direction || Direction does not change the order of evaluation."],
  "Place specific entries before general ones: deny host 192.168.1.66 first, then permit the subnet.", "ACL order",
  ex="access-list 10 permit 192.168.1.0 0.0.0.255\naccess-list 10 deny host 192.168.1.66")

S(D, "5.6", "acl", 1, "command", "Which command applies standard ACL 5 to restrict who can open SSH sessions to the device's VTY lines?",
  ["*access-class 5 in (under line vty) || access-class filters connections to the device's lines.",
   "ip access-group 5 in (under line vty) || ip access-group is an interface command.",
   "access-list 5 vty || This is not valid syntax.",
   "ip ssh access-list 5 || This is not standard IOS syntax for VTY filtering."],
  "Interfaces use 'ip access-group'; VTY lines use 'access-class'.", "VTY access control")

TF(D, "5.6", "acl", 1, "concept", "Only one IPv4 ACL can be applied to an interface per direction.", True,
   "The rule is one ACL per interface, per direction, per protocol. Applying a new one replaces the old one.", "ACL application rules")

S(D, "5.7", "l2-security", 3, "troubleshooting", "Based on the output, what is happening on Gi0/5?",
  ["*Violations are dropped and logged; the port stays up || Restrict mode drops, logs and counts violations without err-disabling the port.",
   "The port has been err-disabled || Port status 'Secure-up' shows it is operational.",
   "Violating frames are dropped silently without counting || That describes protect mode.",
   "Seven sticky MAC addresses were learned || Only 1 MAC address is allowed and present."],
  "Violation modes: protect (drop silently), restrict (drop + log + count), shutdown (err-disable, the default).",
  "Port security violation modes",
  ex="SW1# show port-security interface gi0/5\nPort Security              : Enabled\nPort Status                : Secure-up\nViolation Mode             : Restrict\nMaximum MAC Addresses      : 1\nTotal MAC Addresses        : 1\nSticky MAC Addresses       : 0\nSecurity Violation Count   : 7")

M(D, "5.7", "l2-security", 2, "concept", "Match each port-security violation mode to its behavior.",
  [("Protect", "Drops offending frames without logging or counting"), ("Restrict", "Drops offending frames, logs and increments the counter"),
   ("Shutdown", "Puts the port into the err-disabled state")],
  "Shutdown is the default and the most disruptive; restrict is common where visibility matters but uptime is important.",
  "Port security modes")

S(D, "5.7", "l2-security", 2, "config", "What are the default settings after entering only 'switchport port-security' on an access port?",
  ["*Maximum of 1 MAC address and violation mode shutdown || These are the IOS defaults.",
   "Maximum of 2 MAC addresses and violation mode restrict || These must be configured explicitly.",
   "Maximum of 1 MAC address and violation mode protect || The default mode is shutdown.",
   "Unlimited MAC addresses with logging only || Port security limits addresses by default."],
  "Port security works on access ports (or static trunks), not on dynamic DTP ports.", "Port security defaults")

S(D, "5.7", "l2-security", 2, "scenario", "A user plugs in a home router that starts handing out 192.168.0.x addresses to colleagues. Which feature prevents this?",
  ["*DHCP snooping || Untrusted ports drop DHCP server messages such as OFFER and ACK.",
   "Dynamic ARP Inspection alone || DAI validates ARP; it does not block rogue DHCP servers.",
   "PortFast || PortFast only speeds up port transitions.",
   "Root Guard || Root Guard protects the STP root placement."],
  "DHCP snooping trusts only uplinks toward legitimate servers and builds the binding table that DAI uses.", "DHCP snooping")

S(D, "5.7", "l2-security", 3, "config",
  "DHCP snooping is enabled globally. The legitimate DHCP server is reached through uplink Gi0/24, and clients are in VLAN 10. Which additional commands are required?",
  ["*ip dhcp snooping vlan 10 || Snooping must be enabled for each VLAN.",
   "*ip dhcp snooping trust (on Gi0/24) || Server replies must be accepted on the uplink.",
   "ip dhcp snooping trust (on every access port) || Trusting user ports defeats the protection.",
   "ip arp inspection vlan 10 || DAI is a separate, optional feature built on top of snooping."],
  "Without trusting the uplink, the switch drops the real server's OFFERs and clients get no addresses.", "DHCP snooping configuration")

S(D, "5.7", "l2-security", 2, "concept", "Dynamic ARP Inspection validates ARP packets on untrusted ports against which information?",
  ["*The DHCP snooping binding table || DAI checks that the IP-to-MAC mapping matches a known binding.",
   "The MAC address table on the switch || The MAC table does not contain IP addresses.",
   "The router's IP routing table || Routes are unrelated to ARP validation.",
   "The CDP neighbor table entries || CDP is unrelated to ARP validation."],
  "DAI prevents ARP spoofing/poisoning, a common man-in-the-middle technique.", "Dynamic ARP Inspection")

S(D, "5.5", "vpn", 2, "scenario", "A branch and headquarters must communicate securely over the internet, and branch hosts must not need any VPN software. Which solution fits?",
  ["*A site-to-site IPsec VPN between the two gateways || The gateways build the tunnel; hosts are unaware of it.",
   "A remote-access VPN client on every branch PC || This requires software on each host.",
   "Clientless SSL VPN || This gives individual users browser-based access to specific applications.",
   "GRE without IPsec || GRE alone provides no encryption."],
  "Site-to-site VPNs connect networks; remote-access VPNs connect individual users.", "Site-to-site VPN")

S(D, "5.5", "vpn", 2, "concept", "Which security services does IPsec provide?",
  ["*Data confidentiality || ESP encrypts the payload.",
   "*Anti-replay protection || Sequence numbers detect replayed packets.",
   "Dynamic routing || IPsec protects traffic but does not exchange routes.",
   "Bandwidth reservation || QoS, not IPsec, manages bandwidth."],
  "IPsec services: confidentiality, integrity, origin authentication and anti-replay.", "IPsec services")

M(D, "5.9", "wireless-security", 2, "protocol-id", "Match each Wi-Fi security generation to its key mechanism.",
  [("WEP", "RC4 with static keys — considered broken"), ("WPA", "TKIP"), ("WPA2", "AES-CCMP"), ("WPA3-Personal", "SAE")],
  "Each generation strengthened protection; WPA3 also requires Protected Management Frames.", "Wireless security protocols")

S(D, "5.9", "wireless-security", 2, "scenario", "Each employee must authenticate to Wi-Fi with their own directory credentials, validated by a RADIUS server. Which option fits?",
  ["*WPA2- or WPA3-Enterprise using 802.1X || Enterprise mode uses EAP with a RADIUS server for per-user authentication.",
   "WPA2-Personal with a shared passphrase || Everyone shares one key with no per-user identity.",
   "An open network with MAC filtering || MAC addresses are easily spoofed and provide no encryption.",
   "WEP with a long key || WEP is insecure regardless of key length."],
  "Personal mode = shared key (PSK/SAE). Enterprise mode = individual credentials via 802.1X/EAP and RADIUS.", "WPA Enterprise")

S(D, "5.10", "wireless-security", 2, "config", "In the WLC GUI, which Layer 2 security settings configure a WLAN for WPA2-Personal?",
  ["*WPA2 policy, AES encryption, PSK key management || This is the standard WPA2-Personal combination.",
   "WPA policy, TKIP encryption, 802.1X key management || This is legacy WPA-Enterprise.",
   "Open security with web authentication || This is a guest portal, not WPA2.",
   "WEP with a 104-bit key || WEP is obsolete and insecure."],
  "On the WLAN's Security tab: Layer 2 = WPA+WPA2 with WPA2 policy, AES (CCMP) cipher and PSK authentication key management.", "WPA2-PSK on a WLC")

S(D, "5.9", "wireless-security", 2, "diagram", "Refer to the diagram. Which 802.1X role does the AP/WLC perform?",
  ["*Authenticator || It relays EAP between the client and the RADIUS server and enforces the result.",
   "Supplicant || The supplicant is the client device.",
   "Authentication server || That is the RADIUS server, such as ISE.",
   "Certificate authority || A CA issues certificates and is not an 802.1X role."],
  "802.1X roles: supplicant (client), authenticator (switch/AP/WLC), authentication server (RADIUS).", "802.1X roles", dg="dot1x_quiz")

TF(D, "5.9", "wireless-security", 2, "concept", "WPA3-Personal uses SAE, which resists offline dictionary attacks against the passphrase.", True,
   "SAE replaces the WPA2 pre-shared key handshake, so capturing the handshake does not allow offline guessing. It also provides forward secrecy.",
   "WPA3 SAE")

S(D, "5.1", "sec-concepts", 2, "concept", "How does an IPS differ from an IDS?",
  ["*An IPS is inline and can block; an IDS only alerts || Being inline lets the IPS drop packets in real time.",
   "An IDS is inline and blocks traffic; an IPS only alerts || The roles are reversed.",
   "Both only log traffic and cannot take action || An IPS can drop traffic.",
   "An IPS replaces the need for firewalls || IPS complements firewalls; NGFWs often integrate both."],
  "Prevention requires being in the traffic path; detection can work from a SPAN copy.", "IPS vs IDS")
