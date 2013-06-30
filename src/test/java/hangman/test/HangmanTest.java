package hangman.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import hangman.HangmanGame;
import hangman.GuessLetter;
import hangman.GuessWord;
import hangman.Play;
import hangman.StrategyImpl;

/**
 *
 */
@RunWith(JUnit4.class)
public class HangmanTest {
  /** Play the HangmanGame over most or all words in the dictionary.
   *
   * This is not a part of the automated test suite, because of its
   * long runtime.
   *
   * @param stepSize step through the dictionary, playing the HangmanGame
   * for every stepSize-th word. eg stepSize==50 plays the game with 2%
   * of all the dictionary words.
   * @param numGuesses is the number of wrong guesses allowed.
   */
  public static void runBrute(int stepSize, int numGuesses) throws IOException
  {
    /// Take a subset of the words.txt dictionary as dictL based on stepSize
    List<String> dictL = new LinkedList<String>();
    final BufferedReader dictReader
      = new BufferedReader(new InputStreamReader(HangmanTest.class.getClassLoader().getResourceAsStream("words.txt")));
    String line;
    int lineCount = 0;
    while( (line = dictReader.readLine())!=null ) {
      if( 0==(lineCount%stepSize) ) dictL.add(line.toUpperCase());
      ++lineCount;
    }

    /// Play the game for each element of dictL, display info
    int scoreSum = 0;
    int gamesPlayed = 0;
    for( String wordI : dictL ) {
      scoreSum += Play.run(new HangmanGame(wordI, numGuesses),
                           new StrategyImpl(wordI.length(),
                                            HangmanTest.class.getClassLoader().getResourceAsStream("words.txt")));
      ++gamesPlayed;
      final double averageScore = (double)scoreSum/(double)gamesPlayed;
      System.out.println("Played word: "+wordI+", cumulative average score: "+averageScore);
    }
  }

  // TODO: Test provided word set and assert that mine performs better on average
  // TODO: Test punctuation
  // TODO: Test a Unicode character
  // TODO: Test word not in dictionary

  private int countMysteryLetters(String word) {
    int returnCount = 0;
    for( char charI : word.toCharArray() ) {
      if( Character.toUpperCase(charI)==HangmanGame.MYSTERY_LETTER ) ++returnCount;
    }
    return returnCount;
  }

  @Test
  public void testGame() {
    final String secretWord = "factual";
    final int maxWrongGuesses = 4;
    // secret word is factual, 4 wrong guesses are allowed
    final HangmanGame game = new HangmanGame(secretWord, maxWrongGuesses);

    /// Assert game properties
    class GameVerifier {
      public void verify(int expectedScore,
                         HangmanGame.Status expectedStatus,
                         int expectedWrongGuessesMade,
                         int expectedMysteryLetters,
                         int expectedCorrectLetters,
                         int expectedIncorrectLetters,
                         int expectedIncorrectWords)
      {
        assertEquals(expectedScore, game.currentScore());
        assertEquals(expectedStatus, game.gameStatus());
        assertEquals(expectedWrongGuessesMade, game.numWrongGuessesMade());
        assertEquals(maxWrongGuesses-game.numWrongGuessesMade(),
                     game.numWrongGuessesRemaining());
        assertEquals(maxWrongGuesses, game.getMaxWrongGuesses());
        assertEquals(expectedMysteryLetters, countMysteryLetters(game.getGuessedSoFar()));
        assertEquals(expectedCorrectLetters, game.getCorrectlyGuessedLetters().size());
        assertEquals(expectedIncorrectLetters, game.getIncorrectlyGuessedLetters().size());
        assertEquals(game.getCorrectlyGuessedLetters().size()+game.getIncorrectlyGuessedLetters().size(),
                     game.getAllGuessedLetters().size());
        assertEquals(expectedIncorrectWords, game.getIncorrectlyGuessedWords().size());
        assertEquals(secretWord.length(), game.getSecretWordLength());
      }
    }
    GameVerifier verifier = new GameVerifier();

    verifier.verify(0, HangmanGame.Status.KEEP_GUESSING, 0, 7, 0, 0, 0);
    new GuessLetter('a').makeGuess(game);
    verifier.verify(1, HangmanGame.Status.KEEP_GUESSING, 0, 5, 1, 0, 0);
    new GuessWord("natural").makeGuess(game);
    verifier.verify(2, HangmanGame.Status.KEEP_GUESSING, 1, 5, 1, 0, 1);
    new GuessLetter('x').makeGuess(game);
    verifier.verify(3, HangmanGame.Status.KEEP_GUESSING, 2, 5, 1, 1, 1);
    new GuessLetter('u').makeGuess(game);
    verifier.verify(4, HangmanGame.Status.KEEP_GUESSING, 2, 4, 2, 1, 1);
    new GuessLetter('l').makeGuess(game);
    verifier.verify(5, HangmanGame.Status.KEEP_GUESSING, 2, 3, 3, 1, 1);
    new GuessWord("factual").makeGuess(game);
    verifier.verify(5, HangmanGame.Status.GAME_WON,      2, 0, 3, 1, 1);
  }

  @Test
  public void testGuess() {
    // TODO
  }
}

