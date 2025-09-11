package com.survivor.entity;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Projectile;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import com.survivor.entity.interfaces.Renderable;
import javafx.scene.canvas.GraphicsContext;
import com.almasb.fxgl.core.math.Vec2;
import com.survivor.util.*;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;

public class fire extends Projectile {

    private float damage;
    private Point2D center;
    boolean canMove = true;
    Point2D startPos;
    Vec2 direction;
    float speed;

    public fire(Point2D startPos, float speed, float damage, Point2D center,float hitRadius,Point2D hitCenter) {
        super(startPos, speed, damage,hitRadius,hitCenter);
        this.startPos = startPos;
        this.speed = speed;
        this.damage = damage;
        this.center = center;
    }
    @Override
    protected void getNextMove() {

        direction =  getMoveDirection.getCircularMoveDir(center, startPos, true);

    }



    @Override
    public void setAnimChannel() {
        flyAnim = new AnimationChannel(
                FXGL.image("projectiles/FireBall.png"),
                1,   // 帧数
                80, 112, // 单帧大小
                Duration.seconds(0.5), // 一轮动画时长
                0, 0
        );
        explodeAnim = new AnimationChannel(
                FXGL.image("projectiles/FireBall.png"),
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
