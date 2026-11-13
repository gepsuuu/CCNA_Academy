package com.ccnaacademy.app.data

import kotlin.math.max
import kotlin.math.roundToInt

enum class Rating(val label: String) { AGAIN("Again"), HARD("Hard"), GOOD("Good"), EASY("Easy") }

/**
 * SM-2 style scheduler.
 * Again → relearn today (re-queued in the session), ease −0.20, counts as a lapse.
 * Hard  → shorter than normal (×1.2), ease −0.15.
 * Good  → normal growth (× ease).
 * Easy  → longest growth (× ease × 1.3), ease +0.15.
 */
object Srs {
    fun next(s: SrsState, r: Rating, today: Long): SrsState {
        val reviewed = s.copy(reviews = s.reviews + 1, lastRating = r.ordinal)
        return when (r) {
            Rating.AGAIN -> reviewed.copy(reps = 0, interval = 0, ease = max(1.3, s.ease - 0.2), due = today,
                lapses = if (s.reps > 0) s.lapses + 1 else s.lapses, wrong = s.wrong + 1)
            else -> {
                val iv = interval(s, r)
                val ease = when (r) { Rating.HARD -> max(1.3, s.ease - 0.15); Rating.EASY -> s.ease + 0.15; else -> s.ease }
                reviewed.copy(reps = s.reps + 1, interval = iv, ease = ease, due = today + iv)
            }
        }
    }

    fun interval(s: SrsState, r: Rating): Int = when (r) {
        Rating.AGAIN -> 0
        Rating.HARD -> if (s.reps == 0) 1 else max(s.interval + 1, (s.interval * 1.2).roundToInt())
        Rating.GOOD -> when (s.reps) { 0 -> 1; 1 -> 3; else -> max(s.interval + 1, (s.interval * s.ease).roundToInt()) }
        Rating.EASY -> when (s.reps) { 0 -> 4; 1 -> 6; else -> max(s.interval + 2, (s.interval * s.ease * 1.3).roundToInt()) }
    }

    fun label(s: SrsState, r: Rating): String = when (val d = interval(s, r)) {
        0 -> "now"; 1 -> "1 day"; in 2..30 -> "$d days"; else -> "${(d / 30.0 * 10).roundToInt() / 10.0} mo"
    }
}

/** Snapshot of flashcard workload for today. */
class CardStats(lib: MemoryLibrary, p: ProgressState) {
    private val t = today()
    val total = lib.cards.size
    val overdue = lib.cards.count { c -> p.srs[c.id]?.let { !it.isNew && it.due < t } == true }
    val dueToday = lib.cards.count { c -> p.srs[c.id]?.let { !it.isNew && it.due <= t } == true }
    val newAvailable = lib.cards.count { (p.srs[it.id]?.isNew ?: true) }
    val newRemainingToday = minOf(newAvailable, max(0, p.newPerDay - (p.newIntroduced[t] ?: 0)))
    val reviewedToday = p.reviewsByDay[t] ?: 0
    val target = p.plan?.takeIf { it.day == t }?.cardTarget ?: (dueToday + newRemainingToday)
    val mastered = lib.cards.count { p.srs[it.id]?.mastered == true }
    val leeches = lib.cards.filter { p.srs[it.id]?.leech == true }
    val learning = lib.cards.count { p.srs[it.id]?.let { !it.isNew && !it.mastered } == true }
}
