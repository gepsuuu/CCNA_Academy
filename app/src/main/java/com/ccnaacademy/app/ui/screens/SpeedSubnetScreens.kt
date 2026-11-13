package com.ccnaacademy.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic
import kotlin.math.roundToInt

private fun drillRoute(kind: String, ref: String, level: Int, types: List<String>, count: Int, limit: Int, typed: Boolean) =
    "drill?kind=$kind&ref=$ref&level=$level&types=${types.joinToString(",")}&count=$count&limit=$limit&typed=$typed"

// ============================================================ Speed techniques
@Composable
fun SpeedListScreen(s: AppScope) {
    val sem = LocalSemantic.current
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Exam speed techniques", s, back = true)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { ContentWidth { Text("Each technique shows the method, a worked example, fast vs traditional comparison, when the shortcut is safe, and a timed drill.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            items(s.lib.techniques, key = { it.id }) { t ->
                ContentWidth {
                    val runs = s.p.drills.filter { it.kind == "speed" && it.refId == t.id }
                    val best = runs.maxByOrNull { it.percent }
                    Card(Modifier.fillMaxWidth().clickable { s.go("technique/${t.id}") }, shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Speed, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("Target ≈ ${t.targetSeconds}s per question" + (best?.let { " • best ${it.percent}% in ${it.seconds}s" } ?: " • not practiced"),
                                    style = MaterialTheme.typography.labelSmall)
                            }
                            if (best != null) Tag("${best.percent}%", if (best.percent >= 80) sem.success else sem.warning)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TechniqueScreen(s: AppScope, id: String) {
    val t = s.lib.techniques.firstOrNull { it.id == id } ?: return
    var level by rememberSaveable { mutableIntStateOf(1) }
    val sem = LocalSemantic.current
    StudyTimer { s.vm.addStudyTime(it) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar(t.title, s, back = true)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth {
                SectionCard("1 • The method", Icons.Filled.Bolt, accent = sem.warning) {
                    Text(t.method, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(6.dp))
                    t.steps.forEachIndexed { i, st -> Bullet("${i + 1}. $st") }
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("2 • Worked example", Icons.Filled.Science, accent = MaterialTheme.colorScheme.secondary) {
                    Text(t.example.title, fontWeight = FontWeight.SemiBold)
                    t.example.steps.forEachIndexed { i, st -> Bullet("Step ${i + 1}: $st", MaterialTheme.colorScheme.secondary) }
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("3 • Fast vs traditional", Icons.Filled.CompareArrows) {
                    TableView(Block("table", headers = listOf("", "Fast method", "Traditional"), rows = t.compare.map { listOf(it.aspect, it.fast, it.traditional) }), 0, null)
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("4 • When the shortcut is safe", Icons.Filled.VerifiedUser, accent = sem.success) {
                    t.safe.forEach { Bullet(it, sem.success) }
                    if (t.pitfalls.isNotEmpty()) {
                        Text("Watch out for", fontWeight = FontWeight.SemiBold, color = sem.danger, modifier = Modifier.padding(top = 8.dp))
                        t.pitfalls.forEach { Bullet(it, sem.danger) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("5 • Timed practice", Icons.Filled.Timer, container = MaterialTheme.colorScheme.primaryContainer) {
                    val n = if (t.generators.isNotEmpty()) 10 else t.questions.size
                    Text("$n questions • target ${t.targetSeconds}s each (${(n * t.targetSeconds) / 60} min ${(n * t.targetSeconds) % 60} s total). " +
                        "The clock shows your pace against the target.", style = MaterialTheme.typography.bodyMedium)
                    if (t.generators.isNotEmpty()) {
                        Text("Difficulty", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Gen.LEVELS.forEachIndexed { i, l -> FilterChip(level == i, { level = i }, { Text(l) }) }
                        }
                    }
                    Button(onClick = { s.go(drillRoute("speed", t.id, level, t.generators, n, n * t.targetSeconds, false)) },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("Start timed drill") }
                    val runs = s.p.drills.filter { it.kind == "speed" && it.refId == t.id }.takeLast(5).reversed()
                    runs.forEach { r -> Text("${r.percent}% • ${r.seconds}s total • ${if (r.total > 0) r.seconds / r.total else 0}s/question", style = MaterialTheme.typography.labelSmall) }
                }
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
}

// ============================================================ Subnetting center
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubnetCenterScreen(s: AppScope) {
    var level by rememberSaveable { mutableIntStateOf(0) }
    var typed by rememberSaveable { mutableStateOf(true) }
    val chosen = remember { mutableStateListOf("network", "broadcast") }
    val sem = LocalSemantic.current
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Subnetting center", s, back = true)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                ContentWidth {
                    SectionCard("Learn step by step", Icons.Filled.School) {
                        s.lib.subnetLessons.forEachIndexed { i, l ->
                            val st = s.p.genStats[l.generator]
                            val acc = st?.let { if (it.correct + it.incorrect == 0) null else it.correct * 100 / (it.correct + it.incorrect) }
                            Row(Modifier.fillMaxWidth().clickable { s.go("subnetlesson/${l.id}") }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${i + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(28.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(l.title, style = MaterialTheme.typography.bodyLarge)
                                    Text(l.level + (acc?.let { " • practice accuracy $it%" } ?: ""), style = MaterialTheme.typography.labelSmall)
                                }
                                Icon(Icons.Filled.ChevronRight, null)
                            }
                        }
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Practice generator", Icons.Filled.Casino) {
                        Text("Unlimited randomized questions with full worked solutions.", style = MaterialTheme.typography.bodySmall)
                        Text("Difficulty", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Gen.LEVELS.forEachIndexed { i, l -> FilterChip(level == i, { level = i }, { Text(l) }) } }
                        Text("Question types", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Gen.TYPES.filterKeys { it != "ports" }.forEach { (k, v) ->
                                FilterChip(k in chosen, { if (k in chosen) chosen.remove(k) else chosen.add(k) }, { Text(v) })
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                            Switch(typed, { typed = it }); Spacer(Modifier.width(8.dp)); Text("Type answers (exam-like) instead of multiple choice")
                        }
                        Button(onClick = { s.go(drillRoute("subnet", "practice", level, chosen.toList(), 10, 0, typed)) }, enabled = chosen.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Practice 10 questions") }
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Timed subnetting challenge", Icons.Filled.Timer, container = MaterialTheme.colorScheme.primaryContainer) {
                        Text("10 mixed questions against the clock. Typed answers.", style = MaterialTheme.typography.bodySmall)
                        val limits = listOf(300, 240, 210)
                        Gen.LEVELS.forEachIndexed { i, l ->
                            val best = s.p.drills.filter { it.kind == "challenge" && it.level == i }.maxWithOrNull(compareBy<DrillResult>({ it.correct }, { -it.seconds }))
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("$l — ${limits[i] / 60}:${"%02d".format(limits[i] % 60)}", fontWeight = FontWeight.SemiBold)
                                    Text(best?.let { "Best: ${it.correct}/${it.total} in ${it.seconds}s" } ?: "No attempts yet", style = MaterialTheme.typography.labelSmall)
                                }
                                FilledTonalButton(onClick = { s.go(drillRoute("challenge", "level-$i", i, Gen.CHALLENGE_MIX[i], 10, limits[i], true)) }) { Text("Start") }
                            }
                        }
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Your accuracy by question type", Icons.Filled.Insights) {
                        val rows = Gen.TYPES.mapNotNull { (k, v) -> s.p.genStats[k]?.let { st -> Triple(v, st.correct, st.correct + st.incorrect) } }.filter { it.third > 0 }
                        if (rows.isEmpty()) EmptyHint("Practice to see your strengths and weaknesses here.")
                        rows.sortedBy { it.second.toFloat() / it.third }.forEach { (name, c, n) ->
                            LabeledBar(name, c.toFloat() / n, "$c/$n", if (c * 100 / n >= 80) sem.success else sem.danger)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubnetLessonScreen(s: AppScope, id: String) {
    val l = s.lib.subnetLessons.firstOrNull { it.id == id } ?: return
    StudyTimer { s.vm.addStudyTime(it) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar(l.title, s, back = true)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth {
                Tag(l.level, MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(10.dp))
                BlockList(l.blocks, s.vm.diagrams)
                Spacer(Modifier.height(12.dp))
                Text("Progressive exercises", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Gen.LEVELS.forEachIndexed { i, lv ->
                    OutlinedButton(onClick = { s.go(drillRoute("subnet", l.id, i, listOf(l.generator), 5, 0, Gen.make(l.generator, i).typable)) },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) { Text("5 × $lv: ${Gen.TYPES[l.generator] ?: l.generator}") }
                }
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
}
