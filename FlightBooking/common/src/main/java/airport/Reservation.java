package airport;

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
     * Id of the client that owns this reservation.
     */
    public final UUID clientId;

    /**
     * Flights with the connections of the reservation.
     * E.g. Lisbon -> Tokyo -> London
     */
    public final Set<UUID> flightIds;
    // TODO: Adicionar capacidade aos flights depois de apagar a reservation

    /**
     * Constructor
     *
     * @param clientId          the id of the Client.
     * @param flightsIds        a set of flight's id.
     */
    public Reservation(UUID clientId, Set<UUID> flightsIds) {
        this.id = UUID.randomUUID();
        this.clientId = clientId;
        this.flightIds = flightsIds;
    }
}
