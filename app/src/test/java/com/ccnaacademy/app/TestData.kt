package com.ccnaacademy.app

import com.ccnaacademy.app.data.*
import java.io.File

/** Loads the real bundled assets exactly as the app does (unit tests run with the module directory as working dir). */
object TestData {
    private fun read(name: String) = File("src/main/assets/$name").readText()
    val curriculum: Curriculum by lazy { AppJson.decodeFromString<Curriculum>(read("curriculum.json")) }
    val diagrams: List<Diagram> by lazy { AppJson.decodeFromString<DiagramFile>(read("diagrams.json")).diagrams }
    val library: MemoryLibrary by lazy { AppJson.decodeFromString<MemoryLibrary>(read("memory.json")) }
    val study: StudyContent by lazy { AppJson.decodeFromString<StudyContent>(read("study.json")) }
    val packs: List<QuestionPack> by lazy {
        val idx = AppJson.parseToJsonElement(read("packs/index.json")).let { it as kotlinx.serialization.json.JsonObject }["packs"]
            .let { it as kotlinx.serialization.json.JsonArray }.map { it.toString().trim('"') }
        idx.map { AppJson.decodeFromString<QuestionPack>(read("packs/$it")) }
    }
    val bank: QuestionBank by lazy { QuestionBank(packs.map { LoadedPack(it, builtin = true) }) }
}
