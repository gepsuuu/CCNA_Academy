package com.ccnaacademy.app.data

/** One search hit, grouped by category in the UI. */
data class Hit(val category: String, val title: String, val snippet: String, val route: String, val score: Int)

object GlobalSearch {
    val CATEGORIES = listOf("Lessons", "Glossary & protocols", "Comparisons", "Common mistakes", "Flashcards", "Cheatsheets", "Questions", "My notes")

    private fun norm(s: String) = s.lowercase()

    /** Scores: exact title 100, title starts-with 60, title contains 40, body contains 10 per matching term (all terms must match). */
    private fun score(title: String, body: String, terms: List<String>): Int {
        val t = norm(title); val b = norm(body)
        if (terms.any { it !in t && it !in b }) return 0
        val q = terms.joinToString(" ")
        var s = when { t == q -> 100; t.startsWith(q) -> 60; q in t -> 40; else -> 0 }
        terms.forEach { if (it in t) s += 15 else if (it in b) s += 10 }
        return s
    }

    fun snippet(body: String, terms: List<String>): String {
        val i = terms.map { norm(body).indexOf(it) }.filter { it >= 0 }.minOrNull() ?: return body.take(120)
        val start = (i - 50).coerceAtLeast(0)
        return (if (start > 0) "…" else "") + body.substring(start, (i + 90).coerceAtMost(body.length)).replace('\n', ' ') + if (i + 90 < body.length) "…" else ""
    }

    fun run(query: String, c: Curriculum, lib: MemoryLibrary, study: StudyContent, bank: QuestionBank, p: ProgressState): Map<String, List<Hit>> {
        val terms = norm(query).split(Regex("\\s+")).filter { it.length >= 2 || it.any(Char::isDigit) }
        if (terms.isEmpty()) return emptyMap()
        val hits = mutableListOf<Hit>()
        fun add(cat: String, title: String, body: String, route: String) {
            val sc = score(title, body, terms); if (sc > 0) hits += Hit(cat, title, snippet(body, terms), route, sc)
        }
        c.lessons.forEach { l ->
            val body = listOf(l.simple, l.why, l.summary) + l.deep.map { it.text } + l.terms.map { "${it.term} ${it.definition}" } + l.memorize + l.examNotes + l.mistakes
            add("Lessons", l.title, body.joinToString("\n") + "\n" + l.progressiveText(), "lesson/${l.id}")
        }
        study.glossary.forEach { g -> add("Glossary & protocols", g.term + if (g.full.isNotBlank()) " (${g.full})" else "", "${g.simple}\n${g.technical}", "glossary?term=${g.id}") }
        study.comparisons.forEach { x -> add("Comparisons", x.title, x.summary + "\n" + x.blocks.joinToString("\n") { b -> b.text + " " + b.rows.joinToString(" ") { it.joinToString(" ") } + b.items.joinToString(" ") }, "compare/${x.id}") }
        study.mistakes.forEach { m -> add("Common mistakes", m.title, "${m.wrong}\n${m.right}\n${m.example}", "mistakes?focus=${m.id}") }
        lib.cards.forEach { k -> add("Flashcards", k.front, "${k.answer}\n${k.explanation}", "cardbrowser?deck=&q=${java.net.URLEncoder.encode(k.front.take(40), "UTF-8").replace("+", "%20")}") }
        lib.cheatsheets.forEach { sh -> add("Cheatsheets", sh.title, sh.tags.joinToString(" ") + "\n" + sh.blocks.joinToString("\n") { b -> b.title + " " + b.text + " " + b.items.joinToString(" ") + " " + b.rows.joinToString(" ") { it.joinToString(" ") } }, "sheet/${sh.id}") }
        bank.questions.forEach { q -> add("Questions", q.stem, (q.options + q.left + q.right + q.items).joinToString(" ") + "\n" + q.concept, "question/${q.id}") }
        p.notes.forEach { n -> add("My notes", n.title, n.body + " " + n.topic, "note/${n.id}") }
        return hits.groupBy { it.category }.mapValues { (_, v) -> v.sortedByDescending { it.score } }
            .toList().sortedBy { CATEGORIES.indexOf(it.first) }.toMap()
    }
}
