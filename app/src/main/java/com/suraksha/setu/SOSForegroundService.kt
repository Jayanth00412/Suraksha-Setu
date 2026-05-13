package com.suraksha.setu

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlin.math.sqrt

class SOSForegroundService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private lateinit var triggerReceiver: EmergencyTriggerReceiver

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        setupShakeDetection()
        
        // Register Power Button detection
        triggerReceiver = EmergencyTriggerReceiver()
        val filter = android.content.IntentFilter().apply {
            addAction(android.content.Intent.ACTION_SCREEN_ON)
            addAction(android.content.Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(triggerReceiver, filter)

        startForeground(1, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(triggerReceiver)
    }

    private fun createNotification(): Notification {
        val channelId = "sos_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SOS Background Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Suraksha-Setu Active")
            .setContentText("Listening for emergency triggers...")
            .setSmallIcon(R.drawable.ic_shield)
            .build()
    }

    private fun setupShakeDetection() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta: Float = currentAcceleration - lastAcceleration
        acceleration = acceleration * 0.9f + delta

        if (acceleration > 15) {
            val sharedPref = getSharedPreferences("SurakshaSettings", Context.MODE_PRIVATE)
            if (sharedPref.getBoolean("SHAKE_ENABLED", true)) {
                // Trigger alert broadcast
                val intent = Intent("com.suraksha.setu.TRIGGER_SOS")
                sendBroadcast(intent)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null
}
