package exceptions;

/**
 * Thrown to indicate that the username is already in the system.
 */
public class UsernameAlreadyExistsException extends Exception {

    public UsernameAlreadyExistsException() {
    }

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
