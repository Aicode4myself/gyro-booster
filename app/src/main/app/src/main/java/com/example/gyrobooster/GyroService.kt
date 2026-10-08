package com.example.gyrobooster

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.*
import android.os.Build
import android.os.IBinder

class GyroService : Service(), SensorEventListener {

    private lateinit var sm: SensorManager
    private var gyro: Sensor? = null

    override fun onCreate() {
        super.onCreate()
        val channelId = "gyro_boost"
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(channelId, "Gyro Booster", NotificationManager.IMPORTANCE_LOW)
        )
        val notif = Notification.Builder(this, channelId)
            .setContentTitle("Gyro Booster running")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notif)
        }

        sm = getSystemService(SensorManager::class.java)
        gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("gyro", MODE_PRIVATE)
        val fromIntent = intent?.getIntExtra("level", -1) ?: -1
        val level = if (fromIntent >= 0) fromIntent else prefs.getInt("level", 3)
        prefs.edit().putInt("level", level).apply()

        val delay = when (level) {
            0 -> SensorManager.SENSOR_DELAY_NORMAL
            1 -> SensorManager.SENSOR_DELAY_UI
            2 -> SensorManager.SENSOR_DELAY_GAME
            else -> SensorManager.SENSOR_DELAY_FASTEST
        }

        sm.unregisterListener(this)
        gyro?.let { sm.registerListener(this, it, delay) }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {}
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        sm.unregisterListener(this)
        super.onDestroy()
    }
}
