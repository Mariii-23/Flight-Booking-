package exceptions;

import java.time.LocalDate;

public class InvalidDateException extends Exception {
    public InvalidDateException(LocalDate start, LocalDate end) {
        super("Start day: " + start + " or end day: " + end + " are invalid!");
    }
}
