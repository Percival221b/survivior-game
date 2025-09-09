package com.survivor.ui;

import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

public class HUD {

    public Parent createContent() {
        StackPane layout = new StackPane();

        // 创建 HUD 内容，例如分数、时间等
        Label scoreLabel = new Label("Score: 0");
        layout.getChildren().add(scoreLabel);

        return layout;
    }
}
