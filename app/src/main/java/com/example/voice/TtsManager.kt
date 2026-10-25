package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class TtsManager(
    private val context: Context,
    private val onSpeechStarted: (String) -> Unit,
    private val onSpeechFinished: (String) -> Unit,
    private val onError: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    var isInitialized: Boolean = false
        private set

    var pitch: Float = 1.08f
        set(value) {
            field = value
            tts?.setPitch(value)
        }

    var speechRate: Float = 0.95f
        set(value) {
            field = value
            tts?.setSpeechRate(value)
        }

    init {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("TtsManager", "Error instantiating TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts ?: return
            isInitialized = true

            // Set Bengali Locale
            val bnLocale = Locale.forLanguageTag("bn-BD")
            val result = engine.setLanguage(bnLocale)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Try alternate Bengali locale
                val altResult = engine.setLanguage(Locale.forLanguageTag("bn"))
                if (altResult == TextToSpeech.LANG_MISSING_DATA || altResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w("TtsManager", "Bengali voice data not available on this device")
                }
            }

            // Attempt to choose a pleasant feminine Bengali voice if available
            try {
                val voices = engine.voices
                if (voices != null) {
                    val bengaliVoices = voices.filter { it.locale.language == "bn" }
                    val femaleVoice = bengaliVoices.firstOrNull { voice ->
                        voice.name.contains("female", ignoreCase = true) ||
                        voice.name.contains("bn-bd-x-ban", ignoreCase = true) ||
                        voice.name.contains("-f-", ignoreCase = true) ||
                        voice.features?.any { it.contains("female", ignoreCase = true) } == true
                    } ?: bengaliVoices.firstOrNull()

                    if (femaleVoice != null) {
                        engine.voice = femaleVoice
                    }
                }
            } catch (e: Exception) {
                Log.w("TtsManager", "Unable to filter voices", e)
            }

            engine.setPitch(pitch)
            engine.setSpeechRate(speechRate)

            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    utteranceId?.let { onSpeechStarted(it) }
                }

                override fun onDone(utteranceId: String?) {
                    utteranceId?.let { onSpeechFinished(it) }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    utteranceId?.let { onSpeechFinished(it) }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    utteranceId?.let { onSpeechFinished(it) }
                    Log.e("TtsManager", "TTS error code $errorCode for utterance $utteranceId")
                }
            })
        } else {
            isInitialized = false
            onError("টেক্সট-টু-স্পিচ চালু করা যায়নি।")
        }
    }

    fun speak(text: String, utteranceId: String): Boolean {
        val engine = tts ?: return false
        if (!isInitialized) return false

        stop()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        return result == TextToSpeech.SUCCESS
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun isSpeaking(): Boolean {
        return try {
            tts?.isSpeaking ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }
}
