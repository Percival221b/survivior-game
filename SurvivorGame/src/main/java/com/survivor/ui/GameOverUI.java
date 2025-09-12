package com.survivor.ui;

import com.survivor.core.GameSceneManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameOverUI {

    private final GameSceneManager sceneManager;

    public GameOverUI(GameSceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent createContent() {
        // 创建根容器StackPane，自动将子元素居中
        StackPane root = new StackPane();
        root.setPrefSize(1280, 720);  // 设置StackPane的宽高，确保大小

        // 背景图
        ImageView bg = new ImageView(new Image("images/Victory.png"));
        bg.setFitWidth(700);  // 根据需要设置宽度
        bg.setFitHeight(700);  // 根据需要设置高度

        // 标题
        Label title = new Label("");
        title.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 48px;");

        // 按钮
        StackPane restartButton = createImageButton("images/Restart.png", () -> {
            sceneManager.restartGame();  // 重新开始游戏
            sceneManager.getGameLoop().start();  // 启动游戏循环
        });
        StackPane exitButton = createImageButton("images/exit(1).png", () -> {
            sceneManager.showMenu();  // 返回主菜单
        });

        // 按钮和标题放在一个垂直布局中
        VBox box = new VBox(40, title, restartButton, exitButton);
        box.setAlignment(Pos.CENTER);  // 确保按钮和标题居中

        // 将背景和VBox放入root容器
        root.getChildren().addAll(bg, box);

        return root;
    }

    private StackPane createImageButton(String imagePath, Runnable onClick) {
        // 创建一个按钮，使用StackPane来保持图像
        StackPane button = new StackPane();

        ImageView bg = new ImageView(new Image(imagePath));
        bg.setFitWidth(250);  // 设置按钮的宽度
        bg.setFitHeight(60);  // 设置按钮的高度

        button.getChildren().add(bg);

        // 点击按钮时执行相应的操作
        button.setOnMouseClicked(e -> onClick.run());

        // 鼠标悬停时改变样式
        button.setOnMouseEntered(e -> button.setStyle("-fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-cursor: default;"));

        return button;
    }
}
