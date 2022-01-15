package exceptions;

import users.User;

public class AlreadyLoggedInException extends Exception {
    public AlreadyLoggedInException(User account) {
        super("User with username \"" + account.getUsername() + "\" is already logged in!");
    }

    public AlreadyLoggedInException() {
        super("You are already logged in!");
    }
}
