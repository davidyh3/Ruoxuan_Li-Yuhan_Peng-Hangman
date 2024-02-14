package com.example.hangman

import kotlin.random.Random

class GameManager {

    var usedLetters: String = ""
    lateinit var underscoreWord: String
    lateinit var wordToGuess: String
    val maxAttempts = 10
    var currentAttempts = 0
    private var drawable: Int = R.drawable.hangman0
    var hintClick = 0

    fun startNewGame(): GameState {
        usedLetters = ""
        currentAttempts = 0
        hintClick = 0
        drawable = R.drawable.hangman0
        val index = Random.nextInt(0, Words.words.size)
        wordToGuess = Words.words[index]
        generateUnderscores(wordToGuess)
        return getGameState()
    }

    fun generateUnderscores(word: String) {
        val output = StringBuilder()
        word.forEach { _ ->
            output.append("_ ")
        }

        underscoreWord = output.toString()
    }

    fun play(letter: Char): GameState {
        if (usedLetters.contains(letter)) {
            return GameState.Running(usedLetters, underscoreWord, drawable)
        }

        usedLetters += letter
        val indexes = mutableListOf<Int>()

        wordToGuess.forEachIndexed { index, char ->
            if (char.equals(letter, ignoreCase = true)) {
                indexes.add(index)
            }
        }

        var updatedUnderscoreWord = underscoreWord
        indexes.forEach() {index ->
            val output = StringBuilder(updatedUnderscoreWord)
                .also { it.setCharAt(2 * index, letter.lowercaseChar()) }
            updatedUnderscoreWord = output.toString()
        }

        if (indexes.isEmpty()) {
            currentAttempts++
        }

        underscoreWord = updatedUnderscoreWord
        return getGameState()
    }

    private fun getHangmanDrawable(): Int {
        return when (currentAttempts) {
            0 -> R.drawable.hangman0
            1 -> R.drawable.hangman1
            2 -> R.drawable.hangman2
            3 -> R.drawable.hangman3
            4 -> R.drawable.hangman4
            5 -> R.drawable.hangman5
            6 -> R.drawable.hangman6
            7 -> R.drawable.hangman7
            8 -> R.drawable.hangman8
            9 -> R.drawable.hangman9
            else -> R.drawable.hangman10
        }
    }

    fun getGameState(): GameState {
        val cleanUnderscoreWord = underscoreWord.replace("_", "").replace(" ", "")
        if (cleanUnderscoreWord.equals(wordToGuess, ignoreCase = true)) {
            return GameState.Win(wordToGuess)
        }

        if (currentAttempts == maxAttempts) {
            return GameState.Lose(wordToGuess)
        }

        drawable = getHangmanDrawable()
        return GameState.Running(usedLetters, underscoreWord, drawable)
    }
}
