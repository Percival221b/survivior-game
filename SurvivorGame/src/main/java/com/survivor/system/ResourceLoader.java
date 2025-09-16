package com.survivor.system;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.survivor.core.GameSceneManager;
import com.survivor.core.SpawnArea;
import com.survivor.core.UIManager;
import com.survivor.core.spawnTile;
import com.survivor.entity.Enemy.*;
import com.survivor.entity.Enemy.test.SmartEnemyAI;
import com.survivor.entity.weapon.*;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Player.PlayerAnimationComponent;
import com.survivor.entity.Player.PlayerMovementComponent;
import com.survivor.entity.Player.XPComponent;
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
import com.almasb.fxgl.core.math.Vec2;
import java.util.ArrayList;
import java.util.List;
import com.survivor.util.aStarGrid;

import com.survivor.entity.ExperienceOrb;
import com.survivor.entity.HealthPotionComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ResourceLoader implements EntityFactory {
    private static final List<SpawnArea> spawnAreas = new ArrayList<>();
    private UpgradePanel currentUpgradePanel;

    private final UIManager uiManager;
    private final GameSceneManager gsm;

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
                .with(new spawnTile(w,h))
                //.view(new Rectangle(w, h, Color.color(1, 0, 0, 0.3))) // 红色半透明矩形
                .build();
    }



    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);


        physics.setFixtureDef(new FixtureDef()
                .density(50.f)   // 密度越大，质量越大// 弹性
                .restitution(0.0f)
        );


//        float hitBoxW = 35f;
//        float hitBoxH = 50f;
        float hitBoxW = 40f;
        float hitBoxH = 100f;
        float hitBoxX = 48f;
        float hitBoxY = 35f;
        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
        rectView.setTranslateX(hitBoxX);
        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));

        HealthComponent health = new HealthComponent(1000000);
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
                .zIndex(150)
                .view(rectView)
                .scale(0.5, 0.5)
                .build();

        // 綁定 XP -> HUD
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

    @Spawns("slime_enemy")
    public Entity slimeEnemy(SpawnData data) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef()
                .density(50.f)   // 密度越大，质量越大// 弹性
                .restitution(0.0f)
        );

        float hitBoxW =  10f;
        float hitBoxH = 16f;
        float hitBoxX = -5f;
        float hitBoxY = -8f;
        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
        rectView.setTranslateX(hitBoxX);
        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));



        return FXGL.entityBuilder(data)
                .type(EntityType.ENEMY)
                .with(physics)
                .with(new SlimeEnemyComponent())
//                 .with(new EnemyAIComponent())
//                .bbox(hitBox)
                .view(rectView)
                .scale(1.6, 1.6)
                .collidable()
                .build();
    }

    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        float hitBoxW = 22f;
        float hitBoxH = 15f;
        float hitBoxX = -100f;
        float hitBoxY = -100f;
        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
        rectView.setTranslateX(hitBoxX);
        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));



        return FXGL.entityBuilder(data)
                .type(EntityType.ENEMY)
                .with(physics)
                .with(new RangedEnemyComponent())
                // .with(new EnemyAIComponent())
                .bbox(hitBox)
                //.view(rectView)
                .scale(1.5, 1.5)
                .zIndex(110)
                .collidable()
                .build();
    }

    @Spawns("bat")
    public Entity Bat(SpawnData data) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        float hitBoxW = 20f;
        float hitBoxH = 10f;
        float hitBoxX = -100f;
        float hitBoxY = -100f;
        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
        rectView.setTranslateX(hitBoxX);
        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));



        return FXGL.entityBuilder(data)
                .type(EntityType.ENEMY)
                .with(physics)
                .with(new SprintEnemyCompontBat())
                // .with(new EnemyAIComponent())
                .bbox(hitBox)
               // .view(rectView)
                .scale(1.6, 1.6)
                .collidable()
                .zIndex(110)
                .build();
    }

    @Spawns("AIEnemy")
    public Entity newAIEnemy(SpawnData data) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        float hitBoxW = 25f;
        float hitBoxH = 17f;
        float hitBoxX = 30f;
        float hitBoxY = 40f;
        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
        rectView.setTranslateX(hitBoxX);
        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));


        return FXGL.entityBuilder(data)
                .type(EntityType.AIENEMY)
                .with(physics)
                .with(new SmartEnemyAI())
                .bbox(hitBox)
                //.view(rectView)
                .scale(1.5, 1.5)
                .collidable()
                .build();
    }

    @Spawns("splitenemy")
    public Entity newsplitEnemy(SpawnData data) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef()
                .density(80.f)   // 密度越大，质量越大// 弹性
                .restitution(0.0f)
        );

        float hitBoxW = 27f;
        float hitBoxH = 30f;
        float hitBoxX = -90f;
        float hitBoxY = -95f;
        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
        rectView.setTranslateX(hitBoxX);
        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));

        return FXGL.entityBuilder(data)
                .type(EntityType.ENEMY)
                .with(physics)
                .with(new SplitEnemyComponent())
                // .with(new EnemyAIComponent())
                .bbox(hitBox)
               // .view(rectView)
                .scale(3, 3)
                .zIndex(110)
                .collidable()
                .build();
    }

    @Spawns("small_enemy")
    public Entity samllEnemy(SpawnData data) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef()
                .density(50.f)   // 密度越大，质量越大// 弹性
                .restitution(0.0f)
        );

        float hitBoxW = 27f;
        float hitBoxH = 30f;
        float hitBoxX = -90f;
        float hitBoxY = -95f;
        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
        rectView.setTranslateX(hitBoxX);
        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));



        return FXGL.entityBuilder(data)
                .type(EntityType.ENEMY)
                .with(physics)
                .with(new SmallSplitEnemyComponent())
                // .with(new EnemyAIComponent())
                .bbox(hitBox)
               // .view(rectView)
                .scale(1.6, 1.6)
                .zIndex(110)
                .collidable()
                .build();
    }

    @Spawns("Armyenemy")
    public Entity AEnemy(SpawnData data) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef()
                .density(800.f)   // 密度越大，质量越大// 弹性
                .restitution(0.0f)
        );

        float hitBoxW = 38f;
        float hitBoxH = 25f;
        float hitBoxX = -100f;
        float hitBoxY = -90f;
        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
        rectView.setTranslateX(hitBoxX);
        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));

        return FXGL.entityBuilder(data)
                .type(EntityType.ENEMY)
                .with(physics)
                .with(new zhiEnemyComponent())
                // .with(new EnemyAIComponent())
                //.bbox(hitBox)
             //   .view(rectView)
                .scale(2, 2)
                .zIndex(110)
                .collidable()
                .build();
    }

    @Spawns("ExplodedEnemy")
    public Entity newExplodingEnemy(SpawnData data) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef()
                .density(60.f)   // 密度越大，质量越大// 弹性
                .restitution(0.0f)
        );

        float hitBoxW =40f;
        float hitBoxH = 40f;
        float hitBoxX = -100f;
        float hitBoxY = -90f;
        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
        rectView.setTranslateX(hitBoxX);
        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));



        return FXGL.entityBuilder(data)
                .type(EntityType.ENEMY)
                .with(physics)
                .with(new SelfExplodingEnemyComponent())
                .bbox(hitBox)
                //.view(rectView)
                .scale(1.5, 1.5)
                .zIndex(110)
                .collidable()
                .build();
    }

    @Spawns("bomb")
    public Entity newBomb(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        float hitRadius = data.get("hitRadius");
        Point2D startPos = data.get("startPos");
        Point2D hitCenter = data.get("hitCenter");
        Circle circleView = new Circle(hitRadius, Color.RED);
        circleView.setTranslateX(hitCenter.getX()); // 视图在实体x轴上向右偏移10像素
        circleView.setTranslateY(hitCenter.getY());
        return FXGL.entityBuilder(data)
                .type(EntityType.PROJECTILEENEMY)
                .at(startPos)
              //  .view(circleView)
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



    @Spawns("fireEnemy")
    public Entity newfireEnemy(SpawnData data) {
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
                .type(com.survivor.main.EntityType.PROJECTILEENEMY)
                .at(startPos)
                .view(circleView)
                .with(physics) // 添加物理组件用于碰撞
                .with(new Fire(speed, damage, center, hitRadius, hitCenter, offsetPos, duration)) // 添加自定义逻辑
                .collidable() // 标记为可碰撞
                .build();
    }


    private void applyUpgrade(UpgradeOption opt) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        if (player == null) return;
        switch (opt.getId()) {
            case "atk_up" -> {
                player.getComponent(PlayerMovementComponent.class).increaseAttack(0.5);
            }
            case "spd_up" -> {
                player.getComponent(PlayerMovementComponent.class).increaseSpeed(0.2);
                player.getComponent(PlayerMovementComponent.class).decreaseDashCooldown(0.2);
            }
            case "hp_up" -> {//
                player.getComponent(HealthComponent.class).increaseMaxHP((int)(player.getComponent(HealthComponent.class).getMaxHp()*0.5));
                player.getComponent(HealthComponent.class).heal((int)(player.getComponent(HealthComponent.class).getMaxHp()*0.5));
            }
            case "attcktime_up" -> {
                player.getComponent(PlayerMovementComponent.class).decreaseattackInterval(0.3);
            }
            case "regen_up" ->{//
                player.getComponent(HealthComponent.class).increaseRegenHP(500);
            }
            case "aoe_up" ->{
                player.getComponent(PlayerAnimationComponent.class).increaseAttackRadius(0.2);
                player.getComponent(PlayerMovementComponent.class).increaseScale(0.5);
            }
            case "tool_up" ->{
                player.getComponent(PlayerMovementComponent.class).increasenumbers(1);
            }
            case "waterwindspeed_up"->{
                player.getComponent(PlayerMovementComponent.class).setScaleSpeed( player.getComponent(PlayerMovementComponent.class).getScaleSpeed()*1.4f);
            }
            case "blood_up" ->{
                if(!player.getComponent(PlayerMovementComponent.class).isHadBloodCircle()){
                    player.getComponent(PlayerMovementComponent.class).setHadBloodCircle(true);
                    return;
                }
                player.getComponent(PlayerMovementComponent.class).setScaleBloodCircleX(player.getComponent(PlayerMovementComponent.class).getScaleBloodCircleX()*1.5);
                player.getComponent(PlayerMovementComponent.class).setScaleBloodCircleY(player.getComponent(PlayerMovementComponent.class).getScaleBloodCircleY()*1.5);

            }
            case "blood-increase"->{
                player.getComponent(PlayerMovementComponent.class).setTimeBloodCircle( player.getComponent(PlayerMovementComponent.class).getTimeBloodCircle()*(0.8));
            }
            case "speacil"->{
                PlayerMovementComponent.speacil = true;
            }
        }
        System.out.println("Applied upgrade: " + opt.getId());
    }

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
                .type(com.survivor.main.EntityType.PROJECTILE)
                .at(startPos)
//                .view(circleView)
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
    @Spawns("neutralBlade")
    public Entity newNeutralBlade(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        float hitRadius = data.get("hitRadius");
        Point2D startPos = data.get("startPos");
        Point2D hitCenter = data.get("hitCenter");
        Circle circleView = new Circle(hitRadius, Color.RED);
        circleView.setTranslateX(hitCenter.getX()); // 视图在实体x轴上向右偏移10像素
        circleView.setTranslateY(hitCenter.getY());
        return FXGL.entityBuilder(data)
                .type(com.survivor.main.EntityType.PROJECTILE)
                .at(startPos)
//                .view(circleView)
                //  不要 collidable，传感器不需要
                .with(physics)
                .with(new NeutralBlade(
                        data.get("damage"),
                        hitRadius,
                        data.get("hitCenter"),
                        data.get("offsetPos"),
                        data.get("duration")
                ))
                .build();
    }
    @Spawns("magicAttackEnemy")
    public Entity newMagicAttack(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        Point2D startPos = data.get("startPos");
        float damage = data.get("damage");
        Point2D hitCenter = data.get("hitCenter");
        Point2D offsetPos = data.get("offsetPos");
        float hitRadius = data.get("hitRadius");
        Circle circleView = new Circle(hitRadius, Color.BLUE);
        circleView.setTranslateX(hitCenter.getX()); // 视图在实体x轴上向右偏移10像素
        circleView.setTranslateY(hitCenter.getY());
        return FXGL.entityBuilder(data)
                .type(EntityType.PROJECTILEENEMY)
                .at(startPos)
                .view(circleView)
                .with(physics)
                .with(new MagicAttack(damage, hitRadius, hitCenter, offsetPos)) // 添加自定义逻辑
                .build();
    }
    @Spawns("bulletEnemy")
    public Entity newBulletEnemy(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        Point2D startPos = data.get("startPos");
        // 从SpawnData中获取所有需要的数据
        float speed = data.get("speed");
        float damage = data.get("damage");
        float hitRadius = data.get("hitRadius");
        Point2D hitCenter = data.get("hitCenter");
        Point2D offsetPos = data.get("offsetPos");
        float duration = data.get("duration");
        Point2D targetPos = data.get("targetPos");
        Circle circleView = new Circle(hitRadius, Color.BLUE);
        circleView.setTranslateX(hitCenter.getX()); // 视图在实体x轴上向右偏移10像素
        circleView.setTranslateY(hitCenter.getY());
        return FXGL.entityBuilder(data)
                .type(com.survivor.main.EntityType.PROJECTILEENEMY)
                .at(startPos)
                .view(circleView)
                .with(physics)
                .zIndex(110)
                .with(new Bullet(speed, damage, hitRadius, hitCenter, offsetPos, duration, targetPos))
                .build();
    }
    @Spawns("bullet2")
    public Entity newBullet2(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        Point2D startPos = data.get("startPos");
        // 从SpawnData中获取所有需要的数据
        float speed = data.get("speed");
        float damage = data.get("damage");
        float hitRadius = data.get("hitRadius");
        Point2D hitCenter = data.get("hitCenter");
        Point2D offsetPos = data.get("offsetPos");
        float duration = data.get("duration");
        Point2D targetPos = data.get("targetPos");
        double scaleX = data.get("scaleX");
        double scaleY = data.get("scaleY");
        Circle circleView = new Circle(hitRadius, Color.RED);
        circleView.setTranslateX(hitCenter.getX()); // 视图在实体x轴上向右偏移10像素
        circleView.setTranslateY(hitCenter.getY());
        return FXGL.entityBuilder(data)
                .type(com.survivor.main.EntityType.PROJECTILE)
                .at(startPos)
                .view(circleView)
                .with(physics)
                .scale(scaleX,scaleY )
                .zIndex(110)
                .with(new Bullet2(speed, damage, hitRadius, hitCenter, offsetPos, duration, targetPos))
                .build();
    }
    @Spawns("bloodCircle")
    public Entity newBloodCircle(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        Point2D startPos = data.get("startPos");
        float damage = data.get("damage");
        Point2D hitCenter = data.get("hitCenter");
        Point2D offsetPos = data.get("offsetPos");
        float hitRadius = data.get("hitRadius");
        double scaleX = data.get("scaleX");
        double scaleY = data.get("scaleY");
        Circle circleView = new Circle(hitRadius, Color.BLUE);
        circleView.setTranslateX(hitCenter.getX()); // 视图在实体x轴上向右偏移10像素
        circleView.setTranslateY(hitCenter.getY());
        return FXGL.entityBuilder(data)
                .type(EntityType.PROJECTILE)
                .at(startPos)
                .scale(scaleX,scaleY )
                .zIndex(100)
               // .view(circleView)
                .with(physics)
                .with(new BloodCircle(damage, hitRadius, hitCenter, offsetPos)) // 添加自定义逻辑
                .build();

    }

}
