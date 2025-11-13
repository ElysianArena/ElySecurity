package cn.ElysianArena.ElySecurity.core;

import cn.ElysianArena.ElySecurity.Main;
import cn.ElysianArena.ElySecurity.security.AntiSpam;
import cn.ElysianArena.ElySecurity.security.ProhibitedWords;
import cn.nukkit.plugin.PluginBase;

public class EventManager {
    private final PluginBase plugin;
    private final Main main;
    private CommandPreprocessor commandPreprocessor;

    public EventManager(PluginBase plugin) {
        this.plugin = plugin;
        this.main = (Main) plugin;
        this.commandPreprocessor = new CommandPreprocessor((Main) plugin);
    }

    public void registerEvents() {
        main.getAntiSpam().registerEvents();
        main.getProhibitedWords().registerEvents();
        plugin.getServer().getPluginManager().registerEvents(commandPreprocessor, plugin);
    }
}