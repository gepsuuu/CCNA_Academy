package com.ccnaacademy.app
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.screens.lessonSectionText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test
class InteractiveTeachingTest {
    @Test fun newPayloadsDecode() {
        val blocks = TestData.curriculum.lessons.flatMap { it.learningStages }.flatMap { it.blocks }
        assertEquals(6, blocks.count { it.packet != null })
        assertEquals(10, blocks.count { it.inspector != null })
        blocks.mapNotNull { it.packet }.forEach { p ->
            assertTrue(p.steps.isNotEmpty())
            assertEquals(p.steps.size, p.steps.map { it.id }.toSet().size)
            assertTrue(TestData.diagrams.any { it.id == p.diagramId })
        }
    }
    @Test fun oldBlockDefaults() {
        val b = AppJson.decodeFromString<Block>("""{"type":"text","text":"legacy"}""")
        assertNull(b.packet); assertNull(b.inspector)
    }
    @Test fun existingProgressRoundTrip() {
        val old = """{"completedLessons":["2-01"],"lessonBookmarks":["3-03"],"lessonHighlights":["2-01:deep0","3-03:block:3-03-interactive-check"],"reviews":{"2-01":{"stage":2,"dueEpochDay":20000}}}"""
        val state = AppJson.decodeFromString<ProgressState>(old)
        assertTrue("2-01" in state.completedLessons)
        assertTrue("3-03" in state.lessonBookmarks)
        assertEquals(2, state.lessonHighlights.size)
        assertEquals(state, AppJson.decodeFromString<ProgressState>(AppJson.encodeToString(state)))
    }
    @Test fun oldAndNewHighlightText() {
        val l = TestData.curriculum.lessonMap.getValue("2-01")
        assertEquals(l.deep[0].text, lessonSectionText(l, "deep0"))
        assertTrue(lessonSectionText(l, "block:2-01-interactive-check").contains("Layer 3"))
    }
    @Test fun activitySearchText() {
        val l = TestData.curriculum.lessonMap.getValue("3-03")
        assertTrue(l.progressiveText().contains("ExStart"))
        assertTrue(l.progressiveText().contains("show ip ospf neighbor"))
    }
}
