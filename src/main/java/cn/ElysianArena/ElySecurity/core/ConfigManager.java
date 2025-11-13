package cn.ElysianArena.ElySecurity.core;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private final PluginBase plugin;
    private Config config;
    private Config languageConfig;
    private Config prohibitedWordsConfig;

    public ConfigManager(PluginBase plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        saveDefaultConfigFiles();

        this.config = new Config(new File(plugin.getDataFolder(), "config.yml"), Config.YAML);
        String language = config.getString("language", "zh_CN");
        File languageFile = new File(plugin.getDataFolder(), "lang/" + language + ".yml");
        if (languageFile.exists()) {
            this.languageConfig = new Config(languageFile, Config.YAML);
        } else {
            this.languageConfig = new Config(new File(plugin.getDataFolder(), "lang/zh_CN.yml"), Config.YAML);
        }
        this.prohibitedWordsConfig = new Config(new File(plugin.getDataFolder(), "prohibited-words.yml"), Config.YAML);
        saveDefaultConfigs();
    }

    private void saveDefaultConfigFiles() {
        saveResourceIfNotExists("config.yml", "config.yml");
        saveResourceIfNotExists("lang/zh_CN.yml", "lang/zh_CN.yml");
        saveResourceIfNotExists("lang/en_US.yml", "lang/en_US.yml");
        saveResourceIfNotExists("prohibited-words.yml", "prohibited-words.yml");
    }

    private void saveResourceIfNotExists(String resourcePath, String outputPath) {
        File outFile = new File(plugin.getDataFolder(), outputPath);
        if (!outFile.exists()) {
            try (InputStream inputStream = plugin.getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (inputStream != null) {
                    outFile.getParentFile().mkdirs();
                    Files.copy(inputStream, outFile.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().warning("无法保存资源文件: " + resourcePath + " - " + e.getMessage());
            }
        }
    }

    private void saveDefaultConfigs() {
        setDefaultIfNotExists(config, "language", "zh_CN");
        setDefaultIfNotExists(config, "anti-spam.enabled", true);
        setDefaultIfNotExists(config, "anti-spam.message-interval", 1000);
        setDefaultIfNotExists(config, "anti-spam.similarity-threshold", 0.8);
        setDefaultIfNotExists(config, "anti-spam.entropy-threshold", 1.2);
        setDefaultIfNotExists(config, "anti-spam.cache-size", 10);
        setDefaultIfNotExists(config, "anti-spam.punishment", "kick");
        setDefaultIfNotExists(config, "anti-spam.warning-message", true);
        setDefaultIfNotExists(config, "prohibited-words.enabled", true);
        setDefaultIfNotExists(config, "prohibited-words.mode", "local");
        setDefaultIfNotExists(config, "prohibited-words.chat-interception", true);
        setDefaultIfNotExists(config, "prohibited-words.punishment", "warning");
        setDefaultIfNotExists(config, "prohibited-words.mute-duration", 300);
        setDefaultIfNotExists(config, "prohibited-words.auto-add-baidu-words", false);
        setDefaultIfNotExists(config, "baidu-api.enabled", false);
        setDefaultIfNotExists(config, "baidu-api.api-key", "your_api_key_here");
        setDefaultIfNotExists(config, "baidu-api.secret-key", "your_secret_key_here");
        setDefaultIfNotExists(config, "baidu-api.strategy-id", 1);
        setDefaultIfNotExists(config, "mysql.host", "localhost");
        setDefaultIfNotExists(config, "mysql.port", 3306);
        setDefaultIfNotExists(config, "mysql.database", "elysecurity");
        setDefaultIfNotExists(config, "mysql.username", "root");
        setDefaultIfNotExists(config, "mysql.password", "");
        setDefaultIfNotExists(config, "redis.enabled", false);
        setDefaultIfNotExists(config, "redis.host", "localhost");
        setDefaultIfNotExists(config, "redis.port", 6379);
        setDefaultIfNotExists(config, "redis.password", "");
        setDefaultIfNotExists(config, "redis.timeout", 2000);
        setDefaultIfNotExists(config, "redis.max-total", 10);
        setDefaultIfNotExists(config, "redis.max-idle", 5);
        setDefaultIfNotExists(config, "redis.min-idle", 1);

        config.save();
        languageConfig.save();
        List<String> defaultWords = Arrays.asList(
                "脏话1", "脏话2", "敏感词1", "敏感词2"
        );
        setDefaultIfNotExists(prohibitedWordsConfig, "local-words", defaultWords);
        prohibitedWordsConfig.save();
    }

    private void setDefaultIfNotExists(Config config, String key, Object value) {
        if (!config.exists(key)) {
            config.set(key, value);
        }
    }

    public Config getConfig() {
        return config;
    }

    public Config getLanguageConfig() {
        return languageConfig;
    }

    public Config getProhibitedWordsConfig() {
        return prohibitedWordsConfig;
    }

    public void reloadConfigs() {
        loadConfigs();
    }
}