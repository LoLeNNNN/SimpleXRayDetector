package org.example;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.example.command.CurrentSuspiciousScoreCommand;
import org.example.command.LastDiamondCommand;
import org.example.command.ResetSuspiciousScoreCommand;
import org.example.handler.sEventHandler;

public class SimpleXrayDetector extends JavaPlugin implements Listener {

    private sEventHandler eventHandler;

    @Override
    public void onEnable() {
        try {
            eventHandler = new sEventHandler(this); // Передаем экземпляр плагина
            getServer().getLogger().info("XRayDetector enabled!");

            // Регистрируем обработчик событий
            getServer().getPluginManager().registerEvents(eventHandler, this);

            // Регистрируем команду /suspiciousscore
            getServer().getPluginCommand("suspiciousscore").setExecutor(new CurrentSuspiciousScoreCommand(eventHandler));

            // Регистрируем команду /resetsuspiciousscore
            getServer().getPluginCommand("resetsuspiciousscore").setExecutor(new ResetSuspiciousScoreCommand(eventHandler));

            // Регистрируем команду /lastdiamond
            getServer().getPluginCommand("lastdiamond").setExecutor(new LastDiamondCommand(eventHandler));

            // Создаем папку для данных плагина, если она не существует
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
        } catch (Exception e) {
            getServer().getLogger().severe("Ошибка при запуске плагина: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("XRayDetector disabled!");
        if (eventHandler != null) { // Проверяем, что eventHandler был инициализирован
            eventHandler.saveSuspiciousScores(); // Сохраняем данные при остановке сервера
        }
    }

}