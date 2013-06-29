package hangman;

public class Play {
  // runs your strategy for the given game, then returns the score
  public static int run(HangmanGame game, GuessingStrategy strategy) {
    long startMs = System.currentTimeMillis();
    while( game.gameStatus()==HangmanGame.Status.KEEP_GUESSING ) {
      strategy.nextGuess(game).makeGuess(game);
      System.out.println(game);
    }
    System.out.println("Finished in "+(System.currentTimeMillis()-startMs)+"ms");
    return game.currentScore();
  }
}
