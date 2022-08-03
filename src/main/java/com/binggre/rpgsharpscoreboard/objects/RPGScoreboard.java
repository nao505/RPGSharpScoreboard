package com.binggre.rpgsharpscoreboard.objects;

import com.binggre.rpgsharpscoreboard.RPGSharpScoreboard;
import com.binggre.rpgsharpskillplus.objects.PlayerSkillPlusLoader;
import com.google.gson.JsonObject;
import com.hj.rpgsharp.rpg.apis.rpgsharp.RPGSharpAPI;
import com.hj.rpgsharp.rpg.apis.rpgsharp.enums.skill.SkillKey;
import com.hj.rpgsharp.rpg.apis.rpgsharp.utils.FileUtil;
import com.hj.rpgsharp.rpg.apis.rpgsharp.utils.HexColorUtil;
import com.hj.rpgsharp.rpg.apis.rpgsharp.utils.ItemUtil;
import com.hj.rpgsharp.rpg.apis.rpgsharp.utils.NumberUtil;
import com.hj.rpgsharp.rpg.objects.RPGBuff;
import com.hj.rpgsharp.rpg.objects.RPGPlayer;
import com.hj.rpgsharp.rpg.plugins.party.objects.Party;
import com.hj.rpgsharp.rpg.plugins.skill.objects.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.hj.rpgsharp.rpg.apis.rpgsharp.RPGSharpAPI.getPartyAPI;
import static com.hj.rpgsharp.rpg.apis.rpgsharp.RPGSharpAPI.getRPGPlayerAPI;

public class RPGScoreboard {

    private static final RPGScoreboard instance = new RPGScoreboard();

    public static RPGScoreboard getInstance() {
        return instance;
    }

    private final String TEAM_NAME;
    private final String DELETE_KEY;

    private String TITLE;
    private String BUFF_PLACEHOLDER;
    private List<String> TEXT_CONTENTS;

    private RPGScoreboard() {
        this.TITLE = null;
        this.BUFF_PLACEHOLDER = null;
        this.TEXT_CONTENTS = new ArrayList<>();
        this.TEAM_NAME = "RPGSharpScoreboard";
        this.DELETE_KEY = "RPGSharpScoreboard_Delete_Key";
    }

    public void update(RPGPlayer rpgPlayer) {
        final String NICKNAME = rpgPlayer.getNickname();
        final Player PLAYER = rpgPlayer.getPlayer();

        final Scoreboard SCOREBOARD = createNewScoreboard();
        PLAYER.setScoreboard(SCOREBOARD);
        Objective objective = createObjective(SCOREBOARD, NICKNAME);
        setDisplayName(rpgPlayer, objective);
        reloadObjective(rpgPlayer, objective);
        PLAYER.setScoreboard(SCOREBOARD);
    }

    public Scoreboard createNewScoreboard() {
        return Bukkit.getScoreboardManager().getNewScoreboard();
    }

    private Objective createObjective(Scoreboard scoreboard, String nickname) {
        return scoreboard.registerNewObjective(nickname, TEAM_NAME);
    }

    private void setDisplayName(RPGPlayer rpgPlayer, Objective objective) {
        objective.setDisplayName(TITLE.replace("<player>", rpgPlayer.getNickname()));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private void reloadObjective(RPGPlayer rpgPlayer, Objective objective) {
        List<String> copyTextContents = new ArrayList<>(TEXT_CONTENTS);
        setContents(rpgPlayer, copyTextContents);
        int line = copyTextContents.size();
        for (String str : copyTextContents) {
            if (str.equals(DELETE_KEY)) {
                continue;
            }
            objective.getScore(str).setScore(line);
            line--;
        }
    }

    private void setContents(RPGPlayer rpgPlayer, List<String> textContents) {
        int line = -1;
        if (rpgPlayer.getRPGBuff().hasBuff()) {
            RPGBuff rpgBuff = rpgPlayer.getRPGBuff();
            List<String> buffs = rpgBuff.getBuffs();

            String buffName;
            int buffCooldown;
            String str;

            textContents.add("");
            for (String dataCode : buffs) {
                buffName = RPGSharpAPI.getRPGItemAPI().getRPGItem(dataCode).getFakeItem().getDisplayName();
                buffCooldown = rpgBuff.getBuffDuration(dataCode);
                str = BUFF_PLACEHOLDER.replace("<buff_name>", buffName).replace("<buff_cooldown>", buffCooldown + "");
                textContents.add(str);
            }
        }
        for (String str : textContents) {
            line++;
            if (str.contains("<nickname>")) {
                str = replace(str, "<nickname>", rpgPlayer.getNickname());
            }
            if (str.contains("<account_nickname>")) {
                str = replace(str, "<account_nickname>", rpgPlayer.getDefaultName());
            }
            if (str.contains("<job>")) {
                str = replace(str, "<job>", rpgPlayer.getJob(true));
            }
            if (str.contains("<title>")) {
                str = (rpgPlayer.getRPGTitle().isEquip()) ?
                        replace(str, "<title>", rpgPlayer.getRPGTitle().getTitle())
                        :
                        replace(str, "<title>", "없음");
            }
            if (str.contains("<level>")) {
                str = replace(str, "<level>", rpgPlayer.getLevel() + "");
            }
            if (str.contains("<exp>")) {
                double exp = NumberUtil.getPercentage(rpgPlayer.getExp(), rpgPlayer.getMaxExp());
                str = replace(str, "<exp>", NumberUtil.decimalFormat(exp, 1));
            }
            if (str.contains("<stat_point>")) {
                str = replace(str, "<stat_point>", rpgPlayer.getStatPoint() + "");
            }
            if (str.contains("<skill_point>") && isEnablePlugin("RPGSharpSkillPlus")) {
                str = (isEnablePlugin("RPGSharpSkillPlus")) ?
                        replace(str, "<skill_point>", PlayerSkillPlusLoader.get(rpgPlayer).getSkillPoint() + "")
                        :
                        DELETE_KEY;
            }
            textContents.set(line, str);
        }

    }

    private String replace(String value, String oldVar, String newVar) {
        return value.replace(oldVar, newVar);
    }

    private boolean isEnablePlugin(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    public void loadJson() {
        final String FILE_NAME = "scoreboard";
        final String FILE_FULL_NAME = FILE_NAME + ".json";
        final String FILE_PATH = RPGSharpAPI.getDirectory() + "\\" + FILE_FULL_NAME;
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            FileUtil.moveFile(RPGSharpScoreboard.class.getResourceAsStream("/" + FILE_FULL_NAME), FILE_PATH, false);
        }
        JsonObject json = FileUtil.read(file);
        assert (json != null);

        List<String> textContents = FileUtil.getStringList(json.get("내용").getAsJsonArray());
        ItemUtil.colored(textContents);

        TITLE = HexColorUtil.format(json.get("제목").getAsString().replace("&", "§"));
        BUFF_PLACEHOLDER = HexColorUtil.format(json.get("버프").getAsString().replace("&", "§"));
        TEXT_CONTENTS = textContents;
    }
}