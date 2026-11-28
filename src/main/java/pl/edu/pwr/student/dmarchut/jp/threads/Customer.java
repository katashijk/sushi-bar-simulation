package pl.edu.pwr.student.dmarchut.jp.threads;

import pl.edu.pwr.student.dmarchut.jp.model.DishType;
import pl.edu.pwr.student.dmarchut.jp.model.SimulationConfig;
import pl.edu.pwr.student.dmarchut.jp.sync.SushiBelt;

import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

/** Worker thread representing a customer eating a sequence of desired dishes */
public class Customer implements Runnable {
    private final int seatIndex;
    private final Queue<DishType> wishlist;
    private final SushiBelt belt;
    private final SimulationConfig config;

    public Customer(int seatIndex, Queue<DishType> wishlist, SushiBelt belt, SimulationConfig config) {
        this.seatIndex = seatIndex;
        this.wishlist = wishlist;
        this.belt = belt;
        this.config = config;
    }

    /** Waits for each dish from the wishlist, eats it, then releases new seat */
    @Override
    public void run() {
        try {
            while (!wishlist.isEmpty() && belt.isRunning() && !Thread.currentThread().isInterrupted()) {
                DishType desiredDish = wishlist.poll();
                int remaining = wishlist.size() + 1;

                if(!belt.takeDish(seatIndex, desiredDish, remaining)) {
                    break;
                }

                int eatTime = randomDelay(config.customerMinEatDelay(), config.customerMaxEatDelay());
                Thread.sleep(eatTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            belt.releaseSeat(seatIndex);
        }
    }

    /** Returns a random delay in miliseconds within [min, max] */
    private int randomDelay(int min, int max) {
        int safeMax = Math.max(min, max);
        return ThreadLocalRandom.current().nextInt(min, safeMax + 1);
    }
}
