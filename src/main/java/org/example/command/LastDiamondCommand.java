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

        // Проверяем, что указан ник игрока
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Использование: /lastdiamond <ник игрока>");
            return true;
        }

        // Получаем ник целевого игрока
        String targetPlayerName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        // Проверяем, что целевой игрок онлайн
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Игрок " + targetPlayerName + " не найден или не в сети.");
            return true;
        }

        UUID targetPlayerId = targetPlayer.getUniqueId();

        // Получаем время последнего вскопанного алмаза
        long lastDiamondTime = eventHandler.getLastDiamondTime(targetPlayerId);

        // Если время не записано, выводим сообщение
        if (lastDiamondTime == 0) {
            player.sendMessage(ChatColor.YELLOW + "Игрок " + targetPlayerName + " еще не копал алмазы.");
            return true;
        }

        // Вычисляем время, прошедшее с момента последнего вскопанного алмаза
        long currentTime = System.currentTimeMillis();
        long timeSinceLastDiamond = currentTime - lastDiamondTime;

        // Преобразуем время в минуты и секунды
        long minutes = timeSinceLastDiamond / (60 * 1000);
        long seconds = (timeSinceLastDiamond % (60 * 1000)) / 1000;

        // Выводим сообщение
        player.sendMessage(ChatColor.GREEN + "С момента последнего вскопанного алмаза игрока " +
                targetPlayerName + " прошло: " + minutes + " минут " + seconds + " секунд.");

        return true;
    }
}