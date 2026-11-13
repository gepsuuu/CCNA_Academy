package com.ccnaacademy.app.data

import java.time.LocalDate

fun today(): Long = LocalDate.now().toEpochDay()

/** Spaced revision intervals (days) after completing a lesson. */
val REVIEW_INTERVALS = listOf(1L, 3L, 7L, 14L, 30L, 60L)
/** Leitner box intervals (days) for flashcards. Box >= 4 counts as mastered. */
val CARD_INTERVALS = listOf(0L, 1L, 2L, 4L, 8L, 16L)

data class DomainStats(
    val domain: Domain, val completion: Float, val accuracy: Float?, val attempts: Int,
    val mastery: Float, val examScore: Float?, val readiness: Float
)
data class WeakTopic(val lesson: Lesson, val accuracy: Float, val attempts: Int)
data class Revision(val lesson: Lesson, val dueEpochDay: Long, val stage: Int)

class Stats(val c: Curriculum, val p: ProgressState, val lib: MemoryLibrary) {
    val totalLessons = c.lessons.size
    val completedCount = c.lessons.count { it.id in p.completedLessons }
    val overallProgress = if (totalLessons == 0) 0f else completedCount.toFloat() / totalLessons

    val correctTotal = p.answers.values.sumOf { it.correct }
    val incorrectTotal = p.answers.values.sumOf { it.incorrect }
    val overallAccuracy: Float? = (correctTotal + incorrectTotal).let { if (it == 0) null else correctTotal.toFloat() / it }

    val cards = CardStats(lib, p)
    val cardsMastered = cards.mastered
    val cardsDue = cards.dueToday

    val chaptersTotal = c.chapters().size
    val chaptersCompleted = c.chapters().count { ch -> ch.lessonIds.isNotEmpty() && ch.lessonIds.all { it in p.completedLessons } }

    fun lessonAccuracy(l: Lesson): Pair<Float?, Int> {
        var cor = 0; var inc = 0
        l.quiz.forEach { q -> p.answers[q.id]?.let { cor += it.correct; inc += it.incorrect } }
        val n = cor + inc
        return (if (n == 0) null else cor.toFloat() / n) to n
    }

    val domainStats: List<DomainStats> = c.domains.map { d ->
        val ls = c.lessonsIn(d.id)
        val completion = if (ls.isEmpty()) 0f else ls.count { it.id in p.completedLessons }.toFloat() / ls.size
        var cor = 0; var inc = 0
        ls.flatMap { it.quiz }.forEach { q -> p.answers[q.id]?.let { cor += it.correct; inc += it.incorrect } }
        // Include question-bank and exam-simulator answers for this domain.
        p.bankStats.values.filter { it.domainId == d.id }.forEach { cor += it.correct; inc += it.incorrect }
        val acc = if (cor + inc == 0) null else cor.toFloat() / (cor + inc)
        val dc = lib.cards.filter { it.domainId == d.id }
        // Mastery credits partially learned cards: interval / 21 days, capped at 1.
        val mastery = if (dc.isEmpty()) 0f else dc.sumOf { ((p.srs[it.id]?.interval ?: 0) / 21.0).coerceAtMost(1.0) }.toFloat() / dc.size
        val recent = p.exams.takeLast(3).filter { (it.domainTotal[d.id] ?: 0) > 0 }
        val exam = if (recent.isEmpty()) null else recent.map { (it.domainCorrect[d.id] ?: 0).toFloat() / it.domainTotal.getValue(d.id) }.average().toFloat()
        // Readiness weighting: understanding (completion) + demonstrated recall (quiz, exams) + retention (flashcards)
        val readiness = 0.30f * completion + 0.35f * (acc ?: 0f) + 0.15f * mastery + 0.20f * (exam ?: (acc ?: 0f) * 0.5f)
        DomainStats(d, completion, acc, cor + inc, mastery, exam, readiness.coerceIn(0f, 1f))
    }

    /** Exam readiness weighted by official domain weights. */
    val readiness: Float = domainStats.sumOf { (it.readiness * it.domain.weight).toDouble() }.toFloat() / 100f

    val weakTopics: List<WeakTopic> = c.lessons.mapNotNull { l ->
        val (acc, n) = lessonAccuracy(l)
        if (acc != null && acc < 0.7f) WeakTopic(l, acc, n) else null
    }.sortedBy { it.accuracy }

    val revisions: List<Revision> = p.reviews.mapNotNull { (id, r) -> c.lessonMap[id]?.let { Revision(it, r.dueEpochDay, r.stage) } }
        .sortedBy { it.dueEpochDay }

    val streak: Int = run {
        val days = p.studySecondsByDay.filterValues { it >= 60 }.keys
        var d = today(); if (d !in days) d -= 1
        var n = 0
        while (d in days) { n++; d-- }
        n
    }
    val todaySeconds = p.studySecondsByDay[today()] ?: 0L
    val totalSeconds = p.studySecondsByDay.values.sum()
    fun lastDays(n: Int): List<Pair<Long, Long>> = (n - 1 downTo 0).map { val d = today() - it; d to (p.studySecondsByDay[d] ?: 0L) }
    val weekSeconds = lastDays(7).sumOf { it.second }

    fun nextLesson(): Lesson? = p.lastLessonId?.let { c.lessonMap[it] }?.takeIf { it.id !in p.completedLessons }
        ?: c.domains.flatMap { d -> d.chapters.flatMap { it.lessonIds } }.firstOrNull { it !in p.completedLessons }?.let { c.lessonMap[it] }
}

fun formatDuration(sec: Long): String {
    val h = sec / 3600; val m = (sec % 3600) / 60
    return when { h > 0 -> "${h}h ${m}m"; m > 0 -> "${m}m"; else -> "${sec}s" }
}

fun dueLabel(epochDay: Long): String {
    val diff = epochDay - today()
    return when { diff < 0 -> "Overdue ${-diff}d"; diff == 0L -> "Due today"; diff == 1L -> "Tomorrow"; else -> "In ${diff} days" }
}
