package hangman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/** TODO
 *
 * This assumes HangmanGame returns Character as upper case, as part
 * of the software contract.
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
        charStats_[code2Index(charI)] = -1;
      }
      for( Character charI : incorrectChars ) {
        charStats_[code2Index(charI)] = -1;
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
      if( line.length()==wordLen ) candidateWords_.add(line.toUpperCase());
    }
  }

  private void updateCandidateWords(HangmanGame game) {
    final List<String> newCandidates = new LinkedList<String>();
    final String gameWord = game.getGuessedSoFar();
    final Set<Character> incorrectChars = game.getIncorrectlyGuessedLetters();
    for( String wordI : candidateWords_ ) {
      assert wordI.length()==gameWord.length();
      boolean wordMatches = true; // Until proven otherwise
      for( int charI = 0; charI<gameWord.length(); ++charI ) {
        if( incorrectChars.contains(wordI.charAt(charI)) ) {
          wordMatches = false;
          break;
        }

        if( gameWord.charAt(charI)==HangmanGame.MYSTERY_LETTER ) {
          continue;
        }

        if( wordI.charAt(charI)!=gameWord.charAt(charI) )
        {
          wordMatches = false;
          break;
        }
      }
      if( wordMatches ) newCandidates.add(wordI);
    }
    candidateWords_ = newCandidates;
  }

    // Consider the case where we have a small number of candidate
    // words to a large number of mystery letters. Assume for the
    // moment we have plenty of remaining guesses allowed. In that case,
    // to maximize our score, we want to choose the character that best narrows down the candidates.
    // This means choosing the char whose word count is closest to one half the number of candidate words.
    //
    // On the other hand, if we're nearly out of guesses, we want to be more conservative
    // and choose the char that is most likely to yield a correct letter guess.
    // This means choosing the char present in the most words.
    //
    // We choose the letter that is closest to half of the remaining candidate words.
    // A tie (eg 10 candidates and char counts of 4 and 6 and no 5) is broken by
    // choosing the higher.
  private char chooseChar(HangmanGame game, CharStats charStats)
  {
      if( charStats.getCount(bestChar) < charStats.getCount(charI) ) {
        bestChar = charI;
      }

    // TODO: Don't think I need this value, base the halving strategy simply on wrong guesses left and stepsWhenHalving
    final int remainingMysteryLetters = Collections.frequency(Arrays.asList(game.getGuessedSoFar().toCharArray()),
                                                              HangmanGame.MYSTERY_LETTER);
    // Steps left if candidateWords_ could be halved at each subsequent guess.
    // TODO: Take worst case candidateWords_ size if halfChar is chosen and doesn't reduce by half.
    final int stepsWhenHalving = 1 + Math.log(candidateWords_.size())/Math.log(2);
    // We could expect if we try to halve the candidate words that we'll guess wrong
    // half the time.
    // 

    if( stepsWhenHalving<=remainingMysteryLetters ) {
      game.numWrongGuessesRemaining()
    }
  }

  public Guess nextGuess(HangmanGame game) {
    updateCandidateWords(game);
    // We've found the one
    if( candidateWords_.size()==1 ) TODO;
    // If there's two left, choosing by word or by char has the same information value,
    // but guessing by word will potentially give a better score.
    if( candidateWords_.size()==2 && 0<game.numWrongGuessesRemaining() ) TODO;

    /// Calculate statistics of letter frequencies
    ///
    /// The strategy assumes all words are equally likely, so we do not count a letter
    /// more than once for a given candidate word.
    //
    final CharStats charStats = new CharStats(game.getCorrectlyGuessedLetters(),
                                              game.getIncorrectlyGuessedLetters());
    // Best char to guess with as we iterate
    //
    for( int charI = A_ASCII_CODE; charI<=Z_ASCII_CODE; ++charI ) {
      if( charStats.getCount(charI)<0 ) continue;
      for( String wordI : candidateWords_ ) {
        if( -1!=wordI.indexOf(charI) )
        {
          charStats.incrementCount(charI);
        }
      }
    }
    final char chosenChar = chooseChar(game, charStats);
    System.out.println("chosenChar="+chosenChar+" number candidates: "+candidateWords_.size()+" charStats="+charStats.toString());
    if( candidateWords_.size()<20 ) {
      for( String wordI : candidateWords_ ) {
        System.out.println("  Candidate: "+wordI);
      }
    }
    for( Character charI : game.getIncorrectlyGuessedLetters() ) {
      System.out.println("  Incorrect char: "+charI);
    }
    return new GuessLetter(chosenChar);

    // TODO: Call to verify we're not duplicating a guess: public Set<Character> getAllGuessedLetters()
    // TODO: Call to verify we're not duplicating a guess: public Set<String> getIncorrectlyGuessedWords()
    // TODO: Account for invalid character
    // TODO: Test a unicode character
    //return new GuessWord(candidateWords_.remove(0)); // TODO
    //return new GuessLetter('.');
  }
}

