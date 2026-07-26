package com.fieldministry.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseAsset(
    val name: String,
    @SerializedName("browser_download_url") val downloadUrl: String,
)

data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String,
    val assets: List<ReleaseAsset>,
)

data class AppUpdate(val version: String, val apkUrl: String)

private const val REPO = "Eph0714/Field-Ministry-Activity-Tracker"

object UpdateChecker {

    /** Compares two dot-separated version strings (e.g. "1.2" vs "1.10") numerically, not lexically. */
    fun isNewer(remote: String, current: String): Boolean {
        val r = remote.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val c = current.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(r.size, c.size)) {
            val rv = r.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (rv != cv) return rv > cv
        }
        return false
    }

    /** Checks GitHub Releases for a newer version than [currentVersion]. Returns null if up to date or on any error. */
    suspend fun checkForUpdate(currentVersion: String): AppUpdate? = withContext(Dispatchers.IO) {
        try {
            val connection = URL("https://api.github.com/repos/$REPO/releases/latest").openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "FieldMinistryTracker")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            try {
                if (connection.responseCode != 200) return@withContext null
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val release = Gson().fromJson(body, GitHubRelease::class.java)
                val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") } ?: return@withContext null
                if (isNewer(release.tagName, currentVersion)) {
                    AppUpdate(version = release.tagName, apkUrl = apkAsset.downloadUrl)
                } else {
                    null
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Downloads the APK and launches the system package installer. */
    suspend fun downloadAndInstall(context: Context, update: AppUpdate): Unit = withContext(Dispatchers.IO) {
        val connection = URL(update.apkUrl).openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val file = File(updatesDir, "fieldministry-${update.version}.apk")
        try {
            connection.connect()
            connection.inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
        } finally {
            connection.disconnect()
        }

        withContext(Dispatchers.Main) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
