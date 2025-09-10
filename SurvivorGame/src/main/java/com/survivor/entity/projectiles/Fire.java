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

public class Fire  extends Component implements Renderable{

    private float damage;
    private Point2D center;
    private float radius;
    private float angularSpeed;
    private float currentAngle = 0;
    Entity entity;
    boolean canMove = true;
    Point2D startPos;
    Vec2 direction;
    float speed;
    public Fire(Point2D startPos,Vec2 direction,float speed,float damage) {
        this.startPos = startPos;
        this.direction = direction.normalize();
        this.speed = speed;
        this.damage = damage;

    }
    @Override
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
    public Vec2 getNextMove(double tpf) {
        // 计算下一帧的角度
        currentAngle += angularSpeed * tpf;

        // 使用三角函数计算新的位置，并与圆心位置相减
        double newX = center.getX() + radius * Math.cos(currentAngle);
        double newY = center.getY() + radius * Math.sin(currentAngle);

        // 计算从当前位置到下一个位置的方向向量
        // Vec2 的构造函数需要 float 类型，所以需要转换
        Vec2 nextPosition = new Vec2((float)newX, (float)newY);
        Vec2 currentPosition = new Vec2((float)entity.getX(), (float)entity.getY());

        // 返回归一化后的方向向量
        return nextPosition.sub(currentPosition).normalize();
    }
    private void explode() {
        // 确保火球仍然存在
        if (!getEntity().isActive()) return;


            // TODO

    }
    public void remove() {
        //TODO：移除的时机
        entity.removeFromWorld();
    }

}
