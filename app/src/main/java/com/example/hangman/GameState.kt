package com.example.hangman

sealed class GameState {
    class Running(
        val usedLetters: String,
        val underscoreWord: String,
        val drawable: Int
    ): GameState()
    class Lose(val wordToGuess: String): GameState()
    class Win(val wordToGuess: String): GameState()
}