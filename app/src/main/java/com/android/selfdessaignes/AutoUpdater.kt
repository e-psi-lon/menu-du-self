package com.android.selfdessaignes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
//import android.app.PendingIntent
//import android.content.Intent
//import android.content.pm.PackageInstaller
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
//import java.io.FileInputStream
import java.io.IOException
import java.util.zip.ZipInputStream

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
            val token = "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"
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
        @SuppressLint("UnspecifiedImmutableFlag")
        private fun installApkFromZip(zipFile: File) {
            //val packageInstaller = requireContext().packageManager?.packageInstaller
            //val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            //val sessionId = packageInstaller?.createSession(params)
            //val session = sessionId?.let { packageInstaller.openSession(it) }
            //val out = session?.openWrite("apk", 0, -1)
            /*val file =*/ unzipApp(zipFile)
            //val ins = FileInputStream(file)
            //out?.let { ins.copyTo(it) }
            //out?.let { session.fsync(it) }
            //ins.close()
            //out?.close()
            //if (activity != null) {
            //    val intent = Intent(activity, MainActivity::class.java)
            //    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            //    val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            //    val statusReceiver = pendingIntent.intentSender
            //    session?.commit(statusReceiver)
            //}
            //session?.close()
            //file.delete()
        }

        private fun unzipApp(zipFile: File)/*: File*/ {
            //val apkFile = File.createTempFile("latest-version", ".apk")
            val apkFile = File("/storage/emulated/0/Download", "latest-version-$getLastCommitHash().apk")
            println(apkFile.absolutePath)
            zipFile.inputStream().use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    while (true) {
                        val entry = zipInputStream.nextEntry ?: break
                        if (entry.name.endsWith(".apk")) {
                            apkFile.outputStream().use { outputStream ->
                                zipInputStream.copyTo(outputStream)
                            }
                            break
                        }
                    }
                }
            }
            zipFile.delete()
            //return apkFile

        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setMessage("Une nouvelle version est disponible !")
                    .setPositiveButton("Télécharger la dernière version") { _, _ ->
                        downloadAndInstall()
                        Toast.makeText(activity, "Le fichier d'installation de la dernière version est dans votre dossier de téléchargement", Toast.LENGTH_LONG).show()
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