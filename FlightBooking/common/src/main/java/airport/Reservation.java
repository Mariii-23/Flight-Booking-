package airport;

import users.User;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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
    public final User client;

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


    public Reservation(UUID id, User client, Set<Flight> flights) {
        this.id = id;
        this.client = client;
        this.flights = flights;
        this.lockFlights = new ReentrantLock();
    }

    public Reservation(UUID uuid) {
        this.id = uuid;
        this.client = null;
        this.flights = null;
        this.lockFlights = null;
    }

    public Reservation(UUID id, String username, Set<Flight> flights) {
        this.id = id;
        this.client = new User(username);
        this.flights = new HashSet<>(flights);
        this.lockFlights = new ReentrantLock();
    }

    public static Reservation deserialize(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        byte[] idB = new byte[bb.getInt()];
        bb.get(idB);
        var id = UUID.fromString(new String(idB, StandardCharsets.UTF_8));

        byte[] usernameB = new byte[bb.getInt()];
        bb.get(usernameB);
        String username = new String(usernameB, StandardCharsets.UTF_8);

        int size = bb.getInt();
        Set<Flight> flights = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            Flight flight = Flight.deserialize(bb);
            flights.add(flight);
        }
        return new Reservation(id, username, flights);
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
     * @param id ID
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

    public User getClient() {
        return client;
    }

    public String getUsernameClient() {
        return client.getUsername();
    }

    public Set<Flight> getFlights() {
        return new HashSet<>(flights);
    }

    public byte[] serialize() {
        try {
            lockFlights.lock();
            var uuid = id.toString().getBytes(StandardCharsets.UTF_8);
            byte[] user = client.getUsername().getBytes(StandardCharsets.UTF_8);
            var flights = this.flights.stream().map(Flight::serialize).collect(Collectors.toSet());
            ByteBuffer bb = ByteBuffer.allocate(
                    Integer.BYTES + uuid.length +
                            Integer.BYTES + user.length +
                            Integer.BYTES + flights.stream().mapToInt(arr -> arr.length).sum()
            );

            bb.putInt(uuid.length);
            bb.put(uuid);

            bb.putInt(user.length);
            bb.put(user);

            bb.putInt(flights.size());
            for (byte[] flight : flights) {
                bb.put(flight); // Flight
            }

            return bb.array();
        } finally {
            lockFlights.unlock();
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("id=").append(this.id).append(" client=").append(client).append(" flights=");

        for (Flight flight : flights) res.append(flight);

        return res.toString();
    }
}