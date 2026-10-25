package com.example.model

import java.util.UUID

enum class SenderType {
    USER,
    MAHI
}

sealed class MahiAction {
    data class OpenApp(
        val appName: String,
        val packageName: String,
        val isInstalled: Boolean,
        val webFallback: String = "",
        val playStoreId: String = packageName
    ) : MahiAction()

    data class PrepareWhatsApp(
        val recipient: String,
        val message: String,
        val phoneNumber: String? = null,
        val isConfirmed: Boolean = false
    ) : MahiAction()

    data class ShowTimeDate(
        val time: String,
        val date: String,
        val dayName: String,
        val greeting: String
    ) : MahiAction()

    data class WebSearch(
        val query: String,
        val url: String
    ) : MahiAction()

    data class SystemFeature(
        val featureName: String,
        val actionType: String,
        val description: String
    ) : MahiAction()
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: SenderType,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val action: MahiAction? = null,
    val isVoice: Boolean = false,
    val isSpeaking: Boolean = false,
    val isError: Boolean = false
)

sealed class AssistantState {
    object Idle : AssistantState()
    data class Listening(val rmsDb: Float = 0f, val partialText: String = "") : AssistantState()
    object Thinking : AssistantState()
    data class Speaking(val messageId: String) : AssistantState()
    data class Error(val message: String) : AssistantState()
}

data class AssistantVoiceSettings(
    val pitch: Float = 1.08f,         // Slightly elevated for friendly feminine tone
    val speechRate: Float = 0.95f,    // Clear and natural cadence for Bengali
    val isTtsEnabled: Boolean = true,
    val autoListenAfterReply: Boolean = false
)
