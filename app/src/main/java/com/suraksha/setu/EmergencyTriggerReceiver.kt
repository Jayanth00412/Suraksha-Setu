package com.suraksha.setu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class EmergencyTriggerReceiver : BroadcastReceiver() {
    private var lastScreenOffTime: Long = 0
    private var clickCount = 0

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPref = context.getSharedPreferences("SurakshaSettings", Context.MODE_PRIVATE)
        if (!sharedPref.getBoolean("POWER_TAP_ENABLED", true)) return

        val currentTime = System.currentTimeMillis()
        
        if (intent.action == Intent.ACTION_SCREEN_OFF || intent.action == Intent.ACTION_SCREEN_ON) {
            if (currentTime - lastScreenOffTime < 1000) {
                clickCount++
            } else {
                clickCount = 1
            }
            lastScreenOffTime = currentTime

            val sensitivity = sharedPref.getInt("POWER_SENSITIVITY", 0)
            val threshold = 3 + sensitivity

            if (clickCount >= threshold) { // Rapid toggles triggers SOS
                triggerSOS(context)
                clickCount = 0
            }
        }
    }

    private fun triggerSOS(context: Context) {
        Toast.makeText(context, "SOS TRIGERRED VIA POWER BUTTON", Toast.LENGTH_LONG).show()
        val sosIntent = Intent(context, SOSForegroundService::class.java)
        context.startForegroundService(sosIntent)
    }
}
