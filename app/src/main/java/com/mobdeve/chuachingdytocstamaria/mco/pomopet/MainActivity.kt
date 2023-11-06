package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.adapters.TodoAdapter
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ActivityMainBinding
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.ToDo

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var todoListRV: RecyclerView
    private lateinit var countdownTimer: CountDownTimer
    private var isRunning = false

    private var initialTimeInMins = SettingsActivity.DEFAULT_POMODORO_TIME
    private var shortBreakTimeInMins = SettingsActivity.DEFAULT_SHORT_BREAK
    private var longBreakTimeInMins = SettingsActivity.DEFAULT_LONG_BREAK
    var timeInMs = initialTimeInMins * 60000L

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.todoListRV = binding.todoListRV
        this.todoListRV.adapter = TodoAdapter(arrayListOf(ToDo("Add todo list item here")))
        this.todoListRV.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,
            false)

        binding.startBtn.setOnClickListener{
            val time = minsToMs(initialTimeInMins)
            startTimer(time)
            toggleViewElements(View.INVISIBLE)
        }

        binding.pauseResumeBtn.setOnClickListener{
            if(!isRunning){
                startTimer(timeInMs)
            } else{
                pauseTimer()
            }
        }

        binding.stopBtn.setOnClickListener{
            stopTimer()
            toggleViewElements(View.VISIBLE)
            binding.startBtn.visibility = View.VISIBLE
            binding.timerControlGroupLL.visibility = View.INVISIBLE
        }


        binding.settingsBtn.setOnClickListener{
            val intent = Intent(binding.root.context, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.streakBtn.setOnClickListener{
            val intent = Intent(binding.root.context, StreakTrackerActivity::class.java)

            startActivity(intent)
        }


    }

    override fun onStart() {
        super.onStart()
        loadSharedPreferences()
        updateText()
    }

    private fun loadSharedPreferences(){
        val sp: SharedPreferences = getSharedPreferences(SettingsActivity.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)


        initialTimeInMins = sp.getInt(SettingsActivity.POMODORO_TIME_KEY,
            SettingsActivity.DEFAULT_POMODORO_TIME)

        Log.d("sharedpref", "load shared pref ${sp.getInt(SettingsActivity.POMODORO_TIME_KEY,
            SettingsActivity.DEFAULT_POMODORO_TIME)}")

        shortBreakTimeInMins = sp.getInt(SettingsActivity.SHORT_BREAK_KEY,
            SettingsActivity.DEFAULT_SHORT_BREAK)

        longBreakTimeInMins = sp.getInt(SettingsActivity.LONG_BREAK_KEY,
            SettingsActivity.DEFAULT_LONG_BREAK)

        this.timeInMs = minsToMs(initialTimeInMins)

    }

    private fun minsToMs(timeInMins: Int): Long{
        return timeInMins * 60000L
    }

    private fun toggleViewElements(visibility: Int){
        binding.appTitleTV.visibility = visibility
        binding.settingsBtn.visibility = visibility
        binding.streakBtn.visibility = visibility
    }

    private fun pauseTimer() {
        val playIconDrawable = ContextCompat.getDrawable(this, R.drawable.play_icon)
        binding.pauseResumeBtn.text = "Resume"
        binding.pauseResumeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(playIconDrawable, null, null, null)
        countdownTimer.cancel()
        isRunning = false
    }

    private fun stopTimer(){
        countdownTimer.cancel()
        timeInMs = initialTimeInMins * 60000L
        updateText()
        isRunning = false
    }

    private fun startTimer(time: Long){
        countdownTimer = object : CountDownTimer(time, 1000){
            override fun onFinish() {
                stopTimer()
                toggleViewElements(View.VISIBLE)
                binding.startBtn.visibility = View.VISIBLE
                binding.timerControlGroupLL.visibility = View.INVISIBLE

            }

            override fun onTick(millisUntilFinished: Long) {
                timeInMs = millisUntilFinished
                updateText()
            }
        }
        isRunning = true
        countdownTimer.start()
        binding.startBtn.visibility = View.INVISIBLE
        binding.timerControlGroupLL.visibility = View.VISIBLE
        val pauseIconDrawable = ContextCompat.getDrawable(this, R.drawable.pause_icon)
        binding.pauseResumeBtn.text = "Pause"
        binding.pauseResumeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(pauseIconDrawable, null, null, null)


    }

    private fun updateText(){
        val minute = (timeInMs / 1000) / 60
        val seconds = (timeInMs / 1000) % 60

        binding.timerTV.text = "${padTime(minute)}:${padTime(seconds)}"
    }

    private fun padTime(unit: Long): String{
        return unit.toString().padStart(2, '0')
    }

}