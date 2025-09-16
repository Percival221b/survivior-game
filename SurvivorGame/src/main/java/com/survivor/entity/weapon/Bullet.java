package com.survivor.entity.weapon;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Enemy.*;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.util.getMoveDirection;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import com.survivor.entity.Projectile;

public class Bullet extends Projectile{
    private float duration;
    private Point2D targetPos;
    public Bullet(float speed, float damage,float hitRadius,Point2D hitCenter,Point2D offsetPos,float duration,Point2D targetPos) {
        super(speed, damage,hitRadius,hitCenter,offsetPos,false);
        this.duration = duration;
        this.targetPos = targetPos;
    }
    @Override
    protected void getNextMove() {

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
        direction = new Vec2(targetPos.subtract(entity.getPosition()));
    }

    @Override
    protected void setAutoRemove()
    {
        FXGL.getGameTimer().runOnceAfter(() -> {
            if(currentState ==State.FLY)
                explode();
        }, Duration.seconds(duration));
    }
    @Override
    public void setAnimChannel() {
        flyAnim = new AnimationChannel(
                FXGL.image("bat/Archer-weapon.png"),
                1,// 帧数
                32, 32, // 单帧大小
                Duration.seconds(0), // 一轮动画时长
                0, 0
        );
        explodeAnim = new AnimationChannel(
                FXGL.image("bat/Archer-weapon.png"),
                8,   // 帧数
                32, 32, // 单帧大小
                Duration.seconds(0), // 一轮动画时长
                0, 0
        );
        texture = new AnimatedTexture(flyAnim);
        texture.setTranslateX(-20);
        texture.setTranslateY(-2.5);
        texture.loopAnimationChannel(flyAnim);
        entity.getViewComponent().addChild(texture);
    }

    @Override
    protected void handleHeroCollision(Entity hero) {

            hero.getComponent(HealthComponent.class).takeDamage((int)500);
            explode();
    }

    @Override
    protected void handleMonsterCollision(Entity monster) {
        if(monster.hasComponent(SprintEnemyCompontBat.class)) {
            monster.getComponent(SprintEnemyCompontBat.class).takeDamage(4000);
        }else if (monster.hasComponent(SplitEnemyComponent.class)){
            monster.getComponent(SplitEnemyComponent.class).takeDamage(4000);
        }else if (monster.hasComponent(zhiEnemyComponent.class)){
            monster.getComponent(zhiEnemyComponent.class).takeDamage(4000);
        }else if(monster.hasComponent(SmallSplitEnemyComponent.class)) {
            monster.getComponent(SmallSplitEnemyComponent.class).takeDamage(4000);
        }else if (monster.hasComponent(RangedEnemyComponent.class)) {
            monster.getComponent(RangedEnemyComponent.class).takeDamage(4000);
        }else{

        }
        explode();
    }

}