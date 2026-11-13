package com.ccnaacademy.app.data

/**
 * Resolves a stored highlight key (`lessonId:section`) to readable text for the library and Markdown export.
 *
 * Supported section forms (all must keep working for existing user data):
 * - `simple`, `why`, `summary`        legacy top-level lesson fields
 * - `deepN`, `exN`                    legacy deep-dive / example indexes
 * - `block:<blockId>`                 progressive-lesson block IDs (1.5.0+)
 * Unknown or removed blocks return a visible placeholder instead of silently dropping the highlight.
 * Kept free of Android/Compose types so it can be unit-tested on the JVM.
 */
fun lessonSectionText(l: Lesson, section: String): String = when {
    section.startsWith("block:") -> l.learningStages.flatMap { it.blocks }.firstOrNull { it.id == section.removePrefix("block:") }?.let { b ->
        listOf(b.title, b.text, b.items.joinToString("\n"), b.rows.joinToString("\n") { it.joinToString(": ") }).filter { it.isNotBlank() }.joinToString("\n\n")
    } ?: "Preserved highlight: this block is not available in the current content."
    section == "simple" -> l.simple; section == "why" -> l.why; section == "summary" -> l.summary
    section.startsWith("deep") -> l.deep.getOrNull(section.removePrefix("deep").toIntOrNull() ?: -1)?.text ?: ""
    section.startsWith("ex") -> l.examples.getOrNull(section.removePrefix("ex").toIntOrNull() ?: -1)?.let { "${it.title}: ${it.text}" } ?: ""
    else -> ""
}

