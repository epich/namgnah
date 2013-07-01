package hangman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the strategy for guessing letters in the
 * HangmanGame.
 *
 * The strategy narrows down candidate words based on information from
 * the game and a previous list of candidate words. The strategy for
 * choosing a char is interesting and documented with the chooseChar
 * method.
 *
 * The strategy earns an average HangmanGame score of
 * 7.222350230414746 as measured by 'rake brute STEP_SIZE=100'. Note
 * however, an alternative implementation was tried which earned an
 * average score of 7.194700460829493 but ran 70% slower and made the
 * code slightly more complex. I did one run of 'rake brute
 * STEP_SIZE=1' and earned an average score of 7.454730909530972 in
 * 220m29.628s runtime.
 *
 * That alternate implementation had CharStats determine the word
 * count for each char (as it does now) and the char's overall count
 * in the remaining candidate words. The only way the strategy used
 * the overall count was as the last resort tie breaker in
 * chooseChar's determination of highestChar and reductionChar (see
 * chooseChar documentation). The tie was broken in favor of the char
 * with highest overall count, because the potential to reveal more
 * information for subsequent candidate word elimination is a little
 * greater. If we decided we liked the tradeoff, this could be
 * reimplemented.
 *
 * This class assumes HangmanGame returns Character objects as upper
 * case.
 */
public class StrategyImpl implements GuessingStrategy {
  private List<String> candidateWords_;
  private static final int A_ASCII_CODE = (int)'A';
  private static final int Z_ASCII_CODE = (int)'Z';
  private static final int NUM_LETTERS = Z_ASCII_CODE-A_ASCII_CODE+1;

  /**
   * Statistics on each valid character of the HangmanGame.
   *
   * The only relevant stat is counting the candidate words a char
   * appears in.
   */
  private class CharStats {
    // Contains counts of the number of candidate words that contain
    // the character. eg charWordCounts_[(int)'A'] would return the count
    // of words containing at least one letter A. The strategy
    // assumes all words are equally likely, so we do not count a
    // letter more than once for a given candidate word.
    //
    // By convention, use upper case letters only, just as the
    // HangmanGame does.
    //
    // A value of -1 removes the char from consideration, typically
    // because it is already guessed.
    private int[] charWordCounts_ = new int[NUM_LETTERS];

    private int code2Index(int charCode) { return charCode-A_ASCII_CODE; }

    /**
     * Initialize char statistics based on the info from the game.
     *
     * Those already guessed are set to -1, others to 0.
     */
    public CharStats(Set<Character> correctChars,
                     Set<Character> incorrectChars)
    {
      // Since the inputed chars come from outside StrategyImpl, verify
      // they're valid values.
      for( Character charI : correctChars ) {
        if( charI<A_ASCII_CODE || Z_ASCII_CODE<charI ) {
          throw new IllegalArgumentException("Expected char in [A-Z] but got "+charI+" instead.");
        }
        charWordCounts_[code2Index(charI)] = -1;
      }
      for( Character charI : incorrectChars ) {
        if( charI<A_ASCII_CODE || Z_ASCII_CODE<charI ) {
          throw new IllegalArgumentException("Expected char in [A-Z] but got "+charI+" instead.");
        }
        charWordCounts_[code2Index(charI)] = -1;
      }
    }

    /** Convert char statistics to string, for debugging. */
    public String toString() {
      StringBuilder statsB = new StringBuilder();
      for( int charI = A_ASCII_CODE; charI<=Z_ASCII_CODE; ++charI ) {
        statsB.append((char)charI);
        statsB.append(":");
        statsB.append(charWordCounts_[code2Index(charI)]);
        statsB.append(" ");
      }
      return statsB.toString();
    }

    public int getWordCount(int charCode) { return charWordCounts_[code2Index(charCode)]; }
    public void incrementWordCount(int charCode) { charWordCounts_[code2Index(charCode)] += 1; }
  }

  /**
   * Create strategy, filtering the dictionary InputStream for words
   * of the specified length.
   *
   * @param wordLen length of the word in play for the HangmanGame
   * @param dictionary InputStream of words constituting the dictionary
   * @throws IOException
   */
  public StrategyImpl(int wordLen, InputStream dictionary) throws IOException {
    candidateWords_ = new LinkedList<String>();
    final BufferedReader dictReader = new BufferedReader(new InputStreamReader(dictionary));
    String line;
    while( (line = dictReader.readLine())!=null ) {
      if( line.length()==wordLen ) candidateWords_.add(line.toUpperCase());
    }
  }

  /**
   * Create a new candidateWords_ with a subset of elements of the
   * former candidateWords_, based on updated information from the
   * game.
   */
  private void updateCandidateWords(HangmanGame game) {
    final List<String> newCandidates = new LinkedList<String>();
    final String gameWord = game.getGuessedSoFar();
    final Set<Character> incorrectChars = game.getIncorrectlyGuessedLetters();
    for( String wordI : candidateWords_ ) {
      assert wordI.length()==gameWord.length();
      boolean wordMatches = true; // Until proven otherwise
      // Iterate over each char of the wordI and gameWord
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
      if(wordMatches) newCandidates.add(wordI);
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
   *   - Few: the risk of running out of guesses overrules the desire
   *          to save a point or two from the final score.
   *   - Many: running out of guesses is not a concern, so focus on
   *           using each score point to reduce candidate words as
   *           much as possible.
   *
   * If for example a char appears in 90% of words, how much can it
   * reduce the candidate words? At least 10% is assured, likely more
   * because (if correct), the positions of the revealed chars convey
   * further information to allow more eliminations. Still, a
   * reduction of 50% is not guaranteed.
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
   * which computes average scores on 1% of the dictionary. (The Rakefile
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
    // See method documentation for details about this variable.
    //
    // Selected score measurements (rake brute STEP_SIZE=100):
    //   reductionProportion = 0.61: 7.23963133640553
    //   reductionProportion = 0.62: 7.222350230414746
    //   reductionProportion = 0.63: 7.235023041474655
    final double reductionProportion = 0.62;
    int reductionChar = Z_ASCII_CODE;
    int highestChar = Z_ASCII_CODE;
    // Measured better average scores when breaking ties in favor of
    // chars closer to Z than A.
    for( int charI = Z_ASCII_CODE; A_ASCII_CODE<=charI; --charI ) {
      if( charStats.getWordCount(highestChar) < charStats.getWordCount(charI) )
      {
        highestChar = charI;
      }

      final double reductionPoint = reductionProportion*(double)candidateWords_.size();
      final double charI_fromReductionPoint = Math.abs(reductionPoint-charStats.getWordCount(charI));
      final double reductionChar_fromReductionPoint = Math.abs(reductionPoint-charStats.getWordCount(reductionChar));
      // Choose the one closest to reductionPoint
      if( charI_fromReductionPoint < reductionChar_fromReductionPoint
          // Break a tie when the two are on opposite sides of reductionPoint.
          // Choose the one with the higher word count.
          || (charI_fromReductionPoint==reductionChar_fromReductionPoint
              && charStats.getWordCount(reductionChar)<charStats.getWordCount(charI)) )
      {
        reductionChar = charI;
      }
    }

    // To illustrate the usefulness of having two approaches, the
    // following average score measurements were taken:
    //   If always return reductionChar here:   7.8963133640553
    //   If always return highestChar here:     7.3963133640553
    //   If proceeds according to current code: 7.222350230414746

    /// Determine whether to use reductionChar or highestChar
    //
    // Floor of log base 2 of the number of candidate words estimates
    // guesses left if candidates could be halved for all subsequent
    // guesses.
    final int wordsLg = 31 - Integer.numberOfLeadingZeros(candidateWords_.size());
    if( wordsLg<=game.numWrongGuessesRemaining() )
    {
      return (char)reductionChar;
    }
    return (char)highestChar;
  }

  /**
   * Determine the next guess of this strategy implementation of the
   * HangmanGame.
   *
   * @param game
   */
  public Guess nextGuess(HangmanGame game) {
    updateCandidateWords(game);

    /// Once candidates get low, start guessing by words
    if( candidateWords_.size()==0 ) return null;
    // When there's a small number of words left, the potential for
    // lower score is greater, since guessing a correct word doesn't
    // count for a point but guessing a correct letter does.
    //
    // It's clear that guessing a word when there are 1 or 2 left is
    // best, but 3 was less certain, so I measured it. I used 'rake
    // brute STEP_SIZE=100'.
    //   candidateWords_.size()<2: 7.808755760368664
    //   candidateWords_.size()<3: 7.61671469740634
    //   candidateWords_.size()<4: 7.774193548387097
    // NB: Because of tweaks elsewhere in the strategy, these
    // measurements are not reproducible exactly.
    if( candidateWords_.size()<3 ) return new GuessWord(candidateWords_.remove(0));

    /// Calculate letter frequencies
    final CharStats charStats = new CharStats(game.getCorrectlyGuessedLetters(),
                                              game.getIncorrectlyGuessedLetters());
    for( int charI = A_ASCII_CODE; charI<=Z_ASCII_CODE; ++charI ) {
      if( charStats.getWordCount(charI)<0 ) continue;
      for( String wordI : candidateWords_ ) {
        if( -1!=wordI.indexOf(charI) )
        {
          charStats.incrementWordCount(charI);
        }
      }
    }
    final char chosenChar = chooseChar(game, charStats);

    // Useful for debugging, so keeping it around
    // System.out.println("chosenChar="+chosenChar
    //                    +" number candidates: "+candidateWords_.size()
    //                    +" wrongs left: "+game.numWrongGuessesRemaining()
    //                    +" charStats="+charStats.toString());

    return new GuessLetter(chosenChar);
  }
}

