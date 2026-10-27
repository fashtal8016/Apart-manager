package com.example.util

import java.text.DecimalFormat
import java.util.Calendar
import java.util.Date

object JalaliCalendar {
    val MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    data class JalaliDate(val year: Int, val month: Int, val day: Int) {
        val monthName: String
            get() = if (month in 1..12) MONTH_NAMES[month - 1] else ""

        fun toFormattedString(usePersianDigits: Boolean = true): String {
            val text = String.format("%04d/%02d/%02d", year, month, day)
            return if (usePersianDigits) toPersianDigits(text) else text
        }
    }

    /**
     * Converts a Gregorian date to Jalali
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) (gy + 1) else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + g_d_m[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return JalaliDate(jy, jm, jd)
    }

    fun getCurrentJalaliDate(): JalaliDate {
        val cal = Calendar.getInstance()
        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH) + 1
        val gd = cal.get(Calendar.DAY_OF_MONTH)
        return gregorianToJalali(gy, gm, gd)
    }

    fun toPersianDigits(input: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            when (ch) {
                '۰', '٠' -> sb.append('0')
                '۱', '١' -> sb.append('1')
                '۲', '٢' -> sb.append('2')
                '۳', '٣' -> sb.append('3')
                '۴', '٤' -> sb.append('4')
                '۵', '٥' -> sb.append('5')
                '۶', '٦' -> sb.append('6')
                '۷', '٧' -> sb.append('7')
                '۸', '٨' -> sb.append('8')
                '۹', '٩' -> sb.append('9')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatAmount(amount: Long, currency: String = "TOMAN", usePersianDigits: Boolean = true): String {
        val formatter = DecimalFormat("#,###")
        val formattedNumber = formatter.format(amount)
        val currencyLabel = if (currency == "RIAL") "ریال" else "تومان"
        val full = "$formattedNumber $currencyLabel"
        return if (usePersianDigits) toPersianDigits(full) else full
    }
}
