import exceptions.DayAlreadyCanceledException;
import exceptions.RouteAlreadyExistsException;
import exceptions.RouteDoesntExistException;
import exceptions.UsernameAlreadyExistsException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


class AirportSystemThreadTest {

    // Used to test
    private final String username = "admin";
    protected int testParameter;
    private AirportSystem airportSystem;
    // Used to test.
    private LocalDate date;
    //Used to test
    private ReentrantLock testLock;
    //Test parameters, to test with different values.
    private int N = 20;
    private int numberDays = 3;
    private int routeCapacity = 2;
    private Thread[] threads;


    @BeforeAll
    static void startTest() {
        System.out.println("Starting test with threads");
    }

    @AfterAll
    static void endTest() {
        System.out.println("Ending test");
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        airportSystem = new AirportSystem();
        date = LocalDate.now();
        testParameter = 0;
        testLock = new ReentrantLock();
        initUser();
        initRoutes_LondonParisLisbon(routeCapacity);
        threads = new Thread[N];
        //System.out.println("---- TEST ----");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        //System.out.println("---------------");
    }

    /**
     * Private method to initialize airport system.
     * Capacity of each flight is 1.
     */
    private void initRoutes_LondonParisLisbon() {
        //            System.out.println(airportSystem.getReservation(reservation).toString());
        try {
            airportSystem.addRoute("London", "Paris", 2);
            airportSystem.addRoute("Paris", "Lisbon", 3);
        } catch (RouteAlreadyExistsException e) {
            e.printStackTrace();
        } catch (RouteDoesntExistException e) {
            e.printStackTrace();
        }
    }

    private void generalSuccess() {
        try {
            testLock.lock();
            testParameter++;
        } finally {
            testLock.unlock();
        }
    }

    private void mixThreadsAndRun(List<Thread> threads) {
        Collections.shuffle(threads);
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

    /**
     * Private method to initialize airport system.
     * Capacity of each flight is 1.
     */
    private void initRoutes_LondonParisLisbon(int routeCapacity) {
        //            System.out.println(airportSystem.getReservation(reservation).toString());
        try {
            airportSystem.addRoute("London", "Paris", routeCapacity);
            airportSystem.addRoute("Paris", "Lisbon", routeCapacity);
        } catch (RouteAlreadyExistsException e) {
            e.printStackTrace();
        } catch (RouteDoesntExistException e) {
            e.printStackTrace();
        }
    }

    private void initUser() {
        try {
            airportSystem.registerAdmin(username, username);
        } catch (UsernameAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

    private void initUserAndRoutes_LondonParisLisbon() {
        initUser();
        initRoutes_LondonParisLisbon();
    }


    // -------------------- Multiple reservations Route -------------------
    @org.junit.jupiter.api.Test
    void testMultipleReservations() {
        MakeReservation makeReservation = new MakeReservation(airportSystem, numberDays, new TreeSet<>(), true);

        for (int i = 0; i < N; i++) {
            threads[i] = new Thread(makeReservation);
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
        assert makeReservation.reservationSucceed == routeCapacity * numberDays;
        assert testParameter == routeCapacity * numberDays;
    }

    /**
     * Reserves flights and dele days at a random order.
     */
    @org.junit.jupiter.api.Test
    void testCancelDayAndReservations() {
        System.out.println("Boa sorte");
        int Nreservations = 20;
        int numberDays = 2;
        int routeCapacity = 5;
        List<Thread> threads = new ArrayList<Thread>();
        MakeReservation makeReservation = new MakeReservation(airportSystem, numberDays, new TreeSet<>(), false);

        for (int i = 0; i < Nreservations; i++) {
            threads.add(new Thread(makeReservation));
        }
        for (int i = 0; i < numberDays; i++) {
            threads.add(new Thread(new CancelDay(airportSystem, i)));
        }
        mixThreadsAndRun(threads);
    }

    // ------------------ Teste cancel day ----------------------

    /**
     * Reserves flights and dele days at a random order.
     */
    @org.junit.jupiter.api.Test
    void testCancelReservationAfterCancelDay() {
        System.out.println("Boa sorte outra vez");
        int Nreservations = 20;
        List<Thread> threads = new ArrayList<Thread>();
        Set<UUID> reservas = new TreeSet<>();
        MakeReservation makeReservation = new MakeReservation(airportSystem, numberDays, reservas, false);
        List<Thread> threadsCancelFlights = new ArrayList<Thread>();

        for (int i = 0; i < Nreservations; i++) {
            threads.add(new Thread(makeReservation));
        }
        for (int i = 0; i < numberDays; i++) {
            threads.add(new Thread(new CancelDay(airportSystem, i)));
        }
        mixThreadsAndRun(threads);
        System.out.println("Existem " + reservas.size() + " para cancelar");
        //Cancela viagens pelas reservas
        for (UUID reservation : reservas) {
            threadsCancelFlights.add(new Thread(new CancelFlight(airportSystem, reservation)));
        }
        for (Thread toCancel : threadsCancelFlights)
            toCancel.start();
        try {
            for (Thread t : threadsCancelFlights) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assert testParameter == reservas.size();

    }

    //----------------------------A test to test tests. ----------------------
    @org.junit.jupiter.api.Test
    void partyTest() {
        System.out.println("\n\nParty test\n\n");
        List<Thread> threads = new ArrayList<Thread>();
        int numberReservations = 10;
        MakeReservation makeReservation = new MakeReservation(airportSystem, numberDays, new TreeSet<>(), true);

        //It will add much more.
        int numberInsertRoutes = 10;
        //This number will serve to the range of days of reservations.
        int cancelDays = numberDays;
        int registerClients = 10;

        for (int i = 0; i < numberInsertRoutes; i++) {
            threads.add(new Thread(new InsertRoutes(airportSystem, numberInsertRoutes)));
        }
        for (int i = 0; i < numberReservations; i++) {
            threads.add(new Thread(makeReservation));
        }

        for (int i = 0; i < numberDays; i++) {
            threads.add(new Thread(new CancelDay(airportSystem, i)));
        }
        for (int i = 0; i < registerClients; i++) {
            threads.add(new Thread(new RegisterClient(airportSystem, registerClients)));
        }

        mixThreadsAndRun(threads);

        //As duas rotas são as Lisboa - Paris - Londres.
        assert (airportSystem.getRoutes().size() == numberInsertRoutes * numberInsertRoutes + 2);
        assert ((int) airportSystem.numberClients() == registerClients * registerClients);
        assert (airportSystem.numberCanceledDays() == numberDays);
        System.out.println("\n\n\nEnd Party\n\n\n");
    }

    //--------------Tenta cancelar reservas após dias cancelados -----
    //Deve ter 0 reservas canceladas com sucesso

    private class MakeReservation implements Runnable {
        protected int reservationSucceed;
        protected int numberDays;
        protected Set<UUID> reserves;
        boolean addTestParameter;
        private AirportSystem airportSystem;
        private ReentrantLock lock;

        public MakeReservation(AirportSystem airportSystem, int numberDays, Set<UUID> reservations, boolean flagTestParameter) {
            this.airportSystem = airportSystem;
            this.reservationSucceed = 0;
            this.numberDays = numberDays;
            lock = new ReentrantLock();
            this.reserves = reservations;
            addTestParameter = flagTestParameter;
        }

        public void success(UUID reservationCode) {
            try {
                lock.lock();
                reservationSucceed++;
                reserves.add(reservationCode);
            } finally {
                lock.unlock();
            }
        }

        public void run() {
            List<String> cities1 = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));
            try {
                UUID id = airportSystem.reserveFlight(username, cities1, date, date.plusDays(numberDays - 1));
                System.out.println("Reserva voo");
                success(id);
                if (addTestParameter) generalSuccess();
            } catch (Exception e) {
            }
        }
    }

    private class CancelDay implements Runnable {
        protected int dayToBeCanceled;
        private AirportSystem airportSystem;

        public CancelDay(AirportSystem airportSystem, int dayToBeCanceled) {
            this.airportSystem = airportSystem;
            this.dayToBeCanceled = dayToBeCanceled;
        }

        public void run() {
            try {
                System.out.println("Cancela dia " + date.plusDays(dayToBeCanceled));
                airportSystem.cancelDay(date.plusDays(dayToBeCanceled));
                System.out.println("Dia cancelado" + date.plusDays(dayToBeCanceled));
            } catch (DayAlreadyCanceledException e) {
                System.out.println("Dia já cancelado");
            }

        }
    }

    private class CancelFlight implements Runnable {
        protected int numberCancelations;
        protected UUID toCancel;
        private AirportSystem airportSystem;
        private ReentrantLock lock;

        public CancelFlight(AirportSystem airportSystem, UUID toCancel) {
            this.airportSystem = airportSystem;
            this.toCancel = toCancel;
        }


        public void run() {
            try {
                System.out.println("Cancela voo ");

                airportSystem.cancelReservation(username, toCancel);
                throw new InterruptedException();
            } catch (Exception e) {
                //É suposto nenhuma reserva ser cancelada,
                // porque todas são inválidas, visto que os dias foram cancelados.
                generalSuccess();
            }
        }
    }

    private class InsertRoutes implements Runnable {
        private AirportSystem airportSystem;
        private ReentrantLock lock;
        private int numberInsertions;

        public InsertRoutes(AirportSystem airportSystem, int numberInsertions) {
            this.airportSystem = airportSystem;
            this.numberInsertions = numberInsertions;
        }


        public void run() {
            try {
                byte[] array = new byte[7];
                for (int i = 0; i < numberInsertions; i++) {
                    new Random().nextBytes(array);
                    String orig = new String(array, Charset.forName("UTF-8"));

                    new Random().nextBytes(array);
                    String dest = new String(array, Charset.forName("UTF-8"));

                    System.out.println("Insert route ");

                    airportSystem.addRoute(orig, dest, 2);
                }
            } catch (Exception e) {
                //É suposto nenhuma reserva ser cancelada,
                // porque todas são inválidas, visto que os dias foram cancelados.
                generalSuccess();
            }
        }
    }

    private class RegisterClient implements Runnable {
        private AirportSystem airportSystem;
        private int numberRepetitions;

        public RegisterClient(AirportSystem airportSystem, int numberRepetitions) {
            this.airportSystem = airportSystem;
            this.numberRepetitions = numberRepetitions;
        }


        public void run() {
            try {
                byte[] array = new byte[7];
                for (int i = 0; i < numberRepetitions; i++) {
                    new Random().nextBytes(array);
                    String username = new String(array, Charset.forName("UTF-8"));

                    new Random().nextBytes(array);
                    String password = new String(array, Charset.forName("UTF-8"));

                    System.out.println("Insert client ");

                    airportSystem.registerClient(username, password);
                }
            } catch (Exception e) {
                //É suposto nenhuma reserva ser cancelada,
                // porque todas são inválidas, visto que os dias foram cancelados.
                generalSuccess();
            }
        }
    }

}
