package com.survivor.entity;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;

/**
 * 小怪工厂类
 * 负责生成不同类型的小怪实体，并绑定基础属性和动画组件
 */
public class EnemyFactory {

    public enum EnemyType {
        NORMAL,
        TANK,      // 坦克小怪 - 高血量，低速度
        SPEED,     // 急速小怪 - 低血量，高速度
        WIZARD     // 远程小怪 - 中等血量，远程攻击
    }

    /**
     * 创建小怪实体
     * @param type 小怪类型
     * @param spawnPos 生成位置
     * @return 小怪实体
     */
    public static Entity spawnEnemy(EnemyType type, Point2D spawnPos) {
        // 先创建组件
        EnemyComponent enemyComponent = new EnemyComponent();

        switch (type) {
            case NORMAL:
                enemyComponent.setHealth(100);
                enemyComponent.setDamage(10);
                enemyComponent.setSpeed(1.5);
                enemyComponent.setAttackRange(40);
                enemyComponent.setExpValue(15);
                break;

            case TANK:
                enemyComponent.setHealth(200);
                enemyComponent.setDamage(30);
                enemyComponent.setSpeed(0.8);
                enemyComponent.setAttackRange(40);
                enemyComponent.setExpValue(15);
                break;

            case SPEED:
                enemyComponent.setHealth(60);
                enemyComponent.setDamage(10);
                enemyComponent.setSpeed(2.5);
                enemyComponent.setAttackRange(40);
                enemyComponent.setExpValue(8);
                break;

            case WIZARD:
                enemyComponent.setHealth(100);
                enemyComponent.setDamage(15);
                enemyComponent.setSpeed(1.2);
                enemyComponent.setAttackRange(200);
                enemyComponent.setExpValue(12);
                break;
        }

        // 再创建实体，并绑定组件
        Entity enemy = FXGL.entityBuilder()
                .at(spawnPos)
                .bbox(new HitBox(BoundingShape.box(40, 40)))
                .with(enemyComponent)
                .buildAndAttach();

        return enemy;
    }
}

