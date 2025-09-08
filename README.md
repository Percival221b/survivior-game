# Survivor Game

## 一、项目名称
Survivor（暂定）

## 二、开发工具
- JDK 21  
- FXGL 17.3  
- Maven 3.9.9  
- Maven Plugin 0.0.8

## 三、项目结构

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
    - close-inAi.java (implements EnemyAI)  （近战怪）
    - long-rangeAi.java (implements EnemyAI)  （远程怪）
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
