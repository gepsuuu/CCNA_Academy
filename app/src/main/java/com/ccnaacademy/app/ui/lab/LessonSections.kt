package com.ccnaacademy.app.ui.lab

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.Lesson
import com.ccnaacademy.app.lab.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.theme.LocalSemantic

/** Shows which stages of the learning cycle this lesson offers: Understand → Compare → Visualize → Observe → Configure → Verify → Troubleshoot → Recall. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LearningCycleStrip(x: LessonExtras, l: Lesson) {
    val stages = listOf(
        "Understand" to true,
        "Compare" to (x.tables.isNotEmpty() || x.comparisons.isNotEmpty()),
        "Visualize" to (x.animations.isNotEmpty() || l.diagrams.isNotEmpty()),
        "Observe" to x.outputs.isNotEmpty(),
        "Configure" to (x.walkthroughs.isNotEmpty() || x.labs.any { it.kind == "guided" }),
        "Verify" to x.labs.isNotEmpty(),
        "Troubleshoot" to (x.labs.any { it.kind == "troubleshoot" } || x.tables.any { it.kind == "troubleshoot" }),
        "Recall" to true
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        stages.forEachIndexed { i, (name, on) ->
            Text((if (i > 0) "→ " else "") + name + if (on) "" else " (—)", style = MaterialTheme.typography.labelSmall,
                color = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
fun CompareSection(s: AppScope, x: LessonExtras) {
    val tables = x.tables.filter { it.kind != "troubleshoot" }
    val comps = x.comparisons.mapNotNull { id -> s.study.comparisons.firstOrNull { it.id == id } }
    if (tables.isEmpty() && comps.isEmpty()) return
    SectionCard("Compare", Icons.Filled.TableChart, accent = MaterialTheme.colorScheme.secondary) {
        Text("Tap rows for explanations. Use 'Hide answers' for active recall or 'Quiz me' to fill a missing cell.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        tables.forEach { t -> Spacer(Modifier.height(10.dp)); InteractiveTable(t, s.wide) { ok -> s.vm.recordActivity("table:${t.id}", "concept", ok) } }
        comps.forEach { c ->
            c.blocks.filter { it.type == "table" && it.headers.isNotEmpty() }.forEach { b ->
                Spacer(Modifier.height(10.dp))
                InteractiveTable(LessonTable("cmp-${c.id}-${b.title}", "${c.title}${if (b.title.isNotBlank()) " — ${b.title}" else ""}", b.headers, b.rows.filter { it.size == b.headers.size },
                    caption = c.summary), s.wide) { ok -> s.vm.recordActivity("cmp:${c.id}", "concept", ok) }
            }
            TextButton(onClick = { s.go("compare/${c.id}") }) { Text("Open full comparison: ${c.title}") }
        }
    }
}

@Composable
fun VisualizeSection(s: AppScope, x: LessonExtras) {
    if (x.animations.isEmpty()) return
    x.animations.forEachIndexed { i, a ->
        if (i > 0) Spacer(Modifier.height(12.dp))
        AnimationCard(a, s.p.reducedMotion, s.p.animSpeed, s.wide) { s.vm.setAnimSpeed(it) }
    }
}

@Composable
fun ConfigureSection(s: AppScope, x: LessonExtras) {
    if (x.walkthroughs.isEmpty()) return
    SectionCard("Configure — command by command", Icons.Filled.Terminal) {
        x.walkthroughs.forEach { w -> WalkthroughView(w) }
        if (x.labs.isNotEmpty()) Text("Practise these commands in the CLI trainer below.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WalkthroughView(w: Walkthrough) {
    var open by remember(w.id) { mutableStateOf<Int?>(0) }
    Text(w.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
    w.steps.forEachIndexed { i, st ->
        val sem = LocalSemantic.current
        Surface(Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { open = if (open == i) null else i }, shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))) {
            Column(Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp))
                    Text(st.command, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Tag(st.mode, MaterialTheme.colorScheme.secondary)
                }
                if (open == i) {
                    Text(st.purpose, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    Text("Verify: ${st.verify}", style = MaterialTheme.typography.bodySmall, color = sem.success)
                    Text("Common mistake: ${st.mistake}", style = MaterialTheme.typography.bodySmall, color = sem.danger)
                    if (st.undo.isNotBlank()) Text("Undo: ${st.undo}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    if (st.note.isNotBlank()) Text("Note: ${st.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun PracticeSection(s: AppScope, x: LessonExtras) {
    val trouble = x.tables.filter { it.kind == "troubleshoot" }
    if (x.labs.isEmpty() && x.noLabReason == null && trouble.isEmpty()) return
    SectionCard("Practice, verify and troubleshoot", Icons.Filled.Build, accent = LocalSemantic.current.success) {
        x.labs.forEach { lab -> LabCard(s, lab); Spacer(Modifier.height(8.dp)) }
        x.noLabReason?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        trouble.forEach { t -> Spacer(Modifier.height(8.dp)); InteractiveTable(t, s.wide) }
    }
}

@Composable
fun OutputSection(s: AppScope, x: LessonExtras) {
    if (x.outputs.isEmpty()) return
    SectionCard("Interpret real output", Icons.Filled.Visibility, accent = MaterialTheme.colorScheme.tertiary) {
        x.outputs.forEach { o -> OutputActivityView(s, o) }
    }
}

@Composable
private fun OutputActivityView(s: AppScope, o: OutputActivity) {
    val sem = LocalSemantic.current
    val lines = remember(o.id) { o.render() }
    var chosen by remember(o.id) { mutableStateOf<Int?>(null) }
    Text(o.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    Text("${o.device}# ${o.command}", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 4.dp))
    Text("Generated by the CLI trainer from the '${LabCatalog.byId[o.labId]?.title}' scenario.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    AnnotatedOutput(lines, o.annotations)
    Text(o.question, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))
    o.options.forEachIndexed { i, opt ->
        val answered = chosen != null
        OutlinedButton(onClick = { if (chosen == null) { chosen = i; s.vm.recordActivity("out:${o.id}", "output", i == o.answer) } },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            border = BorderStroke(if (answered && (i == o.answer || i == chosen)) 2.dp else 1.dp,
                when { answered && i == o.answer -> sem.success; answered && i == chosen -> sem.danger; else -> MaterialTheme.colorScheme.outline })) {
            Text(opt, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
        }
    }
    chosen?.let {
        Text((if (it == o.answer) "Correct. " else "Not quite. ") + o.explanation, color = if (it == o.answer) sem.success else sem.danger,
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
        TextButton(onClick = { chosen = null }) { Text("Try again") }
    }
    Spacer(Modifier.height(10.dp))
}

/** Teach-back: the learner explains in their own words, then self-checks against key points. Not auto-graded. */
@Composable
fun TeachBackCard(s: AppScope, id: String, tb: TeachBack) {
    var text by remember(id) { mutableStateOf("") }
    var shown by remember(id) { mutableStateOf(false) }
    val ticks = remember(id) { mutableStateMapOf<Int, Boolean>() }
    var recorded by remember(id) { mutableStateOf(false) }
    SectionCard("Teach it back", Icons.Filled.RecordVoiceOver, accent = MaterialTheme.colorScheme.tertiary) {
        Text(tb.prompt, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth().padding(top = 8.dp), minLines = 3, placeholder = { Text("Explain it in your own words…") })
        if (!shown) TextButton(onClick = { shown = true }) { Text("Compare with the key points") }
        else {
            Text("Tick the points your explanation covered (self-assessed — the app does not grade free text):", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 6.dp))
            tb.checklist.forEachIndexed { i, c ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { ticks[i] = !(ticks[i] ?: false) }) {
                    Checkbox(ticks[i] ?: false, { ticks[i] = it }); Text(c, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text("Model answer: ${tb.model}", Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (!recorded) Button(onClick = { recorded = true; s.vm.recordActivity("teach:$id", "concept", tb.checklist.indices.all { ticks[it] == true }) }, modifier = Modifier.padding(top = 6.dp)) {
                Text("Save self-check")
            } else Text("Saved.", color = LocalSemantic.current.success, style = MaterialTheme.typography.labelMedium)
        }
    }
}
