package com.ccnaacademy.app

import com.ccnaacademy.app.data.*
import org.junit.Assert.*
import org.junit.Test

class ContentTest {
    private val c = TestData.curriculum
    private val diagramIds = TestData.diagrams.map { it.id }.toSet()

    @Test fun allAssetsDecode() {
        assertEquals(6, c.domains.size)
        assertTrue(c.lessons.size >= 41)
        assertTrue(TestData.library.cards.size >= 200)
        assertTrue(TestData.bank.questions.size >= 200)
        assertTrue(TestData.study.glossary.size >= 100)
        assertEquals(15, TestData.study.comparisons.size)
        assertEquals(13, TestData.study.mistakes.size)
    }

    @Test fun everyLessonIsReachableAndComplete() {
        val inChapters = c.domains.flatMap { d -> d.chapters.flatMap { it.lessonIds } }.toSet()
        c.lessons.forEach { l ->
            assertTrue("${l.id} not in a chapter", l.id in inChapters)
            assertTrue("${l.id} missing sections", l.simple.isNotBlank() && l.summary.isNotBlank() && l.deep.size >= 3 && l.quiz.isNotEmpty())
            l.diagrams.forEach { assertTrue("${l.id} → $it", it in diagramIds) }
            l.quiz.forEach { q -> assertTrue(q.id, q.answer in q.options.indices) }
        }
    }

    @Test fun referencesResolve() {
        val lessons = c.lessonMap.keys
        TestData.bank.questions.forEach { q ->
            assertTrue("${q.id} lesson ${q.lessonId}", q.lessonId.isBlank() || q.lessonId in lessons)
            q.diagram?.let { assertTrue("${q.id} diagram $it", it in diagramIds) }
        }
        TestData.study.comparisons.forEach { x -> x.lessons.forEach { assertTrue(x.id, it in lessons) }; x.blocks.forEach { b -> b.diagram?.let { assertTrue(it in diagramIds) } } }
        TestData.study.mistakes.forEach { assertTrue(it.id, it.lessonId in lessons) }
        val terms = TestData.study.glossary.map { it.term }.toSet()
        TestData.study.glossary.forEach { g -> g.related.forEach { assertTrue("${g.term} → $it", it in terms) } }
        Planner.THEMES.flatMap { it.lessonIds }.forEach { assertTrue(it, it in lessons) }
        assertEquals("Planner themes must cover every lesson", lessons, Planner.THEMES.flatMap { it.lessonIds }.toSet())
        TestData.library.cards.forEach { k -> k.diagram?.let { assertTrue(k.id, it in diagramIds) } }
    }

    @Test fun diagramsAreWellFormed() {
        TestData.diagrams.forEach { d ->
            val ids = d.nodes.map { it.id }.toSet()
            d.edges.forEach { e -> assertTrue("${d.id} edge", e.src in ids && e.dst in ids) }
            d.nodes.forEach { n -> assertTrue("${d.id}/${n.id} outside canvas", n.x + n.w <= d.width + 1 && n.y + n.h <= d.height + 1) }
        }
    }

    @Test fun noDuplicateIds() {
        fun <T> unique(l: List<T>, what: String) = assertEquals("duplicate $what", l.size, l.toSet().size)
        unique(TestData.bank.questions.map { it.id }, "question id")
        unique(TestData.bank.questions.map { it.stem }, "question stem")
        unique(TestData.library.cards.map { it.id }, "card id")
        unique(TestData.study.glossary.map { it.term.lowercase() }, "glossary term")
        unique(c.lessons.map { it.id }, "lesson id")
    }
}
