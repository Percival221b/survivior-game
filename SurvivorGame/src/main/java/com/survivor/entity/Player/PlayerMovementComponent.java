package com.survivor.entity.Player;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import com.almasb.fxgl.core.math.Vec2;

/**
 * 玩家移动组件
 * 专为 FXGL 框架设计，处理WASD或方向键控制下的角色移动。
 */
public class PlayerMovementComponent extends Component {

    private PhysicsComponent physics;
    public PlayerState state = PlayerState.IDLE;

    private double speed = 400; // 单位：像素/秒

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


    private double attackInterval = 0.6;  // 攻击冷却
    private double attackTimer = 0;

    private double dashOrbTimer = 0;
    private double dashOrbInterval = 0.03;

    private double attackSlowTimer = 0;   // 攻击减速剩余时间
    private double attackSlowDuration = 0.6; // 攻击动画时长（秒）

    // 新增暂停标志
    private boolean paused = false;

    @Override
    public void onAdded() {
        this.physics = entity.getComponent(PhysicsComponent.class);

        // 注册输入绑定（WASD）
        FXGL.getInput().addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() { if (!paused) movingUp = true; }
            @Override
            protected void onActionEnd() {  if (!paused) movingUp = false; }
        }, KeyCode.W);

        FXGL.getInput().addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {  if (!paused) movingDown = true; }
            @Override
            protected void onActionEnd() {  if (!paused) movingDown = false; }
        }, KeyCode.S);

        FXGL.getInput().addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {  if (!paused) movingLeft = true; }
            @Override
            protected void onActionEnd() {  if (!paused) movingLeft = false; }
        }, KeyCode.A);

        FXGL.getInput().addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {  if (!paused) movingRight = true; }
            @Override
            protected void onActionEnd() {  if (!paused) movingRight = false; }
        }, KeyCode.D);

        FXGL.getInput().addAction(new UserAction("AttackLeft") {
            @Override protected void onActionBegin() {
                if (!paused) attackingLeft = true;
                //FXGL.getNotificationService().pushNotification("攻击开始！");
            }
            @Override protected void onActionEnd() {
                attackingLeft= false;
                //FXGL.getNotificationService().pushNotification("攻击结束！");
            }
        }, KeyCode.J);

        FXGL.getInput().addAction(new UserAction("AttackRight") {
            @Override protected void onActionBegin() {
                if (!paused) attackingRight = true;
                //FXGL.getNotificationService().pushNotification("攻击开始！");
            }
            @Override protected void onActionEnd() {
                attackingRight= false;
                //FXGL.getNotificationService().pushNotification("攻击结束！");
            }
        }, KeyCode.K);

        FXGL.getInput().addAction(new UserAction("Dash") {
            @Override
            protected void onActionBegin() {
                if (!paused && !dashing && dashCooldownTimer <= 0) { // 冷却完成才能冲刺
                    dashing = true;
                    dashTimer = dashDuration;
                    dashCooldownTimer = dashCooldownMax; // 开始进入冷却
                    FXGL.getNotificationService().pushNotification("冲刺！");
                }
            }
        }, KeyCode.L);

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

    public boolean isMoving() {
        return movingUp || movingDown || movingLeft || movingRight;
    }

    public boolean isMovingLeft() { return movingLeft; }
    public boolean isMovingRight() { return movingRight; }

    public double getSpeed() { return speed; }
    public boolean isMovingUp() {return movingUp;}
    public boolean isMovingDown() {return movingDown;}
    public PlayerState getState() {
        return state;
    }
    public boolean isAttackingLeft() {
        return attackingLeft;
    }
    public boolean isAttackingRight() {
        return attackingRight;
    }
    public boolean isDashing() {
        return dashing;
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
}
