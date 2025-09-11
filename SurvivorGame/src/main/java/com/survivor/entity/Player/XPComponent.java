package com.survivor.entity.Player;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Player.PlayerMovementComponent;
import java.util.function.BiConsumer;
public class XPComponent extends Component {

    private int level = 1;
    private int currentXP = 0;
    private int xpToNextLevel = 10;

    // 经验变化回调
    private BiConsumer<Integer, Integer> onXPChange;

    /**
     * 设置经验变化的回调。
     */
    public void setOnXPChange(BiConsumer<Integer, Integer> callback) {
        this.onXPChange = callback;
    }

    /**
     * 角色获得经验值时调用。
     * @param amount 获得的经验值
     */
    public void gainXP(int amount) {
        currentXP += amount;

        // 每次经验变化后，调用回调，通知更新
        if (onXPChange != null) {
            onXPChange.accept(currentXP, xpToNextLevel);  // 传递当前经验和下一级经验
        }

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
            xpToNextLevel = (int) (100 * Math.pow(1.3, level));
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