package com.survivor.entity.projectiles;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import  com.survivor.entity.interfaces.Renderable;
import javafx.scene.canvas.GraphicsContext;
import com.almasb.fxgl.core.math.Vec2;
import com.survivor.util.*;

public class Fire  extends  Projectile{

    private float damage;
    private Point2D center;
    Entity entity;
    boolean canMove = true;
    Point2D startPos;
    Vec2 direction;
    float speed;

    public Fire(Point2D startPos,Vec2 direction,float speed,float damage,Point2D center) {
        super(startPos,speed,damage);
        this.startPos = startPos;
        this.direction = direction.normalize();
        this.speed = speed;
        this.damage = damage;
        this.center = center;
    }

    public Vec2 getNextMove() {

        return getMoveDirection.getCircularMoveDir(center,startPos,true);

    }
    private void explode() {
        // 确保火球仍然存在
        if (!getEntity().isActive()) return;


            // TODO

    }

}
