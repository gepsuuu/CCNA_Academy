package com.ccnaacademy.app.data

import kotlin.random.Random

/** A generated practice problem with a fully worked solution. */
data class Problem(
    val type: String, val prompt: String, val answer: String, val accepted: List<String>,
    val options: List<String>, val steps: List<String>, val typable: Boolean = true
) {
    fun isCorrect(input: String): Boolean {
        val n = norm(input)
        return n.isNotEmpty() && (accepted + answer).any { norm(it) == n }
    }
    companion object {
        fun norm(s: String) = s.trim().lowercase().replace(" ", "").removePrefix("/").trimEnd('.')
    }
}

object Gen {
    val TYPES = linkedMapOf(
        "bin2dec" to "Binary → decimal", "dec2bin" to "Decimal → binary", "hex" to "Hex ↔ decimal",
        "cidr2mask" to "Prefix → mask", "mask2cidr" to "Mask → prefix", "network" to "Network address",
        "broadcast" to "Broadcast address", "hostrange" to "First / last usable host", "hosts" to "Usable hosts",
        "subnets" to "Number of subnets", "vlsm" to "Prefix for N hosts (VLSM)", "wildcard" to "Wildcard mask",
        "private" to "Private / special address", "ports" to "Port numbers"
    )
    val LEVELS = listOf("Beginner", "Intermediate", "CCNA level")
    /** Generator mix used by the timed subnetting challenge at each level. */
    val CHALLENGE_MIX = listOf(
        listOf("bin2dec", "dec2bin", "cidr2mask", "hosts", "network"),
        listOf("cidr2mask", "mask2cidr", "network", "broadcast", "hostrange", "hosts", "wildcard"),
        listOf("network", "broadcast", "hostrange", "subnets", "vlsm", "wildcard", "mask2cidr", "private")
    )

    private val PLACE = listOf(128, 64, 32, 16, 8, 4, 2, 1)
    private val MASK_OCTETS = listOf(128, 192, 224, 240, 248, 252, 254, 255)

    fun ip(v: Long) = "${(v shr 24) and 255}.${(v shr 16) and 255}.${(v shr 8) and 255}.${v and 255}"
    fun maskOf(p: Int): Long { val q = p.coerceIn(0, 32); return if (q == 0) 0L else (0xFFFFFFFFL shl (32 - q)) and 0xFFFFFFFFL }
    fun bin8(v: Int) = v.toString(2).padStart(8, '0')
    private fun bitsSum(v: Int) = PLACE.filter { v and it != 0 }

    private fun prefixFor(level: Int, r: Random) = when (level) {
        0 -> r.nextInt(24, 31)          // 4th octet only
        1 -> r.nextInt(17, 31)          // 3rd and 4th octets
        else -> r.nextInt(9, 31)        // 2nd, 3rd, 4th octets
    }
    private fun randomIp(r: Random): Long {
        val first = listOf(10, 172, 192, r.nextInt(11, 224)).random(r)
        val second = when (first) { 172 -> r.nextInt(16, 32); 192 -> 168; else -> r.nextInt(0, 256) }
        return (first.toLong() shl 24) or (second.toLong() shl 16) or (r.nextInt(0, 256).toLong() shl 8) or r.nextInt(1, 255).toLong()
    }

    /** Explains the block-size method for an address/prefix. */
    /** Public step-by-step explanation used by the tutor (offline mode). Returns null if the input is not a.b.c.d/n. */
    fun explainSubnet(input: String): List<String>? {
        val m = Regex("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\s*/\\s*(\\d{1,2})").find(input) ?: return null
        val o = m.groupValues.drop(1).take(4).map { it.toInt() }
        val p = m.groupValues[5].toInt()
        if (o.any { it > 255 } || p !in 1..32) return null
        val addr = (o[0].toLong() shl 24) or (o[1].toLong() shl 16) or (o[2].toLong() shl 8) or o[3].toLong()
        if (p >= 31) return listOf("/$p: mask ${ip(maskOf(p))}.", if (p == 31) "A /31 has two addresses, both usable on point-to-point links (RFC 3021)." else "A /32 identifies a single host (host route).")
        val (_, _, steps) = blockSteps(addr, p)
        return listOf("Address ${ip(addr)}/$p.", "Subnet mask ${ip(maskOf(p))}; wildcard ${ip(maskOf(p).inv() and 0xFFFFFFFFL)}.",
            "Host bits = 32 − $p = ${32 - p}.") + steps
    }

    private fun blockSteps(addr: Long, p: Int): Triple<Long, Long, List<String>> {
        val m = maskOf(p); val net = addr and m; val bc = net or (m.inv() and 0xFFFFFFFFL)
        val octet = (p - 1) / 8 + 1
        val shift = (4 - octet) * 8
        val maskOct = ((m shr shift) and 255).toInt()
        val block = 256 - maskOct
        val val0 = ((addr shr shift) and 255).toInt()
        val steps = listOf(
            "/$p → mask ${ip(m)}; interesting octet is octet $octet (mask value $maskOct).",
            "Block size = 256 − $maskOct = $block.",
            "Octet $octet of the address is $val0. Largest multiple of $block ≤ $val0 is ${val0 / block * block}.",
            "Network = ${ip(net)} (octets after octet $octet set to 0).",
            "Broadcast = next network − 1 = ${ip(bc)} (octets after octet $octet set to 255).",
            if (p <= 30) "Usable hosts: ${ip(net + 1)} – ${ip(bc - 1)} (${(1L shl (32 - p)) - 2} hosts)." else "/$p has no separate network/broadcast for hosts."
        )
        return Triple(net, bc, steps)
    }

    private fun ipDistractors(correct: Long, p: Int, r: Random): List<String> {
        val octet = (p - 1) / 8 + 1; val shift = (4 - octet) * 8
        val block = (256 - ((maskOf(p) shr shift) and 255)).toLong() shl shift
        val c = listOf(correct + block, correct - block, correct + 1, correct - 1, correct xor (1L shl shift), correct + (block / 2).coerceAtLeast(1))
        return c.filter { it in 0..0xFFFFFFFFL && it != correct }.map(::ip)
    }

    private fun finish(type: String, prompt: String, answer: String, wrong: List<String>, steps: List<String>,
                       accepted: List<String> = emptyList(), typable: Boolean = true, r: Random): Problem {
        val opts = (wrong.distinct().filter { Problem.norm(it) != Problem.norm(answer) }.shuffled(r).take(3) + answer).shuffled(r)
        return Problem(type, prompt, answer, accepted, opts, steps, typable)
    }

    fun make(type: String, level: Int, r: Random = Random.Default): Problem = when (type) {
        "bin2dec" -> {
            val v = if (level == 0 && r.nextBoolean()) MASK_OCTETS.random(r) else r.nextInt(1, 256)
            val parts = bitsSum(v)
            finish(type, "Convert ${bin8(v)} to decimal.", "$v",
                listOf(v + 1, v - 1, v xor 64, v xor 16, 255 - v).filter { it in 0..255 }.map { "$it" },
                listOf("Write place values 128 64 32 16 8 4 2 1 above the bits.", "Add the values where the bit is 1: ${parts.joinToString(" + ")} = $v.",
                    if (bin8(v).count { it == '1' } >= 5) "Shortcut: 255 − (zero-bit values) = 255 − ${255 - v} = $v." else "Nibble shortcut: ${v / 16} × 16 + ${v % 16} = $v."), r = r)
        }
        "dec2bin" -> {
            val v = r.nextInt(1, 256)
            var rem = v
            val steps = mutableListOf<String>()
            PLACE.forEach { pv -> if (rem >= pv) { steps += "$rem ≥ $pv → bit 1, remainder ${rem - pv}"; rem -= pv } }
            finish(type, "Convert $v to 8-bit binary.", bin8(v),
                listOf(bin8(v xor 1), bin8(v xor 2), bin8(v xor 128), bin8((v shl 1) and 255), bin8(v xor 8)),
                listOf("Subtract place values from the left (128 → 1).") + steps + "Result: ${bin8(v)}", r = r)
        }
        "hex" -> {
            val v = r.nextInt(16, 256)
            val h = v.toString(16).uppercase().padStart(2, '0')
            if (r.nextBoolean())
                finish(type, "Convert hex 0x$h to decimal.", "$v", listOf(v + 16, v - 16, v + 1, (v % 16) * 16 + v / 16).filter { it in 0..255 && it != v }.map { "$it" },
                    listOf("First digit ${h[0]} = ${v / 16} → ${v / 16} × 16 = ${v / 16 * 16}.", "Second digit ${h[1]} = ${v % 16}.", "Total ${v / 16 * 16} + ${v % 16} = $v.",
                        "Binary check: ${bin8(v).take(4)} ${bin8(v).takeLast(4)}."), accepted = listOf("$v"), r = r)
            else
                finish(type, "Convert decimal $v to hexadecimal.", h, listOf((v + 1) % 256, (v + 16) % 256, (v xor 0x0F), ((v % 16) * 16 + v / 16)).filter { it != v }
                    .map { it.toString(16).uppercase().padStart(2, '0') },
                    listOf("$v ÷ 16 = ${v / 16} remainder ${v % 16}.", "${v / 16} → ${h[0]}, ${v % 16} → ${h[1]}.", "Answer: $h (0x$h)."), accepted = listOf("0x$h"), r = r)
        }
        "cidr2mask" -> {
            val p = when (level) { 0 -> r.nextInt(24, 31); 1 -> r.nextInt(16, 31); else -> r.nextInt(8, 31) }
            val octet = (p - 1) / 8 + 1; val bits = p - (octet - 1) * 8
            finish(type, "What is the subnet mask for /$p?", ip(maskOf(p)), listOf(ip(maskOf(p + 1)), ip(maskOf(p - 1)), ip(maskOf(p + 4)), ip(maskOf(p - 2))),
                listOf("/$p = ${(octet - 1) * 8} full bits + $bits bits in octet $octet.", "Mask octet values: 128 192 224 240 248 252 254 255 → $bits bits = ${MASK_OCTETS[bits - 1]}.",
                    "Mask: ${ip(maskOf(p))}."), r = r)
        }
        "mask2cidr" -> {
            val p = when (level) { 0 -> r.nextInt(24, 31); 1 -> r.nextInt(16, 31); else -> r.nextInt(8, 31) }
            finish(type, "What prefix length is ${ip(maskOf(p))}?", "/$p", listOf("/${p + 1}", "/${p - 1}", "/${p + 8}", "/${p - 8}").filter { it.drop(1).toInt() in 1..32 },
                listOf("Count 8 for each 255 octet.", "Convert the partial octet: number of leading 1s in ${bin8(((maskOf(p) shr ((4 - ((p - 1) / 8 + 1)) * 8)) and 255).toInt())}.", "Total = /$p."),
                accepted = listOf("$p"), r = r)
        }
        "network", "broadcast" -> {
            val p = prefixFor(level, r); val a = randomIp(r)
            val (net, bc, steps) = blockSteps(a, p)
            val target = if (type == "network") net else bc
            finish(type, if (type == "network") "What is the network address of ${ip(a)}/$p?" else "What is the broadcast address of ${ip(a)}/$p?",
                ip(target), ipDistractors(target, p, r) + ip(if (type == "network") bc else net), steps, r = r)
        }
        "hostrange" -> {
            val p = prefixFor(level, r).coerceAtMost(30); val a = randomIp(r)
            val (net, bc, steps) = blockSteps(a, p)
            val first = r.nextBoolean()
            val target = if (first) net + 1 else bc - 1
            finish(type, "What is the ${if (first) "FIRST" else "LAST"} usable host of ${ip(a)}/$p?", ip(target),
                listOf(ip(net), ip(bc), ip(if (first) bc - 1 else net + 1)) + ipDistractors(target, p, r), steps, r = r)
        }
        "hosts" -> {
            val p = when (level) { 0 -> r.nextInt(24, 31); 1 -> r.nextInt(18, 31); else -> r.nextInt(10, 31) }
            val h = 32 - p; val ans = (1L shl h) - 2
            finish(type, "How many usable hosts are in a /$p subnet?", "$ans",
                listOf((1L shl h), (1L shl h) - 1, (1L shl (h - 1)) - 2, (1L shl (h + 1)) - 2).map { "$it" },
                listOf("Host bits h = 32 − $p = $h.", "Usable = 2^$h − 2 = ${1L shl h} − 2 = $ans.", "Subtract 2 for the network and broadcast addresses."), r = r)
        }
        "subnets" -> {
            val base = when (level) { 0 -> 24; 1 -> listOf(16, 20, 22, 24).random(r); else -> listOf(8, 12, 16, 19, 20).random(r) }
            val borrow = r.nextInt(1, if (level == 0) 5 else 7).coerceAtMost(30 - base)
            if (r.nextBoolean()) {
                val np = base + borrow
                finish(type, "How many /$np subnets fit in a /$base?", "${1 shl borrow}", listOf("${1 shl (borrow + 1)}", "${(1 shl borrow) - 2}", "${borrow * 2}", "${1 shl (borrow - 1).coerceAtLeast(1)}"),
                    listOf("Borrowed bits s = $np − $base = $borrow.", "Subnets = 2^$borrow = ${1 shl borrow}."), r = r)
            } else {
                val need = r.nextInt((1 shl (borrow - 1)) + 1, (1 shl borrow) + 1)
                val np = base + borrow
                finish(type, "You need $need subnets from a /$base network. What is the new prefix (fewest bits borrowed)?", "/$np",
                    listOf("/${np - 1}", "/${np + 1}", "/${np + 2}", "/${base + need}").filter { it.drop(1).toInt() in 1..32 },
                    listOf("Find the smallest s with 2^s ≥ $need → s = $borrow (2^$borrow = ${1 shl borrow}).", "New prefix = $base + $borrow = /$np."), accepted = listOf("$np"), r = r)
            }
        }
        "vlsm" -> {
            val maxH = when (level) { 0 -> 7; 1 -> 10; else -> 14 }
            val h = r.nextInt(2, maxH + 1)
            val need = r.nextInt((1 shl (h - 1)) - 1, (1 shl h) - 1)
            val p = 32 - h
            finish(type, "A subnet must support $need hosts. What is the longest prefix (smallest subnet) that works?", "/$p",
                listOf("/${p + 1}", "/${p - 1}", "/${p - 2}", "/${p + 2}").filter { it.drop(1).toInt() in 8..32 },
                listOf("Need 2^h − 2 ≥ $need.", "h = ${h - 1} gives ${(1 shl (h - 1)) - 2} (too few); h = $h gives ${(1 shl h) - 2}.", "Prefix = 32 − $h = /$p."),
                accepted = listOf("$p"), r = r)
        }
        "wildcard" -> {
            val p = when (level) { 0 -> r.nextInt(24, 31); 1 -> r.nextInt(16, 31); else -> r.nextInt(8, 31) }
            val w = maskOf(p).inv() and 0xFFFFFFFFL
            finish(type, "What wildcard mask matches a /$p (mask ${ip(maskOf(p))})?", ip(w),
                listOf(ip(maskOf(p)), ip(maskOf(p + 1).inv() and 0xFFFFFFFFL), ip(maskOf(p - 1).inv() and 0xFFFFFFFFL), ip((w + 1) and 0xFFFFFFFFL)),
                listOf("Wildcard = 255.255.255.255 − mask.", "255.255.255.255 − ${ip(maskOf(p))} = ${ip(w)}.", "Check: the interesting octet equals block size − 1."), r = r)
        }
        "private" -> {
            val samples = listOf(
                "10.${r.nextInt(256)}.${r.nextInt(256)}.${r.nextInt(1, 255)}" to "Private (RFC 1918)",
                "172.${r.nextInt(16, 32)}.${r.nextInt(256)}.${r.nextInt(1, 255)}" to "Private (RFC 1918)",
                "192.168.${r.nextInt(256)}.${r.nextInt(1, 255)}" to "Private (RFC 1918)",
                "172.${listOf(r.nextInt(1, 16), r.nextInt(32, 256)).random(r)}.${r.nextInt(256)}.${r.nextInt(1, 255)}" to "Public",
                "192.${listOf(167, 169, r.nextInt(1, 167)).random(r)}.${r.nextInt(256)}.${r.nextInt(1, 255)}" to "Public",
                "${r.nextInt(11, 100)}.${r.nextInt(256)}.${r.nextInt(256)}.${r.nextInt(1, 255)}" to "Public",
                "169.254.${r.nextInt(1, 255)}.${r.nextInt(1, 255)}" to "APIPA / link-local",
                "127.${r.nextInt(256)}.${r.nextInt(256)}.${r.nextInt(1, 255)}" to "Loopback"
            )
            val (addr, ans) = samples.random(r)
            Problem(type, "Classify $addr.", ans, emptyList(), listOf("Private (RFC 1918)", "Public", "APIPA / link-local", "Loopback"),
                listOf("Private: 10/8, 172.16-172.31, 192.168/16.", "169.254/16 = APIPA; 127/8 = loopback.", "$addr → $ans."), typable = false)
        }
        "ports" -> {
            val (name, port, tr) = PORTS.random(r)
            val wrong = PORTS.filter { it.second != port }.shuffled(r).take(5).map { "${it.third} ${it.second}" }
            finish(type, "Which transport and port does $name use?", "$tr $port", wrong, listOf("$name → $tr $port."), typable = false, r = r)
        }
        else -> make("network", level, r)
    }

    val PORTS = listOf(
        Triple("FTP control", "21", "TCP"), Triple("SSH", "22", "TCP"), Triple("Telnet", "23", "TCP"), Triple("SMTP", "25", "TCP"),
        Triple("TACACS+", "49", "TCP"), Triple("DNS (queries)", "53", "UDP"), Triple("DHCP server", "67", "UDP"), Triple("DHCP client", "68", "UDP"),
        Triple("TFTP", "69", "UDP"), Triple("HTTP", "80", "TCP"), Triple("POP3", "110", "TCP"), Triple("NTP", "123", "UDP"), Triple("IMAP", "143", "TCP"),
        Triple("SNMP polling", "161", "UDP"), Triple("SNMP traps", "162", "UDP"), Triple("BGP", "179", "TCP"), Triple("HTTPS", "443", "TCP"),
        Triple("IKE", "500", "UDP"), Triple("Syslog", "514", "UDP"), Triple("RADIUS authentication", "1812", "UDP"), Triple("RADIUS accounting", "1813", "UDP"),
        Triple("RDP", "3389", "TCP"), Triple("IPsec NAT-T", "4500", "UDP"), Triple("CAPWAP control", "5246", "UDP"), Triple("CAPWAP data", "5247", "UDP")
    )
}

/** Unified drill item: either a generated problem or a static multiple-choice question. */
data class DrillItem(val problem: Problem?, val question: Question?) {
    val prompt get() = problem?.prompt ?: question!!.question
    val options get() = problem?.options ?: question!!.options
    val correctText get() = problem?.answer ?: question!!.options[question.answer]
    val explanation: List<String> get() = problem?.steps ?: listOf(question!!.explanation)
    val typable get() = problem?.typable ?: false
    fun isCorrectOption(i: Int) = problem?.let { Problem.norm(it.options[i]) == Problem.norm(it.answer) } ?: (i == question!!.answer)
}
