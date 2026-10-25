package com.example.data.media

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class DeviceTtsEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            Log.d("DeviceTtsEngine", "TextToSpeech successfully initialized")
        } else {
            Log.e("DeviceTtsEngine", "TextToSpeech initialization failed with status: $status")
        }
    }

    fun speak(text: String, language: AppLanguage, speed: Float = 1.0f, pitch: Float = 1.0f) {
        if (!isInitialized || tts == null) return

        val locale = if (language == AppLanguage.TAMIL) {
            Locale("ta", "IN")
        } else {
            Locale.US
        }

        tts?.apply {
            setLanguage(locale)
            setSpeechRate(speed)
            setPitch(pitch)
            speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    /**
     * Synthesizes audio directly to a real playable file on disk
     */
    suspend fun synthesizeToFile(
        text: String,
        language: AppLanguage,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isInitialized || tts == null) return@withContext false

        val locale = if (language == AppLanguage.TAMIL) Locale("ta", "IN") else Locale.US
        tts?.setLanguage(locale)

        val params = Bundle()
        val utteranceId = "synth_${System.currentTimeMillis()}"

        val result = tts?.synthesizeToFile(text, params, outputFile, utteranceId)
        result == TextToSpeech.SUCCESS
    }
}
