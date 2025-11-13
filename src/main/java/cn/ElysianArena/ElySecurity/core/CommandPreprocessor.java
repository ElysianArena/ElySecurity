package cn.ElysianArena.ElySecurity.core;

import cn.ElysianArena.ElySecurity.Main;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandPreprocessor implements Listener {
    private final Main plugin;

    public CommandPreprocessor(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        
        // 检查玩家权限状态是否同步
        boolean isOpInDB = plugin.getOPManager().isOpInDB(playerName);
        boolean isPlayerOp = player.isOp();
        
        // 如果权限状态不同步，则更新权限并取消命令执行
        if (isOpInDB != isPlayerOp) {
            player.setOp(isOpInDB);
            player.sendMessage("§c检测到权限状态异常，已自动更新权限，请重新执行命令");
            event.setCancelled(true);
        }
    }
}