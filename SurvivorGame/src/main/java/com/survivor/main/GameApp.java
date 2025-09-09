package com.survivor.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.survivor.core.GameSceneManager;
import java.util.Map;

public class GameApp extends GameApplication {

    private GameSceneManager sceneManager;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("Survivor Game");
        settings.setVersion("0.1");
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("timeSurvived", 0.0);
    }

    @Override
    protected void initGame() {
        sceneManager = new GameSceneManager(this);
        sceneManager.showMenu();  // 启动时显示主菜单
    }

    public static void main(String[] args) {
        launch(args); // 启动 JavaFX 应用
    }
}
