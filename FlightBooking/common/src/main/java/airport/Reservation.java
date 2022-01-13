package airport;

import users.User;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private final Set<UUID> flightIds;

    /**
     * Constructor
     *
     * @param client     Client.
     * @param flightsIds a set of flight's id.
     */
    public Reservation(User client, Set<UUID> flightsIds) {
        this.id = UUID.randomUUID();
        this.client = client;
        this.flightIds = flightsIds;
    }

    /**
     * Return all flight ids from this reservation.
     *
     * @return Flight ids
     */
    public Set<UUID> getFlightIds() {
        return new HashSet<>(flightIds);
    }

    /**
     * Checks if the given user made the reservation
     *
     * @param user User
     * @return true if are the same user
     */
    public boolean checksUser(User user) {
        return client.equals(user);
    }
}
