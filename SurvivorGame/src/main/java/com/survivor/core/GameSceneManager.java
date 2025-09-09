//package com.survivor.core;
//
//import com.almasb.fxgl.app.GameApplication;
//import com.almasb.fxgl.dsl.FXGL;
//import com.survivor.ui.GameOverUI;
//import com.survivor.ui.HUD;
//import com.survivor.ui.MenuUI;
//import javafx.scene.control.Label;
//import javafx.scene.layout.StackPane;
//
//public class GameSceneManager {
//
//    private final GameApplication app;
//    private MenuUI menuUI;
//    private HUD hud;
//
//    public GameSceneManager(GameApplication app) {
//        this.app = app;
//        menuUI = new MenuUI();
//        hud = new HUD();
//    }
//
//    // 显示主菜单
//    public void showMenu() {
//        FXGL.getGameScene().clearUINodes();
//        menuUI.show();
//    }
//
//    // 开始游戏
//    public void startGame() {
//        FXGL.getGameScene().clearUINodes();
//        //hud.show();
//    }
//}