package com.binggre.rpgsharpscoreboard;

import com.binggre.rpgsharpscoreboard.listeners.CharacterListener;
import com.binggre.rpgsharpscoreboard.listeners.RPGSharpReloadListener;
import com.binggre.rpgsharpscoreboard.objects.RPGScoreboard;
import com.hj.rpgsharp.rpg.objects.RPGPlayer;
import com.hj.rpgsharp.rpg.objects.RPGPlayerLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public final class RPGSharpScoreboard extends JavaPlugin {

    private static RPGSharpScoreboard instance;

    public static RPGSharpScoreboard getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        RPGScoreboard.getInstance().loadJson();
        Bukkit.getPluginManager().registerEvents(new RPGSharpReloadListener(), this);
        Bukkit.getPluginManager().registerEvents(new CharacterListener(), this);
    }

    @Override
    public void onDisable() {
        for (RPGPlayer rpgPlayer : RPGPlayerLoader.getOnlineRPGPlayers()) {
            Scoreboard scoreboard = rpgPlayer.getPlayer().getScoreboard();
            Objective objective = scoreboard.getObjective(rpgPlayer.getNickname());
            if (objective == null) {
                continue;
            }
            objective.unregister();
        }
    }
}
