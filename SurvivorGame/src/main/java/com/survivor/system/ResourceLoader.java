package com.survivor.system;

import com.almasb.fxgl.core.math.Vec2;
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
import com.survivor.entity.*;
import com.survivor.entity.Player.*;
import com.survivor.main.EntityType;
import com.survivor.ui.HUD;
import com.survivor.ui.upgrades.UpgradeOption;
import com.survivor.ui.upgrades.UpgradePanel;
import com.survivor.ui.upgrades.UpgradeRepository;
import javafx.application.Platform;
import javafx.geometry.Point2D;

import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


public class ResourceLoader implements EntityFactory {
    private static final List<SpawnArea> spawnAreas = new ArrayList<>();
    private UpgradePanel currentUpgradePanel;

    private final UIManager uiManager;   // ✅ 不再用 FXGL.geto
    private final GameSceneManager gsm;
    private void updateXPUI(int currentXP, int xpToNextLevel) {
            FXGL.getGameScene().getUINodes().forEach(node -> {
            if (node instanceof HUD hud) {
                hud.setMaxExp(xpToNextLevel);
                hud.setExp(currentXP);
            }
        });
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

    public ResourceLoader(GameSceneManager gsm) {
        this.gsm = gsm;
        this.uiManager = gsm.getUiManager();
    }
    private static void applyUpgrade(UpgradeOption chosen) {
        System.out.println("Applying upgrade: " + chosen.getTitle());
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        if (player != null) {
            String upgradeId = chosen.getId();
            if ("health_boost".equals(upgradeId)) {
                player.getComponent(HealthComponent.class).increaseMaxHP(50);
            } else if ("speed_boost".equals(upgradeId)) {
                player.getComponent(PlayerMovementComponent.class).setSpeed(1.2);
            }
        }
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
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
//
//        float hitBoxW = 35f;
//        float hitBoxH = 50f;
//        float hitBoxX = 48f;
//        float hitBoxY = 48f;
//        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
//        rectView.setTranslateX(hitBoxX);
//        rectView.setTranslateY(hitBoxY);
//        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));

        HealthComponent health = new HealthComponent(data);
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
                //.bbox(hitBox)
                //.view(rectView)
                .scale(0.5, 0.5)
                .build();

        // 綁定 XP -> HUD
        xp.setOnXPChange(this::updateXPUI);

        xp.setOnLevelUp(level -> {
            Platform.runLater(() -> {
                System.out.println("333333333333333333333333333333333333333333333333333333333333333333333333333333333");
                updateLevelUI(level);
                FXGL.getGameController().pauseEngine();
                System.out.println("111111111111111111111111111111111111111111111111111111111111111111111111111111111");
                List<UpgradeOption> options;
                try {
                    System.out.println("222222222222222222222222222222222222222222222222222222222222222222222222222222");
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
                uiManager.addOverlay(currentUpgradePanel);  //  用傳進來的 uiManager
                currentUpgradePanel.playIn();
            });
        });

        // 綁定 HP -> HUD
        health.setOnHealthChange(this::updateHealthUI);

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
    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        float hitBoxW = 35f;
        float hitBoxH = 70f;

// 碰撞箱居中在图像 (250x250 假设 Boss 贴图大小)
        float hitBoxX =  - hitBoxW / 2f;
        float hitBoxY =   - hitBoxH / 2f;

        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
        rectView.setTranslateX(hitBoxX);
        rectView.setTranslateY(hitBoxY);

        HitBox hitBox = new HitBox(new Point2D(hitBoxX, hitBoxY), BoundingShape.box(hitBoxW, hitBoxH));
        return FXGL.entityBuilder(data)
                .type(com.survivor.util.EntityType.MONSTER)   // 记得在 EntityType 里定义 MONSTER
                .at(data.getX(), data.getY())
                .with(new BossComponent())
                .view(rectView)
                .bbox(hitBox)
                .with(physics)
                .with(new EnemyAIComponent()) // 怪物AI
                .collidable()
                .build();
    }


    @Spawns("fireEnemy")
    public Entity newFireEnemy(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        Point2D startPos = data.get("startPos");
        float speed = data.get("speed");
        float damage = data.get("damage");
        Point2D center = data.get("center");
        Point2D hitCenter = data.get("hitCenter");
        Point2D offsetPos = data.get("offsetPos");
        float hitRadius = data.get("hitRadius");
        float duration = data.get("duration");
        Circle circleView = new Circle(hitRadius, Color.BLUE);
        circleView.setTranslateX(hitCenter.subtract(startPos).getX()); // 视图在实体x轴上向右偏移10像素
        circleView.setTranslateY(hitCenter.subtract(startPos).getY());
        return FXGL.entityBuilder(data)
                .type(com.survivor.util.EntityType.PROJECTILEENEMY)
                .at(startPos)
                .view(circleView)
                .with(physics) // 添加物理组件用于碰撞
                .with(new fire( speed, damage, center, hitRadius, hitCenter, offsetPos,duration)) // 添加自定义逻辑
                .collidable() // 标记为可碰撞
                .build();
    }

//    @Spawns("blade")
//    public Entity newBlade(SpawnData data) {
//        PhysicsComponent physics = new PhysicsComponent();
//        physics.setBodyType(BodyType.DYNAMIC);
//        Point2D startPos = data.get("startPos");
//        float damage = data.get("damage");
//        Point2D hitCenter = data.get("hitCenter");
//        Point2D offsetPos = data.get("offsetPos");
//        float hitRadius = data.get("hitRadius");
//        float duration = data.get("duration");
//        Circle circleView = new Circle(hitRadius, Color.RED);
//        circleView.setTranslateX(hitCenter.subtract(startPos).getX()); // 视图在实体x轴上向右偏移10像素
//        circleView.setTranslateY(hitCenter.subtract(startPos).getY());
//        return FXGL.entityBuilder(data)
//                .type(com.survivor.util.EntityType.PROJECTILE)
//                .at(startPos)
//                .view(circleView)
//                .with(physics) // 添加物理组件用于碰撞
//                .with(new Blade( damage, hitRadius, hitCenter, offsetPos, duration)) // 添加自定义逻辑
//                .collidable() // 标记为可碰撞
//                .build();
//    }
@Spawns("blade")
public Entity newBlade(SpawnData data) {
    PhysicsComponent physics = new PhysicsComponent();
    physics.setBodyType(BodyType.DYNAMIC);

    float hitRadius = data.get("hitRadius");
    Point2D startPos = data.get("startPos");
    Point2D hitCenter = data.get("hitCenter");
    Circle circleView = new Circle(hitRadius, Color.RED);
    circleView.setTranslateX(hitCenter.getX()); // 视图在实体x轴上向右偏移10像素
    circleView.setTranslateY(hitCenter.getY());
    return FXGL.entityBuilder(data)
            .type(com.survivor.util.EntityType.PROJECTILE)
            .at(startPos)
            .view(circleView)
            //  不要 collidable，传感器不需要
            .with(physics)
            .with(new Blade(
                    data.get("damage"),
                    hitRadius,
                    data.get("hitCenter"),
                    data.get("offsetPos"),
                    data.get("duration")
            ))
            .build();
}



//    @Spawns("magicAttack")
//    public Entity newMagicAttackEnemy(SpawnData data) {
//        PhysicsComponent physics = new PhysicsComponent();
//        physics.setBodyType(BodyType.STATIC);
//        Point2D startPos = data.get("startPos");
//        float damage = data.get("damage");
//        Point2D hitCenter = data.get("hitCenter");
//        Point2D offsetPos = data.get("offsetPos");
//        float hitRadius = data.get("hitRadius");
//        Circle circleView = new Circle(hitRadius, Color.BLUE);
//        circleView.setTranslateX(hitCenter.subtract(startPos).getX()); // 视图在实体x轴上向右偏移10像素
//        circleView.setTranslateY(hitCenter.subtract(startPos).getY());
//        return FXGL.entityBuilder(data)
//                .type(com.survivor.util.EntityType.PROJECTILEENEMY)
//                .at(startPos)
//                .view(circleView)
//                .with(physics) // 添加物理组件用于碰撞
//                .with(new MagicAttack(  damage, hitRadius, hitCenter, offsetPos)) // 添加自定义逻辑
//                .collidable() // 标记为可碰撞
//                .build();
//    }


}
