package com.example.firstassignment.logic
import android.content.Context
import android.widget.Toast
class GameController(
    private val context: Context,
    private val colNum: Int = 3,
    private val rowNum: Int = 7,
    //private val speed: Boolean = false,
   )
{
    private var snailGrid: Array<Array<Obstacle?>> = Array(rowNum) { Array(colNum) { null } }
    private var playerGrid: Array<Int> = Array(colNum) { 0 }
    private var health = 3

    init {
        //clearSnailGrid()
        playerGrid[colNum / 2] = 1
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
        for (col in 0 until colNum) {
            snailGrid[0][col] = generateSnailOrNull()
        }
        //make sure there is at least one empty spot
        if (snailGrid[0].all { it != null }) {
            val colToClear = (0 until colNum).random()
            snailGrid[0][colToClear] = null
        }
        for(col in 0 until colNum){
            when(col){
                0->{
                    if(snailGrid[0][col]?.type==ObstacleType.ENEMY && (snailGrid[1][col+1]?.type==ObstacleType.ENEMY || snailGrid[0][col+1]?.type==ObstacleType.ENEMY) && (snailGrid[1][col+2]?.type==ObstacleType.ENEMY || snailGrid[0][col+2]?.type==ObstacleType.ENEMY)){
                        snailGrid[0][col]=null
                        break
                    }

                }
                1->{
                    if(snailGrid[0][col]?.type==ObstacleType.ENEMY && (snailGrid[1][col+1]?.type==ObstacleType.ENEMY || snailGrid[0][col+1]?.type==ObstacleType.ENEMY) && (snailGrid[1][col-1]?.type==ObstacleType.ENEMY || snailGrid[0][col-1]?.type==ObstacleType.ENEMY)){
                        snailGrid[0][col]=null
                        break
                    }
                }
                2->{
                    if(snailGrid[0][col]?.type==ObstacleType.ENEMY && (snailGrid[1][col-1]?.type==ObstacleType.ENEMY || snailGrid[0][col-1]?.type==ObstacleType.ENEMY) && (snailGrid[1][col-2]?.type==ObstacleType.ENEMY || snailGrid[0][col-2]?.type==ObstacleType.ENEMY)){
                        snailGrid[0][col]=null
                        break
                    }
                }
            }
        }


    }

    private fun generateSnailOrNull(): Obstacle? {

        return if ((0..99).random() < 40) {
            Obstacle(ObstacleType.ENEMY)
        } else {
            null
        }
    }

    fun getSnailGrid(): Array<Array<Obstacle?>> {
        return snailGrid
    }
    fun advanceSnails(){

        for(row in rowNum-1 downTo 1){
            for(col in 0 until snailGrid[row].size){
                snailGrid[row][col]=snailGrid[row-1][col]

            }
       }
        generateSnails()
        checkCollision()

    }
    fun movePlayer(i: Int) {
          for(col in 0 until playerGrid.size){
              if(playerGrid[col]==1 )
                  if((i>0 && col<playerGrid.size-1) || (i<0 && col>0)){
                      playerGrid[col]=0
                      playerGrid[col+i]=1
                      break
                  }
          }
    }
 fun getPlayerGrid(): Array<Int> {
     return playerGrid

 }
 private fun checkCollision() {
      for(col in 0 until snailGrid[0].size)
          if(snailGrid[6][col]?.type==ObstacleType.ENEMY && playerGrid[col]==1)
                  health--


  }

    fun getHealth(): Int {
        return health
    }
    fun setHealth(h: Int){
        health = h
    }

}