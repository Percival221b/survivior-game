package com.survivor.entity;

import com.almasb.fxgl.entity.component.Component;

public class ExperienceOrb extends Component {

    private  int xpAmount;

    public  ExperienceOrb (int xpAmount) {
        this.xpAmount = xpAmount;
    }

    public int getXpAmount() {
        return xpAmount;
    }
}
