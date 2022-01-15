package airport;

import users.User;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents the reservation of one or multiple flights.
 */
public class Reservation {

    /**
     * Reservation code or id of this reservation.
     */
    public final UUID id;

    /**
     * Client that owns this reservation.
     */
    private final User client;

    /**
     * Flights with the connections of the reservation.
     * E.g. Lisbon -> Tokyo -> London
     */
    private final Set<Flight> flights;

    private final Lock lockFlights;


    /**
     * Constructor
     *
     * @param client     Client.
     * @param flightsIds a set of flight's id.
     */

    public Reservation(User client, Set<Flight> flightsIds) {
        this.id = UUID.randomUUID();
        this.client = client;
        this.flights = flightsIds;
        this.lockFlights = new ReentrantLock();
    }


    /**
     * Cancel the reservation on all flights involved in the given reservation
     */
    public void cancelReservation() {
        try {
            lockFlights.lock();
            for (Flight flight : flights) {
                if (flight != null)
                    flight.removeReservation(this);
            }
        } finally {
            lockFlights.unlock();
        }
    }

    /**
     * Used in cancelDay, to don't remove the same flight two times.
     * Only remove the other flights from a reservation that is cancelled.
     *
     * @param id
     */
    public void cancelReservation(UUID id) {
        try {
            lockFlights.lock();
            for (Flight flight : flights) {
                if (flight != null && flight.id != id)
                    flight.removeReservation(this);
            }
        } finally {
            lockFlights.unlock();
        }
    }

    /**
     * Checks if the given user made the reservation
     *
     * @return true if are the same user
     */
    public boolean checksUser(String username) {
        return client.getUsername().equals(username);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("Reservation{" +
                "client=" + client +
                ", flights=");
        for (Flight one : flights)
            res.append(flights.toString());
        return res.toString();
    }
}