package com.example.firstassignment

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firstassignment.logic.HighScore
import com.example.firstassignment.logic.HighScoreManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class Top10Activity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private lateinit var scoresContainer: LinearLayout
    private var pendingScores: List<HighScore> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top10)

        scoresContainer = findViewById(R.id.scores_container)

        // Load high scores (before map is ready)
        pendingScores = HighScoreManager.getTopScores(this)

        // Set up the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Add UI controls to make sure it's interactive
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isScrollGesturesEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
        map.uiSettings.isMapToolbarEnabled = true
        val telAviv = LatLng(32.0853, 34.7818)
        googleMap?.addMarker(MarkerOptions().position(telAviv).title("Tel Aviv"))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(telAviv, 10f))
        // Set a default marker to confirm the map is showing
        val defaultLatLng = LatLng(32.109333, 34.855499) // Tel Aviv
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 8f))

        Log.d("Top10Activity", "Map ready, added default marker")

        // Now render the top 10
        displayScoresWithMap()
    }


    private fun displayScoresWithMap() {
        scoresContainer.removeAllViews()

        if (pendingScores.isEmpty()) {
            Toast.makeText(this, "No scores available yet!", Toast.LENGTH_SHORT).show()
            return
        }

        for ((index, score) in pendingScores.withIndex()) {
            val tv = TextView(this).apply {
                text = "${index + 1}. ${score.name} - ${score.score}"
                textSize = 18f
                setPadding(12, 12, 12, 12)
                setOnClickListener {
                    if (score.lat == 0.0 && score.lng == 0.0) {
                        Toast.makeText(context, "No location data for this score", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    Log.d("Top10Activity", "Clicked on: ${score.name}, lat: ${score.lat}, lng: ${score.lng}")
                    val position = LatLng(score.lat, score.lng)
                    googleMap?.let { map ->
                        Log.d("Top10Activity", "Updating map to: $position")
                        map.clear()
                        map.addMarker(MarkerOptions().position(position).title(score.name))
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 12f))
                    } ?: Toast.makeText(context, "Map not ready", Toast.LENGTH_SHORT).show()
                }
            }
            scoresContainer.addView(tv)
        }

        // Optional: Zoom to the first location
        pendingScores.firstOrNull()?.let {
            if (it.lat != 0.0 || it.lng != 0.0) {
                val startPos = LatLng(it.lat, it.lng)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, 3f))
            }
        }
    }
}
