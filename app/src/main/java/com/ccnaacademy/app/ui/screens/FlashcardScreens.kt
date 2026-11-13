package com.ccnaacademy.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic

// ------------------------------------------------------------ Deck building
fun buildDeck(s: AppScope, mode: String, arg: String): List<Card> {
    val p = s.p; val t = today(); val all = s.lib.cards
    fun st(c: Card) = p.srs[c.id] ?: SrsState()
    return when (mode) {
        "due" -> {
            val cs = CardStats(s.lib, p)
            val due = all.filter { !st(it).isNew && st(it).due <= t }.sortedBy { st(it).due }
            val fresh = all.filter { st(it).isNew }.take(cs.newRemainingToday)
            (due + fresh).shuffled()
        }
        "category" -> all.filter { it.category == arg }.shuffled()
        "categories" -> { val set = arg.split(",").toSet(); all.filter { it.category in set }.shuffled() }
        "domain" -> all.filter { it.domainId == arg }.shuffled()
        "favorites" -> all.filter { it.id in p.favorites }.shuffled()
        "hard" -> all.filter { p.userDifficulty[it.id] == "hard" || st(it).leech || (st(it).reviews > 0 && st(it).ease < 2.1) }.shuffled()
        "incorrect" -> all.filter { st(it).wrong > 0 || st(it).lastRating == Rating.AGAIN.ordinal }.sortedByDescending { st(it).wrong }
        "leech" -> all.filter { st(it).leech }.sortedByDescending { st(it).lapses }
        "deck" -> { val ids = p.customDecks.firstOrNull { it.id == arg }?.cardIds?.toSet() ?: emptySet(); all.filter { it.id in ids }.shuffled() }
        else -> all.shuffled()
    }
}

fun modeTitle(s: AppScope, mode: String, arg: String) = when (mode) {
    "due" -> "Today's review"; "category" -> s.lib.category(arg); "categories" -> "Selected topics"
    "domain" -> s.c.domains.firstOrNull { it.id == arg }?.title ?: "Domain"; "favorites" -> "Favorites"; "hard" -> "Difficult cards"
    "incorrect" -> "Previously incorrect"; "leech" -> "Repeatedly forgotten"; "deck" -> s.p.customDecks.firstOrNull { it.id == arg }?.name ?: "Deck"
    else -> "All cards (shuffled)"
}

// ------------------------------------------------------------ Home
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlashcardsHomeScreen(s: AppScope) {
    val cs = remember(s.p) { CardStats(s.lib, s.p) }
    val sem = LocalSemantic.current
    val selected = remember { mutableStateListOf<String>() }
    var newDeck by remember { mutableStateOf(false) }
    var deleteDeck by remember { mutableStateOf<CustomDeck?>(null) }
    fun open(mode: String, arg: String = "") = s.go("cardsession?mode=$mode&arg=$arg")
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Flashcards", s, back = true) { IconButton(onClick = { s.go("cardbrowser?deck=&q=") }) { Icon(Icons.Filled.Search, "Browse cards") } }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                ContentWidth {
                    SectionCard("Daily revision target", Icons.Filled.Flag, container = MaterialTheme.colorScheme.primaryContainer) {
                        LabeledBar("Reviewed today", if (cs.target == 0) 1f else cs.reviewedToday.toFloat() / cs.target, "${cs.reviewedToday}/${cs.target}", sem.success)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                            Metric("${cs.dueToday}", "due"); Metric("${cs.overdue}", "overdue", if (cs.overdue > 0) sem.danger else null)
                            Metric("${cs.newRemainingToday}", "new today"); Metric("${cs.mastered}", "mastered", sem.success)
                        }
                        Button(onClick = { open("due") }, enabled = cs.dueToday + cs.newRemainingToday > 0, modifier = Modifier.fillMaxWidth()) {
                            Text(if (cs.dueToday + cs.newRemainingToday > 0) "Start today's review" else "All caught up for today")
                        }
                        Text("New cards per day", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 10.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 10, 15, 25, 40).forEach { n -> FilterChip(s.p.newPerDay == n, { s.vm.setNewPerDay(n) }, { Text("$n") }) }
                        }
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Focused practice", Icons.Filled.FilterAlt) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip({ open("all") }, { Text("Shuffle all ${s.lib.cards.size}") }, leadingIcon = { Icon(Icons.Filled.Shuffle, null) })
                            AssistChip({ open("favorites") }, { Text("Favorites (${s.p.favorites.size})") }, leadingIcon = { Icon(Icons.Filled.Star, null) })
                            AssistChip({ open("hard") }, { Text("Difficult only") }, leadingIcon = { Icon(Icons.Filled.Whatshot, null) })
                            AssistChip({ open("incorrect") }, { Text("Previously incorrect") }, leadingIcon = { Icon(Icons.Filled.Replay, null) })
                            AssistChip({ open("leech") }, { Text("Repeatedly forgotten (${cs.leeches.size})") }, leadingIcon = { Icon(Icons.Filled.Warning, null) })
                        }
                        if (cs.leeches.isNotEmpty()) Text("Cards forgotten 3+ times are flagged automatically. Try rewriting them as a mnemonic or reviewing the linked cheatsheet.",
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Topics", Icons.Filled.Category, trailing = {
                        if (selected.isNotEmpty()) TextButton(onClick = { open("categories", selected.joinToString(",")) }) { Text("Practice ${selected.size} selected") }
                    }) {
                        Text("Tap a topic to study it, or tick several and practice them together.", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        s.lib.categories.forEach { cat ->
                            val cards = s.lib.cards.filter { it.category == cat.id }
                            val m = cards.count { s.p.srs[it.id]?.mastered == true }
                            Row(Modifier.fillMaxWidth().clickable { open("category", cat.id) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(cat.id in selected, { if (it) selected.add(cat.id) else selected.remove(cat.id) })
                                Column(Modifier.weight(1f)) {
                                    LabeledBar(cat.title, if (cards.isEmpty()) 0f else m.toFloat() / cards.size, "$m/${cards.size}", sem.success)
                                }
                            }
                        }
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("By exam domain", Icons.Filled.School) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            s.c.domains.forEach { d -> SuggestionChip({ open("domain", d.id) }, { Text("${d.number} ${d.title}") }) }
                        }
                    }
                }
            }
            item {
                ContentWidth {
                    SectionCard("Custom decks", Icons.Filled.CollectionsBookmark, trailing = { TextButton(onClick = { newDeck = true }) { Text("New deck") } }) {
                        if (s.p.customDecks.isEmpty()) EmptyHint("Create a deck, then add cards from the browser or during a session.")
                        s.p.customDecks.forEach { d ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(d.name, style = MaterialTheme.typography.bodyLarge)
                                    Text("${d.cardIds.size} cards", style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(onClick = { open("deck", d.id) }, enabled = d.cardIds.isNotEmpty()) { Icon(Icons.Filled.PlayArrow, "Study") }
                                IconButton(onClick = { s.go("cardbrowser?deck=${d.id}&q=") }) { Icon(Icons.Filled.Edit, "Edit") }
                                IconButton(onClick = { deleteDeck = d }) { Icon(Icons.Filled.Delete, "Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
    if (newDeck) NameDialog("New deck", "", onDismiss = { newDeck = false }) { s.vm.createDeck(it); newDeck = false }
    deleteDeck?.let { d ->
        AlertDialog(onDismissRequest = { deleteDeck = null }, title = { Text("Delete '${d.name}'?") }, text = { Text("Cards stay in the library; only the deck is removed.") },
            confirmButton = { TextButton(onClick = { s.vm.deleteDeck(d.id); deleteDeck = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteDeck = null }) { Text("Cancel") } })
    }
}

@Composable
private fun Metric(value: String, label: String, color: androidx.compose.ui.graphics.Color? = null) {
    Column {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color ?: MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun NameDialog(title: String, initial: String, onDismiss: () -> Unit, onOk: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) },
        text = { OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onOk(name.trim()) }, enabled = name.isNotBlank()) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

// ------------------------------------------------------------ Session
@Composable
fun CardSessionScreen(s: AppScope, mode: String, arg: String) {
    val initial = remember { buildDeck(s, mode, arg).map { it.id } }
    val queue = remember { mutableStateListOf<String>().apply { addAll(initial) } }
    var flipped by remember { mutableStateOf(false) }
    var reviewed by rememberSaveable { mutableIntStateOf(0) }
    var again by rememberSaveable { mutableIntStateOf(0) }
    var deckMenu by remember { mutableStateOf(false) }
    val sem = LocalSemantic.current
    StudyTimer { s.vm.addStudyTime(it) }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(modeTitle(s, mode, arg), s, back = true) {
            if (queue.size > 1) IconButton(onClick = { val cur = queue.first(); val rest = queue.drop(1).shuffled(); queue.clear(); queue.add(cur); queue.addAll(rest) }) {
                Icon(Icons.Filled.Shuffle, "Shuffle remaining")
            }
        }
        if (queue.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Filled.TaskAlt, null, Modifier.size(64.dp), tint = sem.success)
                Text(if (initial.isEmpty()) "No cards match this selection right now." else "Session complete",
                    style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
                if (reviewed > 0) Text("$reviewed reviews • ${reviewed - again} recalled • $again 'Again'", textAlign = TextAlign.Center)
                Button(onClick = { s.nav.popBackStack() }, modifier = Modifier.padding(top = 16.dp)) { Text("Done") }
            }
            return@Column
        }
        val card = s.lib.cardMap[queue.first()] ?: return@Column
        val st = s.p.srs[card.id] ?: SrsState()
        val rot by animateFloatAsState(if (flipped) 180f else 0f, tween(350), label = "flip")
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(progress = { if (initial.isEmpty()) 0f else (initial.size - queue.size.coerceAtMost(initial.size)).toFloat() / initial.size },
                modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth())
            Row(Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${queue.size} left • ${s.lib.category(card.category)}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                if (st.isNew) Tag("NEW", MaterialTheme.colorScheme.primary)
                if (st.leech) { Spacer(Modifier.width(6.dp)); Tag("Often forgotten", sem.danger) }
                IconButton(onClick = { s.vm.toggleFavorite(card.id) }) {
                    Icon(if (card.id in s.p.favorites) Icons.Filled.Star else Icons.Filled.StarBorder, "Favorite", tint = if (card.id in s.p.favorites) sem.warning else MaterialTheme.colorScheme.outline)
                }
                Box {
                    IconButton(onClick = { deckMenu = true }) { Icon(Icons.Filled.PlaylistAdd, "Add to deck") }
                    DropdownMenu(deckMenu, { deckMenu = false }) {
                        if (s.p.customDecks.isEmpty()) DropdownMenuItem({ Text("Create a deck first (Flashcards home)") }, onClick = { deckMenu = false })
                        s.p.customDecks.forEach { d ->
                            DropdownMenuItem({ Text((if (card.id in d.cardIds) "✓ " else "") + d.name) }, onClick = { s.vm.toggleCardInDeck(d.id, card.id); deckMenu = false })
                        }
                    }
                }
            }
            Card(
                Modifier.widthIn(max = 760.dp).fillMaxWidth().weight(1f).graphicsLayer { rotationY = rot; cameraDistance = 14f * density }.clickable { flipped = !flipped },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = if (rot <= 90f) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Box(Modifier.fillMaxSize().graphicsLayer { rotationY = if (rot > 90f) 180f else 0f }) {
                    if (rot <= 90f) CardFront(s, card) else CardBack(s, card)
                }
            }
            if (!flipped) {
                Button(onClick = { flipped = true }, modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(vertical = 12.dp).height(52.dp)) { Text("Show answer") }
            } else {
                Row(Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Rating.entries.forEach { r ->
                        val color = when (r) { Rating.AGAIN -> sem.danger; Rating.HARD -> sem.warning; Rating.GOOD -> MaterialTheme.colorScheme.primary; Rating.EASY -> sem.success }
                        OutlinedButton(onClick = {
                            s.vm.rateCard(card.id, r); reviewed++
                            val id = queue.removeAt(0)
                            if (r == Rating.AGAIN) { again++; queue.add(minOf(3, queue.size), id) }
                            flipped = false
                        }, modifier = Modifier.weight(1f).height(60.dp), contentPadding = PaddingValues(4.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, color)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(r.label, color = color, fontWeight = FontWeight.Bold)
                                Text(Srs.label(st, r), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun CardFront(s: AppScope, card: Card) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(when (card.kind) { "question" -> "QUESTION"; "problem" -> "SOLVE"; "diagram" -> "DIAGRAM"; else -> "TERM" }, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(12.dp))
        if (card.kind == "diagram") card.diagram?.let { id -> s.vm.diagrams[id]?.let { DiagramCanvas(it, Modifier.fillMaxWidth()); Spacer(Modifier.height(12.dp)) } }
        Text(card.front, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("Tap to flip", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 16.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardBack(s: AppScope, card: Card) {
    val sem = LocalSemantic.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("ANSWER", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(card.answer, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text("Explanation", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Text(card.explanation, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text("Example", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Text(card.example, style = MaterialTheme.typography.bodyMedium)
        if (card.kind != "diagram") card.diagram?.let { id -> s.vm.diagrams[id]?.let { Spacer(Modifier.height(8.dp)); DiagramCanvas(it, Modifier.fillMaxWidth()) } }
        Spacer(Modifier.height(10.dp))
        Surface(color = sem.exam.copy(alpha = 0.10f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(10.dp)) {
                Icon(Icons.Filled.Lightbulb, null, tint = sem.exam); Spacer(Modifier.width(8.dp))
                Column { Text("Exam memory tip", fontWeight = FontWeight.SemiBold, color = sem.exam); Text(card.tip, style = MaterialTheme.typography.bodyMedium) }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Your difficulty label", style = MaterialTheme.typography.labelMedium)
        val cur = s.p.userDifficulty[card.id]
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("easy" to "Easy", "medium" to "Medium", "hard" to "Hard").forEach { (k, v) ->
                FilterChip(cur == k, { s.vm.setDifficulty(card.id, if (cur == k) null else k) }, { Text(v) })
            }
        }
    }
}

// ------------------------------------------------------------ Browser / deck editor
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardBrowserScreen(s: AppScope, deckId: String, initialQuery: String = "") {
    val deck = s.p.customDecks.firstOrNull { it.id == deckId }
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var cat by rememberSaveable { mutableStateOf("") }
    var onlyFav by rememberSaveable { mutableStateOf(false) }
    var expanded by remember { mutableStateOf<String?>(null) }
    var rename by remember { mutableStateOf(false) }
    val sem = LocalSemantic.current
    val list = s.lib.cards.filter { c ->
        (cat.isEmpty() || c.category == cat) && (!onlyFav || c.id in s.p.favorites) &&
            (query.isBlank() || listOf(c.front, c.answer, c.explanation, c.tip).any { it.contains(query, ignoreCase = true) })
    }
    Column(Modifier.fillMaxSize()) {
        AppTopBar(deck?.let { "Edit: ${it.name}" } ?: "Card library", s, back = true) {
            if (deck != null) IconButton(onClick = { rename = true }) { Icon(Icons.Filled.DriveFileRenameOutline, "Rename") }
        }
        Column(Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("Search cards") }, singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, null) })
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(onlyFav, { onlyFav = !onlyFav }, { Text("★ Favorites") })
                FilterChip(cat.isEmpty(), { cat = "" }, { Text("All") })
                s.lib.categories.forEach { c -> FilterChip(cat == c.id, { cat = if (cat == c.id) "" else c.id }, { Text(c.title) }) }
            }
            Text("${list.size} cards" + (deck?.let { " • ${it.cardIds.size} in deck" } ?: ""), style = MaterialTheme.typography.labelMedium)
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { c ->
                Card(Modifier.fillMaxWidth().clickable { expanded = if (expanded == c.id) null else c.id }, shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (deck != null) Checkbox(c.id in deck.cardIds, { s.vm.toggleCardInDeck(deck.id, c.id) })
                            Column(Modifier.weight(1f)) {
                                Text(c.front, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                val st = s.p.srs[c.id]
                                Text(s.lib.category(c.category) + (st?.let { " • interval ${it.interval}d • lapses ${it.lapses}" } ?: " • new") +
                                    (s.p.userDifficulty[c.id]?.let { " • $it" } ?: ""), style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { s.vm.toggleFavorite(c.id) }) {
                                Icon(if (c.id in s.p.favorites) Icons.Filled.Star else Icons.Filled.StarBorder, "Favorite",
                                    tint = if (c.id in s.p.favorites) sem.warning else MaterialTheme.colorScheme.outline)
                            }
                        }
                        if (expanded == c.id) {
                            HorizontalDivider(Modifier.padding(vertical = 6.dp))
                            Text(c.answer, fontWeight = FontWeight.SemiBold)
                            Text(c.explanation, style = MaterialTheme.typography.bodySmall)
                            Text("Tip: ${c.tip}", style = MaterialTheme.typography.bodySmall, color = sem.exam)
                        }
                    }
                }
            }
        }
    }
    if (rename && deck != null) NameDialog("Rename deck", deck.name, { rename = false }) { s.vm.renameDeck(deck.id, it); rename = false }
}
