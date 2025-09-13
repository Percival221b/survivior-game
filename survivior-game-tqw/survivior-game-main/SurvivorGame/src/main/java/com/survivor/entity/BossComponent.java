package com.survivor.entity;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.util.Duration;
import javafx.geometry.Point2D;


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
    private static final double ATTACK_RANGE = 60; // 攻击范围

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
    private Point2D target = new Point2D(400, 300);
    private boolean isAttacking = false;

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

        //让纹理居中渲染
        texture.setTranslateX(-125); // 250 像素宽 / 2
        texture.setTranslateY(-125); // 250 像素高 / 2

        BoundingBoxComponent bbox = entity.getBoundingBoxComponent();
        if (bbox == null) {
            entity.addComponent(new BoundingBoxComponent());
            bbox = entity.getBoundingBoxComponent();
        }
        bbox.addHitBox(new HitBox(BoundingShape.box(SIZE, SIZE)));
        texture.loopAnimationChannel(idleAnimPhase1);
    }

    @Override
    public void onUpdate(double tpf) {
        if (!isAlive || isAttacking) return;

        double distance = entity.getCenter().distance(target);

        if (distance > ATTACK_RANGE) {
            // 追逐目标
            double speed = BASE_SPEED * (phase == 2 ? PHASE2_SPEED_BOOST : 1.0);
            Point2D dir = target.subtract(entity.getCenter()).normalize();
            entity.translate(dir.multiply(speed * tpf));

            // 翻转朝向
            texture.setScaleX(dir.getX() < 0 ? -1 : 1);

            if (phase == 1) {
                if (texture.getAnimationChannel() != moveAnimPhase1) {
                    texture.loopAnimationChannel(moveAnimPhase1);
                }
            } else {
                if (texture.getAnimationChannel() != moveAnimPhase2) {
                    texture.loopAnimationChannel(moveAnimPhase2);
                }
            }
        } else {
            // 攻击冷却
            long now = System.currentTimeMillis();
            if (now - lastAttackTime >= attackCooldown) {
                lastAttackTime = now;
                playAttackAnimation();
            }
        }
    }
    public void updateTarget(Point2D newTarget) {
        this.target = newTarget;
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

    // 添加缺失的takeDamage方法
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
//    /**
//     * 每帧更新逻辑 - 处理动画状态机
//     * @param tpf 每帧时间（秒）
//     */
//    @Override
//    public void onUpdate(double tpf) {
//        if (!isAlive) return; // 死亡后不再更新
//
//        // 根据当前状态播放对应动画
//        switch (currentAnimState) {
//            case "idle":
//                updateIdleAnimation();
//                break;
//
//            case "move":
//                updateMoveAnimation();
//                break;
//
//            case "attack":
//                updateAttackAnimation();
//                break;
//        }
//    }
//
//    /**
//     * 移动控制
//     * @param direction 移动方向向量（需标准化）
//     */
//    public void move(Point2D direction) {
//        // 死亡或攻击状态下不可移动
//        if (!isAlive || currentAnimState.equals("attack")) return;
//
//        // 设置移动状态
//        currentAnimState = "move";
//
//        // 计算实际移动速度（第二阶段有加成）
//        double effectiveSpeed = phase == 2 ? BASE_SPEED * PHASE2_SPEED_BOOST : BASE_SPEED;
//
//        if (direction.magnitude() > 0) {
//            // 标准化方向向量并应用速度
//            direction = direction.normalize().multiply(effectiveSpeed);
//
//            // 根据水平方向翻转精灵（实现左右转向）
//            if (direction.getX() < 0) {
//                entity.setScaleX(-1); // 面向左
//            } else if (direction.getX() > 0) {
//                entity.setScaleX(1);  // 面向右
//            }
//
//            // 执行移动（使用tpf而不是FXGL.getdt()）
//            //entity.translate(direction.multiply(`getDeltaTime()));
//        } else {
//            // 无移动输入时转为空闲状态
//            currentAnimState = "idle";
//        }
//    }
//
//    /**
//     * 获取帧时间（替代FXGL.getdt()）
//     */
////    private double getDeltaTime() {
////        return FXGL.getApp().getGameTimer().getLastUpdateTime() - FXGL.getApp().getGameTimer().getNow();
////    }
//
//    /**
//     * 受到伤害处理
//     * @param amount 受到的伤害值
//     */
//    public void takeDamage(int amount) {
//        if (!isAlive) return;
//
//        health -= amount;
//
//        // 受伤效果：闪烁和音效
//        FXGL.play("boss/Take hit.wav"); // 修正：应该是.wav音频文件，不是.png图片
//        entity.getViewComponent().setOpacity(0.5); // 半透明
//        FXGL.runOnce(() -> entity.getViewComponent().setOpacity(1.0), Duration.seconds(0.1));
//
//        // 检查是否需要转换阶段
//        if (phase == 1 && health <= PHASE_TRANSITION_HP) {
//            transitionToPhase2();
//        }
//
//        // 检查是否死亡
//        if (health <= 0) {
//            die();
//        }
//    }
//
//    /**
//     * 执行攻击动作
//     */
//    public void attack() {
//        // 死亡或正在攻击时不可再次攻击
//        if (!isAlive || currentAnimState.equals("attack")) return;
//
//        long currentTime = System.currentTimeMillis();
//        // 检查攻击冷却
//        if (currentTime - lastAttackTime >= attackCooldown) {
//            currentAnimState = "attack"; // 切换至攻击状态
//            lastAttackTime = currentTime; // 记录攻击时间
//
//            // 播放阶段对应的攻击音效
//            FXGL.play(phase == 1 ? "fireball.wav" : "sword_swing.wav");
//
//            // 实际攻击效果可通过FXGL事件系统在其他模块处理
//        }
//    }

//    // ===== 私有方法 =====
//
//    /**
//     * 更新空闲状态动画
//     */
//    private void updateIdleAnimation() {
//        if (phase == 1) {
//            texture.loopAnimationChannel(idleAnimPhase1); // 循环播放一阶段空闲动画
//        } else {
//            texture.loopAnimationChannel(idleAnimPhase2); // 循环播放二阶段空闲动画
//        }
//    }
//
//    /**
//     * 更新移动状态动画
//     */
//    private void updateMoveAnimation() {
//        if (phase == 1) {
//            texture.loopAnimationChannel(moveAnimPhase1); // 循环播放一阶段移动动画
//        } else {
//            texture.loopAnimationChannel(moveAnimPhase2); // 循环播放二阶段移动动画
//        }
//    }
//
//    /**
//     * 更新攻击状态动画
//     */
//    // 修复动画状态判断的正确方式
//    private void updateAttackAnimation() {
//        // 播放对应阶段的攻击动画
//        if (phase == 1) {
//            texture.playAnimationChannel(attackAnimPhase1);
//        } else {
//            texture.playAnimationChannel(attackAnimPhase2);
//        }
//
//        // 正确的动画结束判断方式：
//        // 1. 获取当前动画通道
//        AnimationChannel currentChannel = texture.getAnimationChannel();
//
//        // 2. 检查是否到达最后一帧
//        if (currentChannel.getFrameIndex() == currentChannel.getLastFrameIndex()) {
//            currentAnimState = "idle";
//        }
//    }
//    /**
//     * 转换到第二阶段（近战模式）
//     */
//    private void transitionToPhase2() {
//        phase = 2; // 标记为第二阶段
//        attackCooldown = 1000; // 缩短攻击冷却时间（1秒）
//
//        // 播放转换特效
//        FXGL.play("boss_transform.wav"); // 转换音效
//        FXGL.spawn("PhaseTransitionEffect", entity.getCenter()); // 生成转换特效
//
//        // 更新为二阶段待机动画
//        texture.loopAnimationChannel(idleAnimPhase2);
//    }
//
//    /**
//     * 死亡处理 - 使用专用死亡动画资源
//     */
//    private void die() {
//        isAlive = false;
//        currentAnimState = "die";
//
//        // 加载专用死亡动画资源
//        AnimationChannel dieAnim = new AnimationChannel(
//                FXGL.image("boss/Death.png"),
//                4, 250, 250,
//                Duration.seconds(1.5),
//                0, 3  // 修正：4列精灵图应该是0-3帧
//        );
//
//        // 播放死亡动画和音效
//        texture.playAnimationChannel(dieAnim);
//        FXGL.play("boss_death.wav");
//
//        // 动画播放完毕后移除实体
//        FXGL.runOnce(() -> {
//            entity.removeFromWorld();
//            FXGL.spawn("DeathExplosion", entity.getCenter()); // 生成死亡特效
//        }, Duration.seconds(1.5));
//    }
//
//    // ===== Getter方法 =====
//
//    /**
//     * 获取当前生命值
//     * @return 当前生命值
//     */
//    public int getHealth() { return health; }
//
//    /**
//     * 获取当前阶段
//     * @return 1表示第一阶段（远程），2表示第二阶段（近战）
//     */
//    public int getPhase() { return phase; }
//
//    /**
//     * 检查存活状态
//     * @return true表示存活，false表示死亡
//     */
//    public boolean isAlive() { return isAlive; }
//}