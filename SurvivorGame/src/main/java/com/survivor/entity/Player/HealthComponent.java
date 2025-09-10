package com.survivor.entity.Player;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;

// HealthComponent.java
import com.almasb.fxgl.entity.component.Component;
public class HealthComponent extends Component {
    private int hp;
    private int maxHp;



    public HealthComponent(SpawnData data) {
        Object healthValue = data.hasKey("health") ? data.get("health") : 100;

        // 检查值是否为 null，以防 SpawnData 中没有 "health" 键
        if (healthValue != null) {
            // 将值强制转换为 Number 类型，然后调用 intValue()
            this.maxHp = ((Number) healthValue).intValue();
        } else {
            // 如果 SpawnData 中未提供生命值，则使用默认值
            this.maxHp = 100;
        }

        this.hp = maxHp;

    }

    public HealthComponent(int maxHp) {
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    public int getHP() { return hp; }
    public int getMaxHp() { return maxHp; }

    public void setHp(int hp) {
        this.hp = hp;
    }
    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;

        }
    }

    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }
    public void increaseMaxHP(int amount) {
        // 增加最大生命值
        this.maxHp += amount;

        // 同时增加当前生命值，给玩家即时反馈
        this.hp += amount;

        FXGL.getNotificationService().pushNotification("最大生命值增加 " + amount + " 点！");
    }
}

