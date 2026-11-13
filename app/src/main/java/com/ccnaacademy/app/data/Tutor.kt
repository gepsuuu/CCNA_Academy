package com.ccnaacademy.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Optional online AI tutor. The app ships with NO AI service and NO API key. If the student configures an
 * OpenAI-compatible chat-completions endpoint and key, requests go there; otherwise the tutor answers from the
 * offline knowledge base and labels every answer as offline content.
 */
data class TutorConfig(val enabled: Boolean = false, val endpoint: String = "", val model: String = "", val apiKey: String = "") {
    val usable get() = enabled && endpoint.startsWith("https://") && model.isNotBlank() && apiKey.isNotBlank()
    val host get() = runCatching { URL(endpoint).host }.getOrDefault("")
}

// Separate store so the API key is never included in progress backups or exports (excluded in backup rules too).
private val Context.tutorStore by preferencesDataStore(name = "tutor_settings")
private val K_ON = booleanPreferencesKey("enabled"); private val K_URL = stringPreferencesKey("endpoint")
private val K_MODEL = stringPreferencesKey("model"); private val K_KEY = stringPreferencesKey("api_key")

class TutorRepository(private val context: Context) {
    val config: Flow<TutorConfig> = context.tutorStore.data.map { TutorConfig(it[K_ON] ?: false, it[K_URL] ?: "", it[K_MODEL] ?: "", it[K_KEY] ?: "") }
    suspend fun save(c: TutorConfig) = context.tutorStore.edit { it[K_ON] = c.enabled; it[K_URL] = c.endpoint.trim(); it[K_MODEL] = c.model.trim(); it[K_KEY] = c.apiKey.trim() }

    suspend fun ask(c: TutorConfig, system: String, user: String): String = withContext(Dispatchers.IO) {
        val body = buildJsonObject {
            put("model", c.model); put("temperature", 0.2)
            putJsonArray("messages") {
                addJsonObject { put("role", "system"); put("content", system) }
                addJsonObject { put("role", "user"); put("content", user) }
            }
        }.toString()
        val conn = URL(c.endpoint).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"; conn.doOutput = true; conn.connectTimeout = 15_000; conn.readTimeout = 60_000
            conn.setRequestProperty("Content-Type", "application/json"); conn.setRequestProperty("Authorization", "Bearer ${c.apiKey}")
            conn.outputStream.use { it.write(body.toByteArray()) }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() } ?: ""
            if (code !in 200..299) throw IllegalStateException(when (code) {
                401, 403 -> "The AI service rejected the API key (HTTP $code)."
                404 -> "Endpoint or model not found (HTTP 404). Check the URL and model name."
                429 -> "The AI service is rate-limiting requests (HTTP 429). Try again later."
                else -> "AI service error (HTTP $code)."
            })
            val root = AppJson.parseToJsonElement(text).jsonObject
            root["choices"]?.jsonArray?.firstOrNull()?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.contentOrNull
                ?: throw IllegalStateException("Unexpected response format from the AI service.")
        } finally { conn.disconnect() }
    }
}
