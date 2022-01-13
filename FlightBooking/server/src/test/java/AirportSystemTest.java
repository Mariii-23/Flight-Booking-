import airport.Route;
import exceptions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import users.Admin;
import users.Client;
import users.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AirportSystemTest {

    // Used to test
    private final String username = "admin";
    private AirportSystem airportSystem;
    // Used to test.
    private LocalDate date;

    @BeforeAll
    static void startTest() {
        System.out.println("Starting test");
    }

    @AfterAll
    static void endTest() {
        System.out.println("Ending test");
    }

    /**
     * Method to create a stream composed by list of cities that don't exist in system and current date.
     */
    private static Stream<Arguments> citiesDontExist() {
        List<Arguments> listOfArguments = new ArrayList<>();
        listOfArguments.add(Arguments.of(new ArrayList<>(Arrays.asList("London", "NotCity"))));
        listOfArguments.add(Arguments.of(new ArrayList<>(Arrays.asList("NotCity", "London"))));
        listOfArguments.add(Arguments.of(new ArrayList<>(Arrays.asList("NotCity", "NotCity"))));
        return listOfArguments.stream();
    }

    /**
     * Method to create a stream composed by list of cities that don't exist in system and current date.
     */
    private static Stream<Arguments> usernamesAndPasswords() {
        return Stream.of(
                Arguments.of("Maria", "Hello"),
                Arguments.of("Pedro", "Hello"),
                Arguments.of("Peter", "Hello"),
                Arguments.of("Jacinto", "Hello")
        );
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        airportSystem = new AirportSystem();
        date = LocalDate.now();
        System.out.println("---- TEST ----");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.out.println("---------------");
    }

    /**
     * Private method to initialize airport system.
     * Capacity of each flight is 1.
     */
    private void initRoutes_LondonParisLisbon() {
        addRoute("London", "Paris", 1);
        addRoute("Paris", "Lisbon", 1);
    }

    // -------------------- Add Route -------------------

    private void initUser() {
        registerAdmin(username, username);
    }

    private void initUserAndRoutes_LondonParisLisbon() {
        initUser();
        initRoutes_LondonParisLisbon();
    }

    @DisplayName("Add Route")
    @ParameterizedTest
    @CsvSource({"Lisbon,London,30", "London,Paris,1", "Lisbon,Paris,23"})
    public void addRoute(String orig, String dest, int capacity) {
        Assertions.assertDoesNotThrow(() -> {
            System.out.println("Add route: " + orig + "-> " + dest + " (" + capacity + ")");
            airportSystem.addRoute(orig, dest, capacity);
        });
    }

    //--------------------- Get Routes ----------------------

    @ParameterizedTest
    @CsvSource({"Lisbon,London,30", "Lisbon,London,23", "LiSBon,London,30", "Lisbon,LOndoN,30", "LISbon,lonDOn,21"})
    void routeAlreadyExistException(String orig, String dest, int capacity) {
        addRoute("Lisbon", "London", 30);
        Assertions.assertThrows(RouteAlreadyExistsException.class, () ->
                airportSystem.addRoute(orig, dest, capacity));
    }

    @ParameterizedTest
    @CsvSource({"Lisbon,Lisbon,30"})
    void routeSameOriginDestinationException(String orig, String dest, int capacity) {
        Assertions.assertThrows(RouteDoesntExistException.class, () ->
                airportSystem.addRoute(orig, dest, capacity));
    }

    /**
     * Test  to verify if the route stores with case insensivity.
     * We create one root, and test if we can get roots with names that have other "camel cases" in name.
     * In case the route doesn't exist, an excpetion is thrown.
     */
    @ParameterizedTest
    @CsvSource({"Lisbon,London,30", "London,Paris,1", "Lisbon,Paris,23"})
    void getRoute(String orig, String dest, int capacity) {
        Assertions.assertDoesNotThrow(() -> {
            System.out.println("Add route: " + orig + "-> " + dest + " (" + capacity + ")");
            airportSystem.addRoute(orig, dest, capacity);
            System.out.println("GET route: " + orig + "-> " + dest);
            List<Route> routes_tested = new ArrayList<>();
            routes_tested.add(airportSystem.getRoute(orig, dest));
            System.out.println("GET route: " + orig.toUpperCase() + "-> " + dest.toLowerCase());
            routes_tested.add(airportSystem.getRoute(orig.toUpperCase(), dest.toLowerCase()));
            System.out.println("GET route: " + orig.toLowerCase() + "-> " + dest.toUpperCase());
            routes_tested.add(airportSystem.getRoute(orig.toLowerCase(), dest.toUpperCase()));
            for (Route tested : routes_tested) {
                assert tested.origin.equals(orig);
                assert tested.destination.equals(dest);
                assert tested.capacity == capacity;
            }
        });
    }

    @ParameterizedTest
    @CsvSource({"Lisbon,Paris", "Paris,Lisbon"})
    public void getRouteDoesntExistException(String orig, String dest) {
        addRoute("Lisbon", "London", 30);
        Assertions.assertThrows(RouteDoesntExistException.class, () -> {
            System.out.println("GET route: " + orig + "-> " + dest);
            airportSystem.getRoute(orig, dest);
        });
    }

    @ParameterizedTest
    @CsvSource({"Lisbon,Lisbon,1", "LISbon,Lisbon,2", "LISbon,LisBon,10"})
    public void invalidRouteException(String orig, String dest, int capacity) {
        Assertions.assertThrows(RouteDoesntExistException.class, () ->
                airportSystem.addRoute(orig, dest, capacity));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 1000, 10000})
    void getRoutes(int N) {
        for (int i = 0; i < N; i++)
            addRoute(String.valueOf(i), "London", i);

        for (int i = 0; i < N; i++)
            addRoute(String.valueOf(i), "Paris", i);

        List<Route> list = airportSystem.getRoutes();
        assert list.size() == N * 2;
    }

    //---------------------- Reservation Flights ----------------
    @org.junit.jupiter.api.Test
    void reserveFlight() {
        initUser();
        addRoute("Paris", "Lisbon", 2);
        List<String> cities1 = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));
        Assertions.assertDoesNotThrow(() -> {
            airportSystem.reserveFlight(username, cities1, date, date);
            airportSystem.reserveFlight(username, cities1, date, date);
        });
    }

    @org.junit.jupiter.api.Test
    void reserveFlight_DifferentDays() {
        initUser();
        addRoute("Paris", "Lisbon", 1);
        List<String> cities1 = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));
        Assertions.assertDoesNotThrow(() -> {
            airportSystem.reserveFlight(username, cities1, date, date);
            airportSystem.reserveFlight(username, cities1, date, date.plusDays(1));
        });
    }

    /**
     * Test to check if the pre-reservations are removed if the full flight isn't possible.
     * Example of the tested situation:
     * Flight 1:      B -> C
     * Flight 2: A -> B -> C
     * Flight 3: A -> B
     * The flight 1 is full, and when a client wants to reserve the flight 2, it pre-reserves the flight A -> B.
     * So, the reservation of flight 2 isn't possible, B -> C is full, and the system should remove the pre-reservation for the flight A->B.
     * We test if it's possible to reserve a flight after a pre-reservation is removed.
     */
    @org.junit.jupiter.api.Test
    void reserveFlight_preReservationCanceled() {
        initUser();
        initRoutes_LondonParisLisbon();
        List<String> cities1 = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));
        List<String> cities2 = new ArrayList<>(Arrays.asList("London", "Paris", "Lisbon"));
        List<String> cities3 = new ArrayList<>(Arrays.asList("LoNdoN", "ParIs"));
        try {
            airportSystem.reserveFlight(username, cities1, date, date);
            airportSystem.reserveFlight(username, cities2, date, date);
            fail();
        } catch (BookingFlightsNotPossibleException ignored) {
            System.out.println("One flight isn't possible, the pre-reservations should be removed.");
        } catch (RouteDoesntExistException ignored) {
            fail();
        }
        try {
            airportSystem.reserveFlight(username, cities3, date, date);
        } catch (RouteDoesntExistException | BookingFlightsNotPossibleException e) {
            fail();
        }
    }

    /**
     * Exception thrown when the flight has no free seats.
     */
    @org.junit.jupiter.api.Test
    void reserveFlight_FullFlightException() {
        initUser();
        addRoute("Paris", "Lisbon", 1);
        List<String> cities1 = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));

        Assertions.assertDoesNotThrow(() -> {
            airportSystem.reserveFlight(username, cities1, date, date);
        });
        Assertions.assertThrows(BookingFlightsNotPossibleException.class, () ->
                airportSystem.reserveFlight(username, cities1, date, date));
    }

    //---------------------- Cancel Flights ----------------

    /*
     * Exception thrown when the city don't exist.
     *
     * @param cities that don't exist in system.
     */
    @ParameterizedTest
    @MethodSource("citiesDontExist")
    void reserveFlight_RouteDoesntExistException(List<String> cities) {
        initUserAndRoutes_LondonParisLisbon();
        Assertions.assertThrows(RouteDoesntExistException.class, () ->
                airportSystem.reserveFlight(username, cities, date, date));
    }

    /**
     * Cancel one flight and try to reserve again.
     */
    @org.junit.jupiter.api.Test
    void cancelFlightTest() {
        initRoutes_LondonParisLisbon();
        List<String> cities = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));
        String client = "Hello";

        Assertions.assertDoesNotThrow(() -> {
            User user = airportSystem.registerClient(client, client);
            UUID reservation = airportSystem.reserveFlight(client, cities, date, date);
            assert user.containsReservation(reservation);
            airportSystem.cancelReservation(client, reservation);
            assert !user.containsReservation(reservation);
        });
    }

    /**
     * Cancel one flight in a connection and try to reserve the second flight.
     */
    @org.junit.jupiter.api.Test
    void cancelConnectionFlight() {
        initRoutes_LondonParisLisbon();
        List<String> cities1 = new ArrayList<>(Arrays.asList("London", "Paris", "Lisbon"));
        List<String> cities2 = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));
        String client = "Hello";
        registerClient(client, client);

        Assertions.assertDoesNotThrow(() -> {
            UUID reservation = airportSystem.reserveFlight(client, cities1, date, date);
            airportSystem.cancelReservation(client, reservation);
            airportSystem.reserveFlight(client, cities2, date, date);
        });
    }

    @org.junit.jupiter.api.Test
    void cancelFlight_NotCurrentUserException() {
        initUserAndRoutes_LondonParisLisbon();
        registerAdmin(username + username, username);
        List<String> cities = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));

        Assertions.assertThrows(ReservationDoesNotBelongToTheClientException.class, () -> {
            UUID reservation = airportSystem.reserveFlight(username, cities, date, date);
            airportSystem.cancelReservation(username + username, reservation);
        });
    }

    @org.junit.jupiter.api.Test
    void cancelFlight_UserNotFoundException() {
        initUserAndRoutes_LondonParisLisbon();
        List<String> cities = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));

        Assertions.assertThrows(UserNotFoundException.class, () -> {
            UUID reservation = airportSystem.reserveFlight(username, cities, date, date);
            airportSystem.cancelReservation(username + username, reservation);
        });
    }

    @org.junit.jupiter.api.Test
    void cancelFlight_ReservationDoesNotBelongToTheClient() {
        initUserAndRoutes_LondonParisLisbon();
        List<String> cities = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));
        String s = "other";
        Assertions.assertDoesNotThrow(() -> {
            airportSystem.registerClient(s, s);
            UUID reservation = airportSystem.reserveFlight(s, cities, date, date);
            Assertions.assertThrows(ReservationDoesNotBelongToTheClientException.class, () ->
                    airportSystem.cancelReservation(username, reservation));
        });

    }

    //---------------------- Cancel Days ----------------

    @org.junit.jupiter.api.Test
    void cancelFlight_ReservationNotFoundException() {
        initUser();
        Assertions.assertThrows(ReservationNotFoundException.class, () ->
                airportSystem.cancelReservation(username, UUID.randomUUID()));
        Assertions.assertThrows(ReservationNotFoundException.class, () ->
                airportSystem.cancelReservation(username, UUID.randomUUID()));
    }

    @org.junit.jupiter.api.Test
    void cancelDay() {
        Assertions.assertDoesNotThrow(() -> {
            airportSystem.cancelDay(date);
        });
    }

    @org.junit.jupiter.api.Test
    void cancelDay_DayAlreadyCanceledException() {
        Assertions.assertThrows(DayAlreadyCanceledException.class, () -> {
            airportSystem.cancelDay(date);
            airportSystem.cancelDay(date);
        });
    }

    @org.junit.jupiter.api.Test
    void cancelReservationAfterCancelDay() {
        initRoutes_LondonParisLisbon();
        List<String> cities = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));
        String client = "hello2";
        registerAdmin(client, client);
        Assertions.assertThrows(ReservationNotFoundException.class, () -> {
            UUID reservation = airportSystem.reserveFlight(client, cities, date, date);
            airportSystem.cancelDay(date);
            airportSystem.cancelReservation(client, reservation);
        });
    }

    @org.junit.jupiter.api.Test
    void cancelReservationAfterCancelDay1() {
        initUserAndRoutes_LondonParisLisbon();
        List<String> cities = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));
        Assertions.assertThrows(BookingFlightsNotPossibleException.class, () -> {
            airportSystem.cancelDay(date);
            airportSystem.reserveFlight(username, cities, date, date);
        });
    }

    @org.junit.jupiter.api.Test
    void cancelDayWithFlights() {
        initUserAndRoutes_LondonParisLisbon();
        List<String> cities = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));
        Assertions.assertDoesNotThrow(() -> {
            airportSystem.reserveFlight(username, cities, date, date);
            airportSystem.cancelDay(date);
        });
    }

    // ------------- Users ---------------------

    @org.junit.jupiter.api.Test
    void addFlightAfterDayCanceled() {
        initUserAndRoutes_LondonParisLisbon();
        List<String> cities = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));
        Assertions.assertDoesNotThrow(() -> {
            airportSystem.cancelDay(date.plusDays(1));
            airportSystem.reserveFlight(username, cities, date, date);
        });
    }

    @ParameterizedTest
    @MethodSource("usernamesAndPasswords")
    void registerClient(String username, String password) {
        System.out.println("Register client -> " + username);
        Assertions.assertDoesNotThrow(() -> {
            airportSystem.registerClient(username, password);
        });
    }

    @ParameterizedTest
    @MethodSource("usernamesAndPasswords")
    void registerAdmin(String username, String password) {
        System.out.println("Register admin -> " + username);
        Assertions.assertDoesNotThrow(() -> {
            airportSystem.registerAdmin(username, password);
        });
    }

    @ParameterizedTest
    @MethodSource("usernamesAndPasswords")
    void registerClient_UsernameAlreadyExist(String username, String password) {
        System.out.println("Register client -> " + username);
        registerClient(username, password);
        Assertions.assertThrows(UsernameAlreadyExistsException.class, () -> {
            System.out.println("TRY: Register client -> " + username);
            airportSystem.registerClient(username, password);
        });
    }

    @ParameterizedTest
    @MethodSource("usernamesAndPasswords")
    void registerAdmin_UsernameAlreadyExist(String username, String password) {
        System.out.println("Register admin -> " + username);
        registerAdmin(username, password);
        Assertions.assertThrows(UsernameAlreadyExistsException.class, () -> {
            System.out.println("TRY: Register admin -> " + username);
            airportSystem.registerAdmin(username, password);
        });
    }

    @ParameterizedTest
    @MethodSource("usernamesAndPasswords")
    void authenticateClient(String username, String password) {
        registerClient(username, password);
        Assertions.assertDoesNotThrow(() -> {
            User user = airportSystem.authenticate(username, password);
            assert user != null;
            assert user instanceof Client;
        });
    }

    @ParameterizedTest
    @MethodSource("usernamesAndPasswords")
    void authenticateAdmin(String username, String password) {
        registerAdmin(username, password);
        Assertions.assertDoesNotThrow(() -> {
            User user = airportSystem.authenticate(username, password);
            assert user != null;
            assert user instanceof Admin;
        });
    }


    @ParameterizedTest
    @MethodSource("usernamesAndPasswords")
    void authenticate_UserNotFound(String username, String password) {
        registerClient(username, password);
        Assertions.assertThrows(UserNotFoundException.class, () -> {
            System.out.println("TRY: Authenticate user -> " + username.toUpperCase());
            airportSystem.authenticate(username.toUpperCase(), password);
        });
        registerAdmin(username.toUpperCase(), password);
        Assertions.assertThrows(UserNotFoundException.class, () -> {
            System.out.println("TRY: Authenticate user -> " + username.toLowerCase());
            airportSystem.authenticate(username.toLowerCase(), password);
        });
    }

    @ParameterizedTest
    @MethodSource("usernamesAndPasswords")
    void authenticate_invalidCredentialsException(String username, String password) {
        registerClient(username, password);
        Assertions.assertThrows(InvalidCredentialsException.class, () ->
                airportSystem.authenticate(username, password.toUpperCase()));
        registerAdmin(username.toUpperCase(), password);
        Assertions.assertThrows(InvalidCredentialsException.class, () ->
                airportSystem.authenticate(username.toUpperCase(), password.toLowerCase()));
    }

    @ParameterizedTest
    @MethodSource("usernamesAndPasswords")
    void changePassword(String username, String password) {
        registerClient(username, password);
        Assertions.assertDoesNotThrow(() -> {
            airportSystem.changeUserPassword(username, password, password.toUpperCase());
            airportSystem.authenticate(username, password.toUpperCase());
        });
    }
}