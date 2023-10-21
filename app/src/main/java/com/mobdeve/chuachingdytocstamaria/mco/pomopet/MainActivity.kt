package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import android.content.Intent
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

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val pomodoroTime = result.data?.getIntExtra(SettingsActivity.POMODORO_TIME_KEY,
                SettingsActivity.DEFAULT_POMODORO_TIME)!!

            this.shortBreakTimeInMins = result.data?.getIntExtra(SettingsActivity.SHORT_BREAK_KEY,
                SettingsActivity.DEFAULT_SHORT_BREAK)!!

            this.longBreakTimeInMins = result.data?.getIntExtra(SettingsActivity.LONG_BREAK_KEY,
                SettingsActivity.DEFAULT_LONG_BREAK)!!

            this.initialTimeInMins = pomodoroTime
            this.timeInMs = minsToMs(pomodoroTime)

            updateText()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.todoListRV = binding.todoListRV
        this.todoListRV.adapter = TodoAdapter(arrayListOf(ToDo("Add todo list item here")))
        this.todoListRV.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,
            false)

        updateText()

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
            intent.putExtra(SettingsActivity.POMODORO_TIME_KEY, initialTimeInMins)
            intent.putExtra(SettingsActivity.SHORT_BREAK_KEY, shortBreakTimeInMins)
            intent.putExtra(SettingsActivity.LONG_BREAK_KEY, longBreakTimeInMins)

            settingsLauncher.launch(intent)
        }

        binding.streakBtn.setOnClickListener{
            val intent = Intent(binding.root.context, StreakTrackerActivity::class.java)

            startActivity(intent)
        }


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