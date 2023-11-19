package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    companion object{
        const val POMODORO_TIME_KEY = "POMODORO_TIME_KEY"
        const val SHORT_BREAK_KEY = "SHORT_BREAK_KEY"
        const val LONG_BREAK_KEY = "LONG_BREAK_KEY"
        const val DEFAULT_POMODORO_TIME = 25
        const val DEFAULT_SHORT_BREAK = 5
        const val DEFAULT_LONG_BREAK = 10
        const val SHARED_PREFERENCES_KEY = "Pomopet_Shared_Preferences"
        const val PAUSE_SHAKE_KEY =  "PAUSE_SHAKE_KEY"
        const val RESET_SHAKE_KEY =  "RESET_SHAKE_KEY"

        const val DEFAULT_PAUSE = false
        const val DEFAULT_RESET = false
    }


    private lateinit var binding: ActivitySettingsBinding
    private var isSaved = false

    private var pomodoroTime: Int = DEFAULT_POMODORO_TIME
    private var shortBreakTime: Int = DEFAULT_SHORT_BREAK
    private var longBreakTime: Int = DEFAULT_LONG_BREAK

    private var shakePause: Boolean = DEFAULT_PAUSE
    private var shakeReset: Boolean = DEFAULT_RESET

    private lateinit var pomodoroETNumber: EditText
    private lateinit var longBreakETNumber: EditText
    private lateinit var shortBreakETNumber: EditText
    private lateinit var shakePauseCb: CheckBox
    private lateinit var shakeResetCb: CheckBox
    private lateinit var saveBtn: Button
    private lateinit var defaultBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pomodoroETNumber = binding.pomodoroETNumber
        longBreakETNumber = binding.longBreakETNumber
        shortBreakETNumber = binding.shortBreakETNumber

        shakePauseCb = binding.pauseCheckBtn
        shakeResetCb = binding.resetCheckBtn

        saveBtn = binding.saveBtn
        defaultBtn = binding.defaultBtn


        binding.settingsBackBtn.setOnClickListener{
            finish()
        }

        saveBtn.setOnClickListener{
            this.isSaved = true
            Toast.makeText(this, "Settings saved", Toast.LENGTH_LONG).show()
            pomodoroTime = pomodoroETNumber.text.toString().toInt()
            shortBreakTime = shortBreakETNumber.text.toString().toInt()
            longBreakTime = longBreakETNumber.text.toString().toInt()

            shakePause = shakePauseCb.isChecked
            shakeReset = shakeResetCb.isChecked

            saveBtn.isEnabled = false
            saveBtn.isClickable = false
        }
;
        defaultBtn.setOnClickListener{
            this.isSaved = false
            saveBtn.isEnabled = true
            saveBtn.isClickable = true
            setDefaults()
        }

        val textWatcher: TextWatcher = object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isSaved = false
                if(isTextStillOriginal().not()){
                    Log.d("on change", "changed not original")
                    saveBtn.isEnabled = true
                    saveBtn.isClickable = true
                } else {
                    Log.d("on change", "changed original")
                    Log.d("on change", "$pomodoroTime")
                    saveBtn.isEnabled = false
                    saveBtn.isClickable = false
                }
            }
        }

        longBreakETNumber.addTextChangedListener(textWatcher)
        shortBreakETNumber.addTextChangedListener(textWatcher)
        pomodoroETNumber.addTextChangedListener(textWatcher)

        shakePauseCb.setOnClickListener(checkBoxListen())
        shakeResetCb.setOnClickListener(checkBoxListen())

    }

    override fun onStart() {
        super.onStart()
        loadSharedPreferences()
        pomodoroETNumber.setText(pomodoroTime.toString())
        shortBreakETNumber.setText(shortBreakTime.toString())
        longBreakETNumber.setText(longBreakTime.toString())
    }

    override fun onPause() {
        super.onPause()
        if(isSaved){
            saveToSharedPreferences()
        }
    }

    private fun checkBoxListen(): View.OnClickListener{
        return View.OnClickListener {
            if (isCheckBoxStillOriginal()){
                saveBtn.isEnabled = false
                saveBtn.isClickable = false
            } else {
                saveBtn.isEnabled = true
                saveBtn.isClickable = true
            }
        }
    }
    private fun isTextStillOriginal() : Boolean {
        if((this.pomodoroETNumber.text.toString().isEmpty()) or
            (this.shortBreakETNumber.text.toString().isEmpty())or
            (this.longBreakETNumber.text.toString().isEmpty())) {
            return true
        }

        return (this.pomodoroETNumber.text?.toString()?.toInt() == pomodoroTime) and
                (this.shortBreakETNumber.text?.toString()?.toInt() == shortBreakTime) and
                (this.longBreakETNumber.text?.toString()?.toInt() == longBreakTime)
    }

    // EDITED
    private fun isCheckBoxStillOriginal(): Boolean{
        //Toast.makeText(this, "shakepause1: " + shakePause + " shakepause2: " + shakePauseCb.isChecked + " shakereset1: " + shakeReset + " shakereset2: " + shakeResetCb.isChecked, Toast.LENGTH_SHORT).show();
        return (shakePause == shakePauseCb.isChecked && shakeReset == shakeResetCb.isChecked)

    }

    private fun setDefaults(){
        pomodoroETNumber.setText(DEFAULT_POMODORO_TIME.toString())
        shortBreakETNumber.setText(DEFAULT_SHORT_BREAK.toString())
        longBreakETNumber.setText(DEFAULT_LONG_BREAK.toString())
        shakePauseCb.isChecked = DEFAULT_PAUSE
        shakeResetCb.isChecked = DEFAULT_RESET

    }

    private fun saveToSharedPreferences(){
        val sp:SharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sp.edit()
        Log.d("sharedpref", "settings on destroy ${pomodoroTime}")
        editor.putInt(POMODORO_TIME_KEY, pomodoroTime)

        editor.putInt(SHORT_BREAK_KEY, shortBreakTime)

        editor.putInt(LONG_BREAK_KEY, longBreakTime)

        editor.putBoolean(PAUSE_SHAKE_KEY, shakePauseCb.isChecked)

        editor.putBoolean(RESET_SHAKE_KEY, shakeResetCb.isChecked)

        editor.apply()

    }

    private fun loadSharedPreferences(){
        val sp: SharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)

        pomodoroTime = sp.getInt(POMODORO_TIME_KEY, DEFAULT_POMODORO_TIME)

        shortBreakTime = sp.getInt(SHORT_BREAK_KEY, DEFAULT_SHORT_BREAK)

        longBreakTime = sp.getInt(LONG_BREAK_KEY, DEFAULT_LONG_BREAK)

        shakePauseCb.isChecked = sp.getBoolean(PAUSE_SHAKE_KEY, DEFAULT_PAUSE)

        shakeResetCb.isChecked = sp.getBoolean(RESET_SHAKE_KEY, DEFAULT_RESET)
    }
}
