package hangman;

public class Play {
  public static final int DEFAULT_NUM_GUESSES = 5;
  /**
   * Runs the inputted HangmanGame and applies the inputted GuessingStrategy to it.
   *
   * @param game
   * @param strategy
   * @return final status of the game
   */
  public static int run(HangmanGame game, GuessingStrategy strategy) {
    // If the GuessingStrategy creates duplicate guesses, the while
    // loop can be unending. Play regards that as a strategy bug.
    while( game.gameStatus()==HangmanGame.Status.KEEP_GUESSING ) {
      strategy.nextGuess(game).makeGuess(game);
      System.out.println(game);
    }
    return game.currentScore();
  }
}
