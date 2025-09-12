package com.survivor.system;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.BoundingShape;
import com.survivor.core.GameSceneManager;
import com.survivor.core.SpawnArea;
import com.survivor.core.UIManager;
import com.survivor.entity.Player.HealthComponent;
import com.survivor.entity.Player.XPComponent;
import com.survivor.entity.Player.PlayerAnimationComponent;
import com.survivor.entity.Player.PlayerMovementComponent;
import com.survivor.main.EntityType;
import com.survivor.ui.HUD;
import com.survivor.ui.upgrades.UpgradeOption;
import com.survivor.ui.upgrades.UpgradePanel;
import com.survivor.ui.upgrades.UpgradeRepository;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;

import java.util.List;
import java.util.ArrayList;

public class ResourceLoader implements EntityFactory {

    private static final List<SpawnArea> spawnAreas = new ArrayList<>();
    private UpgradePanel currentUpgradePanel;

    private final UIManager uiManager;   // ✅ 不再用 FXGL.geto
    private final GameSceneManager gsm;

    public ResourceLoader(GameSceneManager gsm) {
        this.gsm = gsm;
        this.uiManager = gsm.getUiManager();
    }

    @Spawns("spawnArea")
    public Entity newSpawnArea(SpawnData data) {
        double x = data.getX();
        double y = data.getY();
        int w = data.get("width");
        int h = data.get("height");

        spawnAreas.add(new SpawnArea(x, y, w, h));

        return FXGL.entityBuilder(data)
                .type(EntityType.SPAWN_AREA)
                .build();
    }

    public static List<SpawnArea> getSpawnAreas() {
        return spawnAreas;
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        HealthComponent health = new HealthComponent(100);
        XPComponent xp = new XPComponent();

        Entity player = FXGL.entityBuilder(data)
                .type(EntityType.PLAYER)
                .with(physics)
                .with(new PlayerMovementComponent())
                .with(health)
                .with(xp)
                .with(new PlayerAnimationComponent())
                .collidable()
                .scale(0.5, 0.5)
                .build();

        // 綁定 XP -> HUD
        xp.setOnXPChange(this::updateXPUI);

        xp.setOnLevelUp(level -> {
            Platform.runLater(() -> {
                updateLevelUI(level);
                FXGL.getGameController().pauseEngine();

                List<UpgradeOption> options;
                try {
                    options = UpgradeRepository.getRandomOptions();
                    if (options.isEmpty()) {
                        System.err.println("No upgrade options available");
                        FXGL.getGameController().resumeEngine();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    FXGL.getGameController().resumeEngine();
                    return;
                }

                currentUpgradePanel = new UpgradePanel(options, chosen -> {
                    applyUpgrade(chosen);
                    if (currentUpgradePanel != null) {
                        uiManager.removeOverlay(currentUpgradePanel);
                    }
                    FXGL.getGameController().resumeEngine();
                });

                currentUpgradePanel.setPrefSize(1280, 720);
                uiManager.addOverlay(currentUpgradePanel);  // ✅ 用傳進來的 uiManager
                currentUpgradePanel.playIn();
            });
        });

        // 綁定 HP -> HUD
        health.setOnHealthChange(this::updateHealthUI);

        return player;
    }

    @Spawns("wall")
    public Entity newWall(SpawnData data) {
        int w = data.get("width");
        int h = data.get("height");

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);

        return FXGL.entityBuilder(data)
                .type(EntityType.WALL)
                .bbox(new HitBox(new Point2D(0, 0), BoundingShape.box(w, h)))
                .with(physics)
                .collidable()
                .build();
    }

    private void updateXPUI(int currentXP, int xpToNextLevel) {
        FXGL.getGameScene().getUINodes().forEach(node -> {
            if (node instanceof HUD hud) {
                hud.setMaxExp(xpToNextLevel);
                hud.setExp(currentXP);
            }
        });
    }

    private void updateLevelUI(int level) {
        FXGL.getGameScene().getUINodes().forEach(node -> {
            if (node instanceof HUD hud) {
                hud.setLevel(level);
            }
        });
    }

    private void updateHealthUI(int hp, int maxHp) {
        FXGL.getGameScene().getUINodes().forEach(node -> {
            if (node instanceof HUD hud) {
                hud.setMaxHealth(maxHp);
                hud.setHealth(hp);
            }
        });
    }

    private static void applyUpgrade(UpgradeOption chosen) {
        System.out.println("Applying upgrade: " + chosen.getTitle());
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        if (player != null) {
            String upgradeId = chosen.getId();
            if ("health_boost".equals(upgradeId)) {
                player.getComponent(HealthComponent.class).increaseMaxHP(50);
            } else if ("speed_boost".equals(upgradeId)) {
                player.getComponent(PlayerMovementComponent.class).setSpeed(1.2);
            }
        }
    }
}
