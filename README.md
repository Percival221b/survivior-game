# Survivor 

## 一、项目介绍
一个基于 JavaFX 开发的轻量级幸存者类游戏。玩家在像素风奇幻世界观中操控角色，击败不断生成的敌人，收集经验升级，解锁新的武器和技能。 

## 二、开发工具
- JDK 17  
- FXGL 17.3  
- Maven 3.9.9  
- Maven Plugin 0.0.8

## 三、接口定义
- 事件总线 (EventBus)**：用于在不同模块间传递消息，实现松耦合  
- 事件 (Event)**：游戏中发生的具体动作（如击杀、升级、碰撞）  
- 事件监听器 (EventListener)**：对特定事件作出响应（如经验条增加、音效播放）  
- Game 上下文 (GameContext)**：维护游戏运行的全局状态（分数、时间、实体列表）  
- Game 模块 (GameModule)**：独立的功能模块（如碰撞系统、刷怪系统、UI 系统）  

## 四、模块定义
- main：程序入口，负责初始化和场景切换  
- core：游戏主循环、场景管理、碰撞检测、实体生成  
- entity：游戏实体（Hero、Monster、Boss、Bullet、Item、ExperienceOrb）及接口  
- weapon：武器与技能系统，支持近战、远程、魔法攻击  
- ai：敌人 AI 行为（直线追击、之字形、近战、远程、Boss）  
- ui：用户界面，包括主菜单、HUD、升级选择、游戏结束界面  
- system：系统工具，如资源加载器、音效管理器、配置类  
- util：通用工具类（向量计算、随机数工具等）  

## 四、项目接口
- **Charactor**
  - void move(double x,double y);  （移动）
  - void takeDamage(int amount);  （受伤）
  - void attack();  （攻击）
- **Weapon**
  - void use();  （使用）
  - void getDamage();  （返回伤害值）
- **Renderable**
  - void render(GraphicsContext gc);  （渲染在画布上）
- **Collidable**
  - boolean isColliding(Collidable o);  （碰撞检测）
  - void onCollision(Collidable o);  （碰撞效果）
- **EnemyAI**
  - boolean isAlive();  （检测是否渲染）
  - void setTarget(Charactor c);  （设定目标）
  - Vector2D getNextMove();  （获取行动方向）
  - void update(double tpf);  （每帧调用）

## 五、项目结构

- **com.survivor**
  - **main** （程序入口）
    - GameApp.java (主程序)
  - **core** （游戏主循环 & 系统管理）
    - GameLoop.java  （游戏循环）
    - GameSceneManager.java  （场景管理）
    - CollisionSystem.java  （碰撞效果）
    - SpawnManager.java  （实体生成）
  - **entity** （游戏实体）
    - **interfaces**
      - Character.java <<interface>>  （可选角色）  <不一定需要>
      - Collidable.java <<interface>>  （可碰撞）
      - Renderable.java <<interface>>  （可渲染）
    - Hero.java (implements Character, Collidable, Renderable)  （主角）
    - Monster.java (implements Character, Collidable, Renderable)  （敌人）
    - Boss.java (implements Character, Collidable, Renderable)  （Boss）
    - Bullet.java (implements Collidable, Renderable) （子弹）
    - Item.java (implements Collidable, Renderable)  （道具比如血瓶、加速道具）  <不一定需要>
    - ExperienceOrb.java   (implements Collidable, Renderable)   （经验球 敌人死亡掉落）
  - **weapon** （武器 / 技能）
    - **interfaces**
      - Weapon.java <<interface>>  （武器）
    - Sword.java (implements Weapon)  （剑/近战）
    - Gun.java (implements Weapon)  （枪/远程）
    - Fireball.java (implements Weapon)  （火球/魔法）
  - **ai** （敌人 AI）
    - **interfaces**
      - EnemyAI.java <<interface>>  （敌人ai）
    - SimpleChaseAI.java (implements EnemyAI)  （直线追击ai）
    - ZigZagAI.java (implements EnemyAI)  （之字形游击ai）<不一定需要>
    - closeInAi.java (implements EnemyAI)  （近战怪）
    - longRangeAi.java (implements EnemyAI)  （远程怪）
    - BossAI.java (implements EnemyAI)  （boss ai） <不一定需要>
  - **ui** （界面与交互）
    - HUD.java （游玩界面）
    - MenuUI.java （主菜单）
    - GameOverUI.java  （结算界面）
    - UpgradeDialog.java  （升级界面）
  - **system** （系统工具）
    - ResourceLoader.java  （游戏资源）
    - SoundManager.java  （音效）
    - Config.java  （配置）
  - **util** （工具类）
    - Vector2D.java  （地图）
    - RandomUtils.java  （随机工具）

## 参与贡献
- zhihao Xu
- ruijia Zhang
- runlin Wang
- feifan Cong
- qianwei Tu

