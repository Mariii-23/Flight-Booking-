package client;

import airport.PossiblePath;
import airport.Reservation;
import airport.Route;
import connection.TaggedConnection;
import exceptions.AlreadyLoggedInException;
import exceptions.NotLoggedInException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import users.Notification;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.out;
import static request.RequestType.*;

public class Client implements Runnable {
    private static final Logger logger = LogManager.getLogger(Client.class);

    private static final String host = "localhost";
    private static final int PORT = 12345; // TODO: Mudar isto dps!

    private final Demultiplexer demultiplexer;
    private final Scanner in; // From console
    private boolean logged_in;

    private final Queue<String> pendingNotifications = new ArrayDeque<>();

    public Client() throws IOException {
        this.demultiplexer = new Demultiplexer(new TaggedConnection(new Socket(host, PORT))); // TODO: Repetir a conexão caso o server não esteja ligado.
        this.in = new Scanner(System.in);
        this.logged_in = false;
        demultiplexer.start();
    }

    public void run() {
        try {
            boolean quit = false;
            while (!quit) {
                try {
                    out.print(getMenu());
                    int option = Integer.parseInt(in.nextLine());
                    switch (getRequestType(option)) {
                        case REGISTER -> registerIO();
                        case LOGIN -> loginIO();
                        case EXIT -> {
                            quit();
                            quit = true;
                        }

                        case CANCEL_DAY -> cancelDayIO();
                        case INSERT_ROUTE -> insertRouteIO();

                        case GET_ROUTES -> getRoutes();
                        case GET_RESERVATIONS -> getReservations();
                        case GET_PATHS_BETWEEN -> getPathsBetweenIO();
                        case RESERVE -> reserveIO();
                        case CANCEL_RESERVATION -> cancelReservationIO();
                        case LOGOUT -> logout();
                        case CHANGE_PASSWORD -> changePasswordIO();

                        case GET_NOTIFICATION -> getNotifications();
                    }
                    out.println();
                } catch (Exception e) {
                    out.println(e.getMessage() + '\n');
                }
            }

            demultiplexer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logout() throws IOException, InterruptedException {
        int tag = LOGOUT.ordinal();
        demultiplexer.send(tag, null);
        var response = demultiplexer.receive(tag);

        if (checkError(response)) printError(response);
        else {
            out.println("Logged out!");
            logged_in = false;
        }
    }

    public void getReservations() throws IOException, InterruptedException {
        int tag = GET_RESERVATIONS.ordinal();
        demultiplexer.send(tag, null);
        var response = demultiplexer.receive(tag);

        out.println("Reservations: ");
        response.stream().map(Reservation::deserialize).forEach(out::println);
    }

    public void quit() throws IOException {
        demultiplexer.send(EXIT.ordinal(), null);
    }

    private void loginIO() throws AlreadyLoggedInException, IOException, InterruptedException {
        if (logged_in) throw new AlreadyLoggedInException();

        out.print("Insert username: ");
        String username = in.nextLine();
        out.print("Insert password: ");
        String password = in.nextLine();
        login(username, password);
    }

    /**
     * Sends one request to the server with the username and password.
     * Then, it waits for a response, and handles it.
     */
    public void login(String username, String password) throws IOException, InterruptedException {
        List<byte[]> args = new ArrayList<>(2);

        args.add(username.getBytes(StandardCharsets.UTF_8));
        args.add(password.getBytes(StandardCharsets.UTF_8));

        int tag = LOGIN.ordinal();
        demultiplexer.send(tag, args);
        var response = demultiplexer.receive(tag);
        if (checkError(response)) printError(response);
        else {
            out.println("Logged in!");
            logged_in = true;
        }
    }

    private void cancelReservationIO() throws NotLoggedInException, IOException, InterruptedException {
        if (!logged_in) throw new NotLoggedInException();

        out.print("Insert the id of the reservation: ");
        String id = in.nextLine();

        cancelReservation(UUID.fromString(id));
    }

    public void cancelReservation(UUID reservationId) throws IOException, InterruptedException {
        List<byte[]> list = new ArrayList<>(1);
        list.add(reservationId.toString().getBytes(StandardCharsets.UTF_8));

        int tag = CANCEL_RESERVATION.ordinal();
        demultiplexer.send(tag, list);
        var response = demultiplexer.receive(tag);

        if (checkError(response)) printError(response);
        else {
            logger.info("Reservation cancelled with success!");
            logger.info(Reservation.deserialize(response.get(0)));
        }
    }


    private void reserveIO() throws NotLoggedInException, IOException, InterruptedException {
        if (!logged_in) throw new NotLoggedInException();

        List<String> cities = new ArrayList<>();

        out.print("Insert the number of the cities: "); // TODO: Melhorar a mensagem disto
        int num = Integer.parseInt(in.nextLine());

        for (int i = 0; i < num; i++) {
            out.print("Please insert the next city: ");
            String city = in.nextLine();
            cities.add(city);
        }

        out.print("Insert the start date with the following format \"2007-12-03\": ");
        LocalDate start = LocalDate.parse(in.nextLine());

        out.print("Insert the end date with the following format \"2007-12-03\": ");
        LocalDate end = LocalDate.parse(in.nextLine());

        reserve(cities, start, end);
    }

    public void reserve(List<String> cities, LocalDate start, LocalDate end) throws IOException, InterruptedException {
        List<byte[]> list = new ArrayList<>(cities.stream().map(str -> str.getBytes(StandardCharsets.UTF_8)).toList());

        list.add(start.toString().getBytes(StandardCharsets.UTF_8));
        list.add(end.toString().getBytes(StandardCharsets.UTF_8));

        int tag = RESERVE.ordinal();
        demultiplexer.send(tag, list);

        new Thread(() -> {
            try {
                var response = demultiplexer.receive(tag);

                if (checkError(response)) {
                    logger.info(response);
                    pendingNotifications.add("Error making reservation: " + cities);
                } else {
                    logger.info("\nReserve with success!");
                    UUID id = UUID.fromString(new String(response.get(0), StandardCharsets.UTF_8));
                    logger.info("Reservation id: " + id);
                    pendingNotifications.add("Made reservation with ID " + id + ": " + cities);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    protected void getRoutes() throws NotLoggedInException, IOException, InterruptedException {
        if (!logged_in) throw new NotLoggedInException();

        int tag = GET_ROUTES.ordinal();
        demultiplexer.send(tag, null);
        var response = demultiplexer.receive(tag);

        if (checkError(response)) printError(response);
        else {
            logger.info("Get routes with success!");
            response.stream().map(Route::deserialize).forEach(out::println);
        }
    }

    private void getPathsBetweenIO() throws NotLoggedInException, IOException, InterruptedException {
        if (!logged_in) throw new NotLoggedInException();

        out.print("Insert origin: ");
        String origin = in.nextLine();
        out.print("Insert destination: ");
        String destination = in.nextLine();


        List<PossiblePath> paths = getPathsBetween(origin, destination);
        if (paths != null)
            paths.forEach(e -> out.println(e.toStringPretty("")));
        else
            out.println("Path not found!");
    }

    public List<PossiblePath> getPathsBetween(String origin, String destination) throws IOException, InterruptedException {
        List<byte[]> args = new ArrayList<>(2);

        args.add(origin.getBytes(StandardCharsets.UTF_8));
        args.add(destination.getBytes(StandardCharsets.UTF_8));

        int tag = GET_PATHS_BETWEEN.ordinal();
        demultiplexer.send(tag, args);
        var response = demultiplexer.receive(tag);

        if (checkError(response)) printError(response);
        else {
            logger.info("Get Possible Path with success!");
            return response.stream().map(PossiblePath::deserialize).collect(Collectors.toList());
        }
        return null;
    }

    private void insertRouteIO() throws NotLoggedInException, IOException, InterruptedException {
        if (!logged_in) throw new NotLoggedInException();

        out.print("Insert origin route: ");
        String origin = in.nextLine();

        out.print("Insert destination route: ");
        String destination = in.nextLine();

        out.print("Insert capacity of the route: ");
        int capacity = Integer.parseInt(in.nextLine());

        insertRoute(origin, destination, capacity);
    }

    public void insertRoute(String origin, String destination, int capacity) throws IOException, InterruptedException {
        List<byte[]> list = new ArrayList<>(3);

        list.add(origin.getBytes(StandardCharsets.UTF_8));
        list.add(destination.getBytes(StandardCharsets.UTF_8));
        list.add(ByteBuffer.allocate(Integer.BYTES).putInt(capacity).array());

        int tag = INSERT_ROUTE.ordinal();
        demultiplexer.send(tag, list);
        var response = demultiplexer.receive(tag);

        if (checkError(response)) printError(response);
        else logger.info("Route successfully inserted!");
    }

    private void cancelDayIO() throws NotLoggedInException, IOException, InterruptedException {
        if (!logged_in) throw new NotLoggedInException();

        out.print("Insert the end date with the following format \"2007-12-03\": ");
        LocalDate day = LocalDate.parse(in.nextLine());

        cancelDay(day);
    }

    public void cancelDay(LocalDate day) throws IOException, InterruptedException {
        List<byte[]> list = new ArrayList<>(1);

        list.add(day.toString().getBytes(StandardCharsets.UTF_8));

        int tag = CANCEL_DAY.ordinal();
        demultiplexer.send(tag, list);
        var response = demultiplexer.receive(tag);

        if (checkError(response)) printError(response);
        else logger.info("Day successfully cancelled!");
    }

    private void registerIO() throws IOException, AlreadyLoggedInException, InterruptedException {
        if (logged_in) throw new AlreadyLoggedInException();

        out.print("Insert username: ");
        String username = in.nextLine();

        out.print("Insert password: ");
        String password = in.nextLine();

        register(username, password);
    }

    public void register(String username, String password) throws IOException, InterruptedException {
        List<byte[]> list = new ArrayList<>(2);

        list.add(username.getBytes(StandardCharsets.UTF_8));
        list.add(password.getBytes(StandardCharsets.UTF_8));

        int tag = REGISTER.ordinal();
        demultiplexer.send(tag, list);
        var response = demultiplexer.receive(tag);

        if (checkError(response)) printError(response);
        else logger.info("Account successfully registered!");
    }


    private void changePasswordIO() throws NotLoggedInException, IOException, InterruptedException {
        if (!logged_in) throw new NotLoggedInException();

        out.print("Insert new password: ");
        String password = in.nextLine();

        changePassword(password);
    }

    public void changePassword(String password) throws IOException, InterruptedException {
        List<byte[]> list = new ArrayList<>(1);

        list.add(password.getBytes(StandardCharsets.UTF_8));

        int tag = CHANGE_PASSWORD.ordinal();
        demultiplexer.send(tag, list);
        var response = demultiplexer.receive(tag);

        if (checkError(response)) printError(response);
        else logger.info("Password successfully changed!");

    }

    public void getNotifications() throws IOException, InterruptedException, NotLoggedInException {
        if (!logged_in) throw new NotLoggedInException();

        int tag = GET_NOTIFICATION.ordinal();
        demultiplexer.send(tag, null);
        var response = demultiplexer.receive(tag);

        if (checkError(response)) printError(response);
        else {
            logger.info("Got Notifications successfully!");
            out.println("General Notifications:");
            try {
                response.stream().map(Notification::deserialize).forEach(out::println);
            } catch (Exception ignored) {
            }
        }
        out.println("\nReservation Notifications:");

        while (!pendingNotifications.isEmpty())
            out.println(pendingNotifications.remove());
    }

    protected boolean checkError(List<byte[]> response) {
        return new String(response.get(0)).equals("ERROR");
    }

    private void printError(List<byte[]> response) {
        out.println();
        for (int i = 1; i < response.size(); i++) {
            out.println(new String(response.get(i)));
        }
        // out.println("Error with the request: " + frame.data().stream().map(String::new).collect(Collectors.joining(" ")));
    }

}