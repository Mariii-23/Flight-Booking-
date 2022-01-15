package system;

import airport.Reservation;
import exceptions.*;
import org.junit.jupiter.api.*;
import system.AirportSystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;


class AirportSystemThreadTest {

    // Used to test
    private final String username = "admin";
    private AirportSystem airportSystem;
    // Used to test.
    private LocalDate date;

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
    private void initRoutes_LondonParisLisbon()  {
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

    /**
     * Private method to initialize airport system.
     * Capacity of each flight is 1.
     */
    private void initRoutes_LondonParisLisbon(int routeCapacity)  {
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


    // -------------------- Add Route -------------------
    @org.junit.jupiter.api.Test
    void testMultipleReservations(){
        int N = 20;
        int numberDays = 3;
        int routeCapacity = 2;
        initUser();
        initRoutes_LondonParisLisbon(2);
        Thread[] threads = new Thread[N];
        makeReservation makeReservation = new makeReservation(airportSystem, numberDays);

        for (int i = 0; i < N; i++){
            threads[i] = new Thread(makeReservation);
        }
        for (Thread t : threads){
            t.start();
        }
        try {
            for (Thread t : threads){
                t.join();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        assert makeReservation.reservationSucceed == routeCapacity * numberDays;
    }

    private class makeReservation implements Runnable {
        private AirportSystem airportSystem;

        protected int reservationSucceed;
        protected int numberDays;
        private ReentrantLock lock;

        public makeReservation(AirportSystem airportSystem, int numberDays) {
            this.airportSystem = airportSystem;
            this.reservationSucceed = 0;
            this.numberDays = numberDays;
            lock = new ReentrantLock();
        }

        public void success() {
            try {
                lock.lock();
                reservationSucceed++;
            } finally {
                lock.unlock();
            }
        }

        public void run() {
            List<String> cities1 = new ArrayList<>(Arrays.asList("Paris", "Lisbon"));
            try {
                UUID id = airportSystem.reserveFlight(username, cities1, date, date.plusDays(numberDays - 1));
                success();
            } catch (Exception e) {
            }
        }
    }
}

