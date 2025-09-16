package com.survivor.entity.weapon;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.SensorCollisionHandler;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.survivor.entity.Enemy.*;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Player.PlayerMovementComponent;
import com.survivor.entity.Projectile;
import com.survivor.main.EntityType;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class NeutralBlade extends Projectile {
    private float duration;

    public NeutralBlade(float damage, float hitRadius, Point2D hitCenter, Point2D offsetPos, float duration) {
        super(0f, damage, hitRadius, hitCenter, offsetPos, true);
        this.duration = duration;
    }

    @Override
    protected void getNextMove() {
        direction = new Vec2(0, 0);
    }
    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
        transform = entity.getTransformComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        HitBox hitBox = new HitBox(hitCenter.subtract(new Point2D(hitRadius,hitRadius)), BoundingShape.circle(hitRadius));
        physics.addSensor(hitBox, new SensorCollisionHandler() {
            @Override
            protected void onCollisionBegin(Entity other) {
                if (other.isType(EntityType.ENEMY)) {
                    handleMonsterCollision(other);
                }
                if (other.isType(EntityType.PLAYER)) {
                    handleHeroCollision(other);
                }
            }
        });

        setAutoRemove();
    }

    @Override
    protected void setAutoRemove() {
        FXGL.getGameTimer().runOnceAfter(() -> {
            explode();
        }, Duration.seconds(duration));
    }

    @Override
    public void setAnimChannel() {
    }

    @Override
    protected void handleHeroCollision(Entity hero) {
        hero.getComponent(HealthComponent.class).takeDamage(4000);
    }

    @Override
    protected void handleMonsterCollision(Entity monster) {
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
    }
}