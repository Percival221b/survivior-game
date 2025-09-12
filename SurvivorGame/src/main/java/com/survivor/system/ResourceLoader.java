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
import com.survivor.entity.Player.XPComponent;
import com.survivor.entity.Player.PlayerMovementComponent;
import com.survivor.entity.Player.PlayerAnimationComponent;
import javafx.geometry.Point2D;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.survivor.main.EntityType;
import com.survivor.ui.HUD;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.ArrayList;

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

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        // 创建物理组件并设置为动态
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        HealthComponent health = new HealthComponent(100);
        XPComponent xp = new XPComponent();

        Entity player = FXGL.entityBuilder(data)
                .type(EntityType.PLAYER)
                .with(physics) // 使用动态物理组件
                .with(new PlayerMovementComponent()) // 移动组件
                .with(health) // 生命值组件
                .with(xp) // 经验值组件
                .with(new PlayerAnimationComponent()) // 动画组件
                .collidable()
                .scale(0.5, 0.5)
                .build();

        // 设置回调
        xp.setOnXPChange((currentXP, maxXP) -> {
            updateXPUI(currentXP, maxXP);
        });

        xp.setOnLevelUp(level -> {
            updateLevelUI(level);
        });

        health.setOnHealthChange((hp, maxHp) -> {
            updateHealthUI(hp, maxHp);
        });

        return player;
    }

    @Spawns("wall")
    public Entity newWall(SpawnData data) {
        int w = data.get("width");
        int h = data.get("height");

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);

        // 返回带有物理碰撞组件的实体
        return FXGL.entityBuilder(data)
                .type(EntityType.WALL)
                .bbox(new HitBox(new Point2D(0, 0), BoundingShape.box(w, h)))  // 设置碰撞盒子
                .with(physics)
                //.view(new Rectangle(w, h, Color.GRAY))  // 设置视图，灰色矩形作为墙体
                .collidable()  // 确保墙壁是可碰撞的
                .build();
    }

    private void updateXPUI(int currentXP, int xpToNextLevel) {
        // 通知 HUD 更新经验条
        FXGL.getGameScene().getUINodes().forEach(node -> {
            if (node instanceof HUD hud) {
                hud.setMaxExp(xpToNextLevel);
                hud.setExp(currentXP);
            }
        });
    }

    private void updateLevelUI(int level) {
        // 通知 HUD 更新等级
        FXGL.getGameScene().getUINodes().forEach(node -> {
            if (node instanceof HUD hud) {
                hud.setLevel(level); // 假设 HUD 有 setLevel 方法
            }
        });
    }

    private void updateHealthUI(int hp, int maxHp) {
        // 通知 HUD 更新血量
        FXGL.getGameScene().getUINodes().forEach(node -> {
            if (node instanceof HUD hud) {
                hud.setMaxHealth(maxHp);
                hud.setHealth(hp);
            }
        });
    }
}
