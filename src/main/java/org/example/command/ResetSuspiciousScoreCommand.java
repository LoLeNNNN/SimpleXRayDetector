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
        // Проверяем, что отправитель команды — игрок
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только игрокам.");
            return true;
        }

        Player player = (Player) sender;

        // Проверяем права администратора
        if (!player.hasPermission("simplexraydetector.admin")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды.");
            return true;
        }

        // Определяем, для кого сбрасывать счетчик
        UUID targetPlayerId;
        String targetPlayerName;

        if (args.length == 0) {
            // Если аргумент не указан, сбрасываем счетчик себе
            targetPlayerId = player.getUniqueId();
            targetPlayerName = player.getName();
        } else {
            // Если указан ник игрока, сбрасываем счетчик для него
            String targetName = args[0];
            Player targetPlayer = Bukkit.getPlayer(targetName);

            if (targetPlayer == null) {
                player.sendMessage(ChatColor.RED + "Игрок " + targetName + " не найден или не в сети.");
                return true;
            }

            targetPlayerId = targetPlayer.getUniqueId();
            targetPlayerName = targetPlayer.getName();
        }

        // Сбрасываем счетчик подозрительности
        eventHandler.resetSuspiciousScore(targetPlayerId);
        player.sendMessage(ChatColor.GREEN + "Счетчик подозрительности игрока " + targetPlayerName + " обнулен.");

        return true;
    }
}