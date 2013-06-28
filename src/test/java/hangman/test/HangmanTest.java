package hangman.test;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import clojure.lang.RT;
import clojure.lang.Var;

import hangman.HangmanGame;
import hangman.GuessLetter;
import hangman.GuessWord;
import hangman.StrategyImpl;

/**
 *
 */
@RunWith(JUnit4.class)
public class HangmanTest {
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
    StrategyImpl.foo(666);
  }
}

