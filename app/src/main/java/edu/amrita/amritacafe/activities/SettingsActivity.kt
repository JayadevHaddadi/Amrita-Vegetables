package edu.amrita.amritacafe.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import edu.amrita.amritacafe.R
import edu.amrita.amritacafe.databinding.ActivitySettingsBinding
import edu.amrita.amritacafe.menu.BREAKFAST_FILE
import edu.amrita.amritacafe.menu.saveIfValidText
import edu.amrita.amritacafe.settings.Configuration
import edu.amrita.amritacafe.settings.Configuration.Companion.COLUMN_NUMBER_RANGE
import java.io.BufferedReader
import java.io.FileReader

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var pref: SharedPreferences
    private lateinit var configuration: Configuration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        pref = PreferenceManager.getDefaultSharedPreferences(this)
        configuration = Configuration(pref)

        val column = pref.getString(COLUMN_NUMBER_RANGE, "10")
        binding.columnNumbersET.setText(column)

        fun loadCurrentMenu() {
            val file = BREAKFAST_FILE
            val br = BufferedReader(FileReader(file))
            binding.menuET.setText(br.readText())
        }
        loadCurrentMenu()
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
}
