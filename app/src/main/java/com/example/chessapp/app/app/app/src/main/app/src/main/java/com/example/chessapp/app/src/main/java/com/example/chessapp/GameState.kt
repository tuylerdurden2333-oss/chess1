package com.example.chessapp

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator
import com.github.bhlangonijr.chesslib.move.MoveList

class GameState {
    private val board = Board()
    private val moveGenerator = MoveGenerator()
    
    fun getBoard(): Board {
        return board
    }
    
    fun isLegalMove(move: Move): Boolean {
        return getLegalMoves().any { it == move }
    }
    
    fun getLegalMoves(): MoveList {
        return moveGenerator.generateLegalMoves(board, board.sideToMove)
    }
    
    fun getLegalMoves(from: Square): List<Square> {
        val moves = getLegalMoves()
        return moves.filter { it.from == from }.map { it.to }
    }
    
    fun makeMove(move: Move) {
        board.doMove(move)
    }
    
    fun isGameOver(): Boolean {
        return board.isMated || board.isDraw || board.isStaleMate || board.isInsufficientMaterial
    }
}
