package fr.e_psi_lon.selfdessaignes

import android.app.Activity
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
import java.io.File
//import java.io.FileInputStream
import java.io.IOException
import java.util.zip.ZipInputStream

class AutoUpdater {
    class GithubDialogFragment : DialogFragment() {
        private fun checkUpdate(activity: Activity) = CoroutineScope(Dispatchers.IO).launch {
            
        }

        private fun download(activity: Activity) = CoroutineScope(Dispatchers.IO).launch {
            try {
                unzipApp(File(activity.filesDir, "latest-version.zip"))
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Le fichier de la dernière version se situe dans le dossier Download du téléphone", Toast.LENGTH_LONG).show()
                }
            }
            catch (Exception: IOException) {
                withContext(Dispatchers.Main) {
                    val strError = "Error: ${Exception.message}"
                    println(strError)
                    Toast.makeText(
                        activity,
                        "Une erreur est survenue lors du téléchargement de la dernière version",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }

        private fun unzipApp(zipFile: File) {
            val apkFile = File("/storage/emulated/0/Download", "latest-version-${getLastCommitHash()}.apk")
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

        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setMessage("Une nouvelle version est disponible !")
                    .setPositiveButton("Télécharger la dernière version") { _, _ ->
                        download(activity as Activity)

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