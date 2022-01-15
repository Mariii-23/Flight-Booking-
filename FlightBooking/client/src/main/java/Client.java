import airport.Reservation;
import airport.Route;
import connection.TaggedConnection;
import exceptions.AlreadyLoggedInException;
import exceptions.NotLoggedInException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static connection.TaggedConnection.Frame;
import static java.lang.System.out;
import static request.RequestType.*;

public class Client implements Runnable {
    private static final Logger logger = LogManager.getLogger(Client.class);

    private static final String host = "localhost";
    private static final int PORT = 12345; // TODO: Mudar isto dps!

    private final TaggedConnection taggedConnection;
    private final Scanner in; // From console
    private boolean logged_in;

    public Client() throws IOException {
        this.taggedConnection = new TaggedConnection(new Socket(host, PORT)); // TODO: Repetir a conexão caso o server não esteja ligado.
        this.in = new Scanner(System.in);
        this.logged_in = false;
    }

    public void run() {
        try {
            boolean quit = false;
            while (!quit) {
                try {
                    out.print(getMenu());
                    int option = Integer.parseInt(in.nextLine());
                    switch (getRequestType(option)) {
                        case REGISTER -> register();
                        case LOGIN -> login();
                        case EXIT -> {
                            quit();
                            quit = true;
                        }

                        case CANCEL_DAY -> cancelDay();
                        case INSERT_ROUTE -> insertRoute();

                        case GET_ROUTES -> getRoutes();
                        case GET_RESERVATIONS -> getReservations();
                        case RESERVE -> reserve();
                        case CANCEL_RESERVATION -> cancelReservation();
                    }
                    out.println();
                } catch (Exception e) {
                    out.println(e.getMessage() + '\n');
                }
            }

            taggedConnection.close();
        } catch (NotLoggedInException e) {
            out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getReservations() throws IOException {
        taggedConnection.send(GET_RESERVATIONS.ordinal(), new ArrayList<>());
    }

    private void quit() throws IOException {
        taggedConnection.send(EXIT.ordinal(), new ArrayList<>());
    }

    /**
     * Sends one request to the server with the username and password.
     * Then, it waits for a response, and handles it.
     */
    private void login() throws IOException, AlreadyLoggedInException {
        if (logged_in) throw new AlreadyLoggedInException();

        out.print("Insert username: ");
        String username = in.nextLine();
        out.print("Insert password: ");
        String password = in.nextLine();

        List<byte[]> args = new ArrayList<>();
        args.add(username.getBytes(StandardCharsets.UTF_8));
        args.add(password.getBytes(StandardCharsets.UTF_8));

        taggedConnection.send(LOGIN.ordinal(), args);

        Frame response = taggedConnection.receive();

        if (checkError(response)) printError(response);
        else {
            out.println("Logged in!");
            logged_in = true;
        }
    }

    private void cancelReservation() throws NotLoggedInException, IOException {
        if (!logged_in) throw new NotLoggedInException();

        out.print("Insert the id of the reservation: ");
        String uuid = in.nextLine();

        List<byte[]> list = new ArrayList<>();
        list.add(uuid.getBytes(StandardCharsets.UTF_8));
        taggedConnection.send(CANCEL_RESERVATION.ordinal(), list);

        Frame response = taggedConnection.receive();

        if (checkError(response)) printError(response);
        else {
            out.println("Cancel reservation with success!");
            out.println(Reservation.deserialize(response.data().get(0)));
        }
    }

    private void reserve() throws NotLoggedInException, IOException {
        if (!logged_in) throw new NotLoggedInException();

        List<byte[]> list = new ArrayList<>();

        out.print("Insert the number of the cities: "); // TODO: Melhorar a mensagem disto
        int num = Integer.parseInt(in.nextLine());

        for (int i = 0; i < num; i++) {
            out.print("Please insert the next city: ");
            String city = in.nextLine();
            list.add(city.getBytes(StandardCharsets.UTF_8));
        }

        out.print("Insert the start date with the following format \"2007-12-03\": ");
        LocalDate start = LocalDate.parse(in.nextLine());
        list.add(start.toString().getBytes(StandardCharsets.UTF_8));

        out.print("Insert the end date with the following format \"2007-12-03\": ");
        LocalDate end = LocalDate.parse(in.nextLine());
        list.add(end.toString().getBytes(StandardCharsets.UTF_8));

        taggedConnection.send(RESERVE.ordinal(), list);

        Frame response = taggedConnection.receive();

        if (checkError(response)) printError(response);
        else {
            out.println("\nReserve with success!");
            out.println("Reservation id: " + new String(response.data().get(0), StandardCharsets.UTF_8));
        }
    }

    private void getRoutes() throws NotLoggedInException, IOException {
        if (!logged_in) throw new NotLoggedInException();

        List<byte[]> list = new ArrayList<>();
        taggedConnection.send(GET_ROUTES.ordinal(), list);

        Frame response = taggedConnection.receive();

        if (checkError(response)) printError(response);
        else {
            logger.info("Get routes with success!");
            response.data().stream().map(Route::deserialize).forEach(out::println);
        }
        // TODO:
    }

    private void insertRoute() throws IOException, NotLoggedInException {
        if (!logged_in) throw new NotLoggedInException();

        out.print("Insert origin route: ");
        String origin = in.nextLine();

        out.print("Insert destiny route: ");
        String destiny = in.nextLine();

        out.print("Insert capacity of the route: ");
        int capacity = Integer.parseInt(in.nextLine());

        List<byte[]> list = new ArrayList<>();

        list.add(origin.getBytes(StandardCharsets.UTF_8));
        list.add(destiny.getBytes(StandardCharsets.UTF_8));
        list.add(ByteBuffer.allocate(Integer.BYTES).putInt(capacity).array());

        taggedConnection.send(INSERT_ROUTE.ordinal(), list);

        Frame response = taggedConnection.receive();

        if (checkError(response)) printError(response);
        else out.println("Route successfully inserted!");
    }

    private void cancelDay() throws IOException, NotLoggedInException {
        if (!logged_in) throw new NotLoggedInException();
        List<byte[]> list = new ArrayList<>();

        out.print("Insert the end date with the following format \"2007-12-03\": ");
        LocalDate day = LocalDate.parse(in.nextLine());
        list.add(day.toString().getBytes(StandardCharsets.UTF_8));

        taggedConnection.send(CANCEL_DAY.ordinal(), list);

        Frame response = taggedConnection.receive();

        if (checkError(response)) printError(response);
        else out.println("Day successfully cancelled!");
        // TODO:
    }

    private void register() throws IOException, AlreadyLoggedInException {
        if (logged_in) throw new AlreadyLoggedInException();

        out.print("Insert username: ");
        String username = in.nextLine();

        out.print("Insert password: ");
        String password = in.nextLine();

        List<byte[]> list = new ArrayList<>();
        list.add(username.getBytes(StandardCharsets.UTF_8));
        list.add(password.getBytes(StandardCharsets.UTF_8));

        taggedConnection.send(REGISTER.ordinal(), list);

        Frame response = taggedConnection.receive();

        if (checkError(response)) printError(response);
        else out.println("Account successfully registered!");
    }

    private boolean checkError(Frame frame) {
        return new String(frame.data().get(0)).equals("ERROR");
    }

    private void printError(Frame frame) {
        // out.println("ERROR!");
        out.println();
        for (int i = 1; i < frame.data().size(); i++) {
            out.println(new String(frame.data().get(i)));
        }
        // out.println("Error with the request: " + frame.data().stream().map(String::new).collect(Collectors.joining(" ")));
    }

}