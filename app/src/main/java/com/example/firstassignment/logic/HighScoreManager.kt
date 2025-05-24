package com.example.firstassignment.logic

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

object HighScoreManager {
    private const val PREF_KEY = "HIGH_SCORES"

    fun saveScore(context: Context, newScore: HighScore): Int {
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(PREF_KEY, "[]")
        val type = object : TypeToken<MutableList<HighScore>>() {}.type
        val scores: MutableList<HighScore> = gson.fromJson(json, type)

        scores.add(newScore)
        val sorted = scores.sortedByDescending { it.score }
        val index = sorted.indexOfFirst { it.timestamp == newScore.timestamp }


        val top10 = sorted.take(10)
        prefs.edit { putString(PREF_KEY, gson.toJson(top10)) }

        return if (index < 10) index else -1
    }


    fun loadScores(context: Context): List<HighScore> {
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(PREF_KEY, "[]")
        val type = object : TypeToken<List<HighScore>>() {}.type
        return gson.fromJson(json, type)
    }
    fun getTopScores(context: Context): List<HighScore> {
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(PREF_KEY, "[]") ?: return emptyList()

        return try {
            val type = object : TypeToken<List<HighScore>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    fun clearHighScores(context: Context) {
        context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
            .edit() { remove("HIGH_SCORES") }
    }

}