package com.ccnaacademy.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic
import java.text.DateFormat
import java.util.Date

private val TABS = listOf("Notes", "Lessons", "Diagrams", "Highlights", "Questions", "Cheatsheets")

fun notesMarkdown(s: AppScope): String = buildString {
    append("# CCNA Academy — my study notes\n\n")
    s.p.notes.groupBy { it.topic.ifBlank { "General" } }.toSortedMap().forEach { (topic, list) ->
        append("## $topic\n\n")
        list.sortedByDescending { it.updated }.forEach { n ->
            append("### ${n.title}\n")
            s.c.lessonMap[n.lessonId]?.let { append("_Lesson: ${it.title}_\n") }
            append("\n${n.body}\n\n")
        }
    }
    if (s.p.lessonHighlights.isNotEmpty()) {
        append("## Highlighted lesson passages\n\n")
        s.p.lessonHighlights.sorted().forEach { k ->
            val (lid, sec) = k.split(":", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
            s.c.lessonMap[lid]?.let { l -> append("- **${l.title}:** ${lessonSectionText(l, sec)}\n") }
        }
    }
}

@Composable
fun LibraryScreen(s: AppScope) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var q by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf("all") }
    val ctx = LocalContext.current
    val sem = LocalSemantic.current
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri ->
        if (uri != null) runCatching { ctx.contentResolver.openOutputStream(uri)?.use { it.write(notesMarkdown(s).toByteArray()) } }
            .onSuccess { Toast.makeText(ctx, "Notes exported", Toast.LENGTH_SHORT).show() }
            .onFailure { Toast.makeText(ctx, "Export failed: ${it.message}", Toast.LENGTH_LONG).show() }
    }
    fun m(vararg t: String) = q.isBlank() || t.any { it.contains(q.trim(), ignoreCase = true) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Notes & bookmarks", s, back = true) {
            IconButton(onClick = { export.launch("ccna-notes.md") }, enabled = s.p.notes.isNotEmpty() || s.p.lessonHighlights.isNotEmpty()) { Icon(Icons.Filled.Download, "Export notes as Markdown") }
            IconButton(onClick = {
                ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, notesMarkdown(s)), "Share notes"))
            }, enabled = s.p.notes.isNotEmpty()) { Icon(Icons.Filled.Share, "Share notes") }
        }
        ScrollableTabRow(selectedTabIndex = tab, edgePadding = 12.dp) {
            TABS.forEachIndexed { i, t ->
                val n = when (i) { 0 -> s.p.notes.size; 1 -> s.p.lessonBookmarks.size; 2 -> s.p.diagramBookmarks.size; 3 -> s.p.lessonHighlights.size; 4 -> s.p.bankBookmarks.size; else -> s.p.sheetBookmarks.size }
                Tab(tab == i, { tab = i }, text = { Text("$t ($n)") })
            }
        }
        OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), singleLine = true,
            placeholder = { Text("Search saved content") }, leadingIcon = { Icon(Icons.Filled.Search, null) })
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (tab) {
                0 -> {
                    val topics = s.p.notes.map { it.topic.ifBlank { "General" } }.distinct().sorted()
                    item {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(topic == "all", { topic = "all" }, { Text("All topics") })
                            topics.forEach { t -> FilterChip(topic == t, { topic = t }, { Text(t) }) }
                        }
                        Button(onClick = { s.go("newnote?lesson=") }, modifier = Modifier.padding(top = 6.dp)) { Icon(Icons.Filled.Add, null); Text(" New note") }
                    }
                    val notes = s.p.notes.filter { (topic == "all" || it.topic.ifBlank { "General" } == topic) && m(it.title, it.body, it.topic) }.sortedByDescending { it.updated }
                    if (notes.isEmpty()) item { EmptyHint("No notes yet. Add one here, from any lesson, or save a tutor answer.") }
                    items(notes, key = { it.id }) { n ->
                        Card(Modifier.fillMaxWidth().clickable { s.go("note/${n.id}") }, shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                            Column(Modifier.padding(14.dp)) {
                                Text(n.title, fontWeight = FontWeight.SemiBold)
                                Text(n.body, maxLines = 3, style = MaterialTheme.typography.bodySmall)
                                Text("${n.topic.ifBlank { "General" }} • ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(n.updated))}",
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                1 -> {
                    val ls = s.p.lessonBookmarks.mapNotNull { s.c.lessonMap[it] }.filter { m(it.title, it.summary) }
                    if (ls.isEmpty()) item { EmptyHint("Bookmark lessons with the bookmark icon at the top of any lesson.") }
                    items(ls, key = { it.id }) { l -> ListRow(l.title, "${s.c.domain(l.domainId).number} ${s.c.domain(l.domainId).title}") { s.lesson(l.id) } }
                }
                2 -> {
                    val ds = s.p.diagramBookmarks.mapNotNull { s.vm.diagrams[it] }.filter { m(it.title, it.explanation) }
                    if (ds.isEmpty()) item { EmptyHint("Bookmark diagrams with the bookmark icon on any diagram.") }
                    items(ds, key = { it.id }) { d -> DiagramCard(d) }
                }
                3 -> {
                    val hs = s.p.lessonHighlights.sorted().mapNotNull { k ->
                        val parts = k.split(":", limit = 2); val l = s.c.lessonMap[parts[0]] ?: return@mapNotNull null
                        Triple(k, l, lessonSectionText(l, parts.getOrElse(1) { "" }))
                    }.filter { m(it.second.title, it.third) }
                    if (hs.isEmpty()) item { EmptyHint("Long-press any explanation in a lesson to highlight it.") }
                    items(hs, key = { it.first }) { (k, l, text) ->
                        Surface(color = HighlightColor.copy(alpha = 0.22f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().clickable { s.lesson(l.id) }) {
                            Column(Modifier.padding(12.dp)) {
                                Text(l.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                Text(text, style = MaterialTheme.typography.bodyMedium)
                                TextButton(onClick = { s.vm.toggleLessonHighlight(k) }, contentPadding = PaddingValues(0.dp)) { Text("Remove highlight") }
                            }
                        }
                    }
                }
                4 -> {
                    val qs = s.p.bankBookmarks.mapNotNull { s.bank.byId[it] }.filter { m(it.stem, it.concept) }
                    if (qs.isEmpty()) item { EmptyHint("Save difficult questions with the bookmark icon during practice.") }
                    else item { Button(onClick = { s.go(bankRoute("bookmarks", 0)) }) { Text("Practice all ${qs.size} saved questions") } }
                    items(qs, key = { it.id }) { q -> ListRow(q.stem, s.bank.topicTitle(q.topic) + " • " + (BankEngine.DIFFICULTY[q.difficulty] ?: "")) { s.go("question/${q.id}") } }
                }
                else -> {
                    val sh = s.lib.cheatsheets.filter { it.id in s.p.sheetBookmarks && m(it.title) }
                    if (sh.isEmpty()) item { EmptyHint("Bookmark cheatsheets from the Cheatsheet Center.") }
                    items(sh, key = { it.id }) { x -> ListRow(x.title, "Cheatsheet") { s.go("sheet/${x.id}") } }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(s: AppScope, id: String, lessonId: String) {
    val existing = s.p.notes.firstOrNull { it.id == id }
    val lesson = s.c.lessonMap[existing?.lessonId ?: lessonId]
    var title by rememberSaveable { mutableStateOf(existing?.title ?: lesson?.let { "Notes: ${it.title}" } ?: "") }
    var body by rememberSaveable { mutableStateOf(existing?.body ?: "") }
    var topic by rememberSaveable { mutableStateOf(existing?.topic ?: lesson?.let { s.c.domain(it.domainId).title } ?: "") }
    var confirmDelete by remember { mutableStateOf(false) }
    val suggestions = (s.c.domains.map { it.title } + s.p.notes.map { it.topic }).filter { it.isNotBlank() }.distinct()
    fun save() {
        val now = System.currentTimeMillis()
        s.vm.saveNote(Note(existing?.id ?: "note-$now", title.trim().ifBlank { "Untitled note" }, body, topic.trim(),
            lesson?.domainId ?: existing?.domainId ?: "", lesson?.id ?: "", existing?.created ?: now, now))
        s.nav.popBackStack()
    }
    Column(Modifier.fillMaxSize()) {
        AppTopBar(if (existing == null) "New note" else "Edit note", s, back = true) {
            if (existing != null) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, "Delete note") }
            TextButton(onClick = { save() }, enabled = title.isNotBlank() || body.isNotBlank()) { Text("Save") }
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp).imePadding()) {
            ContentWidth(760.dp) {
                lesson?.let { Text("Linked lesson: ${it.title}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true)
                OutlinedTextField(topic, { topic = it }, Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Topic (used to organize notes)") }, singleLine = true)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { suggestions.take(10).forEach { t -> SuggestionChip({ topic = t }, { Text(t) }) } }
                OutlinedTextField(body, { body = it }, Modifier.fillMaxWidth().heightIn(min = 260.dp).padding(top = 8.dp), label = { Text("Note") })
            }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Delete this note?") },
        confirmButton = { TextButton(onClick = { existing?.let { s.vm.deleteNote(it.id) }; confirmDelete = false; s.nav.popBackStack() }) { Text("Delete") } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
}

/** Practice or review a single bank question (used by search, bookmarks, tutor links and daily challenge). */
@Composable
fun SingleQuestionScreen(s: AppScope, id: String) {
    val q = s.bank.byId[id] ?: run { MissingContent(s, "Question"); return }
    val item = remember(id) { BankEngine.prepare(q) }
    var ans by remember(id) { mutableStateOf<List<Int>?>(null) }
    var submitted by remember(id) { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Question", s, back = true) {
            IconButton(onClick = { s.vm.toggleBankBookmark(id) }) { Icon(if (id in s.p.bankBookmarks) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, "Save question") }
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth {
                QuestionCard(item, 1, ans ?: if (q.type == "order") item.initOrder else null, reveal = submitted, diagrams = s.vm.diagrams,
                    domainTitle = s.c.domains.firstOrNull { it.id == q.domainId }?.title ?: "", topicTitle = s.bank.topicTitle(q.topic),
                    onAnswer = { ans = it }, onOpenLesson = { s.lesson(q.lessonId) })
                Spacer(Modifier.height(12.dp))
                if (!submitted) Button(onClick = {
                    val a = ans ?: item.initOrder.takeIf { q.type == "order" }; ans = a; submitted = true
                    s.vm.recordBank(q, BankEngine.isCorrect(q, a), 0)
                }, enabled = BankEngine.isComplete(q, ans ?: if (q.type == "order") item.initOrder else null), modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Submit answer") }
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
}
