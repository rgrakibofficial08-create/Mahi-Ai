package com.example.ai

import android.content.Context
import android.util.Log
import com.example.apps.AppLauncherHelper
import com.example.apps.KnownAppInfo
import com.example.model.MahiAction
import com.example.util.BengaliUtils
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class MahiAiResponse(
    val spokenText: String,
    val action: MahiAction? = null,
    val isFromGemini: Boolean = false
)

class MahiAiEngine(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun processQuery(rawQuery: String): MahiAiResponse = withContext(Dispatchers.IO) {
        val query = BengaliUtils.cleanSpokenQuery(rawQuery)

        // 1. Check deterministic commands first for instant response & guaranteed reliability
        val deterministicResult = evaluateDeterministicCommand(query)
        if (deterministicResult != null) {
            return@withContext deterministicResult
        }

        // 2. Query Gemini API if API key is present
        val geminiApiKey = getApiKey()
        if (geminiApiKey.isNotBlank() && geminiApiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiResponse = callGeminiApi(query, geminiApiKey)
                if (geminiResponse != null && geminiResponse.spokenText.isNotBlank()) {
                    return@withContext geminiResponse
                }
            } catch (e: Exception) {
                Log.e("MahiAiEngine", "Gemini API call failed", e)
            }
        }

        // 3. Fallback to smart offline/local Bengali conversational responses
        return@withContext generateSmartLocalResponse(query)
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
    }

    private fun evaluateDeterministicCommand(query: String): MahiAiResponse? {
        val lower = query.lowercase().trim()

        // --- TikTok Intent ---
        if (containsAny(lower, "tiktok", "টিকটক", "tik tok")) {
            if (containsAny(lower, "খোলো", "খুলুন", "ওপেন", "open", "চালাও", "দেখাও", "প্লে", "যাও") || lower.length < 15) {
                val isInstalled = AppLauncherHelper.isPackageInstalled(context, "com.zhiliaoapp.musically") ||
                        AppLauncherHelper.isPackageInstalled(context, "com.ss.android.ugc.trill")
                val text = if (isInstalled) {
                    "অবশ্যই, আমি আপনার ফোনে TikTok খুলে দিচ্ছি!"
                } else {
                    "আপনার ফোনে TikTok অ্যাপ ইনস্টল করা নেই। আপনি চাইলে Play Store থেকে ডাউনলোড করতে পারেন অথবা ওয়েবসাইট দেখতে পারেন।"
                }
                return MahiAiResponse(
                    spokenText = text,
                    action = MahiAction.OpenApp(
                        appName = "TikTok",
                        packageName = "com.zhiliaoapp.musically",
                        isInstalled = isInstalled,
                        webFallback = "https://www.tiktok.com",
                        playStoreId = "com.zhiliaoapp.musically"
                    )
                )
            }
        }

        // --- WhatsApp Messaging Intent ("WhatsApp-এ রহিমকে মেসেজ দাও") ---
        if (containsAny(lower, "whatsapp", "হোয়াটসঅ্যাপ", "হোয়াটসএপ", "ওয়াটসঅ্যাপ", "মেসেজ দাও", "মেসেজ পাঠাও", "বার্তা পাঠাও")) {
            val isMessageIntent = containsAny(lower, "মেসেজ", "বার্তা", "message", "টেক্সট", "পাঠাও", "দাও", "লিখে")

            if (isMessageIntent && containsAny(lower, "whatsapp", "হোয়াটসঅ্যাপ", "হোয়াটসএপ", "ওয়াটসঅ্যাপ")) {
                // Extract recipient and optional message body
                var recipient = "রহিম"
                var message = "কেমন আছেন? মাহী এআই এর মাধ্যমে বার্তা পাঠানো হচ্ছে।"

                // Check for patterns like "রহিমকে মেসেজ দাও" or "রহিমকে বলো..."
                val recipientPattern = Pattern.compile("(?:whatsapp[\\s\\-এ]*|হোয়াটসঅ্যাপ[\\s\\-এ]*)(?:মেসেজ পাঠাও|মেসেজ দাও)?\\s*([\\u0980-\\u09FFA-Za-z]+)(?:কে|\\s+)(?:মেসেজ|বার্তা|বলো|জানাও)", Pattern.CASE_INSENSITIVE)
                val matcher = recipientPattern.matcher(query)
                if (matcher.find()) {
                    val candidate = matcher.group(1)?.trim()
                    if (!candidate.isNullOrBlank() && !candidate.equals("whatsapp", ignoreCase = true) && !candidate.equals("হোয়াটসঅ্যাপ", ignoreCase = true)) {
                        recipient = candidate
                    }
                } else {
                    // Try simpler check
                    if (query.contains("রহিম") || query.contains("rahim")) {
                        recipient = "রহিম"
                    }
                }

                // Check if user specified what to say
                val msgPattern = Pattern.compile("(?:বলো|লিখে|মেসেজ|বার্তা)[\\s:]+(.+)$", Pattern.CASE_INSENSITIVE)
                val msgMatcher = msgPattern.matcher(query)
                if (msgMatcher.find()) {
                    val customMsg = msgMatcher.group(1)?.trim()
                    if (!customMsg.isNullOrBlank()) {
                        message = customMsg
                    }
                }

                return MahiAiResponse(
                    spokenText = "আমি হোয়াটসঅ্যাপে ${recipient}-এর জন্য মেসেজটি প্রস্তুত করেছি। আপনার অনুমতি ছাড়া স্বয়ংক্রিয়ভাবে মেসেজ পাঠানো যাবে না। নিচে দেখে 'অনুমতি দিয়ে পাঠান' বোতামে চাপুন।",
                    action = MahiAction.PrepareWhatsApp(
                        recipient = recipient,
                        message = message
                    )
                )
            }

            // Just open WhatsApp
            if (containsAny(lower, "খোলো", "খুলুন", "ওপেন", "open", "যাও") || lower.length < 18) {
                val isInstalled = AppLauncherHelper.isPackageInstalled(context, "com.whatsapp") ||
                        AppLauncherHelper.isPackageInstalled(context, "com.whatsapp.w4b")
                val text = if (isInstalled) {
                    "অবশ্যই, আমি আপনার ফোনে WhatsApp খুলে দিচ্ছি।"
                } else {
                    "আপনার ফোনে WhatsApp ইনস্টল করা নেই। Play Store থেকে ইনস্টল করে নিতে পারেন।"
                }
                return MahiAiResponse(
                    spokenText = text,
                    action = MahiAction.OpenApp(
                        appName = "WhatsApp",
                        packageName = "com.whatsapp",
                        isInstalled = isInstalled,
                        webFallback = "https://web.whatsapp.com",
                        playStoreId = "com.whatsapp"
                    )
                )
            }
        }

        // --- YouTube Intent ---
        if (containsAny(lower, "youtube", "ইউটিউব", "ইউটিপ")) {
            if (containsAny(lower, "খোলো", "খুলুন", "ওপেন", "open", "প্লে", "চালাও") || lower.length < 16) {
                val isInstalled = AppLauncherHelper.isPackageInstalled(context, "com.google.android.youtube")
                return MahiAiResponse(
                    spokenText = "আমি YouTube খুলে দিচ্ছি।",
                    action = MahiAction.OpenApp(
                        appName = "YouTube",
                        packageName = "com.google.android.youtube",
                        isInstalled = isInstalled,
                        webFallback = "https://www.youtube.com",
                        playStoreId = "com.google.android.youtube"
                    )
                )
            }
        }

        // --- Facebook Intent ---
        if (containsAny(lower, "facebook", "ফেসবুক", "এফবি", "fb")) {
            if (containsAny(lower, "খোলো", "খুলুন", "ওপেন", "open") || lower.length < 16) {
                val isInstalled = AppLauncherHelper.isPackageInstalled(context, "com.facebook.katana") ||
                        AppLauncherHelper.isPackageInstalled(context, "com.facebook.lite")
                return MahiAiResponse(
                    spokenText = "আমি Facebook খুলে দিচ্ছি।",
                    action = MahiAction.OpenApp(
                        appName = "Facebook",
                        packageName = "com.facebook.katana",
                        isInstalled = isInstalled,
                        webFallback = "https://www.facebook.com",
                        playStoreId = "com.facebook.katana"
                    )
                )
            }
        }

        // --- Chrome / Browser Intent ---
        if (containsAny(lower, "chrome", "ক্রোম", "ব্রাউজার", "browser")) {
            if (containsAny(lower, "খোলো", "খুলুন", "ওপেন", "open") || lower.length < 15) {
                val isInstalled = AppLauncherHelper.isPackageInstalled(context, "com.android.chrome")
                return MahiAiResponse(
                    spokenText = "আমি Google Chrome খুলে দিচ্ছি।",
                    action = MahiAction.OpenApp(
                        appName = "Google Chrome",
                        packageName = "com.android.chrome",
                        isInstalled = isInstalled,
                        webFallback = "https://www.google.com",
                        playStoreId = "com.android.chrome"
                    )
                )
            }
        }

        // --- Google Maps Intent ---
        if (containsAny(lower, "maps", "ম্যাপ", "গুগল ম্যাপ", "রাস্তা", "লোকেশন")) {
            if (containsAny(lower, "খোলো", "দেখাও", "ওপেন", "খুলুন")) {
                val isInstalled = AppLauncherHelper.isPackageInstalled(context, "com.google.android.apps.maps")
                return MahiAiResponse(
                    spokenText = "আমি Google Maps খুলে দিচ্ছি।",
                    action = MahiAction.OpenApp(
                        appName = "Google Maps",
                        packageName = "com.google.android.apps.maps",
                        isInstalled = isInstalled,
                        webFallback = "https://maps.google.com",
                        playStoreId = "com.google.android.apps.maps"
                    )
                )
            }
        }

        // --- Camera Intent ---
        if (containsAny(lower, "camera", "ক্যামেরা", "ছবি তুলব", "ছবি তোলো")) {
            return MahiAiResponse(
                spokenText = "ক্যামেরা চালু করা হচ্ছে। সুন্দর করে একটি ছবি তুলুন!",
                action = MahiAction.SystemFeature("ক্যামেরা", "camera", "ক্যামেরা অ্যাপ্লিকেশন")
            )
        }

        // --- Calculator Intent ---
        if (containsAny(lower, "calculator", "ক্যালকুলেটর", "হিসাব নিকাশ", "হিসাব করব")) {
            return MahiAiResponse(
                spokenText = "আমি ক্যালকুলেটর খুলে দিচ্ছি।",
                action = MahiAction.SystemFeature("ক্যালকুলেটর", "calculator", "ক্যালকুলেটর টুল")
            )
        }

        // --- Phone / Dialer Intent ---
        if (containsAny(lower, "phone", "ডায়াল", "কল করব", "ফোন কল", "ডায়ালার")) {
            return MahiAiResponse(
                spokenText = "ফোন ডায়ালার খুলে দিচ্ছি।",
                action = MahiAction.SystemFeature("ফোন", "phone", "ফোন ডায়াল প্যাড")
            )
        }

        // --- Settings Intent ---
        if (containsAny(lower, "settings", "সেটিংস", "ফোন সেটিংস")) {
            return MahiAiResponse(
                spokenText = "ফোনের সেটিংস স্ক্রিন খুলে দিচ্ছি।",
                action = MahiAction.SystemFeature("সেটিংস", "settings", "অ্যান্ড্রয়েড সিস্টেম সেটিংস")
            )
        }

        // --- Time & Date Intent ---
        if (containsAny(lower, "কয়টা বাজে", "সময় কত", "time", "টাইম কত", "ঘড়িতে কয়টা", "তারিখ কত", "আজ কি বার", "আজকের দিন")) {
            val time = BengaliUtils.getCurrentBengaliTime()
            val date = BengaliUtils.getCurrentBengaliDate()
            val dayName = BengaliUtils.getDayOfWeekBengali()
            val greeting = BengaliUtils.getCurrentBengaliGreeting()

            val spoken = if (containsAny(lower, "তারিখ", "আজ কি বার", "আজকের দিন")) {
                "$greeting! আজ $date।"
            } else {
                "$greeting! এখন $time।"
            }

            return MahiAiResponse(
                spokenText = spoken,
                action = MahiAction.ShowTimeDate(
                    time = time,
                    date = date,
                    dayName = dayName,
                    greeting = greeting
                )
            )
        }

        return null
    }

    private suspend fun callGeminiApi(query: String, apiKey: String): MahiAiResponse? {
        val systemPrompt = """
            তুমি 'মাহী' (Mahi AI), একজন বুদ্ধিমান, মার্জিত, অত্যন্ত আন্তরিক এবং বিনয়ী বাংলা মেয়েলি ভয়েস অ্যাসিস্ট্যান্ট।
            তুমি সবসময় ব্যবহারকারীর প্রশ্নের মিষ্টি ও সুন্দর স্বাভাবিক ভাষায় বাংলায় উত্তর দাও।
            যেহেতু এই উত্তরটি টেক্সট-টু-স্পিচে ভয়েসের মাধ্যমে ব্যবহারকারীকে শোনানো হবে, তাই উত্তর খুব বেশি দীর্ঘ করবে না। সরাসরি, প্রাঞ্জল ও আকর্ষণীয় বাংলায় উত্তর দাও।
            যদি ব্যবহারকারী এমন কিছু করতে বলেন যা অ্যান্ড্রয়েডের নিরাপত্তানীতির কারণে সরাসরি অনুমতি ছাড়া করা যায় না (যেমন একা একা মেসেজ পাঠানো, গোপনে কিছু করা), তবে বাস্তবসম্মত ব্যাখ্যা ও বিকল্প প্রদান করো।
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "$systemPrompt\n\nব্যবহারকারী বলেছেন: $query"))
                    })
                })
            }
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 500)
            })
        }

        val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.w("MahiAiEngine", "Gemini HTTP response code: ${response.code}")
            return null
        }

        val responseBodyString = response.body?.string().orEmpty()
        val json = JSONObject(responseBodyString)
        val candidates = json.optJSONArray("candidates")
        val content = candidates?.optJSONObject(0)?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text").orEmpty().trim()

        if (text.isNotBlank()) {
            return MahiAiResponse(spokenText = text, isFromGemini = true)
        }
        return null
    }

    private fun generateSmartLocalResponse(query: String): MahiAiResponse {
        val lower = query.lowercase().trim()

        val reply = when {
            containsAny(lower, "কেমন আছো", "কেমন আছেন", "how are you") ->
                "আমি খুব ভালো আছি! আপনার সাথে কথা বলতে পেরে ভীষণ আনন্দ লাগছে। বলুন, আজ আপনাকে কীভাবে সাহায্য করতে পারি?"

            containsAny(lower, "তুমি কে", "তোমার নাম কি", "who are you", "মাহী কে") ->
                "আমি মাহী (Mahi AI), আপনার স্মার্ট বাংলা ভয়েস অ্যাসিস্ট্যান্ট। আমি আপনার কথা শুনতে পারি, অ্যাপ খুলতে পারি, WhatsApp-এ মেসেজ লিখতে সাহায্য করতে পারি এবং যেকোনো প্রশ্নের উত্তর দিতে পারি।"

            containsAny(lower, "ধন্যবাদ", "থ্যাংক ইউ", "অনেক ধন্যবাদ", "thank you") ->
                "আপনাকে অসংখ্য ধন্যবাদ! আপনার যেকোনো প্রয়োজনে আমি সবসময় আপনার পাশে আছি।"

            containsAny(lower, "আবহাওয়া", "বৃষ্টি", "গরম", "weather") ->
                "আজকের আবহাওয়া বেশ উপভোগ্য! বাইরে যাওয়ার আগে আকাশের অবস্থা দেখে ছাতা বা সানগ্লাস সাথে রাখুন।"

            containsAny(lower, "উক্তি", "অনুপ্রেরণা", "মোটিভেশন", "ভালো কথা") ->
                "“সাফল্য কোনো আকস্মিক ঘটনা নয়; এটি কঠোর পরিশ্রম, অধ্যবসায়, শেখা এবং সবচেয়ে বড় কথা নিজের কাজের প্রতি ভালোবাসার ফল।” আপনি এগিয়ে যান, আপনি পারবেন!"

            containsAny(lower, "অনুবাদ", "translate", "ইংরেজি", "বাংলায় বলো") ->
                "আমি যেকোনো বাক্য বাংলায় বা ইংরেজিতে অনুবাদ করতে পারি। আপনি যে বাক্যটি অনুবাদ করতে চান তা আমাকে স্পষ্ট করে বলুন।"

            containsAny(lower, "গান", "গান শোনাও", "গান গাও", "sing") ->
                "“আমার সোনার বাংলা, আমি তোমায় ভালোবাসি!” আপনার প্রিয় গান শুনতে চাইলে আপনি “Mahi, YouTube খোলো” বলতে পারেন, আমি সাথে সাথে ইউটিউব খুলে দেব!"

            containsAny(lower, "কৌতুক", "হাসাও", "জোক", "joke") ->
                "বলুন তো শিক্ষক ছাত্রকে জিজ্ঞেস করলেন: ‘পৃথিবী কেন ঘোরে?’ ছাত্র বলল: ‘স্যার, আপনি এত কঠিন অংক করালে তো পৃথিবীর মাথাও ঘুরবেই!’"

            containsAny(lower, "হাই", "হ্যালো", "সালাম", "নমস্কার", "hello", "hi") ->
                "${BengaliUtils.getCurrentBengaliGreeting()}! আমি মাহী বলছি। বলুন, আজ আপনার জন্য কী করতে পারি?"

            else ->
                "আমি আপনার কথা বুঝতে পেরেছি। আপনি আমাকে TikTok বা WhatsApp খুলতে বলতে পারেন, কারও জন্য মেসেজ তৈরি করতে বলতে পারেন, অথবা সময় ও অন্যান্য বিষয়ে প্রশ্ন করতে পারেন। আমি কীভাবে সাহায্য করব?"
        }

        return MahiAiResponse(spokenText = reply, isFromGemini = false)
    }

    private fun containsAny(text: String, vararg targets: String): Boolean {
        for (target in targets) {
            if (text.contains(target, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
