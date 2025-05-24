package com.example.firstassignment.logic

import android.content.Context
import kotlin.random.Random

class GameController(
    private val context: Context,
    private val colNum: Int = 5,
    private val rowNum: Int = 10,
) {
    private var snailGrid: Array<Array<Obstacle?>> = Array(rowNum) { Array(colNum) { null } }
    private var playerGrid: Array<Int> = Array(colNum) { 0 }
    private var health = 3

    init {
        playerGrid[colNum / 2] = 1  // Place player in the middle
        generateSnails()
    }

    private fun clearSnailGrid() {
        for (row in 0 until rowNum) {
            for (col in 0 until colNum) {
                snailGrid[row][col] = null
            }
        }
    }

    private fun generateSnails() {
        // Start with all nulls
        snailGrid[0] = Array(colNum) { null }

        // Randomly choose one column to be guaranteed safe (null or coin)
        val safeCol = (0 until colNum).random()

        for (col in 0 until colNum) {
            // If this is the safe column, add either null or coin (never enemy)
            if (col == safeCol) {
                snailGrid[0][col] = if ((0..99).random() < 50) Obstacle(ObstacleType.COIN) else null
            } else {
                // Other columns: randomly add enemy/coin/null
                snailGrid[0][col] = generateObstacle()
            }
        }

        // Optional: Prevent double-enemy "wall" from forming in the same column (row 0 + row 1)
        for (col in 0 until colNum) {
            if (snailGrid[0][col]?.type == ObstacleType.ENEMY &&
                snailGrid[1][col]?.type == ObstacleType.ENEMY) {
                snailGrid[0][col] = null // Break the wall
            }
        }
    }


    private fun generateObstacle(): Obstacle? {
        val roll = (0..99).random()
        return when {
            roll < 40 -> Obstacle(ObstacleType.ENEMY)
            roll < 50-> Obstacle(ObstacleType.COIN)
            else -> null
        }
    }





    fun getSnailGrid(): Array<Array<Obstacle?>> {
        return snailGrid
    }

    fun advanceSnails():Int {
        for (row in rowNum - 1 downTo 1) {
            for (col in 0 until colNum) {
                snailGrid[row][col] = snailGrid[row - 1][col]
            }
        }
        snailGrid[0] = Array(colNum) { null }  // Clear top row before generating
        generateSnails()
        return checkCollision()
    }

    fun movePlayer(direction: Int) {
        for (col in 0 until playerGrid.size) {
            if (playerGrid[col] == 1) {
                val newPos = col + direction
                if (newPos in 0 until colNum) {
                    playerGrid[col] = 0
                    playerGrid[newPos] = 1
                }
                break
            }
        }
    }

    fun getPlayerGrid(): Array<Int> {
        return playerGrid
    }

    fun checkCollision(): Int {
        for (col in 0 until snailGrid[rowNum-1].size) {
            if (playerGrid[col] == 1) {
                when (snailGrid[rowNum-1][col]?.type) {
                    ObstacleType.ENEMY -> {
                        health--
                        snailGrid[6][col] = null
                        return col
                    }
                    ObstacleType.COIN -> {
                        snailGrid[6][col] = null
                        return -2
                    }
                    else -> {}
                }
            }
        }
        return -1
    }



    fun getHealth(): Int {
        return health
    }

    fun setHealth(h: Int) {
        health = h
    }
    fun resetPlayerToCenter() {
        for (i in playerGrid.indices) {
            playerGrid[i] = 0
        }
        playerGrid[playerGrid.size / 2] = 1
    }

}
