package com.survivor.entity.Enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.SensorCollisionHandler;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Player.XPComponent;
import com.survivor.entity.Projectile;
import com.survivor.main.EntityType;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class zhiEnemyComponent extends Component {


    private double health=1000;
    private boolean dead = false;

    private PhysicsComponent physics;  // FXGL 会自动注入

    private AnimatedTexture texture;

    private AnimationChannel idleAnim;
    private AnimationChannel moveAnim;
    private AnimationChannel attackAnim;
    private AnimationChannel deadAnim;


    private boolean isAttacking = false;

    private int attack  = 3000;
    private double speed = 1200;
    private double attackRange = 250;
    private long lastAttackTime = 0;
    private long attackCooldown = 1500; // 毫秒
    private Point2D initialDirection = null; // 保存怪物的初始方向

    @Override
    public void onAdded() {
        FXGL.getGameTimer().runOnceAfter(() -> {
            var playerOpt= FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                    .stream().findFirst();
            playerOpt.get().getComponent(XPComponent.class).gainXP(30);
            entity.removeFromWorld();


        }, Duration.seconds(3));
        float hitBoxW = 38f;
        float hitBoxH = 25f;
        float hitBoxX = -100f;
        float hitBoxY = -90f;
//        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
//        rectView.setTranslateX(hitBoxX);
//        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX,hitBoxY),BoundingShape.box(hitBoxW,hitBoxH));
//        HitBox hitBox = new HitBox(hitCenter.subtract(new Point2D(hitRadius,hitRadius)), BoundingShape.circle(hitRadius));
        physics.addSensor(hitBox, new SensorCollisionHandler() {
            @Override
            protected void onCollisionBegin(Entity other) {

                if (other.isType(EntityType.PLAYER)) {
                    if(entity.hasComponent(SprintEnemyCompontBat.class)) {
                        other.getComponent(HealthComponent.class).takeDamage(entity.getComponent(SprintEnemyCompontBat.class).getAttack());
                    }else if (entity.hasComponent(SplitEnemyComponent.class)){
                        other.getComponent(HealthComponent.class).takeDamage(entity.getComponent(SplitEnemyComponent.class).getAttack());
                    }else if (entity.hasComponent(zhiEnemyComponent.class)){
                        other.getComponent(HealthComponent.class).takeDamage(entity.getComponent(zhiEnemyComponent.class).getAttack());
                    }else if(entity.hasComponent(SmallSplitEnemyComponent.class)) {
                        other.getComponent(HealthComponent.class).takeDamage(entity.getComponent(SmallSplitEnemyComponent.class).getAttack());
                    }else if (entity.hasComponent(RangedEnemyComponent.class)) {
                        other.getComponent(HealthComponent.class).takeDamage(entity.getComponent(RangedEnemyComponent.class).getAttack());
                    }else{

                    }
                }
            }

        });

        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setFixedRotation(true);
        });
        // 初始化动画
        idleAnim = new AnimationChannel(FXGL.image("1.png"),
                6, 100, 100, Duration.seconds(1), 0, 5);
        moveAnim = new AnimationChannel(FXGL.image("1.png"),
                6, 64, 64, Duration.seconds(0.3), 0, 5);
        attackAnim = new AnimationChannel(FXGL.image("Lancer-Attack02.png"),
                9, 100, 100, Duration.seconds(0.8), 0, 8);
        deadAnim = new AnimationChannel(FXGL.image("Lancer-Death.png"),
                4, 100, 100, Duration.seconds(0.8), 0, 3);
        texture = new AnimatedTexture(idleAnim);
        texture.setTranslateX(-125); // 让贴图居中
        texture.setTranslateY(-125);

        entity.getViewComponent().addChild(texture);
        texture.loopAnimationChannel(idleAnim);

//        physics = entity.getComponent(PhysicsComponent.class);
    }

    @Override
    public void onUpdate(double tpf) {
        if (dead) {
            return;
        }


        // 获取玩家位置
        var playerOpt = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                .stream().findFirst();

        if (playerOpt.isEmpty()) {
            return;
        }

        Point2D playerPos = playerOpt.get().getCenter();
        Point2D myPos = entity.getCenter();

        // 计算距离
        double distance = myPos.distance(playerPos);

        // 如果怪物第一次更新（即生成时），计算初始方向
        if (initialDirection == null) {
            // 计算朝向玩家的初始方向
            initialDirection = playerPos.subtract(myPos).normalize();
        }
        int dir = 0;
        if(initialDirection.getX() > 0){
            dir = 1;
        }else {
            dir = -1;
        }
        // 控制怪物一直朝初始方向前进
        physics.setVelocityX(dir * speed); // 沿着初始方向移动
        physics.setVelocityY(initialDirection.getY() * 0); // 沿着初始方向移动

        // 如果怪物在移动，播放移动动画
        if (texture.getAnimationChannel() != moveAnim) {
            texture.loopAnimationChannel(moveAnim);
        }

        // 翻转怪物朝向，使怪物始终朝向玩家方向
        texture.setScaleX(initialDirection.getX() < 0 ? -1 : 1);

        // 如果怪物距离玩家足够近，进入攻击状态

    }




    private void playAttack() {




        isAttacking = true;

        texture.setOnCycleFinished(() -> {
            if (texture.getAnimationChannel() == attackAnim) {
                isAttacking = false;
                texture.loopAnimationChannel(idleAnim);
                // 清空回调时用空实现代替，不要传 null
                texture.setOnCycleFinished(() -> {});
            }
        });
    }

    public void takeDamage(double damage) {
        System.out.println(dead);
        if (dead==true) {return; }// 已经死亡不再处理}

        health -= damage;
        System.out.println("Enemy took " + damage + " damage. Remaining HP: " + health);

        if (health <= 0) {
            var playerOpt= FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                    .stream().findFirst();
            playerOpt.get().getComponent(XPComponent.class).gainXP(80);
//            dead = true;
//            speed=0;
//            isAttacking=false;
            entity.removeFromWorld();
//            BoundingBoxComponent hitBox= entity.getBoundingBoxComponent();

//            entity.removeComponent(PhysicsComponent.class);
//            entity.getComponent(PhysicsComponent.class);
//            entity.getComponent(PhysicsComponent.class).setBodyType();
//            System.out.println();
//            texture.playAnimationChannel(deadAnim);
            // 在动画最后一帧播完时回调
//            texture.setOnCycleFinished(() -> {
//                if (texture.getAnimationChannel() == deadAnim) {
//                    // 清空回调，避免死循环
//                    texture.setOnCycleFinished(() -> {});
//                    if (entity != null) {
//                        entity.removeFromWorld();
////                        hitBox.clearHitBoxes();
//                    }
//
//                }
//            });
        } else {
            // TODO: 可以在这里加受伤动画或闪烁效果
            // 例如 texture.loopAnimationChannel(hurtAnim);
        }
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }
}
