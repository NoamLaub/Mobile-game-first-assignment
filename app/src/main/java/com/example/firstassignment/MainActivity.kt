package com.example.firstassignment

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firstassignment.databinding.ActivityMainBinding
import com.example.firstassignment.logic.GameController
import com.example.firstassignment.logic.Obstacle
import com.example.firstassignment.logic.ObstacleType
import com.example.firstassignment.utils.MySignal
import kotlin.random.Random
import android.os.Vibrator
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.app.AlertDialog
import android.widget.EditText
import com.example.firstassignment.logic.HighScore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit
import com.example.firstassignment.logic.HighScoreManager
import com.example.firstassignment.logic.HighScoreManager.clearHighScores


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var delay: Long = 1000
    private var score: Int = 0
    private var highestScore = 0;
    private var withAccel = false

    private val gameController = GameController(this)
    private var mediaPlayer: MediaPlayer? = null
    private val crashSoundIds = listOf(R.raw.merde, R.raw.incredible, R.raw.impossible)
    private var crashPlayer: MediaPlayer? = null

    private var mySignal: MySignal? = null
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var useSensorControls: Boolean = false

    private val MIN_DELAY = 300L
    private val MAX_DELAY = 2000L
    private val DELAY_STEP = 100L

    private var sensorListener: SensorEventListener? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mySignal = MySignal(this)
        useSensorControls = intent.getBooleanExtra("SENSOR_MODE", false)
        delay = intent.getLongExtra("DELAY", 1000)
        withAccel = intent.getBooleanExtra("ACCEL", false)

        if (useSensorControls) {
            sensorListener  = initSensor()
            binding.imgMoveLeft.isEnabled = false
            binding.imgMoveRight.isEnabled = false
            binding.imgMoveLeft.alpha = 0.3f
            binding.imgMoveRight.alpha = 0.3f
        } else {
            binding.imgMoveLeft.isEnabled = true
            binding.imgMoveRight.isEnabled = true
        }

        binding.imgMoveLeft.setOnClickListener {
            if (gameController.getHealth() > 0) {
                gameController.movePlayer(-1)
                updatePlayerGrid()
            }
        }

        binding.imgMoveRight.setOnClickListener {
            if (gameController.getHealth() > 0) {
                gameController.movePlayer(1)
                updatePlayerGrid()
            }
        }


        mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        updateSnailGrid()
        updatePlayerGrid()
        updateHealth()
        val savedScores = HighScoreManager.loadScores(this)
        highestScore = if (savedScores.isNotEmpty()) savedScores[0].score else 0
        binding.scoreText.text = "Score: $score\nHighest Score: $highestScore"
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    }

    private fun tick() {
        score++
        if (score > highestScore) highestScore = score
        updatePlayerGrid()
        updateSnailGrid()

    }

    private fun snailTick(): Int {
       return gameController.advanceSnails()
    }

    private fun updateSnailGrid() {
        val snailImages = listOf(R.drawable.img_snail, R.drawable.img_snail2, R.drawable.img_snail3)
        val snailGrid: Array<Array<Obstacle?>> = gameController.getSnailGrid()

        for (row in snailGrid.size - 1 downTo 0) {
            for (col in 0 until snailGrid[row].size) {
                val viewId = "img_${row}${col}"
                val resId = resources.getIdentifier(viewId, "id", packageName)
                val imageView = findViewById<ImageView>(resId)

                when (snailGrid[row][col]?.type) {
                    ObstacleType.ENEMY -> {
                        if (row == 0) {
                            val randomSnail = snailImages[Random.nextInt(snailImages.size)]
                            imageView.setImageResource(randomSnail)
                        } else {
                            val prevRowId = "img_${row - 1}${col}"
                            val prevResId = resources.getIdentifier(prevRowId, "id", packageName)
                            val prevImageView = findViewById<ImageView>(prevResId)
                            imageView.setImageDrawable(prevImageView.drawable)
                        }
                    }ObstacleType.COIN ->{

                            imageView.setImageResource(R.drawable.img_coin);
                    }
                    else -> imageView.setImageDrawable(null)
                }
            }
        }
    }

    private fun updatePlayerGrid() {
        val playerGrid = gameController.getPlayerGrid()
        val views = listOf(
            binding.playerPos1,
            binding.playerPos2,
            binding.imgPlayerPos3,
            binding.playerPos4,
            binding.playerPos5
        )

        for (i in views.indices) {
            if (playerGrid[i] == 1) {
                views[i].setImageResource(R.drawable.player)
            } else {
                views[i].setImageDrawable(null)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        startTimer()
    }
    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
    }



    override fun onStop() {
        super.onStop()
        stopTimer()
    }

    private val handler: Handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            val collisionResult  = snailTick()
            tick()
            updateHealth()
            if (collisionResult == -2) {
                score += 5

            } else if (collisionResult >= 0) {
                handleCollision(collisionResult)

            }
            if(collisionResult!=-1)
                playCrashSound(collisionResult)
            updateHealth()
            binding.scoreText.setText("Score: $score \n Highest Score: $highestScore")
            handler.postDelayed(this, delay)
        }
    }

    private fun startTimer() {
        handler.postDelayed(runnable, delay)

//        clearHighScores(this)

    }

    private fun stopTimer() {
        handler.removeCallbacks(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        crashPlayer?.release()
        crashPlayer = null


    }

    fun getScore(): Int = score

    fun updateHealth() {
        when (val health = gameController.getHealth()) {
            3 -> {
                binding.imgHeart1.setImageResource(R.drawable.ic_heart)
                binding.imgHeart2.setImageResource(R.drawable.ic_heart)
                binding.imgHeart3.setImageResource(R.drawable.ic_heart)
            }
            2 -> {
                binding.imgHeart1.setImageResource(R.drawable.ic_heart)
                binding.imgHeart2.setImageResource(R.drawable.ic_heart)
                binding.imgHeart3.setImageResource(R.drawable.ic_empty_heart)
            }
            1 -> {
                binding.imgHeart1.setImageResource(R.drawable.ic_heart)
                binding.imgHeart2.setImageResource(R.drawable.ic_empty_heart)
                binding.imgHeart3.setImageResource(R.drawable.ic_empty_heart)
            }
            0 -> {
                binding.imgHeart1.setImageResource(R.drawable.ic_black_heart)
                binding.imgHeart2.setImageResource(R.drawable.ic_black_heart)
                binding.imgHeart3.setImageResource(R.drawable.ic_black_heart)
                Toast.makeText(this, "C'est Escargot Sont Pas Mangeables\n Score: $score", Toast.LENGTH_LONG).show()
                triggerVibration()

                val finalScore = score
                val finalHealth = 3
                score = 0
                gameController.setHealth(finalHealth)
                getCurrentLocation { lat, lon ->
                    checkAndSaveHighScore(finalScore, lat, lon)
                }


                binding.scoreText.text = "Score: $score\nHighest Score: $highestScore"
            }
        }
    }




    private fun triggerVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }
    private fun updatePlayerLane(lane: Int) {
        val playerGrid = gameController.getPlayerGrid()
        val current = playerGrid.indexOfFirst { it == 1 }

        if (lane in playerGrid.indices && lane != current) {
            playerGrid[current] = 0
            playerGrid[lane] = 1
            updatePlayerGrid()
        }
    }
    private fun playCrashSound(isCoin: Int) {
        crashPlayer?.release()
        if(isCoin == -2)
            MediaPlayer.create(this, R.raw.coin).start()
        else{
            val randomSound = crashSoundIds.random()
            crashPlayer = MediaPlayer.create(this, randomSound)
            crashPlayer?.start()
        }

    }

    private fun handleCollision(col: Int) {

        val deadId = resources.getIdentifier("img_9$col", "id", packageName)
        val deadImageView = findViewById<ImageView>(deadId)
        deadImageView.setImageResource(R.drawable.dead_player)

        // Clear player visuals
        val playerViews = listOf(
            binding.playerPos1,
            binding.playerPos2,
            binding.imgPlayerPos3,
            binding.playerPos4,
            binding.playerPos5
        )
        playerViews.forEach { it.setImageDrawable(null) }

        // Respawn after delay
        Handler(Looper.getMainLooper()).postDelayed({
            deadImageView.setImageDrawable(null)
            gameController.resetPlayerToCenter()
            updatePlayerGrid()
        }, 1000)
    }
    private fun getCurrentLocation(onLocationReady: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationReady(location.latitude, location.longitude)
            } else {
                // Fallback coordinates or error handling if location is null
                Toast.makeText(this, "Unable to retrieve location", Toast.LENGTH_SHORT).show()
                onLocationReady(0.0, 0.0) // Optional fallback
            }
        }
    }
    private fun checkAndSaveHighScore(score: Int, lat: Double, lng: Double) {
        val placeholder = HighScore(name = "", score = score, timestamp = System.currentTimeMillis(), lat = lat, lng = lng)

        // Load current top scores
        val currentScores = HighScoreManager.getTopScores(this).toMutableList()

        // Add the new score to the list to evaluate its rank
        currentScores.add(placeholder)
        val sorted = currentScores.sortedByDescending { it.score }
        val rank = sorted.indexOfFirst { it == placeholder }

        if (rank < 10) {
            showNameInputDialog(rank, placeholder)
        }
    }



    private fun showNameInputDialog(rank: Int, scoreToUpdate: HighScore) {
        stopTimer() // ⛔ Pause the game loop immediately
        val editText = EditText(this).apply {
            hint = "Enter your name"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("🎉 New High Score! You're #${rank + 1}")
            .setMessage("Enter your name to save it to the leaderboard.")
            .setView(editText)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ ->
                val name = editText.text.toString().ifBlank { "Anonymous" }
                val updated = scoreToUpdate.copy(name = name)
                HighScoreManager.saveScore(this, updated)
                Toast.makeText(this, "Score saved as $name!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(this, "Score not saved.", Toast.LENGTH_SHORT).show()
            }
            .create()

        // Resume the game after dialog is dismissed
        dialog.setOnDismissListener {
            startTimer()  // ✅ Resume the game loop
        }

        dialog.show()
    }



    private fun initSensor(): SensorEventListener {
        return object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val x = -it.values[0]  // Left/right tilt
                    val y = it.values[1]   // Forward/backward tilt

                    // Lane control via X-axis
                    val lane = when {
                        x < -5.5 -> 0
                        x in -4.0..-2.5 -> 1
                        x in -1.5..1.5 -> 2
                        x in 2.5..4.0 -> 3
                        x > 5.5 -> 4
                        else -> -1
                    }
                    if (lane != -1) updatePlayerLane(lane)

                    // Speed control via Y-axis (acceleration/deceleration granularity)
                    if (withAccel) {
                        val speedAdjustment = when {
                            y < -7 -> -200L
                            y < -5 -> -150L
                            y < -3 -> -100L
                            y < -1 -> -50L
                            y > 7 -> +200L
                            y > 5 -> +150L
                            y > 3 -> +100L
                            y > 1 -> +50L
                            else -> 0L
                        }

                        if (speedAdjustment != 0L) {
                            delay = (delay + speedAdjustment).coerceIn(MIN_DELAY, MAX_DELAY)
                            Log.d("SensorSpeed", "Adjusted delay to $delay ms based on tilt Y: $y")
                        }
                    }
                }
            }
        }
    }



}
