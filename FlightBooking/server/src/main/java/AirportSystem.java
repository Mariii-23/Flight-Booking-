import airport.Flight;
import airport.Reservation;
import airport.Route;

import java.time.LocalDate;
import java.util.*;

public class AirportSystem {

    /**
     * Associates each city, by name, with the flights that leave that city.
     */
    private final Map<String, Set<Route>> connectionsByCityOrig;

    /**
     * Associates each ID to the respective flight
     */
    private final Map<UUID, Flight> flightsById;

    /**
     * Associates each day to the flies that happen in that day.
     * If a connection exists, but the fly in that day doesn't, then the flight will be created.
     * We can only have one fligth by connection in each day.
     */
    private final Map<LocalDate, Set<Flight>> flightsByDate;

    /**
     * Days cancelled by the adminstrator.
     * This is used to avoid reservations in cancelled days.
     */
    private final Set<LocalDate> canceledDays;

    /**
     * Associates each reservation to his id.
     */
    private final Map<UUID, Reservation> reservationsById;

    /**
     * Constructor.
     * It starts with empty parameters because they are all inserted by the users.
     */
    public AirportSystem() {
        this.connectionsByCityOrig = new HashMap<>();
        this.flightsById = new HashMap<>();
        this.flightsByDate = new HashMap<>();
        this.canceledDays = new HashSet<>();
        this.reservationsById = new HashMap<>();
    }

    /**
     * Method to add a connection between two cities, with a given capacity.
     *
     * @param orig     the origin city.
     * @param dest     the destiny city.
     * @param capacity the capacity of each flight.
     */
    public void addRoute(String orig, String dest, int capacity) {
        Route newConn = new Route(orig, dest, capacity);
        if (connectionsByCityOrig.containsKey(orig)) connectionsByCityOrig.get(orig).add(newConn);
        else {
            Set<Route> toInsert = new HashSet<>();
            toInsert.add(new Route(orig, dest, capacity));
            connectionsByCityOrig.put(orig, toInsert);
        }
    }

}
