package com.survivor.core;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class AudioManager {
    private MediaPlayer mediaPlayer;
    private boolean musicEnabled = true;

    public void playMusic(String sourceUrl) {
        stopMusic();
        if (!musicEnabled) return;

        Media media = new Media(sourceUrl); // 直接用 URL 字符串
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.play();

        // 可选：增加错误日志
        media.setOnError(() -> System.out.println("Media error: " + media.getError()));
        mediaPlayer.setOnError(() -> System.out.println("MediaPlayer error: " + mediaPlayer.getError()));
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    public void toggleMusic(String sourceUrl) {
        musicEnabled = !musicEnabled;
        if (musicEnabled) playMusic(sourceUrl);
        else stopMusic();
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }
}
