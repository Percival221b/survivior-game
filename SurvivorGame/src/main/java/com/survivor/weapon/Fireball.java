package com.survivor.weapon;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.survivor.weapon.interfaces.Weapon;
import com.almasb.fxgl.core.math.Vec2;
import javafx.geometry.Point2D;

public class Fireball implements Weapon{
    Entity hero;
    private float damage;
    public Fireball(Entity hero)
    {
        this.hero = hero;
    }
    public float getDamage()
    {
        return this.damage;
    }
    public void use()
    {
        //TODO:方向
        FXGL.getGameWorld().spawn("fire",new SpawnData(hero.getPosition()).put("startPosition",hero.getPosition()).put("direction",new Vec2(1f,0f)).put("damage",damage));
        //TODO:注册entity
    }
}
