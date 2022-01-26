package users;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {
    private final LocalDateTime date;
    private final String message;

    public Notification(String message) {
        this.date = LocalDateTime.now();
        this.message = message;
    }

    public Notification(LocalDateTime date, String message) {
        this.date = date;
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return date.equals(that.date) &&
                message.equals(that.message);
    }

    public byte[] serialize() {
        var dateBytes = date.toString().getBytes(StandardCharsets.UTF_8);
        var messageBytes = message.getBytes(StandardCharsets.UTF_8);

        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES * 2 + dateBytes.length + messageBytes.length);

        bb.putInt(dateBytes.length);
        bb.put(dateBytes);

        bb.putInt(messageBytes.length);
        bb.put(messageBytes);

        return bb.array();
    }

    public static Notification deserialize(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        var dataBytes = new byte[bb.getInt()];
        bb.get(dataBytes);
        var data = LocalDateTime.parse(new String(dataBytes, StandardCharsets.UTF_8));

        var messageBytes = new byte[bb.getInt()];
        bb.get(messageBytes);
        var message = new String(messageBytes, StandardCharsets.UTF_8);

        return new Notification(data, message);
    }

    @Override
    public String toString() {
        return "[" + date.format(DateTimeFormatter.ISO_DATE) + "] -> " + message;
    }
}
