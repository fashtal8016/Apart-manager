package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.data.model.Building
import com.example.data.model.BuildingUnit
import com.example.data.model.ChargeRecord
import com.example.data.model.UnitAnnualNote
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    /**
     * Generates a PDF for the Annual Charge Table.
     */
    fun generateAnnualChargePdf(
        context: Context,
        building: Building,
        units: List<BuildingUnit>,
        charges: List<ChargeRecord>,
        annualNotes: Map<Long, String>,
        year: Int,
        includeNotes: Boolean = true
    ): File {
        val document = PdfDocument()
        // Landscape A4: 842 x 595 pt
        val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(26, 54, 93) // Navy
            textSize = 16f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }

        val subTitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            textAlign = Paint.Align.RIGHT
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(30, 58, 138)
            textSize = 9.5f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249) // Light Slate
            style = Paint.Style.FILL
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }

        val smallTextPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
        }

        // Draw Title & Header
        val startX = 810f
        val leftX = 30f
        var currentY = 40f

        val currentDate = JalaliCalendar.getCurrentJalaliDate().toFormattedString()
        canvas.drawText("گزارش جدول سالانه وضعیت شارژ ساختمان: ${building.name}", startX, currentY, titlePaint)
        currentY += 20f
        canvas.drawText("سال مالی: $year  |  تاریخ تهیه گزارش: $currentDate  |  مدیر: ${building.managerName}", startX, currentY, subTitlePaint)
        currentY += 25f

        // Table Layout:
        // Right to Left:
        // Row (25) | Unit (35) | Resident (90) | Far..Esfand (12 x 36 = 432) | Total Paid (65) | Balance (65) | Notes (100)
        val colWidths = mutableListOf(25f, 35f, 85f)
        for (i in 1..12) colWidths.add(35f)
        colWidths.add(65f)
        colWidths.add(65f)
        if (includeNotes) colWidths.add(105f)

        val totalTableWidth = colWidths.sum()
        val tableRight = 810f
        val tableLeft = tableRight - totalTableWidth

        val rowHeight = 22f

        // Draw Table Header
        canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, headerBgPaint)
        canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, borderPaint)

        val headerTitles = mutableListOf("ردیف", "واحد", "نام ساکن")
        headerTitles.addAll(listOf("فر", "ارد", "خرد", "تیر", "مرد", "شهر", "مهر", "آبان", "آذر", "دی", "بهم", "اسف"))
        headerTitles.add("پرداخت سال")
        headerTitles.add("مانده بدهی")
        if (includeNotes) headerTitles.add("توضیحات کلی")

        var colX = tableRight
        for (idx in headerTitles.indices) {
            val width = colWidths[idx]
            val cellCenter = colX - (width / 2)
            canvas.drawText(headerTitles[idx], cellCenter, currentY + 15f, headerPaint)
            canvas.drawLine(colX - width, currentY, colX - width, currentY + rowHeight, borderPaint)
            colX -= width
        }

        currentY += rowHeight

        // Draw Data Rows
        val chargeMap = charges.groupBy { it.unitId }

        for ((index, unit) in units.withIndex()) {
            val unitCharges = chargeMap[unit.id] ?: emptyList()
            val totalPaid = unitCharges.sumOf { it.paidAmount }
            val totalTarget = unitCharges.sumOf { it.targetAmount }
            val debt = (totalTarget - totalPaid).coerceAtLeast(0L) + unit.initialDebt

            // Alternate row background
            if (index % 2 == 1) {
                val altBg = Paint().apply {
                    color = Color.rgb(248, 250, 252)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, altBg)
            }
            canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, borderPaint)

            colX = tableRight

            // 1. Row index
            canvas.drawText("${index + 1}", colX - (colWidths[0] / 2), currentY + 14f, textPaint)
            canvas.drawLine(colX - colWidths[0], currentY, colX - colWidths[0], currentY + rowHeight, borderPaint)
            colX -= colWidths[0]

            // 2. Unit
            canvas.drawText(unit.unitNumber, colX - (colWidths[1] / 2), currentY + 14f, textPaint)
            canvas.drawLine(colX - colWidths[1], currentY, colX - colWidths[1], currentY + rowHeight, borderPaint)
            colX -= colWidths[1]

            // 3. Resident
            val residentShort = if (unit.residentName.length > 15) unit.residentName.take(13) + ".." else unit.residentName
            canvas.drawText(residentShort, colX - (colWidths[2] / 2), currentY + 14f, textPaint)
            canvas.drawLine(colX - colWidths[2], currentY, colX - colWidths[2], currentY + rowHeight, borderPaint)
            colX -= colWidths[2]

            // 4. Months 1 to 12
            for (m in 1..12) {
                val rec = unitCharges.firstOrNull { it.monthIndex == m }
                val statusText: String
                val statusPaint = Paint().apply {
                    textSize = 8.5f
                    textAlign = Paint.Align.CENTER
                }
                when (rec?.status) {
                    "PAID" -> {
                        statusText = "✓"
                        statusPaint.color = Color.rgb(22, 163, 74) // Green
                        statusPaint.isFakeBoldText = true
                    }
                    "PARTIAL" -> {
                        statusText = "جزئی"
                        statusPaint.color = Color.rgb(217, 119, 6) // Amber
                    }
                    "EXEMPT" -> {
                        statusText = "معاف"
                        statusPaint.color = Color.rgb(79, 70, 229) // Indigo
                    }
                    "REVIEW" -> {
                        statusText = "بررسی"
                        statusPaint.color = Color.rgb(225, 29, 72) // Rose
                    }
                    else -> {
                        statusText = "—"
                        statusPaint.color = Color.rgb(156, 163, 175) // Gray
                    }
                }
                val mWidth = colWidths[2 + m]
                canvas.drawText(statusText, colX - (mWidth / 2), currentY + 14f, statusPaint)
                canvas.drawLine(colX - mWidth, currentY, colX - mWidth, currentY + rowHeight, borderPaint)
                colX -= mWidth
            }

            // 5. Total Paid
            val paidWidth = colWidths[15]
            val paidFormatted = JalaliCalendar.formatAmount(totalPaid, building.currency, usePersianDigits = true)
                .replace(" تومان", "").replace(" ریال", "")
            canvas.drawText(paidFormatted, colX - (paidWidth / 2), currentY + 14f, textPaint)
            canvas.drawLine(colX - paidWidth, currentY, colX - paidWidth, currentY + rowHeight, borderPaint)
            colX -= paidWidth

            // 6. Debt Balance
            val debtWidth = colWidths[16]
            val debtFormatted = JalaliCalendar.formatAmount(debt, building.currency, usePersianDigits = true)
                .replace(" تومان", "").replace(" ریال", "")
            val debtPaint = Paint().apply {
                color = if (debt > 0) Color.rgb(220, 38, 38) else Color.rgb(22, 163, 74)
                textSize = 9f
                isFakeBoldText = debt > 0
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(debtFormatted, colX - (debtWidth / 2), currentY + 14f, debtPaint)
            canvas.drawLine(colX - debtWidth, currentY, colX - debtWidth, currentY + rowHeight, borderPaint)
            colX -= debtWidth

            // 7. General Note
            if (includeNotes) {
                val noteWidth = colWidths[17]
                val noteText = annualNotes[unit.id] ?: unit.notes
                val shortNote = if (noteText.length > 20) noteText.take(18) + ".." else noteText
                canvas.drawText(shortNote, colX - (noteWidth / 2), currentY + 14f, smallTextPaint)
                canvas.drawLine(colX - noteWidth, currentY, colX - noteWidth, currentY + rowHeight, borderPaint)
                colX -= noteWidth
            }

            currentY += rowHeight
        }

        // Draw Footer info
        currentY += 20f
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8.5f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("راهنما: (✓) پرداخت کامل   (جزئی) پرداخت بخشی از شارژ   (معاف) معافیت مصوب   (—) پرداخت‌نشده", 810f, currentY, footerPaint)

        document.finishPage(page)

        val file = File(context.cacheDir, "annual_charges_${building.id}_$year.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    /**
     * Generates a clean One-Page Resident Deposit Summary PDF.
     */
    fun generateDepositSummaryPdf(
        context: Context,
        building: Building,
        units: List<BuildingUnit>,
        charges: List<ChargeRecord>,
        year: Int,
        monthName: String = "شهریور"
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Portrait A4
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(26, 54, 93)
            textSize = 15f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }

        val subPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(30, 58, 138)
            textSize = 9.5f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val cellPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }

        var currentY = 40f
        val rightX = 565f
        val leftX = 30f

        val currentDate = JalaliCalendar.getCurrentJalaliDate().toFormattedString()
        canvas.drawText("خلاصه واریز ساکنین - ${building.name}", rightX, currentY, titlePaint)
        currentY += 18f
        canvas.drawText("دوره: ماه $monthName سال $year  |  تاریخ صدور: $currentDate", rightX, currentY, subPaint)
        currentY += 25f

        // Table:
        // Row (25) | Unit (35) | Resident (110) | Deposit Count (50) | Last Date (75) | Total Paid (115) | Status (90)
        val colWidths = listOf(25f, 40f, 120f, 55f, 75f, 115f, 70f)
        val tableRight = 565f
        val tableLeft = 565f - colWidths.sum()
        val rowHeight = 22f

        val headerBg = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, headerBg)
        canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, borderPaint)

        val headers = listOf("ردیف", "واحد", "نام ساکن", "تعداد واریز", "آخرین تاریخ", "مجموع واریزی", "وضعیت")
        var colX = tableRight
        for (i in headers.indices) {
            val w = colWidths[i]
            canvas.drawText(headers[i], colX - (w / 2), currentY + 15f, headerPaint)
            canvas.drawLine(colX - w, currentY, colX - w, currentY + rowHeight, borderPaint)
            colX -= w
        }

        currentY += rowHeight

        val chargeMap = charges.groupBy { it.unitId }
        var totalDepositsSum = 0L
        var payingUnitsCount = 0

        for ((idx, unit) in units.withIndex()) {
            val uCharges = chargeMap[unit.id] ?: emptyList()
            val totalPaid = uCharges.sumOf { it.paidAmount }
            val paidCount = uCharges.count { it.paidAmount > 0 }
            val lastRecord = uCharges.filter { it.paidAmount > 0 }.maxByOrNull { it.monthIndex }
            val lastDate = lastRecord?.paymentDate ?: "—"

            if (totalPaid > 0) {
                totalDepositsSum += totalPaid
                payingUnitsCount++
            }

            if (idx % 2 == 1) {
                val rowBg = Paint().apply {
                    color = Color.rgb(248, 250, 252)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, rowBg)
            }
            canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, borderPaint)

            colX = tableRight
            // 1. Index
            canvas.drawText("${idx + 1}", colX - (colWidths[0] / 2), currentY + 14f, cellPaint)
            canvas.drawLine(colX - colWidths[0], currentY, colX - colWidths[0], currentY + rowHeight, borderPaint)
            colX -= colWidths[0]

            // 2. Unit
            canvas.drawText(unit.unitNumber, colX - (colWidths[1] / 2), currentY + 14f, cellPaint)
            canvas.drawLine(colX - colWidths[1], currentY, colX - colWidths[1], currentY + rowHeight, borderPaint)
            colX -= colWidths[1]

            // 3. Resident
            canvas.drawText(unit.residentName, colX - (colWidths[2] / 2), currentY + 14f, cellPaint)
            canvas.drawLine(colX - colWidths[2], currentY, colX - colWidths[2], currentY + rowHeight, borderPaint)
            colX -= colWidths[2]

            // 4. Count
            canvas.drawText("$paidCount", colX - (colWidths[3] / 2), currentY + 14f, cellPaint)
            canvas.drawLine(colX - colWidths[3], currentY, colX - colWidths[3], currentY + rowHeight, borderPaint)
            colX -= colWidths[3]

            // 5. Last Date
            canvas.drawText(lastDate, colX - (colWidths[4] / 2), currentY + 14f, cellPaint)
            canvas.drawLine(colX - colWidths[4], currentY, colX - colWidths[4], currentY + rowHeight, borderPaint)
            colX -= colWidths[4]

            // 6. Total Paid
            val paidFormatted = JalaliCalendar.formatAmount(totalPaid, building.currency, true)
            canvas.drawText(paidFormatted, colX - (colWidths[5] / 2), currentY + 14f, cellPaint)
            canvas.drawLine(colX - colWidths[5], currentY, colX - colWidths[5], currentY + rowHeight, borderPaint)
            colX -= colWidths[5]

            // 7. Status
            val statusLabel = if (totalPaid > 0) "دارای پرداخت" else "بدون پرداخت"
            val statusColor = if (totalPaid > 0) Color.rgb(22, 163, 74) else Color.rgb(220, 38, 38)
            val stPaint = Paint().apply {
                color = statusColor
                textSize = 8.5f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(statusLabel, colX - (colWidths[6] / 2), currentY + 14f, stPaint)
            canvas.drawLine(colX - colWidths[6], currentY, colX - colWidths[6], currentY + rowHeight, borderPaint)
            colX -= colWidths[6]

            currentY += rowHeight
        }

        // Summary Card at bottom
        currentY += 25f
        val summaryCardPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(leftX, currentY, rightX, currentY + 75f, 8f, 8f, summaryCardPaint)
        canvas.drawRoundRect(leftX, currentY, rightX, currentY + 75f, 8f, 8f, borderPaint)

        val statTitlePaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        val statValuePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText("مجموع کل واریزی‌های ساکنین: ${JalaliCalendar.formatAmount(totalDepositsSum, building.currency, true)}", rightX - 15f, currentY + 22f, statTitlePaint)
        canvas.drawText("تعداد کل واحدها: ${units.size}  |  واحدهای دارای پرداخت: $payingUnitsCount  |  واحدهای بدون واریزی: ${units.size - payingUnitsCount}", rightX - 15f, currentY + 44f, statValuePaint)
        canvas.drawText("تمامی تراکنش‌ها بر اساس صورتحساب و دفاتر معین تأییدشده استخراج شده‌اند.", rightX - 15f, currentY + 62f, Paint().apply {
            color = Color.GRAY
            textSize = 8.5f
            textAlign = Paint.Align.RIGHT
        })

        document.finishPage(page)

        val file = File(context.cacheDir, "deposit_summary_${building.id}_$year.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }
}
