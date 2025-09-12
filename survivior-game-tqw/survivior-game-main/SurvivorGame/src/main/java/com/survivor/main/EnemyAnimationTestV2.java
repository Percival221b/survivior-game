package com.survivor.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.survivor.entity.EnemyAnimationPool;
import com.survivor.entity.EnemyComponent;
import com.survivor.entity.EnemyFactory;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.effect.ColorAdjust;

/**
 * 新版小怪动画测试工具
 * 按键 Z/X/C/V 切换 NORMAL/TANK/SPEED/WIZARD 小怪，其余操作不变
 */
public class EnemyAnimationTestV2 extends GameApplication {

    /* ============================== 新增字段 ============================== */
    private EnemyFactory.EnemyType currentType = EnemyFactory.EnemyType.NORMAL; // 默认初始小怪
    /* ==================================================================== */

    private Entity enemy;
    private EnemyComponent enemyComponent;
    private Text instructionText;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Enemy Animation Test V2 — Z/X/C/V 切换小怪");
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setDeveloperMenuEnabled(true);
    }

    @Override
    protected void initGame() {
        createEnemyByType(currentType);   // 修改：抽取成独立方法，方便复用
    }

    /* ============================== 新增方法 ============================== */
    // 根据类型重新创建小怪实体，并绑定动画
    private void createEnemyByType(EnemyFactory.EnemyType type) {
        // 如果场景里已有旧实体，先移除
        if (enemy != null) {
            enemy.removeFromWorld();
        }

        // 创建一个假玩家作为追踪目标（坐标随意）
        Entity dummyPlayer = FXGL.entityBuilder()
                .at(400, 300)
                .view("role.png")   // 需要实际资源
                .buildAndAttach();

        // 通过工厂生成指定类型小怪
        enemy = EnemyFactory.spawnEnemy(type, new Point2D(100, 100));
        enemyComponent = enemy.getComponent(EnemyComponent.class);

        /* ---------- 关键：把工厂里用的动画换成我们测试想看的动画 ---------- */
        EnemyAnimationPool.EnemyAnimationSet animSet = EnemyAnimationPool.get(type);

        // 重新设置动画通道（保证与工厂属性一致，且能完整播放）
        enemyComponent.setAnimIdle(animSet.idle);
        enemyComponent.setAnimWalk(animSet.walk);
        enemyComponent.setAnimAttack(animSet.attack);
        enemyComponent.setAnimDeath(animSet.death);
        enemyComponent.setAnimHit(animSet.hit);

        // 把动画纹理重新绑定到实体视图
        enemyComponent.getTexture().loopAnimationChannel(animSet.idle);
    }
    /* ==================================================================== */

    @Override
    protected void initUI() {
        instructionText = new Text();
        instructionText.setTranslateX(10);
        instructionText.setTranslateY(20);
        instructionText.setFill(Color.WHITE);
        instructionText.setText(
                "动画测试控制:\n" +
                        "方向键: 移动敌人(触发行走动画)\n" +
                        "鼠标左键: 播放攻击动画\n" +
                        "鼠标右键: 播放受伤效果\n" +
                        "R键: 重置敌人状态\n" +
                        "K键: 播放死亡动画\n" +
                        "Z键: 切换 NORMAL 小怪\n" +      // 新增
                        "X键: 切换 TANK 小怪\n" +       // 新增
                        "C键: 切换 SPEED 小怪\n" +      // 新增
                        "V键: 切换 WIZARD 小怪"         // 新增
        );
        FXGL.getGameScene().addUINode(instructionText);
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        /* ========================== 新增：Z/X/C/V 切换小怪 ========================== */
        input.addAction(new UserAction("Switch to NORMAL") {
            @Override
            protected void onActionBegin() {
                currentType = EnemyFactory.EnemyType.NORMAL;
                createEnemyByType(currentType);
            }
        }, KeyCode.Z);

        input.addAction(new UserAction("Switch to TANK") {
            @Override
            protected void onActionBegin() {
                currentType = EnemyFactory.EnemyType.TANK;
                createEnemyByType(currentType);
            }
        }, KeyCode.X);

        input.addAction(new UserAction("Switch to SPEED") {
            @Override
            protected void onActionBegin() {
                currentType = EnemyFactory.EnemyType.SPEED;
                createEnemyByType(currentType);
            }
        }, KeyCode.C);

        input.addAction(new UserAction("Switch to WIZARD") {
            @Override
            protected void onActionBegin() {
                currentType = EnemyFactory.EnemyType.WIZARD;
                createEnemyByType(currentType);
            }
        }, KeyCode.V);
        /* ========================================================================== */

        // 以下原有逻辑不变，仅把对 enemy/enemyComponent 的引用换成最新实体即可
        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                enemy.translateY(-5);
                enemyComponent.getTexture().loopAnimationChannel(enemyComponent.getAnimWalk());
            }
        }, KeyCode.UP);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                enemy.translateY(5);
                enemyComponent.getTexture().loopAnimationChannel(enemyComponent.getAnimWalk());
            }
        }, KeyCode.DOWN);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                enemy.translateX(-5);
                enemyComponent.getTexture().loopAnimationChannel(enemyComponent.getAnimWalk());
            }
        }, KeyCode.LEFT);

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                enemy.translateX(5);
                enemyComponent.getTexture().loopAnimationChannel(enemyComponent.getAnimWalk());
            }
        }, KeyCode.RIGHT);

        input.addAction(new UserAction("Play Attack Animation") {
            @Override
            protected void onActionBegin() {
                enemyComponent.getTexture().playAnimationChannel(enemyComponent.getAnimAttack());
            }
        }, MouseButton.PRIMARY);

        input.addAction(new UserAction("Play Hit Effect") {
            @Override
            protected void onActionBegin() {
                ColorAdjust redEffect = new ColorAdjust();
                redEffect.setHue(-0.8);
                redEffect.setSaturation(0.9);
                redEffect.setBrightness(0.2);
                enemyComponent.getTexture().setEffect(redEffect);
                enemyComponent.getTexture().playAnimationChannel(enemyComponent.getAnimHit());

                FXGL.getGameTimer().runOnceAfter(() -> {
                    enemyComponent.getTexture().setEffect(null);
                    enemyComponent.getTexture().loopAnimationChannel(enemyComponent.getAnimIdle());
                }, Duration.seconds(0.3));
            }
        }, MouseButton.SECONDARY);

        input.addAction(new UserAction("Reset Enemy") {
            @Override
            protected void onActionBegin() {
                enemy.setPosition(100, 100);
                enemyComponent.getTexture().loopAnimationChannel(enemyComponent.getAnimIdle());
            }
        }, KeyCode.R);

        input.addAction(new UserAction("Play Death Animation") {
            @Override
            protected void onActionBegin() {
                enemyComponent.getTexture().playAnimationChannel(enemyComponent.getAnimDeath());
            }
        }, KeyCode.K);
    }

    public static void main(String[] args) {
        launch(args);
    }
}