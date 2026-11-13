package com.ccnaacademy.app.data

import kotlinx.serialization.Serializable

@Serializable data class Blueprint(val exam: String, val version: String, val note: String)
@Serializable data class Chapter(val id: String, val title: String, val lessonIds: List<String>)
@Serializable data class Domain(
    val id: String, val number: String, val title: String, val weight: Int,
    val description: String, val chapters: List<Chapter>
)
@Serializable data class DeepSection(val level: String, val heading: String, val text: String)
@Serializable data class Example(val title: String, val text: String)
@Serializable data class Term(val term: String, val definition: String)
@Serializable data class Question(
    val id: String, val question: String, val options: List<String>, val answer: Int,
    val explanation: String, val lessonId: String, val domainId: String
)
@Serializable data class Flashcard(val id: String, val front: String, val back: String, val lessonId: String, val domainId: String)
@Serializable data class LearningStage(
    val id: String, val title: String, val applicable: Boolean = true,
    val reason: String = "", val blocks: List<Block> = emptyList()
)
@Serializable data class Lesson(
    val id: String, val domainId: String, val chapterId: String, val title: String,
    val objectives: List<String>, val examRequired: Boolean = true, val minutes: Int,
    val simple: String, val deep: List<DeepSection>, val why: String,
    val examples: List<Example>, val mistakes: List<String>, val terms: List<Term>,
    val memorize: List<String>, val examNotes: List<String>, val supplementary: List<String> = emptyList(),
    val summary: String, val diagrams: List<String> = emptyList(), val quiz: List<Question>,
    val learningStages: List<LearningStage> = emptyList()
)
@Serializable data class Curriculum(
    val blueprint: Blueprint, val domains: List<Domain>, val lessons: List<Lesson>,
    val flashcards: List<Flashcard> = emptyList(), val tips: List<String>
) {
    val lessonMap: Map<String, Lesson> by lazy { lessons.associateBy { it.id } }
    val allQuestions: List<Question> by lazy { lessons.flatMap { it.quiz } }
    fun domain(id: String) = domains.first { it.id == id }
    fun lessonsIn(domainId: String) = lessons.filter { it.domainId == domainId }
    fun chapters() = domains.flatMap { it.chapters }
}

// ---------- Diagrams ----------
@Serializable data class DNode(
    val id: String, val x: Float, val y: Float, val w: Float, val h: Float,
    val label: String, val sub: String = "", val kind: String = "neutral", val shape: String = "round"
)
@Serializable data class DEdge(
    val src: String, val dst: String, val label: String = "", val style: String = "solid",
    val arrow: String = "end", val kind: String = "neutral"
)
@Serializable data class DText(val x: Float, val y: Float, val text: String, val size: Float = 22f, val bold: Boolean = false, val kind: String = "text")
@Serializable data class LegendItem(val kind: String, val label: String)
@Serializable data class Diagram(
    val id: String, val title: String, val width: Float, val height: Float,
    val nodes: List<DNode>, val edges: List<DEdge> = emptyList(), val texts: List<DText> = emptyList(),
    val legend: List<LegendItem> = emptyList(), val explanation: String = ""
)
@Serializable data class DiagramFile(val diagrams: List<Diagram>)

// ---------- Progress (persisted locally) ----------
@Serializable data class QuestionStat(val correct: Int = 0, val incorrect: Int = 0, val lastCorrect: Boolean = false)
@Serializable data class CardState(val box: Int = 0, val dueEpochDay: Long = 0) {
    val mastered get() = box >= 4
}
@Serializable data class ReviewState(val stage: Int = 0, val dueEpochDay: Long = 0)
@Serializable data class ExamResult(
    val timestamp: Long, val total: Int, val correct: Int, val durationSec: Int,
    val domainCorrect: Map<String, Int> = emptyMap(), val domainTotal: Map<String, Int> = emptyMap()
) {
    val percent get() = if (total == 0) 0 else correct * 100 / total
}
@Serializable data class ProgressState(
    val completedLessons: Map<String, Long> = emptyMap(),   // lessonId -> epochDay completed
    val lastLessonId: String? = null,
    val answers: Map<String, QuestionStat> = emptyMap(),
    val cards: Map<String, CardState> = emptyMap(),
    val studySecondsByDay: Map<Long, Long> = emptyMap(),    // epochDay -> seconds
    val reviews: Map<String, ReviewState> = emptyMap(),
    val exams: List<ExamResult> = emptyList(),
    val themeMode: String = "system",                        // system | light | dark
    val dynamicColor: Boolean = false,
    // ---- Memorization & revision system (v1.1) ----
    val srs: Map<String, SrsState> = emptyMap(),
    val favorites: Set<String> = emptySet(),
    val userDifficulty: Map<String, String> = emptyMap(),    // cardId -> easy | medium | hard
    val customDecks: List<CustomDeck> = emptyList(),
    val newIntroduced: Map<Long, Int> = emptyMap(),          // epochDay -> new cards started
    val reviewsByDay: Map<Long, Int> = emptyMap(),           // epochDay -> card reviews
    val newPerDay: Int = 15,
    val sheetBookmarks: Set<String> = emptySet(),
    val highlights: Set<String> = emptySet(),                // "sheetId:block:item"
    val guideVisits: Map<String, Long> = emptyMap(),         // guideId -> last epochDay
    val recallStats: Map<String, QuestionStat> = emptyMap(), // "guideId:index"
    val challengeScores: Map<Long, Int> = emptyMap(),        // epochDay -> % score
    val drills: List<DrillResult> = emptyList(),
    val genStats: Map<String, QuestionStat> = emptyMap(),    // generator type -> accuracy
    val dailyDone: Map<Long, Set<String>> = emptyMap(),
    val plan: DailyPlanState? = null,
    // ---- Question bank & exam simulator (v1.2) ----
    val bankStats: Map<String, BankStat> = emptyMap(),
    val bankBookmarks: Set<String> = emptySet(),
    val sessions: List<SessionResult> = emptyList(),
    val disabledPacks: Set<String> = emptySet(),
    // ---- Personalization, notes and motivation (v1.3) ----
    val planner: PlannerSettings? = null,
    val notes: List<Note> = emptyList(),
    val lessonBookmarks: Set<String> = emptySet(),
    val diagramBookmarks: Set<String> = emptySet(),
    val lessonHighlights: Set<String> = emptySet(),          // "lessonId:section"
    val challengeDone: Map<Long, Set<String>> = emptyMap(),   // epochDay -> completed daily-challenge parts
    val badges: Map<String, Long> = emptyMap(),               // badgeId -> first earned (epoch ms)
    val conceptsMemorized: Set<String> = emptySet(),          // glossary ids self-marked as memorized
    // ---- CLI labs, animations and interactive lessons (v1.4). All fields default, so v1.0-v1.3 data loads unchanged. ----
    val labs: Map<String, LabSave> = emptyMap(),              // labId -> saved lab (resumable)
    val skills: Map<String, QuestionStat> = emptyMap(),       // concept | cli | output | troubleshoot
    val labMistakes: List<LabMistake> = emptyList(),          // mistake notebook (last 200)
    val activities: Map<String, QuestionStat> = emptyMap(),   // output-interpretation, table recall and teach-back activities
    val reducedMotion: Boolean = false,
    val animSpeed: Float = 1f
)

// ---------- CLI labs (v1.4) ----------
@Serializable data class TranscriptLine(val text: String, val kind: String)
@Serializable data class LabSave(
    val state: com.ccnaacademy.app.lab.LabState,
    val transcripts: Map<String, List<TranscriptLine>> = emptyMap(),  // deviceId -> terminal lines
    val device: String = "",
    val hints: Map<String, Int> = emptyMap(),                         // taskId -> highest hint level revealed (0-3)
    val started: Long = 0,
    val completedAt: Long? = null,
    val completedWithHints: Int? = null,
    val checks: Int = 0                                               // challenge: number of "Check my work" attempts
) {
    val hintsUsed get() = hints.values.sum()
}
@Serializable data class LabMistake(
    val ts: Long, val labId: String, val device: String, val command: String, val error: String, val explanation: String, val fixed: Boolean = false
)

/** Search/tutor text includes hidden explanations and command examples. */
fun Lesson.progressiveText(): String = learningStages.joinToString("\n") { stage ->
    stage.title + "\n" + stage.reason + "\n" + stage.blocks.joinToString("\n") { b ->
        listOf(b.title, b.text, b.items.joinToString("\n"), b.rationales.joinToString("\n"),
            b.rows.joinToString("\n") { it.joinToString(" ") },
            b.packet?.let { p -> p.assumptions + "\n" + p.steps.joinToString("\n") { it.title + " " + it.action + " " + it.why + " " + it.next + " " + it.headers.entries.joinToString(" ") { h -> h.key + " " + h.value } } }.orEmpty(),
            b.inspector?.let { o -> listOf(o.command, o.fixture, o.healthy, o.abnormal, o.question, o.answer, o.fields.joinToString(" ") { f -> f.label + " " + f.healthy + " " + f.abnormal }).joinToString("\n") }.orEmpty()).joinToString("\n")
    }
}
