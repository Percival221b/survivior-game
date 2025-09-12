package com.survivor.entity.Player;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.PlayerMovementComponent;
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

    @Override
    public void onAdded() {
        idle = new AnimationChannel(
                FXGL.image("Idle.png"), 10, 135, 135, Duration.seconds(0.5), 0, 9
        );
        walk = new AnimationChannel(
                FXGL.image("Run.png"), 6, 135, 135, Duration.seconds(0.7), 0, 5
        );
        attack = new AnimationChannel(
                FXGL.image("Attack2.png"), 4, 135, 135, Duration.seconds(0.6), 0, 3
        );
        dash = new AnimationChannel(
                FXGL.image("Run.png"), 6, 135, 135, Duration.seconds(0.2), 0, 5
        );

        texture = new AnimatedTexture(idle);
        texture.setScaleX(2.5);
        texture.setScaleY(2.5);
        entity.getViewComponent().addChild(texture);
        texture.loopAnimationChannel(idle);

        movement = entity.getComponent(PlayerMovementComponent.class);
    }

    @Override
    public void onUpdate(double tpf) {
        // 朝向：优先用攻击方向，其次移动方向
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

        // 攻击进行中：完全锁住，禁止任何其它动画打断
        if (attackInProgress) {
            return;
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

        // 根据当前按键方向再确认一下朝向


        texture.playAnimationChannel(attack);

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
