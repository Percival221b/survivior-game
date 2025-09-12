package com.survivor.entity;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Point2D;

public class MapWall {
    private int width, height;
    private Point2D startPos;
    private Entity wallEntity;
    private boolean isFinished = false;

    public MapWall(int width, int height, Point2D startPos) {
        this.width = width;
        this.height = height;
        this.startPos = startPos;
    }

    public boolean spawn() {
        if (isFinished) return false;

        wallEntity = FXGL.spawn("wall", new SpawnData(startPos)
                .put("width", width)
                .put("height", height));

        isFinished = true;
        return true;
    }

    public void remove() {
        if (wallEntity != null) {
            wallEntity.removeFromWorld();
        }
    }
}
