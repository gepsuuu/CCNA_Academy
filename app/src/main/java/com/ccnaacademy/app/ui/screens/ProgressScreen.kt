package com.ccnaacademy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(s: AppScope) {
    val st = remember(s.c, s.p) { Stats(s.c, s.p, s.lib) }
    val sem = LocalSemantic.current
    val full: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }
    val ranked = st.domainStats.sortedByDescending { it.readiness }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Progress", s)
        LazyVerticalGrid(GridCells.Adaptive(160.dp), contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { StatTile("Lessons", "${st.completedCount}/${st.totalLessons}", "completed", Icons.Filled.MenuBook, MaterialTheme.colorScheme.primary) }
            item { StatTile("Answers", "${st.correctTotal}✓ ${st.incorrectTotal}✗", st.overallAccuracy?.let { "${(it * 100).roundToInt()}% accuracy" } ?: "No answers yet", Icons.Filled.Quiz, MaterialTheme.colorScheme.secondary) }
            item { StatTile("Flashcards", "${st.cardsMastered}", "of ${s.lib.cards.size} mastered", Icons.Filled.Style, sem.success) }
            item { StatTile("Study time", formatDuration(st.totalSeconds), "streak ${st.streak} days", Icons.Filled.Timer, sem.warning) }

            item(span = full) {
                SectionCard("Skills (tracked separately)", Icons.Filled.Terminal) {
                    listOf("concept" to "Concepts (tables, teach-back)", "cli" to "CLI syntax (labs)", "output" to "Output interpretation", "troubleshoot" to "Troubleshooting").forEach { (k, label) ->
                        val q = s.p.skills[k]; val n = (q?.correct ?: 0) + (q?.incorrect ?: 0)
                        LabeledBar(label, if (n == 0) 0f else q!!.correct.toFloat() / n, if (n == 0) "no data yet" else "${q!!.correct}/$n")
                    }
                    val labs = com.ccnaacademy.app.lab.LabCatalog.all
                    val guided = labs.filter { !it.assessed }; val indep = labs.filter { it.assessed }
                    Text("Labs: ${guided.count { s.p.labs[it.id]?.completedAt != null }}/${guided.size} guided or troubleshooting completed · " +
                        "${indep.count { s.p.labs[it.id]?.completedAt != null }}/${indep.size} independent challenges mastered",
                        style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                    Text("CLI syntax counts completed lab tasks as correct and commands the trainer rejected as incorrect.", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item(span = full) {
                SectionCard("Readiness by domain", Icons.Filled.Insights) {
                    Text("Blends lesson completion (30%), quiz accuracy (35%), flashcard mastery (15%), and recent exam simulations (20%).",
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    st.domainStats.forEach { d ->
                        val col = when { d.readiness >= 0.8f -> sem.success; d.readiness >= 0.5f -> sem.warning; else -> sem.danger }
                        LabeledBar("${d.domain.number} ${d.domain.title} (${d.domain.weight}%)", d.readiness, "${(d.readiness * 100).roundToInt()}%", col)
                        Text("Completion ${(d.completion * 100).roundToInt()}% • Accuracy ${d.accuracy?.let { "${(it * 100).roundToInt()}%" } ?: "—"} (${d.attempts}) • Cards ${(d.mastery * 100).roundToInt()}%" +
                                (d.examScore?.let { " • Exams ${(it * 100).roundToInt()}%" } ?: ""),
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item(span = full) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionCard("Strengths", Icons.Filled.ThumbUp, accent = sem.success, modifier = Modifier.weight(1f)) {
                        val strong = ranked.filter { it.readiness >= 0.6f }.take(3)
                        if (strong.isEmpty()) EmptyHint("Build strengths by completing lessons and checks.")
                        strong.forEach { Bullet(it.domain.title, sem.success) }
                    }
                    SectionCard("Needs work", Icons.Filled.TrendingDown, accent = sem.danger, modifier = Modifier.weight(1f)) {
                        ranked.reversed().take(3).forEach { Bullet("${it.domain.title} (${(it.readiness * 100).roundToInt()}%)", sem.danger) }
                    }
                }
            }
            item(span = full) {
                SectionCard("Topics requiring revision", Icons.Filled.Warning, accent = sem.danger) {
                    val due = st.revisions.filter { it.dueEpochDay <= today() }
                    if (st.weakTopics.isEmpty() && due.isEmpty()) EmptyHint("Nothing flagged right now.")
                    st.weakTopics.take(6).forEach { w -> ListRow(w.lesson.title, "Accuracy ${(w.accuracy * 100).roundToInt()}% • ${w.attempts} answers") { s.lesson(w.lesson.id) } }
                    due.forEach { r -> ListRow(r.lesson.title, "Scheduled revision ${dueLabel(r.dueEpochDay).lowercase()}") { s.lesson(r.lesson.id) } }
                }
            }
            item(span = full) {
                SectionCard("Memorization & speed", Icons.Filled.Psychology) {
                    val cs = st.cards
                    LabeledBar("Flashcards mastered (interval ≥ 21 days)", if (cs.total == 0) 0f else cs.mastered.toFloat() / cs.total, "${cs.mastered}/${cs.total}", sem.success)
                    LabeledBar("Flashcards in learning", if (cs.total == 0) 0f else cs.learning.toFloat() / cs.total, "${cs.learning}", MaterialTheme.colorScheme.primary)
                    Text("Overdue: ${cs.overdue} • Repeatedly forgotten: ${cs.leeches.size}", style = MaterialTheme.typography.labelMedium)
                    cs.leeches.take(5).forEach { Bullet(it.front, sem.danger) }
                    val recalls = s.lib.guides.mapNotNull { g -> DailyPlanner.recallAccuracy(s.p, g)?.let { g to it } }.sortedBy { it.second }
                    if (recalls.isNotEmpty()) {
                        Text("Memorization recall (weakest first)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                        recalls.take(5).forEach { (g, a) -> LabeledBar(g.title, a, "${(a * 100).roundToInt()}%", if (a >= 0.8f) sem.success else sem.danger) }
                    }
                    val sub = s.p.genStats.values.let { v -> v.sumOf { it.correct } to v.sumOf { it.correct + it.incorrect } }
                    if (sub.second > 0) LabeledBar("Subnetting / calculation accuracy", sub.first.toFloat() / sub.second, "${sub.first}/${sub.second}", MaterialTheme.colorScheme.secondary)
                    val ch = s.p.drills.filter { it.kind == "challenge" }.takeLast(10)
                    if (ch.isNotEmpty()) {
                        Text("Timed challenge scores", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                        LineChart(ch.map { it.percent.toFloat() }, Modifier.fillMaxWidth().height(100.dp))
                    }
                }
            }
            item(span = full) {
                SectionCard("Exam simulation history", Icons.Filled.ShowChart) {
                    if (s.p.exams.isEmpty()) EmptyHint("No exam simulations yet. Your scores will be charted here (dashed line ≈ 82.5% target).")
                    else {
                        LineChart(s.p.exams.takeLast(15).map { it.percent.toFloat() })
                        Spacer(Modifier.height(8.dp))
                        s.p.exams.takeLast(5).reversed().forEach { e ->
                            Row(Modifier.padding(vertical = 4.dp)) {
                                Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(e.timestamp)),
                                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text("${e.percent}% (${e.correct}/${e.total}) • ${formatDuration(e.durationSec.toLong())}", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            item(span = full) {
                SectionCard("Study history — last 14 days", Icons.Filled.CalendarMonth) {
                    val days = st.lastDays(14)
                    BarChart(days.map { it.second / 60f }, days.map { java.time.LocalDate.ofEpochDay(it.first).dayOfMonth.toString() })
                    Text("Minutes per day", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
