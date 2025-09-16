package com.survivor.entity.Enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.XPComponent;
import com.survivor.main.EntityType;
import javafx.animation.FadeTransition;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.sql.RowIdLifetime;
import java.util.Random;

public class BossComponent extends Component {

    private double health = 5000;
    private boolean dead = false;
    private int phase = 1; // 阶段

    private PhysicsComponent physics;  // FXGL 会自动注入

    private AnimatedTexture texture;
    private AnimationChannel idleAnim;
    private AnimationChannel moveAnim;
    private AnimationChannel meleeAnim;
    private AnimationChannel rangedAnim;
    private AnimationChannel deadAnim;

    private boolean isAttacking = false;

    private int attack = 20;
    private double speed = 5000;
    private double attackRange = 200;   // 近战范围
    private double chaseRange = 600;   // 发现玩家范围
    private long lastAttackTime = 0;
    private long attackCooldown; // 毫秒
    private double lifeTimeSec = 0;               // 存活秒數
    private static final double RAMP_DURATION = 100.0; // 100s 完成加速
    private long baseAttackCooldown = 2000;       // 起始 CD(ms)
    private long minAttackCooldown  = 600;
    private double smooth;// 100s 時的最小 CD(ms)
    private Random random = new Random();
    private float bulletSpeed = 300f;

    @Override
    public void onAdded() {
        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setFixedRotation(true);
        });
        // 初始化动画（需要你换成自己的资源）
        idleAnim = new AnimationChannel(FXGL.image("cthlu/cthulu-ldle.png"), 15, 192, 112, Duration.seconds(1), 0, 14);
        moveAnim = new AnimationChannel(FXGL.image("cthlu/cthlu-run.png"), 6, 192, 112, Duration.seconds(0.8), 0, 5);
        meleeAnim = new AnimationChannel(FXGL.image("cthlu/cthlu-attack1.png"), 7, 192, 112, Duration.seconds(0.8), 0, 6);
        rangedAnim = new AnimationChannel(FXGL.image("cthlu/cthlu-attack2.png"), 9, 192, 112, Duration.seconds(0.8), 0, 8);
        deadAnim = new AnimationChannel(FXGL.image("cthlu/cthulu-die.png"), 11, 192, 112, Duration.seconds(0.8), 0, 10);

        texture = new AnimatedTexture(idleAnim);
        texture.setTranslateX(-96);
        texture.setTranslateY(-56);
        entity.getViewComponent().addChild(texture);

        texture.loopAnimationChannel(idleAnim);
    }

    @Override
    public void onUpdate(double tpf) {
        if (dead) return;

        // 累計生存時間
        lifeTimeSec += tpf;
        bulletSpeed += 0.1 ;

        // 0→1 的進度；超過 100s 就鎖 1
        double progress = Math.min(1.0, lifeTimeSec / RAMP_DURATION);

        // 曲線：線性 or 平滑（選一）——建議用平滑會自然些
        double p = progress;
         smooth = p * p * (3 - 2 * p); // SmoothStep: 前期溫和、後期加快

        // ① 攻擊冷卻：從 base 降到 min（用 smooth）
        long dynamicCD = (long)(
                baseAttackCooldown - (baseAttackCooldown - minAttackCooldown) * smooth
        );
        attackCooldown = dynamicCD / 2;  // 直接覆蓋你原本使用的 cooldow

        // 查找玩家
        var playerOpt = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER).stream().findFirst();
        if (playerOpt.isEmpty()) return;

        var player = playerOpt.get();
        Point2D playerPos = player.getPosition();
        Point2D myPos = entity.getCenter();
        double distance = myPos.distance(playerPos);

        // 攻击冷却检查
        long now = System.currentTimeMillis();
        if (isAttacking || now - lastAttackTime < attackCooldown) {
            return;
        }

        // 移动 + 攻击决策
        if (distance > chaseRange) {
            // 追踪玩家
            Point2D dir = playerPos.subtract(myPos).normalize();
            physics.setVelocityX(dir.getX() * speed);
            physics.setVelocityY(dir.getY() * speed);

            if (texture.getAnimationChannel() != moveAnim) {
                texture.loopAnimationChannel(moveAnim);

                doRangedAttack(playerPos);


                // 翻转朝向
                texture.setScaleX(dir.getX() < 0 ? -1 : 1);
            }
        }
        else if(distance < chaseRange) {
            physics.setVelocityX(0);
            physics.setVelocityY(0);
            texture.playAnimationChannel(rangedAnim);
            doRangedAttack(playerPos);
            lastAttackTime = now;
        }
            else {
            // 玩家在近战范围：大概率近战，小概率远程
            physics.setVelocityX(0);
            physics.setVelocityY(0);
            doMeleeAttack(playerPos);

            lastAttackTime = now;
        }
    }

    private void doMeleeAttack(Point2D targetPos) {
        isAttacking = true;
//        // 1. 鏡頭震動
//        FXGL.getGameScene().getViewport().shakeTranslational(30);
//
//        // 2. 鏡頭拉近
//        FXGL.getGameScene().getViewport().setZoom(1.5);
//        FXGL.runOnce(() -> {
//            FXGL.getGameScene().getViewport().setZoom(1.0);
//        }, Duration.seconds(1.5));
//
//        // 3. 閃光效果
//        Rectangle flash = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.WHITE);
//        flash.setOpacity(0);
//
//// 添加到 UI 層
//        FXGL.getGameScene().addUINode(flash);
//
//        FadeTransition ft = new FadeTransition(Duration.seconds(0.3), flash);
//        ft.setFromValue(0);
//        ft.setToValue(1);
//        ft.setCycleCount(2);
//        ft.setAutoReverse(true);
//        ft.setOnFinished(e -> FXGL.getGameScene().removeUINode(flash)); // 結束後移除
//        ft.play();


        texture.playAnimationChannel(meleeAnim);

        // TODO: 近战伤害判定（比如在攻击动画某帧判定范围内的玩家受伤）
        FXGL.runOnce(() -> {
            FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER).forEach(player -> {

            });
        }, Duration.seconds(0.4));

        texture.setOnCycleFinished(() -> {
            isAttacking = false;
            texture.loopAnimationChannel(idleAnim);
            texture.setOnCycleFinished(() -> {});
        });
    }

    private void doRangedAttack(Point2D targetPos) {
//        isAttacking = true;


        double interval = 0.3;
        int totalBullets = 20 + (int)(smooth * 20);
        int waveNum = totalBullets * 2;


        if (random.nextDouble() < 0.5) {
            for (int i = 0; i < totalBullets; i++) {
                int index = i;

                    // 如果 Boss 已經死亡或被移除，直接 return
                    if (entity == null || !entity.isActive()) {
                        return;
                    }

                    FXGL.spawn("BossBullet", new SpawnData(entity.getPosition())
                            .put("startPos",entity.getPosition().add(-140,-140))
                            .put("damage", 10f)
                            .put("speed",bulletSpeed)
                            .put("hitRadius", 7f)
                            .put("hitCenter", new Point2D(9f, 0f))
                            .put("offsetPos", new Point2D(0f, 0f))
                            .put("duration",6f)
                            .put("isClockWise",new Random().nextFloat()-0.5f>0)
                            .put("center",targetPos.add(new Point2D(58f,55f))
                                    .add(new Point2D((new Random().nextFloat()-0.5f)*2*5f,(new Random().nextFloat()-0.5f)*2*50f))
                            ));


            }

            for (int i = 0; i < waveNum; i++) {
                double offsetX = random.nextDouble() * 2000f + 33f -1000f;
                double offsetY = random.nextDouble() * 2000f + 50f -1000f;

                FXGL.runOnce(() -> {
                    // 如果 Boss 已經死亡或被移除，直接 return
                    if (entity == null || !entity.isActive()) {
                        return;
                    }


                        FXGL.spawn("BossMagic", new SpawnData(targetPos)
                                .put("startPos",targetPos.add(offsetX,offsetY))
                                .put("damage", 10f)
                                .put("hitRadius", 5f)
                                .put("hitCenter", new Point2D(2.5f, 2f))
                                .put("offsetPos", new Point2D(0f, 0f)));
                    }, Duration.seconds(0.2f));

            }
    }


//        texture.setOnCycleFinished(() -> {
//            isAttacking = false;
//            texture.loopAnimationChannel(idleAnim);
//            texture.setOnCycleFinished(() -> {});
//        });
    }

    public void takeDamage(double damage) {
        if (dead) return;

        health -= damage;
        System.out.println("Boss HP: " + health);



        if (health <= 0) {
            dead = true;
            physics.setVelocityX(0);
            physics.setVelocityY(0);
            texture.playAnimationChannel(deadAnim);

            texture.setOnCycleFinished(() -> {
                entity.removeFromWorld();
            });
        }
    }
}
