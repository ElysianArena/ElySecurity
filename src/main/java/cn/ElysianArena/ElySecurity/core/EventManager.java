package cn.ElysianArena.ElySecurity.core;

import cn.ElysianArena.ElySecurity.Main;
import cn.ElysianArena.ElySecurity.security.AntiSpam;
import cn.ElysianArena.ElySecurity.security.ProhibitedWords;
import cn.nukkit.plugin.PluginBase;

public class EventManager {
    private final PluginBase plugin;
    private final AntiSpam antiSpam;
    private final ProhibitedWords prohibitedWords;
    private CommandPreprocessor commandPreprocessor; // 新增

    public EventManager(PluginBase plugin) {
        this.plugin = plugin;
        this.antiSpam = ((Main) plugin).getAntiSpam();
        this.prohibitedWords = ((Main) plugin).getProhibitedWords();
        this.commandPreprocessor = new CommandPreprocessor((Main) plugin); // 新增
    }

    public void registerEvents() {
        // 注册反刷屏事件
        antiSpam.registerEvents();
        // 注册违禁词检测事件
        prohibitedWords.registerEvents();
        // 注册命令预处理事件
        plugin.getServer().getPluginManager().registerEvents(commandPreprocessor, plugin);
    }
}