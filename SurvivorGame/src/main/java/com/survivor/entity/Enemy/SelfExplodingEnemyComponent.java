package com.survivor.entity.Enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.main.EntityType;
import javafx.geometry.Point2D;
import javafx.util.Duration;

@Required(PhysicsComponent.class)   // ⚠️ 声明依赖
public class SelfExplodingEnemyComponent extends Component {


    private double health=40;
    private boolean dead = false;

    private PhysicsComponent physics;  // FXGL 会自动注入

    private AnimatedTexture texture;

    private AnimationChannel idleAnim;
    private AnimationChannel moveAnim;
    private AnimationChannel attackAnim;
    private AnimationChannel deadAnim;


    private boolean isAttacking = false;

    private double speed = 100;
    private double attackRange = 80;
    private long lastAttackTime = 0;
    private long attackCooldown = 1500; // 毫秒

    @Override
    public void onAdded() {
        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setFixedRotation(true);
        });
        // 初始化动画
        idleAnim = new AnimationChannel(FXGL.image("ExplodingCrab/ExplodingCrab idle 80x80.png"),
                13, 80, 80, Duration.seconds(1), 0, 12);
        moveAnim = new AnimationChannel(FXGL.image("ExplodingCrab/ExplodingCrab Walk 80x80.png"),
                10, 80, 80, Duration.seconds(0.8), 0, 9);
        attackAnim = new AnimationChannel(FXGL.image("ExplodingCrab/ExplodingCrab Explosion 80x80.png"),
                23, 80, 80, Duration.seconds(0.8), 0, 22);
        deadAnim = new AnimationChannel(FXGL.image("ExplodingCrab/ExplodingCrab Death 80x80.png"),
                8, 80, 80, Duration.seconds(0.8), 0, 7);
        texture = new AnimatedTexture(idleAnim);
        texture.setTranslateX(-125); // 让贴图居中
        texture.setTranslateY(-125);

        entity.getViewComponent().addChild(texture);
        texture.loopAnimationChannel(idleAnim);
    }

    @Override
    public void onUpdate(double tpf) {
        if (dead) {return;}

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

        Point2D playerPos = playerOpt.get().getCenter().add(new Point2D(30f,40f));
        Point2D myPos = entity.getCenter();

        double distance = myPos.distance(playerPos);
        Point2D center =entity.getCenter();

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
            physics.setVelocityX(0);
            physics.setVelocityY(0);

            FXGL.runOnce(() -> {
                FXGL.spawn("bomb", new SpawnData( center)
                        .put("startPos",  center)
                        .put("damage", 10f)
                        .put("hitCenter",(new Point2D(-60f,-37f)))
                        .put("hitRadius", 50f)
                        .put("offsetPos",new Point2D(0f,0f))
                        .put("duration",0.1f));
            }, Duration.seconds(0.4));

           this.takeDamage(1999);
            texture.playAnimationChannel(attackAnim);

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
        Point2D center =entity.getCenter();
        health -= damage;
        System.out.println("Enemy took " + damage + " damage. Remaining HP: " + health);

        if (health <= 0) {

            if(dead){
                return;
            }
            dead = true;
            physics.setVelocityX(0);
            physics.setVelocityY(0);

            FXGL.runOnce(() -> {
                FXGL.spawn("bomb", new SpawnData( center)
                        .put("startPos",  center)
                        .put("damage", 10f)
                        .put("hitCenter",(new Point2D(-60f,-37f)))
                        .put("hitRadius", 70f)
                        .put("offsetPos",new Point2D(0f,0f))
                        .put("duration",0.1f));
            }, Duration.seconds(0.4));

            FXGL.runOnce(() -> {
                if(entity != null){
                entity.removeFromWorld();}
            }, Duration.seconds(0.8));

            texture.playAnimationChannel(attackAnim);

        } else {
            // TODO: 可以在这里加受伤动画或闪烁效果
            // 例如 texture.loopAnimationChannel(hurtAnim);
        }
    }

    private void die() {
        dead = true;

        // 停止动作
        physics.setVelocityX(0);
        physics.setVelocityY(0);
        isAttacking = false;
        texture.playAnimationChannel(deadAnim);
        // 绑定“动画播完”回调
        texture.setOnCycleFinished(() -> {
            if (texture.getAnimationChannel() ==deadAnim ) {
                // 解绑：不能传 null，改成空 lambda
                texture.setOnCycleFinished(() -> {});
                if (entity != null ) {
                    entity.removeFromWorld();
                }
            }
        });

        // 播放死亡动画（单参）
        texture.playAnimationChannel(deadAnim);
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
