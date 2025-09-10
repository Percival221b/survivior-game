package com.survivor.entity;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;

public class XPComponent extends Component {

    private int level = 1;
    private int currentXP = 0;
    private int xpToNextLevel = 10;

    /**
     * 角色获得经验值时调用。
     * @param amount 获得的经验值
     */
    public void gainXP(int amount) {
        currentXP += amount;
        FXGL.getNotificationService().pushNotification("XP");

        checkLevelUp();
    }

    /**
     * 检查当前经验值是否达到升级要求，并处理升级。
     */
    private void checkLevelUp() {
        if (currentXP >= xpToNextLevel) {
            // 升级玩家
            level++;
            currentXP -= xpToNextLevel; // 减去所需的经验值

            // 增加下一级所需的经验值（例如，指数增长）
            xpToNextLevel = (int) (100 * Math.pow(1.3, level ));

            entity.getComponentOptional(PlayerMovementComponent.class).ifPresent(move -> {
                double oldSpeed = move.getSpeed();
                move.setSpeed(oldSpeed * 1.25);  // 移动速度 +5%
                FXGL.getNotificationService().pushNotification("移速提升到 " + (int) move.getSpeed());
            });

            entity.getComponentOptional(HealthComponent.class).ifPresent(hp -> {
                // 假设 HealthComponent 里有 setMaxHP / getMaxHP / getHP / setHP 方法
                int dMax =  10 * (level^2);;
                hp.increaseMaxHP(dMax);
                // 回一点血
                FXGL.getNotificationService().pushNotification("最大生命加成");
            });

            FXGL.getNotificationService().pushNotification("等级提升");

            // 如果玩家一次性获得足够升多级的经验，则再次检查
            checkLevelUp();
        }
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentXP() {
        return currentXP;
    }

    public int getXpToNextLevel() {
        return xpToNextLevel;
    }
}