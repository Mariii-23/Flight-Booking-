package users;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Client class.
 */
public class Client extends User {

    /**
     * Set of the current notifications of the client.
     */
    private final Queue<Notification> notifications;

    /**
     * Constructor
     *
     * @param username the username.
     * @param password the password.
     */
    public Client(String username, String password) {
        super(username, password);
        this.notifications = new ArrayDeque<>();
    }

    public boolean emptyNotifications() {
        try {
            super.lock.lock();
            return notifications.isEmpty();
        } finally {
            super.lock.unlock();
        }
    }

    public Notification removeNotification() {
        try {
            super.lock.lock();
            return notifications.remove();
        } finally {
            super.lock.unlock();
        }
    }

    public void addNotification(Notification notification) {
        try {
            super.lock.lock();
            notifications.add(notification);
        } finally {
            super.lock.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        try {
            super.lock.lock();
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Client client = (Client) o;
            return super.equals(o) && notifications.equals(client.notifications);
        } finally {
            super.lock.unlock();
        }
    }
}
