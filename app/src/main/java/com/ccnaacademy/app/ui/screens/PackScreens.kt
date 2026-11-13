package com.ccnaacademy.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

// =========================================================== Pack manager
@Composable
fun PackManagerScreen(s: AppScope) {
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var urlDialog by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf<QuestionPack?>(null) }
    val sem = LocalSemantic.current
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { busy = true; s.vm.importPack(uri) { busy = false; message = it } }
    }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Question packs", s, back = true)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                ContentWidth {
                    SectionCard("Add question packs", Icons.Filled.AddCircle, container = MaterialTheme.colorScheme.primaryContainer) {
                        Text("Packs are JSON files (schema 1). Imported packs are validated, stored on this device and work offline.",
                            style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            Button(onClick = { pick.launch(arrayOf("application/json", "text/plain", "*/*")) }, enabled = !busy) {
                                Icon(Icons.Filled.FileOpen, null); Spacer(Modifier.width(6.dp)); Text("Import file")
                            }
                            OutlinedButton(onClick = { urlDialog = true }, enabled = !busy) {
                                Icon(Icons.Filled.CloudDownload, null); Spacer(Modifier.width(6.dp)); Text("Download")
                            }
                        }
                        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
                        message?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp),
                            color = if (it.startsWith("Import failed")) sem.danger else sem.success) }
                    }
                }
            }
            item {
                ContentWidth {
                    Text("${s.bank.questions.size} questions enabled from ${s.bank.enabledPacks.size} of ${s.bank.packs.size} packs",
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
            items(s.bank.packs, key = { it.pack.id }) { lp ->
                ContentWidth {
                    val p = lp.pack
                    val enabled = p.id !in s.p.disabledPacks
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(p.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text("${p.questions.size} questions • v${p.version}" + (if (p.category.isNotBlank()) " • ${p.category}" else ""),
                                        style = MaterialTheme.typography.labelSmall)
                                }
                                Switch(enabled, { s.vm.setPackEnabled(p.id, it) })
                            }
                            if (p.description.isNotBlank()) Text(p.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Tag(if (lp.builtin) "Built-in" else "Imported", if (lp.builtin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
                                Spacer(Modifier.width(6.dp))
                                Tag("Offline", sem.success)
                                Spacer(Modifier.weight(1f))
                                if (!lp.builtin) TextButton(onClick = { delete = p }) { Text("Remove") }
                            }
                            if (lp.issues.isNotEmpty()) Text("${lp.issues.size} invalid question(s) skipped: ${lp.issues.take(2).joinToString("; ")}",
                                style = MaterialTheme.typography.labelSmall, color = sem.warning)
                        }
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Pack format", Icons.Filled.Code) {
                        Text("{ \"schema\": 1, \"id\": \"my-pack\", \"name\": \"…\", \"version\": 1, \"category\": \"…\", \"topics\": [ … ], \"questions\": [ … ] }",
                            style = MaterialTheme.typography.bodySmall)
                        Text("Question types: single, multi, tf, match, order. Domains: d1–d6. Difficulty: 1–3. New topic ids are added automatically. " +
                            "See docs/question-pack-format.md in the project for the full specification.", style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
    if (urlDialog) {
        var url by remember { mutableStateOf("https://") }
        AlertDialog(onDismissRequest = { urlDialog = false }, title = { Text("Download a question pack") },
            text = {
                Column {
                    OutlinedTextField(url, { url = it }, label = { Text("HTTPS link to a .json pack") }, singleLine = true)
                    Text("Only download packs from sources you trust. The pack is validated before it is installed.", style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 6.dp))
                }
            },
            confirmButton = { TextButton(onClick = { urlDialog = false; busy = true; s.vm.downloadPack(url) { busy = false; message = it } }, enabled = url.length > 10) { Text("Download") } },
            dismissButton = { TextButton(onClick = { urlDialog = false }) { Text("Cancel") } })
    }
    delete?.let { p ->
        AlertDialog(onDismissRequest = { delete = null }, title = { Text("Remove '${p.name}'?") },
            text = { Text("The pack's questions are removed from practice. Your answer history is kept.") },
            confirmButton = { TextButton(onClick = { s.vm.deletePack(p.id); delete = null }) { Text("Remove") } },
            dismissButton = { TextButton(onClick = { delete = null }) { Text("Cancel") } })
    }
}

// =========================================================== Performance analytics
@Composable
fun BankAnalyticsScreen(s: AppScope) {
    val sem = LocalSemantic.current
    val stats = s.p.bankStats
    val attempts = stats.values.sumOf { it.attempts }
    val correct = stats.values.sumOf { it.correct }
    val avgSec = if (attempts == 0) 0 else (stats.values.sumOf { it.totalMs } / attempts / 1000).toInt()
    val seen = s.bank.questions.count { it.id in stats }
    val topics = BankEngine.topicPerf(s.p).sortedBy { it.acc }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Practice analytics", s, back = true)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                ContentWidth {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile("Questions seen", "$seen/${s.bank.questions.size}", "unique questions", Icons.Filled.Visibility, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                        StatTile("Accuracy", if (attempts == 0) "—" else "${correct * 100 / attempts}%", "$correct of $attempts answers", Icons.Filled.TrackChanges, sem.success, Modifier.weight(1f))
                        StatTile("Avg time", "${avgSec}s", "per answer", Icons.Filled.Timer, sem.warning, Modifier.weight(1f))
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Practice accuracy trend", Icons.Filled.ShowChart) {
                        val pr = s.p.sessions.filter { !it.exam }.takeLast(20)
                        if (pr.size < 2) EmptyHint("Complete at least two practice sessions to see a trend.")
                        LineChart(pr.map { it.percent.toFloat() })
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Exam simulation trend", Icons.Filled.AssignmentTurnedIn) {
                        val ex = s.p.sessions.filter { it.exam }.takeLast(20)
                        if (ex.isEmpty()) EmptyHint("No exam simulations yet.")
                        LineChart(ex.map { it.percent.toFloat() })
                        Text("Dashed line ≈ 82.5%, a common practice target. Cisco does not publish a fixed passing score; practice scores are estimates only.",
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Accuracy by exam domain", Icons.Filled.BarChart) {
                        s.c.domains.forEach { d ->
                            val l = stats.values.filter { it.domainId == d.id }
                            val n = l.sumOf { it.attempts }; val c = l.sumOf { it.correct }
                            LabeledBar("${d.number} ${d.title}", if (n == 0) 0f else c.toFloat() / n, if (n == 0) "—" else "${c * 100 / n}% ($n)",
                                if (n > 0 && c * 100 / n >= 80) sem.success else if (n > 0 && c * 100 / n >= 60) sem.warning else sem.danger)
                        }
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Topics (weakest first)", Icons.Filled.Category) {
                        if (topics.isEmpty()) EmptyHint("Answer some questions to see topic performance.")
                        topics.forEach { t ->
                            val lesson = s.c.lessonMap[s.bank.lessonFor(t.topic)]
                            ListRow("${s.bank.topicTitle(t.topic)} — ${(t.acc * 100).roundToInt()}%", "${t.correct}/${t.total} correct" + (lesson?.let { " • revise: ${it.title}" } ?: "")) {
                                if (lesson != null) s.lesson(lesson.id) else s.go(bankRoute("topic", 0, 0, BankFilter(topic = t.topic)))
                            }
                        }
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Session history", Icons.Filled.History) {
                        if (s.p.sessions.isEmpty()) EmptyHint("No sessions yet.")
                        s.p.sessions.takeLast(25).reversed().forEach { r ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text(r.title, style = MaterialTheme.typography.bodyMedium)
                                    Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(r.id)) + " • ${formatDuration(r.seconds.toLong())}",
                                        style = MaterialTheme.typography.labelSmall)
                                }
                                Text("${r.percent}% (${r.correct}/${r.total})", fontWeight = FontWeight.SemiBold,
                                    color = if (r.percent >= 80) sem.success else if (r.percent >= 60) sem.warning else sem.danger)
                            }
                        }
                    }
                }
            }
        }
    }
}
