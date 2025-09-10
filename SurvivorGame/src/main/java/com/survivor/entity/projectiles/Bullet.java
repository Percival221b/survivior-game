package com.survivor.entity.projectiles;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import  com.survivor.entity.interfaces.Renderable;
import javafx.scene.canvas.GraphicsContext;

public class Bullet  extends Component implements Renderable{

    Entity entity;
    boolean canMove = true;
    Point2D startPos;
    Vec2 direction;
    float speed;
    public Bullet(Point2D startPos, Vec2 direction, float speed,float damage) {
        this.startPos = startPos;
        this.direction = direction.normalize();
        this.speed = speed;

    }
    @Override
    public void onAdded() {
        entity = getEntity();

    }

    public void move(double tpf) {
        entity.translateX(direction.x * speed * tpf);
        entity.translateY(direction.y * speed * tpf);

    }
    public void setCanMove(boolean canMove)
    {
        this.canMove = canMove;
    }


}
