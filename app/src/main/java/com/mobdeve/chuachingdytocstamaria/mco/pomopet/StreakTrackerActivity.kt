package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.marginBottom
import androidx.core.view.setMargins
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ActivityStreakTrackerBinding
import kotlin.random.Random

class StreakTrackerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStreakTrackerBinding
    private val PAWS_PER_ROW = 16

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStreakTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = generateRandomBooleanList(365)
        createRandomStreaks(data)
    }

    private fun createRandomStreaks(data: ArrayList<Boolean>){
        var currRow: LinearLayout? = null
        for ((index, isInStreak) in data.withIndex()){
            if(index % PAWS_PER_ROW == 0){
                val rowLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                val horizontalMargin = if (index / PAWS_PER_ROW == 22) 36 else 0
                currRow = LinearLayout(this)
                rowLayoutParams.weight = 1f
                rowLayoutParams.setMargins(horizontalMargin, 0, horizontalMargin, 15)
                currRow.layoutParams = rowLayoutParams

                binding.dailyStreakVerticalLL.addView(currRow)

            }
            val img = ImageView(this)
            val imgLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            img.setImageResource(if (isInStreak) R.drawable.paw_streak else R.drawable.paw_streak_gray)
            imgLayoutParams.weight = 1f
            img.setPadding(0, 0, 5, 0)
            img.layoutParams = imgLayoutParams
            currRow?.addView(img)
        }
    }

    private fun generateRandomBooleanList(size: Int): ArrayList<Boolean> {
        val random = Random.Default
        val booleanList = ArrayList<Boolean>(size)

        repeat(size) {
            val randomBoolean = random.nextBoolean()
            booleanList.add(randomBoolean)
        }

        return booleanList
    }


}