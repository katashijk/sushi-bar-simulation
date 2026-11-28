package pl.edu.pwr.student.dmarchut.jp.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import pl.edu.pwr.student.dmarchut.jp.model.SimulationConfig;
import pl.edu.pwr.student.dmarchut.jp.sync.SushiBelt;
import pl.edu.pwr.student.dmarchut.jp.threads.Creator;

import java.util.stream.Collectors;

public class SushiBarApp extends Application {
    private final BoardPane boardPane = new BoardPane();
    private final TextArea threadMonitorArea = new TextArea();

    private SushiBelt belt;
    private Creator creator;
    private Thread creatorThread;
    private Timeline threadMonitorTimeline;

    @Override
    public void start(Stage primaryStage) {
        Button startBtn = new Button("Start");
        Button stopBtn = new Button("Stop");
        stopBtn.setDisable(true);

        startBtn.setOnAction(e -> {
            startSimulation();
            startBtn.setDisable(true);
            stopBtn.setDisable(false);
        });

        stopBtn.setOnAction(e -> {
            stopSimulation();
            stopBtn.setDisable(true);
            startBtn.setDisable(false);
        });

        HBox topBar = new HBox(10, startBtn, stopBtn);
        topBar.setPadding(new Insets(12));

        threadMonitorArea.setEditable(false);
        threadMonitorArea.setPrefWidth(290);
        threadMonitorArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");

        VBox rightPanel = new VBox(8, new Label("Obserwacja wątków:"), threadMonitorArea);
        rightPanel.setPadding(new Insets(0, 12, 12, 12));
        VBox.setVgrow(threadMonitorArea, Priority.ALWAYS);

        Label legend = new Label("Legenda: [-] Pusto   [▓/Â/█/¤] Danie   [?] Klient czeka na danie   " +
                "[a..l] Wolne miejsce   [X, Y] Kucharz   [X!] Kucharz czeka (zajęty tor)");
        legend.setPadding(new Insets(10, 12, 12, 12));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(boardPane);
        root.setRight(rightPanel);
        root.setBottom(legend);
        BorderPane.setMargin(boardPane, new Insets(0, 0, 12, 12));

        boardPane.render(new SushiBelt(SimulationConfig.defaultConfig()).getSnapshot());

        primaryStage.setTitle("Symulator Sushi Baru - Wielowątkowość");
        primaryStage.setScene(new Scene(root, 880, 520));
        primaryStage.setOnCloseRequest(e -> stopSimulation());
        primaryStage.show();
    }

    private void startSimulation() {
        SimulationConfig config = SimulationConfig.defaultConfig();
        belt = new SushiBelt(config);
        creator = new Creator(belt, config);

        belt.setOnUpdateListener(() -> Platform.runLater(() -> {
            if(belt != null) {
                boardPane.render(belt.getSnapshot());
            }
        }));

        creatorThread = new Thread(creator, "Creator");
        creatorThread.start();

        threadMonitorTimeline = new Timeline(new KeyFrame(Duration.millis(150), e -> updateThreadMonitor()));
        threadMonitorTimeline.setCycleCount(Timeline.INDEFINITE);
        threadMonitorTimeline.play();
    }

    private void stopSimulation() {
        if(threadMonitorTimeline != null) {
            threadMonitorTimeline.stop();
            threadMonitorTimeline = null;
        }

        if(creator != null) {
            creator.stopAll();
        }

        if(creatorThread != null) {
            creatorThread.interrupt();
        }
    }

    private void updateThreadMonitor() {
        if(creator == null) return;

        StringBuilder sb = new StringBuilder();
        if(creatorThread != null) {
            sb.append(String.format("%-16s -> %s%n", creatorThread.getName(), creatorThread.getState()));
        }

        String threadsText = creator.getManagedThreads().stream()
                .map(t -> String.format("%-16s -> %s", t.getName(), t.getState()))
                .collect(Collectors.joining("\n"));
        sb.append(threadsText);

        threadMonitorArea.setText(sb.toString());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
