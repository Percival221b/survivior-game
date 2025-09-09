package com.survivor.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.dsl.FXGL;

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
        settings.setAppIcon("icon.png"); // 资源目录 /assets/textures/icon.png
    }

    public enum EntityType {
        PLAYER, ENEMY, BULLET
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("timeSurvived", 0.0);
    }

    @Override
    protected void initGame() {

        sceneManager = new GameSceneManager(this);
        sceneManager.showMenu(); // 启动时显示主菜单

        // 生成玩家
        /*FXGL.entityBuilder()
                .type(EntityType.PLAYER)
                .at(600, 350)
                .viewWithBBox("player.png")
                .with(new com.almasb.fxgl.dsl.components.KeepOnScreenComponent())
                .buildAndAttach();*/
    }

    public GameSceneManager getSceneManager() {
        return sceneManager;
    }

    @Override
    protected void initInput() {
        FXGL.onKey(javafx.scene.input.KeyCode.W, "Move Up",
                () -> FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                        .forEach(e -> e.translateY(-1)));

        FXGL.onKey(javafx.scene.input.KeyCode.S, "Move Down",
                () -> FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                        .forEach(e -> e.translateY(1)));

        FXGL.onKey(javafx.scene.input.KeyCode.A, "Move Left",
                () -> FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                        .forEach(e -> e.translateX(-1)));

        FXGL.onKey(javafx.scene.input.KeyCode.D, "Move Right",
                () -> FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                        .forEach(e -> e.translateX(1)));
    }

    public static void main(String[] args) {
        launch(args); // This calls JavaFX Application.launch()
    }

}
