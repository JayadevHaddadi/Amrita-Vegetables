package edu.amrita.amritacafe.menu

import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

data class RegularOrderItem(
    val menuItem: MenuItemUS,
    var quantity: Float = 1f,
    var comment: String = "",
    var priceMultiplier: Float = 1f,
    val id: UUID = UUID.randomUUID()
) {

    val refund: Float by lazy {
        if (priceMultiplier == -1f) (originalPrice * priceMultiplier) else 0f
    }

    val finalPrice: Float by lazy {
        Math.round(originalPrice * priceMultiplier).toFloat()
    }

    val originalPrice: Float by lazy {
        quantity * menuItem.price
    }

    fun editComment(newComment: String) = copy(comment = newComment)
    fun increment(weight: Float = 1f) {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.HALF_UP

        quantity = df.format(quantity + weight).toFloat()
    }
}

