package com.ccnaacademy.app

import com.ccnaacademy.app.data.*
import kotlinx.serialization.decodeFromString
import org.junit.Assert.*
import org.junit.Test

/** 1.5.0-rc2 regression tests: stage navigation, diagram text alternatives, highlight resolution, backup compatibility. */
class LessonPresentationTest {
    private val lessons get() = TestData.curriculum.lessons

    @Test fun stageJumpTargetsMatchLazyColumnEmissionOrder() {
        lessons.forEach { l ->
            // Rebuild LessonScreen's emission order independently: header, notice, then stage header + applicable blocks.
            val emitted = mutableListOf("header", "notice")
            l.learningStages.forEach { s ->
                emitted += "stage-${s.id}"
                if (s.applicable) s.blocks.forEach { emitted += "content-${it.id}" }
            }
            l.learningStages.forEachIndexed { i, s ->
                assertEquals(l.id + " " + s.title, "stage-${s.id}", emitted[stageItemIndex(l.learningStages, i)])
            }
        }
    }

    @Test fun stageJumpRejectsOutOfRange() {
        val stages = lessons.first().learningStages
        assertEquals(-1, stageItemIndex(stages, -1))
        assertEquals(-1, stageItemIndex(stages, stages.size))
        assertEquals(LESSON_LEADING_ITEMS, stageItemIndex(stages, 0))
    }

    @Test fun notApplicableStageContributesOnlyItsHeader() {
        val stages = listOf(
            LearningStage("a", "A", true, "", listOf(Block("text", id = "1"), Block("text", id = "2"))),
            LearningStage("b", "B", false, "No CLI applies", listOf(Block("text", id = "3"))),
            LearningStage("c", "C"))
        assertEquals(2, stageItemIndex(stages, 0))
        assertEquals(5, stageItemIndex(stages, 1))
        assertEquals(6, stageItemIndex(stages, 2))
    }

    @Test fun everyDiagramHasCompleteTextAlternative() {
        TestData.diagrams.forEach { d ->
            val alt = diagramTextAlternative(d)
            assertTrue(d.id, alt.startsWith("Diagram: " + d.title))
            d.nodes.forEach { n -> assertTrue("${d.id} ${n.id}", alt.contains(n.label)) }
            d.edges.filter { it.label.isNotBlank() }.forEach { e -> assertTrue("${d.id} edge", alt.contains(e.label)) }
        }
    }

    @Test fun correctedMechanismDiagramsTeachTheirLesson() {
        val byId = TestData.diagrams.associateBy { it.id }
        assertTrue(diagramTextAlternative(byId.getValue("mechanism_1-07")).contains("0xAC"))
        assertTrue(diagramTextAlternative(byId.getValue("mechanism_2-09")).contains("line vty 0 4"))
        assertTrue(diagramTextAlternative(byId.getValue("mechanism_6-04")).contains("terraform plan"))
        // Explanations are visible text beneath the canvas, not part of the content description.
        assertTrue(byId.getValue("mechanism_1-08").explanation.contains("RFC1918"))
        assertFalse(diagramTextAlternative(byId.getValue("mechanism_1-07")).contains("RFC1918"))
        // Every mechanism diagram is placed only in the lesson named by its ID.
        lessons.forEach { l ->
            l.learningStages.flatMap { it.blocks }.mapNotNull { it.diagram }.filter { it.startsWith("mechanism_") }
                .forEach { assertEquals("mechanism_" + l.id, it) }
        }
    }

    @Test fun everyBlockHighlightResolvesAndLegacyKeysStillWork() {
        lessons.forEach { l ->
            l.learningStages.flatMap { it.blocks }.forEach { b ->
                val text = lessonSectionText(l, "block:" + b.id)
                assertFalse(l.id + " " + b.id, text.startsWith("Preserved highlight"))
                assertTrue(l.id + " " + b.id, text.isNotBlank())
            }
            assertEquals(l.simple, lessonSectionText(l, "simple"))
            assertEquals(l.summary, lessonSectionText(l, "summary"))
            l.deep.indices.forEach { assertEquals(l.deep[it].text, lessonSectionText(l, "deep$it")) }
            l.examples.indices.forEach { assertTrue(lessonSectionText(l, "ex$it").endsWith(l.examples[it].text)) }
        }
        val l = lessons.first()
        assertTrue(lessonSectionText(l, "block:removed-in-future").startsWith("Preserved highlight"))
        assertEquals("", lessonSectionText(l, "deep999"))
    }

    @Test fun preV14BackupDecodesWithDefaultsAndKeepsUserData() {
        // Shape of a v1.3 backup: no lab/skills/activities/reducedMotion fields, plus an unknown future key.
        val json = """{"completedLessons":{"2-01":20000},"lessonHighlights":["2-01:deep0","3-03:simple"],
            "lessonBookmarks":["3-03"],"diagramBookmarks":["vlan"],"notes":[],"themeMode":"dark",
            "futureField":{"x":1}}"""
        val p = AppJson.decodeFromString<ProgressState>(json)
        assertEquals(20000L, p.completedLessons["2-01"])
        assertEquals(setOf("2-01:deep0", "3-03:simple"), p.lessonHighlights)
        assertEquals("dark", p.themeMode)
        assertFalse(p.reducedMotion)
        assertTrue(p.labs.isEmpty())
        // Legacy highlight keys resolve against current content.
        val m = TestData.curriculum.lessonMap
        assertEquals(m.getValue("2-01").deep[0].text, lessonSectionText(m.getValue("2-01"), "deep0"))
        // Round trip preserves everything that was decoded.
        assertEquals(p, AppJson.decodeFromString<ProgressState>(AppJson.encodeToString(ProgressState.serializer(), p)))
    }

    @Test fun corruptBackupIsRejectedNotSilentlyEmptied() {
        listOf("", "not json", "[1,2]", """{"completedLessons":"oops"}""").forEach { bad ->
            assertTrue(bad, runCatching { AppJson.decodeFromString<ProgressState>(bad) }.isFailure)
        }
    }

    @Test fun diagramBookmarksPointAtExistingDiagrams() {
        val ids = TestData.diagrams.map { it.id }.toSet()
        lessons.flatMap { it.diagrams }.forEach { assertTrue(it, it in ids) }
        lessons.flatMap { it.learningStages }.flatMap { it.blocks }.forEach { b ->
            b.diagram?.let { assertTrue(it, it in ids) }
            b.packet?.let { assertTrue(it.diagramId, it.diagramId in ids) }
        }
    }
}
