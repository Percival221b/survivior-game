package com.survivor.entity.Enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.XPComponent;
import com.survivor.main.EntityType;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import java.util.Random;

//@Required(PhysicsComponent.class)   // ⚠️ 声明依赖
public class RangedEnemyComponent extends Component {


    private double health = 100;
    private boolean dead = false;

    private PhysicsComponent physics;  // FXGL 会自动注入

    private AnimatedTexture texture;

    private AnimationChannel idleAnim;
    private AnimationChannel moveAnim;
    private AnimationChannel attackAnim;
    private AnimationChannel deadAnim;


    private boolean isAttacking = false;

    private int attack = 1;
    private double speed = 100;
    private double attackRange = 400;
    private long lastAttackTime = 0;
    private long attackCooldown = 1500; // 毫秒

    @Override
    public void onAdded() {
        // 初始化动画
        idleAnim = new AnimationChannel(FXGL.image("bat/Archer-attack.png"),
                8, 64, 64, Duration.seconds(1), 0, 7);
        moveAnim = new AnimationChannel(FXGL.image("bat/Archer-run.png"),
                8, 64, 64, Duration.seconds(0.8), 0, 7);
        attackAnim = new AnimationChannel(FXGL.image("bat/Archer-attack.png"),
                7, 64, 64, Duration.seconds(0.8), 0, 6);
        deadAnim = new AnimationChannel(FXGL.image("bat/Archer-die.png"),
                8, 250, 250, Duration.seconds(0.8), 0, 7);
        texture = new AnimatedTexture(idleAnim);
        texture.setTranslateX(-125); // 让贴图居中
        texture.setTranslateY(-125);

        entity.getViewComponent().addChild(texture);
        texture.loopAnimationChannel(idleAnim);
    }

    @Override
    public void onUpdate(double tpf) {
        if (dead) {
            return;
        }

        if (isAttacking) {
            // 攻击过程中不移动
            physics.setVelocityX(0);
            physics.setVelocityY(0);
            return;
        }

        // 获取玩家
        var playerOpt = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                .stream().findFirst();

        if (playerOpt.isEmpty()) {
            return;
        }

        Point2D playerPos = playerOpt.get().getPosition();
        Point2D myPos = entity.getCenter();

        double distance = myPos.distance(playerPos);

        if (distance > attackRange) {
            // 追踪玩家
            Point2D dir = playerPos.subtract(myPos).normalize();
            physics.setVelocityX(dir.getX() * speed);
            physics.setVelocityY(dir.getY() * speed);

            if (texture.getAnimationChannel() != moveAnim) {
                texture.loopAnimationChannel(moveAnim);
            }

            // 翻转朝向
            texture.setScaleX(dir.getX() < 0 ? -1 : 1);

        } else {
            // 停下并攻击
            physics.setVelocityX(0);
            physics.setVelocityY(0);

            long now = System.currentTimeMillis();
            if (now - lastAttackTime >= attackCooldown) {
                lastAttackTime = now;
                playAttack(playerPos);
            } else {
                if (texture.getAnimationChannel() != idleAnim) {
                    texture.loopAnimationChannel(idleAnim);
                }
            }
        }
    }

    private void playAttack(Point2D playerPos) {
        isAttacking = true;
        texture.playAnimationChannel(attackAnim);
//        FXGL.spawn("magicAttackEnemy", new SpawnData(playerPos)
//                .put("startPos", playerPos)
//                .put("damage", 10f)
//                .put("hitRadius", 5f)
//                .put("hitCenter", new Point2D(3f, 20f))
//                .put("offsetPos", new Point2D(0f, 0f)));

                FXGL.spawn("bulletEnemy", new SpawnData(entity.getPosition())
                .put("startPos",entity.getPosition().add(-140,-140))
                .put("damage", 10f)
                .put("speed",100f)
                .put("hitRadius", 5f)
                .put("hitCenter", new Point2D(0f, 0f))
                .put("offsetPos", new Point2D(0f, 0f))
                .put("duration",6f)
                .put("targetPos",playerPos.add(new Point2D(58f,55f))
                .add(new Point2D((new Random().nextFloat()-0.5f)*2*5f,(new Random().nextFloat()-0.5f)*2*50f))
                ));
        texture.setOnCycleFinished(() -> {
            if (texture.getAnimationChannel() == attackAnim) {
                isAttacking = false;
                texture.loopAnimationChannel(idleAnim);
                // 清空回调时用空实现代替，不要传 null
                texture.setOnCycleFinished(() -> {
                });
            }
        });
    }

    public void takeDamage(double damage) {
        System.out.println(dead);
        if (dead == true) {
            return;
        }// 已经死亡不再处理}

        health -= damage;
        System.out.println("Enemy took " + damage + " damage. Remaining HP: " + health);

        if (health <= 0) {
            var playerOpt= FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                    .stream().findFirst();
            playerOpt.get().getComponent(XPComponent.class).gainXP(20);

            entity.removeFromWorld();
//            dead = true;
//            speed = 0;
//            isAttacking = false;
//            //entity.removeComponent(PhysicsComponent.class);
//
//            texture.playAnimationChannel(deadAnim);
//
//            // 在动画最后一帧播完时回调
//            texture.setOnCycleFinished(() -> {
//                if (texture.getAnimationChannel() == deadAnim) {
//                    // 清空回调，避免死循环
//                    texture.setOnCycleFinished(() -> {
//                    });
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

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }
}
