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
import java.util.stream.Collectors;

public class sEventHandler implements Listener {
    private final Map<UUID, Long> suspiciousScores = new HashMap<>(); // Хранит счетчики подозрительности для каждого игрока
    private final Map<UUID, Location> lastBlockLocations = new HashMap<>(); // Хранит последний сломанный блок для каждого игрока
    private final Map<UUID, Long> lastDiamondTime = new HashMap<>(); // Хранит время последнего вскопанного алмаза
    private final JavaPlugin plugin; // Для работы с файлами и конфигурацией
    private final File scoresFile; // Файл suspicious_scores.yml
    private final YamlConfiguration scoresConfig; // Конфигурация для suspicious_scores.yml

    public sEventHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.scoresFile = new File(plugin.getDataFolder(), "suspicious_scores.yml"); // Указываем путь к файлу
        this.scoresConfig = YamlConfiguration.loadConfiguration(scoresFile); // Загружаем конфигурацию
        loadSuspiciousScores(); // Загружаем данные при создании экземпляра
    }

    public Map<String, Long> getTopSuspiciousPlayers(int limit) {
        // Создаем список для сортировки
        List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>();

        // Собираем данные из конфигурации
        for (String key : scoresConfig.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            long score = scoresConfig.getLong(key + ".score"); // Получаем счетчик
            String playerName = scoresConfig.getString(key + ".name"); // Получаем имя игрока

            if (playerName != null) {
                sortedEntries.add(new HashMap.SimpleEntry<>(playerName, score));
            }
        }

        // Сортируем по убыванию счетчика
        sortedEntries.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));

        // Создаем LinkedHashMap для сохранения порядка
        Map<String, Long> topPlayers = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : sortedEntries) {
            if (topPlayers.size() >= limit) {
                break; // Ограничиваем количество записей
            }
            topPlayers.put(entry.getKey(), entry.getValue());
        }

        return topPlayers;
    }

    public long getOfflineSuspiciousScore(String playerName) {
        for (String key : scoresConfig.getKeys(false)) {
            // Получаем имя игрока из конфигурации
            String storedPlayerName = scoresConfig.getString(key + ".name"); // Предполагаем, что имя хранится в ключе "name"
            if (storedPlayerName != null && storedPlayerName.equalsIgnoreCase(playerName)) {
                return scoresConfig.getLong(key + ".score"); // Предполагаем, что счетчик хранится в ключе "score"
            }
        }
        return -1; // Игрок не найден
    }

    private void loadSuspiciousScores() {
        if (!scoresFile.exists()) {
            plugin.saveResource("suspicious_scores.yml", false); // Создаем файл, если его нет
        }

        for (String key : scoresConfig.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(key); // Преобразуем строку в UUID
                String playerName = scoresConfig.getString(key + ".name"); // Получаем имя игрока
                long score = scoresConfig.getLong(key + ".score"); // Получаем счетчик

                // Загружаем в память
                suspiciousScores.put(playerId, score);
                lastDiamondTime.put(playerId, scoresConfig.getLong(key + ".lastDiamondTime", 0L)); // Загружаем время последнего алмаза
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Некорректный UUID в suspicious_scores.yml: " + key);
            }
        }
    }

    public void saveSuspiciousScores() {
        for (Map.Entry<UUID, Long> entry : suspiciousScores.entrySet()) {
            UUID playerId = entry.getKey();
            String playerName = Bukkit.getOfflinePlayer(playerId).getName();// Получаем имя игрока

            // Сохраняем данные
            scoresConfig.set(playerId.toString() + ".name", playerName);
            scoresConfig.set(playerId.toString() + ".score", entry.getValue());
            scoresConfig.set(playerId.toString() + ".lastDiamondTime", lastDiamondTime.get(playerId));
        }

        try {
            scoresConfig.save(scoresFile); // Сохраняем изменения в файл
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить suspicious_scores.yml: " + e.getMessage());
        }
    }

    // Возвращает текущий счетчик подозрительности игрока
    public long getCurrentSuspiciousScore(UUID playerId) {
        return suspiciousScores.getOrDefault(playerId, 0L); // Возвращаем 0, если счетчик отсутствует
    }

    // Обнуляет счетчик подозрительности игрока
    public void resetSuspiciousScore(UUID playerId) {
        suspiciousScores.put(playerId, 0L); // Обнуляем счетчик
        saveSuspiciousScores(); // Сохраняем изменения
    }

    // Возвращает время последнего вскопанного алмаза для игрока
    public long getLastDiamondTime(UUID playerId) {
        return lastDiamondTime.getOrDefault(playerId, 0L); // Возвращаем 0, если время не записано
    }

    // Рассчитывает коэффициент подозрительности на основе времени
    private int calculateSuspiciousCoefficient(long timeSinceLastDiamond) {
        long minutes = timeSinceLastDiamond / (60 * 1000); // Переводим миллисекунды в минуты

        if (minutes < 1) {
            return 15; // Менее 1 минуты
        } else if (minutes <= 15) {
            return 15 - (int) minutes; // От 1 до 15 минут
        } else if (minutes <= 60) {
            return 10; // От 15 минут до 1 часа
        } else {
            return 0; // Более 1 часа
        }
    }

    // Обработчик события BlockBreakEvent
    @EventHandler
    public void BlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer(); // Получаем игрока, который сломал блок
        UUID playerId = player.getUniqueId(); // Уникальный идентификатор игрока

        // Проверяем, что блок является алмазной рудой
        if (e.getBlock().getType() == Material.DIAMOND_ORE || e.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            Location currentBlockLocation = e.getBlock().getLocation(); // Координаты текущего блока

            // Получаем время последнего вскопанного алмаза
            long currentTime = System.currentTimeMillis();
            long lastDiamond = lastDiamondTime.getOrDefault(playerId, 0L);
            long timeSinceLastDiamond = currentTime - lastDiamond;

            // Рассчитываем коэффициент подозрительности
            int coefficient = calculateSuspiciousCoefficient(timeSinceLastDiamond);

            // Получаем координаты последнего сломанного блока для этого игрока
            Location lastBlockLocation = lastBlockLocations.get(playerId);

            // Если координаты последнего блока известны, проверяем расстояние
            if (lastBlockLocation != null) {
                double distance = lastBlockLocation.distance(currentBlockLocation); // Расстояние между блоками

                // Если расстояние больше 2 блоков, увеличиваем счетчик подозрительности
                if (distance > 2) {
                    long currentScore = suspiciousScores.getOrDefault(playerId, 0L);
                    suspiciousScores.put(playerId, currentScore + coefficient);
                    saveSuspiciousScores(); // Сохраняем изменения
                }
            }

            // Обновляем координаты последнего сломанного блока для этого игрока
            lastBlockLocations.put(playerId, currentBlockLocation);

            // Обновляем время последнего вскопанного алмаза
            lastDiamondTime.put(playerId, currentTime);
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // Проверяем, что игрок — администратор
        if (player.hasPermission("simplexraydetector.admin")) {
            Map<String, Long> topPlayers = getTopSuspiciousPlayers(5);

            if (topPlayers.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "Топ подозрительных игроков пуст.");
            } else {
                player.sendMessage(ChatColor.GOLD + "Топ-5 подозрительных игроков:");
                int position = 1;
                for (Map.Entry<String, Long> entry : topPlayers.entrySet()) {
                    player.sendMessage(ChatColor.GREEN + "" + position + ". " + entry.getKey() + ": " + entry.getValue());
                    position++;
                }
            }
        }
    }
}