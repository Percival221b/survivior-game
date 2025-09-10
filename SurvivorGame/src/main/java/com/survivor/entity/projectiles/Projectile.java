package com.survivor.entity.projectiles;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.survivor.entity.interfaces.Renderable;
import javafx.geometry.Point2D;

public abstract class Projectile extends Component implements Renderable{
    private float damage;
    Entity entity;
    boolean canMove = true;
    Point2D startPos;
    float speed;
    Vec2 direction;
    protected Projectile(Point2D startPos, float speed, float damage) {
        this.startPos = startPos;
        this.speed = speed;
        this.damage = damage;
    }
    public void onAdded()
    {
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
    public abstract Vec2 getNextMove();
    public void remove() {
        //TODO：移除的时机
        entity.removeFromWorld();
    }
}
