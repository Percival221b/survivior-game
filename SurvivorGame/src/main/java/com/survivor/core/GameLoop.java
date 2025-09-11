package com.survivor.core;

import com.almasb.fxgl.dsl.FXGL;

public class GameLoop {

    private double elapsedTime = 0; // 生存时间
    private boolean isRunning = false;

    private SpawnManager spawnManager;

    public GameLoop() {
        spawnManager = new SpawnManager();
    }

    // 每帧调用
    public void update(double tpf) {
        if (!isRunning) return;
        elapsedTime += tpf;
        FXGL.set("timeSurvived", elapsedTime); // 更新到全局变量，UI 可以绑定显示
        spawnManager.update(tpf);
    }

    public void reset() {
        elapsedTime = 0;
        FXGL.set("timeSurvived", elapsedTime);
        spawnManager.reset();
        isRunning = false;
        System.out.println("GameLoop reset.");
    }

    /** 启动循环（进入游戏时） */
    public void start() {
        reset();            // 每次开始都清空数据
        isRunning = true;
        System.out.println("GameLoop started.");
    }

    /** 停止循环（返回菜单时） */
    public void stop() {
        isRunning = false;
        System.out.println("GameLoop stopped.");
    }

    public void pause() {
        isRunning = false;
        System.out.println("GameLoop paused.");
    }

    public void resume() {
        isRunning = true;
        System.out.println("GameLoop resumed.");
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
