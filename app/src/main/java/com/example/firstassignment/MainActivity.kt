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
import com.example.firstassignment.utils.TimeFormatter
import kotlin.random.Random
import androidx.core.content.edit
import android.os.Vibrator
import android.content.Context
import android.os.Build
import android.os.VibrationEffect


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val startTime = System.currentTimeMillis()
    private var mySignal: MySignal? = null
    private val DELAY: Long = 1000
    private var score: Int = 0
    private var highScore = 0

    private val gameController = GameController(this)
    private var mediaPlayer: MediaPlayer? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mySignal = MySignal(this)

        binding.imgMoveLeft.setOnClickListener {
            if(gameController.getHealth()>0) {
                gameController.movePlayer(-1)
                updatePlayerGrid()
            }
        }
        binding.imgMoveRight.setOnClickListener {
            if(gameController.getHealth()>0){
                gameController.movePlayer(1)
                updatePlayerGrid()
            }
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music) // assuming it's my_audio_file.mp4
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()


        updateSnailGrid()
        updatePlayerGrid()
        updateHealth()
        highScore = loadHighScore()

        binding.scoreText.text = "Score: $score\nHigh Score: $highScore"

        /* binding.lblTime.setOnClickListener {
             mySignal?.sound()
         }*/
    }

    private fun tick() {
        score++
        if (score > highScore)
            highScore = score

        Log.d("pttt", "tick - " + Thread.currentThread().name)
        val currentTime = System.currentTimeMillis()
        binding.scoreText.text = "Score: $score\nHigh Score: $highScore"
        updateSnailGrid()
        updatePlayerGrid()
    }
    private fun snailTick(){
        gameController.advanceSnails()
    }

    private fun updateSnailGrid(){
        val snailImages = listOf(
            R.drawable.img_snail,
            R.drawable.img_snail2,
            R.drawable.img_snail3,
        )
        val snailGrid: Array<Array<Obstacle?>> = gameController.getSnailGrid()
          for (row in snailGrid.size - 1 downTo 0) {
              for (col in 0 until snailGrid[row].size) {
                  val viewId = "img_${row}${col}"  // Construct the ID string, e.g., "img00", "img01", etc.
                  val resId = resources.getIdentifier(viewId, "id", packageName)
                  val imageView = findViewById<ImageView>(resId)
                  when (snailGrid[row][col]?.type) {
                      ObstacleType.ENEMY -> {   if(row==0){
                          val randomSnail = snailImages[Random.nextInt(snailImages.size)]
                          imageView.setImageResource(randomSnail)
                      }
                      else {
                          val row2 = row-1
                          val viewId2 = "img_${row2}${col}"
                          val resId2 = resources.getIdentifier(viewId2, "id", packageName)
                          val imageView2 = findViewById<ImageView>(resId2)
                          imageView.setImageDrawable(imageView2.drawable)
                      }}
                      //ObstacleType.BONUS -> setImageResource(R.drawable.img_bonus)
                      else -> imageView.setImageDrawable(null) // Empty cell
                  }

                  }
          }
    }
    private fun updatePlayerGrid(){
        val playerview0: ImageView = binding.playerPos1
        val playerview1: ImageView = binding.playerPos2
        val playerview2: ImageView = binding.imgPlayerPos3
        val playerGrid: Array<Int> = gameController.getPlayerGrid()

        for(col in 0 until playerGrid.size){
            if(playerGrid[col]==1){
                when(col){
                    0->{playerview0.setImageResource(R.drawable.player)
                        playerview1.setImageDrawable(null)
                        playerview2.setImageDrawable(null)}
                    1->{playerview1.setImageResource(R.drawable.player)
                        playerview0.setImageDrawable(null)
                        playerview2.setImageDrawable(null)}
                    2->{playerview2.setImageResource(R.drawable.player)
                        playerview0.setImageDrawable(null)
                        playerview1.setImageDrawable(null)}
                }
        }
    }}


    override fun onStart() {
        super.onStart()
        startTimer()
    }

    override fun onStop() {
        super.onStop()
        stopTimer()
    }


    //////////////////////////////// START STOP CODE ////////////////////////////////

    private val handler: Handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {


            snailTick()
            tick()
            updateHealth()
            binding.scoreText.text = "Score: $score High Score: $highScore"

            handler.postDelayed(this, DELAY)

        }
    }

    private fun startTimer() {
        handler.postDelayed(runnable, DELAY)
    }

    private fun stopTimer() {
        // Remove any pending runnable from the handler
        handler.removeCallbacks(runnable)
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        saveHighScore(score)
    }
    fun getScore(): Int {
        return score
    }
    fun updateHealth(){
        val health = gameController.getHealth()
        when(health){
            3->{binding.imgHeart1.setImageResource(R.drawable.ic_heart)
                binding.imgHeart2.setImageResource(R.drawable.ic_heart)
                binding.imgHeart3.setImageResource(R.drawable.ic_heart)}
            2->{binding.imgHeart1.setImageResource(R.drawable.ic_heart)
                binding.imgHeart2.setImageResource(R.drawable.ic_heart)
                binding.imgHeart3.setImageResource(R.drawable.ic_empty_heart)}
            1->{binding.imgHeart1.setImageResource(R.drawable.ic_heart)
                binding.imgHeart2.setImageResource(R.drawable.ic_empty_heart)
                binding.imgHeart3.setImageResource(R.drawable.ic_empty_heart)}
            0->{binding.imgHeart1.setImageResource(R.drawable.ic_empty_heart)
                binding.imgHeart2.setImageResource(R.drawable.ic_empty_heart)
                binding.imgHeart3.setImageResource(R.drawable.ic_empty_heart)
                binding.imgHeart1.setImageResource(R.drawable.ic_black_heart)
                binding.imgHeart2.setImageResource(R.drawable.ic_black_heart)
                binding.imgHeart3.setImageResource(R.drawable.ic_black_heart)
                Toast.makeText(this, "C'est Escargot Sont Pas Mangeables\n"+" Score: "+score, Toast.LENGTH_LONG).show()
                triggerVibration()
                saveHighScore(score)
                gameController.setHealth(3)
                score=0
                binding.scoreText.text = "Score: $score High Score: $highScore"
            }
        }
    }
    private fun saveHighScore(score: Int) {
        val sharedPref = getSharedPreferences("game_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("HIGH_SCORE", score)
            apply()  // Make sure changes are applied
        }
    }

    private fun loadHighScore(): Int {
        val sharedPref = getSharedPreferences("game_prefs", MODE_PRIVATE)
        return sharedPref.getInt("HIGH_SCORE", 0) // Default to 0 if not found
    }
    private fun triggerVibration() {
        // Check if device has vibration capability
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For devices with API >= 26 (Android Oreo), use VibrationEffect
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // For devices with lower API levels, use the old method
            vibrator.vibrate(500)  // Vibrate for 500 milliseconds
        }
    }

}
