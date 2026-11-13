package com.ccnaacademy.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.lab.*
import com.ccnaacademy.app.lab.LessonExtrasCatalog
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic

@Composable
fun LessonScreen(s: AppScope, id: String) {
    val l = s.c.lessonMap[id] ?: return
    val sem = LocalSemantic.current
    val d = s.c.domain(l.domainId)
    val complete = l.id in s.p.completedLessons
    val review = s.p.reviews[l.id]
    var showLegacy by remember(id) { mutableStateOf(false) }
    val lessonList = rememberLazyListState()
    val navigationScope = rememberCoroutineScope()
    val extras = remember(id) { LessonExtrasCatalog.forLesson(id) }
    LaunchedEffect(id) { s.vm.openLesson(id) }
    StudyTimer { s.vm.addStudyTime(it) }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(l.title, s, back = true) {
            IconButton(onClick = { s.go("newnote?lesson=${l.id}") }) { Icon(Icons.Filled.NoteAdd, "Add a note for this lesson") }
            IconButton(onClick = { s.vm.toggleLessonBookmark(l.id) }) {
                Icon(if (l.id in s.p.lessonBookmarks) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    if (l.id in s.p.lessonBookmarks) "Remove lesson bookmark" else "Bookmark lesson")
            }
        }
        LazyColumn(Modifier.fillMaxSize(), state = lessonList, contentPadding = PaddingValues(16.dp)) {
            item {
                ContentWidth {
                    Text("${d.number} ${d.title}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(l.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        ExamTag(l.examRequired)
                        Tag("Objective ${l.objectives.joinToString()}", MaterialTheme.colorScheme.secondary)
                        Tag("${l.minutes} min", MaterialTheme.colorScheme.outline)
                    }
                    Text("Tip: use section highlight controls in the new flow; long-press remains available in original references.", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                    Spacer(Modifier.height(6.dp))
                    if (l.learningStages.isEmpty()) LearningCycleStrip(extras, l)
                    else Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        l.learningStages.forEachIndexed { stageIndex, stage ->
                            OutlinedButton(onClick = {
                                val target = 2 + l.learningStages.take(stageIndex).sumOf { 1 + if (it.applicable) it.blocks.size else 0 }
                                navigationScope.launch {
                                    if (s.p.reducedMotion) lessonList.scrollToItem(target) else lessonList.animateScrollToItem(target)
                                }
                            }) { Text(stage.title) }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            if (l.learningStages.isNotEmpty()) {
                item { Block { Text("Migrated content draft: Android build and device acceptance pending. Existing learning records are retained.", style = MaterialTheme.typography.labelMedium) } }
                l.learningStages.forEach { stage ->
                    item(key = "stage-${stage.id}") {
                        Block {
                            Text(stage.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            if (!stage.applicable) Text("Not applicable: ${stage.reason}")
                        }
                    }
                    if (stage.applicable) stage.blocks.forEach { block ->
                        item(key = "content-${block.id}") {
                            val highlightKey = "${l.id}:block:${block.id}"
                            val highlighted = highlightKey in s.p.lessonHighlights
                            Block {
                                Column(Modifier.fillMaxWidth().background(if (highlighted) HighlightColor.copy(alpha = 0.16f) else Color.Transparent, RoundedCornerShape(12.dp))) {
                                    TextButton(onClick = { s.vm.toggleLessonHighlight(highlightKey) }) {
                                        Text(if (highlighted) "Remove section highlight" else "Highlight this section")
                                    }
                                    BlockView(block, 0, s.vm.diagrams, null)
                                }
                            }
                        }
                    }
                }
                if (extras.comparisons.isNotEmpty() || extras.tables.any { it.kind != "troubleshoot" }) item(key = "migration-compare") { Block { CompareSection(s, extras) } }
                if (extras.walkthroughs.isNotEmpty()) item(key = "migration-configure") { Block { ConfigureSection(s, extras) } }
                if (extras.outputs.isNotEmpty()) item(key = "migration-output") { Block { OutputSection(s, extras) } }
                if (extras.animations.isNotEmpty()) item(key = "pilot-animations") { Block { VisualizeSection(s, extras) } }
                if (extras.labs.isNotEmpty()) item(key = "pilot-labs") { Block { PracticeSection(s, extras) } }
                item { Block { TextButton(onClick = { showLegacy = !showLegacy }) { Text(if (showLegacy) "Hide preserved reference and highlights" else "Open preserved reference and highlights") } } }
            }
            if (l.learningStages.isEmpty() || showLegacy) {
            item { Block { SectionCard("1 • Start here — the simple version", Icons.Filled.EmojiObjects, accent = sem.warning) { HL(s, l, "simple", l.simple) } } }
            item {
                Block {
                    SectionCard("2 • In depth", Icons.Filled.Layers) {
                        l.deep.forEachIndexed { i, sec ->
                            if (i > 0) HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(sec.heading, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            HL(s, l, "deep$i", sec.text)
                        }
                    }
                }
            }
            l.diagrams.mapNotNull { s.vm.diagrams[it] }.forEach { dg -> item(key = "dg-${dg.id}") { Block { DiagramCard(dg) } } }
            // ---- v1.4: compare, visualize, configure, practise, interpret ----
            if (extras.tables.any { it.kind != "troubleshoot" } || extras.comparisons.isNotEmpty()) item(key = "x-compare") { Block { CompareSection(s, extras) } }
            if (extras.animations.isNotEmpty()) item(key = "x-visual") { Block { VisualizeSection(s, extras) } }
            if (extras.walkthroughs.isNotEmpty()) item(key = "x-config") { Block { ConfigureSection(s, extras) } }
            if (extras.labs.isNotEmpty() || extras.noLabReason != null || extras.tables.any { it.kind == "troubleshoot" }) item(key = "x-practice") { Block { PracticeSection(s, extras) } }
            if (extras.outputs.isNotEmpty()) item(key = "x-output") { Block { OutputSection(s, extras) } }
            item { Block { SectionCard("3 • Why it matters", Icons.Filled.Public, accent = MaterialTheme.colorScheme.secondary) { HL(s, l, "why", l.why) } } }
            item {
                Block {
                    SectionCard("4 • Practical examples", Icons.Filled.Science, accent = MaterialTheme.colorScheme.secondary) {
                        l.examples.forEachIndexed { i, ex ->
                            if (i > 0) Spacer(Modifier.height(10.dp))
                            Text(ex.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            HL(s, l, "ex$i", ex.text)
                        }
                    }
                }
            }
            item { Block { SectionCard("5 • Common mistakes", Icons.Filled.ReportProblem, accent = sem.danger) { l.mistakes.forEach { Bullet(it, sem.danger) } } } }
            item {
                Block {
                    SectionCard("6 • Terminology", Icons.Filled.Translate) {
                        l.terms.forEach { t ->
                            Column(Modifier.padding(vertical = 5.dp)) {
                                Text(t.term, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                Text(t.definition, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            item {
                Block {
                    SectionCard("7 • Must memorize", Icons.Filled.PushPin, accent = sem.exam,
                        container = sem.exam.copy(alpha = 0.08f), trailing = { Tag("Required for exam", sem.exam) }) {
                        l.memorize.forEach { Bullet(it, sem.exam) }
                    }
                }
            }
            item { Block { SectionCard("8 • Exam-focused notes", Icons.Filled.FactCheck, accent = sem.exam) { l.examNotes.forEach { Bullet(it, sem.exam) } } } }
            if (l.supplementary.isNotEmpty()) item {
                Block {
                    SectionCard("Beyond the exam", Icons.Filled.AutoStories, accent = sem.supplementary,
                        trailing = { Tag("Supplementary", sem.supplementary) }) {
                        Text("Helpful context that is not a CCNA 200-301 objective.", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        l.supplementary.forEach { Bullet(it, sem.supplementary) }
                    }
                }
            }
            item {
                Block {
                    SectionCard("9 • Summary", Icons.Filled.Summarize, container = MaterialTheme.colorScheme.primaryContainer) { HL(s, l, "summary", l.summary) }
                }
            }
            extras.teachBack?.let { tb -> item(key = "x-teach") { Block { TeachBackCard(s, "lesson-${l.id}", tb) } } }
            }
            item { Block { KnowledgeCheck(s, l) } }
            item {
                Block {
                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (!complete) {
                            Button(onClick = { s.vm.completeLesson(l.id) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                                Icon(Icons.Filled.CheckCircle, null); Spacer(Modifier.width(8.dp)); Text("Mark lesson complete")
                            }
                        } else {
                            Surface(color = sem.success.copy(alpha = 0.12f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Verified, null, tint = sem.success)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Lesson completed", fontWeight = FontWeight.SemiBold)
                                        Text(review?.let { "Next revision: ${dueLabel(it.dueEpochDay)}" } ?: "All revisions done",
                                            style = MaterialTheme.typography.labelMedium)
                                    }
                                    if (review != null && review.dueEpochDay <= today())
                                        FilledTonalButton(onClick = { s.vm.markReviewed(l.id) }) { Text("Revised") }
                                }
                            }
                            TextButton(onClick = { s.vm.uncompleteLesson(l.id) }) { Text("Mark as not complete") }
                        }
                        NextLessonButton(s, l)
                    }
                }
            }
            item { Spacer(Modifier.navigationBarsPadding().height(24.dp)) }
        }
    }
}

@Composable
private fun Block(content: @Composable ColumnScope.() -> Unit) = ContentWidth {
    content(); Spacer(Modifier.height(12.dp))
}

@Composable
private fun Body(text: String) = Text(text, style = MaterialTheme.typography.bodyLarge)

/** Long-press to highlight an explanation; highlights appear in Notes & bookmarks and are exported with notes. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun HL(s: AppScope, l: Lesson, section: String, text: String) {
    val key = "${l.id}:$section"
    val on = key in s.p.lessonHighlights
    Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth()
        .background(if (on) HighlightColor.copy(alpha = 0.28f) else Color.Transparent, RoundedCornerShape(6.dp))
        .combinedClickable(onClick = {}, onLongClick = { s.vm.toggleLessonHighlight(key) }, onLongClickLabel = if (on) "Remove highlight" else "Highlight"))
}

@Composable
private fun levelColor(level: String): Color = when (level) {
    "Beginner" -> LocalSemantic.current.success
    "Intermediate" -> LocalSemantic.current.warning
    else -> LocalSemantic.current.exam
}

@Composable
private fun NextLessonButton(s: AppScope, l: Lesson) {
    val order = s.c.domains.flatMap { d -> d.chapters.flatMap { it.lessonIds } }
    val idx = order.indexOf(l.id)
    val next = order.getOrNull(idx + 1)?.let { s.c.lessonMap[it] } ?: return
    OutlinedButton(onClick = { s.nav.popBackStack(); s.nav.navigate("lesson/${next.id}") },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("Next: ${next.title}", maxLines = 1)
    }
}

@Composable
fun KnowledgeCheck(s: AppScope, l: Lesson) {
    val selected = remember(l.id) { mutableStateMapOf<String, Int>() }
    val sem = LocalSemantic.current
    SectionCard("10 • Knowledge check", Icons.Filled.Quiz, accent = MaterialTheme.colorScheme.tertiary,
        trailing = { Text("${selected.count { (k, v) -> l.quiz.first { it.id == k }.answer == v }}/${l.quiz.size}", style = MaterialTheme.typography.labelLarge) }) {
        l.quiz.forEachIndexed { qi, q ->
            if (qi > 0) HorizontalDivider(Modifier.padding(vertical = 12.dp))
            QuestionView(q, qi + 1, selected[q.id]) { choice ->
                if (selected[q.id] == null) { selected[q.id] = choice; s.vm.recordAnswer(q, choice == q.answer) }
            }
        }
        if (selected.size == l.quiz.size) {
            LaunchedEffect(l.id) { s.vm.lessonCheckDone(l.id) }
            val score = selected.count { (k, v) -> l.quiz.first { it.id == k }.answer == v }
            Spacer(Modifier.height(10.dp))
            Text(if (score == l.quiz.size) "Excellent — full marks." else "Review the explanations above, then revisit the lesson sections you missed.",
                color = if (score == l.quiz.size) sem.success else sem.warning, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = { selected.clear() }) { Text("Try again") }
        }
    }
}

/** Reusable multiple-choice question with immediate feedback. */
@Composable
fun QuestionView(q: Question, number: Int, chosen: Int?, reveal: Boolean = true, onChoose: (Int) -> Unit) {
    val sem = LocalSemantic.current
    Text("Q$number. ${q.question}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    q.options.forEachIndexed { i, opt ->
        val answered = chosen != null && reveal
        val border = when {
            answered && i == q.answer -> sem.success
            answered && i == chosen -> sem.danger
            chosen == i -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        }
        Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(enabled = chosen == null || !reveal) { onChoose(i) },
            shape = RoundedCornerShape(12.dp), border = BorderStroke(if (border == MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)) 1.dp else 2.dp, border),
            color = if (answered && i == q.answer) sem.success.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${'A' + i}", fontWeight = FontWeight.Bold, color = border)
                Spacer(Modifier.width(12.dp))
                Text(opt, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                if (answered && i == q.answer) Icon(Icons.Filled.Check, null, tint = sem.success)
                if (answered && i == chosen && i != q.answer) Icon(Icons.Filled.Close, null, tint = sem.danger)
            }
        }
    }
    if (chosen != null && reveal) {
        Spacer(Modifier.height(6.dp))
        Text((if (chosen == q.answer) "Correct. " else "Not quite. ") + q.explanation, style = MaterialTheme.typography.bodyMedium,
            color = if (chosen == q.answer) sem.success else sem.danger)
    }
}
