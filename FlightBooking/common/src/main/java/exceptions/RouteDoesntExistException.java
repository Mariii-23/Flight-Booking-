package exceptions;

/**
 * Thrown to indicate that there is no connection between the two given airports.
 */
public class RouteDoesntExistException extends Exception {

    public RouteDoesntExistException() {
    }

    public RouteDoesntExistException(String message) {
        super(message);
    }

    public RouteDoesntExistException(String orig, String dest) {
        super("Route doesn't exist:  " + orig + " [origin] -> "
                + dest + " [destination]");
    }
}
