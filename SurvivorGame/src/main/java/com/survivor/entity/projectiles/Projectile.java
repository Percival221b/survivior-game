package com.survivor.entity.projectiles;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.entity.components.TransformComponent;
import com.almasb.fxgl.physics.*;
import com.almasb.fxgl.physics.box2d.collision.shapes.CircleShape;
import com.almasb.fxgl.physics.box2d.collision.shapes.Shape;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.interfaces.Renderable;
import com.survivor.util.EntityType;
import javafx.geometry.Point2D;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Handler;

public abstract class Projectile extends Component implements Renderable {
    protected  float damage;
    protected Entity entity;
    protected PhysicsComponent physics;
    protected TransformComponent transform;
    protected boolean canMove = true;
    protected Point2D startPos;
    protected float speed;
    protected Vec2 direction;
    protected AnimatedTexture texture;
    protected float hitRadius;
    protected Point2D hitCenter;
    protected enum State {FLY, EXPLODE,DIE}

    ;
    protected AnimationChannel flyAnim;
    protected AnimationChannel explodeAnim;
    protected State currentState = State.FLY;

    protected Projectile(Point2D startPos, float speed, float damage) {
        this.startPos = startPos;
        this.speed = speed;
        this.damage = damage;
    }

    public void onAdded() {

        entity = getEntity();
        physics = entity.getComponent(PhysicsComponent.class);
        transform = entity.getTransformComponent();
        SensorCollisionHandler handler = new SensorCollisionHandler() {
            @Override
            protected void onCollisionBegin(Entity other) {
//                super.onCollisionBegin(other);
                if(other.getType().equals(EntityType.HERO)&&entity.getType().equals(EntityType.PROJECTILEX))
                    handleHeroCollision(other);
                else if(other.getType().equals(EntityType.MONSTER)&&entity.getType().equals(EntityType.PROJECTILE))
                    handleMonsterCollision(other);
            }
        };
        HitBox hitBox = new HitBox(hitCenter, BoundingShape.circle(hitRadius)) ;
        physics.addSensor(hitBox,handler);
        setAnimChannel();

    }

    public void move(double tpf) {
        physics.setVelocityX(direction.x * speed);
        physics.setVelocityY(direction.y * speed);
        transform.rotateToVector(direction.toPoint2D());
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public abstract Vec2 getNextMove();

    public void remove() {
        entity.removeFromWorld();
    }

    protected abstract void setAnimChannel();

    protected abstract void handleHeroCollision(Entity hero);

    protected abstract void handleMonsterCollision(Entity monster);
    protected void explode() {
        texture.playAnimationChannel(explodeAnim);
        texture.setOnCycleFinished(() -> {
            remove();
        });

    }
}




