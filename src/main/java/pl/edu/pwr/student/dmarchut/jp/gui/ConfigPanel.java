package pl.edu.pwr.student.dmarchut.jp.gui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import pl.edu.pwr.student.dmarchut.jp.model.SimulationConfig;

public class ConfigPanel extends VBox {
    private final Spinner<Integer> beltSizeSpinner;
    private final Spinner<Integer> engineDelaySpinner;

    private final Spinner<Integer> chefCountSpinner;
    private final Spinner<Integer> chefMinPrepSpinner;
    private final Spinner<Integer> chefMaxPrepSpinner;

    private final Spinner<Integer> customerSpawnSpinner;
    private final Spinner<Integer> customerMinEatSpinner;
    private final Spinner<Integer> customerMaxEatSpinner;

    private final Spinner<Integer> dishesMinSpinner;
    private final Spinner<Integer> dishesMaxSpinner;

    private final Runnable onPreviewChange;

    public ConfigPanel(Runnable onPreviewChange) {
        this.onPreviewChange = onPreviewChange;
        SimulationConfig def = SimulationConfig.defaultConfig();

        beltSizeSpinner = createSpinner(8, 32, def.beltSize(), 4);
        engineDelaySpinner = createSpinner(100, 3000, def.engineDelay(), 50);

        int initialMaxChefs = calculateMaxChefs(def.beltSize());
        chefCountSpinner = createSpinner(1, initialMaxChefs, def.chefCount(), 1);
        chefMinPrepSpinner = createSpinner(200, 5000, def.chefMinPrepDelay(), 100);
        chefMaxPrepSpinner = createSpinner(200, 8000, def.chefMaxPrepDelay(), 100);

        customerSpawnSpinner = createSpinner(200, 5000, def.customerSpawnDelay(), 100);
        customerMinEatSpinner = createSpinner(200, 5000, def.customerMinEatDelay(), 100);
        customerMaxEatSpinner = createSpinner(200, 8000, def.customerMaxEatDelay(), 100);

        dishesMinSpinner = createSpinner(1, 10, def.dishesPerCustomerMin(), 1);
        dishesMaxSpinner = createSpinner(1, 20, def.dishesPerCustomerMax(), 1);

        beltSizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateMaxChefs(newVal);
            notifyPreview();
        });
        chefCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> notifyPreview());

        linkMinMax(chefMinPrepSpinner, chefMaxPrepSpinner);
        linkMinMax(customerMinEatSpinner, customerMaxEatSpinner);
        linkMinMax(dishesMinSpinner, dishesMaxSpinner);

        Button resetBtn = new Button("Przywróć domyślnie");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setOnAction(e -> resetToDefault());

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);

        int row = 0;
        row = addSectionHeader(form, row, "Tor i prędkość");
        row = addRow(form, row, "Długość toru: ", beltSizeSpinner);
        row = addRow(form, row, "Prędkość toru [ms]: ", engineDelaySpinner);

        row = addSectionHeader(form, row, "Kucharze");
        row = addRow(form, row, "Liczba kucharzy: ", chefCountSpinner);
        row = addRow(form, row, "Gotowanie min [ms]: ", chefMinPrepSpinner);
        row = addRow(form, row, "Gotowanie max [ms]: ", chefMaxPrepSpinner);

        row = addSectionHeader(form, row, "Klienci");
        row = addRow(form, row, "Nowy klient [ms]: ", customerSpawnSpinner);
        row = addRow(form, row, "Jedzenie min [ms]: ", customerMinEatSpinner);
        row = addRow(form, row, "Jedzenie max [ms]: ", customerMaxEatSpinner);
        row = addRow(form, row, "Dań min: ", dishesMinSpinner);
        addRow(form, row, "Dań max: ", dishesMaxSpinner);

        Label title = new Label("Parametry symulacji");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        setSpacing(10);
        setPadding(new Insets(0, 12, 12, 12));
        setPrefWidth(275);
        getChildren().addAll(title, new Separator(), form, new Separator(), resetBtn);
    }

    private int calculateMaxChefs(int beltSize) {
        int height = Math.max(3, (beltSize / 4) + 1);
        int width = Math.max(3, (beltSize - 2 * (height - 2)) / 2);

        int innerH = height - 2;
        int innerW = width - 2;

        if(innerW <= 2 || innerH <= 2) {
            return Math.max(1, innerW * innerH);
        }

        return 2 * innerW + 2 * (innerH - 2);
    }

    public SimulationConfig buildConfig() {
        int chefMin = chefMinPrepSpinner.getValue();
        int chefMax = Math.max(chefMin, chefMaxPrepSpinner.getValue());

        int eatMin = customerMinEatSpinner.getValue();
        int eatMax = Math.max(eatMin, customerMaxEatSpinner.getValue());

        int dishesMin = dishesMinSpinner.getValue();
        int dishesMax = Math.max(dishesMin, dishesMaxSpinner.getValue());

        return new SimulationConfig(
                engineDelaySpinner.getValue(),
                beltSizeSpinner.getValue(),
                chefCountSpinner.getValue(),
                chefMin,
                chefMax,
                eatMin,
                eatMax,
                customerSpawnSpinner.getValue(),
                dishesMin,
                dishesMax
        );
    }

    private void resetToDefault() {
        SimulationConfig def = SimulationConfig.defaultConfig();

        beltSizeSpinner.getValueFactory().setValue(def.beltSize());
        engineDelaySpinner.getValueFactory().setValue(def.engineDelay());

        chefCountSpinner.getValueFactory().setValue(def.chefCount());
        chefMinPrepSpinner.getValueFactory().setValue(def.chefMinPrepDelay());
        chefMaxPrepSpinner.getValueFactory().setValue(def.chefMaxPrepDelay());

        customerSpawnSpinner.getValueFactory().setValue(def.customerSpawnDelay());
        customerMinEatSpinner.getValueFactory().setValue(def.customerMinEatDelay());
        customerMaxEatSpinner.getValueFactory().setValue(def.customerMaxEatDelay());

        dishesMinSpinner.getValueFactory().setValue(def.dishesPerCustomerMin());
        dishesMaxSpinner.getValueFactory().setValue(def.dishesPerCustomerMax());

        notifyPreview();
    }

    private void updateMaxChefs(int beltSize) {
        int maxChefs = calculateMaxChefs(beltSize);
        SpinnerValueFactory.IntegerSpinnerValueFactory factory = (SpinnerValueFactory.IntegerSpinnerValueFactory) chefCountSpinner.getValueFactory();
        factory.setMax(maxChefs);

        if(chefCountSpinner.getValue() > maxChefs) {
            factory.setValue(maxChefs);
        }
    }

    private void linkMinMax(Spinner<Integer> minSpinner, Spinner<Integer> maxSpinner) {
        minSpinner.valueProperty().addListener((obs, o, newMin) -> {
            if(newMin > maxSpinner.getValue()) {
                maxSpinner.getValueFactory().setValue(newMin);
            }
        });

        maxSpinner.valueProperty().addListener((obs, o, newMax) -> {
            if(newMax < minSpinner.getValue()) {
                minSpinner.getValueFactory().setValue(newMax);
            }
        });
    }

    private Spinner<Integer> createSpinner(int min, int max, int initial, int step) {
        Spinner<Integer> spinner = new Spinner<>(min, max, initial, step);
        spinner.setPrefWidth(95);
        spinner.setEditable(false);

        return spinner;
    }

    private int addSectionHeader(GridPane grid, int row, String text) {
        Label header = new Label(text);
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-padding: 6 0 2 0;");
        grid.add(header, 0, row, 2, 1);

        return row + 1;
    }

    private int addRow(GridPane grid, int row, String text, Spinner<Integer> spinner) {
        grid.add(new Label(text), 0, row);
        grid.add(spinner, 1, row);

        return row + 1;
    }

    private void notifyPreview() {
        if(onPreviewChange != null) {
            onPreviewChange.run();
        }
    }
}
