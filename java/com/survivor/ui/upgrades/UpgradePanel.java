package com.survivor.ui.upgrades;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

public class UpgradePanel extends StackPane {

    private final List<UpgradeOption> options;
    private final Consumer<UpgradeOption> onChosen;

    private final StackPane scrim = new StackPane();
    private final HBox cardsBox = new HBox(18);

    public UpgradePanel(List<UpgradeOption> options, Consumer<UpgradeOption> onChosen) {
        this.options = options;
        this.onChosen = onChosen;

        // 遮罩
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.rgb(0,0,0,180/255.0));
        overlay.widthProperty().bind(widthProperty());
        overlay.heightProperty().bind(heightProperty());
        scrim.getChildren().add(overlay);
        scrim.setOpacity(0);

        // 卡片容器
        cardsBox.setAlignment(Pos.CENTER);
        cardsBox.setPadding(new Insets(24));
        cardsBox.setOpacity(0);
        cardsBox.setTranslateY(140);

        for (int i = 0; i < options.size(); i++) {
            cardsBox.getChildren().add(buildCard(options.get(i), i));
        }

        getChildren().addAll(scrim, cardsBox);
        setPickOnBounds(true);
        setVisible(false);

        // 键盘 1/2/3
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DIGIT1 || e.getCode() == KeyCode.NUMPAD1) pick(0);
            else if (e.getCode() == KeyCode.DIGIT2 || e.getCode() == KeyCode.NUMPAD2) pick(1);
            else if (e.getCode() == KeyCode.DIGIT3 || e.getCode() == KeyCode.NUMPAD3) pick(2);
            else if (e.getCode() == KeyCode.ESCAPE) close(null);
        });

        var css = getClass().getResource("/ui/upgrades/upgrades.css");
        if (css != null) getStylesheets().add(css.toExternalForm());
        getStyleClass().add("upgrade-root");
    }

    private VBox buildCard(UpgradeOption opt, int idx) {
        ImageView icon = new ImageView(opt.icon());
        icon.setFitWidth(64); icon.setFitHeight(64); icon.setPreserveRatio(true);

        Label title = new Label(opt.title());
        title.getStyleClass().add("upgrade-title");

        Label desc = new Label(opt.description());
        desc.getStyleClass().add("upgrade-desc");
        desc.setWrapText(true);
        desc.setMaxWidth(320);

        VBox box = new VBox(10, icon, title, desc);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(16));
        box.getStyleClass().add("upgrade-card");
        box.setMaxWidth(360);
        box.setPrefWidth(320);

        box.setOnMouseClicked(e -> pick(idx));
        box.setOnMouseEntered(e -> box.getStyleClass().add("hover"));
        box.setOnMouseExited(e -> box.getStyleClass().remove("hover"));
        return box;
    }

    public void playIn() {
        setVisible(true);
        requestFocus();

        var scrimFade = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(scrim.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(220), new KeyValue(scrim.opacityProperty(), 1, Interpolator.EASE_BOTH))
        );

        var cardsRise = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(cardsBox.opacityProperty(), 0),
                        new KeyValue(cardsBox.translateYProperty(), 140)
                ),
                new KeyFrame(Duration.millis(360),
                        new KeyValue(cardsBox.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(cardsBox.translateYProperty(), 0, Interpolator.SPLINE(0.2,0.8,0.2,1))
                )
        );

        ParallelTransition pt = new ParallelTransition(scrimFade, cardsRise);

        // 子项交错
        SequentialTransition stagger = new SequentialTransition();
        int i = 0;
        for (Node n : cardsBox.getChildren()) {
            n.setOpacity(0); n.setTranslateY(24);
            Timeline tl = new Timeline(
                    new KeyFrame(Duration.millis(60 * i)),
                    new KeyFrame(Duration.millis(280),
                            new KeyValue(n.opacityProperty(), 1, Interpolator.EASE_OUT),
                            new KeyValue(n.translateYProperty(), 0, Interpolator.SPLINE(0.2,0.8,0.2,1))
                    )
            );
            stagger.getChildren().add(tl);
            i++;
        }

        new SequentialTransition(pt, stagger).play();
    }

    private void pick(int idx) {
        if (idx < 0 || idx >= options.size()) return;
        VBox card = (VBox) cardsBox.getChildren().get(idx);
        var opt = options.get(idx);

        ScaleTransition st = new ScaleTransition(Duration.millis(120), card);
        st.setFromX(1); st.setFromY(1);
        st.setToX(1.06); st.setToY(1.06);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.setOnFinished(e -> {
            if (onChosen != null) onChosen.accept(opt);
            close(null);
        });
        st.play();
    }

    private void close(Runnable after) {
        var scrimFade = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(scrim.opacityProperty(), scrim.getOpacity())),
                new KeyFrame(Duration.millis(180), new KeyValue(scrim.opacityProperty(), 0, Interpolator.EASE_BOTH))
        );
        var cardsDown = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(cardsBox.opacityProperty(), cardsBox.getOpacity()),
                        new KeyValue(cardsBox.translateYProperty(), cardsBox.getTranslateY())
                ),
                new KeyFrame(Duration.millis(240),
                        new KeyValue(cardsBox.opacityProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(cardsBox.translateYProperty(), 140, Interpolator.SPLINE(0.4,0,1,1))
                )
        );
        ParallelTransition pt = new ParallelTransition(scrimFade, cardsDown);
        pt.setOnFinished(e -> {
            setVisible(false);
            if (after != null) after.run();
        });
        pt.play();
    }
}
