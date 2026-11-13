package com.ccnaacademy.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic

private data class Exchange(val mode: TutorMode, val input: String, val answer: TutorAnswer?)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TutorScreen(s: AppScope) {
    val cfg by s.vm.tutorConfig.collectAsStateWithLifecycle()
    var mode by remember { mutableStateOf(TutorMode.EXPLAIN) }
    var level by remember { mutableIntStateOf(0) }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var pickLesson by remember { mutableStateOf(false) }
    var pickQuestion by remember { mutableStateOf(false) }
    val history = remember { mutableStateListOf<Exchange>() }
    val listState = rememberLazyListState()
    val sem = LocalSemantic.current
    val online = cfg.usable
    fun send(text: String) {
        if (busy) return
        val inp = text.ifBlank { if (mode == TutorMode.NOTES) "weak topics" else return }
        busy = true
        history += Exchange(mode, inp, null)
        val idx = history.lastIndex
        s.vm.askTutor(mode, inp, level) { a -> history[idx] = history[idx].copy(answer = a); busy = false }
        input = ""
    }
    LaunchedEffect(history.size, busy) { if (history.isNotEmpty()) listState.animateScrollToItem(history.size) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Networking tutor", s, back = true) { IconButton(onClick = { s.go("settings") }) { Icon(Icons.Filled.Tune, "Tutor settings") } }
        Surface(color = if (online) sem.success.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (online) Icons.Filled.Cloud else Icons.Filled.OfflineBolt, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (online) "Online AI mode via ${cfg.host}. AI answers can contain mistakes — cross-check with lessons. Subnetting is always calculated on-device."
                    else "Offline mode: answers come from this app's verified lessons, glossary, comparisons and calculators — no AI is used. Add an AI service in Settings to enable AI answers.",
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TutorMode.entries.forEach { m -> FilterChip(mode == m, { mode = m }, { Text(m.label) }) }
        }
        if (mode == TutorMode.LEVEL) Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Core", "Mechanism", "Technical detail").forEachIndexed { i, l -> FilterChip(level == i, { level = i }, { Text(l) }) }
        }
        LazyColumn(Modifier.weight(1f), state = listState, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (history.isEmpty()) item {
                ContentWidth {
                    SectionCard("What can I help with?", Icons.Filled.Psychology) {
                        listOf("Explain simply" to "What is a native VLAN?", "Subnetting step by step" to "172.16.70.200/21", "Compare protocols" to "RADIUS vs TACACS+",
                            "Explain at a level" to "OSPF", "Practice questions" to "ACL").forEach { (m, ex) ->
                            Row(Modifier.fillMaxWidth().clickable { mode = TutorMode.entries.first { it.label == m }; send(ex) }.padding(vertical = 6.dp)) {
                                Icon(Icons.Filled.ChevronRight, null); Text("$m: “$ex”", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            items(history) { ex ->
                ContentWidth {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(16.dp), modifier = Modifier.align(Alignment.End)) {
                        Text("${ex.mode.label}: ${ex.input}", Modifier.padding(12.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    val a = ex.answer
                    if (a == null) LinearProgressIndicator(Modifier.fillMaxWidth()) else AnswerCard(s, a, ex.input)
                }
            }
        }
        when (mode) {
            TutorMode.SUMMARIZE -> PickerBar("Choose a lesson to summarize") { pickLesson = true }
            TutorMode.WHY -> PickerBar("Choose a question you've answered") { pickQuestion = true }
            TutorMode.NOTES -> PickerBar("Create notes from my weak topics") { send("weak topics") }
            else -> Row(Modifier.fillMaxWidth().padding(12.dp).navigationBarsPadding().imePadding(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text(mode.hint) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send), keyboardActions = KeyboardActions(onSend = { send(input) }))
                IconButton(onClick = { send(input) }, enabled = input.isNotBlank() && !busy) { Icon(Icons.AutoMirrored.Filled.Send, "Ask") }
            }
        }
    }
    if (pickLesson) PickDialog("Summarize a lesson", s.c.lessons.map { it.id to it.title }, { pickLesson = false }) { pickLesson = false; send(it) }
    if (pickQuestion) {
        val answered = s.p.bankStats.entries.sortedByDescending { it.value.lastTs }.mapNotNull { (id, st) -> s.bank.byId[id]?.let { id to ((if (st.lastCorrect) "✓ " else "✗ ") + it.stem) } }
        PickDialog("Explain an answer", answered.ifEmpty { s.bank.questions.take(30).map { it.id to it.stem } }, { pickQuestion = false }) { pickQuestion = false; send(it) }
    }
}

@Composable
private fun PickerBar(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(12.dp).navigationBarsPadding().height(50.dp)) { Text(label) }
}

@Composable
private fun PickDialog(title: String, options: List<Pair<String, String>>, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(options) { (id, label) -> Text(label, Modifier.fillMaxWidth().clickable { onPick(id) }.padding(vertical = 10.dp), maxLines = 2) }
            }
        })
}

@Composable
private fun AnswerCard(s: AppScope, a: TutorAnswer, input: String) {
    val ctx = LocalContext.current
    val sem = LocalSemantic.current
    var saved by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(a.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Tag(if (a.online) "AI-generated" else "Offline content", if (a.online) sem.warning else sem.success)
            }
            a.sections.forEach { (h, b) ->
                if (h.isNotBlank()) Text(h, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Text(b, style = MaterialTheme.typography.bodyMedium)
            }
            a.sources.forEach { (label, route) -> TextButton(onClick = { s.go(route) }, contentPadding = PaddingValues(0.dp)) { Text(label) } }
            Row {
                TextButton(onClick = {
                    val now = System.currentTimeMillis()
                    s.vm.saveNote(Note("note-$now", a.title.take(60) + " — " + input.take(40), a.asText(), topic = "Tutor", created = now, updated = now)); saved = true
                }, enabled = !saved) { Icon(Icons.Filled.NoteAdd, null); Spacer(Modifier.width(4.dp)); Text(if (saved) "Saved to notes" else "Save to notes") }
                TextButton(onClick = {
                    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, a.asText()), "Share"))
                }) { Icon(Icons.Filled.Share, null); Spacer(Modifier.width(4.dp)); Text("Share") }
            }
        }
    }
}
