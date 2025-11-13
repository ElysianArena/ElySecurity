# ElySecurity

ElySecurity 是一个基于 Nukkit 服务器平台的多功能安全插件，提供反垃圾信息、违禁词过滤、OP权限管理等功能，保障服务器环境的安全与秩序。

## 功能特性

### 1. 反垃圾信息 (Anti-Spam)
- 控制消息发送间隔，防止刷屏
- 检测重复或高度相似的消息
- 基于信息熵算法识别无意义内容
- 可自定义的惩罚机制（警告、踢出等）

### 2. 违禁词过滤系统
- 本地违禁词库支持
- 可选集成百度内容审核API进行云端检测
- 支持多种检测模式：
    - 仅本地检测
    - 仅百度API检测
    - 本地+百度API双重检测
- 灵活的惩罚机制（警告、禁言、踢出）
- 自动缓存机制提升性能

### 3. OP权限管理系统
- 将OP权限存储在MySQL数据库中，实现持久化管理
- 可选Redis缓存支持，提高查询效率
- 实时同步在线玩家的OP状态
- 提供 `/op` 命令用于管理OP权限

## API 文档

### 违禁词检测 API

#### 检查内容是否违规
```java
/**
 * 检查文本内容是否包含违禁词
 * @param playerName 玩家名称
 * @param content 要检查的文本内容
 * @return ViolationResult 违规检测结果
 */
public ViolationResult checkContent(String playerName, String content)
```

#### ViolationResult 类说明
```java
public class ViolationResult {
    private boolean violated;           // 是否违规
    private int violationType;         // 违规类型ID
    private int subType;               // 子类型
    private List<String> violationDetails; // 违规详情(如命中的关键词)
    private String source;             // 检测来源 local/baidu
    private double confidence;         // 置信度
    private String message;            // 附加消息
    
    // Getter 和 Setter 方法
    public boolean isViolated() { ... }
    public int getViolationType() { ... }
    public List<String> getViolationDetails() { ... }
    // ... 其他方法
}
```

#### 使用示例
```java
// 在其他插件中调用违禁词检测
Main main = Main.getInstance();
ProhibitedWords prohibitedWords = main.getProhibitedWords();

ViolationResult result = prohibitedWords.checkContent(player.getName(), message);
if (result.isViolated()) {
    // 处理违规内容
    player.sendMessage("检测到违规内容: " + result.getViolationDetails());
}
```

### 禁言管理 API

#### 禁言玩家
```java
/**
 * 禁言玩家
 * @param playerName 玩家名称
 * @param durationSeconds 禁言时长(秒)
 */
public void mutePlayer(String playerName, int durationSeconds)
```

#### 解除禁言
```java
/**
 * 解除玩家禁言
 * @param playerName 玩家名称
 */
public void unmutePlayer(String playerName)
```

#### 检查是否被禁言
```java
/**
 * 检查玩家是否被禁言
 * @param playerName 玩家名称
 * @return boolean 是否被禁言
 */
public boolean isMuted(String playerName)
```

#### 获取剩余禁言时间
```java
/**
 * 获取玩家剩余禁言时间
 * @param playerName 玩家名称
 * @return long 剩余禁言时间(秒)
 */
public long getMuteTimeLeft(String playerName)
```

### 本地违禁词管理 API

#### 添加本地违禁词
```java
/**
 * 添加本地违禁词
 * @param word 要添加的违禁词
 */
public void addLocalWord(String word)
```

#### 移除本地违禁词
```java
/**
 * 移除本地违禁词
 * @param word 要移除的违禁词
 */
public void removeLocalWord(String word)
```

#### 获取所有本地违禁词
```java
/**
 * 获取所有本地违禁词
 * @return Set<String> 违禁词集合
 */
public Set<String> getLocalWords()
```

#### 重新加载配置
```java
/**
 * 重新加载违禁词配置
 */
public void reloadWords()
```

### OP权限管理 API

#### 检查玩家OP状态
```java
/**
 * 检查玩家是否在数据库中有OP权限
 * @param username 玩家名称
 * @return boolean 是否有OP权限
 */
public boolean isOpInDB(String username)
```

#### 添加OP权限
```java
/**
 * 添加玩家OP权限到数据库
 * @param username 玩家名称
 */
public void addOpToDB(String username)
```

#### 移除OP权限
```java
/**
 * 从数据库移除玩家OP权限
 * @param username 玩家名称
 */
public void removeOpFromDB(String username)
```

#### 获取所有OP玩家
```java
/**
 * 获取数据库中所有有OP权限的玩家
 * @return Set<String> OP玩家集合
 */
public Set<String> getAllOpsFromDB()
```

#### 同步玩家OP状态
```java
/**
 * 同步所有在线玩家的OP状态
 */
public void syncPlayerOpStatus()
```

### 语言管理 API

#### 获取本地化消息
```java
/**
 * 获取本地化消息
 * @param key 消息键
 * @return String 本地化消息
 */
public String getMessage(String key)

/**
 * 获取本地化消息，带默认值
 * @param key 消息键
 * @param defaultValue 默认值
 * @return String 本地化消息
 */
public String getMessage(String key, String defaultValue)
```

#### 设置当前语言
```java
/**
 * 设置当前语言
 * @param languageCode 语言代码 (如: zh_CN, en_US)
 */
public void setCurrentLanguage(String languageCode)
```

#### 检查语言是否可用
```java
/**
 * 检查语言是否可用
 * @param languageCode 语言代码
 * @return boolean 是否可用
 */
public boolean isLanguageAvailable(String languageCode)
```

#### 重新加载语言配置
```java
/**
 * 重新加载所有语言配置
 */
public void reloadLanguages()
```

### 集成示例

#### 在其他插件中集成 ElySecurity
```java
public class YourPlugin extends PluginBase {
    
    private Main elySecurity;
    
    @Override
    public void onEnable() {
        // 获取ElySecurity实例
        elySecurity = Main.getInstance();
        
        if (elySecurity != null) {
            // 使用违禁词检测
            ProhibitedWords pw = elySecurity.getProhibitedWords();
            ViolationResult result = pw.checkContent("testPlayer", "测试消息");
            
            // 使用禁言功能
            pw.mutePlayer("badPlayer", 300); // 禁言5分钟
            
            // 使用语言管理
            String message = elySecurity.getLanguageManager().getMessage("anti_spam.warning");
        }
    }
}
```

## 配置说明

### 主配置文件 (config.yml)

```yaml
# 语言设置
language: "zh_CN"

# 反刷屏配置
anti-spam:
  enabled: true
  message-interval: 1000           # 消息最小间隔(毫秒)
  similarity-threshold: 0.8        # 相似度阈值
  entropy-threshold: 1.2           # 信息熵阈值
  cache-size: 10                   # 聊天历史缓存数量
  punishment: "warning"            # 惩罚方式: warning, kick
  warning-message: true            # 是否显示警告消息

# 违禁词配置
prohibited-words:
  enabled: true
  # 检测模式: local(仅本地), baidu(仅百度), both(两者都用)
  mode: "local"
  # 是否拦截聊天
  chat-interception: true
  # 惩罚方式: warning, kick, mute
  punishment: "warning"
  # 禁言时长(秒)
  mute-duration: 300

# 百度API配置
baidu-api:
  enabled: false
  api-key: "your_api_key_here"
  secret-key: "your_secret_key_here"
  strategy-id: 1

# MySQL数据库配置
mysql:
  host: "localhost"
  port: 3306
  database: "elysecurity"
  username: "root"
  password: ""

# Redis配置
redis:
  enabled: true
  host: "localhost"
  port: 6379
  timeout: 2000
  max-total: 10
  max-idle: 5
  min-idle: 1
```

### 违禁词配置 (prohibited-words.yml)

```yaml
# 本地违禁词列表
local-words:
  - "脏话1"
  - "脏话2"
  - "敏感词1"
  - "敏感词2"
  - "违规词1"
```

## 安装说明

1. 将编译好的插件 `.jar` 文件放入服务器的 `plugins` 目录
2. 启动服务器以生成配置文件
3. 根据需要修改 `config.yml` 和 `prohibited-words.yml` 配置文件
4. 重启服务器使配置生效

## 使用说明

### 命令

- `/op add/remove <player>` - 给予/移除玩家OP权限（控制台）

### 权限节点

- `elysecurity.op` - 使用OP相关命令的权限

![bStats](https://bstats.org/signatures/bukkit/ElySecurity.svg)

## 许可证

本项目根据 MIT 许可证发布 - 查看 [LICENSE](LICENSE) 文件了解详情。