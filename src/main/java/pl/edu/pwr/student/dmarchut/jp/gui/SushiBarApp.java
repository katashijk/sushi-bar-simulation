package pl.edu.pwr.student.dmarchut.jp.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

/** Main JavaFX application combining configuration, 2D board view, and thread monitor */
public class SushiBarApp extends Application {
    private final BoardPane boardPane = new BoardPane();
    private final TextArea threadMonitorArea = new TextArea();
    private ConfigPanel configPanel;

    private SushiBelt belt;
    private Creator creator;
    private Thread creatorThread;
    private Timeline threadMonitorTimeline;

    /** Initializes the window layout, controls, and event handlers */
    @Override
    public void start(Stage primaryStage) {
        Button startBtn = new Button("Start");
        Button stopBtn = new Button("Stop");
        stopBtn.setDisable(true);

        configPanel = new ConfigPanel(this::renderPreviewFromConfig);

        startBtn.setOnAction(e -> {
            startSimulation();
            startBtn.setDisable(true);
            stopBtn.setDisable(false);
            configPanel.setDisable(true);
        });

        stopBtn.setOnAction(e -> {
            stopSimulation();
            stopBtn.setDisable(true);
            startBtn.setDisable(false);
            configPanel.setDisable(false);
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

        ScrollPane centerScroll = new ScrollPane(boardPane);
        centerScroll.setFitToWidth(true);
        centerScroll.setFitToHeight(true);
        centerScroll.setStyle("-fx-background-color: transparent");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(configPanel);
        root.setCenter(centerScroll);
        root.setRight(rightPanel);
        root.setBottom(legend);

        renderPreviewFromConfig();

        primaryStage.setTitle("Symulator Sushi Baru - Wielowątkowość");
        primaryStage.setScene(new Scene(root, 1200, 700));
        primaryStage.setOnCloseRequest(e -> stopSimulation());
        primaryStage.show();
    }

    /** Renders an initial board preview using the current ConfigPanel settings */
    private void renderPreviewFromConfig() {
        SimulationConfig previewConfig = configPanel.buildConfig();
        boardPane.render(new SushiBelt(previewConfig).getSnapshot());
    }

    /** Starts the simulation threads and live thread-state monitoring */
    private void startSimulation() {
        SimulationConfig config = configPanel.buildConfig();
        final SushiBelt currentBelt = new SushiBelt(config);
        belt = currentBelt;
        creator = new Creator(currentBelt, config);

        currentBelt.setOnUpdateListener(() -> Platform.runLater(() -> {
            if(belt == currentBelt) {
                boardPane.render(currentBelt.getSnapshot());
            }
        }));

        creatorThread = new Thread(creator, "Creator");
        creatorThread.start();

        threadMonitorTimeline = new Timeline(new KeyFrame(Duration.millis(150), e -> updateThreadMonitor()));
        threadMonitorTimeline.setCycleCount(Timeline.INDEFINITE);
        threadMonitorTimeline.play();
    }

    /** Stops the thread monitor timer and terminates all running simulation threads */
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

    /** Refreshes the text area showing current states of all active threads */
    private void updateThreadMonitor() {
        if(creator == null) return;

        StringBuilder sb = new StringBuilder();
        if(creatorThread != null) {
            sb.append(String.format("%-18s -> %s%n", creatorThread.getName(), creatorThread.getState()));
        }

        String threadsText = creator.getManagedThreads().stream()
                .map(t -> String.format("%-18s -> %s", t.getName(), t.getState()))
                .collect(Collectors.joining("\n"));
        sb.append(threadsText);

        threadMonitorArea.setText(sb.toString());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
