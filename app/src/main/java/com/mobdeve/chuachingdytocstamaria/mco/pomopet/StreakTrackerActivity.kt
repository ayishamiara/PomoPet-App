package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ActivityStreakTrackerBinding
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.StreakState
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.random.Random

class StreakTrackerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStreakTrackerBinding
    private val PAWS_PER_ROW = 16

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStreakTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = generateStreak()
        createRandomStreaks(data)

        binding.streakBackBtn.setOnClickListener{
            finish()
        }

    }

    private fun createRandomStreaks(data: ArrayList<StreakState>){
        var currRow: LinearLayout? = null
        for ((index, streak) in data.withIndex()){
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
            img.setImageResource(streak.pawStreak)
//            img.setImageResource(if (isInStreak) R.drawable.paw_streak else R.drawable.paw_streak_gray)
            imgLayoutParams.weight = 1f
            img.setPadding(0, 0, 5, 0)
            img.layoutParams = imgLayoutParams
            currRow?.addView(img)
        }
    }

    private fun generateStreak(): ArrayList<StreakState>{
        val startDate = GregorianCalendar()
        startDate.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
        startDate.set(Calendar.DAY_OF_YEAR, 1) // Get the first day of the year

        val currentDate = Calendar.getInstance()
        val random = Random.Default

        val streakData = ArrayList<StreakState>()

        var date = startDate.clone() as Calendar

        while (date.before(currentDate)) {
            val randomState = if (random.nextBoolean()) StreakState.PRODUCTIVE else StreakState.MISSED
            streakData.add(randomState)
            date.add(Calendar.DAY_OF_YEAR, 1)
        }

        val lastDay = Calendar.getInstance()
        lastDay.set(Calendar.DAY_OF_YEAR, 365)

        while (date.before(lastDay)) {
            streakData.add(StreakState.UPCOMING)
            date.add(Calendar.DAY_OF_YEAR, 1)
        }

        return streakData
    }


}