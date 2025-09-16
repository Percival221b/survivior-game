package com.survivor.entity.weapon;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Projectile;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class MagicAttack extends Projectile {
    public MagicAttack(float damage,float hitRadius,Point2D hitCenter,Point2D offsetPos) {
        super(0, damage,hitRadius,hitCenter,offsetPos,false);
    }
    @Override
    protected void getNextMove() {

        //        direction =  getMoveDirection.getCircularMoveDir(center,entity.getPosition(), true);
//        direction = getMoveDirection.getCentripetalMoveDir(center,entity.getPosition(),true,true,0.9999f);
        //        System.out.println(entity.getPosition().distance(center));

        //        System.out.println(direction);
//        direction = getMoveDirection.getLinearMoveDir(entity.getPosition(),targetPos);
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

        FXGL.getGameTimer().runOnceAfter(() -> {
            currentState = State.EXPLODE;
        }, Duration.seconds(0.5f));

    }


    @Override
    public void setAnimChannel() {

        explodeAnim = new AnimationChannel(
                FXGL.image("Priest-Attack_effect.png"),
                5,   // 帧数
                100, 100, // 单帧大小
                Duration.seconds(0.5), // 一轮动画时长
                0, 4
        );
        flyAnim = explodeAnim;
        texture = new AnimatedTexture(explodeAnim);
        entity.getViewComponent().addChild(texture);
        explode();

    }

    @Override
    protected void handleHeroCollision(Entity hero) {


        if(currentState == State.EXPLODE)
        {
            currentState = State.DIE;
            //TODO hero.hurt
        }
    }

    @Override
    protected void handleMonsterCollision(Entity monster) {

    }

    @Override
    protected void setAutoRemove() {

    }
}