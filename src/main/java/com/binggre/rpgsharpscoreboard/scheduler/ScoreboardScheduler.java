package com.binggre.rpgsharpscoreboard.scheduler;

import com.binggre.rpgsharpscoreboard.objects.RPGScoreboard;
import com.hj.rpgsharp.rpg.apis.rpgsharp.RPGSharpAPI;
import com.hj.rpgsharp.rpg.apis.rpgsharp.utils.NumberUtil;
import com.hj.rpgsharp.rpg.objects.RPGPlayer;
import com.hj.rpgsharp.rpg.objects.RPGPlayerLoader;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ScoreboardScheduler extends BukkitRunnable {

    private static boolean start = false;

    public static boolean isStart() {
        return start;
    }

    public static void setStart(boolean start) {
        ScoreboardScheduler.start = start;
    }

    private final RPGScoreboard rpgScoreboard;

    public ScoreboardScheduler() {
        this.rpgScoreboard = RPGScoreboard.getInstance();
        setStart(true);
    }

    @Override
    public void run() {
        List<RPGPlayer> onlineRPGPlayers = RPGSharpAPI.getRPGPlayerAPI().getOnlineRPGPlayers();
        if (onlineRPGPlayers.isEmpty()) {
            setStart(false);
            cancel();
            return;
        }
        onlineRPGPlayers.forEach(rpgScoreboard::update);
    }
}