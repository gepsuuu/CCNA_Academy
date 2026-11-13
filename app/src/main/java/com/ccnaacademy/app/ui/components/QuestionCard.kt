package com.ccnaacademy.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.theme.LocalSemantic

/**
 * Renders any bank question type. Answers use ORIGINAL indices:
 *  single/tf/multi → selected option indices; match → chosen right index per left row (−1 = unset); order → item indices in user order.
 * When [reveal] is false nothing about the correct answer is shown.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionCard(
    item: SessionItem, number: Int, answer: List<Int>?, reveal: Boolean, diagrams: Map<String, Diagram>,
    domainTitle: String, topicTitle: String, enabled: Boolean = !reveal,
    onAnswer: (List<Int>) -> Unit, onOpenLesson: (() -> Unit)? = null
) {
    val q = item.q
    val sem = LocalSemantic.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Tag(BankEngine.TYPES[q.type] ?: q.type, MaterialTheme.colorScheme.primary)
            Tag(BankEngine.FORMATS[q.format] ?: q.format, MaterialTheme.colorScheme.secondary)
            Tag(BankEngine.DIFFICULTY[q.difficulty] ?: "", when (q.difficulty) { 1 -> sem.success; 2 -> sem.warning; else -> sem.danger })
        }
        Text("Question $number", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(q.stem, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        q.exhibit?.let { Exhibit(it) }
        q.diagram?.let { id -> diagrams[id]?.let { DiagramCard(it, showExplanation = reveal) } }
        when (q.type) {
            "single", "tf" -> item.optOrder.forEachIndexed { pos, orig ->
                OptionRow(letter(pos), q.options[orig], selected = answer?.contains(orig) == true, multi = false, enabled = enabled,
                    state = optionState(reveal, orig in q.correct, answer?.contains(orig) == true)) { onAnswer(listOf(orig)) }
            }
            "multi" -> {
                Text("Select ${q.correct.size}.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                item.optOrder.forEachIndexed { pos, orig ->
                    val sel = answer?.contains(orig) == true
                    OptionRow(letter(pos), q.options[orig], sel, multi = true, enabled = enabled,
                        state = optionState(reveal, orig in q.correct, sel)) {
                        val cur = answer.orEmpty()
                        onAnswer(if (sel) cur - orig else cur + orig)
                    }
                }
            }
            "match" -> MatchView(item, answer, reveal, enabled, onAnswer)
            "order" -> OrderView(item, answer, reveal, enabled, onAnswer)
        }
        if (reveal) Explanation(item, answer, domainTitle, topicTitle, onOpenLesson)
    }
}

private fun letter(i: Int) = ('A' + i).toString()
private enum class OptState { NONE, CORRECT, WRONG_PICK, MISSED }
private fun optionState(reveal: Boolean, isCorrect: Boolean, picked: Boolean) = when {
    !reveal -> OptState.NONE
    isCorrect && picked -> OptState.CORRECT
    isCorrect -> OptState.MISSED
    picked -> OptState.WRONG_PICK
    else -> OptState.NONE
}

@Composable
fun Exhibit(text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("EXHIBIT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Box(Modifier.horizontalScroll(rememberScrollState())) {
                Text(text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, softWrap = false)
            }
        }
    }
}

@Composable
private fun OptionRow(letter: String, text: String, selected: Boolean, multi: Boolean, enabled: Boolean, state: OptState, onClick: () -> Unit) {
    val sem = LocalSemantic.current
    val border = when (state) {
        OptState.CORRECT, OptState.MISSED -> sem.success
        OptState.WRONG_PICK -> sem.danger
        OptState.NONE -> if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }
    Surface(Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick), shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (selected || state != OptState.NONE) 2.dp else 1.dp, border),
        color = if (state == OptState.CORRECT || state == OptState.MISSED) sem.success.copy(alpha = 0.07f) else MaterialTheme.colorScheme.surface) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (multi) Checkbox(selected, null, enabled = enabled) else RadioButton(selected, null, enabled = enabled)
            Text("$letter.", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(vertical = 6.dp))
            when (state) {
                OptState.CORRECT -> Icon(Icons.Filled.CheckCircle, "Correct", tint = sem.success)
                OptState.MISSED -> Icon(Icons.Filled.CheckCircleOutline, "Correct answer (not selected)", tint = sem.success)
                OptState.WRONG_PICK -> Icon(Icons.Filled.Cancel, "Incorrect", tint = sem.danger)
                else -> {}
            }
        }
    }
}

@Composable
private fun MatchView(item: SessionItem, answer: List<Int>?, reveal: Boolean, enabled: Boolean, onAnswer: (List<Int>) -> Unit) {
    val q = item.q
    val sem = LocalSemantic.current
    val cur = answer ?: List(q.left.size) { -1 }
    Text("Choose the matching item for each row.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    q.left.forEachIndexed { li, leftText ->
        var open by remember(item) { mutableStateOf(false) }
        val chosen = cur.getOrElse(li) { -1 }
        val ok = chosen == li
        Surface(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, if (!reveal) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else if (ok) sem.success else sem.danger)) {
            Column(Modifier.padding(10.dp)) {
                Text(leftText, fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(onClick = { open = true }, enabled = enabled, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                        Text(if (chosen >= 0) q.right[chosen] else "Choose…", modifier = Modifier.weight(1f), maxLines = 3)
                        Icon(Icons.Filled.ArrowDropDown, null)
                    }
                    DropdownMenu(open, { open = false }) {
                        item.rightOrder.forEach { ri ->
                            DropdownMenuItem({ Text(q.right[ri]) }, onClick = {
                                open = false
                                onAnswer(cur.mapIndexed { i, v -> if (i == li) ri else if (v == ri) -1 else v })
                            })
                        }
                    }
                }
                if (reveal && !ok) Text("Correct: ${q.right[li]}", color = sem.success, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun OrderView(item: SessionItem, answer: List<Int>?, reveal: Boolean, enabled: Boolean, onAnswer: (List<Int>) -> Unit) {
    val q = item.q
    val sem = LocalSemantic.current
    val cur = answer ?: item.initOrder
    Text("Use the arrows to put the items in the correct order (first at the top).", style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary)
    cur.forEachIndexed { pos, itemIdx ->
        val ok = itemIdx == pos
        Surface(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, if (!reveal) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else if (ok) sem.success else sem.danger)) {
            Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${pos + 1}.", fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                Text(q.items[itemIdx], modifier = Modifier.weight(1f).padding(vertical = 10.dp))
                if (enabled) {
                    IconButton(onClick = { onAnswer(cur.toMutableList().also { java.util.Collections.swap(it, pos, pos - 1) }) }, enabled = pos > 0) {
                        Icon(Icons.Filled.KeyboardArrowUp, "Move up")
                    }
                    IconButton(onClick = { onAnswer(cur.toMutableList().also { java.util.Collections.swap(it, pos, pos + 1) }) }, enabled = pos < cur.size - 1) {
                        Icon(Icons.Filled.KeyboardArrowDown, "Move down")
                    }
                }
            }
        }
    }
    if (reveal && cur != q.items.indices.toList()) {
        Text("Correct order:", fontWeight = FontWeight.SemiBold, color = sem.success)
        q.items.forEachIndexed { i, t -> Text("${i + 1}. $t", style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun Explanation(item: SessionItem, answer: List<Int>?, domainTitle: String, topicTitle: String, onOpenLesson: (() -> Unit)?) {
    val q = item.q
    val sem = LocalSemantic.current
    val ok = BankEngine.isCorrect(q, answer)
    val answered = BankEngine.isAnswered(q, answer)
    Surface(color = (if (ok) sem.success else sem.danger).copy(alpha = 0.08f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(when { ok -> "Correct"; !answered -> "Not answered"; else -> "Incorrect" }, fontWeight = FontWeight.Bold,
                color = if (ok) sem.success else sem.danger, style = MaterialTheme.typography.titleMedium)
            if (q.type in setOf("single", "multi", "tf")) {
                val letters = item.optOrder.mapIndexed { pos, orig -> orig to letter(pos) }.toMap()
                Text("Correct answer: " + q.correct.sortedBy { item.optOrder.indexOf(it) }.joinToString { "${letters[it]}. ${q.options[it]}" },
                    fontWeight = FontWeight.SemiBold)
            }
            Section("Explanation", q.explanation)
            Section("Why the correct answer is correct", q.whyCorrect)
            if (q.type in setOf("single", "multi") && q.whyWrong.any { it.isNotBlank() }) {
                Text("Why the other options are incorrect", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                item.optOrder.forEachIndexed { pos, orig ->
                    val why = q.whyWrong.getOrElse(orig) { "" }
                    if (orig !in q.correct && why.isNotBlank()) Text("${letter(pos)}. ${q.options[orig]} — $why", style = MaterialTheme.typography.bodySmall)
                }
            }
            HorizontalDivider()
            Text("Concept: ${q.concept}", style = MaterialTheme.typography.bodySmall)
            Text("Domain: $domainTitle • Topic: $topicTitle" + (if (q.objective.isNotBlank()) " • Objective ${q.objective}" else ""), style = MaterialTheme.typography.bodySmall)
            Text("Difficulty: ${BankEngine.DIFFICULTY[q.difficulty]}" + if (q.verified) " • Answer machine-verified" else "", style = MaterialTheme.typography.bodySmall)
            if (q.tip.isNotBlank()) Row {
                Icon(Icons.Filled.Lightbulb, null, tint = sem.warning, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                Text("Memory tip: ${q.tip}", style = MaterialTheme.typography.bodySmall)
            }
            if (onOpenLesson != null && q.lessonId.isNotBlank()) TextButton(onClick = onOpenLesson) { Text("Open the related lesson") }
        }
    }
}

@Composable
private fun Section(title: String, text: String) {
    if (text.isBlank()) return
    Column {
        Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
