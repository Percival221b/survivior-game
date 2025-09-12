package com.survivor.entity.Player;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.dsl.FXGL;

import java.util.function.BiConsumer;

public class HealthComponent extends Component {
    private int hp;         // 当前血量
    private int maxHp;      // 最大血量
    private BiConsumer<Integer, Integer> onHealthChange; // 血量变化回调

    // 构造器
    public HealthComponent(int maxHp) {
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    // 注册回调
    public void setOnHealthChange(BiConsumer<Integer, Integer> callback) {
        this.onHealthChange = callback;
    }

    // 通知 UI 更新血量
    private void notifyHealthChange() {
        if (onHealthChange != null) {
            onHealthChange.accept(hp, maxHp);  // 通知 UI 更新血量
        }
    }

    // 获取当前血量
    public int getHP() {
        return hp;
    }

    // 获取最大血量
    public int getMaxHp() {
        return maxHp;
    }

    // 设置当前血量
    public void setHp(int hp) {
        this.hp = Math.max(0, hp);  // 血量不能小于 0
        notifyHealthChange();       // 通知 UI 更新
    }

    // 设置最大血量
    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
        notifyHealthChange();       // 通知 UI 更新
    }

    // 扣除血量
    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            FXGL.getNotificationService().pushNotification("玩家死亡！");
        }
        notifyHealthChange();       // 通知 UI 更新
    }

    // 治疗
    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount); // 确保血量不超过最大血量
        notifyHealthChange();       // 通知 UI 更新
    }

    // 增加最大血量
    public void increaseMaxHP(int amount) {
        this.maxHp += amount;
        this.hp += amount;           // 增加最大血量时，当前血量也增加
        notifyHealthChange();       // 通知 UI 更新
    }
}
