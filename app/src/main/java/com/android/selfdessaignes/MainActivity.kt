package com.android.selfdessaignes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.zip.ZipFile

class GithubDialogFragment : DialogFragment() {
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
    private fun downloadAndInstall() = CoroutineScope(Dispatchers.IO).launch {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.github.com/repos/e-psi-lon/menu-du-self/actions/artifacts?per_page=10")
            .build()
        val response = client.newCall(request).execute()
        val json = response.body?.string()
        val jsonArray = json?.let { JSONObject(it) }
        val jsonObject = jsonArray?.getJSONArray("artifacts")
        val downloadUrl = jsonObject?.getJSONObject(0)?.get("archive_download_url")
        val request2 = Request.Builder()
            .url(downloadUrl.toString())
            .build()
        val response2 = client.newCall(request2).execute()
        val zipFile = File(context?.cacheDir, "menu-du-self.zip")
        withContext(Dispatchers.IO) {
            FileOutputStream(zipFile).use { outputStream ->
                response2.body?.byteStream()?.copyTo(outputStream)
            }
        }
        installApkFromZip(zipFile)
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

class MainActivity : AppCompatActivity() {

    private lateinit var menuTextView: TextView
    private lateinit var menuCurrentState: TextView
    private lateinit var menuListView: ListView
    private lateinit var currentDay: String
    private lateinit var dayView: TextView
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var openMenuButton: Button
    private lateinit var switchToDinner: SwitchCompat
    private lateinit var appVersion: TextView
    private lateinit var mainLayout: LinearLayout
    private val appVersionName: String = BuildConfig.VERSION_NAME
    private val dayInWeek: List<String> = listOf("Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        menuTextView = findViewById(R.id.titleTextView)
        menuCurrentState = findViewById(R.id.currentState)
        menuListView = findViewById(R.id.menuListView)
        dayView = findViewById(R.id.actualDay)
        backButton = findViewById(R.id.back)
        nextButton = findViewById(R.id.next)
        openMenuButton = findViewById(R.id.mainMenu)
        switchToDinner = findViewById(R.id.switchToDinner)
        appVersion = findViewById(R.id.currentVersion)
        mainLayout = findViewById(R.id.mainPage)
        checkVersion()
        currentDay = getDayOfWeek()
        executeForXDay(currentDay)
        backButton.setOnClickListener {
            when (currentDay) {
                "Lundi" -> {}
                else -> {
                    executeForXDay(dayInWeek[dayInWeek.indexOf(currentDay) - 1])
                    currentDay = dayInWeek[dayInWeek.indexOf(currentDay) - 1]
                }
            }
        }
        nextButton.setOnClickListener {
            when (currentDay) {
                "Dimanche" -> {}
                else -> {
                    executeForXDay(dayInWeek[dayInWeek.indexOf(currentDay) + 1])
                    currentDay = dayInWeek[dayInWeek.indexOf(currentDay) + 1]
                }
            }

        }
        openMenuButton.setOnClickListener {
            for (Views in listOf(menuTextView,menuCurrentState,menuListView,dayView,backButton,nextButton,openMenuButton)){
                Views.visibility = View.GONE
            }
            switchToDinner.visibility = View.VISIBLE
            appVersion.visibility = View.VISIBLE
            appVersion.text = getString(R.string.version)+" "+appVersionName
        }

    }
    private fun checkVersion() = CoroutineScope(Dispatchers.IO).launch {
        if (getLastCommitHash() != BuildConfig.GIT_COMMIT_HASH) {
            GithubDialogFragment().show(supportFragmentManager, "GithubDialogFragment")
        }
    }
    private fun getDayOfWeek(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lundi"
            Calendar.TUESDAY -> "Mardi"
            Calendar.WEDNESDAY -> "Mercredi"
            Calendar.THURSDAY -> "Jeudi"
            Calendar.FRIDAY -> "Vendredi"
            Calendar.SATURDAY -> "Samedi"
            Calendar.SUNDAY -> "Dimanche"
            else -> ""
        }
    }

    private fun getMenu(table:Int,cell:Int) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val menuList: ArrayList<String> = ArrayList()
            val doc = Jsoup.connect("https://standarddelunivers.wordpress.com/2022/06/28/menu-de-la-semaine/").get()
            val tables = doc.select("table")
            val dayToShow = tables[table].select("th")[cell].text()
            val rows = tables[table].select("tr")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size == 2) {
                    val plat = cells[cell].text()
                    menuList.add(plat)
                }
            }
            withContext(Dispatchers.Main) {
                runOnUiThread {
                    dayView.text = StringBuilder(dayToShow)
                    menuListView.visibility = View.VISIBLE
                    menuCurrentState.visibility = View.GONE
                    menuListView.adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, menuList)
                }
            }
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                runOnUiThread {
                    menuCurrentState.text = getString(R.string.noMenuAvailable)
                    menuListView.visibility = View.GONE
                    menuCurrentState.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun getLastCommitHash(): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
        .url("https://api.github.com/repos/e-psi-lon/menu-du-self/commits")
        .build()
        val response = client.newCall(request).execute()
        val jsonData = response.body?.string()
        val jsonArray = JSONArray(jsonData)
        val jsonObject = jsonArray.getJSONObject(0)
        println(jsonObject.getString("sha").take(8))
        return if (jsonObject.has("sha")) {
            jsonObject.getString("sha").take(8)
        } else {
            null
        }
    }
    @SuppressLint("SetTextI18n")
    private fun executeForXDay(day: String) = CoroutineScope(Dispatchers.IO).launch {
        when (day){
            "Mardi" -> {
                getMenu(0,0)
            }
            "Mercredi" -> {
                getMenu(0,1)
            }
            "Jeudi" -> {
                getMenu(1,0)
            }
            "Vendredi" -> {
                getMenu(1,0)
            }
            else -> {
                withContext(Dispatchers.Main) {
                    runOnUiThread {
                        dayView.text = StringBuilder(day)
                        menuCurrentState.text = getString(R.string.noMenuAvailableOn)+" le "+day
                        menuListView.visibility = View.GONE
                        menuCurrentState.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}