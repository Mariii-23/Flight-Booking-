import airport.Flight;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IAirportSystem {

    /**
     * Adds a new route into the system.
     *
     * @param origin   the origin city.
     * @param destiny  the destiny city.
     * @param capacity the route capacity.
     */
    void addRoute(String origin, String destiny, int capacity);

    /**
     * Cancels a day. Preventing new reservations and canceling the remaining ones from that day.
     *
     * @param day the day.
     */
    void cancelDay(LocalDate day);

    /**
     * Reserves a flight given the connections, in the time interval.
     *
     * @param userId the user's id.
     * @param cities the connections.
     * @param start  the start date of the interval.
     * @param end    the end date of the interval.
     * @return       the reservation's id.
     */
    UUID reserveFlight(UUID userId, List<String> cities, LocalDate start, LocalDate end);

    /**
     * Cancels a flight.
     *
     * @param userId        the user's id.
     * @param reservationId the reservation's id.
     */
    void cancelFlight(UUID userId, UUID reservationId);

    /**
     * Gets the existent flights.
     *
     * @return the list of the existent flights.
     */
    List<Flight> getFlights();
}
