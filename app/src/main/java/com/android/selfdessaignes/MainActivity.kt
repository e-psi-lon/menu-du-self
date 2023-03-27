package com.android.selfdessaignes

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.coroutines.*
import org.jsoup.Jsoup

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
    private val appVersionCode: Int = BuildConfig.VERSION_CODE
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

        if (appVersionCode > 10) {
            var updateAvailable = true
            while (updateAvailable) {
                Toast.makeText(this, "Une mise à jour est disponible", Toast.LENGTH_LONG).show()
                updateAvailable = false
            }
        }
        mainLayout = findViewById(R.id.mainPage)
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
                // Set the adapter for the ListView
                runOnUiThread {
                    dayView.text = StringBuilder(dayToShow)
                    menuCurrentState.text = getString(R.string.noMenuAvailable)
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
                getMenu(1,0)
            }
            else -> {
                withContext(Dispatchers.Main) {
                    runOnUiThread {
                        dayView.text = StringBuilder(day)
                        menuCurrentState.text = getString(R.string.noMenuAvailable)+" "+day
                        menuListView.visibility = View.GONE
                        menuCurrentState.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}