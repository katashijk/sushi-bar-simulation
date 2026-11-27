package pl.edu.pwr.student.dmarchut.jp.model.chef;

import pl.edu.pwr.student.dmarchut.jp.model.DishType;

/** Represents Chef */
public record ChefView(
        String chefLabel,
        int targetBeltIndex,
        ChefStatus status,
        DishType currentDish
) {}
