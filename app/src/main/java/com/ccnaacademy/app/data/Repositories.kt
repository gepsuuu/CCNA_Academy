package com.ccnaacademy.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext


/** All learning material ships inside the APK (assets), so it is always available offline. */
class ContentRepository(private val context: Context) {
    data class Bundle(val curriculum: Curriculum, val diagrams: Map<String, Diagram>, val library: MemoryLibrary, val study: StudyContent)

    suspend fun load(): Bundle = withContext(Dispatchers.IO) {
        fun read(name: String) = context.assets.open(name).bufferedReader().use { it.readText() }
        val cur = AppJson.decodeFromString<Curriculum>(read("curriculum.json"))
        val dia = AppJson.decodeFromString<DiagramFile>(read("diagrams.json"))
        val lib = AppJson.decodeFromString<MemoryLibrary>(read("memory.json"))
        val study = AppJson.decodeFromString<StudyContent>(read("study.json"))
        Bundle(cur, dia.diagrams.associateBy { it.id }, lib, study)
    }
}

private val Context.store by preferencesDataStore(name = "ccna_progress")
private val KEY = stringPreferencesKey("progress_json")
/** If stored progress ever fails to decode, the raw JSON is preserved here instead of being overwritten. */
private val CORRUPT_KEY = stringPreferencesKey("progress_json_unreadable_backup")

class ProgressRepository(private val context: Context) {
    val progress: Flow<ProgressState> = context.store.data
        .catch { e -> if (e is java.io.IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY]?.let { decode(it) } ?: ProgressState() }

    private fun decode(raw: String): ProgressState? = runCatching { AppJson.decodeFromString<ProgressState>(raw) }.getOrNull()

    suspend fun update(transform: (ProgressState) -> ProgressState) {
        context.store.edit { prefs ->
            val raw = prefs[KEY]
            val cur = raw?.let { decode(it) }
            if (raw != null && cur == null && prefs[CORRUPT_KEY] == null) prefs[CORRUPT_KEY] = raw
            prefs[KEY] = AppJson.encodeToString(ProgressState.serializer(), transform(cur ?: ProgressState()))
        }
    }

    /** Full JSON backup of all progress, notes and settings (excluding the AI tutor API key, which is stored separately). */
    suspend fun exportJson(): String = AppJson.encodeToString(ProgressState.serializer(), progress.first())

    /** Validates a backup before replacing current data. Throws with a readable message if invalid. */
    suspend fun restoreJson(json: String) {
        val restored = runCatching { AppJson.decodeFromString<ProgressState>(json) }
            .getOrElse { throw IllegalArgumentException("This file is not a CCNA Academy backup (${it.message?.take(80)})") }
        context.store.edit { it[KEY] = AppJson.encodeToString(ProgressState.serializer(), restored) }
    }
}
