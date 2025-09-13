package com.survivor.entity.weapon;


import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Projectile;
import javafx.geometry.Point2D;
import com.almasb.fxgl.texture.AnimatedTexture;
import javafx.util.Duration;
import com.survivor.util.getMoveDirection;

public class Fire extends Projectile {

    private Point2D center;
    private float duration;
    public Fire(float speed, float damage, Point2D center,float hitRadius,Point2D hitCenter,Point2D offsetPos,float duration) {
        super(speed, damage,hitRadius,hitCenter,offsetPos,false);
        this.center = center;
        this.duration = duration;
    }
    @Override
    protected void getNextMove() {
        direction = getMoveDirection.getCentripetalMoveDir(center,entity.getPosition(),true,true,0.9999f);
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
                FXGL.image("FireBall.png"),
                1,   // 帧数
                80, 112, // 单帧大小
                Duration.seconds(0.5), // 一轮动画时长
                0, 0
        );
        explodeAnim = new AnimationChannel(
                FXGL.image("FireBall.png"),
                4,   // 帧数
                80, 112, // 单帧大小
                Duration.seconds(0.5), // 一轮动画时长
                1, 4
        );
        texture = new AnimatedTexture(flyAnim);
        texture.loopAnimationChannel(flyAnim);
        entity.getViewComponent().addChild(texture);
    }

    @Override
    protected void handleHeroCollision(Entity hero) {

        if (currentState == State.FLY)
        {
            explode();
        }
        else if(currentState == State.EXPLODE)
        {
            currentState = State.DIE;
            //TODO hero.hurt
        }
    }

    @Override
    protected void handleMonsterCollision(Entity monster) {

    }

}
