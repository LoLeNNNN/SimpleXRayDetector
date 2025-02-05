package org.example.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
        // Проверяем, что отправитель команды — игрок
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только игрокам.");
            return true;
        }

        Player player = (Player) sender;

        // Если аргументов нет, показываем счетчик подозрительности отправителя
        if (args.length == 0) {
            UUID playerId = player.getUniqueId();
            long score = eventHandler.getCurrentSuspiciousScore(playerId);
            player.sendMessage(ChatColor.RED + "У вас нет прав для просмотра счетчика других игроков.");
            return true;
        }

        // Если аргумент есть, проверяем права администратора
        if (!player.hasPermission("simplexraydetector.admin")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для просмотра счетчика других игроков.");
            return true;
        }

        // Получаем ник игрока из аргумента
        String targetPlayerName = args[0];

        // Проверяем, онлайн ли игрок
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        long score;

        if (targetPlayer != null) {
            // Если игрок онлайн, получаем его счетчик
            score = eventHandler.getCurrentSuspiciousScore(targetPlayer.getUniqueId());
        } else {
            // Если игрок оффлайн, ищем его в файле
            score = eventHandler.getOfflineSuspiciousScore(targetPlayerName);

            if (score == -1) {
                player.sendMessage(ChatColor.RED + "Игрок " + targetPlayerName + " не найден.");
                return true;
            }
        }

        // Выводим счетчик подозрительности
        player.sendMessage(ChatColor.DARK_GREEN + "Счетчик подозрительности игрока " + targetPlayerName + ": " + score);

        return true;
    }
}