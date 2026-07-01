package edu.amrita.amritacafe.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import edu.amrita.amritacafe.databinding.ActivitySettingsBinding
import edu.amrita.amritacafe.menu.*
import edu.amrita.amritacafe.settings.Configuration
import java.io.File

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var pref: SharedPreferences
    private lateinit var configuration: Configuration
    private lateinit var BREAKFAST_FILE: File
    private lateinit var adapter: SettingsMenuAdapter
    private val menuList = mutableListOf<MenuItemUS>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        pref = PreferenceManager.getDefaultSharedPreferences(this)
        configuration = Configuration(pref)

        val dir = File(filesDir, "Menus")
        BREAKFAST_FILE = File(dir, "Breakfast.txt")

        adapter = SettingsMenuAdapter(menuList) { position ->
            menuList.removeAt(position)
            adapter.notifyItemRemoved(position)
        }

        binding.menuRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.menuRecyclerView.adapter = adapter

        loadCurrentMenu()

        binding.addButton.setOnClickListener {
            val newItem = MenuItemUS("", "", 0f, "VEGTABLES")
            menuList.add(newItem)
            adapter.notifyItemInserted(menuList.size - 1)
            binding.menuRecyclerView.scrollToPosition(menuList.size - 1)
        }

        binding.resetButton.setOnClickListener {
            showResetConfirmationDialog()
        }
    }

    private fun loadCurrentMenu() {
        try {
            val list = getListOfMenu(BREAKFAST_FILE)
            menuList.clear()
            menuList.addAll(list.sortedBy { it.malaylamName })
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load menu: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun saveSettings(view: View) {
        try {
            val sortedList = menuList.filter { it.malaylamName.isNotBlank() }
                .sortedBy { it.malaylamName }
            
            createMenuFileFromMenuList(BREAKFAST_FILE, sortedList)
            
            menuList.clear()
            menuList.addAll(sortedList)
            adapter.notifyDataSetChanged()

            Toast.makeText(
                applicationContext,
                "Menu saved successfully",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                applicationContext,
                "Failed to save: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showResetConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Reset")
        builder.setMessage("Are you sure you want to reset the menu to the default?")
        builder.setPositiveButton("Reset") { _, _ ->
            resetMenu()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun resetMenu() {
        createMenuFileFromMenuList(BREAKFAST_FILE, DEFAULT_BREAKFAST_MENU)
        loadCurrentMenu()
        Toast.makeText(
            applicationContext,
            "Menu reset to default",
            Toast.LENGTH_SHORT
        ).show()
    }
}