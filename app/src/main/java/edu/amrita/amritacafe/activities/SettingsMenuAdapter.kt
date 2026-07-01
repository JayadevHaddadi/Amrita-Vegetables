package edu.amrita.amritacafe.activities

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView
import edu.amrita.amritacafe.databinding.ItemSettingsMenuBinding
import edu.amrita.amritacafe.menu.MenuItemUS

class SettingsMenuAdapter(
    private var menuItems: MutableList<MenuItemUS>,
    private val onChanged: () -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<SettingsMenuAdapter.ViewHolder>() {

    var focusPosition: Int = -1

    inner class ViewHolder(private val binding: ItemSettingsMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentWatcher: TextWatcher? = null
        private var priceWatcher: TextWatcher? = null

        fun bind(item: MenuItemUS) {
            binding.nameET.removeTextChangedListener(currentWatcher)
            binding.priceET.removeTextChangedListener(priceWatcher)

            binding.nameET.setText(item.malaylamName)
            binding.priceET.setText(item.price.toString())

            currentWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val newText = s.toString()
                        if (menuItems[pos].malaylamName != newText) {
                            val updatedItem = menuItems[pos].copy(
                                malaylamName = newText,
                                englishName = newText.uppercase()
                            )
                            menuItems[pos] = updatedItem
                            onChanged()
                        }
                    }
                }
            }

            priceWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val price = s.toString().toFloatOrNull() ?: 0f
                        if (menuItems[pos].price != price) {
                            val updatedItem = menuItems[pos].copy(price = price)
                            menuItems[pos] = updatedItem
                            onChanged()
                        }
                    }
                }
            }

            binding.nameET.addTextChangedListener(currentWatcher)
            binding.priceET.addTextChangedListener(priceWatcher)

            binding.deleteButton.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDelete(pos)
                }
            }
            
            if (adapterPosition == focusPosition) {
                binding.nameET.post {
                    binding.nameET.requestFocus()
                    val imm = binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.nameET, InputMethodManager.SHOW_IMPLICIT)
                }
                focusPosition = -1
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingsMenuBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(menuItems[position])
    }

    override fun getItemCount() = menuItems.size
}
