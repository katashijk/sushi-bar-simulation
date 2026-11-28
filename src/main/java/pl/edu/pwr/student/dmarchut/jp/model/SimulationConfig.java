package pl.edu.pwr.student.dmarchut.jp.model;

/** Simulation config */
public record SimulationConfig(
        //General Simulation Config
        int engineDelay,
        int beltSize,

        //Chefs Config
        int chefCount,
        int chefMinPrepDelay,
        int chefMaxPrepDelay,

        //Customer Config
        int customerMinEatDelay,
        int customerMaxEatDelay,
        int customerSpawnDelay,

        int dishesPerCustomerMin,
        int dishesPerCustomerMax
) {
    /** Default Simulation Config */
    public static SimulationConfig defaultConfig() {
        return new SimulationConfig(
                2000, 12,
                2, 3000, 8000,
                2000, 10000, 5000,
                2, 4
        );
    }
}
