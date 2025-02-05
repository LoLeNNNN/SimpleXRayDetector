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
            eventHandler = new sEventHandler(this);
            getServer().getLogger().info("XRayDetector enabled!");
            getServer().getPluginManager().registerEvents(eventHandler, this);
            getServer().getPluginCommand("suspiciousscore").setExecutor(new CurrentSuspiciousScoreCommand(eventHandler));
            getServer().getPluginCommand("resetsuspiciousscore").setExecutor(new ResetSuspiciousScoreCommand(eventHandler));
            getServer().getPluginCommand("lastdiamond").setExecutor(new LastDiamondCommand(eventHandler));
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
        } catch (Exception e) {
            getServer().getLogger().severe("Failed to load plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void onDisable() {
        getServer().getLogger().info("XRayDetector disabled!");
        if (eventHandler != null) {
            eventHandler.saveSuspiciousScores();
        }
    }

}