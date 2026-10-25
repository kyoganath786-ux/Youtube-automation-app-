package com.example.ui.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object YouTubeAppHelper {

    const val PACKAGE_YOUTUBE = "com.google.android.youtube"
    const val PACKAGE_YOUTUBE_STUDIO = "com.google.android.apps.youtube.creator"

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isYouTubeInstalled(context: Context): Boolean = isAppInstalled(context, PACKAGE_YOUTUBE)
    fun isYouTubeStudioInstalled(context: Context): Boolean = isAppInstalled(context, PACKAGE_YOUTUBE_STUDIO)

    /**
     * Launch official YouTube app or fallback to browser
     */
    fun openYouTubeApp(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(PACKAGE_YOUTUBE)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            // Browser Fallback
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://m.youtube.com"))
            context.startActivity(webIntent)
        }
    }

    /**
     * Launch official YouTube Studio app or fallback to browser
     */
    fun openYouTubeStudioApp(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(PACKAGE_YOUTUBE_STUDIO)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            // Browser Fallback
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://studio.youtube.com"))
            context.startActivity(webIntent)
        }
    }

    /**
     * Deep link to Channel Analytics inside YouTube Studio or Web Studio
     */
    fun openChannelAnalytics(context: Context) {
        val studioUri = Uri.parse("https://studio.youtube.com/channel/analytics")
        val intent = Intent(Intent.ACTION_VIEW, studioUri).apply {
            if (isYouTubeStudioInstalled(context)) {
                setPackage(PACKAGE_YOUTUBE_STUDIO)
            }
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Intent.ACTION_VIEW, studioUri)
            context.startActivity(fallback)
        }
    }

    fun openYouTubeStudioAnalytics(context: Context) {
        openChannelAnalytics(context)
    }

    /**
     * Share rendered video file directly to YouTube app or Android system share sheet
     */
    fun shareVideoToYouTube(
        context: Context,
        videoFile: File,
        title: String,
        description: String
    ): Boolean {
        if (!videoFile.exists() || videoFile.length() == 0L) {
            Toast.makeText(context, "Video file not found on disk. Please render first.", Toast.LENGTH_SHORT).show()
            return false
        }

        val contentUri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                videoFile
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n\n$description")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // If official YouTube app is present, prioritize it
        if (isYouTubeInstalled(context)) {
            sendIntent.setPackage(PACKAGE_YOUTUBE)
        }

        return try {
            val chooser = Intent.createChooser(sendIntent, "Share Video to YouTube")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            // Fallback without target package
            sendIntent.setPackage(null)
            val chooser = Intent.createChooser(sendIntent, "Share Video")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        }
    }

    /**
     * Copy text to system clipboard with notification
     */
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label to clipboard!", Toast.LENGTH_SHORT).show()
    }
}
