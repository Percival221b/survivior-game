package com.survivor.core;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.survivor.main.EntityType;

import java.util.Random;

public class SpawnManager {

    private double spawnTimer = 0;      // 刷怪计时器
    private double spawnInterval = 2.0; // 每隔多少秒刷一次
    private Random random = new Random();

    public void update(double tpf) {
        spawnTimer += tpf;

        if (spawnTimer >= spawnInterval) {
            spawnEnemy();
            spawnTimer = 0; // 重置计时器
        }
    }

    private void spawnEnemy() {
        // 在屏幕范围内随机生成敌人
        double x = random.nextInt(1280);
        double y = random.nextInt(720);

        Entity enemy = FXGL.entityBuilder()
                .type(EntityType.ENEMY)
                .at(x, y)
                .viewWithBBox("enemy.png")
                .buildAndAttach();

        System.out.println("Spawned enemy at (" + x + ", " + y + ")");
    }

    public void reset() {
        spawnTimer = 0;
    }
}

