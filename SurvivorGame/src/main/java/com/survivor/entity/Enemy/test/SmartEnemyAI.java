package com.survivor.entity.Enemy.test;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survivor.entity.Player.PlayerSoundComponent;
import com.survivor.main.EntityType;
import com.survivor.main.GameApp;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmartEnemyAI extends Component {

    // ========== 小怪属性 ==========
    private int health = 1000;          // 生命值 (基础值，可根据小怪类型调整)
    private int damage = 5000;          // 攻击伤害 (每次攻击造成的伤害)
    private double speed = 200;       // 移动速度 (像素/帧)
    private double attackRange = 50;  // 攻击范围 (像素)
    private int expValue = 5;         // 死亡时掉落的经验值
    boolean isAttacking = false;

    // ========== 组件引用 ==========
    private PhysicsComponent physics; // 物理组件用于移动
    private AnimatedTexture texture;  // 动画纹理

    // ========== 动画通道 ==========
    private AnimationChannel animIdle;   // 空闲动画
    private AnimationChannel animWalk;   // 行走动画
    private AnimationChannel animAttack; // 攻击动画
    private AnimationChannel animDeath;// 死亡动画
    private AnimationChannel animHit;

    private long lastAttackTime = 0;// 攻击冷却计时器
    private double attackCooldown = 1000;
    private boolean isDead = false;              // 是否已死亡
    private boolean paused = false;
    private String currentAction = "patrol";

    //玩家血量（可以从别的组件同步）
    private double playerHp = 100;

    // === 后端请求节流 ===
    private double queryCooldown = 0;       // 距离下次请求的时间
    private double queryInterval = 5.0;// 每秒钟请求一次

    // === patrol 随机巡逻支持 ===
    private Random random = new Random();
    private double patrolCooldown = 0;
    private Point2D patrolDir = new Point2D(0, 0);

    private Entity cachedTargetMonster = null;  // 缓存的目标怪物
    private double targetUpdateCooldown = 0;    // 更新计时器
    private final double targetUpdateInterval = 2.0; // 每隔 2 秒更新一次最近怪物

    public SmartEnemyAI() {
        // 初始化动画 (假设使用100x100像素的精灵图)
        animIdle = new AnimationChannel(FXGL.image("ai_idle.png"), 9, 80, 100, Duration.seconds(1), 0, 8);
        animWalk = new AnimationChannel(FXGL.image("ai_run.png"), 6, 80, 100, Duration.seconds(0.8), 0, 5);
        animAttack = new AnimationChannel(FXGL.image("ai_attack.png"), 12, 80, 100, Duration.seconds(1.0), 0, 11);
        animDeath = new AnimationChannel(FXGL.image("ai_die.png"), 23, 80, 80, Duration.seconds(1.5), 0, 22);
        animHit=new AnimationChannel(FXGL.image("ai_hit.png"),5,80,100,Duration.seconds(1),0,4);

        texture = new AnimatedTexture(animIdle);
    }

    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
        // 添加碰撞组件
        entity.addComponent(new CollidableComponent(true));
        // 添加动画纹理到实体
        entity.getViewComponent().addChild(texture);
        // 初始播放空闲动画
        texture.loopAnimationChannel(animIdle);
    }

    // 线程池，避免阻塞主线程
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(double tpf) {
        if (isDead || paused) {
            stopMovement();
            return;
        }

        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        if (player == null) {
            stopMovement();
            return;
        }

        // 如果正在攻击，锁定动作，不执行移动
        if (isAttacking) {
            stopMovement();
            return;
        }

        // 获取玩家位置
        var playerOpt = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER).stream().findFirst();
        Point2D playerPos = playerOpt.get().getCenter().add(new Point2D(-50f,-50f));
        Point2D myPos = entity.getCenter();
        double distance = myPos.distance(playerPos);



        targetUpdateCooldown -= tpf;
        if (targetUpdateCooldown <= 0 || cachedTargetMonster == null || !cachedTargetMonster.isActive()) {
            cachedTargetMonster = getClosestMonsterToPlayer();
            targetUpdateCooldown = targetUpdateInterval;
        }

        if (cachedTargetMonster != null) {
            distance = myPos.distance(cachedTargetMonster.getPosition());
            if (distance > 80) {
                moveTowards(cachedTargetMonster.getCenter());
            }
            else {
                stopMovement();
                long now = System.currentTimeMillis();
                if (now - lastAttackTime >= attackCooldown) {
                    lastAttackTime = now;
                    playAttack();
                }
            }
        } else {
            performPatrol(tpf);
        }

        // 决策逻辑
        /*String action = NeutralMonsterAIManager.getInstance().getGlobalDecision();

        switch (action) {
            case "attack_player":
                if (distance > attackRange) {
                    // 玩家在攻击范围之外，移动过去
                    moveTowards(playerPos);
                } else {
                    // 玩家在攻击范围内，停止移动并攻击
                    stopMovement();
                    long now = System.currentTimeMillis();
                    if (now - lastAttackTime >= attackCooldown) {
                        lastAttackTime = now;
                        playAttack();
                    }
                }
                break;

            case "patrol":
                performPatrol(tpf);
                break;

            case "flee":
                moveAway(player);
                break;

            case "attack_monster":
                // 更新计时器
                targetUpdateCooldown -= tpf;
                if (targetUpdateCooldown <= 0 || cachedTargetMonster == null || !cachedTargetMonster.isActive()) {
                    cachedTargetMonster = getClosestMonsterToPlayer();
                    targetUpdateCooldown = targetUpdateInterval;
                }

                if (cachedTargetMonster != null) {
                    distance = myPos.distance(cachedTargetMonster.getPosition());
                    if (distance > 50) {
                        moveTowards(cachedTargetMonster.getCenter());
                    }
                    else {
                        stopMovement();
                        long now = System.currentTimeMillis();
                        if (now - lastAttackTime >= attackCooldown) {
                            lastAttackTime = now;
                            playAttack();
                        }
                    }
                } else {
                    performPatrol(tpf);
                }
                break;

            default:
                performPatrol(tpf);
                break;
        }*/
    }

    // 随机选取一个其他怪物
    private Entity getClosestMonsterToPlayer() {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        if (player == null) return null;

        var monsters = FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY);
        Entity closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity m : monsters) {
            if (m == entity) continue; // 排除自己
            double distance = m.getCenter().distance(player.getCenter());
            if (distance < minDistance) {
                minDistance = distance;
                closest = m;
            }
        }

        return closest;
    }


    public static String sendMessage(String message) {
        try {
            URL url = new URL("http://127.0.0.1:8001/chat");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // 构造 JSON 请求体
            String jsonInputString = "{\"message\": \"" + message.replace("\"", "\\\"") + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 读取返回内容
            String responseText;
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                responseText = scanner.useDelimiter("\\A").next();
            }

            // ===== Debug 打印原始 JSON =====
            System.out.println("=== SmartEnemyAI.sendMessage ===");
            System.out.println("Request: " + jsonInputString);
            System.out.println("Raw Response: " + responseText);

            // 解析 JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseText);

            String action = null;
            if (jsonNode.has("answer")) {
                action = jsonNode.get("answer").asText();
            } else {
                // 如果没有 answer 字段，就直接返回原始字符串，方便调试
                action = responseText;
            }

            System.out.println("Parsed action: " + action);
            return action;

        } catch (Exception e) {
            e.printStackTrace();
            return "patrol"; // 出错时默认巡逻
        }
    }

    private void performPatrol(double tpf) {
        patrolCooldown -= tpf;
        if (patrolCooldown <= 0 || patrolDir.equals(Point2D.ZERO)) {
            double angle = random.nextDouble() * 2 * Math.PI;
            patrolDir = new Point2D(Math.cos(angle), Math.sin(angle)).normalize();
            patrolCooldown = 2 + random.nextDouble() * 2; // 2~4秒换一次方向
        }
        physics.setVelocityX(patrolDir.getX() * speed * 0.5);
        physics.setVelocityY(patrolDir.getY() * speed * 0.5);
        texture.setScaleX(patrolDir.getX() < 0 ? -1 : 1);
    }

    private void moveTowards(Point2D targetPos) {
        Point2D dir = targetPos.subtract(entity.getCenter());
        if (!dir.equals(Point2D.ZERO)) {
            dir = dir.normalize();
            physics.setVelocityX(dir.getX() * speed);
            physics.setVelocityY(dir.getY() * speed);

            if (texture.getAnimationChannel() != animWalk) {
                texture.loopAnimationChannel(animWalk);
            }
            texture.setScaleX(dir.getX() < 0 ? -1 : 1);
        }
    }

    private void playAttack() {
        if (isAttacking) return; // 防止重复触发

        isAttacking = true;
        stopMovement();

        // 播放一次攻击动画
        texture.playAnimationChannel(animAttack);

        FXGL.runOnce(() -> {
            FXGL.spawn("neutralBlade", new SpawnData( entity.getCenter())
                    .put("startPos",  entity.getCenter())
                    .put("damage", 1000f)
                    .put("hitCenter",(new Point2D(0f,0f)))
                    .put("hitRadius", 50f)
                    .put("offsetPos",new Point2D(0f,0f))
                    .put("duration",0.1f));
        }, Duration.seconds(0.6)); // 动画一半时出招

        // 动画完成回调
        texture.setOnCycleFinished(() -> {
            if (texture.getAnimationChannel() == animAttack) {
                isAttacking = false;
                texture.loopAnimationChannel(animIdle); // 回到空闲动画
                texture.setOnCycleFinished(() -> {});   // 清空回调
            }
        });
    }



    private void moveAway(Entity player) {
        System.out.println("moveAway");
        texture.loopAnimationChannel(animWalk);
        Point2D dir = entity.getCenter().subtract(player.getCenter());
        if (!dir.equals(Point2D.ZERO)) {
            dir = dir.normalize();
            physics.setVelocityX(dir.getX() * speed);
            physics.setVelocityY(dir.getY() * speed);
            texture.setScaleX(dir.getX() < 0 ? 1 : -1);
        }
    }

    public void takeDamage(int amount) {
        if (isDead) return;

        health -= amount;

        if (health > 0) {
            // 播放受击动画
            texture.playAnimationChannel(animHit);

            // 动画结束后回到 Idle
            FXGL.runOnce(() -> {
                if (!isDead) {
                    texture.loopAnimationChannel(animIdle);
                }
            }, Duration.seconds(1.0));
        } else {
            // 触发死亡
            isDead = true;
            playDeathAnimation();
        }
    }

    // 死亡
    private void playDeathAnimation() {
        texture.playAnimationChannel(animDeath);

        // 动画结束后移除实体
        FXGL.runOnce(() -> {
            entity.removeFromWorld();
        }, Duration.seconds(1.0));
    }
    public AnimatedTexture getTexture() {
        return texture;
    }

    public AnimationChannel getAnimIdle() {
        return animIdle;
    }

    public AnimationChannel getAnimWalk() {
        return animWalk;
    }

    public AnimationChannel getAnimAttack() {
        return animAttack;
    }

    public AnimationChannel getAnimDeath() {
        return animDeath;
    }
    public AnimationChannel getAnimHit() {
        return animHit;
    }

    private void stopMovement() {
        physics.setVelocityX(0);
        physics.setVelocityY(0);
    }

    public void setPaused(boolean value) {
        this.paused = value;
    }

    public String getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(String action) {
        this.currentAction = action;
    }
}
