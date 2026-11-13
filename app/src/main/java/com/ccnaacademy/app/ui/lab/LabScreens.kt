package com.ccnaacademy.app.ui.lab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.LabMistake
import com.ccnaacademy.app.data.LabSave
import com.ccnaacademy.app.data.TranscriptLine
import com.ccnaacademy.app.lab.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic

private val TERM_BG = Color(0xFF0D1117)
private val TERM_FG = Color(0xFFE6EDF3)

// ================================================================== Hub

@Composable
fun LabsHubScreen(s: AppScope) {
    val sem = LocalSemantic.current
    Column(Modifier.fillMaxSize()) {
        AppTopBar("CLI labs & animations", s, back = true)
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                ContentWidth {
                    Text("Practise configuration, verification and troubleshooting on an offline, stateful CLI trainer. It models a documented subset of IOS — it is not a full emulator.",
                        style = MaterialTheme.typography.bodyMedium)
                    Row(Modifier.padding(top = 8.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip({ s.go("labref") }, { Text("Supported commands & limits") }, leadingIcon = { Icon(Icons.Filled.Terminal, null, Modifier.size(18.dp)) })
                        AssistChip({ s.go("labnotebook") }, { Text("Mistake notebook (${s.p.labMistakes.count { !it.fixed }})") }, leadingIcon = { Icon(Icons.Filled.EditNote, null, Modifier.size(18.dp)) })
                    }
                }
            }
            listOf("guided" to "Guided practice", "troubleshoot" to "Troubleshooting", "challenge" to "Independent challenges").forEach { (kind, title) ->
                item { ContentWidth { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) } }
                items(LabCatalog.all.filter { it.kind == kind }, key = { it.id }) { lab -> ContentWidth { LabCard(s, lab) } }
            }
            item { ContentWidth { Text("Animations (concept demonstrations)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) } }
            items(AnimationCatalog.all, key = { it.id }) { a ->
                ContentWidth {
                    Card(Modifier.fillMaxWidth().clickable { s.lesson(a.lessonIds.first()) }, shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Animation, null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(a.title, fontWeight = FontWeight.SemiBold)
                                Text("${a.steps.size} steps · lesson ${a.lessonIds.joinToString()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Filled.ChevronRight, null)
                        }
                    }
                }
            }
            item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
        }
    }
}

@Composable
fun LabCard(s: AppScope, lab: Lab) {
    val sem = LocalSemantic.current
    val save = s.p.labs[lab.id]
    val (status, color) = when {
        save?.completedAt != null && lab.assessed -> "Mastered independently" to sem.success
        save?.completedAt != null -> "Completed${save.completedWithHints?.let { if (it == 0) " without hints" else " · $it hint(s)" } ?: ""}" to sem.success
        save != null -> "In progress" to sem.warning
        else -> "Not started" to MaterialTheme.colorScheme.outline
    }
    Card(Modifier.fillMaxWidth().clickable { s.go("lab/${lab.id}") }, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(when (lab.kind) { "troubleshoot" -> Icons.Filled.BugReport; "challenge" -> Icons.Filled.EmojiEvents; else -> Icons.Filled.Terminal }, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text(lab.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            }
            Text(lab.scenario, style = MaterialTheme.typography.bodySmall, maxLines = 3, modifier = Modifier.padding(top = 6.dp))
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Tag(lab.label, MaterialTheme.colorScheme.secondary); Tag("${lab.minutes} min", MaterialTheme.colorScheme.outline); Tag(status, color)
            }
        }
    }
}

// ================================================================== Lab screen

@Composable
fun LabScreen(s: AppScope, id: String) {
    val lab = LabCatalog.byId[id] ?: return
    val sem = LocalSemantic.current
    val saved0 = remember(id) { s.p.labs[id] }
    val engine = remember(id) { CliEngine(saved0?.state ?: lab.initial()) }
    var tick by remember(id) { mutableIntStateOf(0) }
    val transcripts = remember(id) { mutableStateMapOf<String, List<TranscriptLine>>().apply { saved0?.transcripts?.let { putAll(it) } } }
    val configurable = engine.state.devices.filter { it.configurable }.map { it.id }
    var device by remember(id) { mutableStateOf(saved0?.device?.takeIf { it in configurable } ?: configurable.first()) }
    val hints = remember(id) { mutableStateMapOf<String, Int>().apply { saved0?.hints?.let { putAll(it) } } }
    var checks by remember(id) { mutableIntStateOf(saved0?.checks ?: 0) }
    var checked by remember(id) { mutableStateOf<Map<String, Boolean>?>(null) }
    var tab by rememberSaveable(id) { mutableIntStateOf(0) }
    var confirmReset by remember { mutableStateOf(false) }
    val started = remember(id) { saved0?.started ?: System.currentTimeMillis() }

    val net = remember(tick) { Network(engine.state) }
    val results = remember(tick) { lab.tasks.associate { it.id to it.check(engine.state, net) } }
    val allDone = results.values.all { it }
    fun current() = LabSave(engine.state, transcripts.toMap(), device, hints.toMap(), started, s.p.labs[id]?.completedAt, s.p.labs[id]?.completedWithHints, checks)
    fun persist() { s.vm.saveLab(id, current()) }

    // Guided and troubleshooting labs complete as soon as every task passes; challenges complete on "Check my work".
    LaunchedEffect(allDone) {
        if (allDone && !lab.assessed && s.p.labs[id]?.completedAt == null) s.vm.completeLab(id, current(), lab.tasks.map { it.skill }.distinct())
    }

    fun submit(line: String) {
        val out = engine.run(device, line)
        transcripts[device] = ((transcripts[device] ?: emptyList()) + out.map { TranscriptLine(it.text, it.kind) }).takeLast(400)
        val err = out.firstOrNull { it.kind == "err" && it.text.trimStart().startsWith("%") }
        if (err != null) s.vm.recordLabMistake(LabMistake(System.currentTimeMillis(), id, device, line.trim(), err.text.trim(),
            out.filter { it.kind == "note" }.joinToString(" ") { it.text.removePrefix("[Trainer] ") }))
        tick++; persist()
    }
    fun runVerify(dev: String, cmd: String): List<String> {
        // Runs in privileged EXEC on a copy so the learner's current mode is untouched; evidence-gathering is still recorded.
        val tmp = CliEngine(engine.state.update(engine.state.dev(dev).copy(session = Session(Mode.PRIV))))
        val out = tmp.run(dev, cmd)
        engine.state = engine.state.copy(events = tmp.state.events); tick++; persist()
        return out.map { it.text }
    }

    val tabs = listOf("Tasks", "Topology", "CLI", "Verify", "Review")
    Column(Modifier.fillMaxSize()) {
        AppTopBar(lab.title, s, back = true) {
            IconButton(onClick = { confirmReset = true }) { Icon(Icons.Filled.RestartAlt, "Reset lab") }
        }
        if (s.wide) {
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(1f)) {
                    val side = listOf(0, 1, 3, 4)
                    val sel = if (tab in side) tab else 0
                    TabRow(side.indexOf(sel)) { side.forEach { t -> Tab(sel == t, { tab = t }, text = { Text(tabs[t]) }) } }
                    LabTabContent(s, lab, sel, engine, net, results, hints, checked, device, { device = it; tab = 2 }, ::runVerify, ::persist,
                        onCheck = { checks++; checked = results; persist(); if (allDone && s.p.labs[id]?.completedAt == null) s.vm.completeLab(id, current(), lab.tasks.map { it.skill }.distinct()) })
                }
                VerticalDivider()
                Column(Modifier.weight(1.1f)) { Terminal(engine, device, configurable, { device = it; persist() }, transcripts[device].orEmpty(), ::submit) }
            }
        } else {
            ScrollableTabRow(tab, edgePadding = 8.dp) { tabs.forEachIndexed { i, t -> Tab(tab == i, { tab = i }, text = { Text(t) }) } }
            if (tab == 2) Terminal(engine, device, configurable, { device = it; persist() }, transcripts[device].orEmpty(), ::submit)
            else LabTabContent(s, lab, tab, engine, net, results, hints, checked, device, { device = it; tab = 2 }, ::runVerify, ::persist,
                onCheck = { checks++; checked = results; persist(); if (allDone && s.p.labs[id]?.completedAt == null) s.vm.completeLab(id, current(), lab.tasks.map { it.skill }.distinct()) })
        }
    }
    if (confirmReset) AlertDialog(onDismissRequest = { confirmReset = false }, title = { Text("Reset this lab?") },
        text = { Text("All devices return to the starting configuration and the transcript is cleared. Completion history is kept.") },
        confirmButton = { TextButton(onClick = {
            engine.state = lab.initial(); transcripts.clear(); hints.clear(); checked = null; tick++; confirmReset = false
            s.vm.saveLab(id, current())
        }) { Text("Reset") } }, dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } })
}

@Composable
private fun LabTabContent(
    s: AppScope, lab: Lab, tab: Int, engine: CliEngine, net: Network, results: Map<String, Boolean>, hints: MutableMap<String, Int>,
    checked: Map<String, Boolean>?, device: String, openCli: (String) -> Unit, runVerify: (String, String) -> List<String>, persist: () -> Unit, onCheck: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        when (tab) {
            0 -> TasksTab(s, lab, results, hints, checked, persist, onCheck)
            1 -> TopologyTab(lab, engine, net, openCli)
            3 -> VerifyTab(lab, runVerify)
            4 -> ReviewTab(s, lab, engine, results)
        }
        Spacer(Modifier.navigationBarsPadding().height(24.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TasksTab(s: AppScope, lab: Lab, results: Map<String, Boolean>, hints: MutableMap<String, Int>, checked: Map<String, Boolean>?, persist: () -> Unit, onCheck: () -> Unit) {
    val sem = LocalSemantic.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Tag(lab.label, MaterialTheme.colorScheme.secondary); Tag("${lab.minutes} min", MaterialTheme.colorScheme.outline) }
    Text(lab.scenario, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
    lab.symptom?.let {
        Surface(color = sem.danger.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
            Row(Modifier.padding(12.dp)) { Icon(Icons.Filled.ReportProblem, null, tint = sem.danger); Spacer(Modifier.width(8.dp)); Text("Reported symptom: $it", style = MaterialTheme.typography.bodyMedium) }
        }
    }
    Text("Objectives", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
    lab.objectives.forEach { Bullet(it) }
    Spacer(Modifier.height(12.dp))
    if (lab.assessed) {
        Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Independent assessment: no hints and no live checkmarks. Configure everything, then tap 'Check my work'. Results count as independent mastery.",
                Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(8.dp))
    }
    lab.tasks.forEachIndexed { i, t ->
        val done = if (lab.assessed) checked?.get(t.id) else results[t.id]
        val level = hints[t.id] ?: 0
        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, when (done) { true -> sem.success; false -> if (lab.assessed) sem.danger else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f); null -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) })) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(when (done) { true -> Icons.Filled.CheckCircle; false -> if (lab.assessed) Icons.Filled.Cancel else Icons.Filled.RadioButtonUnchecked; null -> Icons.Filled.RadioButtonUnchecked },
                        if (done == true) "Done" else "Not done", tint = when (done) { true -> sem.success; false -> if (lab.assessed) sem.danger else MaterialTheme.colorScheme.outline; null -> MaterialTheme.colorScheme.outline })
                    Spacer(Modifier.width(10.dp))
                    Text("${i + 1}. ${t.title}", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    if (done == true) Text("Done", color = sem.success, style = MaterialTheme.typography.labelMedium)
                }
                if (t.detail.isNotBlank()) Text(t.detail, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 34.dp, top = 2.dp))
                if (!lab.assessed) {
                    Text("Verify: ${t.verify}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 34.dp, top = 2.dp))
                    t.hints.take(level).forEachIndexed { hi, h ->
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().padding(start = 34.dp, top = 6.dp)) {
                            Text("${listOf("Concept", "Command family", "Partial syntax")[hi]}: $h", Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall,
                                fontFamily = if (hi == 2) FontFamily.Monospace else FontFamily.Default)
                        }
                    }
                    if (done != true && level < t.hints.size) TextButton(onClick = { hints[t.id] = level + 1; persist() }, modifier = Modifier.padding(start = 26.dp)) {
                        Icon(Icons.Filled.Lightbulb, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text(if (level == 0) "Hint" else "Next hint (${level + 1}/3)")
                    }
                }
            }
        }
    }
    if (lab.assessed) {
        Button(onClick = onCheck, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(50.dp)) { Icon(Icons.Filled.FactCheck, null); Spacer(Modifier.width(8.dp)); Text("Check my work") }
        checked?.let { c -> val n = c.values.count { it }
            Text(if (n == c.size) "All requirements met — independent mastery recorded." else "$n of ${c.size} requirements met. Use verification commands to find what is missing.",
                color = if (n == c.size) sem.success else sem.warning, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp)) }
    } else if (results.values.all { it }) {
        Surface(color = sem.success.copy(alpha = 0.12f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("Lab complete", fontWeight = FontWeight.Bold, color = sem.success)
                Text(lab.whyItWorks, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                val next = LabCatalog.all.firstOrNull { it.kind == "challenge" && it.lessonIds.any { l -> l in lab.lessonIds } && it.id != lab.id }
                if (next != null) OutlinedButton(onClick = { s.nav.popBackStack(); s.go("lab/${next.id}") }, modifier = Modifier.padding(top = 8.dp)) { Text("Try the independent challenge") }
            }
        }
    }
}

// ---------------------------------------------------------------- Terminal

@Composable
private fun Terminal(engine: CliEngine, device: String, configurable: List<String>, onDevice: (String) -> Unit, lines: List<TranscriptLine>, submit: (String) -> Unit) {
    var input by remember(device) { mutableStateOf(TextFieldValue("")) }
    var histIdx by remember(device) { mutableIntStateOf(-1) }
    val list = rememberLazyListState()
    val history = engine.state.history[device].orEmpty()
    LaunchedEffect(lines.size, device) { if (lines.isNotEmpty()) list.scrollToItem(lines.size) }
    fun set(t: String) { input = TextFieldValue(t, TextRange(t.length)) }
    fun go() { val t = input.text; submit(t); set(""); histIdx = -1 }
    fun up() { if (history.isEmpty()) return; histIdx = if (histIdx == -1) history.lastIndex else (histIdx - 1).coerceAtLeast(0); set(history[histIdx]) }
    fun down() { if (histIdx == -1) return; histIdx++; if (histIdx > history.lastIndex) { histIdx = -1; set("") } else set(history[histIdx]) }
    fun tabComplete() = set(engine.complete(device, input.text))

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text("Console:", style = MaterialTheme.typography.labelLarge)
            configurable.forEach { d -> FilterChip(d == device, { onDevice(d) }, { Text(engine.state.dev(d).name) }, leadingIcon = { Icon(Icons.Filled.Terminal, null, Modifier.size(16.dp)) }) }
        }
        Surface(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp), color = TERM_BG, shape = RoundedCornerShape(12.dp)) {
            SelectionContainer {
                LazyColumn(state = list, contentPadding = PaddingValues(10.dp)) {
                    if (lines.isEmpty()) item { Text("Connected to ${engine.state.dev(device).name} console. Type a command, or ? for help.", color = Color(0xFF8B949E), fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall) }
                    items(lines.size) { i ->
                        val l = lines[i]
                        Text(l.text.ifEmpty { " " }, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall,
                            color = when (l.kind) { "echo" -> Color(0xFF7EE787); "err" -> Color(0xFFFF7B72); "note" -> Color(0xFF79C0FF); else -> TERM_FG },
                            fontWeight = if (l.kind == "echo") FontWeight.SemiBold else FontWeight.Normal)
                    }
                    item { Text(engine.prompt(device), fontFamily = FontFamily.Monospace, color = Color(0xFF7EE787), style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
        Text("Green = your commands · white = device output · red = IOS errors · blue [Trainer] = explanations (not IOS output)",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistChip({ submit(input.text + "?"); set(input.text) }, { Text("?") })
            AssistChip({ tabComplete() }, { Text("Tab ⇥") })
            AssistChip({ up() }, { Text("↑ history") })
            AssistChip({ down() }, { Text("↓") })
            AssistChip({ submit("end") }, { Text("Ctrl+Z (end)") })
            AssistChip({ set("do " + input.text) }, { Text("do …") })
        }
        OutlinedTextField(
            value = input, onValueChange = { input = it }, singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(8.dp).semantics { contentDescription = "Command input for ${engine.prompt(device)}" }.onPreviewKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) false
                else when (e.key) { Key.Tab -> { tabComplete(); true }; Key.DirectionUp -> { up(); true }; Key.DirectionDown -> { down(); true }; else -> false }
            },
            prefix = { Text(engine.prompt(device), fontFamily = FontFamily.Monospace) },
            textStyle = TextStyle(fontFamily = FontFamily.Monospace),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false, keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { go() }),
            trailingIcon = { IconButton(onClick = { go() }) { Icon(Icons.Filled.KeyboardReturn, "Run command") } }
        )
    }
}

// ---------------------------------------------------------------- Topology

private fun vlanKind(v: Int) = when (v) { 10, 30 -> "vlan10"; 20, 40 -> "vlan20"; 99, 98 -> "vlan99"; else -> "neutral" }

@Composable
private fun TopologyTab(lab: Lab, engine: CliEngine, net: Network, openCli: (String) -> Unit) {
    val sem = LocalSemantic.current
    var selected by remember { mutableStateOf<String?>(null) }
    var trace by remember(lab.id) { mutableStateOf<Pair<String, Network.Trace>?>(null) }
    SourceTag("lab")
    Text("Links and statuses are derived from the current configuration. Tap a device for details.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 6.dp))
    TopologyCanvas(engine.state, net, selected, trace?.second, Modifier.fillMaxWidth()) { selected = it }
    Row(Modifier.padding(top = 6.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Tag("━ trunk", MaterialTheme.colorScheme.primary); Tag("━ access (VLAN shown)", MaterialTheme.colorScheme.secondary); Tag("┅ down / not forwarding", sem.danger)
    }
    selected?.let { DeviceDetails(engine.state, net, it, openCli) }
    Text("Reachability tests", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp))
    Text("Modelled from the lab tables (VLANs, trunks, routes, gateways), checking both directions. Not real ICMP.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    val hosts = engine.state.devices.filter { it.kind == "host" }.map { it.id }
    val tests = (lab.pingTests + hosts.flatMap { a -> hosts.filter { it != a }.map { a to it } }).distinct().take(12)
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        tests.forEach { (a, b) -> AssistChip({ trace = "$a → $b" to net.hostPing(a, b) }, { Text("$a → $b") }) }
    }
    trace?.let { (title, t) ->
        Surface(color = (if (t.ok) sem.success else sem.danger).copy(alpha = 0.10f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("$title: ${if (t.ok) "reachable (both directions)" else "FAILS"}", fontWeight = FontWeight.Bold, color = if (t.ok) sem.success else sem.danger)
                t.steps.forEach { st -> Text(st, style = MaterialTheme.typography.bodySmall, fontWeight = if (st.endsWith(":")) FontWeight.SemiBold else FontWeight.Normal) }
                t.failure?.let { Text("Why: $it", style = MaterialTheme.typography.bodySmall, color = sem.danger, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp)) }
            }
        }
    }
}

@Composable
fun TopologyCanvas(st: LabState, net: Network, selected: String?, trace: Network.Trace?, modifier: Modifier, onTap: (String) -> Unit) {
    val tm = rememberTextMeasurer()
    val sem = LocalSemantic.current; val dark = sem.dark
    val text = if (dark) Color(0xFFECEFF4) else Color(0xFF1B1F24)
    val line = if (dark) Color(0xFFB0BEC5) else Color(0xFF546E7A)
    val bg = MaterialTheme.colorScheme.surfaceContainer
    val primary = MaterialTheme.colorScheme.primary
    val traceLinks = trace?.links?.toSet() ?: emptySet()
    fun size(d: Device) = if (d.kind == "host") Size(130f, 64f) else Size(160f, 72f)
    fun rect(d: Device, s: Float) = Rect(Offset((d.x - size(d).width / 2) * s, (d.y - size(d).height / 2) * s), Size(size(d).width * s, size(d).height * s))
    val desc = st.devices.joinToString("; ") { d -> d.name + if (d.kind == "host") " ${d.hostIp}" else "" } + ". Links: " +
        st.links.joinToString("; ") { l -> "${st.dev(l.a).name} ${IfName.short(l.aIf)} to ${st.dev(l.b).name} ${if (l.bIf == "NIC") "" else IfName.short(l.bIf)}" }
    Canvas(modifier.aspectRatio(1000f / 560f).semantics { contentDescription = "Lab topology: $desc" }.pointerInput(st) {
        detectTapGestures { o -> val s = this.size.width / 1000f; st.devices.firstOrNull { rect(it, s).contains(o) }?.let { onTap(it.id) } }
    }) {
        val s = size.width / 1000f
        st.links.forEach { l ->
            val a = st.dev(l.a); val b = st.dev(l.b)
            val pa = rect(a, s).center; val pb = rect(b, s).center
            val (label, color, up) = linkInfo(st, net, l, line, primary, dark)
            val inTrace = l in traceLinks
            val col = if (inTrace) (if (trace?.ok == true) sem.success else sem.warning) else if (up) color else sem.danger
            drawLine(col, pa, pb, strokeWidth = (if (inTrace) 9f else 5f) * s, pathEffect = if (!up) PathEffect.dashPathEffect(floatArrayOf(16f * s, 12f * s)) else null)
            if (label.isNotBlank()) {
                val lay = tm.measure(label, TextStyle(fontSize = (15f * s).toSp(), color = col, fontWeight = FontWeight.SemiBold))
                val mid = Offset((pa.x + pb.x) / 2 - lay.size.width / 2f, (pa.y + pb.y) / 2 - lay.size.height / 2f)
                drawRoundRect(bg, mid - Offset(4f * s, 2f * s), Size(lay.size.width + 8f * s, lay.size.height + 4f * s), CornerRadius(6f * s)); drawText(lay, topLeft = mid)
            }
            listOf(Triple(pa, pb, l.aIf), Triple(pb, pa, l.bIf)).forEach { (p, q, name) ->
                if (name == "NIC") return@forEach
                val pos = Offset(p.x + (q.x - p.x) * 0.28f, p.y + (q.y - p.y) * 0.28f)
                val lay = tm.measure(IfName.short(name), TextStyle(fontSize = (13f * s).toSp(), color = text))
                drawRoundRect(bg, pos - Offset(lay.size.width / 2f + 3f * s, lay.size.height / 2f + 1f * s), Size(lay.size.width + 6f * s, lay.size.height + 2f * s), CornerRadius(4f * s))
                drawText(lay, topLeft = pos - Offset(lay.size.width / 2f, lay.size.height / 2f))
            }
        }
        st.devices.forEach { d ->
            val r = rect(d, s)
            val (fill, stroke) = kindColors(if (d.kind == "host") "host" else d.kind, dark)
            drawRoundRect(fill, r.topLeft, r.size, CornerRadius(14f * s))
            drawRoundRect(if (d.id == selected) primary else stroke, r.topLeft, r.size, CornerRadius(14f * s), style = Stroke((if (d.id == selected) 6f else 3f) * s))
            val sub = if (d.kind == "host") "${d.hostIp}" else if (d.kind == "switch") "switch" else "router"
            val l1 = tm.measure(d.name, TextStyle(fontSize = (18f * s).toSp(), fontWeight = FontWeight.Bold, color = text, textAlign = TextAlign.Center), constraints = Constraints(maxWidth = r.width.toInt().coerceAtLeast(1)))
            val l2 = tm.measure(sub, TextStyle(fontSize = (13f * s).toSp(), color = text.copy(alpha = 0.8f), textAlign = TextAlign.Center), constraints = Constraints(maxWidth = r.width.toInt().coerceAtLeast(1)))
            val top = r.top + (r.height - l1.size.height - l2.size.height) / 2
            drawText(l1, topLeft = Offset(r.left, top)); drawText(l2, topLeft = Offset(r.left, top + l1.size.height))
        }
    }
}

/** (label, colour, forwarding?) for a cable, derived from the shared state. */
private fun linkInfo(st: LabState, net: Network, l: Link, line: Color, primary: Color, dark: Boolean): Triple<String, Color, Boolean> {
    val a = st.dev(l.a); val b = st.dev(l.b)
    val up = net.linkUp(l.a, l.aIf)
    if (!up) return Triple("down", line, false)
    val swSide = listOf(a to l.aIf, b to l.bIf).firstOrNull { (d, i) -> d.kind == "switch" && d.running.intf(i)?.switchport == true }
    if (swSide != null) {
        val (d, i) = swSide
        return when (net.operMode(d.id, i)) {
            "trunk" -> { val p = d.running.intf(i)!!; Triple("trunk · native ${p.nativeVlan} · ${VlanList.format(p.allowed)}", primary, true) }
            "access" -> { val v = d.running.intf(i)!!.accessVlan
                if (d.running.vlanExists(v)) Triple("VLAN $v", kindColors(vlanKind(v), dark).second, true) else Triple("VLAN $v inactive", line, false) }
            else -> Triple("", line, false)
        }
    }
    return Triple("routed", line, true)
}

@Composable
private fun DeviceDetails(st: LabState, net: Network, id: String, openCli: (String) -> Unit) {
    val d = st.dev(id)
    Card(Modifier.fillMaxWidth().padding(top = 10.dp), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("${d.name} — ${d.kind}", fontWeight = FontWeight.Bold)
            if (d.kind == "host") {
                Text("IP ${d.hostIp} / ${d.hostMask?.let { Ip.prefixLen(it) }}, gateway ${d.hostGw ?: "none"} (fixed host settings)", style = MaterialTheme.typography.bodySmall)
                st.peer(d.id, "NIC")?.let { (pd, pi) -> Text("Connected to ${st.dev(pd).name} ${IfName.short(pi)}", style = MaterialTheme.typography.bodySmall) }
            } else {
                val rows = d.running.interfaces.filter { st.peer(d.id, it.name) != null || it.ip != null || IfName.isSub(it.name) }.map { i ->
                    val (stt, proto) = net.status(d.id, i.name)
                    val peer = st.peer(d.id, i.name)?.let { (pd, pi) -> "${st.dev(pd).name}${if (pi == "NIC") "" else " " + IfName.short(pi)}" } ?: "—"
                    val role = when { i.switchport -> net.operMode(d.id, i.name).let { m -> if (m == "trunk") "trunk (native ${i.nativeVlan})" else if (m == "access") "access VLAN ${i.accessVlan}" else m }
                        i.ip != null -> "${i.ip}/${Ip.prefixLen(i.mask!!)}${i.dot1q?.let { " dot1Q $it" } ?: ""}"; else -> "no IP" }
                    listOf(IfName.short(i.name), "$stt/$proto", role, peer)
                }
                InteractiveTable(LessonTable("dev-${d.id}", "Interfaces (live)", listOf("Port", "Status", "Role", "Connected to"), rows, kind = "lab", recall = false), wide = true)
                if (d.kind == "switch") Text("VLANs: " + (d.running.vlans.keys + 1).sorted().joinToString { v -> "$v ${d.running.vlanName(v)}" }, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                if (d.kind == "router") Text("Routes: " + net.routes(d.id).filter { it.code != "L" }.joinToString { r -> "${r.code} ${Ip.str(r.prefix)}/${r.len}" }.ifEmpty { "none" },
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                TextButton(onClick = { openCli(d.id) }) { Icon(Icons.Filled.Terminal, null); Spacer(Modifier.width(6.dp)); Text("Open ${d.name} console") }
            }
        }
    }
}

// ---------------------------------------------------------------- Verify & Review

@Composable
private fun VerifyTab(lab: Lab, runVerify: (String, String) -> List<String>) {
    var out by remember(lab.id) { mutableStateOf<Pair<String, List<String>>?>(null) }
    Text("Verification commands", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    Text("Runs in privileged EXEC without changing the mode you are in on the console. Output reflects the current lab state.", style = MaterialTheme.typography.bodySmall)
    lab.verifyCommands.forEach { (d, c) ->
        OutlinedButton(onClick = { out = "$d# $c" to runVerify(d, c) }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            Text("$d: $c", fontFamily = FontFamily.Monospace, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
        }
    }
    out?.let { (title, lines) ->
        Text(title, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
        AnnotatedOutput(lines.drop(1), verifyAnnotations)
    }
}

/** Generic field explanations for common verification output (lesson activities add scenario-specific ones). */
private val verifyAnnotations = listOf(
    "Native vlan" to "Untagged VLAN on this trunk. Must match the other end.",
    "Vlans allowed on trunk" to "Configured allowed list on this end only — compare with the neighbour.",
    "allowed and active" to "Allowed VLANs that also exist on this switch.",
    "Gateway of last resort" to "The default route currently in use (or 'not set').",
    "is directly connected" to "Connected (C) or local (L) route from an up/up interface with an IP address.",
    "administratively down" to "The interface is shut down — 'no shutdown' enables it.",
    "(Inactive)" to "The VLAN assigned to this port does not exist on the switch.",
    "Operational Mode" to "What the port is actually doing after DTP, not just what was configured.",
    "startup-config is not present" to "Nothing has been saved to NVRAM yet."
)

@Composable
private fun ReviewTab(s: AppScope, lab: Lab, engine: CliEngine, results: Map<String, Boolean>) {
    val sem = LocalSemantic.current
    val save = s.p.labs[lab.id]
    val mistakes = s.p.labMistakes.filter { it.labId == lab.id }
    var reveal by remember(lab.id) { mutableStateOf(false) }
    SectionCard("Summary", Icons.Filled.Insights) {
        Text("Tasks passing now: ${results.values.count { it }}/${results.size}")
        Text("Hints revealed: ${save?.hintsUsed ?: 0}")
        Text("Commands rejected in this lab: ${mistakes.size}")
        Text(when { save?.completedAt != null && lab.assessed -> "Independent mastery recorded."
            save?.completedAt != null -> "Completed as guided practice${if ((save.completedWithHints ?: 0) == 0) " without hints" else ""}."
            else -> "Not completed yet." }, fontWeight = FontWeight.SemiBold, color = if (save?.completedAt != null) sem.success else MaterialTheme.colorScheme.onSurface)
    }
    Spacer(Modifier.height(12.dp))
    SectionCard("Why the solution works", Icons.Filled.EmojiObjects, accent = sem.warning) {
        if (reveal || save?.completedAt != null || lab.kind == "guided") Text(lab.whyItWorks, style = MaterialTheme.typography.bodyMedium)
        else TextButton(onClick = { reveal = true }) { Text("Reveal explanation (spoiler)") }
    }
    Spacer(Modifier.height(12.dp))
    SectionCard("Your changes vs the starting configuration", Icons.Filled.CompareArrows) {
        val start = lab.initial()
        engine.state.devices.filter { it.configurable }.forEach { d ->
            val before = engine.runningConfig(d.id, start.dev(d.id).running)
            val after = engine.runningConfig(d.id, d.running)
            val diff = configDiff(before, after)
            Text(d.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
            if (diff.isEmpty()) Text("No configuration changes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            diff.forEach { (sign, txt) -> Text("$sign $txt", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall,
                color = when (sign) { "+" -> sem.success; "−" -> sem.danger; else -> MaterialTheme.colorScheme.onSurfaceVariant }) }
            val vb = start.dev(d.id).running.vlans; val va = d.running.vlans
            (va.keys - vb.keys).forEach { Text("+ vlan $it (${va[it]}) — stored in vlan.dat", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, color = sem.success) }
            (vb.keys - va.keys).forEach { Text("− vlan $it (${vb[it]})", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, color = sem.danger) }
        }
    }
    lab.lessonIds.firstNotNullOfOrNull { LessonExtrasCatalog.forLesson(it).teachBack }?.let { tb ->
        Spacer(Modifier.height(12.dp)); TeachBackCard(s, "tb-${lab.id}", tb)
    }
    if (mistakes.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        SectionCard("Rejected commands in this lab", Icons.Filled.EditNote, accent = sem.danger) {
            mistakes.takeLast(8).reversed().forEach { m -> MistakeRow(m) }
        }
    }
}

/** Line-level diff grouped by configuration section (interface, global). Signs: "+" added, "−" removed, " " section header. */
fun configDiff(before: String, after: String): List<Pair<String, String>> {
    fun keyed(cfg: String): List<Pair<String, String>> {
        var section = "global"; val out = mutableListOf<Pair<String, String>>()
        cfg.lines().forEach { raw ->
            val l = raw.trimEnd()
            if (l.isBlank() || l == "!" || l.startsWith("Building") || l.startsWith("Current configuration") || l.startsWith("version") || l == "end" || l.startsWith("Using")) return@forEach
            if (!l.startsWith(" ")) { if (l.startsWith("interface ")) { section = l; return@forEach } else section = "global" }
            out += section to l.trim()
        }
        return out
    }
    val b = keyed(before); val a = keyed(after)
    val removed = b.filter { it !in a }; val added = a.filter { it !in b }
    val sections = (removed + added).map { it.first }.distinct()
    return sections.flatMap { sec ->
        (if (sec != "global") listOf(" " to sec) else emptyList()) + removed.filter { it.first == sec }.map { "−" to it.second } + added.filter { it.first == sec }.map { "+" to it.second }
    }
}

@Composable
private fun MistakeRow(m: LabMistake) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(m.command, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
        Text(m.error, style = MaterialTheme.typography.bodySmall, color = LocalSemantic.current.danger)
        if (m.explanation.isNotBlank()) Text(m.explanation, style = MaterialTheme.typography.bodySmall)
    }
}

// ================================================================== Reference & notebook

@Composable
fun SupportedCommandsScreen(s: AppScope) {
    val ref = remember { CliEngine(LabCatalog.cliBasics.initial()).reference() }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Supported commands", s, back = true)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth {
                SectionCard("What the trainer models", Icons.Filled.Info) { SUPPORTED_SCOPE.forEach { Bullet(it) } }
                Spacer(Modifier.height(12.dp))
                InteractiveTable(LessonTable("ref", "Commands (generated from the trainer's grammar)", listOf("Command", "Mode", "Purpose"),
                    ref.map { listOf(it.first, it.second, it.third) }, recall = false,
                    caption = "Abbreviations work as on IOS (for example 'sh ip int br'). '?' lists options; Tab completes unambiguous words; 'do' runs EXEC commands from configuration mode."), s.wide)
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
}

@Composable
fun LabNotebookScreen(s: AppScope) {
    val sem = LocalSemantic.current
    val open = s.p.labMistakes.filter { !it.fixed }.reversed()
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Mistake notebook", s, back = true) {
            if (s.p.labMistakes.any { it.fixed }) TextButton(onClick = { s.vm.clearFixedMistakes() }) { Text("Clear understood") }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { ContentWidth { Text("Commands the trainer rejected, with the reason. Review them, retry the lab, then mark them as understood.", style = MaterialTheme.typography.bodyMedium) } }
            if (open.isEmpty()) item { ContentWidth { Text("Nothing to review.", color = sem.success) } }
            items(open) { m ->
                ContentWidth {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${LabCatalog.byId[m.labId]?.title ?: m.labId} · ${m.device}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            MistakeRow(m)
                            Row {
                                TextButton(onClick = { s.go("lab/${m.labId}") }) { Text("Retry lab") }
                                TextButton(onClick = { s.vm.markMistakeFixed(m.ts) }) { Text("Understood") }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
        }
    }
}
