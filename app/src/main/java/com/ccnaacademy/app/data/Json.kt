package com.ccnaacademy.app.data

import kotlinx.serialization.json.Json

/** Shared JSON configuration. Unknown keys are ignored so older/newer data files stay compatible. */
val AppJson = Json { ignoreUnknownKeys = true; encodeDefaults = true; allowStructuredMapKeys = true; explicitNulls = false }
