package com.mobdeve.chuachingdytocstamaria.mco.pomopet.models

import com.mobdeve.chuachingdytocstamaria.mco.pomopet.R


enum class StreakState(val pawStreak: Int) {
    PRODUCTIVE(R.drawable.paw_streak),
    MISSED(R.drawable.paw_streak_gray),
    UPCOMING(R.drawable.paw_streak_light_gray)
}