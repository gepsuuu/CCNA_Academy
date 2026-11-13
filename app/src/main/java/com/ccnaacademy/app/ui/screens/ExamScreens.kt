package com.ccnaacademy.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic
import kotlinx.coroutines.delay

@Composable
fun ExamDisclaimer() {
    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp)) {
            Icon(Icons.Filled.Info, null); Spacer(Modifier.width(10.dp))
            Text("This is an independent CCNA-style practice simulator with original questions. It does not reproduce Cisco's exam interface, " +
                "scoring algorithm or question pool, and its scores are estimates of preparation — not predictions of your official result.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

// =========================================================== Setup
@Composable
fun ExamSetupScreen(s: AppScope) {
    val total = s.bank.questions.size
    val lengths = (listOf(20, 40, 60, 80, 100, 120).filter { it < total } + total).distinct()
    var count by rememberSaveable { mutableIntStateOf(lengths.firstOrNull { it >= 60 } ?: lengths.last()) }
    var minutes by rememberSaveable { mutableIntStateOf(-1) }      // -1 = auto (≈1.2 min per question)
    var level by rememberSaveable { mutableIntStateOf(1) }
    val effMinutes = if (minutes == -1) ((count * 72 + 59) / 60) else minutes
    val pool = s.bank.questions.count { it.difficulty >= level }
    val preview = remember(count, level, s.bank) { BankEngine.examSet(s.bank, s.c.domains, count.coerceAtMost(pool), level) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Exam simulator", s, back = true)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth {
                ExamDisclaimer()
                Spacer(Modifier.height(12.dp))
                SectionCard("Exam length", Icons.Filled.FormatListNumbered) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        lengths.forEach { n -> FilterChip(count == n, { count = n }, { Text(if (n == total) "All $n" else "$n") }) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("Time limit", Icons.Filled.Timer) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(minutes == -1, { minutes = -1 }, { Text("Auto (${(count * 72 + 59) / 60} min)") })
                        listOf(30, 45, 60, 90, 120, 150).forEach { m -> FilterChip(minutes == m, { minutes = m }, { Text("$m min") }) }
                    }
                    Text("Auto allows about 72 seconds per question. Cisco lists the 200-301 exam as 120 minutes; check Cisco's site for current details.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("Question difficulty", Icons.Filled.Stairs) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(level == 1, { level = 1 }, { Text("All levels") })
                        FilterChip(level == 2, { level = 2 }, { Text("Intermediate + CCNA level") })
                        FilterChip(level == 3, { level = 3 }, { Text("CCNA level only") })
                    }
                    if (pool < count) Text("Only $pool questions available at this level; the exam will use $pool.", color = LocalSemantic.current.warning,
                        style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("Domain distribution (blueprint-weighted)", Icons.Filled.PieChart) {
                    s.c.domains.forEach { d ->
                        val n = preview.count { it.domainId == d.id }
                        LabeledBar("${d.number} ${d.title} (${d.weight}%)", if (preview.isEmpty()) 0f else n.toFloat() / preview.size, "$n")
                    }
                    Text("Questions are randomly selected within each domain every time.", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { s.nav.navigate("exam?count=${count.coerceAtMost(pool)}&minutes=$effMinutes&level=$level") { popUpTo("examsetup") { inclusive = true } } },
                    enabled = pool > 0, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                    Text("Start exam • ${count.coerceAtMost(pool)} questions • $effMinutes min")
                }
                Text("Answers and explanations are shown only after you submit the exam.", style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(8.dp))
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

// =========================================================== Runner
@Composable
fun ExamRunScreen(s: AppScope, count: Int, minutes: Int, level: Int) {
    val items = remember { BankEngine.examSet(s.bank, s.c.domains, count, level).map { BankEngine.prepare(it) } }
    val answers = remember { mutableStateMapOf<Int, List<Int>>() }
    val flagged = remember { mutableStateMapOf<Int, Boolean>() }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var reviewing by rememberSaveable { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var result by remember { mutableStateOf<SessionResult?>(null) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    val limitMs = minutes * 60_000L
    val sem = LocalSemantic.current
    StudyTimer { s.vm.addStudyTime(it) }

    fun submit() {
        if (submitted) return
        submitted = true
        val secs = (elapsedMs / 1000).toInt().coerceAtMost(minutes * 60)
        items.forEachIndexed { i, it -> if (BankEngine.isAnswered(it.q, answers[i])) s.vm.recordBank(it.q, BankEngine.isCorrect(it.q, answers[i]), secs * 1000L / items.size) }
        val r = BankEngine.summarize("exam", "Exam simulation (${items.size} q)", true, items, answers, secs, minutes * 60, countUnanswered = true)
        result = r; s.vm.saveSession(r)
    }
    LaunchedEffect(submitted) {
        val start = System.currentTimeMillis() - elapsedMs
        while (!submitted) {
            delay(250); elapsedMs = System.currentTimeMillis() - start
            if (elapsedMs >= limitMs) submit()
        }
    }
    BackHandler(enabled = !submitted) { if (reviewing) reviewing = false else confirmLeave = true }
    val remaining = ((limitMs - elapsedMs) / 1000).toInt().coerceAtLeast(0)
    val unanswered = items.indices.count { !BankEngine.isAnswered(items[it].q, answers[it]) }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(if (submitted) "Exam results" else if (reviewing) "Review before submitting" else "Exam simulation", s, back = submitted) {
            if (!submitted) {
                Text("%d:%02d".format(remaining / 60, remaining % 60), fontWeight = FontWeight.Bold,
                    color = if (remaining < 300) sem.danger else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(end = 4.dp))
                IconButton(onClick = { reviewing = !reviewing }) { Icon(if (reviewing) Icons.Filled.Edit else Icons.Filled.GridView, "Review / navigate") }
            }
        }
        val r = result
        if (submitted && r != null) { SessionResultsView(s, r, items, answers, flagged.filterValues { it }.keys, allowReviewFilterFlagged = true); return@Column }
        if (items.isEmpty()) { EmptyHint("No questions available."); return@Column }
        if (remaining < 300 && remaining > 0 && !reviewing) Surface(color = sem.danger.copy(alpha = 0.12f), modifier = Modifier.fillMaxWidth()) {
            Text("Less than 5 minutes remaining — the exam submits automatically at 0:00.", Modifier.padding(8.dp), style = MaterialTheme.typography.labelMedium, color = sem.danger)
        }
        if (reviewing) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    ContentWidth {
                        Text("${items.size - unanswered} answered • $unanswered unanswered • ${flagged.count { it.value }} flagged",
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Tap a question to return to it. Answers are not checked until you submit.", style = MaterialTheme.typography.labelSmall)
                        Button(onClick = { confirm = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Text("Submit exam") }
                    }
                }
                itemsIndexed(items) { i, it ->
                    ContentWidth {
                        val ans = BankEngine.isAnswered(it.q, answers[i])
                        Surface(Modifier.fillMaxWidth().clickable { index = i; reviewing = false }, shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (ans) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${i + 1}", fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                                Text(it.q.stem, maxLines = 1, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                if (flagged[i] == true) Icon(Icons.Filled.Flag, "Flagged", tint = sem.warning, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Tag(if (ans) "Answered" else "Unanswered", if (ans) MaterialTheme.colorScheme.primary else sem.danger)
                            }
                        }
                    }
                }
            }
        } else {
            val item = items[index]
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                ContentWidth {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Question ${index + 1} of ${items.size}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        FilterChip(flagged[index] == true, { flagged[index] = !(flagged[index] ?: false) }, { Text("Flag for review") },
                            leadingIcon = { Icon(Icons.Filled.Flag, null, Modifier.size(16.dp)) })
                    }
                    LinearProgressIndicator(progress = { (items.size - unanswered).toFloat() / items.size }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                    QuestionCard(item, index + 1, answers[index], reveal = false, diagrams = s.vm.diagrams, domainTitle = "", topicTitle = "",
                        onAnswer = { answers[index] = it })
                    if (item.q.type == "order" && answers[index] == null)
                        TextButton(onClick = { answers[index] = item.initOrder }) { Text("Keep this order as my answer") }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { index-- }, enabled = index > 0, modifier = Modifier.weight(1f)) { Text("Previous") }
                        if (index < items.size - 1) Button(onClick = { index++ }, modifier = Modifier.weight(1f)) { Text("Next") }
                        else Button(onClick = { reviewing = true }, modifier = Modifier.weight(1f)) { Text("Review answers") }
                    }
                    Spacer(Modifier.navigationBarsPadding().height(24.dp))
                }
            }
        }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false }, title = { Text("Submit exam?") },
        text = { Text("$unanswered unanswered and ${flagged.count { it.value }} flagged question(s). Unanswered questions are scored as incorrect. You cannot change answers after submitting.") },
        confirmButton = { TextButton(onClick = { confirm = false; submit() }) { Text("Submit") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Keep working") } })
    if (confirmLeave) AlertDialog(
        onDismissRequest = { confirmLeave = false }, title = { Text("Leave the exam?") },
        text = { Text("Leaving discards this attempt. To keep a score, submit the exam instead.") },
        confirmButton = { TextButton(onClick = { confirmLeave = false; s.nav.popBackStack() }) { Text("Discard and leave") } },
        dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text("Stay") } })
}
