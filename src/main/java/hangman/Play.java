package hangman;

public class Play {
  public static final int DEFAULT_NUM_GUESSES = 5;
  /**
   * Runs the inputted HangmanGame and applies the inputted GuessingStrategy to it.
   *
   * @param game
   * @param strategy
   * @param displayGame whether to display the game's progress to stdout
   * @return final status of the game
   */
  public static int run(HangmanGame game, GuessingStrategy strategy, boolean displayGame) throws NullGuessException
  {
    // If the GuessingStrategy creates duplicate guesses, the while
    // loop can be unending. Play regards that as a strategy bug.
    while( game.gameStatus()==HangmanGame.Status.KEEP_GUESSING ) {
      final Guess guess = strategy.nextGuess(game);
      if( guess==null ) throw new NullGuessException("Strategy made a null guess. ");
      guess.makeGuess(game);
      if( displayGame ) System.out.println(game);
    }
    return game.currentScore();
  }
  public static int run(HangmanGame game, GuessingStrategy strategy) throws NullGuessException {
    return run(game, strategy, true);
  }
}
