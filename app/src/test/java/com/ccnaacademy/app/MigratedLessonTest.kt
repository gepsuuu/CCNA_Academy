package com.ccnaacademy.app
import com.ccnaacademy.app.data.*
import kotlinx.serialization.decodeFromString
import org.junit.Assert.*
import org.junit.Test
class MigratedLessonTest {
    @Test fun allLessonsHaveStagesAndStableBlocks() {
        val lessons = TestData.curriculum.lessons
        assertEquals(41, lessons.size)
        val ids = lessons.flatMap { it.learningStages }.flatMap { it.blocks }.map { it.id }
        assertTrue(ids.all { it.isNotBlank() })
        assertEquals(ids.size, ids.toSet().size)
        lessons.forEach { l ->
            assertEquals(8, l.learningStages.size)
            l.learningStages.forEach { s -> assertTrue(if (s.applicable) s.blocks.isNotEmpty() else s.reason.isNotBlank()) }
        }
    }
    @Test fun oldBlockStillDecodes() {
        val block = AppJson.decodeFromString<Block>("""{"type":"text","text":"legacy"}""")
        assertEquals("", block.id)
        assertEquals(-1, block.correctIndex)
        assertTrue(block.rationales.isEmpty())
    }
    @Test fun progressiveSearchTextContainsNewNarrative() {
        val lesson = TestData.curriculum.lessonMap.getValue("1-01")
        assertTrue(lesson.progressiveText().contains("power budget", ignoreCase = true))
    }
}
