import airport.Flight;
import airport.Reservation;
import airport.Route;
import exceptions.*;
import users.Admin;
import users.Client;
import users.User;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class AirportSystem implements IAirportSystem {

    /**
     * Associates ID to the respective User
     */
    private final Map<String, User> usersById;

    /**
     * Associates each city, by name, with the flights that leave that city.
     */
    private final Map<String, Map<String, Route>> connectionsByCityOrig;

    /**
     * Associates each ID to the respective flight
     */
    private final Map<UUID, Flight> flightsById;

    /**
     * Associates each day to the flies that happen in that day.
     * If a connection exists, but the fly in that day doesn't, then the flight will be created.
     * We can only have one flight by connection in each day.
     */
    private final Map<LocalDate, Map<Route, Flight>> flightsByDate;

    /**
     * Days cancelled by the administrator.
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
        this.usersById = new HashMap<>();
        this.connectionsByCityOrig = new HashMap<>();
        this.flightsById = new HashMap<>();
        this.flightsByDate = new HashMap<>();
        this.canceledDays = new HashSet<>();
        this.reservationsById = new HashMap<>();
    }

    /**
     * See if a certain date is in the canceled days.
     *
     * @param dateToSearch Date
     * @return true if the given date is canceled.
     */
    private boolean invalidDate(LocalDate dateToSearch) {
        return canceledDays.contains(dateToSearch);
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
        Map<String, Route> connectionsByCityDest = connectionsByCityOrig.get(origUpperCase);
        if (connectionsByCityDest != null) {
            if (connectionsByCityDest.putIfAbsent(destUpperCase, newRoute) != null)
                throw new RouteAlreadyExistsException(orig, dest);
        } else {
            connectionsByCityDest = new HashMap<>();
            connectionsByCityDest.put(destUpperCase, newRoute);
            connectionsByCityOrig.put(origUpperCase, connectionsByCityDest);
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
        Map<String, Route> routesByDestCity = connectionsByCityOrig.get(origUpperCase);
        if (routesByDestCity == null || routesByDestCity.isEmpty())
            throw new RouteDoesntExistException(orig, dest);
        Route route = routesByDestCity.get(destUpperCase);
        if (route == null)
            throw new RouteDoesntExistException(orig, dest);
        return route;
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
        Map<String, Route> routes = connectionsByCityOrig.get(origUpperCase);
        if (routes != null) {
            return routes.containsKey(destUpperCase);
        }
        return false;
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
    }

    /**
     * @param cities All cities in order of passage
     * @return all cities in order of passage
     */
    private Queue<Route> getRoutesByCities(final List<String> cities) throws RouteDoesntExistException {
        String origCity = null;
        String destCity;
        Queue<Route> routes = new ArrayDeque<>();
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
    }

    /**
     * Get a valid Flight on the given day, departing from the origin city to the destination city.
     *
     * @param date  Date we want
     * @param route Route
     * @return a Flight
     * @throws FlightDoesntExistYetException Is launched if the flight doesn't exist yet
     */
    private Flight getValidFlight(LocalDate date, Route route)
            throws FlightDoesntExistYetException {
        Map<Route, Flight> flightsByRoute = this.flightsByDate.get(date);
        if (flightsByRoute == null)
            throw new FlightDoesntExistYetException();
        Flight flight = flightsByRoute.get(route);
        if (flight == null)
            throw new FlightDoesntExistYetException();

        return flight;
    }

    /**
     * Returns a set of flights that make the trip possible.
     *
     * @param cities the connections.
     * @param start  the start date of the interval.
     * @param end    the end date of the interval.
     * @return The available flights
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
                    flight.cancelSeat();
                throw new BookingFlightsNotPossibleException();
            }

            if (invalidDate(dateToSearch)) {
                dateToSearch = dateToSearch.plusDays(1);
                continue;
            }

            Flight flight;
            try {
                flight = getValidFlight(dateToSearch, route);
            } catch (FlightDoesntExistYetException e) {
                flight = addFlight(route, dateToSearch);
            }

            try {
                flight.preReservationSeat();
            } catch (FullFlightException ignored) {
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
        flightsById.putIfAbsent(flight.id, flight);
        Map<Route, Flight> flights = flightsByDate.get(date);
        if (flights == null) {
            flights = new HashMap<>();
            flights.put(route, flight);
            flightsByDate.put(date, flights);
        } else
            flights.put(route, flight);
        return flight;
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
            throws BookingFlightsNotPossibleException, RouteDoesntExistException {
        User user = usersById.get(userName);

        Set<Flight> flights;
        try {
            flights = getConnectedFlights(cities, start, end);
        } catch (BookingFlightsNotPossibleException e) {
            throw new BookingFlightsNotPossibleException();
        }
        Set<UUID> flightIds = flights.stream().map(e -> e.id).collect(Collectors.toSet());
        Reservation reservation = new Reservation(user, flightIds);
        for (Flight flight : flights) {
            try {
                flight.addReservation(reservation.id);
                user.addReservation(reservation.id);
            } catch (FullFlightException ignored) {
                // Shouldn't happen
                throw new BookingFlightsNotPossibleException();
            }
        }
        reservationsById.put(reservation.id, reservation);
        return reservation.id;
    }

    /**
     * Cancel the reservation on all flights involved in the given reservation
     *
     * @param reservation Reservation to cancel
     */
    private void cancelReservation(Reservation reservation) {
        Set<UUID> flightIds = reservation.getFlightIds();
        for (UUID id : flightIds) {
            Flight flight = flightsById.get(id);
            if (flight != null)
                flight.removeReservation(reservation.id);
        }
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

        Reservation reservation = this.reservationsById.remove(reservationId);
        if (reservation == null)
            throw new ReservationNotFoundException(reservationId);

        User user = usersById.get(userName);
        if (user == null) {
            this.reservationsById.put(reservationId, reservation);
            throw new UserNotFoundException("User not found: " + userName + " [username]");
        }
        if (!user.containsReservation(reservationId))
            throw new ReservationDoesNotBelongToTheClientException(reservationId, userName);


        cancelReservation(reservation);
        user.removeReservation(reservationId);
        return reservation;
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
        this.canceledDays.add(day);
        Set<Reservation> canceledReservations = new HashSet<>();
        Map<Route, Flight> flightsOneDay = this.flightsByDate.remove(day);
        if (flightsOneDay == null || flightsOneDay.isEmpty())
            return new HashSet<>();
        List<Flight> flights = new ArrayList<>(flightsOneDay.values());
        // Maybe thrown an exception when doesn't exist reservations in that day
        if (!flights.isEmpty()) {
            for (Flight flight : flights) {
                // Remove flight from flightsById
                this.flightsById.remove(flight.id);
                for (UUID reservationId : flight.getReservations()) {
                    // Remove reservation from reservationsById
                    Reservation reservation = reservationsById.remove(reservationId);
                    cancelReservation(reservation);
                    canceledReservations.add(reservation);
                }
            }
        }
        return canceledReservations;
    }

    /**
     * Gets the existent routes.
     *
     * @return the list of the existent routes.
     */
    public List<Route> getRoutes() {
        return this.connectionsByCityOrig.values()
                .stream()
                .flatMap(e -> e.values().stream())
                .collect(Collectors.toList());
    }

    /**
     * Registers a user into the system.
     *
     * @param user the user
     * @throws UsernameAlreadyExistsException when the username is already registered.
     */
    private void register(User user) throws UsernameAlreadyExistsException {
        String username = user.getUsername();
        if (usersById.containsKey(username))
            throw new UsernameAlreadyExistsException("Username already exists: " + username);
        usersById.put(username, user);
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
        register(user);
        return user;
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
        register(user);
        return user;
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
        User user = usersById.get(username);
        if (user == null)
            throw new UserNotFoundException("User not found: " + username + " [username]");

        if (!user.validPassword(password))
            throw new InvalidCredentialsException("Invalid credentials: " + username + " [username]");
        return user;
    }
}
