package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val indonesiaLocale = Locale("id", "ID")
    private val currencyFormat: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(indonesiaLocale).apply {
            maximumFractionDigits = 0
        }
    }

    fun formatRupiah(amount: Long): String {
        val formatted = NumberFormat.getNumberInstance(indonesiaLocale).format(amount)
        return "Rp $formatted"
    }

    fun formatRupiahSigned(amount: Long, isIncome: Boolean): String {
        val prefix = if (isIncome) "+ " else "- "
        return prefix + formatRupiah(kotlin.math.abs(amount))
    }

    fun todayIsoDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun currentYearMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        return sdf.format(Date())
    }

    fun todayIndonesianFull(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", indonesiaLocale)
        return sdf.format(Date())
    }

    fun formatIsoDateShort(isoDate: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(isoDate) ?: return isoDate
            val formatter = SimpleDateFormat("dd MMM yyyy", indonesiaLocale)
            formatter.format(date)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatDateTimeIndonesian(isoDateTime: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val date = parser.parse(isoDateTime) ?: return isoDateTime
            val formatter = SimpleDateFormat("EEE, dd MMM yyyy • HH:mm", indonesiaLocale)
            formatter.format(date) + " WIB"
        } catch (_: Exception) {
            isoDateTime
        }
    }

    fun formatMonthYearLabel(yearMonth: String): String {
        if (yearMonth == "ALL") return "Semua Bulan"
        return try {
            val parser = SimpleDateFormat("yyyy-MM", Locale.US)
            val date = parser.parse(yearMonth) ?: return yearMonth
            val formatter = SimpleDateFormat("MMMM yyyy", indonesiaLocale)
            formatter.format(date)
        } catch (_: Exception) {
            yearMonth
        }
    }

    fun dateOffsetFromToday(daysOffset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun dateTimeOffsetFromToday(daysOffset: Int, hour: Int, minute: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(cal.time)
    }

    fun sanitizeRupiahInput(raw: String): Long {
        val digits = raw.filter { it.isDigit() }
        return digits.toLongOrNull() ?: 0L
    }

    fun formatThousandsInput(digitsOnly: String): String {
        val value = digitsOnly.filter { it.isDigit() }.toLongOrNull() ?: return ""
        return NumberFormat.getNumberInstance(indonesiaLocale).format(value)
    }
}
