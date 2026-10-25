package com.example.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import java.net.URLEncoder

data class KnownAppInfo(
    val nameEn: String,
    val nameBn: String,
    val primaryPackage: String,
    val alternativePackages: List<String> = emptyList(),
    val webFallbackUrl: String,
    val isSystemFeature: Boolean = false
)

object AppLauncherHelper {

    val POPULAR_APPS = listOf(
        KnownAppInfo(
            nameEn = "TikTok",
            nameBn = "টিকটক",
            primaryPackage = "com.zhiliaoapp.musically",
            alternativePackages = listOf("com.ss.android.ugc.trill"),
            webFallbackUrl = "https://www.tiktok.com"
        ),
        KnownAppInfo(
            nameEn = "WhatsApp",
            nameBn = "হোয়াটসঅ্যাপ",
            primaryPackage = "com.whatsapp",
            alternativePackages = listOf("com.whatsapp.w4b"),
            webFallbackUrl = "https://web.whatsapp.com"
        ),
        KnownAppInfo(
            nameEn = "YouTube",
            nameBn = "ইউটিউব",
            primaryPackage = "com.google.android.youtube",
            webFallbackUrl = "https://www.youtube.com"
        ),
        KnownAppInfo(
            nameEn = "Facebook",
            nameBn = "ফেসবুক",
            primaryPackage = "com.facebook.katana",
            alternativePackages = listOf("com.facebook.lite"),
            webFallbackUrl = "https://www.facebook.com"
        ),
        KnownAppInfo(
            nameEn = "Chrome",
            nameBn = "গুগল ক্রোম",
            primaryPackage = "com.android.chrome",
            webFallbackUrl = "https://www.google.com"
        ),
        KnownAppInfo(
            nameEn = "Google Maps",
            nameBn = "গুগল ম্যাপস",
            primaryPackage = "com.google.android.apps.maps",
            webFallbackUrl = "https://maps.google.com"
        ),
        KnownAppInfo(
            nameEn = "Camera",
            nameBn = "ক্যামেরা",
            primaryPackage = "android.media.action.IMAGE_CAPTURE",
            webFallbackUrl = "",
            isSystemFeature = true
        ),
        KnownAppInfo(
            nameEn = "Calculator",
            nameBn = "ক্যালকুলেটর",
            primaryPackage = "com.google.android.calculator",
            webFallbackUrl = "",
            isSystemFeature = true
        ),
        KnownAppInfo(
            nameEn = "Settings",
            nameBn = "ফোন সেটিংস",
            primaryPackage = "android.settings.SETTINGS",
            webFallbackUrl = "",
            isSystemFeature = true
        ),
        KnownAppInfo(
            nameEn = "Phone",
            nameBn = "ফোন কল",
            primaryPackage = "android.intent.action.DIAL",
            webFallbackUrl = "",
            isSystemFeature = true
        ),
        KnownAppInfo(
            nameEn = "Clock",
            nameBn = "ঘড়ি ও অ্যালার্ম",
            primaryPackage = "android.intent.action.SHOW_ALARMS",
            webFallbackUrl = "",
            isSystemFeature = true
        )
    )

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    fun findInstalledPackageForApp(context: Context, app: KnownAppInfo): String? {
        if (isPackageInstalled(context, app.primaryPackage)) {
            return app.primaryPackage
        }
        for (alt in app.alternativePackages) {
            if (isPackageInstalled(context, alt)) {
                return alt
            }
        }
        return null
    }

    fun launchPackage(context: Context, packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun openInPlayStore(context: Context, packageName: String): Boolean {
        return try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
            true
        } catch (_: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun openWebUrl(context: Context, url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun launchSystemFeature(context: Context, featureType: String): Boolean {
        return try {
            val intent = when (featureType.lowercase()) {
                "camera", "android.media.action.image_capture" -> Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                "calculator", "calc" -> {
                    // Try direct launcher intent or general calculator category
                    val calcIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.calculator")
                        ?: context.packageManager.getLaunchIntentForPackage("com.sec.android.app.popupcalculator")
                        ?: Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_CALCULATOR) }
                    calcIntent
                }
                "settings", "android.settings.settings" -> Intent(Settings.ACTION_SETTINGS)
                "phone", "dial", "android.intent.action.dial" -> Intent(Intent.ACTION_DIAL)
                "clock", "alarm", "android.intent.action.show_alarms" -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
                else -> null
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Prepares and launches WhatsApp message intent with user consent.
     * Complies with Android sandboxing and WhatsApp policies.
     */
    fun sendWhatsAppMessageWithConfirmation(context: Context, recipient: String, message: String): Boolean {
        return try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val isInstalled = isPackageInstalled(context, "com.whatsapp") || isPackageInstalled(context, "com.whatsapp.w4b")

            if (isInstalled) {
                // Try WhatsApp api view intent
                val uri = Uri.parse("https://api.whatsapp.com/send?text=$encodedMessage")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    return true
                } catch (_: Exception) {
                    // Fallback to general send intent targeted at whatsapp
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage("com.whatsapp")
                        putExtra(Intent.EXTRA_TEXT, message)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(shareIntent)
                    return true
                }
            } else {
                // Offer browser WhatsApp or general send sheet
                val webUri = Uri.parse("https://api.whatsapp.com/send?text=$encodedMessage")
                val intent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
