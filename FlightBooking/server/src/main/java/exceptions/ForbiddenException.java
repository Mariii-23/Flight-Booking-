package exceptions;

import users.User;

public class ForbiddenException extends Exception {
    public ForbiddenException(User account) {
        super("Forbidden operation with user type: " + account.getClass());
    }
}
