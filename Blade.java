package com.survivor.entity;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.util.getMoveDirection;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class Blade extends Projectile{
    public Blade(Point2D startPos,float damage,float hitRadius,Point2D hitCenter,Point2D offsetPos,float duration) {
        super(0f, damage,hitRadius,hitCenter,offsetPos,true);
        FXGL.getGameTimer().runOnceAfter(() -> {
            explode();
        }, Duration.seconds(duration));
    }
    @Override
    protected void getNextMove() {

        direction = new Vec2(0,0);
    }
    @Override
    public void onAdded()
    {
        super.onAdded();

    }


    @Override
    public void setAnimChannel() {
    }

    @Override
    protected void handleHeroCollision(Entity hero) {


    }

    @Override
    protected void handleMonsterCollision(Entity monster) {
        //TODO:Enemy.hurt
    }

}
