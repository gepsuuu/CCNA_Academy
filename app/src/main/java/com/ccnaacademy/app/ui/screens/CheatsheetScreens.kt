package com.ccnaacademy.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import com.ccnaacademy.app.data.Block
import com.ccnaacademy.app.data.Cheatsheet
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic
import com.ccnaacademy.app.util.PdfExport
import kotlinx.coroutines.delay

private fun Block.searchText(): String = listOf(title, text, mnemonic).joinToString(" ") + " " + items.joinToString(" ") + " " +
    headers.joinToString(" ") + " " + rows.joinToString(" ") { it.joinToString(" ") }

/** Returns a short snippet around the first match, or null if the sheet doesn't match. */
fun matchSnippet(sheet: Cheatsheet, q: String): String? {
    if (q.isBlank()) return ""
    if (sheet.title.contains(q, true) || sheet.tags.any { it.contains(q, true) }) return ""
    sheet.blocks.forEach { b ->
        val t = b.searchText(); val i = t.indexOf(q, ignoreCase = true)
        if (i >= 0) return "…" + t.substring((i - 30).coerceAtLeast(0), (i + q.length + 50).coerceAtMost(t.length)).trim() + "…"
    }
    return null
}

@Composable
fun CheatsheetListScreen(s: AppScope) {
    var q by rememberSaveable { mutableStateOf("") }
    var onlyBookmarks by rememberSaveable { mutableStateOf(false) }
    val ctx = LocalContext.current
    val sem = LocalSemantic.current
    val results = s.lib.cheatsheets.mapNotNull { sh -> matchSnippet(sh, q.trim())?.let { sh to it } }
        .filter { !onlyBookmarks || it.first.id in s.p.sheetBookmarks }
    val saveAll = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            val sheets = results.map { it.first }
            ctx.contentResolver.openOutputStream(uri)?.use { PdfExport.write(sheets, s.p.highlights, s.vm.diagrams, it) }
            Toast.makeText(ctx, "Saved ${sheets.size} cheatsheets as PDF", Toast.LENGTH_SHORT).show()
        }
    }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Cheatsheets", s, back = true) {
            IconButton(onClick = { saveAll.launch("CCNA-cheatsheets.pdf") }, enabled = results.isNotEmpty()) { Icon(Icons.Filled.Download, "Save listed sheets as PDF") }
        }
        Column(Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search keyword, e.g. 'wildcard', '161', 'DTP'") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = { if (q.isNotEmpty()) IconButton(onClick = { q = "" }) { Icon(Icons.Filled.Close, "Clear") } })
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                FilterChip(onlyBookmarks, { onlyBookmarks = !onlyBookmarks }, { Text("Bookmarked (${s.p.sheetBookmarks.size})") },
                    leadingIcon = { Icon(Icons.Filled.Bookmark, null, Modifier.size(16.dp)) })
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.OfflinePin, null, tint = sem.success, modifier = Modifier.size(16.dp))
                Text(" All sheets are stored on-device", style = MaterialTheme.typography.labelSmall)
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (results.isEmpty()) item { EmptyHint("No cheatsheets match.") }
            items(results, key = { it.first.id }) { (sh, snip) ->
                ContentWidth {
                    val d = s.c.domains.firstOrNull { it.id == sh.domainId }
                    val marks = s.p.highlights.count { it.startsWith("${sh.id}:") }
                    Card(Modifier.fillMaxWidth().clickable { s.go("sheet/${sh.id}") }, shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(sh.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("${d?.number ?: ""} ${d?.title ?: ""}" + if (marks > 0) " • $marks marked" else "", style = MaterialTheme.typography.labelSmall)
                                if (snip.isNotEmpty()) Text(snip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                            IconButton(onClick = { s.vm.toggleBookmark(sh.id) }) {
                                Icon(if (sh.id in s.p.sheetBookmarks) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, "Bookmark",
                                    tint = if (sh.id in s.p.sheetBookmarks) sem.warning else MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheatsheetScreen(s: AppScope, id: String) {
    val sh = s.lib.cheatsheets.firstOrNull { it.id == id } ?: return
    val ctx = LocalContext.current
    val sem = LocalSemantic.current
    var menu by remember { mutableStateOf(false) }
    var onlyMarked by rememberSaveable { mutableStateOf(false) }
    StudyTimer { s.vm.addStudyTime(it) }
    LaunchedEffect(id) { delay(20_000); s.vm.sheetReviewed(id) }   // counts as reviewed after 20 s of reading
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            ctx.contentResolver.openOutputStream(uri)?.use { PdfExport.write(listOf(sh), s.p.highlights, s.vm.diagrams, it) }
            Toast.makeText(ctx, "PDF saved", Toast.LENGTH_SHORT).show()
        }
    }
    val marks = Marks(sh.id, s.p.highlights) { s.vm.toggleHighlight(it) }
    // "Marked only" view: keep blocks that contain marked items, showing only those items.
    val blocks = if (!onlyMarked) sh.blocks else sh.blocks.mapIndexedNotNull { bi, b ->
        val keep = { i: Int -> marks.isMarked(bi, i) }
        when (b.type) {
            "table" -> b.rows.filterIndexed { i, _ -> keep(i) }.takeIf { it.isNotEmpty() }?.let { b.copy(rows = it) }
            "bullets", "code", "mnemonic" -> b.items.filterIndexed { i, _ -> keep(i) }.takeIf { it.isNotEmpty() }?.let { b.copy(items = it) }
            "chunks" -> b.rows.filterIndexed { i, _ -> keep(i) }.takeIf { it.isNotEmpty() }?.let { b.copy(rows = it) }
            else -> null
        }
    }
    Column(Modifier.fillMaxSize()) {
        AppTopBar(sh.title, s, back = true) {
            IconButton(onClick = { s.vm.toggleBookmark(sh.id) }) {
                Icon(if (sh.id in s.p.sheetBookmarks) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, "Bookmark",
                    tint = if (sh.id in s.p.sheetBookmarks) sem.warning else MaterialTheme.colorScheme.onSurface)
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, "More") }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem({ Text("Share as PDF") }, leadingIcon = { Icon(Icons.Filled.Share, null) }, onClick = {
                        menu = false
                        runCatching { PdfExport.share(ctx, PdfExport.shareUri(ctx, "CCNA-${sh.id}", listOf(sh), s.p.highlights, s.vm.diagrams), sh.title) }
                            .onFailure { Toast.makeText(ctx, "Export failed: ${it.message}", Toast.LENGTH_LONG).show() }
                    })
                    DropdownMenuItem({ Text("Save PDF to device") }, leadingIcon = { Icon(Icons.Filled.Download, null) }, onClick = { menu = false; save.launch("CCNA-${sh.id}.pdf") })
                    DropdownMenuItem({ Text(if (onlyMarked) "Show full sheet" else "Show marked items only") }, leadingIcon = { Icon(Icons.Filled.Highlight, null) },
                        onClick = { menu = false; onlyMarked = !onlyMarked })
                }
            }
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth {
                if (onlyMarked && blocks.isEmpty()) EmptyHint("Nothing marked yet. Long-press a row, bullet or command to mark it as important.")
                BlockList(blocks, s.vm.diagrams, if (onlyMarked) null else marks)
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
}
