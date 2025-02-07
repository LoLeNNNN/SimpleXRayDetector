package org.example.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.handler.sEventHandler;
import org.example.handler.LocaleManager;

import java.util.UUID;

public class LastDiamondCommand implements CommandExecutor {
    private final sEventHandler eventHandler;
    private final LocaleManager localeManager;

    public LastDiamondCommand(sEventHandler eventHandler, LocaleManager localeManager) {
        this.eventHandler = eventHandler;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is available only for players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("simplexraydetector.admin")) {
            player.sendMessage(localeManager.getMessage(player, "no_permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(localeManager.getMessage(player, "usage_lastdiamond"));
            return true;
        }

        String targetPlayerName = args[0];
        UUID targetPlayerId = Bukkit.getOfflinePlayer(targetPlayerName).getUniqueId();

        long lastDiamondTime = eventHandler.getLastDiamondTime(targetPlayerId);

        if (lastDiamondTime == 0) {
            player.sendMessage(localeManager.getMessage(player, "no_diamonds_mined", targetPlayerName));
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastDiamond = currentTime - lastDiamondTime;

        long minutes = timeSinceLastDiamond / (60 * 1000);
        long seconds = (timeSinceLastDiamond % (60 * 1000)) / 1000;

        player.sendMessage(localeManager.getMessage(player, "last_diamond_time", minutes, seconds));

        return true;
    }
}