package com.survivor.entity.Enemy.test;

import com.almasb.fxgl.dsl.FXGL;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.main.EntityType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NeutralMonsterAIManager {

    private static final NeutralMonsterAIManager instance = new NeutralMonsterAIManager();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String globalDecision = "DEFAULT"; // 默认行为
    private double queryCooldown = 0;
    private double queryInterval = 5.0; // 每 5 秒请求一次大模型

    public static NeutralMonsterAIManager getInstance() {
        return instance;
    }

    public void onUpdate(double tpf) {
        System.out.println("[NeutralMonsterAIManager] onUpdate called, cooldown=" + queryCooldown);
        queryCooldown -= tpf;
        if (queryCooldown <= 0) {
            queryCooldown = queryInterval;

            // === 收集全局状态 ===
            int playerHp = FXGL.getGameWorld().getSingleton(EntityType.PLAYER)
                    .getComponent(HealthComponent.class).getHP();

            long monsterCount = FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY).size();

            String message = "Answer only one word. If you are in my game. The hero's health is "+playerHp+". The number of monster is "+monsterCount+". Who will you attack, hero or monster?";
            System.out.println("[NeutralMonsterAIManager] Submitting query: " + message);
            executor.submit(() -> {
                String action = SmartEnemyAI.sendMessage(message);

                System.out.println("=== NeutralMonsterAIManager Debug ===");
                System.out.println("Raw action from SmartEnemyAI: " + action);

                if (action != null && !action.isEmpty()) {
                    synchronized (this) {
                        synchronized (this) {
                            globalDecision = action.toLowerCase().trim();
                        }
                        switch (action) {
                            case "hero":
                                globalDecision = "attack_player";
                                break;
                            case "monster":
                                globalDecision = "attack_monster";
                                break;
                            case "flee":
                                globalDecision = "flee";
                                break;
                            default:
                                globalDecision = "patrol";
                                break;
                        }

                        System.out.println("Mapped globalDecision: " + globalDecision);
                    }
                } else {
                    System.out.println("Action is null or empty, fallback to patrol");
                }
            });

        }
    }

    private String mapDecision(String action) {
        switch (action) {
            case "hero":
                return "attack_player";
            case "player":
                return "attack_player";
            case "monster":
                return "attack_monster";
            case "enemy":
                return "attack_monster";
            default:
                System.out.println(action+" is not a valid action");
                return "patrol"; // 如果返回了其他奇怪的东西，就默认巡逻
        }
    }


    public String getGlobalDecision() {
        synchronized (this) {
            return globalDecision;
        }
    }
}

