package com.suraksha.setu

import android.content.Context
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sharedPref = getSharedPreferences("SurakshaSettings", Context.MODE_PRIVATE)
        
        val switchShake = findViewById<Switch>(R.id.switchShake)
        val switchPower = findViewById<Switch>(R.id.switchPowerTap)
        val seekSensitivity = findViewById<android.widget.SeekBar>(R.id.seekPowerSensitivity)

        switchShake.isChecked = sharedPref.getBoolean("SHAKE_ENABLED", true)
        switchPower.isChecked = sharedPref.getBoolean("POWER_TAP_ENABLED", true)
        seekSensitivity.progress = sharedPref.getInt("POWER_SENSITIVITY", 0)

        switchShake.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("SHAKE_ENABLED", isChecked).apply()
        }

        switchPower.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("POWER_TAP_ENABLED", isChecked).apply()
        }

        seekSensitivity.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                sharedPref.edit().putInt("POWER_SENSITIVITY", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }
}
