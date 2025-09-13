package com.example.chessapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move

class MainActivity : AppCompatActivity(), ChessBoardView.OnMoveListener {
    private lateinit var chessBoardView: ChessBoardView
    private lateinit var resetButton: Button
    private lateinit var chessEngine: ChessEngine
    private var gameState = GameState()
    private var userSide: Side = Side.WHITE
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        chessBoardView = findViewById(R.id.chessBoardView)
        resetButton = findViewById(R.id.resetButton)
        
        chessBoardView.setOnMoveListener(this)
        chessEngine = ChessEngine(this)
        
        showSideSelectionDialog()
        
        resetButton.setOnClickListener {
            showSideSelectionDialog()
        }
    }
    
    private fun showSideSelectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_side_selection, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Choose Your Side")
            .setCancelable(false)
        
        val dialog = builder.create()
        
        dialogView.findViewById<Button>(R.id.whiteButton).setOnClickListener {
            userSide = Side.WHITE
            startNewGame()
            dialog.dismiss()
        }
        
        dialogView.findViewById<Button>(R.id.blackButton).setOnClickListener {
            userSide = Side.BLACK
            startNewGame()
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun startNewGame() {
        gameState = GameState()
        chessBoardView.setGameState(gameState)
        chessBoardView.setUserSide(userSide)
        
        if (userSide == Side.BLACK) {
            // AI makes first move if user plays black
            makeAIMove()
        }
    }
    
    override fun onMoveSelected(from: Square, to: Square) {
        val move = Move(from, to)
        
        if (gameState.isLegalMove(move)) {
            gameState.makeMove(move)
            chessBoardView.setGameState(gameState)
            
            if (gameState.isGameOver()) {
                showGameResult()
            } else {
                makeAIMove()
            }
        }
    }
    
    private fun makeAIMove() {
        Thread {
            val aiMove = chessEngine.getBestMove(gameState.getBoard(), gameState.getBoard().sideToMove)
            
            runOnUiThread {
                gameState.makeMove(aiMove)
                chessBoardView.setGameState(gameState)
                
                if (gameState.isGameOver()) {
                    showGameResult()
                }
            }
        }.start()
    }
    
    private fun showGameResult() {
        val result = when {
            gameState.getBoard().isMated -> "Checkmate! " + 
                if (gameState.getBoard().sideToMove == userSide) "You lost!" else "You won!"
            gameState.getBoard().isDraw -> "Draw!"
            gameState.getBoard().isStaleMate -> "Stalemate!"
            else -> "Game over!"
        }
        
        Toast.makeText(this, result, Toast.LENGTH_LONG).show()
    }
}
