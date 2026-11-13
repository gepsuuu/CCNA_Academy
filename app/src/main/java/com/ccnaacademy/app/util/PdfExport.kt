package com.ccnaacademy.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.ccnaacademy.app.data.Block
import com.ccnaacademy.app.data.Cheatsheet
import com.ccnaacademy.app.data.Diagram
import java.io.File
import java.io.OutputStream

/** Renders cheatsheets to an A4 PDF using only the Android framework (no external libraries). */
object PdfExport {
    private const val W = 595; private const val H = 842; private const val M = 40f
    private val contentW get() = W - 2 * M

    private class Writer(val doc: PdfDocument, val footer: String) {
        var pageNo = 0
        lateinit var page: PdfDocument.Page
        var y = M
        val canvas get() = page.canvas
        fun newPage() {
            if (pageNo > 0) finishPage()
            pageNo++
            page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
            y = M
        }
        fun finishPage() {
            val p = TextPaint().apply { textSize = 8f; color = Color.GRAY; isAntiAlias = true }
            canvas.drawText("$footer — page $pageNo", M, H - 20f, p)
            doc.finishPage(page)
        }
        fun ensure(h: Float) { if (y + h > H - M - 10) newPage() }
    }

    private fun layout(text: String, paint: TextPaint, width: Float): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt().coerceAtLeast(10))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(1f, 1.1f).build()

    private fun Writer.draw(text: String, paint: TextPaint, x: Float = M, width: Float = contentW, gapAfter: Float = 4f, bg: Int? = null) {
        val l = layout(text, paint, width)
        ensure(l.height.toFloat())
        if (bg != null) canvas.drawRect(x - 2, y - 1, x + width + 2, y + l.height + 1, Paint().apply { color = bg })
        canvas.save(); canvas.translate(x, y); l.draw(canvas); canvas.restore()
        y += l.height + gapAfter
    }

    private val title = TextPaint().apply { textSize = 20f; typeface = Typeface.DEFAULT_BOLD; color = Color.rgb(11, 92, 173); isAntiAlias = true }
    private val h2 = TextPaint().apply { textSize = 13f; typeface = Typeface.DEFAULT_BOLD; color = Color.rgb(20, 20, 20); isAntiAlias = true }
    private val body = TextPaint().apply { textSize = 10f; color = Color.rgb(30, 30, 30); isAntiAlias = true }
    private val bold = TextPaint(body).apply { typeface = Typeface.DEFAULT_BOLD }
    private val mono = TextPaint(body).apply { typeface = Typeface.MONOSPACE; textSize = 9f }
    private val small = TextPaint(body).apply { textSize = 8.5f; color = Color.DKGRAY }
    private val HL = Color.argb(110, 255, 213, 79)

    fun write(sheets: List<Cheatsheet>, highlights: Set<String>, diagrams: Map<String, Diagram>, out: OutputStream) {
        val doc = PdfDocument()
        val w = Writer(doc, "CCNA Academy cheatsheets")
        w.newPage()
        sheets.forEachIndexed { si, s ->
            if (si > 0) w.newPage()
            w.draw(s.title, title, gapAfter = 2f)
            w.draw("Highlighted rows are items you marked as important.", small, gapAfter = 10f)
            s.blocks.forEachIndexed { bi, b -> w.block(s.id, bi, b, highlights, diagrams) }
        }
        w.finishPage()
        doc.writeTo(out); doc.close()
    }

    private fun Writer.block(sheetId: String, bi: Int, b: Block, hl: Set<String>, diagrams: Map<String, Diagram>) {
        fun marked(i: Int) = "$sheetId:$bi:$i" in hl
        ensure(40f)
        if (b.title.isNotBlank()) draw(b.title, h2, gapAfter = 4f)
        when (b.type) {
            "text", "visual", "callout" -> draw(b.text, body, gapAfter = 8f)
            "bullets" -> { b.items.forEachIndexed { i, t -> draw("•  $t", body, x = M + 6, width = contentW - 6, gapAfter = 2f, bg = if (marked(i)) HL else null) }; y += 6 }
            "mnemonic" -> { draw("“${b.mnemonic}”", bold); b.items.forEachIndexed { i, t -> draw("•  $t", body, x = M + 6, width = contentW - 6, gapAfter = 2f, bg = if (marked(i)) HL else null) }; y += 6 }
            "code" -> { b.items.forEachIndexed { i, t -> draw(t, mono, x = M + 8, width = contentW - 8, gapAfter = 1f, bg = if (marked(i)) HL else Color.rgb(242, 244, 247)) }; y += 8 }
            "chunks" -> { b.rows.forEachIndexed { i, r -> draw("${r.getOrElse(0) { "" }}: ${r.getOrElse(1) { "" }}", body, gapAfter = 3f, bg = if (marked(i)) HL else null) }; y += 6 }
            "table" -> table(b, ::marked)
            "diagram" -> draw("Diagram: ${diagrams[b.diagram]?.title ?: b.diagram} — ${diagrams[b.diagram]?.explanation ?: ""} (interactive version in the app)", small, gapAfter = 8f)
        }
        if (b.type in listOf("table", "code", "chunks", "mnemonic") && b.text.isNotBlank()) draw(b.text, small, gapAfter = 8f)
    }

    private fun Writer.table(b: Block, marked: (Int) -> Boolean) {
        val cols = b.headers.size.coerceAtLeast(1)
        val weights = (0 until cols).map { c -> (listOf(b.headers.getOrElse(c) { "" }) + b.rows.map { it.getOrElse(c) { "" } }).maxOf { it.length }.coerceIn(4, 40).toFloat() }
        val total = weights.sum()
        val widths = weights.map { it / total * contentW }
        val border = Paint().apply { style = Paint.Style.STROKE; color = Color.rgb(190, 196, 204); strokeWidth = 0.6f }
        fun row(cells: List<String>, paint: TextPaint, fill: Int?) {
            val layouts = (0 until cols).map { c -> layout(cells.getOrElse(c) { "" }, paint, widths[c] - 8) }
            val h = layouts.maxOf { it.height } + 8f
            ensure(h)
            var x = M
            if (fill != null) canvas.drawRect(M, y, M + contentW, y + h, Paint().apply { color = fill })
            layouts.forEachIndexed { c, l ->
                canvas.drawRect(x, y, x + widths[c], y + h, border)
                canvas.save(); canvas.translate(x + 4, y + 4); l.draw(canvas); canvas.restore()
                x += widths[c]
            }
            y += h
        }
        row(b.headers, bold, Color.rgb(213, 228, 255))
        b.rows.forEachIndexed { i, r -> row(r, body, if (marked(i)) HL else null) }
        y += 8
    }

    /** Writes a PDF into the app cache and returns a shareable content URI. */
    fun shareUri(context: Context, name: String, sheets: List<Cheatsheet>, highlights: Set<String>, diagrams: Map<String, Diagram>): Uri {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "$name.pdf")
        file.outputStream().use { write(sheets, highlights, diagrams, it) }
        return FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    }

    fun share(context: Context, uri: Uri, title: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share cheatsheet PDF").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
