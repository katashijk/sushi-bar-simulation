package pl.edu.pwr.student.dmarchut.jp.threads;

import pl.edu.pwr.student.dmarchut.jp.model.SimulationConfig;
import pl.edu.pwr.student.dmarchut.jp.sync.SushiBelt;

public class BeltEngine implements Runnable {
    private final SushiBelt belt;
    private final SimulationConfig config;

    public BeltEngine(SushiBelt belt, SimulationConfig config) {
        this.belt = belt;
        this.config = config;
    }

    @Override
    public void run() {
        try {
            while (belt.isRunning() && !Thread.currentThread().isInterrupted()) {
                Thread.sleep(config.engineDelay());
                belt.rotate();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
