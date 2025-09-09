package com.survivor.core;

import com.almasb.fxgl.app.GameApplication;
import com.survivor.ui.MenuUI;
import com.survivor.ui.HUD;
import javafx.scene.Parent;

public class GameSceneManager {

    private final GameApplication app;
    private final UIManager uiManager;

    public GameSceneManager(GameApplication app) {
        this.app = app;
        uiManager = new UIManager();

        // 创建并注册主菜单 UI
        MenuUI menuUI = new MenuUI(this);  // 将 GameSceneManager 传入 MenuUI
        uiManager.registerUI("menu", menuUI.createContent());

        // 创建并注册 HUD UI
        HUD hud = new HUD(1280, 720, 100,50,this);
        uiManager.registerUI("hud", hud.createContent());
    }

    // 显示主菜单
    public void showMenu() {
        uiManager.showUI("menu");  // 使用 UIManager 显示主菜单
    }

    // 开始游戏
    public void startGame() {
        // 这里可以添加游戏开始时的初始化代码
        uiManager.showUI("hud");  // 显示游戏 HUD
        System.out.println("Start game called");
    }
}
