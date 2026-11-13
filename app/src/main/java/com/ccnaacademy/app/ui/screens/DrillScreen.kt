package com.ccnaacademy.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic
import kotlinx.coroutines.delay

data class DrillConfig(val kind: String, val ref: String, val level: Int, val types: List<String>, val count: Int, val limitSec: Int, val typed: Boolean)

private data class Answered(val correct: Boolean, val given: String, val ms: Long)

fun buildDrill(s: AppScope, cfg: DrillConfig): List<DrillItem> {
    val tech = if (cfg.kind == "speed") s.lib.techniques.firstOrNull { it.id == cfg.ref } else null
    val gens = cfg.types.ifEmpty { tech?.generators ?: emptyList() }
    val generated = if (gens.isEmpty()) emptyList() else
        List(if (tech != null && tech.questions.isNotEmpty()) (cfg.count - tech.questions.size.coerceAtMost(cfg.count / 2)) else cfg.count) { i ->
            DrillItem(Gen.make(gens[i % gens.size], cfg.level), null)
        }
    val static = tech?.questions?.shuffled()?.take(if (gens.isEmpty()) cfg.count else cfg.count / 2)?.map { DrillItem(null, it) } ?: emptyList()
    return (generated + static).shuffled()
}

@Composable
fun DrillScreen(s: AppScope, cfg: DrillConfig) {
    val items = remember { buildDrill(s, cfg) }
    val answers = remember { mutableStateMapOf<Int, Answered>() }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var input by remember { mutableStateOf("") }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var qStart by remember { mutableLongStateOf(0L) }   // elapsedMs when the current question appeared
    var finished by rememberSaveable { mutableStateOf(false) }
    val sem = LocalSemantic.current
    val tech = s.lib.techniques.firstOrNull { it.id == cfg.ref }
    val target = tech?.targetSeconds ?: if (cfg.limitSec > 0 && items.isNotEmpty()) cfg.limitSec / items.size else 0
    StudyTimer { s.vm.addStudyTime(it) }

    fun finish() {
        if (finished) return
        finished = true
        if (cfg.kind != "subnet" || answers.isNotEmpty())
            s.vm.saveDrill(DrillResult(cfg.kind, cfg.ref, cfg.level, answers.values.count { it.correct }, items.size, (elapsedMs / 1000).toInt(), System.currentTimeMillis()))
        if (cfg.kind == "daily") s.vm.challengePart("speed")
    }
    fun answer(correct: Boolean, given: String) {
        if (answers[index] != null) return
        answers[index] = Answered(correct, given, elapsedMs - qStart)
        items[index].problem?.let { s.vm.recordGen(it.type, correct) }
        items[index].question?.let { s.vm.recordAnswer(it, correct) }
    }
    LaunchedEffect(finished) {
        val start = System.currentTimeMillis() - elapsedMs
        while (!finished) {
            delay(250); elapsedMs = System.currentTimeMillis() - start
            if (cfg.limitSec > 0 && cfg.kind in setOf("challenge", "daily") && elapsedMs >= cfg.limitSec * 1000L) finish()
        }
    }

    val title = when (cfg.kind) { "speed" -> tech?.title ?: "Speed drill"; "challenge" -> "Challenge: ${Gen.LEVELS.getOrElse(cfg.level) { "" }}"; "daily" -> "60-second speed challenge"; else -> "Subnetting practice" }
    Column(Modifier.fillMaxSize()) {
        AppTopBar(title, s, back = true) {
            val sec = (elapsedMs / 1000).toInt()
            val shown = if (cfg.kind == "challenge" || cfg.kind == "daily") (cfg.limitSec - sec).coerceAtLeast(0) else sec
            val over = cfg.limitSec > 0 && sec > cfg.limitSec
            Text("%d:%02d".format(shown / 60, shown % 60), fontWeight = FontWeight.Bold,
                color = if (over || ((cfg.kind == "challenge" || cfg.kind == "daily") && shown < 30)) sem.danger else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(end = 12.dp))
        }
        if (items.isEmpty()) { EmptyHint("No exercises available."); return@Column }
        if (finished || index >= items.size) {
            if (!finished) LaunchedEffect(Unit) { finish() }
            DrillResults(s, cfg, items, answers, elapsedMs, target)
            return@Column
        }
        val item = items[index]
        val done = answers[index]
        val qSec = ((done?.ms ?: (elapsedMs - qStart)) / 1000).toInt()
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth {
                LinearProgressIndicator(progress = { (index + 1f) / items.size }, modifier = Modifier.fillMaxWidth())
                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Question ${index + 1} of ${items.size}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    item.problem?.let { Tag(Gen.TYPES[it.type] ?: it.type, MaterialTheme.colorScheme.secondary) }
                    if (target > 0) { Spacer(Modifier.width(6.dp)); Tag("${qSec}s / ${target}s", if (qSec > target) sem.danger else sem.success) }
                }
                Text(item.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(14.dp))
                if (cfg.typed && item.typable) {
                    OutlinedTextField(input, { if (done == null) input = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Your answer") },
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { if (done == null && input.isNotBlank()) answer(item.problem!!.isCorrect(input), input) }),
                        enabled = done == null)
                    if (done == null) Button(onClick = { answer(item.problem!!.isCorrect(input), input) }, enabled = input.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(50.dp)) { Text("Check") }
                } else {
                    item.options.forEachIndexed { i, opt ->
                        val isRight = item.isCorrectOption(i)
                        val border = when {
                            done == null -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            isRight -> sem.success
                            done.given == opt -> sem.danger
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                        Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(enabled = done == null) { answer(isRight, opt) },
                            shape = RoundedCornerShape(12.dp), border = BorderStroke(if (done != null && (isRight || done.given == opt)) 2.dp else 1.dp, border)) {
                            Text(opt, Modifier.padding(14.dp), fontFamily = if (item.problem != null) FontFamily.Monospace else FontFamily.Default)
                        }
                    }
                }
                if (done != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(if (done.correct) "Correct in ${done.ms / 1000}s" else "Answer: ${item.correctText}", fontWeight = FontWeight.Bold,
                        color = if (done.correct) sem.success else sem.danger)
                    SectionCard("Solution", Icons.Filled.Lightbulb, accent = sem.warning) { item.explanation.forEach { Bullet(it, sem.warning) } }
                    Button(onClick = { index++; input = ""; qStart = elapsedMs; if (index >= items.size) finish() },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(50.dp)) { Text(if (index == items.size - 1) "See results" else "Next") }
                }
            }
        }
    }
}

@Composable
private fun DrillResults(s: AppScope, cfg: DrillConfig, items: List<DrillItem>, answers: Map<Int, Answered>, elapsedMs: Long, target: Int) {
    val sem = LocalSemantic.current
    val correct = answers.values.count { it.correct }
    val answered = answers.size
    val avg = if (answered == 0) 0 else (answers.values.sumOf { it.ms } / answered / 1000).toInt()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        ContentWidth {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                ProgressRing(correct.toFloat() / items.size, "$correct/${items.size}", "correct", size = 150.dp,
                    color = if (correct * 100 / items.size >= 80) sem.success else sem.warning)
                Text("Total ${elapsedMs / 1000}s • average ${avg}s per answered question" + if (target > 0) " (target ${target}s)" else "",
                    textAlign = TextAlign.Center, modifier = Modifier.padding(8.dp))
                if (target > 0) Text(when {
                    correct * 100 / items.size < 70 -> "Accuracy first: slow down and follow the method step by step."
                    avg > target -> "Accurate — now work on speed. Repeat until you're under ${target}s."
                    else -> "Fast and accurate. Move up a difficulty level."
                }, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                if (answered < items.size) Text("${items.size - answered} unanswered when time ran out.", color = sem.danger)
            }
            Spacer(Modifier.height(12.dp))
            SectionCard("Review", Icons.Filled.FactCheck) {
                items.forEachIndexed { i, item ->
                    val a = answers[i]
                    Row(Modifier.padding(vertical = 6.dp)) {
                        Icon(if (a?.correct == true) Icons.Filled.CheckCircle else Icons.Filled.Cancel, null, tint = if (a?.correct == true) sem.success else sem.danger)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(item.prompt, style = MaterialTheme.typography.bodyMedium)
                            Text("Answer: ${item.correctText}" + (a?.let { x -> " • you: ${x.given} • ${x.ms / 1000}s" } ?: " • not answered"),
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Button(onClick = { s.nav.popBackStack() }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Done") }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}
