package com.binggre.rpgsharpscoreboard.listeners;

import com.binggre.rpgsharpscoreboard.RPGSharpScoreboard;
import com.binggre.rpgsharpscoreboard.objects.RPGScoreboard;
import com.binggre.rpgsharpscoreboard.scheduler.ScoreboardScheduler;
import com.hj.rpgsharp.rpg.apis.rpgsharp.events.character.CharacterLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

public class CharacterListener implements Listener {

    @EventHandler
    public void onCharacterLoad(CharacterLoadEvent event) {
        RPGScoreboard rpgScoreboard = RPGScoreboard.getInstance();
        String nickname = event.getRPGPlayer().getNickname();
        if (!rpgScoreboard.getScoreboard().containsKey(nickname)) {
            Scoreboard scoreboard = rpgScoreboard.createNewScoreboard();
            scoreboard.registerNewObjective(nickname, rpgScoreboard.CRITERIA_NAME);
            rpgScoreboard.getScoreboard().put(nickname, scoreboard);
        }
        if (!ScoreboardScheduler.isStart()) {
            BukkitRunnable bukkitRunnable = new ScoreboardScheduler();
            bukkitRunnable.runTaskTimerAsynchronously(RPGSharpScoreboard.getInstance(), 0, 20);
        }
    }
}