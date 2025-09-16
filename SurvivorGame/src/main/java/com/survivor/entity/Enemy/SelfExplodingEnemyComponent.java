package com.survivor.entity.Enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.XPComponent;
import com.survivor.main.EntityType;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameScene;

@Required(PhysicsComponent.class)   // ⚠️ 声明依赖
public class SelfExplodingEnemyComponent extends Component {


    private double health=1500;
    private boolean dead = false;

    private PhysicsComponent physics;  // FXGL 会自动注入

    private AnimatedTexture texture;

    private AnimationChannel idleAnim;
    private AnimationChannel moveAnim;
    private AnimationChannel attackAnim;
    private AnimationChannel deadAnim;


    private boolean isAttacking = false;

    private double speed = 340;
    private double attackRange = 80;
    private long lastAttackTime = 0;
    private long attackCooldown = 1500; // 毫秒

    @Override
    public void onAdded() {
        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setFixedRotation(true);
        });
        // 初始化动画
        idleAnim = new AnimationChannel(FXGL.image("ExplodingCrab/ExplodingCrab idle 80x80.png"),
                13, 80, 80, Duration.seconds(1), 0, 12);
        moveAnim = new AnimationChannel(FXGL.image("ExplodingCrab/ExplodingCrab Walk 80x80.png"),
                10, 80, 80, Duration.seconds(0.8), 0, 9);
        attackAnim = new AnimationChannel(FXGL.image("ExplodingCrab/ExplodingCrab Explosion 80x80.png"),
                23, 80, 80, Duration.seconds(0.8), 0, 22);
        deadAnim = new AnimationChannel(FXGL.image("ExplodingCrab/ExplodingCrab Death 80x80.png"),
                8, 80, 80, Duration.seconds(0.8), 0, 7);
        texture = new AnimatedTexture(idleAnim);
        texture.setTranslateX(-125); // 让贴图居中
        texture.setTranslateY(-125);

        entity.getViewComponent().addChild(texture);
        texture.loopAnimationChannel(idleAnim);
    }

    @Override
    public void onUpdate(double tpf) {
        if (dead) {return;}

        if (isAttacking) {
            // 攻击过程中不移动
            physics.setVelocityX(0);
            physics.setVelocityY(0);
            return;
        }

        // 获取玩家
        var playerOpt = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                .stream().findFirst();

        if (playerOpt.isEmpty()) {
            return;
        }

        Point2D playerPos = playerOpt.get().getCenter().add(new Point2D(30f,40f));
        Point2D myPos = entity.getCenter();

        double distance = myPos.distance(playerPos);
        Point2D center =entity.getCenter();

        if (distance > attackRange) {
            // 追踪玩家
            Point2D dir = playerPos.subtract(myPos).normalize();
            physics.setVelocityX(dir.getX() * speed);
            physics.setVelocityY(dir.getY() * speed);

            if (texture.getAnimationChannel() != moveAnim) {
                texture.loopAnimationChannel(moveAnim);
            }

            // 翻转朝向
            texture.setScaleX(dir.getX() < 0 ? -1 : 1);

        } else {
            physics.setVelocityX(0);
            physics.setVelocityY(0);

            FXGL.runOnce(() -> {
                FXGL.spawn("bomb", new SpawnData( center)
                        .put("startPos",  center)
                        .put("damage", 10f)
                        .put("hitCenter",(new Point2D(-60f,-37f)))
                        .put("hitRadius", 80f)
                        .put("offsetPos",new Point2D(0f,0f))
                        .put("duration",0.1f));
            }, Duration.seconds(0.4));

           this.takeDamage(1999);
            texture.playAnimationChannel(attackAnim);

        }
    }

    private void playAttack() {
        isAttacking = true;
        texture.playAnimationChannel(attackAnim);

        texture.setOnCycleFinished(() -> {
            if (texture.getAnimationChannel() == attackAnim) {
                isAttacking = false;
                texture.loopAnimationChannel(idleAnim);
                // 清空回调时用空实现代替，不要传 null
                texture.setOnCycleFinished(() -> {});
            }
        });
    }

    public void takeDamage(double damage) {
        this.showDamage(damage,entity.getX(),entity.getY());
        System.out.println(dead);
        if (dead==true) {return; }// 已经死亡不再处理}
        Point2D center =entity.getCenter();
        health -= damage;
        System.out.println("Enemy took " + damage + " damage. Remaining HP: " + health);

        if (health <= 0) {
            var playerOpt= FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                    .stream().findFirst();
            playerOpt.get().getComponent(XPComponent.class).gainXP(80);

            if(dead){
                return;
            }
            dead = true;
            physics.setVelocityX(0);
            physics.setVelocityY(0);

            FXGL.runOnce(() -> {
                FXGL.spawn("bomb", new SpawnData( center)
                        .put("startPos",  center)
                        .put("damage", 10f)
                        .put("hitCenter",(new Point2D(-60f,-37f)))
                        .put("hitRadius", 70f)
                        .put("offsetPos",new Point2D(0f,0f))
                        .put("duration",0.1f));
            }, Duration.seconds(0.4));

            FXGL.runOnce(() -> {
                if(entity != null){
                entity.removeFromWorld();}
            }, Duration.seconds(0.8));

            texture.playAnimationChannel(attackAnim);

        } else {
            // TODO: 可以在这里加受伤动画或闪烁效果
            // 例如 texture.loopAnimationChannel(hurtAnim);
        }
    }

    private void die() {
        dead = true;

        // 停止动作
        physics.setVelocityX(0);
        physics.setVelocityY(0);
        isAttacking = false;
        texture.playAnimationChannel(deadAnim);
        // 绑定“动画播完”回调
        texture.setOnCycleFinished(() -> {
            if (texture.getAnimationChannel() ==deadAnim ) {
                // 解绑：不能传 null，改成空 lambda
                texture.setOnCycleFinished(() -> {});
                if (entity != null ) {
                    entity.removeFromWorld();
                }
            }
        });

        // 播放死亡动画（单参）
        texture.playAnimationChannel(deadAnim);
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
    public void showDamage(double dmg, double x, double y) {
        Text text = new Text(String.valueOf((int) dmg));
        if(dmg<260){
            text.setFill(Color.WHITE);
            //text.setStyle("-fx-font-size: 12px");
            text.setFont(Font.font("Impact", 20));
        } else if (dmg<400) {
            text.setFill(Color.YELLOW);
            text.setFont(Font.font("Impact", 32));
            //text.setStyle("-fx-font-size: 20px");
        }else {
            text.setFill(Color.RED);
            text.setFont(Font.font("Impact", 40));
        }
        var playerOpt = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                .stream().findFirst();
        Point2D playerPos = playerOpt.get().getCenter();
        double dx=playerPos.getX()-x;
        double dy=playerPos.getY()-y;

        text.setTranslateX(1280-dx-100);
        text.setTranslateY(640-dy-40); // 往上偏移 40 像素
        text.setScaleX(0.1);
        text.setScaleY(0.1);

// 放大动画
        ScaleTransition st = new ScaleTransition(Duration.seconds(0.2), text);
        st.setFromX(0.1);
        st.setFromY(0.1);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();


        getGameScene().addUINode(text);

        // 1 秒后移除
        FXGL.runOnce(() -> getGameScene().removeUINode(text), Duration.seconds(0.3));

    }
}
