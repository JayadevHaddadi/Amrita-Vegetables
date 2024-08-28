package edu.amrita.amritacafe.model

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import edu.amrita.amritacafe.R
import edu.amrita.amritacafe.databinding.ItemMenuBinding
import edu.amrita.amritacafe.menu.MenuItemUS

class MenuAdapter(
    menu: List<MenuItemUS>,
    private val context: Context,
    private val onItemClick: (MenuItemUS) -> Unit // Update to use MenuItemUS
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private var menuItems: List<Any>
    private var colorMap: Map<String, Int>
    private var menuItemDisplayNameHandler: (MenuItemUS) -> String =
        { menuItemUS -> menuItemUS.englishName }

    init {
        menu.groupBy {
            it.category
        }.let { menuByCategory ->
            val colors: TypedArray = context.resources.obtainTypedArray(R.array.colors)
            colorMap = menuByCategory.keys.mapIndexed { idx, cat ->
                cat to colors.getColor(idx, 0)
            }.toMap()

            colors.recycle()
            val columns = context.resources.getInteger(R.integer.columns)
            menuItems = menuByCategory.map { (category, items) ->
                listOf(category) + items.sortedBy(menuItemDisplayNameHandler) +
                        Array((columns - (items.size + 1) % columns) % columns) { Unit }
            }.flatten()
        }
    }

    // ViewHolder class
    inner class MenuViewHolder(val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(menuItem: Any) {
            when (menuItem) {
                is String -> {
                    // Ensure proper view reset for category headers
                    (binding.root.background as GradientDrawable).apply {
                        setStroke(2, colorMap.getValue(menuItem))
                        setColor(colorMap.getValue(menuItem))
                    }
                    binding.englishName.setTypeface(null, android.graphics.Typeface.NORMAL)
                    binding.englishName.text = menuItem
                    binding.malayalamName.text = ""
                    binding.cost.text = ""
                }
                is MenuItemUS -> {
                    // Ensure proper view reset for menu items
                    val color = colorMap.getValue(menuItem.category)
                    val lighten = color.lighten
                    (binding.root.background as GradientDrawable).apply {
                        setStroke(3, color)
                        setColor(lighten)
                    }
                    binding.englishName.text = menuItem.englishName
                    binding.malayalamName.text = menuItem.malaylamName
                    binding.englishName.setTypeface(android.graphics.Typeface.SERIF, android.graphics.Typeface.NORMAL)
                    binding.cost.text = menuItem.price.toString()

                    // Set click listener
                    binding.root.setOnClickListener {
                        println("PRESSED3")
                        onItemClick(menuItem)
                    }
                }
                is Unit -> {
                    // Reset for empty spaces
                    (binding.root.background as GradientDrawable).apply {
                        setStroke(0, Color.TRANSPARENT)
                        setColor(Color.TRANSPARENT)
                    }
                    binding.englishName.text = ""
                    binding.malayalamName.text = ""
                    binding.cost.text = ""
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBinding.inflate(LayoutInflater.from(context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.bind(menuItem)
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    // Extension properties for darken and lighten colors
    inline val @receiver:ColorInt Int.darken
        @ColorInt get() = ColorUtils.blendARGB(this, Color.BLACK, 0.2f)

    inline val @receiver:ColorInt Int.lighten
        @ColorInt get() = ColorUtils.blendARGB(this, Color.WHITE, 0.3f)
}
