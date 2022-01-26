package system;

import airport.PossiblePath;
import airport.Reservation;
import airport.Route;
import exceptions.*;
import users.Notification;
import users.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public interface IAirportSystem {

    /**
     * Adds a new route into the system.
     *
     * @param origin   the origin city.
     * @param destiny  the destiny city.
     * @param capacity the route capacity.
     * @throws RouteAlreadyExistsException is launched if this route already exists
     * @throws RouteDoesntExistException   is launched if this route has the same origin and destination.
     */
    void addRoute(String origin, String destiny, int capacity) throws RouteAlreadyExistsException,
            RouteDoesntExistException;

    /**
     * Cancels a day. Preventing new reservations and canceling the remaining ones from that day.
     *
     * @param day the day.
     * @return all canceled @see airport.Reservation .
     */
    Set<Reservation> cancelDay(LocalDate day) throws DayAlreadyCanceledException;

    /**
     * Reserves a flight given the connections, in the time interval.
     *
     * @param userName the user's name.
     * @param cities   the connections.
     * @param start    the start date of the interval.
     * @param end      the end date of the interval.
     * @return the reservation's id.
     * @throws BookingFlightsNotPossibleException This can happen because the day is cancel,
     *                                            or because the possible flights are full.
     * @throws RouteDoesntExistException          if there is no route possible.
     */
    UUID reserveFlight(String userName, List<String> cities, LocalDate start, LocalDate end)
            throws BookingFlightsNotPossibleException, RouteDoesntExistException, UserNotFoundException, InvalidDateException;

    /**
     * Cancels a flight.
     *
     * @param userName      the name of the client
     * @param reservationId the id of the reservation
     * @return the deleted @see airport.Reservation .
     * @throws ReservationNotFoundException                 is launched if the reservation doesn't exist in the system.AirportSystem
     * @throws ReservationDoesNotBelongToTheClientException is launched if the reservation doesn't belong to the given
     *                                                      client
     */
    Reservation cancelReservation(String userName, UUID reservationId) throws ReservationNotFoundException,
            ReservationDoesNotBelongToTheClientException, UserNotFoundException;

    /**
     * Gets the existent routes.
     *
     * @return the list of the existent routes.
     */
    List<Route> getRoutes();

    PossiblePath getPathsBetween(String from, String dest) throws RouteDoesntExistException;

    /**
     * @param username the name of the user
     * @return Reservations
     * @throws UserNotFoundException Invalid username.
     */
    Set<Reservation> getReservationsFromClient(String username) throws UserNotFoundException;

    /**
     * Registers a client into the system.
     *
     * @param username Username
     * @param password Password
     * @return Client
     * @throws UsernameAlreadyExistsException Username already exists.
     */
    User registerClient(String username, String password) throws UsernameAlreadyExistsException;

    /**
     * Registers an admin into the system.
     *
     * @param username Username
     * @param password Password
     * @return Admin
     * @throws UsernameAlreadyExistsException Username already exists.
     */
    User registerAdmin(String username, String password) throws UsernameAlreadyExistsException;

    /**
     * Authenticates a user.
     *
     * @param username the user's username.
     * @param password the user's password.
     * @return User
     */
    User authenticate(String username, String password)
            throws UserNotFoundException, InvalidCredentialsException;

    /**
     * Change password of a user.
     *
     * @param username    username.
     * @param oldPassword old password.
     * @param newPassword new password.
     */
    default void changeUserPassword(String username, String oldPassword, String newPassword)
            throws UserNotFoundException, InvalidCredentialsException {
        User user = authenticate(username, oldPassword);
        user.changePassword(newPassword);
    }

    Queue<Notification> getNotificationsByUsername(String username) throws UserNotFoundException;
}
