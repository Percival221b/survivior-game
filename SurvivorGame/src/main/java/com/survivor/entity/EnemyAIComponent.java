package com.survivor.entity;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.survivor.main.EntityType;
import javafx.geometry.Point2D;

public class EnemyAIComponent extends Component {

    private PhysicsComponent physics;
    private double speed = 150;  // 怪物移动速度

    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
    }

    @Override
    public void onUpdate(double tpf) {
        // 获取玩家实体
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        if (player == null) return;

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
