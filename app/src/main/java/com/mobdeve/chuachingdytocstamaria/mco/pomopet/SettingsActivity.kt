package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
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
    }


    private lateinit var binding: ActivitySettingsBinding
    private var isSaved = false
    private var pomodoroTime: Int = SettingsActivity.DEFAULT_POMODORO_TIME
    private var shortBreakTime: Int = SettingsActivity.DEFAULT_SHORT_BREAK
    private var longBreakTime: Int = SettingsActivity.DEFAULT_LONG_BREAK
    private lateinit var pomodoroETNumber: EditText
    private lateinit var longBreakETNumber: EditText
    private lateinit var shortBreakETNumber: EditText
    private lateinit var saveBtn: Button
    private lateinit var defaultBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pomodoroETNumber = binding.pomodoroETNumber
        longBreakETNumber = binding.longBreakETNumber
        shortBreakETNumber = binding.shortBreakETNumber
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
            saveBtn.isEnabled = false
            saveBtn.isClickable = false
        }

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
        saveToSharedPreferences()
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

    private fun setDefaults(){
        pomodoroETNumber.setText(SettingsActivity.DEFAULT_POMODORO_TIME.toString())
        shortBreakETNumber.setText(SettingsActivity.DEFAULT_SHORT_BREAK.toString())
        longBreakETNumber.setText(SettingsActivity.DEFAULT_LONG_BREAK.toString())

    }

    private fun saveToSharedPreferences(){
        val sp:SharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sp.edit()
        Log.d("sharedpref", "settings on destroy ${pomodoroTime}")
        editor.putInt(POMODORO_TIME_KEY, pomodoroTime)
        editor.putInt(SHORT_BREAK_KEY, shortBreakTime)
        editor.putInt(LONG_BREAK_KEY, longBreakTime)

        editor.apply()

    }

    private fun loadSharedPreferences(){
        val sp: SharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)


        pomodoroTime = sp.getInt(SettingsActivity.POMODORO_TIME_KEY,
            SettingsActivity.DEFAULT_POMODORO_TIME)

        shortBreakTime = sp.getInt(SettingsActivity.SHORT_BREAK_KEY,
            SettingsActivity.DEFAULT_SHORT_BREAK)

        longBreakTime = sp.getInt(SettingsActivity.LONG_BREAK_KEY,
            SettingsActivity.DEFAULT_LONG_BREAK)
    }
}