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
        return notifications.isEmpty();
    }

    public Notification removeNotification() {
        return notifications.remove();
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return super.equals(o) && notifications.equals(client.notifications);
    }
}
