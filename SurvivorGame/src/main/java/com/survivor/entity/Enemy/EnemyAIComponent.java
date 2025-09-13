package com.survivor.entity.Enemy;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.survivor.main.EntityType;
import com.survivor.main.GameApp;
import javafx.geometry.Point2D;

public class EnemyAIComponent extends Component {

    private PhysicsComponent physics;
    private double speed = 70;  // 怪物移动速度
    // 新增暂停标志
    private boolean paused = false;

    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
    }

    @Override
    public void onUpdate(double tpf) {
        // 如果游戏没在运行（暂停或停止），怪物静止
        if (!FXGL.<GameApp>getAppCast().getSceneManager().getGameLoop().isRunning()) {
            physics.setVelocityX(0);
            physics.setVelocityY(0);
            return;
        }

        // 获取玩家实体
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        if (player == null) {
            physics.setVelocityX(0);
            physics.setVelocityY(0);
            return;
        }

        // 玩家和怪物坐标
        Point2D enemyPos = entity.getCenter();
        Point2D playerPos = player.getCenter();

        // 方向向量（归一化）
        Point2D dir = playerPos.subtract(enemyPos).normalize();

        // 设置速度
        physics.setVelocityX(dir.getX() * speed);
        physics.setVelocityY(dir.getY() * speed);
    }

}
