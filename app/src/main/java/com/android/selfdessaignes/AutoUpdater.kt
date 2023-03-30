package com.android.selfdessaignes

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipFile

class AutoUpdater {
    class GithubDialogFragment : DialogFragment() {
        private fun downloadAndInstall() = CoroutineScope(Dispatchers.IO).launch {
            var client = OkHttpClient()
            var request = Request.Builder()
                .url("https://api.github.com/repos/e-psi-lon/menu-du-self/actions/artifacts?per_page=10")
                .build()
            val response1 = client.newCall(request).execute()
            val json = response1.body?.string()
            val jsonArray = json?.let { JSONObject(it) }
            val jsonObject = jsonArray?.getJSONArray("artifacts")
            val downloadUrl = jsonObject?.getJSONObject(0)?.get("archive_download_url")
            val token = BuildConfig.TOKEN_ACCESS_ACTION
            println(token)
            val request2 = Request.Builder()
                .url(downloadUrl.toString())
                .header("Authorization", "Bearer $token")
                .header("accept", "application/vnd.github.v3+json")
                .build()
            val response2 = client.newCall(request2).execute()
            val url = response2.request.url.toString()
            client = OkHttpClient()
            request = Request.Builder()
                .url(url)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val tempFile = File.createTempFile("latest-version", ".zip")
                response.body?.bytes()?.let { tempFile.writeBytes(it) }
                installApkFromZip(tempFile)
            }
        }
        private fun installApkFromZip(zipFile: File) {
            println("installApkFromZip")
            val apkEntryName = findApkEntryName(zipFile)

            ZipFile(zipFile).use { zip ->
                val apkEntry = zip.getEntry(apkEntryName)

                val apkFile = apkEntryName?.let { File(context?.cacheDir, it) }
                FileOutputStream(apkFile).use { outputStream ->
                    zip.getInputStream(apkEntry).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }


                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context?.startActivity(intent)
            }
            zipFile.delete()
        }

        private fun findApkEntryName(zipFile: File): String? {
            println("findApkEntryName")
            ZipFile(zipFile).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name.endsWith(".apk")) {
                        return entry.name
                    }
                }
            }
            return null
        }
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setMessage("Une nouvelle version est disponible !")
                    .setPositiveButton("Télécharger la dernière version") { _, _ ->
                        println("Téléchargement de la dernière version...")
                        downloadAndInstall()
                    }
                    .setNegativeButton("Annuler") { dialog, _ ->
                        dialog.cancel()
                    }
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
    }
    companion object {
        fun getLastCommitHash(): String? {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.github.com/repos/e-psi-lon/menu-du-self/commits")
                .build()
            val response = client.newCall(request).execute()
            val jsonData = response.body?.string()
            val jsonArray = JSONArray(jsonData)
            val jsonObject = jsonArray.getJSONObject(0)
            return if (jsonObject.has("sha")) {
                jsonObject.getString("sha").take(8)
            } else {
                null
            }
        }
    }
}