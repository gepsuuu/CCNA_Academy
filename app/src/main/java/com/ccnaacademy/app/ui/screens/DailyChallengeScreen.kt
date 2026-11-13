package com.ccnaacademy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic

@Composable
fun DailyChallengeScreen(s: AppScope) {
    val day = today()
    val set = remember(day, s.bank) { DailyChallenges.build(day, s.bank, s.lib, s.study, s.p) }
    val done = s.p.challengeDone[day] ?: emptySet()
    val sem = LocalSemantic.current
    StudyTimer { s.vm.addStudyTime(it) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Daily challenge", s, back = true)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        ProgressRing(done.size / 5f, "${done.size}/5", "today", size = 92.dp, color = if (done.size == 5) sem.success else MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("${DailyChallenges.streak(s.p)}-day challenge streak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Complete all five parts each day to extend the streak. New challenges appear at midnight.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Part("1 • Networking question", "question" in done) { QuestionPart(s, set.question, "question" in done) }
                Part("2 • Subnetting challenge", "subnet" in done) { SubnetPart(s, set.subnet, "subnet" in done) }
                Part("3 • Flashcard challenge", "card" in done) { CardPart(s, set.card, "card" in done) }
                Part("4 • Concept to memorize", "concept" in done) { ConceptPart(s, set.concept, "concept" in done) }
                Part("5 • Exam-speed challenge", "speed" in done) {
                    Text("5 rapid calculations (binary, masks, wildcards, hosts, private ranges, ports) in 60 seconds.", style = MaterialTheme.typography.bodyMedium)
                    val best = s.p.drills.lastOrNull { it.kind == "daily" && java.time.Instant.ofEpochMilli(it.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay() == day }
                    best?.let { Text("Today: ${it.correct}/${it.total} in ${it.seconds}s", color = sem.success, fontWeight = FontWeight.SemiBold) }
                    Button(onClick = { s.go("drill?kind=daily&ref=daily-$day&level=1&types=${set.speedTypes.joinToString(",")}&count=5&limit=60&typed=false") },
                        modifier = Modifier.padding(top = 6.dp)) { Text(if (best == null) "Start 60-second challenge" else "Try again") }
                }
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
}

@Composable
private fun Part(title: String, done: Boolean, content: @Composable ColumnScope.() -> Unit) {
    val sem = LocalSemantic.current
    SectionCard(title, if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked, accent = if (done) sem.success else MaterialTheme.colorScheme.primary,
        trailing = { if (done) Tag("Done", sem.success) }, content = content)
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun QuestionPart(s: AppScope, q: BankQuestion?, done: Boolean) {
    if (q == null) { EmptyHint("No questions are enabled. Enable a pack in Practice → Question packs."); return }
    val item = remember(q.id) { BankEngine.prepare(q) }
    var ans by remember(q.id) { mutableStateOf<List<Int>?>(null) }
    var submitted by remember(q.id) { mutableStateOf(false) }
    if (done && !submitted) {
        val st = s.p.bankStats[q.id]
        Text(if (st?.lastCorrect == true) "Answered correctly today." else "Answered today — review it below.", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = { s.go("question/${q.id}") }) { Text("Review the question and explanation") }
        return
    }
    QuestionCard(item, 1, ans ?: if (q.type == "order") item.initOrder else null, reveal = submitted, diagrams = s.vm.diagrams,
        domainTitle = s.c.domains.firstOrNull { it.id == q.domainId }?.title ?: "", topicTitle = s.bank.topicTitle(q.topic),
        onAnswer = { ans = it }, onOpenLesson = { s.lesson(q.lessonId) })
    if (!submitted) Button(onClick = {
        val a = ans ?: item.initOrder.takeIf { q.type == "order" }
        ans = a; submitted = true
        s.vm.recordBank(q, BankEngine.isCorrect(q, a), 0); s.vm.challengePart("question")
    }, enabled = BankEngine.isComplete(q, ans ?: if (q.type == "order") item.initOrder else null), modifier = Modifier.padding(top = 8.dp)) { Text("Submit") }
}

@Composable
private fun SubnetPart(s: AppScope, p: Problem, done: Boolean) {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Boolean?>(null) }
    val sem = LocalSemantic.current
    Text(p.prompt, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    if (result == null && !done) {
        OutlinedTextField(input, { input = it }, Modifier.fillMaxWidth().padding(top = 6.dp), singleLine = true, label = { Text("Your answer") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
        Button(onClick = { val ok = p.isCorrect(input); result = ok; s.vm.recordGen(p.type, ok); s.vm.challengePart("subnet") }, enabled = input.isNotBlank(),
            modifier = Modifier.padding(top = 6.dp)) { Text("Check") }
    } else {
        if (result != null) Text(if (result == true) "Correct!" else "Not quite — you entered $input", color = if (result == true) sem.success else sem.danger, fontWeight = FontWeight.SemiBold)
        Text("Answer: ${p.answer}", fontWeight = FontWeight.SemiBold)
        p.steps.forEach { Bullet(it) }
    }
}

@Composable
private fun CardPart(s: AppScope, c: Card?, done: Boolean) {
    if (c == null) { EmptyHint("No flashcards available."); return }
    var flipped by remember { mutableStateOf(false) }
    Text(c.front, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    if (!flipped && !done) { Button(onClick = { flipped = true }, modifier = Modifier.padding(top = 6.dp)) { Text("Recall it, then reveal") }; return }
    Text(c.answer, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
    Text(c.explanation, style = MaterialTheme.typography.bodySmall)
    Text("Tip: ${c.tip}", style = MaterialTheme.typography.bodySmall, color = LocalSemantic.current.exam)
    if (!done) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
        listOf(Rating.AGAIN, Rating.HARD, Rating.GOOD, Rating.EASY).forEach { r ->
            OutlinedButton(onClick = { s.vm.rateCard(c.id, r); s.vm.challengePart("card") }, contentPadding = PaddingValues(horizontal = 10.dp)) { Text(r.label) }
        }
    }
}

@Composable
private fun ConceptPart(s: AppScope, g: GlossaryEntry?, done: Boolean) {
    if (g == null) { EmptyHint("No concepts available."); return }
    var shown by remember { mutableStateOf(false) }
    Text(g.term + if (g.full.isNotBlank()) " — ${g.full}" else "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text(g.simple, style = MaterialTheme.typography.bodyMedium)
    if (!shown && !done) {
        Text("Read it, look away, and explain the technical detail out loud. Then reveal and compare.", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
        TextButton(onClick = { shown = true }) { Text("Reveal technical detail") }
    } else {
        Text(g.technical, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
        if (!done) Button(onClick = { s.vm.memorizeConcept(g.id); s.vm.challengePart("concept") }, modifier = Modifier.padding(top = 6.dp)) { Text("I can explain it") }
    }
}
