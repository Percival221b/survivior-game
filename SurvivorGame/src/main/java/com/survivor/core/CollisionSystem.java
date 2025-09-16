package com.survivor.core;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.minigames.MiniGame;
import com.almasb.fxgl.physics.CollisionHandler;
import com.survivor.entity.Enemy.*;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.main.EntityType;

public class CollisionSystem {

    /**
     * 将所有碰撞处理器注册到 FXGL 物理世界。
     */
    public void registerCollisionHandlers() {


        // 注册玩家和敌人碰撞处理器
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.ENEMY) {
            @Override
            protected void onCollision(Entity hero, Entity monster) {
                handleHeroMonsterCollision(hero, monster);
            }
        });

    }




    private void handleHeroMonsterCollision(Entity hero, Entity monster) {
        //TODO玩家受击
//        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        //hero.getComponent(HealthComponent.class).takeDamage(monster.);
        if(monster.hasComponent(SprintEnemyCompontBat.class)) {
            hero.getComponent(HealthComponent.class).takeDamage(monster.getComponent(SprintEnemyCompontBat.class).getAttack());
        }else if (monster.hasComponent(SplitEnemyComponent.class)){
            hero.getComponent(HealthComponent.class).takeDamage(monster.getComponent(SplitEnemyComponent.class).getAttack());
        }else if (monster.hasComponent(zhiEnemyComponent.class)){
            hero.getComponent(HealthComponent.class).takeDamage(monster.getComponent(zhiEnemyComponent.class).getAttack());
        }else if(monster.hasComponent(SmallSplitEnemyComponent.class)) {
            hero.getComponent(HealthComponent.class).takeDamage(monster.getComponent(SmallSplitEnemyComponent.class).getAttack());
        }else if (monster.hasComponent(RangedEnemyComponent.class)) {
            hero.getComponent(HealthComponent.class).takeDamage(monster.getComponent(RangedEnemyComponent.class).getAttack());
        }else{

        }
    }
}