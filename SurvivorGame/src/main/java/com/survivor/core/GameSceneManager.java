package com.survivor.core;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.dsl.FXGL;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.main.EntityType;
import com.survivor.main.GameApp;
import com.survivor.system.ResourceLoader;
import com.survivor.ui.GameOverUI;
import com.survivor.ui.HUD;
import com.survivor.ui.MenuUI;

import java.net.URL;

public class GameSceneManager {

    private HUD hud;

    private final GameApplication app;
    private final UIManager uiManager;
    private final AudioManager audioManager;
    private GameLoop gameLoop;
    private ResourceLoader resourceLoader;
    private boolean gameOverShown = false;

    private final double victoryTime = 420; // 例如 5 分鐘勝利，可自行修改
    private final int countdownStart = 100;

    private boolean countdownActive = false;

    private CollisionSystem collisionSystem = new CollisionSystem();
    public GameSceneManager(GameApplication app) {
        this.app = app;
        this.uiManager = new UIManager();
        this.gameLoop = new GameLoop();
        this.audioManager = new AudioManager();
        this.resourceLoader = new ResourceLoader(this);

        // 主菜单
        MenuUI menuUI = new MenuUI(this);
        uiManager.registerUI("menu", menuUI.createContent());

        // HUD
        this.hud = new HUD(2560, 1280, 100, 10000, this);
        uiManager.registerUI("hud", this.hud.createContent());
    }

    public void showMenu() {
        uiManager.showUI("menu");
       // gameLoop.stop();
        URL resource = getClass().getResource("/sounds/Perennial_Respite_Loop.wav");
        if (resource != null) audioManager.playMusic(resource.toExternalForm());
        // 重置状态
        gameOverShown = false;
        //gameLoop.stop();

        FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                .forEach(player -> player.getComponentOptional(HealthComponent.class)
                        .ifPresent(hp -> hp.setHp(hp.getMaxHp())));
    }

    public void startGame() {
        uiManager.showUI("hud");

        URL resource = getClass().getResource("/sounds/Decimation_Loop.wav");
        if (resource != null) audioManager.playMusic(resource.toExternalForm());
        // 初始化一局
        hud.setHealth(10000);
        hud.setExp(0);
        gameLoop.start();
        gameOverShown = false;
        collisionSystem.registerCollisionHandlers();
    }

    // 重新开始游戏
    public void restartGame() {
        if (app instanceof GameApp ga) {
            hud.setHealth(10000);
            hud.setExp(0);
            hud.setLevel(1);
            ga.restartGame();
        }

        gameOverShown = false;
        gameLoop.start();
        uiManager.showUI("hud");

        URL resource = getClass().getResource("/sounds/Decimation_Loop.wav");
        if (resource != null) audioManager.playMusic(resource.toExternalForm());
    }

    // 每帧更新
    public void update(double tpf) {
        gameLoop.update(tpf);
        if (!gameLoop.isRunning() || gameOverShown) return;

        double elapsed = gameLoop.getElapsedTime();
        double remaining = victoryTime - elapsed;

        //  在最後 100 秒啟用倒計時
        if (remaining <= countdownStart && !countdownActive) {
            countdownActive = true;
            hud.startCountdown((int) remaining);
        }

        //  更新倒計時
        if (countdownActive) {
            if (remaining > 0) {
                hud.updateCountdown((int) remaining);
            } else {
                //  時間到 -> 勝利
                showEndScreen(true);
            }
        }

        //  玩家死亡 -> 失敗
        if (hud != null && hud.getHealth() <= 0) {
            //FXGL.getGameWorld().getEntitiesCopy().forEach(e -> e.removeFromWorld());
            showEndScreen(false);
        }
    }


    private void showEndScreen(boolean isVictory) {
        if (gameOverShown) return;
        gameOverShown = true;
       // gameLoop.stop();
        // 动态创建 GameOverUI，传递胜利/失败状态
        GameOverUI overUI = new GameOverUI(this, isVictory);
        uiManager.registerUI("end", overUI.createContent());
        uiManager.showUI("end");
        audioManager.stopMusic();
    }


    // Getter
    public GameLoop getGameLoop() { return gameLoop; }
    public AudioManager getAudioManager() { return audioManager; }
    public HUD getHud() { return hud; }
    public UIManager getUiManager() { return uiManager; }
    public ResourceLoader getResourceLoader() { return resourceLoader; }
}
