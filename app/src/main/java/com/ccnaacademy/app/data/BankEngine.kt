package com.ccnaacademy.app.data

import kotlin.math.floor

data class BankFilter(val domain: String = "all", val topic: String = "all", val difficulty: Int = 0, val format: String = "all", val pack: String = "all")

data class TopicPerf(val topic: String, val correct: Int, val total: Int) { val acc get() = if (total == 0) 0f else correct.toFloat() / total }

object BankEngine {
    val DIFFICULTY = mapOf(1 to "Beginner", 2 to "Intermediate", 3 to "CCNA level")
    val FORMATS = linkedMapOf(
        "concept" to "Concept", "scenario" to "Scenario", "troubleshooting" to "Troubleshooting", "diagram" to "Diagram",
        "calculation" to "Calculation", "routing-table" to "Routing table", "protocol-id" to "Protocol ID",
        "config" to "Configuration", "command" to "Command"
    )
    val TYPES = mapOf("single" to "Single answer", "multi" to "Multiple select", "tf" to "True / false", "match" to "Matching", "order" to "Ordering")

    // ---------- grading ----------
    fun isAnswered(q: BankQuestion, a: List<Int>?): Boolean = when (q.type) {
        "match" -> a != null && a.any { it >= 0 }
        else -> !a.isNullOrEmpty()
    }
    fun isComplete(q: BankQuestion, a: List<Int>?): Boolean = when (q.type) {
        "match" -> a != null && a.size == q.left.size && a.all { it >= 0 }
        "multi" -> a != null && a.size == q.correct.size
        else -> !a.isNullOrEmpty()
    }
    fun isCorrect(q: BankQuestion, a: List<Int>?): Boolean {
        if (a == null) return false
        return when (q.type) {
            "single", "tf", "multi" -> a.toSet() == q.correct.toSet() && a.size == q.correct.size
            "match" -> a == q.left.indices.toList()
            "order" -> a == q.items.indices.toList()
            else -> false
        }
    }

    fun prepare(q: BankQuestion, shuffle: Boolean = true): SessionItem {
        val opt = if (shuffle && q.shuffle && q.type in setOf("single", "multi")) q.options.indices.shuffled() else q.options.indices.toList()
        val right = q.right.indices.shuffled()
        var init = q.items.indices.shuffled()
        if (q.items.size > 1) while (init == q.items.indices.toList()) init = q.items.indices.shuffled()
        return SessionItem(q, opt, right, init)
    }

    // ---------- performance ----------
    fun topicPerf(p: ProgressState): List<TopicPerf> =
        p.bankStats.values.groupBy { it.topic }.map { (t, l) -> TopicPerf(t, l.sumOf { it.correct }, l.sumOf { it.attempts }) }

    fun weakTopics(p: ProgressState, minAttempts: Int = 2): List<TopicPerf> =
        topicPerf(p).filter { it.total >= minAttempts && it.acc < 0.7f }.sortedBy { it.acc }

    fun matches(q: BankQuestion, f: BankFilter, bank: QuestionBank) =
        (f.domain == "all" || q.domainId == f.domain) && (f.topic == "all" || q.topic == f.topic) &&
            (f.difficulty == 0 || q.difficulty == f.difficulty) && (f.format == "all" || q.format == f.format) &&
            (f.pack == "all" || bank.packOf[q.id] == f.pack)

    /** Question selection for each practice mode. */
    fun select(bank: QuestionBank, p: ProgressState, mode: String, count: Int, f: BankFilter): List<BankQuestion> {
        val pool = bank.questions.filter { matches(it, f, bank) }
        val n = if (count <= 0) Int.MAX_VALUE else count
        return when (mode) {
            "incorrect" -> pool.filter { p.bankStats[it.id]?.let { s -> !s.lastCorrect && s.incorrect > 0 } == true }
                .sortedByDescending { p.bankStats[it.id]?.incorrect ?: 0 }.take(n).shuffled()
            "bookmarks" -> pool.filter { it.id in p.bankBookmarks }.shuffled().take(n)
            "weak" -> {
                val weak = weakTopics(p).map { it.topic }
                val weakQs = pool.filter { it.topic in weak }
                    .sortedWith(compareBy({ p.bankStats[it.id]?.lastCorrect == true }, { weak.indexOf(it.topic) }))
                // Fill with never-attempted questions from domains with the lowest accuracy so the mode always has content.
                val domAcc = p.bankStats.values.groupBy { it.domainId }.mapValues { (_, l) -> l.sumOf { it.correct }.toFloat() / l.sumOf { it.attempts }.coerceAtLeast(1) }
                val filler = pool.filter { it !in weakQs && p.bankStats[it.id]?.lastCorrect != true }.sortedBy { domAcc[it.domainId] ?: 0.5f }
                (weakQs + filler).take(n).shuffled()
            }
            "daily" -> {
                val missed = pool.filter { p.bankStats[it.id]?.lastCorrect == false }.shuffled()
                val weak = weakTopics(p).map { it.topic }.toSet()
                val weakQs = pool.filter { it.topic in weak }.shuffled()
                val dom = bank.questions.filter { it.domainId == f.domain }.shuffled()
                (missed.take(2) + weakQs.take(2) + dom + bank.questions.shuffled()).distinctBy { it.id }.take(n)
            }
            "full" -> pool.shuffled()
            else -> pool.shuffled().take(n)  // quick, standard, intensive, timed, topic, difficulty
        }
    }

    /** Distributes [count] questions across domains by blueprint weight (largest remainder), random within each domain. */
    fun examSet(bank: QuestionBank, domains: List<Domain>, count: Int, minDifficulty: Int): List<BankQuestion> {
        val pools = domains.associate { d -> d.id to bank.questions.filter { it.domainId == d.id && it.difficulty >= minDifficulty }.shuffled() }
        val total = domains.sumOf { it.weight }.coerceAtLeast(1)
        val exact = domains.associate { it.id to count * it.weight.toDouble() / total }
        val alloc = exact.mapValues { floor(it.value).toInt() }.toMutableMap()
        var left = count - alloc.values.sum()
        exact.entries.sortedByDescending { it.value - floor(it.value) }.forEach { if (left > 0) { alloc[it.key] = alloc.getValue(it.key) + 1; left-- } }
        // Respect pool sizes; redistribute any shortfall to domains that still have questions.
        val out = mutableListOf<BankQuestion>()
        var short = 0
        alloc.forEach { (d, k) -> val pool = pools[d].orEmpty(); out += pool.take(k); short += (k - pool.size).coerceAtLeast(0) }
        if (short > 0) out += pools.values.flatten().filter { it !in out }.shuffled().take(short)
        return out.shuffled()
    }

    fun summarize(mode: String, title: String, exam: Boolean, items: List<SessionItem>, answers: Map<Int, List<Int>>,
                  seconds: Int, limitSec: Int, countUnanswered: Boolean): SessionResult {
        val idx = items.indices.filter { countUnanswered || isAnswered(items[it].q, answers[it]) }
        fun ok(i: Int) = isCorrect(items[i].q, answers[i])
        val dc = mutableMapOf<String, Int>(); val dt = mutableMapOf<String, Int>(); val tc = mutableMapOf<String, Int>(); val tt = mutableMapOf<String, Int>()
        idx.forEach { i ->
            val q = items[i].q
            dt[q.domainId] = (dt[q.domainId] ?: 0) + 1; tt[q.topic] = (tt[q.topic] ?: 0) + 1
            if (ok(i)) { dc[q.domainId] = (dc[q.domainId] ?: 0) + 1; tc[q.topic] = (tc[q.topic] ?: 0) + 1 }
        }
        return SessionResult(System.currentTimeMillis(), mode, title, exam, idx.size, idx.count { isAnswered(items[it].q, answers[it]) },
            idx.count(::ok), seconds, limitSec, dc, dt, tc, tt, idx.filterNot(::ok).map { items[it].q.id })
    }
}
