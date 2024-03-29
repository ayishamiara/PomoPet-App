package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ActivitySettingsBinding
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.utils.ThemeUtil

class SettingsActivity : BaseActivity() {
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
        const val THEME_KEY = "THEME_KEY"

        const val THEME_REQUEST_CODE = 101

        var THEME_BUNNY = R.style.Bunny_Theme_Pomopet
        var THEME_CAT = R.style.Cat_Theme_Pomopet
        var THEME_BEAR = R.style.Bear_Theme_Pomopet

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
    private lateinit var bunnyBtn: ImageButton
    private lateinit var catBtn: ImageButton
    private lateinit var bearBtn: ImageButton

    private var selectedTheme: Int = THEME_BUNNY
    private var themeChanged: Boolean = false

    // onCreate method initializes the activity, sets up UI components, and initializes variables
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        themeChanged = false

        pomodoroETNumber = binding.pomodoroETNumber
        longBreakETNumber = binding.longBreakETNumber
        shortBreakETNumber = binding.shortBreakETNumber

        shakePauseCb = binding.pauseCheckBtn
        shakeResetCb = binding.resetCheckBtn

        saveBtn = binding.saveBtn
        defaultBtn = binding.defaultBtn

        bunnyBtn = binding.bunnyBtn
        catBtn = binding.catBtn
        bearBtn = binding.bearBtn

        binding.settingsBackBtn.setOnClickListener{
            if(themeChanged){
                val returnIntent = Intent()
                returnIntent.putExtra(THEME_KEY, THEME_REQUEST_CODE)
                setResult(RESULT_OK, returnIntent)
            }
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(themeChanged){
                    val returnIntent = Intent()
                    returnIntent.putExtra(THEME_KEY, THEME_REQUEST_CODE)
                    setResult(RESULT_OK, returnIntent)
                }
                finish()
            }
        })

        bunnyBtn.setOnClickListener {
            if (selectedTheme != THEME_BUNNY) {
                selectThemeBtn(bunnyBtn, THEME_BUNNY)
            }
        }

        catBtn.setOnClickListener {
            if (selectedTheme != THEME_CAT) {
                selectThemeBtn(catBtn, THEME_CAT)
            }
        }

        bearBtn.setOnClickListener {
            if (selectedTheme != THEME_BEAR) {
                selectThemeBtn(bearBtn, THEME_BEAR)
            }
        }

        saveBtn.setOnClickListener{
            this.isSaved = true
            Toast.makeText(this, "Settings saved", Toast.LENGTH_LONG).show()
            pomodoroTime = pomodoroETNumber.text.toString().toInt()
            shortBreakTime = shortBreakETNumber.text.toString().toInt()
            longBreakTime = longBreakETNumber.text.toString().toInt()

            shakePause = shakePauseCb.isChecked
            shakeReset = shakeResetCb.isChecked

            updateSaveButtonState(false)



            ThemeUtil.changeTheme(this, selectedTheme)


        }

        defaultBtn.setOnClickListener{
            this.isSaved = false
            updateSaveButtonState(true)
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
                    updateSaveButtonState(true)
                } else {
                    Log.d("on change", "changed original")
                    Log.d("on change", "$pomodoroTime")
                    updateSaveButtonState(false)
                }
            }
        }

        longBreakETNumber.addTextChangedListener(textWatcher)
        shortBreakETNumber.addTextChangedListener(textWatcher)
        pomodoroETNumber.addTextChangedListener(textWatcher)

        shakePauseCb.setOnClickListener(checkBoxListen())
        shakeResetCb.setOnClickListener(checkBoxListen())
    }


    // onStart method triggers when the activity becomes visible to the user
    override fun onStart() {
        super.onStart()
        loadSharedPreferences()
        ThemeUtil.previousTheme = selectedTheme
        ThemeUtil.selectedTheme = selectedTheme
        initThemeButtons()
        pomodoroETNumber.setText(pomodoroTime.toString())
        shortBreakETNumber.setText(shortBreakTime.toString())
        longBreakETNumber.setText(longBreakTime.toString())
    }

    // onPause method is triggered when the system is about to pause the activity
    override fun onPause() {
        super.onPause()
        if(isSaved){
            saveToSharedPreferences()
        }
    }

    // checkBoxListen method handles clicks on checkbox views
    private fun checkBoxListen(): View.OnClickListener{
        return View.OnClickListener {
            if (isCheckBoxStillOriginal()){
                updateSaveButtonState(false)
            } else {
                updateSaveButtonState(true)
            }
        }
    }

    // isTextStillOriginal method checks if text fields retain their original values
    private fun isTextStillOriginal() : Boolean {
        if((this.pomodoroETNumber.text.toString().isEmpty()) or
            (this.shortBreakETNumber.text.toString().isEmpty())or
            (this.longBreakETNumber.text.toString().isEmpty())) {
            return true
        }
        else if ((this.pomodoroETNumber.text.toString() == "0") or
            (this.shortBreakETNumber.text.toString() == "0")or
            (this.longBreakETNumber.text.toString() == "0")) {
            Toast.makeText(this, "An input of 0 for time is not allowed", Toast.LENGTH_LONG).show()
            return true
        }

        return (this.pomodoroETNumber.text?.toString()?.toInt() == pomodoroTime) and
                (this.shortBreakETNumber.text?.toString()?.toInt() == shortBreakTime) and
                (this.longBreakETNumber.text?.toString()?.toInt() == longBreakTime)
    }

    // isCheckBoxStillOriginal method checks if checkboxes retain their original values
    private fun isCheckBoxStillOriginal(): Boolean{
        return (shakePause == shakePauseCb.isChecked && shakeReset == shakeResetCb.isChecked)
    }

    // setDefaults method sets default values to UI elements
    private fun setDefaults(){
        pomodoroETNumber.setText(DEFAULT_POMODORO_TIME.toString())
        shortBreakETNumber.setText(DEFAULT_SHORT_BREAK.toString())
        longBreakETNumber.setText(DEFAULT_LONG_BREAK.toString())
        shakePauseCb.isChecked = DEFAULT_PAUSE
        shakeResetCb.isChecked = DEFAULT_RESET
        selectThemeBtn(bunnyBtn, THEME_BUNNY)
    }

    // saveToSharedPreferences method saves settings to shared preferences
    private fun saveToSharedPreferences(){
        val sp:SharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sp.edit()

        Log.d("sharedpref", "settings on destroy ${pomodoroTime}")
        editor.putInt(POMODORO_TIME_KEY, pomodoroTime)
        editor.putInt(SHORT_BREAK_KEY, shortBreakTime)
        editor.putInt(LONG_BREAK_KEY, longBreakTime)
        editor.putBoolean(PAUSE_SHAKE_KEY, shakePauseCb.isChecked)
        editor.putBoolean(RESET_SHAKE_KEY, shakeResetCb.isChecked)
        editor.putInt(THEME_KEY, selectedTheme)

        editor.apply()
    }

    // loadSharedPreferences method loads settings from shared preferences
    private fun loadSharedPreferences(){
        val sp: SharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)

//        previousTheme = selectedTheme
        pomodoroTime = sp.getInt(POMODORO_TIME_KEY, DEFAULT_POMODORO_TIME)
        shortBreakTime = sp.getInt(SHORT_BREAK_KEY, DEFAULT_SHORT_BREAK)
        longBreakTime = sp.getInt(LONG_BREAK_KEY, DEFAULT_LONG_BREAK)
        shakePauseCb.isChecked = sp.getBoolean(PAUSE_SHAKE_KEY, DEFAULT_PAUSE)
        shakeResetCb.isChecked = sp.getBoolean(RESET_SHAKE_KEY, DEFAULT_RESET)

        selectedTheme = sp.getInt(THEME_KEY, THEME_BUNNY)
    }


    // selectThemeBtn method handles the selection of theme buttons
    @SuppressLint("ResourceAsColor")
    fun selectThemeBtn(button: ImageButton, theme: Int) {
        // Reset color of previously selected button
        bunnyBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.theme_disabled_button)
        catBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.theme_disabled_button)
        bearBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.theme_disabled_button)

        // Set color of the clicked button
        button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.theme_enabled_button)

        // Update save button state
        themeChanged = true
        updateSaveButtonState(ThemeUtil.previousTheme != theme)
        selectedTheme = theme
    }

    // updateSaveButtonState method updates the state of the save button
    fun updateSaveButtonState(enable: Boolean) {
        saveBtn.isEnabled = enable
        saveBtn.isClickable = enable
    }

    // initThemeButtons method initializes and selects the appropriate theme button
    private fun initThemeButtons(){
        var btn: ImageButton = bunnyBtn
        when(selectedTheme){
            THEME_BUNNY -> btn = bunnyBtn
            THEME_CAT -> btn = catBtn
            THEME_BEAR -> btn = bearBtn
        }
        selectThemeBtn(btn, selectedTheme)
    }

}
