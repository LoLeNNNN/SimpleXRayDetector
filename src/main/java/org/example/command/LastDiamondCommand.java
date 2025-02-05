package org.example.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.handler.sEventHandler;

import java.util.UUID;

public class LastDiamondCommand implements CommandExecutor {
    private final sEventHandler eventHandler;

    public LastDiamondCommand(sEventHandler eventHandler) {
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

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /lastdiamond <nickname>");
            return true;
        }

        String targetPlayerName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Player  " + targetPlayerName + " not found.");
            return true;
        }

        UUID targetPlayerId = targetPlayer.getUniqueId();

        long lastDiamondTime = eventHandler.getLastDiamondTime(targetPlayerId);

        if (lastDiamondTime == 0) {
            player.sendMessage(ChatColor.YELLOW + "Player  " + targetPlayerName + " has not mined any diamonds yet.");
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastDiamond = currentTime - lastDiamondTime;

        long minutes = timeSinceLastDiamond / (60 * 1000);
        long seconds = (timeSinceLastDiamond % (60 * 1000)) / 1000;

        player.sendMessage(ChatColor.GREEN + "Time since last mined diamond " +
                targetPlayerName + + minutes + " minutes  " + seconds + " seconds.");

        return true;
    }
}