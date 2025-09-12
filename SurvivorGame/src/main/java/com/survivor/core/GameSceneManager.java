package com.survivor.core;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Player.XPComponent;
import com.survivor.main.EntityType;
import com.survivor.main.GameApp;
import com.survivor.system.ResourceLoader;
import com.survivor.ui.MenuUI;
import com.survivor.ui.HUD;
import com.survivor.ui.GameOverUI;
import com.survivor.ui.upgrades.UpgradeOption;
import com.survivor.ui.upgrades.UpgradePanel;
import com.survivor.ui.upgrades.UpgradeRepository;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;

import java.net.URL;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;

public class GameSceneManager {

    private HUD hud;
    private final GameApplication app;
    private final UIManager uiManager;
    private final AudioManager audioManager;
    private final GameLoop gameLoop;

    private UpgradePanel currentUpgradePanel;
    private boolean gameOverShown = false;
    private Entity player;


    public GameSceneManager(GameApplication app) {
        this.app = app;
        uiManager = new UIManager();
        gameLoop = new GameLoop();
        audioManager = new AudioManager();



        // 主菜单
        MenuUI menuUI = new MenuUI(this);
        uiManager.registerUI("menu", menuUI.createContent());

        // HUD
        this.hud = new HUD(1280, 720, 100, 50, this);
        System.out.println("HUD initialized: " + this.hud);
        uiManager.registerUI("hud", this.hud.createContent());

        FXGL.runOnce(() -> {
            // 在 HUD 初始化完成后，设置回调
            this.hud.setOnLevelUp(this::showUpgradeChoices);
        }, Duration.seconds(0.1));  // 延迟 0.1 秒执行


        // 结算界面
        GameOverUI overUI = new GameOverUI(this);
        uiManager.registerUI("gameover", overUI.createContent());
    }

    // 显示主菜单
    public void showMenu() {
        uiManager.showUI("menu");
        URL resource = getClass().getResource("/sounds/Perennial_Respite_Loop.wav");
        if (resource != null) audioManager.playMusic(resource.toExternalForm());

        // 重置状态
        gameOverShown = false;
        gameLoop.stop();

        FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                .forEach(player -> {
                    player.getComponentOptional(HealthComponent.class)
                            .ifPresent(hp -> hp.setHp(hp.getMaxHp()));
                });
    }

    // 开始新游戏
    public void startGame() {
        uiManager.showUI("hud");

        // 播放游戏BGM
        URL resource = getClass().getResource("/sounds/Decimation_Loop.wav");
        if (resource != null) audioManager.playMusic(resource.toExternalForm());

        // 初始化一局
        hud.setHealth(100);   // 血量恢复满
        hud.setExp(0);        // 经验清零
        gameLoop.start();     // 启动游戏循环
        gameOverShown = false; // 重置游戏结束标志
    }

    // 重新开始游戏
    public void restartGame() {
        // 调用 GameApp 的 restartGame 方法
        if (app instanceof GameApp ga) {
            ga.restartGame();
        }


        // 重置 HUD
        hud.setHealth(100);
        hud.setExp(0);

        // 重置游戏结束标志
        gameOverShown = false;

        // 启动游戏循环
        gameLoop.start();

        // 切回 HUD 界面
        uiManager.showUI("hud");

        // 播放游戏 BGM
        URL resource = getClass().getResource("/sounds/Decimation_Loop.wav");
        if (resource != null) audioManager.playMusic(resource.toExternalForm());
    }

    // 每帧更新（GameApplication.onUpdate 调用）
    public void update(double tpf) {
        gameLoop.update(tpf);

        if (gameOverShown) return;

        // 条件1：生存时间 >= 300秒
        if (gameLoop.getElapsedTime() >= 300) {
            showGameOver();
        }

        // 条件2：血量 <= 0
        if (hud != null && hud.getHealth() <= 0) {
            showGameOver();
        }
    }

    // 显示结算界面
    private void showGameOver() {
        if (gameOverShown) return;  // 如果已经显示过游戏结束界面，直接返回

        gameOverShown = true;
        gameLoop.stop();            // 停止游戏循环

        uiManager.showUI("gameover"); // 显示游戏结束界面
        audioManager.stopMusic();      // 停止当前音乐
    }

    public GameLoop getGameLoop() { return gameLoop; }
    public AudioManager getAudioManager() { return audioManager; }
    public HUD getHud() { return hud; }

    // —— 升级面板 —— //
    private void showUpgradeChoices(int level) {
        System.out.println("In showUpgradeChoices. Current level: " + level);
        getGameLoop().pause();

        var options = UpgradeRepository.getRandomOptions();

        currentUpgradePanel = new UpgradePanel(options, chosen -> {
            applyUpgrade(chosen);
            uiManager.removeOverlay(currentUpgradePanel);
            getGameLoop().resume();
        });

        currentUpgradePanel.setPrefSize(1280, 720);
        uiManager.addOverlay(currentUpgradePanel);
        currentUpgradePanel.playIn();
    }

    /** 安全加载图片 */
    private Image img(String path) {
        try {
            Image im = new Image(path, false);
            if (!im.isError()) return im;
        } catch (Exception ignored) {}

        try {
            String p = path.startsWith("/") ? path : ("/" + path);
            URL url = getClass().getResource(p);
            if (url != null) return new Image(url.toExternalForm());
        } catch (Exception ignored) {}

        return new WritableImage(1, 1);
    }

    /** 应用升级 */
    private void applyUpgrade(UpgradeOption opt) {
        switch (opt.getId()) {
            case "atk_up" -> {
                // TODO: 增加玩家攻击力 20%
            }
            case "spd_up" -> {
                // TODO: 移动速度 +15%，冲刺冷却 -0.5s
            }
            case "hp_up" -> {
                // TODO: 最大生命 +1，并恢复 50% 当前生命
            }
        }
        System.out.println("Applied upgrade: " + opt.getId());
    }
}
