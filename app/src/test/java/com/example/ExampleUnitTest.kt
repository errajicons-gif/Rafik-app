package com.example

import com.example.core.hardware.DocumentReaderHelper
import com.example.core.i18n.stringsFor
import com.example.data.local.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun splitSentences_handlesArabicAndLatinPunctuation() {
        val text = "مرحباً بك في رفيق. كيف حالك؟ أنا بخير!"
        val sentences = DocumentReaderHelper.splitIntoSentences(text)
        assertEquals(3, sentences.size)
    }

    @Test
    fun stringsFor_providesDistinctLocalizedTitles() {
        val ar = stringsFor(AppLanguage.ARABIC)
        val fr = stringsFor(AppLanguage.FRENCH)
        val en = stringsFor(AppLanguage.ENGLISH)
        assertTrue(ar.visionTitle.contains("العين"))
        assertTrue(fr.visionTitle.contains("Vision"))
        assertTrue(en.visionTitle.contains("Vision"))
    }
}
