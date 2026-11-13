package com.ccnaacademy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.Block
import com.ccnaacademy.app.data.Diagram
import com.ccnaacademy.app.ui.theme.LocalSemantic

/** Highlight support: keys are "$prefix:$blockIndex:$itemIndex". Null prefix disables marking. */
class Marks(val prefix: String?, val marked: Set<String>, val toggle: (String) -> Unit) {
    fun key(b: Int, i: Int) = "$prefix:$b:$i"
    fun isMarked(b: Int, i: Int) = prefix != null && key(b, i) in marked
}

val HighlightColor = Color(0xFFFFD54F)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun Markable(marks: Marks?, b: Int, i: Int, content: @Composable () -> Unit) {
    if (marks == null || marks.prefix == null) { Box(Modifier.fillMaxWidth()) { content() }; return }
    val on = marks.isMarked(b, i)
    Box(Modifier.fillMaxWidth()
        .background(if (on) HighlightColor.copy(alpha = 0.28f) else Color.Transparent, RoundedCornerShape(6.dp))
        .combinedClickable(onClick = {}, onLongClick = { marks.toggle(marks.key(b, i)) })) { content() }
}

@Composable
fun BlockList(blocks: List<Block>, diagrams: Map<String, Diagram>, marks: Marks? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (marks?.prefix != null) Text("Tip: long-press any row, bullet or command to mark it as important.",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        blocks.forEachIndexed { bi, b -> BlockView(b, bi, diagrams, marks) }
    }
}

@Composable
fun BlockView(b: Block, bi: Int, diagrams: Map<String, Diagram>, marks: Marks?) {
    val sem = LocalSemantic.current
    when (b.type) {
        "diagram" -> diagrams[b.diagram]?.let { DiagramCard(it) }
        "callout" -> {
            val (color, icon) = when (b.tone) { "warn" -> sem.danger to Icons.Filled.Warning; "exam" -> sem.exam to Icons.Filled.School; else -> sem.warning to Icons.Filled.Lightbulb }
            Surface(color = color.copy(alpha = 0.10f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp)) {
                    Icon(icon, null, tint = color)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        if (b.title.isNotBlank()) Text(b.title, fontWeight = FontWeight.SemiBold, color = color)
                        Text(b.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        else -> TeachingBlockFrame(b) {
            when (b.type) {
                "text" -> Text(b.text, style = MaterialTheme.typography.bodyLarge)
                "bullets" -> b.items.forEachIndexed { i, line -> Markable(marks, bi, i) { Bullet(line) } }
                "mnemonic" -> {
                    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("“${b.mnemonic}”", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    b.items.forEachIndexed { i, line -> Markable(marks, bi, i) { Bullet(line, MaterialTheme.colorScheme.tertiary) } }
                    if (b.text.isNotBlank()) Text(b.text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }
                "chunks" -> {
                    b.rows.forEachIndexed { i, r ->
                        Markable(marks, bi, i) {
                            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(10.dp)) {
                                Text(r.getOrElse(0) { "" }, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                Text(r.getOrElse(1) { "" }, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    if (b.text.isNotBlank()) Text(b.text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }
                "visual" -> {
                    Text(b.text, style = MaterialTheme.typography.bodyLarge)
                    b.diagram?.let { id -> diagrams[id]?.let { Spacer(Modifier.height(8.dp)); DiagramCard(it) } }
                }
                "packet" -> b.packet?.let { PacketWalkthroughView(it, diagrams) }
                "inspector" -> b.inspector?.let { OutputInspectorView(it) }
                "check" -> {
                    var chosen by rememberSaveable(b.id) { mutableStateOf(-1) }
                    Text(b.text, style = MaterialTheme.typography.bodyLarge)
                    Text("Practice feedback only; this check is not added to exam analytics.", style = MaterialTheme.typography.labelSmall)
                    b.items.forEachIndexed { index, option ->
                        OutlinedButton(onClick = { chosen = index }, enabled = chosen < 0, modifier = Modifier.fillMaxWidth()) {
                            Text("${index + 1}. $option")
                        }
                    }
                    if (chosen >= 0) {
                        Text(if (chosen == b.correctIndex) "Correct" else "Review the reasoning", fontWeight = FontWeight.Bold)
                        b.items.forEachIndexed { index, option ->
                            Text("${if (index == b.correctIndex) "Correct answer" else "Alternative"}: $option", fontWeight = FontWeight.SemiBold)
                            Text(b.rationales.getOrElse(index) { "Explanation unavailable" }, modifier = Modifier.padding(bottom = 8.dp))
                        }
                        TextButton(onClick = { chosen = -1 }) { Text("Try again") }
                    }
                }
                "detail" -> {
                    var expanded by rememberSaveable(b.id, b.title) { mutableStateOf(false) }
                    if (b.text.isNotBlank()) Text(b.text, style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Hide explanation" else "Explore explanation")
                    }
                    if (expanded) b.items.forEach { Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 6.dp)) }
                }
                "code" -> {
                    val clipboard = LocalClipboardManager.current
                    TextButton(onClick = { clipboard.setText(AnnotatedString(b.items.joinToString("\n"))) }) { Text("Copy terminal text") }
                    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.horizontalScroll(rememberScrollState()).padding(12.dp)) {
                            b.items.forEachIndexed { i, line ->
                                Markable(marks, bi, i) {
                                    Text(line, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium, softWrap = false,
                                        color = when {
                                            line.trimStart().startsWith("!") -> MaterialTheme.colorScheme.onSurfaceVariant
                                            line.trimStart().startsWith("no ") -> MaterialTheme.colorScheme.error
                                            line.trimStart().startsWith("show ") -> MaterialTheme.colorScheme.tertiary
                                            line.trimStart().startsWith("interface ") || line.trimStart().startsWith("router ") -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        })
                                }
                            }
                        }
                    }
                    if (b.text.isNotBlank()) Text(b.text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }
                "table" -> {
                    TableView(b, bi, marks)
                    if (b.text.isNotBlank()) Text(b.text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }
                else -> Text(b.text)
            }
        }
    }
}

private fun blockIcon(type: String) = when (type) {
    "table" -> Icons.Filled.TableChart; "mnemonic" -> Icons.Filled.Psychology; "chunks" -> Icons.Filled.ViewModule
    "visual" -> Icons.Filled.Image; "code" -> Icons.Filled.Terminal; "bullets" -> Icons.Filled.Checklist; else -> Icons.Filled.Notes
}

@Composable
fun TableView(b: Block, bi: Int, marks: Marks?) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth < 600.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                b.rows.forEachIndexed { ri, row ->
                    Markable(marks, bi, ri) {
                        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            row.forEachIndexed { ci, value ->
                                Text(b.headers.getOrElse(ci) { "Field ${ci + 1}" }, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 8.dp))
                            }
                        }
                    }
                }
            }
        } else WideTableView(b, bi, marks)
    }
}

@Composable
private fun WideTableView(b: Block, bi: Int, marks: Marks?) {
    val cols = b.headers.size.coerceAtLeast(1)
    val widths = (0 until cols).map { c ->
        val longest = (listOf(b.headers.getOrElse(c) { "" }) + b.rows.map { it.getOrElse(c) { "" } }).maxOf { it.length }
        (longest * 7 + 24).coerceIn(64, 240).dp
    }
    val line = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row(Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))) {
            b.headers.forEachIndexed { c, h -> Text(h, Modifier.width(widths[c]).padding(8.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge) }
        }
        b.rows.forEachIndexed { ri, r ->
            Markable(marks, bi, ri) {
                Row(Modifier.border(0.5.dp, line), verticalAlignment = Alignment.Top) {
                    (0 until cols).forEach { c ->
                        Text(r.getOrElse(c) { "" }, Modifier.width(widths[c]).padding(8.dp), style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (c == 0) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeachingBlockFrame(b: Block, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    if (b.id.isNotBlank()) {
        Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (b.title.isNotBlank()) Text(b.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    } else SectionCard(b.title.ifBlank { " " }, icon = blockIcon(b.type), content = content)
}
