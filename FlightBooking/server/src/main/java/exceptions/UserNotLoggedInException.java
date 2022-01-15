package exceptions;

public class UserNotLoggedInException extends Exception {
    public UserNotLoggedInException() {
        super("User needs to be login to perform this operation!");
    }
}
