package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object BengaliUtils {

    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBengaliDigits(input: String): String {
        val sb = java.lang.StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toBengaliDigits(number: Int): String {
        return toBengaliDigits(number.toString())
    }

    fun getCurrentBengaliGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "শুভ সকাল"
            in 12..15 -> "শুভ দুপুর"
            in 16..17 -> "শুভ বিকেল"
            in 18..21 -> "শুভ সন্ধ্যা"
            else -> "শুভ রাত্রি"
        }
    }

    fun getCurrentBengaliTime(): String {
        val calendar = Calendar.getInstance()
        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val hour12 = calendar.get(Calendar.HOUR)
        val effectiveHour = if (hour12 == 0) 12 else hour12
        val minute = calendar.get(Calendar.MINUTE)

        val period = when (hour24) {
            in 4..5 -> "ভোর"
            in 6..11 -> "সকাল"
            in 12..14 -> "দুপুর"
            in 15..17 -> "বিকেল"
            in 18..19 -> "সন্ধ্যা"
            else -> "রাত"
        }

        val bnHour = toBengaliDigits(effectiveHour)
        val bnMinute = toBengaliDigits(minute)

        return if (minute == 0) {
            "$period $bnHour টা"
        } else {
            "$period $bnHour টা $bnMinute মিনিট"
        }
    }

    fun getCurrentBengaliDate(): String {
        val calendar = Calendar.getInstance()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)

        val monthNameBn = when (calendar.get(Calendar.MONTH)) {
            Calendar.JANUARY -> "জানুয়ারি"
            Calendar.FEBRUARY -> "ফেব্রুয়ারি"
            Calendar.MARCH -> "মার্চ"
            Calendar.APRIL -> "এপ্রিল"
            Calendar.MAY -> "মে"
            Calendar.JUNE -> "জুন"
            Calendar.JULY -> "জুলাই"
            Calendar.AUGUST -> "আগস্ট"
            Calendar.SEPTEMBER -> "সেপ্টেম্বর"
            Calendar.OCTOBER -> "অক্টোবর"
            Calendar.NOVEMBER -> "নভেম্বর"
            Calendar.DECEMBER -> "ডিসেম্বর"
            else -> ""
        }

        val dayNameBn = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "রবিবার"
            Calendar.MONDAY -> "সোমবার"
            Calendar.TUESDAY -> "মঙ্গলবার"
            Calendar.WEDNESDAY -> "বুধবার"
            Calendar.THURSDAY -> "বৃহস্পতিবার"
            Calendar.FRIDAY -> "শুক্রবার"
            Calendar.SATURDAY -> "শনিবার"
            else -> ""
        }

        val bnDay = toBengaliDigits(dayOfMonth)
        val bnYear = toBengaliDigits(year)

        return "$bnDay $monthNameBn $bnYear, $dayNameBn"
    }

    fun getDayOfWeekBengali(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "রবিবার"
            Calendar.MONDAY -> "সোমবার"
            Calendar.TUESDAY -> "মঙ্গলবার"
            Calendar.WEDNESDAY -> "বুধবার"
            Calendar.THURSDAY -> "বৃহস্পতিবার"
            Calendar.FRIDAY -> "শুক্রবার"
            Calendar.SATURDAY -> "শনিবার"
            else -> ""
        }
    }

    fun cleanSpokenQuery(text: String): String {
        var cleaned = text.trim()
        val prefixes = listOf(
            "mahi", "hey mahi", "hi mahi", "মাহী", "হে মাহী", "হাই মাহী",
            "দয়া করে", "প্লিজ", "একটু", "মাহি"
        )
        for (prefix in prefixes) {
            if (cleaned.startsWith(prefix, ignoreCase = true)) {
                cleaned = cleaned.substring(prefix.length).trim()
            }
        }
        return cleaned.removePrefix(",").removePrefix("।").trim()
    }
}
