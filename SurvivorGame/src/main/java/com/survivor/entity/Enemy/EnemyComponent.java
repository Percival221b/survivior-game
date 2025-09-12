package com.survivor.entity.Enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;


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
}
