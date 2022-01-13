package users;

import java.time.LocalDateTime;

public class Notification {
    private final LocalDateTime date;
    private final String message;

    public Notification(String message) {
        this.message = message;
        this.date = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return date.equals(that.date) &&
                message.equals(that.message);
    }
}
