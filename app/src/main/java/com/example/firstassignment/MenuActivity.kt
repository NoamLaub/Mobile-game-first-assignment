package com.example.firstassignment

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.firstassignment.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPlaySlow.setOnClickListener {
            launchGame(sensorMode = false, delay = 1500L)
        }

        binding.btnPlayFast.setOnClickListener {
            launchGame(sensorMode = false, delay = 500L)
        }

        binding.btnSensorGameplay.setOnClickListener {
            launchGame(sensorMode = true, withAccel = false)
        }

        binding.btnSensorGameplayAccel.setOnClickListener {
            launchGame(sensorMode = true, withAccel = true)
        }

        binding.btnTop10.setOnClickListener {
            startActivity(Intent(this, Top10Activity::class.java))
        }
    }

    private fun launchGame(sensorMode: Boolean, delay: Long = 1000, withAccel: Boolean = false) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("SENSOR_MODE", sensorMode)
        intent.putExtra("DELAY", delay)
        intent.putExtra("ACCEL", withAccel)
        startActivity(intent)
    }
}
