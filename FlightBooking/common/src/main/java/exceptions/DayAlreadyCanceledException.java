package exceptions;

import java.time.LocalDate;

public class DayAlreadyCanceledException extends Exception {

    public DayAlreadyCanceledException() {
    }

    public DayAlreadyCanceledException(String message) {
        super(message);
    }

    public DayAlreadyCanceledException(LocalDate date) {
        super("Day already canceled: " + date);
    }
}
