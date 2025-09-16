package com.survivor.entity.weapon;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Enemy.*;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Player.PlayerMovementComponent;
import com.survivor.entity.Projectile;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import static com.survivor.main.GameApp.player;

public class Bullet2 extends Projectile{
    private float duration;
    private Point2D targetPos;
    public Bullet2(float speed, float damage, float hitRadius, Point2D hitCenter, Point2D offsetPos, float duration, Point2D targetPos) {
        super(speed, damage,hitRadius,hitCenter,offsetPos,false);
        this.duration = duration;
        this.targetPos = targetPos;
    }
    @Override
    protected void getNextMove() {

    }
    @Override
    public void onAdded()
    {
        super.onAdded();
        //        ViewComponent viewComponent = player.getComponent(ViewComponent.class);

        // 将视图向左偏移其宽度的一半，向上偏移其高度的一半
        // 这样视图的中心就与实体的 (x, y) 坐标对齐了
        //        view.setTranslateX(-view.getBoundsInLocal().getWidth() / 2);
        //        view.setTranslateY(-view.getBoundsInLocal().getHeight() / 2);
        direction = new Vec2(targetPos.subtract(entity.getPosition()));
    }

    @Override
    protected void setAutoRemove()
    {
        FXGL.getGameTimer().runOnceAfter(() -> {
            if(currentState ==State.FLY)
                explode();
        }, Duration.seconds(duration));
    }
    @Override
    public void setAnimChannel() {
        flyAnim = new AnimationChannel(
                FXGL.image("Windwater1.png"),
                9,// 帧数
                128, 128, // 单帧大小
                Duration.seconds(0.2), // 一轮动画时长
                0, 8
        );
        explodeAnim = new AnimationChannel(
                FXGL.image("windwater2.png"),
                8,   // 帧数
                128, 128, // 单帧大小
                Duration.seconds(0.4), // 一轮动画时长
                0, 7
        );
        texture = new AnimatedTexture(flyAnim);
        texture.setTranslateX(-64);
        texture.setTranslateY(-64);
        texture.loopAnimationChannel(flyAnim);
        entity.getViewComponent().addChild(texture);
    }

    @Override
    protected void handleHeroCollision(Entity hero) {

            hero.getComponent(HealthComponent.class).takeDamage((int)damage);
            explode();
    }

    @Override
    protected void handleMonsterCollision(Entity monster) {
        if(PlayerMovementComponent.speacil) {
            Vec2 impulse = new Vec2(-1000, -1000);
            Vec2 point = new Vec2(monster.getCenter());
            physics.applyBodyLinearImpulse(impulse, point, true);
        }




        if(monster.hasComponent(SprintEnemyCompontBat.class)) {
            monster.getComponent(SprintEnemyCompontBat.class).takeDamage(PlayerMovementComponent.attack);
        }else if (monster.hasComponent(SplitEnemyComponent.class)){
            monster.getComponent(SplitEnemyComponent.class).takeDamage(PlayerMovementComponent.attack);
        }else if (monster.hasComponent(zhiEnemyComponent.class)){
            monster.getComponent(zhiEnemyComponent.class).takeDamage(PlayerMovementComponent.attack);
        }else if(monster.hasComponent(SmallSplitEnemyComponent.class)) {
            monster.getComponent(SmallSplitEnemyComponent.class).takeDamage(PlayerMovementComponent.attack);
        }else if (monster.hasComponent(RangedEnemyComponent.class)) {
            monster.getComponent(RangedEnemyComponent.class).takeDamage(PlayerMovementComponent.attack);
        }
        else if (monster.hasComponent(SelfExplodingEnemyComponent.class)) {
            monster.getComponent(SelfExplodingEnemyComponent.class).takeDamage(PlayerMovementComponent.attack);
        }
        else{
        }
        explode();
    }
    public void move() {
//        System.out.println(direction);
        direction = direction.normalize();
        physics.setVelocityX(direction.x * speed);
        physics.setVelocityY(direction.y * speed);

    }

}