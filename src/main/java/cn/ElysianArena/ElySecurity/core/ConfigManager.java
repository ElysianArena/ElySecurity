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
        // 创建插件数据文件夹
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // 创建lang文件夹
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // 保存默认语言文件
        saveDefaultLanguageFiles();

        // 加载主配置
        this.config = new Config(new File(plugin.getDataFolder(), "config.yml"), Config.YAML);

        // 加载语言文件
        String language = config.getString("language", "zh_CN");
        File languageFile = new File(plugin.getDataFolder(), "lang/" + language + ".yml");
        if (languageFile.exists()) {
            this.languageConfig = new Config(languageFile, Config.YAML);
        } else {
            // 如果指定的语言文件不存在，则使用默认语言
            this.languageConfig = new Config(new File(plugin.getDataFolder(), "lang/zh_CN.yml"), Config.YAML);
        }

        // 加载违禁词配置
        this.prohibitedWordsConfig = new Config(new File(plugin.getDataFolder(), "prohibited-words.yml"), Config.YAML);

        // 保存默认配置
        saveDefaultConfigs();
    }

    private void saveDefaultLanguageFiles() {
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        // 保存默认中文语言文件（如果不存在）
        File zhCNFile = new File(langDir, "zh_CN.yml");
        if (!zhCNFile.exists()) {
            saveResource("lang/zh_CN.yml", "lang/zh_CN.yml");
        }

        // 保存默认英文语言文件（如果不存在）
        File enUSFile = new File(langDir, "en_US.yml");
        if (!enUSFile.exists()) {
            saveResource("lang/en_US.yml", "lang/en_US.yml");
        }
    }

    private void saveResource(String resourcePath, String outputPath) {
        File outFile = new File(plugin.getDataFolder(), outputPath);
        if (!outFile.exists()) {
            try (InputStream inputStream = plugin.getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (inputStream != null) {
                    Files.copy(inputStream, outFile.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().warning("无法保存资源文件: " + resourcePath);
            }
        }
    }

    private void saveDefaultConfigs() {
        // 主配置默认值
        setDefaultIfNotExists(config, "language", "zh_CN");
        setDefaultIfNotExists(config, "anti-spam.enabled", true);
        setDefaultIfNotExists(config, "anti-spam.message-interval", 1000);
        setDefaultIfNotExists(config, "anti-spam.similarity-threshold", 0.8);
        setDefaultIfNotExists(config, "anti-spam.entropy-threshold", 1.2); // 已修复
        setDefaultIfNotExists(config, "anti-spam.cache-size", 10);
        setDefaultIfNotExists(config, "anti-spam.punishment", "kick");
        setDefaultIfNotExists(config, "anti-spam.warning-message", true);

        // 违禁词配置默认值
        setDefaultIfNotExists(config, "prohibited-words.enabled", true);
        setDefaultIfNotExists(config, "prohibited-words.mode", "local");
        setDefaultIfNotExists(config, "prohibited-words.chat-interception", true);
        setDefaultIfNotExists(config, "prohibited-words.punishment", "warning");
        setDefaultIfNotExists(config, "prohibited-words.mute-duration", 300);
        setDefaultIfNotExists(config, "baidu-api.enabled", false);
        setDefaultIfNotExists(config, "baidu-api.api-key", "your_api_key_here");
        setDefaultIfNotExists(config, "baidu-api.secret-key", "your_secret_key_here");
        setDefaultIfNotExists(config, "baidu-api.strategy-id", 1);
        config.save();

        // 语言文件默认值 - 现在由LanguageManager处理
        languageConfig.save();

        // 违禁词文件默认值
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