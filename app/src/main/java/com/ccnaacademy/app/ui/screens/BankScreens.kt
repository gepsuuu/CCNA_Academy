package com.ccnaacademy.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

fun bankRoute(mode: String, count: Int, minutes: Int = 0, f: BankFilter = BankFilter()) =
    "bank?mode=$mode&count=$count&minutes=$minutes&domain=${f.domain}&topic=${f.topic}&diff=${f.difficulty}&format=${f.format}&pack=${f.pack}"

private data class ModeDef(val id: String, val title: String, val sub: String, val icon: ImageVector, val count: Int)

// =========================================================== Practice home
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BankHomeScreen(s: AppScope) {
    var domain by rememberSaveable { mutableStateOf("all") }
    var diff by rememberSaveable { mutableIntStateOf(0) }
    var format by rememberSaveable { mutableStateOf("all") }
    var pack by rememberSaveable { mutableStateOf("all") }
    var timedCount by rememberSaveable { mutableIntStateOf(25) }
    var timedMin by rememberSaveable { mutableIntStateOf(30) }
    val f = BankFilter(domain, "all", diff, format, pack)
    val pool = s.bank.questions.count { BankEngine.matches(it, f, s.bank) }
    val incorrect = s.bank.questions.count { s.p.bankStats[it.id]?.let { st -> !st.lastCorrect && st.incorrect > 0 } == true }
    val weak = BankEngine.weakTopics(s.p)
    val full: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }
    val sem = LocalSemantic.current
    val modes = listOf(
        ModeDef("quick", "Quick practice", "10 random questions", Icons.Filled.Bolt, 10),
        ModeDef("standard", "Standard practice", "25 questions", Icons.Filled.FormatListNumbered, 25),
        ModeDef("intensive", "Intensive practice", "50 questions", Icons.Filled.FitnessCenter, 50),
        ModeDef("full", "Full question bank", "All $pool matching questions", Icons.Filled.LibraryBooks, 0),
        ModeDef("weak", "Weak topics", if (weak.isEmpty()) "Builds from unattempted & low-accuracy areas" else "${weak.size} weak topic(s) detected", Icons.Filled.TrendingDown, 20),
        ModeDef("incorrect", "Review incorrect", "$incorrect question(s) answered wrong last time", Icons.Filled.Replay, 0),
        ModeDef("bookmarks", "Bookmarked", "${s.p.bankBookmarks.size} saved question(s)", Icons.Filled.Bookmark, 0),
    )
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Practice", s) {
            IconButton(onClick = { s.go("bankstats") }) { Icon(Icons.Filled.Insights, "Performance analytics") }
            IconButton(onClick = { s.go("packs") }) { Icon(Icons.Filled.Inventory2, "Question packs") }
        }
        LazyVerticalGrid(GridCells.Adaptive(170.dp), contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item(span = full) {
                Card(Modifier.fillMaxWidth().clickable { s.go("examsetup") }, shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AssignmentTurnedIn, null, Modifier.size(40.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Exam simulator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Configurable length and time • blueprint-weighted • review before submitting", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Filled.ChevronRight, null)
                    }
                }
            }
            item(span = full) {
                Card(Modifier.fillMaxWidth().clickable { s.go("labs") }, shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Terminal, null, Modifier.size(40.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("CLI labs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            val done = s.p.labs.values.count { it.completedAt != null }
                            Text("Stateful offline trainer • guided, troubleshooting and challenge labs • $done/${com.ccnaacademy.app.lab.LabCatalog.all.size} completed",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Filled.ChevronRight, null)
                    }
                }
            }
            item(span = full) {
                SectionCard("Question bank filters", Icons.Filled.FilterAlt, trailing = { Text("$pool of ${s.bank.questions.size}", style = MaterialTheme.typography.labelLarge) }) {
                    Text("Apply to quick, standard, intensive, full and timed practice.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ChipRow("Domain") {
                        FilterChip(domain == "all", { domain = "all" }, { Text("All") })
                        s.c.domains.forEach { d -> FilterChip(domain == d.id, { domain = d.id }, { Text("${d.number} ${d.title}") }) }
                    }
                    ChipRow("Difficulty") {
                        FilterChip(diff == 0, { diff = 0 }, { Text("All") })
                        BankEngine.DIFFICULTY.forEach { (k, v) -> FilterChip(diff == k, { diff = k }, { Text(v) }) }
                    }
                    ChipRow("Question category") {
                        FilterChip(format == "all", { format = "all" }, { Text("All") })
                        BankEngine.FORMATS.forEach { (k, v) -> FilterChip(format == k, { format = k }, { Text(v) }) }
                    }
                    if (s.bank.enabledPacks.size > 1) ChipRow("Pack") {
                        FilterChip(pack == "all", { pack = "all" }, { Text("All enabled") })
                        s.bank.enabledPacks.forEach { lp -> FilterChip(pack == lp.pack.id, { pack = lp.pack.id }, { Text(lp.pack.name) }) }
                    }
                }
            }
            modes.forEach { m ->
                item {
                    val empty = (m.id == "incorrect" && incorrect == 0) || (m.id == "bookmarks" && s.p.bankBookmarks.isEmpty()) || pool == 0
                    ModeTile(m.title, m.sub, m.icon, !empty) {
                        val filter = if (m.id in setOf("incorrect", "bookmarks", "weak")) BankFilter() else f
                        s.go(bankRoute(m.id, m.count, 0, filter))
                    }
                }
            }
            item(span = full) {
                SectionCard("Timed practice", Icons.Filled.Timer, accent = sem.warning) {
                    Text("Instant feedback after each answer, with a countdown for the whole set.", style = MaterialTheme.typography.bodySmall)
                    ChipRow("Questions") { listOf(10, 25, 50).forEach { n -> FilterChip(timedCount == n, { timedCount = n }, { Text("$n") }) } }
                    ChipRow("Time limit") { listOf(10, 20, 30, 45, 60).forEach { n -> FilterChip(timedMin == n, { timedMin = n }, { Text("$n min") }) } }
                    Button(onClick = { s.go(bankRoute("timed", timedCount, timedMin, f)) }, enabled = pool > 0, modifier = Modifier.padding(top = 6.dp)) {
                        Text("Start timed practice")
                    }
                }
            }
            item(span = full) {
                SectionCard("Topic packs", Icons.Filled.Category) {
                    Text("Practice one topic at a time. Accuracy shown where you have answers.", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val perf = BankEngine.topicPerf(s.p).associateBy { it.topic }
                    s.c.domains.forEach { d ->
                        val topics = s.bank.topics.values.filter { it.domainId == d.id && s.bank.questions.any { q -> q.topic == it.id } }
                        if (topics.isNotEmpty()) {
                            Text("${d.number} ${d.title}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                topics.forEach { t ->
                                    val n = s.bank.questions.count { it.topic == t.id }
                                    val acc = perf[t.id]?.let { " • ${(it.acc * 100).roundToInt()}%" } ?: ""
                                    SuggestionChip({ s.go(bankRoute("topic", 0, 0, BankFilter(topic = t.id))) }, { Text("${t.title} ($n)$acc") })
                                }
                            }
                        }
                    }
                }
            }
            item(span = full) {
                SectionCard("Difficulty packs", Icons.Filled.Stairs) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BankEngine.DIFFICULTY.forEach { (k, v) ->
                            val n = s.bank.questions.count { it.difficulty == k }
                            OutlinedButton(onClick = { s.go(bankRoute("difficulty", 0, 0, BankFilter(difficulty = k))) }, modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(6.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(v, maxLines = 1); Text("$n questions", style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(label: String, content: @Composable RowScope.() -> Unit) {
    Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
private fun ModeTile(title: String, sub: String, icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().heightIn(min = 118.dp).clickable(enabled = enabled, onClick = onClick), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun bankModeTitle(mode: String, s: AppScope, f: BankFilter) = when (mode) {
    "quick" -> "Quick practice"; "standard" -> "Standard practice"; "intensive" -> "Intensive practice"; "full" -> "Full question bank"
    "weak" -> "Weak topics"; "incorrect" -> "Review incorrect"; "bookmarks" -> "Bookmarked questions"; "timed" -> "Timed practice"
    "daily" -> "Daily quiz"; "topic" -> "Topic: ${s.bank.topicTitle(f.topic)}"; "difficulty" -> "Difficulty: ${BankEngine.DIFFICULTY[f.difficulty]}"
    else -> "Practice"
}

// =========================================================== Practice session (feedback after each submitted answer)
@Composable
fun BankSessionScreen(s: AppScope, mode: String, count: Int, minutes: Int, f: BankFilter) {
    val items = remember { BankEngine.select(s.bank, s.p, mode, count, f).map { BankEngine.prepare(it) } }
    val answers = remember { mutableStateMapOf<Int, List<Int>>() }
    val submitted = remember { mutableStateMapOf<Int, Boolean>() }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var qStart by remember { mutableLongStateOf(0L) }
    var finished by rememberSaveable { mutableStateOf(false) }
    var result by remember { mutableStateOf<SessionResult?>(null) }
    var confirmExit by remember { mutableStateOf(false) }
    val limitMs = minutes * 60_000L
    val title = bankModeTitle(mode, s, f)
    StudyTimer { s.vm.addStudyTime(it) }

    fun finish() {
        if (finished) return
        finished = true
        val r = BankEngine.summarize(mode, title, false, items, answers.filterKeys { submitted[it] == true }, (elapsedMs / 1000).toInt(),
            (limitMs / 1000).toInt(), countUnanswered = minutes > 0)
        result = r
        if (r.total > 0) s.vm.saveSession(r)
    }
    LaunchedEffect(finished) {
        val start = System.currentTimeMillis() - elapsedMs
        while (!finished) {
            delay(250); elapsedMs = System.currentTimeMillis() - start
            if (limitMs > 0 && elapsedMs >= limitMs) finish()
        }
    }
    BackHandler(enabled = !finished && submitted.isNotEmpty()) { confirmExit = true }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(title, s, back = true) {
            if (!finished) {
                val sec = if (limitMs > 0) ((limitMs - elapsedMs) / 1000).toInt().coerceAtLeast(0) else (elapsedMs / 1000).toInt()
                Text("%d:%02d".format(sec / 60, sec % 60), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp),
                    color = if (limitMs > 0 && sec < 60) LocalSemantic.current.danger else MaterialTheme.colorScheme.onSurface)
                if (items.isNotEmpty() && index < items.size) {
                    val id = items[index].q.id
                    IconButton(onClick = { s.vm.toggleBankBookmark(id) }) {
                        Icon(if (id in s.p.bankBookmarks) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, "Bookmark question")
                    }
                }
                if (submitted.isNotEmpty()) TextButton(onClick = { confirmExit = true }) { Text("End") }
            }
        }
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(when (mode) {
                    "incorrect" -> "No incorrect answers to review — nice work."
                    "bookmarks" -> "No bookmarked questions yet. Use the bookmark icon during practice."
                    else -> "No questions match these filters."
                }, textAlign = TextAlign.Center)
            }
            return@Column
        }
        val r = result
        if (finished && r != null) { SessionResultsView(s, r, items, answers.filterKeys { submitted[it] == true }, emptySet(), allowReviewFilterFlagged = false); return@Column }
        val item = items[index]
        val done = submitted[index] == true
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth {
                LinearProgressIndicator(progress = { (index + 1f) / items.size }, modifier = Modifier.fillMaxWidth())
                Text("${index + 1} of ${items.size} • ${submitted.values.count { it }} answered", style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(vertical = 8.dp))
                QuestionCard(item, index + 1, answers[index], reveal = done, diagrams = s.vm.diagrams,
                    domainTitle = s.c.domains.firstOrNull { it.id == item.q.domainId }?.title ?: "", topicTitle = s.bank.topicTitle(item.q.topic),
                    onAnswer = { answers[index] = it }, onOpenLesson = { s.lesson(item.q.lessonId) })
                Spacer(Modifier.height(14.dp))
                if (!done) {
                    val ready = BankEngine.isComplete(item.q, answers[index] ?: if (item.q.type == "order") item.initOrder else null)
                    Button(onClick = {
                        val a = answers[index] ?: item.initOrder.takeIf { item.q.type == "order" }
                        if (a != null) answers[index] = a
                        submitted[index] = true
                        s.vm.recordBank(item.q, BankEngine.isCorrect(item.q, a), elapsedMs - qStart)
                    }, enabled = ready, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Submit answer") }
                    if (item.q.type == "multi" && (answers[index]?.size ?: 0) != item.q.correct.size)
                        Text("Select exactly ${item.q.correct.size} options to submit.", style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { if (index < items.size - 1) { index++; qStart = elapsedMs } else finish() }, modifier = Modifier.align(Alignment.End)) { Text("Skip") }
                } else {
                    Button(onClick = { if (index < items.size - 1) { index++; qStart = elapsedMs } else finish() }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(if (index < items.size - 1) "Next question" else "See results")
                    }
                }
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
    if (confirmExit) AlertDialog(
        onDismissRequest = { confirmExit = false }, title = { Text("End this session?") },
        text = { Text("Your ${submitted.size} submitted answers are saved and scored.") },
        confirmButton = { TextButton(onClick = { confirmExit = false; finish() }) { Text("End and see results") } },
        dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("Keep going") } })
}

// =========================================================== Results & analytics (shared by practice and exam)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionResultsView(s: AppScope, r: SessionResult, items: List<SessionItem>, answers: Map<Int, List<Int>>, flagged: Set<Int>, allowReviewFilterFlagged: Boolean) {
    val sem = LocalSemantic.current
    var filter by rememberSaveable { mutableStateOf("incorrect") }
    var expanded by remember { mutableStateOf<Int?>(null) }
    val counted = items.indices.filter { r.exam || r.limitSec > 0 || BankEngine.isAnswered(items[it].q, answers[it]) }
    val topicRows = r.topicTotal.map { (t, n) -> TopicPerf(t, r.topicCorrect[t] ?: 0, n) }
    val weakest = topicRows.filter { it.correct < it.total }.sortedWith(compareBy({ it.acc }, { -it.total })).take(4)
    val strongest = topicRows.filter { it.correct > 0 }.sortedWith(compareBy({ -it.acc }, { -it.total })).take(4)
    val lessons = weakest.mapNotNull { s.bank.lessonFor(it.topic).takeIf { id -> id.isNotBlank() } }.distinct().mapNotNull { s.c.lessonMap[it] }
    val history = s.p.sessions.filter { it.exam == r.exam }.takeLast(15).map { it.percent.toFloat() }.let { h -> if (s.p.sessions.any { it.id == r.id }) h else h + r.percent.toFloat() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        ContentWidth {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                ProgressRing(r.percent / 100f, "${r.percent}%", "accuracy", size = 160.dp, color = if (r.percent >= 80) sem.success else if (r.percent >= 60) sem.warning else sem.danger)
                if (r.exam) Text("This score estimates your current preparation. It is not a prediction of your official Cisco result — Cisco's scoring method and question pool are not public.",
                    style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), maxItemsInEachRow = 4) {
                Metric("Total", "${r.total}"); Metric("Correct", "${r.correct}", sem.success); Metric("Incorrect", "${r.incorrect}", sem.danger)
                Metric("Unanswered", "${r.total - r.answered}"); Metric("Accuracy", "${r.percent}%")
                Metric("Time spent", formatDuration(r.seconds.toLong())); Metric("Avg / question", "${r.avgSeconds}s")
                if (r.limitSec > 0) Metric("Time limit", formatDuration(r.limitSec.toLong()))
            }
            Spacer(Modifier.height(12.dp))
            SectionCard("Performance by exam domain", Icons.Filled.BarChart) {
                s.c.domains.forEach { d ->
                    val n = r.domainTotal[d.id] ?: 0
                    if (n > 0) {
                        val c = r.domainCorrect[d.id] ?: 0
                        LabeledBar("${d.number} ${d.title}", c.toFloat() / n, "$c/$n", if (c * 100 / n >= 80) sem.success else if (c * 100 / n >= 60) sem.warning else sem.danger)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionCard("Weakest topics", Icons.Filled.TrendingDown, accent = sem.danger, modifier = Modifier.weight(1f)) {
                    if (weakest.isEmpty()) EmptyHint("None — every topic fully correct.")
                    weakest.forEach { Text("${s.bank.topicTitle(it.topic)} — ${it.correct}/${it.total}", style = MaterialTheme.typography.bodySmall) }
                }
                SectionCard("Strongest topics", Icons.Filled.TrendingUp, accent = sem.success, modifier = Modifier.weight(1f)) {
                    if (strongest.isEmpty()) EmptyHint("No correct answers yet.")
                    strongest.forEach { Text("${s.bank.topicTitle(it.topic)} — ${it.correct}/${it.total}", style = MaterialTheme.typography.bodySmall) }
                }
            }
            Spacer(Modifier.height(12.dp))
            SectionCard("Recommended revision lessons", Icons.Filled.MenuBook) {
                if (lessons.isEmpty()) EmptyHint("No revision needed from this session.")
                lessons.forEach { l -> ListRow(l.title, "${s.c.domain(l.domainId).number} ${s.c.domain(l.domainId).title} • ${l.minutes} min") { s.lesson(l.id) } }
            }
            Spacer(Modifier.height(12.dp))
            SectionCard(if (r.exam) "Exam score trend" else "Practice accuracy trend", Icons.Filled.ShowChart) {
                if (history.size < 2) EmptyHint("Complete more sessions to see your trend. The dashed line marks 82.5%.")
                LineChart(history)
            }
            Spacer(Modifier.height(12.dp))
            SectionCard("Answer review & explanations", Icons.Filled.FactCheck) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(filter == "incorrect", { filter = "incorrect" }, { Text("Incorrect / unanswered") })
                    FilterChip(filter == "all", { filter = "all" }, { Text("All") })
                    if (allowReviewFilterFlagged) FilterChip(filter == "flagged", { filter = "flagged" }, { Text("Flagged (${flagged.size})") })
                }
                val shown = counted.filter { i ->
                    when (filter) { "incorrect" -> !BankEngine.isCorrect(items[i].q, answers[i]); "flagged" -> i in flagged; else -> true }
                }
                if (shown.isEmpty()) EmptyHint("Nothing to show for this filter.")
                shown.forEach { i ->
                    val it = items[i]; val ok = BankEngine.isCorrect(it.q, answers[i])
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    Row(Modifier.fillMaxWidth().clickable { expanded = if (expanded == i) null else i }, verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (ok) Icons.Filled.CheckCircle else Icons.Filled.Cancel, null, tint = if (ok) sem.success else sem.danger)
                        Spacer(Modifier.width(8.dp))
                        Text("${i + 1}. ${it.q.stem}", style = MaterialTheme.typography.bodyMedium, maxLines = if (expanded == i) 1 else 2, modifier = Modifier.weight(1f))
                        if (i in flagged) Icon(Icons.Filled.Flag, "Flagged", tint = sem.warning, modifier = Modifier.size(18.dp))
                        IconButton(onClick = { s.vm.toggleBankBookmark(it.q.id) }) {
                            Icon(if (it.q.id in s.p.bankBookmarks) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, "Bookmark", modifier = Modifier.size(20.dp))
                        }
                    }
                    if (expanded == i) QuestionCard(it, i + 1, answers[i], reveal = true, diagrams = s.vm.diagrams,
                        domainTitle = s.c.domains.firstOrNull { d -> d.id == it.q.domainId }?.title ?: "", topicTitle = s.bank.topicTitle(it.q.topic),
                        onAnswer = {}, onOpenLesson = { s.lesson(it.q.lessonId) })
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { s.nav.popBackStack() }, modifier = Modifier.fillMaxWidth()) { Text("Done") }
            Spacer(Modifier.navigationBarsPadding().height(24.dp))
        }
    }
}

@Composable
private fun Metric(label: String, value: String, color: androidx.compose.ui.graphics.Color? = null) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.width(140.dp).padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color ?: MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
