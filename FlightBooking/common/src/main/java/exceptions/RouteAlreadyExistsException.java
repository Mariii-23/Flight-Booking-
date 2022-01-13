package exceptions;

/**
 * Thrown to indicate that the route between the two given airports is already registered.
 * We compare the names with case insensitivity.
 */
public class RouteAlreadyExistsException extends Exception {

    public RouteAlreadyExistsException() {
    }

    public RouteAlreadyExistsException(String message) {
        super(message);
    }

    public RouteAlreadyExistsException(String orig, String dest) {
        super("Route already exists:  " + orig + " [origin] -> "
                + dest + "[destination]");
    }
}
