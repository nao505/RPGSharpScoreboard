package com.binggre.rpgsharpscoreboard.listeners;

import com.binggre.rpgsharpscoreboard.objects.RPGScoreboard;
import com.hj.rpgsharp.rpg.apis.rpgsharp.events.RPGSharpReloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RPGSharpReloadListener implements Listener {

    @EventHandler
    public void onRPGSharpReload(RPGSharpReloadEvent event) {
        RPGScoreboard.getInstance().loadJson();
    }
}