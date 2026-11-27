package pl.edu.pwr.student.dmarchut.jp.model.customer;

import pl.edu.pwr.student.dmarchut.jp.model.DishType;

/** Represents Customer */
public record CustomerView(
        char seatLabel,
        CustomerStatus status,
        DishType currentDish,
        int remainingDishes
) {}
