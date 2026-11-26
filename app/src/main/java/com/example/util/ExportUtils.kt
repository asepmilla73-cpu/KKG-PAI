package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.KopSuratConfig
import com.example.data.model.LetterDraft
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExportUtils {
    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842
    private const val MARGIN_X = 48f
    private const val MARGIN_TOP = 42f
    private const val MARGIN_BOTTOM = 46f

    suspend fun generateFinancePdf(
        kop: KopSuratConfig,
        transactions: List<TransactionEntity>,
        filterLabel: String
    ): ByteArray = withContext(Dispatchers.Default) {
        val doc = PdfDocument()
        var pageNum = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, pageNum).create())
        var canvas = page.canvas

        val contentWidth = A4_WIDTH - (MARGIN_X * 2)
        var y = MARGIN_TOP

        // Draw Kop Header on first page
        y = drawKopSuratPdf(canvas, kop, y, contentWidth, serif = false)
        y += 14f

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#065F46")
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("LAPORAN KEUANGAN KAS KKG PAI", A4_WIDTH / 2f, y, titlePaint)
        y += 16f

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Periode: $filterLabel  •  Dicetak: ${FormatUtils.todayIndonesianFull()}", A4_WIDTH / 2f, y, subPaint)
        y += 18f

        val totalIncome = transactions.filter { it.type == TransactionType.INCOME.code }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE.code }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        // Summary box
        val boxBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#ECFDF5") }
        val boxStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#059669")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(MARGIN_X, y, MARGIN_X + contentWidth, y + 48f, 6f, 6f, boxBg)
        canvas.drawRoundRect(MARGIN_X, y, MARGIN_X + contentWidth, y + 48f, 6f, 6f, boxStroke)

        val colW = contentWidth / 3f
        drawSummaryCell(canvas, "Total Pemasukan", FormatUtils.formatRupiah(totalIncome), MARGIN_X + 10f, y + 17f, Color.parseColor("#059669"))
        drawSummaryCell(canvas, "Total Pengeluaran", FormatUtils.formatRupiah(totalExpense), MARGIN_X + colW + 10f, y + 17f, Color.parseColor("#DC2626"))
        drawSummaryCell(canvas, "Saldo Akhir Kas", FormatUtils.formatRupiah(balance), MARGIN_X + colW * 2f + 10f, y + 17f, Color.parseColor("#0F172A"))
        y += 64f

        fun drawTableHeader(c: Canvas, topY: Float): Float {
            val headerPaint = Paint().apply { color = Color.parseColor("#059669") }
            c.drawRect(MARGIN_X, topY, MARGIN_X + contentWidth, topY + 24f, headerPaint)
            val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 9.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            c.drawText("No", MARGIN_X + 6f, topY + 16f, thPaint)
            c.drawText("Tanggal", MARGIN_X + 30f, topY + 16f, thPaint)
            c.drawText("Kategori & Keterangan", MARGIN_X + 105f, topY + 16f, thPaint)
            thPaint.textAlign = Paint.Align.RIGHT
            c.drawText("Masuk (Rp)", MARGIN_X + contentWidth - 95f, topY + 16f, thPaint)
            c.drawText("Keluar (Rp)", MARGIN_X + contentWidth - 6f, topY + 16f, thPaint)
            return topY + 24f
        }

        y = drawTableHeader(canvas, y)

        val rowStroke = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 0.6f
        }
        val altBg = Paint().apply { color = Color.parseColor("#F8FAFC") }
        val cellText = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        transactions.forEachIndexed { index, tx ->
            val descText = "[${tx.category}] ${tx.description}"
            val descLayout = StaticLayout.Builder
                .obtain(descText, 0, descText.length, cellText, (contentWidth - 295f).toInt().coerceAtLeast(100))
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1f, 1.15f)
                .build()
            val rowHeight = (descLayout.height + 12f).coerceAtLeast(24f)

            if (y + rowHeight > A4_HEIGHT - MARGIN_BOTTOM - 90f) {
                doc.finishPage(page)
                pageNum++
                page = doc.startPage(PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, pageNum).create())
                canvas = page.canvas
                y = MARGIN_TOP
                y = drawTableHeader(canvas, y)
            }

            if (index % 2 == 1) {
                canvas.drawRect(MARGIN_X, y, MARGIN_X + contentWidth, y + rowHeight, altBg)
            }
            canvas.drawLine(MARGIN_X, y + rowHeight, MARGIN_X + contentWidth, y + rowHeight, rowStroke)

            val baseline = y + 15f
            canvas.drawText("${index + 1}", MARGIN_X + 6f, baseline, cellText)
            canvas.drawText(FormatUtils.formatIsoDateShort(tx.date), MARGIN_X + 30f, baseline, cellText)

            canvas.save()
            canvas.translate(MARGIN_X + 105f, y + 6f)
            descLayout.draw(canvas)
            canvas.restore()

            val isIncome = tx.type == TransactionType.INCOME.code
            if (isIncome) {
                numPaint.color = Color.parseColor("#059669")
                canvas.drawText(FormatUtils.formatRupiah(tx.amount).removePrefix("Rp "), MARGIN_X + contentWidth - 95f, baseline, numPaint)
                numPaint.color = Color.parseColor("#94A3B8")
                canvas.drawText("-", MARGIN_X + contentWidth - 6f, baseline, numPaint)
            } else {
                numPaint.color = Color.parseColor("#94A3B8")
                canvas.drawText("-", MARGIN_X + contentWidth - 95f, baseline, numPaint)
                numPaint.color = Color.parseColor("#DC2626")
                canvas.drawText(FormatUtils.formatRupiah(tx.amount).removePrefix("Rp "), MARGIN_X + contentWidth - 6f, baseline, numPaint)
            }
            y += rowHeight
        }

        // Signature section
        if (y + 105f > A4_HEIGHT - MARGIN_BOTTOM) {
            doc.finishPage(page)
            pageNum++
            page = doc.startPage(PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, pageNum).create())
            canvas = page.canvas
            y = MARGIN_TOP
        }
        y += 24f
        drawSignaturesPdf(canvas, kop, y, contentWidth, serif = false)

        doc.finishPage(page)
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        out.toByteArray()
    }

    private fun drawSummaryCell(c: Canvas, label: String, value: String, x: Float, y: Float, valueColor: Int) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = valueColor
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        c.drawText(label, x, y, labelPaint)
        c.drawText(value, x, y + 17f, valPaint)
    }

    suspend fun generateFinanceXlsx(
        kop: KopSuratConfig,
        transactions: List<TransactionEntity>,
        filterLabel: String
    ): ByteArray = withContext(Dispatchers.Default) {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME.code }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE.code }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        val rowsXml = StringBuilder()
        var rowIndex = 1

        fun addTextRow(cells: List<Pair<String, Int>>) {
            rowsXml.append("""<row r="$rowIndex">""")
            cells.forEachIndexed { colIdx, (text, styleId) ->
                val colRef = ('A' + colIdx).toString() + rowIndex
                rowsXml.append("""<c r="$colRef" t="inlineStr" s="$styleId"><is><t>${escapeXml(text)}</t></is></c>""")
            }
            rowsXml.append("</row>")
            rowIndex++
        }

        addTextRow(listOf(kop.kkgName to 1))
        addTextRow(listOf("LAPORAN KEUANGAN KAS KKG PAI - Periode: $filterLabel" to 1))
        addTextRow(listOf("Dicetak: ${FormatUtils.todayIndonesianFull()}" to 0))
        rowIndex++ // Blank spacer row

        addTextRow(
            listOf(
                "No" to 2,
                "Tanggal" to 2,
                "Jenis" to 2,
                "Kategori" to 2,
                "Keterangan" to 2,
                "Pemasukan (Rp)" to 2,
                "Pengeluaran (Rp)" to 2
            )
        )

        transactions.forEachIndexed { idx, tx ->
            val isIncome = tx.type == TransactionType.INCOME.code
            val typeLabel = if (isIncome) "Pemasukan" else "Pengeluaran"
            rowsXml.append("""<row r="$rowIndex">""")
            rowsXml.append("""<c r="A$rowIndex" s="3"><v>${idx + 1}</v></c>""")
            rowsXml.append("""<c r="B$rowIndex" t="inlineStr" s="3"><is><t>${escapeXml(tx.date)}</t></is></c>""")
            rowsXml.append("""<c r="C$rowIndex" t="inlineStr" s="3"><is><t>${escapeXml(typeLabel)}</t></is></c>""")
            rowsXml.append("""<c r="D$rowIndex" t="inlineStr" s="3"><is><t>${escapeXml(tx.category)}</t></is></c>""")
            rowsXml.append("""<c r="E$rowIndex" t="inlineStr" s="3"><is><t>${escapeXml(tx.description)}</t></is></c>""")
            if (isIncome) {
                rowsXml.append("""<c r="F$rowIndex" s="4"><v>${tx.amount}</v></c>""")
                rowsXml.append("""<c r="G$rowIndex" s="4"><v>0</v></c>""")
            } else {
                rowsXml.append("""<c r="F$rowIndex" s="4"><v>0</v></c>""")
                rowsXml.append("""<c r="G$rowIndex" s="4"><v>${tx.amount}</v></c>""")
            }
            rowsXml.append("</row>")
            rowIndex++
        }

        // Totals row
        rowsXml.append("""<row r="$rowIndex">""")
        rowsXml.append("""<c r="E$rowIndex" t="inlineStr" s="1"><is><t>TOTAL</t></is></c>""")
        rowsXml.append("""<c r="F$rowIndex" s="5"><v>$totalIncome</v></c>""")
        rowsXml.append("""<c r="G$rowIndex" s="5"><v>$totalExpense</v></c>""")
        rowsXml.append("</row>")
        rowIndex++

        rowsXml.append("""<row r="$rowIndex">""")
        rowsXml.append("""<c r="E$rowIndex" t="inlineStr" s="1"><is><t>SALDO AKHIR KAS</t></is></c>""")
        rowsXml.append("""<c r="F$rowIndex" s="5"><v>$balance</v></c>""")
        rowsXml.append("</row>")

        val sheetXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <cols>
    <col min="1" max="1" width="6" customWidth="1"/>
    <col min="2" max="2" width="14" customWidth="1"/>
    <col min="3" max="3" width="14" customWidth="1"/>
    <col min="4" max="4" width="20" customWidth="1"/>
    <col min="5" max="5" width="42" customWidth="1"/>
    <col min="6" max="6" width="18" customWidth="1"/>
    <col min="7" max="7" width="18" customWidth="1"/>
  </cols>
  <sheetData>$rowsXml</sheetData>
</worksheet>"""

        val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

        val rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

        val workbook = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Kas KKG PAI" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

        val workbookRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

        val styles = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <numFmts count="1">
    <numFmt numFmtId="164" formatCode="#,##0"/>
  </numFmts>
  <fonts count="3">
    <font><sz val="11"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
  </fonts>
  <fills count="3">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF059669"/></patternFill></fill>
  </fills>
  <borders count="2">
    <border><left/><right/><top/><bottom/><diagonal/></border>
    <border>
      <left style="thin"><color rgb="FFCBD5E1"/></left>
      <right style="thin"><color rgb="FFCBD5E1"/></right>
      <top style="thin"><color rgb="FFCBD5E1"/></top>
      <bottom style="thin"><color rgb="FFCBD5E1"/></bottom>
      <diagonal/>
    </border>
  </borders>
  <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
  <cellXfs count="6">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
    <xf numFmtId="0" fontId="2" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1"/>
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyBorder="1"/>
    <xf numFmtId="164" fontId="0" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyBorder="1"/>
    <xf numFmtId="164" fontId="1" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyBorder="1"/>
  </cellXfs>
</styleSheet>"""

        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            fun putEntry(name: String, content: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            putEntry("[Content_Types].xml", contentTypes)
            putEntry("_rels/.rels", rels)
            putEntry("xl/workbook.xml", workbook)
            putEntry("xl/_rels/workbook.xml.rels", workbookRels)
            putEntry("xl/styles.xml", styles)
            putEntry("xl/worksheets/sheet1.xml", sheetXml)
        }
        out.toByteArray()
    }

    suspend fun generateLetterPdf(
        kop: KopSuratConfig,
        draft: LetterDraft
    ): ByteArray = withContext(Dispatchers.Default) {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create())
        val canvas = page.canvas
        val contentWidth = A4_WIDTH - (MARGIN_X * 2)

        var y = MARGIN_TOP
        y = drawKopSuratPdf(canvas, kop, y, contentWidth, serif = true)
        y += 18f

        val normalPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
        val boldPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        // Date on right
        val dateText = "${draft.cityName}, ${draft.letterDate}"
        val rightPaint = Paint(normalPaint).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText(dateText, MARGIN_X + contentWidth, y, rightPaint)

        // Nomor, Lampiran, Perihal on left
        val labelX = MARGIN_X
        val colonX = MARGIN_X + 64f
        val valX = MARGIN_X + 74f

        canvas.drawText("Nomor", labelX, y, normalPaint)
        canvas.drawText(":", colonX, y, normalPaint)
        canvas.drawText(draft.letterNumber, valX, y, normalPaint)
        y += 16f

        canvas.drawText("Lampiran", labelX, y, normalPaint)
        canvas.drawText(":", colonX, y, normalPaint)
        canvas.drawText(draft.attachment, valX, y, normalPaint)
        y += 16f

        canvas.drawText("Perihal", labelX, y, normalPaint)
        canvas.drawText(":", colonX, y, normalPaint)
        val subjectLayout = StaticLayout.Builder
            .obtain(draft.subject, 0, draft.subject.length, boldPaint, (contentWidth - 80f).toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        canvas.save()
        canvas.translate(valX, y - 10f)
        subjectLayout.draw(canvas)
        canvas.restore()
        y += subjectLayout.height + 16f

        // Recipient
        canvas.drawText("Kepada,", labelX, y, normalPaint)
        y += 14f
        val recipientLayout = StaticLayout.Builder
            .obtain(draft.recipient, 0, draft.recipient.length, boldPaint, contentWidth.toInt())
            .setLineSpacing(2f, 1.15f)
            .build()
        canvas.save()
        canvas.translate(labelX, y)
        recipientLayout.draw(canvas)
        canvas.restore()
        y += recipientLayout.height + 18f

        // Opening paragraph
        val openingLayout = StaticLayout.Builder
            .obtain(draft.openingText, 0, draft.openingText.length, normalPaint, contentWidth.toInt())
            .setLineSpacing(2f, 1.25f)
            .build()
        canvas.save()
        canvas.translate(labelX, y)
        openingLayout.draw(canvas)
        canvas.restore()
        y += openingLayout.height + 14f

        // Event detail table
        val indentX = labelX + 24f
        val detailColonX = indentX + 95f
        val detailValX = indentX + 106f

        fun drawDetailRow(label: String, value: String) {
            canvas.drawText(label, indentX, y, normalPaint)
            canvas.drawText(":", detailColonX, y, normalPaint)
            val valLayout = StaticLayout.Builder
                .obtain(value, 0, value.length, boldPaint, (contentWidth - 135f).toInt())
                .build()
            canvas.save()
            canvas.translate(detailValX, y - 10f)
            valLayout.draw(canvas)
            canvas.restore()
            y += valLayout.height + 6f
        }

        drawDetailRow("Hari / Tanggal", draft.eventDayDate)
        drawDetailRow("Waktu", draft.eventTime)
        drawDetailRow("Tempat", draft.eventLocation)
        y += 10f

        // Closing paragraph
        val closingLayout = StaticLayout.Builder
            .obtain(draft.closingText, 0, draft.closingText.length, normalPaint, contentWidth.toInt())
            .setLineSpacing(2f, 1.25f)
            .build()
        canvas.save()
        canvas.translate(labelX, y)
        closingLayout.draw(canvas)
        canvas.restore()
        y += closingLayout.height + 28f

        // Signatures
        drawSignaturesPdf(canvas, kop, y.coerceAtMost(A4_HEIGHT - 135f), contentWidth, serif = true)

        doc.finishPage(page)
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        out.toByteArray()
    }

    private fun drawKopSuratPdf(
        canvas: Canvas,
        kop: KopSuratConfig,
        startY: Float,
        contentWidth: Float,
        serif: Boolean
    ): Float {
        val family = if (serif) Typeface.SERIF else Typeface.SANS_SERIF
        val logoBitmap: Bitmap? = ImageCompressionUtils.decodeBase64ToBitmap(kop.logoBase64)
        val logoSize = 54
        if (logoBitmap != null) {
            val destRect = Rect(
                MARGIN_X.toInt(),
                startY.toInt(),
                (MARGIN_X + logoSize).toInt(),
                (startY + logoSize).toInt()
            )
            canvas.drawBitmap(logoBitmap, null, destRect, null)
        }

        val textLeft = if (logoBitmap != null) MARGIN_X + logoSize + 10f else MARGIN_X
        val textWidth = if (logoBitmap != null) contentWidth - logoSize - 10f else contentWidth
        val centerX = textLeft + textWidth / 2f

        val orgPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 11.5f
            typeface = Typeface.create(family, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val distPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(family, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val addrPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 8.5f
            typeface = Typeface.create(family, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        var y = startY + 12f
        canvas.drawText(kop.kkgName, centerX, y, orgPaint)
        y += 14f
        canvas.drawText(kop.districtName, centerX, y, distPaint)
        y += 13f
        canvas.drawText(kop.address, centerX, y, addrPaint)
        y += 11f
        canvas.drawText(kop.contactInfo, centerX, y, addrPaint)

        val lineY = maxOf(y + 10f, startY + logoSize + 8f)
        val thickLine = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2.2f
        }
        val thinLine = Paint().apply {
            color = Color.BLACK
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN_X, lineY, MARGIN_X + contentWidth, lineY, thickLine)
        canvas.drawLine(MARGIN_X, lineY + 3.5f, MARGIN_X + contentWidth, lineY + 3.5f, thinLine)
        return lineY + 6f
    }

    private fun drawSignaturesPdf(
        canvas: Canvas,
        kop: KopSuratConfig,
        startY: Float,
        contentWidth: Float,
        serif: Boolean
    ) {
        val family = if (serif) Typeface.SERIF else Typeface.SANS_SERIF
        val normal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10.5f
            typeface = Typeface.create(family, Typeface.NORMAL)
        }
        val boldUnderline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10.5f
            typeface = Typeface.create(family, Typeface.BOLD)
            isUnderlineText = true
        }
        val nipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 9.5f
            typeface = Typeface.create(family, Typeface.NORMAL)
        }

        val leftX = MARGIN_X + 10f
        val rightX = MARGIN_X + contentWidth * 0.56f

        canvas.drawText("Mengetahui,", leftX, startY, normal)
        canvas.drawText("Ketua KKG PAI", leftX, startY + 14f, normal)
        canvas.drawText(kop.chairpersonName, leftX, startY + 66f, boldUnderline)
        canvas.drawText("NIP. ${kop.chairpersonNip}", leftX, startY + 80f, nipPaint)

        canvas.drawText(" ", rightX, startY, normal)
        canvas.drawText("Sekretaris KKG PAI", rightX, startY + 14f, normal)
        canvas.drawText(kop.secretaryName, rightX, startY + 66f, boldUnderline)
        canvas.drawText("NIP. ${kop.secretaryNip}", rightX, startY + 80f, nipPaint)
    }

    suspend fun generateLetterWordDoc(
        kop: KopSuratConfig,
        draft: LetterDraft
    ): ByteArray = withContext(Dispatchers.Default) {
        val logoHtml = if (kop.logoBase64.isNotBlank()) {
            """<img src="data:image/jpeg;base64,${kop.logoBase64}" width="68" height="68" style="object-fit:contain;" />"""
        } else {
            ""
        }

        val openingHtml = escapeHtml(draft.openingText).replace("\n", "<br/>")
        val closingHtml = escapeHtml(draft.closingText).replace("\n", "<br/>")
        val recipientHtml = escapeHtml(draft.recipient).replace("\n", "<br/>")

        val html = """<html xmlns:o="urn:schemas-microsoft-com:office:office"
xmlns:w="urn:schemas-microsoft-com:office:word"
xmlns="http://www.w3.org/TR/REC-html40">
<head>
<meta charset="utf-8"/>
<title>${escapeHtml(draft.subject)}</title>
<style>
  @page Section1 {
    size: 21.0cm 29.7cm;
    margin: 2.0cm 2.2cm 2.0cm 2.2cm;
  }
  div.Section1 { page: Section1; }
  body {
    font-family: "Times New Roman", Times, serif;
    font-size: 12pt;
    line-height: 1.45;
    color: #000000;
  }
  table { border-collapse: collapse; width: 100%; }
  .kop-table td { vertical-align: middle; }
  .kop-rule { border-top: 3px solid #000; border-bottom: 1px solid #000; height: 2px; margin: 8px 0 18px 0; }
  .meta-table td { vertical-align: top; padding: 2px 0; font-size: 12pt; }
  .detail-table td { vertical-align: top; padding: 3px 0; font-size: 12pt; }
  .sig-table td { vertical-align: top; width: 50%; padding-top: 24px; font-size: 12pt; }
</style>
</head>
<body>
<div class="Section1">
  <table class="kop-table">
    <tr>
      ${if (logoHtml.isNotEmpty()) """<td style="width:82px;text-align:center;">$logoHtml</td>""" else ""}
      <td style="text-align:center;">
        <div style="font-size:13pt;font-weight:bold;">${escapeHtml(kop.kkgName)}</div>
        <div style="font-size:12pt;font-weight:bold;">${escapeHtml(kop.districtName)}</div>
        <div style="font-size:9.5pt;">${escapeHtml(kop.address)}</div>
        <div style="font-size:9.5pt;">${escapeHtml(kop.contactInfo)}</div>
      </td>
    </tr>
  </table>
  <div class="kop-rule"></div>

  <table class="meta-table">
    <tr>
      <td style="width:90px;">Nomor</td>
      <td style="width:14px;">:</td>
      <td>${escapeHtml(draft.letterNumber)}</td>
      <td style="text-align:right;white-space:nowrap;">${escapeHtml(draft.cityName)}, ${escapeHtml(draft.letterDate)}</td>
    </tr>
    <tr>
      <td>Lampiran</td>
      <td>:</td>
      <td colspan="2">${escapeHtml(draft.attachment)}</td>
    </tr>
    <tr>
      <td>Perihal</td>
      <td>:</td>
      <td colspan="2"><b>${escapeHtml(draft.subject)}</b></td>
    </tr>
  </table>

  <p style="margin-top:18px;margin-bottom:16px;">
    Kepada,<br/>
    <b>$recipientHtml</b>
  </p>

  <p style="text-align:justify;margin-bottom:12px;">
    $openingHtml
  </p>

  <table class="detail-table" style="margin-left:28px;width:92%;margin-bottom:14px;">
    <tr>
      <td style="width:130px;">Hari / Tanggal</td>
      <td style="width:14px;">:</td>
      <td><b>${escapeHtml(draft.eventDayDate)}</b></td>
    </tr>
    <tr>
      <td>Waktu</td>
      <td>:</td>
      <td><b>${escapeHtml(draft.eventTime)}</b></td>
    </tr>
    <tr>
      <td>Tempat</td>
      <td>:</td>
      <td><b>${escapeHtml(draft.eventLocation)}</b></td>
    </tr>
  </table>

  <p style="text-align:justify;margin-bottom:24px;">
    $closingHtml
  </p>

  <table class="sig-table">
    <tr>
      <td>
        Mengetahui,<br/>
        Ketua KKG PAI<br/><br/><br/><br/>
        <u><b>${escapeHtml(kop.chairpersonName)}</b></u><br/>
        NIP. ${escapeHtml(kop.chairpersonNip)}
      </td>
      <td style="padding-left:36px;">
        <br/>
        Sekretaris KKG PAI<br/><br/><br/><br/>
        <u><b>${escapeHtml(kop.secretaryName)}</b></u><br/>
        NIP. ${escapeHtml(kop.secretaryNip)}
      </td>
    </tr>
  </table>
</div>
</body>
</html>"""
        html.toByteArray(Charsets.UTF_8)
    }

    suspend fun writeBytesToUri(context: Context, uri: Uri, bytes: ByteArray): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(bytes)
                    out.flush()
                } ?: return@withContext Result.failure(IllegalStateException("Gagal membuka file tujuan."))
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun shareBytes(
        context: Context,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ) = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Bagikan $fileName").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun escapeXml(input: String): String =
        input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")

    private fun escapeHtml(input: String): String =
        input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
}
