package com.survivor.system;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.BoundingShape;
import com.survivor.core.GameSceneManager;
import com.survivor.core.SpawnArea;
import com.survivor.core.UIManager;
import com.survivor.entity.Enemy.EnemyComponent;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Player.PlayerAnimationComponent;
import com.survivor.entity.Player.PlayerMovementComponent;
import com.survivor.entity.Player.XPComponent;
import com.survivor.entity.Enemy.EnemyAIComponent;
import com.survivor.entity.Player.*;
import com.survivor.entity.weapon.Fire;
import com.survivor.main.EntityType;
import com.survivor.ui.HUD;
import com.survivor.ui.upgrades.UpgradeOption;
import com.survivor.ui.upgrades.UpgradePanel;
import com.survivor.ui.upgrades.UpgradeRepository;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.core.math.Vec2;
import java.util.ArrayList;
import java.util.List;

import com.survivor.entity.ExperienceOrb;
import com.survivor.entity.HealthPotionComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ResourceLoader implements EntityFactory {
    private static final List<SpawnArea> spawnAreas = new ArrayList<>();
    private UpgradePanel currentUpgradePanel;
    private final UIManager uiManager;   // ✅ 不再用 FXGL.geto
    private final GameSceneManager gsm;

    public ResourceLoader(GameSceneManager gsm) {
        this.gsm = gsm;
        this.uiManager = gsm.getUiManager();
    }

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
                //.view(new Rectangle(w, h, Color.color(1, 0, 0, 0.3))) // 红色半透明矩形
                .build();
    }


    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        // 创建物理组件并设置为动态
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        float hitBoxW = 35f;
        float hitBoxH = 50f;
        float hitBoxX = 48f;
        float hitBoxY = 48f;
        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
        rectView.setTranslateX(hitBoxX);
        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));

        HealthComponent health = new HealthComponent(100);
        XPComponent xp = new XPComponent();

        Entity player =  FXGL.entityBuilder(data)
                .type(EntityType.PLAYER)
                .with(physics) // 使用动态物理组件
                .with(new PlayerMovementComponent()) // 移动组件
                .with(health) // 生命值组件
                .with(xp) // 经验值组件
                .with(new PlayerAnimationComponent())
                .with(new PlayerSoundComponent())
                .collidable()
                .bbox(hitBox)
                //.view(rectView)
                .scale(0.5, 0.5)
                .build();

        // ---- 绑定到 UI ----
        FXGL.runOnce(() -> {
            // 设置初始血条
            FXGL.getGameScene().getUINodes().forEach(node -> {
                if (node instanceof HUD hud) {
                    hud.setMaxHealth(100);
                    hud.setHealth(health.getHP());

                    hud.setMaxExp(xp.getXpToNextLevel());
                    hud.setExp(xp.getCurrentXP());
                }
            });
        }, Duration.seconds(0.1));  // 等待 UI 初始化完成后执行

        health.setOnHealthChange(this::updateHealthUI);

        xp.setOnXPChange(this::updateXPUI);

        xp.setOnLevelUp(level -> {
            Platform.runLater(() -> {
                updateLevelUI(level);
                FXGL.getGameController().pauseEngine();

                List<UpgradeOption> options;
                try {
                    options = UpgradeRepository.getRandomOptions();
                    if (options.isEmpty()) {
                        System.err.println("No upgrade options available");
                        FXGL.getGameController().resumeEngine();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    FXGL.getGameController().resumeEngine();
                    return;
                }
                currentUpgradePanel = new UpgradePanel(options, chosen -> {
                    applyUpgrade(chosen);
                    if (currentUpgradePanel != null) {
                        uiManager.removeOverlay(currentUpgradePanel);
                    }
                    FXGL.getGameController().resumeEngine();
                });
                currentUpgradePanel.setPrefSize(1280, 720);
                uiManager.addOverlay(currentUpgradePanel);  // ✅ 用傳進來的 uiManager
                currentUpgradePanel.playIn();
                });
            });
        return player;
    }

    @Spawns("bullet")
    public Entity newBullet(SpawnData data) {
        Point2D startPos = data.get("startPos");
        Vec2 direction = data.get("direction");
        float speed = data.get("speed");
        float damage = data.get("damage");
        return FXGL.entityBuilder(data)
                .type(com.survivor.main.EntityType.PROJECTILE)
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
                .type(com.survivor.main.EntityType.PROJECTILE)
                .at(startPos)
                //.view() TODO设置子弹外观
                .with(new PhysicsComponent()) // 添加物理组件用于碰撞
                .with(new Fire(startPos,speed,damage,center,hitRadius,hitCenter,offsetPos)) // 添加自定义逻辑
                .collidable() // 标记为可碰撞
                .build();
    }

    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        float hitBoxW = 22f;
        float hitBoxH = 15f;
        float hitBoxX = 42f;
        float hitBoxY = 42f;
        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
        rectView.setTranslateX(hitBoxX);
        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));

        EnemyComponent enemyComp = new EnemyComponent(player);

        return FXGL.entityBuilder(data)
                .type(EntityType.ENEMY)
                .with(physics)
                .with(enemyComp)
                .with(new EnemyAIComponent())
                .bbox(hitBox)
                //.view(rectView)
                .scale(1.6, 1.6)
                .collidable()
                .build();
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

    private void updateLevelUI(int level) {
        FXGL.getGameScene().getUINodes().forEach(node -> {
            if (node instanceof HUD hud) {
                hud.setLevel(level);
            }
        });
    }

    private void updateHealthUI(int hp, int maxHp) {
        FXGL.getGameScene().getUINodes().forEach(node -> {
            if (node instanceof HUD hud) {
                hud.setMaxHealth(maxHp);
                hud.setHealth(hp);
            }
        });
    }

    private void updateXPUI(int currentXP, int xpToNextLevel) {
        FXGL.getGameScene().getUINodes().forEach(node -> {
            if (node instanceof HUD hud) {
                hud.setMaxExp(xpToNextLevel);
                hud.setExp(currentXP);
            }
        });
    }

    private void applyUpgrade(UpgradeOption opt) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        if (player == null) return;
        switch (opt.getId()) {
            case "atk_up" -> {
                player.getComponent(PlayerMovementComponent.class).increaseAttack(0.2);
            }
            case "spd_up" -> {
                player.getComponent(PlayerMovementComponent.class).increaseSpeed(0.1);
                player.getComponent(PlayerMovementComponent.class).decreaseDashCooldown(0.2);
            }
            case "hp_up" -> {
                player.getComponent(HealthComponent.class).increaseMaxHP(20);
                player.getComponent(HealthComponent.class).heal(50);
            }
            case "crit_up" -> {

            }
            case "regen_up" ->{
                player.getComponent(HealthComponent.class).increaseRegenHP(10);
            }
            case "aoe_up" ->{

            }
            case "tool_up" ->{

            }
            case "shield_up"->{
                player.getComponent(HealthComponent.class).increaseShield(3);
            }
            case "cooldown_up" ->{

            }
            case "xp_up"->{

            }
        }
        System.out.println("Applied upgrade: " + opt.getId());
    }

}
