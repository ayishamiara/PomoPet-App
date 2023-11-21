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

        // Updates the selectedTheme to the specified theme and checks if the theme has changed from the previous one. If changed, it sets the previousTheme and recreates the activity to apply the new theme.
        fun changeTheme(activity: Activity, theme: Int){
            selectedTheme = theme
            if(selectedTheme != previousTheme){
                previousTheme = theme
                activity.recreate()
            }


        }

        // Sets the theme of the provided Activity to the specified theme without checking for changes or triggering an activity recreation.
        fun setThemeOnCreate(activity: Activity, theme: Int){
            activity.setTheme(theme)
        }

    }



}