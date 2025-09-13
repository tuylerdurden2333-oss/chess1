package com.example.chessapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.Side

class ChessBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    interface OnMoveListener {
        fun onMoveSelected(from: Square, to: Square)
    }
    
    private var gameState: GameState? = null
    private var userSide: Side = Side.WHITE
    private var selectedSquare: Square? = null
    private var legalMoves: List<Square> = emptyList()
    private var onMoveListener: OnMoveListener? = null
    private var squareSize = 0f
    private val lightSquareColor = Color.parseColor("#F0D9B5")
    private val darkSquareColor = Color.parseColor("#B58863")
    private val selectedColor = Color.parseColor("#FF0000")
    private val legalMoveColor = Color.parseColor("#88FF0000")
    
    private val pieceBitmaps = mutableMapOf<Piece, Bitmap>()
    
    fun setGameState(gameState: GameState) {
        this.gameState = gameState
        selectedSquare = null
        legalMoves = emptyList()
        invalidate()
    }
    
    fun setUserSide(side: Side) {
        userSide = side
        invalidate()
    }
    
    fun setOnMoveListener(listener: OnMoveListener) {
        onMoveListener = listener
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (width == 0 || height == 0) return
        
        squareSize = width / 8f
        drawBoard(canvas)
        drawHighlights(canvas)
        drawPieces(canvas)
    }
    
    private fun drawBoard(canvas: Canvas) {
        val paint = Paint()
        
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val isLightSquare = (row + col) % 2 == 0
                paint.color = if (isLightSquare) lightSquareColor else darkSquareColor
                
                val x = col * squareSize
                val y = if (userSide == Side.WHITE) row * squareSize else (7 - row) * squareSize
                
                canvas.drawRect(x, y, x + squareSize, y + squareSize, paint)
            }
        }
    }
    
    private fun drawHighlights(canvas: Canvas) {
        val paint = Paint()
        
        // Draw selected square highlight
        selectedSquare?.let { square ->
            val (col, row) = getSquarePosition(square)
            paint.color = selectedColor
            paint.alpha = 100
            canvas.drawRect(col * squareSize, row * squareSize, 
                           (col + 1) * squareSize, (row + 1) * squareSize, paint)
        }
        
        // Draw legal moves highlights
        val legalMovePaint = Paint().apply {
            color = legalMoveColor
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        
        for (move in legalMoves) {
            val (col, row) = getSquarePosition(move)
            val centerX = col * squareSize + squareSize / 2
            val centerY = row * squareSize + squareSize / 2
            val radius = squareSize / 4
            
            canvas.drawCircle(centerX, centerY, radius, legalMovePaint)
        }
    }
    
    private fun drawPieces(canvas: Canvas) {
        gameState?.getBoard()?.let { board ->
            for (square in Square.values()) {
                val piece = board.getPiece(square)
                if (piece != Piece.NONE) {
                    drawPiece(canvas, piece, square)
                }
            }
        }
    }
    
    private fun drawPiece(canvas: Canvas, piece: Piece, square: Square) {
        val bitmap = getPieceBitmap(piece)
        val (col, row) = getSquarePosition(square)
        
        val left = col * squareSize
        val top = row * squareSize
        
        canvas.drawBitmap(bitmap, null, RectF(left, top, left + squareSize, top + squareSize), null)
    }
    
    private fun getPieceBitmap(piece: Piece): Bitmap {
        if (!pieceBitmaps.containsKey(piece)) {
            val resourceName = when (piece) {
                Piece.WHITE_PAWN -> "wp"
                Piece.WHITE_KNIGHT -> "wn"
                Piece.WHITE_BISHOP -> "wb"
                Piece.WHITE_ROOK -> "wr"
                Piece.WHITE_QUEEN -> "wq"
                Piece.WHITE_KING -> "wk"
                Piece.BLACK_PAWN -> "bp"
                Piece.BLACK_KNIGHT -> "bn"
                Piece.BLACK_BISHOP -> "bb"
                Piece.BLACK_ROOK -> "br"
                Piece.BLACK_QUEEN -> "bq"
                Piece.BLACK_KING -> "bk"
                else -> return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
            
            val resourceId = resources.getIdentifier(resourceName, "drawable", context.packageName)
            pieceBitmaps[piece] = BitmapFactory.decodeResource(resources, resourceId)
        }
        
        return pieceBitmaps[piece]!!
    }
    
    private fun getSquarePosition(square: Square): Pair<Int, Int> {
        val file = square.file.ordinal
        val rank = square.rank.ordinal
        
        return if (userSide == Side.WHITE) {
            Pair(file, 7 - rank)
        } else {
            Pair(7 - file, rank)
        }
    }
    
    private fun getSquareFromPosition(x: Float, y: Float): Square? {
        val col = (x / squareSize).toInt()
        val row = (y / squareSize).toInt()
        
        if (col < 0 || col >= 8 || row < 0 || row >= 8) return null
        
        val file = if (userSide == Side.WHITE) col else 7 - col
        val rank = if (userSide == Side.WHITE) 7 - row else row
        
        return Square.fromValue("${('a' + file)}${rank + 1}")
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val square = getSquareFromPosition(event.x, event.y)
            square?.let {
                handleSquareTouch(it)
            }
        }
        return true
    }
    
    private fun handleSquareTouch(square: Square) {
        gameState?.let { state ->
            if (state.getBoard().sideToMove != userSide) return
            
            val piece = state.getBoard().getPiece(square)
            
            if (selectedSquare == null) {
                // Selecting a piece
                if (piece != Piece.NONE && piece.pieceSide == userSide) {
                    selectedSquare = square
                    legalMoves = state.getLegalMoves(square)
                    invalidate()
                }
            } else {
                // Moving a piece or selecting a different piece
                if (square == selectedSquare) {
                    // Deselect
                    selectedSquare = null
                    legalMoves = emptyList()
                    invalidate()
                } else if (legalMoves.contains(square)) {
                    // Make move
                    onMoveListener?.onMoveSelected(selectedSquare!!, square)
                    selectedSquare = null
                    legalMoves = emptyList()
                } else if (piece != Piece.NONE && piece.pieceSide == userSide) {
                    // Select different piece
                    selectedSquare = square
                    legalMoves = state.getLegalMoves(square)
                    invalidate()
                } else {
                    // Invalid move, deselect
                    selectedSquare = null
                    legalMoves = emptyList()
                    invalidate()
                }
            }
        }
    }
}
