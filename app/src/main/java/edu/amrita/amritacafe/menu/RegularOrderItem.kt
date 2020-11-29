package edu.amrita.amritacafe.menu

import java.util.*

data class RegularOrderItem(
    val menuItem: MenuItemUS,
    var quantity: Float = 1f,
    var comment: String = "",
    var priceMultiplier: Float = 1f,
    val id: UUID = UUID.randomUUID()
) {

    init {

    }

    val discount: Float by lazy {
        if (priceMultiplier < 1f && priceMultiplier > -1f) (originalPrice * priceMultiplier) else 0f
    }

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
    fun increment() = copy(quantity = quantity + 1)
}

