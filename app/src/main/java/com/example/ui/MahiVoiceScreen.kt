package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.core.content.ContextCompat
import com.example.model.AssistantState
import com.example.ui.components.AudioWaveformIndicator
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.CommandGuideDialog
import com.example.ui.components.GlowingMicButton
import com.example.ui.components.MahiTopBar
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.MahiBackground
import com.example.ui.theme.MahiCardBorder
import com.example.ui.theme.MahiListeningGlow
import com.example.ui.theme.MahiPrimary
import com.example.ui.theme.MahiPrimaryLight
import com.example.ui.theme.MahiSecondary
import com.example.ui.theme.MahiSpeakingGlow
import com.example.ui.theme.MahiSurface
import com.example.ui.theme.MahiSurfaceVariant
import com.example.ui.theme.MahiWarning
import com.example.viewmodel.MahiViewModel
import kotlinx.coroutines.launch

@Composable
fun MahiVoiceScreen(
    viewModel: MahiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages by viewModel.messages.collectAsState()
    val assistantState by viewModel.assistantState.collectAsState()
    val voiceSettings by viewModel.voiceSettings.collectAsState()
    val isPermissionGranted by viewModel.isAudioPermissionGranted.collectAsState()

    var showHelpDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showTextInput by remember { mutableStateOf(false) }
    var textInputState by remember { mutableStateOf("") }

    // Audio record permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setAudioPermissionGranted(granted)
        if (granted) {
            viewModel.startVoiceListening()
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setAudioPermissionGranted(hasPermission)
    }

    // Scroll to bottom when messages change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = MahiBackground,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            MahiTopBar(
                assistantState = assistantState,
                voiceSettings = voiceSettings,
                onToggleTts = { viewModel.toggleTtsEnabled() },
                onShowHelp = { showHelpDialog = true },
                onShowSettings = { showSettingsDialog = true },
                onClearChat = { viewModel.clearMessages() }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission Banner (if not yet granted)
            if (!isPermissionGranted) {
                Surface(
                    color = Color(0xFF2C1E0A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MahiWarning.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MahiWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "বাংলায় কথা বলতে মাইক্রোফোনের অনুমতি দিন",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MahiWarning),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("grant_mic_permission_button")
                        ) {
                            Text("অনুমতি দিন", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Chat Messages Stream
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        onSpeakClick = { viewModel.replayMessageSpeech(it) },
                        onAppActionClick = { viewModel.executeAppLaunch(it) },
                        onWhatsAppConfirmClick = { recipient, msg ->
                            viewModel.confirmAndSendWhatsApp(recipient, msg)
                        },
                        onSystemFeatureClick = { viewModel.executeSystemFeature(it) }
                    )
                }
            }

            // Quick suggestion chips
            val suggestions = listOf(
                "🎵 TikTok খোলো",
                "💬 WhatsApp খোলো",
                "✍️ রহিমকে মেসেজ দাও",
                "🕒 এখন কয়টা বাজে?",
                "📅 আজকের তারিখ কত?",
                "▶️ YouTube খোলো",
                "📷 ক্যামেরা খোলো",
                "✨ অনুপ্রেরণামূলক উক্তি"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                suggestions.forEach { chipText ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E1738),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF382960), Color(0xFF4C1D95)))
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable {
                                val cleanText = chipText.substringAfter(" ").trim()
                                viewModel.processUserQuery(cleanText, isVoice = false)
                            }
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Real-time audio waveform / partial speech transcript banner
            if (assistantState is AssistantState.Listening) {
                val listening = assistantState as AssistantState.Listening
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    AudioWaveformIndicator(
                        rmsDb = listening.rmsDb,
                        isListening = true
                    )

                    if (listening.partialText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "“${listening.partialText}”",
                            fontSize = 13.sp,
                            color = MahiListeningGlow,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bottom Control Area
            Surface(
                color = Color(0xFF130E26),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.navigationBars.asPaddingValues())
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (showTextInput) {
                        // Text input field with send button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = textInputState,
                                onValueChange = { textInputState = it },
                                placeholder = { Text("বাংলায় লিখুন...", fontSize = 14.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = MahiPrimary,
                                    unfocusedBorderColor = Color(0xFF382960),
                                    focusedContainerColor = Color(0xFF1B1433),
                                    unfocusedContainerColor = Color(0xFF1B1433),
                                    cursorColor = MahiPrimary
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("text_query_input")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (textInputState.isNotBlank()) {
                                        viewModel.processUserQuery(textInputState, isVoice = false)
                                        textInputState = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MahiPrimary)
                                    .testTag("send_text_query_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "পাঠান",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = { showTextInput = false },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "বন্ধ করুন",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    } else {
                        // Central Glowing Voice Button + Keyboard Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Left spacer / info
                            Box(modifier = Modifier.size(48.dp))

                            // Glowing Central Microphone Button
                            GlowingMicButton(
                                assistantState = assistantState,
                                onClick = {
                                    if (!isPermissionGranted) {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        viewModel.toggleVoiceListening()
                                    }
                                }
                            )

                            // Right: Keyboard toggle button
                            IconButton(
                                onClick = { showTextInput = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF261D45))
                                    .testTag("toggle_keyboard_input_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Keyboard,
                                    contentDescription = "লিখে কথা বলুন",
                                    tint = Color(0xFFCBD5E1),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Guidance subtext
                        val guidanceText = when (assistantState) {
                            is AssistantState.Listening -> "মাহী শুনছে... মুখে কথা বলুন (থামাতে বোতাম চাপুন)"
                            is AssistantState.Thinking -> "মাহী চিন্তা করছে..."
                            is AssistantState.Speaking -> "মাহী কথা বলছে (থামাতে বোতাম চাপুন)"
                            else -> "কথা বলতে বড় বোতামে চাপুন"
                        }

                        Text(
                            text = guidanceText,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Command Guide dialog
    if (showHelpDialog) {
        CommandGuideDialog(
            onDismiss = { showHelpDialog = false }
        )
    }

    // Voice & assistant settings dialog
    if (showSettingsDialog) {
        SettingsDialog(
            voiceSettings = voiceSettings,
            onPitchChange = { viewModel.updatePitch(it) },
            onSpeechRateChange = { viewModel.updateSpeechRate(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
