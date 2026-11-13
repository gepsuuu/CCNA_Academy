package com.ccnaacademy.app

import com.ccnaacademy.app.data.*
import kotlinx.serialization.json.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class PilotLessonTest {
    private val codec = Json { ignoreUnknownKeys = true }
    @Test fun pilotStagesDecodeAndReferencesResolve() {
        val ids = TestData.diagrams.map { it.id }.toSet()
        listOf("2-01", "3-03").forEach { id ->
            val l = TestData.curriculum.lessonMap.getValue(id)
            assertEquals(listOf("Understand", "Compare", "Visualize", "Observe", "Configure", "Verify", "Troubleshoot", "Recall"), l.learningStages.map { it.title })
            l.learningStages.flatMap { it.blocks }.mapNotNull { it.diagram }.forEach { assertTrue(it in ids) }
        }
    }
    @Test fun legacyLessonWithoutStagesStillDecodes() {
        val l = TestData.curriculum.lessonMap.getValue("2-01")
        val element = codec.parseToJsonElement(codec.encodeToString(l)).jsonObject
        val old = JsonObject(element.filterKeys { it != "learningStages" })
        val decoded = codec.decodeFromString<Lesson>(old.toString())
        assertTrue(decoded.learningStages.isEmpty())
        assertEquals(l.deep, decoded.deep)
        assertEquals(l.quiz, decoded.quiz)
    }
}
