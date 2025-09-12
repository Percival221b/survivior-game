package com.survivor.entity.Player;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.PlayerMovementComponent;
import javafx.util.Duration;
import javafx.geometry.Point2D;

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

    @Override
    public void onAdded() {
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
//
        double x = entity.getPosition().getX()+50;
        double y = entity.getPosition().getY()+36;

        Entity debugCircle = FXGL.entityBuilder()
                .at(x, y) // 直接放在这个位置
                .view(new javafx.scene.shape.Circle(5, javafx.scene.paint.Color.BLUE))
                .buildAndAttach();

// 生成一个蓝色圆，放在 entity.getPosition() 的坐标点
        movement = entity.getComponent(PlayerMovementComponent.class);
    }

    @Override
    public void onUpdate(double tpf) {
        // 朝向：优先用攻击方向，其次移动方向



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

        boolean leftAttack = movement.isAttackingLeft();
        boolean rightAttack = movement.isAttackingRight();

        texture.playAnimationChannel(attack);

        // 关键：在攻击动画的关键帧触发攻击逻辑
        FXGL.runOnce(() -> {
            if (leftAttack) {
                //if (movement.isAttackingLeft()) {
                FXGL.spawn("fireX", new SpawnData( entity.getCenter())
                        .put("startPos",  entity.getCenter())
                        .put("speed", 150f)
                        .put("damage", 10f)
                        .put("center", new Point2D(entity.getPosition().getX()+50,entity.getPosition().getY()+67))
                        .put("hitCenter",entity.getCenter())
                        .put("hitRadius", 40f)
                        .put("offsetPos",new Point2D(0f,0f))
                );
                entity.getComponent(PlayerSoundComponent.class).playAttack();

                //  );
            } else if (rightAttack) {
                entity.getComponent(PlayerSoundComponent.class).playAttack();
                //  } else if (movement.isAttackingRight()) {
                FXGL.spawn("xpOrb", new SpawnData(entity.getCenter()).put("xpAmount", 10));
            }
        }, Duration.seconds(0.6*0.6)); // 动画一半时出招

        texture.setOnCycleFinished(() -> {
            boolean stillHolding = movement.isAttackingLeft() || movement.isAttackingRight();
            if (stillHolding) {
                startAttack(); // 长按连续攻击
            } else {
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