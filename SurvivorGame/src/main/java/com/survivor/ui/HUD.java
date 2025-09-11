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

import java.net.URL;

public class HUD extends StackPane {

    private ExpBar expBar;
    private ExpBar healthBar;
    private StackPane pauseMenu;
    private final GameSceneManager gameSceneManager;
    private int level = 1;  // 初始等级
    private Label levelLabel;  // 等级显示标签

    // ==== 新增：升级事件回调 ====
    private java.util.function.Consumer<Integer> onLevelUp;
    public void setOnLevelUp(java.util.function.Consumer<Integer> cb) { this.onLevelUp = cb; }

    public HUD(double width, double height, double maxExp, double maxHealth, GameSceneManager gameSceneManager) {
        this.gameSceneManager = gameSceneManager;

        AnchorPane root = new AnchorPane();
        root.setPrefSize(width, height);

        // 血条（左下角）
        double healthWidth = width * 0.2;
        healthBar = new ExpBar(healthWidth, 20, maxHealth, Color.RED, true);
        AnchorPane.setBottomAnchor(healthBar, 60.0);
        AnchorPane.setLeftAnchor(healthBar, 10.0 + width * 0.094);
        root.getChildren().add(healthBar);

        // 经验条（左下角，血条下面）
        double expWidth = width * 0.8;
        expBar = new ExpBar(expWidth, 20, maxExp, Color.GOLDENROD, false);
        AnchorPane.setBottomAnchor(expBar, 30.0);
        AnchorPane.setLeftAnchor(expBar, (width - expWidth) / 2);
        root.getChildren().add(expBar);

        // 等级显示（经验条下面）
        levelLabel = new Label("等级: " + level);
        levelLabel.setStyle("-fx-text-fill: black; -fx-font-size: 16;");
        AnchorPane.setBottomAnchor(levelLabel, 5.0);
        AnchorPane.setLeftAnchor(levelLabel, (width - expWidth) / 2);
        root.getChildren().add(levelLabel);

        // 暂停按钮（右上角）
        Image pauseImage = new Image("images/table.png");
        ImageView pauseIcon = new ImageView(pauseImage);
        pauseIcon.setFitWidth(70);
        pauseIcon.setFitHeight(70);
        pauseIcon.setPreserveRatio(true);

        Button pauseButton = new Button();
        pauseButton.setGraphic(pauseIcon);
        pauseButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        pauseButton.setOnAction(e -> togglePauseMenu());
        AnchorPane.setTopAnchor(pauseButton, 25.0);
        AnchorPane.setRightAnchor(pauseButton, 20.0);
        root.getChildren().add(pauseButton);

        // 音乐按钮（暂停按钮左边）
        Image musicOnImg = new Image("images/musicOn.png");
        Image musicOffImg = new Image("images/musicOff.png");
        ImageView musicIcon = new ImageView(
                gameSceneManager.getAudioManager().isMusicEnabled() ? musicOnImg : musicOffImg
        );
        musicIcon.setFitWidth(70);
        musicIcon.setFitHeight(70);

        Button musicButton = new Button();
        musicButton.setGraphic(musicIcon);
        musicButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        musicButton.setOnAction(e -> {
            URL musicResource = getClass().getResource("/sounds/Decimation_Loop.wav");
            if (musicResource != null) {
                gameSceneManager.getAudioManager().toggleMusic(musicResource.toExternalForm());
                musicIcon.setImage(gameSceneManager.getAudioManager().isMusicEnabled() ? musicOnImg : musicOffImg);
            } else {
                System.out.println("BGM 未找到！");
            }
        });
        AnchorPane.setTopAnchor(musicButton, 25.0);
        AnchorPane.setRightAnchor(musicButton, 100.0);
        root.getChildren().add(musicButton);

        // 暂停菜单（初始隐藏）
        pauseMenu = createPauseMenu(width, height);
        pauseMenu.setVisible(false);
        root.getChildren().add(pauseMenu);

        getChildren().add(root);
        // ====== NEW: 初始化数值（血量满、经验空）======
        healthBar.setValue(maxHealth); // 血条满
        setExp(0);                     // 经验条清零（不用直接改 expBar，走统一接口）
        // ============================================
    }

    public StackPane createContent() {
        return this;
    }

    private StackPane createPauseMenu(double width, double height) {
        Rectangle overlay = new Rectangle(width, height);
        overlay.setFill(Color.rgb(0, 0, 0, 0.7));

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
        if (pauseMenu.isVisible()) {
            gameSceneManager.getGameLoop().pause();
        } else {
            gameSceneManager.getGameLoop().resume();
        }
    }

    private static class ExpBar extends StackPane {
        private Rectangle background;
        private Rectangle bar;
        private Label text;
        private double maxValue;
        private double currentValue=0;
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

    private StackPane createCustomButton(String text, String imagePath, Runnable onClick) {
        StackPane button = new StackPane();
        ImageView bg = new ImageView(new Image(imagePath));
        bg.setFitWidth(220);
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

    // 外部接口
    public void setExp(double value) {
        double currentMaxExp = expBar.maxValue;
        if (value >= currentMaxExp) {
            level++;
            value -= currentMaxExp;
            currentMaxExp *= 1.2;
            expBar.setMaxValue(currentMaxExp);
            levelLabel.setText("等级: " + level);

            // ==== 新增：触发升级回调 ====
            if (onLevelUp != null) onLevelUp.accept(level);
        }
        expBar.setValue(value);
    }

    public void addExp(double value) { setExp(expBar.currentValue + value); }
    public void setMaxExp(double value) { expBar.setMaxValue(value); }
    public void setHealth(double value) { healthBar.setValue(value); }
    public void addHealth(double value) { healthBar.addValue(value); }
    public void setMaxHealth(double value) { healthBar.setMaxValue(value); }
}
