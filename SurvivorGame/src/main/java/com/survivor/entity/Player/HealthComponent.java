package com.survivor.entity.Player;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.survivor.main.GameApp;

import java.util.function.BiConsumer;

public class HealthComponent extends Component {
    private int hp;
    private int maxHp;
    private int regenHp = 0;
    private int shield = 0;

    private BiConsumer<Integer, Integer> onHealthChange;
    private double regenTimer = 0;

    @Override
    public void onUpdate(double tpf) {
        if(!FXGL.<GameApp>getAppCast().getSceneManager().getGameLoop().isRunning()){
            return;
        }
        regenTimer += tpf;
        if (regenTimer >= 5) {   // 每 5 秒触发一次
            regenTimer = 0;
            if (regenHp > 0 && hp > 0 && hp + regenHp <= maxHp) {
                heal(regenHp);
                FXGL.getNotificationService().pushNotification("恢复生命 +" + regenHp);
            }
            else if (regenHp > 0 && hp + regenHp <= maxHp) {
                setHp(maxHp);
            }
        }
    }

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
    public int getRegenHp() { return regenHp; }
    public int getShield() { return shield; }

    public void setHp(int hp) {
        this.hp = hp;
        notifyHealthChange();
    }
    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
        notifyHealthChange();
    }

    public void takeDamage(int dmg) {
        if (shield > 0) {
            shield--;
            notifyHealthChange();
            return;
        }
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            FXGL.getNotificationService().pushNotification("玩家死亡！");
        }
        notifyHealthChange();
    }

    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
        notifyHealthChange();
    }

    public void increaseMaxHP(int amount) {
        // 增加最大生命值
        this.maxHp += amount;
        this.hp += amount;
        notifyHealthChange();
        //FXGL.getNotificationService().pushNotification("最大生命值增加 " + amount + " 点！");
    }

    public void increaseRegenHP(int amount) {
        regenHp += amount;
    }

    public void increaseShield(int amount) {
        shield += amount;
    }
}

