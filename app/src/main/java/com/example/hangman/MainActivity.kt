package com.example.hangman

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children

class MainActivity : AppCompatActivity() {
    private val gameManager = GameManager()

    private lateinit var wordTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var gameLoseTextView: TextView
    private lateinit var gameWinTextView: TextView
    private lateinit var newGameButton: Button
    private lateinit var lettersLayout: GridLayout
    private lateinit var hintText: TextView
    private lateinit var hintButton: Button

    private val isRunning = false //initially


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_main)
        initializeViews() // Method to initialize all views
        restoreGameState() // Method to restore the game state
    }

    private fun initializeViews() {
        imageView = findViewById(R.id.hangmanDrawing)
        wordTextView = findViewById(R.id.wordTextView)
        gameLoseTextView = findViewById(R.id.gameLoseTextView)
        gameWinTextView = findViewById(R.id.gameWinTextView)
        newGameButton = findViewById(R.id.newGameButton)
        lettersLayout = findViewById(R.id.gridLayout)
        hintButton = findViewById(R.id.hintButton)
        hintText = findViewById(R.id.hintText)

        newGameButton.setOnClickListener {
            startNewGame()
        }

        hintButton.setOnClickListener {
            handleHint()
        }

        lettersLayout.children.forEach { letter ->
            if (letter is Button) {
                letter.setOnClickListener {
                    val newGameState = gameManager.play((letter).text[0])
                    update(newGameState)
                    letter.isEnabled = false
                }
            }
        }
    }

    private fun restoreGameState() {
        // Use gameManager to restore the game state
        update(gameManager.getGameState())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save your game state
        outState.putString("usedLetters", gameManager.usedLetters)
        outState.putString("underscoreWord", gameManager.underscoreWord)
        outState.putInt("currentAttempts", gameManager.currentAttempts)
        outState.putString("wordToGuess", gameManager.wordToGuess)
        outState.putInt("hintClick", gameManager.hintClick)
        outState.putBoolean("hintState", hintButton.isEnabled)

        // Save the enabled/disabled state of each button
        val buttonStates = BooleanArray(lettersLayout.childCount)
        lettersLayout.children.forEachIndexed { index, view ->
            if (view is Button) {
                buttonStates[index] = view.isEnabled
            }
        }
        outState.putBooleanArray("buttonStates", buttonStates)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        gameManager.usedLetters = savedInstanceState.getString("usedLetters") ?: ""
        gameManager.underscoreWord = savedInstanceState.getString("underscoreWord") ?: ""
        gameManager.currentAttempts = savedInstanceState.getInt("currentAttempts")
        gameManager.wordToGuess = savedInstanceState.getString("wordToGuess") ?: ""
        gameManager.hintClick = savedInstanceState.getInt("hintClick")
        hintButton.isEnabled = savedInstanceState.getBoolean("hintState")

        val buttonStates = savedInstanceState.getBooleanArray("buttonStates")
        buttonStates?.forEachIndexed { index, isEnabled ->
            (lettersLayout.getChildAt(index) as? Button)?.isEnabled = isEnabled
        }

        update(gameManager.getGameState())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()

        if (savedInstanceState == null) {
            val gameState = gameManager.startNewGame()
            update(gameState)
        }
    }

    private fun update(gameState: GameState) {
        when (gameState) {
            is GameState.Win -> showGameWin(gameState.wordToGuess)
            is GameState.Lose -> showGameLose(gameState.wordToGuess)
            is GameState.Running -> {
                wordTextView.text = gameState.underscoreWord
                imageView.setImageDrawable(ContextCompat.getDrawable(this, gameState.drawable))
            }
        }
    }

    private fun handleHint() {
        gameManager.hintClick++

        when (gameManager.hintClick) {
            1 -> {
                hintText.setText("Hint: Food")
            }
            2 -> {
                if (gameManager.currentAttempts >= gameManager.maxAttempts - 1) {
                    Toast.makeText(this, "Hint not available", Toast.LENGTH_SHORT).show()
                    return
                }

                disableRandomLetters()
                gameManager.currentAttempts++

                update(gameManager.getGameState())
            }
            3 -> {
                if (gameManager.currentAttempts >= gameManager.maxAttempts - 1) {
                    Toast.makeText(this, "Hint not available", Toast.LENGTH_SHORT).show()
                    return
                }

                showVowels()
                gameManager.currentAttempts++

                update(gameManager.getGameState())

                hintButton.isEnabled = false
            }
            else -> hintButton.isEnabled = false
        }
    }

    private fun disableRandomLetters() {
        val lettersToDisable = getLettersToDisable()
        lettersLayout.children.forEach { letter ->
            if (letter is Button && letter.text[0] in lettersToDisable) {
                letter.isEnabled = false
            }
        }
    }

    private fun getLettersToDisable(): List<Char> {
        val letters = ('A'..'Z').toList() // Adjust based on your letter range
        val lettersNotInWord = letters.filter { it !in gameManager.wordToGuess.uppercase() &&
                                                it !in gameManager.usedLetters.uppercase() }

        val lettersToDisable = lettersNotInWord.shuffled().take(lettersNotInWord.size / 2)
        gameManager.usedLetters += lettersToDisable.joinToString("")

        return lettersToDisable
    }

    fun showVowels() {
        val vowels = listOf('A', 'E', 'I', 'O', 'U')
        var updatedUnderscoreWord = gameManager.underscoreWord
        for (i in gameManager.wordToGuess.indices) {
            if (gameManager.wordToGuess[i].uppercaseChar() in vowels &&
                gameManager.wordToGuess[i].uppercaseChar() !in gameManager.usedLetters) {
                gameManager.usedLetters += gameManager.wordToGuess[i]
                val output = StringBuilder(updatedUnderscoreWord)
                    .also { it.setCharAt(2 * i, gameManager.wordToGuess[i]) }
                updatedUnderscoreWord = output.toString()
            }
        }

        gameManager.underscoreWord = updatedUnderscoreWord

        lettersLayout.children.forEach { letter ->
            if (letter is Button && letter.text[0] in vowels) {
                letter.isEnabled = false
            }
        }
    }

    private fun showGameLose(wordToGuess: String) {
        wordTextView.text = wordToGuess
        gameLoseTextView.visibility = View.VISIBLE
        imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.hangman10))
        lettersLayout.visibility = View.GONE
    }

    private fun showGameWin(wordToGuess: String) {
        wordTextView.text = wordToGuess
        gameWinTextView.visibility = View.VISIBLE
        lettersLayout.visibility = View.GONE
    }

    private fun startNewGame() {
        gameLoseTextView.visibility = View.GONE
        gameWinTextView.visibility = View.GONE
        val gameState = gameManager.startNewGame()
        lettersLayout.visibility = View.VISIBLE
        lettersLayout.children.forEach { letter ->
            letter.isEnabled = true
        }
        hintButton.isEnabled = true
        update(gameState)
    }
}
