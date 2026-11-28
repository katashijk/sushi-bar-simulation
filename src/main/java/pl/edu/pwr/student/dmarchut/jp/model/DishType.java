package pl.edu.pwr.student.dmarchut.jp.model;

import java.util.concurrent.ThreadLocalRandom;

/** Represents sushi variants (via ASCII or text) */
public enum DishType {
    NIGIRI('▓'),
    ONIGIRI('Â'),
    SASHIMI('█'),
    MAKI('¤');

    private final char symbol;

    DishType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    /** Returns a random dish from specified dishes */
    public static DishType random() {
        DishType[] values = values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }
}
