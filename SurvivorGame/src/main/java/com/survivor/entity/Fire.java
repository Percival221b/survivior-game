package com.survivor.entity;


import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import com.almasb.fxgl.texture.AnimatedTexture;
import javafx.util.Duration;
import com.survivor.util.getMoveDirection;

public class Fire extends Projectile {

    private Point2D center;

    public Fire(Point2D startPos, float speed, float damage, Point2D center, float hitRadius, Point2D hitCenter, Point2D offsetPos) {
        super(speed, damage,hitRadius,hitCenter,offsetPos);
        this.center = center;
    }
    @Override
    protected void getNextMove() {
        direction = getMoveDirection.getCentripetalMoveDir(center,entity.getPosition(),true,true,0.9999f);
    }
    @Override
    public void onAdded() {
        super.onAdded();
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
