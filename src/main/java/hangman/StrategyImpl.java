package hangman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/** TODO
 */
public class StrategyImpl implements GuessingStrategy {
  private List<String> candidateWords_;
  private static final int A_ASCII_CODE = (int)'A';
  private static final int Z_ASCII_CODE = (int)'Z';
  private static final int NUM_LETTERS = Z_ASCII_CODE-A_ASCII_CODE+1;

  private class CharStats {
    // Contains counts of the number of candidate words that contain that character
    //   eg charStats_[(int)'A'] would return the count for the letter a.
    // By convention, use upper case letters only, just as the HangmanGame does.
    // Entries in the array not relevant to valid letters are ignored.
    //
    // A value of -1 removes the char from consideration, because it is already guessed.
    private int[] charStats_ = new int[NUM_LETTERS];

    private int code2Index(int charCode) { return charCode-A_ASCII_CODE; }

    /** Initialize char statistics based on the info from the game.
     *
     * Those already guessed are set to -1, others to 0.
     */
    public CharStats(Set<Character> correctChars,
                     Set<Character> incorrectChars)
    {
      for( Character charI : correctChars ) {
        charStats_[code2Index(Character.toUpperCase(charI))] = -1;
      }
      for( Character charI : incorrectChars ) {
        charStats_[code2Index(Character.toUpperCase(charI))] = -1;
      }
    }

    /** Convert char statistics to string, for debugging. */
    public String toString() {
      StringBuilder statsB = new StringBuilder();
      for( int charI = A_ASCII_CODE; charI<=Z_ASCII_CODE; ++charI ) {
        statsB.append((char)charI);
        statsB.append(":");
        statsB.append(charStats_[code2Index(charI)]);
        statsB.append(" ");
      }
      return statsB.toString();
    }

    public int getCount(int charCode) { return charStats_[code2Index(charCode)]; }
    public void incrementCount(int charCode) { charStats_[code2Index(charCode)] += 1; }
  }

  public StrategyImpl(int wordLen, InputStream dictionary) throws IOException {
    candidateWords_ = new LinkedList<String>();
    final BufferedReader dictReader = new BufferedReader(new InputStreamReader(dictionary));
    String line;
    while( (line = dictReader.readLine())!=null ) {
      if( line.length()==wordLen ) candidateWords_.add(line);
    }
  }

  public Guess nextGuess(HangmanGame game) {
    /// Update candidate words
    // game.getCorrectlyGuessedLetters()
    // game.getIncorrectlyGuessedLetters()


    /// Calculate statistics of letter frequencies
    ///
    /// The strategy assumes all words are equally likely, so we do not count a letter
    /// more than once for a given candidate word.
    //
    CharStats charStats = new CharStats(game.getCorrectlyGuessedLetters(),
                                        game.getIncorrectlyGuessedLetters());
    int bestChar = A_ASCII_CODE;
    for( int charI = A_ASCII_CODE; charI<=Z_ASCII_CODE; ++charI ) {
      if( charStats.getCount(charI)<0 ) continue;
      for( String wordI : candidateWords_ ) {
        if( -1!=wordI.indexOf(Character.toLowerCase(charI))
            || -1!=wordI.indexOf(Character.toUpperCase(charI)))
        {
          charStats.incrementCount(charI);
        }
      }
      if( charStats.getCount(bestChar) < charStats.getCount(charI) ) {
        bestChar = charI;
      }
    }
    System.out.println("bestChar="+(char)bestChar+" number candidates: "+candidateWords_.size()+" charStats="+charStats.toString());
    return new GuessLetter((char)bestChar);

    // TODO: Call to verify we're not duplicating a guess: public Set<Character> getAllGuessedLetters()
    // TODO: Call to verify we're not duplicating a guess: public Set<String> getIncorrectlyGuessedWords()
    // TODO: Account for invalid character
    // TODO: Test a unicode character
    //return new GuessWord(candidateWords_.remove(0)); // TODO
    //return new GuessLetter('.');
  }
}

