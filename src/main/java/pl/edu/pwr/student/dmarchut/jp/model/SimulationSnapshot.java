package pl.edu.pwr.student.dmarchut.jp.model;

import pl.edu.pwr.student.dmarchut.jp.model.chef.ChefView;
import pl.edu.pwr.student.dmarchut.jp.model.customer.CustomerView;

import java.util.List;

/** Read-Only object that GUI gets all info from */
public record SimulationSnapshot(
        List<DishType> beltSlots,
        List<ChefView> chefs,
        List<CustomerView> seats
) {}
