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
  /** TODO
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
    long scoreSum = 0;
    int gamesPlayed = 0;
    for( String wordI : dictL ) {
      scoreSum += Play.run(new HangmanGame(wordI, numGuesses),
                           new StrategyImpl(wordI.length(),
                                            HangmanTest.class.getClassLoader().getResourceAsStream("words.txt")));
      ++gamesPlayed;
      final double percentPlayed = (double)gamesPlayed/(double)dictL.size();
      final double averageScore = (double)scoreSum/(double)gamesPlayed;
      System.out.println("Played word: "+wordI+" cumulative average score: "+averageScore);
    }
  }

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

