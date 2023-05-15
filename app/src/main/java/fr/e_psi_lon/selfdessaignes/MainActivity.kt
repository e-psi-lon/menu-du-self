package fr.e_psi_lon.selfdessaignes

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*

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
            //switchToDinner.visibility = View.VISIBLE
            //appVersion.visibility = View.VISIBLE
            //appVersion.text = getString(R.string.version)+" "+appVersionName
        }

    }
    private fun checkVersion() = CoroutineScope(Dispatchers.IO).launch {
        if (AutoUpdater.getLastCommitHash() != BuildConfig.GIT_COMMIT_HASH) {
            AutoUpdater.GithubDialogFragment().show(supportFragmentManager, "GithubDialogFragment")
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
                    if ("<" in plat) {
                        println(plat)
                        val platSplit = plat.split("<")
                        menuList.add(platSplit[0])
                    }
                    else if (plat=="" || plat==" ") {
                        continue
                    }
                    else {
                        menuList.add(plat)
                    }
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
                getMenu(1,1)
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