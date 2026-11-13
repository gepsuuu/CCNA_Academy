package com.ccnaacademy.app.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Question packs:
 *  - built-in packs ship in assets/packs (listed in assets/packs/index.json) and are always available offline;
 *  - imported packs (from a file or an HTTPS download) are validated and stored in filesDir/packs, then also work offline.
 */
class PackRepository(private val context: Context) {
    private val dir = File(context.filesDir, "packs").apply { mkdirs() }
    private val domains = setOf("d1", "d2", "d3", "d4", "d5", "d6")
    private val types = setOf("single", "multi", "tf", "match", "order")

    @kotlinx.serialization.Serializable private data class Index(val packs: List<String>)

    suspend fun loadAll(): List<LoadedPack> = withContext(Dispatchers.IO) {
        val out = mutableListOf<LoadedPack>()
        val seen = mutableSetOf<String>()
        val index = AppJson.decodeFromString<Index>(context.assets.open("packs/index.json").bufferedReader().use { it.readText() })
        index.packs.forEach { f ->
            val pack = AppJson.decodeFromString<QuestionPack>(context.assets.open("packs/$f").bufferedReader().use { it.readText() })
            val (clean, issues) = validate(pack, seen)
            seen += clean.questions.map { it.id }
            out += LoadedPack(clean, builtin = true, issues = issues)
        }
        dir.listFiles { f -> f.extension == "json" }?.sortedBy { it.name }?.forEach { f ->
            runCatching { AppJson.decodeFromString<QuestionPack>(f.readText()) }.onSuccess { pack ->
                if (out.none { it.pack.id == pack.id }) {
                    val (clean, issues) = validate(pack, seen)
                    seen += clean.questions.map { it.id }
                    out += LoadedPack(clean, builtin = false, issues = issues)
                }
            }
        }
        out
    }

    /** Drops malformed or duplicate questions and reports why; adds topics that a pack references but does not define. */
    fun validate(pack: QuestionPack, existingIds: Set<String>): Pair<QuestionPack, List<String>> {
        val issues = mutableListOf<String>()
        val ids = existingIds.toMutableSet()
        val good = pack.questions.filter { q ->
            val problem = when {
                q.id.isBlank() -> "missing id"
                q.id in ids -> "duplicate id"
                q.domainId !in domains -> "unknown domain '${q.domainId}'"
                q.type !in types -> "unknown type '${q.type}'"
                q.stem.isBlank() || q.explanation.isBlank() -> "missing stem or explanation"
                q.type in setOf("single", "multi", "tf") && (q.options.size < 2 || q.correct.isEmpty() || q.correct.any { it !in q.options.indices }) -> "invalid options/answer"
                q.type == "single" && q.correct.size != 1 -> "single-answer question must have exactly one answer"
                q.type == "multi" && q.correct.size < 2 -> "multiple-select question needs at least two answers"
                q.type == "match" && (q.left.size < 2 || q.left.size != q.right.size) -> "matching needs equal left/right lists"
                q.type == "order" && q.items.size < 2 -> "ordering needs at least two items"
                q.difficulty !in 1..3 -> "difficulty must be 1-3"
                else -> null
            }
            if (problem != null) issues += "${q.id.ifBlank { "?" }}: $problem"
            if (problem == null) ids += q.id
            problem == null
        }
        val known = pack.topics.map { it.id }.toSet()
        val extraTopics = good.filter { it.topic !in known }.distinctBy { it.topic }.map {
            BankTopic(it.topic, it.topic.replace('-', ' ').replaceFirstChar { c -> c.uppercase() }, it.domainId, it.lessonId)
        }
        return pack.copy(questions = good, topics = pack.topics + extraTopics) to issues
    }

    /** Parses, validates and stores a pack. Returns a human-readable summary. */
    suspend fun install(json: String, installed: List<LoadedPack>): String = withContext(Dispatchers.IO) {
        val pack = runCatching { AppJson.decodeFromString<QuestionPack>(json) }.getOrElse { throw IllegalArgumentException("Not a valid question pack: ${it.message?.take(120)}") }
        require(pack.schema == 1) { "Unsupported pack schema ${pack.schema}" }
        require(pack.id.matches(Regex("[A-Za-z0-9._-]{2,60}"))) { "Pack id must be 2-60 letters, digits, '.', '_' or '-'" }
        require(installed.none { it.builtin && it.pack.id == pack.id }) { "A built-in pack already uses id '${pack.id}'" }
        val others = installed.filter { it.pack.id != pack.id }.flatMap { it.pack.questions.map { q -> q.id } }.toSet()
        val (clean, issues) = validate(pack, others)
        require(clean.questions.isNotEmpty()) { "No valid questions found. ${issues.take(3).joinToString("; ")}" }
        File(dir, "${pack.id}.json").writeText(AppJson.encodeToString(QuestionPack.serializer(), clean))
        "Installed '${pack.name}' v${pack.version}: ${clean.questions.size} questions" + if (issues.isNotEmpty()) " (${issues.size} skipped)" else ""
    }

    suspend fun readUri(uri: Uri): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { it.readLimited() } ?: throw IllegalArgumentException("Cannot open file")
    }

    suspend fun download(url: String): String = withContext(Dispatchers.IO) {
        require(url.startsWith("https://")) { "Only https:// links are allowed" }
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 10_000; c.readTimeout = 15_000
        try {
            require(c.responseCode == 200) { "Server returned HTTP ${c.responseCode}" }
            c.inputStream.use { it.readLimited() }
        } finally { c.disconnect() }
    }

    fun delete(id: String) { File(dir, "$id.json").delete() }

    private fun java.io.InputStream.readLimited(max: Int = 5 * 1024 * 1024): String {
        // Manual loop: InputStream.readNBytes() needs API 33, and minSdk is 26.
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(16 * 1024)
        while (true) {
            val n = read(buf)
            if (n < 0) break
            out.write(buf, 0, n)
            require(out.size() <= max) { "Pack is larger than 5 MB" }
        }
        return out.toString("UTF-8")
    }
}
