package com.survivor.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
import com.almasb.fxgl.entity.Entity;
import java.net.URL;
import java.util.Map;

import com.survivor.entity.Player.PlayerMovementComponent;
import com.survivor.system.ResourceLoader;
import com.survivor.core.GameLoop;
import com.survivor.core.GameSceneManager;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;

public class GameApp extends GameApplication {

    final int MAP_WIDTH = 9600;
    final int MAP_HEIGHT = 5600;
    private GameSceneManager sceneManager;

    private Entity player;


    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("Survivor Game");
        settings.setVersion("1.0");
        settings.setAppIcon("icon.png");
        settings.setTicksPerSecond(120);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("timeSurvived", 0.0);
    }

    @Override
    protected void initPhysics() {
        // 关闭重力（顶视角）
        FXGL.getPhysicsWorld().setGravity(0, 0);
    }

    @Override
    protected void initGame() {
        sceneManager = new GameSceneManager(this);
        sceneManager.showMenu();

        getGameWorld().addEntityFactory(new ResourceLoader());

        URL url = getClass().getResource("/assets/levels/Bigdongeonmap.tmx");
        Level baseLevel = new TMXLevelLoader().load(url, FXGL.getGameWorld());

        FXGL.getGameWorld().setLevel(baseLevel);

        // 生成玩家
        player = FXGL.spawn("player", 6000, 3000);

        FXGL.run(() -> {
            player.getComponentOptional(com.survivor.entity.Player.XPComponent.class)
                    .ifPresent(xp -> xp.gainXP(10));
        }, Duration.seconds(1));

        FXGL.run(() -> {
            player.getComponentOptional(com.survivor.entity.Player.HealthComponent.class)
                    .ifPresent(hp -> hp.setHp(hp.getHP() - 1));
        }, Duration.seconds(1));

        FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2, FXGL.getAppHeight() / 2);
    }


    @Override
    protected void onUpdate(double tpf) {
        sceneManager.getGameLoop().update(tpf);
        if(!sceneManager.getGameLoop().isRunning()){player.getComponent(PlayerMovementComponent.class).setPaused(true);}
        else {player.getComponent(PlayerMovementComponent.class).setPaused(false);}
        System.out.println("Elapsed time: " + sceneManager.getGameLoop().getElapsedTime());
    }


    public GameSceneManager getSceneManager() {
        return sceneManager;
    }
    public static void main(String[] args) {
        launch(args);
    }

}
