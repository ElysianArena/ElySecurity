package cn.ElysianArena.ElySecurity;

import cn.ElysianArena.ElySecurity.core.ConfigManager;
import cn.ElysianArena.ElySecurity.core.EventManager;
import cn.ElysianArena.ElySecurity.core.LanguageManager;
import cn.ElysianArena.ElySecurity.commands.OPCommand;
import cn.ElysianArena.ElySecurity.core.OPManager;
import cn.ElysianArena.ElySecurity.security.AntiSpam;
import cn.ElysianArena.ElySecurity.security.ProhibitedWords;
import cn.ElysianArena.ElySecurity.utils.MetricsLite;
import cn.nukkit.plugin.PluginBase;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Main extends PluginBase {
    private static Main instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private EventManager eventManager;
    private AntiSpam antiSpam;
    private ProhibitedWords prohibitedWords;
    private OPManager opManager;

    @Override
    public void onLoad() {
        instance = this;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            getLogger().warning("未能加载MySQL驱动: " + e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.eventManager = new EventManager(this);
        this.antiSpam = new AntiSpam(this);
        this.prohibitedWords = new ProhibitedWords(this);
        this.opManager = new OPManager(this);

        eventManager.registerEvents();

        getServer().getCommandMap().register("op", new OPCommand(this, opManager));

        try {
            new MetricsLite(this, 27970);
        } catch (Throwable ignore) { }

        getLogger().info(" " +
                "  _____ _       ____                       _ _         \n" +
                " | ____| |_   _/ ___|  ___  ___ _   _ _ __(_) |_ _   _ \n" +
                " |  _| | | | | \\___ \\ / _ \\/ __| | | | '__| | __| | | |\n" +
                " | |___| | |_| |___) |  __/ (__| |_| | |  | | |_| |_| |\n" +
                " |_____|_|\\__, |____/ \\___|\\___|\\__,_|_|  |_|\\__|\\__, |\n" +
                "          |___/                                  |___/ " +
                "Author: NemoCat");
    }

    @Override
    public void onDisable() {
        if (opManager != null) {
            opManager.closeConnections();
        }
        getLogger().info(" " +
                "  _____ _       ____                       _ _         \n" +
                " | ____| |_   _/ ___|  ___  ___ _   _ _ __(_) |_ _   _ \n" +
                " |  _| | | | | \\___ \\ / _ \\/ __| | | | '__| | __| | | |\n" +
                " | |___| | |_| |___) |  __/ (__| |_| | |  | | |_| |_| |\n" +
                " |_____|_|\\__, |____/ \\___|\\___|\\__,_|_|  |_|\\__|\\__, |\n" +
                "          |___/                                  |___/ ");
        getLogger().info("ElySecurity 安全插件已禁用!");
    }

    public static Main getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public AntiSpam getAntiSpam() {
        return antiSpam;
    }

    public ProhibitedWords getProhibitedWords() {
        return prohibitedWords;
    }
    
    public OPManager getOPManager() {
        return opManager;
    }
}