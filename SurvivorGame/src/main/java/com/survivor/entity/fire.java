package com.survivor.entity;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.util.getMoveDirection;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class fire extends Projectile {

    private Point2D center;

    public fire(Point2D startPos, float speed, float damage, Point2D center, float hitRadius, Point2D hitCenter, Point2D offsetPos) {
        super(speed, damage,hitRadius,hitCenter,offsetPos);
        this.center = center;
    }
    @Override
    protected void getNextMove() {

//        direction =  getMoveDirection.getCircularMoveDir(center,entity.getPosition(), true);
        direction = getMoveDirection.getCentripetalMoveDir(center,entity.getPosition(),true,true,0.9999f);
//        System.out.println(entity.getPosition().distance(center));

//        System.out.println(direction);
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
    }


    @Override
    public void setAnimChannel() {
        flyAnim = new AnimationChannel(
                FXGL.image("ireBall.png"),
                1,   // 帧数
                80, 112, // 单帧大小
                Duration.seconds(0.5), // 一轮动画时长
                0, 0
        );
        explodeAnim = new AnimationChannel(
                FXGL.image("ireBall.png"),
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
