package cn.ElysianArena.ElySecurity.core;

import cn.ElysianArena.ElySecurity.Main;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final PluginBase plugin;
    private final ConfigManager configManager;
    private Map<String, Config> languageConfigs;
    private String currentLanguage;

    public LanguageManager(PluginBase plugin) {
        this.plugin = plugin;
        this.configManager = ((Main) plugin).getConfigManager();
        this.languageConfigs = new HashMap<>();
        loadLanguages();
    }

    private void loadLanguages() {
        // 加载所有语言文件
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        // 默认语言
        currentLanguage = configManager.getConfig().getString("language", "zh_CN");
        
        // 加载所有语言配置文件
        File[] langFiles = langDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File langFile : langFiles) {
                String langCode = langFile.getName().replace(".yml", "");
                Config langConfig = new Config(langFile, Config.YAML);
                languageConfigs.put(langCode, langConfig);
            }
        }
        
        // 确保至少有默认语言
        if (!languageConfigs.containsKey(currentLanguage)) {
            languageConfigs.put(currentLanguage, configManager.getLanguageConfig());
        }
    }

    public String getMessage(String key) {
        return getMessage(key, "&cMessage not found: " + key);
    }

    public String getMessage(String key, String defaultValue) {
        Config langConfig = languageConfigs.get(currentLanguage);
        if (langConfig == null) {
            // 如果当前语言不存在，使用默认语言
            langConfig = languageConfigs.get("zh_CN");
            if (langConfig == null && !languageConfigs.isEmpty()) {
                // 如果中文也不存在且有其他语言，使用第一个找到的语言
                langConfig = languageConfigs.values().iterator().next();
            }
        }
        
        String message = defaultValue;
        if (langConfig != null) {
            message = langConfig.getString(key, defaultValue);
        }
        
        return TextFormat.colorize('&', message);
    }
    
    public void setCurrentLanguage(String languageCode) {
        this.currentLanguage = languageCode;
    }
    
    public String getCurrentLanguage() {
        return this.currentLanguage;
    }
    
    public boolean isLanguageAvailable(String languageCode) {
        return languageConfigs.containsKey(languageCode);
    }
    
    public void reloadLanguages() {
        languageConfigs.clear();
        loadLanguages();
    }
}