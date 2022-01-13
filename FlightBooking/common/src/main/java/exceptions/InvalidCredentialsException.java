package exceptions;

/**
 * Thrown to indicate that the given password is incorrect, but the user exists.
 */
public class InvalidCredentialsException extends Exception {

    public InvalidCredentialsException() {
        super("Invalid Credentials");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
