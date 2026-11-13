package com.ccnaacademy.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Optional online AI tutor. The app ships with NO AI service and NO API key. If the student configures an
 * OpenAI-compatible chat-completions endpoint and key, requests go there; otherwise the tutor answers from the
 * offline knowledge base and labels every answer as offline content.
 */
data class TutorConfig(val enabled: Boolean = false, val endpoint: String = "", val model: String = "", val apiKey: String = "") {
    val usable get() = enabled && endpoint.startsWith("https://") && model.isNotBlank() && apiKey.isNotBlank()
    val host get() = runCatching { URL(endpoint).host }.getOrDefault("")
}

// Separate store so the API key is never included in progress backups or exports (excluded in backup rules too).
private val Context.tutorStore by preferencesDataStore(name = "tutor_settings")
private val K_ON = booleanPreferencesKey("enabled"); private val K_URL = stringPreferencesKey("endpoint")
private val K_MODEL = stringPreferencesKey("model"); private val K_KEY = stringPreferencesKey("api_key")

class TutorRepository(private val context: Context) {
    val config: Flow<TutorConfig> = context.tutorStore.data.map { TutorConfig(it[K_ON] ?: false, it[K_URL] ?: "", it[K_MODEL] ?: "", it[K_KEY] ?: "") }
    suspend fun save(c: TutorConfig) = context.tutorStore.edit { it[K_ON] = c.enabled; it[K_URL] = c.endpoint.trim(); it[K_MODEL] = c.model.trim(); it[K_KEY] = c.apiKey.trim() }

    suspend fun ask(c: TutorConfig, system: String, user: String): String = withContext(Dispatchers.IO) {
        val body = buildJsonObject {
            put("model", c.model); put("temperature", 0.2)
            putJsonArray("messages") {
                addJsonObject { put("role", "system"); put("content", system) }
                addJsonObject { put("role", "user"); put("content", user) }
            }
        }.toString()
        val conn = URL(c.endpoint).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"; conn.doOutput = true; conn.connectTimeout = 15_000; conn.readTimeout = 60_000
            conn.setRequestProperty("Content-Type", "application/json"); conn.setRequestProperty("Authorization", "Bearer ${c.apiKey}")
            conn.outputStream.use { it.write(body.toByteArray()) }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() } ?: ""
            if (code !in 200..299) throw IllegalStateException(when (code) {
                401, 403 -> "The AI service rejected the API key (HTTP $code)."
                404 -> "Endpoint or model not found (HTTP 404). Check the URL and model name."
                429 -> "The AI service is rate-limiting requests (HTTP 429). Try again later."
                else -> "AI service error (HTTP $code)."
            })
            val root = AppJson.parseToJsonElement(text).jsonObject
            root["choices"]?.jsonArray?.firstOrNull()?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.contentOrNull
                ?: throw IllegalStateException("Unexpected response format from the AI service.")
        } finally { conn.disconnect() }
    }
}

enum class TutorMode(val label: String, val hint: String) {
    EXPLAIN("Explain simply", "e.g. What is a native VLAN?"),
    LEVEL("Explore explanation depth", "e.g. OSPF DR election"),
    ASK("Ask a question", "e.g. Why does OSPF need a router ID?"),
    WHY("Why is this answer correct?", "Pick a question you answered"),
    GENERATE("Practice questions", "e.g. ACLs"),
    SUBNET("Subnetting step by step", "e.g. 172.16.70.200/21"),
    COMPARE("Compare protocols", "e.g. RADIUS vs TACACS+"),
    EXAMPLES("Give examples", "e.g. PAT"),
    SUMMARIZE("Summarize a lesson", "Pick a lesson"),
    NOTES("My revision notes", "Built from your weak topics")
}

/** Rendered answer: [online] is true only when text came from the configured AI service. */
data class TutorAnswer(val title: String, val sections: List<Pair<String, String>>, val online: Boolean, val sources: List<Pair<String, String>> = emptyList()) {
    fun asText() = (listOf(title) + sections.map { (h, b) -> if (h.isBlank()) b else "$h\n$b" }).joinToString("\n\n")
}

object OfflineTutor {
    const val SYSTEM = "You are a precise Cisco CCNA 200-301 (v1.1 exam topics) networking tutor. Prioritize technical accuracy over fluency. " +
        "Use correct Cisco IOS syntax and standard protocol facts (ports, ADs, timers). If you are not certain, say so instead of guessing. " +
        "Do not invent commands, standards or exam requirements. Distinguish exam-required knowledge from extra detail. Keep answers concise and structured."

    private fun terms(q: String) = q.lowercase().split(Regex("[^a-z0-9./:+-]+")).filter { it.length >= 2 && it !in STOP }
    private val STOP = setOf("what", "is", "the", "a", "an", "of", "how", "does", "do", "why", "and", "to", "in", "on", "for", "vs", "versus", "explain", "me", "with", "are", "between", "difference")

    fun bestGlossary(q: String, s: StudyContent): GlossaryEntry? {
        val t = terms(q); if (t.isEmpty()) return null
        return s.glossary.maxByOrNull { g ->
            val term = g.term.lowercase(); val full = g.full.lowercase()
            (if (term in t || t.joinToString(" ") == term) 100 else 0) + (if (full.isNotBlank() && t.joinToString(" ") in full) 60 else 0) +
                t.count { it in term || it in full } * 20 + t.count { it in g.simple.lowercase() || it in g.technical.lowercase() } * 3
        }?.takeIf { g -> t.any { it in g.term.lowercase() || it in g.full.lowercase() || it in g.simple.lowercase() } }
    }

    fun bestLesson(q: String, c: Curriculum): Lesson? {
        val t = terms(q); if (t.isEmpty()) return null
        return c.lessons.map { l ->
            val title = l.title.lowercase()
            val body = (listOf(l.simple, l.summary, l.progressiveText()) + l.deep.map { it.text } + l.terms.map { it.term + " " + it.definition }).joinToString(" ").lowercase()
            l to (t.count { it in title } * 30 + t.sumOf { Regex(Regex.escape(it)).findAll(body).count().coerceAtMost(8) })
        }.filter { it.second > 0 }.maxByOrNull { it.second }?.first
    }

    fun answer(mode: TutorMode, input: String, level: Int, c: Curriculum, s: StudyContent, lib: MemoryLibrary, bank: QuestionBank, p: ProgressState): TutorAnswer {
        val g = bestGlossary(input, s); val l = bestLesson(input, c)
        fun src(): List<Pair<String, String>> = listOfNotNull(l?.let { "Lesson: ${it.title}" to "lesson/${it.id}" }, g?.let { "Glossary: ${it.term}" to "glossary?term=${it.id}" })
        return when (mode) {
            TutorMode.EXPLAIN, TutorMode.ASK -> {
                if (g == null && l == null) return notFound(input)
                val secs = mutableListOf<Pair<String, String>>()
                g?.let { secs += "${it.term}${if (it.full.isNotBlank()) " — ${it.full}" else ""}" to it.simple; if (mode == TutorMode.ASK) secs += "Technical detail" to it.technical }
                l?.let { lesson ->
                    val tokens = terms(input)
                    val blocks = lesson.learningStages.flatMap { it.blocks }.filter { it.type != "check" }
                    val relevant = blocks.maxByOrNull { b -> tokens.count { token -> token in (b.title + " " + b.text + " " + b.items.joinToString(" ")).lowercase() } }
                    if (relevant != null) secs += relevant.title to (relevant.text + if (relevant.items.isNotEmpty()) "\n" + relevant.items.joinToString("\n") else "")
                }
                l?.let { secs += "From the lesson “${it.title}”" to it.simple; if (mode == TutorMode.ASK) secs += "Why it matters" to it.why }
                TutorAnswer("Offline knowledge base", secs, false, src())
            }
            TutorMode.LEVEL -> {
                val lesson = l ?: return notFound(input)
                val depth = level.coerceIn(0, 2)
                val blocks = lesson.learningStages.filter { it.title in if (depth == 0) listOf("Understand") else if (depth == 1) listOf("Understand", "Observe") else listOf("Understand", "Observe", "Configure", "Verify") }.flatMap { it.blocks }
                val sections = blocks.filter { depth > 0 || it.type == "text" }.map { b ->
                    b.title to (b.text + if (b.items.isNotEmpty()) "\n" + b.items.joinToString("\n") else "")
                }
                TutorAnswer("Offline knowledge base: progressive explanation", sections.ifEmpty { listOf("Core explanation" to lesson.simple) }, false, src())
            }
            TutorMode.EXAMPLES -> {
                val lesson = l ?: return notFound(input)
                TutorAnswer("Examples from “${lesson.title}”", lesson.examples.map { it.title to it.text }, false, src())
            }
            TutorMode.COMPARE -> compare(input, s, c)
            TutorMode.SUBNET -> {
                val steps = Gen.explainSubnet(input)
                    ?: return TutorAnswer("Enter an address with a prefix", listOf("" to "Type something like 192.168.33.150/26 and I will work through the block-size method step by step."), false)
                TutorAnswer("Subnetting step by step (calculated on-device)", listOf("" to steps.joinToString("\n") { "• $it" }), false)
            }
            TutorMode.SUMMARIZE -> {
                val lesson = c.lessonMap[input] ?: l ?: return notFound(input)
                TutorAnswer("Summary: ${lesson.title}", listOf("In one paragraph" to lesson.summary, "Must memorize" to lesson.memorize.joinToString("\n• ", "• "),
                    "Exam focus" to lesson.examNotes.joinToString("\n• ", "• "), "Common mistakes" to lesson.mistakes.joinToString("\n• ", "• ")), false,
                    listOf("Open lesson" to "lesson/${lesson.id}"))
            }
            TutorMode.WHY -> {
                val q = bank.byId[input] ?: return TutorAnswer("Choose a question", listOf("" to "Pick a question from the list above."), false)
                val correct = when (q.type) { "match" -> q.left.zip(q.right).joinToString("\n") { "${it.first} → ${it.second}" }; "order" -> q.items.joinToString(" → ")
                    else -> q.correct.joinToString("\n") { q.options[it] } }
                TutorAnswer("Why the answer is correct", listOfNotNull("Question" to q.stem, "Correct answer" to correct, "Explanation" to q.explanation,
                    q.whyCorrect.takeIf { it.isNotBlank() }?.let { "Why it is correct" to it },
                    q.whyWrong.withIndex().filter { it.value.isNotBlank() }.joinToString("\n") { "• ${q.options[it.index]}: ${it.value}" }.takeIf { it.isNotBlank() }?.let { "Why the others are wrong" to it },
                    q.tip.takeIf { it.isNotBlank() }?.let { "Memory tip" to it }), false, listOf("Open the related lesson" to "lesson/${q.lessonId}"))
            }
            TutorMode.GENERATE -> {
                val t = terms(input)
                val matches = bank.questions.filter { q -> t.isEmpty() || t.any { it in q.stem.lowercase() || it in q.concept.lowercase() || it in q.topic } }
                    .sortedBy { p.bankStats[it.id]?.attempts ?: 0 }.take(5)
                val gen = if (t.any { it.contains("subnet") || it.contains("cidr") || it.contains("mask") || it.contains("wildcard") })
                    List(3) { Gen.make(listOf("network", "broadcast", "wildcard", "hosts")[it % 4], 1) } else emptyList()
                if (matches.isEmpty() && gen.isEmpty()) return notFound(input)
                TutorAnswer("Practice set (offline: from the verified bank and on-device generators — not AI-generated)",
                    matches.mapIndexed { i, q -> "Question ${i + 1}" to q.stem + if (q.options.isNotEmpty() && q.type != "tf") "\n" + q.options.joinToString("\n") { "○ $it" } else "" } +
                        gen.mapIndexed { i, pr -> "Calculation ${i + 1}" to pr.prompt },
                    false, matches.map { "Answer: ${it.stem.take(40)}…" to "question/${it.id}" } + (if (gen.isNotEmpty()) listOf("Check calculations in the subnetting center" to "subnet") else emptyList()))
            }
            TutorMode.NOTES -> revisionNotes(c, s, bank, p)
        }
    }

    fun compare(input: String, s: StudyContent, c: Curriculum): TutorAnswer {
        val t = terms(input)
        val cmp = s.comparisons.maxByOrNull { x -> t.count { it in x.title.lowercase() } }?.takeIf { x -> t.count { it in x.title.lowercase() } >= minOf(2, t.size) }
        if (cmp != null) {
            val tables = cmp.blocks.filter { it.type == "table" }.joinToString("\n\n") { b -> (listOf(b.headers) + b.rows).joinToString("\n") { it.joinToString(" | ") } }
            return TutorAnswer(cmp.title, listOf("" to cmp.summary, "Comparison" to tables), false, listOf("Open full comparison" to "compare/${cmp.id}"))
        }
        val parts = input.split(Regex("(?i)\\s+(vs\\.?|versus|and|or)\\s+|,"))
        val entries = parts.mapNotNull { bestGlossary(it, s) }.distinct()
        if (entries.size >= 2) return TutorAnswer("${entries[0].term} vs ${entries[1].term}", entries.take(3).map { "${it.term}${if (it.full.isNotBlank()) " (${it.full})" else ""}" to "${it.simple}\n${it.technical}" },
            false, entries.take(3).map { "Glossary: ${it.term}" to "glossary?term=${it.id}" })
        return notFound(input)
    }

    fun revisionNotes(c: Curriculum, s: StudyContent, bank: QuestionBank, p: ProgressState): TutorAnswer {
        val weak = BankEngine.weakTopics(p).take(4)
        val lessons = weak.mapNotNull { c.lessonMap[bank.lessonFor(it.topic)] }.distinct().ifEmpty {
            c.lessons.filter { it.id in p.completedLessons }.takeLast(3).ifEmpty { c.lessons.take(2) } }
        val secs = lessons.map { l ->
            val mistakes = s.mistakes.filter { it.lessonId == l.id }.joinToString("\n") { "⚠ ${it.title}: ${it.right}" }
            l.title to (l.memorize.joinToString("\n") { "• $it" } + if (mistakes.isNotBlank()) "\n$mistakes" else "")
        }
        val why = if (weak.isNotEmpty()) "Based on your weakest question-bank topics: ${weak.joinToString { bank.topicTitle(it.topic) }}." else "Answer more practice questions to personalize these notes; showing recent lessons."
        return TutorAnswer("Personal revision notes", listOf("" to why) + secs, false, lessons.map { "Lesson: ${it.title}" to "lesson/${it.id}" })
    }

    private fun notFound(q: String) = TutorAnswer("No offline match", listOf("" to "I couldn't find “$q” in the offline lessons, glossary or comparisons. Try a key term such as OSPF, VLAN, NAT or ACL, or use global search."), false)

    /** Builds the prompt sent to an online AI service, grounded with matching offline content. */
    fun onlinePrompt(mode: TutorMode, input: String, level: Int, c: Curriculum, s: StudyContent, bank: QuestionBank, p: ProgressState): String {
        val g = bestGlossary(input, s); val l = c.lessonMap[input] ?: bestLesson(input, c)
        val ctx = buildString {
            g?.let { append("Reference glossary entry: ${it.term}: ${it.technical}\n") }
            l?.let { append("Reference lesson '${it.title}': ${it.summary}\nKey facts: ${it.memorize.joinToString("; ")}\n") }
        }
        val lv = listOf("a complete beginner", "an intermediate student", "a student at CCNA exam level")[level.coerceIn(0, 2)]
        return when (mode) {
            TutorMode.EXPLAIN -> "Explain in simple language for a beginner: $input"
            TutorMode.LEVEL -> "Explain for $lv: $input"
            TutorMode.ASK -> "Answer this CCNA question: $input"
            TutorMode.WHY -> bank.byId[input]?.let { q -> "Explain why the correct answer is correct and why each other option is wrong.\nQuestion: ${q.stem}\n" +
                (q.exhibit?.let { "Exhibit:\n$it\n" } ?: "") + "Options: ${q.options.joinToString(" | ")}\nCorrect: ${q.correct.joinToString { q.options[it] }}\nReference explanation: ${q.explanation}" } ?: input
            TutorMode.GENERATE -> "Write 3 original CCNA 200-301 multiple-choice practice questions about: $input. For each give 4 options, the correct answer, and a one-paragraph explanation. Only use facts you are certain of."
            TutorMode.SUBNET -> "Solve step by step with the block-size method and show network, broadcast, first/last host and usable hosts: $input"
            TutorMode.COMPARE -> "Compare for the CCNA exam, using a table followed by exam tips: $input"
            TutorMode.EXAMPLES -> "Give 3 practical, realistic enterprise examples of: $input"
            TutorMode.SUMMARIZE -> l?.let { "Summarize this lesson into concise revision notes:\n${it.title}\n${it.simple}\n${it.deep.joinToString("\n") { d -> d.text }}\n${it.summary}" } ?: "Summarize: $input"
            TutorMode.NOTES -> "Create concise personalized CCNA revision notes for these weak topics: ${BankEngine.weakTopics(p).take(4).joinToString { bank.topicTitle(it.topic) }.ifBlank { input }}"
        } + if (ctx.isNotBlank()) "\n\n$ctx" else ""
    }
}
