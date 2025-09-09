package com.survivor.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.level.Level;

import com.survivor.core.GameLoop;
import com.survivor.core.GameSceneManager;

import java.util.Map;
public class GameApp extends GameApplication {

    public enum EntityType {
        PLAYER, ENEMY, BULLET
    }
    private GameSceneManager sceneManager;
    private GameLoop gameLoop;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(960);
        settings.setHeight(640);
        settings.setTitle("Survivor Game");
        settings.setVersion("1.0");
        settings.setAppIcon("icon.png"); // 资源目录 /assets/textures/icon.png
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("timeSurvived", 0.0);
    }

    @Override
    protected void initGame() {
        gameLoop = new GameLoop();
        sceneManager = new GameSceneManager(this);
        sceneManager.showMenu(); // 启动时显示主菜单

        FXGL.setLevelFromMap("testmap.tmx");

        // 生成玩家
        FXGL.entityBuilder()
                .type(EntityType.PLAYER)
                .at(480, 360)
                .viewWithBBox("player.png")
                .with(new com.almasb.fxgl.dsl.components.KeepOnScreenComponent())
                .buildAndAttach();
    }

    @Override
    protected void onUpdate(double tpf) {
        gameLoop.update(tpf);
        System.out.println("Elapsed time: " + gameLoop.getElapsedTime());
    }

    public GameSceneManager getSceneManager() {
        return sceneManager;
    }

    @Override
    protected void initInput() {
        FXGL.onKey(javafx.scene.input.KeyCode.W, "Move Up",
                () -> FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                        .forEach(e -> e.translateY(-2)));

        FXGL.onKey(javafx.scene.input.KeyCode.S, "Move Down",
                () -> FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                        .forEach(e -> e.translateY(2)));

        FXGL.onKey(javafx.scene.input.KeyCode.A, "Move Left",
                () -> FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                        .forEach(e -> e.translateX(-2)));

        FXGL.onKey(javafx.scene.input.KeyCode.D, "Move Right",
                () -> FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                        .forEach(e -> e.translateX(2)));
    }

    public static void main(String[] args) {
        launch(args);
    }

}
