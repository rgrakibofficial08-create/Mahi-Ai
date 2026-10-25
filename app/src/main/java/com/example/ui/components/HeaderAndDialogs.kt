package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssistantState
import com.example.model.AssistantVoiceSettings
import com.example.ui.theme.MahiBackground
import com.example.ui.theme.MahiCardBorder
import com.example.ui.theme.MahiListeningGlow
import com.example.ui.theme.MahiPrimary
import com.example.ui.theme.MahiPrimaryLight
import com.example.ui.theme.MahiSecondary
import com.example.ui.theme.MahiSpeakingGlow
import com.example.ui.theme.MahiSuccess
import com.example.ui.theme.MahiSurface
import com.example.ui.theme.MahiSurfaceVariant
import com.example.ui.theme.MahiTertiary
import com.example.ui.theme.MahiWarning

@Composable
fun MahiTopBar(
    assistantState: AssistantState,
    voiceSettings: AssistantVoiceSettings,
    onToggleTts: () -> Unit,
    onShowHelp: () -> Unit,
    onShowSettings: () -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MahiBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left: Logo & Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MahiPrimary, MahiTertiary)
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Mahi AI",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mahi AI",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MahiPrimary.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "বাংলা",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MahiPrimaryLight,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }

                        // Assistant status text
                        val statusText = when (assistantState) {
                            is AssistantState.Listening -> "শুনছি... বলুন 🎙️"
                            is AssistantState.Thinking -> "চিন্তা করছি... 💭"
                            is AssistantState.Speaking -> "কথা বলছি... 🔊"
                            else -> "কথা বলতে প্রস্তুত 🟢"
                        }
                        val statusColor = when (assistantState) {
                            is AssistantState.Listening -> MahiListeningGlow
                            is AssistantState.Speaking -> MahiSpeakingGlow
                            is AssistantState.Thinking -> MahiSecondary
                            else -> Color(0xFF94A3B8)
                        }

                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            color = statusColor
                        )
                    }
                }

                // Right: Action buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mute/Unmute TTS
                    IconButton(
                        onClick = onToggleTts,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("toggle_voice_audio_button")
                    ) {
                        Icon(
                            imageVector = if (voiceSettings.isTtsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = if (voiceSettings.isTtsEnabled) "ভয়েস চালু" else "ভয়েস বন্ধ",
                            tint = if (voiceSettings.isTtsEnabled) MahiSecondary else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Help & Commands Guide
                    IconButton(
                        onClick = onShowHelp,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("help_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "কমান্ড গাইড ও নিয়মাবলী",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Settings
                    IconButton(
                        onClick = onShowSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "ভয়েস সেটিংস",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Clear chat
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "চ্যাট মুছে ফেলুন",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommandGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF16102E),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MahiSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mahi AI ব্যবহারের নির্দেশিকা",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "আপনি মুখে বাংলায় যা বলতে পারেন:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MahiPrimaryLight
                )

                Spacer(modifier = Modifier.height(8.dp))

                CommandHelpRow("🎵 “Mahi, TikTok খোলো”", "ফোনে টিকটক অ্যাপ থাকলে খুলবে, না থাকলে Play Store বা ওয়েবসাইটের অপশন দেখাবে।")
                CommandHelpRow("💬 “Mahi, WhatsApp খোলো”", "ফোনে সরাসরি WhatsApp ওপেন করবে।")
                CommandHelpRow("✍️ “WhatsApp-এ রহিমকে মেসেজ দাও”", "মেসেজ তৈরি করবে এবং আপনার অনুমতি নেওয়ার পর পাঠানোর জন্য প্রস্তুত করবে।")
                CommandHelpRow("🕒 “এখন কয়টা বাজে?”", "বর্তমান সঠিক সময় বাংলায় বলবে।")
                CommandHelpRow("📅 “আজ কি বার ও কত তারিখ?”", "আজকের বাংলা ও গ্রেগরিয়ান তারিখ ও বারের নাম জানাবে।")
                CommandHelpRow("▶️ “YouTube খোলো”", "ইউটিউব অ্যাপ চালু করবে।")
                CommandHelpRow("🌐 “ফেসবুক / ক্রোম / ম্যাপস খোলো”", "ফোনের যেকোনো সমর্থিত অ্যাপ সহজেই খুলবে।")
                CommandHelpRow("📷 “ক্যামেরা / ক্যালকুলেটর খোলো”", "ছবি তোলার ক্যামেরা ও হিসাবের ক্যালকুলেটর অন করবে।")
                CommandHelpRow("🤖 সাধারণ প্রশ্ন ও অনুবাদ", "Gemini AI-এর মাধ্যমে বাংলায় যেকোনো প্রশ্নের উত্তর ও অনুবাদ দেবে।")

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFF382960))
                Spacer(modifier = Modifier.height(10.dp))

                // Security notice
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MahiSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "নিরাপত্তা ও অ্যান্ড্রয়েড সিস্টেম তথ্য:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF86EFAC)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Android অপারেটিং সিস্টেম নিরাপত্তার খাতিরে কোনো থার্ড-পার্টি অ্যাপকে গোপনে বা ব্যবহারকারীর অনুমতি ছাড়া কারও কাছে মেসেজ পাঠাতে দেয় না। তাই মাহী আপনার মেসেজ তৈরি করে নিশ্চিতকরণ কার্ডে দেখাবে, এবং আপনার সম্মতিতে ১-ট্যাপে পাঠাবে।",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MahiPrimary)
            ) {
                Text("বুঝেছি", color = Color.White)
            }
        }
    )
}

@Composable
private fun CommandHelpRow(command: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = command,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = description,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            lineHeight = 15.sp
        )
    }
}

@Composable
fun SettingsDialog(
    voiceSettings: AssistantVoiceSettings,
    onPitchChange: (Float) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var pitch by remember { mutableFloatStateOf(voiceSettings.pitch) }
    var speed by remember { mutableFloatStateOf(voiceSettings.speechRate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF16102E),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MahiPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ভয়েস ও অ্যাসিস্ট্যান্ট সেটিংস",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Voice Pitch
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("কণ্ঠের সুর (Pitch - মেয়েলি সুর):", fontSize = 13.sp, color = Color.White)
                    Text("${String.format("%.2f", pitch)}x", fontSize = 13.sp, color = MahiSecondary)
                }
                Slider(
                    value = pitch,
                    onValueChange = {
                        pitch = it
                        onPitchChange(it)
                    },
                    valueRange = 0.8f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = MahiSecondary,
                        activeTrackColor = MahiSecondary,
                        inactiveTrackColor = Color(0xFF382960)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Speech Rate
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("কথা বলার গতি (Speed):", fontSize = 13.sp, color = Color.White)
                    Text("${String.format("%.2f", speed)}x", fontSize = 13.sp, color = MahiPrimaryLight)
                }
                Slider(
                    value = speed,
                    onValueChange = {
                        speed = it
                        onSpeechRateChange(it)
                    },
                    valueRange = 0.7f..1.3f,
                    colors = SliderDefaults.colors(
                        thumbColor = MahiPrimary,
                        activeTrackColor = MahiPrimary,
                        inactiveTrackColor = Color(0xFF382960)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFF382960))
                Spacer(modifier = Modifier.height(10.dp))

                // Gemini AI Engine status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MahiTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gemini AI ইঞ্জিন: সক্রিয় (হাইব্রিড বাংলা মোড)",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AI Studio Secrets প্যানেলে আপনার নিজস্ব GEMINI_API_KEY প্রদান করতে পারেন। অফলাইনেও মাহী অ্যাপ খোলা ও প্রয়োজনীয় উত্তর দিতে সক্ষম।",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 15.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MahiPrimary)
            ) {
                Text("সম্পন্ন", color = Color.White)
            }
        }
    )
}
