package com.ccnaacademy.app.data

/**
 * Pure presentation helpers shared by Compose screens and JVM unit tests (1.5.0-rc2).
 * They contain no Android types so behavior can be verified without a device.
 */

/**
 * Number of LazyColumn items LessonScreen emits before the first stage header:
 * item 0 = lesson header with stage buttons, item 1 = draft/acceptance notice.
 */
const val LESSON_LEADING_ITEMS = 2

/**
 * LazyColumn index of the header for [stageIndex], matching LessonScreen's emission order:
 * each stage emits one header item, plus one item per block only when the stage is applicable.
 * Returns -1 for an out-of-range stage instead of scrolling to an unrelated item.
 */
fun stageItemIndex(stages: List<LearningStage>, stageIndex: Int, leadingItems: Int = LESSON_LEADING_ITEMS): Int {
    if (stageIndex !in stages.indices) return -1
    return leadingItems + stages.take(stageIndex).sumOf { 1 + if (it.applicable) it.blocks.size else 0 }
}

/**
 * Screen-reader text alternative for a vector diagram. Canvas drawings expose no text to TalkBack, so this
 * reads the title, every element with its detail, every labelled relationship, free text, and the legend.
 * Relationships are phrased as "A to B" because many authored arrows are reading guides, not one-way traffic;
 * the authored explanation (shown visibly beneath the diagram) states which.
 */
fun diagramTextAlternative(d: Diagram): String = buildString {
    val names = d.nodes.associate { it.id to it.label }
    append("Diagram: ").append(d.title).append(". ")
    append(d.nodes.size).append(if (d.nodes.size == 1) " element. " else " elements. ")
    d.nodes.forEach { n ->
        append(n.label)
        if (n.sub.isNotBlank()) append(", ").append(n.sub)
        append(". ")
    }
    if (d.edges.isNotEmpty()) {
        append("Relationships: ")
        d.edges.forEach { e ->
            append(names[e.src] ?: e.src).append(" to ").append(names[e.dst] ?: e.dst)
            if (e.label.isNotBlank()) append(", ").append(e.label)
            append(". ")
        }
    }
    d.texts.filter { it.text.isNotBlank() }.takeIf { it.isNotEmpty() }?.let { t ->
        append("Labels: ").append(t.joinToString("; ") { it.text }).append(". ")
    }
    if (d.legend.isNotEmpty()) append("Legend: ").append(d.legend.joinToString("; ") { it.label }).append(".")
}.trim()
