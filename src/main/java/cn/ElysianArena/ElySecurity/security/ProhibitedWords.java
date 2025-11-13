package cn.ElysianArena.ElySecurity.security;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.plugin.PluginBase;
import cn.ElysianArena.ElySecurity.Main;
import cn.ElysianArena.ElySecurity.utils.BaiduContentModeration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProhibitedWords implements SecurityModule, Listener {
    private final Main plugin;
    private final BaiduContentModeration baiduModeration;

    private final Set<String> localProhibitedWords;
    private final Map<String, Long> mutedPlayers;
    private final Map<String, CacheResult> cache;

    public ProhibitedWords(Main plugin) {
        this.plugin = plugin;
        this.baiduModeration = new BaiduContentModeration(plugin);
        this.localProhibitedWords = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.mutedPlayers = new ConcurrentHashMap<>();
        this.cache = new ConcurrentHashMap<>();

        loadLocalWords();
    }

    @Override
    public void onEnable() {
        plugin.getLogger().info("违禁词检测模块已启用");

        // 测试百度API连接（如果启用）
        if (plugin.getConfigManager().getConfig().getBoolean("baidu-api.enabled", false)) {
            plugin.getServer().getScheduler().scheduleTask(plugin, () -> {
                if (baiduModeration.testConnection()) {
                    plugin.getLogger().info("百度API连接测试成功");
                } else {
                    plugin.getLogger().warning("百度API连接测试失败，将仅使用本地词库");
                }
            }, true);
        }
    }

    @Override
    public void onDisable() {
        mutedPlayers.clear();
        cache.clear();
    }

    @Override
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getModuleName() {
        return "ProhibitedWords";
    }

    // 加载本地违禁词
    private void loadLocalWords() {
        localProhibitedWords.clear();
        List<String> words = plugin.getConfigManager().getProhibitedWordsConfig().getStringList("local-words");
        localProhibitedWords.addAll(words);
        plugin.getLogger().info("已加载 " + words.size() + " 个本地违禁词");
    }

    // 重新加载违禁词
    public void reloadWords() {
        loadLocalWords();
        cache.clear();
        plugin.getLogger().info("违禁词配置已重新加载");
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) return;

        String playerName = event.getPlayer().getName();
        String message = event.getMessage().trim();

        // 检查是否被禁言
        if (isMuted(playerName)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("prohibited_words.muted"));
            return;
        }

        // 检查违禁词
        ViolationResult result = checkContent(playerName, message);
        if (result.isViolated()) {
            event.setCancelled(true);
            handleViolationPunishment(event.getPlayer(), result);
        }
    }

    // 检查内容是否违规 - 公开API供其他插件调用
    public ViolationResult checkContent(String playerName, String content) {
        // 检查缓存
        String cacheKey = playerName + ":" + content.hashCode();
        CacheResult cached = cache.get(cacheKey);
        if (cached != null && System.currentTimeMillis() - cached.timestamp < 60000) {
            return cached.result;
        }

        ViolationResult result = new ViolationResult();
        String mode = plugin.getConfigManager().getConfig().getString("prohibited-words.mode", "local");
        boolean baiduEnabled = plugin.getConfigManager().getConfig().getBoolean("baidu-api.enabled", false);

        // 本地词库检测
        if (mode.equals("local") || mode.equals("both")) {
            LocalViolation localResult = checkLocalWords(content);
            if (localResult.violated) {
                result.setViolated(true);
                result.setViolationType(1000); // 本地违禁词类型
                result.setSubType(0);
                result.setViolationDetails(localResult.violatedWords);
                result.setSource("local");
                result.setConfidence(1.0);
                result.setMessage("命中本地违禁词");
            }
        }

        // 百度API检测 (异步) - 只有在本地检测未违规或both模式时才调用
        if ((mode.equals("baidu") || mode.equals("both")) && baiduEnabled && !result.isViolated()) {
            BaiduViolation baiduResult = baiduModeration.checkContent(content);
            if (baiduResult.violated) {
                result.setViolated(true);
                result.setViolationType(baiduResult.type);
                result.setSubType(baiduResult.subType);
                result.setViolationDetails(baiduResult.violatedWords);
                result.setSource("baidu");
                result.setConfidence(baiduResult.confidence);
                result.setMessage(baiduResult.message);
                
                // 如果启用了自动添加百度违禁词到本地词库
                if (plugin.getConfigManager().getConfig().getBoolean("prohibited-words.auto-add-baidu-words", false)) {
                    for (String word : baiduResult.violatedWords) {
                        if (!localProhibitedWords.contains(word)) {
                            addLocalWord(word);
                        }
                    }
                }
            }
        }

        // 缓存结果
        cache.put(cacheKey, new CacheResult(result, System.currentTimeMillis()));
        return result;
    }

    // 检查本地违禁词
    private LocalViolation checkLocalWords(String content) {
        LocalViolation result = new LocalViolation();
        List<String> violatedWords = new ArrayList<>();

        for (String word : localProhibitedWords) {
            if (content.contains(word)) {
                violatedWords.add(word);
            }
        }

        result.violated = !violatedWords.isEmpty();
        result.violatedWords = violatedWords;
        return result;
    }

    // 处理违规惩罚
    private void handleViolationPunishment(cn.nukkit.Player player, ViolationResult result) {
        String punishment = plugin.getConfigManager().getConfig().getString("prohibited-words.punishment", "warning");

        switch (punishment.toLowerCase()) {
            case "kick":
                player.kick(plugin.getLanguageManager().getMessage("prohibited_words.kick_message"));
                break;
            case "mute":
                int duration = plugin.getConfigManager().getConfig().getInt("prohibited-words.mute-duration", 300);
                mutePlayer(player.getName(), duration);
                String muteMessage = plugin.getLanguageManager().getMessage("prohibited_words.mute_message")
                        .replace("{duration}", String.valueOf(duration));
                player.sendMessage(muteMessage);
                break;
            case "warning":
            default:
                player.sendMessage(plugin.getLanguageManager().getMessage("prohibited_words.warning"));
                break;
        }

        // 记录日志
        plugin.getLogger().warning("玩家 " + player.getName() + " 发送违规内容: " +
                result.getViolationDetails() + " 来源: " + result.getSource() +
                " 类型: " + result.getViolationType());
    }

    // 其他方法保持不变...
    public void mutePlayer(String playerName, int durationSeconds) {
        long muteUntil = System.currentTimeMillis() + (durationSeconds * 1000L);
        mutedPlayers.put(playerName, muteUntil);
    }

    public void unmutePlayer(String playerName) {
        mutedPlayers.remove(playerName);
    }

    public boolean isMuted(String playerName) {
        Long muteUntil = mutedPlayers.get(playerName);
        if (muteUntil == null) return false;

        if (System.currentTimeMillis() > muteUntil) {
            mutedPlayers.remove(playerName);
            return false;
        }
        return true;
    }

    public long getMuteTimeLeft(String playerName) {
        Long muteUntil = mutedPlayers.get(playerName);
        if (muteUntil == null) return 0;

        long timeLeft = (muteUntil - System.currentTimeMillis()) / 1000;
        return Math.max(0, timeLeft);
    }

    public void addLocalWord(String word) {
        localProhibitedWords.add(word);
        List<String> words = new ArrayList<>(localProhibitedWords);
        plugin.getConfigManager().getProhibitedWordsConfig().set("local-words", words);
        plugin.getConfigManager().getProhibitedWordsConfig().save();
        cache.clear();
    }

    public void removeLocalWord(String word) {
        localProhibitedWords.remove(word);
        List<String> words = new ArrayList<>(localProhibitedWords);
        plugin.getConfigManager().getProhibitedWordsConfig().set("local-words", words);
        plugin.getConfigManager().getProhibitedWordsConfig().save();
        cache.clear();
    }

    public Set<String> getLocalWords() {
        return Collections.unmodifiableSet(localProhibitedWords);
    }

    // 内部类
    private static class LocalViolation {
        boolean violated;
        List<String> violatedWords;
    }

    private static class CacheResult {
        ViolationResult result;
        long timestamp;

        CacheResult(ViolationResult result, long timestamp) {
            this.result = result;
            this.timestamp = timestamp;
        }
    }
}