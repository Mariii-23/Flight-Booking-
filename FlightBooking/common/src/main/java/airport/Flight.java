package airport;

import exceptions.FullFlightException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a flight.
 */
public class Flight {

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

    private int currentOccupation;

    /**
     * Constructor of the flight.
     *
     * @param route the route.
     * @param date  the date.
     */
    public Flight(Route route, LocalDate date) {
        this.currentOccupation = 0;
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
     * @throws FullFlightException is launched if aren't seats available.
     */
    public boolean addReservation(UUID reservationId) throws FullFlightException {
        if (route.capacity > reservations.size())
            return this.reservations.add(reservationId);
        throw new FullFlightException();
    }

    /**
     * Removes a reservation to this flight.
     *
     * @param reservationId the id of the reservation.
     * @return true if this set contained the specified element.
     */
    public boolean removeReservation(UUID reservationId) {
        cancelSeat();
        return this.reservations.remove(reservationId);
    }

    /**
     * Get all reservation's ids on the flight
     *
     * @return reservation's ids
     */
    public Set<UUID> getReservations() {
        return new HashSet<>(reservations);
    }

    /**
     * Checks if there are available seats.
     *
     * @return true if there is a seat.
     */
    public boolean seatAvailable() {
        return route.capacity > currentOccupation;
    }

    public void preReservationSeat() throws FullFlightException {
        if (seatAvailable())
            currentOccupation++;
        else
            throw new FullFlightException();
    }

    public void cancelSeat() {
        if (currentOccupation > 0)
            currentOccupation--;
    }
}
