package com.ccnaacademy.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic
import kotlinx.coroutines.launch

// =========================================================== Comparison center
@Composable
fun ComparisonListScreen(s: AppScope) {
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Comparison center", s, back = true)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            s.c.domains.forEach { d ->
                val list = s.study.comparisons.filter { it.domainId == d.id }
                if (list.isNotEmpty()) {
                    item(key = d.id) { ContentWidth { Text("${d.number} ${d.title}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary) } }
                    items(list, key = { it.id }) { x ->
                        ContentWidth {
                            Card(Modifier.fillMaxWidth().clickable { s.go("compare/${x.id}") }, shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CompareArrows, null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(x.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        Text(x.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.Filled.ChevronRight, null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonScreen(s: AppScope, id: String) {
    val x = s.study.comparisons.firstOrNull { it.id == id } ?: run { MissingContent(s, "Comparison"); return }
    StudyTimer { s.vm.addStudyTime(it) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar(x.title, s, back = true)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth {
                Text(x.summary, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
                BlockList(x.blocks, s.vm.diagrams)
                Spacer(Modifier.height(12.dp))
                SectionCard("Learn more", Icons.Filled.MenuBook) {
                    x.lessons.mapNotNull { s.c.lessonMap[it] }.forEach { l -> ListRow(l.title, "Lesson • ${l.minutes} min") { s.lesson(l.id) } }
                }
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
}

@Composable
fun MissingContent(s: AppScope, what: String) {
    Column(Modifier.fillMaxSize()) {
        AppTopBar(what, s, back = true)
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Text("This $what is no longer available.") }
    }
}

// =========================================================== Common mistakes
@Composable
fun MistakesScreen(s: AppScope, focus: String) {
    val sem = LocalSemantic.current
    var open by rememberSaveable { mutableStateOf(focus) }
    val state = rememberLazyListState()
    LaunchedEffect(focus) { val i = s.study.mistakes.indexOfFirst { it.id == focus }; if (i >= 0) state.scrollToItem(i + 1) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Common exam mistakes", s, back = true)
        LazyColumn(state = state, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { ContentWidth { Text("Each card shows the wrong assumption, the correct concept, how the trap appears in questions and how to avoid it.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            items(s.study.mistakes, key = { it.id }) { m ->
                ContentWidth {
                    val expanded = open == m.id
                    Card(Modifier.fillMaxWidth().clickable { open = if (expanded) "" else m.id }, shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ReportProblem, null, tint = sem.danger)
                                Spacer(Modifier.width(10.dp))
                                Text(m.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null)
                            }
                            if (expanded) {
                                Labeled("Incorrect assumption", m.wrong, sem.danger)
                                Labeled("Correct concept", m.right, sem.success)
                                Labeled("How to recognize it in a question", m.recognize, MaterialTheme.colorScheme.primary)
                                Labeled("How to avoid it", m.avoid, MaterialTheme.colorScheme.secondary)
                                Labeled("Example", m.example, MaterialTheme.colorScheme.onSurfaceVariant)
                                s.c.lessonMap[m.lessonId]?.let { l -> TextButton(onClick = { s.lesson(l.id) }) { Text("Revise: ${l.title}") } }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Labeled(label: String, text: String, color: androidx.compose.ui.graphics.Color) {
    Column(Modifier.padding(top = 10.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = color)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

// =========================================================== Glossary
@Composable
fun GlossaryScreen(s: AppScope, initialTerm: String) {
    var q by rememberSaveable { mutableStateOf("") }
    var domain by rememberSaveable { mutableStateOf("all") }
    var selected by rememberSaveable { mutableStateOf(initialTerm) }
    val list = s.study.glossary.filter { g ->
        (domain == "all" || g.domainId == domain) &&
            (q.isBlank() || listOf(g.term, g.full, g.simple, g.technical).any { it.contains(q.trim(), ignoreCase = true) })
    }
    val letters = list.map { it.term.first().uppercaseChar().let { c -> if (c.isLetter()) c else '#' } }.distinct()
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(initialTerm) {
        val i = list.indexOfFirst { it.id == initialTerm }; if (i >= 0 && !s.wide) state.scrollToItem(i)
    }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Glossary", s, back = true)
        Column(Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search ${s.study.glossary.size} terms") },
                leadingIcon = { Icon(Icons.Filled.Search, null) }, trailingIcon = { if (q.isNotEmpty()) IconButton(onClick = { q = "" }) { Icon(Icons.Filled.Close, "Clear") } })
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(domain == "all", { domain = "all" }, { Text("All domains") })
                s.c.domains.forEach { d -> FilterChip(domain == d.id, { domain = d.id }, { Text(d.title) }) }
            }
        }
        Row(Modifier.weight(1f)) {
            LazyColumn(Modifier.weight(1f), state = state, contentPadding = PaddingValues(start = 16.dp, end = 8.dp, bottom = 16.dp)) {
                if (list.isEmpty()) item { EmptyHint("No terms match.") }
                items(list, key = { it.id }) { g ->
                    if (s.wide) {
                        ListRow(g.term, g.full.ifBlank { g.simple }) { selected = g.id }
                    } else GlossaryCard(s, g, expanded = selected == g.id) { selected = if (selected == g.id) "" else g.id }
                }
            }
            // A–Z index for fast navigation
            Column(Modifier.width(28.dp).fillMaxHeight().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                letters.forEach { L ->
                    Text(L.toString(), Modifier.clickable {
                        val i = list.indexOfFirst { (it.term.first().uppercaseChar().takeIf { c -> c.isLetter() } ?: '#') == L }
                        if (i >= 0) scope.launch { state.scrollToItem(i) }
                    }.padding(vertical = 3.dp, horizontal = 4.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            if (s.wide) {
                VerticalDivider()
                Box(Modifier.weight(1.3f).verticalScroll(rememberScrollState()).padding(16.dp)) {
                    val g = s.study.glossary.firstOrNull { it.id == selected } ?: list.firstOrNull()
                    if (g != null) GlossaryCard(s, g, expanded = true) {} else EmptyHint("Select a term.")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlossaryCard(s: AppScope, g: GlossaryEntry, expanded: Boolean, onClick: () -> Unit) {
    val d = s.c.domains.firstOrNull { it.id == g.domainId }
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(14.dp)) {
            Text(g.term, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            if (g.full.isNotBlank()) Text(g.full, style = MaterialTheme.typography.labelLarge)
            Text(g.simple, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            if (expanded) {
                Text("Technical explanation", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))
                Text(g.technical, style = MaterialTheme.typography.bodyMedium)
                Text("Exam domain: ${d?.number ?: ""} ${d?.title ?: ""}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                if (g.related.isNotEmpty()) {
                    Text("Related concepts", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        g.related.forEach { r -> s.study.termMap[r.lowercase()]?.let { e -> SuggestionChip({ s.go("glossary?term=${e.id}") }, { Text(r) }) } }
                    }
                }
            }
        }
    }
}
