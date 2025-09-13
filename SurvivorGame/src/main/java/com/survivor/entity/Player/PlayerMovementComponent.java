package com.survivor.entity.Player;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import com.almasb.fxgl.core.math.Vec2;

import java.util.function.Consumer;

/**
 * 玩家移动组件
 * 专为 FXGL 框架设计，处理WASD或方向键控制下的角色移动。
 */
public class PlayerMovementComponent extends Component {

    private PhysicsComponent physics;
    public PlayerState state = PlayerState.IDLE;

    private double speed = 400; // 单位：像素/秒
    private double attack = 10;       // 基础攻击

    // 移动方向状态
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    private boolean attackingLeft = false;
    private boolean attackingRight = false;

    private boolean dashing = false;
    // 冲刺速度，可以自己调
    private double dashDuration = 0.1; // 持续时间（秒）
    private double dashTimer = 0;      // 冲刺剩余时间

    private double dashCooldownMax = 2.0; // 冲刺冷却 2 秒
    private double dashCooldownTimer = 0; // 冷却计时器
    private boolean dashSoundPlayed = false;  // 新增字段


    private double attackInterval = 0.6;  // 攻击冷却
    private double attackTimer = 0;

    private double dashOrbTimer = 0;
    private double dashOrbInterval = 0.03;

    private double attackSlowTimer = 0;   // 攻击减速剩余时间
    private double attackSlowDuration = 0.6; // 攻击动画时长（秒）

    // 新增暂停标志
    private boolean paused = false;
    private Consumer<Double> attackIntervalonChange;
    private Consumer<Double> onSpeedChange;
    @Override
    public void onAdded() {
        this.physics = entity.getComponent(PhysicsComponent.class);
    }
    @Override
    public void onUpdate(double tpf) {
        if (paused) {
            // 停止一切移动
            physics.setVelocityX(0);
            physics.setVelocityY(0);
            return;
        }
        double dx = 0, dy = 0;

        if (movingLeft)  dx -= 1;
        if (movingRight) dx += 1;
        if (movingUp)    dy -= 1;
        if (movingDown)  dy += 1;

        Point2D velocity = new Point2D(dx, dy);

        if (dashing) {
            velocity = velocity.normalize().multiply(speed+1000);
            dashTimer -= tpf;
            if (!dashSoundPlayed) {
                entity.getComponent(PlayerSoundComponent.class).playDash();
                dashSoundPlayed = true; // 只放一次
            }
            if (dashTimer <= 0) {
                dashing = false; // 冲刺结束
            }
            dashOrbTimer -= tpf;
            if (dashOrbTimer <= 0) {
                FXGL.spawn("xpOrb", new SpawnData(entity.getCenter()).put("xpAmount", 1));
                dashOrbTimer = dashOrbInterval; // 重置间隔
            }
        } else if (attackSlowTimer > 0) {
            velocity = velocity.normalize().multiply(speed * 0.75);
            attackSlowTimer -= tpf;
        }
        else {
            velocity = velocity.normalize().multiply(speed);
        }
        physics.setVelocityX(velocity.getX());
        physics.setVelocityY(velocity.getY());

        if (movingLeft) {
            state = PlayerState.WALK_LEFT;
        } else if (movingRight) {
            state = PlayerState.WALK_RIGHT;
        } else if (attackingLeft) {
            state = PlayerState.ATTACKLEFT;
        }  else if (attackingRight) {
            state = PlayerState.ATTACKRIGHT;
        } else {
            state = PlayerState.IDLE;
        }
        if (dashCooldownTimer > 0) {
            dashCooldownTimer -= tpf;
        }
    }


    // --- 公共方法供动画或其他组件调用 ---

    public boolean isMoving() {return movingUp || movingDown || movingLeft || movingRight;}
    public boolean isMovingLeft() { return movingLeft; }
    public boolean isMovingRight() { return movingRight; }
    public boolean isMovingUp() {return movingUp;}
    public boolean isMovingDown() {return movingDown;}

    public double getSpeed() { return speed; }
    public PlayerState getState() {return state;}
    public boolean isAttackingLeft() {return attackingLeft;}
    public boolean isAttackingRight() {return attackingRight;}
    public boolean isDashing() {return dashing;}
    public double getAttackInterval() {return attackInterval;}

    public void increaseAttack(double percent) {
        attack *= (1 + percent);
    }
    public void setAttackInterval(double newattackInterval) {
        this.attackInterval = newattackInterval;
        if (attackIntervalonChange != null) {
            attackIntervalonChange.accept(newattackInterval);
        }
    }

    public void increaseSpeed(double percent) {
        speed *= (1 + percent);
    }

    public void decreaseDashCooldown(double percent) {
        dashCooldownMax *= (1 + percent);
    }


    public void stop() {
        movingUp = movingDown = movingLeft = movingRight = false;
        physics.setBodyLinearVelocity(new Vec2(0, 0));
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) {
            stop();
        }
    }

    public void setSpeed(double speed) { this.speed = speed; }

    public void resetAttack() {
        attackingLeft = false;
        attackingRight = false;
    }
    public void triggerAttackSlow(double duration) {
        attackSlowTimer = duration;
    }

    public void setMovingUp(boolean value) { movingUp = value; }
    public void setMovingDown(boolean value) { movingDown = value; }
    public void setMovingLeft(boolean value) { movingLeft = value; }
    public void setMovingRight(boolean value) { movingRight = value; }

    public void setAttackingLeft(boolean value) { attackingLeft = value; }
    public void setAttackingRight(boolean value) { attackingRight = value; }

    public void startDash() {
        if (!dashing && dashCooldownTimer <= 0) {
            dashing = true;
            dashTimer = dashDuration;
            dashCooldownTimer = dashCooldownMax;
            FXGL.getNotificationService().pushNotification("冲刺！");
        }
    }
    //回调
    public void setattackIntervalOnChange(Consumer<Double> callback) {
        this.attackIntervalonChange = callback;
    }
    public void setOnSpeedChange(Consumer<Double> callback) {
        this.onSpeedChange = callback;
    }
}
