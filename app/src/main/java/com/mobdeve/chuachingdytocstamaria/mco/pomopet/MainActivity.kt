package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.adapters.TodoAdapter
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ActivityMainBinding
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.db.ToDoDB
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.ToDo
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var todoListRV: RecyclerView
    private lateinit var countdownTimer: CountDownTimer
    private var isRunning = false

    private var initialTimeInMins = SettingsActivity.DEFAULT_POMODORO_TIME
    private var shortBreakTimeInMins = SettingsActivity.DEFAULT_SHORT_BREAK
    private var longBreakTimeInMins = SettingsActivity.DEFAULT_LONG_BREAK
    var timeInMs = initialTimeInMins * 60000L

    // ADDED
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private val SHAKE_THRESHOLD_LOW = 500
    private val SHAKE_THRESHOLD_HIGH = 1000
    private var isShakePauseChecked = false
    private var isShakeResetChecked = false
    private lateinit var todos: ArrayList<ToDo>
    private lateinit var todoDb: ToDoDB

    private var currentTimerType = TimerType.FOCUS
    private var cycleCounter = 1

    private val executorService = Executors.newSingleThreadExecutor()


    enum class TimerType {
        FOCUS,
        SHORT_BREAK,
        LONG_BREAK
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Bear_Theme_Pomopet)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        todoDb = ToDoDB(applicationContext)

        executorService.execute{
            todos = todoDb.getAllTodos()
            runOnUiThread{
                Log.d("todos", "${todos.size}")
                this.todoListRV = binding.todoListRV
                this.todoListRV.adapter = TodoAdapter(todos)
                this.todoListRV.layoutManager = LinearLayoutManager(this,
                    LinearLayoutManager.VERTICAL,
                    false)
            }
        }

        // ADDED
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

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

    // EDITED
    override fun onStart() {
        super.onStart()
        loadSharedPreferences()
        loadGyroscope()
        updateText()
//        todos = todoDb.getAllTodos()
    }

    override fun onStop() {

        todos.forEach{todo ->
            if(todo.id == -1 && todo.label.isNotEmpty()){
                val id = todoDb.addToDo(todo)
                todo.id = id
            } else{
                todoDb.updateTodo(todo)
            }
        }
        super.onStop()
    }

    // EDITED
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

        isShakePauseChecked = sp.getBoolean(SettingsActivity.PAUSE_SHAKE_KEY, false)
        isShakeResetChecked = sp.getBoolean(SettingsActivity.RESET_SHAKE_KEY, false)

        this.timeInMs = minsToMs(initialTimeInMins)

    }

    //ADDED
    private fun loadGyroscope(){
        if ((isShakePauseChecked || isShakeResetChecked) && sensorManager != null && accelerometer != null) {
            // Only register the sensor listener if either shakePauseCb or shakeResetCb is checked
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else {
            // Unregister the sensor listener if neither shakePauseCb nor shakeResetCb is checked
            sensorManager.unregisterListener(this)
            //Toast.makeText(this, "Sensor service not detected or preferences not set.", Toast.LENGTH_SHORT).show()
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

                when (currentTimerType) {
                    TimerType.FOCUS -> {
                        if (cycleCounter >= 4) {
                            cycleCounter = 1
                            Log.d("LongBreakStart", "--LONG Break Start--")
                            startTimer(minsToMs(longBreakTimeInMins))
                            currentTimerType = TimerType.LONG_BREAK
                            binding.longBreakBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.bunny_theme_btn_active)
                            binding.pomoBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.bunny_theme_btn_inactive)
                        }else{
                            Log.d("ShortBreakStart", "--SHORT Break Start--")
                            startTimer(minsToMs(shortBreakTimeInMins))
                            currentTimerType = TimerType.SHORT_BREAK
                            binding.shortBreakBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.bunny_theme_btn_active)
                            binding.pomoBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.bunny_theme_btn_inactive)
                        }
                    }
                    TimerType.SHORT_BREAK, TimerType.LONG_BREAK -> {
                        cycleCounter++
                        Log.d("CycleCounter", "Cycle Counter: $cycleCounter")
                        Log.d("FocusStart", "--FOCUS Start--")
                        startTimer(minsToMs(initialTimeInMins))
                        currentTimerType = TimerType.FOCUS
                        binding.longBreakBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.bunny_theme_btn_inactive)
                        binding.shortBreakBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.bunny_theme_btn_inactive)
                        binding.pomoBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.bunny_theme_btn_active)
                    }
                }
            }

            override fun onTick(millisUntilFinished: Long) {
                timeInMs = millisUntilFinished
                updateText()
            }
        }
        isRunning = true
        countdownTimer.start()
        toggleViewElements(View.INVISIBLE)
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

    // ADDED
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdate > 100) {
                val diffTime = currentTime - lastUpdate
                lastUpdate = currentTime

                val x = event.values[0]
                val y = event.values[1]

                val deltaX = x - lastX
                val deltaY = y - lastY

                lastX = x
                lastY = y

                val speed = Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()) / diffTime * 10000

                if (speed > SHAKE_THRESHOLD_LOW && speed < SHAKE_THRESHOLD_HIGH) {
                    if (isShakePauseChecked && isShakeResetChecked) {
                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            // Left-Right shake detected
                            pauseTimerSettings()
                        } else {
                            // Up-Down shake detected
                            resetTimer()
                        }
                    } else if (isShakePauseChecked) {
                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            // Left-Right shake detected
                            pauseTimerSettings()
                        }
                    } else if (isShakeResetChecked) {
                        if (Math.abs(deltaY) > Math.abs(deltaX)) {
                            // Up-Down shake detected
                            resetTimer()
                        }
                    }
                }
            }
        }
    }

    // ADDED
    private fun pauseTimerSettings() {
        if (isRunning) {
            Log.d("PauseTimerShake", "---!!! Timer was paused (Shake) !!!---")
            val playIconDrawable = ContextCompat.getDrawable(this, R.drawable.play_icon)
            binding.pauseResumeBtn.text = "Resume"
            binding.pauseResumeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                playIconDrawable,
                null,
                null,
                null
            )
            countdownTimer.cancel()
            isRunning = false
        }
    }

    // ADDED
    private fun resetTimer() {
        Log.d("ResetTimerShake", "---!!! Timer was reset (Shake) !!!---")
        stopTimer()
        cycleCounter = 1 // Reset the cycle counter to 1
        timeInMs = minsToMs(initialTimeInMins) // Set the timer to the initial focus time
        currentTimerType = TimerType.FOCUS // Set the timer type to focus
        updateText()
        toggleViewElements(View.VISIBLE)
        binding.startBtn.visibility = View.VISIBLE
        binding.timerControlGroupLL.visibility = View.INVISIBLE
    }

    // ADDED
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes here if needed
    }

    // ADDED
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    //ADDED
    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

}
