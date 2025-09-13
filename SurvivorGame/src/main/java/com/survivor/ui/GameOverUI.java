package com.survivor.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.survivor.core.GameSceneManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
public class GameOverUI {
    private final GameSceneManager sceneManager;
    private final boolean isVictory; // 新增：胜利/失败状态

    public GameOverUI(GameSceneManager sceneManager, boolean isVictory) {
        this.sceneManager = sceneManager;
        this.isVictory = isVictory;
    }

    public Parent createContent() {
        // 创建根容器StackPane，自动将子元素居中
        StackPane root = new StackPane();
        root.setPrefSize(1280, 720);

        // 动态选择背景图
        String bgImagePath = isVictory ? "images/Victory.png" : "images/Game Over.png";
        ImageView bg = new ImageView(new Image(bgImagePath));
        bg.setFitWidth(700);
        bg.setFitHeight(700);
        bg.setMouseTransparent(true);

        // 标题
        Label title = new Label("");
        title.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 48px;");

        // 按钮
        StackPane restartButton = createImageButton("images/Restart.png", () -> {
            sceneManager.restartGame();
            sceneManager.getGameLoop().start();
        });
        StackPane exitButton = createImageButton("images/exit(1).png", () -> {
            FXGL.getGameController().exit();
        });

        // 按钮和标题放在垂直布局中
        VBox box = new VBox(40, title, restartButton, exitButton);
        box.setAlignment(Pos.CENTER);

        // 将背景和VBox放入root容器
        root.getChildren().addAll(bg, box);

        return root;
    }

    private StackPane createImageButton(String imagePath, Runnable onClick) {
        StackPane button = new StackPane();

        ImageView bg = new ImageView(new Image(imagePath));
        bg.setFitWidth(250);
        bg.setFitHeight(60);
        bg.setPickOnBounds(true); // 确保透明区域响应鼠标事件

        button.getChildren().add(bg);

        button.setOnMouseClicked(e -> onClick.run());
        button.setOnMouseEntered(e -> button.setStyle("-fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-cursor: default;"));

        return button;
    }
}

