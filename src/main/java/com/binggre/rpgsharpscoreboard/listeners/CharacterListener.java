package com.binggre.rpgsharpscoreboard.listeners;

import com.binggre.rpgsharpscoreboard.RPGSharpScoreboard;
import com.binggre.rpgsharpscoreboard.scheduler.ScoreboardScheduler;
import com.hj.rpgsharp.rpg.apis.rpgsharp.events.character.CharacterLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class CharacterListener implements Listener {

    @EventHandler
    public void onCharacterLoad(CharacterLoadEvent event) {
        if (!ScoreboardScheduler.isStart()) {
            BukkitRunnable bukkitRunnable = new ScoreboardScheduler();
            bukkitRunnable.runTaskTimer(RPGSharpScoreboard.getInstance(), 0, 20);
        }
    }
}