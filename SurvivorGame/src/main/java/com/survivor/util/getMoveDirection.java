package com.survivor.util;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Point2D;

public class getMoveDirection {
    public enum MoveType {
        LINEAR,
        CIRCULAR_ORBIT,
        CIRCULAR_AROUND_ENTITY,
        RANDOM_WALK,
        TOWARDS_TARGET
    }

    public static Vec2 getLinearMoveDir(Vec2 initialDirection) {

        return initialDirection.normalize();
    }

    public static Vec2 getLinearMoveDir(Entity start, Entity target) {
        return new Vec2(target.getPosition().subtract(start.getPosition())).normalize();
    }

    public static Vec2 getLinearMoveDir(Point2D startPos, Point2D targetPos) {
        return new Vec2(targetPos.subtract(startPos)).normalize();
    }


    public static Vec2 getCircularMoveDir(Point2D center, Point2D start, boolean isClockwise) {
        Point2D radiusVector = start.subtract(center);

        // 如果半径向量为零（即在圆心），返回零向量以避免错误
        if (radiusVector.magnitude() == 0) {
            return new Vec2(0, 0);
        }

        // 2. 将半径向量转换为 Vec2 类型，以便进行向量运算
        Vec2 direction = new Vec2(radiusVector);

        // 3. 将方向向量旋转90度以获得切线方向
        if (isClockwise) {
            // 顺时针旋转90度 (x, y) -> (y, -x)
            direction.set(direction.y, -direction.x);
        } else {
            // 逆时针旋转90度 (x, y) -> (-y, x)
            direction.set(-direction.y, direction.x);
        }

        // 4. 返回归一化后的切线向量
//        System.out.println(direction.normalize()
//        System.out.println(direction.normalize());
        return direction.normalize();
    }

    public static Vec2 getCentripetalMoveDir(Point2D center, Point2D start, boolean isClockwise, boolean isCentripetal, float centripetalRatio) {

        Point2D radiusVector = start.subtract(center);
        Vec2 circularVector;
        Vec2 finalVector;
        if (radiusVector.magnitude() == 0) {
            circularVector = new Vec2(0, 0);
        } else {
            Vec2 direction = new Vec2(radiusVector);

            if (isClockwise) {

                direction.set(direction.y, -direction.x);
            } else {

                direction.set(-direction.y, direction.x);
            }


            circularVector = direction.normalize();
        }
        finalVector = isCentripetal ? circularVector.mul(-centripetalRatio).sub(new Vec2(radiusVector).mul(1 - centripetalRatio)) : circularVector.mul(centripetalRatio).add(new Vec2(radiusVector).mul(1 - centripetalRatio));
        return finalVector;
    }
}