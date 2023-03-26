package com.android.selfdessaignes

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
    private val appVersionName: String = BuildConfig.VERSION_NAME
    private val appVersionCode: Int = BuildConfig.VERSION_CODE
    private val dayInWeek: List<String> = listOf("Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche")

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
        currentDay = getDayOfWeek()
        getMenuFromWebsite(currentDay)
        backButton.setOnClickListener {
            when (currentDay) {
                "Lundi" -> {}
                else -> {
                    getMenuFromWebsite(dayInWeek[dayInWeek.indexOf(currentDay) - 1])
                    currentDay = dayInWeek[dayInWeek.indexOf(currentDay) - 1]
                }
            }
        }
        nextButton.setOnClickListener {
            when (currentDay) {
                "Dimanche" -> {}
                else -> {
                    getMenuFromWebsite(dayInWeek[dayInWeek.indexOf(currentDay) + 1])
                    currentDay = dayInWeek[dayInWeek.indexOf(currentDay) + 1]
                }
            }

        }
        openMenuButton

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
    @SuppressLint("SetTextI18n")
    private fun getMenuFromWebsite(day: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            when (day){
                "Mardi" -> {
                    val menuList: ArrayList<String> = ArrayList()
                    val doc = Jsoup.connect("https://standarddelunivers.wordpress.com/2022/06/28/menu-de-la-semaine/").get()
                    val tables = doc.select("table")
                    val dayToShow = tables[0].select("th")[0].text()
                    val rows = tables[0].select("tr")
                    for (row in rows) {
                        val cells = row.select("td")
                        if (cells.size == 2) {
                            val plat = cells[0].text()
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
                }
                "Mercredi" -> {
                    val menuList: ArrayList<String> = ArrayList()
                    val doc = Jsoup.connect("https://standarddelunivers.wordpress.com/2022/06/28/menu-de-la-semaine/").get()

                    val tables = doc.select("table")
                    val dayToShow = tables[0].select("th")[1].text()
                    val rows = tables[0].select("tr")
                    for (row in rows) {
                        val cells = row.select("td")
                        if (cells.size == 2) {
                            val plat = cells[1].text()
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
                }
                "Jeudi" -> {
                    val menuList: ArrayList<String> = ArrayList()
                    val doc = Jsoup.connect("https://standarddelunivers.wordpress.com/2022/06/28/menu-de-la-semaine/").get()

                    val tables = doc.select("table")
                    val dayToShow = tables[1].select("th")[0].text()
                    val rows = tables[1].select("tr")
                    for (row in rows) {
                        val cells = row.select("td")
                        if (cells.size == 2) {
                            val plat = cells[0].text()
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
                }
                "Vendredi" -> {
                    val menuList: ArrayList<String> = ArrayList()
                    val doc = Jsoup.connect("https://standarddelunivers.wordpress.com/2022/06/28/menu-de-la-semaine/").get()

                    val tables = doc.select("table")
                    val dayToShow = tables[1].select("th")[1].text()
                    val rows = tables[1].select("tr")
                    for (row in rows) {
                        val cells = row.select("td")
                        if (cells.size == 2) {
                            val plat = cells[1].text()
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
                }
                else -> {
                    withContext(Dispatchers.Main) {
                        runOnUiThread {
                            dayView.text = StringBuilder(day)
                            val temp = getString(R.string.noMenuAvailableOn)
                            menuListView.visibility = View.GONE
                            menuCurrentState.visibility = View.VISIBLE
                            menuCurrentState.text = "$temp le $day"
                        }
                    }
                }
            }



        } catch (e: IOException) {
            runOnUiThread {
                menuCurrentState.text = getString(R.string.noMenuAvailable)
            }

        }
    }
}