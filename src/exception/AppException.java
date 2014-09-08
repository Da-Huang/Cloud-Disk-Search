package exception;

/**
 * App Exception.
 * @author dhuang
 *
 */
public class AppException extends Exception {
  private static final long serialVersionUID = 7696261877362832852L;
  public AppException(String message) {
    super(message);
  }
}
