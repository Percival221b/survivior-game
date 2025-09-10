package com.survivor.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
import com.almasb.fxgl.entity.Entity;
import java.net.URL;
import java.util.Map;

import com.survivor.system.ResourceLoader;
import com.survivor.core.GameLoop;
import com.survivor.core.GameSceneManager;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;

public class GameApp extends GameApplication {

    final int MAP_WIDTH = 9600;
    final int MAP_HEIGHT = 5600;
    private GameSceneManager sceneManager;
    private GameLoop gameLoop;

    private Entity player;


    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("Survivor Game");
        settings.setVersion("1.0");
        settings.setAppIcon("icon.png");
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
        sceneManager.showMenu();

        getGameWorld().addEntityFactory(new ResourceLoader());

        URL url = getClass().getResource("/assets/levels/Bigdongeonmap.tmx");
        Level baseLevel = new TMXLevelLoader().load(url, FXGL.getGameWorld());

        FXGL.getGameWorld().setLevel(baseLevel);


        //FXGL.setLevelFromMap("dongeonmap.tmx");

        // 生成玩家
        player = FXGL.spawn("player", 6000, 3000);

        FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2, FXGL.getAppHeight() / 2);
    }


    @Override
    protected void onUpdate(double tpf) {
        gameLoop.update(tpf);
        //System.out.println("Elapsed time: " + gameLoop.getElapsedTime());
    }

    /*@Override
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
    }*/

    public GameSceneManager getSceneManager() {
        return sceneManager;
    }
    public static void main(String[] args) {
        launch(args);
    }

}
