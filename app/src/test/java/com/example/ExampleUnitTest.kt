package com.example

import com.example.util.BengaliUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testBengaliDigitsConversion() {
    assertEquals("১২৩৪৫৬৭৮৯০", BengaliUtils.toBengaliDigits("1234567890"))
  }

  @Test
  fun testCleanSpokenQuery() {
    val cleaned = BengaliUtils.cleanSpokenQuery("Mahi, TikTok খোলো")
    assertEquals("TikTok খোলো", cleaned)

    val cleanedBn = BengaliUtils.cleanSpokenQuery("মাহী WhatsApp খোলো")
    assertEquals("WhatsApp খোলো", cleanedBn)
  }

  @Test
  fun testBengaliDateAndGreeting() {
    val greeting = BengaliUtils.getCurrentBengaliGreeting()
    assertTrue(greeting.startsWith("শুভ"))

    val date = BengaliUtils.getCurrentBengaliDate()
    assertTrue(date.isNotEmpty())
  }
}

