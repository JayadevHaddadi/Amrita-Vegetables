package edu.amrita.amritacafe.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.amrita.amritacafe.R
import edu.amrita.amritacafe.databinding.ActivitySettingsBinding
import edu.amrita.amritacafe.menu.*
import java.io.File

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var BREAKFAST_FILE: File
    private lateinit var adapter: SettingsMenuAdapter
    private val menuList = mutableListOf<MenuItemUS>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val dir = File(filesDir, "Menus")
        BREAKFAST_FILE = File(dir, "Breakfast.txt")

        adapter = SettingsMenuAdapter(menuList, 
            onChanged = {
                saveMenuToFile(notifyAdapter = false)
            },
            onDelete = { position ->
                menuList.removeAt(position)
                adapter.notifyItemRemoved(position)
                saveMenuToFile(notifyAdapter = false)
            }
        )

        binding.menuRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.menuRecyclerView.adapter = adapter

        loadCurrentMenu()

        binding.addButton.setOnClickListener {
            val newItem = MenuItemUS("", "", 0f, "VEGTABLES")
            menuList.add(newItem)
            
            // Sort list as requested: "once item has been added it is arranged in alphabetic order"
            val sorted = menuList.sortedBy { it.malaylamName }
            menuList.clear()
            menuList.addAll(sorted)
            
            val newPos = menuList.indexOf(newItem)
            if (newPos != -1) {
                adapter.focusPosition = newPos
                adapter.notifyDataSetChanged()
                binding.menuRecyclerView.scrollToPosition(newPos)
            } else {
                adapter.notifyDataSetChanged()
            }
            
            saveMenuToFile(notifyAdapter = false)
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
            Toast.makeText(this, "${getString(R.string.settings_load_failed)}: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveMenuToFile(notifyAdapter: Boolean) {
        try {
            // Filter out fully blank items but keep those with at least a name or price being edited
            val listToSave = menuList.filter { it.malaylamName.isNotBlank() }
            createMenuFileFromMenuList(BREAKFAST_FILE, listToSave)
            
            if (notifyAdapter) {
                adapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            // Log or show error if critical, but for auto-save we avoid frequent toasts
        }
    }

    private fun showResetConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.settings_confirm_reset_title))
        builder.setMessage(getString(R.string.settings_confirm_reset_message))
        builder.setPositiveButton(getString(R.string.settings_reset)) { _, _ ->
            resetMenu()
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun resetMenu() {
        createMenuFileFromMenuList(BREAKFAST_FILE, DEFAULT_BREAKFAST_MENU)
        loadCurrentMenu()
        Toast.makeText(
            applicationContext,
            getString(R.string.settings_reset_success),
            Toast.LENGTH_SHORT
        ).show()
    }
}
