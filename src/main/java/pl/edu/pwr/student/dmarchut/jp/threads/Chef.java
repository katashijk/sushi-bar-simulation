package pl.edu.pwr.student.dmarchut.jp.threads;

import pl.edu.pwr.student.dmarchut.jp.model.DishType;
import pl.edu.pwr.student.dmarchut.jp.model.SimulationConfig;
import pl.edu.pwr.student.dmarchut.jp.model.chef.ChefStatus;
import pl.edu.pwr.student.dmarchut.jp.sync.SushiBelt;

import java.util.concurrent.ThreadLocalRandom;

public class Chef implements Runnable {
    private final int chefId;
    private final SushiBelt belt;
    private final SimulationConfig config;

    public Chef(int chefId, SushiBelt belt, SimulationConfig config) {
        this.chefId = chefId;
        this.belt = belt;
        this.config = config;
    }


    @Override
    public void run() {
        try {
            while (belt.isRunning() && !Thread.currentThread().isInterrupted()) {
                DishType dish = DishType.random();
                belt.updateChefState(chefId, ChefStatus.PREPARING, dish);

                int prepTime = randomDelay(config.chefMinPrepDelay(), config.chefMaxPrepDelay());
                Thread.sleep(prepTime);

                if(!belt.putDish(chefId, dish)) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int randomDelay(int min, int max) {
        int safeMax = Math.max(min, max);
        return ThreadLocalRandom.current().nextInt(min, safeMax + 1);
    }
}
