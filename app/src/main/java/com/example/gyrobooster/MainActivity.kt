package com.example.gyrobooster

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var running = false

    private fun send(level: Int) {
        val i = Intent(this, GyroService::class.java).putExtra("level", level)
        startForegroundService(i)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        val prefs = getSharedPreferences("gyro", MODE_PRIVATE)

        val label = TextView(this).apply { text = "Sensitivity"; textSize = 18f }

        val picker = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Low", "Medium", "High", "Ultra")
            )
            setSelection(prefs.getInt("level", 3))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    prefs.edit().putInt("level", pos).apply()
                    if (running) send(pos)
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }
        }

        val start = Button(this).apply {
            text = "Start Gyro Boost"
            setOnClickListener {
                running = true
                send(picker.selectedItemPosition)
            }
        }

        val stop = Button(this).apply {
            text = "Stop"
            setOnClickListener {
                running = false
                stopService(Intent(this@MainActivity, GyroService::class.java))
            }
        }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
            addView(label)
            addView(picker)
            addView(start)
            addView(stop)
        })
    }
}
