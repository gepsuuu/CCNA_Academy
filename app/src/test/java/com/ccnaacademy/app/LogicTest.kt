package com.ccnaacademy.app

import com.ccnaacademy.app.data.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    private val bank = TestData.bank

    @Test fun gradingAllTypes() {
        bank.questions.forEach { q ->
            val right: List<Int> = when (q.type) { "match" -> q.left.indices.toList(); "order" -> q.items.indices.toList(); else -> q.correct }
            assertTrue("${q.id} correct answer must grade correct", BankEngine.isCorrect(q, right))
            assertFalse(BankEngine.isCorrect(q, null))
            when (q.type) {
                "single", "tf" -> q.options.indices.filter { it !in q.correct }.forEach { assertFalse(q.id, BankEngine.isCorrect(q, listOf(it))) }
                "multi" -> { assertFalse("partial multi", BankEngine.isCorrect(q, q.correct.dropLast(1)))
                             assertFalse("superset multi", BankEngine.isCorrect(q, q.options.indices.toList())) }
                "match" -> assertFalse(BankEngine.isCorrect(q, right.reversed()))
                "order" -> assertFalse(BankEngine.isCorrect(q, right.reversed()))
            }
        }
    }

    @Test fun shufflingKeepsOriginalIndices() {
        bank.questions.forEach { q ->
            val it = BankEngine.prepare(q)
            assertEquals(q.options.indices.toSet(), it.optOrder.toSet())
            if (q.items.size > 1) assertNotEquals("order must start shuffled", q.items.indices.toList(), it.initOrder)
        }
    }

    @Test fun examDistributionFollowsBlueprint() {
        listOf(20, 40, 60, 100).forEach { n ->
            val set = BankEngine.examSet(bank, TestData.curriculum.domains, n, 1)
            assertEquals(n, set.size); assertEquals(n, set.map { it.id }.toSet().size)
            TestData.curriculum.domains.forEach { d ->
                val got = set.count { it.domainId == d.id }; val want = n * d.weight / 100.0
                assertTrue("$n q: ${d.id} got $got want ≈$want", kotlin.math.abs(got - want) <= 1.0)
            }
        }
    }

    @Test fun scoringCountsUnansweredAsWrongInExams() {
        val items = bank.questions.take(10).map { BankEngine.prepare(it) }
        val answers = mapOf(0 to (if (items[0].q.type == "match") items[0].q.left.indices.toList() else if (items[0].q.type == "order") items[0].q.items.indices.toList() else items[0].q.correct))
        val r = BankEngine.summarize("exam", "t", true, items, answers, 100, 600, countUnanswered = true)
        assertEquals(10, r.total); assertEquals(1, r.correct); assertEquals(10, r.percent); assertEquals(10, r.avgSeconds)
        assertEquals(10, r.domainTotal.values.sum())
    }

    private fun ip(s: String) = s.split(".").fold(0L) { a, o -> (a shl 8) or o.toLong() }
    @Test fun generatorsAreArithmeticallyCorrect() {
        val r = Random(42)
        repeat(3000) {
            val p = Gen.make(listOf("network", "broadcast", "hostrange", "hosts", "wildcard", "cidr2mask", "mask2cidr", "bin2dec", "dec2bin", "hex", "vlsm", "subnets")[it % 12], it % 3, r)
            assertTrue("answer must be accepted: ${p.prompt}", p.isCorrect(p.answer))
            assertTrue("options must contain the answer", p.options.any { o -> Problem.norm(o) == Problem.norm(p.answer) })
            assertEquals("options must be distinct", p.options.size, p.options.map { Problem.norm(it) }.toSet().size)
            val m = Regex("of (\\d+\\.\\d+\\.\\d+\\.\\d+)/(\\d+)").find(p.prompt)
            if (m != null && p.type in setOf("network", "broadcast")) {
                val a = ip(m.groupValues[1]); val pre = m.groupValues[2].toInt()
                val mask = (0xFFFFFFFFL shl (32 - pre)) and 0xFFFFFFFFL
                val want = if (p.type == "network") a and mask else (a and mask) or (mask.inv() and 0xFFFFFFFFL)
                assertEquals(p.prompt, Gen.ip(want), p.answer)
            }
        }
        assertNotNull(Gen.explainSubnet("172.16.70.200/21")); assertTrue(Gen.explainSubnet("172.16.70.200/21")!!.any { "172.16.71.255" in it })
        assertNull(Gen.explainSubnet("999.1.1.1/24"))
    }

    @Test fun srsIntervalsGrowAndAgainResets() {
        var s = SrsState(); val t = 1000L
        s = Srs.next(s, Rating.GOOD, t); assertEquals(1, s.interval)
        s = Srs.next(s, Rating.GOOD, t); assertEquals(3, s.interval)
        val good = Srs.next(s, Rating.GOOD, t).interval; val easy = Srs.next(s, Rating.EASY, t).interval; val hard = Srs.next(s, Rating.HARD, t).interval
        assertTrue(hard < good && good < easy)
        val again = Srs.next(s, Rating.AGAIN, t); assertEquals(0, again.interval); assertEquals(t, again.due); assertEquals(1, again.lapses)
    }

    @Test fun plannerBuildsAdaptiveWeek() {
        val c = TestData.curriculum; val lib = TestData.library
        val ps = PlannerSettings(examDay = 20_000 + 60, studyDays = setOf(1, 2, 3, 4, 5, 6, 7), minutesPerDay = 60)
        val plan = Planner.build(c, lib, ProgressState(), ps, today = 20_000)
        assertEquals(7, plan.week.size)
        assertTrue(plan.week.any { it.review })
        assertTrue(plan.week.filter { it.study }.all { it.items.isNotEmpty() })
        assertTrue(plan.phases.isNotEmpty())
        // Completing every fundamentals lesson must move that theme down the priority list.
        val done = ProgressState(completedLessons = Planner.THEMES.first { it.id == "fund" }.lessonIds.associateWith { 1L })
        val before = Planner.priorities(c, ProgressState()).indexOfFirst { it.first.id == "fund" }
        val after = Planner.priorities(c, done).indexOfFirst { it.first.id == "fund" }
        assertTrue(after > before)
        val past = Planner.build(c, lib, ProgressState(), ps.copy(examDay = 19_000), today = 20_000)
        assertTrue(past.advice.first().contains("passed"))
    }

    @Test fun searchFindsAcrossCategories() {
        val r = GlobalSearch.run("ospf", TestData.curriculum, TestData.library, TestData.study, bank, ProgressState(notes = listOf(Note("n1", "OSPF notes", "DR election", created = 0, updated = 0))))
        listOf("Lessons", "Glossary & protocols", "Flashcards", "Questions", "My notes").forEach { assertTrue("missing $it", r[it].orEmpty().isNotEmpty()) }
        assertTrue(GlobalSearch.run("", TestData.curriculum, TestData.library, TestData.study, bank, ProgressState()).isEmpty())
    }

    @Test fun progressSerializationIsBackwardCompatible() {
        val old = """{"completedLessons":{"1-01":20000},"themeMode":"dark"}"""
        val p = AppJson.decodeFromString<ProgressState>(old)
        assertEquals("dark", p.themeMode); assertTrue(p.notes.isEmpty()); assertNull(p.planner)
        val full = p.copy(notes = listOf(Note("a", "t", "b", created = 1, updated = 2)), planner = PlannerSettings(20100), lessonHighlights = setOf("1-01:simple"))
        assertEquals(full, AppJson.decodeFromString<ProgressState>(AppJson.encodeToString(ProgressState.serializer(), full)))
    }

    @Test fun noFakeAchievementsOrStats() {
        val badges = Achievements.compute(TestData.curriculum, TestData.library, bank, ProgressState())
        assertTrue(badges.none { it.earned })
        val st = Stats(TestData.curriculum, ProgressState(), TestData.library)
        assertEquals(0f, st.overallProgress); assertEquals(0, st.streak); assertNull(st.overallAccuracy)
        assertEquals(3, Achievements.longest(setOf(1L, 2L, 3L, 5L)))
    }

    @Test fun offlineTutorAnswersFromContent() {
        val c = TestData.curriculum; val s = TestData.study; val lib = TestData.library
        val a = OfflineTutor.answer(TutorMode.EXPLAIN, "native VLAN", 0, c, s, lib, bank, ProgressState())
        assertFalse(a.online); assertTrue(a.sections.isNotEmpty())
        val cmp = OfflineTutor.answer(TutorMode.COMPARE, "TCP vs UDP", 0, c, s, lib, bank, ProgressState())
        assertTrue(cmp.title.contains("TCP"))
        val sub = OfflineTutor.answer(TutorMode.SUBNET, "192.168.33.150/26", 0, c, s, lib, bank, ProgressState())
        assertTrue(sub.sections.joinToString().contains("192.168.33.191"))
    }

    @Test fun dailyChallengeIsStableForTheDay() {
        val a = DailyChallenges.build(20_000, bank, TestData.library, TestData.study, ProgressState())
        val b = DailyChallenges.build(20_000, bank, TestData.library, TestData.study, ProgressState())
        assertEquals(a.question?.id, b.question?.id); assertEquals(a.subnet.prompt, b.subnet.prompt); assertEquals(a.card?.id, b.card?.id)
        assertNotEquals(a.subnet.prompt, DailyChallenges.build(20_001, bank, TestData.library, TestData.study, ProgressState()).subnet.prompt)
    }
}
