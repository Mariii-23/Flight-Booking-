package exceptions;

public class NotLoggedInException extends Exception {

    public NotLoggedInException() {
        super("You have to logged in first!");
    }
}
