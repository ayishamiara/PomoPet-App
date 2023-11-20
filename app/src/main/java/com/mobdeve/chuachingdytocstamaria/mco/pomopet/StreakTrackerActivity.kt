package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ActivityStreakTrackerBinding
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.db.StreakDB
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.StreakState
import java.util.Calendar
import java.util.GregorianCalendar
import java.text.SimpleDateFormat

class StreakTrackerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStreakTrackerBinding
    private val PAWS_PER_ROW = 16

    override fun onCreate(savedInstanceState: Bundle?) {
        val streakDB = StreakDB(this)
        super.onCreate(savedInstanceState)
        binding = ActivityStreakTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // where the data is taken from the db
        val data = generateStreak()
        createStreaks(data)

//        binding.dailyStreakTV.setText()
        binding.completedCyclesNumTV.text = streakDB.getCycles().toString()

        binding.streakBackBtn.setOnClickListener{
            finish()
        }
    }

    private fun createStreaks(data: ArrayList<StreakState>){
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

    // take the dates from the db then make a year array with the streak states
    private fun generateStreak(): ArrayList<StreakState>{
        val streakDB = StreakDB(this)
        val streakData = ArrayList<StreakState>()

        //setting the start date
        val startDate = GregorianCalendar()
        startDate.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
        startDate.set(Calendar.DAY_OF_YEAR, 1) // Get the first day of the year

        //todays date
        val currentDate = Calendar.getInstance()
        val date = startDate.clone() as Calendar

        while (date.before(currentDate)) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd").format(date.time)

            // Check if the streak date exists in the database
            // Check if the streak date exists in the database
            val streakState = try {
                val streakState = if (streakDB.isDateInDB(dateStr)) StreakState.PRODUCTIVE else StreakState.MISSED
                streakData.add(streakState)
            } catch (e: Exception) {
                // Handle any exceptions (e.g., database errors) gracefully
                e.printStackTrace()
                // You might want to add a default state or handle the exception according to your logic
                streakData.add(StreakState.MISSED)
            }

//            val randomState = if (random.nextBoolean()) StreakState.PRODUCTIVE else StreakState.MISSED
            date.add(Calendar.DAY_OF_YEAR, 1)
        }

        //setting the last day in a year
        val lastDay = Calendar.getInstance()
        lastDay.set(Calendar.DAY_OF_YEAR, 365)

        //fill up remaining days
        while (date.before(lastDay)) {
            streakData.add(StreakState.UPCOMING)
            date.add(Calendar.DAY_OF_YEAR, 1)
        }

        return streakData
    }


}