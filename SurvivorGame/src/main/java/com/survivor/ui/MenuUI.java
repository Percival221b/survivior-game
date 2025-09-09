package com.survivor.ui;

import com.survivor.core.GameSceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class MenuUI {

    private GameSceneManager sceneManager;
    private Button startButton;
    private Button exitButton;
    private ImageView background;
    private ImageView logo;

    public MenuUI(GameSceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.startButton = new Button("Start Game");
        this.exitButton = new Button("Exit");

        // 设置按钮样式（像素风格）
        startButton.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 24; -fx-text-fill: #FFD700; -fx-background-color: #2C3E50; -fx-border-color: #FFD700; -fx-border-width: 2;");
        exitButton.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 24; -fx-text-fill: #FFD700; -fx-background-color: #2C3E50; -fx-border-color: #FFD700; -fx-border-width: 2;");

        // 设置背景和 logo
        setBackground("images/93b4daa4ea11318241a42d5a72fec5d5_720.png");
        setLogo("88images/menuUI.png");

        // 添加按钮悬停效果
        addButtonHoverEffect(startButton);
        addButtonHoverEffect(exitButton);

        // 设置按钮点击事件
        setOnStartClicked(() -> sceneManager.startGame());
        setOnExitClicked(() -> System.exit(0));
    }

    // 设置背景图片
    private void setBackground(String path) {
        background = new ImageView(new Image(path));
        background.setFitWidth(1280);  // 设置背景宽度
        background.setFitHeight(720);  // 设置背景高度
        background.setOpacity(0.8);  // 背景透明度，避免过于突出
    }

    // 设置 Logo 图片
    private void setLogo(String path) {
        logo = new ImageView(new Image(path));
        logo.setPreserveRatio(true);
        logo.setFitWidth(400);  // 设置 Logo 宽度
    }

    // 创建主菜单 UI 内容
    public Parent createContent() {
        StackPane layout = new StackPane();

        // 设置背景
        layout.getChildren().add(background);

        // 添加标题（可选）
        Text title = new Text("");
        title.setFont(Font.font("Press Start 2P", 48));
        title.setFill(Color.WHITE);

        // 设置 Logo 位置，居中显示
        StackPane.setAlignment(logo, javafx.geometry.Pos.TOP_CENTER);
        StackPane.setMargin(logo, new javafx.geometry.Insets(50, 0, 0, 0));

        // 按钮布局
        StackPane buttonLayout = new StackPane();
        buttonLayout.getChildren().addAll(startButton, exitButton);

        // 设置按钮之间的间距
        StackPane.setMargin(startButton, new Insets(0, 0, 80, 0));  // 设置 startButton 下方的间距
        StackPane.setMargin(exitButton, new Insets(80, 0, 0, 0));   // 设置 exitButton 上方的间距

        // 设置按钮布局居中
        StackPane.setAlignment(title, javafx.geometry.Pos.TOP_CENTER);
        StackPane.setAlignment(buttonLayout, javafx.geometry.Pos.CENTER);

        // 为背景添加过渡动画
        addFadeTransition(background);

        layout.getChildren().addAll(title, logo, buttonLayout);

        return layout;
    }

    // 设置“开始游戏”按钮点击回调
    public void setOnStartClicked(Runnable onStartClicked) {
        startButton.setOnAction(e -> {
            ScaleTransition scaleTransition = scaleButton(startButton, 0.9);
            scaleTransition.setOnFinished(event -> {
                // 动画完成后再执行跳转逻辑
                if (onStartClicked != null) {
                    onStartClicked.run();
                }
            });
        });
    }

    // 设置“退出”按钮点击回调
    public void setOnExitClicked(Runnable onExitClicked) {
        exitButton.setOnAction(e -> {
            ScaleTransition scaleTransition = scaleButton(exitButton, 0.9);
            scaleTransition.setOnFinished(event -> {
                // 动画完成后再执行退出逻辑
                if (onExitClicked != null) {
                    onExitClicked.run();
                }
            });
        });
    }

    // 按钮的悬停动画效果
    private void addButtonHoverEffect(Button button) {
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 24; -fx-text-fill: #FF6347; -fx-background-color: #34495E; -fx-border-color: #FF6347; -fx-border-width: 2;");
            scaleButton(button, 1.1); // 按钮悬停时放大
        });
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 24; -fx-text-fill: #FFD700; -fx-background-color: #2C3E50; -fx-border-color: #FFD700; -fx-border-width: 2;");
            scaleButton(button, 1.0); // 按钮恢复正常大小
        });
    }

    // 按钮的缩放动画（修改为返回 ScaleTransition 对象）
    private ScaleTransition scaleButton(Button button, double scale) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.2), button);
        scaleTransition.setToX(scale);
        scaleTransition.setToY(scale);
        scaleTransition.play();
        return scaleTransition;
    }

    // 为背景添加淡入过渡动画
    private void addFadeTransition(ImageView node) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
}