package hangman;

public class NullGuessException extends Exception {
  public NullGuessException() { super(); }
  public NullGuessException(String message) { super(message); }
  public NullGuessException(String message, Throwable cause) { super(message, cause); }
  public NullGuessException(Throwable cause) { super(cause); }
}
