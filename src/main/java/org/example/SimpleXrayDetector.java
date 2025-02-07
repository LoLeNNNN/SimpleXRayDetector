package org.example;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.example.command.CurrentSuspiciousScoreCommand;
import org.example.command.LastDiamondCommand;
import org.example.command.ResetSuspiciousScoreCommand;
import org.example.handler.LocaleManager;
import org.example.handler.sEventHandler;

public class SimpleXrayDetector extends JavaPlugin implements Listener {

    private sEventHandler eventHandler;

    @Override
    public void onEnable() {
        LocaleManager localeManager = new LocaleManager(this);
        sEventHandler eventHandler = new sEventHandler(this, localeManager);
        getServer().getPluginManager().registerEvents(eventHandler, this);
        getCommand("suspiciousscore").setExecutor(new CurrentSuspiciousScoreCommand(eventHandler, localeManager));
        getCommand("resetsuspiciousscore").setExecutor(new ResetSuspiciousScoreCommand(eventHandler, localeManager));
        getCommand("lastdiamond").setExecutor(new LastDiamondCommand(eventHandler, localeManager));

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
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