package com.survivor.entity.Enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.pathfinding.astar.AStarCell;
import com.almasb.fxgl.pathfinding.astar.AStarGrid;
import com.almasb.fxgl.pathfinding.astar.AStarPathfinder;
import com.almasb.fxgl.physics.*;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Player.XPComponent;
import com.survivor.entity.Projectile;
import com.survivor.entity.weapon.Blade;
import com.survivor.entity.weapon.BloodCircle;
import com.survivor.entity.weapon.Bullet2;
import com.survivor.main.EntityType;
import com.survivor.util.aStarGrid;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.survivor.entity.Player.XPComponent;
import com.survivor.main.EntityType;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class SlimeEnemyComponent extends Component {


    private double health = 500;
    private boolean dead = false;
    private int waypointIndex = 0;
    private List<Point2D> waypoints = new ArrayList<>();
    private PhysicsComponent physics;  // FXGL 会自动注入

    private AnimatedTexture texture;

    private AnimationChannel idleAnim;
    private AnimationChannel moveAnim;
    private AnimationChannel attackAnim;
    private AnimationChannel deadAnim;


    private boolean isAttacking = false;
    private double timeSinceLastPathfind = 0.0;
    private int attack = 1;
    private double speed = 200;
    private double attackRange = 30;
    private long lastAttackTime = 0;
    private long attackCooldown = 1500; // 毫秒

    @Override
    public void onAdded() {
        float hitBoxW =  10f;
        float hitBoxH = 16f;
        float hitBoxX = -5f;
        float hitBoxY = -8f;
//        Rectangle rectView = new Rectangle(hitBoxW, hitBoxH, Color.GREEN);
//        rectView.setTranslateX(hitBoxX);
//        rectView.setTranslateY(hitBoxY);
        HitBox hitBox = new HitBox(new Point2D(hitBoxX, hitBoxY), BoundingShape.box(hitBoxW, hitBoxH));
//        HitBox hitBox = new HitBox(hitCenter.subtract(new Point2D(hitRadius,hitRadius)), BoundingShape.circle(hitRadius));
        physics.addSensor(hitBox, new SensorCollisionHandler() {
            @Override
            protected void onCollisionBegin(Entity other) {
                if (other.isType(EntityType.PLAYER)) {
                    if (entity.hasComponent(SprintEnemyCompontBat.class)) {
                        other.getComponent(HealthComponent.class).takeDamage(entity.getComponent(SprintEnemyCompontBat.class).getAttack());
                    } else if (entity.hasComponent(SplitEnemyComponent.class)) {
                        other.getComponent(HealthComponent.class).takeDamage(entity.getComponent(SplitEnemyComponent.class).getAttack());
                    } else if (entity.hasComponent(zhiEnemyComponent.class)) {
                        other.getComponent(HealthComponent.class).takeDamage(entity.getComponent(zhiEnemyComponent.class).getAttack());
                    } else if (entity.hasComponent(SmallSplitEnemyComponent.class)) {
                        other.getComponent(HealthComponent.class).takeDamage(entity.getComponent(SmallSplitEnemyComponent.class).getAttack());
                    } else if (entity.hasComponent(RangedEnemyComponent.class)) {
                        other.getComponent(HealthComponent.class).takeDamage(entity.getComponent(RangedEnemyComponent.class).getAttack());
                    } else if (entity.hasComponent(SlimeEnemyComponent.class)) {

                        other.getComponent(HealthComponent.class).takeDamage(entity.getComponent(SlimeEnemyComponent.class).getAttack());

                    } else {

                    }
                }
                else if(other.isType(EntityType.PROJECTILE))
                {


                        takeDamage(4000);

                }
            }


        });
        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setFixedRotation(true);
        });
        idleAnim = new AnimationChannel(FXGL.image("monster/normal/Orc_Idle.png"),
                6, 100, 100, Duration.seconds(0.8), 0, 5);
        moveAnim = new AnimationChannel(FXGL.image("monster/normal/Orc_Walk.png"),
                8, 100, 100, Duration.seconds(0.8), 0, 7);
        attackAnim = new AnimationChannel(FXGL.image("monster/normal/Orc_Attack.png"),
                6, 100, 100, Duration.seconds(0.8), 0, 5);
        deadAnim = new AnimationChannel(FXGL.image("monster/normal/Orc_Death.png"),
                4, 100, 100, Duration.seconds(0.8), 0, 3);
        texture = new AnimatedTexture(idleAnim);
        texture.setTranslateX(-50f); // 让贴图居中
        texture.setTranslateY(-50f);

        entity.getViewComponent().addChild(texture);
        texture.loopAnimationChannel(idleAnim);
    }

    @Override
    public void onUpdate(double tpf) {
        if (dead) {
            return;
        }

        if (isAttacking) {
            // 攻击过程中不移动
            physics.setVelocityX(0);
            physics.setVelocityY(0);
            return;
        }

        // 获取玩家
        var playerOpt = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                .stream().findFirst();

        if (playerOpt.isEmpty()) {
            return;
        }

        Point2D playerPos = playerOpt.get().getCenter().add(new Point2D(70f, 70f));
        Point2D myPos = entity.getCenter();

        double distance = myPos.distance(playerPos);
        timeSinceLastPathfind += tpf;

        // 追踪玩家
//                Point2D dir = playerPos.subtract(myPos).normalize();
        Point2D dir = getMoveDir(myPos, playerPos);
        physics.setVelocityX(dir.getX() * speed);
        physics.setVelocityY(dir.getY() * speed);

        if (texture.getAnimationChannel() != moveAnim) {
            texture.loopAnimationChannel(moveAnim);
        }

        // 翻转朝向
        texture.setScaleX(dir.getX() < 0 ? -1 : 1);


    }

    private void playAttack() {
        isAttacking = true;
        texture.playAnimationChannel(attackAnim);

        texture.setOnCycleFinished(() -> {
            if (texture.getAnimationChannel() == attackAnim) {
                isAttacking = false;
                texture.loopAnimationChannel(idleAnim);
                // 清空回调时用空实现代替，不要传 null
                texture.setOnCycleFinished(() -> {
                });
            }
        });
    }

    public void takeDamage(double damage) {
        System.out.println(dead);
        if (dead == true) {
            return;
        }// 已经死亡不再处理}

        health -= damage;
        System.out.println("Enemy took " + damage + " damage. Remaining HP: " + health);

        if (health <= 0) {
            var playerOpt = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER)
                    .stream().findFirst();
            playerOpt.get().getComponent(XPComponent.class).gainXP(25);
            dead = true;
            speed = 0;
            isAttacking = false;
            //entity.removeComponent(PhysicsComponent.class);

            texture.playAnimationChannel(deadAnim);

            // 在动画最后一帧播完时回调
            texture.setOnCycleFinished(() -> {
                if (texture.getAnimationChannel() == deadAnim) {
                    // 清空回调，避免死循环
                    texture.setOnCycleFinished(() -> {
                    });
                    if (entity != null) {
                        entity.removeFromWorld();
                    }
                }
            });
        } else {
            // TODO: 可以在这里加受伤动画或闪烁效果
            // 例如 texture.loopAnimationChannel(hurtAnim);
        }
    }

    public void findMove() {
        Entity player = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER).stream().findFirst().get();
        if (entity == null || player == null) {
            return;
        }

        // 获取寻路网格和寻路服务
        AStarGrid pathfinderGrid = aStarGrid.grid;
        AStarPathfinder pathfinder = new AStarPathfinder(pathfinderGrid);
        waypointIndex = 0;
        // 获取实体坐标
        Point2D enemyPos = entity.getPosition();
        Point2D playerPos = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER).stream().findFirst().get().getPosition().add(30f, 30f);
        //    var path = pathfinder.findPath((int)entity.getPosition().getX()/ pathfinderGrid.getCellWidth(),entity.getPosition().getY(), FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER).stream().findFirst().get().getPosition().getX(),FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER).stream().findFirst().get().getPosition().getY());
        // 1. 将 Point2D 坐标转换为 Cell 对象
        int startCellX = (int) (enemyPos.getX() / 16);
        int startCellY = (int) (enemyPos.getY() / 16);
        int endCellX = (int) (playerPos.getX() / 16);
        int endCellY = (int) (playerPos.getY() / 16);
//        if (startCellX < 0 || startCellY < 0 || startCellX>pathfinderGrid.getWidth()) return;
        if (startCellX < 0 || startCellY < 0 || startCellX >= 600 || startCellY >= 350) {
            return;
        }
        if (endCellX < 0 || endCellY < 0 || endCellX >= 600 || endCellY >= 350) {
            return;
        }

        List<AStarCell> pathInCells = pathfinder.findPath(startCellX, startCellY, endCellX, endCellY);

        waypoints.clear();
        for (var cell : pathInCells) {
            waypoints.add(new Point2D(cell.getX() * 16, cell.getY() * 16));
        }

    }

    public Point2D getMoveDir(Point2D myPos, Point2D playerPos) {

        if (timeSinceLastPathfind >= 5.0) {
            findMove();
            timeSinceLastPathfind = 0.0; // 重置计时器
        }
        Point2D dir;
        boolean shouldFindNewPath = waypoints == null || waypoints.isEmpty() || waypointIndex >= waypoints.size();

        if (shouldFindNewPath) {
            // 如果需要，则重新寻路
            findMove();

        }

        // 接下来，根据寻路结果来设置最终的移动方向
        if (waypoints != null && !waypoints.isEmpty() && waypointIndex < waypoints.size()) {
            // 寻路成功，沿着路径移动
            Point2D nextWaypoint = waypoints.get(waypointIndex);

            // 检查是否到达当前路点
            if (myPos.distance(nextWaypoint) < 2f) { // 1是一个可调整的阈值
                waypointIndex++;
            }

            if (waypointIndex < waypoints.size()) {
                // 设置方向为下一个路点
                nextWaypoint = waypoints.get(waypointIndex);
                dir = nextWaypoint.subtract(myPos).normalize();
            } else {
                // 如果到达路径终点，则直接朝向玩家
                dir = playerPos.subtract(myPos).normalize();
            }

        } else {
            // 如果寻路失败或没有路径，则直接朝向玩家
            dir = playerPos.subtract(myPos).normalize();
        }
        return dir;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }
}

