package edu.amrita.amritacafe.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import edu.amrita.amritacafe.R
import edu.amrita.amritacafe.menu.BREAKFAST_FILE
import edu.amrita.amritacafe.menu.LUNCH_DINNER_FILE
import edu.amrita.amritacafe.menu.saveIfValidText
import edu.amrita.amritacafe.settings.Configuration
import edu.amrita.amritacafe.settings.Configuration.Companion.COLUMN_NUMBER_RANGE
import edu.amrita.amritacafe.settings.Configuration.Companion.ORDER_NUMBER_RANGE
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.BufferedReader
import java.io.FileReader

class SettingsActivity : AppCompatActivity() {
    private lateinit var pref: SharedPreferences
    private lateinit var configuration: Configuration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.hide()

        pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.let { preferences ->
            configuration = Configuration(preferences)
        }
//        val kitchenIP = pref.getString(IP_KITCEN_PRINTER, "192.168.0.11")
//        val receiptIP = pref.getString(IP_RECEIPT_PRINTER,
//        )
        receipt_ip_ET.setText(configuration.receiptPrinterConnStr)
        kitchen_ip_ET.setText(configuration.kitchenPrinterConnStr)

        val orderNr = pref.getString(ORDER_NUMBER_RANGE, "100")
        range_ET.setText(orderNr)
        val column = pref.getString(COLUMN_NUMBER_RANGE, "10")
        column_numbers_ET.setText(column)

        fun loadCurrentMenu() {
            println("Loading is breakfast: ${configuration.isBreakfastTime}")
            val file = BREAKFAST_FILE
            val br = BufferedReader(FileReader(file))
            menu_ET.setText(br.readText())
        }
        menu_toggle_button.isChecked = configuration.isBreakfastTime
        loadCurrentMenu()

        menu_toggle_button.setOnCheckedChangeListener { compoundButton, isBreakfastTime ->
            configuration.isBreakfastTime = isBreakfastTime
            loadCurrentMenu()
        }
    }

    override fun onStop() {
        super.onStop()
        configuration.kitchenPrinterConnStr = kitchen_ip_ET.text.toString()
        configuration.receiptPrinterConnStr = receipt_ip_ET.text.toString()
//        val edit = pref.edit()
//        edit.putString(IP_RECEIPT_PRINTER, receipt_ip_ET.text.toString())
//        edit.putString(IP_KITCEN_PRINTER, kitchen_ip_ET.text.toString())
    }

    fun saveSettings(view: View) {
        val file = BREAKFAST_FILE
        val response = saveIfValidText(menu_ET.text.toString(), applicationContext, file)

        Toast.makeText(
            applicationContext,
            response,
            Toast.LENGTH_LONG
        ).show()

//        if (response == "SUCCESS")
//            overrideFile(menu_ET.text.toString(), file, applicationContext)
    }
}