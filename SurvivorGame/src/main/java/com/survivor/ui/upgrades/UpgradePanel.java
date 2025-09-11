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

    // === 装饰边框相关（根据你的资源路径改） ===
    private static final String FRAME_PATH = "ui/upgrades/frame.png";
    // 目标卡片宽度（与原来接近；外观会更饱满）
    private static final double CARD_TARGET_WIDTH = 360;
    // 内容与边框的内边距（让内容别贴边框）
    private static final double FRAME_INNER_PADDING = 24;

    private static javafx.scene.image.Image img(String path) {
        try {
            var im = new javafx.scene.image.Image(path, false);
            if (!im.isError()) return im;
        } catch (Exception ignored) {}
        try {
            var url = UpgradePanel.class.getResource(path.startsWith("/") ? path : ("/" + path));
            if (url != null) return new javafx.scene.image.Image(url.toExternalForm());
        } catch (Exception ignored) {}
        return new javafx.scene.image.WritableImage(1, 1); // 占位，不崩溃
    }

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
        // 1) 读取边框图片，计算卡片宽高（等比于边框）
        var frameImage = img(FRAME_PATH);
        double frameW = frameImage.getWidth();
        double frameH = frameImage.getHeight();
        // 边框图可能还没读到像素（异步），做个保底比例：3:4
        double aspect = (frameW > 0 && frameH > 0) ? (frameW / frameH) : (3.0 / 4.0);

        double cardW = CARD_TARGET_WIDTH;
        double cardH = Math.round(cardW / aspect);

        // 2) 内容：图标 + 标题 + 描述（和你原来一样，只是字号/对齐更适合置中）
        ImageView icon = new ImageView(opt.getImage());
        icon.setFitWidth(168);
        icon.setFitHeight(168);
        icon.setPreserveRatio(true);
        VBox.setMargin(icon, new Insets(0, 0, 10, 0));

        Label title = new Label(opt.getTitle());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label desc = new Label(opt.getDescription());
        desc.setWrapText(true);
        desc.setMaxWidth(cardW - FRAME_INNER_PADDING * 2); // 根据卡宽动态限制
        desc.setAlignment(Pos.CENTER);
        desc.setStyle("-fx-font-size: 16px; -fx-text-fill: #DDDDDD;");

        VBox content = new VBox(20, icon, title, desc);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(FRAME_INNER_PADDING));

        // 3) 边框层（完整 PNG）；保持比例填满 cardRoot
        ImageView frameView = new ImageView(frameImage);
        frameView.setPreserveRatio(true);
        // 用 fitWidth 优先撑满宽度，高度跟着等比；若想完全铺满可改成 setPreserveRatio(false)+同时设宽高
        frameView.setFitWidth(cardW);

        // 4) 卡根节点：StackPane，尺寸与边框一致；底层 frame，上面 content
        StackPane cardRoot = new StackPane(frameView, content);
        cardRoot.setPrefSize(cardW, cardH);
        cardRoot.setMaxSize(cardW, cardH);
        cardRoot.setMinSize(cardW, cardH);
        cardRoot.getStyleClass().add("upgrade-card"); // 继续复用你的 hover 等类
        StackPane.setAlignment(content, Pos.CENTER);

        // 5) 把交互绑定在 cardRoot（整张卡区域）
        cardRoot.setOnMouseClicked(e -> pick(idx));
        cardRoot.setOnMouseEntered(e -> cardRoot.getStyleClass().add("hover"));
        cardRoot.setOnMouseExited(e -> cardRoot.getStyleClass().remove("hover"));

        // 6) 为了兼容你现有代码返回 VBox，这里用一个“壳”VBox 包一下（也可以把返回类型换成 StackPane）
        VBox shell = new VBox(cardRoot);
        shell.setAlignment(Pos.CENTER);
        // HBox 间距已在 cardsBox 里设了；这里不用 padding
        // 但为了和旧代码对齐，我们仍然让 shell 看起来像“卡片容器”
        shell.setPrefWidth(cardW);
        shell.setMaxWidth(cardW);

        return shell;
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
