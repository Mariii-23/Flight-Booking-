package users;

import java.util.ArrayDeque;
import java.util.HashSet;
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
     * @param username the username.
     * @param password the password.
     */
    public Client(String username, String password) {
        super(username, password);
        this.notifications = new ArrayDeque<>();
    }
}
