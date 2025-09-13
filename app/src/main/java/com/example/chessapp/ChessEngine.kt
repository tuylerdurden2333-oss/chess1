package com.example.chessapp

import android.content.Context
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import java.io.*

class ChessEngine(context: Context) {
    private var process: Process? = null
    private var output: OutputStreamWriter? = null
    private var input: BufferedReader? = null
    
    init {
        initializeStockfish(context)
    }
    
    private fun initializeStockfish(context: Context) {
        try {
            // Extract Stockfish binary from assets
            val assetManager = context.assets
            val files = assetManager.list("stockfish")
            
            if (files != null && files.isNotEmpty()) {
                val inputStream = assetManager.open("stockfish/${files[0]}")
                val file = File(context.filesDir, files[0])
                val outputStream = FileOutputStream(file)
                
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                
                // Make the file executable
                file.setExecutable(true)
                
                // Start Stockfish process
                process = ProcessBuilder(file.absolutePath).start()
                output = OutputStreamWriter(process!!.outputStream)
                input = BufferedReader(InputStreamReader(process!!.inputStream))
                
                // Configure Stockfish for maximum difficulty
                sendCommand("uci")
                sendCommand("setoption name Hash value 1024") // Max hash size
                sendCommand("setoption name Threads value 8") // Max threads
                sendCommand("setoption name Skill Level value 20") // Max skill level
                sendCommand("setoption name Contempt value 0")
                sendCommand("setoption name Aggressiveness value 100")
                sendCommand("setoption name Min Split Depth value 1")
                sendCommand("setoption name Slow Mover value 10")
                sendCommand("setoption name nodestime value 0")
                sendCommand("setoption name Ponder value false")
                sendCommand("setoption name UCI_Chess960 value false")
                sendCommand("setoption name SyzygyPath value <empty>")
                sendCommand("setoption name SyzygyProbeDepth value 1")
                sendCommand("setoption name Syzygy50MoveRule value true")
                sendCommand("setoption name SyzygyProbeLimit value 7")
                sendCommand("ucinewgame")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getBestMove(board: Board, side: Side): Move {
        sendCommand("position fen ${board.getFen()}")
        sendCommand("go depth 30 movetime 30000") // Maximum thinking time and depth
        
        var bestMove: String? = null
        var line: String?
        
        while (input?.readLine().also { line = it } != null) {
            if (line!!.startsWith("bestmove")) {
                bestMove = line!!.split(" ")[1]
                break
            }
        }
        
        return Move(bestMove!!, side)
    }
    
    private fun sendCommand(command: String) {
        try {
            output?.write("$command\n")
            output?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun destroy() {
        try {
            sendCommand("quit")
            process?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
