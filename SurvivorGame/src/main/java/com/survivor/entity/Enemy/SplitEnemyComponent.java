package com.survivor.entity.Enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.XPComponent;
import com.survivor.main.EntityType;
import javafx.geometry.Point2D;
import javafx.util.Duration;


public class SplitEnemyComponent extends Component {


    private double health=1500;
    private boolean dead = false;

    private PhysicsComponent physics;  // FXGL 会自动注入

    private AnimatedTexture texture;

    private AnimationChannel idleAnim;
    private AnimationChannel moveAnim;
    private AnimationChannel attackAnim;
    private AnimationChannel deadAnim;


    private boolean isAttacking = false;

    private int attack  =8;
    private double speed = 50;
    private double attackRange = 100;
    private long lastAttackTime = 0;
    private long attackCooldown = 1500; // 毫秒

    @Override
    public void onAdded() {
        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setFixedRotation(true);
        });
        // 初始化动画
        idleAnim = new AnimationChannel(FXGL.image("golem/Golem_1_idle.png"),
                8, 90, 64, Duration.seconds(1), 0, 7);
        moveAnim = new AnimationChannel(FXGL.image("golem/Golem_1_walk.png"),
                10, 90, 64, Duration.seconds(0.8), 0, 9);
        attackAnim = new AnimationChannel(FXGL.image("golem//Golem_1_attack.png"),
                11, 90, 64, Duration.seconds(0.8), 0, 10);
        deadAnim = new AnimationChannel(FXGL.image("golem/Golem_1_die.png"),
                13, 90, 64, Duration.seconds(0.8), 0, 12);
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

        Point2D playerPos = playerOpt.get().getCenter().add(new Point2D(170f,150f));

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
                playAttack();
            } else {
                if (texture.getAnimationChannel() != idleAnim) {
                    texture.loopAnimationChannel(idleAnim);
                }
            }
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
            playerOpt.get().getComponent(XPComponent.class).gainXP(100);
            spawnSmallEnemies();
            entity.removeFromWorld();


                    // 分裂成两个小怪物



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



    //死後分裂
    private void spawnSmallEnemies() {
        // 获取怪物当前的位置
        double x = entity.getX();
        double y = entity.getY();

        // 在当前怪物的位置附近生成两个小怪物
        double offsetX = 0; // 偏移量
        double offsetY = 25; // 偏移量

        // 生成第一个小怪物，稍微偏移
        FXGL.spawn("small_enemy", x + offsetX, y + offsetY);  // 小怪物生成位置偏移
        // 生成第二个小怪物，稍微偏移
        FXGL.spawn("small_enemy", x - offsetX, y - offsetY);  // 小怪物生成位置偏移

        System.out.println("Small enemies spawned at two locations near (" + x + ", " + y + ")");
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }
}
