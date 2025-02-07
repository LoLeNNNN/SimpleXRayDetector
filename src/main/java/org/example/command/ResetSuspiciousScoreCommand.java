package org.example.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.handler.sEventHandler;
import org.example.handler.LocaleManager;

import java.util.UUID;

public class ResetSuspiciousScoreCommand implements CommandExecutor {
    private final sEventHandler eventHandler;
    private final LocaleManager localeManager;

    public ResetSuspiciousScoreCommand(sEventHandler eventHandler, LocaleManager localeManager) {
        this.eventHandler = eventHandler;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(localeManager.getMessage(null, "usage_resetsuspiciousscore"));
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is available only for players.");
            return true;
        }
        String targetPlayerName = args[0];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName); // Работает с оффлайн-игроками

        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
            sender.sendMessage(localeManager.getMessage(null, "player_not_found", targetPlayerName));
            return true;
        }

        UUID targetPlayerId = targetPlayer.getUniqueId();

        eventHandler.resetSuspiciousScore(targetPlayerId);

        if (sender instanceof Player) {
            sender.sendMessage(localeManager.getMessage((Player) sender, "score_reset", targetPlayerName));
        } else {
            sender.sendMessage(localeManager.getMessage(null, "score_reset", targetPlayerName));
        }

        return true;
    }
}