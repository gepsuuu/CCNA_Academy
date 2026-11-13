package com.ccnaacademy.app.data

import kotlinx.serialization.Serializable

@Serializable data class Comparison(val id: String, val title: String, val domainId: String, val summary: String, val blocks: List<Block>, val lessons: List<String> = emptyList())
@Serializable data class Mistake(
    val id: String, val title: String, val domainId: String, val wrong: String, val right: String,
    val recognize: String, val avoid: String, val example: String, val lessonId: String
)
@Serializable data class GlossaryEntry(
    val id: String, val term: String, val full: String = "", val simple: String, val technical: String,
    val domainId: String, val related: List<String> = emptyList()
)
@Serializable data class StudyContent(val comparisons: List<Comparison>, val mistakes: List<Mistake>, val glossary: List<GlossaryEntry>) {
    val termMap: Map<String, GlossaryEntry> by lazy { glossary.associateBy { it.term.lowercase() } }
}

// ---------- persisted (inside ProgressState) ----------
@Serializable data class Note(
    val id: String, val title: String, val body: String, val topic: String = "", val domainId: String = "",
    val lessonId: String = "", val created: Long, val updated: Long
)
@Serializable data class PlannerSettings(
    val examDay: Long,                         // epochDay of the target exam
    val studyDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),   // java.time.DayOfWeek values (1 = Monday)
    val minutesPerDay: Int = 60,
    val sessionMinutes: Int = 30,              // preferred length of one study block
    val level: Int = 0,                        // 0 new to networking, 1 some experience, 2 experienced
    val targetScore: Int = 85,
    val createdDay: Long = 0
)
