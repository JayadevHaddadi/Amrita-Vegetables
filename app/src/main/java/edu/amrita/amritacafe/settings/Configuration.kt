package edu.amrita.amritacafe.settings


import android.content.SharedPreferences

data class Configuration(private val preferences: SharedPreferences) {

    companion object {
        const val COLUMN_NUMBER_RANGE = "column_number_range"
    }
}