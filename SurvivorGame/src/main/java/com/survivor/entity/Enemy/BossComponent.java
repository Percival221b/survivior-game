package com.survivor.entity.Enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.util.Duration;

/**
 * Boss实体组件 - 专注于攻击动作和动画表现
 *
 * 核心功能：
 * 1. 双阶段动画系统（远程/近战形态）
 * 2. 基础移动控制和攻击动作
 * 3. 血量管理和阶段转换逻辑
 *
 * 动画状态说明：
 * - idle: 站立待机状态
 * - move: 移动状态
 * - attack: 攻击动作状态
 */
public class BossComponent extends Component {

    // ===== 游戏常量配置 =====
    public static final int MAX_HEALTH = 1000;          // Boss最大生命值
    public static final int PHASE_TRANSITION_HP = 400; // 阶段转换的生命阈值
    public static final double BASE_SPEED = 150;       // 基础移动速度（像素/秒）
    public static final double PHASE2_SPEED_BOOST = 1.5; // 第二阶段速度加成
    public static final double SIZE = 60;              // 实体碰撞体积大小

    // ===== 动画系统 =====
    // 第一阶段（远程形态）动画通道
    private final AnimationChannel idleAnimPhase1;   // 空闲动画
    private final AnimationChannel moveAnimPhase1;   // 移动动画
    private final AnimationChannel attackAnimPhase1; // 攻击动画

    // 第二阶段（近战形态）动画通道
    private final AnimationChannel idleAnimPhase2;
    private final AnimationChannel moveAnimPhase2;
    private final AnimationChannel attackAnimPhase2;

    //boss死亡/受击
    private final AnimationChannel deathAnim;
    private final AnimationChannel takehitAnim;

    private final AnimatedTexture texture;           // 动画纹理控制器
    private String currentAnimState = "idle";        // 当前动画状态标记

    // ===== 战斗属性 =====
    private int health = MAX_HEALTH;    // 当前生命值
    private int phase = 1;              // 当前阶段（1=远程，2=近战）
    private boolean isAlive = true;     // 存活状态
    private long lastAttackTime = 0;    // 上次攻击时间戳（毫秒）
    private int attackCooldown = 2000;  // 攻击冷却时间（毫秒）

    /**
     * 构造函数 - 初始化动画通道
     * 动画资源假设：
     * - boss/Idle.png: 空闲动画精灵图
     * - boss/Run.png: 移动动画精灵图
     * - boss/Attack1.png: 第一阶段攻击动画
     * - boss/Attack2.png: 第二阶段攻击动画
     */
    public BossComponent() {
        // 初始化第一阶段动画（远程形态）
        // 参数说明：精灵图, 列数, 帧宽, 帧高, 动画时长, 起始帧, 结束帧
        idleAnimPhase1 = new AnimationChannel(FXGL.image("boss/Idle.png"),
                8, 250, 250, Duration.seconds(1), 0, 7);
        moveAnimPhase1 = new AnimationChannel(FXGL.image("boss/Run.png"),
                8, 250, 250, Duration.seconds(0.8), 0, 7);
        attackAnimPhase1 = new AnimationChannel(FXGL.image("boss/Attack1.png"),
                8, 250, 250, Duration.seconds(0.8), 0, 7);

        // 初始化第二阶段动画（近战形态，动画速度更快）
        idleAnimPhase2 = new AnimationChannel(FXGL.image("boss/Idle.png"),
                8, 250, 250, Duration.seconds(0.8), 0, 7);
        moveAnimPhase2 = new AnimationChannel(FXGL.image("boss/Run.png"),
                8, 250, 250, Duration.seconds(0.8), 0, 7);
        attackAnimPhase2 = new AnimationChannel(FXGL.image("boss/Attack2.png"),
                8, 250, 250, Duration.seconds(0.8), 0, 7);
        //初始化死亡受击动画
        deathAnim = new AnimationChannel(FXGL.image("boss/Death.png"),
                8, 250, 250, Duration.seconds(2.5), 0, 7);
        takehitAnim = new AnimationChannel(FXGL.image("boss/Take Hit.png"),
                8, 250, 250, Duration.seconds(0.5), 0, 7);


        // 创建动画纹理并设置初始动画
        texture = new AnimatedTexture(idleAnimPhase1);
    }

    /**
     * 组件附加到实体时调用 - 初始化渲染和物理属性
     */
    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
        texture.setTranslateX(-125); // 250 像素宽 / 2
        texture.setTranslateY(-125); // 250 像素高 / 2
    }

    public void setPhase(int phase) {
        this.phase = phase;
        if (phase == 1) {
            texture.loop(); // 使用loop()而不是loopAnimation()
        } else {
            texture.loop(); // 使用loop()而不是loopAnimation()
        }
        currentAnimState = "idle";
    }

    public void reset() {
        health = MAX_HEALTH;
        phase = 1;
        isAlive = true;
        lastAttackTime = 0;
        attackCooldown = 2000;
        texture.loop(); // 使用loop()而不是loopAnimation()
        currentAnimState = "idle";
    }

    public int getHealth() {
        return health;
    }

    public int getPhase() {
        return phase;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void takeDamage(int amount) {
        if (!isAlive) return;

        health -= amount;

        // 检查是否需要转换阶段
        if (phase == 1 && health <= PHASE_TRANSITION_HP) {
            transitionToPhase2();
        }

        // 检查是否死亡
        if (health <= 0) {
            isAlive = false;
            playDeathAnimation();
        }
    }

    // 阶段转换私有方法
    private void transitionToPhase2() {
        phase = 2;
        attackCooldown = 1000; // 缩短攻击冷却
    }

    public void playIdleAnimationPhase1() {
        texture.loopAnimationChannel(idleAnimPhase1);
    }

    public void playMoveAnimationPhase1() {
        texture.loopAnimationChannel(moveAnimPhase1);
    }

    public void playAttackAnimation() {
        if (phase == 1) {
            texture.playAnimationChannel(attackAnimPhase1);
        } else {
            texture.playAnimationChannel(attackAnimPhase2);
        }
    }


    public void playIdleAnimationPhase2() {
        texture.loopAnimationChannel(idleAnimPhase2);
    }

    public void playMoveAnimationPhase2() {
        texture.loopAnimationChannel(moveAnimPhase2);
    }

    public void playTakeHitAnimation() {
        texture.playAnimationChannel(takehitAnim);
    }

    public void playDeathAnimation() {
        texture.playAnimationChannel(deathAnim);
    }

}
