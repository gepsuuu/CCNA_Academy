package com.ccnaacademy.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ccnaacademy.app.ui.theme.LocalSemantic

/** Centers content and limits line length on tablets / landscape. */
@Composable
fun ContentWidth(max: Dp = 900.dp, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = max).fillMaxWidth(), content = content)
    }
}

@Composable
fun SectionCard(
    title: String, icon: ImageVector? = null, accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier, container: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    trailing: @Composable (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = container), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.animateContentSize().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Box(Modifier.size(34.dp).background(accent.copy(alpha = 0.15f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                }
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                trailing?.invoke()
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun Bullet(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Box(Modifier.padding(top = 8.dp).size(6.dp).background(color, RoundedCornerShape(3.dp)))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun Tag(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(8.dp)) {
        Text(text, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), maxLines = 1)
    }
}

@Composable
fun ExamTag(required: Boolean) {
    val s = LocalSemantic.current
    if (required) Tag("Exam objective", s.exam) else Tag("Foundation / supplementary", s.supplementary)
}

@Composable
fun ProgressRing(value: Float, label: String, sub: String, size: Dp = 112.dp, color: Color = MaterialTheme.colorScheme.primary) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(6.dp)) {
            val st = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            drawArc(track, 0f, 360f, false, style = st)
            drawArc(color, -90f, 360f * value.coerceIn(0f, 1f), false, style = st)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LabeledBar(label: String, value: Float, trailing: String, color: Color = MaterialTheme.colorScheme.primary) {
    Column(Modifier.padding(vertical = 5.dp)) {
        Row {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(trailing, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = { value.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color, trackColor = MaterialTheme.colorScheme.surfaceVariant, strokeCap = StrokeCap.Round, drawStopIndicator = {})
    }
}

/** Simple bar chart with labels underneath. Values are normalized to the max value. */
@Composable
fun BarChart(values: List<Float>, labels: List<String>, modifier: Modifier = Modifier.fillMaxWidth().height(120.dp), color: Color = MaterialTheme.colorScheme.primary) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().weight(1f)) {
            val n = values.size.coerceAtLeast(1)
            val maxV = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)
            val slot = size.width / n
            val bw = slot * 0.55f
            values.forEachIndexed { i, v ->
                val x = i * slot + (slot - bw) / 2
                drawRoundRect(track, Offset(x, 0f), Size(bw, size.height), CornerRadius(8f))
                val h = size.height * (v / maxV)
                drawRoundRect(color, Offset(x, size.height - h), Size(bw, h), CornerRadius(8f))
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            labels.forEach { Text(it, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

/** Line chart for score history (0..100). */
@Composable
fun LineChart(points: List<Float>, modifier: Modifier = Modifier.fillMaxWidth().height(140.dp), color: Color = MaterialTheme.colorScheme.secondary) {
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val pass = LocalSemantic.current.success
    Canvas(modifier) {
        listOf(0f, 0.5f, 1f).forEach { f -> drawLine(grid, Offset(0f, size.height * f), Offset(size.width, size.height * f), 2f) }
        val passY = size.height * (1 - 0.825f)
        drawLine(pass.copy(alpha = 0.6f), Offset(0f, passY), Offset(size.width, passY), 3f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 10f)))
        if (points.isEmpty()) return@Canvas
        val step = if (points.size == 1) 0f else size.width / (points.size - 1)
        val pts = points.mapIndexed { i, v -> Offset(if (points.size == 1) size.width / 2 else i * step, size.height * (1 - v / 100f)) }
        for (i in 0 until pts.size - 1) drawLine(color, pts[i], pts[i + 1], 6f, cap = StrokeCap.Round)
        pts.forEach { drawCircle(color, 9f, it) }
    }
}

@Composable
fun StatTile(title: String, value: String, sub: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Records active study time while the composable is visible and the app is in the foreground. */
@Composable
fun StudyTimer(onElapsed: (Long) -> Unit) {
    val owner = LocalLifecycleOwner.current
    val cb by rememberUpdatedState(onElapsed)
    DisposableEffect(owner) {
        var start = System.currentTimeMillis()
        var running = true
        fun flush() { if (running) { cb((System.currentTimeMillis() - start) / 1000); running = false } }
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_PAUSE -> flush()
                Lifecycle.Event.ON_RESUME -> if (!running) { start = System.currentTimeMillis(); running = true }
                else -> {}
            }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs); flush() }
    }
}

@Composable
fun EmptyHint(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 6.dp))
}
