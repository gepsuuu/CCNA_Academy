package com.ccnaacademy.app.data

import kotlinx.serialization.Serializable

/** Question types: single, multi, tf, match (left[i] ↔ right[i]), order (items in correct order). */
@Serializable data class BankQuestion(
    val id: String, val domainId: String, val objective: String = "", val topic: String,
    val difficulty: Int = 2, val format: String = "concept", val type: String, val stem: String,
    val exhibit: String? = null, val diagram: String? = null,
    val options: List<String> = emptyList(), val correct: List<Int> = emptyList(),
    val left: List<String> = emptyList(), val right: List<String> = emptyList(), val items: List<String> = emptyList(),
    val explanation: String, val whyCorrect: String = "", val whyWrong: List<String> = emptyList(),
    val concept: String = "", val tip: String = "", val lessonId: String = "",
    val verified: Boolean = false, val shuffle: Boolean = true
)
@Serializable data class BankTopic(val id: String, val title: String, val domainId: String, val lessonId: String = "")
@Serializable data class QuestionPack(
    val schema: Int = 1, val id: String, val name: String, val version: Int = 1, val category: String = "",
    val description: String = "", val author: String = "", val topics: List<BankTopic> = emptyList(),
    val questions: List<BankQuestion>
)
data class LoadedPack(val pack: QuestionPack, val builtin: Boolean, val issues: List<String> = emptyList())

/** All installed packs; [disabled] pack ids are excluded from practice. */
class QuestionBank(val packs: List<LoadedPack>, val disabled: Set<String> = emptySet()) {
    val enabledPacks = packs.filter { it.pack.id !in disabled }
    val questions: List<BankQuestion> = enabledPacks.flatMap { it.pack.questions }
    val byId: Map<String, BankQuestion> = packs.flatMap { it.pack.questions }.associateBy { it.id }
    val topics: Map<String, BankTopic> = packs.flatMap { it.pack.topics }.associateBy { it.id }
    val packOf: Map<String, String> = packs.flatMap { lp -> lp.pack.questions.map { it.id to lp.pack.id } }.toMap()
    fun topicTitle(id: String) = topics[id]?.title ?: id.replace('-', ' ').replaceFirstChar { it.uppercase() }
    fun lessonFor(topic: String) = topics[topic]?.lessonId.orEmpty()
    fun withDisabled(d: Set<String>) = QuestionBank(packs, d)
}

// ---------- persisted ----------
@Serializable data class BankStat(
    val domainId: String, val topic: String, val lessonId: String = "", val correct: Int = 0, val incorrect: Int = 0,
    val lastCorrect: Boolean = false, val lastTs: Long = 0, val totalMs: Long = 0
) { val attempts get() = correct + incorrect }

@Serializable data class SessionResult(
    val id: Long, val mode: String, val title: String, val exam: Boolean, val total: Int, val answered: Int, val correct: Int,
    val seconds: Int, val limitSec: Int = 0,
    val domainCorrect: Map<String, Int> = emptyMap(), val domainTotal: Map<String, Int> = emptyMap(),
    val topicCorrect: Map<String, Int> = emptyMap(), val topicTotal: Map<String, Int> = emptyMap(),
    val wrongIds: List<String> = emptyList()
) {
    val incorrect get() = total - correct
    val percent get() = if (total == 0) 0 else correct * 100 / total
    val avgSeconds get() = if (total == 0) 0 else seconds / total
}

/** A question as presented in one session: display orders are randomized but answers are stored in ORIGINAL indices. */
data class SessionItem(val q: BankQuestion, val optOrder: List<Int>, val rightOrder: List<Int>, val initOrder: List<Int>)
