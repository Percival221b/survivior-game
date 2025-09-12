package com.survivor.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
import com.almasb.fxgl.entity.Entity;
import java.net.URL;
import java.util.Map;

import com.almasb.fxgl.input.UserAction;
import com.survivor.debug.DebugHelper;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Player.PlayerMovementComponent;
import com.survivor.entity.Player.XPComponent;
import com.survivor.system.ResourceLoader;
import com.survivor.core.GameLoop;
import com.survivor.core.GameSceneManager;
import com.survivor.ui.HUD;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;

public class GameApp extends GameApplication {

    final int MAP_WIDTH = 9600;
    final int MAP_HEIGHT = 5600;
    private GameSceneManager sceneManager;

    static public Entity player;


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
    protected void initInput() {
        FXGL.getInput().clearAll();
        // W
        FXGL.getInput().addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerMovementComponent.class).setMovingUp(true);
            }
            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerMovementComponent.class).setMovingUp(false);
            }
        }, KeyCode.W);

        // S
        FXGL.getInput().addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerMovementComponent.class).setMovingDown(true);
            }
            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerMovementComponent.class).setMovingDown(false);
            }
        }, KeyCode.S);

        // A
        FXGL.getInput().addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerMovementComponent.class).setMovingLeft(true);
            }
            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerMovementComponent.class).setMovingLeft(false);
            }
        }, KeyCode.A);

        // D
        FXGL.getInput().addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerMovementComponent.class).setMovingRight(true);
            }
            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerMovementComponent.class).setMovingRight(false);
            }
        }, KeyCode.D);

        // 攻击 J
        FXGL.getInput().addAction(new UserAction("AttackLeft") {
            @Override
            protected void onActionBegin() {
                player.getComponent(PlayerMovementComponent.class).setAttackingLeft(true);
            }
            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerMovementComponent.class).setAttackingLeft(false);
            }
        }, KeyCode.J);

        // 攻击 K
        FXGL.getInput().addAction(new UserAction("AttackRight") {
            @Override
            protected void onActionBegin() {
                player.getComponent(PlayerMovementComponent.class).setAttackingRight(true);
            }
            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerMovementComponent.class).setAttackingRight(false);
            }
        }, KeyCode.K);

        // 冲刺 L
        FXGL.getInput().addAction(new UserAction("Dash") {
            @Override
            protected void onActionBegin() {
                player.getComponent(PlayerMovementComponent.class).startDash();
            }
        }, KeyCode.L);
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
                    .ifPresent(xp -> xp.gainXP(30));
        }, Duration.seconds(1));


        FXGL.run(() -> {
            player.getComponentOptional(com.survivor.entity.Player.HealthComponent.class)
                    .ifPresent(hp -> hp.setHp(hp.getHP() - 10));
        }, Duration.seconds(1));



        FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2, FXGL.getAppHeight() / 2);
    }


    @Override
    protected void onUpdate(double tpf) {
        sceneManager.getGameLoop().update(tpf);
        if (sceneManager != null) {
            sceneManager.update(tpf);
        }
        System.out.println("Elapsed time: " + sceneManager.getGameLoop().getElapsedTime());
        DebugHelper.printPlayerHealth();

    }


    public GameSceneManager getSceneManager() {
        return sceneManager;
    }
    public static void main(String[] args) {
        launch(args);
    }

    public void restartGame() {
        // 1. 删除所有旧实体
        FXGL.getGameWorld().getEntitiesCopy().forEach(e -> e.removeFromWorld());

        // 2. 重置游戏变量
        FXGL.set("score", 0);
        FXGL.set("timeSurvived", 0.0);

        // 3. 重新加载关卡
        URL url = getClass().getResource("/assets/levels/Bigdongeonmap.tmx");
        Level baseLevel = new TMXLevelLoader().load(url, FXGL.getGameWorld());
        FXGL.getGameWorld().setLevel(baseLevel);

        // 4. 重新生成玩家
        player = FXGL.spawn("player", 6000, 3000);

        // 5. 重置玩家血量和经验
        player.getComponentOptional(HealthComponent.class)
                .ifPresent(hp -> hp.setHp(hp.getMaxHp()));

        player.getComponentOptional(XPComponent.class)
                .ifPresent(XPComponent::reset);

        // 6. 绑定摄像机
        FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2, FXGL.getAppHeight() / 2);

        // 7. 启动游戏循环
        sceneManager.getGameLoop().start();

        // 8. 播放游戏 BGM
        URL resource = getClass().getResource("/sounds/Decimation_Loop.wav");
        if (resource != null) sceneManager.getAudioManager().playMusic(resource.toExternalForm());
    }

}
