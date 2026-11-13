from memorize import txt, bul, tab, trick, dia
SUBNET_LESSONS = []
def L(id, title, level, blocks, gen): SUBNET_LESSONS.append(dict(id=id, title=title, level=level, blocks=blocks, generator=gen))
L("bin", "Binary fundamentals", "Beginner", [
  txt("Why binary", "Every IPv4 address and mask is 32 bits. Subnetting decisions happen at bit boundaries, so you need to see an octet as 8 switches."),
  tab("Place values", ["128","64","32","16","8","4","2","1"], [["2^7","2^6","2^5","2^4","2^3","2^2","2^1","2^0"]]),
  trick("Key fact", "8 bits make an octet; the largest octet value is 255 (all ones).")], "bin2dec")
L("conv", "Decimal ↔ binary conversion", "Beginner", [
  txt("Binary → decimal", "Add the place values of the 1 bits. 10110010 = 128 + 32 + 16 + 2 = 178."),
  txt("Decimal → binary", "Subtract the largest place value that fits, write 1, continue. 99: 64 (35 left) → 32 (3) → 2 (1) → 1 (0) → 01100011."),
  trick("Shortcut", "Mostly ones? Subtract zeros from 255: 11111101 = 255 − 2 = 253.")], "dec2bin")
L("mask", "Subnet masks", "Beginner", [
  txt("What a mask does", "Contiguous 1 bits mark the network portion; 0 bits mark the host portion. The mask is applied with a bitwise AND to find the network."),
  tab("Valid mask octets", ["Bits","1","2","3","4","5","6","7","8"], [["Value","128","192","224","240","248","252","254","255"]])], "cidr2mask")
L("cidr", "CIDR notation", "Beginner", [
  txt("Prefix length", "/n counts the network bits. /24 = 255.255.255.0; /20 = 255.255.240.0."),
  trick("Find the octet", "/1-/8 → octet 1, /9-/16 → octet 2, /17-/24 → octet 3, /25-/32 → octet 4.")], "mask2cidr")
L("network", "Network addresses", "Intermediate", [
  txt("Method", "Block = 256 − mask octet. Network = largest multiple of block ≤ address octet; later octets = 0."),
  txt("Example", "192.168.1.200/27 → block 32 → 192 ≤ 200 < 224 → network 192.168.1.192.")], "network")
L("broadcast", "Broadcast addresses", "Intermediate", [
  txt("Method", "Broadcast = network + block − 1 in the interesting octet; later octets = 255."),
  txt("Example", "10.5.66.3/22 → block 4 in 3rd octet → network 10.5.64.0 → broadcast 10.5.67.255.")], "broadcast")
L("range", "Usable host ranges", "Intermediate", [
  txt("Method", "First usable = network + 1; last usable = broadcast − 1 (in the last octet)."),
  trick("Special cases", "/31 point-to-point links use both addresses; /32 is a single host route.")], "hostrange")
L("subnets", "Number of subnets", "Intermediate", [
  txt("Formula", "Subnets = 2^s, where s = bits borrowed (new prefix − original prefix)."),
  txt("Example", "10.0.0.0/8 split into /12s → 2^4 = 16 subnets.")], "subnets")
L("hosts", "Number of usable hosts", "Intermediate", [
  txt("Formula", "Usable hosts = 2^h − 2, h = 32 − prefix."),
  tab("Quick list", ["Prefix","/24","/25","/26","/27","/28","/29","/30"], [["Hosts","254","126","62","30","14","6","2"]])], "hosts")
L("vlsm", "VLSM fundamentals", "CCNA level", [
  bul("Process", ["List requirements largest first", "Pick the smallest prefix with 2^h − 2 ≥ hosts", "Allocate on the next boundary of that block size", "Continue from the next free address"]),
  txt("Example from 192.168.50.0/24", "Sales 100 → /25 .0-.127 • HR 50 → /26 .128-.191 • IT 20 → /27 .192-.223 • WAN 2 → /30 .224-.227"),
  dia("Equal-size split for comparison", "subnetting")], "vlsm")
