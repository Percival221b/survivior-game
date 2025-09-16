package com.survivor.entity.weapon;


import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Projectile;
import javafx.geometry.Point2D;
import com.almasb.fxgl.texture.AnimatedTexture;
import javafx.util.Duration;
import com.survivor.util.getMoveDirection;

public class BossBullet extends Projectile {

    private Point2D center;
    private float duration;
    private boolean isClockwise;
    public BossBullet(float speed, float damage, Point2D center, float hitRadius, Point2D hitCenter, Point2D offsetPos, float duration,boolean isClockwise) {
        super(speed, damage,hitRadius,hitCenter,offsetPos,false);
        this.center = center;
        this.duration = duration;
        this.isClockwise = isClockwise;
    }
    @Override
    protected void getNextMove() {
        direction = getMoveDirection.getCentripetalMoveDir(center,entity.getPosition(),isClockwise,true,0.9999f);
    }
    @Override
    public void onAdded() {
        super.onAdded();
    }

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
                FXGL.image("GreenBall.png"),
                10,   // 帧数
                40, 32, // 单帧大小
                Duration.seconds(1), // 一轮动画时长
                0, 9
        );

        explodeAnim = new AnimationChannel(
                FXGL.image("GreenBallExplode.png"),
                6,   // 帧数
                40, 32, // 单帧大小
                Duration.seconds(0.5), // 一轮动画时长
                0, 5
        );
        texture = new AnimatedTexture(flyAnim);
        texture.loopAnimationChannel(flyAnim);
        texture.setTranslateX(-16);
        texture.setTranslateY(-16);
        entity.getViewComponent().addChild(texture);
    }

    @Override
    protected void handleHeroCollision(Entity hero) {

            hero.getComponent(HealthComponent.class).takeDamage((int)500);
            explode();


    }

    @Override
    protected void handleMonsterCollision(Entity monster) {

    }

}