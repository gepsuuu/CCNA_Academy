package com.ccnaacademy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.Achievements
import com.ccnaacademy.app.data.Badge
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic
import java.text.DateFormat
import java.util.Date

fun badgeIcon(k: String): ImageVector = when (k) {
    "lesson" -> Icons.Filled.MenuBook; "domain" -> Icons.Filled.Layers; "school" -> Icons.Filled.School; "quiz" -> Icons.Filled.Quiz
    "explore" -> Icons.Filled.TravelExplore; "cards" -> Icons.Filled.Style; "calc" -> Icons.Filled.Calculate; "exam" -> Icons.Filled.AssignmentTurnedIn
    "streak" -> Icons.Filled.LocalFireDepartment; "daily" -> Icons.Filled.EventAvailable; else -> Icons.Filled.EditNote
}

@Composable
fun AchievementsScreen(s: AppScope) {
    val badges = remember(s.p, s.bank) { Achievements.compute(s.c, s.lib, s.bank, s.p) }
    LaunchedEffect(badges) { s.vm.syncBadges(badges.filter { it.earned }.map { it.id }) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Milestones", s, back = true)
        LazyVerticalGrid(GridCells.Adaptive(260.dp), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text("${badges.count { it.earned }} of ${badges.size} milestones reached. Every milestone is calculated from your saved study data.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(badges.sortedBy { !it.earned }, key = { it.id }) { b -> BadgeCard(b, s.p.badges[b.id]) }
        }
    }
}

@Composable
fun BadgeCard(b: Badge, earnedAt: Long?) {
    val sem = LocalSemantic.current
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.padding(14.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = (if (b.earned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline).copy(alpha = 0.15f)) {
                Icon(badgeIcon(b.icon), null, Modifier.padding(10.dp).size(26.dp), tint = if (b.earned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(b.title, fontWeight = FontWeight.SemiBold, color = if (b.earned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                Text(b.description, style = MaterialTheme.typography.bodySmall)
                if (b.earned) Text("Reached" + (earnedAt?.let { " " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it)) } ?: ""),
                    style = MaterialTheme.typography.labelSmall, color = sem.success)
                else LabeledBar("", b.progress, "${b.current}/${b.goal}", MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
