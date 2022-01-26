package airport;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class RouteTest {

    @BeforeAll
    static void startTest() {
        System.out.println("Starting test");
    }

    @AfterAll
    static void endTest() {
        System.out.println("Ending test");
    }

    private static Stream<Arguments> routes() {
        return Stream.of(
                Arguments.of(new Route("London", "Paris", 200)),
                Arguments.of(new Route("Lisbon", "Porto", 100)),
                Arguments.of(new Route("Porto", "Lisbon", 100)),
                Arguments.of(new Route("Paris", "London", 500))
        );
    }

    @ParameterizedTest
    @MethodSource("routes")
    void serializeAndDeserialize(Route route) {
        byte[] bytes = route.serialize();
        Route r = Route.deserialize(bytes);

        Assertions.assertEquals(r, route);
    }

}
