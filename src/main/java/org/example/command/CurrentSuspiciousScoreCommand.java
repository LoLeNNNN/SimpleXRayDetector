package org.example.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.handler.sEventHandler;

import java.util.UUID;

public class CurrentSuspiciousScoreCommand implements CommandExecutor {
    private final sEventHandler eventHandler;
    public CurrentSuspiciousScoreCommand(sEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only 4 players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            UUID playerId = player.getUniqueId();
            long score = eventHandler.getCurrentSuspiciousScore(playerId);
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (!player.hasPermission("simplexraydetector.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        String targetPlayerName = args[0];

        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        long score;

        if (targetPlayer != null) {
            score = eventHandler.getCurrentSuspiciousScore(targetPlayer.getUniqueId());
        } else {
            score = eventHandler.getOfflineSuspiciousScore(targetPlayerName);

            if (score == -1) {
                player.sendMessage(ChatColor.RED + "Player" + targetPlayerName + " not found.");
                return true;
            }
        }

        player.sendMessage(ChatColor.DARK_GREEN + "Suspicious score of player  " + targetPlayerName + ": " + score);

        return true;
    }
}