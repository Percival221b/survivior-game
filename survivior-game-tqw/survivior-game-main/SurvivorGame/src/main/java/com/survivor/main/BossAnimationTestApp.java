 /********------------------版本1----------------------****
package com.survivor.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.survivor.entity.BossComponent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.effect.ColorAdjust;

public class BossAnimationTestApp extends GameApplication {

    private Entity boss;
    private BossComponent bossComponent;
    private Text instructionText;
    private Text statusText;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Boss Animation Test");
        settings.setWidth(1000);
        settings.setHeight(700);
        settings.setDeveloperMenuEnabled(true);
    }

    @Override
    protected void initGame() {
        // 创建测试用的Boss实体
        boss = FXGL.entityBuilder()
                .at(400, 300)
                .with(new BossComponent())
                .scaleOrigin(0.5, 0.5)  // 设置缩放原点为中心
                .scale(0.5, 0.5)        // 缩小以适应屏幕
                .buildAndAttach();

        bossComponent = boss.getComponent(BossComponent.class);
    }

    @Override
    protected void initUI() {
        // 添加操作说明
        instructionText = new Text();
        instructionText.setTranslateX(10);
        instructionText.setTranslateY(20);
        instructionText.setFill(Color.WHITE);
        instructionText.setText(
                "Boss动画测试控制:\n" +
                        "方向键: 移动Boss\n" +
                        "鼠标左键: 攻击动作\n" +
                        "鼠标右键: 受击效果\n" +
                        "Z键: 切换到第一阶段(远程形态)\n" +
                        "X键: 切换到第二阶段(近战形态)\n" +
                        "K键: 播放死亡动画\n" +
                        "R键: 重置Boss状态"
        );

        // 添加状态显示
        statusText = new Text();
        statusText.setTranslateX(10);
        statusText.setTranslateY(180);
        statusText.setFill(Color.WHITE);
        updateStatusText();

        FXGL.getGameScene().addUINode(instructionText);
        FXGL.getGameScene().addUINode(statusText);

        // 设置背景色
        FXGL.getGameScene().setBackgroundColor(Color.DARKGRAY);
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        // 方向键控制移动
        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                boss.translateY(-5);
                if (bossComponent.getPhase() == 1) {
                    bossComponent.playMoveAnimationPhase1();
                } else {
                    bossComponent.playMoveAnimationPhase2();
                }
            }
        }, KeyCode.UP);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                boss.translateY(5);
                if (bossComponent.getPhase() == 1) {
                    bossComponent.playMoveAnimationPhase1();
                } else {
                    bossComponent.playMoveAnimationPhase2();
                }
            }
        }, KeyCode.DOWN);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                boss.translateX(-5);
                boss.setScaleX(-0.5); // 面向左
                if (bossComponent.getPhase() == 1) {
                    bossComponent.playMoveAnimationPhase1();
                } else {
                    bossComponent.playMoveAnimationPhase2();
                }
            }
        }, KeyCode.LEFT);

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                boss.translateX(5);
                boss.setScaleX(0.5); // 面向右
                if (bossComponent.getPhase() == 1) {
                    bossComponent.playMoveAnimationPhase1();
                } else {
                    bossComponent.playMoveAnimationPhase2();
                }
            }
        }, KeyCode.RIGHT);


        // 鼠标左键播放攻击动画
        input.addAction(new UserAction("Play Attack Animation") {
            @Override
            protected void onActionBegin() {
                bossComponent.playAttackAnimation();
                updateStatusText();
            }
        }, MouseButton.PRIMARY);

        // 鼠标右键播放受击效果
        input.addAction(new UserAction("Play Hit Effect") {
            @Override
            protected void onActionBegin() {
                // 创建红色闪烁效果
                ColorAdjust redEffect = new ColorAdjust();
                redEffect.setHue(-0.8);       // 红色色调
                redEffect.setSaturation(0.9);  // 高饱和度
                redEffect.setBrightness(0.2);  // 稍微变亮


                // 播放受击动画
                bossComponent.playTakeHitAnimation();
                updateStatusText();

                // 动画播放完成后恢复
                FXGL.getGameTimer().runOnceAfter(() -> {

                    // 恢复空闲动画
                    if (bossComponent.getPhase() == 1) {
                        bossComponent.playIdleAnimationPhase1();
                    } else {
                        bossComponent.playIdleAnimationPhase2();
                    }
                    updateStatusText();
                }, Duration.seconds(0.3));
            }
        }, MouseButton.SECONDARY);

        // Z键切换到第一阶段
        input.addAction(new UserAction("Switch to Phase 1") {
            @Override
            protected void onActionBegin() {
                bossComponent.setPhase(1);
                bossComponent.playIdleAnimationPhase1();
                updateStatusText();
            }
        }, KeyCode.Z);

        // X键切换到第二阶段
        input.addAction(new UserAction("Switch to Phase 2") {
            @Override
            protected void onActionBegin() {
                bossComponent.setPhase(2);
                bossComponent.playIdleAnimationPhase2();
                updateStatusText();
            }
        }, KeyCode.X);

        // R键重置Boss状态
        input.addAction(new UserAction("Reset Boss") {
            @Override
            protected void onActionBegin() {
                boss.setPosition(400, 300);
                boss.setScaleX(0.5); // 重置朝向
                bossComponent.reset();
                updateStatusText();
            }
        }, KeyCode.R);

        // K键播放死亡动画
        input.addAction(new UserAction("Play Death Animation") {
            @Override
            protected void onActionBegin() {
                bossComponent.playDeathAnimation();
                updateStatusText();
            }
        }, KeyCode.K);
    }

    // 更新状态文本
    private void updateStatusText() {
        String phaseText = bossComponent.getPhase() == 1 ? "第一阶段(远程)" : "第二阶段(近战)";
        String aliveText = bossComponent.isAlive() ? "存活" : "死亡";

        statusText.setText(
                "当前状态:\n" +
                        "阶段: " + phaseText + "\n" +
                        "生命值: " + bossComponent.getHealth() + "/" + BossComponent.MAX_HEALTH + "\n" +
                        "状态: " + aliveText
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}*****/
 package com.survivor.main;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import com.survivor.entity.BossComponent;
import com.survivor.entity.BossFactory;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BossAnimationTestApp extends GameApplication{

    private Entity boss;
    private Point2D mouseTarget = new Point2D(400, 300); // 初始目标点

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("Boss Mouse Chase Test");
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new BossFactory());

        boss = spawn("boss", 400, 300);

        // 监听鼠标移动，实时更新 Boss 的目标点
        getGameScene().getInput().addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            mouseTarget = new Point2D(e.getX(), e.getY());
        });
    }

    @Override
    protected void onUpdate(double tpf) {
        BossComponent bossComp = boss.getComponent(BossComponent.class);
        bossComp.updateTarget(mouseTarget);
    }

    @Spawns("boss")
    public Entity newBoss(SpawnData data) {
        return entityBuilder(data)
                .with(new BossComponent())
                .build();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

