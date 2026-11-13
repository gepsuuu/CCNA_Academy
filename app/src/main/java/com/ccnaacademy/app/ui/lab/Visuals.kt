package com.ccnaacademy.app.ui.lab

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ccnaacademy.app.lab.*
import com.ccnaacademy.app.ui.components.Tag
import com.ccnaacademy.app.ui.components.kindColors
import com.ccnaacademy.app.ui.theme.LocalSemantic
import kotlinx.coroutines.delay

/** True when the user asked for reduced motion in the app, or the system animator scale is 0. */
@Composable
fun reducedMotion(appSetting: Boolean): Boolean {
    val ctx = LocalContext.current
    val system = remember { runCatching { Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }.getOrDefault(false) }
    return appSetting || system
}

/** Badge explaining what a visual represents, so scripted demos are never mistaken for the learner's lab state. */
@Composable
fun SourceTag(kind: String) {
    val sem = LocalSemantic.current
    when (kind) {
        "demo" -> Tag("Concept demonstration (scripted)", MaterialTheme.colorScheme.tertiary)
        "lab" -> Tag("Lab state — computed from your configuration", sem.success)
        "troubleshoot" -> Tag("Troubleshooting", sem.danger)
        else -> Tag("Reference", MaterialTheme.colorScheme.secondary)
    }
}

// ================================================================== Interactive table

/**
 * Table with: tap a row for its explanation, hide/reveal cells for active recall, "complete the table" multiple choice,
 * filtering for long tables, and a stacked-card layout on narrow screens. Rows are never re-sorted, so ordered
 * content (ACL rules, routing decisions) keeps its meaning.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveTable(
    t: LessonTable, wide: Boolean, highlightRows: Set<Int> = emptySet(),
    onQuiz: ((Boolean) -> Unit)? = null
) {
    val sem = LocalSemantic.current
    var recall by remember(t.id) { mutableStateOf(false) }
    val revealed = remember(t.id) { mutableStateMapOf<String, Boolean>() }
    var selected by remember(t.id) { mutableStateOf<Int?>(null) }
    var filter by remember(t.id) { mutableStateOf("") }
    var quiz by remember(t.id) { mutableStateOf<Triple<Int, Int, List<String>>?>(null) }
    var quizAnswer by remember(t.id) { mutableStateOf<String?>(null) }
    val rows = t.rows.withIndex().filter { (_, r) -> filter.isBlank() || r.any { it.contains(filter, ignoreCase = true) } }
    val stacked = !wide && t.headers.size > 3

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(14.dp)) {
            Text(t.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SourceTag(t.kind)
                if (t.recall) FilterChip(recall, { recall = !recall; revealed.clear() }, { Text(if (recall) "Recall mode on" else "Hide answers") },
                    leadingIcon = { Icon(if (recall) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, Modifier.size(16.dp)) })
                if (t.recall && t.headers.size >= 2 && t.rows.size >= 3) AssistChip({
                    val r = t.rows.indices.random(); val c = (1 until t.headers.size).random()
                    val correct = t.rows[r][c]
                    val opts = (t.rows.map { it[c] }.filter { it != correct && it.isNotBlank() }.distinct().shuffled().take(3) + correct).shuffled()
                    quiz = Triple(r, c, opts); quizAnswer = null
                }, { Text("Quiz me") }, leadingIcon = { Icon(Icons.Filled.Quiz, null, Modifier.size(16.dp)) })
            }
            if (t.caption.isNotBlank()) Text(t.caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            if (t.rows.size > 8) OutlinedTextField(filter, { filter = it }, Modifier.fillMaxWidth().padding(top = 6.dp), singleLine = true,
                placeholder = { Text("Filter rows") }, leadingIcon = { Icon(Icons.Filled.FilterList, null) })
            Spacer(Modifier.height(8.dp))

            fun cellText(r: Int, c: Int): String {
                val key = "$r:$c"
                return if (recall && c > 0 && revealed[key] != true) "Tap to reveal" else t.rows[r][c]
            }
            if (stacked) {
                rows.forEach { (ri, r) ->
                    val hl = ri in highlightRows || ri == selected
                    Surface(Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { selected = if (selected == ri) null else ri },
                        shape = RoundedCornerShape(10.dp), color = if (hl) sem.warning.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(if (hl) 2.dp else 1.dp, if (hl) sem.warning else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))) {
                        Column(Modifier.padding(10.dp)) {
                            Text(r[0], fontWeight = FontWeight.SemiBold)
                            t.headers.drop(1).forEachIndexed { i, h ->
                                val c = i + 1
                                Row(Modifier.fillMaxWidth().clickable(enabled = recall) { revealed["$ri:$c"] = true }.padding(top = 2.dp)) {
                                    Text("$h: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(cellText(ri, c), style = MaterialTheme.typography.bodySmall,
                                        color = if (recall && revealed["$ri:$c"] != true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            } else {
                val colW = if (t.headers.size <= 3) null else 150.dp
                Column(Modifier.horizontalScroll(rememberScrollState())) {
                    Row(Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), RoundedCornerShape(8.dp))) {
                        t.headers.forEach { h -> Text(h, Modifier.then(if (colW != null) Modifier.width(colW) else Modifier.widthIn(min = 110.dp, max = 260.dp)).padding(8.dp),
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge) }
                    }
                    rows.forEach { (ri, r) ->
                        val hl = ri in highlightRows || ri == selected
                        Row(Modifier.background(if (hl) sem.warning.copy(alpha = 0.16f) else Color.Transparent).clickable { selected = if (selected == ri) null else ri },
                            verticalAlignment = Alignment.Top) {
                            if (hl) Box(Modifier.width(3.dp).height(36.dp).background(sem.warning))
                            r.indices.forEach { c ->
                                Text(cellText(ri, c), Modifier.then(if (colW != null) Modifier.width(colW) else Modifier.widthIn(min = 110.dp, max = 260.dp))
                                    .clickable(enabled = recall && c > 0) { revealed["$ri:$c"] = true }.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall, fontWeight = if (c == 0) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (recall && c > 0 && revealed["$ri:$c"] != true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
            selected?.let { ri ->
                val note = t.notes.getOrNull(ri)?.takeIf { it.isNotBlank() }
                if (note != null) Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Row(Modifier.padding(10.dp)) { Icon(Icons.Filled.Info, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(note, style = MaterialTheme.typography.bodySmall) }
                }
            }
            if (t.notes.any { it.isNotBlank() } && selected == null) Text("Tap a row for an explanation.", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))

            quiz?.let { (r, c, opts) ->
                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Complete the table: ${t.headers[c]} for \"${t.rows[r][0]}\"?", fontWeight = FontWeight.SemiBold)
                        opts.forEach { o ->
                            val correct = o == t.rows[r][c]
                            val chosen = quizAnswer == o
                            OutlinedButton(onClick = { if (quizAnswer == null) { quizAnswer = o; onQuiz?.invoke(correct) } }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                border = BorderStroke(if (quizAnswer != null && (correct || chosen)) 2.dp else 1.dp,
                                    when { quizAnswer != null && correct -> sem.success; chosen -> sem.danger; else -> MaterialTheme.colorScheme.outline })) {
                                Text(o, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                            }
                        }
                        quizAnswer?.let { a ->
                            val ok = a == t.rows[r][c]
                            Text((if (ok) "Correct. " else "Not quite — the answer is \"${t.rows[r][c]}\". ") + (t.notes.getOrNull(r)?.takeIf { it.isNotBlank() } ?: ""),
                                color = if (ok) sem.success else sem.danger, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                            TextButton(onClick = { quiz = null }) { Text("Done") }
                        }
                    }
                }
            }
        }
    }
}

// ================================================================== Animation player

@Composable
fun AnimationCard(a: Anim, appReducedMotion: Boolean, speed: Float, wide: Boolean, onSpeed: (Float) -> Unit) {
    val reduce = reducedMotion(appReducedMotion)
    var step by remember(a.id) { mutableIntStateOf(0) }
    var playing by remember(a.id) { mutableStateOf(false) }
    var staticView by remember(a.id) { mutableStateOf(false) }
    var full by remember(a.id) { mutableStateOf(false) }
    val progress = remember(a.id) { Animatable(1f) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_PAUSE || e == Lifecycle.Event.ON_STOP) playing = false }
        lifecycle.addObserver(obs); onDispose { lifecycle.removeObserver(obs) }
    }
    // Animate the packets of the current step, then (if playing) dwell and advance.
    LaunchedEffect(a.id, step, playing, reduce) {
        if (reduce) progress.snapTo(1f)
        else { progress.snapTo(0f); progress.animateTo(1f, tween((1400 / speed).toInt().coerceAtLeast(200), easing = LinearEasing)) }
        if (playing) {
            delay((2600 / speed).toLong())
            if (step < a.steps.lastIndex) step++ else playing = false
        }
    }
    val cur = a.steps[step]
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("ANIMATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(a.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = { full = true }) { Icon(Icons.Filled.ZoomOutMap, "Open full screen (zoom and pan)") }
            }
            SourceTag("demo")
            Text(a.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(8.dp))
            if (!staticView) {
                AnimCanvas(a, step, progress.value, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                StepCaption(a, step)
                Controls(a, step, playing, speed, onPlay = { if (step == a.steps.lastIndex && !playing) step = 0; playing = !playing },
                    onPrev = { playing = false; if (step > 0) step-- }, onNext = { playing = false; if (step < a.steps.lastIndex) step++ },
                    onReset = { playing = false; step = 0 }, onSpeed = onSpeed)
                if (reduce) Text("Reduced motion is on: steps change without moving packets.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                a.tablesAt(step).filter { it.rows.isNotEmpty() }.forEach { t ->
                    Spacer(Modifier.height(8.dp))
                    InteractiveTable(LessonTable(t.id, t.title, t.headers, t.rows, kind = "demo", recall = false), wide, cur.rows[t.id]?.toSet() ?: emptySet())
                }
            } else {
                StaticSteps(a, wide)
            }
            TextButton(onClick = { staticView = !staticView; playing = false }) {
                Icon(if (staticView) Icons.Filled.PlayCircle else Icons.Filled.FormatListNumbered, null); Spacer(Modifier.width(6.dp))
                Text(if (staticView) "Back to the animation" else "Static step-by-step view")
            }
            if (a.note.isNotBlank()) Text(a.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (full) Dialog(onDismissRequest = { full = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxSize().padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(a.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { full = false }) { Icon(Icons.Filled.Close, "Close") }
                }
                Text("Pinch to zoom, drag to pan", style = MaterialTheme.typography.labelSmall)
                Box(Modifier.weight(1f).fillMaxWidth().pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ -> scale = (scale * zoom).coerceIn(1f, 4f); offset = if (scale == 1f) Offset.Zero else offset + pan }
                }, contentAlignment = Alignment.Center) {
                    AnimCanvas(a, step, progress.value, Modifier.fillMaxWidth().graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y))
                }
                StepCaption(a, step)
                Controls(a, step, playing, speed, onPlay = { if (step == a.steps.lastIndex && !playing) step = 0; playing = !playing },
                    onPrev = { playing = false; if (step > 0) step-- }, onNext = { playing = false; if (step < a.steps.lastIndex) step++ },
                    onReset = { playing = false; step = 0 }, onSpeed = onSpeed)
            }
        }
    }
}

@Composable
private fun StepCaption(a: Anim, step: Int) {
    val s = a.steps[step]
    Text("Step ${step + 1} of ${a.steps.size}: ${s.title}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Text(s.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp).semantics { contentDescription = "Step ${step + 1}: ${s.title}. ${s.text}" })
    LinearProgressIndicator(progress = { (step + 1f) / a.steps.size }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
}

@Composable
private fun Controls(a: Anim, step: Int, playing: Boolean, speed: Float, onPlay: () -> Unit, onPrev: () -> Unit, onNext: () -> Unit, onReset: () -> Unit, onSpeed: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onReset) { Icon(Icons.Filled.Replay, "Replay from the start") }
        IconButton(onClick = onPrev, enabled = step > 0) { Icon(Icons.Filled.SkipPrevious, "Previous step") }
        FilledIconButton(onClick = onPlay) { Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (playing) "Pause" else "Play") }
        IconButton(onClick = onNext, enabled = step < a.steps.lastIndex) { Icon(Icons.Filled.SkipNext, "Next step") }
        Spacer(Modifier.weight(1f))
        listOf(0.5f, 1f, 1.5f).forEach { v ->
            FilterChip(speed == v, { onSpeed(v) }, { Text("${if (v == 1f) "1" else v.toString()}×") }, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun StaticSteps(a: Anim, wide: Boolean) {
    a.steps.forEachIndexed { i, s ->
        Row(Modifier.padding(vertical = 6.dp)) {
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary) {
                Text("${i + 1}", color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(s.title, fontWeight = FontWeight.SemiBold)
                Text(s.text, style = MaterialTheme.typography.bodySmall)
                if (s.packets.isNotEmpty()) Text("Traffic: " + s.packets.joinToString("; ") { p -> "${a.nodes.first { it.id == p.from }.label} → ${a.nodes.first { it.id == p.to }.label}: ${p.label}" },
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                s.drop?.let { d -> Text("Stops at: ${a.nodes.first { it.id == d }.label}", style = MaterialTheme.typography.labelSmall, color = LocalSemantic.current.danger) }
            }
        }
    }
    a.tablesAt(a.steps.lastIndex).filter { it.rows.isNotEmpty() }.forEach { t ->
        Spacer(Modifier.height(8.dp)); InteractiveTable(LessonTable(t.id, t.title + " (final state)", t.headers, t.rows, kind = "demo", recall = false), wide)
    }
}

@Composable
fun AnimCanvas(a: Anim, step: Int, progress: Float, modifier: Modifier) {
    val tm = rememberTextMeasurer()
    val sem = LocalSemantic.current
    val dark = sem.dark
    val text = if (dark) Color(0xFFECEFF4) else Color(0xFF1B1F24)
    val line = if (dark) Color(0xFFB0BEC5) else Color(0xFF546E7A)
    val bg = MaterialTheme.colorScheme.surfaceContainer
    val hl = MaterialTheme.colorScheme.primary
    val cur = a.steps[step]
    fun statusColor(k: String) = when (k) { "ok" -> sem.success; "danger" -> sem.danger; "warn" -> sem.warning; else -> hl }
    Canvas(modifier.aspectRatio(1000f / 560f).semantics { contentDescription = "${a.title}. Step ${step + 1}: ${cur.title}. ${cur.text}" }) {
        val s = size.width / 1000f
        fun rect(nd: ANode) = Rect(nd.x * s, nd.y * s, (nd.x + nd.w) * s, (nd.y + nd.h) * s)
        val byId = a.nodes.associateBy { it.id }
        a.edges.forEach { e ->
            val ra = rect(byId.getValue(e.a)); val rb = rect(byId.getValue(e.b))
            val st = cur.links["${e.a}|${e.b}"] ?: cur.links["${e.b}|${e.a}"]
            val col = st?.let { statusColor(it) } ?: if (e.kind == "neutral") line else kindColors(e.kind, dark).second
            val dashed = st == "danger"
            drawLine(col, ra.center, rb.center, strokeWidth = (if (st != null) 7f else 4f) * s, pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(18f * s, 12f * s)) else null)
            if (e.label.isNotBlank()) {
                val lay = tm.measure(e.label, TextStyle(fontSize = (15f * s).toSp(), color = col, fontWeight = FontWeight.SemiBold), constraints = Constraints(maxWidth = (300 * s).toInt().coerceAtLeast(1)))
                val mid = Offset((ra.center.x + rb.center.x) / 2 - lay.size.width / 2f, (ra.center.y + rb.center.y) / 2 - lay.size.height / 2f)
                drawRoundRect(bg, mid - Offset(4f * s, 2f * s), Size(lay.size.width + 8f * s, lay.size.height + 4f * s), CornerRadius(6f * s))
                drawText(lay, topLeft = mid)
            }
        }
        a.nodes.forEach { nd ->
            val r = rect(nd)
            val (fill, stroke) = kindColors(nd.kind, dark)
            val on = nd.id in cur.nodes || nd.id in cur.badges
            drawRoundRect(fill, r.topLeft, r.size, CornerRadius(16f * s))
            drawRoundRect(if (on) hl else stroke, r.topLeft, r.size, CornerRadius(16f * s), style = Stroke(width = (if (on) 6f else 3f) * s))
            val l = tm.measure(nd.label, TextStyle(fontSize = (19f * s).toSp(), fontWeight = FontWeight.Bold, color = text, textAlign = TextAlign.Center), constraints = Constraints(maxWidth = r.width.toInt().coerceAtLeast(1)))
            val sub = if (nd.sub.isBlank()) null else tm.measure(nd.sub, TextStyle(fontSize = (13f * s).toSp(), color = text.copy(alpha = 0.85f), textAlign = TextAlign.Center),
                constraints = Constraints(maxWidth = r.width.toInt().coerceAtLeast(1)))
            val h = l.size.height + (sub?.size?.height ?: 0)
            val top = r.top + (r.height - h) / 2
            drawText(l, topLeft = Offset(r.left, top))
            if (sub != null) drawText(sub, topLeft = Offset(r.left, top + l.size.height))
            cur.badges[nd.id]?.let { b ->
                val lay = tm.measure(b, TextStyle(fontSize = (14f * s).toSp(), fontWeight = FontWeight.Bold, color = Color.White))
                val tl = Offset(r.center.x - lay.size.width / 2f - 6f * s, r.top - lay.size.height - 10f * s)
                drawRoundRect(if (nd.id == cur.drop) sem.danger else hl, tl, Size(lay.size.width + 12f * s, lay.size.height + 4f * s), CornerRadius(8f * s))
                drawText(lay, topLeft = tl + Offset(6f * s, 2f * s))
            }
            if (nd.id == cur.drop) {
                val c = Offset(r.right - 6f * s, r.top + 6f * s); val d = 14f * s
                drawCircle(sem.danger, 18f * s, c)
                drawLine(Color.White, c - Offset(d / 2, d / 2), c + Offset(d / 2, d / 2), 4f * s); drawLine(Color.White, c + Offset(-d / 2, d / 2), c + Offset(d / 2, -d / 2), 4f * s)
            }
        }
        cur.packets.forEach { p ->
            val from = rect(byId.getValue(p.from)).center; val to = rect(byId.getValue(p.to)).center
            val pos = Offset(from.x + (to.x - from.x) * progress, from.y + (to.y - from.y) * progress)
            val col = when (p.kind) { "ok" -> sem.success; "danger" -> sem.danger; "warn" -> sem.warning; "accent" -> hl; else -> kindColors(p.kind, dark).second }
            val lay = tm.measure(p.label, TextStyle(fontSize = (14f * s).toSp(), fontWeight = FontWeight.SemiBold, color = Color.White),
                constraints = Constraints(maxWidth = (260 * s).toInt().coerceAtLeast(1)))
            val tl = Offset(pos.x - lay.size.width / 2f - 8f * s, pos.y - lay.size.height / 2f - 4f * s)
            drawRoundRect(col, tl, Size(lay.size.width + 16f * s, lay.size.height + 8f * s), CornerRadius(10f * s))
            drawText(lay, topLeft = tl + Offset(8f * s, 4f * s))
        }
    }
}

/** Monospace output with tappable annotated lines (used by output-interpretation activities and the Verify tab). */
@Composable
fun AnnotatedOutput(lines: List<String>, annotations: List<Pair<String, String>>) {
    var open by remember(lines) { mutableStateOf<String?>(null) }
    val sem = LocalSemantic.current
    Surface(color = if (sem.dark) Color(0xFF0D1117) else Color(0xFF1E2329), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        SelectionContainer {
            Column(Modifier.horizontalScroll(rememberScrollState()).padding(10.dp)) {
                lines.forEach { l ->
                    val ann = annotations.firstOrNull { l.contains(it.first) }
                    Text(l.ifEmpty { " " }, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall,
                        color = if (ann != null) Color(0xFFFFD54F) else Color(0xFFE6EDF3),
                        modifier = if (ann != null) Modifier.clickable { open = if (open == ann.second) null else ann.second } else Modifier)
                }
            }
        }
    }
    if (annotations.isNotEmpty()) Text("Highlighted lines are annotated — tap one.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
    open?.let { Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Text(it, Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall) } }
}
