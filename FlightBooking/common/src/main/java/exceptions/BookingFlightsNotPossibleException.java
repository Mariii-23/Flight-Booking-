package exceptions;

/**
 * Thrown to indicate that the booking of flight isn't possible.
 * This can happen because the day is cancel, or because the possible flights are full.
 */
public class BookingFlightsNotPossibleException extends Exception {

    public BookingFlightsNotPossibleException() {
        super("Booking Flights not possible");
    }

    public BookingFlightsNotPossibleException(String message) {
        super(message);
    }
}
