package edu.amrita.amritacafe.menu

import kotlinx.serialization.Serializable

@Serializable
data class MenuItemUS(
    val malaylamName: String,
    val englishName: String,
    val price: Float,
    val category: String
)
