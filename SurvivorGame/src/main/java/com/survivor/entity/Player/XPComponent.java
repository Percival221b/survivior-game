package com.survivor.entity.Player;
import com.almasb.fxgl.entity.component.Component;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class XPComponent extends Component {
    private int level = 1;           // 玩家等级
    private int currentXP = 0;       // 当前经验
    private int xpToNextLevel = 140; // 升级所需经验

    private BiConsumer<Integer, Integer> onXPChange;  // 经验变化回调
    private Consumer<Integer> onLevelUp;               // 升级回调

    // 设置经验变化回调
    public void setOnXPChange(BiConsumer<Integer, Integer> callback) {
        this.onXPChange = callback;
    }

    // 设置升级回调
    public void setOnLevelUp(Consumer<Integer> callback) {
        System.out.println("Setting onLevelUp callback");
        this.onLevelUp = callback;
    }

    // 获取经验值
    public void gainXP(int amount) {
        currentXP += amount;
        System.out.println("Current XP: " + currentXP + ", Required XP for next level: " + xpToNextLevel);

        // 检查升级
        checkLevelUp();

        if (onXPChange != null) {
            onXPChange.accept(currentXP, xpToNextLevel);  // 通知 UI 更新
        }
    }

    // 检查升级条件
    private void checkLevelUp() {
        System.out.println("Checking level up: currentXP = " + currentXP + ", xpToNextLevel = " + xpToNextLevel);
        while (currentXP >= xpToNextLevel) {
            level++;
            currentXP -= xpToNextLevel;
            xpToNextLevel = (int) (100 * Math.pow(1.3, level));  // 升级经验呈指数增长

            System.out.println("Level up triggered. Current Level: " + level);

            if (onLevelUp != null) {
                System.out.println("Calling onLevelUp callback");
                onLevelUp.accept(level);  // 通知 UI 更新等级
            }
        }
    }

    // 获取当前等级
    public int getLevel() {
        return level;
    }

    // 获取当前经验
    public int getCurrentXP() {
        return currentXP;
    }

    // 获取下一级所需经验
    public int getXpToNextLevel() {
        return xpToNextLevel;
    }

    // 重置经验与等级
    public void reset() {
        level = 1;
        currentXP = 0;
        xpToNextLevel = 140;

        if (onXPChange != null) {
            onXPChange.accept(currentXP, xpToNextLevel);  // 通知 UI 更新
        }
    }
}
