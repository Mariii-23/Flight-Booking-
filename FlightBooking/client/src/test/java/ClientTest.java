import client.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Server has to running in order to run this class test.
 */
class ClientTest {
    private static final Consumer<Client> reserve = client -> {
        List<String> cities = new ArrayList<>();

        var city1 = "London";
        var city2 = "Faro";

        cities.add(city1);
        cities.add(city2);

        LocalDate start = LocalDate.of(2022, 5, 12);
        LocalDate end = LocalDate.of(2022, 8, 17);

        Assertions.assertDoesNotThrow(() -> client.reserve(cities, start, end));
    };
    private static final Consumer<Client> insertRoute = client -> {
        Random random = new Random();
        var city1 = "London" + Math.abs(random.nextInt() / 10000);
        var city2 = "Faro" + Math.abs(random.nextInt() / 10000);
        var capacity = Math.abs(random.nextInt() / 10000);

        Assertions.assertDoesNotThrow(() -> client.insertRoute(city1, city2, capacity));
    };
    private final int N_THREADS = 100;
    private final Thread[] threads = new Thread[N_THREADS];

    private static Stream<Arguments> methods() {
        return Stream.of(
                Arguments.of("1", "1", reserve),
                Arguments.of("admin", "admin", insertRoute)
        );
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @ParameterizedTest
    @MethodSource("methods")
    void logged(String username, String password, Consumer<Client> clientConsumer) {
        for (int i = 0; i < N_THREADS; i++) {
            threads[i] = new Thread(() -> {
                try {
                    Client client = new Client();
                    Assertions.assertDoesNotThrow(() -> {
                                client.login(username, password);

                                clientConsumer.accept(client);

                                client.quit();
                            }
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        for (Thread t : threads) {
            t.start();
        }

        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}