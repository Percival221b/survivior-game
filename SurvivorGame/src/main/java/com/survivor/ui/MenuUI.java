package com.survivor.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MenuUI {
    private Stage stage;

    public MenuUI(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        // 背景图
        Image bgImage = new Image(getClass().getResource("/images/menuUi.png").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(800);
        bgView.setFitHeight(600);

        // 标题
        Text title = new Text("Survivor Game");
        title.setFont(new Font(48));
        title.setStyle("-fx-fill: white; -fx-stroke: black; -fx-stroke-width: 2;");

        // 按钮
        Button startButton = new Button("开始游戏");
        Button exitButton = new Button("退出");

        // 点击事件
        startButton.setOnAction(e -> {
            System.out.println("进入游戏场景！");
            // 切换到游戏场景（举例：用一个新的 Scene）
            StackPane gameRoot = new StackPane(new Text("这里是游戏界面"));
            Scene gameScene = new Scene(gameRoot, 800, 600);
            stage.setScene(gameScene);
        });

        exitButton.setOnAction(e -> Platform.exit());

        // 布局（垂直放置标题和按钮）
        VBox menuBox = new VBox(20, title, startButton, exitButton);
        menuBox.setAlignment(Pos.CENTER);

        // 背景 + 菜单叠加
        StackPane root = new StackPane(bgView, menuBox);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Survivor - Main Menu");
        stage.show();
    }
}