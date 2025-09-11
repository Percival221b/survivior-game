package com.survivor.entity.factories;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.TransformComponent;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import com.survivor.entity.projectiles.*;
import com.survivor.util.EntityType;
import com.survivor.entity.projectiles.Bullet;
import javafx.scene.transform.Rotate;

public class ProjectileFactory implements EntityFactory {

    @Spawns("bullet")
    public Entity newBullet(SpawnData data) {
        Point2D startPos = data.get("startPos");
        Vec2 direction = data.get("direction");
        float speed = data.get("speed");
        float damage = data.get("damage");
        return FXGL.entityBuilder(data)
                .type(EntityType.PROJECTILE)
                .at(startPos)
                //.view() TODO设置子弹外观
                .with(new PhysicsComponent()) // 添加物理组件用于碰撞
                .with(new Bullet(startPos,direction,speed,damage)) // 添加自定义逻辑
                .collidable() // 标记为可碰撞
                .build();
    }
    @Spawns("fireX")
    public Entity newFire(SpawnData data) {
        Point2D startPos = data.get("startPos");
        Vec2 direction = data.get("direction");
        float speed = data.get("speed");
        float damage = data.get("damage");
        return FXGL.entityBuilder(data)
                .type(EntityType.PROJECTILEX)
                .at(startPos)
                //.view() TODO设置子弹外观
                .with(new PhysicsComponent()) // 添加物理组件用于碰撞
                .with(new Bullet(startPos,direction,speed,damage)) // 添加自定义逻辑
                .collidable() // 标记为可碰撞
                .build();
    }
}