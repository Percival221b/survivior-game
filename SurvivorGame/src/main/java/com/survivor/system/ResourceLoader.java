package com.survivor.system;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.BoundingShape;
import com.survivor.core.SpawnArea;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Player.PlayerAnimationComponent;
import com.survivor.entity.Player.PlayerMovementComponent;
import com.survivor.entity.Player.XPComponent;
import com.survivor.ui.HUD;
import javafx.geometry.Point2D;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.survivor.main.EntityType;

import java.util.ArrayList;
import java.util.List;

import com.survivor.entity.ExperienceOrb;
import com.survivor.entity.HealthPotionComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class ResourceLoader implements EntityFactory {
    private static final List<SpawnArea> spawnAreas = new ArrayList<>();

    @Spawns("spawnArea")
    public Entity newSpawnArea(SpawnData data) {
        double x = data.getX();
        double y = data.getY();
        int w = data.get("width");
        int h = data.get("height");

        spawnAreas.add(new SpawnArea(x, y, w, h));

        // 返回空实体即可
        return FXGL.entityBuilder(data)
                .type(EntityType.SPAWN_AREA)
                .build();
    }

    public static List<SpawnArea> getSpawnAreas() {
        return spawnAreas;
    }

    @Spawns("wall")
    public Entity newWall(SpawnData data) {
        int w = data.get("width");
        int h = data.get("height");

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);

        return FXGL.entityBuilder(data)
                .type(EntityType.WALL)
                .bbox(new HitBox(new Point2D(0, 0), BoundingShape.box(w, h)))
                .with(physics)
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        // 创建物理组件并设置为动态
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        HealthComponent health = new HealthComponent(data);
        XPComponent xp = new XPComponent();

        Entity player =  FXGL.entityBuilder(data)
                .type(EntityType.PLAYER)
                .with(physics) // 使用动态物理组件
                .with(new PlayerMovementComponent()) // 移动组件
                .with(health) // 生命值组件
                .with(xp) // 经验值组件
                // 蓝色小球
                .with(new PlayerAnimationComponent())
                .collidable()
                .scale(0.5, 0.5)
                .build();

        // ---- 绑定到 UI ----
        FXGL.runOnce(() -> {
            // 设置初始血条
            FXGL.getGameScene().getUINodes().forEach(node -> {
                if (node instanceof HUD hud) {
                    hud.setMaxHealth(health.getMaxHp());
                    hud.setHealth(health.getHP());

                    // 如果 XPComponent 有 maxXP/currentXP 方法
                    hud.setMaxExp(xp.getXpToNextLevel());
                    hud.setExp(xp.getCurrentXP());
                }
            });
        }, Duration.seconds(0.1));  // 等待 UI 初始化完成后执行


        health.setOnHealthChange((hp, maxHp) -> {
            FXGL.getGameScene().getUINodes().forEach(node -> {
                if (node instanceof HUD hud) {
                    hud.setMaxHealth(maxHp);
                    hud.setHealth(hp);
                }
            });
        });

        xp.setOnXPChange((currentXP, maxXP) -> {
            FXGL.getGameScene().getUINodes().forEach(node -> {
                if (node instanceof HUD hud) {
                    hud.setMaxExp(maxXP);
                    hud.setExp(currentXP);
                }
            });
        });
        return player;
    }


    @Spawns("xpOrb")
    public Entity newXPOrb(SpawnData data) {
        int xpAmount = data.get("xpAmount");

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        return FXGL.entityBuilder(data)
                .type(EntityType.XP_ORB)
                .with(new ExperienceOrb(xpAmount))
                .with(physics) // 经验球也用动态，方便碰撞检测
                .view(new Circle(5, Color.LIMEGREEN))
                .collidable()
                .build();
    }

    @Spawns("healthPotion")
    public Entity newHealthPotion(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        return FXGL.entityBuilder(data)
                .type(EntityType.HEALTH_POTION)
                .with(new HealthPotionComponent())   // 回复 10% 最大血量
                .with(physics)
                .view(new Circle(7, Color.RED))      // 临时红球作为血瓶
                .collidable()
                .build();
    }
}
