package cn.ElysianArena.ElySecurity.core;

import cn.ElysianArena.ElySecurity.Main;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class OPManager {
    private Main plugin;
    private Connection dbConnection;
    private JedisPool jedisPool;
    private OPTask opTask;
    // MySQL 配置
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    
    // Redis 配置
    private boolean redisEnabled;
    private String redisHost;
    private int redisPort;
    private int redisTimeout;
    private int redisMaxTotal;
    private int redisMaxIdle;
    private int redisMinIdle;

    public OPManager(Main plugin) {
        this.plugin = plugin;
        // 从配置加载MySQL设置
        loadMySQLConfig();
        // 从配置加载Redis设置
        loadRedisConfig();
        initDatabase();
        initRedis();
        startOPTask();
    }

    private void loadMySQLConfig() {
        this.host = plugin.getConfig().getString("mysql.host", "localhost");
        this.port = plugin.getConfig().getInt("mysql.port", 3306);
        this.database = plugin.getConfig().getString("mysql.database", "elysecurity");
        this.username = plugin.getConfig().getString("mysql.username", "root");
        this.password = plugin.getConfig().getString("mysql.password", "");
    }
    
    private void loadRedisConfig() {
        this.redisEnabled = plugin.getConfig().getBoolean("redis.enabled", true);
        this.redisHost = plugin.getConfig().getString("redis.host", "localhost");
        this.redisPort = plugin.getConfig().getInt("redis.port", 6379);
        this.redisTimeout = plugin.getConfig().getInt("redis.timeout", 2000);
        this.redisMaxTotal = plugin.getConfig().getInt("redis.max-total", 10);
        this.redisMaxIdle = plugin.getConfig().getInt("redis.max-idle", 5);
        this.redisMinIdle = plugin.getConfig().getInt("redis.min-idle", 1);
    }

    private void initDatabase() {
        try {
            // 创建MySQL数据库连接
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            this.dbConnection = DriverManager.getConnection(url, username, password);
            
            // 创建op列表表
            Statement stmt = dbConnection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS ops (username VARCHAR(50) PRIMARY KEY)");
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().error("初始化数据库失败", e);
        }
    }

    private void initRedis() {
        // 如果Redis被禁用，则不初始化
        if (!redisEnabled) {
            this.jedisPool = null;
            plugin.getLogger().info("Redis功能已被禁用");
            return;
        }
        
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(redisMaxTotal);
            config.setMaxIdle(redisMaxIdle);
            config.setMinIdle(redisMinIdle);
            // 使用从配置文件读取的Redis配置
            this.jedisPool = new JedisPool(config, redisHost, redisPort, redisTimeout);
            
            // 测试连接
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
                plugin.getLogger().info("成功连接到Redis服务器");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("无法连接到Redis服务器，将直接使用数据库");
            this.jedisPool = null;
        }
    }

    private void startOPTask() {
        this.opTask = new OPTask(this);
        plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, opTask, 10);
    }

    public void stopOPTask() {
        if (opTask != null) {
            plugin.getServer().getScheduler().cancelTask(opTask.getTaskId());
        }
    }

    public boolean isOpInDB(String username) {
        // 先尝试从Redis获取
        if (jedisPool != null) {
            try (Jedis jedis = jedisPool.getResource()) {
                return "1".equals(jedis.hget("ops", username));
            } catch (Exception e) {
                plugin.getLogger().warning("Redis查询失败，回退到数据库查询");
            }
        }
        
        // 从数据库获取
        try {
            PreparedStatement stmt = dbConnection.prepareStatement("SELECT 1 FROM ops WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            boolean result = rs.next();
            rs.close();
            stmt.close();
            
            // 更新Redis缓存
            if (jedisPool != null) {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.hset("ops", username, result ? "1" : "0");
                } catch (Exception e) {
                    plugin.getLogger().warning("无法更新Redis缓存");
                }
            }
            
            return result;
        } catch (SQLException e) {
            plugin.getLogger().error("查询数据库失败", e);
            return false;
        }
    }

    public void addOpToDB(String username) {
        try {
            PreparedStatement stmt = dbConnection.prepareStatement("INSERT IGNORE INTO ops (username) VALUES (?)");
            stmt.setString(1, username);
            stmt.executeUpdate();
            stmt.close();
            
            // 更新Redis缓存
            if (jedisPool != null) {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.hset("ops", username, "1");
                } catch (Exception e) {
                    plugin.getLogger().warning("无法更新Redis缓存");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().error("添加OP到数据库失败", e);
        }
    }

    public void removeOpFromDB(String username) {
        try {
            PreparedStatement stmt = dbConnection.prepareStatement("DELETE FROM ops WHERE username = ?");
            stmt.setString(1, username);
            stmt.executeUpdate();
            stmt.close();
            
            // 更新Redis缓存
            if (jedisPool != null) {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.hdel("ops", username);
                } catch (Exception e) {
                    plugin.getLogger().warning("无法更新Redis缓存");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().error("从数据库移除OP失败", e);
        }
    }

    public Set<String> getAllOpsFromDB() {
        Set<String> ops = new HashSet<>();
        try {
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT username FROM ops");
            while (rs.next()) {
                ops.add(rs.getString("username"));
            }
            rs.close();
            stmt.close();
            
            // 更新Redis缓存
            if (jedisPool != null) {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.del("ops");
                    for (String op : ops) {
                        jedis.hset("ops", op, "1");
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("无法更新Redis缓存");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().error("从数据库获取所有OP失败", e);
        }
        return ops;
    }

    public void syncPlayerOpStatus() {
        for (Player player : plugin.getServer().getOnlinePlayers().values()) {
            String username = player.getName();
            boolean isOpInDB = isOpInDB(username);
            boolean isPlayerOp = player.isOp();

            if (isOpInDB && !isPlayerOp) {
                // 数据库中有OP权限但玩家没有，给予权限
                player.setOp(true);
                plugin.getLogger().info("为玩家 " + username + " 添加了OP权限");
            } else if (!isOpInDB && isPlayerOp) {
                // 玩家有OP权限但数据库中没有，移除权限
                player.setOp(false);
                plugin.getLogger().info("移除了玩家 " + username + " 的OP权限");
            }
        }
    }

    public void closeConnections() {
        stopOPTask();
        
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().error("关闭数据库连接失败", e);
        }
        
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

    private static class OPTask extends PluginTask<Main> {
        private OPManager opManager;

        public OPTask(OPManager opManager) {
            super(opManager.plugin);
            this.opManager = opManager;
        }

        @Override
        public void onRun(int currentTick) {
            opManager.syncPlayerOpStatus();
        }
    }
}