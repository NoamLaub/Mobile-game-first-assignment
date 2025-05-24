package com.example.firstassignment.logic

data class Obstacle(
    val type: ObstacleType?
)

enum class ObstacleType {
    ENEMY, COIN
}