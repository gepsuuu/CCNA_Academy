package com.ccnaacademy.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.CenterFocusStrong
import com.ccnaacademy.app.data.diagramTextAlternative
import androidx.compose.ui.text.TextMeasurer
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
import com.ccnaacademy.app.data.DNode
import com.ccnaacademy.app.data.Diagram
import com.ccnaacademy.app.ui.theme.LocalSemantic
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Semantic palette for diagram elements: (fill, stroke). Consistent across all diagrams so legends stay meaningful. */
fun kindColors(kind: String, dark: Boolean): Pair<Color, Color> {
    val base = when (kind) {
        "app" -> Color(0xFF7E57C2); "transport" -> Color(0xFF1E88E5); "network" -> Color(0xFF00897B)
        "datalink" -> Color(0xFFF9A825); "physical" -> Color(0xFF8D6E63); "host" -> Color(0xFF5C6BC0)
        "server" -> Color(0xFF3949AB); "router" -> Color(0xFF00838F); "switch" -> Color(0xFF0277BD)
        "cloud" -> Color(0xFF78909C); "security" -> Color(0xFFD84315); "danger" -> Color(0xFFE53935)
        "ok" -> Color(0xFF2E7D32); "warn" -> Color(0xFFF57C00); "accent" -> Color(0xFF6A1B9A)
        "note" -> Color(0xFF546E7A); "mask" -> Color(0xFF26A69A); "vlan10" -> Color(0xFF1E88E5)
        "vlan20" -> Color(0xFF43A047); "vlan30" -> Color(0xFFFB8C00); "vlan99" -> Color(0xFF8E24AA)
        else -> Color(0xFF78909C)
    }
    return if (dark) base.copy(alpha = 0.38f) to base.copy(red = (base.red + 0.25f).coerceAtMost(1f), green = (base.green + 0.25f).coerceAtMost(1f), blue = (base.blue + 0.25f).coerceAtMost(1f))
    else base.copy(alpha = 0.16f) to base
}

/** Diagram bookmarks are provided app-wide so every DiagramCard can show a bookmark toggle. */
class DiagramBookmarks(val ids: Set<String>, val toggle: (String) -> Unit)
val LocalDiagramBookmarks = staticCompositionLocalOf<DiagramBookmarks?> { null }

@Composable
fun DiagramCard(d: Diagram, showExplanation: Boolean = true) {
    val bm = LocalDiagramBookmarks.current
    var full by remember { mutableStateOf(false) }
    val dark = LocalSemantic.current.dark
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("DIAGRAM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(d.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                if (bm != null && showExplanation) IconButton(onClick = { bm.toggle(d.id) }) {
                    Icon(if (d.id in bm.ids) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, if (d.id in bm.ids) "Remove diagram bookmark" else "Bookmark diagram")
                }
                IconButton(onClick = { full = true }) { Icon(Icons.Filled.ZoomOutMap, "Open full screen") }
            }
            Spacer(Modifier.height(8.dp))
            DiagramCanvas(d, Modifier.fillMaxWidth().clickable(onClickLabel = "Open zoomable diagram") { full = true })
            Legend(d, dark)
            if (showExplanation && d.explanation.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(d.explanation, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    if (full) {
        Dialog(onDismissRequest = { full = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxSize().padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(d.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        IconButton(onClick = { full = false }) { Icon(Icons.Filled.Close, "Close") }
                    }
                    Text("Pinch or use the buttons to zoom; drag to pan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    // Single-pointer alternative to pinch gestures (accessibility; also works with a mouse or switch access).
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { scale = (scale / 1.5f).coerceIn(1f, 5f); if (scale == 1f) offset = Offset.Zero }, enabled = scale > 1f) { Icon(Icons.Filled.ZoomOut, "Zoom out") }
                        Text("${(scale * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                        IconButton(onClick = { scale = (scale * 1.5f).coerceIn(1f, 5f) }, enabled = scale < 5f) { Icon(Icons.Filled.ZoomIn, "Zoom in") }
                        IconButton(onClick = { scale = 1f; offset = Offset.Zero }, enabled = scale != 1f || offset != Offset.Zero) { Icon(Icons.Filled.CenterFocusStrong, "Reset zoom and position") }
                    }
                    Box(Modifier.weight(1f).fillMaxWidth().pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset = if (scale == 1f) Offset.Zero else offset + pan
                        }
                    }, contentAlignment = Alignment.Center) {
                        DiagramCanvas(d, Modifier.fillMaxWidth().graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y))
                    }
                    Legend(d, LocalSemantic.current.dark)
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun Legend(d: Diagram, dark: Boolean) {
    if (d.legend.isEmpty()) return
    Spacer(Modifier.height(10.dp))
    Text("Legend", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
        d.legend.forEach { li ->
            val (fill, stroke) = kindColors(li.kind, dark)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(14.dp).background(fill, RoundedCornerShape(4.dp)).border(2.dp, stroke, RoundedCornerShape(4.dp)))
                Spacer(Modifier.width(6.dp))
                Text(li.label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun DiagramCanvas(d: Diagram, modifier: Modifier = Modifier) {
    val tm = rememberTextMeasurer()
    val dark = LocalSemantic.current.dark
    val textColor = if (dark) Color(0xFFECEFF4) else Color(0xFF1B1F24)
    val lineColor = if (dark) Color(0xFFB0BEC5) else Color(0xFF455A64)
    val labelBg = MaterialTheme.colorScheme.surfaceContainer
    val description = remember(d.id) { diagramTextAlternative(d) }
    Canvas(modifier.aspectRatio(d.width / d.height).semantics { contentDescription = description }) {
        val s = size.width / d.width
        val nodes = d.nodes.associateBy { it.id }
        fun r(n: DNode) = Rect(n.x * s, n.y * s, (n.x + n.w) * s, (n.y + n.h) * s)

        // Edges first (under nodes)
        d.edges.forEach { e ->
            val a = nodes[e.src] ?: return@forEach; val b = nodes[e.dst] ?: return@forEach
            val ra = r(a); val rb = r(b)
            val p1 = clip(ra, rb.center); val p2 = clip(rb, ra.center)
            val col = if (e.kind == "neutral") lineColor else kindColors(e.kind, dark).second
            val effect = if (e.style == "dashed") PathEffect.dashPathEffect(floatArrayOf(14f * s * 1.4f, 10f * s * 1.4f)) else null
            drawLine(col, p1, p2, strokeWidth = (4f * s).coerceAtLeast(2.5f), pathEffect = effect)
            if (e.arrow == "end" || e.arrow == "both") arrow(p1, p2, col, s)
            if (e.arrow == "both") arrow(p2, p1, col, s)
            if (e.label.isNotBlank()) {
                val mid = Offset((p1.x + p2.x) / 2, (p1.y + p2.y) / 2)
                val st = TextStyle(fontSize = (17f * s).toSp(), color = col, fontWeight = FontWeight.SemiBold)
                val lay = tm.measure(e.label, st, constraints = Constraints(maxWidth = (360 * s).toInt().coerceAtLeast(1)))
                val tl = Offset(mid.x - lay.size.width / 2f, mid.y - lay.size.height / 2f)
                drawRoundRect(labelBg, tl - Offset(6f * s, 3f * s), Size(lay.size.width + 12f * s, lay.size.height + 6f * s), CornerRadius(8f * s))
                drawText(lay, topLeft = tl)
            }
        }

        // Nodes
        d.nodes.forEach { n ->
            val rect = r(n)
            val (fill, stroke) = kindColors(n.kind, dark)
            val radius = when (n.shape) { "rect" -> 6f * s; "pill" -> rect.height / 2; else -> 18f * s }
            drawRoundRect(fill, rect.topLeft, rect.size, CornerRadius(radius))
            drawRoundRect(stroke, rect.topLeft, rect.size, CornerRadius(radius), style = Stroke(width = (3f * s).coerceAtLeast(2f)))
            val mono = n.sub.contains('\n') && n.shape == "rect"
            centeredText(tm, n.label, n.sub, rect, s, textColor, mono)
        }

        // Free text
        d.texts.forEach { t ->
            val st = TextStyle(fontSize = (t.size * s).toSp(), color = textColor, fontWeight = if (t.bold) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center)
            val w = (d.width * s).toInt()
            val lay = tm.measure(t.text, st, constraints = Constraints(maxWidth = w))
            drawText(lay, topLeft = Offset(t.x * s - w / 2f, t.y * s - lay.size.height / 2f))
        }
    }
}

private fun clip(r: Rect, toward: Offset): Offset {
    val c = r.center; val dx = toward.x - c.x; val dy = toward.y - c.y
    if (dx == 0f && dy == 0f) return c
    val sx = if (dx != 0f) (r.width / 2) / abs(dx) else Float.MAX_VALUE
    val sy = if (dy != 0f) (r.height / 2) / abs(dy) else Float.MAX_VALUE
    val k = minOf(sx, sy)
    return Offset(c.x + dx * k, c.y + dy * k)
}

private fun DrawScope.arrow(from: Offset, to: Offset, color: Color, s: Float) {
    val ang = atan2(to.y - from.y, to.x - from.x)
    val len = (18f * s).coerceAtLeast(10f); val spread = 0.45f
    val p = Path().apply {
        moveTo(to.x, to.y)
        lineTo(to.x - len * cos(ang - spread), to.y - len * sin(ang - spread))
        lineTo(to.x - len * cos(ang + spread), to.y - len * sin(ang + spread))
        close()
    }
    drawPath(p, color)
}

private fun DrawScope.centeredText(tm: TextMeasurer, label: String, sub: String, rect: Rect, s: Float, color: Color, mono: Boolean) {
    val pad = 8f * s
    val maxW = (rect.width - 2 * pad).toInt().coerceAtLeast(1)
    var labelSize = 22f; var subSize = 17f
    repeat(5) {
        val l = tm.measure(label, TextStyle(fontSize = (labelSize * s).toSp(), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), constraints = Constraints(maxWidth = maxW))
        val sb = if (sub.isBlank()) null else tm.measure(sub, TextStyle(fontSize = (subSize * s).toSp(), textAlign = if (mono) TextAlign.Start else TextAlign.Center,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default), constraints = Constraints(maxWidth = maxW))
        val total = l.size.height + (sb?.size?.height ?: 0)
        if (total <= rect.height - pad || it == 4) {
            val top = rect.top + (rect.height - total) / 2
            drawText(l, color = color, topLeft = Offset(rect.left + pad, top))
            if (sb != null) drawText(sb, color = color.copy(alpha = 0.85f), topLeft = Offset(rect.left + pad, top + l.size.height))
            return
        }
        labelSize *= 0.88f; subSize *= 0.88f
    }
}
