package com.survivor.core;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.survivor.util.EntityType;

public class CollisionSystem {

    /**
     * 将所有碰撞处理器注册到 FXGL 物理世界。
     */
    //TODO攻击间隔与连续攻击
    public void registerCollisionHandlers() {

        // 注册子弹和敌人碰撞处理器
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PROJECTILE, EntityType.MONSTER) {
            @Override
            protected void onCollisionBegin(Entity projectile, Entity monster) {
                handleProjectileMonsterCollision(projectile, monster);
            }
        });

        // 注册玩家和敌人碰撞处理器
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.HERO, EntityType.MONSTER) {
            @Override
            protected void onCollisionBegin(Entity hero, Entity monster) {
                handleHeroMonsterCollision(hero, monster);
            }
        });

//        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PROJECTILESENSOR,EntityType.HERO) {
//            @Override
//            protected void onCollisionBegin(Entity projectile, Entity hero) {
//                if (projectile.getType().equals(EntityType.PROJECTILEX))
//
//            }
//        });
        // 如果有更多碰撞类型，可以在这里继续添加
    }

    /**
     * 处理子弹击中敌人的逻辑。
     * @param projectile 子弹实体
     * @param monster 敌人实体
     */
    private void handleProjectileMonsterCollision(Entity projectile, Entity monster) {
        // 移除子弹和敌人
        projectile.removeFromWorld();
        monster.removeFromWorld();

        // 增加分数
//        FXGL.inc("score", +100);

        // 播放音效或特效
        // FXGL.play("hit_sound.wav");
    }

    /**
     * 处理玩家被敌人击中的逻辑。
     * @param hero 玩家实体
     * @param monster 敌人实体
     */
    private void handleHeroMonsterCollision(Entity hero, Entity monster) {
        // 游戏结束
//        FXGL.getGameController().exit();
    }
//    private void handleProjectileHeroCollision(Entity projectile,Entity hero){
//
//    }
}