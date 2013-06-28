package hangman;

import java.io.InputStream;

public class StrategyImpl implements GuessingStrategy {
  public StrategyImpl(int wordSize, InputStream dictionary) throws Exception {
    System.out.println("wordSize="+wordSize+" dictionary available="+dictionary.available());
  }

  public Guess nextGuess(HangmanGame game) {
    // TODO: Call to verify we're not duplicating a guess: public Set<Character> getAllGuessedLetters()
    // TODO: Call to verify we're not duplicating a guess: public Set<String> getIncorrectlyGuessedWords()
    // TODO: Account for invalid character
    // TODO: Test a unicode character
    return null;
    //return new GuessLetter('.');
  }
}

