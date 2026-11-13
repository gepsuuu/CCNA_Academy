package com.ccnaacademy.app.data

import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.roundToInt

/** A themed block of the curriculum used by the weekly schedule. */
data class Theme(val id: String, val title: String, val lessonIds: List<String>, val domains: List<String>, val practiceRoute: String)

data class PlanItem(val title: String, val detail: String, val minutes: Int, val route: String)
data class PlanDay(val day: Long, val dow: DayOfWeek, val study: Boolean, val theme: Theme?, val review: Boolean, val items: List<PlanItem>, val isExamDay: Boolean = false)
data class Phase(val name: String, val from: Long, val to: Long, val focus: String)
data class StudyPlan(
    val settings: PlannerSettings, val daysLeft: Long, val studyDaysLeft: Int, val minutesAvailable: Int, val minutesNeeded: Int,
    val feasible: Boolean, val phases: List<Phase>, val currentPhase: Phase?, val week: List<PlanDay>, val readiness: Float,
    val priorities: List<Pair<Theme, Float>>, val advice: List<String>
)

object Planner {
    private fun dr(domain: String) = "bank?mode=quick&count=10&minutes=0&domain=$domain&topic=all&diff=0&format=all&pack=all"
    val THEMES = listOf(
        Theme("fund", "Network fundamentals", listOf("1-01", "1-02", "1-03", "1-04", "1-05", "1-06", "1-11", "1-12", "1-13"), listOf("d1"), dr("d1")),
        Theme("subnet", "Subnetting and IP addressing", listOf("1-07", "1-08", "1-09", "1-10"), listOf("d1"), "subnet"),
        Theme("switching", "VLANs and switching", listOf("1-14", "2-01", "2-02", "2-03", "2-04", "2-05", "2-06"), listOf("d2"), dr("d2")),
        Theme("routing", "Routing and OSPF", listOf("3-01", "3-02", "3-03", "3-04"), listOf("d3"), dr("d3")),
        Theme("services", "Network services and wireless", listOf("4-01", "4-02", "4-03", "4-04", "2-07", "2-08", "2-09"), listOf("d4", "d2"), dr("d4")),
        Theme("secauto", "Security and automation", listOf("5-01", "5-02", "5-03", "5-04", "5-05", "5-06", "6-01", "6-02", "6-03", "6-04"), listOf("d5", "d6"), dr("d5")),
    )
    val REVIEW = Theme("review", "Revision and practice exam", emptyList(), emptyList(), "examsetup")

    /** Weakness score per theme (0 strong … 1 weak): incomplete lessons + low bank accuracy + low lesson-check accuracy. */
    fun priorities(c: Curriculum, p: ProgressState): List<Pair<Theme, Float>> = THEMES.map { t ->
        val lessons = t.lessonIds.mapNotNull { c.lessonMap[it] }
        val incomplete = if (lessons.isEmpty()) 0f else lessons.count { it.id !in p.completedLessons }.toFloat() / lessons.size
        val ids = t.lessonIds.toSet()
        val bank = p.bankStats.values.filter { it.lessonId in ids }
        val bankN = bank.sumOf { it.attempts }
        val bankAcc = if (bankN == 0) null else bank.sumOf { it.correct }.toFloat() / bankN
        var qc = 0; var qn = 0
        lessons.flatMap { it.quiz }.forEach { q -> p.answers[q.id]?.let { qc += it.correct; qn += it.correct + it.incorrect } }
        val quizAcc = if (qn == 0) null else qc.toFloat() / qn
        val accs = listOfNotNull(bankAcc, quizAcc)
        val weakness = if (accs.isEmpty()) 0.5f else 1f - accs.average().toFloat()
        // Domain weight nudges heavier blueprint areas up (IP Connectivity 25%).
        val weight = t.domains.sumOf { d -> c.domains.firstOrNull { it.id == d }?.weight ?: 10 }.toFloat() / (t.domains.size * 25f)
        t to (0.5f * incomplete + 0.4f * weakness + 0.1f * weight).coerceIn(0f, 1f)
    }.sortedByDescending { it.second }

    fun build(c: Curriculum, lib: MemoryLibrary, p: ProgressState, s: PlannerSettings, today: Long = today()): StudyPlan {
        val daysLeft = (s.examDay - today).coerceAtLeast(0)
        val studyDays = s.studyDays.ifEmpty { setOf(1, 2, 3, 4, 5, 6, 7) }
        val studyDaysLeft = (0 until daysLeft).count { (LocalDate.ofEpochDay(today + it).dayOfWeek.value) in studyDays }
        val levelFactor = when (s.level) { 0 -> 1.5; 1 -> 1.15; else -> 0.85 }   // beginners need more time per lesson
        val lessonMin = c.lessons.filter { it.id !in p.completedLessons }.sumOf { it.minutes } * levelFactor
        val practiceMin = 236 * 1.5 + 4 * 75                                         // one pass of the bank + several mock exams
        val minutesNeeded = (lessonMin + practiceMin * (if (p.sessions.size > 10) 0.6 else 1.0)).roundToInt()
        val minutesAvailable = studyDaysLeft * s.minutesPerDay
        val stats = Stats(c, p, lib)

        // Phases: learn (≈60%), practice (≈25%), final review (last ≈15%, at least the last 5 days if possible).
        val total = daysLeft.coerceAtLeast(1)
        val finalLen = maxOf(minOf(5L, total), (total * 0.15).toLong())
        val learnLen = ((total - finalLen) * 0.7).toLong()
        val phases = listOf(
            Phase("Learn", today, today + learnLen - 1, "Complete lessons theme by theme with short checks and flashcards"),
            Phase("Practice", today + learnLen, s.examDay - finalLen - 1, "Mixed practice sets, weak-topic drills, timed sets"),
            Phase("Final review", s.examDay - finalLen, s.examDay - 1, "Full exam simulations, cheatsheets, common mistakes")
        ).filter { it.to >= it.from }
        val cur = phases.firstOrNull { today in it.from..it.to }
        val pri = priorities(c, p)

        // Week schedule: last selected weekday of each week is revision/exam day; other study days rotate through themes by priority.
        val reviewDow = studyDays.maxOrNull() ?: 7
        val rotation = pri.map { it.first }
        var k = 0   // highest-priority theme first, then rotate; priorities are recomputed from progress every time the plan is built
        val finalPhase = cur?.name == "Final review"
        val week = (0 until 7).map { i ->
            val d = today + i
            val dow = LocalDate.ofEpochDay(d).dayOfWeek
            val isExam = d == s.examDay
            val study = dow.value in studyDays && d < s.examDay
            val review = study && ((dow.value == reviewDow && studyDays.size > 1) || (finalPhase && i % 2 == 0))
            val theme = when { !study -> null; review -> REVIEW; else -> rotation[k++ % rotation.size] }
            PlanDay(d, dow, study, theme, review, if (study && theme != null) items(c, lib, p, s, theme, cur?.name ?: "Learn") else emptyList(), isExam)
        }
        val advice = buildList {
            if (daysLeft == 0L) add("Your target date is today or has passed — set a new date to rebuild the plan.")
            else if (minutesAvailable < minutesNeeded) add("At ${s.minutesPerDay} min on ${studyDays.size} day(s)/week you have about ${minutesAvailable / 60} h before the exam, " +
                "but roughly ${minutesNeeded / 60} h of work remains. Add study days, increase daily time, or move the exam date.")
            else add("Your schedule fits: about ${minutesAvailable / 60} h available for roughly ${minutesNeeded / 60} h of remaining work.")
            val gap = s.targetScore - (stats.readiness * 100).roundToInt()
            if (gap > 0) add("Readiness is ${(stats.readiness * 100).roundToInt()}% versus your ${s.targetScore}% goal. Focus first on: ${pri.take(2).joinToString { it.first.title }}.")
            else add("Readiness meets your ${s.targetScore}% goal — keep it there with mixed practice and mock exams.")
            if (BankEngine.weakTopics(p).isNotEmpty()) add("Weak bank topics: ${BankEngine.weakTopics(p).take(3).joinToString { it.topic.replace('-', ' ') }}.")
        }
        return StudyPlan(s, daysLeft, studyDaysLeft, minutesAvailable, minutesNeeded, minutesAvailable >= minutesNeeded, phases, cur, week,
            stats.readiness, pri, advice)
    }

    /** Concrete, clickable tasks that fit the student's daily minutes. */
    fun items(c: Curriculum, lib: MemoryLibrary, p: ProgressState, s: PlannerSettings, t: Theme, phase: String): List<PlanItem> {
        val budget = s.minutesPerDay.coerceAtLeast(15)
        val out = mutableListOf<PlanItem>()
        val cards = minOf(15, (budget * 0.15).roundToInt().coerceAtLeast(5))
        if (t.id == "review") {
            val n = if (budget >= 90) 60 else if (budget >= 50) 40 else 20
            out += PlanItem("Exam simulation ($n questions)", "Blueprint-weighted, timed", (n * 1.2).roundToInt(), "examsetup")
            if (budget - n * 1.2 >= 10) out += PlanItem("Review incorrect answers", "Re-attempt questions you missed", 10,
                "bank?mode=incorrect&count=0&minutes=0&domain=all&topic=all&diff=0&format=all&pack=all")
            out += PlanItem("Flashcard review", "Due and overdue cards", cards, "cardsession?mode=due&arg=")
            return out
        }
        val learnShare = when (phase) { "Learn" -> 0.6; "Practice" -> 0.3; else -> 0.15 }
        var learnBudget = (budget * learnShare).roundToInt()
        val levelFactor = when (s.level) { 0 -> 1.5; 1 -> 1.15; else -> 0.85 }
        t.lessonIds.mapNotNull { c.lessonMap[it] }.filter { it.id !in p.completedLessons }.forEach { l ->
            val m = (l.minutes * levelFactor).roundToInt()
            if (learnBudget > 0) { out += PlanItem("Lesson: ${l.title}", "${c.domain(l.domainId).number} ${c.domain(l.domainId).title}", m, "lesson/${l.id}"); learnBudget -= m }
        }
        if (out.isEmpty()) {   // all lessons done → revisit the weakest lesson in this theme
            val ids = t.lessonIds.toSet()
            val weak = p.bankStats.values.filter { it.lessonId in ids && !it.lastCorrect }.groupingBy { it.lessonId }.eachCount().maxByOrNull { it.value }?.key
            (weak ?: t.lessonIds.firstOrNull())?.let { id -> c.lessonMap[id]?.let { out += PlanItem("Revise: ${it.title}", "Re-read and redo the knowledge check", 15, "lesson/$id") } }
        }
        val practice = (budget - out.sumOf { it.minutes } - cards).coerceAtLeast(10)
        out += PlanItem(if (t.id == "subnet") "Subnetting practice" else "Practice: ${t.title}",
            if (t.id == "subnet") "Generator drills and timed challenge" else "~${(practice / 1.5).roundToInt().coerceAtLeast(5)} questions with explanations", practice, t.practiceRoute)
        out += PlanItem("Flashcard review", "Due and overdue cards", cards, "cardsession?mode=due&arg=")
        return out
    }

    fun weeksText(days: Long) = if (days >= 14) "${ceil(days / 7.0).toInt()} weeks" else "$days days"
}
