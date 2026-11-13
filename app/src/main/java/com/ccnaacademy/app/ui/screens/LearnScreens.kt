package com.ccnaacademy.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic

fun domainIcon(id: String): ImageVector = when (id) {
    "d1" -> Icons.Filled.Hub; "d2" -> Icons.Filled.SettingsEthernet; "d3" -> Icons.Filled.Route
    "d4" -> Icons.Filled.Dns; "d5" -> Icons.Filled.Security; else -> Icons.Filled.Code
}

@Composable
fun LearnScreen(s: AppScope) {
    var selected by rememberSaveable { mutableStateOf("d1") }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Learn", s)
        if (s.wide) {
            // Tablet / landscape: list-detail layout
            Row(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.width(340.dp).fillMaxHeight(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item { BlueprintBanner(s) }
                    item { StudyLinks(s) }
                    items(s.c.domains) { d -> DomainCard(s, d, selected == d.id) { selected = d.id } }
                }
                VerticalDivider()
                Box(Modifier.weight(1f)) { DomainContent(s, selected) }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { BlueprintBanner(s) }
                item { StudyLinks(s) }
                items(s.c.domains) { d -> DomainCard(s, d, false) { s.domain(d.id) } }
            }
        }
    }
}

@Composable
fun BlueprintBanner(s: AppScope) {
    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("${s.c.blueprint.exam} ${s.c.blueprint.version} blueprint", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(s.c.blueprint.note, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun DomainCard(s: AppScope, d: Domain, selected: Boolean, onClick: () -> Unit) {
    val ls = s.c.lessonsIn(d.id)
    val done = ls.count { it.id in s.p.completedLessons }
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(domainIcon(d.id), null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("${d.number}  ${d.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${d.weight}% of exam • ${ls.size} lessons • ${d.chapters.size} chapters", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(d.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LabeledBar("Progress", if (ls.isEmpty()) 0f else done.toFloat() / ls.size, "$done/${ls.size}")
        }
    }
}

@Composable
fun DomainScreen(s: AppScope, id: String) {
    val d = s.c.domain(id)
    Column(Modifier.fillMaxSize()) {
        AppTopBar("${d.number} ${d.title}", s, back = true)
        DomainContent(s, id)
    }
}

@Composable
fun DomainContent(s: AppScope, id: String) {
    val d = s.c.domain(id)
    val sem = LocalSemantic.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("${d.number} ${d.title}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Exam weight ${d.weight}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        d.chapters.forEachIndexed { ci, ch ->
            item(key = ch.id) {
                val done = ch.lessonIds.count { it in s.p.completedLessons }
                SectionCard("Chapter ${ci + 1}: ${ch.title}", trailing = { Text("$done/${ch.lessonIds.size}", style = MaterialTheme.typography.labelLarge) }) {
                    ch.lessonIds.mapNotNull { s.c.lessonMap[it] }.forEach { l ->
                        val complete = l.id in s.p.completedLessons
                        Row(Modifier.fillMaxWidth().clickable { s.lesson(l.id) }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (complete) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked, null,
                                tint = if (complete) sem.success else MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(l.title, style = MaterialTheme.typography.bodyLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    ExamTag(l.examRequired)
                                    Text("Obj. ${l.objectives.joinToString()} • ${l.minutes} min", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                            }
                            Icon(Icons.Filled.ChevronRight, null)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.navigationBarsPadding()) }
    }
}

@Composable
fun StudyLinks(s: AppScope) {
    Row(Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip({ s.go("labs") }, { Text("CLI labs & animations") }, leadingIcon = { Icon(Icons.Filled.Terminal, null, Modifier.size(18.dp)) })
        AssistChip({ s.go("glossary?term=") }, { Text("Glossary") }, leadingIcon = { Icon(Icons.Filled.MenuBook, null, Modifier.size(18.dp)) })
        AssistChip({ s.go("comparisons") }, { Text("Comparisons") }, leadingIcon = { Icon(Icons.Filled.CompareArrows, null, Modifier.size(18.dp)) })
        AssistChip({ s.go("mistakes?focus=") }, { Text("Common mistakes") }, leadingIcon = { Icon(Icons.Filled.ReportProblem, null, Modifier.size(18.dp)) })
        AssistChip({ s.go("tutor") }, { Text("Tutor") }, leadingIcon = { Icon(Icons.Filled.Psychology, null, Modifier.size(18.dp)) })
    }
}
