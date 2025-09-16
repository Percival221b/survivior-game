package com.survivor.core;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.survivor.system.ResourceLoader;

import java.util.Random;

import static com.almasb.fxgl.core.math.FXGLMath.getRandom;

public class SpawnManager {
    private double spawnTimer = 0;
    private double spawnInterval = 0.8; // 初始刷怪间隔（秒）
    private double minSpawnInterval = 0.2; // 最小间隔
    private Random random = new Random();

    // 难度随时间增加
    private double elapsedTime = 0; // 游戏运行总时间
    private double specialSpawnTime = 1; // 1分钟后生成一排怪物
    private double time1=5;

    public void update(double tpf) {
        elapsedTime += tpf;
        spawnTimer += tpf;

        // 随时间逐渐增加难度
        // 随着时间增长，spawnInterval 逐渐减小
        double difficultyFactor = Math.min(elapsedTime / 120.0, 1.0); // 0~1，120秒达到最高难度
        spawnInterval = 0.8 - 0.6 * difficultyFactor; // 最终达到 0.2 秒

        if (spawnTimer >= spawnInterval) {
            // 每次刷怪的数量也随难度增加
            int numEnemies = 1 + (int)(2 * difficultyFactor); // 1~4 个
            for (int i = 0; i < numEnemies; i++) {
                spawnEnemy();
            }

            spawnTimer = 0;
        }

//        if (elapsedTime >= specialSpawnTime) {
//
//            spawnZigzagEnemies(); // 召唤一排怪物
//            specialSpawnTime += time1;
//            if(time1>1) {time1 -=1;}
//
//        }
    }


    private void spawnEnemy() {
        var areas = ResourceLoader.getSpawnAreas();
        if (areas.isEmpty()) {
            System.out.println("没有刷怪区域！");
            return;
        }

        // 当前相机视野
        var viewport = FXGL.getGameScene().getViewport();
        double camX = viewport.getX();
        double camY = viewport.getY();
        double camW = FXGL.getAppWidth();
        double camH = FXGL.getAppHeight();

        // 镜头中心
        double camCenterX = camX + camW / 2.0;
        double camCenterY = camY + camH / 2.0;

        // 设置怪物最小距离和最大距离
        double minDist = 800;  // 怪物距离中心至少800像素
        double maxDist = 1500;  // 怪物距离中心最多1500像素

        int tries = 0;
        while (tries < 100) { // 最多尝试100次
            tries++;

            // 随机选一个刷怪区域
            var areaOpt = FXGLMath.random(areas);
            if (areaOpt.isEmpty())
                continue;

            SpawnArea area = areaOpt.get();

            // 在该区域内随机生成点
            double x = FXGLMath.random(area.x, area.x + area.width);
            double y = FXGLMath.random(area.y, area.y + area.height);

            // 计算点到镜头中心的距离
            double dx = x - camCenterX;
            double dy = y - camCenterY;
            double distance = Math.sqrt(dx*dx + dy*dy);

            // 判定是否在允许距离范围之外
            if (distance >= minDist && distance <= maxDist) {

                int roll = getRandom().nextInt(100); // 0-99

                if (roll < 40) {
                    FXGL.spawn("bat", x, y);
                } else if (roll < 70) {
                    FXGL.spawn("enemy", x, y);
                } else if (roll < 80) {
                    FXGL.spawn("ExplodedEnemy", x, y);
                } else if (roll < 98) {
                    FXGL.spawn("splitenemy", x, y);
                } else {
                    this. spawnZigzagEnemies();
                }


                //System.out.println("Spawned enemy at (" + x + ", " + y + "), distance=" + distance);
                return;
            }
        }

        System.out.println("⚠ 无法在允许范围内找到合适刷怪点");
    }

    private void spawnZigzagEnemies() {
        // 获取屏幕的宽度和高度
        double camW = FXGL.getAppWidth();
        double camH = FXGL.getAppHeight();
        var viewport = FXGL.getGameScene().getViewport();
        double camX = viewport.getX();
        double camY = viewport.getY();

        Random rand = new Random();
        Random rand0 = new Random();
        int edgeChoice = rand.nextInt(2);// 0: 左, 1: 右, 2: 上, 3: 下
        int enemyNum = rand.nextInt(5)+2;

        for (int i = 0; i < enemyNum; i++) {
            // 随机选择一个边缘来生成怪物
            double spawnX = 0;
            double spawnY = 0;

            double offsetX = i * (camW * 0.6 / enemyNum);
            double offsetY = i * (camH * 0.6 / enemyNum);

            // 根据边缘选择生成怪物的位置
            switch (edgeChoice) {
//                case 0: // 上边缘
//                    spawnX = camX + camW*0.2 + offsetX;
//                    spawnY = camY;
//                    break;
//                case 1: // 下边缘
//                    spawnX = camX + camW*0.2 + offsetX;
//                    spawnY = camY+ camH;
//                    break;
                case 0: // 左边缘
                    spawnX = camX;
                    spawnY = camY + camH * 0.2 + offsetY;
                    break;
                case 1: // 右边缘
                    spawnX = camX + camW ;
                    spawnY = camY + camH * 0.2 + offsetY;
                    break;
            }



            // 在边缘生成怪物并设置偏移
            FXGL.spawn("Armyenemy", spawnX, spawnY+300);

//            System.out.println("Spawned enemy at edge: (" + spawnX + ", " + spawnY + ")");
        }
    }





    public void reset() {
        spawnTimer = 0;
    }




}

