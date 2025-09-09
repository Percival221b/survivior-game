package com.survivor.ui;


import com.almasb.fxgl.dsl.FXGL;
import com.survivor.main.GameApp;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class MenuUI {

    private VBox root;

    public MenuUI() {
        root = new VBox(20);
        root.setPrefSize(1280, 720);
        root.setStyle("-fx-alignment: center; -fx-background-color: black;");

        Button startBtn = new Button("Start Game");
        startBtn.setOnAction(e -> {
            GameApp app = (GameApp) FXGL.getApp(); // ✅ 强制转换为 GameApp
            app.getSceneManager().startGame();
        });

        root.getChildren().addAll(startBtn);
    }

    public void show() {
        FXGL.getGameScene().addUINode(root);
    }
}


