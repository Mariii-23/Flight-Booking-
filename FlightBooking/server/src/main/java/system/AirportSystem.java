package system;

import airport.Flight;
import airport.Reservation;
import airport.Route;
import exceptions.*;
import locks.LockObject;
import users.Admin;
import users.Client;
import users.User;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class AirportSystem implements IAirportSystem {

    /**
     * Associates ID to the respective User
     */
    private final Map<String, User> usersById;
    private final Lock readLockUser;
    private final Lock writeLockUser;

    /**
     * Associates each city, by name, with the flights that leave that city.
     */
    private final Map<String, LockObject<Map<String, Route>>> connectionsByCityOrig;
    private final Lock readLockConnections;
    private final Lock writeLockConnections;

    /**
     * Associates each ID to the respective flight
     */
    private final Map<UUID, Flight> flightsById;
    // Por só um lock
    private final Lock readLockFlights;
    private final Lock writeLockFlights;

    /**
     * Associates each day to the flies that happen in that day.
     * If a connection exists, but the fly in that day doesn't, then the flight will be created.
     * We can only have one flight by connection in each day.
     */
    private final Map<LocalDate, LockObject<Map<Route, Flight>>> flightsByDate;
    private final Lock readLockFlightsByDate;
    private final Lock writeLockFlightsByDate;

    /**
     * Days cancelled by the administrator.
     * This is used to avoid reservations in cancelled days.
     */
    private final Set<LocalDate> canceledDays;
    private final Lock readLockCanceledDays;
    private final Lock writeLockCanceledDays;

    /**
     * Associates each reservation to his id.
     */
    private final Map<UUID, Reservation> reservationsById;
    private final Lock readLockReservations;
    private final Lock writeLockReservations;

    /**
     * Constructor.
     * It starts with empty parameters because they are all inserted by the users.
     */
    public AirportSystem() {
        this.usersById = new HashMap<>();
        this.connectionsByCityOrig = new HashMap<>();
        this.flightsById = new HashMap<>();
        this.flightsByDate = new HashMap<>();
        this.canceledDays = new HashSet<>();
        this.reservationsById = new HashMap<>();

        ReentrantReadWriteLock lockUser = new ReentrantReadWriteLock();
        this.readLockUser = lockUser.readLock();
        this.writeLockUser = lockUser.writeLock();
        ReentrantReadWriteLock lockConnections = new ReentrantReadWriteLock();
        this.readLockConnections = lockConnections.readLock();
        this.writeLockConnections = lockConnections.readLock();
        ReentrantReadWriteLock lockFlights = new ReentrantReadWriteLock();
        this.readLockFlights = lockFlights.readLock();
        this.writeLockFlights = lockFlights.writeLock();
        ReentrantReadWriteLock lockFlightsByDate = new ReentrantReadWriteLock();
        this.readLockFlightsByDate = lockFlightsByDate.readLock();
        this.writeLockFlightsByDate = lockFlightsByDate.writeLock();
        ReentrantReadWriteLock lockCanceledDays = new ReentrantReadWriteLock();
        this.readLockCanceledDays = lockCanceledDays.readLock();
        this.writeLockCanceledDays = lockCanceledDays.readLock();
        ReentrantReadWriteLock lockReservations = new ReentrantReadWriteLock();
        this.readLockReservations = lockReservations.readLock();
        this.writeLockReservations = lockReservations.writeLock();
    }

    /**
     * See if a certain date is in the canceled days.
     *
     * @param dateToSearch Date
     * @return true if the given date is canceled.
     */
    private boolean invalidDate(LocalDate dateToSearch) {
        try {
            this.readLockCanceledDays.lock();
            return canceledDays.contains(dateToSearch);
        } finally {
            this.readLockCanceledDays.unlock();
        }
    }

    private User getUserById(String username) {
        try {
            this.readLockUser.lock();
            return this.usersById.get(username);
        } finally {
            this.readLockUser.unlock();
        }
    }

    /**
     * Method to add a connection between two cities, with a given capacity.
     *
     * @param orig     the origin city.
     * @param dest     the destiny city.
     * @param capacity the capacity of each flight.
     * @throws RouteAlreadyExistsException is launched if this route already exists
     * @throws RouteDoesntExistException   is launched if this route has the same origin and destination.
     */
    public void addRoute(String orig, String dest, int capacity)
            throws RouteAlreadyExistsException, RouteDoesntExistException {
        String origUpperCase = orig.toUpperCase();
        String destUpperCase = dest.toUpperCase();
        if (origUpperCase.equals(destUpperCase)) {
            throw new RouteDoesntExistException(orig, dest);
        }
        Route newRoute = new Route(orig, dest, capacity);
        this.writeLockConnections.lock();
        LockObject<Map<String, Route>> connectionsByCityDestWithLock = connectionsByCityOrig.get(origUpperCase);
        if (connectionsByCityDestWithLock == null) {
            try {
                Map<String,Route> connectionsByCityDest = new HashMap<>();
                connectionsByCityDest.put(destUpperCase, newRoute);
                connectionsByCityOrig.put(origUpperCase, new LockObject<>(connectionsByCityDest));
            } finally {
                this.writeLockConnections.unlock();
            }
        } else {
            try {
                connectionsByCityDestWithLock.writeLock();
                this.writeLockConnections.unlock();
                Map<String,Route> connectionsByCityDest = connectionsByCityDestWithLock.elem();
                if (connectionsByCityDest.putIfAbsent(destUpperCase, newRoute) != null)
                    throw new RouteAlreadyExistsException(orig, dest);
            } finally {
                connectionsByCityDestWithLock.writeUnlock();
            }
        }
    }

    /**
     * Method to get a connection between two cities.
     *
     * @param orig the origin city.
     * @param dest the destiny city.
     * @throws RouteDoesntExistException is launched if this route doesn't exist.
     */
    protected Route getRoute(String orig, String dest) throws RouteDoesntExistException {
        String origUpperCase = orig.toUpperCase();
        String destUpperCase = dest.toUpperCase();
        try {
            readLockConnections.lock();
            LockObject<Map<String, Route>> routesByDestCityWithLock = connectionsByCityOrig.get(origUpperCase);
            if (routesByDestCityWithLock == null || routesByDestCityWithLock.elem().isEmpty())
                throw new RouteDoesntExistException(orig, dest);
            Map<String, Route> routesByDestCity = routesByDestCityWithLock.elem();
            Route route = routesByDestCity.get(destUpperCase);
            if (route == null)
                throw new RouteDoesntExistException(orig, dest);
            return route;
        } finally {
            readLockConnections.unlock();
        }
    }

    /**
     * Checks if a route is valid.
     *
     * @param orig City of departure of the flight.
     * @param dest City of arrival of the flight.
     * @return true if this route exists.
     */
    private boolean validRoute(String orig, String dest) {
        String origUpperCase = orig.toUpperCase();
        String destUpperCase = dest.toUpperCase();
        try {
            readLockConnections.lock();
            LockObject<Map<String, Route>> routesWithLock = connectionsByCityOrig.get(origUpperCase);
            return routesWithLock != null &&
                   routesWithLock.elem().containsKey(destUpperCase);
        } finally {
            readLockConnections.unlock();
        }
    }

    /**
     * Verifies if a given route can be made through the possible routes.
     *
     * @param cities All cities in order of passage
     * @return true if it's possible to make this trip
     */
    private boolean validRoutes(final List<String> cities) {
        String origCity = null;
        String destCity;
        try {
            readLockConnections.lock();
            for (String city : cities) {
                if (origCity == null) {
                    origCity = city;
                    continue;
                }
                destCity = city;
                if (!validRoute(origCity, destCity))
                    return false;
                origCity = city;
            }
            return true;
        } finally {
            readLockConnections.unlock();
        }
    }

    /**
     * @param cities All cities in order of passage
     * @return all cities in order of passage
     */
    private Queue<Route> getRoutesByCities(final List<String> cities) throws RouteDoesntExistException {
        String origCity = null;
        String destCity;
        Queue<Route> routes = new ArrayDeque<>();
        try {
            readLockConnections.lock();
            for (String city : cities) {
                if (origCity == null) {
                    origCity = city;
                    continue;
                }
                destCity = city;
                routes.add(getRoute(origCity, destCity));
                origCity = city;
            }
            return routes;
        } finally {
            readLockConnections.unlock();
        }
    }

    /**
     * Get a valid Flight on the given day, departing from the origin city to the destination city.
     *
     * @param date  Date we want
     * @param route Route
     * @return a Flight
     */
    private Flight getValidFlight(LocalDate date, Route route) {
        Map<Route, Flight> flightsByRoute;
        LockObject<Map<Route, Flight>> flightsByRouteWithLock;
        try {
            writeLockFlightsByDate.lock();
            flightsByRouteWithLock = this.flightsByDate.get(date);
            if (flightsByRouteWithLock == null) {
                Flight flight = addFlight(route, date);
                flight.lock();
                return flight;
            }

            flightsByRouteWithLock.writeLock();
            flightsByRoute = flightsByRouteWithLock.elem();

        } finally {
            writeLockFlightsByDate.unlock();
        }
        try {
            Flight flight = flightsByRoute.get(route);
            if (flight == null) {
                flight = addFlight(route, date);
            }

            flight.lock();
            return flight;
        } finally {
            flightsByRouteWithLock.writeUnlock();
        }
    }

    /**
     * Returns a set of flights that make the trip possible.
     *
     * @param cities the connections.
     * @param start  the start date of the interval.
     * @param end    the end date of the interval.
     * @return The available flights with lock active
     */
    private Set<Flight> getConnectedFlights(List<String> cities, LocalDate start, LocalDate end)
            throws BookingFlightsNotPossibleException, RouteDoesntExistException {
        LocalDate dateToSearch = start;
        Queue<Route> routes;
        routes = getRoutesByCities(cities);

        Set<Flight> flights = new HashSet<>();
        int numberFlights = routes.size();

        Route route = routes.remove();
        int reservedSeats = 0;
        while (true) {

            if (dateToSearch.isAfter(end)) {
                for (Flight flight : flights)
                    flight.unlock();
                throw new BookingFlightsNotPossibleException();
            }

            if (invalidDate(dateToSearch)) {
                dateToSearch = dateToSearch.plusDays(1);
                continue;
            }

            Flight flight;
            flight = getValidFlight(dateToSearch, route);

            if (!flight.seatAvailable()) {
                flight.unlock();
                dateToSearch = dateToSearch.plusDays(1);
                continue;
            }

            flights.add(flight);
            reservedSeats++;
            if (reservedSeats < numberFlights)
                route = routes.remove();
            else
                break;
        }
        return flights;
    }

    /**
     * Creates and add a flight
     *
     * @param route the connection
     * @param date  the day
     * @return the flight
     */
    private Flight addFlight(Route route, LocalDate date) {
        Flight flight = new Flight(route, date);
        LockObject<Map<Route, Flight>> flightsWithLock;
        try {
            writeLockFlightsByDate.lock();
            flightsWithLock = flightsByDate.get(date);

            writeLockFlights.lock();
            flightsById.putIfAbsent(flight.id, flight);

            if (flightsWithLock == null) {
                flightsWithLock = new LockObject<>(new HashMap<>());
                flightsWithLock.elem().put(route, flight);
                flightsByDate.put(date, flightsWithLock);
                return flight;
            }
            flightsWithLock.writeLock();
        } finally {
            writeLockFlights.unlock();
            writeLockFlightsByDate.unlock();
        }
        try {
            flightsWithLock.elem().put(route, flight);
                //flights.put(route, flightsWithLock.elem().put(flight));
            return flight;
        } finally {
            flightsWithLock.writeUnlock();
        }
    }

    /**
     * Reserves a flight given the connections, in the time interval.
     *
     * @param userName the user's name.
     * @param cities   the connections.
     * @param start    the start date of the interval.
     * @param end      the end date of the interval.
     * @return the reservation's id.
     */
    // FIXME change strategy -> locks flights
    public UUID reserveFlight(String userName, List<String> cities, LocalDate start, LocalDate end)
            throws BookingFlightsNotPossibleException, RouteDoesntExistException, UserNotFoundException {
        User user;
        try {
            this.readLockUser.lock();
            user = usersById.get(userName);
        } finally {
            this.readLockUser.unlock();
        }

        if (user == null)
            throw new UserNotFoundException("User not found: " + userName + " [username]");

        Set<Flight> flights;
            try {
                flights = getConnectedFlights(cities, start, end);
            } catch (BookingFlightsNotPossibleException e) {
                throw new BookingFlightsNotPossibleException();
            }
            // Set<UUID> flightIds = flights.stream().map(e -> e.id).collect(Collectors.toSet());
            Reservation reservation = new Reservation(user, flights);
            try {
                writeLockReservations.lock();
                reservationsById.put(reservation.id, reservation);
            } finally {
                writeLockReservations.unlock();
            }
            user.addReservation(reservation.id);

        try {
            for (Flight flight: flights) {
                flight.addReservation(reservation);
            }
        } catch (FullFlightException e) {
            // nao devia acontecer
            throw new BookingFlightsNotPossibleException();
        }
        finally {
            for (Flight flight: flights) {
                flight.unlock();
            }
        }

        return reservation.id;
    }

    /**
     * Cancels a flight.
     *
     * @param userName      the name of the client
     * @param reservationId the id of the reservation
     * @return the deleted @see airport.Reservation .
     * @throws ReservationNotFoundException                 is launched if the reservation doesn't exist in the AirportSystem
     * @throws ReservationDoesNotBelongToTheClientException is launched if the reservation doesn't belong to the given
     * @throws UserNotFoundException                        is launched if the given userName doesn't exist on the system
     */
    public Reservation cancelReservation(String userName, UUID reservationId) throws ReservationNotFoundException,
            ReservationDoesNotBelongToTheClientException, UserNotFoundException {

        Reservation reservation;
        try {
            writeLockReservations.lock();
            reservation = this.reservationsById.remove(reservationId);
        } finally {
            writeLockReservations.unlock();
        }

        if (reservation == null) {
            throw new ReservationNotFoundException(reservationId);
        }

        User user = getUserById(userName);

        if (user == null) {
            try {
                writeLockReservations.lock();
                this.reservationsById.put(reservationId, reservation);
                throw new UserNotFoundException("User not found: " + userName + " [username]");
            } finally {
                writeLockReservations.unlock();
            }
        }

        if (!user.containsReservation(reservationId))
            throw new ReservationDoesNotBelongToTheClientException(reservationId, userName);

        reservation.cancelReservation();
        user.removeReservation(reservationId);
        return reservation;
    }

    private void cancelReservation(Set<Reservation> reservations) {
        try {
            this.readLockReservations.lock();
            for (Reservation reservation : reservations) {
                this.reservationsById.remove(reservation.id);
            }
        } finally {
            this.readLockReservations.unlock();
        }
    }

    /**
     * Cancels a day. Preventing new reservations and canceling the remaining ones from that day.
     *
     * @param day the day.
     * @return all canceled @see airport.Reservation .
     */
    public Set<Reservation> cancelDay(LocalDate day) throws DayAlreadyCanceledException {
        if (invalidDate(day))
            throw new DayAlreadyCanceledException(day);

        try {
            this.writeLockCanceledDays.lock();
            this.canceledDays.add(day);
        } finally {
            this.writeLockCanceledDays.unlock();
        }

        Set<Reservation> canceledReservations = new HashSet<>();
        LockObject<Map<Route, Flight>> flightsOneDayWithLock;
        try {
            this.writeLockFlightsByDate.lock();
            flightsOneDayWithLock = this.flightsByDate.remove(day);
            if (flightsOneDayWithLock == null)
                return new HashSet<>();

            flightsOneDayWithLock.writeLock();
        } finally {
            this.writeLockFlightsByDate.lock();
        }
        try {
            Map<Route, Flight> flightsOneDay = flightsOneDayWithLock.elem();
            if (flightsOneDay == null || flightsOneDay.isEmpty())
                return new HashSet<>();

            List<Flight> flights = new ArrayList<>(flightsOneDay.values());
            if (!flights.isEmpty()) {
                flights.forEach(Flight::lock);
                try {
                    for (Flight flight : flights) {
                        try {
                            this.writeLockFlights.lock();
                            this.flightsById.remove(flight.id);
                        } finally {
                            this.writeLockFlights.unlock();
                        }
                        canceledReservations.addAll(flight.getReservations());
                        flight.cancelFlight();
                    }
                } finally {
                    flights.forEach(Flight::unlock);
                }
            }
            cancelReservation(canceledReservations);
            return canceledReservations;
        } finally {
            flightsOneDayWithLock.writeUnlock();
        }
    }

    /**
     * Gets the existent routes.
     *
     * @return the list of the existent routes.
     */
    public List<Route> getRoutes() {
        try {
            this.readLockConnections.lock();
            return this.connectionsByCityOrig.values()
                    .stream()
                    .flatMap(e -> {
                        try {
                            e.readLock();
                            var elems = e.elem().values();
                            return elems.stream();
                        } finally {
                            e.readUnlock();
                        }
                    })
                    .collect(Collectors.toList());
        } finally {
            this.readLockConnections.unlock();
        }
    }

    /**
     * Registers a user into the system.
     *
     * @param user the user
     * @throws UsernameAlreadyExistsException when the username is already registered.
     */
    private void register(User user) throws UsernameAlreadyExistsException {
        String username = user.getUsername();
        try {
            this.writeLockUser.lock();
            if (usersById.containsKey(username))
                throw new UsernameAlreadyExistsException("Username already exists: " + username);
            usersById.put(username, user);
        } finally {
            this.writeLockUser.unlock();
        }
    }

    /**
     * Registers a client into the system.
     *
     * @param username Username
     * @param password Password
     * @return Client
     * @throws UsernameAlreadyExistsException Username already exists.
     */
    public User registerClient(String username, String password) throws UsernameAlreadyExistsException {
        User user = new Client(username, password);
        try {
            this.writeLockUser.lock();
            register(user);
            return user;
        } finally {
            this.writeLockUser.unlock();
        }
}

    /**
     * Registers an admin into the system.
     *
     * @param username Username
     * @param password Password
     * @return Admin
     * @throws UsernameAlreadyExistsException Username already exists.
     */
    public User registerAdmin(String username, String password) throws UsernameAlreadyExistsException {
        User user = new Admin(username, password);
        try {
            this.writeLockUser.lock();
            register(user);
            return user;
        } finally {
            this.writeLockUser.unlock();
        }
    }

    /**
     * Authenticates a user.
     *
     * @param username the user's name.
     * @param password the user's password.
     * @return User
     * @throws UserNotFoundException       If the user isn't in the system.
     * @throws InvalidCredentialsException If the password isn't correct.
     */
    public User authenticate(String username, String password)
            throws UserNotFoundException, InvalidCredentialsException {
        try {
            this.readLockUser.lock();
            User user = usersById.get(username);
            if (user == null)
                throw new UserNotFoundException("User not found: " + username + " [username]");

            if (!user.validPassword(password))
                throw new InvalidCredentialsException("Invalid credentials: " + username + " [username]");
            return user;
        } finally {
            this.readLockUser.unlock();
        }
    }

    public Reservation getReservation(UUID resID){
        return this.reservationsById.get(resID);
    }

    public PossiblePath getPathsBetween(String from, String dest) throws RouteDoesntExistException {
        if (destinationCitiesFrom(from.toUpperCase()).size() == 0) throw new RouteDoesntExistException();
        return getPathsBetweenAux(from.toUpperCase(), dest.toUpperCase(), 4);
    }

    //Faltam locks
    private Set<String> destinationCitiesFrom(String origin){

        LockObject<Map<String, Route>> thisCity =connectionsByCityOrig.get(origin);
        if (thisCity == null) return new TreeSet<>();
        return thisCity.elem().keySet();
    }

    private PossiblePath getPathsBetweenAux(String from, String dest, int depth){
        // No city was found from this city.
        if (depth == 0)
            return null;
        // A connection was possible.
        if (from.equals(dest))
            return new PossiblePath(true, from);
        Set<String> connectedCitiesFromHere = destinationCitiesFrom(from);
        PossiblePath here = new PossiblePath(false, from);
        for (String city : connectedCitiesFromHere){
            PossiblePath res = getPathsBetweenAux(city, dest, depth-1);
            if (res != null){
                here.addPossiblePath(res);
            }
        }
        if (here.numPossiblePaths() == 0)
            return null;
        return here;

    }

    public static void main(String[] args) throws RouteAlreadyExistsException, RouteDoesntExistException {
        AirportSystem air = new AirportSystem();
        air.addRoute("a", "b", 1);
        air.addRoute("a", "c", 1);
        air.addRoute("c", "dest", 1);
        air.addRoute("b", "dest", 1);
        air.addRoute("b", "d", 1);
        air.addRoute("d", "dest", 1);
        air.addRoute("a", "dest", 1);
        air.addRoute("a", "d", 1);
        air.addRoute("f", "o", 1);

        System.out.println("Conexões:");
        try {
            System.out.println(air.getPathsBetween("a", "dest").toStringPretty(""));
            System.out.println(air.getPathsBetween("dest", "dest2").toStringPretty(""));
        }
        catch (RouteDoesntExistException e){}
    }
}
