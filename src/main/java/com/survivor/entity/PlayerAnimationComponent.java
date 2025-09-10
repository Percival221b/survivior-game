package com.survivor.entity;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;


/**
 * 主角动画组件
 * 依赖 PlayerMovementComponent 来切换 Idle / Walk
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

    @Override
    public void onAdded() {
        // 初始化动画通道
        idle = new AnimationChannel(
                FXGL.image("Idle.png"),
                10,   // 帧数
                135, 135, // 单帧大小
                Duration.seconds(0.5), // 一轮动画时长
                0, 9
        );

        walk = new AnimationChannel(
                FXGL.image("Run.png"),
                6,
                135, 135,
                Duration.seconds(0.7),
                0, 5
        );

        attack = new AnimationChannel(
                FXGL.image("Attack3.png"),
                5,      // 帧数（按你的攻击图集来改）
                135, 135,
                Duration.seconds(0.6),
                0, 4
        );
        dash = new AnimationChannel(
                FXGL.image("Run.png"),
                6,      // 帧数（按你的攻击图集来改）
                135, 135,
                Duration.seconds(0.2),
                0, 5
        );


        texture = new AnimatedTexture(idle);
        texture.loopAnimationChannel(idle);
        texture.setScaleX(2.5);
        texture.setScaleY(2.5);
        entity.getViewComponent().addChild(texture);

        // 依赖移动组件
        movement = entity.getComponent(PlayerMovementComponent.class);

    }

    @Override
    public void onUpdate(double tpf) {
        if (movement.isMovingLeft()) {
            texture.setScaleX(-2.5);
        } else if (movement.isMovingRight()) {
            texture.setScaleX(2.5);
        }

        if (movement.isAttackingLeft()) {
            texture.setScaleX(-2.5);
        } else if (movement.isAttackingRight()) {
            texture.setScaleX(2.5);
        }

        if (movement.isAttackingLeft()||movement.isAttackingRight()) {
            if (state != State.ATTACK) {
                state = State.ATTACK;


                // 播放一次攻击动画
                texture.playAnimationChannel(attack);

                // 动画播完后才回到 Idle（或者根据是否在移动回到 Walk）
                texture.setOnCycleFinished(() -> {
                    if (movement.isMoving()) {
                        state = State.WALK;
                        texture.loopAnimationChannel(walk);
                    } else {
                        state = State.IDLE;
                        texture.loopAnimationChannel(idle);
                    }
                });
            }

            return; // 攻击时完全屏蔽后续移动/Idle 切换逻辑
        }

        boolean moving = movement.isMoving();

        if (moving && state != State.WALK) {
            state = State.WALK;
            texture.loopAnimationChannel(walk);
        } else if (!moving && state != State.IDLE) {
            state = State.IDLE;
            texture.loopAnimationChannel(idle);
        }



        if (movement.isDashing()) {
            if (state != State.DASH) {
                state = State.DASH;
                texture.playAnimationChannel(dash);
                texture.setOnCycleFinished(() -> {
                    state = State.IDLE;
                    texture.loopAnimationChannel(idle);
                });
            }
            return;
        }
    }


}
