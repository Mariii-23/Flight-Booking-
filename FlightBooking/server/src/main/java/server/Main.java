package server;


import exceptions.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import system.AirportSystem;
import system.IAirportSystem;
import users.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class Main {
    public static final int PORT = 12345;
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final int NTHREADS = 50;
    @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal", "FieldCanBeLocal"})
    private static boolean running = true;

    private static IAirportSystem initState() throws UsernameAlreadyExistsException, RouteDoesntExistException, RouteAlreadyExistsException, BookingFlightsNotPossibleException, UserNotFoundException {
        IAirportSystem iAirportSystem = new AirportSystem();

        iAirportSystem.registerAdmin("admin", "admin");
        User user =  iAirportSystem.registerClient("1", "1");
        logger.info("Client with success with id: " + user.getUsername());
        iAirportSystem.addRoute("Porto", "Lisbon", 200);
        iAirportSystem.addRoute("Lisbon", "London", 200);
        iAirportSystem.addRoute("London", "Faro", 200);

        List<String> cities = new ArrayList<>();
        cities.add("Porto");
        cities.add("Lisbon");
        cities.add("London");

        UUID id = iAirportSystem.reserveFlight(user.getUsername(), cities, LocalDate.now(), LocalDate.now().plusDays(10));
        logger.info("Reservation with success with id: " + id);
        return iAirportSystem;
    }

    public static void main(String[] args) throws IOException, UsernameAlreadyExistsException, RouteDoesntExistException, RouteAlreadyExistsException, BookingFlightsNotPossibleException, UserNotFoundException {
        IAirportSystem iAirportSystem = initState();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("ServerSocket starting...");
            var pool = Executors.newFixedThreadPool(NTHREADS); // TODO: Fazer isto manualmente

            while (running) pool.execute(new ClientHandler(serverSocket.accept(), iAirportSystem));
        }
        logger.info("ServerSocket closing...");
    }

}
