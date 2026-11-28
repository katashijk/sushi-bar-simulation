package pl.edu.pwr.student.dmarchut.jp.threads;

import pl.edu.pwr.student.dmarchut.jp.model.DishType;
import pl.edu.pwr.student.dmarchut.jp.model.SimulationConfig;
import pl.edu.pwr.student.dmarchut.jp.sync.SushiBelt;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/** Spawner thread starting the engine, chefs, and periodic customer threads */
public class Creator implements Runnable {
    private final SushiBelt belt;
    private final SimulationConfig config;
    private final List<Thread> managedThreads = new CopyOnWriteArrayList<>();

    public Creator(SushiBelt belt, SimulationConfig config) {
        this.belt = belt;
        this.config = config;
    }

    /** Starts engine and chef threads, then spawns customers onto free seats */
    @Override
    public void run() {
        Thread engineThread = new Thread(new BeltEngine(belt, config), "Engine");
        managedThreads.add(engineThread);
        engineThread.start();

        for(int i=0; i<config.chefCount(); i++) {
            String chefLabel = String.valueOf((char) ('X' + (i % 26)));
            Thread chefThread = new Thread(new Chef(i, belt, config), "Chef-" + chefLabel);

            managedThreads.add(chefThread);
            chefThread.start();
        }

        int customerCounter = 1;
        try {
            while (belt.isRunning() && !Thread.currentThread().isInterrupted()) {
                Queue<DishType> wishlist = generateWishList();

                int seatIndex = belt.tryOccupySeat(wishlist.size(), wishlist.peek());
                if(seatIndex != -1) {
                    char seatLabel = (char) ('a' + (seatIndex % 26));
                    String customerLabel = "Customer-" + customerCounter++ + "(" + seatLabel + ")";
                    Thread customerThread = new Thread(new Customer(seatIndex, wishlist, belt, config), customerLabel);

                    managedThreads.add(customerThread);
                    customerThread.start();
                }

                managedThreads.removeIf(t -> t.getState() == Thread.State.TERMINATED);
                Thread.sleep(config.customerSpawnDelay());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Generates a random ordered queue of dishes for a new customer */
    private Queue<DishType> generateWishList() {
        int min = config.dishesPerCustomerMin();
        int max = config.dishesPerCustomerMax();
        int count = ThreadLocalRandom.current().nextInt(min, max + 1);

        Queue<DishType> list = new ArrayDeque<>(count);
        for(int i=0; i<count; i++) {
            list.add(DishType.random());
        }

        return list;
    }

    /** Returns the list of currently tracked simulation threads */
    public List<Thread> getManagedThreads() {
        return managedThreads;
    }

    /** Shuts down the belt and interrupts all managed threads */
    public void stopAll() {
        belt.shutdown();
        for(Thread t : managedThreads) {
            t.interrupt();
        }
    }
}
