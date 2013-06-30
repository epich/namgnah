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



  /**
   * Choose the char (letter) to use given the game status and
   * character stats.
   *
   * This function decides which of two approaches to use in
   * choosing. The result of the two approaches are the local
   * variables:
   *   - highestChar
   *   - reductionChar
   *
   * To understand the distinction, contrast the cases where we have
   * few wrong guesses remaining vs many.
   *   - Few: the risk of running out of guesses overrules the the
   *          desire to save a point or two from the final score.
   *   - Many: running out of guesses is not a concern, so focus on
   *           using each score point to reduce candidate words as
   *           much as possible.
   *
   * If for example a char appears in 90% of words, how much can it
   * reduce the candidate words? At least 10% is assured, likely more
   * because (if correct), the positions of the revealed chars convey
   * further information to allow more eliminations. Still, a
   * reduction of 50% is not assured.
   *
   * If a char appears in 50% of words, then a 50% reduction
   * in candidate words is a sure thing. If the candidate words can
   * be halved at each guess, convergence to the answer is good.
   * Since the potential to halve at all subsequent guesses is
   * not verifiable efficiently, this approach is heuristic.
   *
   * But is 50% the right proportion, still assuming wrong guesses
   * remaining are sufficiently high? As mentioned, correct guesses
   * have a benefit wrong guesses do not: they can eliminate
   * additional words based on the revealed positions of the
   * characters. IOW, more useful information is revealed. So a
   * proportion a bit above 50% would be better. reductionProportion
   * is precisely this value and was tuned
   * experimentally using:
   *   rake brute STEP_SIZE=100
   * to compute average scores on 1% of the dictionary. (The Rakefile
   * documents the Rake build target further.)
   *
   * The final matter is quantifying when the wrong guesses remaining
   * are sufficient to choose reductionChar over highestChar. Since
   * the goal of the reductionChar is to reduce candidate words by
   * half on average, we compare the log base 2 of the remaining
   * candidate words to the wrong guesses remaining.
   */
  private char chooseChar(HangmanGame game, CharStats charStats)
  {
    // When we have ample wrong guess remaining, we'll choose chars
    // with a count that is reductionProportion percent of the total
    // candidate words. eg 0.5 means we would attempt to halve.
    //
    // With 0.5:   7.514976958525345
    // With 0.55:  7.36520737327189
    // With 0.6:   7.348502304147465
    // With 0.61:  7.341013824884793
    // With 0.62:  7.334677419354839
    // With 0.625: 7.341589861751152
    // With 0.63:  7.350230414746544
    // With 0.65:  7.352534562211981
    // With 0.7:   7.400345622119816
    double reductionProportion = 0.62;
    // We'll either try to halve the candidate words, or try for the
    // most probable correct letter
    int reductionChar = A_ASCII_CODE;
    int halvingChar = A_ASCII_CODE;
    int highestChar = A_ASCII_CODE;
    for( int charI = A_ASCII_CODE; charI<=Z_ASCII_CODE; ++charI ) {
      if( charStats.getCount(highestChar) < charStats.getCount(charI) )
      {
        highestChar = charI;
      }

      final double reductionPoint = reductionProportion*(double)candidateWords_.size();
      final double charI_fromReductionPoint = Math.abs(reductionPoint-charStats.getCount(charI));
      final double reductionChar_fromReductionPoint = Math.abs(reductionPoint-charStats.getCount(reductionChar));
      if( charI_fromReductionPoint < reductionChar_fromReductionPoint ) {
        reductionChar = charI;
      }

      // Multiply by 2 to keep integers whole. We're still measuring from the middle,
      // just in other (consistent) units.
      final int charI_fromMid = Math.abs(2*charStats.getCount(charI)-candidateWords_.size());
      final int halvingChar_fromMid = Math.abs(2*charStats.getCount(halvingChar)-candidateWords_.size());
      // Pick between charI and halvingChar. Break tie by choosing the higher count one.
      if( charI_fromMid < halvingChar_fromMid
          || (charI_fromMid==halvingChar_fromMid
              && charStats.getCount(halvingChar)<charStats.getCount(charI)) )
      {
        halvingChar = charI;
      }
    }

    // Some performance notes measuring average score ('rake brute STEP_SIZE=100'):
    //   If always return halvingChar from here:  9.097926267281107
    //   If always return highestChar from here:  7.523041474654378
    //   If allowed to proceed with current code: 7.372695852534562

    // Floor of log base 2 of the number of candidate words
    //
    // Estimates guesses if candidates could be halved for subsequent
    // guesses (speculative).
    final int wordsLg = 31 - Integer.numberOfLeadingZeros(candidateWords_.size());
    if( wordsLg<=game.numWrongGuessesRemaining() )
    {
      return (char)reductionChar;
    }
    return (char)highestChar;
  }

  public Guess nextGuess(HangmanGame game) {
    updateCandidateWords(game);

    /// Once candidates get low, start guessing by words
    if( candidateWords_.size()==0 ) return null;
    // When there's a small number of words left, the potential for lower
    // score is greater, since guessing a correct word doesn't count for
    // a point while guessing a correct letter does.
    //
    // Some performance notes measuring average score ('rake brute STEP_SIZE=100'):
    //   candidateWords_.size()<2: 7.808755760368664
    //   candidateWords_.size()<3: 7.61671469740634
    //   candidateWords_.size()<4: 7.774193548387097
    // Because of tweaks elsewhere in the algorithm, these measurements
    // are not reproducible exactly.
    if( candidateWords_.size()<3 ) return new GuessWord(candidateWords_.remove(0));

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

    // TODO tmp
    // System.out.println("chosenChar="+chosenChar+" number candidates: "+candidateWords_.size()+" charStats="+charStats.toString());
    // if( candidateWords_.size()<20 ) {
    //   for( String wordI : candidateWords_ ) {
    //     System.out.println("  Candidate: "+wordI);
    //   }
    // }

    return new GuessLetter(chosenChar);

    // TODO: Call to verify we're not duplicating a guess: public Set<Character> getAllGuessedLetters()
    // TODO: Call to verify we're not duplicating a guess: public Set<String> getIncorrectlyGuessedWords()
    // TODO: Account for invalid character
    // TODO: Test a unicode character
    //return new GuessWord(candidateWords_.remove(0)); // TODO
    //return new GuessLetter('.');
  }
}

