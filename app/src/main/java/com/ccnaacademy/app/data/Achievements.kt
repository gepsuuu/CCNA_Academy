package com.ccnaacademy.app.data

/** A milestone computed only from real stored progress — nothing is awarded without the underlying data. */
data class Badge(val id: String, val title: String, val description: String, val icon: String, val progress: Float, val current: Int, val goal: Int) {
    val earned get() = current >= goal
}

object Achievements {
    fun compute(c: Curriculum, lib: MemoryLibrary, bank: QuestionBank, p: ProgressState): List<Badge> {
        val st = Stats(c, p, lib)
        val answered = p.bankStats.values.sumOf { it.attempts }
        val distinct = p.bankStats.size
        val bestExam = p.sessions.filter { it.exam && it.total >= 40 }.maxOfOrNull { it.percent } ?: 0
        val domainsDone = c.domains.count { d -> c.lessonsIn(d.id).all { it.id in p.completedLessons } }
        val subnetChallenges = p.drills.filter { it.kind == "challenge" && it.level == 2 && it.percent >= 90 }.size
        val longestStreak = longest(p.studySecondsByDay.filterValues { it >= 60 }.keys)
        val challengeStreak = longest(p.challengeDone.filterValues { it.size >= 5 }.keys)
        fun b(id: String, t: String, d: String, icon: String, cur: Int, goal: Int) = Badge(id, t, d, icon, (cur.toFloat() / goal).coerceIn(0f, 1f), cur.coerceAtMost(goal), goal)
        return listOf(
            b("first-lesson", "First lesson", "Complete your first lesson", "lesson", st.completedCount, 1),
            b("domain", "Domain complete", "Finish every lesson in one exam domain", "domain", domainsDone, 1),
            b("curriculum", "Full curriculum", "Complete all ${st.totalLessons} lessons", "school", st.completedCount, st.totalLessons),
            b("q100", "100 answers", "Answer 100 question-bank questions", "quiz", answered, 100),
            b("bank-coverage", "Bank explorer", "Attempt every question in the enabled bank at least once", "explore", distinct.coerceAtMost(bank.questions.size), bank.questions.size.coerceAtLeast(1)),
            b("cards50", "Flashcard foundation", "Master 50 flashcards (interval of 21+ days)", "cards", st.cards.mastered, 50),
            b("cards-all", "Flashcard mastery", "Master every flashcard", "cards", st.cards.mastered, lib.cards.size),
            b("subnet-pro", "Subnetting pro", "Score 90%+ on the CCNA-level timed subnetting challenge", "calc", subnetChallenges, 1),
            b("exam70", "Exam ready: 70%", "Score 70%+ on an exam simulation of 40+ questions", "exam", if (bestExam >= 70) 1 else 0, 1),
            b("exam85", "Exam ready: 85%", "Score 85%+ on an exam simulation of 40+ questions", "exam", if (bestExam >= 85) 1 else 0, 1),
            b("streak7", "Consistent week", "Study 7 days in a row", "streak", longestStreak, 7),
            b("streak30", "Consistent month", "Study 30 days in a row", "streak", longestStreak, 30),
            b("daily7", "Daily challenger", "Complete all five daily challenges 7 days in a row", "daily", challengeStreak, 7),
            b("notes", "Note taker", "Write 10 personal study notes", "notes", p.notes.size, 10),
        )
    }

    fun longest(days: Set<Long>): Int {
        var best = 0
        days.forEach { d -> if (d - 1 !in days) { var n = 0; while (d + n in days) n++; best = maxOf(best, n) } }
        return best
    }
}
