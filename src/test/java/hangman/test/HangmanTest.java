package hangman.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.Assert;
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

  @Test
  public void factualExample() {
    HangmanGame game = new HangmanGame("factual", 4); // secret word is factual, 4 wrong guesses are allowed
    System.out.println(game);
    new GuessLetter('a').makeGuess(game);
    System.out.println(game);
    new GuessWord("natural").makeGuess(game);
    System.out.println(game);
    new GuessLetter('x').makeGuess(game);
    System.out.println(game);
    new GuessLetter('u').makeGuess(game);
    System.out.println(game);
    new GuessLetter('l').makeGuess(game);
    System.out.println(game);
    new GuessWord("factual").makeGuess(game);
    System.out.println(game);
  }

  @Test
  public void testGuess() {
    // TODO
  }
}

