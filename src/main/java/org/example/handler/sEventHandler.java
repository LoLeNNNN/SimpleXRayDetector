package org.example.handler;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class sEventHandler implements Listener {
    private final Map<UUID, Long> suspiciousScores = new HashMap<>();
    private final Map<UUID, Location> lastBlockLocations = new HashMap<>();
    private final Map<UUID, Long> lastDiamondTime = new HashMap<>();
    private final JavaPlugin plugin;
    private final File scoresFile;
    private final YamlConfiguration scoresConfig;

    public sEventHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.scoresFile = new File(plugin.getDataFolder(), "suspicious_scores.yml");
        this.scoresConfig = YamlConfiguration.loadConfiguration(scoresFile);
        loadSuspiciousScores();
    }
    public Map<String, Long> getTopSuspiciousPlayers(int limit) {
        List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>();
        for (String key : scoresConfig.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            long score = scoresConfig.getLong(key + ".score");
            String playerName = scoresConfig.getString(key + ".name");

            if (playerName != null) {
                sortedEntries.add(new HashMap.SimpleEntry<>(playerName, score));
            }
        }
        sortedEntries.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));
        Map<String, Long> topPlayers = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : sortedEntries) {
            if (topPlayers.size() >= limit) {
                break;
            }
            topPlayers.put(entry.getKey(), entry.getValue());
        }
        return topPlayers;
    }
    public long getOfflineSuspiciousScore(String playerName) {
        for (String key : scoresConfig.getKeys(false)) {
            String storedPlayerName = scoresConfig.getString(key + ".name");
            if (storedPlayerName != null && storedPlayerName.equalsIgnoreCase(playerName)) {
                return scoresConfig.getLong(key + ".score");
            }
        }
        return -1;
    }
    private void loadSuspiciousScores() {
        if (!scoresFile.exists()) {
            plugin.saveResource("suspicious_scores.yml", false);
        }
        for (String key : scoresConfig.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(key);
                String playerName = scoresConfig.getString(key + ".name");
                long score = scoresConfig.getLong(key + ".score");

                // Загружаем в память
                suspiciousScores.put(playerId, score);
                lastDiamondTime.put(playerId, scoresConfig.getLong(key + ".lastDiamondTime", 0L)); // Загружаем время последнего алмаза
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in suspicious_scores.yml: " + key);
            }
        }
    }
    public void saveSuspiciousScores() {
        for (Map.Entry<UUID, Long> entry : suspiciousScores.entrySet()) {
            UUID playerId = entry.getKey();
            String playerName = Bukkit.getOfflinePlayer(playerId).getName();

            // Сохраняем данные
            scoresConfig.set(playerId.toString() + ".name", playerName);
            scoresConfig.set(playerId.toString() + ".score", entry.getValue());
            scoresConfig.set(playerId.toString() + ".lastDiamondTime", lastDiamondTime.get(playerId));
        }

        try {
            scoresConfig.save(scoresFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save suspicious_scores.yml.yml: " + e.getMessage());
        }
    }
    public long getCurrentSuspiciousScore(UUID playerId) {
        return suspiciousScores.getOrDefault(playerId, 0L);
    }

    // Обнуляет счетчик подозрительности игрока
    public void resetSuspiciousScore(UUID playerId) {
        suspiciousScores.put(playerId, 0L);
        saveSuspiciousScores();
    }


    public long getLastDiamondTime(UUID playerId) {
        return lastDiamondTime.getOrDefault(playerId, 0L);
    }


    private int calculateSuspiciousCoefficient(long timeSinceLastDiamond) {
        long minutes = timeSinceLastDiamond / (60 * 1000);

        if (minutes < 1) {
            return 15;
        } else if (minutes <= 15) {
            return 15 - (int) minutes;
        } else if (minutes <= 60) {
            return 10;
        } else {
            return 0;
        }
    }

    @EventHandler
    public void BlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();


        if (e.getBlock().getType() == Material.DIAMOND_ORE || e.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            Location currentBlockLocation = e.getBlock().getLocation();


            long currentTime = System.currentTimeMillis();
            long lastDiamond = lastDiamondTime.getOrDefault(playerId, 0L);
            long timeSinceLastDiamond = currentTime - lastDiamond;

            int coefficient = calculateSuspiciousCoefficient(timeSinceLastDiamond);

            Location lastBlockLocation = lastBlockLocations.get(playerId);


            if (lastBlockLocation != null) {
                double distance = lastBlockLocation.distance(currentBlockLocation);

                if (distance > 2) {
                    long currentScore = suspiciousScores.getOrDefault(playerId, 0L);
                    suspiciousScores.put(playerId, currentScore + coefficient);
                    saveSuspiciousScores();
                }
            }

            lastBlockLocations.put(playerId, currentBlockLocation);

            lastDiamondTime.put(playerId, currentTime);
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (player.hasPermission("simplexraydetector.admin")) {
            Map<String, Long> topPlayers = getTopSuspiciousPlayers(5);

            if (topPlayers.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "Suspicious top is empty.");
            } else {
                player.sendMessage(ChatColor.GOLD + "Top 5 suspicious players:");
                int position = 1;
                for (Map.Entry<String, Long> entry : topPlayers.entrySet()) {
                    player.sendMessage(ChatColor.GREEN + "" + position + ". " + entry.getKey() + ": " + entry.getValue());
                    position++;
                }
            }
        }
    }
}