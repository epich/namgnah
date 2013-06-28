package hangman;

public class Play {
  // runs your strategy for the given game, then returns the score
  public static int run(HangmanGame game, GuessingStrategy strategy) {
    // TODO: Number of iterations shouldn't exceed game.getSecretWordLength()+game.getMaxWrongGuesses()
    while( game.gameStatus()==HangmanGame.Status.KEEP_GUESSING ) {
      strategy.nextGuess(game).makeGuess(game);
      System.out.println(game);
    }
    return game.currentScore();
  }
}
