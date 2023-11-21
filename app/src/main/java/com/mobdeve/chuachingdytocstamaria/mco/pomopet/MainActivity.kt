package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources.Theme
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.TypedArrayUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.adapters.TodoAdapter
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ActivityMainBinding
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.db.StreakDB
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.db.ToDoDB
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.Streak
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.TimerType
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.ToDo
import java.text.SimpleDateFormat
import java.util.Calendar
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.utils.ThemeUtil
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.utils.getAppColorRes
import java.util.concurrent.Executors

class MainActivity : BaseActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var todoListRV: RecyclerView
    private lateinit var countdownTimer: CountDownTimer
    private lateinit var sensorManager: SensorManager
    private lateinit var mediaPlayer: MediaPlayer

    private var isRunning = false
    private var initialTimeInMins = SettingsActivity.DEFAULT_POMODORO_TIME
    private var shortBreakTimeInMins = SettingsActivity.DEFAULT_SHORT_BREAK
    private var longBreakTimeInMins = SettingsActivity.DEFAULT_LONG_BREAK
    var timeInMs = initialTimeInMins * 60000L

    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private val SHAKE_THRESHOLD_LOW = 500
    private val SHAKE_THRESHOLD_HIGH = 1000
    private var isShakePauseChecked = false
    private var isShakeResetChecked = false
    private var lastPauseResumeTime: Long = 0

    private lateinit var todos: ArrayList<ToDo>
    private lateinit var todoDb: ToDoDB
    private lateinit var streakDb: StreakDB

    private var currentTimerType = TimerType.FOCUS
    private var cycleCounter = 1
    private var cycleTimer = 0

    private val executorService = Executors.newSingleThreadExecutor()
    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val themeOk = result.data?.getIntExtra(SettingsActivity.THEME_KEY, -1)!!
        if(result.resultCode == RESULT_OK && themeOk == SettingsActivity.THEME_REQUEST_CODE){
            recreate()
        }
    }

    private var currStreak: Streak? = null

    // onCreate method initializes the activity, sets up UI components, and initializes required variables
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        todoDb = ToDoDB(applicationContext)
        streakDb = StreakDB(applicationContext)

        val currentDateStr = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time)
        currStreak = streakDb.getStreakForDate(currentDateStr)
        cycleTimer = if (currStreak?.cycle_num != null) currStreak!!.cycle_num else 0

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
            resetTimer()
        }


        binding.settingsBtn.setOnClickListener{
            val intent = Intent(binding.root.context, SettingsActivity::class.java)
            settingsLauncher.launch(intent)
        }

        binding.streakBtn.setOnClickListener{
            val intent = Intent(binding.root.context, StreakTrackerActivity::class.java)
            startActivity(intent)
        }
    }

    // onStart method triggers when the activity becomes visible to the user
    override fun onStart() {
        super.onStart()
        loadSharedPreferences()
        loadGyroscope()
        updateText()
    }

    // onStop method is called when the activity is no longer visible to the user
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

    // onPause method is triggered when the system is about to pause the activity
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // onResume method is called when the activity will start interacting with the user
    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
        resetTimer()
        ThemeUtil.setThemeOnCreate(this, loadTheme())
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    // loadSharedPreferences method loads settings from shared preferences
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

    // loadGyroscope method configures and loads gyroscope sensor settings
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
        }
    }

    // minsToMs method converts minutes to milliseconds
    private fun minsToMs(timeInMins: Int): Long{
        return timeInMins * 60000L
    }

    // toggleViewElements method toggles visibility of specific UI elements
    private fun toggleViewElements(visibility: Int){
        binding.appTitleTV.visibility = visibility
        binding.settingsBtn.visibility = visibility
        binding.streakBtn.visibility = visibility
    }

    // pauseTimer method pauses the countdown timer
    private fun pauseTimer() {
        val playIconDrawable = ContextCompat.getDrawable(this, R.drawable.play_icon)
        binding.pauseResumeBtn.text = "Resume"
        binding.pauseResumeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(playIconDrawable, null, null, null)
        countdownTimer.cancel()
        isRunning = false
    }

    // stopTimer method stops the countdown timer
    private fun stopTimer(){
        if(isRunning){
            countdownTimer.cancel()
        }
        timeInMs = initialTimeInMins * 60000L
        updateText()
        isRunning = false
    }

    // startTimer method initiates and starts the countdown timer
    private fun startTimer(time: Long){
        countdownTimer = object : CountDownTimer(time, 1000){
            override fun onFinish() {
                handleFinish()
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

    private fun handleFinish(){
        stopTimer()
        toggleViewElements(View.VISIBLE)
        binding.startBtn.visibility = View.VISIBLE
        binding.timerControlGroupLL.visibility = View.INVISIBLE


//                val a = theme.obtainStyledAttributes(ThemeUtil.selectedTheme, intArrayOf(R.attr.activeState))
//                a.getColor(0, 0)
        val active = getAppColorRes(R.attr.activeState)
        val inactive = getAppColorRes(R.attr.inactiveState)
        when (currentTimerType) {
            TimerType.FOCUS -> {
                if (cycleCounter >= 4) {
                    cycleCounter = 1
                    Log.d("LongBreakStart", "--LONG Break Start--")
                    startTimer(minsToMs(longBreakTimeInMins))
                    currentTimerType = TimerType.LONG_BREAK
                    binding.longBreakBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, active)
                    binding.pomoBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, inactive)
                }else{
                    Log.d("ShortBreakStart", "--SHORT Break Start--")
                    startTimer(minsToMs(shortBreakTimeInMins))
                    currentTimerType = TimerType.SHORT_BREAK
                    binding.shortBreakBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, active)
                    binding.pomoBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, inactive)
                }

            }
            TimerType.SHORT_BREAK, TimerType.LONG_BREAK -> {
                cycleCounter++
                Log.d("CycleCounter", "Cycle Counter: $cycleCounter")
                Log.d("FocusStart", "--FOCUS Start--")
                startTimer(minsToMs(initialTimeInMins))
                cycleTimer++
                currentTimerType = TimerType.FOCUS
                binding.longBreakBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, inactive)
                binding.shortBreakBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, inactive)
                binding.pomoBtn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, active)
            }
        }

        val streak = addStreakDay()
        streak.cycle_num = cycleTimer
        streakDb.updateCycle(streak)

        playNotificationSound()
    }

    // updateText method updates the timer text on the UI
    private fun updateText() {
        val hours = ((timeInMs / 1000) / 3600)
        val minutes = ((timeInMs / 1000) % 3600) / 60
        val seconds = (timeInMs / 1000) % 60
        val timerText = if(hours > 0)
            "${padTime(hours)}:${padTime(minutes)}:${padTime(seconds)}" else "${padTime(minutes)}:${padTime(seconds)}"

        binding.timerTV.text = timerText
    }

    // padTime method adds padding to time units
    private fun padTime(unit: Long): String{
        return unit.toString().padStart(2, '0')
    }

    // onSensorChanged method detects changes in sensor data (accelerometer)
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
                            pauseResumeTimer()
                        } else {
                            // Up-Down shake detected
                            resetTimer()
                        }
                    } else if (isShakePauseChecked) {
                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            // Left-Right shake detected
                            pauseResumeTimer()
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

    // pauseResumeTimer method handles the pause and resume functionality triggered by sensor shake
    private fun pauseResumeTimer(){
        if(!isRunning){
            if(System.currentTimeMillis() - lastPauseResumeTime >= 1000) {
                startTimer(timeInMs)
                lastPauseResumeTime = System.currentTimeMillis()
            }
        } else{
            if(System.currentTimeMillis() - lastPauseResumeTime >= 1000) {
                pauseTimer()
                lastPauseResumeTime = System.currentTimeMillis()
            }
        }
    }

    // resetTimer method resets the countdown timer
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

    // onAccuracyChanged method handles changes in sensor accuracy, if needed
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    // playNotificationSound method plays a notification sound through a media player
    private fun playNotificationSound() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.notification_sound)
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
            }
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // addStreakDay method handles adding a new streak day or updating an existing one
    private fun addStreakDay(): Streak {
        val streakDB = StreakDB(this)

        // Get the current date in the format "yyyy-MM-dd"
        val currentDateStr = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time)

        // Fetch the streak for the current date
        val existingStreak = streakDB.getStreakForDate(currentDateStr)

        if (existingStreak == null) {
            // If no streak exists for the current date, add a new streak day
            val streak = Streak(date = currentDateStr)  // Assuming cycle is 0 for a new streak day
            val id = streakDB.addDate(streak)
            streak.id = id
            return streak
        }
        return existingStreak
    }

}
