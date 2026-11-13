package com.ccnaacademy.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic

@Composable
fun ReviseHubScreen(s: AppScope) {
    LaunchedEffect(Unit) { s.vm.ensurePlan() }
    val cs = remember(s.p) { CardStats(s.lib, s.p) }
    val plan = s.p.plan?.takeIf { it.day == today() }
    val tasks = plan?.let { DailyPlanner.tasks(s.c, s.lib, s.p, it) } ?: emptyList()
    val full: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Revise", s)
        LazyVerticalGrid(GridCells.Adaptive(170.dp), contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item(span = full) { PlanSummaryCard(s, tasks) }
            item { HubTile("Flashcards", "${cs.dueToday} due • ${cs.overdue} overdue", Icons.Filled.Style) { s.go("flashcards") } }
            item { HubTile("Memorization Center", "${s.lib.guides.size} guides • daily challenge", Icons.Filled.Psychology) { s.go("memory") } }
            item { HubTile("Cheatsheets", "${s.lib.cheatsheets.size} sheets • ${s.p.sheetBookmarks.size} bookmarked", Icons.Filled.Description) { s.go("sheets") } }
            item { HubTile("Speed techniques", "${s.lib.techniques.size} methods + timed drills", Icons.Filled.Speed) { s.go("speed") } }
            item { HubTile("Subnetting center", "Lessons • practice • timed challenge", Icons.Filled.Calculate) { s.go("subnet") } }
            item { HubTile("Daily challenge", "${s.p.challengeDone[today()]?.size ?: 0}/5 today • ${DailyChallenges.streak(s.p)}-day streak", Icons.Filled.EventAvailable) { s.go("daily") } }
            item { HubTile("Study planner", s.p.planner?.let { "Exam ${dayLabel(it.examDay)}" } ?: "Create a personal schedule", Icons.Filled.CalendarMonth) { s.go("planner") } }
            item { HubTile("Networking tutor", "Explain • compare • subnet step by step", Icons.Filled.SupportAgent) { s.go("tutor") } }
            item { HubTile("Comparison center", "${s.study.comparisons.size} side-by-side comparisons", Icons.Filled.CompareArrows) { s.go("comparisons") } }
            item { HubTile("Common mistakes", "${s.study.mistakes.size} exam traps explained", Icons.Filled.ReportProblem) { s.go("mistakes?focus=") } }
            item { HubTile("Glossary", "${s.study.glossary.size} terms, A–Z", Icons.Filled.MenuBook) { s.go("glossary?term=") } }
            item { HubTile("Memory challenge", s.p.challengeScores[today()]?.let { "Today: $it%" } ?: "Not done today", Icons.Filled.EmojiEvents) { s.go("challenge") } }
        }
    }
}

@Composable
private fun HubTile(title: String, sub: String, icon: ImageVector, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().heightIn(min = 120.dp).clickable(onClick = onClick), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(sub, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PlanSummaryCard(s: AppScope, tasks: List<PlanTask>) {
    val done = tasks.count { it.done }
    Card(Modifier.fillMaxWidth().clickable { s.go("plan") }, shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(if (tasks.isEmpty()) 0f else done.toFloat() / tasks.size, "$done/${tasks.size}", "done", size = 92.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Today's revision plan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(tasks.firstOrNull { !it.done }?.let { "Next: ${it.title}" } ?: if (tasks.isEmpty()) "Preparing…" else "All done — great work!",
                    style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.Filled.ChevronRight, null)
        }
    }
}

@Composable
fun DailyPlanScreen(s: AppScope) {
    LaunchedEffect(Unit) { s.vm.ensurePlan() }
    val plan = s.p.plan?.takeIf { it.day == today() }
    val sem = LocalSemantic.current
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Today's plan", s, back = true) {
            IconButton(onClick = { s.vm.regeneratePlan() }) { Icon(Icons.Filled.Refresh, "Rebuild plan") }
        }
        if (plan == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return@Column }
        val tasks = DailyPlanner.tasks(s.c, s.lib, s.p, plan)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                ContentWidth {
                    Text("Your routine adapts every day to due flashcards, weak domains, recall scores and drill accuracy.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LabeledBar("Completed", tasks.count { it.done }.toFloat() / tasks.size, "${tasks.count { it.done }}/${tasks.size}", sem.success)
                }
            }
            items(tasks, key = { it.key }) { t ->
                ContentWidth {
                    Card(Modifier.fillMaxWidth().clickable { s.go(t.route) }, shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = if (t.done) sem.success.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (t.done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked, null,
                                tint = if (t.done) sem.success else MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(t.detail, style = MaterialTheme.typography.bodySmall)
                                Text("Why: ${t.why}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Filled.ChevronRight, null)
                        }
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Bonus: daily memory challenge", Icons.Filled.EmojiEvents, accent = sem.warning) {
                        Text("8 rapid active-recall prompts mixed from every memorization guide.", style = MaterialTheme.typography.bodyMedium)
                        Button(onClick = { s.go("challenge") }, modifier = Modifier.padding(top = 8.dp)) {
                            Text(s.p.challengeScores[today()]?.let { "Done today ($it%) — try again" } ?: "Start challenge")
                        }
                    }
                }
            }
        }
    }
}
