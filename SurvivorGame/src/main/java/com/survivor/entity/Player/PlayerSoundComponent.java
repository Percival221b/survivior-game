package com.survivor.entity.Player;

import com.almasb.fxgl.audio.Sound;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;


public class PlayerSoundComponent extends Component {

    private Sound attackSfx;

    @Override
    public void onAdded() {
        // 从 assets/sounds 目录加载音效文件
        attackSfx = FXGL.getAssetLoader().loadSound("sword-clashhit-393837.mp3");
    }

    /**
     * 播放攻击音效
     */
    public void playAttack() {
        if (attackSfx != null) {
            FXGL.getAudioPlayer().playSound(attackSfx);
        }
    }
}