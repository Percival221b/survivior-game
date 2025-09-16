package com.survivor.entity.Player;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.PlayerMovementComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Point2D;
import javafx.util.Duration;

/**
 * 主角动画组件：保证攻击最少播放一遍且不可被打断；长按连击。
 * 依赖 PlayerMovementComponent 的按键状态（true/false）。
 */
public class PlayerAnimationComponent extends Component {

    private AnimatedTexture texture;

    private AnimationChannel idle;
    private AnimationChannel walk;
    private AnimationChannel attack;
    private AnimationChannel dash;

    private PlayerMovementComponent movement;

    private enum State { WALK ,ATTACK,IDLE,DASH}
    private State state = State.IDLE;

    // === 关键：攻击锁，确保一旦开始就播完一轮；以及边沿检测 ===
    private boolean attackInProgress = false;   // 正在播放攻击（期间禁止其它动画切换）
    private boolean prevAttackInput  = false;   // 上一帧是否按着 J/K（用来捕捉“刚按下”）

    public void decreaseAttackInterval(double n) {
        this.newattackInterval2 *= (1-n);
    }

    double newattackInterval2=0.6;
    double speedrate=1;

    public void increaseAttackRadius(double n) {
        this.attackRadius *= (1+n);
    }

    float attackRadius = 45f;


    @Override
    public void onAdded() {
        movement = entity.getComponent(PlayerMovementComponent.class);
        idle = new AnimationChannel(
                FXGL.image("Idle.png"), 10, 135, 135, Duration.seconds(0.5), 0, 9
        );
        walk = new AnimationChannel(
                FXGL.image("Run.png"), 6, 135, 135, Duration.seconds(0.7), 0, 5
        );
        attack = new AnimationChannel(
                FXGL.image("Attack3.png"), 5, 135, 135, Duration.seconds(0.6), 0, 4
        );
        dash = new AnimationChannel(
                FXGL.image("Run.png"), 6, 135, 135, Duration.seconds(0.2), 0, 5
        );

        texture = new AnimatedTexture(idle);
        texture.setScaleX(2.5);
        texture.setScaleY(2.5);
        entity.getViewComponent().addChild(texture);
        texture.loopAnimationChannel(idle);

        javafx.scene.shape.Circle centerDebug = new javafx.scene.shape.Circle(3, javafx.scene.paint.Color.RED);
        entity.getViewComponent().addChild(centerDebug);
        double x = entity.getPosition().getX()+50;
        double y = entity.getPosition().getY()+36;

        movement = entity.getComponent(PlayerMovementComponent.class);
    }

    @Override
    public void onUpdate(double tpf) {
        movement.setattackIntervalOnChange(newattackInterval -> {
            newattackInterval2= Math.max(newattackInterval, 0.08);
            attack = new AnimationChannel(
                    FXGL.image("Attack2.png"), 5, 135, 135, Duration.seconds(newattackInterval2), 0, 4
            );
        });
        movement.setOnSpeedChange(newSpeed -> {
            speedrate= Math.max(400/newSpeed, 0.1);
            walk = new AnimationChannel(
                    FXGL.image("Run.png"), 6, 135, 135, Duration.seconds(0.7*speedrate), 0, 5
            );
        });

        // 朝向：优先用攻击方向，其次移动方向
        if (movement.isAttackingLeft()) {
            texture.setScaleX(-2.5);
        } else if (movement.isAttackingRight()) {
            texture.setScaleX(2.5);
        }


        // 攻击进行中：完全锁住，禁止任何其它动画打断
        if (attackInProgress) {
            return;
        }
        if ( movement.isMovingLeft()) {
            texture.setScaleX(-2.5);
        } else if ( movement.isMovingRight()) {
            texture.setScaleX(2.5);
        }
        if (movement.isAttackingLeft()) {
            texture.setScaleX(-2.5);
        } else if (movement.isAttackingRight()) {
            texture.setScaleX(2.5);
        }



        // 捕捉“刚按下”触发一次攻击（即使很快松手，也能完整播完一轮）
        boolean attackPressed = movement.isAttackingLeft() || movement.isAttackingRight();
        if (attackPressed && !prevAttackInput) {
            startAttack();      // 开始第一轮攻击
            prevAttackInput = attackPressed;
            return;             // 攻击优先级最高
        }
        prevAttackInput = attackPressed;

        // 冲刺（只有在不攻击时才允许切换）
        if (movement.isDashing()) {
            if (state != State.DASH) {
                state = State.DASH;
                texture.playAnimationChannel(dash);
                texture.setOnCycleFinished(() -> {
                    if (attackInProgress) return; // 若期间被触发攻击，保持攻击优先
                    state = movement.isMoving() ? State.WALK : State.IDLE;
                    texture.loopAnimationChannel(state == State.WALK ? walk : idle);
                });
            }
            return;
        }

        // 行走 / 待机 切换（只有不攻击、不冲刺时才切）
        boolean moving = movement.isMoving();
        if (moving && state != State.WALK) {
            state = State.WALK;
            texture.loopAnimationChannel(walk);
        } else if (!moving && state != State.IDLE) {
            state = State.IDLE;
            texture.loopAnimationChannel(idle);
        }
    }

    /** 开始或续播一轮攻击：一旦进入就加锁，直到本轮攻击完整结束 */
    private void startAttack() {
        state = State.ATTACK;
        attackInProgress = true;
        movement.triggerAttackSlow(0.6);
        // 根据当前按键方向再确认一下朝向
        boolean leftAttack = movement.isAttackingLeft();
        boolean rightAttack = movement.isAttackingRight();

        texture.playAnimationChannel(attack);

        FXGL.runOnce(() -> {
            if (leftAttack) {;
                System.out.println(movement.getScaleX());
                if(movement.getNumbers()>=1){FXGL.spawn("bullet2", new SpawnData(entity.getPosition())
                        .put("startPos",entity.getPosition().add(new Point2D(-25f,40f)))
                        .put("damage", 10f)
                        .put("speed",movement.getScaleSpeed())
                        .put("hitRadius", 25f)
                        .put("hitCenter", new Point2D(0f, 0f))
                        .put("offsetPos", new Point2D(0f, 0f))
                        .put("duration",6f)
                        .put("targetPos",entity.getPosition().add(new Point2D(-25f,40f)).add(new Point2D(-1,0)))
                        .put("scaleX", movement.getScaleX())
                        .put("scaleY", movement.getScaleY()));}
                if(movement.getNumbers()>=2) {FXGL.spawn("bullet2", new SpawnData(entity.getPosition())
                        .put("startPos",entity.getPosition().add(new Point2D(-25f,40f)))
                        .put("damage", 10f)
                        .put("speed",movement.getScaleSpeed())
                        .put("hitRadius", 5f)
                        .put("hitCenter", new Point2D(0f, 0f))
                        .put("offsetPos", new Point2D(0f, 0f))
                        .put("duration",6f)
                        .put("targetPos",entity.getPosition().add(new Point2D(-25f,40f)).add(new Point2D(-3,1)))
                        .put("scaleX", movement.getScaleX())
                        .put("scaleY", movement.getScaleX()));
                         FXGL.spawn("bullet2", new SpawnData(entity.getPosition())
                        .put("startPos",entity.getPosition().add(new Point2D(-25f,40f)))
                        .put("damage", 10f)
                        .put("speed",movement.getScaleSpeed())
                        .put("hitRadius", 5f)
                        .put("hitCenter", new Point2D(0f, 0f))
                        .put("offsetPos", new Point2D(0f, 0f))
                        .put("duration",6f)
                        .put("targetPos",entity.getPosition().add(new Point2D(-25f,40f)).add(new Point2D(-3,-1)))
                        .put("scaleX", movement.getScaleX())
                        .put("scaleY", movement.getScaleY()));}
                if(movement.getNumbers()>=3) {FXGL.spawn("bullet2", new SpawnData(entity.getPosition())
                        .put("startPos",entity.getPosition().add(new Point2D(-25f,40f)))
                        .put("damage", 10f)
                        .put("speed",movement.getScaleSpeed())
                        .put("hitRadius", 5f)
                        .put("hitCenter", new Point2D(0f, 0f))
                        .put("offsetPos", new Point2D(0f, 0f))
                        .put("duration",6f)
                        .put("targetPos",entity.getPosition().add(new Point2D(-25f,40f)).add(new Point2D(-1,1)))
                        .put("scaleX", movement.getScaleX())
                        .put("scaleY", movement.getScaleX()));
                    FXGL.spawn("bullet2", new SpawnData(entity.getPosition())
                            .put("startPos",entity.getPosition().add(new Point2D(-25f,40f)))
                            .put("damage", 10f)
                            .put("speed",movement.getScaleSpeed())
                            .put("hitRadius", 5f)
                            .put("hitCenter", new Point2D(0f, 0f))
                            .put("offsetPos", new Point2D(0f, 0f))
                            .put("duration",6f)
                            .put("targetPos",entity.getPosition().add(new Point2D(-25f,40f)).add(new Point2D(-1,-1)))
                            .put("scaleX", movement.getScaleX())
                            .put("scaleY", movement.getScaleY()));}

                FXGL.spawn("blade", new SpawnData( entity.getCenter())
                        .put("startPos",  entity.getCenter())
                        .put("damage", 10f)
                        .put("hitCenter",(new Point2D(-50f,-30f)))
                        .put("hitRadius", attackRadius)
                        .put("offsetPos",new Point2D(0f,0f))
                        .put("duration",0.1f));

                entity.getComponent(PlayerSoundComponent.class).playAttack();

            } else if (rightAttack) {
                if(movement.getNumbers()>=1){FXGL.spawn("bullet2", new SpawnData(entity.getPosition().add(60f,0f))
                        .put("startPos",entity.getPosition().add(new Point2D(60f,40f)))
                        .put("damage", 10f)
                        .put("speed",movement.getScaleSpeed())
                        .put("hitRadius", 25f)
                        .put("hitCenter", new Point2D(0f, 0f))
                        .put("offsetPos", new Point2D(0f, 0f))
                        .put("duration",6f)
                        .put("targetPos",entity.getPosition().add(new Point2D(60f,40f)).add(new Point2D(1,0)))
                        .put("scaleX", movement.getScaleX())
                        .put("scaleY", movement.getScaleY()));}
                if(movement.getNumbers()>=2) {FXGL.spawn("bullet2", new SpawnData(entity.getPosition().add(60f,0f))
                        .put("startPos",entity.getPosition().add(new Point2D(60f,40f)))
                        .put("damage", 10f)
                        .put("speed",movement.getScaleSpeed())
                        .put("hitRadius", 5f)
                        .put("hitCenter", new Point2D(0f, 0f))
                        .put("offsetPos", new Point2D(0f, 0f))
                        .put("duration",6f)
                        .put("targetPos",entity.getPosition().add(new Point2D(60f,40f)).add(new Point2D(3,1)))
                        .put("scaleX", movement.getScaleX())
                        .put("scaleY", movement.getScaleX()));
                    FXGL.spawn("bullet2", new SpawnData(entity.getPosition().add(60f,0f))
                            .put("startPos",entity.getPosition().add(new Point2D(60f,40f)))
                            .put("damage", 10f)
                            .put("speed",movement.getScaleSpeed())
                            .put("hitRadius", 5f)
                            .put("hitCenter", new Point2D(0f, 0f))
                            .put("offsetPos", new Point2D(0f, 0f))
                            .put("duration",6f)
                            .put("targetPos",entity.getPosition().add(new Point2D(60f,40f)).add(new Point2D(3,-1)))
                            .put("scaleX", movement.getScaleX())
                            .put("scaleY", movement.getScaleY()));}
                if(movement.getNumbers()>=3) {FXGL.spawn("bullet2", new SpawnData(entity.getPosition().add(60f,0f))
                        .put("startPos",entity.getPosition().add(new Point2D(60f,40f)))
                        .put("damage", 10f)
                        .put("speed",movement.getScaleSpeed())
                        .put("hitRadius", 5f)
                        .put("hitCenter", new Point2D(0f, 0f))
                        .put("offsetPos", new Point2D(0f, 0f))
                        .put("duration",6f)
                        .put("targetPos",entity.getPosition().add(new Point2D(60f,40f)).add(new Point2D(1,1)))
                        .put("scaleX", movement.getScaleX())
                        .put("scaleY", movement.getScaleX()));
                    FXGL.spawn("bullet2", new SpawnData(entity.getPosition().add(60f,0f))
                            .put("startPos",entity.getPosition().add(new Point2D(60f,40f)))
                            .put("damage", 10f)
                            .put("speed",movement.getScaleSpeed())
                            .put("hitRadius", 5f)
                            .put("hitCenter", new Point2D(0f, 0f))
                            .put("offsetPos", new Point2D(0f, 0f))
                            .put("duration",6f)
                            .put("targetPos",entity.getPosition().add(new Point2D(60f,40f)).add(new Point2D(1,-1)))
                            .put("scaleX", movement.getScaleX())
                            .put("scaleY", movement.getScaleY()));}
                FXGL.spawn("blade", new SpawnData( entity.getCenter())
                        .put("startPos",  entity.getCenter())
                        .put("damage", 10f)
                        .put("hitCenter",(new Point2D(10f,-30f)))
                        .put("hitRadius", attackRadius)
                        .put("offsetPos",new Point2D(0f,0f))
                        .put("duration",0.1f));
                entity.getComponent(PlayerSoundComponent.class).playAttack();

            }
        }, Duration.seconds(newattackInterval2*0.6)); // 动画一半时出招

        // 本轮攻击播完后的处理：要么连击、要么解锁回闲/走
        texture.setOnCycleFinished(() -> {
            boolean stillHolding = movement.isAttackingLeft() || movement.isAttackingRight();
            if (stillHolding) {
                // 长按：无缝进入下一轮攻击（注意：这里不解锁，持续连击）
                startAttack();
            } else {
                // 松手：解锁，并回到 Idle/Walk
                attackInProgress = false;
                if (movement.isMoving()) {
                    state = State.WALK;
                    texture.loopAnimationChannel(walk);
                } else {
                    state = State.IDLE;
                    texture.loopAnimationChannel(idle);
                }
            }
        });
    }
}
