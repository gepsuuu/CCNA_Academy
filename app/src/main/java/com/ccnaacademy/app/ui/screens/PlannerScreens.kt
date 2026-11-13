package com.ccnaacademy.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val DATE = DateTimeFormatter.ofPattern("EEE d MMM yyyy")
fun dayLabel(d: Long): String = LocalDate.ofEpochDay(d).format(DATE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerSetupScreen(s: AppScope) {
    val cur = s.p.planner
    var examDay by remember { mutableLongStateOf(cur?.examDay ?: (today() + 84)) }
    val days = remember { mutableStateListOf<Int>().apply { addAll(cur?.studyDays ?: setOf(1, 2, 3, 4, 5, 6)) } }
    var minutes by remember { mutableIntStateOf(cur?.minutesPerDay ?: 60) }
    var session by remember { mutableIntStateOf(cur?.sessionMinutes ?: 30) }
    var level by remember { mutableIntStateOf(cur?.level ?: 0) }
    var target by remember { mutableIntStateOf(cur?.targetScore ?: 85) }
    var picker by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar(if (cur == null) "Create study plan" else "Edit study plan", s, back = true)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth(700.dp) {
                SectionCard("Target exam date", Icons.Filled.Event) {
                    OutlinedButton(onClick = { picker = true }, modifier = Modifier.fillMaxWidth()) { Text(dayLabel(examDay)) }
                    Text("${(examDay - today()).coerceAtLeast(0)} days from today", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("Days you can study", Icons.Filled.CalendarMonth) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DayOfWeek.entries.forEach { d ->
                            FilterChip(d.value in days, { if (d.value in days) { if (days.size > 1) days.remove(d.value) } else days.add(d.value) },
                                { Text(d.getDisplayName(TextStyle.SHORT, Locale.getDefault())) })
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("Daily study time", Icons.Filled.Timer) {
                    Text("$minutes minutes per study day", fontWeight = FontWeight.SemiBold)
                    Slider(minutes.toFloat(), { minutes = (it / 15).roundToInt() * 15 }, valueRange = 15f..240f, steps = 14)
                    Text("Preferred session length", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(20, 30, 45, 60).forEach { m -> FilterChip(session == m, { session = m }, { Text("$m min") }) }
                    }
                    Text("Longer days are split into sessions of this length with short breaks.", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("Current networking knowledge", Icons.Filled.School) {
                    listOf("New to networking" to "Lessons start from the very basics; extra time is allowed per lesson.",
                        "Some experience" to "You know IP addresses and basic switching.",
                        "Experienced" to "You work with networks; plan focuses on exam objectives and practice.").forEachIndexed { i, (t, d) ->
                        Row(Modifier.fillMaxWidth().clickable { level = i }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(level == i, { level = i })
                            Column { Text(t); Text(d, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("Preparation goal", Icons.Filled.Flag) {
                    Text("Target readiness / practice score: $target%", fontWeight = FontWeight.SemiBold)
                    Slider(target.toFloat(), { target = (it / 5).roundToInt() * 5 }, valueRange = 70f..100f, steps = 5)
                    Text("Cisco does not publish a fixed passing score; this is your personal practice target.", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    s.vm.savePlanner(PlannerSettings(examDay, days.toSet(), minutes, session, level, target, cur?.createdDay ?: 0))
                    s.nav.popBackStack(); if (cur == null) s.go("planner")
                }, enabled = examDay > today(), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Generate my plan") }
                if (examDay <= today()) Text("Choose a date after today.", color = LocalSemantic.current.danger, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
    if (picker) {
        val st = rememberDatePickerState(initialSelectedDateMillis = LocalDate.ofEpochDay(examDay).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis / 86_400_000L > today()
            })
        DatePickerDialog(onDismissRequest = { picker = false },
            confirmButton = { TextButton(onClick = { st.selectedDateMillis?.let { examDay = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay() }; picker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { picker = false }) { Text("Cancel") } }) { DatePicker(st) }
    }
}

@Composable
fun PlannerScreen(s: AppScope) {
    val ps = s.p.planner
    if (ps == null) { PlannerSetupScreen(s); return }
    val plan = remember(s.p, s.c) { Planner.build(s.c, s.lib, s.p, ps) }
    val sem = LocalSemantic.current
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Study planner", s, back = true) { IconButton(onClick = { s.go("plannersetup") }) { Icon(Icons.Filled.Tune, "Edit plan settings") } }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        ProgressRing(plan.readiness, "${(plan.readiness * 100).roundToInt()}%", "of ${ps.targetScore}% goal", size = 104.dp,
                            color = if (plan.readiness * 100 >= ps.targetScore) sem.success else MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Exam: ${dayLabel(ps.examDay)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${Planner.weeksText(plan.daysLeft)} left • ${plan.studyDaysLeft} study days", style = MaterialTheme.typography.bodyMedium)
                            Text("Phase: ${plan.currentPhase?.name ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                            Tag(if (plan.feasible) "On track for time" else "Time is tight", if (plan.feasible) sem.success else sem.warning)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("What the plan is telling you", Icons.Filled.Lightbulb, accent = sem.warning) { plan.advice.forEach { Bullet(it) } }
                Spacer(Modifier.height(12.dp))
                SectionCard("This week", Icons.Filled.ViewWeek) {
                    Text("Adapts automatically: themes are ordered by incomplete lessons and your accuracy, and your last study day each week is revision.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    plan.week.forEach { d -> PlanDayRow(s, d, ps.sessionMinutes) }
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("Phases until the exam", Icons.Filled.Timeline) {
                    plan.phases.forEach { ph ->
                        val active = ph == plan.currentPhase
                        Row(Modifier.padding(vertical = 6.dp)) {
                            Icon(if (active) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked, null,
                                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(ph.name, fontWeight = FontWeight.SemiBold)
                                Text("${dayLabel(ph.from)} → ${dayLabel(ph.to)}", style = MaterialTheme.typography.labelSmall)
                                Text(ph.focus, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("Theme priorities", Icons.Filled.Sort) {
                    plan.priorities.forEach { (t, v) -> LabeledBar(t.title, v, "${(v * 100).roundToInt()}", if (v > 0.6f) sem.danger else if (v > 0.35f) sem.warning else sem.success) }
                    Text("Higher = needs more attention (incomplete lessons + lower accuracy).", style = MaterialTheme.typography.labelSmall)
                }
                TextButton(onClick = { s.vm.clearPlanner(); s.nav.popBackStack() }, modifier = Modifier.padding(top = 8.dp)) { Text("Delete plan") }
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
}

@Composable
private fun PlanDayRow(s: AppScope, d: PlanDay, session: Int) {
    var open by remember { mutableStateOf(d.day == today()) }
    val isToday = d.day == today()
    Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(enabled = d.items.isNotEmpty()) { open = !open }, shape = RoundedCornerShape(14.dp),
        color = if (isToday) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(d.dow.getDisplayName(TextStyle.FULL, Locale.getDefault()) + if (isToday) " (today)" else "", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(140.dp))
                Text(when { d.isExamDay -> "Exam day — good luck!"; !d.study -> "Rest day"; else -> d.theme?.title ?: "" },
                    modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                if (d.items.isNotEmpty()) {
                    val total = d.items.sumOf { it.minutes }
                    val n = (total + session - 1) / session
                    Text("$total min" + if (n > 1) " • $n × ${session}m" else "", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (open) d.items.forEach { it2 ->
                Row(Modifier.fillMaxWidth().clickable { s.go(it2.route) }.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PlayCircleOutline, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) { Text(it2.title, style = MaterialTheme.typography.bodyMedium); Text(it2.detail, style = MaterialTheme.typography.labelSmall) }
                    Text("${it2.minutes}m", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
