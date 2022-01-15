package users;

import encryption.BCrypt;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
    private final Set<UUID> reservations;
    /**
     * Password.
     */
    private String password;

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
    }

    public User(String username) {
        this.username = username;
        this.password = null;
        this.reservations = null;
        this.lock = new ReentrantLock();
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
    public void changerUserPassword(String newPassword) {
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
