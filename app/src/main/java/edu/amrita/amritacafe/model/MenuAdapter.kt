package edu.amrita.amritacafe.model


import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface.*
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import edu.amrita.amritacafe.R
import edu.amrita.amritacafe.menu.MenuItemUS
import kotlinx.android.synthetic.main.item_menu.view.*

class MenuAdapter(
    menu: List<MenuItemUS>,
    private val context: Context,
    private val onChanged: () -> Unit
) : BaseAdapter() {
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

    override fun getCount(): Int {
        return menuItems.size
    }

    override fun getItem(position: Int): Any? {
        return menuItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inline val @receiver:ColorInt Int.darken
        @ColorInt
        get() = ColorUtils.blendARGB(this, Color.BLACK, 0.2f)

    inline val @receiver:ColorInt Int.lighten
        @ColorInt
        get() = ColorUtils.blendARGB(this, Color.WHITE, 0.3f)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        convertOrInflate(convertView, parent).apply {

            val menuItem = menuItems[position]

            tag = menuItem

            when (menuItem) {
                is String -> {
                    val color = colorMap.getValue(menuItem)
                    (background as GradientDrawable).setStroke(2, color)
                    (background as GradientDrawable).setColor(color)
                    english_name.setTypeface(SANS_SERIF, NORMAL)
                    english_name.text = menuItem
                    malayalam_name.text = ""
                    cost.text = ""
                }
                is MenuItemUS -> {
                    val color = colorMap.getValue(menuItem.category)
                    val lighten = color.lighten
                    (background as GradientDrawable).setStroke(3, color)
                    (background as GradientDrawable).setColor(lighten)
                    english_name.text = menuItem.englishName
                    malayalam_name.text = menuItem.malaylamName;
                    english_name.setTypeface(SERIF, NORMAL)
                    cost.text = menuItem.price.toString()
                }
                is Unit -> {
                    (background as GradientDrawable).setStroke(0, Color.TRANSPARENT)
                    (background as GradientDrawable).setColor(Color.TRANSPARENT)
                    english_name.text = ""
                    malayalam_name.text = ""
                    cost.text = ""
                }

            }
        }

    private fun convertOrInflate(view: View?, parent: ViewGroup) =
        view ?: context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).let {
            (it as LayoutInflater).inflate(R.layout.item_menu, parent, false)
        }
}