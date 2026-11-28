package pl.edu.pwr.student.dmarchut.jp.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import pl.edu.pwr.student.dmarchut.jp.model.DishType;
import pl.edu.pwr.student.dmarchut.jp.model.SimulationSnapshot;
import pl.edu.pwr.student.dmarchut.jp.model.chef.ChefStatus;
import pl.edu.pwr.student.dmarchut.jp.model.chef.ChefView;
import pl.edu.pwr.student.dmarchut.jp.model.customer.CustomerStatus;
import pl.edu.pwr.student.dmarchut.jp.model.customer.CustomerView;

import java.util.HashSet;
import java.util.Set;

/** JavaFX grid component rendering customers, belt slots, and chefs in 2D */
public class BoardPane extends StackPane {
    private static final int CELL_SIZE = 48;
    private final GridPane grid = new GridPane();

    public BoardPane() {
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(3);
        grid.setVgap(3);
        getChildren().add(grid);
        setStyle("-fx-padding: 16; -fx-background-color: #f8fafc; -fx-background-radius: 8;" +
                "-fx-border-color: #cbd5e1; -fx-border-radius: 8;");
    }

    /** Clears and redraws the 2D board from the given simulation snapshot */
    public void render(SimulationSnapshot snapshot) {
        grid.getChildren().clear();
        if(snapshot == null) return;

        int n = snapshot.beltSlots().size();
        int height = Math.max(3, (n/4) + 1);
        int width = Math.max(3, (n - 2 * (height - 2)) / 2);

        int[] beltCols = new int[n];
        int[] beltRows = new int[n];

        for(int i=0; i<n; i++) {
            int bCol, bRow, cCol, cRow;

            if(i < width){
                bCol = 1 + i;
                bRow = 1;
                cCol = bCol;
                cRow = 0;
            } else if(i < width + (height - 2)) {
                int k = i - width;
                bCol = width;
                bRow = 2 + k;
                cCol = width + 1;
                cRow = bRow;
            } else if(i < 2 * width + (height - 2)) {
                int k = i - (width + height - 2);
                bCol = width - k;
                bRow = height;
                cCol = bCol;
                cRow = height + 1;
            } else {
                int k = i - (2 * width + height - 2);
                bCol = 1;
                bRow = height - 1 - k;
                cCol = 0;
                cRow = bRow;
            }

            beltCols[i] = bCol;
            beltRows[i] = bRow;

            grid.add(createBeltCell(snapshot.beltSlots().get(i)), bCol, bRow);
            grid.add(createCustomerCell(snapshot.seats().get(i)), cCol, cRow);
        }

        Set<String> occupiedInside = new HashSet<>();
        for(ChefView chef : snapshot.chefs()) {
            int idx = chef.targetBeltIndex();
            int bCol = beltCols[idx];
            int bRow = beltRows[idx];

            int prefCol = clamp(bCol, 2, width - 1);
            int prefRow = clamp(bRow, 2, height - 1);

            if(bRow == 1) prefRow = 2;
            else if(bRow == height) prefRow = height - 1;
            else if(bCol == 1) prefCol = 2;
            else if(bCol == width) prefCol = width - 1;

            String key = prefCol + "," + prefRow;
            if(occupiedInside.contains(key)) {
                outer:
                for(int r=2; r<=height-1; r++) {
                    for(int c=2; c<=width-1; c++) {
                        if(r != 2 && r != height - 1 && c != 2 && c != width - 1){
                            continue;
                        }

                        String candidate = c + "," + r;
                        if(!occupiedInside.contains(candidate)) {
                            prefCol = c;
                            prefRow = r;
                            key = candidate;
                            break outer;
                        }
                    }
                }
            }

            occupiedInside.add(key);
            grid.add(createChefCell(chef), prefCol, prefRow);
        }
    }

    /** Creates a styled cell representing single belt slot */
    private Label createBeltCell(DishType dish) {
        String text, bg;

        if(dish == null) {
            text = "-";
        } else {
            text = String.valueOf(dish.getSymbol());
        }

        if(dish == null) {
            bg = "#f1f5f9";
        } else {
            bg = "#dbeafe";
        }

        return baseCell(text, bg, "#1e3a8a", "#60a5fa");
    }

    /** Creates a styled cell representing a customer seat and its current status */
    private Label createCustomerCell(CustomerView seat) {
        String text, bgColor, textColor = "#0f172a";

        switch (seat.status()) {
            case EMPTY -> {
                text = String.valueOf(seat.seatLabel());
                bgColor = "#e2e8f0";
                textColor = "#64748b";
            }
            case WAITING -> {
                String dish = String.valueOf(seat.currentDish().getSymbol());
                text = "?" + dish;
                bgColor = "#fef08a";
            }
            case EATING -> {
                text = String.valueOf(seat.currentDish().getSymbol());
                bgColor = "#bbf7d0";
            }
            default -> {
                text = "?";
                bgColor = "#e2e8f0";
            }
        }

        Label label = baseCell(text, bgColor, textColor, "#94a3b8");

        if(seat.status() != CustomerStatus.EMPTY && seat.currentDish() != null) {
            label.setTooltip(new Tooltip("Miejsce " + seat.seatLabel() + " | Cel: " + seat.currentDish().getSymbol()
                    + " | Pozostało dań: " + seat.remainingDishes()));
        }

        return label;
    }

    /** Creates a styled cell representing a chef and its current status */
    private Label createChefCell(ChefView chef) {
        String text, bg;

        if(chef.status() == ChefStatus.PREPARING && chef.currentDish() != null) {
            text = String.valueOf(chef.currentDish().getSymbol());
            bg = "#fed7aa";
        } else if(chef.status() == ChefStatus.WAITING) {
            text = chef.chefLabel() + "!";
            bg = "#fecaca";
        } else {
            text = chef.chefLabel();
            bg = "#f3e8ff";
        }

        return baseCell(text, bg, "#581c87", "#a855f7");
    }

    /** Builds a fixed-size square label cell with consissten typography and colors */
    private Label baseCell(String text, String bgColor, String textColor, String borderColor) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setMinSize(CELL_SIZE, CELL_SIZE);
        label.setPrefSize(CELL_SIZE, CELL_SIZE);
        label.setMaxSize(CELL_SIZE, CELL_SIZE);
        label.setStyle(String.format("-fx-font-family: 'Monospaced'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: %s;" +
                "-fx-background-color: %s; -fx-background-radius: 6;" +
                "-fx-border-color: %s; -fx-border-radius: 6;", bgColor, textColor, borderColor));

        return label;
    }

    /** Clamps value to he inclusive [min, max] range */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
