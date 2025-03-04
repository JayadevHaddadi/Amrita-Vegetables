package edu.amrita.amritacafe.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import edu.amrita.amritacafe.databinding.ActivitySettingsBinding
import edu.amrita.amritacafe.menu.saveIfValidText
import edu.amrita.amritacafe.settings.Configuration
import edu.amrita.amritacafe.settings.Configuration.Companion.COLUMN_NUMBER_RANGE
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import edu.amrita.amritacafe.menu.DEFAULT_BREAKFAST_MENU
import edu.amrita.amritacafe.menu.createMenuFileFromMenuList
import androidx.appcompat.app.AlertDialog

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var pref: SharedPreferences
    private lateinit var configuration: Configuration
    private lateinit var BREAKFAST_FILE: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        pref = PreferenceManager.getDefaultSharedPreferences(this)
        configuration = Configuration(pref)

        val column = pref.getString(COLUMN_NUMBER_RANGE, "10")
        binding.columnNumbersET.setText(column)

        val dir = File(filesDir, "Menus")
        BREAKFAST_FILE = File(dir, "Breakfast.txt")

        fun loadCurrentMenu() {
            val file = BREAKFAST_FILE
            val br = BufferedReader(FileReader(file))
            binding.menuET.setText(br.readText())
        }
        loadCurrentMenu()

        binding.resetButton.setOnClickListener {
            showResetConfirmationDialog()
        }
    }

    fun saveSettings(view: View) {
        val file = BREAKFAST_FILE
        val response = saveIfValidText(binding.menuET.text.toString(), applicationContext, file)

        Toast.makeText(
            applicationContext,
            response,
            Toast.LENGTH_LONG
        ).show()
    }
    private fun showResetConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Reset")
        builder.setMessage("Are you sure you want to reset the menu to the default?")
        builder.setPositiveButton("Reset") { dialog, which ->
            resetMenu()
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            // Do nothing, just close the dialog
        }
        builder.show()
    }

    private fun resetMenu() {
        // Overwrite the current menu file with the default data
        createMenuFileFromMenuList(BREAKFAST_FILE, DEFAULT_BREAKFAST_MENU)

        Toast.makeText(
            applicationContext,
            "Menu reset to default",
            Toast.LENGTH_SHORT
        ).show()
    }
}