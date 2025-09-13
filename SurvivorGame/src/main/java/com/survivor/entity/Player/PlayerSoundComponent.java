package com.survivor.entity.Player;

import com.almasb.fxgl.audio.Sound;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;


public class PlayerSoundComponent extends Component {

    private Sound attackSfx;
    private Sound dashSfx;
    private Sound dieSfx;

    @Override
    public void onAdded() {
        // 从 assets/sounds 目录加载音效文件
        attackSfx = FXGL.getAssetLoader().loadSound("sword-slash-315218.mp3");
        dashSfx = FXGL.getAssetLoader().loadSound("split-[AudioTrimmer.com].mp3");
        dieSfx = FXGL.getAssetLoader().loadSound("die_knight.wav");
    }

    /**
     * 播放攻击音效
     */
    public void playAttack() {
        if (attackSfx != null) {
            FXGL.getAudioPlayer().playSound(attackSfx);
        }
    }
    public void playDash() {
        if (dashSfx != null) {
            FXGL.getAudioPlayer().playSound(dashSfx);
        }
    }
    public void playDie() {
        if (dieSfx != null) {
            FXGL.getAudioPlayer().playSound(dieSfx);
        }
    }
}