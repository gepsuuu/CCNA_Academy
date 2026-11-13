package com.ccnaacademy.app.data

import kotlin.random.Random

/** Today's five challenges. Chosen deterministically from the date so they stay the same all day. */
data class DailyChallengeSet(
    val day: Long, val question: BankQuestion?, val subnet: Problem, val card: Card?, val concept: GlossaryEntry?, val speedTypes: List<String>
) {
    companion object { val PARTS = listOf("question", "subnet", "card", "concept", "speed") }
}

object DailyChallenges {
    fun build(day: Long, bank: QuestionBank, lib: MemoryLibrary, study: StudyContent, p: ProgressState): DailyChallengeSet {
        val r = Random(day * 7919 + 17)
        // Prefer questions not yet answered correctly; stable for the day because the candidate list is sorted by id.
        val qs = bank.questions.sortedBy { it.id }
        val fresh = qs.filter { p.bankStats[it.id]?.lastCorrect != true }
        val question = (fresh.ifEmpty { qs }).let { if (it.isEmpty()) null else it[r.nextInt(it.size)] }
        // Subnetting difficulty follows the student's generator accuracy.
        val gs = p.genStats.values; val n = gs.sumOf { it.correct + it.incorrect }
        val acc = if (n == 0) 0.0 else gs.sumOf { it.correct }.toDouble() / n
        val level = when { n < 10 -> 0; acc >= 0.85 -> 2; else -> 1 }
        val type = listOf("network", "broadcast", "hostrange", "hosts", "wildcard", "vlsm")[r.nextInt(6)]
        val subnet = Gen.make(type, level, Random(day * 31 + 5))
        val cards = lib.cards.sortedBy { it.id }
        val dueOrNew = cards.filter { (p.srs[it.id]?.due ?: 0) <= day }
        val card = (dueOrNew.ifEmpty { cards }).let { if (it.isEmpty()) null else it[r.nextInt(it.size)] }
        val terms = study.glossary.filter { it.id !in p.conceptsMemorized }.ifEmpty { study.glossary }
        val concept = if (terms.isEmpty()) null else terms[r.nextInt(terms.size)]
        val speed = listOf("bin2dec", "cidr2mask", "wildcard", "hosts", "private", "ports")
        return DailyChallengeSet(day, question, subnet, card, concept, speed)
    }

    /** Consecutive days (ending today, or yesterday if today is not finished yet) with all five parts done. */
    fun streak(p: ProgressState): Int {
        val full = p.challengeDone.filterValues { it.containsAll(DailyChallengeSet.PARTS) }.keys
        var d = today(); if (d !in full) d -= 1
        var s = 0; while (d in full) { s++; d-- }
        return s
    }
}
