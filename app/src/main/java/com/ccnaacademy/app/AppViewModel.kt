package com.ccnaacademy.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ccnaacademy.app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val content = ContentRepository(app)
    private val repo = ProgressRepository(app)

    private val _curriculum = MutableStateFlow<Curriculum?>(null)
    val curriculum: StateFlow<Curriculum?> = _curriculum
    var diagrams: Map<String, Diagram> = emptyMap(); private set
    var library: MemoryLibrary? = null; private set
    var study: StudyContent? = null; private set
    private val tutorRepo = TutorRepository(app)
    val tutorConfig: StateFlow<TutorConfig> = tutorRepo.config.stateIn(viewModelScope, SharingStarted.Eagerly, TutorConfig())
    /** Set when bundled content fails to load, so the UI can show a clear error instead of crashing. */
    val loadError = MutableStateFlow<String?>(null)

    val progress: StateFlow<ProgressState> = repo.progress.stateIn(viewModelScope, SharingStarted.Eagerly, ProgressState())
    /** True once stored progress has been read, so screens that restore saved state (CLI labs) never start from an empty default. */
    val progressReady: StateFlow<Boolean> = repo.progress.map { true }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val packs = PackRepository(app)
    private val _bank = MutableStateFlow<QuestionBank?>(null)
    /** All installed question packs (enabled/disabled filtering is applied in the UI from ProgressState). */
    val bank: StateFlow<QuestionBank?> = _bank

    init {
        viewModelScope.launch {
            runCatching {
                val b = content.load()
                diagrams = b.diagrams
                library = b.library
                study = b.study
                _bank.value = QuestionBank(packs.loadAll())
                _curriculum.value = b.curriculum
            }.onFailure { loadError.value = "Study content could not be loaded (${it.javaClass.simpleName}: ${it.message?.take(160)}). Please reinstall the app." }
        }
    }

    private fun edit(t: (ProgressState) -> ProgressState) = viewModelScope.launch { repo.update(t) }
    private fun ProgressState.markDone(key: String): ProgressState {
        val t = today()
        return copy(dailyDone = (dailyDone + (t to ((dailyDone[t] ?: emptySet()) + key))).filterKeys { it >= t - 30 })
    }

    // ---------- Lessons ----------
    fun openLesson(id: String) = edit { it.copy(lastLessonId = id) }
    fun completeLesson(id: String) = edit { p ->
        if (id in p.completedLessons) p else p.copy(
            completedLessons = p.completedLessons + (id to today()),
            reviews = p.reviews + (id to ReviewState(0, today() + REVIEW_INTERVALS[0]))
        )
    }
    fun uncompleteLesson(id: String) = edit { p -> p.copy(completedLessons = p.completedLessons - id, reviews = p.reviews - id) }
    fun markReviewed(id: String) = edit { p ->
        val r = p.reviews[id] ?: ReviewState()
        val next = r.stage + 1
        val base = if (next >= REVIEW_INTERVALS.size) p.copy(reviews = p.reviews - id)
        else p.copy(reviews = p.reviews + (id to ReviewState(next, today() + REVIEW_INTERVALS[next])))
        base.markDone("lesson:$id")
    }
    fun lessonCheckDone(id: String) = edit { it.markDone("lesson:$id") }

    // ---------- Questions ----------
    fun recordAnswer(q: Question, correct: Boolean) = edit { p ->
        val s = p.answers[q.id] ?: QuestionStat()
        val ns = if (correct) s.copy(correct = s.correct + 1, lastCorrect = true) else s.copy(incorrect = s.incorrect + 1, lastCorrect = false)
        p.copy(answers = p.answers + (q.id to ns))
    }
    fun saveExam(r: ExamResult) = edit { p -> p.copy(exams = (p.exams + r).takeLast(100)) }
    fun dailyQuizDone() = edit { it.markDone("quiz") }

    // ---------- Flashcards ----------
    fun rateCard(id: String, rating: Rating) = edit { p ->
        val t = today()
        val old = p.srs[id] ?: SrsState()
        val intro = if (old.isNew) p.newIntroduced + (t to ((p.newIntroduced[t] ?: 0) + 1)) else p.newIntroduced
        p.copy(srs = p.srs + (id to Srs.next(old, rating, t)), newIntroduced = intro.filterKeys { it >= t - 60 },
            reviewsByDay = (p.reviewsByDay + (t to ((p.reviewsByDay[t] ?: 0) + 1))).filterKeys { it >= t - 365 })
    }
    fun toggleFavorite(id: String) = edit { p -> p.copy(favorites = if (id in p.favorites) p.favorites - id else p.favorites + id) }
    fun setDifficulty(id: String, d: String?) = edit { p -> p.copy(userDifficulty = if (d == null) p.userDifficulty - id else p.userDifficulty + (id to d)) }
    fun setNewPerDay(n: Int) = edit { it.copy(newPerDay = n, plan = null) }
    fun createDeck(name: String, cards: List<String> = emptyList()) = edit { p ->
        p.copy(customDecks = p.customDecks + CustomDeck("deck-${System.currentTimeMillis()}", name.ifBlank { "My deck" }, cards))
    }
    fun renameDeck(id: String, name: String) = edit { p -> p.copy(customDecks = p.customDecks.map { if (it.id == id) it.copy(name = name) else it }) }
    fun deleteDeck(id: String) = edit { p -> p.copy(customDecks = p.customDecks.filterNot { it.id == id }) }
    fun toggleCardInDeck(deckId: String, cardId: String) = edit { p ->
        p.copy(customDecks = p.customDecks.map { d ->
            if (d.id != deckId) d else d.copy(cardIds = if (cardId in d.cardIds) d.cardIds - cardId else d.cardIds + cardId)
        })
    }

    // ---------- Memorization / cheatsheets / drills ----------
    fun visitGuide(id: String) = edit { p -> p.copy(guideVisits = p.guideVisits + (id to today())).markDone("guide:$id") }
    fun recordRecall(key: String, correct: Boolean) = edit { p ->
        val s = p.recallStats[key] ?: QuestionStat()
        p.copy(recallStats = p.recallStats + (key to if (correct) s.copy(correct = s.correct + 1, lastCorrect = true) else s.copy(incorrect = s.incorrect + 1, lastCorrect = false)))
    }
    fun saveChallenge(percent: Int) = edit { p -> p.copy(challengeScores = p.challengeScores + (today() to percent)).markDone("challenge") }
    fun toggleBookmark(id: String) = edit { p -> p.copy(sheetBookmarks = if (id in p.sheetBookmarks) p.sheetBookmarks - id else p.sheetBookmarks + id) }
    fun toggleHighlight(key: String) = edit { p -> p.copy(highlights = if (key in p.highlights) p.highlights - key else p.highlights + key) }
    fun sheetReviewed(id: String) = edit { it.markDone("sheet:$id") }
    fun recordGen(type: String, correct: Boolean) = edit { p ->
        val s = p.genStats[type] ?: QuestionStat()
        p.copy(genStats = p.genStats + (type to if (correct) s.copy(correct = s.correct + 1) else s.copy(incorrect = s.incorrect + 1)))
    }
    fun saveDrill(r: DrillResult) = edit { p ->
        val base = p.copy(drills = (p.drills + r).takeLast(300))
        if (r.kind == "speed") base.markDone("speed:${r.refId}") else base
    }

    // ---------- Daily plan ----------
    fun ensurePlan() {
        val c = _curriculum.value ?: return
        val lib = library ?: return
        if (progress.value.plan?.day == today()) return
        edit { p -> if (p.plan?.day == today()) p else p.copy(plan = DailyPlanner.build(c, lib, p)) }
    }
    fun regeneratePlan() { val c = _curriculum.value ?: return; val lib = library ?: return; edit { p -> p.copy(plan = DailyPlanner.build(c, lib, p)) } }

    // ---------- Question bank ----------
    fun recordBank(q: BankQuestion, correct: Boolean, ms: Long) = edit { p ->
        val s = p.bankStats[q.id] ?: BankStat(q.domainId, q.topic, q.lessonId)
        val ns = s.copy(domainId = q.domainId, topic = q.topic, lessonId = q.lessonId,
            correct = s.correct + if (correct) 1 else 0, incorrect = s.incorrect + if (correct) 0 else 1,
            lastCorrect = correct, lastTs = System.currentTimeMillis(), totalMs = s.totalMs + ms.coerceIn(0, 30 * 60_000))
        p.copy(bankStats = p.bankStats + (q.id to ns))
    }
    fun toggleBankBookmark(id: String) = edit { p -> p.copy(bankBookmarks = if (id in p.bankBookmarks) p.bankBookmarks - id else p.bankBookmarks + id) }
    fun saveSession(r: SessionResult) = edit { p ->
        val withExam = if (r.exam) p.copy(exams = (p.exams + ExamResult(r.id, r.total, r.correct, r.seconds, r.domainCorrect, r.domainTotal)).takeLast(100)) else p
        val base = withExam.copy(sessions = (withExam.sessions + r).takeLast(200))
        if (r.mode == "daily") base.markDone("quiz") else base
    }
    fun setPackEnabled(id: String, enabled: Boolean) = edit { p -> p.copy(disabledPacks = if (enabled) p.disabledPacks - id else p.disabledPacks + id) }

    /** Installs a pack from JSON text (file import or download) and reloads the bank. */
    fun installPack(source: suspend (PackRepository) -> String, onDone: (String) -> Unit) = viewModelScope.launch {
        val msg = runCatching {
            val text = source(packs)
            packs.install(text, _bank.value?.packs ?: emptyList())
        }.fold({ it }, { "Import failed: ${it.message}" })
        _bank.value = QuestionBank(packs.loadAll())
        onDone(msg)
    }
    fun importPack(uri: android.net.Uri, onDone: (String) -> Unit) = installPack({ it.readUri(uri) }, onDone)
    fun downloadPack(url: String, onDone: (String) -> Unit) = installPack({ it.download(url.trim()) }, onDone)
    fun deletePack(id: String) = viewModelScope.launch {
        packs.delete(id)
        _bank.value = QuestionBank(packs.loadAll())
        edit { p -> p.copy(disabledPacks = p.disabledPacks - id) }
    }

    // ---------- Planner ----------
    fun savePlanner(ps: PlannerSettings) = edit { it.copy(planner = ps.copy(createdDay = if (ps.createdDay == 0L) today() else ps.createdDay)) }
    fun clearPlanner() = edit { it.copy(planner = null) }

    // ---------- Notes, bookmarks, highlights ----------
    fun saveNote(n: Note) = edit { p -> p.copy(notes = if (p.notes.any { it.id == n.id }) p.notes.map { if (it.id == n.id) n else it } else p.notes + n) }
    fun deleteNote(id: String) = edit { p -> p.copy(notes = p.notes.filterNot { it.id == id }) }
    fun toggleLessonBookmark(id: String) = edit { p -> p.copy(lessonBookmarks = p.lessonBookmarks.toggle(id)) }
    fun toggleDiagramBookmark(id: String) = edit { p -> p.copy(diagramBookmarks = p.diagramBookmarks.toggle(id)) }
    fun toggleLessonHighlight(key: String) = edit { p -> p.copy(lessonHighlights = p.lessonHighlights.toggle(key)) }
    private fun Set<String>.toggle(x: String) = if (x in this) this - x else this + x

    // ---------- Daily challenge, concepts, badges ----------
    fun challengePart(part: String) = edit { p ->
        val t = today()
        p.copy(challengeDone = (p.challengeDone + (t to ((p.challengeDone[t] ?: emptySet()) + part))).filterKeys { it >= t - 400 })
    }
    fun memorizeConcept(id: String) = edit { p -> p.copy(conceptsMemorized = p.conceptsMemorized + id) }
    /** Records the first time each badge was earned (badges themselves are always computed from real progress). */
    fun syncBadges(earned: List<String>) = edit { p ->
        val missing = earned.filter { it !in p.badges }
        if (missing.isEmpty()) p else p.copy(badges = p.badges + missing.associateWith { System.currentTimeMillis() })
    }

    // ---------- CLI labs, activities, skills (v1.4) ----------
    /** Saves lab state. Completion history is never cleared by a later save (or by resetting the lab). */
    fun saveLab(id: String, save: LabSave) = edit { p ->
        val prev = p.labs[id]
        p.copy(labs = p.labs + (id to save.copy(completedAt = save.completedAt ?: prev?.completedAt, completedWithHints = save.completedWithHints ?: prev?.completedWithHints)))
    }
    fun resetLab(id: String) = edit { p -> p.copy(labs = p.labs - id) }
    /** Called once when every task passes. Guided completion and independent mastery are recorded separately via the lab kind. */
    fun completeLab(id: String, save: LabSave, skills: List<String>) = edit { p ->
        if (p.labs[id]?.completedAt != null) p.copy(labs = p.labs + (id to save.copy(completedAt = p.labs[id]?.completedAt, completedWithHints = minOf(p.labs[id]?.completedWithHints ?: Int.MAX_VALUE, save.hintsUsed))))
        else skills.fold(p.copy(labs = p.labs + (id to save.copy(completedAt = System.currentTimeMillis(), completedWithHints = save.hintsUsed)))) { acc, sk -> acc.skill(sk, true) }
    }
    fun recordLabMistake(m: LabMistake) = edit { p -> p.copy(labMistakes = (p.labMistakes + m).takeLast(200)).skill("cli", false) }
    fun markMistakeFixed(ts: Long) = edit { p -> p.copy(labMistakes = p.labMistakes.map { if (it.ts == ts) it.copy(fixed = true) else it }) }
    fun clearFixedMistakes() = edit { p -> p.copy(labMistakes = p.labMistakes.filterNot { it.fixed }) }
    fun recordActivity(id: String, skill: String, correct: Boolean) = edit { p ->
        val st = p.activities[id] ?: QuestionStat()
        p.copy(activities = p.activities + (id to if (correct) st.copy(correct = st.correct + 1, lastCorrect = true) else st.copy(incorrect = st.incorrect + 1, lastCorrect = false))).skill(skill, correct)
    }
    private fun ProgressState.skill(k: String, correct: Boolean): ProgressState {
        val st = skills[k] ?: QuestionStat()
        return copy(skills = skills + (k to if (correct) st.copy(correct = st.correct + 1, lastCorrect = true) else st.copy(incorrect = st.incorrect + 1, lastCorrect = false)))
    }
    fun setReducedMotion(on: Boolean) = edit { it.copy(reducedMotion = on) }
    fun setAnimSpeed(v: Float) = edit { it.copy(animSpeed = v) }

    // ---------- Tutor ----------
    fun saveTutorConfig(c: TutorConfig) = viewModelScope.launch { tutorRepo.save(c) }
    fun askTutor(mode: TutorMode, input: String, level: Int, onResult: (TutorAnswer) -> Unit) = viewModelScope.launch {
        val c = _curriculum.value ?: return@launch; val s = study ?: return@launch
        val lib = library ?: return@launch; val bank = _bank.value?.withDisabled(progress.value.disabledPacks) ?: return@launch
        val cfg = tutorConfig.value
        val offline = OfflineTutor.answer(mode, input, level, c, s, lib, bank, progress.value)
        if (!cfg.usable || mode == TutorMode.SUBNET) { onResult(offline); return@launch }   // subnetting is always computed exactly on-device
        val res = runCatching { tutorRepo.ask(cfg, OfflineTutor.SYSTEM, OfflineTutor.onlinePrompt(mode, input, level, c, s, bank, progress.value)) }
        onResult(res.fold(
            { TutorAnswer("AI tutor (${cfg.host})", listOf("" to it), true, offline.sources) },
            { offline.copy(title = "AI unavailable — offline answer", sections = listOf("Note" to "${it.message ?: "Network error"} Showing offline knowledge base content instead.") + offline.sections) }))
    }

    // ---------- Backup & restore ----------
    fun exportBackup(onDone: (String?) -> Unit) = viewModelScope.launch { onDone(runCatching { repo.exportJson() }.getOrNull()) }
    fun restoreBackup(json: String, onDone: (String) -> Unit) = viewModelScope.launch {
        onDone(runCatching { repo.restoreJson(json); "Backup restored." }.getOrElse { "Restore failed: ${it.message}" })
    }

    // ---------- Misc ----------
    fun addStudyTime(seconds: Long) {
        if (seconds <= 0) return
        val s = seconds.coerceAtMost(45 * 60)
        edit { p -> p.copy(studySecondsByDay = p.studySecondsByDay + (today() to ((p.studySecondsByDay[today()] ?: 0L) + s))) }
    }
    fun setTheme(mode: String) = edit { it.copy(themeMode = mode) }
    fun setDynamic(on: Boolean) = edit { it.copy(dynamicColor = on) }
    fun resetProgress() = edit { ProgressState(themeMode = it.themeMode, dynamicColor = it.dynamicColor, reducedMotion = it.reducedMotion, animSpeed = it.animSpeed) }
}
