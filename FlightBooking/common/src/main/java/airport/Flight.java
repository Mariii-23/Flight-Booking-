package airport;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a flight.
 */
public class Flight {
    // TODO: Lock;

    /**
     * Id of the flight.
     */
    public final UUID id;

    /**
     * Route of the flight.
     */
    public final Route route;

    /**
     * Date of the flight.
     */
    public final LocalDate date;

    /**
     * Association between each reservation code to the flight.
     * <p>
     * This is necessary because when a flight is canceled, we need
     * to know all reservations associated to cancel then.
     * Like, if a connection flight is canceled, we need to cancel the flights associated to that connection
     */
    private final Set<UUID> reservations;

    /**
     * Constructor of the flight.
     *
     * @param route the route.
     * @param date  the date.
     */
    public Flight(Route route, LocalDate date) {
        this.id = UUID.randomUUID();
        this.route = route;
        this.date = date;
        this.reservations = new HashSet<>();
    }

    /**
     * Adds a reservation to this flight.
     *
     * @param reservationId the id of the reservation.
     * @return true if this set did not already contain the specified element.
     */
    public boolean addReservation(UUID reservationId) {
        return this.reservations.add(reservationId);
    }

    /**
     * Removes a reservation to this flight.
     *
     * @param reservationId the id of the reservation.
     * @return true if this set contained the specified element.
     */
    public boolean removeReservation(UUID reservationId) {
        return this.reservations.remove(reservationId);
    }
}
