package com.ccnaacademy.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.GlobalSearch
import com.ccnaacademy.app.data.Hit
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SearchScreen(s: AppScope) {
    var q by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<Map<String, List<Hit>>>(emptyMap()) }
    var expanded by remember { mutableStateOf(setOf<String>()) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    LaunchedEffect(q) {
        delay(200)   // debounce typing; search runs off the main thread
        results = if (q.trim().length < 2) emptyMap() else withContext(Dispatchers.Default) { GlobalSearch.run(q, s.c, s.lib, s.study, s.bank, s.p) }
    }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Search", s, back = true)
        OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp).focusRequester(focus), singleLine = true,
            placeholder = { Text("Terms, protocols, ports, lessons, notes…") }, leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = { if (q.isNotEmpty()) IconButton(onClick = { q = "" }) { Icon(Icons.Filled.Close, "Clear") } })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (q.trim().length >= 2 && results.isEmpty()) item { EmptyHint("No results for “$q”.") }
            if (q.isBlank()) item { EmptyHint("Searches lessons, glossary, comparisons, common mistakes, flashcards, cheatsheets, questions and your notes — all offline.") }
            results.forEach { (cat, hits) ->
                item(key = "h-$cat") {
                    ContentWidth {
                        Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(cat, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                            Text("${hits.size}", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                val shown = if (cat in expanded) hits else hits.take(4)
                itemsIndexed(shown, key = { i, _ -> "$cat-$i" }) { _, h ->
                    ContentWidth {
                        Column(Modifier.fillMaxWidth().clickable { s.go(h.route) }.padding(vertical = 8.dp)) {
                            Text(h.title, style = MaterialTheme.typography.bodyLarge, maxLines = 2)
                            Text(h.snippet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        }
                    }
                }
                if (hits.size > 4) item(key = "m-$cat") {
                    ContentWidth { TextButton(onClick = { expanded = if (cat in expanded) expanded - cat else expanded + cat }) { Text(if (cat in expanded) "Show fewer" else "Show all ${hits.size}") } }
                }
            }
        }
    }
}
