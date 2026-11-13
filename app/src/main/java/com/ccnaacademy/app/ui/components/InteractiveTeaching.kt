package com.ccnaacademy.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccnaacademy.app.data.*

/** Authored steps only. No live traffic, socket, or CLI engine is invoked. */
@Composable
fun PacketWalkthroughView(packet: PacketWalkthrough, diagrams: Map<String, Diagram>) {
    var dnsWarm by rememberSaveable(packet.id) { mutableStateOf(false) }
    var gatewayWarm by rememberSaveable(packet.id) { mutableStateOf(false) }
    var position by rememberSaveable(packet.id) { mutableStateOf(0) }
    var selectedDevice by rememberSaveable(packet.id) { mutableStateOf("") }
    val steps = packet.steps.filter { it.condition == "always" || (it.condition == "dnsMiss" && !dnsWarm) || (it.condition == "gatewayMiss" && !gatewayWarm) }
    if (steps.isEmpty()) { Text("No authored steps available."); return }
    val index = position.coerceIn(0, steps.lastIndex)
    val step = steps[index]
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        diagrams[packet.diagramId]?.let { DiagramCard(it) }
        Text(packet.assumptions, style = MaterialTheme.typography.bodyMedium)
        // Whole row is one toggle so TalkBack announces the label with the checkbox state (48dp+ touch target).
        if (packet.steps.any { it.condition == "dnsMiss" }) Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(dnsWarm, role = Role.Checkbox) { dnsWarm = it; position = 0 }, verticalAlignment = Alignment.CenterVertically) {
            Checkbox(dnsWarm, null); Spacer(Modifier.width(8.dp)); Text("DNS cache already contains the answer", Modifier.weight(1f))
        }
        if (packet.steps.any { it.condition == "gatewayMiss" }) Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(gatewayWarm, role = Role.Checkbox) { gatewayWarm = it; position = 0 }, verticalAlignment = Alignment.CenterVertically) {
            Checkbox(gatewayWarm, null); Spacer(Modifier.width(8.dp)); Text("Gateway MAC already cached", Modifier.weight(1f))
        }
        Text("Inspect a device", fontWeight = FontWeight.SemiBold)
        // Stacked controls remain readable at narrow widths and large font scales.
        packet.devices.forEach { device ->
            OutlinedButton(onClick = { selectedDevice = if (selectedDevice == device.name) "" else device.name }, modifier = Modifier.fillMaxWidth()) {
                Text((if (device.name == step.device) "Current: " else "") + device.name + " | " + device.`interface`)
            }
            if (selectedDevice == device.name) Text(device.details, style = MaterialTheme.typography.bodyMedium)
        }
        Text("Step ${index + 1} of ${steps.size}: ${step.title}", style = MaterialTheme.typography.titleMedium)
        Text("Device: ${step.device}\nInterface: ${step.`interface`}", fontWeight = FontWeight.SemiBold)
        Text(step.action)
        Text("Why", fontWeight = FontWeight.Bold); Text(step.why)
        Text("Header inspector", fontWeight = FontWeight.Bold)
        if (step.headers.isEmpty()) Text("No on-wire frame at this local decision step. MAC/IP header fields are not invented.")
        else SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                step.headers.forEach { (field, value) ->
                    Text(field, style = MaterialTheme.typography.labelLarge)
                    Text(value, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (index > 0 && step.headers.isNotEmpty()) {
            val previous = steps.take(index).lastOrNull { it.headers.isNotEmpty() }?.headers.orEmpty()
            val changes = step.headers.filter { (key, value) -> previous[key] != value }
            Text("Changed since the previous displayed frame", fontWeight = FontWeight.SemiBold)
            Text(changes.entries.joinToString("\n") { "${it.key}: ${previous[it.key] ?: "not present"} -> ${it.value}" }.ifBlank { "No displayed header changes." })
            Text("A comparison may cross different messages. Read the step title before treating a change as a routed-hop rewrite.", style = MaterialTheme.typography.labelSmall)
        }
        Text("Next: ${step.next}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { position = index - 1 }, enabled = index > 0, modifier = Modifier.weight(1f)) { Text("Previous") }
            Button(onClick = { position = index + 1 }, enabled = index < steps.lastIndex, modifier = Modifier.weight(1f)) { Text("Next") }
        }
        TextButton(onClick = { position = 0 }) { Text("Restart walkthrough") }
        Text("Manual step navigation: no automatic motion. This walkthrough does not change the CLI trainer state.", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun OutputInspectorView(output: OutputInspection) {
    var abnormal by rememberSaveable(output.id) { mutableStateOf(false) }
    var selected by rememberSaveable(output.id) { mutableStateOf(-1) }
    var answer by rememberSaveable(output.id) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(output.provenance, style = MaterialTheme.typography.labelMedium)
        Text("Fixture: ${output.fixture}")
        Text(output.command, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = { abnormal = !abnormal; answer = false }, modifier = Modifier.fillMaxWidth()) {
            Text(if (abnormal) "Abnormal example: show healthy" else "Healthy example: show abnormal")
        }
        Surface(color = if (abnormal) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer) {
            SelectionContainer {
                Text(if (abnormal) output.abnormal else output.healthy,
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(12.dp),
                    fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium, softWrap = false)
            }
        }
        Text("Inspect a field", fontWeight = FontWeight.SemiBold)
        output.fields.forEachIndexed { index, field ->
            OutlinedButton(onClick = { selected = if (selected == index) -1 else index }, modifier = Modifier.fillMaxWidth()) { Text(field.label) }
            if (selected == index) Text(if (abnormal) field.abnormal else field.healthy)
        }
        Text(output.question, fontWeight = FontWeight.SemiBold)
        TextButton(onClick = { answer = !answer }) { Text(if (answer) "Hide interpretation" else "Reveal interpretation") }
        if (answer) Text(output.answer)
    }
}
