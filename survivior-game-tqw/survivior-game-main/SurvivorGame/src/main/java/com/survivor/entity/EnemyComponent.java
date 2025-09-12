package com.survivor.entity;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;

/**
 * 小怪基础组件 - 控制小怪的核心行为逻辑
 * 参考黎明前20分钟的小怪设计：
 * - 简单移动AI：追踪玩家
 * - 近战攻击模式
 * - 死亡掉落经验
 */
public class EnemyComponent extends Component  {

    // ========== 小怪属性 ==========
    private int health = 30;          // 生命值 (基础值，可根据小怪类型调整)
    private int damage = 10;          // 攻击伤害 (每次攻击造成的伤害)
    private double speed = 1.5;       // 移动速度 (像素/帧)
    private double attackRange = 50;  // 攻击范围 (像素)
    private double detectionRange = 300; // 发现玩家的范围 (像素)
    private int expValue = 5;         // 死亡时掉落的经验值

    // ========== 组件引用 ==========
    private PhysicsComponent physics; // 物理组件用于移动
    private AnimatedTexture texture;  // 动画纹理

    // ========== 动画通道 ==========
    private AnimationChannel animIdle;   // 空闲动画
    private AnimationChannel animWalk;   // 行走动画
    private AnimationChannel animAttack; // 攻击动画
    private AnimationChannel animDeath;// 死亡动画
    private AnimationChannel animHit;

    // ========== 目标玩家 ==========
    private Entity player; // 追踪的目标玩家

    // ========== 战斗相关 ==========
    private double attackCooldown = 0;           // 攻击冷却计时器
    private final double attackCooldownMax = 1.0; // 攻击冷却时间 (秒)
    private boolean isDead = false;              // 是否已死亡

    /**
     * 构造函数
     *
     * @param player 目标玩家实体
     */
    public EnemyComponent(Entity player) {
        this.player = player;

        // 初始化动画 (假设使用100x100像素的精灵图)
        animIdle = new AnimationChannel(FXGL.image("monster/normal/Orc_Idle.png"), 6, 100, 100, Duration.seconds(1), 0, 5);
        animWalk = new AnimationChannel(FXGL.image("monster/normal/Orc_Walk.png"), 8, 100, 100, Duration.seconds(0.8), 0, 7);
        animAttack = new AnimationChannel(FXGL.image("monster/normal/Orc_Attack.png"), 6, 100, 100, Duration.seconds(0.8), 0, 5);
        animDeath = new AnimationChannel(FXGL.image("monster/normal/Orc_Death.png"), 4, 100, 100, Duration.seconds(1), 0, 3);
        animHit=new AnimationChannel(FXGL.image("monster/normal/Orc_Hit.png"),5,100,100,Duration.seconds(1),0,4);

        texture = new AnimatedTexture(animIdle);
    }

    @Override
    public void onAdded() {
        // 添加碰撞组件
        entity.addComponent(new CollidableComponent(true));
        // 添加动画纹理到实体
        entity.getViewComponent().addChild(texture);
        // 初始播放空闲动画
        texture.loopAnimationChannel(animIdle);
    }
    public AnimatedTexture getTexture() {
        return texture;
    }

    public AnimationChannel getAnimIdle() {
        return animIdle;
    }

    public AnimationChannel getAnimWalk() {
        return animWalk;
    }

    public AnimationChannel getAnimAttack() {
        return animAttack;
    }

    public AnimationChannel getAnimDeath() {
        return animDeath;
    }
    public AnimationChannel getAnimHit() {
        return animHit;
    }
    // ========== 添加 setter ==========
    public void setHealth(int health) {
        this.health = health;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setAttackRange(double attackRange) {
        this.attackRange = attackRange;
    }

    public void setExpValue(int expValue) {
        this.expValue = expValue;
    }

}
 /*   @Override
    public void onUpdate(double tpf) {
        if (isDead) return;

        if (health <= 0) {
            die();
            return;
        }

        // 更新攻击冷却
        if (attackCooldown > 0) {
            attackCooldown -= tpf;
        }

        // 计算与玩家的距离
        Point2D playerPos = player.getPosition();
        Point2D enemyPos = entity.getPosition();
        double distance = playerPos.distance(enemyPos);

        if (distance < detectionRange) {
            // 玩家在探测范围内
            if (distance > attackRange) {
                // 移动向玩家
                moveTowardsPlayer(tpf);
                texture.loopAnimationChannel(animWalk);
            } else {
                // 在攻击范围内，停止移动并攻击
                physics.setVelocityX(0);
                physics.setVelocityY(0);

                if (attackCooldown <= 0) {
                    attack();
                    attackCooldown = attackCooldownMax;
                    texture.playAnimationChannel(animAttack);
                }
            }
        } else {
            // 玩家不在探测范围内，空闲状态
            physics.setVelocityX(0);
            physics.setVelocityY(0);
            texture.loopAnimationChannel(animIdle);
        }
    }

    *//**
     * 向玩家移动
     *//*
    private void moveTowardsPlayer(double tpf) {
        Point2D direction = player.getPosition()
                .subtract(entity.getPosition())
                .normalize()
                .multiply(speed);

        physics.setVelocityX(direction.getX());
        physics.setVelocityY(direction.getY());
    }
}
    *
     * 攻击玩家

    private void attack() {
        // 对玩家造成伤害
        player.getComponent(PlayerComponent.class).takeDamage(damage);

        // 播放攻击音效
        FXGL.play("enemy_attack.wav");
    }

    *
     * 受到伤害
     * @param amount 伤害值

    public void takeDamage(int amount) {
        health -= amount;

        // 受伤效果
        FXGL.play("enemy_hit.wav");
        texture.setOpacity(0.5); // 受伤时半透明闪烁
        FXGL.runOnce(() -> texture.setOpacity(1.0), Duration.seconds(0.1));

        if (health <= 0) {
            die();
        }
    }

    *
     * 死亡处理

    private void die() {
        isDead = true;
        texture.playAnimationChannel(animDeath);

        // 禁用碰撞
        entity.getComponent(CollidableComponent.class).setValue(false);

        // 动画结束后移除实体
        texture.setOnCycleFinished(() -> {
            entity.removeFromWorld();

            // 掉落经验球
            FXGL.spawn("exp_orb", entity.getPosition().add(16, 16),
                    new ExpOrbData(expValue));

            // 播放死亡音效
            FXGL.play("enemy_death.wav");
        });
    }

    // ========== Getter/Setter ==========
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public int getExpValue() { return expValue; }
    public void setExpValue(int expValue) { this.expValue = expValue; }
}*/