package com.ccnaacademy.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardScreen(s: AppScope) {
    val st = remember(s.c, s.p) { Stats(s.c, s.p, s.lib) }
    LaunchedEffect(Unit) { s.vm.ensurePlan() }
    val planTasks = s.p.plan?.takeIf { it.day == today() }?.let { DailyPlanner.tasks(s.c, s.lib, s.p, it) } ?: emptyList()
    val sem = LocalSemantic.current
    val next = st.nextLesson()
    val tip = s.c.tips[(today() % s.c.tips.size).toInt()]
    val full: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }
    val badges = remember(s.p, s.bank) { Achievements.compute(s.c, s.lib, s.bank, s.p) }
    LaunchedEffect(badges) { s.vm.syncBadges(badges.filter { it.earned }.map { it.id }) }
    val plannerToday = remember(s.p) { s.p.planner?.let { Planner.build(s.c, s.lib, s.p, it).week.firstOrNull() } }
    val challengeDone = s.p.challengeDone[today()]?.size ?: 0
    val questionsDone = s.p.bankStats.size

    Column(Modifier.fillMaxSize()) {
        AppTopBar("CCNA Academy", s)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Continue learning
            item(span = full) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(if (next == null) "All lessons completed" else "Continue learning", style = MaterialTheme.typography.labelLarge)
                            Text(next?.title ?: "Keep your edge with revisions and exam simulations",
                                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (next != null) Text("${s.c.domain(next.domainId).number} ${s.c.domain(next.domainId).title} • ${next.minutes} min",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.width(12.dp))
                        FilledTonalButton(onClick = { if (next != null) s.lesson(next.id) else s.go("examsetup") }) {
                            Text(if (next != null) "Continue" else "Exam")
                            Spacer(Modifier.width(6.dp)); Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                        }
                    }
                }
            }
            item(span = full) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
                    FeatureCard("Study planner", Icons.Filled.CalendarMonth, Modifier.weight(1f).fillMaxHeight(), onClick = { s.go("planner") },
                        line1 = s.p.planner?.let { "Exam ${dayLabel(it.examDay)}" } ?: "Set your exam date",
                        line2 = plannerToday?.let { d -> if (d.study) "Today: ${d.theme?.title ?: ""}" else "Rest day" } ?: "Get a personalized weekly schedule")
                    FeatureCard("Daily challenge", Icons.Filled.EmojiEvents, Modifier.weight(1f).fillMaxHeight(), onClick = { s.go("daily") },
                        line1 = "$challengeDone/5 done today", line2 = "${DailyChallenges.streak(s.p)}-day challenge streak")
                    if (s.wide) FeatureCard("Networking tutor", Icons.Filled.Psychology, Modifier.weight(1f).fillMaxHeight(), onClick = { s.go("tutor") },
                        line1 = "Explain, compare, subnet", line2 = "Offline knowledge base or your AI service")
                }
            }
            if (!s.wide) item(span = full) {
                FeatureCard("Networking tutor", Icons.Filled.Psychology, Modifier.fillMaxWidth(), onClick = { s.go("tutor") },
                    line1 = "Explain concepts, compare protocols, solve subnetting step by step", line2 = "Works offline; optional AI service in Settings")
            }
            item(span = full) { PlanSummaryCard(s, planTasks) }
            // Progress & readiness rings
            item(span = full) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ProgressRing(st.overallProgress, "${(st.overallProgress * 100).toInt()}%", "complete")
                            Text("Learning progress", style = MaterialTheme.typography.labelLarge)
                            Text("${st.completedCount}/${st.totalLessons} lessons", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val col = when { st.readiness >= 0.8f -> sem.success; st.readiness >= 0.5f -> sem.warning; else -> MaterialTheme.colorScheme.tertiary }
                            ProgressRing(st.readiness, "${(st.readiness * 100).toInt()}%", "ready", color = col)
                            Text("Exam readiness", style = MaterialTheme.typography.labelLarge)
                            Text("weighted by blueprint", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            item { StatTile("Day streak", "${st.streak}", if (st.todaySeconds >= 60) "Studied today" else "Study today to extend", Icons.Filled.LocalFireDepartment, sem.warning) }
            item { StatTile("Today", formatDuration(st.todaySeconds), "This week ${formatDuration(st.weekSeconds)}", Icons.Filled.Timer, MaterialTheme.colorScheme.primary) }
            item { StatTile("Chapters", "${st.chaptersCompleted}/${st.chaptersTotal}", "completed", Icons.Filled.CheckCircle, sem.success) }
            item { StatTile("Questions", "$questionsDone/${s.bank.questions.size}", "bank questions attempted", Icons.Filled.Quiz, MaterialTheme.colorScheme.primary) }
            item { StatTile("Flashcards", "${st.cardsMastered}", "of ${s.lib.cards.size} mastered", Icons.Filled.Style, MaterialTheme.colorScheme.secondary) }
            item { StatTile("Accuracy", st.overallAccuracy?.let { "${(it * 100).toInt()}%" } ?: "—", "${st.correctTotal} correct • ${st.incorrectTotal} wrong", Icons.Filled.TrackChanges, MaterialTheme.colorScheme.secondary) }

            item(span = full) {
                SectionCard("Study time — last 7 days", Icons.Filled.BarChart) {
                    val days = st.lastDays(7)
                    BarChart(days.map { it.second / 60f }, days.map { LocalDate.ofEpochDay(it.first).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) })
                    val studied = days.count { it.second >= 60 }
                    Text("This week: ${formatDuration(st.weekSeconds)} over $studied day(s) • average ${formatDuration(st.weekSeconds / 7)}/day • all time ${formatDuration(st.totalSeconds)}",
                        style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                }
            }
            item(span = full) {
                SectionCard("Domain performance", Icons.Filled.Insights, trailing = { TextButton(onClick = { s.top("progress") }) { Text("Details") } }) {
                    st.domainStats.forEach { d ->
                        LabeledBar("${d.domain.number} ${d.domain.title}", d.readiness, "${(d.readiness * 100).toInt()}%",
                            when { d.readiness >= 0.8f -> sem.success; d.readiness >= 0.5f -> sem.warning; else -> sem.danger })
                    }
                }
            }
            item(span = full) {
                SectionCard("Weak topics", Icons.Filled.Warning, accent = sem.danger) {
                    val bankWeak = BankEngine.weakTopics(s.p).take(3)
                    if (st.weakTopics.isEmpty() && bankWeak.isEmpty()) EmptyHint("No weak topics yet. Topics below 70% accuracy in knowledge checks or practice will appear here.")
                    st.weakTopics.take(3).forEach { w ->
                        ListRow(w.lesson.title, "Lesson checks: ${(w.accuracy * 100).toInt()}% over ${w.attempts} answers") { s.lesson(w.lesson.id) }
                    }
                    bankWeak.forEach { t ->
                        ListRow(s.bank.topicTitle(t.topic), "Question bank: ${(t.acc * 100).toInt()}% (${t.correct}/${t.total}) • tap to practice") {
                            s.go(bankRoute("topic", 0, 0, BankFilter(topic = t.topic)))
                        }
                    }
                }
            }
            item(span = full) {
                SectionCard("Upcoming revision sessions", Icons.Filled.Event, accent = MaterialTheme.colorScheme.tertiary) {
                    if (st.revisions.isEmpty()) EmptyHint("Complete a lesson to schedule spaced revisions (1, 3, 7, 14, 30, 60 days).")
                    st.revisions.take(4).forEach { r -> ListRow(r.lesson.title, "${dueLabel(r.dueEpochDay)} • review ${r.stage + 1} of 6") { s.lesson(r.lesson.id) } }
                }
            }
            item(span = full) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAction("Flashcards", "${st.cardsDue} due • ${st.cardsMastered} mastered", Icons.Filled.Style, Modifier.weight(1f)) { s.go("flashcards") }
                    QuickAction("Practice", "${s.bank.questions.size} questions", Icons.Filled.Quiz, Modifier.weight(1f)) { s.top("practice") }
                    if (s.wide) QuickAction("Exam simulation", "Timed, blueprint-weighted", Icons.Filled.AssignmentTurnedIn, Modifier.weight(1f)) { s.go("examsetup") }
                }
            }
            if (!s.wide) item(span = full) {
                QuickAction("Exam simulation", "Timed, blueprint-weighted mock exam", Icons.Filled.AssignmentTurnedIn, Modifier.fillMaxWidth()) { s.go("examsetup") }
            }
            item(span = full) {
                SectionCard("Milestones", Icons.Filled.WorkspacePremium, trailing = { TextButton(onClick = { s.go("achievements") }) { Text("All ${badges.size}") } }) {
                    Text("${badges.count { it.earned }} of ${badges.size} reached", style = MaterialTheme.typography.labelLarge)
                    badges.filter { !it.earned }.sortedByDescending { it.progress }.take(2).forEach { b ->
                        LabeledBar("Next: ${b.title}", b.progress, "${b.current}/${b.goal}", MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            item(span = full) {
                SectionCard("Daily networking tip", Icons.Filled.Lightbulb, accent = sem.warning) {
                    Text(tip, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun ListRow(title: String, sub: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(sub, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Filled.ChevronRight, null)
    }
}

@Composable
fun QuickAction(title: String, sub: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null)
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(sub, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun FeatureCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, line1: String, line2: String, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(line1, style = MaterialTheme.typography.bodySmall)
            Text(line2, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
