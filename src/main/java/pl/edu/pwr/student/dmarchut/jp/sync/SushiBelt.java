package pl.edu.pwr.student.dmarchut.jp.sync;

import pl.edu.pwr.student.dmarchut.jp.model.DishType;
import pl.edu.pwr.student.dmarchut.jp.model.SimulationConfig;
import pl.edu.pwr.student.dmarchut.jp.model.SimulationSnapshot;
import pl.edu.pwr.student.dmarchut.jp.model.chef.ChefStatus;
import pl.edu.pwr.student.dmarchut.jp.model.chef.ChefView;
import pl.edu.pwr.student.dmarchut.jp.model.customer.CustomerStatus;
import pl.edu.pwr.student.dmarchut.jp.model.customer.CustomerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** PLACEHOLDER */
public class SushiBelt {
    private final int size;
    private final DishType[] belt;
    private final ChefView[] chefs;
    private final CustomerView[] seats;

    private final Object monitor = new Object();

    private volatile boolean running = true;
    private Runnable onUpdateListener;

    public SushiBelt(SimulationConfig config) {
        this.size = config.beltSize();
        this.belt = new DishType[size];
        this.chefs = new ChefView[config.chefCount()];
        this.seats = new CustomerView[size];

        for(int i=0; i<size; i++) {
            char label = (char) ('a' + (i % 26));

            seats[i] = new CustomerView(label, CustomerStatus.EMPTY, null, 0);
        }

        List<Integer> validTargets = buildValidChefTargets(size);
        for(int i=0; i<config.chefCount(); i++) {
            String label = String.valueOf((char) ('X' + (i % 26)));
            int targetIndex = validTargets.get((i * validTargets.size()) / config.chefCount());

            chefs[i] = new ChefView(label, targetIndex, ChefStatus.IDLE, null);
        }
    }

    /** PLACEHOLDER */
    public void setOnUpdateListener(Runnable listener) {
        this.onUpdateListener = listener;
    }

    /** PLACEHOLDER */
    private void notifyListener() {
        if(onUpdateListener != null) {
            onUpdateListener.run();
        }
    }

    /** Run by Engine */
    public void rotate() {
        synchronized (monitor) {
            if(!running) return;

            DishType last = belt[size - 1];
            System.arraycopy(belt, 0, belt, 1, size-1);
            belt[0] = last;

            monitor.notifyAll();
        }
        notifyListener();
    }

    /** Updates Chef visual status */
    public void updateChefState(int chefId, ChefStatus status, DishType dish) {
        synchronized (monitor) {
            ChefView current = chefs[chefId];

            chefs[chefId] = new ChefView(current.chefLabel(), current.targetBeltIndex(), status, dish);
        }
        notifyListener();
    }

    /** Run when Chef wants to put the cooked dish on the belt */
    public boolean putDish(int chefId, DishType dish) throws InterruptedException {
        boolean isOccupied;
        synchronized (monitor) {
            int targetIndex = chefs[chefId].targetBeltIndex();
            isOccupied = (belt[targetIndex] != null);

            if(isOccupied) {
                chefs[chefId] = new ChefView(chefs[chefId].chefLabel(), targetIndex, ChefStatus.WAITING, dish);
            }
        }

        if(isOccupied) {
            notifyListener();
        }

        synchronized (monitor) {
            int targetIndex = chefs[chefId].targetBeltIndex();
            while(running && belt[targetIndex] != null) {
                monitor.wait();
            }

            if(!running) return false;

            belt[targetIndex] = dish;
            chefs[chefId] = new ChefView(chefs[chefId].chefLabel(), targetIndex, ChefStatus.IDLE, null);

            monitor.notifyAll();
        }

        notifyListener();
        return true;
    }

    private List<Integer> buildValidChefTargets(int beltSize) {
        int height = Math.max(3, (beltSize / 4) + 1);
        int width = Math.max(3, (beltSize - 2 * (height - 2)) / 2);

        List<Integer> targets = new ArrayList<>();
        if(width <= 3 || height <= 3) {
            targets.add(1);
            return targets;
        }

        for (int i=1; i<=width-3; i++) {
            targets.add(i);
        }
        for (int i=width; i<=width+height-4; i++) {
            targets.add(i);
        }

        int bottomStart = width + (height - 2);
        for (int i=bottomStart+1; i<=bottomStart+width-3; i++) {
            targets.add(i);
        }

        int leftStart = 2 * width + (height - 2);
        for (int i=leftStart; i<=leftStart+height-4; i++) {
            targets.add(i);
        }

        return targets;
    }

    /** Run when Creator wants to put new Customer to an available seat */
    public int tryOccupySeat(int totalDishes, DishType firstDesiredDish) {
        int chosen = -1;
        synchronized (monitor) {
            List<Integer> freeSeats = new ArrayList<>();

            for(int i=0; i<size; i++) {
                if(seats[i].status() == CustomerStatus.EMPTY) {
                    freeSeats.add(i);
                }
            }

            if(!freeSeats.isEmpty()) {
                chosen = freeSeats.get(ThreadLocalRandom.current().nextInt(freeSeats.size()));

                CustomerView current = seats[chosen];
                seats[chosen] = new CustomerView(current.seatLabel(), CustomerStatus.WAITING, firstDesiredDish, totalDishes);
            }
        }

        if(chosen != -1) {
            notifyListener();
        }

        return chosen;
    }

    /** Run when Customer waits for his dish to arrive on the belt */
    public boolean takeDish(int seatIndex, DishType desiredDish, int remainingDishes) throws InterruptedException {
        synchronized (monitor) {
            CustomerView current = seats[seatIndex];
            seats[seatIndex] = new CustomerView(current.seatLabel(), CustomerStatus.WAITING, desiredDish, remainingDishes);
        }

        notifyListener();

        synchronized (monitor) {
            while(running && belt[seatIndex] != desiredDish) {
                monitor.wait();
            }

            if(!running) return false;

            belt[seatIndex] = null;
            CustomerView current = seats[seatIndex];
            seats[seatIndex] = new CustomerView(current.seatLabel(), CustomerStatus.EATING, desiredDish, remainingDishes);

            monitor.notifyAll();
        }

        notifyListener();
        return true;
    }

    /** Run when Customer eats all of his desired dishes */
    public void releaseSeat(int seatIndex) {
        synchronized (monitor) {
            char label = seats[seatIndex].seatLabel();
            seats[seatIndex] = new CustomerView(label, CustomerStatus.EMPTY, null, 0);
        }
        notifyListener();
    }

    public SimulationSnapshot getSnapshot() {
        synchronized (monitor) {
            return new SimulationSnapshot(
                    Arrays.asList(belt.clone()),
                    List.of(chefs.clone()),
                    List.of(seats.clone())
            );
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void shutdown() {
        synchronized (monitor) {
            running = false;
            monitor.notifyAll();
        }
    }
}
