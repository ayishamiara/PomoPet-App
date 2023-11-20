package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.utils.ThemeUtil

abstract class BaseActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setThemeOnCreate(this, loadTheme())
    }

    // Load the theme from SharedPreferences
     protected fun loadTheme(): Int {
        val sp: SharedPreferences = getSharedPreferences(SettingsActivity.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
        return sp.getInt(SettingsActivity.THEME_KEY, SettingsActivity.THEME_BUNNY)
    }
}