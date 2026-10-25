package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.MahiAction
import com.example.model.SenderType
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
fun ChatMessageItem(
    message: ChatMessage,
    onSpeakClick: (ChatMessage) -> Unit,
    onAppActionClick: (MahiAction.OpenApp) -> Unit,
    onWhatsAppConfirmClick: (recipient: String, message: String) -> Unit,
    onSystemFeatureClick: (MahiAction.SystemFeature) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUser = message.sender == SenderType.USER

    Row(
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        if (!isUser) {
            // Mahi avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(MahiPrimary, MahiTertiary)
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "মাহী এআই অবতার",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Main message bubble
            val bubbleShape = if (isUser) {
                RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
            } else {
                RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
            }

            val bubbleBackground = if (isUser) {
                Brush.linearGradient(listOf(Color(0xFF6D28D9), Color(0xFF8B5CF6)))
            } else if (message.isError) {
                Brush.linearGradient(listOf(Color(0xFF3F141E), Color(0xFF281017)))
            } else {
                Brush.linearGradient(listOf(Color(0xFF1E1738), Color(0xFF261D45)))
            }

            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleBackground)
                    .border(
                        1.dp,
                        if (isUser) Color.White.copy(alpha = 0.2f)
                        else if (message.isSpeaking) MahiSpeakingGlow.copy(alpha = 0.7f)
                        else Color(0xFF382960),
                        bubbleShape
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag(if (isUser) "user_message_bubble" else "mahi_message_bubble")
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isUser) "আপনি" else "মাহী",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUser) Color(0xFFE2E8F0) else MahiSecondary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (message.isVoice) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "ভয়েসে বলা হয়েছে",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            // Copy message button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Mahi Message", message.text)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "টেক্সট কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "কপি করুন",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            if (!isUser) {
                                Spacer(modifier = Modifier.width(4.dp))
                                // TTS Audio speak / stop toggle button
                                IconButton(
                                    onClick = { onSpeakClick(message) },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = if (message.isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = if (message.isSpeaking) "থামান" else "ভয়েস শুনুন",
                                        tint = if (message.isSpeaking) MahiSpeakingGlow else Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = message.text,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Color.White
                    )
                }
            }

            // Attached Action Card (if any)
            when (val action = message.action) {
                is MahiAction.OpenApp -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    AppActionCard(
                        action = action,
                        onActionClick = { onAppActionClick(action) }
                    )
                }
                is MahiAction.PrepareWhatsApp -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    WhatsAppConfirmCard(
                        action = action,
                        onSendClick = onWhatsAppConfirmClick
                    )
                }
                is MahiAction.ShowTimeDate -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    TimeDateCard(action = action)
                }
                is MahiAction.SystemFeature -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    SystemFeatureCard(
                        action = action,
                        onClick = { onSystemFeatureClick(action) }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun AppActionCard(
    action: MahiAction.OpenApp,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140F28)),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF382960), Color(0xFF5B3B96)))),
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_action_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = action.appName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (action.isInstalled) "অ্যান্ড্রয়েড ফোনে ইনস্টল করা আছে" else "ফোনে ইনস্টল নেই (বিকল্প দেখুন)",
                        fontSize = 12.sp,
                        color = if (action.isInstalled) MahiSuccess else MahiWarning
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (action.isInstalled) MahiSuccess.copy(alpha = 0.2f) else MahiWarning.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = if (action.isInstalled) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (action.isInstalled) MahiSuccess else MahiWarning,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (action.isInstalled) MahiPrimary else Color(0xFF0284C7)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_app_button")
                ) {
                    Icon(
                        imageVector = if (action.isInstalled) Icons.AutoMirrored.Filled.Launch else Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (action.isInstalled) "অ্যাপটি খুলুন" else "Play Store থেকে ইনস্টল",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!action.isInstalled && action.webFallback.isNotBlank()) {
                    val context = LocalContext.current
                    OutlinedButton(
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(action.webFallback)
                            ).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MahiSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ওয়েব", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppConfirmCard(
    action: MahiAction.PrepareWhatsApp,
    onSendClick: (recipient: String, message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var recipientState by remember { mutableStateOf(action.recipient) }
    var messageState by remember { mutableStateOf(action.message) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E19)),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF166534), Color(0xFF22C55E)))),
        modifier = modifier
            .fillMaxWidth()
            .testTag("whatsapp_confirm_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF22C55E).copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "WhatsApp বার্তা নিশ্চিতকরণ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "প্রাপক: $recipientState",
                        fontSize = 12.sp,
                        color = Color(0xFF86EFAC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Message text field for editing
            OutlinedTextField(
                value = messageState,
                onValueChange = { messageState = it },
                label = { Text("বার্তার বিষয়বস্তু", fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFE2E8F0),
                    focusedBorderColor = Color(0xFF22C55E),
                    unfocusedBorderColor = Color(0xFF166534),
                    cursorColor = Color(0xFF22C55E),
                    focusedContainerColor = Color(0xFF07140F),
                    unfocusedContainerColor = Color(0xFF07140F)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("whatsapp_message_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Android Security Guarantee Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A2218), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF86EFAC),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "অ্যান্ড্রয়েড নিরাপত্তা নীতি: আপনার সম্মতি ছাড়া কোনো মেসেজ পাঠানো হবে না। বোতাম চাপলে মেসেজ সহ হোয়াটসঅ্যাপ খুলবে।",
                    fontSize = 11.sp,
                    color = Color(0xFFBBF7D0),
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onSendClick(recipientState, messageState) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_send_whatsapp_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "অনুমতি দিয়ে হোয়াটসঅ্যাপে পাঠান",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TimeDateCard(
    action: MahiAction.ShowTimeDate,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151833)),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF38437D), Color(0xFF06B6D4)))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MahiSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = action.greeting,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MahiSecondary
                    )
                }
                Text(
                    text = action.dayName,
                    fontSize = 13.sp,
                    color = Color(0xFFA5F3FC)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = action.time,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = action.date,
                fontSize = 13.sp,
                color = Color(0xFFCBD5E1)
            )
        }
    }
}

@Composable
fun SystemFeatureCard(
    action: MahiAction.SystemFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1435)),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF4C1D95), MahiPrimary))),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.featureName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = action.description,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = MahiPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Launch,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("চালু করুন", fontSize = 12.sp)
            }
        }
    }
}
