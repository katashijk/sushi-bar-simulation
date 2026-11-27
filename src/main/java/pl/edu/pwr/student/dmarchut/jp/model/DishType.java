package pl.edu.pwr.student.dmarchut.jp.model;

import java.util.concurrent.ThreadLocalRandom;

/** Represents sushi variants (via ASCII or text) */
public enum DishType {
    NIGIRI('▓', "Nigiri"),
    ONIGIRI('Â', "Onigiri"),
    SASHIMI('█', "Sashimi"),
    MAKI('¤', "Maki");

    private final char symbol;
    private final String displayName;

    DishType(char symbol, String displayName) {
        this.symbol = symbol;
        this.displayName = displayName;
    }

    public char getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DishType random() {
        DishType[] values = values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }
}
