package users;

import java.util.UUID;

/**
 * User class.
 */
public abstract class User {

    /**
     * The user's id.
     */
    final UUID id;

    /**
     * Username.
     */
    final String username;

    /**
     * Password.
     */
    final String password;

    /**
     * Constructor
     * @param username the username.
     * @param password the password.
     */
    public User(String username, String password) {
        this.password = password;
        this.username = username;
        this.id = UUID.randomUUID();
    }

    //abstract void login();
}
