package exceptions;

/**
 * Thrown to indicate that the username given in authentication isn't valid.
 */
public class UserNotFoundException extends Exception {

    public UserNotFoundException() {
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
