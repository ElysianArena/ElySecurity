package cn.ElysianArena.ElySecurity.security;

import cn.ElysianArena.ElySecurity.Main;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;

import java.util.*;

public class AntiSpam implements SecurityModule, Listener {
    private final Main plugin;
    private final ChatAnalyzer chatAnalyzer;

    // 玩家聊天记录缓存
    private final Map<String, LinkedList<String>> playerChatHistory;
    private final Map<String, Long> lastMessageTime;

    public AntiSpam(Main plugin) {
        this.plugin = plugin;
        this.chatAnalyzer = new ChatAnalyzer();
        this.playerChatHistory = new HashMap<>();
        this.lastMessageTime = new HashMap<>();
    }

    @Override
    public void onEnable() {
        plugin.getLogger().info("反刷屏模块已启用");
    }

    @Override
    public void onDisable() {
        playerChatHistory.clear();
        lastMessageTime.clear();
    }

    @Override
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getModuleName() {
        return "AntiSpam";
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) return;

        String playerName = event.getPlayer().getName();
        String message = event.getMessage().trim();

        if (checkSpam(playerName, message)) {
            event.setCancelled(true);
            handleSpamPunishment(event.getPlayer());
        }
    }

    private boolean checkSpam(String playerName, String message) {
        // 检查消息间隔
        if (checkMessageInterval(playerName)) {
            if (plugin.getConfigManager().getConfig().getBoolean("anti-spam.warning-message", true)) {
                plugin.getLanguageManager().getMessage("anti_spam.warning");
            }
            return true;
        }

        // 检查信息熵
        if (checkEntropy(message)) {
            if (plugin.getConfigManager().getConfig().getBoolean("anti-spam.warning-message", true)) {
                plugin.getLanguageManager().getMessage("anti_spam.warning");
            }
            return true;
        }

        // 检查相似度
        if (checkSimilarity(playerName, message)) {
            if (plugin.getConfigManager().getConfig().getBoolean("anti-spam.warning-message", true)) {
                plugin.getLanguageManager().getMessage("anti_spam.similarity_warning");
            }
            return true;
        }

        // 更新聊天记录
        updateChatHistory(playerName, message);
        lastMessageTime.put(playerName, System.currentTimeMillis());

        return false;
    }

    private boolean checkMessageInterval(String playerName) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastMessageTime.get(playerName);

        if (lastTime != null) {
            long interval = plugin.getConfigManager().getConfig().getLong("anti-spam.message-interval", 1000);
            if (currentTime - lastTime < interval) {
                return true;
            }
        }
        return false;
    }

    private boolean checkEntropy(String message) {
        if (message.length() < 3) return false;

        double entropyThreshold = plugin.getConfigManager().getConfig().getDouble("anti-spam.entropy-threshold", 1.2);
        double entropy = chatAnalyzer.calculateAverageEntropy(message);

        return entropy < entropyThreshold;
    }

    private boolean checkSimilarity(String playerName, String message) {
        LinkedList<String> history = playerChatHistory.get(playerName);
        if (history == null || history.isEmpty()) return false;

        double similarityThreshold = plugin.getConfigManager().getConfig().getDouble("anti-spam.similarity-threshold", 0.8);

        for (String prevMessage : history) {
            double similarity = chatAnalyzer.calculateSimilarity(message, prevMessage);
            if (similarity > similarityThreshold) {
                return true;
            }
        }
        return false;
    }

    private void updateChatHistory(String playerName, String message) {
        LinkedList<String> history = playerChatHistory.computeIfAbsent(playerName, k -> new LinkedList<>());
        int cacheSize = plugin.getConfigManager().getConfig().getInt("anti-spam.cache-size", 10);

        history.addFirst(message);
        while (history.size() > cacheSize) {
            history.removeLast();
        }
    }

    private void handleSpamPunishment(cn.nukkit.Player player) {
        String punishment = plugin.getConfigManager().getConfig().getString("anti-spam.punishment", "warning");

        switch (punishment.toLowerCase()) {
            case "kick":
                player.kick(plugin.getLanguageManager().getMessage("anti_spam.kick_message"));
                break;
            case "warning":
            default:
                // 警告消息已经在checkSpam方法中发送
                break;
        }
    }
}