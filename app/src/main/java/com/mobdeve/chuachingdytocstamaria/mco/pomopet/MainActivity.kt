package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
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

    private var initialTimeInMins = 25
    var timeInMs = 0L

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
            val time = initialTimeInMins * 60000L
            startTimer(time)
        }

        binding.pauseResumeBtn.setOnClickListener{
            if(!isRunning){
                startTimer(timeInMs)
            } else{
                pauseTimer()

            }
        }

    }

    private fun pauseTimer(){
        binding.pauseResumeBtn.text = "Resume"
        countdownTimer.cancel()
        isRunning = false

    }

    private fun startTimer(time: Long){
        countdownTimer = object : CountDownTimer(time, 1000){
            override fun onFinish() {
                TODO("Handle changing between breaks and increasing cycles")
            }

            override fun onTick(millisUntilFinished: Long) {
                timeInMs = millisUntilFinished
                updateText()
            }
        }

        binding.startBtn.visibility = View.INVISIBLE
        binding.timerControlGroupLL.visibility = View.VISIBLE
        binding.pauseResumeBtn.text = "Pause"
        countdownTimer.start()
        isRunning = true

    }

    private fun updateText(){
        val minute = (timeInMs / 1000) / 60
        val seconds = (timeInMs / 1000) % 60

        binding.timerTV.text = "$minute:$seconds"
    }

}