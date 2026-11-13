package com.ccnaacademy.app.data

import kotlinx.serialization.Serializable

/** Generic content block shared by memorization guides, cheatsheets and subnetting lessons. */
@Serializable data class Block(
    val type: String, val title: String = "", val text: String = "", val items: List<String> = emptyList(),
    val headers: List<String> = emptyList(), val rows: List<List<String>> = emptyList(),
    val mnemonic: String = "", val diagram: String? = null, val tone: String = "tip",
    val id: String = "", val correctIndex: Int = -1, val rationales: List<String> = emptyList(),
    val packet: PacketWalkthrough? = null, val inspector: OutputInspection? = null
)
@Serializable data class Category(val id: String, val title: String)
@Serializable data class Card(
    val id: String, val category: String, val domainId: String, val kind: String = "term",
    val front: String, val diagram: String? = null, val answer: String, val explanation: String,
    val example: String, val tip: String
)
@Serializable data class Recall(val prompt: String, val answer: String)
@Serializable data class Guide(
    val id: String, val title: String, val domainId: String, val summary: String, val techniques: List<String>,
    val blocks: List<Block>, val recall: List<Recall>, val quiz: List<Question>
)
@Serializable data class Cheatsheet(val id: String, val title: String, val domainId: String, val tags: List<String>, val blocks: List<Block>)
@Serializable data class Compare(val aspect: String, val fast: String, val traditional: String)
@Serializable data class WorkedExample(val title: String, val steps: List<String>)
@Serializable data class Technique(
    val id: String, val title: String, val domainId: String, val method: String, val steps: List<String>,
    val example: WorkedExample, val compare: List<Compare>, val safe: List<String>, val pitfalls: List<String>,
    val generators: List<String>, val targetSeconds: Int, val questions: List<Question>
)
@Serializable data class SubnetLesson(val id: String, val title: String, val level: String, val blocks: List<Block>, val generator: String)
@Serializable data class MemoryLibrary(
    val categories: List<Category>, val cards: List<Card>, val guides: List<Guide>,
    val cheatsheets: List<Cheatsheet>, val techniques: List<Technique>, val subnetLessons: List<SubnetLesson>
) {
    val cardMap: Map<String, Card> by lazy { cards.associateBy { it.id } }
    fun category(id: String) = categories.firstOrNull { it.id == id }?.title ?: id
}

// ---- Persisted state for the revision system ----
@Serializable data class SrsState(
    val reps: Int = 0, val interval: Int = 0, val ease: Double = 2.5, val due: Long = 0,
    val lapses: Int = 0, val lastRating: Int = -1, val reviews: Int = 0, val wrong: Int = 0
) {
    val isNew get() = reviews == 0
    val mastered get() = interval >= 21
    /** Cards the student repeatedly forgets ("leeches"). */
    val leech get() = lapses >= 3
}
@Serializable data class CustomDeck(val id: String, val name: String, val cardIds: List<String> = emptyList())
@Serializable data class DrillResult(
    val kind: String, val refId: String, val level: Int, val correct: Int, val total: Int,
    val seconds: Int, val timestamp: Long
) { val percent get() = if (total == 0) 0 else correct * 100 / total }
@Serializable data class DailyPlanState(
    val day: Long, val cardTarget: Int, val guideId: String, val sheetId: String,
    val techniqueId: String, val quizDomain: String, val lessonId: String, val reason: String
)

@Serializable data class PacketDevice(val name: String, val `interface`: String, val details: String)
@Serializable data class PacketStep(
    val id: String, val title: String, val device: String, val `interface`: String,
    val action: String, val why: String, val next: String,
    val headers: Map<String, String> = emptyMap(), val condition: String = "always"
)
@Serializable data class PacketWalkthrough(
    val id: String, val title: String, val assumptions: String,
    val devices: List<PacketDevice>, val steps: List<PacketStep>, val diagramId: String = ""
)
@Serializable data class OutputField(val label: String, val healthy: String, val abnormal: String)
@Serializable data class OutputInspection(
    val id: String, val lessonId: String, val command: String, val fixture: String,
    val healthy: String, val abnormal: String, val fields: List<OutputField>,
    val question: String, val answer: String, val provenance: String
)
