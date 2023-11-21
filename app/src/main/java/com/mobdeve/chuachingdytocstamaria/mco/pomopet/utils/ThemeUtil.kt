package com.mobdeve.chuachingdytocstamaria.mco.pomopet.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.R
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.SettingsActivity

class ThemeUtil {
    companion object{
        var selectedTheme: Int = R.style.Bunny_Theme_Pomopet
        var previousTheme: Int = R.style.Bunny_Theme_Pomopet

        fun changeTheme(activity: Activity, theme: Int){
            selectedTheme = theme
            if(selectedTheme != previousTheme){
                previousTheme = theme
                activity.recreate()
            }


        }

        fun setThemeOnCreate(activity: Activity, theme: Int){
            activity.setTheme(theme)
        }

    }



}