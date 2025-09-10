package com.survivor.core;

import com.almasb.fxgl.app.GameApplication;
import com.survivor.ui.MenuUI;
import com.survivor.ui.HUD;
import com.survivor.ui.upgrades.UpgradeOption;
import com.survivor.ui.upgrades.UpgradePanel;
import javafx.scene.image.Image;

import java.net.URL;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;
import javafx.scene.input.KeyCode;
import com.almasb.fxgl.input.UserAction;

public class GameSceneManager {

    // 字段
    private HUD hud;

    private final GameApplication app;
    private final UIManager uiManager;
    private final AudioManager audioManager;
    private GameLoop gameLoop;

    // 保存当前弹出的升级面板实例
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
        HUD hud = new HUD(1280, 720, 100, 50, this);
        hud.setOnLevelUp(this::showUpgradeChoices);   // 升级时弹出
        uiManager.registerUI("hud", hud.createContent());
    }

    public void showMenu() {
        uiManager.showUI("menu");
        URL resource = getClass().getResource("/sounds/Perennial_Respite_Loop.wav");
        if (resource != null) audioManager.playMusic(resource.toExternalForm());
    }

    public void startGame() {
        uiManager.showUI("hud");
        URL resource = getClass().getResource("/sounds/Decimation_Loop.wav");
        // 调试：U 直接打开升级面板；G 增加 150 经验
//        onKeyDown(KeyCode.U, () -> showUpgradeChoices(1));
//        onKeyDown(KeyCode.G, () -> hud.addExp(150));

        if (resource != null) audioManager.playMusic(resource.toExternalForm());
    }

    public GameLoop getGameLoop() { return gameLoop; }
    public AudioManager getAudioManager() { return audioManager; }

    // —— 升级面板 —— //
    private void showUpgradeChoices(int level) {
        getGameLoop().pause();

        var options = List.of(
                new UpgradeOption("atk_up","狂热斩击 +20%","基础攻击力 +20%，并小幅提升硬直稳定性。",
                        new Image(getClass().getResource("/images/atk.png").toExternalForm())),
                new UpgradeOption("spd_up","疾风步 +15%","移动速度 +15%，冲刺冷却 -0.5s。",
                        new Image(getClass().getResource("/images/spd.png").toExternalForm())),
                new UpgradeOption("hp_up","不屈之心 +1","最大生命 +1，并立即回复 50% 生命。",
                        new Image(getClass().getResource("/images/hp.png").toExternalForm()))
        );

        currentUpgradePanel = new UpgradePanel(options, chosen -> {
            applyUpgrade(chosen);
            uiManager.removeOverlay(currentUpgradePanel);
            getGameLoop().resume();
        });

        currentUpgradePanel.setPrefSize(1280, 720); // 覆盖全屏
        uiManager.addOverlay(currentUpgradePanel);
        currentUpgradePanel.playIn();
    }

    /** 实际应用升级（按你的数据结构改） */
    private void applyUpgrade(UpgradeOption opt) {
        switch (opt.id()) {
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
        System.out.println("Applied upgrade: " + opt.id());
    }
}

/*package com.survivor.core;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.dsl.FXGL;
import com.survivor.ui.GameOverUI;
import com.survivor.ui.HUD;
import com.survivor.ui.MenuUI;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class GameSceneManager {

    private final GameApplication app;
    private MenuUI menuUI;
    private HUD hud;

    public GameSceneManager(GameApplication app) {
        this.app = app;
        menuUI = new MenuUI();
        hud = new HUD();
    }

    // 显示主菜单
    public void showMenu() {
        FXGL.getGameScene().clearUINodes();
        menuUI.show();
    }

    // 开始游戏
    public void startGame() {
        FXGL.getGameScene().clearUINodes();
        hud.show();
    }
}*/