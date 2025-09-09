package com.survivor.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class HUD {

    private HBox root;
    private Label scoreLabel;

    public HUD() {
        root = new HBox(20);
        root.setStyle("-fx-padding: 10; -fx-background-color: rgba(0,0,0,0.5);");

        scoreLabel = new Label("Score: 0");
        scoreLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");

        root.getChildren().add(scoreLabel);
    }

    public void show() {
        FXGL.getGameScene().addUINode(root);
    }

    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }
}

