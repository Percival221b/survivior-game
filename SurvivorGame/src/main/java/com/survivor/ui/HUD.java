package com.survivor.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.survivor.core.GameSceneManager;
import com.survivor.entity.Player.XPComponent;
import com.survivor.main.EntityType;
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
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

import static com.sun.media.jfxmedia.logging.Logger.setLevel;

public class HUD extends StackPane {

    private ExpBar expBar;
    private ExpBar healthBar;
    private StackPane pauseMenu;
    private final GameSceneManager gameSceneManager;
    private int level = 1;  // 初始等级
    private Label levelLabel;  // 等级显示标签
    private Text countdownText;
    private boolean countdownVisible = false;
    private double countdownTime = 0;
    Font customFont = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/antiquity-print.ttf"), 100);
    AnchorPane root = new AnchorPane();
    // ==== 新增：升级事件回调 ====
    private java.util.function.Consumer<Integer> onLevelUp;
    public void setOnLevelUp(Consumer<Integer> callback) {
        Optional<Entity> playerOpt = FXGL.getGameWorld()
                .getEntitiesByType(EntityType.PLAYER)  // 返回 List<Entity>
                .stream()
                .findFirst();  // 返回 Optional<Entity>，获取第一个

        if (playerOpt.isPresent()) {
            Entity player = playerOpt.get();
            XPComponent xp = player.getComponent(XPComponent.class);
            if (xp != null) {
                xp.setOnLevelUp(level -> {
                    setLevel(level);  // 更新 UI（可选链式）
                    if (callback != null) {
                        callback.accept(level);  // 转发到 showUpgradeChoices
                    }
                });
            } else {
                System.err.println("XPComponent not found on player!");
            }
        } else {
            System.err.println("No player entity found in world!");
        }
    }


    public HUD(double width, double height, double maxExp, double maxHealth, GameSceneManager gameSceneManager) {
        this.gameSceneManager = gameSceneManager;


        root.setPrefSize(width, height);

        // 血条（左下角）
        double healthWidth = width * 0.5;
        healthBar = new ExpBar(healthWidth, 40, maxHealth, Color.RED, true);
        AnchorPane.setBottomAnchor(healthBar, 60.0);
        AnchorPane.setLeftAnchor(healthBar, 10.0 + width * 0.094);
        root.getChildren().add(healthBar);

        // 经验条（左下角，血条下面）
        double expWidth = width * 0.8;
        expBar = new ExpBar(expWidth, 60, maxExp, Color.GOLDENROD, false);
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
        pauseIcon.setFitWidth(140);
        pauseIcon.setFitHeight(140);
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
        musicIcon.setFitWidth(140);
        musicIcon.setFitHeight(140);

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
        AnchorPane.setRightAnchor(musicButton, 200.0);
        root.getChildren().add(musicButton);

        // 暂停菜单（初始隐藏）
        pauseMenu = createPauseMenu(width, height);
        pauseMenu.setVisible(false);
        root.getChildren().add(pauseMenu);

        getChildren().add(root);

        healthBar.setValue(maxHealth); // 血条满
        setExp(0);                     // 经验条清零（不用直接改 expBar，走统一接口）

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
        private double currentValue = 0;

        private ImageView healthView;
        private double fullWidth = 1400;   // healthbar.png 原始宽度
        private double barHeight = 150;   // 图片高度
        private final boolean showFraction;


        public ExpBar(double width, double height, double maxValue, Color color, boolean showFraction) {
            this.maxValue = maxValue;
            this.currentValue = maxValue;
            this.showFraction = showFraction;

            if (showFraction) {
                healthView = new ImageView(new Image(
                        getClass().getResource("/images/healthbar.png").toExternalForm()
                ));
                healthView.setFitHeight(barHeight);
                healthView.setPreserveRatio(false);

                text = new Label();
                text.setTextFill(Color.WHITE);

                getChildren().addAll(healthView, text);
                updateUI();
            } else {
                // ========= 原经验条 =========
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
            }

            updateUI();
        }

        public void setValue(double value) {
            if (value < 0) value = 0;
            if (value > maxValue) value = maxValue;
            this.currentValue = value;
            updateUI();
        }
        public double getValue() {
            return currentValue;
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
            if (showFraction) {
                // ==== 血条：用 viewport 裁剪 ====
                double visibleWidth = fullWidth * progress;

                if (healthView != null) {
                    healthView.setViewport(new javafx.geometry.Rectangle2D(0, 0, visibleWidth, barHeight));
                }
                healthView.setTranslateX(0);

                if (text != null) {
                    text.setText(String.format("%.0f / %.0f", currentValue, maxValue));
                }
            } else {
                // ==== 经验条：用矩形 ====
                double barWidth = background.getWidth() * progress;
                bar.setWidth(barWidth);
                if (text != null) {
                    text.setText(String.format("%.0f%%", progress * 100));
                }
            }
        }
    }

    private StackPane createCustomButton(String text, String imagePath, Runnable onClick) {
        StackPane button = new StackPane();
        ImageView bg = new ImageView(new Image(imagePath));
        bg.setFitWidth(507);
        bg.setFitHeight(120);

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
    public void setLevel(int level) {
        levelLabel.setText("等级: " + level);
    }

    public void reset() {
        level = 1;
        levelLabel.setText("等级: 1");

        setExp(0);
    }
    public void startCountdown(double seconds) {
        if (countdownText == null) {
            countdownText = new Text();
            countdownText.setFill(Color.RED);
            countdownText.setFont(customFont);;
            // 固定上方
            AnchorPane.setTopAnchor(countdownText,0.0);
            countdownText.setLayoutX(root.getPrefWidth() / 2 - 30);
            root.getChildren().add(countdownText);
        }
        countdownVisible = true;
        countdownTime = seconds;
        updateCountdown((int) countdownTime);
    }

    public void hideCountdown() {
        if (countdownText != null) {
            countdownText.setVisible(false);   // 隱藏
            countdownVisible = false;
        }
    }


    public void updateCountdown(int secondsLeft) {
        if (countdownText != null) {
            countdownText.setText(String.valueOf(secondsLeft));
        }
    }

    // 每幀更新（GameLoop 調用）
    public void onUpdate(double tpf) {
        if (countdownVisible && countdownTime > 0) {
            countdownTime -= tpf;
            if (countdownTime < 0) countdownTime = 0;
            updateCountdown((int) countdownTime);
        }
    }

    // 获取当前血量
    public double getHealth() {
        return healthBar.getValue();
    }

    // 获取当前经验
    public double getExp() {
        return expBar.getValue();
    }
    public void addExp(double value) { setExp(expBar.currentValue + value); }
    public void setMaxExp(double value) { expBar.setMaxValue(value); }
    public void setHealth(double value) { healthBar.setValue(value); }
    public void addHealth(double value) { healthBar.addValue(value); }
    public void setMaxHealth(double value) { healthBar.setMaxValue(value); }
}
