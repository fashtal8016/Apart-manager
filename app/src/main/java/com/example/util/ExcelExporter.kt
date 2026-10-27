package com.example.util

import android.content.Context
import com.example.data.model.Building
import com.example.data.model.BuildingUnit
import com.example.data.model.ChargeRecord
import com.example.data.model.MonthChargeNote
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object ExcelExporter {

    /**
     * Exports Annual Charge Table to CSV with UTF-8 BOM so Microsoft Excel renders Persian correctly.
     */
    fun exportAnnualChargeCsv(
        context: Context,
        building: Building,
        units: List<BuildingUnit>,
        charges: List<ChargeRecord>,
        annualNotes: Map<Long, String>,
        monthNotes: List<MonthChargeNote>,
        year: Int
    ): File {
        val file = File(context.cacheDir, "annual_charges_${building.id}_$year.csv")
        val chargeMap = charges.groupBy { it.unitId }
        val monthNotesMap = monthNotes.associateBy { "${it.unitId}_${it.monthIndex}" }

        OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
            // Write UTF-8 BOM
            writer.write("\uFEFF")

            // Title rows
            writer.write("جدول سالانه شارژ ساختمان ${building.name}\n")
            writer.write("سال مالی: $year,تاریخ صدور: ${JalaliCalendar.getCurrentJalaliDate().toFormattedString()},مدیر ساختمان: ${building.managerName}\n\n")

            // Header row
            val headers = listOf(
                "ردیف", "شماره واحد", "نام ساکن", "متراژ", "تعداد ساکن",
                "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
                "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
                "مجموع پرداختی سال", "مانده بدهی", "توضیحات کلی واحد", "یادداشت‌های ماهانه"
            )
            writer.write(headers.joinToString(",") { escapeCsv(it) } + "\n")

            for ((index, unit) in units.withIndex()) {
                val uCharges = chargeMap[unit.id] ?: emptyList()
                val totalPaid = uCharges.sumOf { it.paidAmount }
                val totalTarget = uCharges.sumOf { it.targetAmount }
                val debt = (totalTarget - totalPaid).coerceAtLeast(0L) + unit.initialDebt

                val rowValues = mutableListOf<String>()
                rowValues.add("${index + 1}")
                rowValues.add(unit.unitNumber)
                rowValues.add(unit.residentName)
                rowValues.add("${unit.area}")
                rowValues.add("${unit.residentCount}")

                // Months
                for (m in 1..12) {
                    val cr = uCharges.firstOrNull { it.monthIndex == m }
                    val statusText = when (cr?.status) {
                        "PAID" -> "پرداخت کامل (${cr.paidAmount})"
                        "PARTIAL" -> "جزئی (${cr.paidAmount})"
                        "EXEMPT" -> "معاف"
                        "REVIEW" -> "بررسی"
                        else -> "پرداخت‌نشده"
                    }
                    rowValues.add(statusText)
                }

                rowValues.add("$totalPaid")
                rowValues.add("$debt")

                // General Annual Note
                val generalNote = annualNotes[unit.id] ?: unit.notes
                rowValues.add(generalNote)

                // Concatenated monthly notes for this unit
                val monthlyNotesList = (1..12).mapNotNull { m ->
                    val mn = monthNotesMap["${unit.id}_$m"]
                    if (mn != null && mn.note.isNotBlank()) "${JalaliCalendar.MONTH_NAMES[m - 1]}: ${mn.note}" else null
                }
                rowValues.add(monthlyNotesList.joinToString(" | "))

                writer.write(rowValues.joinToString(",") { escapeCsv(it) } + "\n")
            }
        }

        return file
    }

    /**
     * Exports One-Page Resident Deposit Summary to CSV with UTF-8 BOM.
     */
    fun exportDepositSummaryCsv(
        context: Context,
        building: Building,
        units: List<BuildingUnit>,
        charges: List<ChargeRecord>,
        year: Int
    ): File {
        val file = File(context.cacheDir, "deposit_summary_${building.id}_$year.csv")
        val chargeMap = charges.groupBy { it.unitId }

        OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
            writer.write("\uFEFF")
            writer.write("خلاصه واریز ساکنین - ${building.name}\n")
            writer.write("سال مالی: $year,تاریخ تهیه: ${JalaliCalendar.getCurrentJalaliDate().toFormattedString()}\n\n")

            val headers = listOf("ردیف", "شماره واحد", "نام ساکن", "تعداد واریزی", "آخرین تاریخ واریز", "مجموع مبالغ واریز", "وضعیت پرداخت")
            writer.write(headers.joinToString(",") { escapeCsv(it) } + "\n")

            for ((index, unit) in units.withIndex()) {
                val uCharges = chargeMap[unit.id] ?: emptyList()
                val totalPaid = uCharges.sumOf { it.paidAmount }
                val count = uCharges.count { it.paidAmount > 0 }
                val lastDate = uCharges.filter { it.paidAmount > 0 }.maxByOrNull { it.monthIndex }?.paymentDate ?: "—"
                val status = if (totalPaid > 0) "دارای پرداخت" else "بدون واریز"

                val row = listOf(
                    "${index + 1}",
                    unit.unitNumber,
                    unit.residentName,
                    "$count",
                    lastDate,
                    "$totalPaid",
                    status
                )
                writer.write(row.joinToString(",") { escapeCsv(it) } + "\n")
            }
        }
        return file
    }

    private fun escapeCsv(value: String): String {
        var str = value.replace("\"", "\"\"")
        if (str.contains(",") || str.contains("\n") || str.contains("\"")) {
            str = "\"$str\""
        }
        return str
    }
}
