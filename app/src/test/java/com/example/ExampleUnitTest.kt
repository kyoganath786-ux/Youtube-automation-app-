package com.example

import com.example.data.model.AppLanguage
import com.example.data.model.ProjectStatus
import com.example.data.model.VideoFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `verify video formats and resolutions`() {
        assertEquals("9:16", VideoFormat.SHORTS.aspectRatio)
        assertEquals("1080x1920", VideoFormat.SHORTS.resolution)

        assertEquals("16:9", VideoFormat.STANDARD.aspectRatio)
        assertEquals("1920x1080", VideoFormat.STANDARD.resolution)
    }

    @Test
    fun `verify language codes`() {
        assertEquals("en", AppLanguage.ENGLISH.code)
        assertEquals("ta", AppLanguage.TAMIL.code)
    }

    @Test
    fun `verify project status labels in English and Tamil`() {
        assertEquals("Draft", ProjectStatus.DRAFT.labelEn)
        assertEquals("வரைவு", ProjectStatus.DRAFT.labelTa)

        assertEquals("Ready", ProjectStatus.READY.labelEn)
        assertEquals("தயார்", ProjectStatus.READY.labelTa)
    }
}
