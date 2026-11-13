import json
from qlib import *
D = "d6"

M(D, "6.2", "automation", 1, "concept", "Match each activity to the network plane it belongs to.",
  [("Forwarding a frame using the MAC address table", "Data plane"), ("Exchanging OSPF LSAs with a neighbor", "Control plane"),
   ("An administrator logging in with SSH", "Management plane")],
  "The data plane moves user traffic, the control plane builds forwarding information, and the management plane is used to operate the device.",
  "Network planes")

S(D, "6.3", "automation", 2, "scenario", "A Python script retrieves the device inventory from Catalyst Center using its REST API. Which interface is the script using?",
  ["*Northbound interface || Applications talk to the controller through its northbound API.",
   "Southbound interface || The controller uses southbound interfaces to talk to network devices.",
   "Data plane || The data plane forwards user traffic.",
   "Underlay || The underlay is the physical IP transport network."],
  "Picture the controller in the middle: applications above (northbound, usually REST/JSON) and devices below (southbound).",
  "NBI vs SBI", "North = up to apps; south = down to devices.")

S(D, "6.3", "automation", 2, "protocol-id", "Which of these are southbound protocols a controller can use to configure network devices?",
  ["*NETCONF || NETCONF configures devices over SSH using YANG data models.",
   "*OpenFlow || OpenFlow programs forwarding tables on switches.",
   "JSON || JSON is a data format, not a protocol.",
   "YAML || YAML is a data format, typically used by Ansible playbooks.",
   "OAuth || OAuth is an authorization framework, typically for APIs."],
  "Southbound options include NETCONF, RESTCONF, OpenFlow, OpFlex, SSH/CLI and SNMP.", "Southbound interfaces")

TF(D, "6.3", "automation", 2, "concept", "In controller-based networking, user traffic no longer passes through switches; the controller forwards all packets.", False,
   "The controller centralizes control and management, but devices still forward user traffic in their data planes.", "Controller role")

M(D, "6.2", "automation", 3, "protocol-id", "Match each Cisco SD-Access function to the technology that provides it.",
  [("Underlay routing", "IS-IS"), ("Overlay data plane encapsulation", "VXLAN"), ("Overlay control plane", "LISP"), ("Management and automation", "Catalyst Center")],
  "SD-Access builds a fabric: an IS-IS underlay, VXLAN encapsulation, LISP mapping, and Catalyst Center for automation and assurance.",
  "SD-Access architecture")

S(D, "6.2", "automation", 1, "concept", "In a software-defined fabric, what is the underlay?",
  ["*The physical IP transport network || Overlay tunnels are built on top of it.",
   "The virtual tunnels that carry user traffic || That is the overlay.",
   "The controller's northbound API || APIs are not part of the underlay.",
   "The wireless network only || The underlay covers the whole transport network."],
  "Underlay = physical transport; overlay = virtual networks on top; fabric = both managed as one.", "Underlay vs overlay")

S(D, "6.1", "automation", 2, "scenario", "Why does controller-based management reduce configuration errors across 300 switches?",
  ["*Policy is defined centrally and pushed consistently || Automation removes repetitive manual steps.",
   "It removes the need for spanning tree || Controllers do not change Layer 2 loop prevention requirements.",
   "It encrypts the CLI || Encryption is unrelated to consistency.",
   "It disables the management plane on switches || Devices still need management access."],
  "Automation improves consistency, speed and auditability, and frees engineers for design work.", "Benefits of automation")

S(D, "6.4", "ai", 2, "scenario", "A monitoring platform learns typical DHCP onboarding times for each site and alerts when a site deviates. Which kind of AI is this?",
  ["*Predictive AI || It learns a baseline from historical data and flags deviations.",
   "Generative AI || Generative AI creates content such as text or configurations.",
   "A static threshold alarm || A static threshold does not learn per-site baselines.",
   "Rule-based ACL logging || ACL logging records matches; it does not learn behavior."],
  "Predictive AI uses telemetry to build baselines, detect anomalies and forecast problems.", "Predictive AI")

S(D, "6.4", "ai", 2, "scenario", "An engineer uses a generative AI assistant to draft an ACL for a production router. What is the correct practice?",
  ["*Validate it in a lab before deploying || Generative output can be plausible but wrong.",
   "Apply it immediately because AI output is always accurate || Generated configurations can contain errors.",
   "Trust it because the model was trained on vendor documentation || Training data does not guarantee correctness.",
   "Paste the router's credentials into the prompt for better results || Sensitive data should not be shared with AI services."],
  "Use AI to speed up drafting and analysis, but keep humans responsible for validating changes.", "Responsible use of generative AI")

TF(D, "6.4", "ai", 1, "concept", "Supervised machine learning trains models using labeled data.", True,
   "Supervised learning uses labeled examples; unsupervised learning finds patterns in unlabeled data; reinforcement learning learns from rewards.",
   "Machine learning types")

M(D, "6.5", "rest-json", 1, "protocol-id", "Match each CRUD operation to the HTTP method normally used in REST APIs.",
  [("Create", "POST"), ("Read", "GET"), ("Update (replace)", "PUT"), ("Delete", "DELETE")],
  "REST maps CRUD to HTTP verbs. PATCH is used for partial updates.", "CRUD and HTTP methods")

S(D, "6.5", "rest-json", 2, "troubleshooting", "A script calling a controller API receives HTTP 401. What is the most likely cause?",
  ["*Missing or invalid authentication || 401 means not authenticated.",
   "The requested resource does not exist || That is 404.",
   "The server hit an internal error || That is 500.",
   "The request succeeded || Success codes are 2xx."],
  "401 Unauthorized = authentication needed or failed; 403 Forbidden = authenticated but not allowed.", "HTTP status codes")

S(D, "6.5", "rest-json", 1, "protocol-id", "Which HTTP status code indicates that a POST request successfully created a new resource?",
  ["*201 Created || The resource was created, often with its location returned.",
   "200 OK || 200 is generic success, typically for GET.",
   "204 No Content || 204 is success with no response body.",
   "400 Bad Request || 400 is a client error."],
  "2xx = success, 4xx = client error, 5xx = server error.", "HTTP status codes")

S(D, "6.5", "rest-json", 2, "concept", "Which statements describe REST APIs?",
  ["*They are stateless; each request carries all the information the server needs || The server keeps no client session state between calls.",
   "*They use HTTP methods on resources identified by URIs || For example, GET /api/v1/devices.",
   "They require SNMP as the transport || REST normally uses HTTP/HTTPS.",
   "They keep a session state on the server between calls || That would violate the stateless constraint.",
   "They can only return XML || JSON is the most common format; XML and YAML are also possible."],
  "REST constraints: client-server, stateless, cacheable, uniform interface, layered system (and optional code-on-demand).",
  "REST characteristics")

def _valid(t):
    try: json.loads(t); return True
    except Exception: return False

JSON_DOC = '{\n  "device": {\n    "hostname": "SW1",\n    "interfaces": [\n      {"name": "Gi0/1", "enabled": true},\n      {"name": "Gi0/2", "enabled": false}\n    ]\n  }\n}'
def count_objects(o):
    if isinstance(o, dict): return 1 + sum(count_objects(v) for v in o.values())
    if isinstance(o, list): return sum(count_objects(v) for v in o)
    return 0

S(D, "6.7", "rest-json", 3, "calculation", "How many JSON objects does this document contain, including the outermost one?",
  ["*4 || The outer object, the 'device' object and the two interface objects inside the array.",
   "3 || This misses one of the objects.",
   "2 || This counts only the outer and 'device' objects.",
   "5 || The array itself is not an object."],
  "Every { } is an object; [ ] is an array. Count braces, not keys.", "JSON structure",
  "Curly = object, square = array.", ex=JSON_DOC, verify=lambda: str(count_objects(json.loads(JSON_DOC))))

S(D, "6.7", "rest-json", 2, "calculation", "Which statement about this JSON document is correct?",
  ["*'interfaces' is an array containing two objects || Square brackets hold two { } objects.",
   "The values of 'enabled' are strings || true and false are unquoted JSON booleans.",
   "'hostname' has a number value || \"SW1\" is a string.",
   "The document contains two arrays || There is only one array ('interfaces')."],
  "JSON value types: string, number, boolean, null, object and array.", "JSON data types", ex=JSON_DOC)

S(D, "6.7", "rest-json", 1, "concept", "Which of these is valid JSON?",
  ["*{\"vlan\": 10, \"name\": \"SALES\"} || Keys and strings use double quotes, and numbers are unquoted.",
   "{'vlan': 10, 'name': 'SALES'} || JSON does not allow single quotes.",
   "{\"vlan\": 10, \"name\": \"SALES\",} || Trailing commas are not allowed.",
   "{vlan: 10, name: \"SALES\"} || Keys must be quoted strings."],
  "JSON rules: double-quoted keys and strings, commas between members, no trailing commas, no comments.", "JSON syntax",
  verify=lambda: [t for t in ['{"vlan": 10, "name": "SALES"}', "{'vlan': 10, 'name': 'SALES'}", '{"vlan": 10, "name": "SALES",}', '{vlan: 10, name: "SALES"}'] if _valid(t)])

S(D, "6.6", "config-mgmt", 2, "concept", "Which statements about Ansible are true?",
  ["*It is agentless and connects to devices over SSH or APIs || No software is installed on managed devices.",
   "*Playbooks are written in YAML || Plays and tasks are defined in YAML files.",
   "It requires an agent on every managed device || That describes Puppet or Chef.",
   "It describes infrastructure using HCL || HCL is Terraform's language."],
  "Ansible key files: inventory (hosts and groups), playbooks (YAML), modules (units of work). It is written in Python and push-based.",
  "Ansible")

S(D, "6.6", "config-mgmt", 1, "concept", "Which tool uses declarative HCL configuration files and keeps a state file of the resources it manages?",
  ["*Terraform || Terraform compares desired state in .tf files with its state file and applies the differences.",
   "Ansible || Ansible uses YAML playbooks and has no state file.",
   "NETCONF || NETCONF is a device configuration protocol.",
   "Syslog || Syslog collects log messages."],
  "Terraform is infrastructure as code: providers, plan/apply workflow, state file.", "Terraform")

O(D, "6.6", "config-mgmt", 2, "command", "Arrange the typical Terraform workflow steps in order.",
  ["Write the .tf configuration files", "Run terraform init", "Run terraform plan", "Run terraform apply"],
  "'init' downloads providers, 'plan' previews changes, and 'apply' makes them.", "Terraform workflow")

S(D, "6.6", "config-mgmt", 1, "concept", "Which Ansible file lists the managed devices and organizes them into groups?",
  ["*Inventory || The inventory defines hosts, groups and host variables.",
   "Playbook || A playbook defines the tasks to run.",
   "Module || A module is a unit of work called by a task.",
   "State file || State files belong to Terraform."],
  "Example: an inventory group [access_switches] targeted by a playbook that sets NTP and banners.", "Ansible inventory")

TF(D, "6.6", "config-mgmt", 2, "command", "'terraform plan' changes the managed infrastructure.", False,
   "'terraform plan' only previews the changes Terraform would make; 'terraform apply' performs them.", "Terraform plan vs apply")

S(D, "6.5", "rest-json", 2, "concept", "Which HTTP request header commonly carries a token for REST API authentication?",
  ["*Authorization || Token-based APIs expect the token on every stateless request.",
   "Content-Type || Content-Type describes the body format, such as application/json.",
   "Accept || Accept states which response formats the client can handle.",
   "User-Agent || User-Agent identifies the client software."],
  "Some APIs use custom headers (Catalyst Center uses X-Auth-Token), but the idea is the same: credentials accompany each request.",
  "API authentication")

S(D, "6.1", "automation", 1, "concept", "Which statement best compares traditional and controller-based network management?",
  ["*Traditional is per-device; controller-based is centralized policy || The key difference is where control and intent live.",
   "Traditional networks have no control plane || Every traditional device runs its own control plane.",
   "Controller-based networks do not use IP addressing || IP is still used everywhere.",
   "Traditional networks cannot be monitored || SNMP, syslog and similar tools work on traditional networks."],
  "Controllers add a central point for policy, automation, assurance and APIs.", "Traditional vs controller-based")
