package airport;

/**
 * This class stores the information about routes between cities.
 * The flights happen only if a route exists.
 * If a route exists, but the flight does not, we create the flight.
 */
public class Route {

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

    @Override
    public String toString() {
        return "Route{" +
                "origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", capacity=" + capacity +
                '}';
    }
}
