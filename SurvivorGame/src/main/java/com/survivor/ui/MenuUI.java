package com.survivor.ui;

import com.survivor.core.GameSceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class MenuUI {

    private final GameSceneManager sceneManager;
    private StackPane startButton;
    private StackPane exitButton;
    private ImageView background;

    public MenuUI(GameSceneManager sceneManager) {
        this.sceneManager = sceneManager;

        // 设置背景
        setBackground("images/image (5).png");

        // 创建自定义按钮
        startButton = createCustomButton("", "images/Start Game(1).png", () -> sceneManager.startGame());
        exitButton = createCustomButton("", "images/exit(1).png", () -> System.exit(0));
    }

    private void setBackground(String path) {
        background = new ImageView(new Image(path));
        background.setFitWidth(2560);
        background.setFitHeight(1280);
        background.setOpacity(0.8);
    }

    public Parent createContent() {
        StackPane layout = new StackPane();

        // 背景
        layout.getChildren().add(background);

        // 可选标题（现在没用文字，但保留结构方便以后拓展）
        Text title = new Text("");
        title.setFont(Font.font("Press Start 2P", 48));
        title.setStyle("-fx-fill: white;");

        // 按钮布局
        VBox buttonLayout = new VBox(100, startButton, exitButton);
        buttonLayout.setAlignment(Pos.CENTER);

        // 背景淡入动画
        addFadeTransition(background);

        layout.getChildren().addAll(title, buttonLayout);

        return layout;
    }

    // -------------------------------
    // 自定义按钮（StackPane + 图片 + Label + 悬浮缩放）
    // -------------------------------
    private StackPane createCustomButton(String text, String imagePath, Runnable onClick) {
        StackPane button = new StackPane();
        button.setPrefSize(220, 80);
        button.setMaxSize(220, 80);
        button.setMinSize(220, 80);

        ImageView bg = new ImageView(new Image(imagePath));
        bg.setFitWidth(507);
        bg.setFitHeight(120);

        Label label = new Label(text);
        label.setFont(Font.font("Press Start 2P", 24));
        label.setStyle("-fx-text-fill: #FFD700;");
        label.setMouseTransparent(true); // 避免阻挡鼠标事件

        button.getChildren().addAll(bg, label);
        button.setAlignment(Pos.CENTER);

        // 点击事件
        button.setOnMouseClicked(e -> onClick.run());

        // 悬浮放大效果
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

    private void addFadeTransition(ImageView node) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
}
