package com.survivor.core;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Player.PlayerMovementComponent;
import com.survivor.main.EntityType;
import com.survivor.ui.HUD;
import com.survivor.ui.MenuUI;
import com.survivor.ui.upgrades.UpgradeOption;
import com.survivor.ui.upgrades.UpgradePanel;
import com.survivor.ui.upgrades.UpgradeRepository;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.net.URL;

public class GameSceneManager {

    private HUD hud;

    private final GameApplication app;
    private final UIManager uiManager;
    private final AudioManager audioManager;
    private GameLoop gameLoop;

    private UpgradePanel currentUpgradePanel;

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
        this.hud.setOnLevelUp(this::showUpgradeChoices);
        uiManager.registerUI("hud", this.hud.createContent());
    }

    public void showMenu() {
        uiManager.showUI("menu");
        gameLoop.stop();
        URL resource = getClass().getResource("/sounds/Perennial_Respite_Loop.wav");
        if (resource != null) audioManager.playMusic(resource.toExternalForm());
    }

    public void startGame() {
        uiManager.showUI("hud");

        URL resource = getClass().getResource("/sounds/Decimation_Loop.wav");
        if (resource != null) audioManager.playMusic(resource.toExternalForm());
        gameLoop.start();
    }

    public GameLoop getGameLoop() { return gameLoop; }
    public AudioManager getAudioManager() { return audioManager; }

    // —— 升级面板 —— //
    private void showUpgradeChoices(int level) {
        getGameLoop().pause();

        var options = UpgradeRepository.getRandomOptions();

        currentUpgradePanel = new UpgradePanel(options, chosen -> {
            applyUpgrade(chosen);
            uiManager.removeOverlay(currentUpgradePanel);
            getGameLoop().resume();
        });

        currentUpgradePanel.setPrefSize(1280, 720); // 覆盖全屏
        uiManager.addOverlay(currentUpgradePanel);
        currentUpgradePanel.playIn();
    }

    private Image img(String path) {
        try {
            Image im = new Image(path, false);     // 试 1：直接用字符串路径
            if (!im.isError()) return im;
        } catch (Exception ignored) {}

        try {                                      // 试 2：用 getResource 找 URL
            String p = path.startsWith("/") ? path : ("/" + path);
            URL url = getClass().getResource(p);
            if (url != null) return new Image(url.toExternalForm());
        } catch (Exception ignored) {}

        return new WritableImage(1, 1);            // 兜底：1×1 透明像素，避免 NPE
    }



    /** 实际应用升级（按你的数据结构改） */
    private void applyUpgrade(UpgradeOption opt) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        if (player == null) return;
        switch (opt.getId()) {
            case "atk_up" -> {
                player.getComponent(PlayerMovementComponent.class).increaseAttack(0.2);
            }
            case "spd_up" -> {
                player.getComponent(PlayerMovementComponent.class).increaseSpeed(0.1);
                player.getComponent(PlayerMovementComponent.class).decreaseDashCooldown(0.2);
            }
            case "hp_up" -> {
                player.getComponent(HealthComponent.class).increaseMaxHP(20);
                player.getComponent(HealthComponent.class).heal(50);
            }
            case "crit_up" -> {

            }
            case "regen_up" ->{
                player.getComponent(HealthComponent.class).increaseRegenHP(10);
            }
            case "aoe_up" ->{

            }
            case "tool_up" ->{

            }
            case "shield_up"->{
                player.getComponent(HealthComponent.class).increaseShield(3);
            }
            case "cooldown_up" ->{

            }
            case "xp_up"->{

            }
        }
        System.out.println("Applied upgrade: " + opt.getId());
    }
}
