package airport;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import users.Admin;
import users.Client;
import users.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class FlightTest {


    @BeforeAll
    static void startTest() {
        System.out.println("Starting test");
    }

    @AfterAll
    static void endTest() {
        System.out.println("Ending test");
    }

    //@org.junit.jupiter.api.BeforeEach
    //void setUp() {
    //}

    //@org.junit.jupiter.api.AfterEach
    //void tearDown() {
    //}


    private static Stream<Arguments> flights() {
        Admin admin = new Admin("Diogo", "Diogo1");
        User user = new Client("Mariana", "Mariana1");

        Route r1 = new Route("Porto", "Lisbon", 30);
        Route r2 = new Route("Paris", "Nevada", 30);
        Route r3 = new Route("Mallorca", "Barcelona", 30);

        Set<Reservation> reservations = new HashSet<>();

        Flight f1 = new Flight(UUID.randomUUID(), r1, LocalDate.now(), reservations);

        Reservation reservation1 = new Reservation(admin, new HashSet<>());
        Reservation reservation2 = new Reservation(user, new HashSet<>());
        Reservation reservation3 = new Reservation(user, new HashSet<>());

        reservations.add(reservation1);
        reservations.add(reservation2);
        reservations.add(reservation3);


        return Stream.of(
                Arguments.of(new Flight(UUID.randomUUID(), r1, LocalDate.now(), reservations)),
                Arguments.of(new Flight(UUID.randomUUID(), r2, LocalDate.now(), reservations)),
                Arguments.of(new Flight(UUID.randomUUID(), r3, LocalDate.now().plusDays(3), reservations))
        );
    }

    public static boolean equals(Flight flight1, Flight flight2) {
        return flight1.id.equals(flight2.id) && Objects.equals(flight1.route, flight2.route) && Objects.equals(flight1.date, flight2.date);
    }

    @ParameterizedTest
    @MethodSource("flights")
    void serializeAndDeserialize(Flight flight) {
        byte[] bytes = flight.serialize();
        Flight r = flight.deserialize(bytes);

        Assertions.assertTrue(equals(r, flight));
    }
}
