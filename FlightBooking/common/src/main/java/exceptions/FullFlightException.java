package exceptions;

import java.util.UUID;

/**
 * Thrown to indicate that the flight has all seats occupy, so it's impossible to register more clients.
 */
public class FullFlightException extends Exception {

    public FullFlightException() {
    }

    public FullFlightException(String message) {
        super(message);
    }

    public FullFlightException(UUID id) {
        super("Full flight: " + id + " [id]");
    }
}
