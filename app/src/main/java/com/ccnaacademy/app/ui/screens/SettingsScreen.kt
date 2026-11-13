package com.ccnaacademy.app.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccnaacademy.app.data.TutorConfig
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.ui.components.*
import com.ccnaacademy.app.ui.nav.AppScope
import com.ccnaacademy.app.ui.nav.AppTopBar
import com.ccnaacademy.app.ui.theme.LocalSemantic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(s: AppScope) {
    var confirmReset by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Settings", s, back = true)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            ContentWidth(700.dp) {
                SectionCard("Appearance", Icons.Filled.Palette) {
                    Text("Theme", style = MaterialTheme.typography.labelLarge)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEachIndexed { i, (k, v) ->
                            SegmentedButton(selected = s.p.themeMode == k, onClick = { s.vm.setTheme(k) },
                                shape = SegmentedButtonDefaults.itemShape(i, 3)) { Text(v) }
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Use wallpaper colors (Material You)", modifier = Modifier.weight(1f))
                        Switch(s.p.dynamicColor, { s.vm.setDynamic(it) })
                    }
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("Motion and animations", Icons.Filled.Animation) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Reduce motion")
                            Text("Animations change step by step without moving packets. Also follows the system 'Remove animations' setting.",
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(s.p.reducedMotion, { s.vm.setReducedMotion(it) })
                    }
                    Text("Default animation speed", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        listOf(0.5f to "0.5×", 1f to "1×", 1.5f to "1.5×").forEachIndexed { i, (k, v) ->
                            SegmentedButton(selected = s.p.animSpeed == k, onClick = { s.vm.setAnimSpeed(k) }, shape = SegmentedButtonDefaults.itemShape(i, 3)) { Text(v) }
                        }
                    }
                    Text("Animations never autoplay and pause when you leave the screen.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("Offline content", Icons.Filled.OfflinePin, accent = LocalSemantic.current.success) {
                    Text("Stored on this device and available without internet:", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    listOf("${s.c.lessons.size} lessons and ${s.vm.diagrams.size} diagrams", "${s.lib.cards.size} flashcards and ${s.lib.cheatsheets.size} cheatsheets",
                        "${s.bank.questions.size} questions in ${s.bank.packs.size} packs (including imported packs)", "${s.study.glossary.size} glossary terms, ${s.study.comparisons.size} comparisons, ${s.study.mistakes.size} common mistakes",
                        "${com.ccnaacademy.app.lab.LabCatalog.all.size} CLI labs and ${com.ccnaacademy.app.lab.AnimationCatalog.all.size} step-through animations", "Your progress, notes, bookmarks, highlights and saved labs").forEach { Bullet(it, LocalSemantic.current.success) }
                    Text("Needs internet only for: optional AI tutor answers and downloading new question packs.", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 6.dp))
                }
                Spacer(Modifier.height(12.dp))
                SectionCard("Exam blueprint", Icons.Filled.Description) {
                    Text("${s.c.blueprint.exam} ${s.c.blueprint.version}")
                    Text(s.c.blueprint.note, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(6.dp))
                    s.c.domains.forEach { Text("${it.number} ${it.title} — ${it.weight}%", style = MaterialTheme.typography.bodySmall) }
                }
                Spacer(Modifier.height(12.dp))
                TutorSettingsCard(s)
                Spacer(Modifier.height(12.dp))
                BackupCard(s)
                Spacer(Modifier.height(12.dp))
                SectionCard("Data", Icons.Filled.DeleteForever, accent = LocalSemantic.current.danger) {
                    Text("Reset clears completed lessons, answers, flashcard progress, study time and exam history.", style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.padding(top = 8.dp)) { Text("Reset all progress") }
                }
                Spacer(Modifier.height(12.dp))
                Text("CCNA Academy 1.3 • Independent study app; not affiliated with or endorsed by Cisco. Cisco, CCNA and Packet Tracer are trademarks of Cisco Systems, Inc.",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    if (confirmReset) AlertDialog(
        onDismissRequest = { confirmReset = false },
        title = { Text("Reset all progress?") }, text = { Text("This cannot be undone.") },
        confirmButton = { TextButton(onClick = { s.vm.resetProgress(); confirmReset = false }) { Text("Reset") } },
        dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } }
    )
}

@Composable
private fun TutorSettingsCard(s: AppScope) {
    val cfg by s.vm.tutorConfig.collectAsStateWithLifecycle()
    var enabled by remember(cfg) { mutableStateOf(cfg.enabled) }
    var url by remember(cfg) { mutableStateOf(cfg.endpoint.ifBlank { "https://" }) }
    var model by remember(cfg) { mutableStateOf(cfg.model) }
    var key by remember(cfg) { mutableStateOf(cfg.apiKey) }
    var saved by remember { mutableStateOf(false) }
    SectionCard("AI tutor (optional)", Icons.Filled.Psychology) {
        Text("No AI service is included. The tutor works offline from the app's own content. To enable AI answers, enter an OpenAI-compatible " +
            "chat-completions endpoint, model name and your own API key. Questions you send are processed by that provider.", style = MaterialTheme.typography.bodySmall)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
            Text("Use online AI service", modifier = Modifier.weight(1f)); Switch(enabled, { enabled = it })
        }
        if (enabled) {
            OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Endpoint URL (https://…/v1/chat/completions)") })
            OutlinedTextField(model, { model = it }, Modifier.fillMaxWidth().padding(top = 6.dp), singleLine = true, label = { Text("Model name") })
            OutlinedTextField(key, { key = it }, Modifier.fillMaxWidth().padding(top = 6.dp), singleLine = true, label = { Text("API key") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation())
            Text("The key is stored only on this device, separately from backups and exports.", style = MaterialTheme.typography.labelSmall)
            if (!url.startsWith("https://")) Text("The endpoint must start with https://", color = LocalSemantic.current.danger, style = MaterialTheme.typography.labelSmall)
        }
        Button(onClick = { s.vm.saveTutorConfig(TutorConfig(enabled, url, model, key)); saved = true }, modifier = Modifier.padding(top = 8.dp)) {
            Text(if (saved) "Saved" else "Save tutor settings")
        }
    }
}

@Composable
private fun BackupCard(s: AppScope) {
    val ctx = LocalContext.current
    var msg by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<String?>(null) }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) s.vm.exportBackup { json ->
            msg = if (json == null) "Backup failed." else runCatching { ctx.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }; "Backup saved." }
                .getOrElse { "Backup failed: ${it.message}" }
        }
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pending = runCatching { ctx.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() } }.getOrNull()
            .also { if (it == null) msg = "Could not read that file." }
    }
    SectionCard("Backup & restore", Icons.Filled.Backup) {
        Text("Save all progress, notes, bookmarks, highlights and settings to a file, or restore them on a new device. " +
            "Android cloud backup is also enabled where your device supports it.", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            Button(onClick = { save.launch("ccna-academy-backup-${java.time.LocalDate.now()}.json") }) { Text("Back up") }
            OutlinedButton(onClick = { open.launch(arrayOf("application/json", "*/*")) }) { Text("Restore") }
        }
        msg?.let { Text(it, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 6.dp)) }
    }
    pending?.let { json ->
        AlertDialog(onDismissRequest = { pending = null }, title = { Text("Restore this backup?") },
            text = { Text("Your current progress on this device will be replaced by the backup.") },
            confirmButton = { TextButton(onClick = { s.vm.restoreBackup(json) { msg = it }; pending = null }) { Text("Restore") } },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancel") } })
    }
}
