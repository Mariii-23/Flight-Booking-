package exceptions;

import java.util.UUID;

/**
 * Thrown to indicate that the given code to cancel a reservation isn't in the system.
 */
public class ReservationNotFoundException extends Exception {

    public ReservationNotFoundException() {
    }

    public ReservationNotFoundException(String message) {
        super(message);
    }

    public ReservationNotFoundException(UUID reservation) {
        super("Reservation " + reservation + " [id] not found");
    }
}
