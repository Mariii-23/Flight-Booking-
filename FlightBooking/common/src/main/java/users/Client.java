package users;

/**
 * Client class.
 */
public class Client extends User {
    /**
     * Constructor
     *
     * @param username the username.
     * @param password the password.
     */
    public Client(String username, String password) {
        super(username, password);
    }

    @Override
    public boolean equals(Object o) {
        try {
            super.lock.lock();
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return super.equals(o);
        } finally {
            super.lock.unlock();
        }
    }
}
