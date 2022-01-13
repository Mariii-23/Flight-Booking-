package exceptions;

import java.util.UUID;

/**
 * Thrown to indicate that the given password is incorrect, but the user exists.
 */
public class ReservationDoesNotBelongToTheClientException extends Exception {

    public ReservationDoesNotBelongToTheClientException() {
    }

    public ReservationDoesNotBelongToTheClientException(String message) {
        super(message);
    }

    public ReservationDoesNotBelongToTheClientException(UUID reservation, String client) {
        super("Reservation " + reservation + " [id] doesn't belong to the client " + client + " [username]");
    }
}
