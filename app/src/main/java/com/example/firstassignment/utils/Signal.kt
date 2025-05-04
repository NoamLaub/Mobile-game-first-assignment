package com.example.firstassignment.utils

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.core.content.ContextCompat


class MySignal(private val context: Context) {

    private var ringtone: Ringtone? = null

    fun sound() {
        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        ringtone?.stop()
        ringtone = RingtoneManager.getRingtone(context, notificationUri)
        ringtone?.play()
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun vibrate(durationMillis: Long = 500) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                // API 31 and above: VibratorManager
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                // API 26 to 30: Vibrator with VibrationEffect
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
            }

            else -> {
                // Below API 26: Deprecated vibrate method without VibrationEffect
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMillis)
            }
        }
    }

}
