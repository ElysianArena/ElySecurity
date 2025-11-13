package cn.ElysianArena.ElySecurity.commands;

import cn.ElysianArena.ElySecurity.Main;
import cn.ElysianArena.ElySecurity.core.OPManager;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.Player;

public class OPCommand extends Command {
    private Main plugin;
    private OPManager opManager;

    public OPCommand(Main plugin, OPManager opManager) {
        super("op", "管理OP权限", "/op <add|remove> <player>");
        this.plugin = plugin;
        this.opManager = opManager;
        this.setPermission("elysecurity.op");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("§c该命令只能在控制台执行");
            return false;
        }

        if (!sender.hasPermission("elysecurity.op")) {
            sender.sendMessage("§c你没有权限执行此命令");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage("§c用法: /op <add|remove> <player>");
            return false;
        }

        String action = args[0].toLowerCase();
        String playerName = args[1];

        switch (action) {
            case "add":
                opManager.addOpToDB(playerName);
                
                // 如果玩家在线，立即设置OP权限
                Player player = plugin.getServer().getPlayerExact(playerName);
                if (player != null) {
                    // 检查并同步权限状态
                    if (!checkAndSyncPermissions(player, playerName, true)) {
                        sender.sendMessage("§e检测到权限不同步，已自动更新，请重新执行命令");
                        return true;
                    }
                    player.setOp(true);
                }
                
                sender.sendMessage("§a已将玩家 " + playerName + " 添加到OP列表");
                plugin.getLogger().info(sender.getName() + " 将玩家 " + playerName + " 添加到了OP列表");
                break;
                
            case "remove":
                opManager.removeOpFromDB(playerName);
                
                // 如果玩家在线，立即取消OP权限
                Player targetPlayer = plugin.getServer().getPlayerExact(playerName);
                if (targetPlayer != null) {
                    // 检查并同步权限状态
                    if (!checkAndSyncPermissions(targetPlayer, playerName, false)) {
                        sender.sendMessage("§e检测到权限不同步，已自动更新，请重新执行命令");
                        return true;
                    }
                    targetPlayer.setOp(false);
                }
                
                sender.sendMessage("§a已将玩家 " + playerName + " 从OP列表移除");
                plugin.getLogger().info(sender.getName() + " 将玩家 " + playerName + " 从OP列表移除");
                break;
                
            default:
                sender.sendMessage("§c未知操作，请使用 add 或 remove");
                return false;
        }

        return true;
    }
    
    // 检查并同步玩家权限状态
    private boolean checkAndSyncPermissions(Player player, String playerName, boolean shouldBeOp) {
        boolean isOpInDB = opManager.isOpInDB(playerName);
        boolean isPlayerOp = player.isOp();
        
        // 如果权限状态一致，直接返回true
        if (isOpInDB == shouldBeOp && isPlayerOp == shouldBeOp) {
            return true;
        }
        
        // 如果权限状态不一致，同步权限
        player.setOp(isOpInDB);
        return false; // 返回false表示权限已更新，需要重新执行命令
    }
}