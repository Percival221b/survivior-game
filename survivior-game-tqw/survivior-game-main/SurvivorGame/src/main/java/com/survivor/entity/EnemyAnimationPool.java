package com.survivor.entity;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;

/**
 * 所有小怪动画的“仓库”
 * 统一加载、统一获取，避免到处硬编码路径
 */
public final class EnemyAnimationPool {
    /* -----------NORMAL系列----------- */
    public static final class Normal{
        public static final AnimationChannel IDLE=new AnimationChannel(
                FXGL.image("monster/normal/Orc_Idle.png"), 6, 100, 100, Duration.seconds(1), 0, 5);
        public static final AnimationChannel WALK = new AnimationChannel(
                FXGL.image("monster/normal/Orc_Walk.png"), 8, 100, 100, Duration.seconds(0.8), 0, 7);
        public static final AnimationChannel ATTACK = new AnimationChannel(
                FXGL.image("monster/normal/Orc_Attack.png"), 6, 100, 100, Duration.seconds(0.8), 0, 5);
        public static final AnimationChannel DEATH = new AnimationChannel(
                FXGL.image("monster/normal/Orc_Death.png"), 4, 100, 100, Duration.seconds(1), 0, 3);
        public static final AnimationChannel HIT = new AnimationChannel(
                FXGL.image("monster/normal/Orc_Hit.png"),5,100,100,Duration.seconds(1),0,4);
    }

    /* ---------- TANK 系列 ---------- */
    public static final class Tank {
        public static final AnimationChannel IDLE = new AnimationChannel(
                FXGL.image("monster/Tank/Armored Orc_Idle.png"), 6, 100, 100, Duration.seconds(1), 0, 5);
        public static final AnimationChannel WALK = new AnimationChannel(
                FXGL.image("monster/Tank/Armored Orc_Walk.png"), 8, 100, 100, Duration.seconds(0.8), 0, 7);
        public static final AnimationChannel ATTACK = new AnimationChannel(
                FXGL.image("monster/Tank/Armored Orc_Attack.png"), 7, 100, 100, Duration.seconds(0.8), 0, 6);
        public static final AnimationChannel DEATH = new AnimationChannel(
                FXGL.image("monster/Tank/Armored Orc_Death.png"), 4, 100, 100, Duration.seconds(1), 0, 3);
        public static final AnimationChannel HIT = new AnimationChannel(
                FXGL.image("monster/Tank/Armored Orc_Hit.png"), 5, 100, 100, Duration.seconds(0.5), 0, 4);
    }

    /* ---------- SPEED 系列 ---------- */
    public static final class Speed {
        public static final AnimationChannel IDLE = new AnimationChannel(
                FXGL.image("monster/Speed/Slime-Idle.png"), 6, 100, 100, Duration.seconds(1), 0, 5);
        public static final AnimationChannel WALK = new AnimationChannel(
                FXGL.image("monster/Speed/Slime-Walk.png"), 6, 100, 100, Duration.seconds(0.8), 0, 5);
        public static final AnimationChannel ATTACK = new AnimationChannel(
                FXGL.image("monster/Speed/Slime-Attack.png"), 6, 100, 100, Duration.seconds(0.8), 0, 5);
        public static final AnimationChannel DEATH = new AnimationChannel(
                FXGL.image("monster/Speed/Slime-Death.png"), 4, 100, 100, Duration.seconds(1), 0, 3);
        public static final AnimationChannel HIT = new AnimationChannel(
                FXGL.image("monster/Speed/Slime-Hurt.png"), 4, 100, 100, Duration.seconds(0.5), 0, 3);
    }

    /* ---------- WIZARD 系列 ---------- */
    public static final class Wizard {
        public static final AnimationChannel IDLE = new AnimationChannel(
                FXGL.image("monster/ranged/Wizard-Idle.png"), 6, 100, 100, Duration.seconds(1), 0, 5);
        public static final AnimationChannel WALK = new AnimationChannel(
                FXGL.image("monster/ranged/Wizard-Walk.png"), 8, 100, 100, Duration.seconds(0.8), 0, 7);
        public static final AnimationChannel ATTACK = new AnimationChannel(
                FXGL.image("monster/Wizard/Wizard-Attack.png"), 6, 100, 100, Duration.seconds(0.8), 0, 5);
        public static final AnimationChannel DEATH = new AnimationChannel(
                FXGL.image("monster/Wizard/Wizard-DEATH.png"), 4, 100, 100, Duration.seconds(1), 0, 3);
        public static final AnimationChannel HIT = new AnimationChannel(
                FXGL.image("monster/ranged/Wizard-Hurt.png"), 4, 100, 100, Duration.seconds(0.5), 0, 3);
    }

    /* ---------- 工具方法 ---------- */
    public static EnemyAnimationSet get(EnemyFactory.EnemyType type) {
        switch (type) {
            case NORMAL:
                return new EnemyAnimationSet(Normal.IDLE, Normal.WALK, Normal.ATTACK, Normal.DEATH, Normal.HIT);
            case TANK:
                return new EnemyAnimationSet(Tank.IDLE, Tank.WALK, Tank.ATTACK, Tank.DEATH, Tank.HIT);
            case SPEED:
                return new EnemyAnimationSet(Speed.IDLE, Speed.WALK, Speed.ATTACK, Speed.DEATH, Speed.HIT);
            case WIZARD:
                return new EnemyAnimationSet(Wizard.IDLE, Wizard.WALK, Wizard.ATTACK, Wizard.DEATH, Wizard.HIT);
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    /* ---------- 简单 DTO ---------- */
    public static final class EnemyAnimationSet {
        public final AnimationChannel idle;
        public final AnimationChannel walk;
        public final AnimationChannel attack;
        public final AnimationChannel death;
        public final AnimationChannel hit;

        public EnemyAnimationSet(AnimationChannel idle, AnimationChannel walk,
                                 AnimationChannel attack, AnimationChannel death,
                                 AnimationChannel hit) {
            this.idle = idle;
            this.walk = walk;
            this.attack = attack;
            this.death = death;
            this.hit = hit;
        }
    }
}