# survivior-game
##一、项目名称：Survivor（暂定）
##二、开发工具
1.Jdk（版本21）
2.FXGL模型（版本17.3）
3.maven（版本3.9.9），maven-plugin（版本0.0.8）
......
##三、项目结构
com.survivor
│
├─ main                # 程序入口
│   └─ GameApp.java
│
├─ core                # 游戏主循环 & 系统管理
│   ├─ GameLoop.java
│   ├─ GameSceneManager.java
│   ├─ CollisionSystem.java
│   └─ SpawnManager.java
│
├─ entity              # 游戏实体
│   ├─ interfaces
│   │   ├─ Character.java       <<interface>>
│   │   ├─ Collidable.java      <<interface>>
│   │   └─ Renderable.java      <<interface>>
│   │
│   ├─ Hero.java                (implements Character, Collidable, Renderable)
│   ├─ Monster.java              (implements Character, Collidable, Renderable)
│   ├─ Boss.java                 (implements Character, Collidable, Renderable)
│   ├─ Bullet.java               (implements Collidable, Renderable)
│   └─ Item.java                 (implements Collidable, Renderable)
│
├─ weapon              # 武器/技能
│   ├─ interfaces
│   │   └─ Weapon.java          <<interface>>
│   │
│   ├─ Sword.java               (implements Weapon)
│   ├─ Gun.java                 (implements Weapon)
│   └─ Fireball.java            (implements Weapon)
│
├─ ai                  # 敌人 AI
│   ├─ interfaces
│   │   └─ EnemyAI.java         <<interface>>
│   │
│   ├─ SimpleChaseAI.java       (implements EnemyAI)
│   ├─ ZigZagAI.java            (implements EnemyAI)
│   └─ BossAI.java              (implements EnemyAI)
│
├─ ui                  # 界面与交互
│   ├─ HUD.java
│   ├─ MenuUI.java
│   └─ UpgradeDialog.java
│
├─ system              # 系统工具
│   ├─ ResourceLoader.java
│   ├─ SoundManager.java
│   └─ Config.java
│
└─ util                # 工具类
    ├─ Vector2D.java
    └─ RandomUtils.java

##四、项目接口
