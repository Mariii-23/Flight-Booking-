package airport;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import users.Client;
import users.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class ReservationTest {

    @BeforeAll
    static void startTest() {
        System.out.println("Starting test");
    }

    @AfterAll
    static void endTest() {
        System.out.println("Ending test");
    }

    private static Stream<Arguments> reservations() {
        User user1 = new Client("1", "1");
        User user2 = new Client("2", "2");
        User user3 = new Client("3", "3");

        Route route1 = new Route("London", "Paris", 200);
        Route route2 = new Route("Paris", "London", 100);

        LocalDate localDate1 = LocalDate.of(1, 1, 1);
        LocalDate localDate2 = LocalDate.of(2, 2, 2);

        Set<Flight> flights = new HashSet<>();
        flights.add(new Flight(route1, localDate1));
        flights.add(new Flight(route2, localDate2));

        return Stream.of(
                Arguments.of(new Reservation(user1, flights)),
                Arguments.of(new Reservation(user2, flights)),
                Arguments.of(new Reservation(user3, flights))
        );
    }

    public static boolean equals(Reservation reservation1, Reservation reservation2) {
        var flights = reservation2.getFlights();

        return reservation1.id.equals(reservation2.id) &&
                reservation1.getClient().getUsername().equals(reservation2.getClient().getUsername());
                //&& reservation1.getFlights().stream().allMatch(flights::remove) && flights.size() == 0;
                //reservation1.getFlights().equals(reservation2.getFlights());
    }

    @ParameterizedTest
    @MethodSource("reservations")
    void serializeAndDeserialize(Reservation reservation) {
        byte[] bytes = reservation.serialize();
        Reservation r = Reservation.deserialize(bytes);

        System.out.println(reservation);
        System.out.println(r);

        Assertions.assertTrue(equals(r, reservation));
    }
}
