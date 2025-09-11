package com.survivor.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.survivor.entity.EnemyComponent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.effect.ColorAdjust;  // 添加这行导入
import javafx.scene.paint.Color;         // 添加这行导入

public class EnemyAnimationTest extends GameApplication {

    private Entity enemy;
    private EnemyComponent enemyComponent;
    private Text instructionText;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Enemy Animation Test");
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setDeveloperMenuEnabled(true);
    }

    @Override
    protected void initGame() {
        // 创建一个虚拟的"玩家"实体作为目标
        Entity dummyPlayer = FXGL.entityBuilder()
                .at(400, 300)
                .view("role.png") // 假设有这个图片资源
                .buildAndAttach();

        // 创建测试用的敌人
        enemy = FXGL.entityBuilder()
                .at(100, 100)
                .with(new EnemyComponent(dummyPlayer))
                .scaleOrigin(0.5, 0.5)  // 设置缩放原点为中心
                .scale(2.0, 2.0)        // 放大两倍
                .buildAndAttach();

        enemyComponent = enemy.getComponent(EnemyComponent.class);
    }

    @Override
    protected void initUI() {
        // 添加操作说明
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
                        "K键: 播放死亡动画"
        );

        FXGL.getGameScene().addUINode(instructionText);
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        // 方向键控制移动(触发行走动画)
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

        // 鼠标左键播放攻击动画
        input.addAction(new UserAction("Play Attack Animation") {
            @Override
            protected void onActionBegin() {
                enemyComponent.getTexture().playAnimationChannel(enemyComponent.getAnimAttack());
            }
        }, MouseButton.PRIMARY);

        // 鼠标右键播放受伤效果(短暂闪烁红色)
        input.addAction(new UserAction("Play Hit Effect") {
            @Override
            protected void onActionBegin() {
                // ========== 添加的代码开始 ==========
                // 创建红色闪烁效果
                ColorAdjust redEffect = new ColorAdjust();
                redEffect.setHue(-0.8);       // 红色色调
                redEffect.setSaturation(0.9);  // 高饱和度
                redEffect.setBrightness(0.2);  // 稍微变亮

                // 应用红色效果
                enemyComponent.getTexture().setEffect(redEffect);
                // ========== 添加的代码结束 ==========

                // 播放受击动画（原有代码保持不变）
                enemyComponent.getTexture().playAnimationChannel(enemyComponent.getAnimHit());

                // 动画播放完成后恢复空闲状态（修改了这部分）
                FXGL.getGameTimer().runOnceAfter(() -> {
                    // ========== 修改的代码开始 ==========
                    // 移除红色效果
                    enemyComponent.getTexture().setEffect(null);
                    // ========== 修改的代码结束 ==========

                    // 恢复空闲动画（原有代码保持不变）
                    enemyComponent.getTexture().loopAnimationChannel(enemyComponent.getAnimIdle());
                }, Duration.seconds(0.3));
            }
        }, MouseButton.SECONDARY);

        // R键重置敌人状态(回到空闲动画)
        input.addAction(new UserAction("Reset Enemy") {
            @Override
            protected void onActionBegin() {
                enemy.setPosition(100, 100);
                enemyComponent.getTexture().loopAnimationChannel(enemyComponent.getAnimIdle());
            }
        }, KeyCode.R);

        // K键播放死亡动画
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