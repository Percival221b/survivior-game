package com.survivor.core;

import com.almasb.fxgl.dsl.FXGL;

public class GameLoop {

    private double elapsedTime = 0; // 生存时间
    private boolean isPaused = false;

    private SpawnManager spawnManager;

    public GameLoop() {
        spawnManager = new SpawnManager();
    }

    // 每帧调用
    public void update(double tpf) {
        if (isPaused) return;
        elapsedTime += tpf;
        FXGL.set("timeSurvived", elapsedTime); // 更新到全局变量，UI 可以绑定显示
        spawnManager.update(tpf);
    }

    public void reset() {
        elapsedTime = 0;
        FXGL.set("timeSurvived", elapsedTime);
        spawnManager.reset();
        isPaused = false;
        System.out.println("GameLoop reset.");
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }
}
