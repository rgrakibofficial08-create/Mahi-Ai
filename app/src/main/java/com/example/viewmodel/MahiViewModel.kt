package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.MahiAiEngine
import com.example.apps.AppLauncherHelper
import com.example.model.AssistantState
import com.example.model.AssistantVoiceSettings
import com.example.model.ChatMessage
import com.example.model.MahiAction
import com.example.model.SenderType
import com.example.voice.SpeechRecognitionManager
import com.example.voice.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MahiViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _assistantState = MutableStateFlow<AssistantState>(AssistantState.Idle)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _voiceSettings = MutableStateFlow(AssistantVoiceSettings())
    val voiceSettings: StateFlow<AssistantVoiceSettings> = _voiceSettings.asStateFlow()

    private val _isAudioPermissionGranted = MutableStateFlow(false)
    val isAudioPermissionGranted: StateFlow<Boolean> = _isAudioPermissionGranted.asStateFlow()

    private val aiEngine = MahiAiEngine(context)

    private val ttsManager: TtsManager by lazy {
        TtsManager(
            context = context,
            onSpeechStarted = { messageId ->
                _assistantState.value = AssistantState.Speaking(messageId)
                updateMessageSpeakingStatus(messageId, true)
            },
            onSpeechFinished = { messageId ->
                _assistantState.value = AssistantState.Idle
                updateMessageSpeakingStatus(messageId, false)
            },
            onError = {
                _assistantState.value = AssistantState.Idle
            }
        )
    }

    private val speechRecognitionManager: SpeechRecognitionManager by lazy {
        SpeechRecognitionManager(
            context = context,
            onStateChanged = { state ->
                when (state) {
                    is SpeechRecognitionManager.SpeechUiState.Idle -> {
                        if (_assistantState.value is AssistantState.Listening) {
                            _assistantState.value = AssistantState.Idle
                        }
                    }
                    is SpeechRecognitionManager.SpeechUiState.Listening -> {
                        _assistantState.value = AssistantState.Listening(state.rmsDb, state.partialText)
                    }
                    is SpeechRecognitionManager.SpeechUiState.Processing -> {
                        _assistantState.value = AssistantState.Thinking
                    }
                }
            },
            onSpeechRecognized = { recognizedText ->
                processUserQuery(recognizedText, isVoice = true)
            },
            onError = { errorMessage ->
                _assistantState.value = AssistantState.Idle
                addErrorMessage(errorMessage)
            }
        )
    }

    init {
        sendWelcomeGreeting()
    }

    private fun sendWelcomeGreeting() {
        val welcomeMessage = ChatMessage(
            sender = SenderType.MAHI,
            text = "নমস্কার / আসসালামু আলাইকুম! আমি মাহী (Mahi AI), আপনার স্মার্ট বাংলা পার্সোনাল ভয়েস অ্যাসিস্ট্যান্ট।\n\nআপনি মুখে বাংলায় যা বলবেন, আমি তা শুনব ও সাহায্য করব। যেমন:\n• “Mahi, TikTok খোলো” 🎵\n• “Mahi, WhatsApp খোলো” 💬\n• “WhatsApp-এ রহিমকে মেসেজ দাও” ✍️\n• “এখন কয়টা বাজে?” 🕒\n\nনিচের মাইক্রোফোন চেপে কথা বলা শুরু করুন!",
            isVoice = false
        )
        _messages.value = listOf(welcomeMessage)
    }

    fun setAudioPermissionGranted(granted: Boolean) {
        _isAudioPermissionGranted.value = granted
    }

    fun toggleVoiceListening() {
        if (_assistantState.value is AssistantState.Listening) {
            stopVoiceListening()
        } else {
            startVoiceListening()
        }
    }

    fun startVoiceListening() {
        // Stop any running speech first
        ttsManager.stop()
        speechRecognitionManager.startListening()
    }

    fun stopVoiceListening() {
        speechRecognitionManager.stopListening()
    }

    fun cancelVoiceListening() {
        speechRecognitionManager.cancel()
        _assistantState.value = AssistantState.Idle
    }

    fun processUserQuery(query: String, isVoice: Boolean) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        // 1. Add user message
        val userMsg = ChatMessage(
            sender = SenderType.USER,
            text = trimmed,
            isVoice = isVoice
        )
        _messages.update { it + userMsg }

        // 2. Set thinking state
        _assistantState.value = AssistantState.Thinking

        // 3. Process AI query asynchronously
        viewModelScope.launch {
            try {
                val response = aiEngine.processQuery(trimmed)

                val mahiMsg = ChatMessage(
                    sender = SenderType.MAHI,
                    text = response.spokenText,
                    action = response.action,
                    isVoice = false
                )
                _messages.update { it + mahiMsg }

                // 4. Handle auto-launch if requested and installed
                handleAutoActionExecution(response.action)

                // 5. Speak response if enabled
                if (_voiceSettings.value.isTtsEnabled) {
                    ttsManager.speak(response.spokenText, mahiMsg.id)
                } else {
                    _assistantState.value = AssistantState.Idle
                }
            } catch (e: Exception) {
                _assistantState.value = AssistantState.Idle
                addErrorMessage("উত্তর তৈরি করতে সমস্যা হয়েছে: ${e.localizedMessage}")
            }
        }
    }

    private fun handleAutoActionExecution(action: MahiAction?) {
        when (action) {
            is MahiAction.OpenApp -> {
                if (action.isInstalled) {
                    AppLauncherHelper.launchPackage(context, action.packageName)
                }
            }
            is MahiAction.SystemFeature -> {
                AppLauncherHelper.launchSystemFeature(context, action.actionType)
            }
            is MahiAction.PrepareWhatsApp -> {
                // Per user request & security guideline: DO NOT auto send without explicit user permission/click
            }
            else -> {}
        }
    }

    fun executeAppLaunch(action: MahiAction.OpenApp) {
        if (action.isInstalled) {
            val launched = AppLauncherHelper.launchPackage(context, action.packageName)
            if (!launched) {
                AppLauncherHelper.openWebUrl(context, action.webFallback)
            }
        } else {
            val playStoreOpened = AppLauncherHelper.openInPlayStore(context, action.playStoreId)
            if (!playStoreOpened && action.webFallback.isNotBlank()) {
                AppLauncherHelper.openWebUrl(context, action.webFallback)
            }
        }
    }

    fun executeSystemFeature(action: MahiAction.SystemFeature) {
        AppLauncherHelper.launchSystemFeature(context, action.actionType)
    }

    fun confirmAndSendWhatsApp(recipient: String, message: String) {
        val success = AppLauncherHelper.sendWhatsAppMessageWithConfirmation(context, recipient, message)
        if (success) {
            val confirmReply = "আমি হোয়াটসঅ্যাপ ওপেন করে দিয়েছি। অনুগ্রহ করে প্রাপক যাচাই করে মেসেজটি পাঠিয়ে দিন।"
            val confirmMsg = ChatMessage(
                sender = SenderType.MAHI,
                text = confirmReply,
                isVoice = false
            )
            _messages.update { it + confirmMsg }
            if (_voiceSettings.value.isTtsEnabled) {
                ttsManager.speak(confirmReply, confirmMsg.id)
            }
        }
    }

    fun replayMessageSpeech(message: ChatMessage) {
        if (ttsManager.isSpeaking() && _assistantState.value is AssistantState.Speaking &&
            (_assistantState.value as AssistantState.Speaking).messageId == message.id) {
            ttsManager.stop()
            _assistantState.value = AssistantState.Idle
            updateMessageSpeakingStatus(message.id, false)
        } else {
            ttsManager.stop()
            ttsManager.speak(message.text, message.id)
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
        _assistantState.value = AssistantState.Idle
        _messages.update { list -> list.map { it.copy(isSpeaking = false) } }
    }

    fun toggleTtsEnabled() {
        _voiceSettings.update { it.copy(isTtsEnabled = !it.isTtsEnabled) }
        if (!_voiceSettings.value.isTtsEnabled) {
            stopSpeaking()
        }
    }

    fun updatePitch(newPitch: Float) {
        _voiceSettings.update { it.copy(pitch = newPitch) }
        ttsManager.pitch = newPitch
    }

    fun updateSpeechRate(newRate: Float) {
        _voiceSettings.update { it.copy(speechRate = newRate) }
        ttsManager.speechRate = newRate
    }

    fun clearMessages() {
        stopSpeaking()
        speechRecognitionManager.cancel()
        _messages.value = emptyList()
        sendWelcomeGreeting()
    }

    private fun updateMessageSpeakingStatus(messageId: String, isSpeaking: Boolean) {
        _messages.update { list ->
            list.map { msg ->
                if (msg.id == messageId) msg.copy(isSpeaking = isSpeaking) else msg.copy(isSpeaking = false)
            }
        }
    }

    private fun addErrorMessage(text: String) {
        val errorMsg = ChatMessage(
            sender = SenderType.MAHI,
            text = text,
            isError = true
        )
        _messages.update { it + errorMsg }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.destroy()
        ttsManager.shutdown()
    }
}
