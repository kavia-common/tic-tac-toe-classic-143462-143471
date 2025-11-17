package org.example.app

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView

/**
 * PUBLIC_INTERFACE
 * MainActivity is the entry point of the Tic Tac Toe application.
 * It presents a centered 3x3 grid, current player indicator, game status,
 * and a Reset button. Handles turn-taking, win/draw detection, preventing
 * overwrites, and reset, styled with the Ocean Professional theme.
 */
class MainActivity : Activity() {

    // Ocean Professional theme colors
    private val oceanPrimary by lazy { 0xFF2563EB.toInt() }   // Blue
    private val oceanSecondary by lazy { 0xFFF59E0B.toInt() } // Amber
    private val oceanError by lazy { 0xFFEF4444.toInt() }     // Red
    private val surface by lazy { 0xFFFFFFFF.toInt() }        // White
    private val textColor by lazy { 0xFF111827.toInt() }      // Near-black

    private lateinit var gridLayout: GridLayout
    private lateinit var statusText: TextView
    private lateinit var turnText: TextView
    private lateinit var resetButton: Button
    private lateinit var cellButtons: Array<Button>

    private var board = CharArray(9) { ' ' }
    private var currentPlayer: Char = 'X'
    private var gameActive: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bind to XML layout
        setContentView(R.layout.activity_main)

        // Initialize views
        gridLayout = findViewById(R.id.grid)
        statusText = findViewById(R.id.statusText)
        turnText = findViewById(R.id.turnText)
        resetButton = findViewById(R.id.resetButton)

        // Prepare cell buttons array from ids
        cellButtons = Array(9) { i ->
            val resId = resources.getIdentifier("cell$i", "id", packageName)
            findViewById(resId)
        }

        styleHeader()
        setInitialTexts()
        wireCellClicks()
        wireReset()
        applyIntroFade()
    }

    private fun styleHeader() {
        // Accent underline for title-like status
        statusText.setTextColor(textColor)
        statusText.typeface = Typeface.DEFAULT_BOLD

        turnText.setTextColor(oceanPrimary)
        resetButton.setBackgroundColor(oceanPrimary)
        resetButton.setTextColor(surface)
        resetButton.stateListAnimator = null // flatter modern look
    }

    private fun setInitialTexts() {
        updateTurnText()
        statusText.text = getString(R.string.status_in_progress)
    }

    private fun wireCellClicks() {
        cellButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                onCellClicked(index, button)
            }
        }
    }

    private fun wireReset() {
        resetButton.setOnClickListener { resetGame() }
    }

    private fun onCellClicked(index: Int, button: Button) {
        if (!gameActive) return
        if (board[index] != ' ') return // prevent overwrite

        // Place current player's mark
        board[index] = currentPlayer
        button.text = currentPlayer.toString()
        button.setTextColor(if (currentPlayer == 'X') oceanPrimary else oceanSecondary)
        button.isEnabled = false
        pulse(button)

        // Check for win/draw
        when {
            checkWin(currentPlayer) -> {
                gameActive = false
                statusText.text = if (currentPlayer == 'X')
                    getString(R.string.status_x_wins) else getString(R.string.status_o_wins)
                highlightWin()
            }
            isBoardFull() -> {
                gameActive = false
                statusText.text = getString(R.string.status_draw)
                statusText.setTextColor(oceanSecondary)
            }
            else -> {
                togglePlayer()
                updateTurnText()
            }
        }
    }

    private fun pulse(view: View) {
        val anim = AlphaAnimation(0.6f, 1f)
        anim.duration = 120
        view.startAnimation(anim)
    }

    // PUBLIC_INTERFACE
    /**
     * Fades in the primary UI to provide a subtle entrance animation.
     * This keeps visual behavior consistent with Modern/Ocean theme without extra dependencies.
     */
    private fun applyIntroFade() {
        // Animate the root ScrollView (content view of the Activity)
        val root = findViewById<View>(android.R.id.content)
        root?.let {
            it.alpha = 0f
            val anim = AlphaAnimation(0f, 1f)
            anim.duration = 220
            it.startAnimation(anim)
            it.alpha = 1f
        }
    }

    private fun updateTurnText() {
        turnText.text = if (currentPlayer == 'X')
            getString(R.string.turn_x) else getString(R.string.turn_o)
        turnText.setTextColor(if (currentPlayer == 'X') oceanPrimary else oceanSecondary)
    }

    private fun togglePlayer() {
        currentPlayer = if (currentPlayer == 'X') 'O' else 'X'
    }

    private fun isBoardFull(): Boolean = board.all { it != ' ' }

    private fun checkWin(p: Char): Boolean {
        val b = board
        val wins = arrayOf(
            intArrayOf(0, 1, 2),
            intArrayOf(3, 4, 5),
            intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6),
            intArrayOf(1, 4, 7),
            intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8),
            intArrayOf(2, 4, 6),
        )
        return wins.any { (a, c, d) -> b[a] == p && b[c] == p && b[d] == p }
    }

    private fun highlightWin() {
        // Optional subtle feedback: tint status text to primary/secondary based on winner
        statusText.setTextColor(if (currentPlayer == 'X') oceanPrimary else oceanSecondary)
    }

    private fun resetGame() {
        board = CharArray(9) { ' ' }
        currentPlayer = 'X'
        gameActive = true
        statusText.text = getString(R.string.status_in_progress)
        statusText.setTextColor(textColor)
        cellButtons.forEach {
            it.text = ""
            it.isEnabled = true
        }
        updateTurnText()
        pulse(gridLayout)
    }
}
