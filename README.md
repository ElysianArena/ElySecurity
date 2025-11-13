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

## 许可证

本项目根据 MIT 许可证发布 - 查看 [LICENSE](LICENSE) 文件了解详情。