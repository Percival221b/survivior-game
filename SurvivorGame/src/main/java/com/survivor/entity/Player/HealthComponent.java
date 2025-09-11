package com.survivor.entity.Player;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;

import java.util.function.BiConsumer;

public class HealthComponent extends Component {
    private int hp;
    private int maxHp;

    // 回调接口（两个参数：当前血量, 最大血量）
    private BiConsumer<Integer, Integer> onHealthChange;

    public HealthComponent(SpawnData data) {
        Object healthValue = data.hasKey("health") ? data.get("health") : 100;

        if (healthValue != null) {
            this.maxHp = ((Number) healthValue).intValue();
        } else {
            this.maxHp = 100;
        }
        this.hp = maxHp;
    }

    public HealthComponent(int maxHp) {
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    // 注册回调
    public void setOnHealthChange(BiConsumer<Integer, Integer> callback) {
        this.onHealthChange = callback;
    }

    // 通知 UI
    private void notifyHealthChange() {
        if (onHealthChange != null) {
            onHealthChange.accept(hp, maxHp);
        }
    }

    public int getHP() { return hp; }
    public int getMaxHp() { return maxHp; }

    public void setHp(int hp) {
        this.hp = hp;
        notifyHealthChange();
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
        notifyHealthChange();
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            // 这里可以顺便触发死亡逻辑
            FXGL.getNotificationService().pushNotification("玩家死亡！");
        }
        notifyHealthChange();
    }

    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
        notifyHealthChange();
    }

    public void increaseMaxHP(int amount) {
        this.maxHp += amount;
        this.hp += amount;
        notifyHealthChange();
        FXGL.getNotificationService().pushNotification("最大生命值增加 " + amount + " 点！");
    }
}
