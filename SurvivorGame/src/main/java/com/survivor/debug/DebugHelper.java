package com.survivor.debug;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.main.EntityType;

public class DebugHelper {

    /** 打印玩家的当前血量 */
    public static void printPlayerHealth() {
        Entity player = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER).get(0);
        HealthComponent health = player.getComponent(HealthComponent.class);

        System.out.println("玩家当前血量: " + health.getHP() + " / " + health.getMaxHp());
    }
}
