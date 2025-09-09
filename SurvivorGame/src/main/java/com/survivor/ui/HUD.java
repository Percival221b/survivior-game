package com.survivor.ui;

import com.survivor.core.GameSceneManager;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class HUD extends StackPane {

    private ExpBar expBar;
    private ExpBar healthBar;
    private StackPane pauseMenu;
    private final GameSceneManager gameSceneManager;
    private int level = 1;  // 初始等级
    private Label levelLabel;  // 等级显示标签

    public HUD(double width, double height, double maxExp, double maxHealth, GameSceneManager gameSceneManager) {
        this.gameSceneManager = gameSceneManager;

        AnchorPane root = new AnchorPane();
        root.setPrefSize(width, height);

        // ----------------------
        // 血条（左下角）
        // ----------------------
        double healthWidth = width * 0.2;
        healthBar = new ExpBar(healthWidth, 20, maxHealth, Color.RED, true);
        AnchorPane.setBottomAnchor(healthBar, 60.0);
        AnchorPane.setLeftAnchor(healthBar, 10.0 + width * 0.094);
        root.getChildren().add(healthBar);

        // ----------------------
        // 经验条（左下角，血条下面）
        // ----------------------
        double expWidth = width * 0.8;
        expBar = new ExpBar(expWidth, 20, maxExp, Color.GOLDENROD, false);
        AnchorPane.setBottomAnchor(expBar, 30.0);
        AnchorPane.setLeftAnchor(expBar, (width - expWidth) / 2);
        root.getChildren().add(expBar);

        // ----------------------
        // 等级显示（经验条下面）
        // ----------------------
        levelLabel = new Label("等级: " + level);
        levelLabel.setStyle("-fx-text-fill: black; -fx-font-size: 16;");
        AnchorPane.setBottomAnchor(levelLabel, 5.0);  // 经验条下面
        AnchorPane.setLeftAnchor(levelLabel, (width - expWidth) / 2);  // 与经验条对齐
        root.getChildren().add(levelLabel);

        // ----------------------
        // 暂停按钮（右上角，原生 Button）
        // ----------------------
        Button pauseButton = new Button("⏸");
        pauseButton.setStyle(
                "-fx-background-color: #444;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 40px;" +
                        "-fx-min-height: 40px;"
        );
        pauseButton.setOnAction(e -> togglePauseMenu());
        AnchorPane.setTopAnchor(pauseButton, 20.0);
        AnchorPane.setRightAnchor(pauseButton, 20.0);
        root.getChildren().add(pauseButton);

        // ----------------------
        // 暂停菜单（初始隐藏，自定义按钮）
        // ----------------------
        pauseMenu = createPauseMenu(width, height);
        pauseMenu.setVisible(false);
        root.getChildren().add(pauseMenu);

        getChildren().add(root);
    }

    public StackPane createContent() {
        return this;
    }

    // -------------------------------
    // 暂停菜单
    // -------------------------------
    private StackPane createPauseMenu(double width, double height) {
        Rectangle overlay = new Rectangle(width, height);
        overlay.setFill(Color.rgb(0, 0, 0, 0.7));

        // 自定义按钮
        StackPane resumeButton = createCustomButton("", "images/Continue(1).png", this::togglePauseMenu);
        StackPane exitButton = createCustomButton("", "images/Return Menu(1).png", () -> {
            togglePauseMenu();
            gameSceneManager.showMenu();
        });

        VBox menuBox = new VBox(20, resumeButton, exitButton);
        menuBox.setAlignment(Pos.CENTER);

        return new StackPane(overlay, menuBox);
    }

    private void togglePauseMenu() {
        pauseMenu.setVisible(!pauseMenu.isVisible());
    }

    // -------------------------------
    // 条形条类（血条/经验条）
    // -------------------------------
    private static class ExpBar extends StackPane {
        private Rectangle background;
        private Rectangle bar;
        private Label text;
        private double maxValue;
        private double currentValue;
        private final boolean showFraction;

        public ExpBar(double width, double height, double maxValue, Color color, boolean showFraction) {
            this.maxValue = maxValue;
            this.currentValue = maxValue;
            this.showFraction = showFraction;

            background = new Rectangle(width, height);
            background.setFill(Color.DARKGRAY);
            background.setArcWidth(8);
            background.setArcHeight(8);

            bar = new Rectangle(width, height);
            bar.setFill(color);
            bar.setArcWidth(8);
            bar.setArcHeight(8);

            text = new Label();
            text.setTextFill(Color.WHITE);

            getChildren().addAll(background, bar, text);

            updateUI();
        }

        public void setValue(double value) {
            if (value < 0) value = 0;
            if (value > maxValue) value = maxValue;
            this.currentValue = value;
            updateUI();
        }

        public void addValue(double value) {
            setValue(this.currentValue + value);
        }

        public void setMaxValue(double maxValue) {
            if (maxValue <= 0) maxValue = 1;
            this.maxValue = maxValue;
            if (currentValue > maxValue) currentValue = maxValue;
            updateUI();
        }

        private void updateUI() {
            double progress = currentValue / maxValue;
            bar.setWidth(background.getWidth() * progress);
            if (showFraction) {
                text.setText(String.format("%.0f / %.0f", currentValue, maxValue));
            } else {
                text.setText(String.format("%.0f%%", progress * 100));
            }
        }
    }

    // -------------------------------
    // 自定义按钮（StackPane + 图片背景 + Label + 悬浮放大）
    // -------------------------------
    private StackPane createCustomButton(String text, String imagePath, Runnable onClick) {
        StackPane button = new StackPane();

        ImageView bg = new ImageView(new Image(imagePath));
        bg.setFitWidth(220);  // 拉长宽度，可调
        bg.setFitHeight(80);

        Label label = new Label(text);
        label.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 20; -fx-text-fill: #FFD700;");

        button.getChildren().addAll(bg, label);
        button.setAlignment(Pos.CENTER);

        button.setOnMouseClicked(e -> onClick.run());
        button.setOnMouseEntered(e -> scaleNode(button, 1.1));
        button.setOnMouseExited(e -> scaleNode(button, 1.0));

        return button;
    }

    private void scaleNode(StackPane node, double scale) {
        ScaleTransition st = new ScaleTransition(Duration.seconds(0.2), node);
        st.setToX(scale);
        st.setToY(scale);
        st.play();
    }

    // -------------------------------
    // 外部接口
    // -------------------------------
    public void setExp(double value) {
        double currentMaxExp = expBar.maxValue;  // 获取当前 maxExp
        if (value >= currentMaxExp) {
            level++;  // 升级
            value -= currentMaxExp;  // 重置经验（溢出部分保留）
            currentMaxExp *= 1.2;  // 每级 maxExp 增加 20%（可调整）
            expBar.setMaxValue(currentMaxExp);
            levelLabel.setText("等级: " + level);  // 更新等级显示
        }
        expBar.setValue(value);
    }

    public void addExp(double value) {
        setExp(expBar.currentValue + value);  // 通过 setExp 处理升级
    }

    public void setMaxExp(double value) { expBar.setMaxValue(value); }

    public void setHealth(double value) { healthBar.setValue(value); }
    public void addHealth(double value) { healthBar.addValue(value); }
    public void setMaxHealth(double value) { healthBar.setMaxValue(value); }
}