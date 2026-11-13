package com.ccnaacademy.app.data

/** One task in today's routine. */
data class PlanTask(
    val key: String, val title: String, val detail: String, val route: String, val done: Boolean, val why: String
)

object DailyPlanner {
    /**
     * Builds today's plan. It adapts to performance:
     *  - flashcard target = due + overdue + new-card allowance (reduced when a large backlog exists)
     *  - memorization topic / cheatsheet / quiz focus on the weakest blueprint domain
     *  - speed technique = lowest drill accuracy (or never practiced)
     *  - weak-topic revision = lowest-accuracy lesson, else overdue revision, else next lesson
     */
    fun build(c: Curriculum, lib: MemoryLibrary, p: ProgressState): DailyPlanState {
        val t = today()
        val stats = Stats(c, p, lib)
        val cs = CardStats(lib, p)
        val backlog = cs.dueToday
        val newAllowance = if (backlog > 60) 0 else if (backlog > 30) cs.newRemainingToday / 2 else cs.newRemainingToday
        val target = (backlog + newAllowance).coerceAtMost(120)

        // Weakest domain: lowest readiness, weighted toward heavier exam domains.
        val weakDomain = stats.domainStats.minByOrNull { it.readiness - it.domain.weight / 400f }?.domain?.id ?: "d1"

        val guide = lib.guides.sortedWith(compareBy<Guide>(
            { recallAccuracy(p, it) ?: -1f },                   // never practiced first, then low recall
            { p.guideVisits[it.id] ?: Long.MIN_VALUE },         // least recently visited
            { if (it.domainId == weakDomain) 0 else 1 }
        )).let { list -> list[(t % 3).toInt().coerceAtMost(list.size - 1)] }

        val sheetsWeak = lib.cheatsheets.filter { it.domainId == weakDomain }.ifEmpty { lib.cheatsheets }
        val sheet = sheetsWeak[(t % sheetsWeak.size).toInt()]

        val tech = lib.techniques.minByOrNull { tq ->
            val runs = p.drills.filter { it.kind == "speed" && it.refId == tq.id }.takeLast(3)
            if (runs.isEmpty()) -1.0 else runs.map { it.percent }.average()
        } ?: lib.techniques.first()

        // Weakest question-bank topic (≥2 attempts, <70%) points at its lesson first.
        val bankWeak = BankEngine.weakTopics(p).firstNotNullOfOrNull { t -> p.bankStats.values.firstOrNull { it.topic == t.topic }?.lessonId?.let { c.lessonMap[it] } }
        val lesson = bankWeak ?: stats.weakTopics.firstOrNull()?.lesson
            ?: stats.revisions.firstOrNull { it.dueEpochDay <= t }?.lesson
            ?: stats.nextLesson() ?: c.lessons.first()
        val reason = when {
            bankWeak != null -> "lowest question-bank topic accuracy"
            stats.weakTopics.isNotEmpty() -> "lowest quiz accuracy"
            stats.revisions.any { it.dueEpochDay <= t } -> "scheduled spaced revision"
            else -> "next lesson in the curriculum"
        }
        return DailyPlanState(t, target, guide.id, sheet.id, tech.id, weakDomain, lesson.id, reason)
    }

    fun recallAccuracy(p: ProgressState, g: Guide): Float? {
        var c = 0; var w = 0
        g.recall.indices.forEach { i -> p.recallStats["${g.id}:$i"]?.let { c += it.correct; w += it.incorrect } }
        return if (c + w == 0) null else c.toFloat() / (c + w)
    }

    fun tasks(c: Curriculum, lib: MemoryLibrary, p: ProgressState, plan: DailyPlanState): List<PlanTask> {
        val t = today()
        val done = p.dailyDone[t] ?: emptySet()
        val cs = CardStats(lib, p)
        val guide = lib.guides.firstOrNull { it.id == plan.guideId }
        val sheet = lib.cheatsheets.firstOrNull { it.id == plan.sheetId }
        val tech = lib.techniques.firstOrNull { it.id == plan.techniqueId }
        val lesson = c.lessonMap[plan.lessonId]
        val dom = c.domains.firstOrNull { it.id == plan.quizDomain }
        return listOf(
            PlanTask("cards", "Flashcard revision", "${cs.reviewedToday}/${plan.cardTarget} reviews • ${cs.overdue} overdue",
                "cardsession?mode=due&arg=", plan.cardTarget == 0 || cs.reviewedToday >= plan.cardTarget,
                "Due + overdue cards plus today's new-card allowance."),
            PlanTask("guide", "Memorization: ${guide?.title ?: ""}", guide?.techniques?.joinToString() ?: "", "guide/${plan.guideId}",
                "guide:${plan.guideId}" in done, "Chosen from topics you have recalled least or not visited recently."),
            PlanTask("sheet", "Cheatsheet: ${sheet?.title ?: ""}", "Review and mark key facts", "sheet/${plan.sheetId}",
                "sheet:${plan.sheetId}" in done, "From your weakest domain: ${dom?.title ?: ""}."),
            PlanTask("speed", "Speed technique: ${tech?.title ?: ""}", "Read the method, then run a timed drill", "technique/${plan.techniqueId}",
                "speed:${plan.techniqueId}" in done, "Your lowest-scoring or not-yet-practiced technique."),
            PlanTask("quiz", "Short quiz (5 questions)", "Focus: ${dom?.number ?: ""} ${dom?.title ?: ""} + previously missed",
                "bank?mode=daily&count=5&minutes=0&domain=${plan.quizDomain}&topic=all&diff=0&format=all&pack=all", "quiz" in done, "Targets your weakest domain."),
            PlanTask("lesson", "Weak-topic revision: ${lesson?.title ?: ""}", "Re-read and redo the knowledge check", "lesson/${plan.lessonId}",
                "lesson:${plan.lessonId}" in done, "Selected because of ${plan.reason}.")
        )
    }
}
