package com.survivor.entity;
import com.almasb.fxgl.entity.component.Component;
import com.survivor.entity.Player.HealthComponent;

public class HealthPotionComponent extends Component {

    public HealthPotionComponent() {
    }
    /**
     * 当玩家拾取血瓶时调用
     */
    public void applyTo(HealthComponent health) {
        health.setHp(Math.min(health.getHP() +(int) (health.getMaxHp() * 0.1), health.getMaxHp()));
    }
}


