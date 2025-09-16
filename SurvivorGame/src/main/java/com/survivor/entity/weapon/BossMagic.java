package com.survivor.entity.weapon;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.SensorCollisionHandler;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Projectile;
import com.survivor.main.EntityType;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class BossMagic extends Projectile {
    public BossMagic(float damage,float hitRadius,Point2D hitCenter,Point2D offsetPos) {
        super(0, damage,hitRadius,hitCenter,offsetPos,false);
    }
    @Override
    protected void getNextMove() {

        //        direction =  getMoveDirection.getCircularMoveDir(center,entity.getPosition(), true);
//        direction = getMoveDirection.getCentripetalMoveDir(center,entity.getPosition(),true,true,0.9999f);
        //        System.out.println(entity.getPosition().distance(center));

        //        System.out.println(direction);
//        direction = getMoveDirection.getLinearMoveDir(entity.getPosition(),targetPos);
    }
    @Override
    public void onAdded()
    {
        super.onAdded();
        //        ViewComponent viewComponent = player.getComponent(ViewComponent.class);

        // 将视图向左偏移其宽度的一半，向上偏移其高度的一半
        // 这样视图的中心就与实体的 (x, y) 坐标对齐了
        //        view.setTranslateX(-view.getBoundsInLocal().getWidth() / 2);
        //        view.setTranslateY(-view.getBoundsInLocal().getHeight() / 2);


        transform = entity.getTransformComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        HitBox hitBox = new HitBox(hitCenter.subtract(new Point2D(hitRadius,hitRadius)), BoundingShape.circle(hitRadius));
        physics.addSensor(hitBox, new SensorCollisionHandler() {
            @Override
            protected void onCollisionBegin(Entity other) {
                if (other.isType(EntityType.ENEMY)&&entity.isType(EntityType.PROJECTILE)) {
                    handleMonsterCollision(other);
                }
                if (other.isType(EntityType.PLAYER)&&entity.isType(EntityType.PROJECTILEENEMY)) {
                    handleHeroCollision(other);
                }
            }
        });


        setAnimChannel();
    }


    @Override
    public void setAnimChannel() {

        explodeAnim = new AnimationChannel(
                FXGL.image("GreenLight.png"),
                5,   // 帧数
                100, 100, // 单帧大小
                Duration.seconds(0.8f), // 一轮动画时长
                0, 4
        );
        flyAnim = explodeAnim;
        texture = new AnimatedTexture(explodeAnim);
        texture.setTranslateX(-50f);
        texture.setTranslateY(-50f);
        entity.getViewComponent().addChild(texture);
        explode();

    }

    @Override
    protected void handleHeroCollision(Entity hero) {
        hero.getComponent(HealthComponent.class).takeDamage((int)512);
    }

    @Override
    protected void handleMonsterCollision(Entity monster) {

    }

    @Override
    protected void setAutoRemove() {

    }
}