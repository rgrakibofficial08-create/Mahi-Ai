package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechRecognitionManager(
    private val context: Context,
    private val onStateChanged: (SpeechUiState) -> Unit,
    private val onSpeechRecognized: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    sealed class SpeechUiState {
        object Idle : SpeechUiState()
        data class Listening(val rmsDb: Float, val partialText: String = "") : SpeechUiState()
        object Processing : SpeechUiState()
    }

    private var speechRecognizer: SpeechRecognizer? = null
    var isCurrentlyListening: Boolean = false
        private set

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }
            } catch (e: Exception) {
                Log.e("SpeechRecognizer", "Error creating SpeechRecognizer", e)
            }
        }
    }

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        if (isCurrentlyListening) return

        if (speechRecognizer == null) {
            initRecognizer()
        }

        val recognizer = speechRecognizer
        if (recognizer == null) {
            onError("ডিভাইসে স্পিচ রিকগনিশন সার্ভিস প্রস্তুত নয়। টেক্সট লিখে কথা বলুন।")
            return
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("bn-BD", "bn-IN", "bn"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            isCurrentlyListening = true
            onStateChanged(SpeechUiState.Listening(0f, ""))
            recognizer.startListening(intent)
        } catch (e: Exception) {
            isCurrentlyListening = false
            onStateChanged(SpeechUiState.Idle)
            onError("মাইক্রোফোন চালু করতে সমস্যা হয়েছে: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        if (!isCurrentlyListening) return
        try {
            speechRecognizer?.stopListening()
            isCurrentlyListening = false
            onStateChanged(SpeechUiState.Processing)
        } catch (_: Exception) {
            isCurrentlyListening = false
            onStateChanged(SpeechUiState.Idle)
        }
    }

    fun cancel() {
        isCurrentlyListening = false
        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {}
        onStateChanged(SpeechUiState.Idle)
    }

    fun destroy() {
        isCurrentlyListening = false
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isCurrentlyListening = true
                onStateChanged(SpeechUiState.Listening(0f, ""))
            }

            override fun onBeginningOfSpeech() {
                onStateChanged(SpeechUiState.Listening(2f, ""))
            }

            override fun onRmsChanged(rmsdB: Float) {
                if (isCurrentlyListening) {
                    val normalized = if (rmsdB < 0f) 0f else rmsdB
                    onStateChanged(SpeechUiState.Listening(normalized))
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isCurrentlyListening = false
                onStateChanged(SpeechUiState.Processing)
            }

            override fun onError(error: Int) {
                isCurrentlyListening = false
                onStateChanged(SpeechUiState.Idle)

                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "অডিও রেকর্ডিংয়ে ত্রুটি হয়েছে"
                    SpeechRecognizer.ERROR_CLIENT -> "স্পিচ ক্লায়েন্ট সমস্যা। আবার চেষ্টা করুন।"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "মাইক্রোফোন ব্যবহারের অনুমতি দিন।"
                    SpeechRecognizer.ERROR_NETWORK -> "ইন্টারনেট সংযোগ চেক করুন।"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ইন্টারনেট টাইমআউট হয়েছে।"
                    SpeechRecognizer.ERROR_NO_MATCH -> "আপনার কথা পরিষ্কার বোঝা যায়নি। আবার বলুন।"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "ভয়েস সার্ভিস ব্যস্ত, কিছুক্ষণ পর চেষ্টা করুন।"
                    SpeechRecognizer.ERROR_SERVER -> "সার্ভারে ত্রুটি হয়েছে।"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "কোনো কথা শোনা যায়নি।"
                    else -> "কথা সনাক্তকরণে সমস্যা হয়েছে।"
                }
                // Do not show error on gentle speech timeouts if user just cancelled or was silent
                if (error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT && error != SpeechRecognizer.ERROR_NO_MATCH) {
                    onError(errorMessage)
                }
            }

            override fun onResults(results: Bundle?) {
                isCurrentlyListening = false
                onStateChanged(SpeechUiState.Idle)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull()?.trim()
                if (!recognizedText.isNullOrEmpty()) {
                    onSpeechRecognized(recognizedText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull()?.trim().orEmpty()
                if (partial.isNotEmpty() && isCurrentlyListening) {
                    onStateChanged(SpeechUiState.Listening(4f, partial))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
