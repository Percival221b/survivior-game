package com.survivor.util;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Point2D;

public class MapWall {
    private int w,h;
    private Point2D startPos;
    Entity wallEntity;
    boolean isFinished = false;
    public MapWall(int w, int h, Point2D startPos)
    {
        this.w = w;
        this.h = h;
        this.startPos = startPos;
    }
    public boolean spawn()
    {
        if(isFinished)return false;
         wallEntity = FXGL.spawn("wall", new SpawnData( startPos)
                .put("w",w)
                .put("h",h)
                .put("startPos",startPos));
         isFinished = true;
         return isFinished;
    }
    public void remove()
    {
        wallEntity.removeFromWorld();
    }
}
