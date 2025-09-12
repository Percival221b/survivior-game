package com.survivor.system;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.BoundingShape;
import com.survivor.core.SpawnArea;
import com.survivor.entity.EnemyAIComponent;
import com.survivor.entity.Player.*;
import com.survivor.entity.fire;
import com.survivor.main.EntityType;
import com.survivor.ui.HUD;
import javafx.geometry.Point2D;

import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;

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
                .with(new PlayerSoundComponent())
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
    @Spawns("bullet")
    public Entity newBullet(SpawnData data) {
        Point2D startPos = data.get("startPos");
        Vec2 direction = data.get("direction");
        float speed = data.get("speed");
        float damage = data.get("damage");
        return FXGL.entityBuilder(data)
                .type(com.survivor.util.EntityType.PROJECTILE)
                .at(startPos)
                //.view() TODO设置子弹外观
                .with(new PhysicsComponent()) // 添加物理组件用于碰撞

                .collidable() // 标记为可碰撞
                .build();
    }
    @Spawns("fireX")
    public Entity newFire(SpawnData data) {
        Point2D startPos = data.get("startPos");
        float speed = data.get("speed");
        float damage = data.get("damage");
        Point2D center = data.get("center");
        Point2D hitCenter = data.get("hitCenter");
        float hitRadius = data.get("hitRadius");
        Point2D offsetPos = data.get("startPos");
        return FXGL.entityBuilder(data)
                .type(com.survivor.util.EntityType.PROJECTILE)
                .at(startPos)
                //.view() TODO设置子弹外观
                .with(new PhysicsComponent()) // 添加物理组件用于碰撞
                .with(new fire(startPos,speed,damage,center,hitRadius,hitCenter,offsetPos)) // 添加自定义逻辑
                .collidable() // 标记为可碰撞
                .build();
    }
    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        return FXGL.entityBuilder(data)
                .type(com.survivor.util.EntityType.PROJECTILE)   // 记得在 EntityType 里定义 MONSTER
                .at(data.getX(), data.getY())
                .view("enemy.png")  // 你可以用一张图片，或改成 Circle/Rectangle
                .with(physics)
                .with(new EnemyAIComponent()) // 怪物AI
                .collidable()
                .build();
    }

}
