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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic
import kotlin.math.roundToInt
import kotlin.random.Random

private val TECHNIQUE_HELP = listOf(
    "Mnemonic" to "A phrase whose initials match an ordered list (e.g., OSI layers). Only used where order and meaning stay exact.",
    "Acronym" to "Expand every acronym fully and connect it to what it does — never memorize letters alone.",
    "Visual association" to "Link a concept to a picture (postal system for encapsulation, building floors for OSI vs TCP/IP).",
    "Comparison table" to "Put similar items side by side (RADIUS vs TACACS+, STP vs OSPF elections) to separate them.",
    "Memory trick" to "A short rule that preserves the real behavior (e.g., 'longest prefix first, then AD').",
    "Chunking" to "Group many facts into 3-5 meaningful chunks (port families, prefix-by-octet).",
    "Active recall" to "Answer from memory before revealing. Retrieval strengthens memory far more than re-reading.",
    "Association" to "Anchor a number to a meaning (162 = 'the next port' for traps after 161 polls).",
    "Daily challenge" to "A short mixed recall set every day — spacing plus interleaving."
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemoryCenterScreen(s: AppScope) {
    var tech by rememberSaveable { mutableStateOf("") }
    var showHelp by rememberSaveable { mutableStateOf(false) }
    val allTech = s.lib.guides.flatMap { it.techniques }.distinct().sorted()
    val sem = LocalSemantic.current
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Memorization Center", s, back = true)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                ContentWidth {
                    Card(Modifier.fillMaxWidth().clickable { s.go("challenge") }, shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = sem.warning, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Daily memory challenge", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(s.p.challengeScores[today()]?.let { "Today's score: $it%" } ?: "8 quick recall prompts • about 3 minutes")
                            }
                            Icon(Icons.Filled.ChevronRight, null)
                        }
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("How the techniques work", Icons.Filled.Psychology, trailing = {
                        TextButton(onClick = { showHelp = !showHelp }) { Text(if (showHelp) "Hide" else "Show") }
                    }) {
                        if (showHelp) TECHNIQUE_HELP.forEach { (k, v) ->
                            Text(k, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text(v, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 6.dp))
                        } else Text("Every mnemonic here keeps the correct technical meaning — none replace understanding.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(tech.isEmpty(), { tech = "" }, { Text("All techniques") })
                    allTech.forEach { t -> FilterChip(tech == t, { tech = if (tech == t) "" else t }, { Text(t) }) }
                }
            }
            items(s.lib.guides.filter { tech.isEmpty() || tech in it.techniques }, key = { it.id }) { g ->
                ContentWidth {
                    val acc = DailyPlanner.recallAccuracy(s.p, g)
                    Card(Modifier.fillMaxWidth().clickable { s.go("guide/${g.id}") }, shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(g.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                acc?.let { Tag("Recall ${(it * 100).roundToInt()}%", if (it >= 0.8f) sem.success else if (it >= 0.5f) sem.warning else sem.danger) }
                            }
                            Text(g.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) { g.techniques.forEach { Tag(it, MaterialTheme.colorScheme.secondary) } }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuideScreen(s: AppScope, id: String) {
    val g = s.lib.guides.firstOrNull { it.id == id } ?: return
    LaunchedEffect(id) { s.vm.visitGuide(id) }
    StudyTimer { s.vm.addStudyTime(it) }
    val revealed = remember(id) { mutableStateMapOf<Int, Boolean>() }
    val graded = remember(id) { mutableStateMapOf<Int, Boolean>() }
    val chosen = remember(id) { mutableStateMapOf<String, Int>() }
    val sem = LocalSemantic.current
    Column(Modifier.fillMaxSize()) {
        AppTopBar(g.title, s, back = true)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth {
                Text(g.summary, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
                BlockList(g.blocks, s.vm.diagrams)
                Spacer(Modifier.height(12.dp))
                SectionCard("Active recall", Icons.Filled.Psychology, accent = MaterialTheme.colorScheme.tertiary,
                    trailing = { Text("${graded.count { it.value }}/${g.recall.size}", style = MaterialTheme.typography.labelLarge) }) {
                    Text("Say or write the answer before revealing it, then grade yourself honestly.", style = MaterialTheme.typography.labelSmall)
                    g.recall.forEachIndexed { i, r ->
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text(r.prompt, fontWeight = FontWeight.SemiBold)
                        if (revealed[i] != true) TextButton(onClick = { revealed[i] = true }) { Text("Reveal answer") }
                        else {
                            Text(r.answer, color = MaterialTheme.colorScheme.primary)
                            if (graded[i] == null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { graded[i] = false; s.vm.recordRecall("${g.id}:$i", false) }) { Text("Missed it") }
                                Button(onClick = { graded[i] = true; s.vm.recordRecall("${g.id}:$i", true) }) { Text("Got it") }
                            } else Text(if (graded[i] == true) "✓ Recalled" else "✗ Will come back in your plan", color = if (graded[i] == true) sem.success else sem.danger,
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                if (g.quiz.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    SectionCard("Check yourself", Icons.Filled.Quiz) {
                        g.quiz.forEachIndexed { i, q ->
                            if (i > 0) HorizontalDivider(Modifier.padding(vertical = 10.dp))
                            QuestionView(q, i + 1, chosen[q.id]) { c -> if (chosen[q.id] == null) chosen[q.id] = c }
                        }
                    }
                }
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
}

@Composable
fun MemoryChallengeScreen(s: AppScope) {
    // Deterministic per day so the challenge is the same if reopened today.
    val items = remember {
        val r = Random(today())
        s.lib.guides.flatMap { g -> g.recall.mapIndexed { i, rc -> Triple(g, i, rc) } }.shuffled(r).take(8)
    }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var revealed by rememberSaveable { mutableStateOf(false) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    val sem = LocalSemantic.current
    StudyTimer { s.vm.addStudyTime(it) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Daily memory challenge", s, back = true)
        if (index >= items.size) {
            val pct = score * 100 / items.size.coerceAtLeast(1)
            LaunchedEffect(Unit) { s.vm.saveChallenge(pct) }
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                ProgressRing(pct / 100f, "$pct%", "$score/${items.size}", size = 150.dp, color = if (pct >= 80) sem.success else sem.warning)
                Text(if (pct >= 80) "Strong recall today." else "Missed prompts feed back into tomorrow's plan.", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                Button(onClick = { s.nav.popBackStack() }) { Text("Done") }
            }
            return@Column
        }
        val (g, i, rc) = items[index]
        Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(progress = { index.toFloat() / items.size }, modifier = Modifier.widthIn(max = 700.dp).fillMaxWidth())
            Text("Prompt ${index + 1} of ${items.size} • ${g.title}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(8.dp))
            Card(Modifier.widthIn(max = 700.dp).fillMaxWidth().weight(1f), shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(rc.prompt, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    if (revealed) Text(rc.answer, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(top = 20.dp))
                }
            }
            Row(Modifier.widthIn(max = 700.dp).fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!revealed) Button(onClick = { revealed = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Reveal") }
                else {
                    OutlinedButton(onClick = { s.vm.recordRecall("${g.id}:$i", false); revealed = false; index++ }, modifier = Modifier.weight(1f).height(52.dp)) { Text("Missed") }
                    Button(onClick = { s.vm.recordRecall("${g.id}:$i", true); score++; revealed = false; index++ }, modifier = Modifier.weight(1f).height(52.dp)) { Text("Got it") }
                }
            }
        }
    }
}
