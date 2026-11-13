package com.ccnaacademy.app

import com.ccnaacademy.app.data.*
import com.ccnaacademy.app.lab.*
import org.junit.Assert.*
import org.junit.Test

class LabContentTest {
    private val lessons = TestData.curriculum.lessonMap.keys
    private val comparisons = TestData.study.comparisons.map { it.id }.toSet()

    @Test fun animationsAreConsistent() {
        assertTrue(AnimationCatalog.all.size >= 12)
        AnimationCatalog.all.forEach { a ->
            val ids = a.nodes.map { it.id }.toSet()
            assertEquals("${a.id}: unique nodes", a.nodes.size, ids.size)
            a.lessonIds.forEach { assertTrue("${a.id}: lesson $it", it in lessons) }
            assertTrue("${a.id}: learning question", a.question.endsWith("?"))
            assertTrue("${a.id}: at least 5 steps", a.steps.size >= 5)
            a.edges.forEach { assertTrue("${a.id}: edge ${it.a}-${it.b}", it.a in ids && it.b in ids) }
            a.steps.forEachIndexed { i, s ->
                val where = "${a.id} step ${i + 1}"
                assertTrue("$where caption", s.title.isNotBlank() && s.text.length > 30)
                s.packets.forEach { p -> assertNotNull("$where packet ${p.from}->${p.to} needs an edge", a.edge(p.from, p.to)) }
                (s.nodes + s.badges.keys + listOfNotNull(s.drop)).forEach { assertTrue("$where node $it", it in ids) }
                s.links.keys.forEach { k -> val (x, y) = k.split("|"); assertNotNull("$where link $k", a.edge(x, y)) }
                val before = if (i == 0) a.tables else a.tablesAt(i - 1)
                s.ops.forEach { op ->
                    val t = before.firstOrNull { it.id == op.table }; assertNotNull("$where table ${op.table}", t)
                    assertEquals("$where row width", t!!.headers.size, op.row.size)
                    if (op.op != "add") assertTrue("$where update key ${op.row[0]} exists", t.rows.any { it[0] == op.row[0] })
                }
                val after = a.tablesAt(i)
                s.rows.forEach { (tid, idx) -> val t = after.first { it.id == tid }; idx.forEach { r -> assertTrue("$where row $r in $tid", r in t.rows.indices) } }
            }
            a.tables.forEach { t -> t.rows.forEach { assertEquals("${a.id}/${t.id}", t.headers.size, it.size) } }
        }
    }

    @Test fun visualLayoutsFitAndDoNotOverlap() {
        AnimationCatalog.all.forEach { a ->
            a.nodes.forEach { n -> assertTrue("${a.id}/${n.id} inside canvas", n.x >= 0 && n.y >= 0 && n.x + n.w <= 1000 && n.y + n.h <= 560) }
            a.nodes.forEachIndexed { i, p -> a.nodes.drop(i + 1).forEach { q ->
                val overlap = p.x < q.x + q.w && q.x < p.x + p.w && p.y < q.y + q.h && q.y < p.y + p.h
                assertFalse("${a.id}: ${p.id} overlaps ${q.id}", overlap) } }
        }
        LabCatalog.all.forEach { lab ->
            val ds = lab.initial().devices
            ds.forEach { d -> val w = if (d.kind == "host") 130f else 160f; val h = if (d.kind == "host") 64f else 72f
                assertTrue("${lab.id}/${d.id} inside canvas", d.x - w / 2 >= 0 && d.y - h / 2 >= 0 && d.x + w / 2 <= 1000 && d.y + h / 2 <= 560) }
            ds.forEachIndexed { i, p -> ds.drop(i + 1).forEach { q -> assertTrue("${lab.id}: ${p.id} and ${q.id} too close", kotlin.math.abs(p.x - q.x) >= 140 || kotlin.math.abs(p.y - q.y) >= 80) } }
        }
    }

    @Test fun requiredAnimationTopicsExist() {
        listOf("anim_arp_switch", "anim_vlan_trunk", "anim_intervlan", "anim_stp", "anim_etherchannel", "anim_dhcp", "anim_dhcp_relay",
            "anim_routing_lpm", "anim_ospf", "anim_pat", "anim_acl", "anim_encap").forEach { assertNotNull(it, AnimationCatalog.byId[it]) }
    }

    @Test fun lessonExtrasAreValid() {
        LessonExtrasCatalog.all.values.forEach { x ->
            assertTrue("lesson ${x.lessonId}", x.lessonId in lessons)
            x.comparisons.forEach { assertTrue("${x.lessonId}: comparison $it", it in comparisons) }
            x.tables.forEach { t ->
                t.rows.forEach { assertEquals("${t.id} row width", t.headers.size, it.size) }
                assertTrue("${t.id}: notes empty or one per row", t.notes.isEmpty() || t.notes.size == t.rows.size)
                assertTrue(t.kind in setOf("reference", "demo", "troubleshoot"))
            }
            x.walkthroughs.forEach { w -> w.steps.forEach { assertTrue(it.purpose.isNotBlank() && it.verify.isNotBlank() && it.mistake.isNotBlank()) } }
            x.teachBack?.let { assertTrue(it.checklist.size >= 2 && it.model.isNotBlank()) }
        }
        LabCatalog.all.forEach { l -> l.lessonIds.forEach { assertTrue("${l.id}: lesson $it", it in lessons) } }
    }

    @Test fun everyLessonHasAtLeastOneV14Activity() {
        TestData.curriculum.lessons.forEach { l ->
            val x = LessonExtrasCatalog.forLesson(l.id)
            assertTrue("${l.id} has a table, comparison, animation or lab", x.tables.isNotEmpty() || x.comparisons.isNotEmpty() || x.animations.isNotEmpty() || x.labs.isNotEmpty())
        }
        val ids = Regex("LessonExtras\\(\"([0-9]-[0-9]+)\"").findAll(java.io.File("src/main/java/com/ccnaacademy/app/lab/LessonExtras.kt").readText()).map { it.groupValues[1] }.toList()
        assertEquals("no lesson is defined twice", ids.size, ids.toSet().size)
    }

    @Test fun configurationLessonsHavePracticeOrAReason() {
        val configLessons = listOf("2-01", "2-02", "2-03", "2-05", "2-06", "2-09", "3-02", "3-03", "4-01", "4-02", "5-03", "5-04")
        configLessons.forEach { id ->
            val x = LessonExtrasCatalog.forLesson(id)
            assertTrue("$id needs a lab, an animation+reason, or an explicit reason", x.labs.isNotEmpty() || x.noLabReason != null)
        }
    }

    @Test fun subnetTableIsComputedCorrectly() {
        val t = LessonExtrasCatalog.forLesson("1-09").tables.first { it.id == "t-prefix" }
        val r26 = t.rows.first { it[0] == "/26" }
        assertEquals(listOf("/26", "255.255.255.192", "64 (4th octet)", "62"), r26)
        assertEquals(listOf("/22", "255.255.252.0", "4 (3rd octet)", "1022"), t.rows.first { it[0] == "/22" })
        assertEquals("255.255.255.252", t.rows.first { it[0] == "/30" }[1]); assertEquals("2", t.rows.first { it[0] == "/30" }[3])
    }

    @Test fun outputActivitiesMatchTheTrainer() {
        assertTrue(LessonExtrasCatalog.allOutputs.size >= 5)
        LessonExtrasCatalog.allOutputs.forEach { o ->
            val text = o.render().joinToString("\n")
            assertTrue("${o.id}: output", text.isNotBlank())
            o.annotations.forEach { (needle, _) -> assertTrue("${o.id}: annotated text '$needle' must appear in real output:\n$text", text.contains(needle)) }
            assertTrue(o.answer in o.options.indices)
        }
    }

    @Test fun v13ProgressMigratesAndLabSavesRoundTrip() {
        val v13 = """{"completedLessons":{"2-01":20400},"themeMode":"light","notes":[{"id":"n1","title":"t","body":"b","created":1,"updated":2}],
            "bankStats":{"q1":{"domainId":"d2","topic":"VLAN","lessonId":"2-01","correct":1}},"planner":{"examDay":20500},"unknownFutureKey":true}"""
        val p = AppJson.decodeFromString<ProgressState>(v13)
        assertEquals("light", p.themeMode); assertEquals(1, p.notes.size); assertEquals(1, p.bankStats["q1"]!!.correct)
        assertTrue(p.labs.isEmpty()); assertTrue(p.skills.isEmpty()); assertFalse(p.reducedMotion); assertEquals(1f, p.animSpeed)
        val e = CliEngine(LabCatalog.vlanGuided.initial()); e.run("SW1", "enable"); e.run("SW1", "conf t"); e.run("SW1", "vlan 10")
        val save = LabSave(e.state, mapOf("SW1" to listOf(TranscriptLine("SW1#conf t", "echo"))), "SW1", mapOf("v-sw1" to 2), started = 5)
        val withLab = p.copy(labs = mapOf("vlan-trunk" to save), skills = mapOf("cli" to QuestionStat(3, 1)),
            labMistakes = listOf(LabMistake(1, "vlan-trunk", "SW1", "sh vlan", "% Invalid", "use do")))
        val back = AppJson.decodeFromString<ProgressState>(AppJson.encodeToString(ProgressState.serializer(), withLab))
        assertEquals(withLab, back)
        assertEquals("SW1(config-vlan)#", back.labs["vlan-trunk"]!!.state.dev("SW1").prompt)
        assertEquals(2, back.labs["vlan-trunk"]!!.hintsUsed)
    }
}
