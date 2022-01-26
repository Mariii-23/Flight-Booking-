package users;

import airport.Flight;
import airport.Reservation;
import encryption.BCrypt;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


/**
 * User class.
 */
public class User {

    protected final ReentrantLock lock;
    /**
     * Username.
     */
    private final String username;
    /**
     * Set of the reservations of the client
     */
    private Set<UUID> reservations;
    /**
     * Password.
     */
    private String password;

    /**
     * Set of the current notifications of the client.
     */
    private Queue<Notification> notifications;


    /**
     * Constructor
     *
     * @param username the username.
     * @param password the password.
     */
    public User(String username, String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        this.username = username;
        this.reservations = new HashSet<>();
        this.lock = new ReentrantLock();
        this.notifications = new ArrayDeque<>();
    }

    public User(String username) {
        this.username = username;
        this.password = null;
        this.reservations = null;
        this.lock = new ReentrantLock();
        this.notifications = new ArrayDeque<>();
    }

    /**
     * Checks if the password given is valid to this user.
     *
     * @param password Password to check.
     * @return Is password is correct.
     */
    public boolean validPassword(String password) {
        try {
            lock.lock();
            return BCrypt.checkpw(password, this.password);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the username of this user.
     *
     * @return Username of this object.
     */
    public String getUsername() {
        try {
            lock.lock();
            return username;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Change password of a user.
     *
     * @param newPassword new password.
     */
    public void changePassword(String newPassword) {
        try {
            lock.lock();
            this.password = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        } finally {
            lock.unlock();
        }
    }

    public void addReservation(UUID reservation) {
        try {
            lock.lock();
            this.reservations.add(reservation);
        } finally {
            lock.unlock();
        }
    }

    public void removeReservation(UUID reservation) {
        try {
            lock.lock();
            this.reservations.remove(reservation);
        } finally {
            lock.unlock();
        }
    }

    public boolean containsReservation(UUID reservation) {
        try {
            lock.lock();
            return this.reservations.contains(reservation);
        } finally {
            lock.unlock();
        }
    }

    public Set<UUID> getReservations() {
        try {
            lock.lock();
            return new HashSet<>(reservations);
        } finally {
            lock.unlock();
        }
    }


    public boolean emptyNotifications() {
        try {
            lock.lock();
            return notifications.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public Notification removeNotification() {
        try {
            lock.lock();
            return notifications.remove();
        } finally {
            lock.unlock();
        }
    }

    public Queue<Notification> removeAllNotification() {
        try {
            lock.lock();
            Queue<Notification> remove = notifications;
            notifications = new ArrayDeque<>();
            return remove;
        } finally {
            lock.unlock();
        }
    }

    public void addNotification(Notification notification) {
        try {
            lock.lock();
            notifications.add(notification);
        } finally {
            lock.unlock();
        }
    }

    public void addCancelReservationNotifications(Reservation reservation) {
        StringBuilder msg = new StringBuilder("[Reservation Cancelled] ");
        msg.append(reservation.id).append(" [FLIGHTS]: ");
        Set<Flight> flights = reservation.getFlights();
        for (Flight flight : flights) {
            msg.append(flight.route.origin);
            msg.append(" ; ");
        }
        Notification notification = new Notification(msg.toString());
        addNotification(notification);
    }

    @Override
    public boolean equals(Object o) {
        try {
            lock.lock();
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return username.equals(user.username) &&
                    password.equals(user.password);
        } finally {
            lock.unlock();
        }
    }
}
