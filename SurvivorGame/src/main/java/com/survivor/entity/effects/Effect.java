package com.survivor.entity.effects;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.util.Duration;

/**
 * 特效组件的抽象基类。
 * 所有特效都应继承此类，并实现自己的动画和逻辑。
 */
public abstract class Effect extends Component {

    // 特效的动画纹理
    protected AnimatedTexture animatedTexture;

    // 特效的移动方向和速度
    protected Vec2 moveDirection;
    protected float moveSpeed;

    // 特效的持续时间
    protected Duration duration;
    protected double timer = 0;

    /**
     * 构造函数。
     * @param texture 静态纹理，用于初始化动画
     * @param frameCount 动画帧数
     * @param duration 动画持续时间
     */
    public Effect(Texture texture, int frameCount, Duration duration) {
        // 创建一个每帧100ms的循环播放动画
        this.animatedTexture = texture.toAnimatedTexture(frameCount, Duration.millis(100));
        this.animatedTexture.loop(); // 设置动画循环播放
        this.duration = duration;
    }

    // --- 初始化和动画相关方法 ---

    @Override
    public void onAdded() {
        // 当组件被添加到实体时，将动画纹理添加到实体视图
        getEntity().getViewComponent().addChild(animatedTexture);
    }

    @Override
    public void onRemoved() {
        // 当组件被移除时，停止动画并从实体视图移除
        animatedTexture.stop();
        getEntity().getViewComponent().removeChild(animatedTexture);
    }

    // --- 移动相关方法 ---

    /**
     * 设置特效的移动方向和速度。
     * @param direction 移动方向向量
     * @param speed 移动速度
     */
    public void setMove(Vec2 direction, float speed) {
        this.moveDirection = direction.normalize();
        this.moveSpeed = speed;
    }

    @Override
    public void onUpdate(double tpf) {
        // 累积游戏时间
        timer += tpf;

        // 如果设置了移动，就更新实体位置
        if (moveDirection != null && moveSpeed > 0) {
            Point2D newPosition = getEntity().getPosition()
                    .add(moveDirection.toPoint2D().multiply(moveSpeed * tpf * 60)); // 乘以60是因为tpf是每帧的时间，通常游戏是60FPS，这样能保证速度稳定
            getEntity().setPosition(newPosition);
        }

        // 检查特效是否已过期，如果过期则从实体移除
        if (timer >= duration.toSeconds()) {
            getEntity().removeFromWorld();
        }
    }
}