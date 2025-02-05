package org.example.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.handler.sEventHandler;

import java.util.UUID;

public class ResetSuspiciousScoreCommand implements CommandExecutor {
    private final sEventHandler eventHandler;

    public ResetSuspiciousScoreCommand(sEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only 4 players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("simplexraydetector.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        UUID targetPlayerId;
        String targetPlayerName;

        if (args.length == 0) {
            targetPlayerId = player.getUniqueId();
            targetPlayerName = player.getName();
        } else {
            String targetName = args[0];
            Player targetPlayer = Bukkit.getPlayer(targetName);

            if (targetPlayer == null) {
                player.sendMessage(ChatColor.RED + "Player " + targetName + " not found.");
                return true;
            }

            targetPlayerId = targetPlayer.getUniqueId();
            targetPlayerName = targetPlayer.getName();
        }

        eventHandler.resetSuspiciousScore(targetPlayerId);
        player.sendMessage(ChatColor.GREEN + "Suspicious score of player  " + targetPlayerName + " has been reset.");

        return true;
    }
}