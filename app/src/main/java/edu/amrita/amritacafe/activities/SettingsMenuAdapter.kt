package edu.amrita.amritacafe.activities

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.amrita.amritacafe.databinding.ItemSettingsMenuBinding
import edu.amrita.amritacafe.menu.MenuItemUS

class SettingsMenuAdapter(
    private var menuItems: MutableList<MenuItemUS>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<SettingsMenuAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSettingsMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentWatcher: TextWatcher? = null
        private var priceWatcher: TextWatcher? = null

        fun bind(item: MenuItemUS, position: Int) {
            binding.nameET.removeTextChangedListener(currentWatcher)
            binding.priceET.removeTextChangedListener(priceWatcher)

            binding.nameET.setText(item.malaylamName)
            binding.priceET.setText(item.price.toString())

            currentWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val updatedItem = menuItems[adapterPosition].copy(
                        malaylamName = s.toString(),
                        englishName = s.toString().uppercase()
                    )
                    menuItems[adapterPosition] = updatedItem
                }
            }

            priceWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val price = s.toString().toFloatOrNull() ?: 0f
                    val updatedItem = menuItems[adapterPosition].copy(price = price)
                    menuItems[adapterPosition] = updatedItem
                }
            }

            binding.nameET.addTextChangedListener(currentWatcher)
            binding.priceET.addTextChangedListener(priceWatcher)

            binding.deleteButton.setOnClickListener {
                onDelete(adapterPosition)
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
        holder.bind(menuItems[position], position)
    }

    override fun getItemCount() = menuItems.size

    fun updateList(newList: List<MenuItemUS>) {
        menuItems.clear()
        menuItems.addAll(newList)
        notifyDataSetChanged()
    }
}