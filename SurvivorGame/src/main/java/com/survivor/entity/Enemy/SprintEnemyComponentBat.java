package com.survivor.entity.Enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.XPComponent;
import com.survivor.main.EntityType;
import javafx.geometry.Point2D;
import javafx.util.Duration;

// ⚠️ 声明依赖
public class SprintEnemyComponentBat  extends Component {


    private double health=100;
    private boolean dead = false;

    private PhysicsComponent physics;  // FXGL 会自动注入

    private AnimatedTexture texture;

    private AnimationChannel idleAnim;
    private AnimationChannel moveAnim;
    private AnimationChannel attackAnim;
    private AnimationChannel deadAnim;
    private AnimationChannel moveAnim2;



    private boolean isAttacking = false;

    private double speed = 100;
    private double attackRange = 250;
    private long lastAttackTime = 0;
    private long attackCooldown = 15000; // 毫秒

    @Override
    public void onAdded() {
        // 初始化动画
        idleAnim = new AnimationChannel(FXGL.image("bat/Bat-IdleFly.png"),
                9, 64, 64, Duration.seconds(1), 0, 8);
        moveAnim = new AnimationChannel(FXGL.image("bat/Bat-Run.png"),
                8, 64, 64, Duration.seconds(1.34), 0, 7);
        deadAnim = new AnimationChannel(FXGL.image("bat/Bat-die.png"),
                12, 64, 64, Duration.seconds(0.8), 0, 11);
        moveAnim2 = new AnimationChannel(FXGL.image("bat/Bat-Run.png"),8, 64, 64, Duration.seconds(0.2), 0, 7);
        texture = new AnimatedTexture(idleAnim);
        texture.setTranslateX(-125); // 让贴图居中
        texture.setTranslateY(-125);

        entity.getViewComponent().addChild(texture);
        texture.loopAnimationChannel(idleAnim);
    }

    @Override
    public void onUpdate(double tpf) {
        if (dead) {return;}

        var playerOpt = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                .stream().findFirst();

        if (playerOpt.isEmpty()) {
            return;
        }

        Point2D playerPos = playerOpt.get().getCenter();
        Point2D myPos = entity.getCenter();

        double distance = myPos.distance(playerPos);

        if (distance > attackRange) {
//            moveAnim = new AnimationChannel(FXGL.image("boss/Run.png"),
//                    8, 250, 250, Duration.seconds(0.8), 0, 7);
            // 追踪玩家
            Point2D dir = playerPos.subtract(myPos).normalize();
            physics.setVelocityX(dir.getX() * speed * 0.6);
            physics.setVelocityY(dir.getY() * speed * 0.6);

            if (texture.getAnimationChannel() != moveAnim) {
                texture.loopAnimationChannel(moveAnim);
            }

            // 翻转朝向
            texture.setScaleX(dir.getX() < 0 ? -1 : 1);

        } else {
//            moveAnim = new AnimationChannel(FXGL.image("boss/Run.png"),
//                    8, 250, 250, Duration.seconds(0.4), 0, 7);
            Point2D dir = playerPos.subtract(myPos).normalize();

            physics.setVelocityX(dir.getX() * speed*4);
            physics.setVelocityY(dir.getY() * speed*4);

            if (texture.getAnimationChannel() != moveAnim2) {
                texture.loopAnimationChannel(moveAnim2);
            }

            // 翻转朝向
            texture.setScaleX(dir.getX() < 0 ? -1 : 1);

        }
    }

    private void playAttack() {
        isAttacking = true;
        texture.playAnimationChannel(attackAnim);

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
            playerOpt.get().getComponent(XPComponent.class).gainXP(25);
            entity.removeFromWorld();
//            dead = true;
//            speed=0;
//            physics.setVelocityX(0);
//            physics.setVelocityY(0);
//            isAttacking=false;
//            entity.removeComponent(PhysicsComponent.class);
//            texture.playAnimationChannel(deadAnim);
//
//
//            // 在动画最后一帧播完时回调
//            texture.setOnCycleFinished(() -> {
//                if (texture.getAnimationChannel() == deadAnim) {
//                    // 清空回调，避免死循环
//                    texture.setOnCycleFinished(() -> {});
//                    if (entity != null) {
//                        entity.removeFromWorld();
//                    }
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
}

