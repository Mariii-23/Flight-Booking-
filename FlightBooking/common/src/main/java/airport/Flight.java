package airport;

import exceptions.FullFlightException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

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
    private final Set<Reservation> reservations;

    //private final ReadWriteLock rwReservation ;
    // Get reservations
    private final Lock readLockReservation;

    // Adicionar a reserva depois de ter sido bloqueado.
    private final Lock writeLockReservation;

    public Flight(UUID id, Route route, LocalDate date, Set<Reservation> reservations) {
        this.id = id;
        this.route = route;
        this.date = date;
        this.reservations = new HashSet<>(reservations);
        ReentrantReadWriteLock rwReservation = new ReentrantReadWriteLock();
        this.readLockReservation = rwReservation.readLock();
        this.writeLockReservation = rwReservation.writeLock();

    }
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
        ReentrantReadWriteLock rwReservation = new ReentrantReadWriteLock();
        this.readLockReservation = rwReservation.readLock();
        this.writeLockReservation = rwReservation.writeLock();
    }

    /**
     * Adds a reservation to this flight.
     *
     * @param reservation the id of the reservation.
     * @return true if this set did not already contain the specified element.
     * @throws FullFlightException is launched if aren't seats available.
     */
    public boolean addReservation(Reservation reservation) throws FullFlightException {
        try {
            writeLockReservation.lock();
            if (route.capacity > reservations.size())
                return this.reservations.add(reservation);
            throw new FullFlightException();
        } finally {
            writeLockReservation.unlock();
        }
    }

    /**
     * Removes a reservation to this flight.
     *
     * @param reservation the id of the reservation.
     * @return true if this set contained the specified element.
     */
    public boolean removeReservation(Reservation reservation) {
        try {
            writeLockReservation.lock();
            return this.reservations.remove(reservation);
        } finally {
            writeLockReservation.unlock();
        }
    }

    /**
     * Get all reservation's ids on the flight
     *
     * @return reservation's ids
     */
    public Set<Reservation> getReservations() {
        try {
            readLockReservation.lock();
            return new HashSet<>(reservations);
        } finally {
            readLockReservation.unlock();
        }
    }

    /**
     * Checks if there are available seats.
     *
     * @return true if there is a seat.
     */
    public boolean seatAvailable() {
        try {
            readLockReservation.lock();
            return route.capacity > reservations.size();
        } finally {
            readLockReservation.unlock();
        }
    }

    public void cancelFlight() {
        try {
            writeLockReservation.lock();
            for (Reservation reservation : reservations) {
                reservation.cancelReservation(id);
            }
        } finally {
            writeLockReservation.unlock();
        }
    }

    public void unlock() {
        writeLockReservation.unlock();
    }

    public void lock() {
        writeLockReservation.lock();
    }

    @Override
    public String toString() {
        return "day=" + this.date.toString() +
                " route=" + route.origin +
                " to=" + route.destination +
                " reservation id's=" + this.reservations.stream().map(rev -> rev.id).toList();
    }

    public byte[] serialize() {
        byte[] id = this.id.toString().getBytes(StandardCharsets.UTF_8);
        byte[] route = this.route.serialize();
        String date = this.date.toString();
        ByteBuffer bb = ByteBuffer.allocate(
                Integer.BYTES + id.length + route.length + Integer.BYTES + date.length() +
                        Integer.BYTES + reservations.size() * 36 // UUID size
        );

        // Id
        bb.putInt(id.length);
        bb.put(id);

        // Route
        bb.put(route);

        // LocalDate
        bb.putInt(date.length());
        bb.put(date.getBytes(StandardCharsets.UTF_8));

        bb.putInt(this.reservations.size());
        for (Reservation reservation : this.reservations) {
            UUID reservationId = reservation.id;
            byte[] reservationIdByte = reservationId.toString().getBytes(StandardCharsets.UTF_8);

            bb.put(reservationIdByte);
        }

        return bb.array();
    }

    public static Flight deserialize(ByteBuffer bb) {
        // Id
        byte[] id = new byte[bb.getInt()];
        bb.get(id);

        // Route
        Route route = Route.deserialize(bb);

        // LocalDate
        byte[] dateBuffer = new byte[bb.getInt()];
        bb.get(dateBuffer);
        String date = new String(dateBuffer);

        int size = bb.getInt();
        Set<UUID> reservations = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            byte[] reservationId = new byte[36]; // UUID size
            bb.get(reservationId);

            reservations.add(UUID.fromString(new String(reservationId, StandardCharsets.UTF_8)));
        }

        return new Flight(UUID.fromString(new String(id)), route, LocalDate.parse(date),
                reservations.stream().map(Reservation::new).collect(Collectors.toSet()));
    }

    public static Flight deserialize(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        return deserialize(bb);
    }
}
