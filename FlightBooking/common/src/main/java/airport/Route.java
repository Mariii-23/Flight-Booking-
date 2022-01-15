package airport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * This class stores the information about routes between cities.
 * The flights happen only if a route exists.
 * If a route exists, but the flight does not, we create the flight.
 */
public class Route {

    private static final Logger logger = LogManager.getLogger(Route.class);

    /**
     * City of departure of the flight.
     */
    public final String origin;
    /**
     * City of arrival of the flight.
     */
    public final String destination;
    /**
     * Capacity of the airplane that does the connection.
     * The route as a fixed capacity.
     */
    public final int capacity;

    /**
     * Constructor
     *
     * @param origin      the origin of the flight.
     * @param destination the destination of the flight.
     * @param capacity    the capacity of each flight.
     */
    public Route(String origin, String destination, int capacity) {
        this.origin = origin;
        this.destination = destination;
        this.capacity = capacity;
    }

    public static Route deserialize(ByteBuffer bb) {
        byte[] origin = new byte[bb.getInt()];
        bb.get(origin);
        String originName = new String(origin, StandardCharsets.UTF_8);

        byte[] destination = new byte[bb.getInt()];
        bb.get(destination);
        String destinationName = new String(destination, StandardCharsets.UTF_8);

        int capacity = bb.getInt();

        Route route= new Route(originName, destinationName, capacity);
        logger.info("Deserialize route: " + route);
        return route;
    }

    public static Route deserialize(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        return deserialize(bb);
    }

    public byte[] serialize() {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + origin.length() +
                Integer.BYTES + destination.length() + Integer.BYTES);

        bb.putInt(origin.length());
        bb.put(origin.getBytes(StandardCharsets.UTF_8));

        bb.putInt(destination.length());
        bb.put(destination.getBytes(StandardCharsets.UTF_8));

        bb.putInt(capacity);

        return bb.array();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return capacity == route.capacity && origin.equals(route.origin) && destination.equals(route.destination);
    }

    @Override
    public String toString() {
        return origin +
                " -> " + destination +
                " [capacity = " + capacity + "]";
    }
}
