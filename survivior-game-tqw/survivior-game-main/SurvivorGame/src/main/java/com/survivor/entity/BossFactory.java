package com.survivor.entity;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BossFactory implements EntityFactory {

    @Spawns("boss")
    public Entity newBoss(SpawnData data) {
        return entityBuilder(data)
                .with(new BossComponent())
                .build();
    }
}
