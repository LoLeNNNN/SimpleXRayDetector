package org.example.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.handler.sEventHandler;
import org.example.handler.LocaleManager;

public class CurrentSuspiciousScoreCommand implements CommandExecutor {
    private final sEventHandler eventHandler;
    private final LocaleManager localeManager;

    public CurrentSuspiciousScoreCommand(sEventHandler eventHandler, LocaleManager localeManager) {
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

        if (args.length == 0) {
            long score = eventHandler.getCurrentSuspiciousScore(player.getUniqueId());
            player.sendMessage(localeManager.getMessage(player, "suspicious_score_self", score));
            return true;
        }

        if (!player.hasPermission("simplexraydetector.admin")) {
            player.sendMessage(localeManager.getMessage(player, "no_permission"));
            return true;
        }

        String targetPlayerName = args[0];
        long score = eventHandler.getOfflineSuspiciousScore(targetPlayerName);

        if (score == -1) {
            player.sendMessage(localeManager.getMessage(player, "player_not_found", targetPlayerName));
        } else {
            player.sendMessage(localeManager.getMessage(player, "suspicious_score_other", targetPlayerName, score));
        }

        return true;
    }
}