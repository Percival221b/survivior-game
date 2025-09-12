package com.survivor.entity;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.TransformComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.SensorCollisionHandler;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.interfaces.Renderable;
import com.survivor.main.EntityType;
import javafx.geometry.Point2D;

public abstract class Projectile extends Component implements Renderable {
    protected  float damage;
    //    protected Entity entity;
    protected PhysicsComponent physics;
    protected TransformComponent transform;
    protected boolean canMove = true;
    protected float speed;
    protected Vec2 direction = new Vec2(0,0);
    protected AnimatedTexture texture;
    protected float hitRadius;
    protected Point2D hitCenter;
    protected enum State {FLY, EXPLODE,DIE}
    protected Point2D offsetPos = new Point2D(0,0);
    boolean isTransparent;
    protected AnimationChannel flyAnim;
    protected AnimationChannel explodeAnim;
    protected State currentState = State.FLY;

    protected Projectile(float speed, float damage,float hitRadius,Point2D hitCenter,Point2D offsetPos,boolean isTransparent) {
        this.speed = speed;
        this.damage = damage;
        this.hitRadius = hitRadius;
        this.hitCenter = hitCenter;
        this.offsetPos = offsetPos;
        this.isTransparent = isTransparent;
    }

    public void onAdded() {

//        entity = getEntity();
        physics = entity.getComponent(PhysicsComponent.class);
        transform = entity.getTransformComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        SensorCollisionHandler handler = new SensorCollisionHandler() {
            @Override
            protected void onCollisionBegin(Entity other) {
//                super.onCollisionBegin(other);
                if(other.getType().equals(EntityType.PLAYER)&&entity.getType().equals(EntityType.PROJECTILEX))
                    handleHeroCollision(other);
                else if(other.getType().equals(EntityType.ENEMY)&&entity.getType().equals(EntityType.PROJECTILE))
                    handleMonsterCollision(other);
            }
        };
        HitBox hitBox = new HitBox(hitCenter, BoundingShape.circle(hitRadius)) ;

        physics.addSensor(hitBox,handler);
        if(!isTransparent)
            setAnimChannel();

    }

    public void move() {
//        System.out.println(direction);
        physics.setVelocityX(direction.x * speed);
        physics.setVelocityY(direction.y * speed);
        transform.rotateToVector(direction.toPoint2D().add(offsetPos));
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    protected abstract void getNextMove();

    public void remove() {
        entity.removeFromWorld();
    }

    protected abstract void setAnimChannel();

    protected abstract void handleHeroCollision(Entity hero);

    protected abstract void handleMonsterCollision(Entity monster);
    protected void explode() {
        if(isTransparent) {
            remove();
            return;
        }
        texture.playAnimationChannel(explodeAnim);
        texture.setOnCycleFinished(() -> {
            remove();
        });

    }

    @Override
    public void onUpdate(double tpf) {
//        direction=new Vec2(1,0);
        getNextMove();
        move();
//        System.out.println(physics.getLinearVelocity().distance(0,0));

    }

}